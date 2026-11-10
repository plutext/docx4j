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
 * A picture Word draws <em>behind</em> the text ({@code wp:anchor/@behindDoc="1"}) is wrapped as
 * one in front of it is: behindDoc says where it is painted, its wrap element how the text goes
 * round it.  Measured on CR-080 phase 4's wrap probes (Word 16.0.20326, mode 15: fw10-13 and
 * fw11-13 lay their paragraphs out line for line as fw10-01 and fw11-01, in front); and on the
 * mode 14 document the rule of 17.0.6 to 17.3.6 was read from, which said such a picture
 * displaces nothing: Word centres its anchor paragraph's heading in the room beside the
 * 155.25pt logo, and its caption, which docx4j had set beside the logo, lies below the logo's
 * foot.  The caption had moved because the logo's offset, 22.85pt above its paragraph, was
 * left out: such a picture is now drawn where its offset puts it, and only the part of its band
 * below the paragraph's top is reserved.
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
		return anchoredPicture(relId, behindDoc, 0);
	}

	private static String anchoredPicture(String relId, boolean behindDoc, long offsetV) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"" + (behindDoc ? "1" : "0") + "\""
				+ " locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"margin\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>" + offsetV + "</wp:posOffset></wp:positionV>"
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
		return fo(behindDoc, 0, flags);
	}

	private static org.w3c.dom.Document fo(boolean behindDoc, long offsetV, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String body = "<w:p>" + anchoredPicture(img.getSourceRelationship().getId(), behindDoc, offsetV)
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

	/** Behind the text, a picture wrapped square is floated as it is in front: the text beside it.  @since 17.3.7 */
	@Test
	public void aBehindDocAnchorIsFloatedAsOneInFront() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag) + ": a behindDoc picture wrapped square should be an fo:float",
					1, fo(true, flag).getElementsByTagNameNS(FO, "float").getLength());
		}
	}

	@Test
	public void anOrdinaryWrappedAnchorFloats() throws Exception {
		for (int flag : FLAGS) {
			assertEquals(flagName(flag) + ": the wrapped picture should be an fo:float",
					1, fo(false, flag).getElementsByTagNameNS(FO, "float").getLength());
		}
	}

	/** 4253's logo: 75pt tall, 22.85pt above its paragraph's top (-290195 EMU), behind the text, wrapped square.
	 *  The picture is positioned at its offset, out of the flow, and the float beside the text holds only the band
	 *  below the paragraph's top, 75 - 22.85 = 52.15pt, empty.  @since 17.3.7 */
	@Test
	public void aPictureAboveItsParagraphIsDrawnThereItsBandBelowReserved() throws Exception {
		for (int flag : FLAGS) {
			org.w3c.dom.Document doc = fo(true, -290195, flag);
			org.w3c.dom.NodeList floats = doc.getElementsByTagNameNS(FO, "float");
			assertEquals(flagName(flag), 1, floats.getLength());
			org.w3c.dom.Element fl = (org.w3c.dom.Element) floats.item(0);
			assertEquals(flagName(flag) + ": the band holds no picture", 0, fl.getElementsByTagNameNS(FO, "external-graphic").getLength());
			org.w3c.dom.NodeList boxes = fl.getElementsByTagNameNS(FO, "block-container");
			assertEquals(flagName(flag), 1, boxes.getLength());
			assertEquals(flagName(flag), 52.15, Double.parseDouble(((org.w3c.dom.Element) boxes.item(0)).getAttribute("height").replace("pt", "")), 0.01);
			org.w3c.dom.Element graphic = (org.w3c.dom.Element) doc.getElementsByTagNameNS(FO, "external-graphic").item(0);
			org.w3c.dom.Element abs = (org.w3c.dom.Element) graphic.getParentNode().getParentNode();
			assertEquals(flagName(flag), "absolute", abs.getAttribute("absolute-position"));
			assertEquals(flagName(flag), -22.85, Double.parseDouble(abs.getAttribute("top").replace("pt", "")), 0.01);
		}
	}
}
