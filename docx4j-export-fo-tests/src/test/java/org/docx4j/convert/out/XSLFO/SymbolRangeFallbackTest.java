package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.GlyphCheck;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * U+2190-U+2BFF - Arrows, Mathematical Operators, Box Drawing, Geometric Shapes,
 * Dingbats - in a text font which has no such glyph.  {@code RunFontSelector} asked one
 * substitute, {@code Segoe UI Symbol}, which is a Windows face; failing it, it named no
 * font at all, so the span carried no {@code font-family} for the glyph-coverage pass to
 * work from and FOP painted its {@code NOT_FOUND} glyph, {@code #}.  Every code point of
 * those blocks is {@code UnicodeScript.COMMON}, which the coverage pass treats as always
 * covered, so it would have skipped them even with a family to work from.
 *
 * <p>Measured over the 449 renders of three corpora, 430 excess {@code #} in 39 documents
 * against Word's own PDFs, which have none of them.</p>
 *
 * @since 17.1.0
 */
public class SymbolRangeFallbackTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** Arrow, black right-pointing triangle, box drawing, dingbat. */
	private static final String SYMBOLS = "→▶─✔";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r>"
				+ "<w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/><w:sz w:val=\"24\"/></w:rPr>"
				+ "<w:t xml:space=\"preserve\">before " + SYMBOLS + " after</w:t>"
				+ "</w:r></w:p></w:body></w:document>"));
		return pkg;
	}

	private org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/**
	 * Every one of the four symbols is drawn in a font which actually has its glyph: the
	 * innermost element whose text is (or contains) the symbol names a font-family, and
	 * that face covers the character.
	 */
	private void everySymbolIsSetInAFontWhichHasIt(int flags) throws Exception {

		org.w3c.dom.Document doc = fo(flags);
		for (int i = 0; i < SYMBOLS.length(); i++) {
			char c = SYMBOLS.charAt(i);
			String family = familyDrawing(doc.getDocumentElement(), c);
			assertNotNull("no font-family at all for U+" + Integer.toHexString(c)
					+ ", so FOP will paint its NOT_FOUND glyph", family);
			PhysicalFont pf = PhysicalFonts.get(family);
			assertNotNull("the FO names a font this machine has not got: " + family, pf);
			assertTrue("U+" + Integer.toHexString(c) + " is set in " + family
					+ ", which has no glyph for it", GlyphCheck.hasCodepoint(pf, c));
		}
	}

	/** The font-family of the innermost element whose own text holds this character. */
	private String familyDrawing(Element el, char c) {

		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) continue;
			Element child = (Element) children.item(i);
			if (child.getTextContent().indexOf(c) >= 0) {
				String deeper = familyDrawing(child, c);
				if (deeper != null) return deeper;
				String own = child.getAttributeNS(null, "font-family");
				if (own.length() > 0) return own;
			}
		}
		// not in any child: this element draws it itself
		String own = el.getAttributeNS(null, "font-family");
		return own.length() > 0 && el.getTextContent().indexOf(c) >= 0 ? own : null;
	}

	@Test
	public void visitor() throws Exception {
		everySymbolIsSetInAFontWhichHasIt(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		everySymbolIsSetInAFontWhichHasIt(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** The text either side of the symbols keeps the run's own font: only what needs a
	 *  substitute gets one. */
	@Test
	public void theTextAroundKeepsItsOwnFont() throws Exception {
		org.w3c.dom.Document doc = fo(Docx4J.FLAG_NONE);
		String symbolFamily = familyDrawing(doc.getDocumentElement(), '→');
		String textFamily = familyDrawing(doc.getDocumentElement(), 'b');
		assertNotNull(textFamily);
		assertTrue("the ordinary text was moved into the symbol font too",
				!textFamily.equals(symbolFamily) || GlyphCheck.hasCodepoint(
						PhysicalFonts.get(textFamily), '→'));
		assertTrue("an fo:inline should carry the substitute",
				doc.getElementsByTagNameNS(FO, "inline").getLength() > 0);
	}
}
