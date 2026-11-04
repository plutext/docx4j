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

	/** Adjacent paragraphs share a box only where their borders are the same in every setting and
	 *  their indents are the same (corpus document 4083: one style's top border, three boxes where
	 *  the right indent or the border's w:space differs).  @since 17.3.6 */
	@Test
	public void aBoxHoldsOnlyParagraphsAlikeInBorderAndIndent() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String top = "<w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"C50059\"/></w:pBdr>";
		String topSpace3 = top.replace("w:space=\"1\"", "w:space=\"3\"");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + top + "<w:ind w:left=\"454\" w:right=\"57\" w:hanging=\"454\"/></w:pPr><w:r><w:t>one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + top + "<w:ind w:left=\"454\" w:right=\"57\" w:hanging=\"454\"/></w:pPr><w:r><w:t>two, alike</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + top + "<w:ind w:left=\"454\" w:right=\"114\" w:hanging=\"454\"/></w:pPr><w:r><w:t>three, right indent</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + topSpace3 + "<w:ind w:left=\"454\" w:right=\"114\" w:hanging=\"454\"/></w:pPr><w:r><w:t>four, border space</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		Containerization.process(pkg);
		java.util.List<Object> body = pkg.getMainDocumentPart().getJaxbElement().getBody().getContent();
		assertEquals("three boxes", 3, body.size());
		int[] sizes = new int[3];
		for (int i = 0; i < 3; i++) {
			Object o = XmlUtils.unwrap(body.get(i));
			sizes[i] = o instanceof SdtBlock ? ((SdtBlock)o).getSdtContent().getContent().size() : 1;
		}
		assertEquals(2, sizes[0]);
		assertEquals(1, sizes[1]);
		assertEquals(1, sizes[2]);
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

	/**
	 * A bordered paragraph inside a block-level content control the document holds (Word's TOC gallery, a
	 * template's control) is grouped inside the control, as it would be at body level; the writers apply a
	 * paragraph's top and bottom borders only inside a container, so without this the border was lost
	 * (measured 2026-10-29 on the Enterprise manual's "Contents" heading, 4.96pt of border and spacing).
	 * The control ends the run of bordered paragraphs around it, as a table does. @since 17.3.5
	 */
	@Test
	public void aBorderedParagraphInsideAContentControlIsContainerized() throws Exception {
		String bordered = "<w:p><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"8\" w:space=\"4\" w:color=\"4F81BD\"/></w:pBdr></w:pPr><w:r><w:t>bordered</w:t></w:r></w:p>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ bordered
				+ "<w:sdt><w:sdtPr><w:docPartObj><w:docPartGallery w:val=\"Table of Contents\"/><w:docPartUnique/></w:docPartObj><w:id w:val=\"1\"/></w:sdtPr><w:sdtContent>"
				+   bordered
				+   "<w:p><w:r><w:t>entry</w:t></w:r></w:p>"
				+ "</w:sdtContent></w:sdt>"
				+ bordered
				+ "</w:body></w:document>"));
		Containerization.process(pkg);
		java.util.List<Object> body = pkg.getMainDocumentPart().getJaxbElement().getBody().getContent();
		assertEquals("container, the control, container", 3, body.size());
		assertTrue(XmlUtils.unwrap(body.get(0)) instanceof SdtBlock);
		assertEquals(Containerization.TAG_BORDERS, ((SdtBlock)XmlUtils.unwrap(body.get(0))).getSdtPr().getTag().getVal());
		SdtBlock control = (SdtBlock)XmlUtils.unwrap(body.get(1));
		assertTrue("the document's control stays, at body level", control.getSdtPr().getTag() == null);
		java.util.List<Object> inside = control.getSdtContent().getContent();
		assertEquals("container, then the plain paragraph", 2, inside.size());
		SdtBlock inner = (SdtBlock)XmlUtils.unwrap(inside.get(0));
		assertEquals("the bordered paragraph inside the control gets its container", Containerization.TAG_BORDERS, inner.getSdtPr().getTag().getVal());
		assertTrue(XmlUtils.unwrap(inner.getSdtContent().getContent().get(0)) instanceof org.docx4j.wml.P);
		assertTrue(XmlUtils.unwrap(inside.get(1)) instanceof org.docx4j.wml.P);
		assertTrue("the paragraph after the control opens a container of its own", XmlUtils.unwrap(body.get(2)) instanceof SdtBlock);
		// a second pass does not group the containers again
		Containerization.process(pkg);
		assertEquals(3, pkg.getMainDocumentPart().getJaxbElement().getBody().getContent().size());
		assertEquals(2, control.getSdtContent().getContent().size());
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
