package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
 * A picture Word wraps text around becomes an fo:float, and the float has to be a
 * direct child of the fo:flow.
 *
 * <p>FOP throws NullPointerException from TraitSetter.setVisibility - called with the
 * null curBlockArea of a BlockLayoutManager that produced no area - whenever a float
 * nested inside an fo:block shares a flow with an fo:block nested inside an fo:inline.
 * That second shape is how a line break inside a run reaches the FO (BrWriter emits a
 * block with linefeed-treatment="preserve"), so the combination is common: three
 * documents of a 157 document corpus failed to export at all because of it.  The
 * float is laid out correctly when it is a direct child of the flow, so
 * WordLayoutFixups moves it there - but only where the crash is in prospect, since the
 * paragraph is where the picture measures closest to Word.</p>
 *
 * @since 17.0.6
 */
public class AnchoredPictureFloatTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** a small PNG, so the package has a real image part to point at */
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

	/** an anchored picture, 100 x 75pt, square wrap, at the right of the column */
	private static String anchoredPicture(String relId) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"margin\"><wp:align>right</wp:align></wp:positionH>"
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

	private static final String PROSE = "The quick brown fox jumps over the lazy dog, and then some more "
			+ "words so that this paragraph is long enough to have to wrap beside the picture. ";

	/**
	 * A paragraph carrying the anchored picture, and - the second half of the crash -
	 * a paragraph whose run holds a line break, which is emitted as an fo:block inside
	 * an fo:inline.
	 */
	private static WordprocessingMLPackage pkg() throws Exception {
		return pkg(false);
	}

	/** @param breakFirst put the paragraph holding the line break before the picture's */
	private static WordprocessingMLPackage pkg(boolean breakFirst) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		String lineBreak = "<w:p><w:r><w:t>before the break</w:t><w:br/>"
				+ "<w:t>after the break</w:t></w:r></w:p>";
		StringBuilder body = new StringBuilder();
		if (breakFirst) body.append(lineBreak);
		body.append("<w:p>").append(anchoredPicture(relId))
			.append("<w:r><w:t>").append(PROSE).append("</w:t></w:r></w:p>");
		if (!breakFirst) body.append(lineBreak);
		for (int i = 0; i < 8; i++) {
			body.append("<w:p><w:r><w:t>").append(PROSE).append(PROSE).append("</w:t></w:r></w:p>");
		}

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		return fo(pkg(), flags);
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element theFloat(org.w3c.dom.Document doc) {
		NodeList floats = doc.getElementsByTagNameNS(FO, "float");
		assertEquals("the wrapped picture should be an fo:float", 1, floats.getLength());
		return (Element)floats.item(0);
	}

	private static boolean blockInsideInline(org.w3c.dom.Document doc) {
		NodeList blocks = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			org.w3c.dom.Node parent = blocks.item(i).getParentNode();
			if (parent instanceof Element && "inline".equals(parent.getLocalName())) return true;
		}
		return false;
	}

	@Test
	public void floatIsAtFlowLevelVisitor() throws Exception {
		// the visitor pathway puts the line break's block inside the run's fo:inline,
		// which is the shape that crashes FOP when a float shares the flow with it
		org.w3c.dom.Document doc = fo(Docx4J.FLAG_NONE);
		assertTrue("no fo:block inside an fo:inline - the test no longer covers the crash",
				blockInsideInline(doc));
		assertEquals("an fo:float nested in a block crashes FOP; it belongs to the flow",
				"flow", ((Element)theFloat(doc).getParentNode()).getLocalName());
	}

	/**
	 * The XSLT pathway emits the line break's block as a sibling of the run's inlines,
	 * not inside one, and FOP survives that - so the float is left where the paragraph
	 * put it, which measures closer to Word (the image-anchored probe).
	 */
	@Test
	public void floatIsLeftInTheParagraphXslt() throws Exception {
		org.w3c.dom.Document doc = fo(Docx4J.FLAG_EXPORT_PREFER_XSL);
		assertTrue(!blockInsideInline(doc));
		assertEquals("block", ((Element)theFloat(doc).getParentNode()).getLocalName());
		assertTrue("nothing was laid out",
				lineCount(areaTree(pkg(), Docx4J.FLAG_EXPORT_PREFER_XSL)) > 10);
	}

	/**
	 * A line break before the float lays out in FOP as it is, and the float is left in
	 * its paragraph: only a block inside an inline which follows the float crashes, and
	 * the paragraph is where the picture measures closest to Word.
	 */
	@Test
	public void aLineBreakBeforeTheFloatLeavesItInTheParagraph() throws Exception {
		org.w3c.dom.Document doc = fo(pkg(true), Docx4J.FLAG_NONE);
		assertTrue(blockInsideInline(doc));
		assertEquals("block", ((Element)theFloat(doc).getParentNode()).getLocalName());
		assertTrue("nothing was laid out", lineCount(areaTree(pkg(true), Docx4J.FLAG_NONE)) > 10);
	}

	/** The document lays out: before the fix FOP threw NullPointerException here. */
	@Test
	public void theDocumentLaysOut() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(), Docx4J.FLAG_NONE);
		assertTrue("nothing was laid out", lineCount(areaTree) > 10);
		assertNotNull(areaTree.getElementsByTagName("pageViewport").item(0));
		// and the float did its job: the lines beside the picture are held off the
		// right edge by it (FOP's area tree does not serialise the float's own areas)
		int intruded = 0;
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			String endIndent = ((Element)lines.item(i)).getAttribute("end-indent");
			if (endIndent.length() > 0 && Integer.parseInt(endIndent) > 100000) intruded++;
		}
		assertTrue("no line was pushed aside by the float", intruded > 0);
	}
}
