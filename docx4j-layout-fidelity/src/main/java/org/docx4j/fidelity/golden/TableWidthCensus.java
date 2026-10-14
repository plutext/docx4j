/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.fidelity.golden;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.finders.ClassFinder;
import org.docx4j.model.table.TableStyleConditions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

/**
 * CR-041 phase 0's corpus counts.  For every table of every original document paired with
 * Word's re-save (the re-saved {@code w:tblGrid} is Word's layout in twips, {@link GridDiff}):
 *
 * <ul>
 * <li>how far its grid is from the width the table states - an absolute {@code w:tblW}, or a
 *     percentage of the text column (plus the left and right cell margins below mode 15, the
 *     rule CR-041 finding 3 measured; the plain base is reported beside it) - and whether Word
 *     kept that grid or rewrote it, by direction and size of the disagreement, for autofit and
 *     fixed layout, and whether every column carries a preferred width (the case
 *     {@code gridIsAuthoritative} decides on today);</li>
 * <li>the right-aligned and {@code w:bidiVisual} tables (findings 1, 5);</li>
 * <li>tables whose look has a first row (or last row, or first or last column) and banding on,
 *     whose style chain bands but gives that condition no format of its own (finding 6), with
 *     and without a {@code w:cnfStyle} cache on their rows;</li>
 * <li>tables whose style chain gives a conditional {@code w:vAlign}, with or without a
 *     whole-table one (finding 7).</li>
 * </ul>
 *
 * <pre>java -cp ... org.docx4j.fidelity.golden.TableWidthCensus out.csv &lt;originalDir&gt; &lt;resavedDir&gt; [&lt;originalDir&gt; &lt;resavedDir&gt; ...]</pre>
 *
 * <p>The text column is the body's last section's (as {@link RowGridDiff}); a percentage table
 * in another section, a multi-column section or a cell is reported but not bucketed.</p>
 */
public final class TableWidthCensus {

	private TableWidthCensus() {
	}

	public static void main(String[] args) throws Exception {
		PrintWriter out = new PrintWriter(new File(args[0]), "UTF-8");
		out.println("corpus,id,table,mode,override,nested,fixed,tblWType,tblW,stated,statedPlain,gridSum,cols,"
				+ "everyColPref,ratio,ratioPlain,wordKept,wordSum,jc,bidiVisual,bandNoFormat,cnfStyle,condVAlign,wholeVAlign,"
				+ "textColumn,cellsSum,cellsAgreeTblW,cellsAgreeGrid");
		Map<String, Integer> summary = new TreeMap<String, Integer>();
		Map<String, TreeSet<String>> docs = new TreeMap<String, TreeSet<String>>();
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
				WordprocessingMLPackage pkg;
				List<int[]> word;
				try {
					pkg = Docx4J.load(orig);
					word = ColumnError.grids(resaved);
				} catch (Exception e) {
					System.out.println(id + ": cannot read - " + e);
					continue;
				}
				count(summary, docs, "docs", corpus + "/" + id);
				ClassFinder finder = new ClassFinder(Tbl.class);
				new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
				if (finder.results.size() != word.size()) {
					count(summary, docs, "docs skipped (table count differs)", corpus + "/" + id);
					continue;
				}
				int mode = DocumentSettingsPart.getCompatibilityMode(pkg);
				String override = overrideSetting(pkg);
				Map<Tbl, org.docx4j.wml.SectPr> sections = sections(pkg);
				for (int i = 0; i < finder.results.size(); i++) {
					Tbl tbl = (Tbl) finder.results.get(i);
					org.docx4j.wml.SectPr sp = sections.get(tbl);
					row(out, summary, docs, corpus, id, i + 1, tbl, word.get(i), pkg, mode, override,
							textColumn(sp), multiColumn(sp));
				}
			}
		}
		out.close();
		for (Map.Entry<String, Integer> e : summary.entrySet()) {
			TreeSet<String> d = docs.get(e.getKey());
			System.out.println(String.format("%-90s %6d tables  %4d docs", e.getKey(), e.getValue(), d.size())
					+ (d.size() <= 6 ? "  " + d : ""));
		}
	}

	private static void row(PrintWriter out, Map<String, Integer> summary, Map<String, TreeSet<String>> docs,
			String corpus, String id, int index, Tbl tbl, int[] word, WordprocessingMLPackage pkg, int mode,
			String override, int textColumn, boolean multiColumn) {
		String doc = corpus + "/" + id;
		CTTblPrBase tblPr = tbl.getTblPr();
		boolean nested = RowGridDiff.isNested(tbl);
		boolean fixed = tblPr != null && tblPr.getTblLayout() != null
				&& tblPr.getTblLayout().getType() == STTblLayoutType.FIXED;
		int[] grid = grid(tbl);
		int gridSum = 0;
		for (int g : grid) gridSum += g;
		String type = "";
		int tblW = 0;
		if (tblPr != null && tblPr.getTblW() != null) {
			type = tblPr.getTblW().getType() == null ? "dxa" : tblPr.getTblW().getType();
			tblW = tblPr.getTblW().getW() == null ? 0 : tblPr.getTblW().getW().intValue();
		}
		List<Style> chain = styleChain(pkg, tblPr);
		int[] margins = cellMargins(tblPr, chain);
		int stated = 0, statedPlain = 0;
		if ("dxa".equals(type) && tblW > 0) {
			stated = statedPlain = tblW;
		} else if ("pct".equals(type) && tblW > 0 && textColumn > 0 && !nested && !multiColumn) {
			statedPlain = (int) Math.round(textColumn * tblW / 5000.0);
			int base = mode < 15 ? textColumn + margins[0] + margins[1] : textColumn;
			stated = (int) Math.round(base * tblW / 5000.0);
		}
		boolean everyColPref = everyColumnPreferred(tbl, grid.length);
		boolean kept = word.length == grid.length && sameGrid(grid, word);
		int wordSum = 0;
		for (int w : word) wordSum += w;
		double ratio = stated > 0 && gridSum > 0 ? gridSum / (double) stated - 1 : Double.NaN;
		double ratioPlain = statedPlain > 0 && gridSum > 0 ? gridSum / (double) statedPlain - 1 : Double.NaN;

		String jc = tblPr != null && tblPr.getJc() != null && tblPr.getJc().getVal() != null
				? tblPr.getJc().getVal().value() : styleJc(chain);
		boolean bidiVisual = tblPr != null && tblPr.getBidiVisual() != null && tblPr.getBidiVisual().isVal();
		Set<String> conds = conditions(chain);
		TableStyleConditions.Look look = TableStyleConditions.look(tblPr);
		boolean bands = (look.hBand && (conds.contains("BAND_1_HORZ") || conds.contains("BAND_2_HORZ")))
				|| (look.vBand && (conds.contains("BAND_1_VERT") || conds.contains("BAND_2_VERT")));
		String bandNoFormat = "";
		if (bands) {
			if (look.hBand && look.firstRow && !conds.contains("FIRST_ROW")) bandNoFormat += "firstRow ";
			if (look.hBand && look.lastRow && !conds.contains("LAST_ROW")) bandNoFormat += "lastRow ";
			if (look.vBand && look.firstColumn && !conds.contains("FIRST_COL")) bandNoFormat += "firstCol ";
			if (look.vBand && look.lastColumn && !conds.contains("LAST_COL")) bandNoFormat += "lastCol ";
			bandNoFormat = bandNoFormat.trim();
		}
		boolean cnfStyle = hasCnfStyle(tbl);
		int cellsSum = widestDeclaredRow(tbl);
		boolean cellsAgreeTblW = cellsSum > 0 && stated > 0 && Math.abs(cellsSum - stated) * 100 <= stated;
		boolean cellsAgreeGrid = cellsSum > 0 && gridSum > 0 && Math.abs(cellsSum - gridSum) * 100 <= gridSum;
		boolean condVAlign = conditionalVAlign(chain);
		boolean wholeVAlign = wholeTableVAlign(chain);

		out.println(String.join(",", corpus, id, Integer.toString(index), Integer.toString(mode), override,
				Boolean.toString(nested), Boolean.toString(fixed), type, Integer.toString(tblW),
				Integer.toString(stated), Integer.toString(statedPlain), Integer.toString(gridSum),
				Integer.toString(grid.length), Boolean.toString(everyColPref),
				Double.isNaN(ratio) ? "" : String.format("%.4f", ratio),
				Double.isNaN(ratioPlain) ? "" : String.format("%.4f", ratioPlain),
				Boolean.toString(kept), Integer.toString(wordSum), jc == null ? "" : jc,
				Boolean.toString(bidiVisual), bandNoFormat, Boolean.toString(cnfStyle),
				Boolean.toString(condVAlign), Boolean.toString(wholeVAlign), Integer.toString(textColumn),
				Integer.toString(cellsSum), Boolean.toString(cellsAgreeTblW), Boolean.toString(cellsAgreeGrid)));

		count(summary, docs, "a. tables", doc);
		if (grid.length == 0) {
			count(summary, docs, "a. tables without a grid", doc);
		}
		if (!Double.isNaN(ratio) && grid.length > 0) {
			String key = "b. " + type + " w:tblW " + (fixed ? "fixed" : "autofit") + (nested ? " NESTED" : "")
					+ (everyColPref ? ", every column preferred" : ", not every column preferred")
					+ ", grid " + bucket(ratio)
					+ (Math.abs(ratio) > 0.02 ? (cellsAgreeTblW ? ", cells = w:tblW" : cellsAgreeGrid ? ", cells = grid" : ", cells neither") : "")
					+ (kept ? ": Word kept" : ": Word rewrote");
			count(summary, docs, key, doc);
			if ("pct".equals(type) && mode < 15 && !Double.isNaN(ratioPlain)) {
				count(summary, docs, "c. pct below mode 15 on the plain base, grid " + bucket(ratioPlain)
						+ (kept ? ": Word kept" : ": Word rewrote"), doc);
			}
		} else if ("pct".equals(type) && grid.length > 0) {
			count(summary, docs, "b. pct w:tblW not bucketed (nested, multi-column or no text column)", doc);
		}
		if (jc != null && !"left".equals(jc) && !"start".equals(jc)) {
			count(summary, docs, "d. w:jc " + jc + (bidiVisual ? " (bidiVisual)" : "") + (nested ? " (nested)" : ""), doc);
		}
		if (bidiVisual) count(summary, docs, "d. bidiVisual" + (jc == null ? ", no w:jc" : ""), doc);
		if (!bandNoFormat.isEmpty()) {
			count(summary, docs, "e. bands on, no format for " + bandNoFormat + (cnfStyle ? " (w:cnfStyle cached)" : " (no cache)"), doc);
		}
		if (condVAlign) {
			count(summary, docs, "f. conditional w:vAlign" + (wholeVAlign ? " with" : " without") + " a whole-table one, mode "
					+ (mode < 15 ? "<15" : "15"), doc);
		}
	}

	private static String bucket(double r) {
		double p = r * 100;
		if (p <= -5) return "<= -5%";
		if (p <= -2) return "-5% to -2%";
		if (p < -1) return "-2% to -1%";
		if (p <= 1) return "within 1%";
		if (p < 2) return "1% to 2%";
		if (p < 5) return "2% to 5%";
		return ">= 5%";
	}

	private static void count(Map<String, Integer> summary, Map<String, TreeSet<String>> docs, String key, String doc) {
		summary.merge(key, 1, Integer::sum);
		docs.computeIfAbsent(key, k -> new TreeSet<String>()).add(doc);
	}

	private static int[] grid(Tbl tbl) {
		if (tbl.getTblGrid() == null || tbl.getTblGrid().getGridCol() == null) return new int[0];
		List<TblGridCol> cols = tbl.getTblGrid().getGridCol();
		int[] g = new int[cols.size()];
		for (int i = 0; i < g.length; i++) {
			BigInteger v = cols.get(i).getW();
			g[i] = v == null ? 0 : v.intValue();
		}
		return g;
	}

	/** Word kept the grid: the same columns, each within max(10 twips, 1%). */
	private static boolean sameGrid(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++) {
			if (Math.abs(a[i] - b[i]) > Math.max(10, a[i] / 100)) return false;
		}
		return true;
	}

	/** Every grid column has a non-spanning cell somewhere stating a dxa or pct width. */
	private static boolean everyColumnPreferred(Tbl tbl, int cols) {
		if (cols == 0) return false;
		boolean[] pref = new boolean[cols];
		for (Object ro : tbl.getContent()) {
			Object r = org.docx4j.XmlUtils.unwrap(ro);
			if (!(r instanceof Tr)) continue;
			Tr tr = (Tr) r;
			int c = 0;
			if (tr.getTrPr() != null) {
				for (Object x : tr.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
					Object v = org.docx4j.XmlUtils.unwrap(x);
					if (v instanceof org.docx4j.wml.CTTrPrBase.GridBefore && ((org.docx4j.wml.CTTrPrBase.GridBefore) v).getVal() != null) {
						c = ((org.docx4j.wml.CTTrPrBase.GridBefore) v).getVal().intValue();
					}
				}
			}
			for (Object co : tr.getContent()) {
				Object cell = org.docx4j.XmlUtils.unwrap(co);
				if (!(cell instanceof Tc)) continue;
				Tc tc = (Tc) cell;
				int span = tc.getTcPr() != null && tc.getTcPr().getGridSpan() != null
						&& tc.getTcPr().getGridSpan().getVal() != null ? tc.getTcPr().getGridSpan().getVal().intValue() : 1;
				TblWidth w = tc.getTcPr() == null ? null : tc.getTcPr().getTcW();
				if (span == 1 && c < cols && w != null && w.getW() != null && w.getW().intValue() > 0
						&& (w.getType() == null || "dxa".equals(w.getType()) || "pct".equals(w.getType()))) {
					pref[c] = true;
				}
				c += Math.max(1, span);
			}
		}
		for (boolean b : pref) if (!b) return false;
		return true;
	}

	private static String overrideSetting(WordprocessingMLPackage pkg) {
		try {
			CTCompatSetting cs = pkg.getMainDocumentPart().getDocumentSettingsPart()
					.getWordCompatSetting("overrideTableStyleFontSizeAndJustification");
			return cs == null ? "absent" : cs.getVal();
		} catch (Exception e) {
			return "absent";
		}
	}

	/** Each top-level body table's section: the next w:sectPr after it in body order, else the body's. */
	private static Map<Tbl, org.docx4j.wml.SectPr> sections(WordprocessingMLPackage pkg) {
		Map<Tbl, org.docx4j.wml.SectPr> out = new java.util.IdentityHashMap<Tbl, org.docx4j.wml.SectPr>();
		org.docx4j.wml.Body body = pkg.getMainDocumentPart().getJaxbElement().getBody();
		List<Tbl> pending = new ArrayList<Tbl>();
		collect(body.getContent(), pending, out);
		for (Tbl t : pending) out.put(t, body.getSectPr());
		return out;
	}

	private static void collect(List<Object> content, List<Tbl> pending, Map<Tbl, org.docx4j.wml.SectPr> out) {
		for (Object o : content) {
			Object u = org.docx4j.XmlUtils.unwrap(o);
			if (u instanceof Tbl) {
				pending.add((Tbl) u);
			} else if (u instanceof org.docx4j.wml.P) {
				org.docx4j.wml.P p = (org.docx4j.wml.P) u;
				if (p.getPPr() != null && p.getPPr().getSectPr() != null) {
					for (Tbl t : pending) out.put(t, p.getPPr().getSectPr());
					pending.clear();
				}
			} else if (u instanceof org.docx4j.wml.SdtBlock && ((org.docx4j.wml.SdtBlock) u).getSdtContent() != null) {
				collect(((org.docx4j.wml.SdtBlock) u).getSdtContent().getContent(), pending, out);
			}
		}
	}

	private static int textColumn(org.docx4j.wml.SectPr s) {
		if (s == null || s.getPgSz() == null || s.getPgSz().getW() == null) return -1;
		int w = s.getPgSz().getW().intValue();
		if (s.getPgMar() != null) {
			if (s.getPgMar().getLeft() != null) w -= s.getPgMar().getLeft().intValue();
			if (s.getPgMar().getRight() != null) w -= s.getPgMar().getRight().intValue();
			if (s.getPgMar().getGutter() != null) w -= s.getPgMar().getGutter().intValue();
		}
		return w;
	}

	/** Equal columns (w:num) or unequal ones (w:col children). */
	private static boolean multiColumn(org.docx4j.wml.SectPr s) {
		return s != null && s.getCols() != null
				&& ((s.getCols().getNum() != null && s.getCols().getNum().intValue() > 1) || s.getCols().getCol().size() > 1);
	}

	/** The table's style and the styles it is based on, nearest first; the default table style where it names none. */
	private static List<Style> styleChain(WordprocessingMLPackage pkg, CTTblPrBase tblPr) {
		List<Style> chain = new ArrayList<Style>();
		try {
			org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart sdp = pkg.getMainDocumentPart().getStyleDefinitionsPart();
			if (sdp == null) return chain;
			String id = tblPr != null && tblPr.getTblStyle() != null ? tblPr.getTblStyle().getVal() : null;
			Style s = id == null ? sdp.getDefaultTableStyle() : sdp.getStyleById(id);
			Set<String> seen = new HashSet<String>();
			while (s != null && seen.add(s.getStyleId())) {
				chain.add(s);
				s = s.getBasedOn() == null ? null : sdp.getStyleById(s.getBasedOn().getVal());
			}
		} catch (Exception e) {
			// no styles: an empty chain
		}
		return chain;
	}

	private static int[] cellMargins(CTTblPrBase tblPr, List<Style> chain) {
		int[] m = { -1, -1 };
		List<CTTblPrBase> levels = new ArrayList<CTTblPrBase>();
		if (tblPr != null) levels.add(tblPr);
		for (Style s : chain) if (s.getTblPr() != null) levels.add(s.getTblPr());
		for (CTTblPrBase p : levels) {
			if (p.getTblCellMar() == null) continue;
			if (m[0] < 0 && p.getTblCellMar().getLeft() != null && p.getTblCellMar().getLeft().getW() != null) {
				m[0] = p.getTblCellMar().getLeft().getW().intValue();
			}
			if (m[1] < 0 && p.getTblCellMar().getRight() != null && p.getTblCellMar().getRight().getW() != null) {
				m[1] = p.getTblCellMar().getRight().getW().intValue();
			}
		}
		if (m[0] < 0) m[0] = 108;
		if (m[1] < 0) m[1] = 108;
		return m;
	}

	private static String styleJc(List<Style> chain) {
		for (Style s : chain) {
			if (s.getTblPr() != null && s.getTblPr().getJc() != null && s.getTblPr().getJc().getVal() != null) {
				JcEnumeration v = s.getTblPr().getJc().getVal();
				return v.value();
			}
		}
		return null;
	}

	/** The conditional formats the chain gives any properties at all. */
	private static Set<String> conditions(List<Style> chain) {
		Set<String> out = new HashSet<String>();
		for (Style s : chain) {
			for (CTTblStylePr c : s.getTblStylePr()) {
				if (c.getType() == null) continue;
				if (c.getPPr() != null || c.getRPr() != null || c.getTblPr() != null || c.getTrPr() != null || c.getTcPr() != null) {
					out.add(c.getType().name());
				}
			}
		}
		return out;
	}

	private static boolean conditionalVAlign(List<Style> chain) {
		for (Style s : chain) {
			for (CTTblStylePr c : s.getTblStylePr()) {
				if (c.getTcPr() != null && c.getTcPr().getVAlign() != null) return true;
			}
		}
		return false;
	}

	private static boolean wholeTableVAlign(List<Style> chain) {
		for (Style s : chain) {
			if (s.getTcPr() != null && s.getTcPr().getVAlign() != null) return true;
		}
		return false;
	}

	/** The widest sum of a row whose every cell states a dxa w:tcW (with w:gridBefore/w:wBefore ignored); 0 where none. */
	private static int widestDeclaredRow(Tbl tbl) {
		int best = 0;
		for (Object ro : tbl.getContent()) {
			Object r = org.docx4j.XmlUtils.unwrap(ro);
			if (!(r instanceof Tr)) continue;
			int sum = 0;
			boolean all = true, any = false;
			for (Object co : ((Tr) r).getContent()) {
				Object cell = org.docx4j.XmlUtils.unwrap(co);
				if (!(cell instanceof Tc)) continue;
				any = true;
				TblWidth w = ((Tc) cell).getTcPr() == null ? null : ((Tc) cell).getTcPr().getTcW();
				if (w == null || w.getW() == null || w.getW().intValue() <= 0
						|| !(w.getType() == null || "dxa".equals(w.getType()))) {
					all = false;
					break;
				}
				sum += w.getW().intValue();
			}
			if (any && all) best = Math.max(best, sum);
		}
		return best;
	}

	private static boolean hasCnfStyle(Tbl tbl) {
		for (Object ro : tbl.getContent()) {
			Object r = org.docx4j.XmlUtils.unwrap(ro);
			if (!(r instanceof Tr)) continue;
			Tr tr = (Tr) r;
			if (tr.getTrPr() != null) {
				for (Object x : tr.getTrPr().getCnfStyleOrDivIdOrGridBefore()) {
					if (org.docx4j.XmlUtils.unwrap(x) instanceof org.docx4j.wml.CTCnf) return true;
				}
			}
			for (Object co : tr.getContent()) {
				Object cell = org.docx4j.XmlUtils.unwrap(co);
				if (cell instanceof Tc && ((Tc) cell).getTcPr() != null && ((Tc) cell).getTcPr().getCnfStyle() != null) return true;
			}
		}
		return false;
	}
}
