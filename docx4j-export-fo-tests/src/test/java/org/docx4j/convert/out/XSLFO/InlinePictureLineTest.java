package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
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
 * An inline picture's box, and the line it makes (CR-001 &#xa7;2.4).
 *
 * <p><b>Its size is fractional.</b> Word states a picture's extent in EMU, and 12700 EMU
 * is exactly one point, so a picture 857250 EMU tall is 67.5pt.  docx4j wrote the size
 * with {@code Integer.toString}, which threw the fraction away - "67px" - and everything
 * below the picture moved up half a point; 291 such attributes in 75 of 156 corpus
 * documents.</p>
 *
 * <p><b>And the line is the picture's height, with no descent.</b> The line box the Word
 * line manager needs is sized from the <em>text</em> runs on the line, so a paragraph
 * whose only content is a picture got none at all and fell back to FOP's own line, which
 * adds the paragraph font's descent below the picture.  Measured against Word: a 67.5pt
 * logo above a 14pt paragraph put the next baseline 4.8pt below Word's.</p>
 *
 * @since 17.0.6
 */
public class InlinePictureLineTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** 857250 EMU = 67.5pt high, 2381250 = 187.5pt wide: both a whole point and a half. */
	private static final long CY = 857250L;
	private static final long CX = 2381250L;

	private static String picture(String relId) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:inline><wp:extent cx=\"" + CX + "\" cy=\"" + CY + "\"/>"
				+ "<wp:docPr id=\"1\" name=\"pic\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"pic\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + CX + "\" cy=\"" + CY + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	/** the same 187.5 : 67.5 aspect ratio as the declared extent, so FOP's uniform
	 *  scaling paints the picture at exactly the size the docx asks for */
	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(250, 90, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 250, 90);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p>" + picture(relId) + "</w:p>"
				+ "<w:p><w:r><w:t>the line below the picture</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static double lengthPt(String v) {
		if (v.endsWith("pt") || v.endsWith("px")) v = v.substring(0, v.length() - 2);
		return Double.parseDouble(v);
	}

	private void theExtentIsNotRounded(int flags) throws Exception {
		NodeList graphics = fo(flags).getElementsByTagNameNS(FO, "external-graphic");
		assertEquals(1, graphics.getLength());
		Element g = (Element) graphics.item(0);
		assertEquals("content-height", 67.5, lengthPt(g.getAttribute("content-height")), 0.01);
		assertEquals("content-width", 187.5, lengthPt(g.getAttribute("content-width")), 0.01);
	}

	@Test
	public void theExtentIsNotRoundedVisitor() throws Exception {
		theExtentIsNotRounded(Docx4J.FLAG_NONE);
	}

	@Test
	public void theExtentIsNotRoundedXslt() throws Exception {
		theExtentIsNotRounded(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * The picture-only line is the picture's height: in the area tree, the line holding
	 * the text below it starts within a point of 67.5pt past the top of the body, rather
	 * than the paragraph font's descent lower again.
	 */
	@Test
	public void thePictureLineHasNoDescent() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(), Docx4J.FLAG_NONE);
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		assertTrue("expected the picture line and the text line, got " + lines.getLength(),
				lines.getLength() >= 2);
		int picture = Integer.parseInt(((Element) lines.item(0)).getAttribute("bpd"));
		assertTrue("the picture's line is " + picture / 1000.0 + "pt, not the picture's own 67.5pt",
				Math.abs(picture - 67500) < 1000);
	}
}
