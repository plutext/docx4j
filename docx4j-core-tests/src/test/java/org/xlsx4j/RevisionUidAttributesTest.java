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
package org.xlsx4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.ConnectionsPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.xlsx4j.sml.CTConnection;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.CTTableStyle;
import org.xlsx4j.sml.Workbook;

/**
 * CR-027 phase 1: Excel's revision uid attributes survive a round trip through
 * the typed model. Before it, only CT_Worksheet (and the x14/x15 slicer and
 * timeline roots) declared the attribute; on every other host - autoFilter,
 * hyperlink, table, the pivot definitions, comment, cellStyle, dataValidation
 * (xr), workbookView (xr2), tableColumn (xr3), tableStyle (xr9), connection
 * (xr16) - JAXB dropped it the moment the part was unmarshalled and saved
 * (loadAndSave.xlsx: nine uid in, one out). Found by
 * docx4j-generated-objects-ts's corpus run (CR-004 phase B); measured here the
 * same way, every part forced to unmarshal.
 *
 * The check is by element name over the saved parts' text, so it counts what
 * Excel will read rather than what the model holds; a few typed accessors are
 * read as well.
 */
public class RevisionUidAttributesTest {

	private static final String DATA_MODEL = "cr022-data-model.xlsx";   // xr, xr2, xr3, xr16
	private static final String STRICT_INVOICE = "anon/strict-invoice.xlsx"; // xr on hyperlink, cellStyle, dataValidation; xr9

	/** element local name + " " + attribute qname -> instances, over every .xml entry of the zip. */
	private static Map<String, Integer> uidsByHost(byte[] zip) throws Exception {
		Pattern p = Pattern.compile("<([A-Za-z0-9]+:)?([A-Za-z0-9]+) [^>]*?(xr[0-9]*:uid)=");
		Map<String, Integer> counts = new TreeMap<String, Integer>();
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
		ZipEntry e;
		while ((e = zis.getNextEntry()) != null) {
			if (!e.getName().endsWith(".xml")) continue;
			String xml = new String(zis.readAllBytes(), "UTF-8");
			Matcher m = p.matcher(xml);
			while (m.find()) {
				String key = m.group(2) + " " + m.group(3);
				counts.put(key, counts.getOrDefault(key, 0) + 1);
			}
		}
		return counts;
	}

	private static byte[] bytes(String resource) throws Exception {
		InputStream is = ResourceUtils.getResource(resource);
		try {
			return is.readAllBytes();
		} finally {
			is.close();
		}
	}

	/** Loads, forces every part to unmarshal, saves. */
	private static SpreadsheetMLPackage roundTrip(byte[] in, ByteArrayOutputStream out) throws Exception {
		SpreadsheetMLPackage pkg = (SpreadsheetMLPackage) SpreadsheetMLPackage.load(new ByteArrayInputStream(in));
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof JaxbXmlPart) {
				((JaxbXmlPart<?>) p).getContents();
			}
		}
		pkg.save(out);
		return pkg;
	}

	private static void assertUidsKept(String resource) throws Exception {
		byte[] in = bytes(resource);
		Map<String, Integer> before = uidsByHost(in);
		assertTrue(resource + " carries uid attributes", before.size() > 3);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		roundTrip(in, out);
		Map<String, Integer> after = uidsByHost(out.toByteArray());
		assertEquals(resource + ": uid attributes by host, before and after a forced round trip", before, after);
	}

	@Test
	public void dataModelWorkbookKeepsEveryUid() throws Exception {
		// autoFilter, pivotCacheDefinition, pivotTableDefinition, table, worksheet (xr);
		// workbookView (xr2); tableColumn (xr3); connection (xr16)
		assertUidsKept(DATA_MODEL);
	}

	@Test
	public void strictInvoiceKeepsEveryUid() throws Exception {
		// hyperlink, cellStyle, dataValidation x36, autoFilter, table, worksheet (xr);
		// workbookView (xr2); tableColumn (xr3); tableStyle (xr9). Strict in, transitional out.
		assertUidsKept(STRICT_INVOICE);
	}

	@Test
	public void typedAccessors() throws Exception {
		SpreadsheetMLPackage pkg = roundTrip(bytes(DATA_MODEL), new ByteArrayOutputStream());

		Workbook wb = pkg.getWorkbookPart().getContents();
		String bookViewUid = wb.getBookViews().getWorkbookView().get(0).getUid();
		assertNotNull("workbookView/@xr2:uid", bookViewUid);
		assertTrue(bookViewUid, bookViewUid.startsWith("{"));

		boolean table = false, connection = false;
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof TablePart) {
				CTTableColumn col = ((TablePart) p).getContents().getTableColumns().getTableColumn().get(0);
				assertNotNull("tableColumn/@xr3:uid", col.getUid());
				assertNotNull("table/@xr:uid", ((TablePart) p).getContents().getUid());
				table = true;
			} else if (p instanceof ConnectionsPart) {
				CTConnection c = ((ConnectionsPart) p).getContents().getConnection().get(0);
				assertNotNull("connection/@xr16:uid", c.getUid());
				connection = true;
			}
		}
		assertTrue("a table part", table);
		assertTrue("a connections part", connection);

		SpreadsheetMLPackage invoice = roundTrip(bytes(STRICT_INVOICE), new ByteArrayOutputStream());
		CTTableStyle style = invoice.getWorkbookPart().getStylesPart().getContents().getTableStyles().getTableStyle().get(0);
		assertNotNull("tableStyle/@xr9:uid", style.getUid());
	}
}
