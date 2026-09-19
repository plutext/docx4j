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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.ControlPropertiesPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.DataModelPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.SlicerCachePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.SlicersPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TimelineCachePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TimelinesPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSlicerCacheDefinition;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main.CTSlicers;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTTimelineCacheDefinition;
import org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2010.x11.main.CTTimelines;

/**
 * CR-022 phase 2: the Excel 2010 and 2013 extension parts are typed - slicer caches
 * and slicers, timeline caches and timelines, a control's properties, and the binary
 * data model - reachable through their relationship types, with their content types
 * and relationships kept on save.  They loaded as DefaultXmlPart (or a nameless
 * BinaryPart) before.
 *
 * On cr022-slicers-timelines.xlsx and cr022-checkbox.xlsx (committed, Excel-saved) and,
 * when -Dcr022.samples names a directory holding lo-tdf167689_x15_namespace.xlsx (a
 * LibreOffice test file with a data model, which docx4j cannot write), the data model.
 */
public class ExcelExtensionPartsTest {

	private static final String SLICERS = "cr022-slicers-timelines.xlsx";
	private static final String SAMPLES_PROPERTY = "cr022.samples";

	@Test
	public void slicerAndTimelinePartsTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource(SLICERS));
		assertSlicerAndTimelineParts(pkg);
	}

	private static void assertSlicerAndTimelineParts(SpreadsheetMLPackage pkg) throws Exception {

		// from the workbook: two slicer caches and a timeline cache
		RelationshipsPart wbRels = pkg.getWorkbookPart().getRelationshipsPart();
		int caches = 0;
		for (org.docx4j.relationships.Relationship r : wbRels.getRelationshipsByType(Namespaces.SPREADSHEETML_SLICER_CACHE)) {
			Part p = wbRels.getPart(r);
			assertTrue(p.getClass().getName(), p instanceof SlicerCachePart);
			CTSlicerCacheDefinition def = ((SlicerCachePart) p).getContents();
			assertNotNull(def.getName());
			assertNotNull(def.getSourceName());
			assertEquals("x xr10", def.getIgnorable());
			caches++;
		}
		assertEquals(2, caches);

		Part tc = wbRels.getPart(wbRels.getRelationshipByType(Namespaces.SPREADSHEETML_TIMELINE_CACHE));
		assertTrue(tc.getClass().getName(), tc instanceof TimelineCachePart);
		CTTimelineCacheDefinition tcd = ((TimelineCachePart) tc).getContents();
		assertNotNull(tcd.getName());
		assertNotNull(tcd.getSourceName());
		assertEquals("xr10", tcd.getIgnorable());

		// from sheet 2: the slicers and the timeline
		WorksheetPart sheet2 = pkg.getWorkbookPart().getWorksheet(1);
		RelationshipsPart wsRels = sheet2.getRelationshipsPart();
		Part sl = wsRels.getPart(wsRels.getRelationshipByType(Namespaces.SPREADSHEETML_SLICERS));
		assertTrue(sl.getClass().getName(), sl instanceof SlicersPart);
		CTSlicers slicers = ((SlicersPart) sl).getContents();
		assertTrue(slicers.getSlicer().size() >= 1);
		assertNotNull(slicers.getSlicer().get(0).getCache());
		assertEquals("x xr10", slicers.getIgnorable());

		Part tl = wsRels.getPart(wsRels.getRelationshipByType(Namespaces.SPREADSHEETML_TIMELINES));
		assertTrue(tl.getClass().getName(), tl instanceof TimelinesPart);
		CTTimelines timelines = ((TimelinesPart) tl).getContents();
		assertEquals(1, timelines.getTimeline().size());
		assertNotNull(timelines.getTimeline().get(0).getCache());

		// and by part name
		assertTrue(pkg.getParts().get(new PartName("/xl/slicers/slicer1.xml")) instanceof SlicersPart);
		assertTrue(pkg.getParts().get(new PartName("/xl/slicerCaches/slicerCache1.xml")) instanceof SlicerCachePart);
		assertTrue(pkg.getParts().get(new PartName("/xl/timelines/timeline1.xml")) instanceof TimelinesPart);
		assertTrue(pkg.getParts().get(new PartName("/xl/timelineCaches/timelineCache1.xml")) instanceof TimelineCachePart);
	}

	@Test
	public void partsSurviveASave() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource(SLICERS));
		byte[] saved = save(pkg);

		assertSlicerAndTimelineParts(SpreadsheetMLPackage.load(new ByteArrayInputStream(saved)));

		String types = entry(saved, "[Content_Types].xml");
		assertTrue(types, types.contains("ContentType=\"application/vnd.ms-excel.slicer+xml\" PartName=\"/xl/slicers/slicer1.xml\""));
		assertTrue(types, types.contains("ContentType=\"application/vnd.ms-excel.slicerCache+xml\" PartName=\"/xl/slicerCaches/slicerCache1.xml\""));
		assertTrue(types, types.contains("ContentType=\"application/vnd.ms-excel.timeline+xml\" PartName=\"/xl/timelines/timeline1.xml\""));
		assertTrue(types, types.contains("ContentType=\"application/vnd.ms-excel.timelineCache+xml\" PartName=\"/xl/timelineCaches/timelineCache1.xml\""));

		String wbRels = entry(saved, "xl/_rels/workbook.xml.rels");
		assertTrue(wbRels, wbRels.contains(Namespaces.SPREADSHEETML_TIMELINE_CACHE));
		assertEquals(2, count(wbRels, Namespaces.SPREADSHEETML_SLICER_CACHE));
		String wsRels = entry(saved, "xl/worksheets/_rels/sheet2.xml.rels");
		assertTrue(wsRels, wsRels.contains(Namespaces.SPREADSHEETML_SLICERS));
		assertTrue(wsRels, wsRels.contains(Namespaces.SPREADSHEETML_TIMELINES));

		// the marshalled roots declare what their mc:Ignorable names (x is the main namespace's prefix here)
		assertDeclares(saved, "xl/slicers/slicer1.xml", "x xr10");
		assertDeclares(saved, "xl/slicerCaches/slicerCache1.xml", "x xr10");
		assertDeclares(saved, "xl/timelines/timeline1.xml", "x xr10");
		assertDeclares(saved, "xl/timelineCaches/timelineCache1.xml", "xr10");
		String slicer = entry(saved, "xl/slicers/slicer1.xml");
		assertTrue(slicer, slicer.contains("<x14:slicers ") || slicer.contains("<slicers "));
	}

	@Test
	public void controlPropertiesPartTyped() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("cr022-checkbox.xlsx"));
		RelationshipsPart wsRels = pkg.getWorkbookPart().getWorksheet(0).getRelationshipsPart();
		Part p = wsRels.getPart(wsRels.getRelationshipByType(Namespaces.SPREADSHEETML_CONTROL_PROPERTIES));
		assertTrue(p.getClass().getName(), p instanceof ControlPropertiesPart);
		assertEquals("CheckBox", ((ControlPropertiesPart) p).getContents().getObjectType().value());

		byte[] saved = save(pkg);
		SpreadsheetMLPackage again = SpreadsheetMLPackage.load(new ByteArrayInputStream(saved));
		assertTrue(again.getParts().get(new PartName("/xl/ctrlProps/ctrlProp1.xml")) instanceof ControlPropertiesPart);
		String types = entry(saved, "[Content_Types].xml");
		assertTrue(types, types.contains("application/vnd.ms-excel.controlproperties+xml"));
	}

	@Test
	public void dataModelPartTyped() throws Exception {
		SpreadsheetMLPackage pkg = sample("lo-tdf167689_x15_namespace.xlsx");
		RelationshipsPart wbRels = pkg.getWorkbookPart().getRelationshipsPart();
		Part p = wbRels.getPart(wbRels.getRelationshipByType(Namespaces.SPREADSHEETML_DATA_MODEL));
		assertTrue(p.getClass().getName(), p instanceof DataModelPart);
		long size = ((DataModelPart) p).getBuffer().limit();
		assertTrue(size > 1000);

		SpreadsheetMLPackage again = SpreadsheetMLPackage.load(new ByteArrayInputStream(save(pkg)));
		DataModelPart reloaded = (DataModelPart) again.getParts().get(new PartName("/xl/model/item.data"));
		assertNotNull(reloaded);
		assertEquals(size, reloaded.getBuffer().limit());
	}

	// ---- helpers (as ExcelExtensionsTest)

	private static SpreadsheetMLPackage sample(String name) throws Exception {
		String dir = System.getProperty(SAMPLES_PROPERTY);
		assumeTrue("set -D" + SAMPLES_PROPERTY + "=<dir> to run on the temporary sample workbooks", dir != null);
		File f = new File(dir, name);
		assumeTrue(f.getPath() + " not present", f.exists());
		return SpreadsheetMLPackage.load(f);
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

	private static void assertDeclares(byte[] zip, String partName, String ignorable) throws Exception {
		String xml = entry(zip, partName);
		String head = xml.substring(0, Math.min(500, xml.length()));
		assertTrue(partName + ": " + head, xml.contains("mc:Ignorable=\"" + ignorable + "\""));
		for (String prefix : ignorable.split(" ")) {
			assertTrue(partName + " does not declare xmlns:" + prefix + ": " + head, xml.contains("xmlns:" + prefix + "=\""));
		}
	}

	private static int count(String s, String needle) {
		int n = 0, i = 0;
		while ((i = s.indexOf(needle, i)) >= 0) { n++; i += needle.length(); }
		return n;
	}
}
