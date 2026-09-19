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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;
import org.xlsx4j.sml.Workbook;

/**
 * CR-021: SpreadsheetML's schema admits mc:AlternateContent in the workbook (Excel
 * writes an x15 absPath Choice there, with no Fallback), and the load-time
 * preprocessor - reached for this workbook because of an unrelated xr:revisionPtr -
 * must keep it rather than drop it as an element with no Fallback.
 */
public class WorkbookAlternateContentKeptTest {

	@Test
	public void workbookElementSurvivesThePreprocessorAndRoundTrips() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(ResourceUtils.getResource("loadAndSave.xlsx"));
		Workbook wb = pkg.getWorkbookPart().getContents();
		AlternateContent ac = wb.getAlternateContent();
		assertNotNull("x15 absPath mc:AlternateContent kept in the workbook", ac);
		assertEquals(1, ac.getChoice().size());
		assertEquals("x15", ac.getChoice().get(0).getRequires());
		assertNull("Excel writes it with no Fallback", ac.getFallback());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		SpreadsheetMLPackage again = SpreadsheetMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
		AlternateContent reloaded = again.getWorkbookPart().getContents().getAlternateContent();
		assertNotNull("written back", reloaded);
		assertEquals("x15", reloaded.getChoice().get(0).getRequires());
	}
}
