package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.NodeList;

/**
 * Footnotes as Word lays them out (CR-001 Phase 4, measured against Word 365):
 * the note found by w:id (Word numbers its separators -1 and 0, so position
 * and id differ), its paragraphs directly in the footnote body with the number
 * inline (no hanging indent), and a 2in separator rule in a line of the
 * separator note's font.
 */
public class FootnoteLayoutTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>"
				+ "<w:r><w:t xml:space=\"preserve\">Body text</w:t></w:r>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteReference w:id=\"1\"/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> and more</w:t></w:r>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteReference w:id=\"2\"/></w:r>"
				+ "</w:p></w:body></w:document>"));
		FootnotesPart fp = new FootnotesPart();
		fp.setJaxbElement((CTFootnotes)XmlUtils.unmarshalString(
				"<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p>"
				+   "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteRef/></w:r>"
				+   "<w:r><w:t xml:space=\"preserve\"> First note.</w:t></w:r>"
				+ "</w:p></w:footnote>"
				+ "<w:footnote w:id=\"2\"><w:p>"
				+   "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteRef/></w:r>"
				+   "<w:r><w:t xml:space=\"preserve\"> Second note.</w:t></w:r>"
				+ "</w:p></w:footnote>"
				+ "</w:footnotes>", Context.jc, CTFootnotes.class));
		pkg.getMainDocumentPart().addTargetPart(fp);
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static NodeList nodes(org.w3c.dom.Document doc, String xpath) throws Exception {
		XPath xp = XPathFactory.newInstance().newXPath();
		xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
			public String getNamespaceURI(String prefix) { return "fo".equals(prefix) ? "http://www.w3.org/1999/XSL/Format" : null; }
			public String getPrefix(String uri) { return null; }
			public java.util.Iterator<String> getPrefixes(String uri) { return null; }
		});
		return (NodeList)xp.evaluate(xpath, doc, XPathConstants.NODESET);
	}

	private void check(org.w3c.dom.Document doc) throws Exception {
		// found by id, not position: note 1 is "First note.", not the continuation separator
		NodeList bodies = nodes(doc, "//fo:footnote/fo:footnote-body");
		assertEquals(2, bodies.getLength());
		assertTrue(bodies.item(0).getTextContent().contains("First note."));
		assertTrue(bodies.item(1).getTextContent().contains("Second note."));
		assertFalse("continuation separator rendered as a note", doc.getDocumentElement().getTextContent().contains("\u0001"));

		// the note's paragraph is the body's block, number inline in it (no list-block)
		assertEquals(0, nodes(doc, "//fo:footnote-body//fo:list-block").getLength());
		assertEquals(1, nodes(doc, "(//fo:footnote)[1]/fo:footnote-body/fo:block//fo:inline[text()='1']").getLength());
		assertEquals(1, nodes(doc, "(//fo:footnote)[2]/fo:footnote-body/fo:block//fo:inline[text()='2']").getLength());

		// the reference mark is the number styled by its run: a superscript at 65% size
		// (VerticalAlignment), nothing added by the footnote itself
		assertEquals(1, nodes(doc, "(//fo:footnote)[1]/fo:inline[not(@baseline-shift)]//fo:inline[text()='1']").getLength());
		assertEquals(1, nodes(doc, "(//fo:footnote)[1]/ancestor::fo:inline[@baseline-shift][contains(@font-size,'pt')]").getLength());

		// separator: a line of the separator note's font holding a 2in rule
		NodeList sep = nodes(doc, "//fo:static-content[@flow-name='xsl-footnote-separator']/fo:block-container");
		assertEquals(1, sep.getLength());
		String h = ((org.w3c.dom.Element)sep.item(0)).getAttribute("height");
		assertTrue("line height of the separator note's font: " + h, h.endsWith("pt") && Double.parseDouble(h.replace("pt", "")) > 10);
		NodeList rule = nodes(doc, "//fo:static-content[@flow-name='xsl-footnote-separator']//fo:block-container[@width='144pt'][@border-bottom]");
		assertEquals(1, rule.getLength());
		assertEquals(0, nodes(doc, "//fo:static-content[@flow-name='xsl-footnote-separator']//fo:leader").getLength());

		// superscripts must not grow lines (the FO root disregards shifts)
		assertEquals("disregard-shifts", doc.getDocumentElement().getAttribute("line-height-shift-adjustment"));
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
