package org.docx4j.fidelity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fidelity.compare.LayoutComparison;
import org.docx4j.fidelity.compare.PixelComparison;
import org.docx4j.fidelity.corpus.Corpus;
import org.docx4j.fidelity.extract.LayoutCache;
import org.docx4j.fidelity.extract.PdfLayout;
import org.docx4j.fidelity.extract.PdfLayoutExtractor;
import org.docx4j.fidelity.report.HtmlReport;
import org.docx4j.fidelity.score.DocumentClass;
import org.docx4j.fidelity.score.Scoreboard;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Command line entry point.
 *
 * <pre>
 *   generate &lt;corpusDir&gt;                              write the probe docx files
 *   render   &lt;corpusDir&gt; &lt;pdfDir&gt;                     docx4j+FOP: one PDF (and .fo) per docx
 *   compare  &lt;refPdfDir&gt; &lt;candPdfDir&gt; &lt;reportDir&gt; [dpi]  compare PDFs with the same basename, write report
 *   run      &lt;corpusDir&gt; &lt;refPdfDir&gt; &lt;reportDir&gt; [dpi]  render into reportDir/fop, then compare
 *   score    &lt;corpusDir&gt; &lt;refPdfDir&gt; &lt;outDir&gt; [baseline.csv]  score a large corpus, CSV + delta
 *   rescore  &lt;corpusDir&gt; &lt;refPdfDir&gt; &lt;candPdfDir&gt; &lt;outDir&gt; [baseline.csv]  score existing renders
 * </pre>
 */
public final class Fidelity {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			usage();
			return;
		}
		// -Dfidelity.hyphenate=true|false overrides the documents' hyphenation setting
		// (docx4j.convert.out.fo.hyphenate): a golden made on a machine without the
		// language's proofing tools is unhyphenated whatever the document asks for
		String hyphenate = System.getProperty("fidelity.hyphenate");
		if (hyphenate != null) {
			org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.hyphenate", hyphenate.trim());
		}
		// which FO renderer this classpath carries (Apache FOP, or the docx4j FO renderer and
		// its hooks): logged once here, and written into every scoreboard.txt (CR-020)
		org.docx4j.fidelity.score.Scoreboard.renderer = org.docx4j.convert.out.fo.FopCapabilities.get().describe();
		// real documents are not zip bombs: a 668KB docx whose EMF images unpack to
		// 95MB trips docx4j's 50MB default, so the guard is loosened for scoring
		// (must run before ZipPartStore is loaded, which reads it once)
		if (System.getProperty("fidelity.maxUncompressed") == null) {
			org.docx4j.Docx4jProperties.setProperty(
					"docx4j.openpackaging.package.MAX_UNCOMPRESSED_SIZE.unzip.error", "1073741824");
		}
		// -Dfidelity.fonts=all|jars|<dir> (CR-016 phase 0c): the physical fonts the run may
		// use.  "jars" is docx4j's own font jars alone (the headless-container deployment:
		// ubuntu/debian/fedora/alpine images ship no fonts at all, measured 2026-09-12);
		// a directory is a distribution's stock set extracted from its image, added
		// explicitly (the system font directories are not searched), plus the jars.
		String fontSet = System.getProperty("fidelity.fonts", "all").trim();
		if (!fontSet.equals("all")) {
			org.docx4j.Docx4jProperties.setProperty("docx4j.fonts.discoverPhysicalFonts.enabled", "false");
		}
		// any -Ddocx4j.xxx=yyy on the command line becomes a docx4j property, so a
		// behaviour toggle can be measured without editing docx4j.properties
		for (String name : System.getProperties().stringPropertyNames()) {
			if (name.startsWith("docx4j.")) {
				org.docx4j.Docx4jProperties.setProperty(name, System.getProperty(name));
			}
		}
		fontEnvironment(fontSet);
		switch (args[0]) {
		case "generate":
			Corpus.generate(new File(args[1]));
			break;
		case "render":
			render(new File(args[1]), new File(args[2]));
			break;
		case "compare":
			compare(new File(args[1]), new File(args[2]), new File(args[3]), args.length > 4 ? Integer.parseInt(args[4]) : 100);
			break;
		case "run": {
			File report = new File(args[3]);
			File fop = new File(report, "fop");
			render(new File(args[1]), fop);
			compare(new File(args[2]), fop, report, args.length > 4 ? Integer.parseInt(args[4]) : 100);
			break;
		}
		case "score":
			score(new File(args[1]), new File(args[2]), new File(args[3]),
					args.length > 4 ? new File(args[4]) : null);
			break;
		case "scoremany":
			scoreMany(Arrays.copyOfRange(args, 1, args.length));
			break;
		case "rescore":
			rescore(new File(args[1]), new File(args[2]), new File(args[3]), new File(args[4]),
					args.length > 5 ? new File(args[5]) : null);
			break;
		default:
			usage();
		}
	}

	private static void usage() {
		System.out.println("usage: generate <corpusDir> | render <corpusDir> <pdfDir> | compare <refPdfDir> <candPdfDir> <reportDir> [dpi] | run <corpusDir> <refPdfDir> <reportDir> [dpi]");
		System.out.println("       score <corpusDir> <refPdfDir> <outDir> [baseline.csv]");
		System.out.println("             renders and compares every docx that has a <id>.pdf in refPdfDir,");
		System.out.println("             one row per document; survives failures, writes outDir/scoreboard.csv");
		System.out.println("             and outDir/scoreboard.txt, and diffs against a previous scoreboard.csv.");
		System.out.println("       scoremany (<log> <corpusDir> <refPdfDir> <outDir> <baseline.csv|->)...");
		System.out.println("             score several corpora in one JVM, their documents in one pool; a log each.");
		System.out.println("       rescore <corpusDir> <refPdfDir> <candPdfDir> <outDir> [baseline.csv]");
		System.out.println("             score without rendering: candPdfDir is a previous run's fop directory, so a");
		System.out.println("             change to the extractor or the pairing can be re-baselined on the old renders.");
		System.out.println("       -Dfidelity.only=id,id       restricts render/compare/run/score to those documents");
		System.out.println("       -Dfidelity.timeoutSeconds=N per-document conversion timeout in score (default 120)");
		System.out.println("       -Dfidelity.threads=N        documents scored at once (default 12; 1 = sequential); same files");
		System.out.println("       -Dfidelity.layoutCache=dir|off  where Word's PDFs' layouts are kept (default ~/.cache/docx4j-fidelity/layouts)");
		System.out.println("       -Ddocx4j.xxx=yyy            sets that docx4j property, to measure a behaviour toggle");
	}

	/**
	 * Establish the font set (see main) and record it: discovery runs in the Mapper's
	 * static initialiser, so a Mapper is touched first; a directory's fonts are then
	 * added one by one.  The inventory is printed so that every scoring log says what
	 * fonts the run had.  @since CR-016 phase 0c
	 */
	static void fontEnvironment(String fontSet) throws Exception {
		new org.docx4j.fonts.IdentityPlusMapper(); // triggers discovery (jars, and the system unless disabled)
		if (!fontSet.equals("all") && !fontSet.equals("jars")) {
			// the walk is docx4j-core's since 17.2.0, so the harness and FontsAnalysis
			// build a font environment the same way (CR-017 phase 3)
			File dir = new File(fontSet);
			if (!dir.isDirectory()) throw new IllegalArgumentException("-Dfidelity.fonts: not a directory: " + dir);
			org.docx4j.fonts.FontEnvironment.ofDirectory(dir);
		}
		java.util.TreeSet<String> names = new java.util.TreeSet<>(org.docx4j.fonts.PhysicalFonts.getPhysicalFonts().keySet());
		fontSetLine = "font set '" + fontSet + "': " + names.size() + " physical fonts; mapper " + mapperName();
		System.out.println(fontSetLine);
		if (System.getProperty("fidelity.listFonts") != null) {
			for (String n : names) System.out.println("  " + n);
		}
	}

	/** The font set's line, which scoremany writes at the head of each corpus's log as well. */
	private static volatile String fontSetLine;

	/** -Dfidelity.fontMapper=identity|best (CR-016 phase 0c): identity is docx4j's default
	 *  (the package creates an IdentityPlusMapper when none is set), and is what every score
	 *  before 2026-09-12 measured. */
	static String mapperName() {
		return System.getProperty("fidelity.fontMapper", "identity").trim().toLowerCase();
	}

	static void applyFontMapper(WordprocessingMLPackage pkg) throws Exception {
		if (mapperName().equals("best")) {
			pkg.setFontMapper(new org.docx4j.fonts.BestMatchingMapper());
		} else if (!mapperName().equals("identity")) {
			throw new IllegalArgumentException("-Dfidelity.fontMapper: identity or best, not " + mapperName());
		}
	}

	public static void render(File corpusDir, File pdfDir) throws Exception {
		pdfDir.mkdirs();
		for (File docx : docxFiles(corpusDir)) {
			String id = docx.getName().replaceAll("\\.docx$", "");
			renderOne(docx, pdfDir, id);
			System.out.println("rendered " + id);
		}
	}

	/**
	 * docx4j+FOP: writes pdfDir/id.fo and pdfDir/id.pdf.
	 *
	 * @return the package the PDF was rendered from, <em>after</em> the export, so that
	 *         its {@link org.docx4j.fonts.Mapper} holds the decisions this render made
	 *         and {@link org.docx4j.fonts.FontsAnalysis} can be asked which families it
	 *         substituted.  Asking a freshly loaded package instead would be asking a
	 *         mapper no conversion had populated.
	 */
	static WordprocessingMLPackage renderOne(File docx, File pdfDir, String id) throws Exception {
		WordprocessingMLPackage pkg = Docx4J.load(docx);
		applyFontMapper(pkg);
		FOSettings fo = Docx4J.createFOSettings();
		fo.setOpcPackage(pkg);
		fo.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		try (FileOutputStream os = new FileOutputStream(new File(pdfDir, id + ".fo"))) {
			Docx4J.toFO(fo, os, Docx4J.FLAG_NONE);
		}
		// Explicitly the FO/FOP pathway: Docx4J.toPDF would silently prefer documents4j
		// (Word) when docx4j-documents4j-local is on the classpath, as it is here.
		pkg = Docx4J.load(docx);
		applyFontMapper(pkg);
		FOSettings pdf = Docx4J.createFOSettings();
		pdf.setOpcPackage(pkg);
		try (FileOutputStream os = new FileOutputStream(new File(pdfDir, id + ".pdf"))) {
			Docx4J.toFO(pdf, os, Docx4J.FLAG_NONE);
		}
		return pkg;
	}

	public static List<LayoutComparison.Result> compare(File refDir, File candDir, File reportDir, int dpi) throws Exception {
		List<LayoutComparison.Result> results = new ArrayList<>();
		File[] refs = refDir.listFiles((d, n) -> n.endsWith(".pdf") && selected(n));
		if (refs == null) throw new IllegalArgumentException("no PDFs in " + refDir);
		Arrays.sort(refs);
		for (File ref : refs) {
			File cand = new File(candDir, ref.getName());
			if (!cand.exists()) {
				System.out.println("skip " + ref.getName() + ": no candidate");
				continue;
			}
			String id = ref.getName().replaceAll("\\.pdf$", "");
			PdfLayout a = PdfLayoutExtractor.extract(ref);
			PdfLayout b = PdfLayoutExtractor.extract(cand);
			LayoutComparison.Result r = LayoutComparison.compare(id, a, b);
			if (dpi > 0) {
				r.pixels = PixelComparison.compare(ref, cand, dpi);
			}
			results.add(r);
			System.out.printf("%-24s pages %d/%d  lines %d/%d  parity %.0f%%/%.0f%%  dy med %.2f max %.2f  %s%n", id,
					r.refPages, r.candPages, r.refLines, r.candLines, r.lineParity() * 100, r.pageParity() * 100,
					r.medianDy, r.maxDy, r.firstDivergence);
		}
		HtmlReport.write(reportDir, refDir.getPath(), candDir.getPath(), results);
		System.out.println("report: " + new File(reportDir, "index.html"));
		return results;
	}

	/**
	 * Scores a whole corpus of real documents: renders every docx that has a
	 * reference PDF of the same basename, compares the two, and writes
	 * outDir/scoreboard.csv and outDir/scoreboard.txt. Unlike {@link #render} and
	 * {@link #compare} this is built to survive a corpus that misbehaves: a
	 * document that throws is one error row, a document that hangs is one timeout
	 * row, and the run carries on.
	 *
	 * On one thread documents are done smallest first, so a run that is cut short has
	 * still covered the most documents.
	 *
	 * <p>{@code -Dfidelity.threads=N} scores N documents at once (default 12, where a gate
	 * stops getting shorter on a 24-thread machine; 1 is the sequential run every scoreboard
	 * before 2026-11-01 came from).  Each document is still converted in its own thread named
	 * {@code fidelity-score-<id>}, under its own timeout, and its row, its classes.txt line
	 * and its timings.csv line take the place of the sequential run's, so those files do not
	 * depend on the thread count; only the log's order does.  With more than one thread the
	 * documents are started longest first - by the baseline run's timings.csv where it has
	 * one, else by size - so the long ones do not all land at the end.</p>
	 *
	 * @param baselineCsv a previous scoreboard.csv, or null; when given, a delta
	 *                    section says whether this run is better or worse
	 */
	public static List<Scoreboard.Row> score(File corpusDir, File refPdfDir, File outDir, File baselineCsv)
			throws Exception {
		Job job = new Job(corpusDir, refPdfDir, outDir, baselineCsv, null);
		scoreAll(java.util.Collections.singletonList(job));
		return Arrays.asList(job.rows);
	}

	/**
	 * {@link #score} over several corpora in one JVM:
	 * {@code scoremany (<log> <corpusDir> <refPdfDir> <outDir> <baseline.csv|->)...}.  Their
	 * documents share one pool of {@code -Dfidelity.threads}, longest first across all of them,
	 * so a corpus's long document runs beside the other corpora's documents rather than after
	 * them, and the JIT and the font set are set up once rather than per corpus.  Each corpus
	 * gets the files its own {@code score} run would write, its log included: what a
	 * document's conversion logs goes to the log of the corpus it belongs to.
	 *
	 * <p>The docx4j properties are the JVM's, so only corpora scored with the same settings
	 * (the same {@code -Dfidelity.hyphenate}, say) can share a run.</p>
	 */
	public static void scoreMany(String[] args) throws Exception {
		if (args.length == 0 || args.length % 5 != 0) {
			throw new IllegalArgumentException("scoremany: (<log> <corpusDir> <refPdfDir> <outDir> <baseline.csv|->)...");
		}
		List<Job> jobs = new ArrayList<>();
		for (int k = 0; k < args.length; k += 5) {
			jobs.add(new Job(new File(args[k + 1]), new File(args[k + 2]), new File(args[k + 3]),
					args[k + 4].equals("-") ? null : new File(args[k + 4]), new File(args[k])));
		}
		scoreAll(jobs);
	}

	/** One corpus of a scoring run: what it reads and writes, and a slot per document for its
	 *  results, in the scoreboard's order (smallest first) whatever order they are done in. */
	static final class Job {
		final File corpusDir, refPdfDir, outDir, baselineCsv, logFile, fopDir;
		final String basis;
		final File[] docs;
		final Scoreboard.Row[] rows;
		final String[] classLines;
		final long[][] timings;
		final AtomicInteger done = new AtomicInteger();
		final AtomicInteger left;
		/** Where this corpus's lines go: its log file, or null for this process's own output. */
		java.io.OutputStream log;

		Job(File corpusDir, File refPdfDir, File outDir, File baselineCsv, File logFile) {
			this.corpusDir = corpusDir;
			this.refPdfDir = refPdfDir;
			this.outDir = outDir;
			this.baselineCsv = baselineCsv;
			this.logFile = logFile;
			this.fopDir = new File(outDir, "fop");
			this.basis = basis(corpusDir);
			this.docs = docxFiles(corpusDir);
			Arrays.sort(docs, Comparator.comparingLong(File::length).thenComparing(File::getName));
			this.rows = new Scoreboard.Row[docs.length];
			this.classLines = new String[docs.length];
			this.timings = new long[docs.length][];
			this.left = new AtomicInteger(docs.length);
		}
	}

	/** Where a thread's output goes while documents run in parallel: set by each pool task to
	 *  its corpus's log, and inherited by the conversion thread the task starts. */
	private static final InheritableThreadLocal<java.io.OutputStream> ROUTE = new InheritableThreadLocal<>();

	/** System.out and System.err while documents run in parallel: one stream, so a line is
	 *  written whole (two, with a lock each, let a progress line on System.out be cut by an event
	 *  line on System.err, which events.py, reading an event at the start of a line, then missed:
	 *  21 lines in the first parallel gate, 2026-11-01), and each line to its thread's corpus. */
	private static final class RoutingOutputStream extends java.io.OutputStream {
		private final java.io.OutputStream fallback;

		RoutingOutputStream(java.io.OutputStream fallback) {
			this.fallback = fallback;
		}

		private java.io.OutputStream target() {
			java.io.OutputStream o = ROUTE.get();
			return o == null ? fallback : o;
		}

		@Override
		public void write(int b) throws java.io.IOException {
			target().write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws java.io.IOException {
			target().write(b, off, len);
		}

		@Override
		public void flush() throws java.io.IOException {
			target().flush();
		}
	}

	private static void scoreAll(List<Job> jobs) throws Exception {
		int timeoutSeconds = Integer.getInteger("fidelity.timeoutSeconds", 120);
		int threads = Math.max(1, Integer.getInteger("fidelity.threads", 12));
		long start = System.nanoTime();
		boolean routed = threads > 1 || jobs.size() > 1;
		java.io.PrintStream stdout = System.out, stderr = System.err;
		if (routed) {
			java.io.PrintStream both = new java.io.PrintStream(new RoutingOutputStream(stdout), true,
					System.getProperty("stdout.encoding", java.nio.charset.Charset.defaultCharset().name()));
			System.setOut(both);
			System.setErr(both);
		}
		ExecutorService pool = threads == 1 ? null : Executors.newFixedThreadPool(threads, r -> {
			Thread t = new Thread(r, "fidelity-pool");
			t.setDaemon(true);
			return t;
		});
		try {
			List<int[]> tasks = new ArrayList<>(); // {job, document}, in the order they are started
			for (int j = 0; j < jobs.size(); j++) {
				Job job = jobs.get(j);
				job.fopDir.mkdirs();
				if (job.logFile != null) {
					job.log = new java.io.BufferedOutputStream(new java.io.FileOutputStream(job.logFile));
					if (fontSetLine != null) job.log.write((fontSetLine + System.lineSeparator()).getBytes(UTF8));
				}
				if (job.docs.length == 0) {
					ROUTE.set(job.log);
					try {
						finish(job, start, threads);
					} finally {
						ROUTE.remove();
					}
				}
				for (int i = 0; i < job.docs.length; i++) tasks.add(new int[] { j, i });
			}
			if (pool != null) startOrder(jobs, tasks);
			List<Future<?>> pending = new ArrayList<>();
			for (int[] t : tasks) {
				Job job = jobs.get(t[0]);
				int i = t[1];
				java.util.concurrent.Callable<Void> task = () -> {
					ROUTE.set(job.log);
					try {
						scoreDocument(job, i, timeoutSeconds);
						if (job.left.decrementAndGet() == 0) finish(job, start, threads);
					} finally {
						ROUTE.remove();
					}
					return null;
				};
				if (pool == null) {
					task.call();
				} else {
					pending.add(pool.submit(task));
				}
			}
			/* Every task waited for, so that a corpus whose files could not be written does not
			 * cut the others short; the slots are written before get() returns. */
			ExecutionException failed = null;
			for (Future<?> f : pending) {
				try {
					f.get();
				} catch (ExecutionException e) {
					if (failed == null) failed = e;
				}
			}
			if (failed != null) throw failed;
		} finally {
			if (pool != null) pool.shutdown();
			System.setOut(stdout);
			System.setErr(stderr);
			for (Job job : jobs) {
				if (job.log != null) job.log.close();
			}
		}
	}

	private static final java.nio.charset.Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;

	/** One document's row, timings and class line into its job's slots. */
	private static void scoreDocument(Job job, int i, int timeoutSeconds) {
		File docx = job.docs[i];
		String id = docx.getName().replaceAll("\\.docx$", "");
		File ref = new File(job.refPdfDir, id + ".pdf");
		Scoreboard.Row row;
		if (!ref.exists()) {
			row = new Scoreboard.Row(id, docx.length(), "noref");
			row.error = "no reference PDF " + ref.getName();
			System.out.printf(Locale.ROOT, "[%d/%d] %-44s noref%n", job.done.incrementAndGet(), job.docs.length, id);
		} else {
			List<String> classLine = new ArrayList<>();
			long[] timing = new long[TIMINGS.length];
			try {
				row = scoreOne(docx, ref, job.fopDir, id, timeoutSeconds, job.done, job.docs.length, classLine, timing);
			} catch (Throwable t) { // scoreOne catches its own; this is a pool task's last line
				row = errorRow(id, docx.length(), t);
			}
			job.classLines[i] = classLine.isEmpty() ? null : classLine.get(0);
			job.timings[i] = timing;
		}
		row.compatMode = compatMode(docx);
		row.basis = job.basis;
		job.rows[i] = row;
	}

	/** A corpus's files, once its last document is in: on the thread that did it, so the summary
	 *  lines go to the corpus's log. */
	private static void finish(Job job, long start, int threads) throws Exception {
		List<String> classes = new ArrayList<>();
		for (String l : job.classLines) if (l != null) classes.add(l);
		Scoreboard.writeLines(new File(job.outDir, "classes.txt"), classes);
		writeTimings(new File(job.outDir, "timings.csv"), job.rows, job.timings);
		writeScoreboard(job.outDir, Arrays.asList(job.rows), job.baselineCsv);
		System.out.printf(Locale.ROOT, "scored %d documents in %.1fs on %d thread%s%n", job.docs.length,
				(System.nanoTime() - start) / 1e9, threads, threads == 1 ? "" : "s");
	}

	/** The columns of timings.csv after id and status, in milliseconds: the timed work (or the
	 *  timeout, for a timeout row) and its parts, then the class reading, which is not timed. */
	static final String[] TIMINGS = { "ms", "renderMs", "refExtractMs", "candExtractMs", "compareMs", "classMs" };

	/**
	 * The order to start documents in on more than one thread (on one it is the scoreboard's own,
	 * corpus by corpus): longest first, by the milliseconds each corpus's baseline run recorded in
	 * its timings.csv, the documents none recorded first of all, largest first, so that the run's
	 * length is not one long document started last.
	 */
	static void startOrder(List<Job> jobs, List<int[]> tasks) {
		List<java.util.Map<String, Long>> known = new ArrayList<>();
		for (Job job : jobs) {
			known.add(job.baselineCsv == null ? java.util.Collections.emptyMap()
					: readTimings(new File(job.baselineCsv.getAbsoluteFile().getParentFile(), "timings.csv")));
		}
		java.util.function.ToLongFunction<int[]> cost = t -> {
			Long ms = known.get(t[0]).get(jobs.get(t[0]).docs[t[1]].getName().replaceAll("\\.docx$", ""));
			return ms == null ? Long.MAX_VALUE : ms;
		};
		tasks.sort(Comparator.comparingLong(cost).reversed()
				.thenComparing(Comparator.<int[]>comparingLong(t -> jobs.get(t[0]).docs[t[1]].length()).reversed()));
	}

	/** id to total milliseconds, from a timings.csv; empty where there is none. */
	static java.util.Map<String, Long> readTimings(File csv) {
		java.util.Map<String, Long> ms = new java.util.HashMap<>();
		if (!csv.isFile()) return ms;
		try {
			List<String> lines = java.nio.file.Files.readAllLines(csv.toPath(), java.nio.charset.StandardCharsets.UTF_8);
			for (String line : lines.subList(Math.min(1, lines.size()), lines.size())) {
				String[] f = line.split(",");
				if (f.length > 2 && !f[2].isEmpty()) ms.put(f[0], Long.parseLong(f[2]));
			}
		} catch (Exception e) {
			System.out.println("timings " + csv + ": " + e); // only the start order suffers
		}
		return ms;
	}

	/** One line per row, in the scoreboard's order: where a run's time went, and the next
	 *  parallel run's start order.  A noref row has no timings. */
	static void writeTimings(File csv, Scoreboard.Row[] rows, long[][] timings) throws Exception {
		List<String> lines = new ArrayList<>();
		lines.add("id,status," + String.join(",", TIMINGS));
		for (int i = 0; i < rows.length; i++) {
			StringBuilder sb = new StringBuilder(rows[i].id).append(',').append(rows[i].status);
			for (int k = 0; k < TIMINGS.length; k++) {
				sb.append(',');
				if (timings[i] != null) sb.append(timings[i][k]);
			}
			lines.add(sb.toString());
		}
		Scoreboard.writeLines(csv, lines);
	}

	/**
	 * Which docx a run scored, from the corpus directory's own name: {@code corpus} (the
	 * third-party file) or {@code resaved} (Word's own save of it).  The two bases are not
	 * comparable - the README's "What to score" says why the second is the one to use, and
	 * every baseline from b71 was in fact cut on the first - so every scoreboard states it.
	 * {@code -Dfidelity.basis=} overrides it for a directory named something else.
	 */
	static String basis(File corpusDir) {
		String override = System.getProperty("fidelity.basis");
		if (override != null && !override.trim().isEmpty()) return override.trim();
		String name = corpusDir.getName();
		return name.isEmpty() ? corpusDir.getPath() : name;
	}

	/**
	 * {@link #score} without the render: the candidate PDFs are taken from
	 * {@code candPdfDir} - a previous run's {@code fop} directory - so a change to the
	 * extractor or the pairing (a measuring change, not a rendering one) can be
	 * re-baselined on the renders a baseline was cut from, and its worth read on its
	 * own.  A document with no candidate PDF is one {@code error} row.  The docx is
	 * still read, for the size and the compatibility mode the row carries.
	 */
	public static List<Scoreboard.Row> rescore(File corpusDir, File refPdfDir, File candPdfDir, File outDir,
			File baselineCsv) throws Exception {
		outDir.mkdirs();
		File[] docs = docxFiles(corpusDir);
		Arrays.sort(docs, Comparator.comparingLong(File::length).thenComparing(File::getName));
		List<Scoreboard.Row> rows = new ArrayList<>();
		int n = 0;
		for (File docx : docs) {
			n++;
			String id = docx.getName().replaceAll("\\.docx$", "");
			File ref = new File(refPdfDir, id + ".pdf");
			File cand = new File(candPdfDir, id + ".pdf");
			Scoreboard.Row row;
			if (!ref.exists()) {
				row = new Scoreboard.Row(id, docx.length(), "noref");
				row.error = "no reference PDF " + ref.getName();
			} else if (!cand.exists()) {
				row = new Scoreboard.Row(id, docx.length(), "error");
				row.error = "no candidate PDF " + cand.getName();
			} else {
				try {
					PdfLayout a = LayoutCache.reference(ref);
					PdfLayout b = PdfLayoutExtractor.extract(cand);
					row = Scoreboard.Row.of(LayoutComparison.compare(id, a, b), docx.length());
				} catch (Throwable t) {
					row = errorRow(id, docx.length(), t);
				}
			}
			row.compatMode = compatMode(docx);
			row.basis = basis(corpusDir);
			/* The class column stays empty here: rescore does not render, so there is no
			 * mapper to ask what this machine substituted, and half a class - Word's side
			 * without ours - would be worse than none. */
			rows.add(row);
			if (row.scored()) {
				System.out.printf(Locale.ROOT, "[%d/%d] %-44s ok       parity %.0f%%  pages %d/%d%n", n, docs.length,
						id, row.lineParity * 100, row.refPages, row.candPages);
			} else {
				System.out.printf(Locale.ROOT, "[%d/%d] %-44s %-8s %s%n", n, docs.length, id, row.status, row.error);
			}
		}
		writeScoreboard(outDir, rows, baselineCsv);
		return rows;
	}

	/** The CSV, the text report and the delta against the baseline, if one was given. */
	private static void writeScoreboard(File outDir, List<Scoreboard.Row> rows, File baselineCsv) throws Exception {
		List<String> deltaLines = null;
		if (baselineCsv != null) {
			List<Scoreboard.Row> before = Scoreboard.readCsv(baselineCsv);
			deltaLines = Scoreboard.delta(baselineCsv.getPath(), before, rows);
		}

		File csv = new File(outDir, "scoreboard.csv");
		Scoreboard.writeCsv(csv, rows);
		List<String> text = Scoreboard.textReport(rows, deltaLines);
		Scoreboard.writeLines(new File(outDir, "scoreboard.txt"), text);

		System.out.println();
		for (String l : Scoreboard.Aggregate.of(rows).lines()) System.out.println(l);
		System.out.println();
		for (String l : Scoreboard.byClass(rows)) System.out.println(l);
		if (deltaLines != null) {
			System.out.println();
			for (String l : deltaLines) System.out.println(l);
		}
		System.out.println();
		System.out.println("scoreboard: " + csv + " and " + new File(outDir, "scoreboard.txt"));
	}

	/**
	 * The document's own Word compatibility mode: w:compatSetting w:name="compatibilityMode"
	 * in word/settings.xml, or "" where the document declares none.
	 *
	 * <p>The corpus file names carry a leading number which was used for this, but it
	 * disagrees with the document often enough to be useless for segmenting results
	 * (and eight documents of the sample declare no mode at all), so the docx itself is
	 * read.  The zip entry is parsed directly: this runs before the conversion, on every
	 * document of a large corpus, and a full load would cost far more than it is worth.
	 */
	static String compatMode(File docx) {
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(docx)) {
			java.util.zip.ZipEntry entry = zip.getEntry("word/settings.xml");
			if (entry == null) return "";
			String xml;
			try (java.io.InputStream is = zip.getInputStream(entry)) {
				xml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			}
			java.util.regex.Matcher m = COMPAT_SETTING.matcher(xml);
			while (m.find()) {
				String element = m.group();
				if (!element.contains("\"compatibilityMode\"")) continue;
				java.util.regex.Matcher v = COMPAT_VAL.matcher(element);
				if (v.find()) return v.group(1);
			}
		} catch (Exception e) {
			System.out.println("compatMode " + docx.getName() + ": " + e);
		}
		return "";
	}

	private static final java.util.regex.Pattern COMPAT_SETTING =
			java.util.regex.Pattern.compile("<w:compatSetting\\b[^>]*>");
	private static final java.util.regex.Pattern COMPAT_VAL =
			java.util.regex.Pattern.compile("w:val=\"([^\"]*)\"");

	/**
	 * One document, in its own thread so a conversion that never returns cannot
	 * stop the run. On timeout the thread is interrupted and abandoned: it may keep
	 * running (and holding memory) until the JVM exits, which is accepted here
	 * because the alternative is losing the whole scoring run to one bad document.
	 *
	 * @param timing filled in milliseconds, in the order of {@link #TIMINGS}
	 */
	private static Scoreboard.Row scoreOne(File docx, File ref, File fopDir, String id, int timeoutSeconds,
			AtomicInteger done, int total, List<String> classLines, long[] timing) {
		ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "fidelity-score-" + id);
			t.setDaemon(true); // so an abandoned conversion cannot keep the JVM alive
			return t;
		});
		/* The package the render used, handed back so the class column can be read off
		 * its mapper.  Read after the future has returned, so that reading it can never
		 * turn an ok row into a timeout - the timed work is exactly what it always was. */
		java.util.concurrent.atomic.AtomicReference<WordprocessingMLPackage> rendered =
				new java.util.concurrent.atomic.AtomicReference<>();
		Scoreboard.Row row;
		long start = System.nanoTime();
		long[] parts = new long[TIMINGS.length]; // the conversion thread's, copied once its result is in
		try {
			Future<LayoutComparison.Result> f = exec.submit(() -> {
				long t0 = System.nanoTime();
				rendered.set(renderOne(docx, fopDir, id));
				long t1 = System.nanoTime();
				PdfLayout a = LayoutCache.reference(ref);
				long t2 = System.nanoTime();
				PdfLayout b = PdfLayoutExtractor.extract(new File(fopDir, id + ".pdf"));
				long t3 = System.nanoTime();
				LayoutComparison.Result result = LayoutComparison.compare(id, a, b);
				parts[1] = ms(t1 - t0);
				parts[2] = ms(t2 - t1);
				parts[3] = ms(t3 - t2);
				parts[4] = ms(System.nanoTime() - t3);
				return result;
			});
			try {
				row = Scoreboard.Row.of(f.get(timeoutSeconds, TimeUnit.SECONDS), docx.length());
				System.arraycopy(parts, 1, timing, 1, 4);
			} catch (TimeoutException e) {
				f.cancel(true);
				row = new Scoreboard.Row(id, docx.length(), "timeout");
				row.error = "no result within " + timeoutSeconds + "s";
			} catch (ExecutionException e) {
				row = errorRow(id, docx.length(), e.getCause() == null ? e : e.getCause());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				row = errorRow(id, docx.length(), e);
			}
		} catch (Throwable t) {
			row = errorRow(id, docx.length(), t);
		} finally {
			exec.shutdownNow();
		}
		timing[0] = ms(System.nanoTime() - start);
		if (row.scored()) {
			long c0 = System.nanoTime();
			readClass(row, rendered.getAndSet(null), ref, classLines);
			timing[5] = ms(System.nanoTime() - c0);
			System.out.printf(Locale.ROOT, "[%d/%d] %-44s ok       parity %.0f%%  pages %d/%d  class %s %.2f  %.1fs%n",
					done.incrementAndGet(), total, id, row.lineParity * 100, row.refPages, row.candPages,
					row.docClass.isEmpty() ? "?" : row.docClass, row.substShare, timing[0] / 1000.0);
		} else {
			System.out.printf(Locale.ROOT, "[%d/%d] %-44s %-8s %s%n", done.incrementAndGet(), total, id, row.status,
					row.error);
		}
		return row;
	}

	private static long ms(long nanos) {
		return TimeUnit.NANOSECONDS.toMillis(nanos);
	}

	/**
	 * The class and substituted-share columns of one scored row: the Word side from the
	 * faces the golden PDF embeds, the docx4j side from the grades
	 * {@link org.docx4j.fonts.FontsAnalysis} reports for the render that was just scored.
	 * See {@link org.docx4j.fidelity.score.DocumentClass}.
	 *
	 * <p>A record, never the work: if either side cannot be read the row keeps an empty
	 * class and the run carries on, because a scoreboard without a class column is still
	 * a scoreboard.</p>
	 */
	private static void readClass(Scoreboard.Row row, WordprocessingMLPackage pkg, File ref,
			List<String> classLines) {
		if (pkg == null) return;
		try {
			org.docx4j.fonts.FontReport report = org.docx4j.fonts.FontsAnalysis.analyse(pkg);
			DocumentClass.Reading reading = DocumentClass.of(report,
					org.docx4j.fidelity.golden.PdfFonts.faces(ref));
			row.docClass = reading.getDocClass();
			row.substShare = reading.getShare();
			if (classLines != null) classLines.add(reading.line(row.id));
		} catch (Throwable t) {
			System.out.println("  class " + row.id + ": " + t.getClass().getSimpleName() + ": " + t.getMessage());
		}
	}

	private static Scoreboard.Row errorRow(String id, long size, Throwable t) {
		Scoreboard.Row row = new Scoreboard.Row(id, size, "error");
		String msg = t.getMessage();
		row.error = t.getClass().getSimpleName() + (msg == null ? "" : ": " + Scoreboard.firstLine(msg));
		return row;
	}

	/** -Dfidelity.only=id,id restricts render and compare to those probes. */
	static boolean selected(String fileName) {
		String only = System.getProperty("fidelity.only");
		if (only == null || only.trim().isEmpty()) return true;
		String id = fileName.replaceAll("\\.(docx|pdf)$", "");
		for (String s : only.split(",")) {
			if (s.trim().equals(id)) return true;
		}
		return false;
	}

	static File[] docxFiles(File dir) {
		File[] files = dir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~") && selected(n));
		if (files == null) throw new IllegalArgumentException("no docx in " + dir);
		Arrays.sort(files);
		return files;
	}
}
