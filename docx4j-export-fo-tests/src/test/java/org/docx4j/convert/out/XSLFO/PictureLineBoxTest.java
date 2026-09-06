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
 * A paragraph whose only content is an inline picture is exactly the picture's height in
 * Word (&#xa7;2.4) - <b>even when the paragraph already has a line box of its own</b>.
 *
 * <p>{@code WordLayoutFixups.imageOnlyLineBox} used to skip a block which already carried
 * {@code docx4j:line-box}, which {@code applyBlockLineHeight} very often writes for such a
 * paragraph (from the run the picture sits in, or from the paragraph mark) - so the rule
 * was skipped on exactly the paragraphs it was written for, and the picture's line came
 * out as the paragraph's height <em>plus</em> the picture.  Two measurements:</p>
 *
 * <ul>
 * <li>a 125.04pt picture in a block of {@code line-box="13.799pt"}: every line of the
 * document a flat +4.8pt below Word's (207.0 -> 211.8), x exact to 0.1pt;</li>
 * <li>a 269.68pt diagram in a block of {@code line-box="13.428pt"} and
 * {@code line-height="20.142pt"} (a 1.5 line multiple): the caption after it at y=515.8
 * against Word's 383.2, +132.6pt - almost exactly half the picture, the auto multiple
 * being charged against the <em>picture</em> - and Word's 88 pages came out as 105, with
 * one diagram per page where Word draws two.</li>
 * </ul>
 *
 * @since 17.0.6
 */
public class PictureLineBoxTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** 1587500 EMU = 125pt high, 2540000 = 200pt wide. */
	private static final long CY = 1587500L;
	private static final long CX = 2540000L;
	private static final double HEIGHT_PT = 125.0;

	private static String picture(String relId) {
		return "<w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
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
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing>";
	}

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(200, 125, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 200, 125);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	/**
	 * @param lineTwips w:spacing w:line for the picture's paragraph, 0 for none
	 *        (240 = single, 360 = a 1.5 multiple)
	 */
	private static WordprocessingMLPackage pkg(int lineTwips) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		String pPr = lineTwips == 0 ? ""
				: "<w:pPr><w:spacing w:line=\"" + lineTwips + "\" w:lineRule=\"auto\"/></w:pPr>";

		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				// the picture is in an italic run: that gives it an fo:inline of its own
				// carrying a line-height, which is what made applyBlockLineHeight write a
				// line box for the paragraph and so skip the rule
				+ "<w:p>" + pPr + "<w:r><w:rPr><w:i/></w:rPr>" + picture(relId) + "</w:r></w:p>"
				+ "<w:p><w:r><w:t>the caption below the picture</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
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

	/** The block holding the picture keeps the picture's own height as its line. */
	private void theLineIsThePicture(int lineTwips, int flags) throws Exception {

		org.w3c.dom.Document areaTree = areaTree(pkg(lineTwips), flags);
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		assertTrue("expected the picture line and the caption line, got " + lines.getLength(),
				lines.getLength() >= 2);
		int picture = Integer.parseInt(((Element) lines.item(0)).getAttribute("bpd"));
		assertTrue("the picture's line is " + picture / 1000.0 + "pt, not the picture's own "
				+ HEIGHT_PT + "pt (w:line=" + lineTwips + ")",
				Math.abs(picture - (int) (HEIGHT_PT * 1000)) < 1200);
	}

	@Test
	public void singleSpacedVisitor() throws Exception {
		theLineIsThePicture(0, Docx4J.FLAG_NONE);
	}

	@Test
	public void singleSpacedXslt() throws Exception {
		theLineIsThePicture(0, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A 1.5 line multiple does not make the picture's line 1.5 x the picture. */
	@Test
	public void lineMultipleVisitor() throws Exception {
		theLineIsThePicture(360, Docx4J.FLAG_NONE);
	}

	@Test
	public void lineMultipleXslt() throws Exception {
		theLineIsThePicture(360, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** And the FO says so: the hint is the picture's height, not the run's line box. */
	@Test
	public void theHintIsThePictureHeight() throws Exception {

		NodeList blocks = fo(pkg(360), Docx4J.FLAG_NONE).getElementsByTagNameNS(FO, "block");
		Element pictureBlock = null;
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (b.getElementsByTagNameNS(FO, "external-graphic").getLength() > 0) {
				pictureBlock = b;
				break;
			}
		}
		assertTrue("no block holding the picture", pictureBlock != null);
		assertEquals("docx4j:line-box", HEIGHT_PT,
				pt(pictureBlock.getAttributeNS("http://docx4j.org/fop/word-layout", "line-box")), 0.5);
		assertEquals("docx4j:baseline", HEIGHT_PT,
				pt(pictureBlock.getAttributeNS("http://docx4j.org/fop/word-layout", "baseline")), 0.5);
	}

	private static double pt(String v) {
		assertTrue("no value", v != null && v.length() > 0);
		if (v.endsWith("pt")) v = v.substring(0, v.length() - 2);
		return Double.parseDouble(v);
	}
}
