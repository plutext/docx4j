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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart.CTFormulaRef;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart.CTFullRef;
import org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle.CTColorStyle;
import org.docx4j.copy.Copyable;
import org.docx4j.dml.CTPresetColor;
import org.docx4j.dml.CTSRgbColor;
import org.docx4j.dml.CTScRgbColor;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTSystemColor;
import org.docx4j.dml.CTHslColor;
import org.docx4j.dml.chart.CTAxDataSource;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.dml.chart.CTExtensionList;
import org.docx4j.dml.chart.CTLvl;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTMultiLvlStrData;
import org.docx4j.dml.chart.CTMultiLvlStrRef;
import org.docx4j.dml.chart.CTNumData;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTNumRef;
import org.docx4j.dml.chart.CTNumVal;
import org.docx4j.dml.chart.CTSerTx;
import org.docx4j.dml.chart.CTStrData;
import org.docx4j.dml.chart.CTStrRef;
import org.docx4j.dml.chart.CTStrVal;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTDPt;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.w3c.dom.Element;
import org.xlsx4j.model.AreaRef;
import org.xlsx4j.model.CellRef;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;

import jakarta.xml.bind.JAXBElement;

/**
 * Edits a chart's data as Excel does (CR-035 phase 4): the cells its references name, the table
 * they're in, the references and the caches, kept in step, as Excel's own edits measured them
 * (the e1 to e7 probes, CR-035 §11).  It takes a chart and the workbook its data is in:
 * {@link #of(Chart)} finds it, the xlsx's own or the one embedded with a chart in a docx or pptx
 * (CR-036), which stays live in its part until {@link #close()} and is written with the document
 * whenever that is saved, so {@code try (ChartDataEditor e = ChartDataEditor.of(chart)) {...}}.
 * Word's and PowerPoint's own edits (CR-036's probes) are as Excel's, and add: a pie's or
 * doughnut's new point formatted as its others ({@code c:dPt}), and an edited embedded workbook's
 * {@code calcPr/@calcId} set to 0.
 *
 * <ul>
 * <li>A value, a category or a series name is written into its cell, a name into a table's
 * header cell renaming the table's column; each edit refreshes every cache from the cells.</li>
 * <li>A category added takes the row (or, for series in rows, the column) after the data: every
 * reference that ends there grows by one, the table grows to it, the new cells take the style of
 * the cells before them.  A category removed takes its row out of the data's block (the table, or
 * else the extent of the chart's references), the cells after it moving up, as Excel's table does
 * when a row is deleted.</li>
 * <li>A series added is a copy of the last series shown: {@code idx} and {@code order} one above
 * the largest of any series (filtered ones included), its {@code c16:uniqueId} its {@code idx} as
 * eight hex digits and the suffix the chart's series share; no data labels; its formatting that of
 * the last series still in its automatic colour, given the colour style's colour for the new idx
 * (the {@code cycle} method): fill, line and marker; its references the next column (or row), its
 * name in the table's header, the table grown by a column.  A series removed takes its column out
 * of the block, as Excel's table does, and its own formatting with it (where Excel, deleting a
 * table's column, keeps the chart's series by position: CR-035 D12).</li>
 * <li>Caches: {@code ptCount} the cells the reference names, a point for each one not empty, an
 * error as its code (#N/A); a number as the cell holds it; a cache's {@code formatCode} kept, since
 * Excel spells built-in formats its own way; a series filtered out with Excel's chart filter
 * refreshed too.</li>
 * </ul>
 *
 * <p>A cell holding a formula is not overwritten unless {@link #setOverwriteFormulas(boolean)}
 * says so, and cells with formulas are never moved; once an edit is made in a workbook that has
 * formulas, {@code calcPr/@fullCalcOnLoad} is set so that Excel recalculates as it opens it.  What
 * Excel's edits haven't measured is refused rather than guessed: a table with a totals row or
 * without a header row resized, a bubble chart's series added, a chart whose data is on more than
 * one sheet reshaped.
 *
 * @since 17.3.3 (CR-035)
 */
public final class ChartDataEditor implements AutoCloseable {

	private static final String UNIQUE_ID_URI = "{C3380CC4-5D6E-409C-BE32-E72D297353CC}";
	private static final Pattern UNIQUE_ID = Pattern.compile("\\{[0-9A-Fa-f]{8}(-[^}]*)\\}");

	private final Chart chart;
	private final SpreadsheetMLPackage workbook;
	private final Map<String, SheetCells> sheets = new LinkedHashMap<String, SheetCells>();
	private final DataFormatter formatter = new DataFormatter(Locale.US);
	private boolean overwriteFormulas;
	/** The part holding the workbook, for a chart in a docx or pptx; null for an xlsx's own */
	private EmbeddedPackagePart embedded;
	/** Whether an edit has been made, so that closing an unedited workbook leaves its part as it was */
	private boolean edited;
	/** For {@link #fit}: the edits change the chart alone, its cells being written already */
	private boolean chartOnly;

	/** An editor of the chart whose data is in this workbook */
	public ChartDataEditor(Chart chart, SpreadsheetMLPackage workbook) {
		this.chart = chart;
		this.workbook = workbook;
	}

	/**
	 * An editor of a chart: in an xlsx, of its own workbook; in a docx or pptx, of the workbook
	 * embedded with it ({@code c:externalData}), kept live in its part until {@link #close()}, and
	 * written with the document whenever that is saved (CR-036).  Refused: a linked chart (its data
	 * in another file) and a chart with no workbook.
	 */
	public static ChartDataEditor of(Chart chart) throws Docx4JException {
		if (chart.getPackage() instanceof SpreadsheetMLPackage) {
			return new ChartDataEditor(chart, (SpreadsheetMLPackage) chart.getPackage());
		}
		org.docx4j.dml.chart.CTExternalData ext = chart.getContents().getExternalData();
		if (ext == null || ext.getId() == null || chart.getRelationshipsPart() == null) {
			throw new IllegalStateException("the chart has no workbook (no c:externalData)");
		}
		Relationship r = chart.getRelationshipsPart().getRelationshipByID(ext.getId());
		if (r == null) throw new IllegalStateException("the chart's c:externalData names no relationship: " + ext.getId());
		if ("External".equalsIgnoreCase(r.getTargetMode())) {
			throw new IllegalStateException("the chart is linked: its data is in another file (" + r.getTarget() + ")");
		}
		Part p = chart.getRelationshipsPart().getPart(r);
		if (!(p instanceof EmbeddedPackagePart)) throw new IllegalStateException("the chart's workbook isn't an embedded package");
		OpcPackage wb = ((EmbeddedPackagePart) p).getEmbeddedPackage();
		if (!(wb instanceof SpreadsheetMLPackage)) throw new IllegalStateException("the chart's embedded package isn't a workbook");
		ChartDataEditor e = new ChartDataEditor(chart, (SpreadsheetMLPackage) wb);
		e.embedded = (EmbeddedPackagePart) p;
		return e;
	}

	/**
	 * For a chart in a docx or pptx: its workbook written into its part's bytes now (the part keeps
	 * it live, and writes it whenever the document is saved, so this is for a caller that wants the
	 * bytes current); nothing for an xlsx
	 */
	public void flush() throws Docx4JException {
		if (embedded != null) embedded.writeEmbeddedPackage();
	}

	/**
	 * For a chart in a docx or pptx: its workbook, if edited, written into its part, and let go,
	 * so that later readers load it from the bytes (unedited, the part keeps the bytes it had);
	 * nothing for an xlsx
	 */
	@Override
	public void close() throws Docx4JException {
		if (embedded == null) return;
		if (edited) embedded.releaseEmbeddedPackage();
		else embedded.setEmbeddedPackage(null);
	}

	/** Whether a cell holding a formula may be overwritten (D10: not by default) */
	public ChartDataEditor setOverwriteFormulas(boolean overwrite) {
		this.overwriteFormulas = overwrite;
		return this;
	}

	public Chart getChart() {
		return chart;
	}

	public SpreadsheetMLPackage getWorkbook() {
		return workbook;
	}

	/**
	 * The chart's series as the read model has them now, group by group, each group's in their
	 * order, filtered ones included: the series the edits take, and the order of the values
	 * {@link #addCategory} takes
	 */
	public List<ChartSeries> getSeries() throws Docx4JException {
		List<ChartSeries> out = new ArrayList<ChartSeries>();
		for (ChartGroup g : ChartModel.read(chart.getContents(), null).getGroups()) out.addAll(g.getSeries());
		return out;
	}

	// ------------------------------------------------------------------ edits

	/** A point's value (a y value, for a scatter chart) */
	public void setValue(ChartSeries series, int point, double value) throws Docx4JException {
		CellRef c = values(find(series)).cell(point);
		SheetCells sc = sheet(c);
		writable(sc, c);
		sc.setNumber(c.getColumn(), c.getRow(), value);
		finish();
	}

	/** A point's value made empty: a gap, as the chart's {@code dispBlanksAs} draws it */
	public void setBlank(ChartSeries series, int point) throws Docx4JException {
		CellRef c = values(find(series)).cell(point);
		SheetCells sc = sheet(c);
		writable(sc, c);
		sc.clear(c.getColumn(), c.getRow());
		finish();
	}

	/** A point's value an error, as a constant: #N/A, which a line chart draws as a gap */
	public void setError(ChartSeries series, int point, String code) throws Docx4JException {
		CellRef c = values(find(series)).cell(point);
		SheetCells sc = sheet(c);
		writable(sc, c);
		sc.setError(c.getColumn(), c.getRow(), code);
		finish();
	}

	/** A category's label; for categories on several levels, the innermost */
	public void setCategory(int point, String label) throws Docx4JException {
		doSetCategory(point, label);
		finish();
	}

	/** A category that is a number or a date (a date as its serial number) */
	public void setCategory(int point, double value) throws Docx4JException {
		doSetCategory(point, value);
		finish();
	}

	/** A series' name: its cell (a table's header cell renaming the column), else its text */
	public void setSeriesName(ChartSeries series, String name) throws Docx4JException {
		doSetSeriesName(find(series), name);
		finish();
	}

	/**
	 * A category added after the last, with a value for each series in {@link #getSeries()}'s
	 * order (null, or too few values, leaves a point empty)
	 */
	public void addCategory(String label, Double... values) throws Docx4JException {
		doAddCategory(label, values);
		finish();
	}

	/** A category that is a number or a date (its serial number) added after the last; or an x value, for a scatter chart */
	public void addCategory(double label, Double... values) throws Docx4JException {
		doAddCategory(label, values);
		finish();
	}

	/** The category at that point removed, from the chart and from the cells */
	public void removeCategory(int point) throws Docx4JException {
		doRemoveCategory(point);
		finish();
	}

	/** A series added after the last, as Excel adds one when its table grows by a column */
	public ChartSeries addSeries(String name, Double... values) throws Docx4JException {
		long idx = doAddSeries(name, values);
		finish();
		for (ChartSeries s : getSeries()) if (s.getIdx() == idx) return s;
		return null;
	}

	/** A series removed, from the chart and from the cells */
	public void removeSeries(ChartSeries series) throws Docx4JException {
		doRemoveSeries(find(series));
		finish();
	}

	/**
	 * The chart's data replaced: so many categories (text, or numbers for categories that are
	 * numbers or dates) and series, series removed or added at the end, categories likewise, then
	 * every cell written; {@code values[series][point]}, null for an empty point
	 */
	public void setData(List<?> categories, List<String> seriesNames, Double[][] values) throws Docx4JException {
		if (seriesNames.isEmpty() || categories.isEmpty()) throw new IllegalArgumentException("a chart keeps a series and a category");
		List<ChartSeries> series = getSeries();
		while (series.size() > seriesNames.size()) {
			doRemoveSeries(find(series.get(series.size() - 1)));
			series = getSeries();
		}
		int n = points();
		while (n > categories.size()) doRemoveCategory(--n);
		while (n < categories.size()) {
			doAddCategory(categories.get(n), new Double[0]);
			n++;
		}
		while (series.size() < seriesNames.size()) {
			doAddSeries(seriesNames.get(series.size()), new Double[0]);
			series = getSeries();
		}
		for (int p = 0; p < categories.size(); p++) doSetCategory(p, categories.get(p));
		for (int s = 0; s < series.size(); s++) {
			Ser x = find(series.get(s));
			doSetSeriesName(x, seriesNames.get(s));
			Src v = values(x);
			for (int p = 0; p < categories.size(); p++) {
				Double d = values != null && s < values.length && values[s] != null && p < values[s].length ? values[s][p] : null;
				put(v.cell(p), d, null);
			}
		}
		finish();
	}

	/**
	 * For {@link ChartCreator}: the chart made so many series and points over cells already
	 * written, its references naming the first of them; nothing written into the cells.  Series
	 * dropped at the end, or added as {@link #addSeries} adds them (a copy of the last, numbered,
	 * coloured from the colour style); points dropped at the end, or added as {@link #addCategory}
	 * adds them (a pie's {@code c:dPt} too); then the caches.
	 */
	void fit(int series, int points) throws Docx4JException {
		if (series < 1 || points < 1) throw new IllegalArgumentException("a chart keeps a series and a category");
		chartOnly = true;
		try {
			List<ChartSeries> s = getSeries();
			while (s.size() > series) {
				detach(find(s.get(s.size() - 1)));
				s = getSeries();
			}
			int n = points();
			while (n > points) doRemoveCategory(--n);
			while (n < points) {
				doAddCategory(null, new Double[0]);
				n++;
			}
			while (s.size() < series) {
				doAddSeries(null, new Double[0]);
				s = getSeries();
			}
			refreshCaches();
		} finally {
			chartOnly = false;
		}
	}

	/** Every cache from the cells (each edit does this itself) */
	public void refreshCaches() throws Docx4JException {
		List<Ser> all = all();
		boolean down = down(all);
		for (Ser s : all) for (Src d : s.sources()) refresh(d, down);
	}

	// ------------------------------------------------------------------ the edits' work

	private void doSetCategory(int point, Object label) throws Docx4JException {
		Src cat = categories();
		if (cat == null) throw new IllegalStateException("the chart has no categories");
		if (label instanceof String && cat.ref instanceof CTNumRef) {
			throw new IllegalArgumentException("the chart's categories are numbers or dates; give one as a number");
		}
		CellRef c = categoryCell(cat, point);
		put(c, label, null);
	}

	private void doSetSeriesName(Ser x, String name) throws Docx4JException {
		CTSerTx tx = (CTSerTx) ChartSeries.get(x.ser, "getTx");
		if (tx != null && tx.getStrRef() != null) {
			CellRef c = new Src(tx.getStrRef()).cell(0);
			SheetCells sc = sheet(c);
			writable(sc, c);
			sc.setString(c.getColumn(), c.getRow(), name);
			TablePart t = Tables.containing(sc.getPart(), new AreaRef(c, c));
			if (t != null && Tables.getRef(t).getFirst().getRow() == c.getRow()) Tables.nameColumns(t, sc);
			return;
		}
		if (tx == null) {
			tx = new CTSerTx();
			invoke(x.ser, "setTx", CTSerTx.class, tx);
		}
		tx.setStrRef(null);
		tx.setV(name);
	}

	private void doAddCategory(Object label, Double[] values) throws Docx4JException {
		Geometry g = geometry();
		int end = g.pointEnd();
		List<Ser> series = inOrder();
		Src cat = categories();
		if (label instanceof String && cat != null && cat.ref instanceof CTNumRef) {
			throw new IllegalArgumentException("the chart's categories are numbers or dates; give one as a number");
		}
		if (chartOnly) {
			for (Ser s : all()) {
				int before = s.values() == null || s.values().areas == null ? -1 : s.values().size();
				for (Src d : s.data()) {
					d.areas = g.extend(d.areas, end);
					if (d.full != null) d.full = g.extend(d.full, end);
					d.save();
				}
				if (before >= 0) addPointFormat(s, before);
			}
			return;
		}
		// the new cells, checked before anything changes
		Map<CellRef, Object> writes = new LinkedHashMap<CellRef, Object>();
		Map<CellRef, CellRef> styleFrom = new LinkedHashMap<CellRef, CellRef>();
		if (cat != null) {
			CellRef c = g.at(g.innermost(cat), end + 1);
			writes.put(c, label);
			styleFrom.put(c, g.at(g.innermost(cat), end));
		}
		for (int i = 0; i < series.size(); i++) {
			Src v = values(series.get(i));
			int pos = g.seriesPos(v.areas.get(0).getFirst());
			CellRef c = g.at(pos, end + 1);
			writes.put(c, values != null && i < values.length ? values[i] : null);
			styleFrom.put(c, g.at(pos, end));
		}
		SheetCells sc = sheet(g.sheet);
		for (CellRef c : writes.keySet()) empty(sc, c);
		TablePart t = Tables.containing(sc.getPart(), g.extent);
		AreaRef tr = t == null ? null : Tables.getRef(t);
		boolean grow = tr != null && g.pointPos(tr.getLast()) == end;
		if (grow && !Tables.canResize(t)) throw new IllegalStateException("the table has a totals row or no header row");
		// the references
		for (Ser s : all()) {
			int before = s.values() == null || s.values().areas == null ? -1 : s.values().size();
			for (Src d : s.data()) {
				d.areas = g.extend(d.areas, end);
				if (d.full != null) d.full = g.extend(d.full, end);
				d.save();
			}
			if (before >= 0) addPointFormat(s, before);
		}
		for (Map.Entry<CellRef, Object> w : writes.entrySet()) put(w.getKey(), w.getValue(), styleFrom.get(w.getKey()));
		if (grow) Tables.setRef(t, g.down ? tr.resize(0, 0, 0, 1) : tr.resize(0, 0, 1, 0), sc);
	}

	/**
	 * Where a series formats each of its points itself ({@code c:dPt}, a pie's or a doughnut's varied
	 * colours), the new point's as Word adds it (CR-036 phase 0, p5w-pie-add-category): a copy of the
	 * last point's, its colour the colour style's for the new index where the last had its own
	 * automatic one, its {@code c16:uniqueId} numbered 2 idx + 1 as Word numbers points'
	 */
	@SuppressWarnings("unchecked")
	private void addPointFormat(Ser s, int idx) {
		List<CTDPt> dPts = (List<CTDPt>) ChartSeries.get(s.ser, "getDPt");
		if (dPts == null || dPts.isEmpty()) return;
		CTDPt last = null;
		for (CTDPt d : dPts) {
			if (d.getIdx() != null && (last == null || d.getIdx().getVal() > last.getIdx().getVal())) last = d;
		}
		// only where every point before it has its own (each idx up to idx - 1)
		if (last == null || last.getIdx().getVal() != idx - 1 || dPts.size() != idx) return;
		CTDPt n = (CTDPt) last.copy();
		n.getIdx().setVal(idx);
		CTColorStyle cs = ChartColorStyles.of(chart);
		if (cs != null && !cs.getEGColorChoice().isEmpty() && (cs.getMeth() == null || "cycle".equals(cs.getMeth()))) {
			recolour(n.getSpPr(), key(ChartColorStyles.automatic(cs, idx - 1)), cs, idx);
		}
		uniqueId(n, 2L * idx + 1);
		n.setParent(s.ser);
		dPts.add(n);
	}

	/** A removed point's own formatting gone with it, the later points' renumbered */
	@SuppressWarnings("unchecked")
	private static void removePointFormat(Ser s, int idx) {
		List<CTDPt> dPts = (List<CTDPt>) ChartSeries.get(s.ser, "getDPt");
		if (dPts == null || dPts.isEmpty()) return;
		dPts.removeIf(d -> d.getIdx() != null && d.getIdx().getVal() == idx);
		for (CTDPt d : dPts) if (d.getIdx() != null && d.getIdx().getVal() > idx) d.getIdx().setVal(d.getIdx().getVal() - 1);
	}

	private void doRemoveCategory(int point) throws Docx4JException {
		if (points() <= 1) throw new IllegalStateException("a chart keeps a category");
		Geometry g = geometry();
		int p = g.pointPos(values(inOrder().get(0)).cell(point));
		if (chartOnly) {
			for (Ser s : all()) {
				for (Src d : s.sources()) {
					d.areas = g.removePoint(d.areas, p);
					if (d.full != null) d.full = g.removePoint(d.full, p);
					d.save();
				}
				removePointFormat(s, point);
			}
			return;
		}
		SheetCells sc = sheet(g.sheet);
		AreaRef block = g.block(sc);
		AreaRef moving = g.down ? new AreaRef(new CellRef(g.sheet, block.getFirst().getColumn(), p),
				new CellRef(g.sheet, block.getLast().getColumn(), block.getLast().getRow()))
				: new AreaRef(new CellRef(g.sheet, p, block.getFirst().getRow()),
						new CellRef(g.sheet, block.getLast().getColumn(), block.getLast().getRow()));
		formulas(sc, moving, p, g);
		TablePart t = Tables.containing(sc.getPart(), g.extent);
		if (t != null && !Tables.canResize(t)) throw new IllegalStateException("the table has a totals row or no header row");
		for (Ser s : all()) {
			for (Src d : s.sources()) {
				d.areas = g.removePoint(d.areas, p);
				if (d.full != null) d.full = g.removePoint(d.full, p);
				d.save();
			}
			removePointFormat(s, point);
		}
		if (g.down) sc.deleteRow(moving, p);
		else sc.deleteColumn(moving, p);
		if (t != null) {
			AreaRef tr = Tables.getRef(t);
			if (g.down) Tables.setRef(t, tr.resize(0, 0, 0, -1), sc);
			else Tables.removeColumn(t, p - tr.getFirst().getColumn(), sc);
		}
	}

	private long doAddSeries(String name, Double[] values) throws Docx4JException {
		Geometry g = geometry();
		Ser last = null;
		for (Ser s : inOrder()) if (s.wrapper == null) last = s;
		if (last == null) throw new IllegalStateException("the chart shows no series to copy");
		if (ChartGroup.Kind.of(last.group) == ChartGroup.Kind.BUBBLE) {
			throw new UnsupportedOperationException("adding a bubble chart's series: Excel's way is unmeasured");
		}
		Src lv = values(last);
		int q = g.seriesEnd();
		int delta = q + 1 - g.seriesPos(lv.areas.get(0).getFirst());
		CTSerTx ltx = (CTSerTx) ChartSeries.get(last.ser, "getTx");
		Src ln = ltx != null && ltx.getStrRef() != null ? new Src(ltx.getStrRef()) : null;
		Map<CellRef, Object> writes = new LinkedHashMap<CellRef, Object>();
		Map<CellRef, CellRef> styleFrom = new LinkedHashMap<CellRef, CellRef>();
		TablePart t = null;
		AreaRef tr = null;
		boolean grow = false;
		SheetCells sc = null;
		if (!chartOnly) {
			sc = sheet(g.sheet);
			if (ln != null) {
				CellRef c = g.shift(ln.cell(0), delta);
				writes.put(c, name);
				styleFrom.put(c, ln.cell(0));
			}
			for (int i = 0; i < lv.size(); i++) {
				CellRef c = g.shift(lv.cell(i), delta);
				writes.put(c, values != null && i < values.length ? values[i] : null);
				styleFrom.put(c, lv.cell(i));
			}
			for (CellRef c : writes.keySet()) empty(sc, c);
			t = Tables.containing(sc.getPart(), g.extent);
			tr = t == null ? null : Tables.getRef(t);
			grow = tr != null && g.seriesPos(tr.getLast()) == q;
			if (grow && !Tables.canResize(t)) throw new IllegalStateException("the table has a totals row or no header row");
		}

		// the series: a copy of the last, numbered after every series, its own things dropped
		long idx = -1, order = -1;
		for (Ser s : all()) {
			idx = Math.max(idx, s.idx());
			order = Math.max(order, s.order());
		}
		idx++;
		order++;
		Object ns = ((Copyable) last.ser).copy();
		((CTUnsignedInt) ChartSeries.get(ns, "getIdx")).setVal(idx);
		((CTUnsignedInt) ChartSeries.get(ns, "getOrder")).setVal(order);
		for (String list : new String[] { "getDPt", "getTrendline", "getErrBars" }) {
			Object l = ChartSeries.get(ns, list);
			if (l instanceof List) ((List<?>) l).clear();
		}
		if (ChartSeries.get(ns, "getErrBars") != null && !(ChartSeries.get(ns, "getErrBars") instanceof List)) {
			invoke(ns, "setErrBars", ChartSeries.get(ns, "getErrBars").getClass(), null);
		}
		// as Excel's new series (r10): no data labels of the last one's
		if (ChartSeries.get(ns, "getDLbls") != null) invoke(ns, "setDLbls", CTDLbls.class, null);
		uniqueId(ns, idx);
		// its formatting a series' still in its automatic colour (r11: not the last's own red)
		Ser template = autoFormatted(last);
		if (template != last) {
			for (String[] p : new String[][] { { "getSpPr", "setSpPr" }, { "getMarker", "setMarker" } }) {
				Object v = ChartSeries.get(template.ser, p[0]);
				if (v != null) invoke(ns, p[1], v.getClass(), ((Copyable) v).copy());
			}
		}
		recolour(ns, template.idx(), idx);
		Ser n = new Ser(ns, last.group, null, null);
		CTSerTx tx = (CTSerTx) ChartSeries.get(ns, "getTx");
		if (ln != null) {
			Src d = new Src(tx.getStrRef());
			d.areas = g.shiftSeries(d.areas, delta);
			d.save();
		} else {
			if (tx == null) {
				tx = new CTSerTx();
				invoke(ns, "setTx", CTSerTx.class, tx);
			}
			tx.setV(name);
		}
		Src nv = values(n);
		nv.areas = g.shiftSeries(nv.areas, delta);
		if (nv.full != null) nv.full = g.shiftSeries(nv.full, delta);
		nv.save();
		@SuppressWarnings("unchecked")
		List<Object> sers = (List<Object>) ChartSeries.get(last.group, "getSer");
		sers.add(ns);
		if (ns instanceof org.jvnet.jaxb.lang.Child) ((org.jvnet.jaxb.lang.Child) ns).setParent(last.group);

		for (Map.Entry<CellRef, Object> w : writes.entrySet()) put(w.getKey(), w.getValue(), styleFrom.get(w.getKey()));
		if (grow) Tables.setRef(t, g.down ? tr.resize(0, 0, 1, 0) : tr.resize(0, 0, 0, 1), sc);
		return idx;
	}

	private void doRemoveSeries(Ser x) throws Docx4JException {
		if (all().size() <= 1) throw new IllegalStateException("a chart keeps a series");
		Geometry g = geometry();
		Src v = values(x);
		int q = g.seriesPos(v.areas.get(0).getFirst());
		SheetCells sc = sheet(g.sheet);
		AreaRef block = g.block(sc);
		TablePart t = Tables.containing(sc.getPart(), g.extent);
		if (t != null && !Tables.canResize(t)) throw new IllegalStateException("the table has a totals row or no header row");
		// the cells go only where no other series uses them
		boolean shared = false;
		for (Ser s : all()) {
			if (s.ser == x.ser) continue;
			for (Src d : Arrays.asList(s.name, s.values())) {
				if (d == null || d.areas == null) continue;
				for (AreaRef a : d.areas) if (g.seriesPos(a.getFirst()) == q && g.seriesPos(a.getLast()) == q) shared = true;
			}
		}
		AreaRef moving = g.down ? new AreaRef(new CellRef(g.sheet, q, block.getFirst().getRow()),
				new CellRef(g.sheet, block.getLast().getColumn(), block.getLast().getRow()))
				: new AreaRef(new CellRef(g.sheet, block.getFirst().getColumn(), q),
						new CellRef(g.sheet, block.getLast().getColumn(), block.getLast().getRow()));
		if (!shared) formulas(sc, moving, q, g.flip());
		detach(x);
		if (shared) return;
		for (Ser s : all()) {
			for (Src d : s.sources()) {
				d.areas = g.removeSeriesPos(d.areas, q);
				if (d.full != null) d.full = g.removeSeriesPos(d.full, q);
				d.save();
			}
		}
		if (g.down) sc.deleteColumn(moving, q);
		else sc.deleteRow(moving, q);
		if (t != null) {
			AreaRef tr = Tables.getRef(t);
			if (g.down) Tables.removeColumn(t, q - tr.getFirst().getColumn(), sc);
			else Tables.setRef(t, tr.resize(0, 0, 0, -1), sc);
		}
	}

	private void detach(Ser x) {
		if (x.wrapper == null) {
			((List<?>) ChartSeries.get(x.group, "getSer")).remove(x.ser);
			return;
		}
		x.ext.getAny().removeIf(o -> XmlUtils.unwrap(o) == x.wrapper);
		if (x.ext.getAny().isEmpty()) {
			CTExtensionList l = (CTExtensionList) ChartSeries.get(x.group, "getExtLst");
			l.getExt().remove(x.ext);
			if (l.getExt().isEmpty()) invoke(x.group, "setExtLst", CTExtensionList.class, null);
		}
	}

	/** The number of categories: the cells of the first series' values */
	private int points() throws Docx4JException {
		return values(inOrder().get(0)).size();
	}

	// ------------------------------------------------------------------ cells

	private SheetCells sheet(CellRef c) throws Docx4JException {
		if (c.getSheet() == null) throw new IllegalStateException("a reference names no sheet: " + c);
		return sheet(c.getSheet());
	}

	private SheetCells sheet(String name) throws Docx4JException {
		SheetCells sc = sheets.get(name);
		if (sc == null) {
			sc = SheetCells.of(workbook, name);
			if (sc == null) throw new IllegalStateException("the workbook has no worksheet " + name);
			sheets.put(name, sc);
		}
		return sc;
	}

	private void writable(SheetCells sc, CellRef c) throws Docx4JException {
		if (!overwriteFormulas && SheetCells.hasFormula(sc.getCell(c.getColumn(), c.getRow()))) {
			throw new IllegalStateException(c + " holds a formula; setOverwriteFormulas(true) to replace it");
		}
	}

	private void empty(SheetCells sc, CellRef c) throws Docx4JException {
		Cell cell = sc.getCell(c.getColumn(), c.getRow());
		if (cell != null && (cell.getV() != null || cell.getF() != null || cell.getIs() != null)) {
			throw new IllegalStateException(c + " isn't empty: the chart's data can't grow over it");
		}
	}

	/** No formula in the cells that would move; one in the line deleted only when allowed */
	private void formulas(SheetCells sc, AreaRef area, int deleted, Geometry g) throws Docx4JException {
		for (CellRef c : area.getCells()) {
			Cell cell = sc.getCell(c.getColumn(), c.getRow());
			if (!SheetCells.hasFormula(cell)) continue;
			if (g.pointPos(c) == deleted) writable(sc, new CellRef(sc.getName(), c.getColumn(), c.getRow()));
			else throw new IllegalStateException(new CellRef(sc.getName(), c.getColumn(), c.getRow())
					+ " holds a formula, which would have to move");
		}
	}

	/** A number, text or (null) nothing into a cell, styled as another cell where given */
	private void put(CellRef c, Object value, CellRef styleFrom) throws Docx4JException {
		SheetCells sc = sheet(c);
		writable(sc, c);
		if (value == null) sc.clear(c.getColumn(), c.getRow());
		else if (value instanceof Number) sc.setNumber(c.getColumn(), c.getRow(), ((Number) value).doubleValue());
		else sc.setString(c.getColumn(), c.getRow(), value.toString());
		if (styleFrom != null) {
			Cell from = sc.getCell(styleFrom.getColumn(), styleFrom.getRow());
			if (from != null && from.getS() != 0) sc.cell(c.getColumn(), c.getRow()).setS(from.getS());
		}
	}

	private void finish() throws Docx4JException {
		edited = true;
		refreshCaches();
		for (SheetCells sc : sheets.values()) sc.tidy();
		if (SheetCells.hasFormulas(workbook)) SheetCells.setFullCalcOnLoad(workbook);
		if (embedded != null) {
			// as Word's save of a chart's edited workbook (CR-036 phase 0): calcId 0, so that Excel
			// recalculates it when it next opens it
			org.xlsx4j.sml.Workbook wb = workbook.getWorkbookPart().getContents();
			if (wb.getCalcPr() == null) wb.setCalcPr(new org.xlsx4j.sml.CTCalcPr());
			wb.getCalcPr().setCalcId(0L);
		}
	}

	// ------------------------------------------------------------------ caches

	private void refresh(Src d, boolean down) throws Docx4JException {
		if (d.areas == null) return;
		boolean date1904 = workbook.getWorkbookPart().isDate1904();
		if (d.ref instanceof CTStrRef) {
			CTStrRef r = (CTStrRef) d.ref;
			if (r.getStrCache() == null) r.setStrCache(new CTStrData());
			CTStrData cache = r.getStrCache();
			List<CellRef> cells = d.cells();
			cache.setPtCount(count(cells.size()));
			cache.getPt().clear();
			for (int i = 0; i < cells.size(); i++) {
				String text = display(cells.get(i), date1904);
				if (text != null) cache.getPt().add(strVal(i, text, cache));
			}
		} else if (d.ref instanceof CTNumRef) {
			CTNumRef r = (CTNumRef) d.ref;
			if (r.getNumCache() == null) r.setNumCache(new CTNumData());
			CTNumData cache = r.getNumCache();
			List<CellRef> cells = d.cells();
			Map<Long, String> pointFormats = new LinkedHashMap<Long, String>();
			for (CTNumVal v : cache.getPt()) if (v.getFormatCode() != null) pointFormats.put(v.getIdx(), v.getFormatCode());
			if (cache.getFormatCode() == null) {
				String code = "General";
				for (CellRef c : cells) {
					Cell cell = sheet(c).getCell(c.getColumn(), c.getRow());
					if (SheetCells.isNumber(cell)) {
						code = sheet(c).getFormatCode(cell);
						break;
					}
				}
				cache.setFormatCode(code);
			}
			cache.setPtCount(count(cells.size()));
			cache.getPt().clear();
			for (int i = 0; i < cells.size(); i++) {
				CellRef c = cells.get(i);
				Cell cell = sheet(c).getCell(c.getColumn(), c.getRow());
				String v = null;
				if (SheetCells.isNumber(cell)) v = cell.getV().trim();
				else if (cell != null && cell.getT() == org.xlsx4j.sml.STCellType.E) v = cell.getV();
				if (v == null) continue;
				CTNumVal nv = new CTNumVal();
				nv.setIdx(i);
				nv.setV(v);
				nv.setFormatCode(pointFormats.get((long) i));
				nv.setParent(cache);
				cache.getPt().add(nv);
			}
		} else if (d.ref instanceof CTMultiLvlStrRef) {
			CTMultiLvlStrRef r = (CTMultiLvlStrRef) d.ref;
			if (r.getMultiLvlStrCache() == null) r.setMultiLvlStrCache(new CTMultiLvlStrData());
			CTMultiLvlStrData cache = r.getMultiLvlStrCache();
			AreaRef a = d.areas.get(0);
			int points = down ? a.getRowCount() : a.getColumnCount();
			int levels = down ? a.getColumnCount() : a.getRowCount();
			cache.setPtCount(count(points));
			cache.getLvl().clear();
			// the innermost level first: the last column (or row)
			for (int l = levels - 1; l >= 0; l--) {
				CTLvl lvl = new CTLvl();
				lvl.setParent(cache);
				for (int i = 0; i < points; i++) {
					CellRef c = down ? a.getFirst().offset(l, i) : a.getFirst().offset(i, l);
					String text = display(new CellRef(a.getSheet(), c.getColumn(), c.getRow()), date1904);
					if (text != null) {
						CTStrVal sv = new CTStrVal();
						sv.setIdx(i);
						sv.setV(text);
						sv.setParent(lvl);
						lvl.getPt().add(sv);
					}
				}
				cache.getLvl().add(lvl);
			}
		}
	}

	/** A cell as a text cache holds it: a number in its format, as Excel shows it */
	private String display(CellRef c, boolean date1904) throws Docx4JException {
		SheetCells sc = sheet(c);
		Cell cell = sc.getCell(c.getColumn(), c.getRow());
		String text = sc.getText(cell);
		if (text == null || !SheetCells.isNumber(cell)) return text;
		return ChartTable.format(text, sc.getFormatCode(cell), formatter, date1904);
	}

	private static CTUnsignedInt count(int n) {
		CTUnsignedInt u = new CTUnsignedInt();
		u.setVal(n);
		return u;
	}

	private static CTStrVal strVal(int idx, String text, Object parent) {
		CTStrVal v = new CTStrVal();
		v.setIdx(idx);
		v.setV(text);
		v.setParent(parent);
		return v;
	}

	// ------------------------------------------------------------------ the new series' id and colour

	/**
	 * c16:uniqueId of a series (its idx) or a point (Word's 2 idx + 1): the number as eight hex
	 * digits, the suffix kept; the element a new one, not the copy's shared one
	 */
	private static void uniqueId(Object ser, long idx) {
		CTExtensionList extLst = (CTExtensionList) ChartSeries.get(ser, "getExtLst");
		if (extLst == null) return;
		CTExtension keep = null;
		for (CTExtension e : extLst.getExt()) if (UNIQUE_ID_URI.equalsIgnoreCase(e.getUri())) keep = e;
		extLst.getExt().clear();
		if (keep == null) {
			invoke(ser, "setExtLst", CTExtensionList.class, null);
			return;
		}
		for (int i = 0; i < keep.getAny().size(); i++) {
			Object o = keep.getAny().get(i);
			if (o instanceof Element && "uniqueId".equals(((Element) o).getLocalName())) {
				Element copy = (Element) ((Element) o).cloneNode(true);
				Matcher m = UNIQUE_ID.matcher(copy.getAttribute("val"));
				if (m.matches()) copy.setAttribute("val", "{" + String.format("%08X", idx) + m.group(1) + "}");
				keep.getAny().set(i, copy);
			}
		}
		extLst.getExt().add(keep);
	}

	/**
	 * Where the copied series had its own automatic colour (the colour style's for its idx), the
	 * new series' automatic colour instead: the style's {@code cycle}, colour idx mod n, with
	 * variation (idx / n) mod v.  A colour of the series' own choosing is copied as it is.
	 */
	private void recolour(Object ser, long fromIdx, long toIdx) {
		CTColorStyle cs = ChartColorStyles.of(chart);
		if (cs == null || cs.getEGColorChoice().isEmpty() || (cs.getMeth() != null && !"cycle".equals(cs.getMeth()))) return;
		String from = key(ChartColorStyles.automatic(cs, fromIdx));
		CTShapeProperties sp = (CTShapeProperties) ChartSeries.get(ser, "getSpPr");
		recolour(sp, from, cs, toIdx);
		CTMarker m = (CTMarker) ChartSeries.get(ser, "getMarker");
		if (m != null) recolour(m.getSpPr(), from, cs, toIdx);
	}

	private void recolour(CTShapeProperties sp, String from, CTColorStyle cs, long toIdx) {
		if (sp == null) return;
		recolour(sp.getSolidFill(), from, cs, toIdx);
		if (sp.getLn() != null) recolour(sp.getLn().getSolidFill(), from, cs, toIdx);
	}

	private void recolour(CTSolidColorFillProperties f, String from, CTColorStyle cs, long toIdx) {
		if (f == null || !from.equals(key(colourOf(f)))) return;
		Object c = ChartColorStyles.automatic(cs, toIdx);
		f.setScrgbClr(null);
		f.setSrgbClr(null);
		f.setHslClr(null);
		f.setSysClr(null);
		f.setSchemeClr(null);
		f.setPrstClr(null);
		if (c instanceof CTSchemeColor) f.setSchemeClr((CTSchemeColor) c);
		else if (c instanceof CTSRgbColor) f.setSrgbClr((CTSRgbColor) c);
		else if (c instanceof CTScRgbColor) f.setScrgbClr((CTScRgbColor) c);
		else if (c instanceof CTHslColor) f.setHslClr((CTHslColor) c);
		else if (c instanceof CTSystemColor) f.setSysClr((CTSystemColor) c);
		else if (c instanceof CTPresetColor) f.setPrstClr((CTPresetColor) c);
	}

	/**
	 * The last series of the group, from this one back, whose colour is its automatic one (the
	 * colour style's for its idx): the formatting a new series copies; this one where none is
	 */
	private Ser autoFormatted(Ser last) throws Docx4JException {
		CTColorStyle cs = ChartColorStyles.of(chart);
		if (cs == null || cs.getEGColorChoice().isEmpty()) return last;
		List<Ser> group = new ArrayList<Ser>();
		for (Ser s : inOrder()) if (s.group == last.group && s.wrapper == null) group.add(s);
		Collections.reverse(group);
		for (Ser s : group) {
			CTShapeProperties sp = (CTShapeProperties) ChartSeries.get(s.ser, "getSpPr");
			if (sp == null) continue;
			CTSolidColorFillProperties f = sp.getSolidFill() != null ? sp.getSolidFill()
					: sp.getLn() != null ? sp.getLn().getSolidFill() : null;
			if (f != null && key(colourOf(f)).equals(key(ChartColorStyles.automatic(cs, s.idx())))) return s;
		}
		return last;
	}

	private static Object colourOf(CTSolidColorFillProperties f) {
		for (Object o : new Object[] { f.getSchemeClr(), f.getSrgbClr(), f.getScrgbClr(), f.getHslClr(), f.getSysClr(),
				f.getPrstClr() }) {
			if (o != null) return o;
		}
		return null;
	}

	/** A colour as a key: its kind, value and transforms */
	private static String key(Object colour) {
		if (colour == null) return "";
		StringBuilder sb = new StringBuilder(colour.getClass().getSimpleName()).append(':').append(val(colour));
		Object ts = ChartSeries.get(colour, "getEGColorTransform");
		if (ts instanceof List) {
			for (Object t : (List<?>) ts) {
				if (t instanceof JAXBElement) {
					JAXBElement<?> e = (JAXBElement<?>) t;
					sb.append(' ').append(e.getName().getLocalPart()).append('=').append(val(e.getValue()));
				}
			}
		}
		return sb.toString();
	}

	private static String val(Object o) {
		Object v = ChartSeries.get(o, "getVal");
		if (v instanceof byte[]) {
			StringBuilder sb = new StringBuilder();
			for (byte b : (byte[]) v) sb.append(String.format("%02X", b));
			return sb.toString();
		}
		Object value = ChartSeries.get(v, "value");
		return String.valueOf(value != null ? value : v);
	}

	private static void invoke(Object o, String setter, Class<?> type, Object value) {
		try {
			o.getClass().getMethod(setter, type).invoke(o, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(o.getClass().getSimpleName() + "." + setter, e);
		}
	}

	// ------------------------------------------------------------------ the chart's series and references

	/** Every series, shown and filtered out, group by group */
	private List<Ser> all() throws Docx4JException {
		List<Ser> out = new ArrayList<Ser>();
		if (chart.getContents().getChart() == null || chart.getContents().getChart().getPlotArea() == null) return out;
		for (Object o : chart.getContents().getChart().getPlotArea().getAreaChartOrArea3DChartOrLineChart()) {
			Object g = XmlUtils.unwrap(o);
			Object sers = ChartSeries.get(g, "getSer");
			if (sers instanceof List) for (Object s : (List<?>) sers) out.add(new Ser(s, g, null, null));
			CTExtensionList extLst = (CTExtensionList) ChartSeries.get(g, "getExtLst");
			if (extLst == null) continue;
			for (CTExtension e : extLst.getExt()) {
				for (Object a : e.getAny()) {
					Object u = XmlUtils.unwrap(a);
					if (u != null && u.getClass().getSimpleName().startsWith("CTFiltered") && u.getClass().getSimpleName().endsWith("Ser")) {
						Object s = ChartSeries.get(u, "getSer");
						if (s != null) out.add(new Ser(s, g, e, u));
					}
				}
			}
		}
		return out;
	}

	/** The series in {@link #getSeries()}'s order */
	private List<Ser> inOrder() throws Docx4JException {
		List<Ser> out = new ArrayList<Ser>();
		for (ChartSeries s : getSeries()) out.add(find(s));
		return out;
	}

	private Ser find(ChartSeries series) throws Docx4JException {
		for (Ser s : all()) if (s.ser == series.getSource()) return s;
		throw new IllegalArgumentException("the series isn't this chart's (or has been removed)");
	}

	private static Src values(Ser s) {
		Src v = s.values();
		if (v == null || v.areas == null) throw new IllegalStateException("the series' values are no A1 reference this editor handles");
		return v;
	}

	/** The categories (or x values) of the first series that has them */
	private Src categories() throws Docx4JException {
		for (Ser s : inOrder()) {
			Src c = s.categories();
			if (c != null && c.areas != null) return c;
		}
		return null;
	}

	/** A category's cell: the point's cell, on several levels the innermost level's */
	private CellRef categoryCell(Src cat, int point) throws Docx4JException {
		if (!(cat.ref instanceof CTMultiLvlStrRef)) return cat.cell(point);
		Geometry g = geometry();
		return g.at(g.innermost(cat), g.pointPos(cat.areas.get(0).getFirst()) + point);
	}

	/** A series, as found in the chart's groups */
	private static final class Ser {
		final Object ser, group;
		final CTExtension ext;
		final Object wrapper;
		private Src name, categories, values;
		private List<Src> data;

		Ser(Object ser, Object group, CTExtension ext, Object wrapper) {
			this.ser = ser;
			this.group = group;
			this.ext = ext;
			this.wrapper = wrapper;
			CTSerTx tx = (CTSerTx) ChartSeries.get(ser, "getTx");
			name = tx == null || tx.getStrRef() == null ? null : new Src(tx.getStrRef());
			data = new ArrayList<Src>();
			for (String getter : new String[] { "getCat", "getXVal" }) {
				Src s = Src.of((CTAxDataSource) ChartSeries.get(ser, getter));
				if (s != null) {
					data.add(s);
					if (categories == null) categories = s;
				}
			}
			for (String getter : new String[] { "getVal", "getYVal", "getBubbleSize" }) {
				Src s = Src.of((CTNumDataSource) ChartSeries.get(ser, getter));
				if (s != null) {
					data.add(s);
					if (values == null && !getter.equals("getBubbleSize")) values = s;
				}
			}
		}

		long idx() {
			Object u = ChartSeries.get(ser, "getIdx");
			return u == null ? 0 : ((CTUnsignedInt) u).getVal();
		}

		long order() {
			Object u = ChartSeries.get(ser, "getOrder");
			return u == null ? idx() : ((CTUnsignedInt) u).getVal();
		}

		Src categories() {
			return categories;
		}

		Src values() {
			return values;
		}

		/** Categories, x values, values, y values, bubble sizes */
		List<Src> data() {
			List<Src> out = new ArrayList<Src>();
			for (Src s : data) if (s.areas != null) out.add(s);
			return out;
		}

		/** The name and the data */
		List<Src> sources() {
			List<Src> out = new ArrayList<Src>();
			if (name != null && name.areas != null) out.add(name);
			out.addAll(data());
			return out;
		}
	}

	/** A reference: c:f (or a filtered series' c15:formulaRef) and c15:fullRef, as areas */
	private static final class Src {
		final Object ref;
		List<AreaRef> areas, full;
		private CTFullRef fullRef;
		private CTFormulaRef formulaRef;

		Src(Object ref) {
			this.ref = ref;
			String f = (String) ChartSeries.get(ref, "getF");
			CTExtensionList extLst = (CTExtensionList) ChartSeries.get(ref, "getExtLst");
			if (extLst != null) {
				for (CTExtension e : extLst.getExt()) {
					for (Object a : e.getAny()) {
						Object u = XmlUtils.unwrap(a);
						if (u instanceof CTFullRef) fullRef = (CTFullRef) u;
						else if (u instanceof CTFormulaRef) formulaRef = (CTFormulaRef) u;
					}
				}
			}
			if (f == null && formulaRef != null) f = formulaRef.getSqref();
			areas = parse(f);
			full = fullRef == null ? null : parse(fullRef.getSqref());
		}

		static Src of(CTAxDataSource d) {
			if (d == null) return null;
			if (d.getStrRef() != null) return new Src(d.getStrRef());
			if (d.getNumRef() != null) return new Src(d.getNumRef());
			if (d.getMultiLvlStrRef() != null) return new Src(d.getMultiLvlStrRef());
			return null;
		}

		static Src of(CTNumDataSource d) {
			return d == null || d.getNumRef() == null ? null : new Src(d.getNumRef());
		}

		private static List<AreaRef> parse(String f) {
			if (f == null) return null;
			try {
				List<AreaRef> a = AreaRef.parseUnion(f);
				for (AreaRef r : a) if (r.getSheet() == null) return null;
				return a.isEmpty() ? null : new ArrayList<AreaRef>(a);
			} catch (IllegalArgumentException e) {
				return null; // a name, another workbook's cells: not this editor's
			}
		}

		/** The references written back */
		void save() {
			String f = AreaRef.formatUnion(areas);
			if (ChartSeries.get(ref, "getF") != null || formulaRef == null) invoke(ref, "setF", String.class, f);
			else formulaRef.setSqref(f);
			if (fullRef != null && full != null) fullRef.setSqref(AreaRef.formatUnion(full));
		}

		/** Its cells, area after area, each row by row */
		List<CellRef> cells() {
			if (areas == null) throw new IllegalStateException("not an A1 reference this editor handles: " + ChartSeries.get(ref, "getF"));
			List<CellRef> out = new ArrayList<CellRef>();
			if (ref instanceof CTMultiLvlStrRef) return out;
			for (AreaRef a : areas) out.addAll(a.getCells());
			return out;
		}

		int size() {
			if (ref instanceof CTMultiLvlStrRef) {
				AreaRef a = areas.get(0);
				return Math.max(a.getRowCount(), a.getColumnCount());
			}
			int n = 0;
			for (AreaRef a : areas) n += a.getRowCount() * a.getColumnCount();
			return n;
		}

		/** The point's cell */
		CellRef cell(int point) {
			List<CellRef> cells = cells();
			if (point < 0 || point >= cells.size()) {
				throw new IndexOutOfBoundsException("point " + point + " of " + cells.size() + " (" + AreaRef.formatUnion(areas) + ")");
			}
			return cells.get(point);
		}
	}

	/**
	 * Where the data lies: on one sheet, its points down the rows (or across the columns, for a
	 * chart plotted by row), and its extent
	 */
	private Geometry geometry() throws Docx4JException {
		List<Ser> all = all();
		String sheet = null;
		AreaRef extent = null;
		for (Ser s : all) {
			for (Src d : s.sources()) {
				for (List<AreaRef> list : Arrays.asList(d.areas, d.full)) {
					if (list == null) continue;
					for (AreaRef a : list) {
						if (sheet == null) sheet = a.getSheet();
						else if (!sheet.equals(a.getSheet())) {
							throw new IllegalStateException("the chart's data is on more than one sheet");
						}
						extent = extent == null ? a : new AreaRef(
								new CellRef(sheet, Math.min(extent.getFirst().getColumn(), a.getFirst().getColumn()),
										Math.min(extent.getFirst().getRow(), a.getFirst().getRow())),
								new CellRef(sheet, Math.max(extent.getLast().getColumn(), a.getLast().getColumn()),
										Math.max(extent.getLast().getRow(), a.getLast().getRow())));
					}
				}
			}
		}
		if (sheet == null) throw new IllegalStateException("the chart has no data in cells");
		return new Geometry(sheet, down(all), extent, all);
	}

	/** Whether the series' points run down a column: from a values area, else a name beside a value */
	private static boolean down(List<Ser> all) {
		for (Ser s : all) {
			Src v = s.values();
			if (v == null || v.areas == null) continue;
			for (AreaRef a : v.areas) {
				if (a.getRowCount() > 1) return true;
				if (a.getColumnCount() > 1) return false;
			}
		}
		for (Ser s : all) {
			Src v = s.values();
			if (v == null || v.areas == null || s.name == null || s.name.areas == null) continue;
			CellRef n = s.name.areas.get(0).getFirst(), c = v.areas.get(0).getFirst();
			if (n.getColumn() == c.getColumn()) return true;
			if (n.getRow() == c.getRow()) return false;
		}
		return true;
	}

	private static final class Geometry {
		final String sheet;
		final boolean down;
		final AreaRef extent;
		final List<Ser> all;

		Geometry(String sheet, boolean down, AreaRef extent, List<Ser> all) {
			this.sheet = sheet;
			this.down = down;
			this.extent = extent;
			this.all = all;
		}

		/** The same, its axes swapped: for a series' line, which runs the other way */
		Geometry flip() {
			return new Geometry(sheet, !down, extent, all);
		}

		int pointPos(CellRef c) {
			return down ? c.getRow() : c.getColumn();
		}

		int seriesPos(CellRef c) {
			return down ? c.getColumn() : c.getRow();
		}

		CellRef at(int seriesPos, int pointPos) {
			return down ? new CellRef(sheet, seriesPos, pointPos) : new CellRef(sheet, pointPos, seriesPos);
		}

		CellRef shift(CellRef c, int series) {
			return down ? c.offset(series, 0) : c.offset(0, series);
		}

		/** The last point line of the data: the furthest any categories' or values' area reaches */
		int pointEnd() {
			int end = 0;
			for (Ser s : all) {
				for (Src d : s.data()) {
					for (AreaRef a : d.areas) end = Math.max(end, pointPos(a.getLast()));
					if (d.full != null) for (AreaRef a : d.full) end = Math.max(end, pointPos(a.getLast()));
				}
			}
			return end;
		}

		/** The last series line: the furthest any values' or names' area reaches */
		int seriesEnd() {
			int end = 0;
			for (Ser s : all) {
				for (Src d : s.sources()) for (AreaRef a : d.areas) end = Math.max(end, seriesPos(a.getLast()));
			}
			return end;
		}

		/** The series line of a category reference's innermost level (its last column, or row) */
		int innermost(Src cat) {
			return seriesPos(cat.areas.get(0).getLast());
		}

		/** The block the data's cells move in: its table, else its extent */
		AreaRef block(SheetCells sc) throws Docx4JException {
			TablePart t = Tables.containing(sc.getPart(), extent);
			return t == null ? extent : Tables.getRef(t);
		}

		/** Areas ending at the last point line grown by one point */
		List<AreaRef> extend(List<AreaRef> areas, int end) {
			List<AreaRef> out = new ArrayList<AreaRef>();
			for (AreaRef a : areas) {
				if (pointPos(a.getLast()) == end) {
					out.add(down ? a.resize(0, 0, 0, 1) : a.resize(0, 0, 1, 0));
				} else {
					out.add(a);
				}
			}
			return out;
		}

		/** Areas without the point line p: shrunk where they hold it, moved where they're past it */
		List<AreaRef> removePoint(List<AreaRef> areas, int p) {
			List<AreaRef> out = new ArrayList<AreaRef>();
			for (AreaRef a : areas) {
				int first = pointPos(a.getFirst()), last = pointPos(a.getLast());
				if (last < p) out.add(a);
				else if (first > p) out.add(down ? a.resize(0, -1, 0, -1) : a.resize(-1, 0, -1, 0));
				else if (first == last) continue;
				else out.add(down ? a.resize(0, 0, 0, -1) : a.resize(0, 0, -1, 0));
			}
			if (out.isEmpty()) throw new IllegalStateException("a reference would be left with no cells");
			return out;
		}

		/** Areas without the series line q: moved where they're past it */
		List<AreaRef> removeSeriesPos(List<AreaRef> areas, int q) {
			List<AreaRef> out = new ArrayList<AreaRef>();
			for (AreaRef a : areas) {
				if (seriesPos(a.getFirst()) > q) out.add(down ? a.resize(-1, 0, -1, 0) : a.resize(0, -1, 0, -1));
				else out.add(a);
			}
			return out;
		}

		List<AreaRef> shiftSeries(List<AreaRef> areas, int delta) {
			List<AreaRef> out = new ArrayList<AreaRef>();
			for (AreaRef a : areas) out.add(down ? a.resize(delta, 0, delta, 0) : a.resize(0, delta, 0, delta));
			return out;
		}
	}
}
