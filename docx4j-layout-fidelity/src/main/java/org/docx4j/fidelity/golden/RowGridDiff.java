package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.finders.ClassFinder;
import org.docx4j.finders.TcFinder;
import org.docx4j.model.table.TableModel.TrFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTrPrBase;


import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

/**
 * Where a row's {@code w:tcW} widths disagree with the table's {@code w:tblGrid}, what does
 * Word draw?  The grid, the row's own widths, or something else - and is it one odd row or
 * the whole table?
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.RowGridDiff out.csv &lt;originalDir&gt; &lt;resavedDir&gt; [&lt;originalDir&gt; &lt;resavedDir&gt; ...]</pre>
 *
 * <p>For every table of every original document, each row's cell boundaries are computed from
 * its {@code w:tcW} (with {@code w:gridBefore}/{@code w:wBefore} and the spans) and compared
 * with the boundaries of the grid columns the cells cover.  A row <em>disagrees</em> when any of
 * its cells states a {@code dxa} width more than max(10 twips, 1%) away from the grid columns it
 * spans.  The reference for what Word drew is the {@code w:tblGrid} of the re-saved document,
 * which {@link GridDiff} established is Word's own layout in twips and which the golden PDF's
 * cell rules reproduce exactly.  Word has one grid per table, so had it laid a row out on its
 * own boundaries the re-saved grid would carry the <em>union</em> of the rows' boundaries and the
 * cells new {@code w:gridSpan}s; a grid whose column count did not change is one geometry for
 * every row.
 *
 * <p>Word's grid is compared, as a set of interior boundaries, with four candidates - the
 * declared grid, the widest disagreeing row, the union of every declared row's boundaries, and
 * the widest row's widths scaled to Word's total - by the Hausdorff distance between the
 * boundary sets in twips, so that grids of different column counts can still be compared.
 */
public final class RowGridDiff {

	static final class RowGeom {
		int rowIndex;
		int[] tcW;      // dxa twips; -1 auto or absent; -2 pct or other unit
		int[] start;    // first grid column each cell covers
		int[] span;
		int wBefore, wAfter, gridBefore, gridAfter;
		boolean declared;      // every cell states a dxa width
		boolean anyPct;
		boolean agrees;        // every declared cell within tolerance of the grid columns it spans
		int sum;               // wBefore + tcW + wAfter (declared rows only)
		int maxCellDiff;       // max |tcW - grid span| over declared cells
		boolean uncovered;     // a cell lies beyond the grid's last column
		int[] boundaries;      // interior boundaries from the widths (declared rows only)
	}

	static final class TableGeom {
		int index;
		boolean nested, fixed;
		String tblWType;
		int tblW;
		int[] grid;
		List<RowGeom> rows = new ArrayList<RowGeom>();
		int cols;
		boolean everyColumnPreferred; // gridIsAuthoritative's first clause, approximately
	}

	public static void main(String[] args) throws Exception {
		File csv = new File(args[0]);
		PrintWriter out = new PrintWriter(csv, "UTF-8");
		out.println("corpus,id,table,nested,fixed,tblWType,tblW,cols,rows,declaredRows,pctRows,disagreeRows,"
				+ "distinctGeoms,gridSum,widestRowSum,textColumn,everyColPref,wordKept,wordCols,wordSum,"
				+ "hGrid,hRow,hUnion,hRowScaled,hGridScaled,maxCellDiff,uncoveredRows");
		Map<String, Integer> summary = new TreeMap<String, Integer>();
		Map<String, TreeSet<String>> summaryDocs = new TreeMap<String, TreeSet<String>>();
		List<String> lines = new ArrayList<String>();
		for (int a = 1; a + 1 < args.length; a += 2) {
			File origDir = new File(args[a]), resavedDir = new File(args[a + 1]);
			String corpus = origDir.getParentFile() == null ? origDir.getName() : origDir.getParentFile().getName();
			File[] files = origDir.listFiles((d, n) -> n.endsWith(".docx") && !n.startsWith("~"));
			if (files == null) throw new IllegalArgumentException("no docx in " + origDir);
			Arrays.sort(files);
			for (File orig : files) {
				String id = orig.getName().replaceAll("\\.docx$", "");
				File resaved = new File(resavedDir, id + ".docx");
				if (!resaved.exists() || resaved.length() == 0) continue;
				List<TableGeom> a1;
				List<int[]> word;
				int textColumn;
				try {
					WordprocessingMLPackage pkg = Docx4J.load(orig);
					textColumn = textColumn(pkg);
					a1 = tables(pkg);
					word = ColumnError.grids(resaved);
				} catch (Exception e) {
					System.out.println(id + ": cannot read - " + e);
					continue;
				}
				count(summary, summaryDocs, "docs", id);
				if (a1.size() != word.size()) {
					count(summary, summaryDocs, "docs skipped (table count differs)", id);
					continue;
				}
				for (int i = 0; i < a1.size(); i++) {
					TableGeom t = a1.get(i);
					int[] w = word.get(i);
					count(summary, summaryDocs, "tables", id);
					if (t.grid == null || t.grid.length == 0) {
						count(summary, summaryDocs, "tables without a grid", id);
						continue;
					}
					int declaredRows = 0, pctRows = 0, disagree = 0, widestSum = 0, maxCellDiff = 0, uncoveredRows = 0;
					RowGeom widest = null;
					TreeSet<String> geoms = new TreeSet<String>();
					TreeSet<Integer> union = new TreeSet<Integer>();
					for (RowGeom r : t.rows) {
						if (r.anyPct) pctRows++;
						if (r.uncovered) uncoveredRows++;
						if (!r.declared) continue;
						declaredRows++;
						if (!r.agrees) {
							disagree++;
							geoms.add(key(r.boundaries));
							if (widest == null || r.sum > widest.sum) widest = r;
						}
						if (r.sum > widestSum) widestSum = r.sum;
						maxCellDiff = Math.max(maxCellDiff, r.maxCellDiff);
						for (int b : r.boundaries) union.add(b);
					}
					int gridSum = 0;
					for (int g : t.grid) gridSum += g;
					boolean wordKept = w.length == t.grid.length;
					if (wordKept) for (int k = 0; k < w.length; k++) if (Math.abs(w[k] - t.grid[k]) > 2) wordKept = false;
					int wordSum = 0;
					for (int g : w) wordSum += g;
					String cls = (t.fixed ? "fixed" : "autofit") + " " + (t.nested ? "nested" : "top")
							+ " tblW=" + (t.tblWType == null ? "none" : t.tblWType);
					if (disagree == 0) {
						count(summary, summaryDocs, "tables whose declared rows all agree with the grid", id);
						continue;
					}
					count(summary, summaryDocs, "TABLES with a disagreeing row", id);
					count(summary, summaryDocs, "  " + cls, id);
					if (uncoveredRows > 0) count(summary, summaryDocs, "  grid has fewer columns than a row has cells", id);
					count(summary, summaryDocs, "  rows: " + (disagree == declaredRows ? "every declared row disagrees"
							: disagree == 1 ? "exactly one row disagrees" : "some rows disagree"), id);
					count(summary, summaryDocs, "  geometries among disagreeing rows: " + (geoms.size() == 1 ? "one" : "several"), id);
					count(summary, summaryDocs, "  Word " + (wordKept ? "kept the grid" : w.length == t.grid.length
							? "rewrote the grid, same column count" : "rewrote the grid, " + t.grid.length + " -> " + w.length + " columns"), id);
					double hGrid = hausdorff(boundaries(w), boundaries(t.grid));
					double hRow = widest == null ? -1 : hausdorff(boundaries(w), widest.boundaries);
					int[] u = new int[union.size()];
					int k = 0;
					for (int b : union) u[k++] = b;
					double hUnion = hausdorff(boundaries(w), u);
					double hRowScaled = widest == null ? -1 : hausdorff(boundaries(w), scaled(widest.boundaries, widest.sum, wordSum));
					double hGridScaled = hausdorff(boundaries(w), scaled(boundaries(t.grid), gridSum, wordSum));
					if (!wordKept) {
						String closest;
						double best = Math.min(Math.min(hGrid, hRow < 0 ? 1e9 : hRow), Math.min(hUnion, hRowScaled < 0 ? 1e9 : hRowScaled));
						if (best > 0.01 * wordSum) closest = "none of the candidates (within 1%)";
						else if (best == hRow) closest = "the widest disagreeing row's own widths";
						else if (best == hUnion) closest = "the union of the rows' boundaries";
						else if (best == hRowScaled) closest = "the row's widths scaled to Word's total";
						else closest = "the declared grid (scaled)";
						count(summary, summaryDocs, "    rewritten grid closest to " + closest, id);
					}
					lines.add(String.join(",", corpus, ColumnError.csvCell(id), "" + t.index, "" + t.nested, "" + t.fixed,
							"" + t.tblWType, "" + t.tblW, "" + t.grid.length, "" + t.rows.size(), "" + declaredRows,
							"" + pctRows, "" + disagree, "" + geoms.size(), "" + gridSum, "" + widestSum, "" + textColumn,
							"" + t.everyColumnPreferred, "" + wordKept, "" + w.length, "" + wordSum,
							fmt(hGrid), fmt(hRow), fmt(hUnion), fmt(hRowScaled), fmt(hGridScaled), "" + maxCellDiff, "" + uncoveredRows));
				}
			}
		}
		for (String l : lines) out.println(l);
		out.close();
		System.out.println();
		for (Map.Entry<String, Integer> e : summary.entrySet()) {
			System.out.printf(Locale.ROOT, "%6d  %-70s in %d documents%n", e.getValue(), e.getKey(), summaryDocs.get(e.getKey()).size());
		}
		System.out.println("csv: " + csv);
	}

	static void count(Map<String, Integer> m, Map<String, TreeSet<String>> docs, String k, String id) {
		m.merge(k, 1, Integer::sum);
		docs.computeIfAbsent(k, x -> new TreeSet<String>()).add(id);
	}

	static String fmt(double d) {
		return d < 0 ? "" : String.format(Locale.ROOT, "%.0f", d);
	}

	static String key(int[] b) {
		StringBuilder sb = new StringBuilder();
		for (int x : b) sb.append(x / 10).append(' ');
		return sb.toString();
	}

	/** Interior boundaries of a width vector. */
	static int[] boundaries(int[] widths) {
		int[] b = new int[Math.max(0, widths.length - 1)];
		int x = 0;
		for (int i = 0; i < b.length; i++) {
			x += widths[i];
			b[i] = x;
		}
		return b;
	}

	static int[] scaled(int[] boundaries, int from, int to) {
		int[] out = new int[boundaries.length];
		for (int i = 0; i < out.length; i++) out[i] = from <= 0 ? boundaries[i] : (int) Math.round(boundaries[i] * (double) to / from);
		return out;
	}

	/** Hausdorff distance between two boundary sets, in twips; 0 where both are empty. */
	static double hausdorff(int[] a, int[] b) {
		if (a.length == 0 && b.length == 0) return 0;
		if (a.length == 0 || b.length == 0) return 1e9;
		double h = 0;
		for (int x : a) {
			double d = 1e9;
			for (int y : b) d = Math.min(d, Math.abs(x - y));
			h = Math.max(h, d);
		}
		for (int y : b) {
			double d = 1e9;
			for (int x : a) d = Math.min(d, Math.abs(x - y));
			h = Math.max(h, d);
		}
		return h;
	}

	static int textColumn(WordprocessingMLPackage pkg) {
		try {
			SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
			if (sectPr == null) return -1;
			SectPr.PgSz sz = sectPr.getPgSz();
			SectPr.PgMar mar = sectPr.getPgMar();
			if (sz == null || sz.getW() == null) return -1;
			int w = sz.getW().intValue();
			if (mar != null) {
				if (mar.getLeft() != null) w -= mar.getLeft().intValue();
				if (mar.getRight() != null) w -= mar.getRight().intValue();
			}
			return w;
		} catch (Exception e) {
			return -1;
		}
	}

	static List<TableGeom> tables(WordprocessingMLPackage pkg) {
		ClassFinder finder = new ClassFinder(Tbl.class);
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		List<TableGeom> out = new ArrayList<TableGeom>();
		int index = 0;
		for (Object o : finder.results) {
			Tbl tbl = (Tbl) o;
			TableGeom t = new TableGeom();
			t.index = ++index;
			t.nested = isNested(tbl);
			CTTblPrBase tblPr = tbl.getTblPr();
			t.fixed = tblPr != null && tblPr.getTblLayout() != null && tblPr.getTblLayout().getType() == STTblLayoutType.FIXED;
			if (tblPr != null && tblPr.getTblW() != null) {
				t.tblWType = tblPr.getTblW().getType() == null ? "dxa" : tblPr.getTblW().getType();
				t.tblW = tblPr.getTblW().getW() == null ? 0 : tblPr.getTblW().getW().intValue();
			}
			if (tbl.getTblGrid() != null && tbl.getTblGrid().getGridCol() != null) {
				List<TblGridCol> cols = tbl.getTblGrid().getGridCol();
				t.grid = new int[cols.size()];
				for (int i = 0; i < t.grid.length; i++) {
					BigInteger v = cols.get(i).getW();
					t.grid[i] = v == null ? 0 : v.intValue();
				}
			}
			t.cols = t.grid == null ? 0 : t.grid.length;
			TrFinder trFinder = new TrFinder();
			new TraversalUtil(tbl, trFinder);
			boolean[] colPref = new boolean[t.cols];
			int r = 0;
			for (Tr tr : trFinder.getTrList()) {
				RowGeom g = row(tr, t.grid);
				g.rowIndex = r++;
				t.rows.add(g);
				for (int c = 0; c < g.tcW.length; c++) {
					if (g.span[c] == 1 && g.start[c] < t.cols && (g.tcW[c] > 0 || g.tcW[c] == -2)) colPref[g.start[c]] = true;
				}
			}
			t.everyColumnPreferred = t.cols > 0;
			for (boolean p : colPref) t.everyColumnPreferred &= p;
			out.add(t);
		}
		return out;
	}

	static boolean isNested(Tbl tbl) {
		Object p = tbl.getParent();
		while (p != null) {
			if (p instanceof Tc) return true;
			if (p instanceof org.jvnet.jaxb.lang.Child) p = ((org.jvnet.jaxb.lang.Child) p).getParent();
			else return false;
		}
		return false;
	}

	static RowGeom row(Tr tr, int[] grid) {
		RowGeom g = new RowGeom();
		g.gridBefore = decimal(tr, "gridBefore");
		g.gridAfter = decimal(tr, "gridAfter");
		g.wBefore = width(tr, "wBefore");
		g.wAfter = width(tr, "wAfter");
		TcFinder tcFinder = new TcFinder();
		new TraversalUtil(tr, tcFinder);
		int n = tcFinder.tcList.size();
		g.tcW = new int[n];
		g.start = new int[n];
		g.span = new int[n];
		g.declared = n > 0;
		g.agrees = true;
		int col = g.gridBefore;
		int sum = g.wBefore + g.wAfter;
		List<Integer> bounds = new ArrayList<Integer>();
		int x = g.wBefore;
		for (int i = 0; i < n; i++) {
			Tc tc = tcFinder.tcList.get(i);
			int span = 1;
			if (tc.getTcPr() != null && tc.getTcPr().getGridSpan() != null && tc.getTcPr().getGridSpan().getVal() != null) {
				span = Math.max(1, tc.getTcPr().getGridSpan().getVal().intValue());
			}
			TblWidth tcW = tc.getTcPr() == null ? null : tc.getTcPr().getTcW();
			int w = -1;
			if (tcW != null && tcW.getW() != null && tcW.getW().intValue() > 0) {
				if (tcW.getType() == null || "dxa".equals(tcW.getType())) w = tcW.getW().intValue();
				else if (!"auto".equals(tcW.getType())) { w = -2; g.anyPct = true; }
			}
			g.tcW[i] = w;
			g.start[i] = col;
			g.span[i] = span;
			if (w <= 0) g.declared = false;
			else {
				sum += w;
				x += w;
				if (i < n - 1) bounds.add(x);
				if (grid != null) {
					int gw = 0;
					boolean covered = col + span <= grid.length;
					for (int c = col; c < col + span && c < grid.length; c++) gw += grid[c];
					int tol = Math.max(10, gw / 100);
					int diff = Math.abs(w - gw);
					if (!covered || diff > tol) g.agrees = false;
					if (!covered) g.uncovered = true;
					g.maxCellDiff = Math.max(g.maxCellDiff, covered ? diff : w);
				}
			}
			col += span;
		}
		g.sum = sum;
		g.boundaries = new int[bounds.size()];
		for (int i = 0; i < g.boundaries.length; i++) g.boundaries[i] = bounds.get(i);
		if (!g.declared) g.agrees = true; // only a declared row can disagree
		return g;
	}

	static int decimal(Tr tr, String local) {
		JAXBElement<?> e = element(tr, local);
		if (e == null) return 0;
		Object v = e.getValue();
		if (v instanceof CTTrPrBase.GridBefore) return ((CTTrPrBase.GridBefore) v).getVal().intValue();
		if (v instanceof CTTrPrBase.GridAfter) return ((CTTrPrBase.GridAfter) v).getVal().intValue();
		return 0;
	}

	static int width(Tr tr, String local) {
		JAXBElement<?> e = element(tr, local);
		if (e == null || !(e.getValue() instanceof TblWidth)) return 0;
		TblWidth w = (TblWidth) e.getValue();
		if (w.getW() == null || (w.getType() != null && !"dxa".equals(w.getType()))) return 0;
		return w.getW().intValue();
	}

	static JAXBElement<?> element(Tr tr, String local) {
		if (tr.getTrPr() == null) return null;
		for (JAXBElement<?> e : tr.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
			if (local.equals(e.getName().getLocalPart())) return e;
		}
		return null;
	}
}
