package org.docx4j.fidelity.golden;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
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

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGridCol;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Measures how wrong docx4j's table columns are, in twips, against the widths Word itself
 * computed - the quantitative form of the question {@link GridDiff} asks qualitatively.
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.ColumnError &lt;originalDir&gt; &lt;resavedDir&gt; [out.csv]</pre>
 *
 * <p>The reference is the {@code w:tblGrid} of the <em>resaved</em> document.  A grid Word has
 * written is Word's own cached column layout - what it actually draws - so it states each
 * column in twips, exactly, with none of the noise of reading a column origin off a page
 * image.  The candidate is the {@code fo:table-column} set docx4j produces for the same table
 * from the <em>original</em> document, which is where the autofit sizer's answer ends up.
 * Neither side is a rendering, so this measures the sizer alone: nothing here depends on
 * fonts, on FOP, or on the page the table landed on.
 *
 * <p>Tables are paired by index, the resaved document's tables against the original's FO
 * tables, both in document order.  The {@code w:tbl} side uses the same {@code ClassFinder}
 * traversal as {@link GridDiff}, so a nested table follows its parent on both sides.  Where
 * the two counts differ the document is skipped whole rather than compared out of step - a
 * table in a header, a footnote or a text box is in the FO but not in the main document part's
 * traversal, and one misalignment would otherwise be reported as a page of large errors.  The
 * FO's own wrapper tables (the bands {@code WordLayoutFixups} builds around a floating table
 * or an image) are not counted: only the table writer puts {@code column-number} on a column,
 * so that attribute is what tells a real table from a wrapper.
 *
 * <p>Each row carries {@code wordRewroteGrid} - whether Word kept the grid the document
 * declared - because the two populations answer different questions.  Where Word kept the
 * grid, any error is docx4j declining to use a grid Word was happy with; where Word rewrote
 * it, both sides computed a layout and the error is one sizer against the other.
 *
 * <p>Prints the tables worst first by {@code maxAbsRatioErr}, then an aggregate, split by that
 * flag.  A third argument writes the same rows as CSV so two runs can be diffed.
 */
public final class ColumnError {

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** Per-document render budget; a corpus of long documents should not be lost to one of them. */
	private static final int TIMEOUT_SECONDS =
			Integer.getInteger("fidelity.timeoutSeconds", 180).intValue();

	/** One compared table. */
	static final class Row {
		String id;
		int tableIndex;          // 1-based, in document order
		int[] word;              // Word's grid, twips
		int[] docx4j;            // docx4j's fo:table-column widths, twips
		double[] ratio;          // docx4j / Word, per column; NaN where Word's column is 0
		double maxAbsRatioErr;   // max |ratio - 1|
		long sumAbsDiffTwips;
		int wordTotal, docx4jTotal;
		boolean wordRewroteGrid;
	}

	/** docx4j's columns for one table, or the reason there are none to compare. */
	private static final class FoTable {
		final double[] twips;
		final String reason;
		FoTable(double[] twips) { this.twips = twips; this.reason = null; }
		FoTable(String reason) { this.twips = null; this.reason = reason; }
	}

	public static void main(String[] args) throws Exception {
		File origDir = new File(args[0]), resavedDir = new File(args[1]);
		File csv = args.length > 2 ? new File(args[2]) : null;
		File[] files = origDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
		if (files == null || files.length == 0) throw new IllegalArgumentException("no docx in " + origDir);
		Arrays.sort(files);

		List<Row> rows = new ArrayList<Row>();
		int docsSeen = 0, docsFailed = 0, docsMismatched = 0, tablesSkipped = 0;
		List<String> skips = new ArrayList<String>();
		int n = 0;
		for (File orig : files) {
			String id = orig.getName().replaceAll("\\.docx$", "");
			File resaved = new File(resavedDir, id + ".docx");
			if (resaved.length() == 0) continue;
			n++;
			List<int[]> declared, word;
			List<FoTable> ours;
			try {
				declared = grids(orig);
				word = grids(resaved);
				ours = render(orig);
			} catch (Exception e) {
				docsFailed++;
				System.out.printf(Locale.ROOT, "[%d] %-44s failed - %s%n", n, id, firstLine(e));
				continue;
			}
			docsSeen++;
			if (word.size() != ours.size()) {
				docsMismatched++;
				System.out.printf(Locale.ROOT, "[%d] %-44s tables %d in Word's docx, %d in our FO - skipped%n",
						n, id, word.size(), ours.size());
				continue;
			}
			int compared = 0;
			for (int i = 0; i < word.size(); i++) {
				Row r = compare(id, i + 1, declared, word.get(i), ours.get(i), skips);
				if (r == null) tablesSkipped++;
				else {
					rows.add(r);
					compared++;
				}
			}
			System.out.printf(Locale.ROOT, "[%d] %-44s %d tables, %d compared%n", n, id, word.size(), compared);
		}

		rows.sort(Comparator.comparingDouble((Row r) -> -r.maxAbsRatioErr));
		System.out.println();
		System.out.println("Per table, worst first (docx4j against the grid Word wrote):");
		for (Row r : rows) report(r);
		if (!skips.isEmpty()) {
			System.out.println();
			System.out.println("Tables not compared:");
			for (String s : skips) System.out.println("  " + s);
		}

		System.out.println();
		System.out.printf(Locale.ROOT,
				"%d documents measured, %d failed to load or render, %d skipped on a table-count mismatch;"
				+ " %d tables compared, %d not comparable%n",
				docsSeen, docsFailed, docsMismatched, rows.size(), tablesSkipped);
		aggregate("all tables", rows);
		aggregate("where Word rewrote the grid", filter(rows, true));
		aggregate("where Word kept the declared grid", filter(rows, false));

		if (csv != null) {
			writeCsv(csv, rows);
			System.out.println();
			System.out.println("csv: " + csv);
		}
	}

	/** null where the two sides cannot be compared; the reason is added to {@code skips}. */
	private static Row compare(String id, int index, List<int[]> declared, int[] word, FoTable ours,
			List<String> skips) {
		if (ours.twips == null) {
			skips.add(id + " table " + index + ": " + ours.reason);
			return null;
		}
		if (word.length == 0 || ours.twips.length == 0) {
			skips.add(id + " table " + index + ": columns " + word.length + " vs " + ours.twips.length);
			return null;
		}
		if (word.length != ours.twips.length) {
			skips.add(id + " table " + index + ": columns " + word.length + " vs " + ours.twips.length);
			return null;
		}
		Row r = new Row();
		r.id = id;
		r.tableIndex = index;
		r.word = word;
		r.docx4j = new int[ours.twips.length];
		r.ratio = new double[word.length];
		r.maxAbsRatioErr = 0;
		boolean anyRatio = false;
		for (int j = 0; j < word.length; j++) {
			r.docx4j[j] = (int) Math.round(ours.twips[j]);
			r.wordTotal += word[j];
			r.docx4jTotal += r.docx4j[j];
			r.sumAbsDiffTwips += Math.abs(r.docx4j[j] - word[j]);
			if (word[j] > 0) {
				r.ratio[j] = r.docx4j[j] / (double) word[j];
				r.maxAbsRatioErr = Math.max(r.maxAbsRatioErr, Math.abs(r.ratio[j] - 1));
				anyRatio = true;
			} else {
				r.ratio[j] = Double.NaN;
			}
		}
		if (!anyRatio) {
			skips.add(id + " table " + index + ": Word's grid is all zeroes");
			return null;
		}
		int[] before = index <= declared.size() ? declared.get(index - 1) : new int[0];
		r.wordRewroteGrid = !Arrays.equals(before, word);
		return r;
	}

	private static void report(Row r) {
		System.out.printf(Locale.ROOT, "%-44s t%-3d %2d cols  maxErr %7.4f  sumAbs %7d tw"
				+ "  total %6d/%-6d  %s%n", r.id, r.tableIndex, r.word.length, r.maxAbsRatioErr,
				r.sumAbsDiffTwips, r.wordTotal, r.docx4jTotal,
				r.wordRewroteGrid ? "grid rewritten by Word" : "grid kept by Word");
		System.out.println("    word   " + Arrays.toString(r.word));
		System.out.println("    docx4j " + Arrays.toString(r.docx4j));
		System.out.println("    ratio  " + ratios(r.ratio));
	}

	private static List<Row> filter(List<Row> rows, boolean rewritten) {
		List<Row> out = new ArrayList<Row>();
		for (Row r : rows) if (r.wordRewroteGrid == rewritten) out.add(r);
		return out;
	}

	private static void aggregate(String label, List<Row> rows) {
		if (rows.isEmpty()) {
			System.out.printf(Locale.ROOT, "  %-34s no tables%n", label);
			return;
		}
		int within1 = 0, within5 = 0;
		double sum = 0;
		long twips = 0;
		double[] errs = new double[rows.size()];
		for (int i = 0; i < rows.size(); i++) {
			Row r = rows.get(i);
			errs[i] = r.maxAbsRatioErr;
			sum += r.maxAbsRatioErr;
			twips += r.sumAbsDiffTwips;
			if (r.maxAbsRatioErr <= 0.01) within1++;
			if (r.maxAbsRatioErr <= 0.05) within5++;
		}
		Arrays.sort(errs);
		double median = errs.length % 2 == 1 ? errs[errs.length / 2]
				: (errs[errs.length / 2 - 1] + errs[errs.length / 2]) / 2;
		System.out.printf(Locale.ROOT,
				"  %-34s %4d tables; within 1%% %4d (%.0f%%), within 5%% %4d (%.0f%%);"
				+ " maxAbsRatioErr median %.4f mean %.4f; total error %d tw%n",
				label, rows.size(), within1, 100.0 * within1 / rows.size(),
				within5, 100.0 * within5 / rows.size(), median, sum / rows.size(), twips);
	}

	private static void writeCsv(File csv, List<Row> rows) throws Exception {
		try (PrintWriter w = new PrintWriter(csv, "UTF-8")) {
			w.println("id,tableIndex,columns,wordRewroteGrid,maxAbsRatioErr,sumAbsDiffTwips,"
					+ "wordTotalTwips,docx4jTotalTwips,wordTwips,docx4jTwips,ratios");
			for (Row r : rows) {
				w.printf(Locale.ROOT, "%s,%d,%d,%s,%.6f,%d,%d,%d,%s,%s,%s%n",
						csvCell(r.id), r.tableIndex, r.word.length, r.wordRewroteGrid,
						r.maxAbsRatioErr, r.sumAbsDiffTwips, r.wordTotal, r.docx4jTotal,
						csvCell(join(r.word)), csvCell(join(r.docx4j)), csvCell(ratios(r.ratio)));
			}
		}
	}

	private static String csvCell(String s) {
		return s.indexOf(',') < 0 && s.indexOf('"') < 0 ? s : '"' + s.replace("\"", "\"\"") + '"';
	}

	private static String join(int[] a) {
		StringBuilder sb = new StringBuilder();
		for (int v : a) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(v);
		}
		return sb.toString();
	}

	private static String ratios(double[] a) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < a.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(Double.isNaN(a[i]) ? "-" : String.format(Locale.ROOT, "%.3f", a[i]));
		}
		return sb.append(']').toString();
	}

	/** Each table's w:tblGrid, in document order; an empty array where it has none. */
	private static List<int[]> grids(File docx) throws Exception {
		WordprocessingMLPackage pkg = Docx4J.load(docx);
		ClassFinder finder = new ClassFinder(Tbl.class);
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		List<int[]> out = new ArrayList<int[]>();
		for (Object o : finder.results) {
			Tbl tbl = (Tbl) o;
			if (tbl.getTblGrid() == null || tbl.getTblGrid().getGridCol() == null) {
				out.add(new int[0]);
				continue;
			}
			List<TblGridCol> cols = tbl.getTblGrid().getGridCol();
			int[] w = new int[cols.size()];
			for (int i = 0; i < w.length; i++) {
				BigInteger v = cols.get(i).getW();
				w[i] = v == null ? 0 : v.intValue();
			}
			out.add(w);
		}
		return out;
	}

	/**
	 * docx4j's columns for each table, in document order.  The document is rendered to XSL-FO
	 * in memory - the same call {@code Fidelity.renderOne} makes for the {@code .fo} half, so
	 * what is measured is what the harness renders - and the columns are read back off it.
	 */
	private static List<FoTable> render(File docx) throws Exception {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		try {
			Future<List<FoTable>> f = exec.submit(() -> {
				WordprocessingMLPackage pkg = Docx4J.load(docx);
				FOSettings fo = Docx4J.createFOSettings();
				fo.setOpcPackage(pkg);
				fo.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				Docx4J.toFO(fo, os, Docx4J.FLAG_NONE);
				return columns(os.toByteArray());
			});
			try {
				return f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				f.cancel(true);
				throw new Exception("no FO within " + TIMEOUT_SECONDS + "s");
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				throw cause instanceof Exception ? (Exception) cause : new Exception(cause);
			}
		} finally {
			exec.shutdownNow();
		}
	}

	/**
	 * The {@code fo:table-column} widths of each real table in the FO, in pre-order, twips.
	 *
	 * <p>Only {@code fo:flow} is walked: a header or footer is in {@code fo:static-content} and
	 * has no counterpart in the main document part's traversal, so counting it would put every
	 * table of the document out of step.
	 */
	private static List<FoTable> columns(byte[] fo) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(fo));
		List<FoTable> out = new ArrayList<FoTable>();
		collectFlows(doc.getDocumentElement(), out);
		return out;
	}

	private static void collectFlows(Node n, List<FoTable> out) {
		if (n instanceof Element && isFo((Element) n, "flow")) {
			collectTables(n, out);
			return;
		}
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) collectFlows(c, out);
	}

	private static void collectTables(Node n, List<FoTable> out) {
		if (n instanceof Element && isFo((Element) n, "table")) {
			FoTable t = widths((Element) n);
			if (t != null) out.add(t);
		}
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) collectTables(c, out);
	}

	/**
	 * null where this {@code fo:table} is not one of the document's tables.  The table writer
	 * numbers every column it makes ({@code TableWriter.applyColumnCustomAttributes}); the
	 * wrapper tables {@code WordLayoutFixups} builds to reserve the band beside a floating
	 * table or an image do not, and are not tables of the document.
	 */
	private static FoTable widths(Element table) {
		List<Element> cols = new ArrayList<Element>();
		boolean numbered = false;
		for (Node c = table.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (!(c instanceof Element) || !isFo((Element) c, "table-column")) continue;
			Element e = (Element) c;
			cols.add(e);
			if (e.getAttribute("column-number").length() > 0) numbered = true;
		}
		if (!numbered) return null;
		double[] w = new double[cols.size()];
		for (int i = 0; i < w.length; i++) {
			String v = cols.get(i).getAttribute("column-width");
			if (v.length() == 0) return new FoTable("column " + (i + 1) + " has no column-width");
			try {
				w[i] = twips(v);
			} catch (IllegalArgumentException e) {
				return new FoTable("column " + (i + 1) + " is " + v);
			}
		}
		return new FoTable(w);
	}

	private static boolean isFo(Element e, String local) {
		return FO_NS.equals(e.getNamespaceURI()) && local.equals(e.getLocalName());
	}

	/**
	 * An XSL-FO length in twips.  A percentage or a {@code proportional-column-width()} is a
	 * share of a width FOP resolves at layout time, so it states no width here and is refused.
	 */
	static double twips(String v) {
		String s = v.trim();
		if (s.endsWith("%") || s.startsWith("proportional-column-width")) {
			throw new IllegalArgumentException(v);
		}
		double f;
		if (s.endsWith("pt")) f = 20;
		else if (s.endsWith("in")) f = 1440;
		else if (s.endsWith("mm")) f = 1440 / 25.4;
		else if (s.endsWith("cm")) f = 1440 / 2.54;
		else if (s.endsWith("pc")) f = 240;
		else if (s.endsWith("px")) f = 15;              // CSS px, 96 to the inch
		else if (s.endsWith("em")) throw new IllegalArgumentException(v);
		else f = 20;                                     // a bare number is points
		String num = Character.isDigit(s.charAt(s.length() - 1)) || s.endsWith(".")
				? s : s.substring(0, s.length() - 2);
		try {
			return Double.parseDouble(num.trim()) * f;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(v);
		}
	}

	private static String firstLine(Throwable t) {
		String msg = t.getMessage();
		if (msg != null) {
			int nl = msg.indexOf('\n');
			msg = nl < 0 ? msg : msg.substring(0, nl);
		}
		return t.getClass().getSimpleName() + (msg == null ? "" : ": " + msg);
	}
}
