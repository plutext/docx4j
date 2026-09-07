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
 * An anchored picture in a multi-column section (CR-001 &#xa7;9.1).
 *
 * <p>FOP drops an {@code fo:float} in a multi-column region silently - neither the
 * picture nor the indent beside it is painted - so a wrapped picture there takes the
 * no-float treatment: positioned where Word puts it when it is narrower than 60% of the
 * <b>column</b>, reserving its height otherwise.  The share was measured against the
 * section's whole text column until 17.1.0, which is twice the measure Word uses.
 * Measured on a landscape two-column document (columns 360.675pt): {@code mutool draw -F
 * trace} counts two pictures on Word's page 1 and one on ours - the 87.75 x 48pt logo's
 * float was never painted - and a 340.15pt picture anchored 406.0pt from the margin, ie
 * in column 2, reserved 278.4pt at the head of column 1, putting the title at y=323.0
 * against Word's 37.0.</p>
 *
 * @since 17.1.0
 */
public class MultiColumnAnchoredPictureTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 landscape, two columns with a 35.45pt gap: columns of 360.675pt. */
	private static final String TWO_COLUMNS = "<w:sectPr><w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
			+ "<w:pgMar w:top=\"720\" w:right=\"851\" w:bottom=\"720\" w:left=\"851\"/>"
			+ "<w:cols w:num=\"2\" w:space=\"709\"/></w:sectPr>";

	/** the same page, one column. */
	private static final String ONE_COLUMN = "<w:sectPr><w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
			+ "<w:pgMar w:top=\"720\" w:right=\"851\" w:bottom=\"720\" w:left=\"851\"/></w:sectPr>";

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

	/** @param cx,cy the extent in EMU; @param xEmu the offset from the column */
	private static String anchoredPicture(String relId, long cx, long cy, long xEmu) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>" + xEmu + "</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"" + cx + "\" cy=\"" + cy + "\"/>"
				+ "<wp:wrapSquare wrapText=\"bothSides\"/>"
				+ "<wp:docPr id=\"1\" name=\"anchor1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"101\" name=\"anchor1\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	private static final String PROSE = "The quick brown fox jumps over the lazy dog, and then some more "
			+ "words so that this paragraph is long enough to wrap beside the picture. ";

	/** 87.75 x 48pt (a logo), at 1.7pt from the column. */
	private static WordprocessingMLPackage pkg(String sectPr) throws Exception {
		return pkg(sectPr, 1114425L, 609600L, 21590L);
	}

	private static WordprocessingMLPackage pkg(String sectPr, long cx, long cy, long xEmu) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();
		StringBuilder body = new StringBuilder();
		body.append("<w:p>").append(anchoredPicture(relId, cx, cy, xEmu))
			.append("<w:r><w:t>").append(PROSE).append("</w:t></w:r></w:p>");
		for (int i = 0; i < 6; i++) {
			body.append("<w:p><w:r><w:t>").append(PROSE).append("</w:t></w:r></w:p>");
		}
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + sectPr + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static int count(org.w3c.dom.Document doc, String localName) {
		return doc.getElementsByTagNameNS(FO, localName).getLength();
	}

	/** The innermost block-container the picture was lifted into (a positioned one sits
	 *  inside a zero-height wrapper, which holds the graphic too). */
	private static Element pictureContainer(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block-container");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getElementsByTagNameNS(FO, "external-graphic").getLength() > 0) found = el;
		}
		return found;
	}

	private void check(int flags) throws Exception {

		// one column: the picture is a float, as it has always been
		org.w3c.dom.Document single = fo(pkg(ONE_COLUMN), flags);
		assertEquals("a float in a single-column section", 1, count(single, "float"));

		// two columns: no float (FOP would paint nothing), and the picture - 87.75pt
		// against a 360.675pt column - is positioned where Word puts it
		org.w3c.dom.Document columns = fo(pkg(TWO_COLUMNS), flags);
		assertEquals("no float in a multi-column region", 0, count(columns, "float"));
		Element positioned = pictureContainer(columns);
		assertNotNull(positioned);
		assertEquals("absolute", positioned.getAttribute("absolute-position"));
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * A picture anchored beyond the first column's width sits in a later column, and
	 * reserves nothing where its anchor is: 340.15 x 246.75pt at 406.0pt from a
	 * 360.675pt column.
	 */
	@Test
	public void aPictureInTheNextColumnReservesNothing() throws Exception {
		org.w3c.dom.Document doc = fo(pkg(TWO_COLUMNS, 4319905L, 3133725L, 5155565L), Docx4J.FLAG_NONE);
		assertEquals(0, count(doc, "float"));
		Element container = pictureContainer(doc);
		assertNotNull(container);
		assertEquals("absolute", container.getAttribute("absolute-position"));
		Element wrapper = (Element) container.getParentNode();
		assertEquals("the wrapper reserves no height", "0pt", wrapper.getAttribute("height"));
	}

	/** The same picture in a single-column section is a float, as before: there is room
	 *  for text beside it in a 757.4pt column and FOP lays a float out there. */
	@Test
	public void theSamePictureInOneColumnIsStillAFloat() throws Exception {
		org.w3c.dom.Document doc = fo(pkg(ONE_COLUMN, 4319905L, 3133725L, 5155565L), Docx4J.FLAG_NONE);
		assertEquals(1, count(doc, "float"));
	}
}
