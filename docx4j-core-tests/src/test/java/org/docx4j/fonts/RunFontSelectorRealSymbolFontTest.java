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

import static org.docx4j.fonts.FontsTestSupport.SANS;
import static org.docx4j.fonts.FontsTestSupport.SERIF;
import static org.docx4j.fonts.FontsTestSupport.p;
import static org.docx4j.fonts.FontsTestSupport.packageWith;
import static org.docx4j.fonts.FontsTestSupport.plain;
import static org.docx4j.fonts.FontsTestSupport.selector;
import static org.docx4j.fonts.FontsTestSupport.styles;
import static org.docx4j.fonts.FontsTestSupport.xslFoSelector;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A run in a symbol font is drawn in the <b>real font</b> where this machine has it, with
 * the document's own code points and no Unicode replacement (the mapped path, through
 * SymbolMapper and a substitute face, is what it takes where the machine has not).
 *
 * <p>The real font is stood in for by {@code fonts/Docx4jSymbolTest.ttf}: Liberation Sans's
 * ASCII glyphs behind a (3,0) cmap at U+F020-U+F07E, encoded as Symbol and the Wingdings
 * fonts are (a Microsoft face cannot ship with the tests), registered as the installed
 * Wingdings for this class alone.  Measured on the five Windows faces (2026-10-04): every
 * one maps U+F020-U+F0FF, and Wingdings 2, Wingdings 3 and Webdings map the 8-bit alias
 * (0x20-0xFF) of none of them - which is why the private-use form is the one sent.</p>
 *
 * @since 17.3.1
 */
public class RunFontSelectorRealSymbolFontTest {

	static final String FIXTURE = "Docx4j Symbol Test";

	/** Wingdings 0x6E, the round bullet; the fixture has it (U+F020-U+F07E). */
	private static final String PUA_6E = "\uF06E";
	/** Wingdings 0xFC, the check mark; the fixture has not. */
	private static final String PUA_FC = "\uF0FC";

	@BeforeClass
	public static void installTheFixtureAsWingdings() throws Exception {
		PhysicalFonts.addPhysicalFont(
				RunFontSelectorRealSymbolFontTest.class.getResource("/fonts/Docx4jSymbolTest.ttf").toURI());
		PhysicalFont fixture = PhysicalFonts.get(FIXTURE);
		assertNotNull("the fixture font did not load", fixture);
		assertTrue("the fixture is not symbol-encoded", PhysicalFonts.isSymbolEncoded(fixture));
		PhysicalFonts.put("Wingdings", fixture);
	}

	@AfterClass
	public static void uninstall() {
		PhysicalFonts.getPhysicalFonts().remove("wingdings");
		PhysicalFonts.getPhysicalFonts().remove(FIXTURE.toLowerCase());
	}

	private static WordprocessingMLPackage pkg(String text, String wingdingsMapping) throws Exception {
		return packageWith(
				styles("<w:rFonts w:ascii=\"" + SANS + "\" w:hAnsi=\"" + SANS + "\"/><w:sz w:val=\"22\"/>", null),
				null, null,
				p("<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\"/>", text),
				SANS, SANS, "Wingdings", wingdingsMapping);
	}

	/** the spans the selector produced for the first paragraph's first run */
	private static Element[] spans(WordprocessingMLPackage pkg, RunFontActionType mode) throws Exception {
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		R r = (R) p.getContent().get(0);
		Object o = (mode == RunFontActionType.XSL_FO ? xslFoSelector(pkg) : selector(pkg, mode))
				.fontSelector(p.getPPr(), r.getRPr(), (Text) XmlUtils.unwrap(r.getContent().get(0)));
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		if (o instanceof DocumentFragment) {
			for (Node n = ((DocumentFragment) o).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element) out.add((Element) n);
			}
		}
		return out.toArray(new Element[out.size()]);
	}

	@Test
	public void theRealFontDrawsTheDocumentsOwnCodePoint() throws Exception {
		Element[] spans = spans(pkg(PUA_6E, FIXTURE), RunFontActionType.XSL_FO);
		assertEquals("one span", 1, spans.length);
		assertEquals(FIXTURE, plain(spans[0].getAttribute("font-family")));
		assertEquals("the document's own code point, not a Unicode replacement", PUA_6E, spans[0].getTextContent());
	}

	/** VBA writes the byte (rng.InsertAfter Chr(110)); the font holds the glyph at U+F06E. */
	@Test
	public void aByteIsSentInThePrivateUseForm() throws Exception {
		Element[] spans = spans(pkg("n", FIXTURE), RunFontActionType.XSL_FO);
		assertEquals("one span", 1, spans.length);
		assertEquals(FIXTURE, plain(spans[0].getAttribute("font-family")));
		assertEquals(PUA_6E, spans[0].getTextContent());
	}

	/** A character the real font lacks takes the mapped path, in a stretch of its own. */
	@Test
	public void aCharacterTheRealFontLacksIsMapped() throws Exception {
		Element[] spans = spans(pkg(PUA_6E + PUA_FC, FIXTURE), RunFontActionType.XSL_FO);
		assertEquals("two spans: the real font's, then the mapped character's", 2, spans.length);
		assertEquals(FIXTURE, plain(spans[0].getAttribute("font-family")));
		assertEquals(PUA_6E, spans[0].getTextContent());
		assertFalse("the second span is not in the real font", FIXTURE.equals(plain(spans[1].getAttribute("font-family"))));
		assertFalse("the second span holds a replacement, not the private-use code point",
				PUA_FC.equals(spans[1].getTextContent()));
	}

	/** The real font is also found by name where the mapper put something else under it. */
	@Test
	public void theInstalledFontIsFoundWhateverTheMapperDid() throws Exception {
		Element[] spans = spans(pkg(PUA_6E, SERIF), RunFontActionType.XSL_FO);
		assertEquals(1, spans.length);
		assertEquals(FIXTURE, plain(spans[0].getAttribute("font-family")));
		assertEquals(PUA_6E, spans[0].getTextContent());
	}

	/** Without the real font (the usual machine) the mapped path is taken as before. */
	@Test
	public void withoutTheRealFontTheReplacementIsDrawn() throws Exception {
		PhysicalFont fixture = PhysicalFonts.getPhysicalFonts().remove("wingdings");
		try {
			WordprocessingMLPackage pkg = pkg(PUA_6E, SERIF);
			assertNull(PhysicalFonts.getSymbolEncodedFace("Wingdings", pkg.getFontMapper()));
			Element[] spans = spans(pkg, RunFontActionType.XSL_FO);
			assertEquals(1, spans.length);
			assertFalse(FIXTURE.equals(plain(spans[0].getAttribute("font-family"))));
			assertFalse("a replacement, not the private-use code point", PUA_6E.equals(spans[0].getTextContent()));
		} finally {
			PhysicalFonts.put("Wingdings", fixture);
		}
	}

	/** HTML is unchanged: a font on the converting machine does not help the reader's browser. */
	@Test
	public void htmlKeepsTheReplacement() throws Exception {
		Element[] spans = spans(pkg(PUA_6E, FIXTURE), RunFontActionType.XHTML);
		assertEquals(1, spans.length);
		assertFalse("HTML carries the Unicode replacement", PUA_6E.equals(spans[0].getTextContent()));
	}

	/** The font report grades the real font as it grades any installed font: EXACT. */
	@Test
	public void theReportGradesItExact() throws Exception {
		WordprocessingMLPackage pkg = pkg(PUA_6E, FIXTURE);
		spans(pkg, RunFontActionType.XSL_FO);
		FontReport report = FontsAnalysis.analyse(pkg);
		FontReport.Entry wingdings = null;
		for (FontReport.Entry e : report.getEntries()) {
			if ("Wingdings".equalsIgnoreCase(e.getDocumentFont())) wingdings = e;
		}
		assertNotNull("no entry for Wingdings in " + report.getEntries(), wingdings);
		assertEquals(FontReport.Grade.EXACT, wingdings.getGrade());
		assertFalse("not the symbol-substitute decision",
				wingdings.getDecision() != null && wingdings.getDecision().getSource() == FontDecision.Source.SYMBOL);
	}

	/** The form a symbol font's cmap holds a character at. */
	@Test
	public void symbolCodePoint() {
		assertEquals(0xF06E, RunFontSelector.symbolCodePoint(0xF06E));
		assertEquals(0xF06E, RunFontSelector.symbolCodePoint('n'));
		assertEquals(0xF0FF, RunFontSelector.symbolCodePoint(0xFF));
		assertEquals("issue 632: U+2030 is the Windows-1252 0x89", 0xF089, RunFontSelector.symbolCodePoint(0x2030));
		assertEquals("issue 632: U+2022 is the Windows-1252 0x95", 0xF095, RunFontSelector.symbolCodePoint(0x2022));
		assertEquals(-1, RunFontSelector.symbolCodePoint(0x2713));
		assertEquals(-1, RunFontSelector.symbolCodePoint(0x09));
		assertEquals(-1, RunFontSelector.symbolCodePoint(0xF100));
	}
}
