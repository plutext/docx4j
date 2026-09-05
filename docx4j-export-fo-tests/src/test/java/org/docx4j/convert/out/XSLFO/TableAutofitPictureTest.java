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
 * Word's autofit sizes a column to whatever is in it, pictures included.  docx4j
 * measured only text, so a cell holding nothing but a picture measured zero and its
 * column collapsed to the cell margins - and in a table where every w:tcW is auto,
 * the columns which do hold text then took the whole width and wrapped one word per
 * line.  Measured on a real document (a four-column letterhead with a logo in the
 * first column and another picture in the last), that turned three pages into 28.
 *
 * @since 17.0.5
 */
public class TableAutofitPictureTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final String AUTO_W = "<w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr>";

	/** an inline picture 80pt wide (1016000 EMU), 20pt high */
	private static String picture(String relId) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:inline><wp:extent cx=\"1016000\" cy=\"254000\"/>"
				+ "<wp:docPr id=\"1\" name=\"pic\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"pic\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1016000\" cy=\"254000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	/** a small PNG, so the package has a real image part to point at */
	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(40, 10, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 40, 10);
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

		String row = "<w:tr>"
				+ "<w:tc>" + AUTO_W + "<w:p>" + picture(relId) + "</w:p></w:tc>"
				+ "<w:tc>" + AUTO_W + "<w:p><w:r><w:t>Booked By:</w:t></w:r></w:p></w:tc>"
				+ "<w:tc>" + AUTO_W + "<w:p><w:r><w:t>Some rather longer cell content here</w:t></w:r></w:p></w:tc>"
				+ "<w:tc>" + AUTO_W + "<w:p>" + picture(relId) + "</w:p></w:tc>"
				+ "</w:tr>";

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:tbl>"
				+ "<w:tblPr><w:tblW w:w=\"8691\" w:type=\"dxa\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"1584\"/><w:gridCol w:w=\"953\"/>"
				+ "<w:gridCol w:w=\"4448\"/><w:gridCol w:w=\"1706\"/></w:tblGrid>"
				+ row + "</w:tbl>" + SECT_PR + "</w:body></w:document>"));
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
		return Double.parseDouble(v.substring(0, v.length() - 2));
	}

	private void pictureColumnsAreNotCollapsed(int flags) throws Exception {
		NodeList cols = fo(flags).getElementsByTagNameNS(FO, "table-column");
		assertEquals(4, cols.getLength());
		double first = lengthPt(((Element) cols.item(0)).getAttribute("column-width"));
		double last = lengthPt(((Element) cols.item(3)).getAttribute("column-width"));
		assertTrue("the picture's column collapsed to the cell margins: " + first + "pt",
				first >= 80);
		assertTrue("the picture's column collapsed to the cell margins: " + last + "pt",
				last >= 80);
	}

	@Test
	public void visitor() throws Exception {
		pictureColumnsAreNotCollapsed(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		pictureColumnsAreNotCollapsed(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** and the text column is then wide enough not to wrap one word per line:
	 *  one line per cell, four cells */
	@Test
	public void theTextColumnStillFits() throws Exception {
		assertEquals(4, lineCount(areaTree(pkg(), Docx4J.FLAG_NONE)));
	}
}
