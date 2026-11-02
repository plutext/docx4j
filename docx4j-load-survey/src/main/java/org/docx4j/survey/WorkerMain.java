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
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * A worker JVM. It reads "sha256 TAB path" lines on standard input and answers each on standard
 * output with the document's message rows ("M" TAB row) and then its row ("R" TAB row). Each
 * document runs on a thread of its own; a document still running after the timeout, or one that
 * ran out of memory, is reported and the JVM ends, for the coordinator to start another. Anything
 * else written to standard output (docx4j or a library printing) goes to standard error.
 */
final class WorkerMain {

	private WorkerMain() {
	}

	static void main(long timeoutMs, boolean save) throws Exception {
		PrintStream protocol = new PrintStream(new FileOutputStream(FileDescriptor.out), false, StandardCharsets.UTF_8);
		System.setOut(System.err);
		warmUp(timeoutMs, save);
		protocol.println("READY");
		protocol.flush();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		String line;
		while ((line = in.readLine()) != null) {
			int tab = line.indexOf('\t');
			if (tab <= 0) continue;
			Outcome o = survey(line.substring(0, tab), Paths.get(line.substring(tab + 1)), timeoutMs, save);
			for (Object[] m : o.messages) protocol.println("M\t" + Tsv.row(m));
			protocol.println("R\t" + Tsv.row(o.row));
			protocol.flush();
			if (o.exit) Runtime.getRuntime().halt(3); // a thread may still be running: no shutdown
		}
		protocol.flush();
		Runtime.getRuntime().halt(0);
	}

	static final class Outcome {
		Object[] row;
		List<Object[]> messages;
		/** The JVM must end: a thread is still running, or memory ran out. */
		boolean exit;
	}

	static Outcome survey(String sha, Path path, long timeoutMs, boolean save) throws InterruptedException {
		DocumentProbe probe = new DocumentProbe(save);
		AtomicReference<Throwable> thrown = new AtomicReference<>();
		AtomicReference<Result> done = new AtomicReference<>();
		LogCapture.Session[] session = new LogCapture.Session[1];
		Thread t = new Thread(() -> {
			LogCapture.begin(session[0]);
			try {
				done.set(probe.run(path, sha, session[0]));
			} catch (Throwable e) {
				thrown.set(e);
			} finally {
				LogCapture.end(session[0]);
			}
		}, "survey " + sha.substring(0, Math.min(12, sha.length())));
		t.setDaemon(true);
		session[0] = new LogCapture.Session(t);
		t.start();
		t.join(timeoutMs);
		Outcome o = new Outcome();
		Result r = probe.result;
		if (t.isAlive()) {
			r.set(Column.OUTCOME, "timeout");
			r.set(Column.EXCEPTION, "timeout: still running after " + timeoutMs / 1000 + " s, in " + probe.step);
			o.exit = true;
		} else if (done.get() == null) {
			Throwable e = thrown.get();
			if (e instanceof OutOfMemoryError) {
				r.set(Column.OUTCOME, "out_of_memory");
				r.set(Column.EXCEPTION, Describe.line(Describe.of(e) + ", in " + probe.step));
			} else {
				// the probe catches everything else: this is the survey's own defect
				r.set(Column.OUTCOME, "crashed");
				r.set(Column.EXCEPTION, Describe.line("survey defect: " + Describe.withRoot(e) + ", in " + probe.step));
			}
			o.exit = true;
		}
		o.messages = session[0].rows(sha);
		o.row = r.values();
		return o;
	}

	/**
	 * docx4j's one-off work (JAXB contexts, properties, the zip limits' first log lines) happens
	 * here, on a document of its own, so that no row pays for it or counts its messages.
	 */
	private static void warmUp(long timeoutMs, boolean save) throws Exception {
		WordprocessingMLPackage w = WordprocessingMLPackage.createPackage();
		w.getMainDocumentPart().addParagraphOfText("docx4j load survey");
		Path tmp = Files.createTempFile("warm-up", ".docx");
		try {
			w.save(tmp.toFile());
			for (int i = 0; i < 3; i++) {
				Outcome o = survey("0000000000000000000000000000000000000000000000000000000000000000", tmp, timeoutMs, save);
				if (o.exit) throw new IllegalStateException("warm-up document failed: " + Tsv.row(o.row));
			}
		} finally {
			Files.deleteIfExists(tmp);
		}
	}
}
