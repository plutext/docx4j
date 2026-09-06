package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A picture Word draws <em>behind</em> the text ({@code wp:anchor/@behindDoc="1"})
 * displaces nothing: its wrap element is not applied, the text runs over it, and it
 * reserves no space.
 *
 * <p>Measured against Word 365 on a document whose 155.25pt logo carries
 * {@code wrapSquare} and {@code behindDoc="1"}: Word puts the caption beside it at
 * x=85.0, where the float put docx4j's at 249.3 - +164.3 = the picture's 155.25 plus its
 * 9.0pt distL/distR - and moved a whole block from above the table to below it,
 * everything after it +92.6pt.  9 documents of three corpora carry a wrapped
 * {@code behindDoc} anchor.</p>
 *
 * @since 17.0.6
 */
public class BehindDocAnchorTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(80, 60, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 80, 60);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	private static String anchoredPicture(String relId, boolean behindDoc) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"" + (behindDoc ? "1" : "0") + "\""
				+ " locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"margin\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"1270000\" cy=\"952500\"/>"
				+ "<wp:wrapSquare wrapText=\"bothSides\"/>"
				+ "<wp:docPr id=\"1\" name=\"anchor1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"101\" name=\"anchor1\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1270000\" cy=\"952500\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	private static org.w3c.dom.Document fo(boolean behindDoc, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String body = "<w:p>" + anchoredPicture(img.getSourceRelationship().getId(), behindDoc)
				+ "<w:r><w:t>Palmera Turistica Saona.</w:t></w:r></w:p>";
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	@Test
	public void aBehindDocAnchorIsNotFloated() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag) + ": a behindDoc picture was floated, so it displaces the text",
					0, fo(true, flag).getElementsByTagNameNS(FO, "float").getLength());
		}
	}

	/** The same anchor without behindDoc still floats, so the rule is the attribute's. */
	@Test
	public void anOrdinaryWrappedAnchorStillFloats() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag) + ": the wrapped picture should still be an fo:float",
					1, fo(false, flag).getElementsByTagNameNS(FO, "float").getLength());
		}
	}
}
