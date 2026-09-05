package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
 * An absolutely positioned VML picture (w:pict/v:shape with position:absolute holding a
 * v:imagedata) is placed where Word places it, not rendered inline at the end of its
 * paragraph.  FOPictWriterAbstract said so in its own header comment: only v:textbox
 * was handled, and a picture fell through to the legacy inline path whatever its
 * @style.  Measured against Word's own PDF of a real document: a first-page header
 * holding a 66pt picture at position:absolute plus ten right-aligned address lines had
 * Word's first header line at y=34.3 x=510.5, docx4j's at y=94.3 x=255.1 - the picture
 * had taken a 66pt line in the flow and pushed the whole header down and out of its
 * alignment.
 *
 * <p>A picture whose shape has no position is still laid out inline, as Word does.</p>
 *
 * @since 17.0.6
 */
public class VmlPictureAnchorTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

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

	private static String pict(String relId, String style) {
		return "<w:r><w:pict xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<v:shapetype id=\"_x0000_t75\" o:spt=\"75\" coordsize=\"21600,21600\""
				+ " path=\"m@4@5l@4@11@9@11@9@5xe\"><v:stroke joinstyle=\"miter\"/></v:shapetype>"
				+ "<v:shape id=\"_x0000_s2049\" type=\"#_x0000_t75\" style=\"" + style + "\">"
				+ "<v:imagedata r:id=\"" + relId + "\" o:title=\"logo\"/>"
				+ "</v:shape></w:pict></w:r>";
	}

	private org.w3c.dom.Document fo(String style, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p>" + pict(relId, style) + "<w:r><w:t>beside the picture</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private Element graphic(org.w3c.dom.Document doc) {
		NodeList list = doc.getElementsByTagNameNS(FO, "external-graphic");
		assertEquals("one picture", 1, list.getLength());
		return (Element)list.item(0);
	}

	/** the nearest ancestor of the picture which is an absolutely positioned container */
	private static Element positionedAncestor(Element el) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element)n;
			if ("block-container".equals(e.getLocalName()) && e.hasAttribute("absolute-position")) return e;
		}
		return null;
	}

	private void check(int flags) throws Exception {

		Element positioned = graphic(fo("position:absolute;margin-left:-2.85pt;margin-top:.3pt;"
				+ "width:198.75pt;height:66pt;z-index:251657728", flags));
		Element container = positionedAncestor(positioned);
		assertNotNull("an absolutely positioned VML picture must not be laid out inline", container);
		assertEquals("absolute", container.getAttribute("absolute-position"));
		assertEquals("the shape's margin-left, from the column's left edge", "-2.85pt", container.getAttribute("left"));
		assertEquals("the shape's margin-top, from the paragraph", "0.3pt", container.getAttribute("top"));
		assertEquals("198.75pt", positioned.getAttribute("content-width"));
		assertEquals("66pt", positioned.getAttribute("content-height"));

		Element inline = graphic(fo("width:84.75pt;height:28.5pt", flags));
		assertTrue("a picture the shape lays out in the line stays inline",
				positionedAncestor(inline) == null);
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
