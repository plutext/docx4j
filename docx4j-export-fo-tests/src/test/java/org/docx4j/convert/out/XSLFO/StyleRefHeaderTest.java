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

	private static final String TERMS = "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>Terms</w:t></w:r></w:p>";

	private static WordprocessingMLPackage pkg(String instr, String header) throws Exception {
		return pkg(instr, header, TERMS);
	}

	/** The second heading, Terms, as given: a paragraph, or one in a table cell. */
	private static WordprocessingMLPackage pkg(String instr, String header, String terms) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>Scope</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>body</w:t></w:r></w:p>"
				+ terms
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

	/**
	 * A heading in a table cell is a heading of its style like any other.  On the paths
	 * which give a cell's paragraph a synthetic style (the preprocess), the block's style
	 * hint is the document's own style, so the marker is there: until 17.3.1 the hint was the
	 * synthetic id, and a STYLEREF could not find the heading (CR-030 D7).
	 * @since 17.3.1
	 */
	private void headingInACell(int flags) throws Exception {
		WordprocessingMLPackage pkg = pkg(" STYLEREF &quot;Heading 1&quot; ", "Introduction",
				"<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9000\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w=\"9000\" w:type=\"dxa\"/></w:tcPr>"
				+ TERMS + "</w:tc></w:tr></w:tbl><w:p><w:r><w:t>after</w:t></w:r></w:p>");
		pkg.getMainDocumentPart().getPropertyResolver().activateStyle("TableGrid");
		org.w3c.dom.Document fo = fo(pkg, flags);
		NodeList markers = fo.getElementsByTagNameNS(FO, "marker");
		assertEquals("a marker on each Heading 1 paragraph, the one in the cell too", 2, markers.getLength());
		assertEquals("Terms", markers.item(1).getTextContent());
	}

	@Test
	public void aHeadingInATableCellVisitor() throws Exception {
		headingInACell(Docx4J.FLAG_NONE);
	}

	@Test
	public void aHeadingInATableCellXslt() throws Exception {
		headingInACell(Docx4J.FLAG_EXPORT_PREFER_XSL);
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
