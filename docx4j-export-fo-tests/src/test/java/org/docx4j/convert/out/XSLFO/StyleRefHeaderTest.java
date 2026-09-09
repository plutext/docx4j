package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * STYLEREF in a running header is evaluated page by page (the first paragraph of the
 * style on the page, else the nearest before it): an fo:retrieve-marker in the header
 * and an fo:marker on every paragraph of the style, both FO pathways (§7).
 */
public class StyleRefHeaderTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	private static WordprocessingMLPackage pkg(String instr, String header) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>Scope</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>body</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>Terms</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"709\" w:footer=\"709\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		HeaderPart hp = new HeaderPart(new PartName("/word/header1.xml"));
		hp.setPackage(pkg);
		hp.setJaxbElement((Hdr) XmlUtils.unmarshalString(
				"<w:hdr " + W + "><w:p><w:r><w:t xml:space=\"preserve\">Chapter: </w:t></w:r>"
				+ "<w:fldSimple w:instr=\"" + instr + "\"><w:r><w:t>" + header + "</w:t></w:r></w:fldSimple>"
				+ "</w:p></w:hdr>", Context.jc, Hdr.class));
		Relationship hr = pkg.getMainDocumentPart().addTargetPart(hp);
		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		HeaderReference headerReference = factory.createHeaderReference();
		headerReference.setId(hr.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document fo = fo(pkg(" STYLEREF &quot;Heading 1&quot; \\* MERGEFORMAT ", "Introduction"), flags);
		NodeList retrieves = fo.getElementsByTagNameNS(FO, "retrieve-marker");
		assertEquals("one retrieve-marker in the header", 1, retrieves.getLength());
		Element retrieve = (Element) retrieves.item(0);
		assertEquals("docx4j-styleref-Heading1", retrieve.getAttribute("retrieve-class-name"));
		assertEquals("first-starting-within-page", retrieve.getAttribute("retrieve-position"));
		assertEquals("document", retrieve.getAttribute("retrieve-boundary"));
		assertTrue("the stored result must not be painted as well", isAbsent(fo, "//*[contains(text(),'Introduction')]"));

		NodeList markers = fo.getElementsByTagNameNS(FO, "marker");
		assertEquals("a marker on each Heading 1 paragraph", 2, markers.getLength());
		assertEquals("Scope", markers.item(0).getTextContent());
		assertEquals("Terms", markers.item(1).getTextContent());
		for (int i = 0; i < markers.getLength(); i++) {
			Element marker = (Element) markers.item(i);
			assertEquals("docx4j-styleref-Heading1", marker.getAttribute("marker-class-name"));
			assertTrue("the marker must be its block's initial child",
					marker.getParentNode().getFirstChild() == marker);
			assertEquals("block", marker.getParentNode().getLocalName());
		}
		assertTrue("no leftover hint", isAbsent(fo, "//*[@docx4j-pstyle]"));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A style the document does not have leaves the stored result alone. */
	@Test
	public void anUnknownStyleKeepsTheStoredResult() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(" STYLEREF &quot;Chapter Title&quot; ", "Introduction"), Docx4J.FLAG_NONE);
		assertEquals(0, fo.getElementsByTagNameNS(FO, "retrieve-marker").getLength());
		assertEquals(0, fo.getElementsByTagNameNS(FO, "marker").getLength());
		assertTrue(isPresent(fo, "//*[contains(text(),'Introduction')]"));
	}

	/** The outline-level form (STYLEREF 1) is the built-in "heading 1"; \l takes the last on the page. */
	@Test
	public void outlineLevelAndLastSwitch() throws Exception {
		org.w3c.dom.Document fo = fo(pkg(" STYLEREF 1 \\l ", "x"), Docx4J.FLAG_NONE);
		Element retrieve = (Element) fo.getElementsByTagNameNS(FO, "retrieve-marker").item(0);
		assertEquals("docx4j-styleref-Heading1", retrieve.getAttribute("retrieve-class-name"));
		assertEquals("last-starting-within-page", retrieve.getAttribute("retrieve-position"));
	}
}
