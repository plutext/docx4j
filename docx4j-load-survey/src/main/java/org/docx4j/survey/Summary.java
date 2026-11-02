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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/** The counts the runbook asks for, from the TSVs: every run's rows, not only the last run's. */
final class Summary {

	private Summary() {
	}

	static String compute(Path out) throws IOException {
		Path main = out.resolve(Coordinator.MAIN);
		String header = Tsv.header(main);
		if (header == null) return "no rows yet\n";
		Map<String, Integer> col = new HashMap<>();
		String[] names = Tsv.split(header);
		for (int i = 0; i < names.length; i++) col.put(names[i], i);

		int rows = 0;
		long bytes = 0;
		Map<String, int[]> outcome = new HashMap<>();
		Map<String, int[]> failures = new HashMap<>();
		Map<String, int[]> unreadable = new HashMap<>();
		Map<String, int[]> choices = new HashMap<>();
		TreeSet<String> versions = new TreeSet<>();
		int loaded = 0, partsUnreadable = 0, strict = 0, preprocessed = 0, warned = 0, errored = 0;
		int lenientDocs = 0, oorDocs = 0, mcDocs = 0, saveRun = 0, saveFailed = 0;
		long truncated = 0, empty = 0, dropped = 0, oor = 0, mcElements = 0, mcResolved = 0;
		int[] ms = new int[1024];
		int nms = 0;
		long maxMs = -1;
		String slowest = null;

		try (BufferedReader r = Files.newBufferedReader(main, StandardCharsets.UTF_8)) {
			r.readLine();
			String line;
			while ((line = r.readLine()) != null) {
				String[] f = Tsv.split(line);
				rows++;
				bytes += num(f, col, "bytes");
				versions.add(str(f, col, "docx4j_version"));
				String o = str(f, col, "outcome");
				count(outcome, o);
				if (!o.equals("ok") && !o.equals("main_unreadable")) {
					String ex = className(str(f, col, "exception"));
					String root = className(str(f, col, "root_cause"));
					count(failures, o + "  " + ex + (root.isEmpty() || root.equals(ex) ? "" : " <- " + root));
				}
				if (num(f, col, "loaded") == 1) {
					loaded++;
					long t = num(f, col, "load_ms") + num(f, col, "read_ms");
					if (nms == ms.length) ms = Arrays.copyOf(ms, nms * 2);
					ms[nms++] = (int) Math.min(Integer.MAX_VALUE, t);
					if (t > maxMs) {
						maxMs = t;
						slowest = str(f, col, "sha256");
					}
				}
				if (num(f, col, "parts_unreadable") > 0) {
					partsUnreadable++;
					String u = str(f, col, "unreadable");
					int c = u.indexOf(": ");
					count(unreadable, c < 0 ? u : className(u.substring(c + 2)));
				}
				if (num(f, col, "strict") == 1) strict++;
				if (num(f, col, "parts_preprocessed") > 0) preprocessed++;
				if (num(f, col, "warnings") > 0) warned++;
				if (num(f, col, "errors") > 0) errored++;
				long t1 = num(f, col, "lenient_truncated"), e1 = num(f, col, "lenient_empty"), d1 = num(f, col, "lenient_dropped");
				if (t1 + e1 + d1 > 0) lenientDocs++;
				truncated += t1;
				empty += e1;
				dropped += d1;
				long o1 = num(f, col, "out_of_range");
				if (o1 > 0) oorDocs++;
				oor += o1;
				long m = num(f, col, "mc_elements");
				if (m > 0) mcDocs++;
				mcElements += m;
				mcResolved += num(f, col, "mc_resolved");
				for (String kv : str(f, col, "mc_choices").split(" ")) {
					int eq = kv.lastIndexOf('=');
					if (eq > 0) choices.computeIfAbsent(kv.substring(0, eq), k -> new int[1])[0] += Integer.parseInt(kv.substring(eq + 1));
				}
				String saved = str(f, col, "saved");
				if (!saved.isEmpty()) {
					saveRun++;
					if (saved.equals("0")) saveFailed++;
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.ROOT, "docx4j load survey: %s (docx4j %s)%n", main, String.join(", ", versions)));
		sb.append(String.format(Locale.ROOT, "documents           %,12d   (%,.1f GB)%n", rows, bytes / 1e9));
		for (String o : new String[] { "ok", "main_unreadable", "load_failed", "timeout", "out_of_memory", "crashed" }) {
			sb.append(String.format(Locale.ROOT, "  %-17s %,12d   %s%n", o, n(outcome, o), pct(n(outcome, o), rows)));
		}
		sb.append(String.format(Locale.ROOT, "loaded              %,12d%n", loaded));
		sb.append(String.format(Locale.ROOT, "  a part unreadable %,12d   %s%n", partsUnreadable, pct(partsUnreadable, loaded)));
		sb.append(String.format(Locale.ROOT, "  a part preprocessed (mc-preprocessor.xslt) %,d   %s%n", preprocessed, pct(preprocessed, loaded)));
		sb.append(String.format(Locale.ROOT, "  Strict            %,12d%n", strict));
		sb.append(String.format(Locale.ROOT, "  with a WARN       %,12d   %s;  with an ERROR %,d   %s%n", warned, pct(warned, rows), errored, pct(errored, rows)));
		sb.append(String.format(Locale.ROOT, "  lenient integers  %,12d   docs (truncated %,d, empty %,d, dropped %,d)%n", lenientDocs, truncated, empty, dropped));
		sb.append(String.format(Locale.ROOT, "  out of range      %,12d   docs (%,d values)%n", oorDocs, oor));
		sb.append(String.format(Locale.ROOT, "  mc:AlternateContent %,10d   docs, %,d elements, %,d resolved at load%n", mcDocs, mcElements, mcResolved));
		if (!choices.isEmpty()) sb.append("    branches read: ").append(top(choices, 15, " ")).append('\n');
		if (saveRun > 0) sb.append(String.format(Locale.ROOT, "  saved             %,12d   failed %,d%n", saveRun - saveFailed, saveFailed));
		if (nms > 0) {
			Arrays.sort(ms, 0, nms);
			sb.append(String.format(Locale.ROOT, "load + read ms: median %d, p90 %d, p99 %d, max %d (%s)%n",
					ms[nms / 2], ms[(int) (nms * 0.9)], ms[Math.min(nms - 1, (int) (nms * 0.99))], maxMs, slowest));
		}
		if (!failures.isEmpty()) sb.append("failures (outcome, exception <- root cause):\n").append(top(failures, 20, "\n"));
		if (!unreadable.isEmpty()) sb.append("first unreadable part, by root cause:\n").append(top(unreadable, 10, "\n"));
		messages(out, sb);
		return sb.toString();
	}

	/** The most frequent messages by documents, numbers as #. */
	private static void messages(Path out, StringBuilder sb) throws IOException {
		Path p = out.resolve(Coordinator.MESSAGES);
		String header = Tsv.header(p);
		if (header == null) return;
		Map<String, int[]> docs = new HashMap<>();
		Map<String, String> lastSha = new HashMap<>();
		try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
			r.readLine();
			String line;
			while ((line = r.readLine()) != null) {
				String[] f = Tsv.split(line);
				if (f.length < 7) continue;
				String logger = f[3].substring(f[3].lastIndexOf('.') + 1);
				String msg = f[4].replaceAll("\\b[0-9]+\\b", "#"); // a number on its own: not the 4 of docx4j, the 14 of w14
				String key = f[1] + " " + f[2] + " " + logger + ": " + (msg.length() > 140 ? msg.substring(0, 139) + "…" : msg);
				if (!f[0].equals(lastSha.put(key, f[0]))) docs.computeIfAbsent(key, k -> new int[1])[0]++;
			}
		}
		if (!docs.isEmpty()) sb.append("messages, by documents (phase level logger: message):\n").append(top(docs, 25, "\n"));
	}

	private static String top(Map<String, int[]> m, int n, String sep) {
		List<Map.Entry<String, int[]>> es = new ArrayList<>(m.entrySet());
		es.sort((a, b) -> a.getValue()[0] != b.getValue()[0] ? Integer.compare(b.getValue()[0], a.getValue()[0]) : a.getKey().compareTo(b.getKey()));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < es.size() && i < n; i++) {
			if (sep.equals("\n")) sb.append(String.format(Locale.ROOT, "  %,10d  %s%n", es.get(i).getValue()[0], es.get(i).getKey()));
			else sb.append(i > 0 ? sep : "").append(es.get(i).getKey()).append('=').append(es.get(i).getValue()[0]);
		}
		if (es.size() > n) sb.append(sep.equals("\n") ? String.format(Locale.ROOT, "  %10s  (%,d more)%n", "", es.size() - n) : " ...");
		return sb.toString();
	}

	private static void count(Map<String, int[]> m, String k) {
		m.computeIfAbsent(k, x -> new int[1])[0]++;
	}

	private static int n(Map<String, int[]> m, String k) {
		int[] v = m.get(k);
		return v == null ? 0 : v[0];
	}

	private static String pct(long a, long b) {
		return b == 0 ? "" : String.format(Locale.ROOT, "%.2f%%", 100.0 * a / b);
	}

	/** "a.b.C: message" to "a.b.C"; "timeout: ..." to "timeout". */
	static String className(String s) {
		int c = s.indexOf(": ");
		return c < 0 ? s : s.substring(0, c);
	}

	private static String str(String[] f, Map<String, Integer> col, String name) {
		Integer i = col.get(name);
		return i == null || i >= f.length ? "" : f[i];
	}

	private static long num(String[] f, Map<String, Integer> col, String name) {
		String s = str(f, col, name);
		if (s.isEmpty()) return 0;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
