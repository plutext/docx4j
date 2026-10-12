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

import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTAxDataSource;
import org.docx4j.dml.chart.CTCatAx;
import org.docx4j.dml.chart.CTDPt;
import org.docx4j.dml.chart.CTDateAx;
import org.docx4j.dml.chart.CTExtension;
import org.docx4j.dml.chart.CTExtensionList;
import org.docx4j.dml.chart.CTLblAlgn;
import org.docx4j.dml.chart.CTNumData;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTNumFmt;
import org.docx4j.dml.chart.CTNumRef;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.dml.chart.CTSerTx;
import org.docx4j.dml.chart.CTStrRef;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTimeUnit;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.dml.chart.STLblAlgn;
import org.docx4j.dml.chart.STTickMark;
import org.docx4j.dml.chart.STTimeUnit;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.ChartColorStylePart;
import org.docx4j.openpackaging.parts.DrawingML.ChartStylePart;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Element;
import org.xlsx4j.model.AreaRef;
import org.xlsx4j.model.CellRef;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.Worksheet;

/**
 * Charts made as Excel's <i>Insert Chart</i> makes them (CR-036 phase 2): from data, written
 * into the sheet as a table ({@link #inSheet}), or over cells already there ({@link #fromCells}).
 *
 * <p>Each chart is made from its type's template, Excel's own chart, style and colour parts
 * (CR-036 phase 0, P1), byte for byte as Excel wrote them (D6); its references pointed at the
 * data; its series and points made as many as the data has by {@link ChartDataEditor}'s rules (a
 * series added a copy of the last, coloured from the colour style; a pie's point its own
 * {@code c:dPt}); its category axis a date axis over dates, a category axis over text and numbers;
 * then Excel's defaults for the data (no legend for one series, but a pie's; each of a pie's
 * series with its points' colours; a scatter chart's legend once it has two series) and the
 * definition's settings ({@link ChartSettings}).  What Excel writes for each was measured on its
 * own charts of the same data (CR-036's P1, P2 and P6 probes).
 *
 * <p>Office's ids are random; here they come from the {@link Random} given, so that a seeded one
 * makes the same file each time: the axis ids, the series' {@code c16:uniqueId} suffix, the
 * frame's {@code a16:creationId}, the table's {@code xr:uid}s.
 *
 * @since 17.3.3 (CR-036)
 */
public final class ChartCreator {

	private static final String UNIQUE_ID_URI = "{C3380CC4-5D6E-409C-BE32-E72D297353CC}";
	private static final Pattern UNIQUE_ID = Pattern.compile("\\{([0-9A-Fa-f]{8})-[^}]*\\}");

	/** The columns and rows a chart covers where no place is given: Excel's 5 by 3 inches, about */
	private static final int DEFAULT_COLUMNS = 8, DEFAULT_ROWS = 15;

	private final Random ids;

	/** Ids random, as Office's */
	public ChartCreator() {
		this(new Random());
	}

	/** Ids from this source: a seeded one makes the same file each time */
	public ChartCreator(Random ids) {
		if (ids == null) throw new IllegalArgumentException("no source of ids");
		this.ids = ids;
	}

	// ------------------------------------------------------------------ in a sheet

	/**
	 * A chart of the definition's data, which is written into the sheet at {@code dataAt} as
	 * Word's own chart data is laid out (the categories down the first column under the header
	 * {@link ChartDefinition#categoriesHeader}, each series a column with its name at the top), as
	 * a table; the chart placed over {@code placeAt} (null: beside the data) in the sheet's
	 * drawing, made if the sheet has none.  Refused: cells there that hold something, a table
	 * there, a definition with no series or no categories, more than one group.
	 *
	 * @return the chart, for an editor ({@link ChartDataEditor#of}) or {@link ChartSettings}
	 */
	public ChartRef inSheet(WorksheetPart sheet, ChartDefinition definition, CellRef dataAt, AreaRef placeAt)
			throws Docx4JException {
		SpreadsheetMLPackage workbook = workbook(sheet);
		String name = sheetName(workbook, sheet);
		check(definition);
		ChartType type = definition.getType();
		List<ChartDefinition.Series> series = definition.getGroups().get(0).getSeries();
		List<Object> categories = definition.getCategories();
		int c0 = dataAt.getColumn(), r0 = dataAt.getRow();
		AreaRef area = new AreaRef(new CellRef(name, c0, r0), new CellRef(name, c0 + series.size(), r0 + categories.size()));
		SheetCells sc = new SheetCells(workbook, sheet, name);
		for (CellRef c : area.getCells()) {
			Cell cell = sc.getCell(c.getColumn(), c.getRow());
			if (cell != null && (cell.getV() != null || cell.getF() != null || cell.getIs() != null)) {
				throw new IllegalStateException(new CellRef(name, c.getColumn(), c.getRow()) + " isn't empty: the chart's data can't be written over it");
			}
		}
		for (TablePart t : Tables.of(sheet)) {
			AreaRef r = Tables.getRef(t);
			if (r.getFirst().getColumn() <= area.getLast().getColumn() && area.getFirst().getColumn() <= r.getLast().getColumn()
					&& r.getFirst().getRow() <= area.getLast().getRow() && area.getFirst().getRow() <= r.getLast().getRow()) {
				throw new IllegalStateException("the chart's data would overlap the table " + t.getContents().getName());
			}
		}

		// the data, as Word's: a header row, the categories down the first column
		boolean date1904 = workbook.getWorkbookPart().isDate1904();
		String header = definition.getCategoriesHeader();
		// none: the table names its column (Column1), as Excel's
		if (header != null && !header.isEmpty()) sc.setString(c0, r0, header);
		for (int s = 0; s < series.size(); s++) sc.setString(c0 + 1 + s, r0, series.get(s).getName());
		for (int p = 0; p < categories.size(); p++) {
			Object o = categories.get(p);
			if (o instanceof String) sc.setString(c0, r0 + 1 + p, (String) o);
			else if (o instanceof Double) sc.setNumber(c0, r0 + 1 + p, (Double) o);
			else {
				sc.setNumber(c0, r0 + 1 + p, serial((LocalDate) o, date1904));
				sc.setNumberFormat(c0, r0 + 1 + p, definition.getDateFormat());
			}
			for (int s = 0; s < series.size(); s++) {
				List<Double> v = series.get(s).getValues();
				if (p < v.size() && v.get(p) != null) sc.setNumber(c0 + 1 + s, r0 + 1 + p, v.get(p));
			}
		}
		// as Excel's rows: their spans, which tidy() keeps once a row has them
		for (int r = r0; r <= area.getLast().getRow(); r++) {
			Row row = sc.getRow(r);
			if (row != null && row.getSpans().isEmpty()) row.getSpans().add(c0 + ":" + c0);
		}
		Tables.create(sc, new AreaRef(new CellRef(null, c0, r0), area.getLast()), ids);
		sc.tidy();

		Data data = new Data(name, true, true, true, c0, r0, series.size(), categories.size());
		if (categories.get(0) instanceof Double) {
			data.kind = Kind.NUMBER;
		} else if (categories.get(0) instanceof LocalDate) {
			data.kind = Kind.DATE;
			data.categoryFormat = SheetCells.chartFormatCode(definition.getDateFormat());
			List<LocalDate> days = new ArrayList<LocalDate>();
			for (Object o : categories) days.add((LocalDate) o);
			data.timeUnit = timeUnit(days);
		}
		Chart chart = make(workbook, type, data);
		apply(chart, definition);
		return place(sheet, chart, placeAt != null ? placeAt : beside(area));
	}

	/**
	 * A chart over data already in cells, as Excel's <i>Insert Chart</i> reads a selection (CR-036
	 * P6): an empty top-left cell makes the first row the series' names and the first column the
	 * categories; else the first row is names where it holds no number, the first column
	 * categories where it holds text or dates; the series down the columns where the data has
	 * more rows than columns, else across the rows.  No table made, nothing written.  The data
	 * may be on another sheet (the area names it); the chart is placed over {@code placeAt}
	 * (null: beside the data, where it is on this sheet) in this sheet's drawing.
	 */
	public ChartRef fromCells(WorksheetPart sheet, AreaRef area, ChartType type, AreaRef placeAt) throws Docx4JException {
		SpreadsheetMLPackage workbook = workbook(sheet);
		String name = sheetName(workbook, sheet);
		String dataSheet = area.getSheet() == null ? name : area.getSheet();
		SheetCells sc = SheetCells.of(workbook, dataSheet);
		if (sc == null) throw new IllegalArgumentException("the workbook has no worksheet " + dataSheet);
		Data data = read(sc, area);
		Chart chart = make(workbook, type, data);
		// Excel's defaults, as for a definition with no settings
		apply(chart, ChartDefinition.of(type));
		if (placeAt == null) {
			if (!dataSheet.equals(name)) throw new IllegalArgumentException("where to place the chart: its data is on another sheet");
			placeAt = beside(area);
		}
		return place(sheet, chart, placeAt);
	}

	// ------------------------------------------------------------------ reading cells as Excel does

	/** Where the data is, as the chart's references name it */
	private static final class Data {
		final String sheet;
		/** Series down the columns (else across the rows); a header line of names; a line of categories */
		final boolean down, names, categories;
		/** The area's top-left cell (the header line's and the categories' line's crossing) */
		final int column, row;
		final int series, points;
		/** The categories' kind, read from the cells */
		Kind kind = Kind.TEXT;
		String categoryFormat = "General";
		STTimeUnit timeUnit;

		Data(String sheet, boolean down, boolean names, boolean categories, int column, int row, int series, int points) {
			this.sheet = sheet;
			this.down = down;
			this.names = names;
			this.categories = categories;
			this.column = column;
			this.row = row;
			this.series = series;
			this.points = points;
		}

		/** The first series' first value */
		CellRef firstValue() {
			int dc = down ? (categories ? 1 : 0) : (names ? 1 : 0);
			int dr = down ? (names ? 1 : 0) : (categories ? 1 : 0);
			return new CellRef(sheet, column + dc, row + dr);
		}

		/** A series' values, its first n points */
		AreaRef values(int s, int n) {
			CellRef f = firstValue();
			return down ? area(f.getColumn() + s, f.getRow(), f.getColumn() + s, f.getRow() + n - 1)
					: area(f.getColumn(), f.getRow() + s, f.getColumn() + n - 1, f.getRow() + s);
		}

		/** A series' name's cell */
		AreaRef name(int s) {
			CellRef f = firstValue();
			return down ? area(f.getColumn() + s, row, f.getColumn() + s, row) : area(column, f.getRow() + s, column, f.getRow() + s);
		}

		/** The categories' cells, the first n */
		AreaRef categories(int n) {
			CellRef f = firstValue();
			return down ? area(column, f.getRow(), column, f.getRow() + n - 1) : area(f.getColumn(), row, f.getColumn() + n - 1, row);
		}

		private AreaRef area(int c1, int r1, int c2, int r2) {
			return new AreaRef(new CellRef(sheet, c1, r1, true, true), new CellRef(sheet, c2, r2, true, true));
		}
	}

	private enum Kind {
		TEXT, NUMBER, DATE
	}

	/** Excel's reading of a selection (CR-036 P6's plain ranges, and its tables) */
	private static Data read(SheetCells sc, AreaRef area) throws Docx4JException {
		int c0 = area.getFirst().getColumn(), r0 = area.getFirst().getRow();
		int cols = area.getColumnCount(), rows = area.getRowCount();
		Cell corner = sc.getCell(c0, r0);
		boolean headerRow, headerColumn;
		if (empty(corner)) {
			headerRow = headerColumn = true;
		} else {
			headerRow = rows > 1;
			for (int c = c0; c < c0 + cols && headerRow; c++) {
				if (SheetCells.isNumber(sc.getCell(c, r0))) headerRow = false;
			}
			headerColumn = cols > 1;
			for (int r = r0 + (headerRow ? 1 : 0); r < r0 + rows && headerColumn; r++) {
				Cell cell = sc.getCell(c0, r);
				if (SheetCells.isNumber(cell) && !isDate(sc, cell)) headerColumn = false;
			}
		}
		int dataRows = rows - (headerRow ? 1 : 0), dataCols = cols - (headerColumn ? 1 : 0);
		if (dataRows < 1 || dataCols < 1) throw new IllegalArgumentException("the area " + area + " holds no values to chart");
		boolean down = dataRows > dataCols;
		Data d = down ? new Data(sc.getName(), true, headerRow, headerColumn, c0, r0, dataCols, dataRows)
				: new Data(sc.getName(), false, headerColumn, headerRow, c0, r0, dataRows, dataCols);
		if (d.categories) kind(sc, d);
		return d;
	}

	/** The categories' kind: dates where the first is a date, numbers where every one is, else text */
	private static void kind(SheetCells sc, Data d) throws Docx4JException {
		List<Cell> cells = new ArrayList<Cell>();
		for (CellRef c : d.categories(d.points).getCells()) cells.add(sc.getCell(c.getColumn(), c.getRow()));
		boolean numbers = true;
		Cell first = null;
		for (Cell c : cells) {
			if (empty(c)) continue;
			if (!SheetCells.isNumber(c)) numbers = false;
			if (first == null) first = c;
		}
		if (!numbers || first == null) {
			d.kind = Kind.TEXT;
			return;
		}
		d.categoryFormat = SheetCells.chartFormatCode(sc.getFormatCode(first));
		if (isDate(sc, first)) {
			d.kind = Kind.DATE;
			List<LocalDate> days = new ArrayList<LocalDate>();
			boolean date1904 = sc.getWorkbook().getWorkbookPart().isDate1904();
			for (Cell c : cells) if (SheetCells.isNumber(c)) days.add(day(Double.parseDouble(c.getV().trim()), date1904));
			d.timeUnit = timeUnit(days);
		} else {
			d.kind = Kind.NUMBER;
		}
	}

	private static boolean empty(Cell c) {
		return c == null || ((c.getV() == null || c.getV().isEmpty()) && c.getF() == null && c.getIs() == null);
	}

	private static boolean isDate(SheetCells sc, Cell c) throws Docx4JException {
		if (!SheetCells.isNumber(c)) return false;
		String f = sc.getFormatCode(c);
		return org.xlsx4j.org.apache.poi.ss.usermodel.DateUtil.isADateFormat((int) c.getS(), f) && !f.contains("h");
	}

	/**
	 * Excel's base unit of a date axis: months where every date is on the same day of its month,
	 * years where also in the same month, else days (measured: daily dates days, the first of
	 * each month months)
	 */
	static STTimeUnit timeUnit(List<LocalDate> days) {
		if (days.size() < 2) return STTimeUnit.DAYS;
		boolean sameDay = true, sameMonth = true;
		for (LocalDate d : days) {
			if (d.getDayOfMonth() != days.get(0).getDayOfMonth()) sameDay = false;
			if (d.getMonthValue() != days.get(0).getMonthValue()) sameMonth = false;
		}
		return !sameDay ? STTimeUnit.DAYS : sameMonth ? STTimeUnit.YEARS : STTimeUnit.MONTHS;
	}

	/** A date as Excel's serial number (the 1900 system, or 1904) */
	static double serial(LocalDate d, boolean date1904) {
		if (date1904) return ChronoUnit.DAYS.between(LocalDate.of(1904, 1, 1), d);
		long n = ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), d);
		// Excel's 1900 counts a 29 February 1900 that never was: dates before it are a day less
		return n < 61 ? n - 1 : n;
	}

	private static LocalDate day(double serial, boolean date1904) {
		long n = (long) Math.floor(serial);
		if (date1904) return LocalDate.of(1904, 1, 1).plusDays(n);
		return LocalDate.of(1899, 12, 30).plusDays(n < 61 ? n + 1 : n);
	}

	// ------------------------------------------------------------------ the chart

	/** The type's template made the data's chart: its parts, references, series, points, kinds and ids */
	private Chart make(SpreadsheetMLPackage workbook, ChartType type, Data data) throws Docx4JException {
		Chart chart;
		ChartStylePart style;
		ChartColorStylePart colors;
		try {
			int n = next(workbook, "/xl/charts/chart", ".xml");
			chart = new Chart(new PartName("/xl/charts/chart" + n + ".xml"));
			style = new ChartStylePart(new PartName(free(workbook, "/xl/charts/style", n)));
			colors = new ChartColorStylePart(new PartName(free(workbook, "/xl/charts/colors", n)));
			// in the package first, as a part being read is (its Strict-ness asked)
			chart.setPackage(workbook);
			style.setPackage(workbook);
			colors.setPackage(workbook);
			String template = data.kind == Kind.DATE && type == ChartType.COLUMN_CLUSTERED ? "date-axis" : type.getTemplateName();
			try (InputStream is = template(template + ".xml")) {
				chart.unmarshal(is);
			}
			try (InputStream is = template("style-" + type.getStyleId() + ".xml")) {
				style.unmarshal(is);
			}
			try (InputStream is = template("colors-10.xml")) {
				colors.unmarshal(is);
			}
		} catch (Docx4JException e) {
			throw e;
		} catch (Exception e) {
			throw new Docx4JException("the " + type + " chart template couldn't be read", e);
		}
		// in Excel's order: the style rId1, the colours rId2
		chart.addTargetPart(style);
		chart.addTargetPart(colors);
		chart.getContents().getDate1904().setVal(workbook.getWorkbookPart().isDate1904());

		point(chart, type, data);
		new ChartDataEditor(chart, workbook).fit(data.series, data.points);
		if (type.variesColours()) pointsForEverySeries(chart);
		freshIds(chart);
		return chart;
	}

	/**
	 * The template's references pointed at the data: its series beyond the data's dropped, the
	 * rest named, categorised and valued from the data's cells, as many points as both have (the
	 * editor makes the rest); the categories' kind made the data's, and the axis with it
	 */
	@SuppressWarnings("unchecked")
	private static void point(Chart chart, ChartType type, Data data) throws Docx4JException {
		Object group = ChartSettings.groups(chart).get(0);
		List<Object> sers = (List<Object>) ChartSeries.get(group, "getSer");
		while (sers.size() > data.series) sers.remove(sers.size() - 1);
		int points = data.points;
		for (Object s : sers) {
			CTNumDataSource v = (CTNumDataSource) ChartSeries.get(s, type == ChartType.SCATTER ? "getYVal" : "getVal");
			points = Math.min(points, cells(v.getNumRef().getF()));
		}
		for (int i = 0; i < sers.size(); i++) {
			Object s = sers.get(i);
			CTSerTx tx = (CTSerTx) ChartSeries.get(s, "getTx");
			if (data.names) tx.getStrRef().setF(data.name(i).formatAsString());
			else ChartSettings.invoke(s, "setTx", CTSerTx.class, null);
			String catGetter = type == ChartType.SCATTER ? "getXVal" : "getCat";
			String catSetter = type == ChartType.SCATTER ? "setXVal" : "setCat";
			if (data.categories) {
				CTAxDataSource cat = new CTAxDataSource();
				String f = data.categories(points).formatAsString();
				if (data.kind == Kind.TEXT) {
					CTStrRef r = new CTStrRef();
					r.setF(f);
					r.setParent(cat);
					cat.setStrRef(r);
				} else {
					CTNumRef r = new CTNumRef();
					r.setF(f);
					CTNumData cache = new CTNumData();
					cache.setFormatCode(data.categoryFormat);
					cache.setParent(r);
					r.setNumCache(cache);
					r.setParent(cat);
					cat.setNumRef(r);
				}
				cat.setParent(s);
				ChartSettings.invoke(s, catSetter, CTAxDataSource.class, cat);
			} else {
				ChartSettings.invoke(s, catSetter, CTAxDataSource.class, null);
			}
			CTNumDataSource v = (CTNumDataSource) ChartSeries.get(s, type == ChartType.SCATTER ? "getYVal" : "getVal");
			v.getNumRef().setF(data.values(i, points).formatAsString());
			List<CTDPt> dPts = (List<CTDPt>) ChartSeries.get(s, "getDPt");
			if (dPts != null) dPts.removeIf(d -> d.getIdx() != null && d.getIdx().getVal() >= data.points);
		}
		if (type.hasAxes()) axisKind(chart, type, data);
	}

	/** The cells a reference names */
	private static int cells(String f) {
		int n = 0;
		for (AreaRef a : AreaRef.parseUnion(f)) n += a.getColumnCount() * a.getRowCount();
		return n;
	}

	/**
	 * The category axis made the categories' kind's, as Excel's: over dates a {@code c:dateAx}
	 * (their format, linked; outside tick marks; the base unit), over text or numbers a
	 * {@code c:catAx} (labels centred, General or the numbers' format); with no categories, no
	 * format.  A scatter chart's x axis takes the x values' format, and none where they are text.
	 */
	private static void axisKind(Chart chart, ChartType type, Data data) throws Docx4JException {
		Object ax = ChartSettings.axis(chart, ChartSettings.Axis.CATEGORY);
		CTPlotArea pa = chart.getContents().getChart().getPlotArea();
		List<Object> axes = pa.getValAxOrCatAxOrDateAx();
		if (type == ChartType.SCATTER) {
			// as Excel's: x values that are text (drawn as 1, 2, 3 ...) give the axis no format
			((CTValAx) ax).setNumFmt(data.categories && data.kind != Kind.TEXT ? numFmt(data.categoryFormat, true) : null);
			return;
		}
		if (!data.categories) {
			ChartSettings.invoke(ax, "setNumFmt", CTNumFmt.class, null);
			return;
		}
		if (data.kind == Kind.DATE) {
			CTDateAx d = ax instanceof CTDateAx ? (CTDateAx) ax : dateAx((CTCatAx) ax);
			d.setNumFmt(numFmt(data.categoryFormat, true));
			CTTimeUnit u = new CTTimeUnit();
			u.setVal(data.timeUnit);
			d.setBaseTimeUnit(u);
			replace(axes, ax, d, pa);
		} else {
			CTCatAx c = ax instanceof CTCatAx ? (CTCatAx) ax : catAx((CTDateAx) ax);
			c.setNumFmt(numFmt(data.kind == Kind.NUMBER ? data.categoryFormat : "General", true));
			replace(axes, ax, c, pa);
		}
	}

	private static CTNumFmt numFmt(String code, boolean linked) {
		CTNumFmt f = new CTNumFmt();
		f.setFormatCode(code);
		f.setSourceLinked(linked);
		return f;
	}

	private static void replace(List<Object> axes, Object from, Object to, Object parent) {
		if (from == to) return;
		for (int i = 0; i < axes.size(); i++) {
			if (XmlUtils.unwrap(axes.get(i)) == from) {
				axes.set(i, to);
				ChartSettings.invoke(to, "setParent", Object.class, parent);
				return;
			}
		}
	}

	/** A category axis as Excel's date axis over the same place: outside tick marks, no label alignment */
	private static CTDateAx dateAx(CTCatAx c) {
		CTDateAx d = new CTDateAx();
		d.setAxId(c.getAxId());
		d.setScaling(c.getScaling());
		d.setDelete(c.getDelete());
		d.setAxPos(c.getAxPos());
		d.setMajorGridlines(c.getMajorGridlines());
		d.setMinorGridlines(c.getMinorGridlines());
		d.setTitle(c.getTitle());
		CTTickMark out = new CTTickMark();
		out.setVal(STTickMark.OUT);
		d.setMajorTickMark(out);
		d.setMinorTickMark(c.getMinorTickMark());
		d.setTickLblPos(c.getTickLblPos());
		d.setSpPr(c.getSpPr());
		d.setTxPr(c.getTxPr());
		d.setCrossAx(c.getCrossAx());
		d.setCrosses(c.getCrosses());
		d.setCrossesAt(c.getCrossesAt());
		d.setAuto(c.getAuto());
		d.setLblOffset(c.getLblOffset());
		d.setExtLst(c.getExtLst());
		return d;
	}

	/** A date axis as Excel's category axis over the same place: labels centred, one level */
	private static CTCatAx catAx(CTDateAx d) {
		CTCatAx c = new CTCatAx();
		c.setAxId(d.getAxId());
		c.setScaling(d.getScaling());
		c.setDelete(d.getDelete());
		c.setAxPos(d.getAxPos());
		c.setMajorGridlines(d.getMajorGridlines());
		c.setMinorGridlines(d.getMinorGridlines());
		c.setTitle(d.getTitle());
		c.setMajorTickMark(d.getMajorTickMark());
		c.setMinorTickMark(d.getMinorTickMark());
		c.setTickLblPos(d.getTickLblPos());
		c.setSpPr(d.getSpPr());
		c.setTxPr(d.getTxPr());
		c.setCrossAx(d.getCrossAx());
		c.setCrosses(d.getCrosses());
		c.setCrossesAt(d.getCrossesAt());
		c.setAuto(d.getAuto());
		CTLblAlgn a = new CTLblAlgn();
		a.setVal(STLblAlgn.CTR);
		c.setLblAlgn(a);
		c.setLblOffset(d.getLblOffset());
		c.setNoMultiLvlLbl(ChartSettings.bool(false));
		c.setExtLst(d.getExtLst());
		return c;
	}

	/** As Excel's pie or doughnut of several series: each series' points coloured as the first's */
	@SuppressWarnings("unchecked")
	private static void pointsForEverySeries(Chart chart) throws Docx4JException {
		Object group = ChartSettings.groups(chart).get(0);
		List<Object> sers = (List<Object>) ChartSeries.get(group, "getSer");
		List<CTDPt> first = (List<CTDPt>) ChartSeries.get(sers.get(0), "getDPt");
		for (int i = 1; i < sers.size(); i++) {
			List<CTDPt> d = (List<CTDPt>) ChartSeries.get(sers.get(i), "getDPt");
			d.clear();
			for (CTDPt p : first) {
				CTDPt c = (CTDPt) p.copy();
				c.setParent(sers.get(i));
				d.add(c);
			}
		}
	}

	/** Office's random ids made afresh: the axis ids, the series' (and points') c16:uniqueId suffix */
	@SuppressWarnings("unchecked")
	private void freshIds(Chart chart) throws Docx4JException {
		CTPlotArea pa = chart.getContents().getChart().getPlotArea();
		Set<Long> used = new HashSet<Long>();
		java.util.Map<Long, Long> map = new java.util.HashMap<Long, Long>();
		for (Object o : pa.getValAxOrCatAxOrDateAx()) {
			CTUnsignedInt id = (CTUnsignedInt) ChartSeries.get(XmlUtils.unwrap(o), "getAxId");
			long n;
			do {
				n = 1 + ids.nextInt(Integer.MAX_VALUE - 1);
			} while (!used.add(n));
			map.put(id.getVal(), n);
		}
		for (Object o : pa.getValAxOrCatAxOrDateAx()) {
			Object a = XmlUtils.unwrap(o);
			for (String g : new String[] { "getAxId", "getCrossAx" }) {
				CTUnsignedInt u = (CTUnsignedInt) ChartSeries.get(a, g);
				if (u != null && map.containsKey(u.getVal())) u.setVal(map.get(u.getVal()));
			}
		}
		String suffix = new UUID(ids.nextLong(), ids.nextLong()).toString().toUpperCase(Locale.ROOT).substring(8);
		for (Object g : ChartSettings.groups(chart)) {
			Object ax = ChartSeries.get(g, "getAxId");
			if (ax instanceof List) for (CTUnsignedInt u : (List<CTUnsignedInt>) ax) if (map.containsKey(u.getVal())) u.setVal(map.get(u.getVal()));
			Object sers = ChartSeries.get(g, "getSer");
			if (!(sers instanceof List)) continue;
			for (Object s : (List<?>) sers) {
				suffix(ChartSeries.get(s, "getExtLst"), suffix);
				Object dPts = ChartSeries.get(s, "getDPt");
				if (dPts instanceof List) for (Object d : (List<?>) dPts) suffix(((CTDPt) d).getExtLst(), suffix);
			}
		}
	}

	private static void suffix(Object extLst, String suffix) {
		if (!(extLst instanceof CTExtensionList)) return;
		for (CTExtension e : ((CTExtensionList) extLst).getExt()) {
			if (!UNIQUE_ID_URI.equalsIgnoreCase(e.getUri())) continue;
			for (int i = 0; i < e.getAny().size(); i++) {
				Object o = e.getAny().get(i);
				if (!(o instanceof Element) || !"uniqueId".equals(((Element) o).getLocalName())) continue;
				Element copy = (Element) ((Element) o).cloneNode(true);
				Matcher m = UNIQUE_ID.matcher(copy.getAttribute("val"));
				if (m.matches()) copy.setAttribute("val", "{" + m.group(1) + suffix + "}");
				e.getAny().set(i, copy);
			}
		}
	}

	/**
	 * Excel's defaults for the data, then the definition's settings: no legend for a single
	 * series (but a pie's or a doughnut's), a scatter chart's legend once it has two
	 */
	private static void apply(Chart chart, ChartDefinition d) throws Docx4JException {
		ChartType type = d.getType();
		int series = 0;
		for (Object g : ChartSettings.groups(chart)) {
			Object s = ChartSeries.get(g, "getSer");
			if (s instanceof List) series += ((List<?>) s).size();
		}
		boolean legend = chart.getContents().getChart().getLegend() != null;
		if (series == 1 && !type.variesColours() && legend) ChartSettings.removeLegend(chart);
		if (series > 1 && !legend) ChartSettings.setLegend(chart, LegendPosition.BOTTOM);

		if (d.getTitle() != null) ChartSettings.setTitle(chart, d.getTitle());
		else if (d.isTitleDeleted()) ChartSettings.removeTitle(chart);
		if (d.getLegend() != null) ChartSettings.setLegend(chart, d.getLegend());
		else if (d.isLegendDeleted()) ChartSettings.removeLegend(chart);
		ChartDefinition.Group g = d.getGroups().get(0);
		for (ChartSettings.Axis a : ChartSettings.Axis.values()) {
			ChartDefinition.Axis def = a == ChartSettings.Axis.CATEGORY ? g.getCategoryAxis() : g.getValueAxis();
			if (def.getTitle() != null) ChartSettings.setAxisTitle(chart, a, def.getTitle());
			if (def.getNumberFormat() != null) ChartSettings.setNumberFormat(chart, a, def.getNumberFormat());
		}
		if (d.isDataLabels()) ChartSettings.setDataLabels(chart, true);
	}

	private static void check(ChartDefinition d) {
		if (d.getGroups().size() > 1) throw new UnsupportedOperationException("combination charts (more than one group) come later");
		ChartDefinition.Group g = d.getGroups().get(0);
		if (g.getSeries().isEmpty()) throw new IllegalArgumentException("the definition has no series");
		if (d.getCategories().isEmpty()) throw new IllegalArgumentException("the definition has no categories");
		for (ChartDefinition.Series s : g.getSeries()) {
			if (s.getValues().size() > d.getCategories().size()) {
				throw new IllegalArgumentException("the series " + s.getName() + " has more values than there are categories");
			}
		}
		if (!g.getType().hasAxes()) {
			for (ChartDefinition.Axis a : new ChartDefinition.Axis[] { g.getCategoryAxis(), g.getValueAxis() }) {
				if (a.getTitle() != null || a.getNumberFormat() != null) {
					throw new IllegalArgumentException("a " + g.getType() + " chart has no axes");
				}
			}
		}
	}

	// ------------------------------------------------------------------ placing it

	/** The chart in the sheet's drawing (made where it has none), a two-cell anchor over the area */
	private ChartRef place(WorksheetPart sheet, Chart chart, AreaRef at) throws Docx4JException {
		OpcPackage pkg = sheet.getPackage();
		Worksheet ws = sheet.getContents();
		Drawing drawing = null;
		if (ws.getDrawing() != null && sheet.getRelationshipsPart() != null) {
			Part p = sheet.getRelationshipsPart().getPart(ws.getDrawing().getId());
			if (p instanceof Drawing) drawing = (Drawing) p;
		}
		try {
			if (drawing == null) {
				drawing = new Drawing(new PartName("/xl/drawings/drawing" + next(pkg, "/xl/drawings/drawing", ".xml") + ".xml"));
				drawing.setJaxbElement(new CTDrawing());
				Relationship r = sheet.addTargetPart(drawing);
				org.xlsx4j.sml.CTDrawing d = new org.xlsx4j.sml.CTDrawing();
				d.setId(r.getId());
				d.setParent(ws);
				ws.setDrawing(d);
			}
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		Relationship r = drawing.addTargetPart(chart);
		CTDrawing wsDr = (CTDrawing) drawing.getContents();
		long id = 1;
		for (Object a : wsDr.getEGAnchor()) id = Math.max(id, frameId(XmlUtils.unwrap(a)));
		id++;
		String xml = "<xdr:wsDr xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" "
				+ "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<xdr:twoCellAnchor><xdr:from><xdr:col>" + (at.getFirst().getColumn() - 1) + "</xdr:col><xdr:colOff>0</xdr:colOff>"
				+ "<xdr:row>" + (at.getFirst().getRow() - 1) + "</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:from>"
				+ "<xdr:to><xdr:col>" + at.getLast().getColumn() + "</xdr:col><xdr:colOff>0</xdr:colOff>"
				+ "<xdr:row>" + at.getLast().getRow() + "</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:to>"
				+ "<xdr:graphicFrame macro=\"\"><xdr:nvGraphicFramePr><xdr:cNvPr id=\"" + id + "\" name=\"Chart " + (id - 1) + "\">"
				+ "<a:extLst><a:ext uri=\"{FF2B5EF4-FFF2-40B4-BE49-F238E27FC236}\"><a16:creationId "
				+ "xmlns:a16=\"http://schemas.microsoft.com/office/drawing/2014/main\" id=\"" + guid(ids) + "\"/></a:ext></a:extLst>"
				+ "</xdr:cNvPr><xdr:cNvGraphicFramePr/></xdr:nvGraphicFramePr>"
				+ "<xdr:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/></xdr:xfrm>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">"
				+ "<c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" "
				+ "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + r.getId() + "\"/>"
				+ "</a:graphicData></a:graphic></xdr:graphicFrame><xdr:clientData/></xdr:twoCellAnchor></xdr:wsDr>";
		CTDrawing parsed;
		try {
			parsed = (CTDrawing) XmlUtils.unwrap(XmlUtils.unmarshalString(xml, Context.jc));
		} catch (jakarta.xml.bind.JAXBException e) {
			throw new Docx4JException("the chart's anchor couldn't be made", e);
		}
		Object anchor = parsed.getEGAnchor().get(0);
		wsDr.getEGAnchor().add(anchor);
		ChartSettings.invoke(XmlUtils.unwrap(anchor), "setParent", Object.class, wsDr);
		return ChartRef.of(drawing, anchor);
	}

	private static long frameId(Object anchor) {
		Object f = ChartSeries.get(anchor, "getGraphicFrame");
		Object nv = f == null ? null : ChartSeries.get(f, "getNvGraphicFramePr");
		Object c = nv == null ? null : ChartSeries.get(nv, "getCNvPr");
		for (String kind : new String[] { "getSp", "getPic", "getCxnSp", "getGrpSp" }) {
			if (c != null) break;
			Object o = ChartSeries.get(anchor, kind);
			if (o == null) continue;
			for (String nvGetter : new String[] { "getNvSpPr", "getNvPicPr", "getNvCxnSpPr", "getNvGrpSpPr" }) {
				Object n = ChartSeries.get(o, nvGetter);
				if (n != null) c = ChartSeries.get(n, "getCNvPr");
			}
		}
		Object id = c == null ? null : ChartSeries.get(c, "getId");
		return id instanceof Number ? ((Number) id).longValue() : 0;
	}

	/** Beside the data: a column after it, from its first row, Excel's default size about */
	private static AreaRef beside(AreaRef data) {
		int c = data.getLast().getColumn() + 2, r = data.getFirst().getRow();
		return new AreaRef(new CellRef(null, c, r), new CellRef(null, c + DEFAULT_COLUMNS - 1, r + DEFAULT_ROWS - 1));
	}

	// ------------------------------------------------------------------ parts and names

	private static InputStream template(String name) throws Docx4JException {
		InputStream is = ChartCreator.class.getResourceAsStream("templates/" + name);
		if (is == null) throw new Docx4JException("no chart template " + name);
		return is;
	}

	/** The lowest number n for which prefix n suffix names no part */
	private static int next(OpcPackage pkg, String prefix, String suffix) throws InvalidFormatException {
		int n = 1;
		while (pkg.getParts().get(new PartName(prefix + n + suffix)) != null) n++;
		return n;
	}

	/** prefix n .xml where it names no part, else the next free number's */
	private static String free(OpcPackage pkg, String prefix, int n) throws InvalidFormatException {
		if (pkg.getParts().get(new PartName(prefix + n + ".xml")) == null) return prefix + n + ".xml";
		return prefix + next(pkg, prefix, ".xml") + ".xml";
	}

	private static SpreadsheetMLPackage workbook(WorksheetPart sheet) {
		if (!(sheet.getPackage() instanceof SpreadsheetMLPackage)) throw new IllegalArgumentException("the worksheet isn't in a workbook");
		return (SpreadsheetMLPackage) sheet.getPackage();
	}

	/** A worksheet's name, as its workbook lists it */
	static String sheetName(SpreadsheetMLPackage workbook, WorksheetPart part) throws Docx4JException {
		WorkbookPart wb = workbook.getWorkbookPart();
		for (Sheet s : wb.getContents().getSheets().getSheet()) {
			if (wb.getRelationshipsPart().getPart(s.getId()) == part) return s.getName();
		}
		throw new IllegalArgumentException("the worksheet isn't one of its workbook's sheets");
	}

	private static String guid(Random ids) {
		return "{" + new UUID(ids.nextLong(), ids.nextLong()).toString().toUpperCase(Locale.ROOT) + "}";
	}
}
