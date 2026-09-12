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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.Numbering;
import org.junit.Test;

/**
 * {@link MainDocumentPart#fontsInUse()} as a walk for names (CR-016 phase 4): every
 * slot of every rFonts the document could ask for - runs, paragraph marks, the styles
 * in use and what they are based on, the document defaults, headers, numbering levels,
 * w:sym - with the theme references resolved, a CJK name by its English name, and
 * nothing from a style the document never uses.
 */
public class FontsInUseTest {

	private static final String W = FontsTestSupport.W;

	private static WordprocessingMLPackage pkg(String fontScheme) throws Exception {
		String docDefaults = "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" w:cs=\"\"/>";
		String styles =
				"<w:style w:type=\"paragraph\" w:styleId=\"Base\"><w:name w:val=\"Base\"/><w:rPr><w:rFonts w:ascii=\"Base Font\"/></w:rPr></w:style>"
				+ "<w:style w:type=\"paragraph\" w:styleId=\"Quote\"><w:name w:val=\"Quote\"/><w:basedOn w:val=\"Base\"/><w:rPr><w:rFonts w:cs=\"Quote CS Font\"/></w:rPr></w:style>"
				+ "<w:style w:type=\"paragraph\" w:styleId=\"Unused\"><w:name w:val=\"Unused\"/><w:rPr><w:rFonts w:ascii=\"Unused Font\"/></w:rPr></w:style>"
				+ "<w:style w:type=\"character\" w:styleId=\"Emph\"><w:name w:val=\"Emph\"/><w:rPr><w:rFonts w:ascii=\"Char Style Font\"/></w:rPr></w:style>";
		String body =
				// a paragraph mark's font, a paragraph style with a base, all four slots of a run
				"<w:p><w:pPr><w:pStyle w:val=\"Quote\"/><w:rPr><w:rFonts w:ascii=\"Mark Font\"/></w:rPr></w:pPr>"
				+ "<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:eastAsia=\"MS Mincho\" w:cs=\"Arial Unicode MS\"/></w:rPr><w:t>x</w:t></w:r></w:p>"
				// a character style, a CJK name, a w:sym
				+ "<w:p><w:r><w:rPr><w:rStyle w:val=\"Emph\"/><w:rFonts w:ascii=\"宋体\"/></w:rPr><w:t>y</w:t></w:r>"
				+ "<w:r><w:sym w:font=\"Wingdings\" w:char=\"F0FC\"/></w:r></w:p>"
				// a theme reference beside an explicit name: the theme's face is what the
				// selector uses (CR-016 probe fonts-missing-slots (b)), the explicit name never
				+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Aptos\" w:asciiTheme=\"minorHAnsi\" w:cs=\"\"/></w:rPr><w:t>z</w:t></w:r></w:p>";
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(FontsTestSupport.styles(docDefaults, styles), null, fontScheme, body);
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		HeaderPart header = new HeaderPart();
		mdp.addTargetPart(header);
		header.setJaxbElement((Hdr) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:hdr xmlns:w=\"" + W + "\"><w:p><w:r><w:rPr><w:rFonts w:ascii=\"Header Font\"/></w:rPr><w:t>h</w:t></w:r></w:p></w:hdr>")));

		NumberingDefinitionsPart numbering = new NumberingDefinitionsPart();
		mdp.addTargetPart(numbering);
		numbering.setJaxbElement((Numbering) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:numbering xmlns:w=\"" + W + "\"><w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
				+ "<w:rPr><w:rFonts w:ascii=\"Symbol\" w:cs=\"Numbering CS Font\"/></w:rPr></w:lvl></w:abstractNum></w:numbering>")));
		return pkg;
	}

	@Test
	public void everySlotOfEverythingInUse() throws Exception {
		WordprocessingMLPackage pkg = pkg(FontsTestSupport.fontScheme("Cambria", "Calibri", ""));
		Set<String> fonts = pkg.getMainDocumentPart().fontsInUse();

		for (String expected : new String[] {
				"Calibri",           // the theme's minor face, from the document defaults and the run's minorHAnsi
				"Arial", "MS Mincho", "Arial Unicode MS",  // the run's three named slots (eastAsia and cs too, not w:ascii alone)
				"Mark Font",         // the paragraph mark's
				"Base Font", "Quote CS Font",  // the paragraph style in use and what it is based on
				"Char Style Font",   // the character style in use
				"SimSun",            // the CJK name, in English
				"Wingdings",         // w:sym
				"Header Font",       // a header part
				"Symbol", "Numbering CS Font" }) {  // a numbering level, both slots
			assertTrue(expected + " missing from " + fonts, fonts.contains(expected));
		}
		assertFalse("a style the document never uses: " + fonts, fonts.contains("Unused Font"));
		assertFalse("the explicit name beside a theme reference: " + fonts, fonts.contains("Aptos"));
		assertFalse("the CJK name itself: " + fonts, fonts.contains("宋体"));
		assertFalse("a blank name: " + fonts, fonts.contains(""));
	}

	/** No theme part: a theme reference is the Office theme's face (Word supplies it). */
	@Test
	public void noThemePartGivesTheOfficeFaces() throws Exception {
		WordprocessingMLPackage pkg = pkg(null);
		Set<String> fonts = pkg.getMainDocumentPart().fontsInUse();
		assertTrue(fonts.toString(), fonts.contains("Calibri"));
		assertFalse(fonts.toString(), fonts.contains("Aptos"));
	}
}
