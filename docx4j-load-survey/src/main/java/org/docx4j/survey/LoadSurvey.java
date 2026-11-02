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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * docx4j's load survey of a docx corpus: one row per document for the catalogue's docx4j_load
 * table. See README.md.
 */
public final class LoadSurvey {

	static final String USAGE = String.join("\n",
			"usage:",
			"  java -jar docx4j-load-survey.jar run --corpus DIR --out DIR [options]",
			"  java -jar docx4j-load-survey.jar run --list FILE --out DIR [options]   (one path a line)",
			"  java -jar docx4j-load-survey.jar summary --out DIR      (the counts again; rewrites import.sql)",
			"options: --workers N (8)  --heap SIZE (2g)  --timeout SECONDS (60)  --limit N (all)",
			"         --save (save each loaded document too, to nowhere)  --java PATH (this JVM's)  --progress SECONDS (60)",
			"");

	private LoadSurvey() {
	}

	static final class Options {
		Path corpus;
		Path list;
		Path out;
		int workers = 8;
		String heap = "2g";
		int timeoutS = 60;
		long limit;
		boolean save;
		String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
		int progressS = 60;
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.print(USAGE);
			System.exit(1);
		}
		String command = args[0];
		Options o;
		try {
			o = parse(args);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.print(USAGE);
			System.exit(1);
			return;
		}
		switch (command) {
		case "worker":
			WorkerMain.main(o.timeoutS * 1000L, o.save);
			break;
		case "run":
			if (o.out == null || (o.corpus == null) == (o.list == null)) {
				System.err.print("run needs --out, and --corpus or --list\n" + USAGE);
				System.exit(1);
			}
			if (o.corpus != null && !Files.isDirectory(o.corpus)) {
				System.err.println("no such directory: " + o.corpus);
				System.exit(1);
			}
			System.exit(new Coordinator(o).run());
			break;
		case "summary":
			if (o.out == null) {
				System.err.print("summary needs --out\n" + USAGE);
				System.exit(1);
			}
			String s = Summary.compute(o.out);
			Files.write(o.out.resolve("summary.txt"), s.getBytes(StandardCharsets.UTF_8));
			ImportSql.write(o.out);
			System.out.print(s);
			break;
		default:
			System.err.print("unknown command " + command + "\n" + USAGE);
			System.exit(1);
		}
	}

	static Options parse(String[] args) {
		Options o = new Options();
		for (int i = 1; i < args.length; i++) {
			String a = args[i];
			switch (a) {
			case "--save":
				o.save = true;
				continue;
			default:
				break;
			}
			if (i + 1 >= args.length) throw new IllegalArgumentException("unknown option or missing value: " + a);
			String v = args[++i];
			try {
				switch (a) {
				case "--corpus": o.corpus = Paths.get(v); break;
				case "--list": o.list = Paths.get(v); break;
				case "--out": o.out = Paths.get(v); break;
				case "--workers": o.workers = positive(a, Integer.parseInt(v)); break;
				case "--heap": o.heap = v; break;
				case "--timeout": o.timeoutS = positive(a, Integer.parseInt(v)); break;
				case "--limit": o.limit = Long.parseLong(v); break;
				case "--java": o.java = v; break;
				case "--progress": o.progressS = positive(a, Integer.parseInt(v)); break;
				default: throw new IllegalArgumentException("unknown option " + a);
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(a + " needs a number, not " + v);
			}
		}
		return o;
	}

	private static int positive(String a, int n) {
		if (n <= 0) throw new IllegalArgumentException(a + " must be positive");
		return n;
	}

	private static String version;

	/** docx4j's own version resource (org.docx4j.Version reads it too), without loading docx4j. */
	static synchronized String docx4jVersion() {
		if (version == null) {
			Properties p = new Properties();
			try (InputStream is = LoadSurvey.class.getClassLoader().getResourceAsStream("docx4j_version.properties")) {
				if (is != null) p.load(is);
			} catch (IOException e) {
				// unknown
			}
			version = p.getProperty("version", "unknown");
		}
		return version;
	}
}
