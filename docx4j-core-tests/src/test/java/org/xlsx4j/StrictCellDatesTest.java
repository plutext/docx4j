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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.xlsx4j.jaxb.StrictCellDates;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

/**
 * A strict workbook's date cell (t="d", an ISO 8601 v) becomes the 1900-system
 * serial number Excel writes in a transitional workbook, with no t (2025-12-11
 * is 46002: 1970-01-01 is 25569, plus 20433 days).
 */
public class StrictCellDatesTest {

	@Test
	public void serials() {
		assertEquals("46002", StrictCellDates.serial("2025-12-11"));
		assertEquals("46002.4375", StrictCellDates.serial("2025-12-11T10:30:00"));
		assertEquals("46002.4375", StrictCellDates.serial("2025-12-11T10:30:00Z"));
		assertEquals("46002", StrictCellDates.serial("2025-12-11T00:00:00"));
		assertEquals("1", StrictCellDates.serial("1900-01-01"));   // the 1900 system
		assertEquals("59", StrictCellDates.serial("1900-02-28"));
		assertEquals("61", StrictCellDates.serial("1900-03-01"));  // after Lotus's 1900-02-29
		assertEquals("25569", StrictCellDates.serial("1970-01-01"));
		assertEquals("not a date", StrictCellDates.serial("not a date"));
		assertNull(StrictCellDates.serial(null));
	}

	@Test
	public void strictInvoiceDateCellLoadsAsASerial() throws Exception {
		SpreadsheetMLPackage pkg = (SpreadsheetMLPackage) OpcPackage.load(ResourceUtils.getResource("anon/strict-invoice.xlsx"));
		WorksheetPart sheet = (WorksheetPart) pkg.getParts().get(new PartName("/xl/worksheets/sheet1.xml"));
		Cell g4 = null;
		for (Row row : sheet.getContents().getSheetData().getRow()) for (Cell c : row.getC()) if ("G4".equals(c.getR())) g4 = c;
		assertEquals("no t: a number", org.xlsx4j.sml.STCellType.N, g4.getT()); // the getter's default
		assertEquals("46002", g4.getV());
		assertEquals("TODAY()", g4.getF().getValue());
		String xml = sheet.getXML();
		assertTrue(xml, !xml.contains("t=\"d\""));
	}

}
