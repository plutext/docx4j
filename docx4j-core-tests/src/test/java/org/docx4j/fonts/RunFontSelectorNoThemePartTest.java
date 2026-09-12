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
 * A theme reference in a document with no theme part resolves to the Office theme's
 * face, at the run level as for the document default: Word supplies that theme to such
 * a document (CR-016 probe fonts-missing-slots (b): Calibri for minorHAnsi, with the
 * explicit w:ascii beside the reference unused; measured on Word 365, whose own new-
 * document theme is Aptos).  Until 17.1.1 the run fell to the document default font.
 */
public class RunFontSelectorNoThemePartTest {

	@Test
	public void officeThemeFacesStandIn() throws Exception {
		WordprocessingMLPackage pkg = packageWith(
				styles("<w:sz w:val=\"22\"/>", null), null, null,
				p("<w:rFonts w:asciiTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" w:ascii=\"FontA\" w:hAnsi=\"FontA\"/>", "Hello")
				+ p("<w:rFonts w:asciiTheme=\"majorHAnsi\" w:hAnsiTheme=\"majorHAnsi\"/>", "Hello")
				+ p("<w:rFonts w:ascii=\"FontA\" w:hAnsi=\"FontA\"/>", "Hello"),
				"Calibri", SANS, "Cambria", SERIF, "FontA", SERIF, "Times New Roman", SERIF);
		assertEquals("minorHAnsi, no theme part: Calibri, not the explicit name", Arrays.asList(SANS), families(pkg, 0));
		assertEquals("majorHAnsi, no theme part: Cambria", Arrays.asList(SERIF), families(pkg, 1));
		assertEquals("explicit only", Arrays.asList(SERIF), families(pkg, 2));
	}
}
