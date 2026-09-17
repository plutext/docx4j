package org.docx4j.fidelity.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.docx4j.fidelity.compare.LayoutComparison;

/**
 * The scoreboard of a corpus scoring run: one {@link Row} per document, the
 * {@link Aggregate} over them, CSV read/write, and the delta against a previous
 * run's CSV.
 *
 * Deliberately free of any conversion or PDF code, so the arithmetic and the
 * formats can be unit tested with synthetic results.
 */
public final class Scoreboard {

	/** id used for the aggregate row appended to the CSV. */
	public static final String TOTAL = "TOTAL";

	/** a document whose line parity moved by more than this is worth a reviewer's attention. */
	public static final double NOISE = 0.02;

	/** a document at or above this line parity counts as matching Word. */
	public static final double GOOD = 0.98;

	/**
	 * The CSV's columns.  {@link #readCsv} reads by name, not by position, so a column
	 * may be added here without making the scoreboards written before it unreadable -
	 * which matters because every run is diffed against an older one.  A column a file
	 * does not carry reads as 0 or empty.
	 */
	public static final String[] HEADER = { "id", "compatMode", "sizeBytes", "status", "refPages", "candPages",
			"refLines", "candLines", "lineParity", "pageParity", "matched", "merged", "medianDy", "maxDy",
			"firstDivergence", "error" };

	private Scoreboard() {}

	/** ok = rendered and compared; error / timeout = conversion failed; noref = no Word PDF to compare with. */
	public static final class Row {
		public String id = "";
		public String compatMode = "";
		public long sizeBytes;
		public String status = "ok";
		public int refPages, candPages, refLines, candLines, matched;
		/** Of {@link #matched}, the pairs the merging pass formed by concatenation - one
		 *  side's line against the other side's two or more.  @see
		 *  org.docx4j.fidelity.compare.LayoutComparison */
		public int merged;
		public double lineParity, pageParity, medianDy, maxDy;
		public String firstDivergence = "";
		public String error = "";

		public Row() {}

		public Row(String id, long sizeBytes, String status) {
			this.id = id;
			this.sizeBytes = sizeBytes;
			this.status = status;
		}

		public static Row of(LayoutComparison.Result r, long sizeBytes) {
			Row row = new Row(r.id, sizeBytes, "ok");
			row.refPages = r.refPages;
			row.candPages = r.candPages;
			row.refLines = r.refLines;
			row.candLines = r.candLines;
			row.matched = r.matched;
			row.merged = r.merged;
			row.lineParity = r.lineParity();
			row.pageParity = r.pageParity();
			row.medianDy = r.medianDy;
			row.maxDy = r.maxDy;
			row.firstDivergence = r.firstDivergence == null ? "" : r.firstDivergence;
			return row;
		}

		public boolean scored() {
			return "ok".equals(status);
		}

		public boolean samePages() {
			return refPages == candPages;
		}

		@Override
		public String toString() {
			return line();
		}

		/** the one-line form used in scoreboard.txt, like the {@code compare} mode prints. */
		public String line() {
			if (!scored()) {
				return String.format(Locale.ROOT, "%-44s %-8s %s", id, status,
						error.isEmpty() ? firstDivergence : error);
			}
			return String.format(Locale.ROOT,
					"%-44s pages %d/%d  lines %d/%d  parity %.0f%%/%.0f%%  dy med %.2f max %.2f  %s", id, refPages,
					candPages, refLines, candLines, lineParity * 100, pageParity * 100, medianDy, maxDy,
					firstDivergence);
		}
	}

	/** Everything the aggregate lines report; recomputed from the rows, never parsed back from the TOTAL row. */
	public static final class Aggregate {
		public int scored, errors, timeouts, noref;
		public int samePages, atLeastGood;
		/** The merged pairs of every scored row, summed. */
		public long mergedPairs;
		public long linesMatched, linesTotal, linesCand, sizeBytes;
		public double medianParity, meanParity;

		public static Aggregate of(List<Row> rows) {
			Aggregate a = new Aggregate();
			List<Double> parities = new ArrayList<>();
			for (Row r : rows) {
				if (TOTAL.equals(r.id)) continue;
				a.sizeBytes += r.sizeBytes;
				switch (r.status) {
				case "ok":
					a.scored++;
					a.linesMatched += r.matched;
					a.linesTotal += r.refLines;
					a.linesCand += r.candLines;
					a.mergedPairs += r.merged;
					if (r.samePages()) a.samePages++;
					if (r.lineParity >= GOOD) a.atLeastGood++;
					parities.add(r.lineParity);
					break;
				case "timeout":
					a.timeouts++;
					break;
				case "noref":
					a.noref++;
					break;
				default:
					a.errors++;
				}
			}
			Collections.sort(parities);
			a.medianParity = parities.isEmpty() ? 0 : parities.get(parities.size() / 2);
			double sum = 0;
			for (double d : parities) sum += d;
			a.meanParity = parities.isEmpty() ? 0 : sum / parities.size();
			return a;
		}

		public double lineRatio() {
			return linesTotal == 0 ? 0 : (double) linesMatched / linesTotal;
		}

		/** label / value pairs, in report order. */
		public List<String[]> entries() {
			List<String[]> out = new ArrayList<>();
			out.add(new String[] { "documents scored", Integer.toString(scored) });
			out.add(new String[] { "errors", Integer.toString(errors) });
			out.add(new String[] { "timeouts", Integer.toString(timeouts) });
			out.add(new String[] { "no reference PDF", Integer.toString(noref) });
			out.add(new String[] { "same page count", count(samePages, scored) });
			out.add(new String[] { "lines matched",
					String.format(Locale.ROOT, "%d/%d (%.1f%%)", linesMatched, linesTotal, lineRatio() * 100) });
			out.add(new String[] { "merged pairs", Long.toString(mergedPairs) });
			out.add(new String[] { "median line parity", String.format(Locale.ROOT, "%.4f", medianParity) });
			out.add(new String[] { "mean line parity", String.format(Locale.ROOT, "%.4f", meanParity) });
			out.add(new String[] { String.format(Locale.ROOT, "line parity >= %.2f", GOOD),
					count(atLeastGood, scored) });
			return out;
		}

		/** the human readable block, one string per line. */
		public List<String> lines() {
			List<String> out = new ArrayList<>();
			for (String[] e : entries()) out.add(String.format(Locale.ROOT, "%-20s %s", e[0], e[1]));
			return out;
		}

		/** the single line stashed in the CSV's TOTAL row. */
		public String summary() {
			return String.format(Locale.ROOT,
					"scored=%d errors=%d timeouts=%d noref=%d samePages=%d (%.1f%%) linesMatched=%d/%d (%.1f%%) "
							+ "merged=%d medianParity=%.4f meanParity=%.4f atLeast%.2f=%d (%.1f%%)",
					scored, errors, timeouts, noref, samePages, pct(samePages, scored), linesMatched, linesTotal,
					lineRatio() * 100, mergedPairs, medianParity, meanParity, GOOD, atLeastGood,
					pct(atLeastGood, scored));
		}

		private static String count(int n, int of) {
			return String.format(Locale.ROOT, "%d (%.1f%%)", n, pct(n, of));
		}

		private static double pct(int n, int of) {
			return of == 0 ? 0 : 100.0 * n / of;
		}
	}

	// ---------------------------------------------------------------- CSV

	public static void writeCsv(File file, List<Row> rows) throws IOException {
		Aggregate a = Aggregate.of(rows);
		if (file.getParentFile() != null) file.getParentFile().mkdirs();
		try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
			w.print(String.join(",", HEADER));
			w.print("\n");
			for (Row r : rows) {
				w.print(csv(r));
				w.print("\n");
			}
			w.print(csv(totalRow(a)));
			w.print("\n");
		}
	}

	/**
	 * The aggregate as a Row: the counts that fit a column go in it, and the whole
	 * aggregate is repeated as text in firstDivergence. Reading a scoreboard back
	 * skips this row and recomputes, so nothing depends on this shape.
	 */
	public static Row totalRow(Aggregate a) {
		Row t = new Row();
		t.id = TOTAL;
		t.sizeBytes = a.sizeBytes;
		t.status = "scored=" + a.scored;
		t.refPages = a.samePages;
		t.candPages = a.atLeastGood;
		t.refLines = (int) a.linesTotal;
		t.candLines = (int) a.linesCand;
		t.matched = (int) a.linesMatched;
		t.merged = (int) a.mergedPairs;
		t.lineParity = a.lineRatio();
		t.pageParity = a.medianParity;
		t.medianDy = a.meanParity;
		t.firstDivergence = a.summary();
		return t;
	}

	private static String csv(Row r) {
		StringBuilder sb = new StringBuilder();
		sb.append(q(r.id)).append(',');
		sb.append(q(r.compatMode)).append(',');
		sb.append(r.sizeBytes).append(',');
		sb.append(q(r.status)).append(',');
		sb.append(r.refPages).append(',');
		sb.append(r.candPages).append(',');
		sb.append(r.refLines).append(',');
		sb.append(r.candLines).append(',');
		sb.append(String.format(Locale.ROOT, "%.4f", r.lineParity)).append(',');
		sb.append(String.format(Locale.ROOT, "%.4f", r.pageParity)).append(',');
		sb.append(r.matched).append(',');
		sb.append(r.merged).append(',');
		sb.append(String.format(Locale.ROOT, "%.2f", r.medianDy)).append(',');
		sb.append(String.format(Locale.ROOT, "%.2f", r.maxDy)).append(',');
		sb.append(q(oneLine(r.firstDivergence))).append(',');
		sb.append(q(oneLine(firstLine(r.error))));
		return sb.toString();
	}

	public static String firstLine(String s) {
		if (s == null) return "";
		int i = s.indexOf('\n');
		return i < 0 ? s : s.substring(0, i);
	}

	public static String oneLine(String s) {
		return s == null ? "" : s.replace("\r", " ").replace("\n", " ").trim();
	}

	private static String q(String s) {
		return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\"";
	}

	/** Reads a scoreboard.csv written by {@link #writeCsv}; the TOTAL row is dropped. */
	public static List<Row> readCsv(File file) throws IOException {
		List<Row> rows = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			/* by name, not by position: a scoreboard written before a column existed is
			 * still the baseline the next run is diffed against */
			String line = r.readLine();
			if (line == null) return rows;
			List<String> head = splitCsv(line);
			while ((line = r.readLine()) != null) {
				if (line.trim().isEmpty()) continue;
				List<String> f = splitCsv(line);
				if (f.isEmpty() || TOTAL.equals(f.get(0))) continue;
				Row row = new Row();
				row.id = at(head, f, "id");
				row.compatMode = at(head, f, "compatMode");
				row.sizeBytes = num(at(head, f, "sizeBytes"));
				row.status = at(head, f, "status");
				row.refPages = (int) num(at(head, f, "refPages"));
				row.candPages = (int) num(at(head, f, "candPages"));
				row.refLines = (int) num(at(head, f, "refLines"));
				row.candLines = (int) num(at(head, f, "candLines"));
				row.lineParity = dbl(at(head, f, "lineParity"));
				row.pageParity = dbl(at(head, f, "pageParity"));
				row.matched = (int) num(at(head, f, "matched"));
				row.merged = (int) num(at(head, f, "merged"));
				row.medianDy = dbl(at(head, f, "medianDy"));
				row.maxDy = dbl(at(head, f, "maxDy"));
				row.firstDivergence = at(head, f, "firstDivergence");
				row.error = at(head, f, "error");
				rows.add(row);
			}
		}
		return rows;
	}

	/** One field of a row by its column name; empty where the file has no such column. */
	private static String at(List<String> header, List<String> fields, String name) {
		int i = header.indexOf(name);
		return i < 0 || i >= fields.size() ? "" : fields.get(i);
	}

	private static long num(String s) {
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static double dbl(String s) {
		try {
			return Double.parseDouble(s.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	static List<String> splitCsv(String line) {
		List<String> out = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (inQuotes) {
				if (c == '"') {
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
						cur.append('"');
						i++;
					} else {
						inQuotes = false;
					}
				} else {
					cur.append(c);
				}
			} else if (c == '"') {
				inQuotes = true;
			} else if (c == ',') {
				out.add(cur.toString());
				cur.setLength(0);
			} else {
				cur.append(c);
			}
		}
		out.add(cur.toString());
		return out;
	}

	// ---------------------------------------------------------------- text report

	/** aggregate block, then one line per document, worst line parity first. */
	public static List<String> textReport(List<Row> rows, List<String> deltaLines) {
		List<String> out = new ArrayList<>(Aggregate.of(rows).lines());
		if (deltaLines != null && !deltaLines.isEmpty()) {
			out.add("");
			out.addAll(deltaLines);
		}
		out.add("");
		List<Row> sorted = new ArrayList<>(rows);
		sorted.sort((x, y) -> {
			int c = Double.compare(sortKey(x), sortKey(y));
			return c != 0 ? c : x.id.compareTo(y.id);
		});
		for (Row r : sorted) out.add(r.line());
		return out;
	}

	private static double sortKey(Row r) {
		return r.scored() ? r.lineParity : -1; // failures ahead of the worst scored document
	}

	public static void writeLines(File file, List<String> lines) throws IOException {
		if (file.getParentFile() != null) file.getParentFile().mkdirs();
		try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
			for (String l : lines) {
				w.print(l);
				w.print("\n");
			}
		}
	}

	// ---------------------------------------------------------------- delta

	/**
	 * What a reviewer needs to accept or reject a layout change: the aggregate
	 * before and after, then every document that moved by more than {@link #NOISE}
	 * or changed status or page-count equality, regressions first.
	 */
	public static List<String> delta(String baselineName, List<Row> before, List<Row> after) {
		List<String> out = new ArrayList<>();
		Aggregate a = Aggregate.of(before), b = Aggregate.of(after);
		out.add("delta vs " + baselineName);
		out.add(String.format(Locale.ROOT, "%-20s %20s %20s", "", "before", "after"));
		List<String[]> la = a.entries(), lb = b.entries();
		for (int i = 0; i < la.size(); i++) {
			out.add(String.format(Locale.ROOT, "%-20s %20s %20s", la.get(i)[0], la.get(i)[1], lb.get(i)[1]));
		}

		Map<String, Row> baseById = new LinkedHashMap<>();
		for (Row r : before) baseById.put(r.id, r);
		List<Row[]> changed = new ArrayList<>();
		List<String> added = new ArrayList<>();
		for (Row r : after) {
			Row base = baseById.get(r.id);
			if (base == null) {
				added.add(r.id);
				continue;
			}
			boolean moved = Math.abs(r.lineParity - base.lineParity) > NOISE;
			boolean statusChanged = !base.status.equals(r.status);
			boolean pagesChanged = base.samePages() != r.samePages();
			if (moved || statusChanged || pagesChanged) changed.add(new Row[] { base, r });
		}
		changed.sort((x, y) -> {
			int c = Double.compare(x[1].lineParity - x[0].lineParity, y[1].lineParity - y[0].lineParity);
			return c != 0 ? c : x[0].id.compareTo(y[0].id);
		});
		int regressions = 0;
		for (Row[] p : changed) {
			if (isRegression(p[0], p[1])) regressions++;
		}
		out.add("");
		out.add(String.format(Locale.ROOT, "changed documents: %d (%d regressions, %d improvements)", changed.size(),
				regressions, changed.size() - regressions));
		for (Row[] p : changed) {
			out.add(changeLine(p[0], p[1]));
		}
		List<String> removed = new ArrayList<>();
		for (Row r : before) {
			boolean found = false;
			for (Row s : after) {
				if (s.id.equals(r.id)) {
					found = true;
					break;
				}
			}
			if (!found) removed.add(r.id);
		}
		if (!added.isEmpty()) out.add("not in the baseline: " + added.size() + " " + abbreviate(added));
		if (!removed.isEmpty()) out.add("only in the baseline: " + removed.size() + " " + abbreviate(removed));
		return out;
	}

	static boolean isRegression(Row before, Row after) {
		// the baseline comes back from a CSV written to 4 decimals while the new run's
		// parity is full precision, so compare at the precision both sides have: an
		// unchanged document differing by 1e-9 is not a regression
		double b = round4(before.lineParity), a = round4(after.lineParity);
		if (a < b) return true;
		if (a > b) return false;
		if (before.scored() && !after.scored()) return true;
		return before.samePages() && !after.samePages();
	}

	/** the precision the CSV carries (see {@link Row#line()} and the CSV writer). */
	static double round4(double v) {
		return Math.round(v * 10000.0) / 10000.0;
	}

	static String changeLine(Row before, Row after) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.ROOT, "  %-11s %-44s %.4f -> %.4f  pages %d/%d -> %d/%d",
				isRegression(before, after) ? "REGRESSION" : "improved", after.id, before.lineParity, after.lineParity,
				before.refPages, before.candPages, after.refPages, after.candPages));
		if (!before.status.equals(after.status)) sb.append("  [").append(before.status).append(" -> ")
				.append(after.status).append("]");
		return sb.toString();
	}

	private static String abbreviate(List<String> ids) {
		if (ids.size() <= 10) return ids.toString();
		return ids.subList(0, 10) + " ...";
	}
}
