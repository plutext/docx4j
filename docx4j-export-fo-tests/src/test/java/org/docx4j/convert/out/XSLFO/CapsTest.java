package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * w:caps and w:smallCaps were dropped altogether, so a heading in the built-in
 * Book Title character style came out in lower case and 85pt narrower than
 * Word's (CR-001, a real document's title page).  XSL FO has no text-transform
 * or font-variant, and FOP would ignore them, so the text itself is upper-cased;
 * for small caps the originally lower case stretches go in an inline at 80% of
 * the size.  Both FO pathways.
 */
public class CapsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	private static org.w3c.dom.Document fo(String rPr, String text, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r><w:rPr>" + rPr + "</w:rPr><w:t>" + text + "</w:t></w:r>"
				+ "</w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the outermost fo:block's text, ie what will be painted */
	private static String rendered(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(XSL_FO, "block");
		return nl.getLength() == 0 ? null : nl.item(0).getTextContent();
	}

	private static Element smallCapInline(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS(XSL_FO, "inline");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (text.equals(el.getTextContent()) && el.getAttribute("font-size").length() > 0) return el;
		}
		return null;
	}

	private void checkCaps(int flags) throws Exception {
		assertEquals("CURRICULUM VITAE", rendered(fo("<w:caps/>", "Curriculum Vitae", flags)));
		// w:caps false, and no caps at all, leave the text alone
		assertEquals("Curriculum Vitae", rendered(fo("<w:caps w:val=\"false\"/>", "Curriculum Vitae", flags)));
		assertEquals("Curriculum Vitae", rendered(fo("", "Curriculum Vitae", flags)));
		// the run's language decides the case mapping (Turkish dotted i)
		assertEquals("İSTANBUL",
				rendered(fo("<w:caps/><w:lang w:val=\"tr-TR\"/>", "istanbul", flags)));
	}

	private void checkSmallCaps(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("<w:smallCaps/>", "Curriculum Vitae", flags);
		assertEquals("CURRICULUM VITAE", rendered(doc));

		// the letters which were lower case are set smaller
		Element small = smallCapInline(doc, "URRICULUM");
		assertNotNull("no small capital inline for URRICULUM", small);
		assertEquals("80%", small.getAttribute("font-size"));
		assertNotNull(smallCapInline(doc, "ITAE"));
		// ... and the letters which were already capitals are not
		assertTrue("C should not be a small capital", smallCapInline(doc, "C") == null);

		assertEquals("Curriculum Vitae", rendered(fo("<w:smallCaps w:val=\"false\"/>", "Curriculum Vitae", flags)));
	}

	@Test
	public void capsVisitor() throws Exception {
		checkCaps(Docx4J.FLAG_NONE);
	}

	@Test
	public void capsXslt() throws Exception {
		checkCaps(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void smallCapsVisitor() throws Exception {
		checkSmallCaps(Docx4J.FLAG_NONE);
	}

	@Test
	public void smallCapsXslt() throws Exception {
		checkSmallCaps(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Caps from a character style, which is how Word's Book Title applies them. */
	@Test
	public void fromACharacterStyle() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(org.docx4j.wml.Style)XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"character\" w:styleId=\"BookTitle\"><w:name w:val=\"Book Title\"/>"
				+ "<w:rPr><w:b/><w:smallCaps/></w:rPr></w:style>"));
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"BookTitle\"/></w:rPr><w:t>Moffat Moswane</w:t></w:r>"
				+ "</w:p></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		org.w3c.dom.Document doc =
				XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
		assertEquals("MOFFAT MOSWANE", rendered(doc));
		assertNotNull(smallCapInline(doc, "OFFAT"));
	}
}
