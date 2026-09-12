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
 * w:cs and w:rtl are values (ST_OnOff), not flags: a false one is off.  Word (CR-016
 * probe fonts-cs-off): Latin text with w:cs, or with w:rtl alone, is set in the
 * complex-script font; with w:cs w:val="0" - directly, or over a character style that
 * says w:cs - or with w:rtl w:val="0", in the ascii font.  Until 17.1.1 the elements'
 * presence was tested, so every one of these took the cs font.
 */
public class RunFontSelectorCsValueTest {

	private static final String TEXT = "The quick brown fox";

	@Test
	public void falseIsOff() throws Exception {

		WordprocessingMLPackage pkg = packageWith(
				styles("<w:rFonts w:ascii=\"FontA\" w:hAnsi=\"FontA\" w:cs=\"FontB\" w:eastAsia=\"FontA\"/><w:sz w:val=\"22\"/>",
						"<w:style w:type=\"character\" w:styleId=\"CsOn\"><w:name w:val=\"CsOn\"/>"
						+ "<w:basedOn w:val=\"DefaultParagraphFont\"/><w:rPr><w:cs/></w:rPr></w:style>"),
				null, null,
				p("<w:cs/>", TEXT)                                              // 0: the cs font
				+ p("<w:cs w:val=\"0\"/>", TEXT)                                // 1: off
				+ p("<w:rStyle w:val=\"CsOn\"/><w:cs w:val=\"0\"/>", TEXT)      // 2: the style's cs overridden
				+ p("<w:rtl w:val=\"0\"/>", TEXT)                               // 3: off
				+ p("<w:rtl/>", TEXT)                                           // 4: the cs font
				+ p("<w:rStyle w:val=\"CsOn\"/>", TEXT)                         // 5: the style's cs applies
				+ p(null, TEXT),                                                // 6: nothing: the ascii font
				"FontA", SANS, "FontB", SERIF);

		assertEquals("w:cs", Arrays.asList(SERIF), families(pkg, 0));
		assertEquals("w:cs w:val=0", Arrays.asList(SANS), families(pkg, 1));
		assertEquals("style cs, direct w:val=0", Arrays.asList(SANS), families(pkg, 2));
		assertEquals("w:rtl w:val=0", Arrays.asList(SANS), families(pkg, 3));
		assertEquals("w:rtl", Arrays.asList(SERIF), families(pkg, 4));
		assertEquals("style cs", Arrays.asList(SERIF), families(pkg, 5));
		assertEquals("nothing", Arrays.asList(SANS), families(pkg, 6));
	}
}
