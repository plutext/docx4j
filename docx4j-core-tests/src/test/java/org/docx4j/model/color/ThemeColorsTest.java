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
package org.docx4j.model.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.junit.Test;

/**
 * Which theme and colour map {@link ThemeColors} takes for a host (CR-034 §2.1).  The probes
 * themselves are {@link ThemeColourProbesTest}'s.
 */
public class ThemeColorsTest {

	@Test
	public void newDocxHasTheOffice2023Theme() throws Exception {
		ThemeColors t = ThemeColors.of(WordprocessingMLPackage.createPackage());
		assertEquals("156082", t.schemeColor("accent1").toHex());
		assertEquals("000000", t.wordThemeColor("text1").toHex());
		assertEquals("FFFFFF", t.schemeColor("bg1").toHex());
		assertNull(t.wordThemeColor("none"));
	}

	@Test
	public void noThemePartTakesTheDefaultTheme() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Part theme = pkg.getMainDocumentPart().getThemePart();
		pkg.getMainDocumentPart().getRelationshipsPart().removePart(theme.getPartName());
		pkg.getMainDocumentPart().setPartShortcut(null, org.docx4j.openpackaging.parts.relationships.Namespaces.THEME);
		assertNull(pkg.getMainDocumentPart().getThemePart());
		assertEquals("156082", ThemeColors.of(pkg).schemeColor("accent1").toHex());

		// docx4j.fonts.defaultTheme chooses it; the 2007 setting has the 2013 colours (CR-034 D2)
		String was = Docx4jProperties.getProperty(Docx4jProperties.DEFAULT_THEME, "2023");
		try {
			Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, "2013");
			assertEquals("4472C4", ThemeColors.of(pkg).schemeColor("accent1").toHex());
		} finally {
			Docx4jProperties.setProperty(Docx4jProperties.DEFAULT_THEME, was);
		}
	}

	@Test
	public void sheetThemeIndexesSwapTheFirstPairs() throws Exception {
		ThemeColors t = ThemeColors.of((org.docx4j.dml.Theme) null);
		assertEquals("FFFFFF", t.sheetThemeColor(0).toHex());
		assertEquals("000000", t.sheetThemeColor(1).toHex());
		assertEquals("E8E8E8", t.sheetThemeColor(2).toHex());
		assertEquals("0E2841", t.sheetThemeColor(3).toHex());
		assertEquals("156082", t.sheetThemeColor(4).toHex());
	}

	/** A chart with no override of its own is drawn in its host's theme (charts plan, finding 7) */
	@Test
	public void chartTakesItsHostsTheme() throws Exception {
		WordprocessingMLPackage pkg;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("charts/bar-chart.docx")) {
			pkg = WordprocessingMLPackage.load(is);
		}
		Chart chart = null;
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof Chart) chart = (Chart) p;
		}
		ThemeColors host = ThemeColors.of(pkg);
		// Word's chart: the docx's 2013-2022 theme, accent1 4472C4, not the embedded workbook's
		assertEquals("4472C4", host.forChart(chart).schemeColor("accent1").toHex());
		assertEquals(host.schemeColor("tx1"), host.forChart(chart).schemeColor("tx1"));
	}
}
