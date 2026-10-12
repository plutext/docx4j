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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.dml.chart.CTDPt;
import org.docx4j.dml.chart.CTPieSer;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.junit.Test;

/**
 * The chart data editor on a chart in a docx or pptx (CR-036 phase 1), against Word's and
 * PowerPoint's own edits (CR-036's P5 probes, charts/authoring): each edit made by the editor on
 * Office's before-file, in try-with-resources, the document saved and loaded again, and compared
 * with Office's after-file as {@link ChartGate} compares them, the chart part (Office's random ids
 * aside: Word gives a pie's points fresh id suffixes) and the embedded workbook's cells and table.
 * With {@code -Ddocx4j.chartEdits.write=<directory>} the edited documents are written there, for
 * Word and PowerPoint to open (office-chart-edit-check.ps1).
 */
public class ChartDataEditorEmbeddedTest {

	interface Edit {
		void apply(ChartDataEditor e, List<ChartSeries> series) throws Exception;
	}

	private static void offices(String probe, String ext, Edit edit) throws Exception {
		offices(probe, ext, true, edit);
	}

	/**
	 * Office's edit made by the editor and compared; {@code extentHints} false where Office's save
	 * leaves the sheet's dimension and spans at their larger extent after cells are removed (Word's
	 * and PowerPoint's do, Excel's doesn't: CR-036 §11), which the editor narrows as Excel does
	 */
	private static void offices(String probe, String ext, boolean extentHints, Edit edit) throws Exception {
		OpcPackage ours = ChartGate.load("authoring/" + probe + "-before." + ext);
		OpcPackage office = ChartGate.load("authoring/" + probe + "-after." + ext);
		// Office's edit changed the chart, so that the comparison below isn't idle
		assertNotEquals(probe, ChartGate.chartXml(ChartGate.chart(office), true), ChartGate.chartXml(ChartGate.chart(ours), true));
		try (ChartDataEditor e = ChartDataEditor.of(ChartGate.chart(ours))) {
			edit.apply(e, e.getSeries());
		}
		OpcPackage saved = ChartGate.roundTrip(ours);
		write(saved, probe + "-docx4j." + ext);
		Chart c = ChartGate.chart(saved), o = ChartGate.chart(office);
		assertEquals(probe + ": the chart", ChartGate.chartXml(o, true), ChartGate.chartXml(c, true));
		SpreadsheetMLPackage wc = ChartGate.workbook(c), wo = ChartGate.workbook(o);
		assertEquals(probe + ": the cells", ChartGate.cells(wo, true, extentHints), ChartGate.cells(wc, true, extentHints));
		assertEquals(probe + ": the table", ChartGate.table(wo), ChartGate.table(wc));
		assertEquals(probe + ": calcId", calcId(wo), calcId(wc));
		assertEquals(probe + ": caches and cells", 0, ChartCells.of(c).differences(ChartModel.read(c, null)).size());
	}

	static void write(OpcPackage pkg, String name) throws Exception {
		String dir = System.getProperty("docx4j.chartEdits.write");
		if (dir != null) pkg.save(new File(dir, name));
	}

	private static Long calcId(SpreadsheetMLPackage wb) throws Exception {
		org.xlsx4j.sml.CTCalcPr c = wb.getWorkbookPart().getContents().getCalcPr();
		return c == null ? null : c.getCalcId();
	}

	// ------------------------------------------------------------------ Word's own edits

	@Test
	public void wordValue() throws Exception {
		offices("p5w-value", "docx", (e, s) -> e.setValue(s.get(1), 1, 7.5));
	}

	@Test
	public void wordAddCategory() throws Exception {
		offices("p5w-add-category", "docx", (e, s) -> e.addCategory("Category 5", 3.1, 2.2, 4.0));
	}

	@Test
	public void wordAddSeries() throws Exception {
		offices("p5w-add-series", "docx", (e, s) -> e.addSeries("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	@Test
	public void wordRemoveSeries() throws Exception {
		offices("p5w-remove-series", "docx", false, (e, s) -> e.removeSeries(s.get(2)));
	}

	/** A pie's new point formatted as its others, as Word adds it (c:dPt, accent5, id 9) */
	@Test
	public void wordPieAddCategory() throws Exception {
		offices("p5w-pie-add-category", "docx", (e, s) -> e.addCategory("5th Qtr", 2.6));
	}

	// ------------------------------------------------------------------ PowerPoint's own edits

	@Test
	public void powerPointValue() throws Exception {
		offices("p5p-value", "pptx", (e, s) -> e.setValue(s.get(1), 1, 7.5));
	}

	@Test
	public void powerPointAddCategory() throws Exception {
		offices("p5p-add-category", "pptx", (e, s) -> e.addCategory("Category 5", 3.1, 2.2, 4.0));
	}

	@Test
	public void powerPointAddSeries() throws Exception {
		offices("p5p-add-series", "pptx", (e, s) -> e.addSeries("Series 4", 1.5, 2.5, 3.5, 4.5));
	}

	@Test
	public void powerPointRemoveSeries() throws Exception {
		offices("p5p-remove-series", "pptx", false, (e, s) -> e.removeSeries(s.get(2)));
	}

	// ------------------------------------------------------------------ the live workbook and its controls

	/** The control (A5): opened and closed with no edit, the workbook's bytes are Word's own still */
	@Test
	public void noEditLeavesTheWorkbookAsItWas() throws Exception {
		OpcPackage pkg = ChartGate.load("authoring/p5w-value-before.docx");
		try (ChartDataEditor e = ChartDataEditor.of(ChartGate.chart(pkg))) {
			e.getSeries();
		}
		OpcPackage saved = ChartGate.roundTrip(pkg);
		write(saved, "control-no-edit-docx4j.docx");
		assertArrayEquals(entry("authoring/p5w-value-before.docx", "word/embeddings/Microsoft_Excel_Worksheet.xlsx"),
				((EmbeddedPackagePart) embeddedPart(ChartGate.chart(saved))).getBytes());
		assertEquals(ChartGate.chartXml(ChartGate.chart(ChartGate.load("authoring/p5w-value-before.docx")), false),
				ChartGate.chartXml(ChartGate.chart(saved), false));
	}

	/**
	 * The control's other half (A1, A5): the workbook rewritten by docx4j with no edit (flush) is
	 * Word's in every cell, table and chart; Word's opening it is office-chart-edit-check.ps1's
	 */
	@Test
	public void docx4jsWriteOfWordsWorkbook() throws Exception {
		OpcPackage pkg = ChartGate.load("authoring/p5w-value-before.docx");
		try (ChartDataEditor e = ChartDataEditor.of(ChartGate.chart(pkg))) {
			e.flush();
		}
		OpcPackage saved = ChartGate.roundTrip(pkg);
		write(saved, "control-flush-docx4j.docx");
		OpcPackage word = ChartGate.load("authoring/p5w-value-before.docx");
		SpreadsheetMLPackage ours = ChartGate.workbook(ChartGate.chart(saved)), words = ChartGate.workbook(ChartGate.chart(word));
		assertFalse("rewritten", java.util.Arrays.equals(entry("authoring/p5w-value-before.docx",
				"word/embeddings/Microsoft_Excel_Worksheet.xlsx"), ((EmbeddedPackagePart) embeddedPart(ChartGate.chart(saved))).getBytes()));
		assertEquals(ChartGate.cells(words, true), ChartGate.cells(ours, true));
		assertEquals(ChartGate.table(words), ChartGate.table(ours));
		assertEquals(ChartGate.chartXml(ChartGate.chart(word), false), ChartGate.chartXml(ChartGate.chart(saved), false));
	}

	/** A1: a document saved with its editor still open keeps the edit; readers see it meanwhile */
	@Test
	public void theLiveWorkbook() throws Exception {
		OpcPackage pkg = ChartGate.load("authoring/p5w-value-before.docx");
		Chart chart = ChartGate.chart(pkg);
		ChartDataEditor e = ChartDataEditor.of(chart);
		e.setValue(e.getSeries().get(1), 1, 7.5);
		// a reader meanwhile: the live workbook, not the bytes
		assertEquals("7.5", ChartCells.of(chart).cells("Sheet1!$C$3").get(0));
		// saved without close()
		OpcPackage saved = ChartGate.roundTrip(pkg);
		assertEquals("7.5", ChartCells.of(ChartGate.chart(saved)).cells("Sheet1!$C$3").get(0));
		assertEquals("7.5", ChartModel.read(ChartGate.chart(saved), null).getGroups().get(0).getSeries().get(1).getValues().getText(1));
		e.close();
		assertFalse(((EmbeddedPackagePart) embeddedPart(chart)).hasEmbeddedPackage());
		assertEquals("7.5", ChartCells.of(chart).cells("Sheet1!$C$3").get(0));
	}

	/** Refused: a linked chart (PowerPoint's pasted chart names an external workbook), a chart with no workbook */
	@Test
	public void refused() throws Exception {
		try {
			ChartDataEditor.of(ChartGate.chart(ChartGate.load("p2-pasted.pptx")));
			fail("edited a linked chart");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("linked"));
		}
		OpcPackage pkg = ChartGate.load("authoring/p5w-value-before.docx");
		ChartGate.chart(pkg).getContents().setExternalData(null);
		try {
			ChartDataEditor.of(ChartGate.chart(pkg));
			fail("edited a chart with no workbook");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("no workbook"));
		}
	}

	/** A pie's point removed: its c:dPt goes, the later points' are renumbered */
	@Test
	public void pieRemoveCategory() throws Exception {
		OpcPackage pkg = ChartGate.load("authoring/p5w-pie-add-category-after.docx");
		try (ChartDataEditor e = ChartDataEditor.of(ChartGate.chart(pkg))) {
			e.removeCategory(1);
		}
		CTPieSer ser = (CTPieSer) ChartModel.read(ChartGate.chart(pkg), null).getGroups().get(0).getSeries().get(0).getSource();
		List<CTDPt> dPts = ser.getDPt();
		assertEquals(4, dPts.size());
		for (int i = 0; i < 4; i++) assertEquals(i, dPts.get(i).getIdx().getVal());
		assertEquals(0, ChartCells.of(ChartGate.chart(pkg)).differences(ChartModel.read(ChartGate.chart(pkg), null)).size());
	}

	private static org.docx4j.openpackaging.parts.Part embeddedPart(Chart c) throws Exception {
		Relationship r = c.getRelationshipsPart().getRelationshipByID(c.getContents().getExternalData().getId());
		return c.getRelationshipsPart().getPart(r);
	}

	/** A zip entry of a fixture, as bytes */
	private static byte[] entry(String fixture, String name) throws Exception {
		try (InputStream is = ChartDataEditorEmbeddedTest.class.getClassLoader().getResourceAsStream("charts/" + fixture);
				ZipInputStream zis = new ZipInputStream(is)) {
			for (ZipEntry e; (e = zis.getNextEntry()) != null;) {
				if (!e.getName().equals(name)) continue;
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				for (int n; (n = zis.read(buf)) > 0;) os.write(buf, 0, n);
				return os.toByteArray();
			}
		}
		throw new IllegalArgumentException(name + " not in " + fixture);
	}
}
