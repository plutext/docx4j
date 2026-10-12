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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.junit.Test;
import org.xlsx4j.model.AreaRef;
import org.xlsx4j.model.CellRef;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.sml.Cell;

/**
 * CR-036 phase 2's gate: a chart made by {@link ChartCreator} against Excel's own chart of the same
 * data and settings (the P1, P2 and P6 probes), as {@link ChartGate} compares them: the chart part
 * (Office's random ids aside), and for {@link ChartCreator#inSheet} the sheet's cells and table.
 * Each is made in Excel's own workbook, emptied of its chart, table and cells, so that what Excel
 * keeps per workbook (its styles, the sheet's default row descent) is Excel's on both sides.
 * Office writes a typed title's run in the language of the machine's keyboard (en-AU in the P6
 * run, en-US in P2's), which is set aside too; docx4j writes the chart's own language.
 *
 * <p>With {@code -Ddocx4j.chartCreate.write=<directory>} the created workbooks are written there,
 * for Excel's check ({@code excel-chart-edit-check.ps1}).
 */
public class ChartCreatorTest {

	private static final String WRITE = System.getProperty("docx4j.chartCreate.write");

	// ------------------------------------------------------------------ P1: each type, Word's data

	@Test
	public void p1EveryType() throws Exception {
		List<String> failures = new ArrayList<String>();
		for (ChartType t : ChartType.values()) check(failures, "p1-" + t.getTemplateName(), t, null);
		check(failures, "p1-date-axis", ChartType.COLUMN_CLUSTERED, null);
		report(failures);
	}

	// ------------------------------------------------------------------ P2 and P6: settings

	@Test
	public void p2SettingsOnAColumnChart() throws Exception {
		List<String> failures = new ArrayList<String>();
		for (String s : new String[] { "title-text", "no-title", "legend-bottom", "legend-top", "legend-left", "legend-right",
				"no-legend", "axis-titles", "data-labels", "number-format" }) {
			check(failures, "p2-" + s, ChartType.COLUMN_CLUSTERED, setting(s));
		}
		report(failures);
	}

	@Test
	public void p6SettingsOnEveryType() throws Exception {
		List<String> failures = new ArrayList<String>();
		for (ChartType t : ChartType.values()) {
			if (t == ChartType.COLUMN_CLUSTERED) continue;
			for (String s : new String[] { "title-text", "axis-titles", "data-labels", "number-format" }) {
				if (!t.hasAxes() && (s.equals("axis-titles") || s.equals("number-format"))) continue;
				check(failures, "p6-" + t.getTemplateName() + "-" + s, t, setting(s));
			}
		}
		report(failures);
	}

	private static Consumer<ChartDefinition> setting(String s) {
		switch (s) {
		case "title-text":
			return d -> d.title("Sales");
		case "no-title":
			return d -> d.noTitle();
		case "legend-bottom":
			return d -> d.legend(LegendPosition.BOTTOM);
		case "legend-top":
			return d -> d.legend(LegendPosition.TOP);
		case "legend-left":
			return d -> d.legend(LegendPosition.LEFT);
		case "legend-right":
			return d -> d.legend(LegendPosition.RIGHT);
		case "no-legend":
			return d -> d.noLegend();
		case "axis-titles":
			return d -> d.categoryAxis(a -> a.title("Quarter")).valueAxis(a -> a.title("Units"));
		case "data-labels":
			return d -> d.dataLabels(true);
		case "number-format":
			return d -> d.valueAxis(a -> a.numberFormat("0.0"));
		default:
			throw new IllegalArgumentException(s);
		}
	}

	// ------------------------------------------------------------------ P6: kinds and shapes of data

	@Test
	public void p6DatesTextAndShapes() throws Exception {
		List<String> failures = new ArrayList<String>();
		for (ChartType t : ChartType.values()) {
			if (t != ChartType.COLUMN_CLUSTERED) check(failures, "p6-" + t.getTemplateName() + "-dates", t, null);
			check(failures, "p6-" + t.getTemplateName() + "-two-by-two", t, null);
		}
		check(failures, "p6-area-text", ChartType.AREA, null);
		check(failures, "p6-scatter-text", ChartType.SCATTER, null);
		for (ChartType t : new ChartType[] { ChartType.COLUMN_CLUSTERED, ChartType.LINE_MARKERS, ChartType.PIE, ChartType.SCATTER }) {
			check(failures, "p6-" + t.getTemplateName() + "-one-by-one", t, null);
		}
		for (ChartType t : new ChartType[] { ChartType.COLUMN_CLUSTERED, ChartType.LINE_MARKERS, ChartType.AREA }) {
			check(failures, "p6-" + t.getTemplateName() + "-eight-series", t, null);
		}
		check(failures, "p6-pie-eight-points", ChartType.PIE, null);
		check(failures, "p6-doughnut-eight-points", ChartType.DOUGHNUT, null);
		report(failures);
	}

	// ------------------------------------------------------------------ fromCells: Excel's reading

	@Test
	public void p6RangesAsExcelReadsThem() throws Exception {
		List<String> failures = new ArrayList<String>();
		// Excel's chart there was made on an empty sheet and then given its data (AddChart2, then
		// SetSourceData), which leaves the legend out, as no insert over a selection does: set aside
		cells(failures, "p6-range-other-sheet", "'My Data'!B3:E7", ChartType.COLUMN_CLUSTERED);
		cells(failures, "p6-range-wide", "Sheet1!A1:E3", ChartType.COLUMN_CLUSTERED);
		cells(failures, "p6-range-numbers", "Sheet1!A1:C4", ChartType.COLUMN_CLUSTERED);
		cells(failures, "p6-range-numbers-header", "Sheet1!A1:C4", ChartType.COLUMN_CLUSTERED);
		cells(failures, "p6-range-no-header", "Sheet1!A1:B4", ChartType.COLUMN_CLUSTERED);
		cells(failures, "p6-range-text-corner", "Sheet1!A1:C4", ChartType.COLUMN_CLUSTERED);
		// a table whose first column holds numbers under a " " header: they're a series
		cells(failures, "p6-column-clustered-numbers", "Sheet1!A1:C5", ChartType.COLUMN_CLUSTERED);
		cells(failures, "p6-line-numbers", "Sheet1!A1:C5", ChartType.LINE);
		cells(failures, "p6-pie-numbers", "Sheet1!A1:C5", ChartType.PIE);
		report(failures);
	}

	// ------------------------------------------------------------------ the comparison

	/** inSheet of the fixture's data (and settings) into its emptied workbook, against Excel's chart */
	private static void check(List<String> failures, String fixture, ChartType type, Consumer<ChartDefinition> settings) {
		try {
			SpreadsheetMLPackage excel = (SpreadsheetMLPackage) ChartGate.load("authoring/" + fixture + ".xlsx");
			String chart = xml(ChartGate.chart(excel));
			String cells = ChartGate.cells(excel, false);
			String table = ChartGate.table(excel);

			SpreadsheetMLPackage wb = (SpreadsheetMLPackage) ChartGate.load("authoring/" + fixture + ".xlsx");
			ChartDefinition d = definition(wb, type);
			if (settings != null) settings.accept(d);
			WorksheetPart sheet = emptied(wb);
			ChartRef ref = new ChartCreator(new Random(fixture.hashCode())).inSheet(sheet, d, new CellRef(null, 1, 1), null);
			assertNotNull(ref);
			sameAnchor(excel, ref);
			write(wb, fixture);
			SpreadsheetMLPackage saved = (SpreadsheetMLPackage) ChartGate.roundTrip(wb);
			onlyOurs(excel, saved);
			compare(failures, fixture + " drawing", drawing(excel), drawing(saved));
			compare(failures, fixture + " chart", chart, xml(ChartGate.chart(saved)));
			compare(failures, fixture + " cells", cells, ChartGate.cells(saved, false));
			// P1's and P2's workbooks are Word's embedded ones charted by Excel, their tables Word's
			// (the area's and the scatter's without an autoFilter); P6's tables Excel made
			if (fixture.startsWith("p6-")) compare(failures, fixture + " table", table, ChartGate.table(saved));
		} catch (Throwable e) {
			failures.add(fixture + ": " + e);
			e.printStackTrace();
		}
	}

	/** fromCells over the fixture's own cells (its chart taken out), against Excel's chart */
	private static void cells(List<String> failures, String fixture, String area, ChartType type) {
		try {
			SpreadsheetMLPackage excel = (SpreadsheetMLPackage) ChartGate.load("authoring/" + fixture + ".xlsx");
			String chart = xml(ChartGate.chart(excel));
			SpreadsheetMLPackage wb = (SpreadsheetMLPackage) ChartGate.load("authoring/" + fixture + ".xlsx");
			WorksheetPart sheet = SheetCells.of(wb, "Sheet1").getPart();
			removeDrawing(sheet);
			ChartRef ref = new ChartCreator(new Random(fixture.hashCode())).fromCells(sheet, AreaRef.parse(area), type,
					AreaRef.parse("H2:P16"));
			if (fixture.equals("p6-range-other-sheet")) ChartSettings.removeLegend((Chart) ref.getChartPart());
			sameAnchor(excel, ref);
			write(wb, fixture);
			SpreadsheetMLPackage saved = (SpreadsheetMLPackage) ChartGate.roundTrip(wb);
			onlyOurs(excel, saved);
			compare(failures, fixture + " drawing", drawing(excel), drawing(saved));
			compare(failures, fixture + " chart", chart, xml(ChartGate.chart(saved)));
		} catch (Throwable e) {
			failures.add(fixture + ": " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Our chart placed exactly where Excel's is (its cells and offsets), so that Excel's pictures
	 * of the two can be compared pixel for pixel (the Office check)
	 */
	private static void sameAnchor(SpreadsheetMLPackage excel, ChartRef ours) throws Exception {
		CTTwoCellAnchor theirs = (CTTwoCellAnchor) Charts.in(excel).get(0).getAnchor();
		CTTwoCellAnchor a = (CTTwoCellAnchor) org.docx4j.XmlUtils.unwrap(ours.getAnchor());
		a.setFrom((CTMarker) theirs.getFrom().copy());
		a.setTo((CTMarker) theirs.getTo().copy());
	}

	/** The saved workbook's one chart is ours, not Excel's left behind: a part of its own, fresh ids */
	private static void onlyOurs(SpreadsheetMLPackage excel, SpreadsheetMLPackage saved) throws Exception {
		int n = 0;
		for (Part p : saved.getParts().getParts().values()) if (p instanceof Chart) n++;
		assertEquals("chart parts", 1, n);
		assertTrue("fresh ids", !ChartGate.chartXml(ChartGate.chart(excel), false).equals(ChartGate.chartXml(ChartGate.chart(saved), false)));
	}

	/** The sheet's drawing as docx4j marshals it, the frame's random creationId aside */
	private static String drawing(SpreadsheetMLPackage pkg) throws Exception {
		ChartRef r = Charts.in(pkg).get(0);
		String xml = org.docx4j.XmlUtils.marshaltoString(((org.docx4j.openpackaging.parts.JaxbXmlPart<?>) r.getHost()).getJaxbElement(), true, true);
		return xml.replaceAll("creationId([^>]*) id=\"\\{[^}]*\\}\"", "creationId$1 id=\"ID\"").replaceAll(" r:id=\"rId\\d+\"", " r:id=\"rId\"");
	}

	private static String xml(Chart c) throws Exception {
		return ChartGate.chartXml(c, true).replace("lang=\"en-AU\"", "lang=\"en-US\"");
	}

	private static void compare(List<String> failures, String what, String expected, String actual) {
		if (expected.equals(actual)) return;
		int i = 0;
		while (i < Math.min(expected.length(), actual.length()) && expected.charAt(i) == actual.charAt(i)) i++;
		int from = Math.max(0, i - 200);
		failures.add(what + " differs at " + i + ":\n  excel:  ..." + expected.substring(from, Math.min(expected.length(), i + 300))
				+ "\n  docx4j: ..." + actual.substring(from, Math.min(actual.length(), i + 300)));
	}

	private static void report(List<String> failures) {
		if (!failures.isEmpty()) fail(failures.size() + " differ:\n" + String.join("\n\n", failures));
	}

	private static void write(SpreadsheetMLPackage wb, String fixture) throws Exception {
		if (WRITE == null) return;
		File dir = new File(WRITE);
		dir.mkdirs();
		wb.save(new File(dir, fixture + ".xlsx"));
	}

	/**
	 * The definition of a fixture's data, as its sheet lays it out (Word's way, and Excel's for
	 * P6): the header row the series' names over a corner, the first column the categories (dates
	 * where they are formatted as dates)
	 */
	static ChartDefinition definition(SpreadsheetMLPackage wb, ChartType type) throws Exception {
		SheetCells sc = SheetCells.of(wb, "Sheet1");
		TablePart t = Tables.of(sc.getPart()).get(0);
		AreaRef a = Tables.getRef(t);
		int c0 = a.getFirst().getColumn(), r0 = a.getFirst().getRow();
		List<Object> categories = new ArrayList<Object>();
		String dateFormat = null;
		for (int r = r0 + 1; r <= a.getLast().getRow(); r++) {
			Cell c = sc.getCell(c0, r);
			String text = sc.getText(c);
			if (!SheetCells.isNumber(c)) categories.add(text);
			else if (sc.getFormatCode(c).contains("m")) {
				dateFormat = sc.getFormatCode(c);
				categories.add(LocalDate.of(1899, 12, 30).plusDays((long) Double.parseDouble(text)));
			} else categories.add(Double.parseDouble(text));
		}
		ChartDefinition d = ChartDefinition.of(type).categories(categories);
		if (dateFormat != null) d.dateFormat(dateFormat);
		d.categoriesHeader(sc.getText(sc.getCell(c0, r0)));
		for (int c = c0 + 1; c <= a.getLast().getColumn(); c++) {
			List<Double> v = new ArrayList<Double>();
			for (int r = r0 + 1; r <= a.getLast().getRow(); r++) {
				Cell cell = sc.getCell(c, r);
				v.add(SheetCells.isNumber(cell) ? Double.valueOf(cell.getV()) : null);
			}
			d.series(sc.getText(sc.getCell(c, r0)), v);
		}
		return d;
	}

	/** Sheet1 of Excel's workbook with its drawing (and chart), its tables and its cells taken out */
	static WorksheetPart emptied(SpreadsheetMLPackage wb) throws Exception {
		WorksheetPart sheet = SheetCells.of(wb, "Sheet1").getPart();
		removeDrawing(sheet);
		for (TablePart t : Tables.of(sheet)) sheet.getRelationshipsPart().removePart(t.getPartName());
		sheet.getContents().setTableParts(null);
		sheet.getContents().getSheetData().getRow().clear();
		return sheet;
	}

	private static void removeDrawing(WorksheetPart sheet) throws Exception {
		if (sheet.getContents().getDrawing() == null) return;
		Part d = sheet.getRelationshipsPart().getPart(sheet.getContents().getDrawing().getId());
		sheet.getRelationshipsPart().removePart(d.getPartName());
		sheet.getContents().setDrawing(null);
	}

	// ------------------------------------------------------------------ settings on a loaded chart (A3)

	/** Excel's chart, loaded, given each of P2's settings by ChartSettings: Excel's own chart with it */
	@Test
	public void settingsOnALoadedChart() throws Exception {
		List<String> failures = new ArrayList<String>();
		String[] settings = { "title-text", "no-title", "legend-top", "legend-left", "legend-right", "no-legend", "axis-titles",
				"data-labels", "number-format" };
		for (String s : settings) {
			SpreadsheetMLPackage excel = (SpreadsheetMLPackage) ChartGate.load("authoring/p2-" + s + ".xlsx");
			SpreadsheetMLPackage wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p1-column-clustered.xlsx");
			Chart c = ChartGate.chart(wb);
			switch (s) {
			case "title-text": ChartSettings.setTitle(c, "Sales"); break;
			case "no-title": ChartSettings.removeTitle(c); break;
			case "legend-top": ChartSettings.setLegend(c, LegendPosition.TOP); break;
			case "legend-left": ChartSettings.setLegend(c, LegendPosition.LEFT); break;
			case "legend-right": ChartSettings.setLegend(c, LegendPosition.RIGHT); break;
			case "no-legend": ChartSettings.removeLegend(c); break;
			case "axis-titles":
				ChartSettings.setAxisTitle(c, ChartSettings.Axis.CATEGORY, "Quarter");
				ChartSettings.setAxisTitle(c, ChartSettings.Axis.VALUE, "Units");
				break;
			case "data-labels": ChartSettings.setDataLabels(c, true); break;
			case "number-format": ChartSettings.setNumberFormat(c, ChartSettings.Axis.VALUE, "0.0"); break;
			default: throw new IllegalArgumentException(s);
			}
			compare(failures, "p2-" + s + " by ChartSettings", xml(ChartGate.chart(excel)), xml(ChartGate.chart(ChartGate.roundTrip(wb))));
		}
		// and undone: each setting's reverse gives Excel's chart without it back
		SpreadsheetMLPackage plain = (SpreadsheetMLPackage) ChartGate.load("authoring/p1-column-clustered.xlsx");
		SpreadsheetMLPackage wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p2-axis-titles.xlsx");
		Chart c = ChartGate.chart(wb);
		ChartSettings.setAxisTitle(c, ChartSettings.Axis.CATEGORY, null);
		ChartSettings.setAxisTitle(c, ChartSettings.Axis.VALUE, null);
		compare(failures, "axis titles removed", xml(ChartGate.chart(plain)), xml(ChartGate.chart(ChartGate.roundTrip(wb))));
		wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p2-data-labels.xlsx");
		ChartSettings.setDataLabels(ChartGate.chart(wb), false);
		compare(failures, "data labels removed", xml(ChartGate.chart(plain)), xml(ChartGate.chart(ChartGate.roundTrip(wb))));
		wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p2-title-text.xlsx");
		ChartSettings.setTitle(ChartGate.chart(wb), null);
		compare(failures, "title text removed", xml(ChartGate.chart(plain)), xml(ChartGate.chart(ChartGate.roundTrip(wb))));
		wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p2-number-format.xlsx");
		ChartSettings.setNumberFormat(ChartGate.chart(wb), ChartSettings.Axis.VALUE, null);
		compare(failures, "number format linked again", xml(ChartGate.chart(plain)), xml(ChartGate.chart(ChartGate.roundTrip(wb))));
		report(failures);
	}

	// ------------------------------------------------------------------ reproducible; a new workbook

	/** The same seed, the same chart, drawing and table */
	@Test
	public void seededIdsReproduce() throws Exception {
		String[] first = made(7), second = made(7), other = made(8);
		for (int i = 0; i < first.length; i++) assertEquals(first[i], second[i]);
		assertTrue(!first[0].equals(other[0]));
	}

	private static String[] made(long seed) throws Exception {
		SpreadsheetMLPackage wb = SpreadsheetMLPackage.createPackage();
		WorksheetPart sheet = wb.createWorksheetPart(new org.docx4j.openpackaging.parts.PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		ChartRef r = new ChartCreator(new Random(seed)).inSheet(sheet,
				ChartDefinition.of(ChartType.PIE).categories("A", "B", "C").series("Sales", 1, 2, 3), new CellRef(null, 1, 1), null);
		SpreadsheetMLPackage saved = (SpreadsheetMLPackage) ChartGate.roundTrip(wb);
		return new String[] { ChartGate.chartXml(ChartGate.chart(saved), false), drawing(saved),
				org.docx4j.XmlUtils.marshaltoString(Tables.of(SheetCells.of(saved, "Sheet1").getPart()).get(0).getJaxbElement(), true, true,
				org.xlsx4j.jaxb.Context.jcSML) };
	}

	/**
	 * In a workbook docx4j made (no styles part, no theme): dates make its styles, as Excel's new
	 * workbook has them; a second chart on the same sheet shares its drawing and takes the next
	 * parts and table
	 */
	@Test
	public void inANewWorkbook() throws Exception {
		SpreadsheetMLPackage wb = SpreadsheetMLPackage.createPackage();
		WorksheetPart sheet = wb.createWorksheetPart(new org.docx4j.openpackaging.parts.PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		ChartCreator cc = new ChartCreator(new Random(3));
		cc.inSheet(sheet, ChartDefinition.of(ChartType.LINE_MARKERS)
				.dates("d-mmm", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 3))
				.series("Plan", 1, 2, 3).series("Actual", 2, 3, 4).title("By day"), new CellRef(null, 1, 1), null);
		cc.inSheet(sheet, ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories("North", "South")
				.series("Plan", 1.5, 3.5).series("Actual", 2.5, 4.5).legend(LegendPosition.RIGHT), new CellRef(null, 1, 20), null);
		write(wb, "new-workbook");
		SpreadsheetMLPackage saved = (SpreadsheetMLPackage) ChartGate.roundTrip(wb);
		List<ChartRef> charts = Charts.in(saved);
		assertEquals(2, charts.size());
		assertEquals("/xl/charts/chart1.xml", charts.get(0).getChartPart().getPartName().getName());
		assertEquals("/xl/charts/chart2.xml", charts.get(1).getChartPart().getPartName().getName());
		assertEquals(charts.get(0).getHost(), charts.get(1).getHost());
		assertEquals("Chart 2", charts.get(1).getName());
		SheetCells sc = SheetCells.of(saved, "Sheet1");
		List<TablePart> tables = Tables.of(sc.getPart());
		assertEquals(2, tables.size());
		assertEquals("Table2", tables.get(1).getContents().getName());
		assertEquals("A20:C22", tables.get(1).getContents().getRef());
		Cell d = sc.getCell(1, 2);
		assertEquals("46023", d.getV());
		assertEquals("d-mmm", sc.getFormatCode(d));
		ChartModel m = charts.get(0).read();
		assertEquals("By day", m.getTitle().getText());
		assertEquals(ChartAxis.Kind.DATE, m.getAxes().get(0).getKind());
		// an editor on the made chart: a category added grows the table
		try (ChartDataEditor e = ChartDataEditor.of((Chart) charts.get(1).getChartPart())) {
			e.addCategory("East", 1.0, 2.0);
		}
		assertEquals("A20:C23", Tables.of(sc.getPart()).get(1).getContents().getRef());
	}

	// ------------------------------------------------------------------ refusals and the rest

	@Test
	public void refusals() throws Exception {
		SpreadsheetMLPackage wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p1-column-clustered.xlsx");
		WorksheetPart sheet = SheetCells.of(wb, "Sheet1").getPart();
		ChartCreator cc = new ChartCreator(new Random(1));
		ChartDefinition d = ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories("A").series("S", 1);
		refused(() -> cc.inSheet(sheet, d, new CellRef(null, 2, 3), null), "isn't empty");
		new SheetCells(wb, sheet, "Sheet1").clear(4, 5);
		refused(() -> cc.inSheet(sheet, d, new CellRef(null, 4, 5), null), "overlap the table");
		refused(() -> cc.inSheet(sheet, ChartDefinition.of(ChartType.PIE).categories("A"), new CellRef(null, 10, 10), null), "no series");
		refused(() -> cc.inSheet(sheet, ChartDefinition.of(ChartType.PIE).series("S", 1), new CellRef(null, 10, 10), null), "no categories");
		refused(() -> cc.inSheet(sheet, ChartDefinition.of(ChartType.PIE).categories("A").series("S", 1, 2), new CellRef(null, 10, 10), null),
				"more values");
		refused(() -> cc.inSheet(sheet, ChartDefinition.of(ChartType.PIE).categories("A").series("S", 1).valueAxis(a -> a.title("x")),
				new CellRef(null, 10, 10), null), "no axes");
		refused(() -> cc.inSheet(sheet, ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories("A").series("S", 1)
				.group(ChartType.LINE, g -> g.series("T", 2)), new CellRef(null, 10, 10), null), "combination");
		refused(() -> ChartDefinition.of(ChartType.LINE).categories(java.util.Arrays.asList("A", 1.0)), "all text");
	}

	private interface Call {
		void run() throws Exception;
	}

	private static void refused(Call c, String why) {
		try {
			c.run();
			fail("not refused: " + why);
		} catch (Exception e) {
			assertTrue(e.getMessage(), e.getMessage().contains(why));
		}
	}
}
