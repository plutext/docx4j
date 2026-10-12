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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.junit.Test;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.sml.CTCellFormula;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

/**
 * The chart data editor against Excel's own edits (CR-035 phase 4's gate): each probe of
 * excel-chart-edit-probes.ps1 is a workbook Excel saved before and after one edit; the editor
 * makes the same edit to the first, and the result is compared with Excel's, part by part: the
 * chart part (numbers as numbers: Excel writes 2.2 as 2.2000000000000002), the cells (strings by
 * their text, so the shared strings' order, which Excel rewrites on save, doesn't count), the
 * rows' spans, the sheet's dimension, the table.  With
 * {@code -Ddocx4j.chartEdits.write=<directory>} the edited workbooks are written there, for
 * Excel to open (excel-chart-edit-check.ps1).
 */
public class ChartDataEditorTest {

	interface Edit {
		void apply(ChartDataEditor e, List<ChartSeries> series) throws Exception;
	}

	static SpreadsheetMLPackage load(String name) throws Exception {
		try (InputStream is = ChartDataEditorTest.class.getClassLoader().getResourceAsStream("charts/" + name)) {
			assertNotNull(name, is);
			return (SpreadsheetMLPackage) OpcPackage.load(is);
		}
	}

	static Chart chart(SpreadsheetMLPackage pkg) {
		for (Part p : pkg.getParts().getParts().values()) if (p instanceof Chart) return (Chart) p;
		return null;
	}

	/** Excel's edit, made by the editor and compared with Excel's */
	private static void excels(String probe, boolean formulas, Edit edit) throws Exception {
		SpreadsheetMLPackage ours = load("edit/" + probe + "-before.xlsx"), excel = load("edit/" + probe + "-after.xlsx");
		// Excel's edit changed the chart and the cells, so that the comparison below isn't idle
		assertNotEquals(probe, chartXml(excel), chartXml(ours));
		assertNotEquals(probe, cells(excel, formulas), cells(ours, formulas));
		ChartDataEditor e = ChartDataEditor.of(chart(ours));
		edit.apply(e, e.getSeries());
		write(ours, probe);
		assertEquals(probe + ": the chart", chartXml(excel), chartXml(ours));
		assertEquals(probe + ": the cells", cells(excel, formulas), cells(ours, formulas));
		assertEquals(probe + ": the table", table(excel), table(ours));
		Chart c = chart(ours);
		assertEquals(probe + ": caches and cells", 0, ChartCells.of(c).differences(ChartModel.read(c, null)).size());
	}

	static void write(SpreadsheetMLPackage pkg, String name) throws Exception {
		String dir = System.getProperty("docx4j.chartEdits.write");
		if (dir != null) pkg.save(new File(dir, name + "-docx4j.xlsx"));
	}

	private static final Pattern V = Pattern.compile("<c:v>([^<]*)</c:v>");

	/** The chart part as docx4j writes it, each cached number in one form */
	static String chartXml(SpreadsheetMLPackage pkg) throws Exception {
		String xml = XmlUtils.marshaltoString(chart(pkg).getJaxbElement(), true, true);
		Matcher m = V.matcher(xml);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String v = m.group(1);
			try {
				v = Double.toString(Double.parseDouble(v));
			} catch (NumberFormatException e) {
				// text
			}
			m.appendReplacement(sb, Matcher.quoteReplacement("<c:v>" + v + "</c:v>"));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/** Sheet1's dimension, rows (spans) and cells (type, style, value by its text, formula) */
	static String cells(SpreadsheetMLPackage pkg, boolean formulas) throws Exception {
		SheetCells sc = SheetCells.of(pkg, "Sheet1");
		StringBuilder sb = new StringBuilder("dimension " + sc.getPart().getContents().getDimension().getRef() + "\n");
		for (Row r : sc.getPart().getContents().getSheetData().getRow()) {
			sb.append("row ").append(r.getR()).append(" spans ").append(r.getSpans()).append(" dyDescent ")
					.append(r.getDyDescent()).append('\n');
			for (Cell c : r.getC()) {
				String v = sc.getText(c);
				if (SheetCells.isNumber(c)) v = Double.toString(Double.parseDouble(v));
				sb.append("  ").append(c.getR()).append(" t=").append(c.getT() == null ? "n" : c.getT().value())
						.append(" s=").append(c.getS()).append(" v=").append(v);
				if (formulas && c.getF() != null) sb.append(" f=").append(c.getF().getValue());
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	/** The sheet's table: ref, autoFilter, columns (id, name; a uid each) */
	static String table(SpreadsheetMLPackage pkg) throws Exception {
		SheetCells sc = SheetCells.of(pkg, "Sheet1");
		StringBuilder sb = new StringBuilder();
		for (TablePart t : Tables.of(sc.getPart())) {
			CTTable ct = t.getContents();
			sb.append(ct.getName()).append(' ').append(ct.getRef()).append(" autoFilter ")
					.append(ct.getAutoFilter() == null ? null : ct.getAutoFilter().getRef()).append(" count ")
					.append(ct.getTableColumns().getCount()).append('\n');
			for (CTTableColumn c : ct.getTableColumns().getTableColumn()) {
				sb.append("  ").append(c.getId()).append(" '").append(c.getName()).append("' uid ")
						.append(c.getUid() != null).append('\n');
			}
		}
		return sb.toString();
	}

	// ------------------------------------------------------------------ Excel's seven edits

	@Test
	public void e1Value() throws Exception {
		excels("e1-value", true, (e, s) -> e.setValue(s.get(1), 1, 7.5));
	}

	@Test
	public void e2Labels() throws Exception {
		excels("e2-labels", true, (e, s) -> {
			e.setCategory(1, "Second");
			e.setSeriesName(s.get(1), "Revenue");
		});
	}

	@Test
	public void e3AddCategory() throws Exception {
		excels("e3-add-category", true, (e, s) -> e.addCategory("Category 5", 3.1, 2.2, 4.0));
	}

	@Test
	public void e4AddSeries() throws Exception {
		excels("e4-add-series", true, (e, s) -> e.addSeries("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	@Test
	public void e5RemoveSeries() throws Exception {
		excels("e5-remove-series", true, (e, s) -> e.removeSeries(s.get(2)));
	}

	@Test
	public void e6RemoveCategory() throws Exception {
		excels("e6-remove-category", true, (e, s) -> e.removeCategory(3));
	}

	/** Excel's #N/A was the formula =NA(); the editor writes the error as a constant, so formulas aside */
	@Test
	public void e7BlankAndError() throws Exception {
		excels("e7-blank-and-error", false, (e, s) -> {
			e.setBlank(s.get(0), 1);
			e.setError(s.get(2), 2, "#N/A");
		});
	}

	/** A control: an edit other than Excel's is told apart */
	@Test
	public void aDifferentEditIsToldApart() throws Exception {
		SpreadsheetMLPackage ours = load("edit/e1-value-before.xlsx"), excel = load("edit/e1-value-after.xlsx");
		ChartDataEditor e = ChartDataEditor.of(chart(ours));
		e.setValue(e.getSeries().get(1), 1, 7.6);
		assertNotEquals(chartXml(excel), chartXml(ours));
		assertNotEquals(cells(excel, true), cells(ours, true));
	}

	// ------------------------------------------------------------------ Excel's second round

	@Test
	public void r2DeleteMiddleCategory() throws Exception {
		excels("r2-delete-middle-category", true, (e, s) -> e.removeCategory(1));
	}

	@Test
	public void r3ByRowAddSeries() throws Exception {
		excels("r3-byrow-add-series", true, (e, s) -> e.addSeries("Category 5", 3.1, 2.2, 4.0));
	}

	@Test
	public void r4ByRowAddCategory() throws Exception {
		excels("r4-byrow-add-category", true, (e, s) -> e.addCategory("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	@Test
	public void r6ByRowDeleteCategory() throws Exception {
		excels("r6-byrow-delete-category", true, (e, s) -> e.removeCategory(1));
	}

	/** A name another column has: Excel's "Series 12", in the cell and the table */
	@Test
	public void r7DuplicateName() throws Exception {
		excels("r7-duplicate-name", true, (e, s) -> e.setSeriesName(s.get(1), "Series 1"));
	}

	/** Dates in column A (Excel plotted them as a series): the new cell takes the date style above it */
	@Test
	public void r8bNewCellTakesTheStyleAbove() throws Exception {
		excels("r8b-date-number", true, (e, s) -> e.addCategory("(no categories)", 46027.0, 3.1, 2.2, 4.0));
	}

	/** A line chart: line and marker in the next colour */
	@Test
	public void r9LineAddSeries() throws Exception {
		excels("r9-line-add-series", true, (e, s) -> e.addSeries("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	/** The last series' data labels aren't the new one's */
	@Test
	public void r10LabelsAddSeries() throws Exception {
		excels("r10-labels-add-series", true, (e, s) -> e.addSeries("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	/** The last series' own colour (red) isn't the new one's: it takes its automatic colour */
	@Test
	public void r11ColourAddSeries() throws Exception {
		excels("r11-colour-add-series", true, (e, s) -> e.addSeries("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	/** Excel's chart filter: every reference grows, the filtered series' and c15:fullRef too */
	@Test
	public void r12FilteredAddCategory() throws Exception {
		excels("r12-filtered-add-category", true, (e, s) -> e.addCategory("Category 5", 3.1, 2.2, 4.0));
	}

	/**
	 * Excel's chart filter: the new series' references the union shown and the whole column; its
	 * values are its points shown, so the filtered category's cell (E4) is written as a cell
	 */
	@Test
	public void r13FilteredAddSeries() throws Exception {
		excels("r13-filtered-add-series", true, (e, s) -> {
			e.addSeries("Series 4", 1.5, 2.5, 4.5);
			SheetCells.of(e.getWorkbook(), "Sheet1").setNumber(5, 4, 3.5);
		});
	}

	@Test
	public void r14MultiLevelAddCategory() throws Exception {
		excels("r14-multilevel-add-category", true, (e, s) -> e.addCategory("Q3", 3.9));
	}

	/**
	 * Deleting a table's middle column (or, plotted by row, its middle row), Excel keeps the
	 * chart's series by position: the second series, its formatting and idx, now plots what was
	 * the third's data, and the last series goes.  The editor removes the series it is given, so
	 * its formatting goes with it (D12).  The cells, the table and the chart's data are Excel's.
	 */
	private static void byPosition(String probe, Edit edit) throws Exception {
		SpreadsheetMLPackage ours = load("edit/" + probe + "-before.xlsx"), excel = load("edit/" + probe + "-after.xlsx");
		ChartDataEditor e = ChartDataEditor.of(chart(ours));
		edit.apply(e, e.getSeries());
		write(ours, probe);
		assertEquals(probe, cells(excel, true), cells(ours, true));
		assertEquals(probe, table(excel), table(ours));
		ChartModel mo = ChartModel.read(chart(ours), null), me = ChartModel.read(chart(excel), null);
		assertEquals(probe, ChartTable.of(me).getHeader(), ChartTable.of(mo).getHeader());
		assertEquals(probe, ChartTable.of(me).getRows(), ChartTable.of(mo).getRows());
		List<ChartSeries> so = mo.getGroups().get(0).getSeries(), se = me.getGroups().get(0).getSeries();
		assertEquals(se.size(), so.size());
		for (int i = 0; i < se.size(); i++) {
			assertEquals(se.get(i).getName().getReference(), so.get(i).getName().getReference());
			assertEquals(se.get(i).getCategories().getReference(), so.get(i).getCategories().getReference());
			assertEquals(se.get(i).getValues().getReference(), so.get(i).getValues().getReference());
		}
		assertEquals(1, se.get(1).getIdx());
		assertEquals(2, so.get(1).getIdx());
		assertEquals(0, ChartCells.of(chart(ours)).differences(mo).size());
	}

	@Test
	public void r1DeleteMiddleSeries() throws Exception {
		byPosition("r1-delete-middle-series", (e, s) -> e.removeSeries(s.get(1)));
	}

	@Test
	public void r5ByRowDeleteSeries() throws Exception {
		byPosition("r5-byrow-delete-series", (e, s) -> e.removeSeries(s.get(1)));
	}

	// ------------------------------------------------------------------ beyond Excel's edits

	/** D10: a formula isn't overwritten unless asked; then Excel is told to recalculate */
	@Test
	public void formulas() throws Exception {
		SpreadsheetMLPackage pkg = load("edit/e1-value-before.xlsx");
		SheetCells sc = SheetCells.of(pkg, "Sheet1");
		CTCellFormula f = new CTCellFormula();
		f.setValue("1+3.4");
		sc.getCell(3, 3).setF(f);
		CTCellFormula elsewhere = new CTCellFormula();
		elsewhere.setValue("B2*2");
		sc.cell(6, 1).setF(elsewhere);
		ChartDataEditor e = ChartDataEditor.of(chart(pkg));
		try {
			e.setValue(e.getSeries().get(1), 1, 7.5);
			fail("overwrote a formula");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("Sheet1!$C$3"));
		}
		assertTrue(pkg.getWorkbookPart().getContents().getCalcPr() == null
				|| !pkg.getWorkbookPart().getContents().getCalcPr().isFullCalcOnLoad());
		e.setOverwriteFormulas(true).setValue(e.getSeries().get(1), 1, 7.5);
		assertEquals(null, sc.getCell(3, 3).getF());
		assertEquals("7.5", sc.getCell(3, 3).getV());
		assertTrue(pkg.getWorkbookPart().getContents().getCalcPr().isFullCalcOnLoad());
		// a formula in the cells a removal would move is refused, asked or not
		sc.getCell(4, 5).setF(f);
		try {
			e.removeCategory(1);
			fail("moved a formula");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("move"));
		}
	}

	/** A middle series removed: its column taken out of the table, the next one moved into it */
	@Test
	public void middleSeriesRemoved() throws Exception {
		SpreadsheetMLPackage pkg = load("edit/e1-value-before.xlsx");
		ChartDataEditor e = ChartDataEditor.of(chart(pkg));
		e.removeSeries(e.getSeries().get(1));
		List<ChartSeries> s = e.getSeries();
		assertEquals(2, s.size());
		assertEquals("Series 3", s.get(1).getShownName());
		assertEquals("Sheet1!$C$2:$C$5", s.get(1).getValues().getReference());
		assertEquals("Sheet1!$C$1", s.get(1).getName().getReference());
		assertEquals("Table1 A1:C5 autoFilter A1:C5 count 3\n  1 ' ' uid true\n  2 'Series 1' uid true\n"
				+ "  4 'Series 3' uid true\n", table(pkg));
		SheetCells sc = SheetCells.of(pkg, "Sheet1");
		assertEquals("2", sc.getText(sc.getCell(3, 2)));
		assertEquals(null, sc.getCell(4, 2));
		assertEquals(0, ChartCells.of(chart(pkg)).differences(ChartModel.read(chart(pkg), null)).size());
	}

	/** A middle category removed: its row taken out of the table, the rows under it moved up */
	@Test
	public void middleCategoryRemoved() throws Exception {
		SpreadsheetMLPackage pkg = load("edit/e1-value-before.xlsx");
		ChartDataEditor e = ChartDataEditor.of(chart(pkg));
		e.removeCategory(1);
		ChartSeries s = e.getSeries().get(0);
		assertEquals("Sheet1!$A$2:$A$4", s.getCategories().getReference());
		assertEquals("Category 3", s.getCategories().getText(1));
		assertEquals("3.5", s.getValues().getText(1));
		assertTrue(table(pkg), table(pkg).startsWith("Table1 A1:D4 autoFilter A1:D4 count 4"));
		assertEquals(0, ChartCells.of(chart(pkg)).differences(ChartModel.read(chart(pkg), null)).size());
	}

	/** setData: the shape changed both ways at once, every cell written */
	@Test
	public void setData() throws Exception {
		SpreadsheetMLPackage pkg = load("edit/e1-value-before.xlsx");
		ChartDataEditor e = ChartDataEditor.of(chart(pkg));
		e.setData(Arrays.asList("North", "South"), Arrays.asList("2025", "2026", "Plan", "Gap"),
				new Double[][] { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, null }, { 7.0, 8.0 } });
		List<ChartSeries> s = e.getSeries();
		assertEquals(Arrays.asList("2025", "2026", "Plan", "Gap"), Arrays.asList(s.get(0).getShownName(),
				s.get(1).getShownName(), s.get(2).getShownName(), s.get(3).getShownName()));
		assertEquals("Sheet1!$E$2:$E$3", s.get(3).getValues().getReference());
		assertEquals(null, s.get(2).getValues().getText(1));
		assertEquals("South", s.get(3).getCategories().getText(1));
		assertTrue(table(pkg), table(pkg).startsWith("Table1 A1:E3 autoFilter A1:E3 count 5"));
		assertEquals(0, ChartCells.of(chart(pkg)).differences(ChartModel.read(chart(pkg), null)).size());
		ChartTable t = ChartTable.of(ChartModel.read(chart(pkg), null));
		assertEquals(Arrays.asList("North", "1", "3", "5", "7"), t.getRows().get(0));
	}

	/**
	 * Every chart fixture Excel made: a value set, a category added and removed, a series added
	 * and removed; after each, the caches agree with the cells, and the workbook saves and loads.
	 * Refused only where Excel's way is unmeasured (a bubble chart's series), or where the cells
	 * the new series would take hold data of the sheet's own (the doughnut's).
	 */
	@Test
	public void everyFixture() throws Exception {
		for (String f : new String[] { "bar-chart.xlsx", "bar-chart-style-2.xlsx", "clustered-column.xlsx",
				"line-chart.xlsx", "pie-chart.xlsx", "stacked-column.xlsx", "waterfall.xlsx", "p2-3d-column.xlsx",
				"p2-area.xlsx", "p2-bar-of-pie.xlsx", "p2-bubble.xlsx", "p2-chartsheet.xlsx", "p2-combo-secondary.xlsx",
				"p2-date-axis.xlsx", "p2-doughnut.xlsx", "p2-filtered.xlsx", "p2-gaps.xlsx", "p2-multilevel.xlsx",
				"p2-radar.xlsx", "p2-scatter.xlsx", "p2-stock.xlsx", "p2-surface.xlsx" }) {
			SpreadsheetMLPackage pkg = load(f);
			Chart c = chart(pkg);
			ChartDataEditor e = ChartDataEditor.of(c);
			List<ChartSeries> s = e.getSeries();
			int n = s.size(), points = (s.get(0).getValues() != null ? s.get(0).getValues() : s.get(0).getYValues()).size();
			e.setValue(s.get(0), 0, 9.5);
			agree(f, c);
			ChartData cat = s.get(0).getCategories() != null ? s.get(0).getCategories() : s.get(0).getXValues();
			if (cat != null && cat.isNumeric()) e.addCategory(46100, 1.0, 2.0, 3.0);
			else e.addCategory("Added", 1.0, 2.0, 3.0);
			agree(f, c);
			assertEquals(f, points + 1, values(e.getSeries().get(0)).size());
			e.removeCategory(points);
			agree(f, c);
			assertEquals(f, points, values(e.getSeries().get(0)).size());
			try {
				ChartSeries added = e.addSeries("Added", 1.0, 2.0);
				assertEquals(f, n + 1, e.getSeries().size());
				agree(f, c);
				e.removeSeries(added);
				assertEquals(f, n, e.getSeries().size());
				agree(f, c);
			} catch (UnsupportedOperationException refused) {
				assertEquals("p2-bubble.xlsx", f);
			} catch (IllegalStateException refused) {
				// the doughnut plots column B only; Word's data in C and D isn't the chart's to overwrite
				assertEquals(refused.getMessage(), "p2-doughnut.xlsx", f);
				assertTrue(refused.getMessage(), refused.getMessage().contains("isn't empty"));
			}
			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
			pkg.save(os);
			SpreadsheetMLPackage again = (SpreadsheetMLPackage) OpcPackage.load(new java.io.ByteArrayInputStream(os.toByteArray()));
			agree(f, chart(again));
		}
	}

	private static ChartData values(ChartSeries s) {
		return s.getValues() != null ? s.getValues() : s.getYValues();
	}

	private static void agree(String fixture, Chart c) throws Exception {
		List<ChartCells.Difference> d = ChartCells.of(c).differences(ChartModel.read(c, null));
		assertEquals(fixture + ": " + d, 0, d.size());
	}
}
