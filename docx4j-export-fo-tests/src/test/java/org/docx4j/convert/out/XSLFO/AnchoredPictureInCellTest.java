package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A square/tight/through-wrapped anchored picture inside a table cell.
 *
 * <p>FOP does not implement fo:float in a table - it logs "the following feature isn't
 * implemented by Apache FOP, yet: fo:float (on fo:table)" and paints nothing at all -
 * so a wrapped picture in a cell cannot float, and until 17.0.6 it fell through to the
 * top-and-bottom treatment and reserved its full height in the cell.  Where a cell held
 * two of them (a banner and a logo overlaying it) the two reservations stacked and every
 * line of the page moved down by the smaller picture's height.</p>
 *
 * <p>Word's answer, measured on that document's golden: it draws both pictures at their
 * own offsets from the cell's top left - the 495.95 x 98.55pt banner at (56.7, 70.85)
 * and the 108 x 19.5pt logo at (58.2, 81.45), i.e. overlapping - and the row is as tall
 * as the lower of the two plus the paragraph's own line.  So the rule §9.2 already
 * states for text boxes applies to pictures too wherever there is no float to be had:
 * narrower than 60% of the measure (the cell's content width in a cell) the picture is
 * positioned where Word puts it and takes no space; wider than that it reserves its
 * height.  With that, the document's first text line moved from 28.0pt below Word's to
 * 0.8pt, its 57 pages became Word's 56, and the logo landed within 0.05pt of Word.</p>
 *
 * @since 17.0.6
 */
public class AnchoredPictureInCellTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins: a 451.3pt text column. */
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

	/** An anchored, square-wrapped picture cxEmu x cyEmu at the given offsets from the
	 *  column's / paragraph's top left. */
	private static String anchored(String relId, long cxEmu, long cyEmu, long xEmu, long yEmu, int id) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>" + xEmu + "</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>" + yEmu + "</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"" + cxEmu + "\" cy=\"" + cyEmu + "\"/>"
				+ "<wp:wrapSquare wrapText=\"bothSides\"/>"
				+ "<wp:docPr id=\"" + id + "\" name=\"anchor" + id + "\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"" + (100 + id) + "\" name=\"a" + id + "\"/>"
				+ "<pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + cxEmu + "\" cy=\"" + cyEmu + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	/**
	 * One 451.3pt-wide cell holding a paragraph with a full-width picture (495.95pt, in
	 * EMU) and a narrow one (108pt) 10.6pt below the paragraph's top - the shape the
	 * corpus document has.
	 */
	private org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		String cell = "<w:tc><w:tcPr><w:tcW w:w=\"9026\" w:type=\"dxa\"/>"
				+ "<w:tcMar><w:left w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"0\" w:type=\"dxa\"/></w:tcMar>"
				+ "</w:tcPr><w:p>"
				+ anchored(relId, 5588000L, 1251585L, 3175L, 0L, 1)      // 440 x 98.55pt, full width
				+ anchored(relId, 1371600L, 247650L, 19050L, 134620L, 2) // 108 x 19.5pt, 24% of the cell
				+ "<w:r><w:t>text in the cell</w:t></w:r></w:p></w:tc>";

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"9026\" w:type=\"dxa\"/>"
				+ "<w:tblLayout w:type=\"fixed\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9026\"/></w:tblGrid>"
				+ "<w:tr>" + cell + "</w:tr></w:tbl>"
				+ "<w:p><w:r><w:t>after the table</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** The graphic whose content-width is the given value. */
	private static Element graphic(org.w3c.dom.Document doc, String contentWidth) {
		NodeList list = doc.getElementsByTagNameNS(FO, "external-graphic");
		for (int i = 0; i < list.getLength(); i++) {
			Element g = (Element)list.item(i);
			if (contentWidth.equals(g.getAttribute("content-width"))) return g;
		}
		return null;
	}

	private static Element ancestor(Element el, String localName, String attribute) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element)n;
			if (localName.equals(e.getLocalName()) && e.hasAttribute(attribute)) return e;
		}
		return null;
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);

		assertEquals("FOP paints nothing for an fo:float in a table, so none may be emitted there",
				0, doc.getElementsByTagNameNS(FO, "float").getLength());

		Element narrow = graphic(doc, "108pt");
		assertNotNull("the 108pt picture", narrow);
		Element positioned = ancestor(narrow, "block-container", "absolute-position");
		assertNotNull("a picture narrower than 60% of the cell is positioned, not reserved", positioned);
		assertEquals("absolute", positioned.getAttribute("absolute-position"));
		assertEquals("the docx's horizontal offset, from the cell", "1.5pt", positioned.getAttribute("left"));
		assertEquals("the docx's vertical offset, from the paragraph", "10.6pt", positioned.getAttribute("top"));

		Element wide = graphic(doc, "440pt");
		assertNotNull("the 440pt picture", wide);
		assertNull("a picture as wide as the cell reserves its height instead",
				ancestor(wide, "block-container", "absolute-position"));
		Element reserved = ancestor(wide, "block-container", "height");
		assertNotNull("the reserving container", reserved);
		assertEquals("98.55pt", reserved.getAttribute("height"));

		// the positioned container is placed relative to its own zero-height wrapper, so
		// that wrapper has to come before the reservation; otherwise the 98.55pt is added
		// to the 10.6pt offset
		Node zero = positioned.getParentNode();
		assertTrue("the zero-height wrapper", zero instanceof Element
				&& "0pt".equals(((Element)zero).getAttribute("height")));
		Node cursor = zero;
		boolean before = false;
		while ((cursor = cursor.getNextSibling()) != null) {
			if (cursor == reserved) before = true;
		}
		assertTrue("the wrapper which takes no space must precede the one which reserves height",
				before);
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
