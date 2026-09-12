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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Test;

/**
 * The HTML span's font-family (CR-016 Decisions 1): the document font first, the family
 * of the physical font it mapped to second, the generic class last, and never empty;
 * docx4j.convert.out.html.fontFamily=physical restores the physical-only output.
 */
public class HtmlFontFamilyTest {

	private static final String DOC_DEFAULTS = "<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>";

	/** the font-family declaration of a style attribute (the span may carry white-space too) */
	private static String fontFamily(String style) {
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("font-family:[^;]*;").matcher(style);
		return m.find() ? m.group() : "";
	}

	@After
	public void restoreTheDefault() {
		Docx4jProperties.setProperty(RunFontSelector.HTML_FONT_FAMILY_PROPERTY, "document");
	}

	@Test
	public void documentFontThenPhysicalFamilyThenGeneric() throws Exception {
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(FontsTestSupport.styles(DOC_DEFAULTS, null), null, null,
				FontsTestSupport.p(null, "Hello"), "Calibri", FontsTestSupport.SANS);
		assertEquals("font-family: 'Calibri','" + FontsTestSupport.SANS + "',sans-serif;",
				fontFamily(FontsTestSupport.htmlStyle(pkg, 0)));
	}

	@Test
	public void aFontThisMachineHasNamesItselfOnce() throws Exception {
		// the document font is the physical font: named once, with its class
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(FontsTestSupport.styles(DOC_DEFAULTS, null), null, null,
				FontsTestSupport.p("<w:rFonts w:ascii=\"" + FontsTestSupport.SERIF + "\" w:hAnsi=\"" + FontsTestSupport.SERIF + "\"/>", "Hello"),
				FontsTestSupport.SERIF, FontsTestSupport.SERIF);
		assertEquals("font-family: '" + FontsTestSupport.SERIF + "',serif;", fontFamily(FontsTestSupport.htmlStyle(pkg, 0)));
	}

	@Test
	public void anUnmappedFontIsStillNamed() throws Exception {
		// nothing mapped, no class from the name: the document font alone, never nothing
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(FontsTestSupport.styles(DOC_DEFAULTS, null), null, null,
				FontsTestSupport.p("<w:rFonts w:ascii=\"Zqxjk Nonesuch\" w:hAnsi=\"Zqxjk Nonesuch\"/>", "Hello"));
		assertEquals("font-family: 'Zqxjk Nonesuch';", fontFamily(FontsTestSupport.htmlStyle(pkg, 0)));
	}

	@Test
	public void thePhysicalOnlyOutputOfBefore() throws Exception {
		Docx4jProperties.setProperty(RunFontSelector.HTML_FONT_FAMILY_PROPERTY, "physical");
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(FontsTestSupport.styles(DOC_DEFAULTS, null), null, null,
				FontsTestSupport.p(null, "Hello") + FontsTestSupport.p("<w:rFonts w:ascii=\"Zqxjk Nonesuch\" w:hAnsi=\"Zqxjk Nonesuch\"/>", "x"),
				"Calibri", FontsTestSupport.SANS);
		assertEquals("font-family: '" + FontsTestSupport.SANS + "';", fontFamily(FontsTestSupport.htmlStyle(pkg, 0)));
		assertFalse("an unmapped font got a font-family in physical mode", FontsTestSupport.htmlStyle(pkg, 1).contains("font-family"));
	}
}
