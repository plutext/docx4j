/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.anon.AnonymizeResult.Action;
import org.docx4j.anon.AnonymizeResult.PartAction;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.junit.Test;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

/**
 * The anonymiser on docx4j's own workbooks (see src/test/resources/anon/README.md):
 * STRICT leaves every one clean and verified, the output reloads, and the
 * things each workbook is here for are gone.
 */
public class AnonymizeXlsxCorpusTest {

	static final String[] CORPUS = {
			"loadAndSave.xlsx",
			"cr022-checkbox.xlsx",
			"cr022-conditional-formatting.xlsx",
			"cr022-data-model.xlsx",
			"cr022-data-validation.xlsx",
			"cr022-slicers-timelines.xlsx",
			"cr022-sparklines.xlsx",
			"anon/pivot.xlsm",
			"anon/comments.xlsx",
			"anon/strict-chart.xlsx",
			"anon/strict-invoice.xlsx",
			"anon/strict-simple.xlsx" };

	static SpreadsheetMLPackage load(String name) throws Exception {
		InputStream is = AnonymizeXlsxCorpusTest.class.getResourceAsStream("/" + name);
		assertNotNull("resource " + name, is);
		return (SpreadsheetMLPackage) OpcPackage.load(is);
	}

	static SpreadsheetMLPackage reload(SpreadsheetMLPackage pkg) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		return (SpreadsheetMLPackage) OpcPackage.load(new ByteArrayInputStream(baos.toByteArray()));
	}

	static Part part(OpcPackage pkg, String name) throws Exception {
		return pkg.getParts().get(new PartName(name));
	}

	static PartAction action(AnonymizeResult r, String partName) {
		return AnonymizeCorpusTest.action(r, partName);
	}

	static String cellValue(WorksheetPart ws, String ref) throws Exception {
		for (Row r : ws.getContents().getSheetData().getRow()) {
			for (Cell c : r.getC()) if (ref.equals(c.getR())) return c.getV();
		}
		return null;
	}

	@Test
	public void strictIsCleanVerifiedAndReloads() throws Exception {
		List<String> problems = new ArrayList<String>();
		for (String name : CORPUS) {
			SpreadsheetMLPackage pkg = load(name);
			AnonymizeResult r;
			try {
				r = new Anonymize(pkg).go();
			} catch (Exception e) {
				throw new AssertionError(name + ": " + e, e);
			}
			if (!r.isClean()) {
				problems.add(name + ": not clean\n" + r.summary());
			}
			if (!Boolean.TRUE.equals(r.getVerified())) {
				problems.add(name + ": not verified: " + r.getLeaks());
			}
			SpreadsheetMLPackage again = reload(pkg);
			assertNotNull(again.getWorkbookPart().getContents());
			assertFalse(name + ": no sheets after reload", again.getWorkbookPart().getContents().getSheets().getSheet().isEmpty());
		}
		assertTrue(String.join("\n", problems), problems.isEmpty());
	}

	@Test
	public void threadedCommentsTableVmlAndChart() throws Exception {
		// loadAndSave.xlsx: a threaded comment with its person (email user id), a legacy comment
		// with its VML shape (an <xml> root the binding cannot read), a table, a chart, an SVG,
		// a sensitivity label, headers and footers
		SpreadsheetMLPackage pkg = load("loadAndSave.xlsx");
		String originalPersons = XmlUtils.w3CDomNodeToString(((XmlPart) part(pkg, "/xl/persons/person.xml")).getDocument());
		assertTrue(originalPersons, originalPersons.contains("userId=\"S::"));

		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());

		String persons = XmlUtils.w3CDomNodeToString(((XmlPart) part(pkg, "/xl/persons/person.xml")).getDocument());
		assertTrue(persons, persons.matches("(?s).*displayName=\"Author [12]\".*"));
		assertTrue(persons, persons.contains("providerId=\"None\""));
		assertFalse(persons, persons.contains("@"));
		assertEquals(Action.SCRUBBED, action(r, "/xl/threadedComments/threadedComment1.xml").action);
		assertEquals(Action.SCRUBBED, action(r, "/xl/comments1.xml").action);
		PartAction vml = action(r, "/xl/drawings/vmlDrawing1.vml");
		assertEquals(Action.SCRUBBED, vml.action);
		assertTrue(vml.reason, vml.reason.contains("DOM"));
		assertTrue("the VML part is still there", part(pkg, "/xl/drawings/vmlDrawing1.vml") instanceof XmlPart);

		// the table's columns equal its header cells
		TablePart table = (TablePart) part(pkg, "/xl/tables/table1.xml");
		WorksheetPart sheet = (WorksheetPart) part(pkg, "/xl/worksheets/sheet1.xml");
		String ref = table.getContents().getRef(); // A31:C33
		int row = Integer.parseInt(ref.substring(1, ref.indexOf(':')));
		List<String> strings = new ArrayList<String>();
		for (org.xlsx4j.sml.CTRst si : pkg.getWorkbookPart().getSharedStrings().getContents().getSi()) {
			StringBuilder sb = new StringBuilder();
			if (si.getT() != null) sb.append(si.getT().getValue());
			for (org.xlsx4j.sml.CTRElt rr : si.getR()) sb.append(rr.getT().getValue());
			strings.add(sb.toString());
		}
		String[] cols = { "A", "B", "C" };
		for (int i = 0; i < 3; i++) {
			String v = cellValue(sheet, cols[i] + row);
			assertEquals(table.getContents().getTableColumns().getTableColumn().get(i).getName(), strings.get(Integer.parseInt(v)));
		}
		assertFalse(table.getContents().getTableColumns().getTableColumn().get(0).getName().equals("Col1"));

		// the chart reads the sheet by its new name
		String chartXml = ((org.docx4j.openpackaging.parts.DrawingML.Chart) part(pkg, "/xl/charts/chart1.xml")).getXML();
		assertTrue(chartXml, chartXml.contains("<c:f>Sheet1!$A$2:$A$4</c:f>"));
		assertEquals("Sheet1", pkg.getWorkbookPart().getContents().getSheets().getSheet().get(0).getName());
		assertNull(part(pkg, "/docMetadata/LabelInfo.xml"));
		assertEquals(Action.REPLACED, action(r, "/xl/media/image2.svg").action);
		assertNull(pkg.getDocPropsExtendedPart().getContents().getTitlesOfParts());
		reload(pkg);
	}

	@Test
	public void pivotsSlicersTimelinesAndTheirReferencesGo() throws Exception {
		SpreadsheetMLPackage pkg = load("cr022-slicers-timelines.xlsx");
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		for (String gone : new String[] { "/xl/pivotTables/pivotTable1.xml", "/xl/pivotCache/pivotCacheDefinition1.xml",
				"/xl/pivotCache/pivotCacheRecords1.xml", "/xl/slicers/slicer1.xml", "/xl/slicerCaches/slicerCache1.xml",
				"/xl/timelines/timeline1.xml", "/xl/timelineCaches/timelineCache1.xml" }) {
			assertNull(gone, part(pkg, gone));
		}
		String workbook = pkg.getWorkbookPart().getXML();
		assertFalse(workbook, workbook.contains("pivotCaches"));
		assertFalse(workbook, workbook.contains("slicerCaches"));
		assertFalse(workbook, workbook.contains("timelineCacheRefs"));
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof WorksheetPart) {
				String xml = ((WorksheetPart) p).getXML();
				assertFalse(p.getPartName() + ": " + xml, xml.contains("slicerList") || xml.contains("timelineRefs"));
			}
			if (p instanceof org.docx4j.openpackaging.parts.DrawingML.Drawing) {
				String xml = ((org.docx4j.openpackaging.parts.DrawingML.Drawing) p).getXML();
				assertFalse(p.getPartName() + " still draws a slicer", xml.contains("/slicer") || xml.contains("timeslicer"));
			}
		}
		reload(pkg);
	}

	@Test
	public void pivotChartBecomesAPlainChart() throws Exception {
		SpreadsheetMLPackage pkg = load("anon/pivot.xlsm");
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		String chartXml = ((org.docx4j.openpackaging.parts.DrawingML.Chart) part(pkg, "/xl/charts/chart1.xml")).getXML();
		assertFalse(chartXml, chartXml.contains("pivotSource"));
		assertTrue(chartXml, chartXml.contains("numCache"));
		assertNull(part(pkg, "/xl/pivotTables/pivotTable1.xml"));
		reload(pkg);
	}

	@Test
	public void connectionsAndTheDataModel() throws Exception {
		SpreadsheetMLPackage pkg = load("cr022-data-model.xlsx");
		String original = ((org.docx4j.openpackaging.parts.SpreadsheetML.ConnectionsPart) part(pkg, "/xl/connections.xml")).getXML();
		AnonymizeResult r = new Anonymize(pkg).go();
		assertTrue(r.summary(), r.isClean());
		assertNull(part(pkg, "/xl/model/item.data"));
		String connections = ((org.docx4j.openpackaging.parts.SpreadsheetML.ConnectionsPart) part(pkg, "/xl/connections.xml")).getXML();
		assertFalse(connections.equals(original));
		assertTrue(connections, connections.contains("connection=\"Provider=None\"") || !original.contains("Provider="));
		String workbook = pkg.getWorkbookPart().getXML();
		assertFalse(workbook, workbook.contains("dataModel"));
		reload(pkg);
	}

	@Test
	public void numbersKeptOnRequest() throws Exception {
		SpreadsheetMLPackage pkg = load("cr022-sparklines.xlsx");
		WorksheetPart sheet = (WorksheetPart) part(pkg, "/xl/worksheets/sheet1.xml");
		List<String> before = new ArrayList<String>();
		for (Row row : sheet.getContents().getSheetData().getRow()) {
			for (Cell c : row.getC()) if (c.getT() == null || c.getT() == org.xlsx4j.sml.STCellType.N) before.add(c.getV());
		}
		assertFalse(before.isEmpty());
		Anonymize anon = new Anonymize(pkg);
		anon.setKeepNumbers(true);
		AnonymizeResult r = anon.go();
		assertTrue(r.summary(), r.isClean());
		List<String> after = new ArrayList<String>();
		for (Row row : sheet.getContents().getSheetData().getRow()) {
			for (Cell c : row.getC()) if (c.getT() == null || c.getT() == org.xlsx4j.sml.STCellType.N) after.add(c.getV());
		}
		assertEquals(before, after);
	}

}
