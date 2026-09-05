package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

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
 * Word kerns a run only when its w:kern threshold is at or below its size
 * (both half-points); FOP kerns per font, so such runs go to the font's kerned
 * twin (family name + "+kern", FopConfigUtil).  CR-001 §6.6 item 15.
 */
public class KernedRunsTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FONT = "<w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/>";

	private static String run(String text, int sz, Integer kern) {
		return "<w:r><w:rPr>" + FONT + (kern == null ? "" : "<w:kern w:val=\"" + kern + "\"/>")
				+ "<w:sz w:val=\"" + sz + "\"/></w:rPr><w:t>" + text + "</w:t></w:r>";
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p>" + run("plain", 24, null) + run("below", 24, 28) + run("at", 28, 28) + run("above", 56, 28) + run("off", 24, 0) + "</w:p>"
				+ "<w:p>" + run("AVAILABLE TAX WAYS", 24, 2) + run("PLAIN TAX WAYS", 24, null) + "</w:p>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:rPr>" + FONT + "</w:rPr><w:t>title</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static String family(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (text.equals(el.getTextContent()) && el.getAttribute("font-family").length() > 0) return el.getAttribute("font-family");
		}
		return null;
	}

	/** the span holding the text, whatever its children */
	private static Element span(org.w3c.dom.Document doc, String startsWith) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getTextContent().startsWith(startsWith) && el.getAttribute("font-family").length() > 0) return el;
		}
		return null;
	}

	private static int wordSpacingChildren(Element span) {
		int n = 0;
		NodeList c = span.getChildNodes();
		for (int i = 0; i < c.getLength(); i++) {
			if (c.item(i) instanceof Element && ((Element) c.item(i)).getAttribute("word-spacing").startsWith("-")) n++;
		}
		return n;
	}

	private void check(org.w3c.dom.Document doc) {
		// Word kerns pairs with the space glyph too ("E␠", "␠T", "X␠", "␠W", "S"): each such
		// space of a kerned run is an inline carrying the pair values as word-spacing
		Element kerned = span(doc, "AVAILABLE");
		assertEquals("Liberation Serif+kern", kerned.getAttribute("font-family"));
		assertEquals("AVAILABLE TAX WAYS", kerned.getTextContent());
		org.junit.Assert.assertTrue("no kerned spaces", wordSpacingChildren(kerned) >= 1);
		assertEquals("unkerned run keeps plain spaces", 0, wordSpacingChildren(span(doc, "PLAIN")));

		// an unkerned run goes to the no-ligature twin, which has no OpenType feature
		// at all - neither GSUB liga nor GPOS kern, which is Word's default
		assertEquals("Liberation Serif+noliga", family(doc, "plain"));
		assertEquals("12pt below a 14pt threshold", "Liberation Serif+noliga", family(doc, "below"));
		assertEquals("14pt at a 14pt threshold", "Liberation Serif+kern", family(doc, "at"));
		assertEquals("Liberation Serif+kern", family(doc, "above"));
		assertEquals("w:kern 0", "Liberation Serif+noliga", family(doc, "off"));
		// Word's Title style: w:kern 28 at 26/28pt
		assertEquals("kerning from the style", "Liberation Serif+kern", family(doc, "title"));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(fo(Docx4J.FLAG_NONE));
	}

	@Test
	public void xsltPathway() throws Exception {
		check(fo(Docx4J.FLAG_EXPORT_PREFER_XSL));
	}
}
