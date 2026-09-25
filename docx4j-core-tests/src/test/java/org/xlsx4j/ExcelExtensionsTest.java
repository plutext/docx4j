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
package org.xlsx4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTConditionalFormattings;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTDataValidations;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSlicerCaches;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSlicerRefs;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSlicerStyles;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSparklineGroups;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTDataModel;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTTimelineCacheRefs;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTTimelineRefs;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTTimelineStyles;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2014.revision.CTRevisionPtr;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2018.calcfeatures.CTCalcFeature;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2018.calcfeatures.CTCalcFeatures;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTExtension;
import org.xlsx4j.sml.CTExtensionList;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;

/**
 * CR-022 phase 1: the Excel 2010 and 2013 extension schemas ([MS-XLSX] x14, x15,
 * x14ac, x15ac, x12ac, x16, xr, xr2, xr6, xr10) are bound, so
 *
 *  - extLst content Excel writes in those namespaces unmarshals typed (it was DOM);
 *  - x14ac:dyDescent and x14ac:knownFonts are properties of the row, the sheet
 *    format and the font table;
 *  - xr:revisionPtr is kept in the workbook (it was rejected and dropped);
 *  - mc:Ignorable is a property of every root Excel writes it on, so the prefixes
 *    it names are declared again on save;
 *  - the mc:AlternateContent Excel wraps a worksheet's controls in (Choice
 *    Requires="x14", no Fallback) is kept whole (CR-021) instead of dropped;
 *  - connections and queryTable parts, whose roots had no XmlRootElement, marshal.
 *
 * cr022-slicers-timelines.xlsx is Jason's Excel 365 workbook (a table, two slicers,
 * a timeline, two pivot tables, sparklines).  cr022-sparklines, -conditional-formatting,
 * -data-validation and -checkbox.xlsx were written by docx4j
 * (org.xlsx4j.samples.Excel2010ExtensionsSamples) and re-saved by Excel 365 (phase 2b).
 * cr022-data-model.xlsx is Jason's Excel 365 workbook over a docx4j-built source (a data
 * model, which docx4j cannot write: two Power Query tables and a worksheet table, one
 * relationship, a pivot over the model).
 */
public class ExcelExtensionsTest {

	private static final String SLICERS = "cr022-slicers-timelines.xlsx";

	// ---- the committed workbook

	@Test
	public void workbookExtensionsTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource(SLICERS));
		assertWorkbook(pkg);
	}

	private static void assertWorkbook(SpreadsheetMLPackage pkg) throws Exception {
		Workbook wb = pkg.getWorkbookPart().getContents();
		assertEquals("x15 xr xr6 xr10 xr2", wb.getIgnorable());

		CTRevisionPtr ptr = wb.getRevisionPtr();
		assertNotNull("xr:revisionPtr was rejected before CR-022", ptr);
		assertNotNull(ptr.getDocumentId());
		assertTrue(ptr.getCoauthVersionLast() > 0);
		assertNotNull(ptr.getUidLastSave());

		assertNotNull("x15ac absPath (CR-021)", wb.getAlternateContent());

		// x14:slicerCaches and x15:slicerCaches (the x15 element is of x14's type)
		int slicerCaches = 0;
		for (CTExtension e : wb.getExtLst().getExt()) {
			if (XmlUtils.unwrap(e.getAny()) instanceof CTSlicerCaches) {
				slicerCaches++;
				assertTrue(((CTSlicerCaches) XmlUtils.unwrap(e.getAny())).getSlicerCache().size() >= 1);
			}
		}
		assertEquals(2, slicerCaches);
		CTTimelineCacheRefs timelines = ext(wb.getExtLst(), CTTimelineCacheRefs.class);
		assertEquals(1, timelines.getTimelineCacheRef().size());
		assertNotNull(timelines.getTimelineCacheRef().get(0).getId());
		ext(wb.getExtLst(), org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTWorkbookPr.class);
	}

	@Test
	public void worksheetExtensionsTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource(SLICERS));
		assertWorksheets(pkg);
	}

	private static void assertWorksheets(SpreadsheetMLPackage pkg) throws Exception {
		// sheet 2 carries the slicer and timeline references, sheet 3 the sparklines
		Worksheet sheet2 = pkg.getWorkbookPart().getWorksheet(1).getContents();
		assertEquals("x14ac xr xr2 xr3", sheet2.getIgnorable());
		assertNotNull("x14ac:dyDescent on sheetFormatPr", sheet2.getSheetFormatPr().getDyDescent());
		boolean rowHasIt = false;
		for (Row r : sheet2.getSheetData().getRow()) {
			if (r.getDyDescent() != null) rowHasIt = true;
		}
		assertTrue("x14ac:dyDescent on a row", rowHasIt);
		CTSlicerRefs slicers = ext(sheet2.getExtLst(), CTSlicerRefs.class);
		assertTrue(slicers.getSlicer().size() >= 1);
		CTTimelineRefs timelines = ext(sheet2.getExtLst(), CTTimelineRefs.class);
		assertEquals(1, timelines.getTimelineRef().size());

		Worksheet sheet3 = pkg.getWorkbookPart().getWorksheet(2).getContents();
		CTSparklineGroups sparklines = ext(sheet3.getExtLst(), CTSparklineGroups.class);
		assertTrue(sparklines.getSparklineGroup().size() >= 1);
		assertTrue(sparklines.getSparklineGroup().get(0).getSparklines().getSparkline().size() >= 1);
	}

	@Test
	public void stylesAndTableExtensionsTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource(SLICERS));
		assertStylesAndTable(pkg);
	}

	private static void assertStylesAndTable(SpreadsheetMLPackage pkg) throws Exception {
		CTStylesheet styles = pkg.getWorkbookPart().getStylesPart().getContents();
		assertEquals("x14ac x16r2 xr", styles.getIgnorable());
		assertEquals(Boolean.TRUE, styles.getFonts().isKnownFonts());
		assertNotNull(ext(styles.getExtLst(), CTSlicerStyles.class).getDefaultSlicerStyle());
		assertNotNull(ext(styles.getExtLst(), CTTimelineStyles.class).getDefaultTimelineStyle());

		TablePart table = null;
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof TablePart) table = (TablePart) p;
		}
		assertNotNull(table);
		assertEquals("xr xr3", table.getContents().getIgnorable());
	}

	/**
	 * CR-027 phase 2: xcalcf:calcFeatures, which Excel writes in every workbook's extLst
	 * (ext uri {B58B0392-4F1F-4190-BB64-5DF3571DCE5F}), is typed rather than DOM, and a
	 * save writes it with Excel's prefix.
	 */
	@Test
	public void calcFeaturesTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("loadAndSave.xlsx"));
		CTCalcFeatures features = ext(pkg.getWorkbookPart().getContents().getExtLst(), CTCalcFeatures.class);
		assertTrue(features.getFeature().size() >= 3);
		boolean rd = false;
		for (CTCalcFeature f : features.getFeature()) {
			if ("microsoft.com:RD".equals(f.getName())) rd = true;
		}
		assertTrue("microsoft.com:RD among the features", rd);

		String workbook = entry(save(pkg), "xl/workbook.xml");
		assertTrue(workbook, workbook.contains("<xcalcf:calcFeatures>"));
		assertTrue(workbook, workbook.contains("<xcalcf:feature name=\"microsoft.com:RD\"/>"));
	}

	@Test
	public void roundTripKeepsExtensionsAndDeclaresIgnorablePrefixes() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource(SLICERS));
		byte[] saved = save(pkg);

		SpreadsheetMLPackage again = SpreadsheetMLPackage.load(new ByteArrayInputStream(saved));
		assertWorkbook(again);
		assertWorksheets(again);
		assertStylesAndTable(again);

		// every prefix an mc:Ignorable names is declared on the part that names it
		assertDeclares(saved, "xl/workbook.xml", "x15 xr xr6 xr10 xr2");
		assertDeclares(saved, "xl/worksheets/sheet2.xml", "x14ac xr xr2 xr3");
		assertDeclares(saved, "xl/styles.xml", "x14ac x16r2 xr");
		assertDeclares(saved, "xl/tables/table1.xml", "xr xr3");
		assertDeclares(saved, "xl/pivotTables/pivotTable1.xml", "xr");
		assertDeclares(saved, "xl/pivotCache/pivotCacheDefinition1.xml", "xr");
		assertDeclares(saved, "xl/pivotCache/pivotCacheRecords1.xml", "xr");

		String workbook = entry(saved, "xl/workbook.xml");
		assertTrue(workbook, workbook.contains("<xr:revisionPtr "));
		assertTrue(workbook, workbook.contains("xr6:coauthVersionLast="));
		assertTrue(workbook, workbook.contains("xr10:uidLastSave="));
		String sheet = entry(saved, "xl/worksheets/sheet2.xml");
		assertTrue(sheet, sheet.contains("x14ac:dyDescent="));
		assertTrue(sheet, sheet.contains("<x14:slicerList>") || sheet.contains("<x14:slicerList "));
		assertTrue(sheet, sheet.contains("<x15:timelineRefs"));
		String styles = entry(saved, "xl/styles.xml");
		assertTrue(styles, styles.contains("x14ac:knownFonts=\"1\"") || styles.contains("x14ac:knownFonts=\"true\""));
	}

	@Test
	public void x12acAndX16MarshalWithExcelsPrefixes() throws Exception {
		// no sample workbook carries them; the prefix table and the context are checked directly
		String list = XmlUtils.marshaltoString(
				new org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2011.x1.ac.ObjectFactory().createList("a,b"),
				true, false, Context.jcSML);
		assertTrue(list, list.contains("<x12ac:list"));
		assertTrue(list, list.contains("xmlns:x12ac=\"http://schemas.microsoft.com/office/spreadsheetml/2011/1/ac\""));

		org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2014.x11.main.ObjectFactory x16 =
				new org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2014.x11.main.ObjectFactory();
		String groupings = XmlUtils.marshaltoString(x16.createModelTimeGroupings(x16.createCTModelTimeGroupings()),
				true, false, Context.jcSML);
		assertTrue(groupings, groupings.contains("<x16:modelTimeGroupings"));
		assertTrue(groupings, groupings.contains("xmlns:x16=\"http://schemas.microsoft.com/office/spreadsheetml/2014/11/main\""));
	}

	@Test
	public void controlAnchorMarshalsAsExcelWritesIt() throws Exception {
		// ECMA-376 says xdr:from / xdr:to; Excel writes from / to in the main namespace and repairs a
		// worksheet with the xdr form (found on the generated check box, CR-022 phase 2b)
		org.xlsx4j.sml.CTObjectAnchor anchor = Context.getsmlObjectFactory().createCTObjectAnchor();
		org.docx4j.dml.spreadsheetdrawing.CTMarker from = new org.docx4j.dml.spreadsheetdrawing.CTMarker();
		from.setCol(1); from.setColOff(0); from.setRow(3); from.setRowOff(0);
		anchor.setFrom(from);
		anchor.setTo(from);
		String xml = XmlUtils.marshaltoString(new jakarta.xml.bind.JAXBElement<org.xlsx4j.sml.CTObjectAnchor>(
				new javax.xml.namespace.QName("http://schemas.openxmlformats.org/spreadsheetml/2006/main", "anchor"),
				org.xlsx4j.sml.CTObjectAnchor.class, anchor), true, false, Context.jcSML);
		assertTrue(xml, xml.contains("<from><xdr:col>"));
		assertTrue(xml, xml.contains("<to><xdr:col>"));
		assertFalse(xml, xml.contains("xdr:from"));
	}

	// ---- docx4j's own sample workbooks (Excel2010ExtensionsSamples, each re-saved by Excel 365)

	@Test
	public void sparklinesTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("cr022-sparklines.xlsx"));
		Worksheet sheet = pkg.getWorkbookPart().getWorksheet(0).getContents();
		int groups = ext(sheet.getExtLst(), CTSparklineGroups.class).getSparklineGroup().size();
		assertTrue(groups >= 1);
		SpreadsheetMLPackage again = SpreadsheetMLPackage.load(new ByteArrayInputStream(save(pkg)));
		Worksheet reloaded = again.getWorkbookPart().getWorksheet(0).getContents();
		assertEquals(groups, ext(reloaded.getExtLst(), CTSparklineGroups.class).getSparklineGroup().size());
	}

	@Test
	public void conditionalFormattingsTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("cr022-conditional-formatting.xlsx"));
		Worksheet sheet = pkg.getWorkbookPart().getWorksheet(0).getContents();
		CTConditionalFormattings cf = ext(sheet.getExtLst(), CTConditionalFormattings.class);
		assertEquals("the data bar's x14 twin and the icon set", 2, cf.getConditionalFormatting().size());
		assertNotNull(cf.getConditionalFormatting().get(0).getCfRule().get(0).getDataBar());
		assertEquals("3Triangles", cf.getConditionalFormatting().get(1).getCfRule().get(0).getIconSet().getIconSet());
		// and the x14:id the main-schema cfRule carries in its own extLst
		String id = ext(sheet.getConditionalFormatting().get(0).getCfRule().get(0).getExtLst(), String.class);
		assertTrue(id, id.startsWith("{"));
	}

	@Test
	public void dataValidationsTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("cr022-data-validation.xlsx"));
		Worksheet sheet = pkg.getWorkbookPart().getWorksheet(0).getContents();
		CTDataValidations dv = ext(sheet.getExtLst(), CTDataValidations.class);
		assertTrue(dv.getDataValidation().size() >= 1);
		assertNotNull(dv.getDataValidation().get(0).getSqref());
	}

	@Test
	public void formControlAlternateContentKept() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("cr022-checkbox.xlsx"));
		Worksheet sheet = pkg.getWorkbookPart().getWorksheet(0).getContents();
		assertEquals("the controls' mc:AlternateContent was dropped before CR-022 (no Fallback)",
				1, sheet.getAlternateContent().size());
		AlternateContent ac = sheet.getAlternateContent().get(0);
		assertEquals("x14", ac.getChoice().get(0).getRequires());
		assertNull(ac.getFallback());

		byte[] saved = save(pkg);
		String xml = entry(saved, "xl/worksheets/sheet1.xml");
		assertEquals(xml, 2, count(xml, "<mc:AlternateContent"));  // the worksheet's and the controls'
		assertTrue(xml, xml.contains("<controlPr") || xml.contains(":controlPr"));
		assertEquals(1, SpreadsheetMLPackage.load(new ByteArrayInputStream(saved))
				.getWorkbookPart().getWorksheet(0).getContents().getAlternateContent().size());
	}

	@Test
	public void dataModelWorkbookTypedAndSavesItsConnections() throws Exception {
		// Jason's Excel 365 workbook: two Power Query tables and a worksheet table in the data model,
		// related Sales.Region -> Regions.Region, and a pivot over the model
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("cr022-data-model.xlsx"));
		Workbook wb = pkg.getWorkbookPart().getContents();
		CTDataModel model = ext(wb.getExtLst(), CTDataModel.class);
		// the two Power Query loads, the worksheet connection the pivot made, and the related Regions
		assertTrue(model.getModelTables().getModelTable().size() >= 3);
		boolean sales = false;
		for (org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTModelTable t : model.getModelTables().getModelTable()) {
			if ("Sales".equals(t.getName())) sales = true;
		}
		assertTrue("a model table named Sales", sales);
		assertEquals(1, model.getModelRelationships().getModelRelationship().size());
		assertEquals("Region", model.getModelRelationships().getModelRelationship().get(0).getFromColumn());

		byte[] saved = save(pkg);  // CT_Connections had no XmlRootElement: the save failed
		String connections = entry(saved, "xl/connections.xml");
		assertTrue(connections, connections.contains("<x15:connection"));
		assertTrue(connections, connections.contains("name=\"ThisWorkbookDataModel\""));
		assertDeclares(saved, "xl/connections.xml", "xr16");
		assertNotNull(ext(SpreadsheetMLPackage.load(new ByteArrayInputStream(saved)).getWorkbookPart().getContents().getExtLst(),
				CTDataModel.class).getModelRelationships());
	}

	@Test
	public void connectionsAndQueryTableRootsMarshal() throws Exception {
		// CT_Connections and CT_QueryTable had no root-element annotation, so ConnectionsPart and
		// QueryTablePart could load but not save (no fixture carries a query table any more)
		org.xlsx4j.sml.ObjectFactory f = Context.getsmlObjectFactory();
		org.xlsx4j.sml.CTConnections connections = f.createCTConnections();
		org.xlsx4j.sml.CTConnection c = f.createCTConnection();
		c.setId(1L);
		c.setName("Query - Sales");
		connections.getConnection().add(c);
		connections.setIgnorable("xr16");
		String xml = XmlUtils.marshaltoString(connections, true, false, Context.jcSML);
		assertTrue(xml, xml.contains("<connections ") && xml.contains("mc:Ignorable=\"xr16\""));
		org.xlsx4j.sml.CTQueryTable qt = f.createCTQueryTable();
		qt.setName("Sales");
		qt.setConnectionId(1L);
		xml = XmlUtils.marshaltoString(qt, true, false, Context.jcSML);
		assertTrue(xml, xml.contains("<queryTable "));
	}

	// ---- helpers

	/** The ext content of the given type, unwrapped; fails if the list has none. */
	private static <T> T ext(CTExtensionList extLst, Class<T> type) {
		assertNotNull("no extLst", extLst);
		for (CTExtension ext : extLst.getExt()) {
			Object o = XmlUtils.unwrap(ext.getAny());
			if (type.isInstance(o)) return type.cast(o);
		}
		StringBuilder found = new StringBuilder();
		for (CTExtension ext : extLst.getExt()) {
			found.append(ext.getUri()).append(" -> ").append(XmlUtils.unwrap(ext.getAny()).getClass().getName()).append("; ");
		}
		fail("no " + type.getSimpleName() + " in extLst; found " + found);
		return null;
	}

	private static byte[] save(SpreadsheetMLPackage pkg) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return bos.toByteArray();
	}

	private static String entry(byte[] zip, String name) throws Exception {
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().equals(name)) {
					return new String(readAll(zis), StandardCharsets.UTF_8);
				}
			}
		}
		fail("no " + name + " in the saved package");
		return null;
	}

	private static byte[] readAll(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int n;
		while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
		return bos.toByteArray();
	}

	/** The part names each prefix in an mc:Ignorable and declares each of them. */
	private static void assertDeclares(byte[] zip, String partName, String ignorable) throws Exception {
		String xml = entry(zip, partName);
		assertTrue(partName + ": " + xml.substring(0, Math.min(400, xml.length())),
				xml.contains("mc:Ignorable=\"" + ignorable + "\""));
		for (String prefix : ignorable.split(" ")) {
			assertTrue(partName + " does not declare xmlns:" + prefix + ": " + xml.substring(0, Math.min(400, xml.length())),
					xml.contains("xmlns:" + prefix + "=\""));
		}
	}

	private static int count(String s, String needle) {
		int n = 0, i = 0;
		while ((i = s.indexOf(needle, i)) >= 0) { n++; i += needle.length(); }
		return n;
	}
}
