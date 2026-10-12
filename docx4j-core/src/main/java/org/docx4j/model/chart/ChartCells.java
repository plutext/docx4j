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
package org.docx4j.model.chart;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.Sheet;

/**
 * A chart's data from its cells, beside its caches (CR-035 §4.2, D7): the caches are what Office
 * draws until the data is edited, the cells what the data now is.  The workbook is the chart's
 * own package where that is an xlsx, else the workbook embedded with the chart
 * ({@code c:externalData}), as Word and PowerPoint keep it.
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartCells {

	/** A point whose cache and cell differ */
	public static final class Difference {
		private final ChartSeries series;
		private final String data;
		private final int idx;
		private final String cache, cell;

		Difference(ChartSeries series, String data, int idx, String cache, String cell) {
			this.series = series;
			this.data = data;
			this.idx = idx;
			this.cache = cache;
			this.cell = cell;
		}

		public ChartSeries getSeries() {
			return series;
		}

		/** name, categories, values, xValues, yValues or bubbleSizes */
		public String getData() {
			return data;
		}

		/** The point's index */
		public int getIdx() {
			return idx;
		}

		/** The cache's text; null for a gap */
		public String getCache() {
			return cache;
		}

		/** The cell's text; null for an empty cell */
		public String getCell() {
			return cell;
		}

		@Override
		public String toString() {
			return "series " + series.getIdx() + " " + data + "[" + idx + "]: cache " + cache + ", cell " + cell;
		}
	}

	private final SpreadsheetMLPackage workbook;
	private final Map<String, Map<String, Cell>> sheets = new HashMap<String, Map<String, Cell>>();
	private List<CTRst> sharedStrings;

	private ChartCells(SpreadsheetMLPackage workbook) {
		this.workbook = workbook;
	}

	/** The cells of a workbook */
	public static ChartCells of(SpreadsheetMLPackage workbook) {
		return new ChartCells(workbook);
	}

	/**
	 * The cells a chart's data is in: its own package where that is an xlsx, else the workbook
	 * embedded with it; null where it has none (a linked workbook, or none at all)
	 */
	public static ChartCells of(Chart chart) throws Docx4JException {
		OpcPackage pkg = chart.getPackage();
		if (pkg instanceof SpreadsheetMLPackage) return new ChartCells((SpreadsheetMLPackage) pkg);
		org.docx4j.dml.chart.CTExternalData ext = chart.getContents().getExternalData();
		if (ext == null || ext.getId() == null || chart.getRelationshipsPart() == null) return null;
		Part p = chart.getRelationshipsPart().getPart(ext.getId());
		if (!(p instanceof BinaryPart)) return null;
		// the live package, where an editor has one open (CR-036); else a copy, so that reading
		// leaves the part as it was
		OpcPackage embedded = p instanceof EmbeddedPackagePart && ((EmbeddedPackagePart) p).hasEmbeddedPackage()
				? ((EmbeddedPackagePart) p).getEmbeddedPackage()
				: OpcPackage.load(new ByteArrayInputStream(((BinaryPart) p).getBytes()));
		return embedded instanceof SpreadsheetMLPackage ? new ChartCells((SpreadsheetMLPackage) embedded) : null;
	}

	public SpreadsheetMLPackage getWorkbook() {
		return workbook;
	}

	/**
	 * The text of each cell a reference names, by point index, in the order a chart takes them
	 * (each area row by row, the areas of a union one after another); an empty cell is a gap.
	 * Errors are their codes (#N/A); a shared string its text; a number as the cell holds it.
	 */
	public Map<Integer, String> cells(String reference) throws Docx4JException {
		Map<Integer, String> points = new TreeMap<Integer, String>();
		if (reference == null) return points;
		int idx = 0;
		for (String area : areas(reference)) {
			int bang = area.lastIndexOf('!');
			if (bang < 0) return points;
			String sheet = unquote(area.substring(0, bang));
			String range = area.substring(bang + 1).replace("$", "");
			String[] ends = range.split(":");
			int[] a = cellRef(ends[0]);
			int[] b = ends.length > 1 ? cellRef(ends[1]) : a;
			if (a == null || b == null) return points;
			Map<String, Cell> cells = sheet(sheet);
			for (int r = Math.min(a[1], b[1]); r <= Math.max(a[1], b[1]); r++) {
				for (int c = Math.min(a[0], b[0]); c <= Math.max(a[0], b[0]); c++) {
					String text = cells == null ? null : text(cells.get(column(c) + r));
					if (text != null) points.put(idx, text);
					idx++;
				}
			}
		}
		return points;
	}

	/**
	 * Every point of every series where the cache and the cells differ: names, categories (single
	 * level), values, x and y values, bubble sizes.  Numbers are compared as numbers.
	 */
	public List<Difference> differences(ChartModel model) throws Docx4JException {
		List<Difference> d = new ArrayList<Difference>();
		for (ChartGroup g : model.getGroups()) {
			for (ChartSeries s : g.getSeries()) {
				compare(d, s, "name", s.getName());
				compare(d, s, "categories", s.getCategories());
				compare(d, s, "values", s.getValues());
				compare(d, s, "xValues", s.getXValues());
				compare(d, s, "yValues", s.getYValues());
				compare(d, s, "bubbleSizes", s.getBubbleSizes());
			}
		}
		return Collections.unmodifiableList(d);
	}

	private void compare(List<Difference> d, ChartSeries s, String what, ChartData data) throws Docx4JException {
		if (data == null || data.getReference() == null || data.getKind() == ChartData.Kind.MULTI_LEVEL_REFERENCE) return;
		Map<Integer, String> cells = cells(data.getReference());
		int n = Math.max(data.size(), cells.isEmpty() ? 0 : Collections.max(cells.keySet()) + 1);
		for (int i = 0; i < n; i++) {
			String cache = data.getText(i), cell = cells.get(i);
			if (!same(cache, cell, data.isNumeric())) d.add(new Difference(s, what, i, cache, cell));
		}
	}

	private static boolean same(String a, String b, boolean numeric) {
		if (a == null || b == null) return a == b;
		if (numeric) {
			try {
				double x = Double.parseDouble(a.trim()), y = Double.parseDouble(b.trim());
				return x == y || Math.abs(x - y) <= 1e-12 * Math.max(Math.abs(x), Math.abs(y));
			} catch (NumberFormatException e) {
				// an error code (#N/A) beside a number, or two codes
			}
		}
		return a.equals(b);
	}

	// ------------------------------------------------------------------ the cells

	private Map<String, Cell> sheet(String name) throws Docx4JException {
		if (sheets.containsKey(name)) return sheets.get(name);
		Map<String, Cell> m = null;
		for (Sheet s : workbook.getWorkbookPart().getContents().getSheets().getSheet()) {
			if (!name.equals(s.getName())) continue;
			Part p = workbook.getWorkbookPart().getRelationshipsPart().getPart(s.getId());
			if (p instanceof WorksheetPart) {
				m = new HashMap<String, Cell>();
				for (Row row : ((WorksheetPart) p).getContents().getSheetData().getRow()) {
					for (Cell c : row.getC()) if (c.getR() != null) m.put(c.getR().replace("$", ""), c);
				}
			}
		}
		sheets.put(name, m);
		return m;
	}

	private String text(Cell c) throws Docx4JException {
		if (c == null) return null;
		STCellType t = c.getT();
		if (t == STCellType.S) {
			if (sharedStrings == null) {
				SharedStrings ss = workbook.getWorkbookPart().getSharedStrings();
				sharedStrings = ss == null ? Collections.<CTRst>emptyList() : ss.getContents().getSi();
			}
			try {
				return rst(sharedStrings.get(Integer.parseInt(c.getV())));
			} catch (RuntimeException e) {
				return null;
			}
		}
		if (t == STCellType.INLINE_STR) return c.getIs() == null ? null : rst(c.getIs());
		if (t == STCellType.B) return "1".equals(c.getV()) ? "TRUE" : "FALSE";
		return c.getV();
	}

	private static String rst(CTRst r) {
		if (r.getT() != null) return r.getT().getValue();
		StringBuilder sb = new StringBuilder();
		for (CTRElt e : r.getR()) if (e.getT() != null) sb.append(e.getT().getValue());
		return sb.toString();
	}

	// ------------------------------------------------------------------ references

	/** The areas of a reference: Sheet1!$A$2:$A$5, or a union (Sheet1!$A$2:$A$3,Sheet1!$A$5) */
	static List<String> areas(String reference) {
		String r = reference.trim();
		if (r.startsWith("(") && r.endsWith(")")) r = r.substring(1, r.length() - 1);
		List<String> out = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < r.length(); i++) {
			char ch = r.charAt(i);
			if (ch == '\'') quoted = !quoted;
			if (ch == ',' && !quoted) {
				out.add(sb.toString().trim());
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
		if (sb.length() > 0) out.add(sb.toString().trim());
		return out;
	}

	/** A sheet name as a reference writes it: 'My Sheet' (with '' for a quote) or Sheet1 */
	static String unquote(String s) {
		if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) return s.substring(1, s.length() - 1).replace("''", "'");
		return s;
	}

	/** {column, row}, both 1-based, of A1; null for anything else */
	static int[] cellRef(String a1) {
		int i = 0, col = 0;
		while (i < a1.length() && Character.isLetter(a1.charAt(i))) {
			col = col * 26 + (Character.toUpperCase(a1.charAt(i)) - 'A' + 1);
			i++;
		}
		if (i == 0 || i == a1.length()) return null;
		try {
			return new int[] { col, Integer.parseInt(a1.substring(i)) };
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** A 1-based column number's letters */
	static String column(int c) {
		StringBuilder sb = new StringBuilder();
		while (c > 0) {
			int r = (c - 1) % 26;
			sb.insert(0, (char) ('A' + r));
			c = (c - 1) / 26;
		}
		return sb.toString();
	}
}
