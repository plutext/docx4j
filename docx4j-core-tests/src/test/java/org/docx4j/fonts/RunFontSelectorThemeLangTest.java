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
package org.docx4j.fonts;

import static org.docx4j.fonts.FontsTestSupport.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * The theme language.  An Estonian document (w:themeFontLang w:val="et-EE") whose
 * defaults name the theme's minor font gets that font's Latin face - Word: CR-016 probe
 * fonts-theme-lang, Carlito - not the theme's Ethiopic face (until 17.1.1 "et" matched
 * inside "eth"); and the document default font, which is such a theme reference in
 * most documents, is resolved with the theme language known (until 17.1.1 it was
 * computed before the language was read, and cached).
 */
public class RunFontSelectorThemeLangTest {

	private static final String SCRIPTS = "<a:font script=\"Ethi\" typeface=\"FontEthiopic\"/>"
			+ "<a:font script=\"Jpan\" typeface=\"FontJapanese\"/>";
	private static final String DEFAULTS = "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" "
			+ "w:eastAsiaTheme=\"minorEastAsia\" w:cstheme=\"minorBidi\"/><w:sz w:val=\"22\"/>";

	@Test
	public void estonianIsNotEthiopic() throws Exception {
		WordprocessingMLPackage pkg = packageWith(styles(DEFAULTS, null), settings("w:val=\"et-EE\""),
				fontScheme("FontMajor", "FontMinor", SCRIPTS),
				p(null, "Tere") + p("<w:lang w:val=\"et-EE\"/>", "Tere"),
				"FontMinor", SANS, "FontEthiopic", SERIF, "FontJapanese", SERIF);
		assertEquals(Arrays.asList(SANS), families(pkg, 0));
		assertEquals(Arrays.asList(SANS), families(pkg, 1));
		assertEquals("FontMinor", xslFoSelector(pkg).getDefaultFont());
	}

	@Test
	public void theDefaultFontKnowsTheThemeLanguage() throws Exception {
		// ja-JP as the Latin language selects the theme's Jpan face for minorHAnsi
		// (the documented mechanism: ECMA-376 17.15.1.88); the default font must agree
		// with what the runs get
		WordprocessingMLPackage pkg = packageWith(styles(DEFAULTS, null), settings("w:val=\"ja-JP\""),
				fontScheme("FontMajor", "FontMinor", SCRIPTS),
				p(null, "Hello"),
				"FontMinor", SANS, "FontJapanese", SERIF);
		assertEquals(Arrays.asList(SERIF), families(pkg, 0));
		assertEquals("FontJapanese", xslFoSelector(pkg).getDefaultFont());
	}
}
