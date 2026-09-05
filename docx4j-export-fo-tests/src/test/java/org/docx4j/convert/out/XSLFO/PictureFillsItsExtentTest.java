package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
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
 * Word draws a picture filling the frame the document declares, whatever the stored
 * bitmap's own aspect ratio: a crop (a:srcRect) or a deliberate stretch makes the two
 * differ, and Word honours the frame either way.
 *
 * <p>XSL-FO's default {@code scaling} is {@code uniform}, so where both
 * {@code content-width} and {@code content-height} are given FOP scales by the smaller
 * of the two factors and leaves the rest of the frame empty.  Measured on a corpus
 * document whose 4:3 photographs are cropped to 1.9:1: Word draws one 492.71 x 259.85pt
 * and FOP drew it 346.49 x 259.87 - so two pictures Word fits on one page needed a page
 * each.  651 of the 1737 pictures of a 103-document corpus (50 of its documents) declare
 * an extent whose aspect differs from the bitmap's by more than 2%.</p>
 *
 * <p>The crop itself is not reproduced: the picture is stretched into the frame rather
 * than cropped to it, which is the same geometry.</p>
 *
 * @since 17.0.6
 */
public class PictureFillsItsExtentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** a 4:3 PNG, so the declared 2:1 extent is not the bitmap's aspect ratio */
	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(400, 300, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 400, 300);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	/** An inline picture 200 x 100pt (2:1), cropped from a 4:3 bitmap. */
	private static String inlinePicture(String relId) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
				+ "<wp:extent cx=\"2540000\" cy=\"1270000\"/>"
				+ "<wp:docPr id=\"1\" name=\"cropped\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"101\" name=\"cropped\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/>"
				+ "<a:srcRect l=\"0\" t=\"12500\" r=\"0\" b=\"12500\"/>"
				+ "<a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2540000\" cy=\"1270000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	private org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>" + inlinePicture(relId) + "</w:p>"
				+ SECT_PR + "</w:body></w:document>"));

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private void check(int flags) throws Exception {
		NodeList list = fo(flags).getElementsByTagNameNS(FO, "external-graphic");
		assertEquals("one picture", 1, list.getLength());
		Element g = (Element)list.item(0);
		assertTrue("the declared width", g.getAttribute("content-width").startsWith("200"));
		assertTrue("the declared height", g.getAttribute("content-height").startsWith("100"));
		assertEquals("the picture fills the frame the docx declares, as Word draws it",
				"non-uniform", g.getAttribute("scaling"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
