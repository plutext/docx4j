package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.Document;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.STBorder;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Paragraph borders and shading that come from the paragraph's style (Word's
 * default Title style has a bottom border) are grouped into a container like
 * direct ones; and a border's w:space becomes padding (CR-001 §6.6 item 17).
 */
public class ContainerizationStyleBordersTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	@Test
	public void titleStyleBorderIsContainerized() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage(); // Word's default styles, Title has w:pBdr
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>plain</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t>title</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>plain again</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		Containerization.process(pkg);
		java.util.List<Object> body = pkg.getMainDocumentPart().getJaxbElement().getBody().getContent();
		assertEquals(3, body.size());
		Object second = XmlUtils.unwrap(body.get(1));
		assertTrue("Title paragraph not wrapped in a borders container: " + second.getClass(), second instanceof SdtBlock);
		SdtBlock sdt = (SdtBlock)second;
		assertEquals(Containerization.TAG_BORDERS, sdt.getSdtPr().getTag().getVal());
		assertTrue(XmlUtils.unwrap(body.get(0)) instanceof org.docx4j.wml.P);
		assertTrue(XmlUtils.unwrap(body.get(2)) instanceof org.docx4j.wml.P);
	}

	/** A table ends the group: a bordered paragraph, a table and a bordered paragraph are
	 *  two borders containers with the table between them, not one around all three.
	 *  @since 17.3.1 */
	@Test
	public void aTableEndsTheBorderGroup() throws Exception {
		String bordered = "<w:p><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"6\" w:space=\"1\" w:color=\"auto\"/></w:pBdr>"
				+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"DDDDDD\"/></w:pPr><w:r><w:t>bordered</w:t></w:r></w:p>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ bordered
				+ "<w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr><w:tc><w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ bordered
				+ "</w:body></w:document>"));
		Containerization.process(pkg);
		java.util.List<Object> body = pkg.getMainDocumentPart().getJaxbElement().getBody().getContent();
		assertEquals(3, body.size());
		assertTrue("the first paragraph's container", XmlUtils.unwrap(body.get(0)) instanceof SdtBlock);
		assertTrue("the table at body level, not in the paragraph's border: " + XmlUtils.unwrap(body.get(1)).getClass(),
				XmlUtils.unwrap(body.get(1)) instanceof org.docx4j.wml.Tbl);
		assertTrue("the second paragraph's own container", XmlUtils.unwrap(body.get(2)) instanceof SdtBlock);
	}

	@Test
	public void borderSpaceBecomesPadding() {
		CTBorder b = new CTBorder();
		b.setVal(STBorder.SINGLE);
		b.setSz(java.math.BigInteger.valueOf(8));
		b.setSpace(java.math.BigInteger.valueOf(4));
		b.setColor("4F81BD");
		PBorderBottom p = new PBorderBottom(b);
		Element el = XmlUtils.getNewDocumentBuilder().newDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
		p.setXslFO(el);
		assertEquals("4pt", el.getAttribute("padding-bottom"));
		assertEquals("solid", el.getAttribute("border-bottom-style"));
		assertTrue(p.getCssProperty(), p.getCssProperty().contains("padding-bottom: 4pt"));

		b.setSpace(java.math.BigInteger.ZERO);
		Element el2 = XmlUtils.getNewDocumentBuilder().newDocument().createElementNS("http://www.w3.org/1999/XSL/Format", "fo:block");
		new PBorderBottom(b).setXslFO(el2);
		assertEquals("", el2.getAttribute("padding-bottom"));
	}
}
