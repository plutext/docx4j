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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.FontDecision;
import org.docx4j.fonts.FontReport;
import org.docx4j.fonts.FontsAnalysis;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Where this machine has the real symbol font, a run in it, a {@code w:sym} in it and a
 * numbering label in it are all drawn in that font with the document's own code points,
 * on both pathways; and the font report grades the font EXACT.
 *
 * <p>The real font is stood in for by {@code fonts/Docx4jSymbolTest.ttf} (Liberation
 * Sans's ASCII glyphs behind a (3,0) cmap at U+F020-U+F07E, as the Windows faces are
 * encoded), registered as the installed Wingdings for this class alone.  Measured with the
 * Windows faces themselves (2026-10-04): the PDF embeds Wingdings-Regular, SymbolMT,
 * Webdings and Wingdings3, and its text layer carries U+F06E, U+F0B7 and the rest as
 * Word's own PDF does.</p>
 *
 * @since 17.3.1
 */
public class SymbolRealFontTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	static final String FIXTURE = "Docx4j Symbol Test";
	/** Wingdings 0x6E, the round bullet, which the fixture has. */
	private static final String PUA_6E = "\uF06E";

	@BeforeClass
	public static void installTheFixtureAsWingdings() throws Exception {
		PhysicalFonts.addPhysicalFont(SymbolRealFontTest.class.getResource("/fonts/Docx4jSymbolTest.ttf").toURI());
		PhysicalFont fixture = PhysicalFonts.get(FIXTURE);
		assertNotNull("the fixture font did not load", fixture);
		PhysicalFonts.put("Wingdings", fixture);
	}

	/** export-fo-tests reuse one JVM across classes: leave the installed fonts as found. */
	@AfterClass
	public static void uninstall() {
		PhysicalFonts.getPhysicalFonts().remove("wingdings");
		PhysicalFonts.getPhysicalFonts().remove(FIXTURE.toLowerCase());
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\"/></w:rPr><w:t>" + PUA_6E + "</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:sym w:font=\"Wingdings\" w:char=\"F06E\"/></w:r></w:p>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr><w:r><w:t>item</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(
				"<w:numbering " + W + "><w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
				+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"" + PUA_6E + "\"/><w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>"
				+ "<w:rPr><w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\" w:hint=\"default\"/></w:rPr>"
				+ "</w:lvl></w:abstractNum><w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** the (font-family, text) of every fo:inline and label fo:block holding a private-use character */
	private static List<String[]> symbolSpans(org.w3c.dom.Document fo) {
		List<String[]> out = new ArrayList<String[]>();
		for (String name : new String[] { "inline", "block" }) {
			NodeList nl = fo.getElementsByTagNameNS(FO, name);
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				if (el.getElementsByTagNameNS(FO, "*").getLength() > 0) continue; // a container
				String text = el.getTextContent();
				if (!text.matches("(?s).*[\uF000-\uF8FF].*")) continue;
				out.add(new String[] { el.getAttribute("font-family"), text });
			}
		}
		return out;
	}

	private static void assertAllInTheRealFont(org.w3c.dom.Document fo, String pathway) {
		List<String[]> spans = symbolSpans(fo);
		assertEquals(pathway + ": the run, the w:sym and the label; got " + describe(spans), 3, spans.size());
		for (String[] span : spans) {
			assertTrue(pathway + ": drawn in the real font, not " + span[0], span[0].startsWith(FIXTURE));
			assertEquals(pathway + ": the document's own code point", PUA_6E, span[1]);
		}
	}

	private static String describe(List<String[]> spans) {
		StringBuilder sb = new StringBuilder();
		for (String[] s : spans) sb.append("[").append(s[0]).append(" U+").append(Integer.toHexString(s[1].codePointAt(0))).append("] ");
		return sb.toString();
	}

	@Test
	public void visitorPathway() throws Exception {
		assertAllInTheRealFont(fo(pkg(), Docx4J.FLAG_NONE), "visitor");
	}

	@Test
	public void xsltPathway() throws Exception {
		assertAllInTheRealFont(fo(pkg(), Docx4J.FLAG_EXPORT_PREFER_XSL), "xslt");
	}

	@Test
	public void theReportGradesItExact() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		fo(pkg, Docx4J.FLAG_NONE);
		FontReport report = FontsAnalysis.analyse(pkg);
		FontReport.Entry wingdings = null;
		for (FontReport.Entry e : report.getEntries()) {
			if ("Wingdings".equalsIgnoreCase(e.getDocumentFont())) wingdings = e;
		}
		assertNotNull("no entry for Wingdings", wingdings);
		assertEquals(FontReport.Grade.EXACT, wingdings.getGrade());
		assertFalse(wingdings.getDecision() != null && wingdings.getDecision().getSource() == FontDecision.Source.SYMBOL);
	}

	/** And the renderer takes the (3,0)-encoded face: a PDF comes out, embedding it. */
	@Test
	public void rendersToPdf() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		String pdf = new String(baos.toByteArray(), "ISO-8859-1");
		assertTrue("not a PDF", pdf.startsWith("%PDF"));
		assertTrue("the fixture face is not embedded", pdf.contains("Docx4jSymbolTest"));
	}

	/** The real face's declaration in the FOP configuration, or null. */
	private static org.docx4j.convert.out.fopconf.Fonts.Font realFaceDeclaration(WordprocessingMLPackage pkg) throws Exception {
		org.docx4j.convert.out.fopconf.Fop fop = org.docx4j.fonts.fop.util.FopConfigUtil.createConfigurationObject(
				pkg.getFontMapper(), pkg.getMainDocumentPart().fontsInUse());
		org.docx4j.convert.out.fopconf.Fonts fonts = org.docx4j.fonts.fop.util.FopConfigUtil.get(
				fop.getRenderers(), "application/pdf").getFonts();
		for (org.docx4j.convert.out.fopconf.Fonts.Font f : fonts.getFont()) {
			for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : f.getFontTriplet()) {
				if (t.getName().equals(FIXTURE)) return f;
			}
		}
		return null;
	}

	private static String toUnicode(org.docx4j.convert.out.fopconf.Fonts.Font font, String codePoint) {
		for (org.docx4j.convert.out.fopconf.Fonts.Font.ToUnicode tu : font.getToUnicode()) {
			if (tu.getCodePoint().equals(codePoint)) return tu.getUnicode();
		}
		return null;
	}

	/** The real face's declaration gives each private-use code point the Unicode text
	 *  SymbolMapper has for it, for the PDF's ToUnicode CMap (fork CR-014); a face drawn
	 *  for a substitute's own Unicode needs none. */
	@Test
	public void theRealFaceDeclaresItsUnicodeText() throws Exception {
		org.docx4j.convert.out.fopconf.Fonts.Font font = realFaceDeclaration(pkg());
		assertNotNull("the fixture is not declared", font);
		assertEquals("Wingdings 0x6E", "25A0", toUnicode(font, "F06E"));
		assertEquals("Wingdings 0x4A, the smiling face", "263A", toUnicode(font, "F04A"));
		assertEquals("Wingdings 0x4B, outside the BMP", "1F610", toUnicode(font, "F04B"));
	}

	@Test
	public void theUnicodeTextCanBeLeftOut() throws Exception {
		org.docx4j.Docx4jProperties.setProperty("docx4j.fonts.fop.util.FopConfigUtil.to-unicode", "false");
		try {
			org.docx4j.convert.out.fopconf.Fonts.Font font = realFaceDeclaration(pkg());
			assertNotNull("the fixture is not declared", font);
			assertTrue(font.getToUnicode().isEmpty());
		} finally {
			org.docx4j.Docx4jProperties.setProperty("docx4j.fonts.fop.util.FopConfigUtil.to-unicode", "true");
		}
	}

	/** On a renderer with the hook, the PDF's text is the Unicode: the run, the w:sym and the
	 *  label each copy out as U+25A0, not U+F06E.  Apache FOP has no such hook. */
	@Test
	public void thePdfTextIsTheUnicode() throws Exception {
		org.junit.Assume.assumeTrue("the renderer has no to-unicode-map hook",
				org.docx4j.convert.out.fo.FopCapabilities.has(org.docx4j.convert.out.fo.FopCapabilities.Capability.TO_UNICODE_MAP));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		String text;
		try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(baos.toByteArray())) {
			text = new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
		}
		assertFalse("a private-use code point in the text layer: " + text, text.matches("(?s).*[\uF000-\uF8FF].*"));
		assertEquals("the run, the w:sym and the label: " + text, 3, text.length() - text.replace("\u25A0", "").length());
	}

	/** Without the real font the substitute path is taken, as before (the control). */
	@Test
	public void withoutTheRealFontTheReplacementIsDrawn() throws Exception {
		org.junit.Assume.assumeTrue("no substitute symbol face on the classpath", PhysicalFonts.getWDingsFont() != null);
		PhysicalFont fixture = PhysicalFonts.getPhysicalFonts().remove("wingdings");
		try {
			List<String[]> spans = symbolSpans(fo(pkg(), Docx4J.FLAG_NONE));
			assertTrue("no private-use code point reaches the FO; got " + describe(spans), spans.isEmpty());
		} finally {
			PhysicalFonts.put("Wingdings", fixture);
		}
	}
}
