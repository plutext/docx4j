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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;

/**
 * A chart described as a table, for readers without the picture (CR-035 phase 3): its title as
 * Office shows it, and its data, categories down and the series Office shows across, each number
 * formatted by its format code as Excel formats it (in the US locale, so that a description
 * reads the same wherever it is made).  Multi-level categories take a column a level, the
 * outermost first.  A chart with no categories (scatter, bubble) lists each series' x and y
 * values (and bubble sizes), point by point.  A gap is an empty cell; an error ({@code #N/A}) is
 * written as it is.
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartTable {

	private final String title;
	private final List<String> header;
	private final List<List<String>> rows;

	private ChartTable(String title, List<String> header, List<List<String>> rows) {
		this.title = title;
		this.header = Collections.unmodifiableList(header);
		this.rows = Collections.unmodifiableList(rows);
	}

	/** The chart's title as Office shows it; null for none */
	public String getTitle() {
		return title;
	}

	/** The column headings */
	public List<String> getHeader() {
		return header;
	}

	/** The rows, each as long as the header */
	public List<List<String>> getRows() {
		return rows;
	}

	// ------------------------------------------------------------------ making one

	public static ChartTable of(ChartModel m) {
		DataFormatter df = new DataFormatter(Locale.US);
		boolean date1904 = m.getDate1904() != null && m.getDate1904();
		List<ChartSeries> shown = new ArrayList<ChartSeries>();
		for (ChartGroup g : m.getGroups()) shown.addAll(g.getShownSeries());
		String title = m.getTitle() == null ? null : m.getTitle().getShownText();
		List<String> header = new ArrayList<String>();
		List<List<String>> rows = new ArrayList<List<String>>();

		ChartData categories = null;
		for (ChartSeries s : shown) {
			if (s.getCategories() != null) {
				categories = s.getCategories();
				break;
			}
		}
		boolean xy = categories == null;
		for (ChartSeries s : shown) if (s.getXValues() != null) xy = true;

		if (xy) {
			// x and y: one x column where every series has the same x values, the usual case;
			// else each series' own x beside its y (and bubble size)
			boolean sharedX = true;
			ChartData firstX = null;
			for (ChartSeries s : shown) {
				ChartData x = s.getXValues() != null ? s.getXValues() : s.getCategories();
				if (firstX == null) firstX = x;
				else if (x == null || firstX == null || !x.getPoints().equals(firstX.getPoints())) sharedX = false;
				if (s.getBubbleSizes() != null) sharedX = false;
			}
			int n = 0;
			for (ChartSeries s : shown) n = Math.max(n, size(s.getYValues() != null ? s.getYValues() : s.getValues()));
			if (sharedX) {
				header.add("");
				for (ChartSeries s : shown) header.add(s.getShownName());
			} else {
				for (ChartSeries s : shown) {
					header.add(s.getShownName() + " (x)");
					header.add(s.getShownName());
					if (s.getBubbleSizes() != null) header.add(s.getShownName() + " (size)");
				}
			}
			for (int i = 0; i < n; i++) {
				List<String> row = new ArrayList<String>();
				for (int k = 0; k < shown.size(); k++) {
					ChartSeries s = shown.get(k);
					ChartData y = s.getYValues() != null ? s.getYValues() : s.getValues();
					ChartData x = s.getXValues() != null ? s.getXValues() : s.getCategories();
					if (!sharedX || k == 0) row.add(x == null ? String.valueOf(i + 1) : cell(x, i, df, date1904));
					row.add(cell(y, i, df, date1904));
					if (!sharedX && s.getBubbleSizes() != null) row.add(cell(s.getBubbleSizes(), i, df, date1904));
				}
				rows.add(row);
			}
			return new ChartTable(title, header, rows);
		}

		List<Map<Integer, String>> levels = categories.getLevels();
		for (int l = levels.size() - 1; l >= 0; l--) header.add("");
		for (ChartSeries s : shown) header.add(s.getShownName());
		int n = categories.size();
		for (ChartSeries s : shown) n = Math.max(n, size(s.getValues()));
		for (int i = 0; i < n; i++) {
			List<String> row = new ArrayList<String>();
			for (int l = levels.size() - 1; l >= 0; l--) {
				if (categories.isNumeric() && levels.size() == 1) row.add(cell(categories, i, df, date1904));
				else row.add(blank(levels.get(l).get(i)));
			}
			for (ChartSeries s : shown) row.add(cell(s.getValues(), i, df, date1904));
			rows.add(row);
		}
		return new ChartTable(title, header, rows);
	}

	/** A chartex chart: its shown series' categories and values from the caches */
	public static ChartTable of(ChartExModel m) {
		DataFormatter df = new DataFormatter(Locale.US);
		List<String> header = new ArrayList<String>();
		List<List<String>> rows = new ArrayList<List<String>>();
		List<ChartExModel.Series> shown = new ArrayList<ChartExModel.Series>();
		for (ChartExModel.Series s : m.getSeries()) if (!s.isHidden()) shown.add(s);
		ChartExModel.Dimension cat = null;
		for (ChartExModel.Series s : shown) {
			for (ChartExModel.Dimension d : s.getData()) {
				if (cat == null && !d.isNumeric() && !d.getLevels().isEmpty()) cat = d;
			}
		}
		int catLevels = cat == null ? 0 : cat.getLevels().size();
		for (int l = 0; l < catLevels; l++) header.add("");
		for (ChartExModel.Series s : shown) header.add(s.getName() == null ? "" : s.getName());
		int n = 0;
		if (cat != null) for (Long c : cat.getCounts()) n = Math.max(n, c.intValue());
		for (ChartExModel.Series s : shown) {
			ChartExModel.Dimension v = values(s);
			if (v != null && !v.getCounts().isEmpty()) n = Math.max(n, v.getCounts().get(0).intValue());
		}
		for (int i = 0; i < n; i++) {
			List<String> row = new ArrayList<String>();
			for (int l = catLevels - 1; l >= 0; l--) row.add(blank(cat.getLevels().get(l).get(i)));
			for (ChartExModel.Series s : shown) {
				ChartExModel.Dimension v = values(s);
				String text = v == null || v.getLevels().isEmpty() ? null : v.getLevels().get(0).get(i);
				String format = v == null || v.formatCodes.isEmpty() ? null : v.formatCodes.get(0);
				row.add(format(text, format, df, false));
			}
			rows.add(row);
		}
		return new ChartTable(m.getShownTitle(), header, rows);
	}

	private static ChartExModel.Dimension values(ChartExModel.Series s) {
		for (ChartExModel.Dimension d : s.getData()) if (d.isNumeric()) return d;
		return null;
	}

	private static int size(ChartData d) {
		return d == null ? 0 : d.size();
	}

	private static String cell(ChartData d, int i, DataFormatter df, boolean date1904) {
		if (d == null) return "";
		String text = d.getText(i);
		if (!d.isNumeric()) return blank(text);
		return format(text, d.getFormatCode(i), df, date1904);
	}

	/** A number formatted by its format code as Excel does; anything else as it is */
	static String format(String text, String formatCode, DataFormatter df, boolean date1904) {
		if (text == null) return "";
		double v;
		try {
			v = Double.parseDouble(text.trim());
		} catch (NumberFormatException e) {
			return text;
		}
		String code = formatCode == null || formatCode.isEmpty() ? "General" : formatCode;
		try {
			return df.formatRawCellContents(v, "General".equalsIgnoreCase(code) ? 0 : -1, code, date1904);
		} catch (RuntimeException e) {
			return text;
		}
	}

	private static String blank(String s) {
		return s == null ? "" : s;
	}
}
