/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.survey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Hands the corpus's documents to worker JVMs, a slot thread for each, and appends their rows to
 * the TSVs. Resumable: a document whose row is already in docx4j_load.tsv is skipped. A worker
 * that dies on a document gets it once more in a fresh JVM; a worker that stops answering is
 * killed and the document recorded as a timeout.
 */
final class Coordinator {

	static final String MAIN = "docx4j_load.tsv";
	static final String MESSAGES = "docx4j_load_messages.tsv";

	/** Beyond the worker's own timeout, before the coordinator gives up on it (a JVM thrashing in GC). */
	static final long HARD_SLACK_MS = 60_000;
	static final long START_TIMEOUT_MS = 180_000;
	/** Each worker slot's log of standard error stops growing here. */
	static final long LOG_CAP = 100L << 20;

	private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final LoadSurvey.Options opt;
	private final Path out;
	private final Path workers;
	private final Path mainTsv;
	private final Path msgTsv;

	private DoneSet done;
	private Iterator<Path> source;
	private Stream<Path> walk;
	private BufferedReader list;
	private BufferedWriter mainW;
	private BufferedWriter msgW;
	private final Object writeLock = new Object();
	private final AtomicInteger dispatched = new AtomicInteger();
	private final AtomicInteger skipped = new AtomicInteger();
	private final AtomicInteger written = new AtomicInteger();
	private final Map<String, AtomicInteger> outcomes = new ConcurrentHashMap<>();
	private final Set<Child> live = ConcurrentHashMap.newKeySet();
	private volatile String fatal;
	private long started;

	Coordinator(LoadSurvey.Options opt) {
		this.opt = opt;
		this.out = opt.out.toAbsolutePath().normalize();
		this.workers = out.resolve("workers");
		this.mainTsv = out.resolve(MAIN);
		this.msgTsv = out.resolve(MESSAGES);
	}

	int run() throws Exception {
		Files.createDirectories(out);
		Path outReal = out.toRealPath();
		if (opt.corpus != null) {
			Path corpus = opt.corpus.toRealPath();
			if (outReal.startsWith(corpus)) {
				throw new IllegalArgumentException("--out " + outReal + " is inside the corpus " + corpus + ": the survey writes nothing there");
			}
		}
		Files.createDirectories(workers.resolve("tmp"));

		long cut = Tsv.repairTail(mainTsv) + Tsv.repairTail(msgTsv);
		if (cut > 0) say("cut " + cut + " bytes of an unfinished line off the TSVs (a run stopped mid-write)");
		checkHeader(mainTsv, Column.names());
		checkHeader(msgTsv, Column.Message.names());
		done = DoneSet.read(mainTsv);
		int orphans = Tsv.dropOrphanMessages(msgTsv, done);
		if (orphans > 0) say("dropped " + orphans + " message rows of documents with no row yet (they are surveyed again)");
		mainW = open(mainTsv, Column.names());
		msgW = open(msgTsv, Column.Message.names());
		openSource();

		say("docx4j load survey: docx4j " + LoadSurvey.docx4jVersion() + " on Java " + System.getProperty("java.version")
				+ " (" + System.getProperty("os.arch") + "); " + opt.workers + " workers, " + opt.heap + " heap each, "
				+ opt.timeoutS + " s timeout" + (opt.save ? ", with --save" : "") + (opt.limit > 0 ? ", at most " + opt.limit + " documents" : ""));
		say("source " + (opt.list != null ? "list " + opt.list : "corpus " + opt.corpus) + "; output " + out + "; " + done.size() + " documents already done");
		if (Runtime.version().feature() >= 25) {
			say("note: Java 25's XML parsers refuse elements deeper than 100, which docx4j "
					+ LoadSurvey.docx4jVersion() + " reads on Java 17 and 21 (docx4j sets its own limit from 17.3.6)");
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (Child c : live) c.kill();
		}, "kill workers"));

		started = System.nanoTime();
		List<Thread> slots = new ArrayList<>();
		for (int i = 1; i <= opt.workers; i++) {
			Thread t = new Thread(new Slot(i), "slot " + i);
			t.start();
			slots.add(t);
		}
		Thread progress = new Thread(() -> {
			try {
				while (true) {
					Thread.sleep(opt.progressS * 1000L);
					say(progress());
				}
			} catch (InterruptedException e) {
				// done
			}
		}, "progress");
		progress.setDaemon(true);
		progress.start();
		for (Thread t : slots) t.join();
		progress.interrupt();
		say(progress());
		mainW.close();
		msgW.close();
		closeSource();

		String summary = Summary.compute(out);
		Files.write(out.resolve("summary.txt"), summary.getBytes(StandardCharsets.UTF_8));
		ImportSql.write(out);
		System.out.println();
		System.out.print(summary);
		System.out.println();
		System.out.println("summary: " + out.resolve("summary.txt") + "; into the catalogue: sqlite3 CATALOGUE.db < " + out.resolve("import.sql"));
		if (fatal != null) {
			say("STOPPED: " + fatal);
			return 2;
		}
		return 0;
	}

	private static void checkHeader(Path p, String[] names) throws IOException {
		String h = Tsv.header(p);
		if (h != null && !h.equals(String.join("\t", names))) {
			throw new IllegalStateException(p + " holds other columns (another version of the survey wrote it): use a new --out");
		}
	}

	private static BufferedWriter open(Path p, String[] names) throws IOException {
		boolean fresh = Tsv.header(p) == null;
		BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		if (fresh) {
			w.write(String.join("\t", names));
			w.write('\n');
			w.flush();
		}
		return w;
	}

	// --- the documents ---------------------------------------------------------------------------

	private void openSource() throws IOException {
		if (opt.list != null) {
			list = Files.newBufferedReader(opt.list, StandardCharsets.UTF_8);
			source = new Iterator<Path>() {
				private Path next = advance();

				private Path advance() {
					try {
						String l;
						while ((l = list.readLine()) != null) {
							l = l.trim();
							if (!l.isEmpty() && !l.startsWith("#")) return Paths.get(l).toAbsolutePath().normalize();
						}
						return null;
					} catch (IOException e) {
						throw new java.io.UncheckedIOException(e);
					}
				}

				@Override
				public boolean hasNext() {
					return next != null;
				}

				@Override
				public Path next() {
					Path p = next;
					next = advance();
					return p;
				}
			};
		} else {
			walk = Files.walk(opt.corpus.toAbsolutePath().normalize()); // links are not followed
			source = walk.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS) && isWordprocessingName(p)).iterator();
		}
	}

	private void closeSource() throws IOException {
		if (walk != null) walk.close();
		if (list != null) list.close();
	}

	static boolean isWordprocessingName(Path p) {
		String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
		return n.endsWith(".docx") || n.endsWith(".docm") || n.endsWith(".dotx") || n.endsWith(".dotm");
	}

	static final class Item {
		final String sha;
		final Path path;

		Item(String sha, Path path) {
			this.sha = sha;
			this.path = path;
		}
	}

	private synchronized Item next() throws IOException {
		while (fatal == null && (opt.limit <= 0 || dispatched.get() < opt.limit) && source.hasNext()) {
			Path p = source.next();
			if (p.toString().indexOf('\t') >= 0 || p.toString().indexOf('\n') >= 0 || p.toString().indexOf('\r') >= 0) {
				say("skipped, a tab or line break in its path: " + Tsv.clean(p));
				continue;
			}
			String sha = shaOf(p);
			if (done.contains(sha)) {
				skipped.incrementAndGet();
				continue;
			}
			dispatched.incrementAndGet();
			return new Item(sha, p);
		}
		return null;
	}

	/** The name, where it is a SHA-256 (the corpus's naming); otherwise the content's. */
	static String shaOf(Path p) throws IOException {
		String n = p.getFileName().toString();
		int dot = n.lastIndexOf('.');
		String stem = dot < 0 ? n : n.substring(0, dot);
		if (SHA256.matcher(stem).matches()) return stem;
		try (InputStream is = Files.newInputStream(p)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buf = new byte[65536];
			int k;
			while ((k = is.read(buf)) > 0) md.update(buf, 0, k);
			StringBuilder sb = new StringBuilder(64);
			for (byte b : md.digest()) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	// --- writing ---------------------------------------------------------------------------------

	private void write(List<String> messages, Object[] row) throws IOException {
		synchronized (writeLock) {
			for (String m : messages) {
				msgW.write(m);
				msgW.write('\n');
			}
			msgW.flush(); // the messages before the row: a row is the document's mark of done
			mainW.write(Tsv.row(row));
			mainW.write('\n');
			mainW.flush();
		}
		written.incrementAndGet();
		outcomes.computeIfAbsent(String.valueOf(row[Column.OUTCOME.ordinal()]), k -> new AtomicInteger()).incrementAndGet();
	}

	/** A row the coordinator writes itself, when the worker gave none. */
	private static Object[] synthetic(Item it, String outcome, String exception, String notes) {
		Result r = new Result();
		r.set(Column.SHA256, it.sha);
		r.set(Column.DOCX4J_VERSION, LoadSurvey.docx4jVersion());
		r.set(Column.OUTCOME, outcome);
		r.set(Column.EXCEPTION, Describe.line(exception));
		if (notes != null) r.note(notes);
		try {
			r.set(Column.BYTES, Files.size(it.path));
		} catch (IOException e) {
			// left NULL
		}
		return r.values();
	}

	private String progress() {
		double s = (System.nanoTime() - started) / 1e9;
		int n = written.get();
		Map<String, Integer> o = new TreeMap<>();
		for (Map.Entry<String, AtomicInteger> e : outcomes.entrySet()) o.put(e.getKey(), e.getValue().get());
		return String.format(Locale.ROOT, "%,d surveyed this run in %.0f s (%.1f a second; %,d skipped as done) %s",
				n, s, s > 0 ? n / s : 0.0, skipped.get(), o);
	}

	private void fail(String why) {
		if (fatal == null) fatal = why;
		say("FATAL " + why);
	}

	static synchronized void say(String s) {
		System.out.println(LocalTime.now().format(CLOCK) + "  " + s);
		System.out.flush();
	}

	// --- the workers -----------------------------------------------------------------------------

	private final class Slot implements Runnable {
		private final int id;
		private Child child;

		Slot(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			try {
				Item it;
				while (fatal == null && (it = next()) != null) process(it);
			} catch (Throwable e) {
				fail("slot " + id + ": " + Describe.withRoot(e));
			} finally {
				if (child != null) child.close();
			}
		}

		private void process(Item it) throws Exception {
			for (int attempt = 1;; attempt++) {
				if (child == null || !child.alive()) {
					if (child != null) child.close();
					child = start();
				}
				Reply reply = child.survey(it, opt.timeoutS * 1000L + HARD_SLACK_MS);
				if (reply.row != null) {
					Object[] row = reply.row;
					if (attempt > 1) {
						String n = (String) row[Column.NOTES.ordinal()];
						String again = "surveyed again after the worker died on it";
						row[Column.NOTES.ordinal()] = n == null || n.isEmpty() ? again : n + "; " + again;
					}
					write(reply.messages, row);
					if (reply.exits) {
						child.close();
						child = null;
					}
					return;
				}
				if (reply.hung) {
					child.kill();
					child.close();
					child = null;
					write(Collections.emptyList(), synthetic(it, "timeout",
							"timeout: the worker did not answer within " + (opt.timeoutS + HARD_SLACK_MS / 1000) + " s and was killed", null));
					return;
				}
				// the worker died without a row
				String why = "the worker JVM exited (code " + child.exitCode() + ")";
				String tail = child.errorTail();
				child.close();
				child = null;
				if (attempt == 1) continue;
				write(Collections.emptyList(), synthetic(it, "crashed", why + " on this document, twice", "its last error lines: " + tail));
				return;
			}
		}

		private Child start() throws IOException, InterruptedException {
			IOException last = null;
			for (int i = 0; i < 3; i++) {
				Child c = new Child(id);
				try {
					c.awaitReady();
					return c;
				} catch (IOException e) {
					last = e;
					c.kill();
					c.close();
				}
			}
			throw new IOException("worker " + id + " would not start", last);
		}
	}

	static final class Reply {
		final List<String> messages = new ArrayList<>();
		Object[] row;
		boolean exits;
		boolean hung;
	}

	private static final String EOF = new String("end of the worker's output");

	private final class Child {
		private final Process proc;
		private final BufferedWriter stdin;
		private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
		private final Deque<String> errTail = new ArrayDeque<>();

		Child(int slot) throws IOException {
			List<String> cmd = new ArrayList<>();
			cmd.add(opt.java);
			cmd.add("-Xmx" + opt.heap);
			cmd.add("-XX:+UseSerialGC"); // many JVMs side by side: one GC thread each
			cmd.add("-XX:-UsePerfData");
			cmd.add("-Djava.io.tmpdir=" + workers.resolve("tmp"));
			cmd.add("-cp");
			cmd.add(absoluteClassPath());
			cmd.add(LoadSurvey.class.getName());
			cmd.add("worker");
			cmd.add("--timeout");
			cmd.add(String.valueOf(opt.timeoutS));
			if (opt.save) cmd.add("--save");
			proc = new ProcessBuilder(cmd).directory(workers.toFile()).start();
			live.add(this);
			stdin = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8));
			Thread o = new Thread(() -> {
				try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
					String l;
					while ((l = r.readLine()) != null) lines.add(l);
				} catch (IOException e) {
					// the worker is gone
				} finally {
					lines.add(EOF);
				}
			}, "worker " + slot + " out");
			o.setDaemon(true);
			o.start();
			Path log = workers.resolve("worker-" + slot + ".log");
			Thread e = new Thread(() -> drain(log), "worker " + slot + " err");
			e.setDaemon(true);
			e.start();
		}

		private void drain(Path log) {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8));
					OutputStream w = Files.newOutputStream(log, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
				long size = Files.size(log);
				String l;
				while ((l = r.readLine()) != null) {
					tail(l);
					if (size < LOG_CAP) {
						byte[] b = (l + "\n").getBytes(StandardCharsets.UTF_8);
						w.write(b);
						size += b.length;
					}
				}
			} catch (IOException e) {
				// the worker is gone
			}
		}

		void awaitReady() throws IOException, InterruptedException {
			long deadline = System.currentTimeMillis() + START_TIMEOUT_MS;
			while (true) {
				String l = lines.poll(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
				if (l == null) throw new IOException("no READY within " + START_TIMEOUT_MS / 1000 + " s: " + errorTail());
				if (l == EOF) throw new IOException("exited before READY (code " + exitCode() + "): " + errorTail());
				if ("READY".equals(l)) return;
			}
		}

		Reply survey(Item it, long hardMs) throws InterruptedException {
			Reply rep = new Reply();
			try {
				stdin.write(it.sha + "\t" + it.path + "\n");
				stdin.flush();
			} catch (IOException e) {
				return rep; // died: no row
			}
			long deadline = System.currentTimeMillis() + hardMs;
			while (true) {
				long rem = deadline - System.currentTimeMillis();
				String l = rem > 0 ? lines.poll(rem, TimeUnit.MILLISECONDS) : null;
				if (l == null) {
					rep.hung = true;
					return rep;
				}
				if (l == EOF) return rep;
				if (l.startsWith("M\t")) {
					rep.messages.add(l.substring(2));
				} else if (l.startsWith("R\t")) {
					String[] f = Tsv.split(l.substring(2));
					Object[] row = new Object[Column.values().length];
					for (int i = 0; i < row.length && i < f.length; i++) row[i] = f[i].isEmpty() ? null : f[i];
					rep.row = row;
					String outcome = String.valueOf(row[Column.OUTCOME.ordinal()]);
					rep.exits = outcome.equals("timeout") || outcome.equals("out_of_memory") || outcome.equals("crashed");
					return rep;
				} else {
					tail(l); // the JVM's own last words go to standard output ("Terminating due to ...")
				}
			}
		}

		boolean alive() {
			return proc.isAlive();
		}

		void kill() {
			proc.destroyForcibly();
		}

		int exitCode() {
			try {
				proc.waitFor(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return proc.isAlive() ? -1 : proc.exitValue();
		}

		private void tail(String l) {
			synchronized (errTail) {
				errTail.addLast(l);
				if (errTail.size() > 5) errTail.removeFirst();
			}
		}

		String errorTail() {
			synchronized (errTail) {
				return String.join(" / ", errTail);
			}
		}

		void close() {
			try {
				stdin.close();
			} catch (IOException e) {
				// gone already
			}
			try {
				if (!proc.waitFor(30, TimeUnit.SECONDS)) proc.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				proc.destroyForcibly();
				Thread.currentThread().interrupt();
			}
			live.remove(this);
		}
	}

	/** The class path, each entry absolute: a worker runs in the workers directory. */
	static String absoluteClassPath() {
		StringBuilder sb = new StringBuilder();
		for (String e : System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator))) {
			if (e.isEmpty()) continue;
			if (sb.length() > 0) sb.append(File.pathSeparator);
			sb.append(Paths.get(e).toAbsolutePath().normalize());
		}
		return sb.toString();
	}
}
