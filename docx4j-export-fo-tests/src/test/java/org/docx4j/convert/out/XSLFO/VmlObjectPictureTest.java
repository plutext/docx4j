package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An embedded object's preview picture is drawn, in both XSL-FO pathways.
 *
 * <p>Word writes an embedded object as a {@code w:object} holding the VML shape whose
 * {@code v:imagedata} points at the picture it draws for the object - an Equation
 * Editor 3 or MathType equation (usually a WMF or EMF), the thumbnail of an embedded
 * workbook or Visio drawing, or the icon of an object shown as an icon.  Until 17.0.6
 * <em>neither</em> FO exporter matched {@code w:object}: nothing in
 * {@code AbstractVisitorExporterGenerator} and no template in {@code docx2fo.xslt}, so
 * the object reached the visitor's catch-all ("Need to handle
 * org.docx4j.wml.CTObject") or the XSLT's {@code match="*"} notImplemented, and the
 * preview was lost - equations vanished from the PDF.  A document which wrapped the
 * same preview in {@code w:pict} rendered fine, which is what hid it (CR-011 phase 2).</p>
 *
 * <p>The fix routes {@code w:object} through the same VML picture path, so the preview
 * gets the metafile renderer (SVG in an {@code fo:instream-foreign-object}), bitmap
 * previews get the {@code fo:external-graphic} they always got, the VML style supplies
 * the size, and a {@code position:absolute} shape is placed where Word places it.</p>
 *
 * @since 17.0.6
 */
public class VmlObjectPictureTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG = "http://www.w3.org/2000/svg";

	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT") + ": ";
	}

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	// ------------------------------------------------------------------ fixtures

	/** a small PNG, so a package can point at a bitmap preview */
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

	private static byte[] metafile(String name) throws Exception {
		try (InputStream is = VmlObjectPictureTest.class.getResourceAsStream("/metafiles/" + name)) {
			return org.apache.commons.io.IOUtils.toByteArray(is);
		}
	}

	/**
	 * The w:object Word writes for an embedded object: the VML shape carrying the
	 * preview, and the o:OLEObject naming the object itself (which is not rendered -
	 * Word draws the preview, not a placeholder).
	 */
	private static String object(String relId, String style) {
		return "<w:object w:dxaOrig=\"4000\" w:dyaOrig=\"3000\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<v:shapetype id=\"_x0000_t75\" o:spt=\"75\" coordsize=\"21600,21600\""
				+ " path=\"m@4@5l@4@11@9@11@9@5xe\"><v:stroke joinstyle=\"miter\"/></v:shapetype>"
				+ "<v:shape id=\"_x0000_i1025\" type=\"#_x0000_t75\" o:ole=\"\" style=\"" + style + "\">"
				+ "<v:imagedata r:id=\"" + relId + "\" o:title=\"\"/>"
				+ "</v:shape>"
				+ "<o:OLEObject Type=\"Embed\" ProgID=\"Equation.3\" ShapeID=\"_x0000_i1025\""
				+ " DrawAspect=\"Content\" ObjectID=\"_1234567890\" r:id=\"" + relId + "\"/>"
				+ "</w:object>";
	}

	/** A w:object whose shape has no v:imagedata at all (an inline control, say). */
	private static String objectWithoutImageData() {
		return "<w:object w:dxaOrig=\"1520\" w:dyaOrig=\"680\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\">"
				+ "<v:shape id=\"_x0000_i1030\" type=\"#_x0000_t75\" style=\"width:76pt;height:34pt\"/>"
				+ "</w:object>";
	}

	private static WordprocessingMLPackage pkg(byte[] image, String style) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = BinaryPartAbstractImage.createImagePart(pkg, image)
				.getSourceRelationship().getId();
		return body(pkg, "<w:p><w:r>" + object(relId, style) + "</w:r>"
				+ "<w:r><w:t>beside the object</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>text below the object</w:t></w:r></w:p>");
	}

	/** The same document with the w:object left out: the ink baseline. */
	private static WordprocessingMLPackage textOnlyPkg() throws Exception {
		return body(WordprocessingMLPackage.createPackage(),
				"<w:p><w:r><w:t>beside the object</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>text below the object</w:t></w:r></w:p>");
	}

	private static WordprocessingMLPackage body(WordprocessingMLPackage pkg, String body)
			throws Exception {
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	// ------------------------------------------------------------------ conversion

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg); // builds the fop config from the fonts in use
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static byte[] pdf(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toByteArray();
	}

	private static String pdfText(byte[] pdfBytes) throws Exception {
		try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
			return new org.apache.pdfbox.text.PDFTextStripper().getText(d);
		}
	}

	/** Fraction of the rendered page that is not white. */
	private static double ink(byte[] pdfBytes) throws Exception {
		try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
			java.awt.image.BufferedImage img =
					new org.apache.pdfbox.rendering.PDFRenderer(d).renderImageWithDPI(0, 72);
			long n = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					if ((img.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) n++;
				}
			}
			return n / (double)(img.getWidth() * (long)img.getHeight());
		}
	}

	/** The fo:instream-foreign-object elements which hold an svg:svg. */
	private static java.util.List<Element> svgPictures(org.w3c.dom.Document doc) {
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		NodeList ifos = doc.getElementsByTagNameNS(FO, "instream-foreign-object");
		for (int i = 0; i < ifos.getLength(); i++) {
			Element ifo = (Element)ifos.item(i);
			for (Node n = ifo.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element && SVG.equals(n.getNamespaceURI())
						&& "svg".equals(n.getLocalName())) {
					out.add(ifo);
					break;
				}
			}
		}
		return out;
	}

	/** the nearest ancestor of the picture which is an absolutely positioned container */
	private static Element positionedAncestor(Element el) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element)n;
			if ("block-container".equals(e.getLocalName()) && e.hasAttribute("absolute-position")) return e;
		}
		return null;
	}

	// ------------------------------------------------------------------ tests

	/**
	 * The usual equation case: the preview is a WMF, so it is drawn as vectors in an
	 * fo:instream-foreign-object (CR-011's metafile renderer), at the size the VML
	 * style declares.
	 */
	@Test
	public void aMetafilePreviewIsDrawnAsSvg() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			byte[] wmf = metafile("60677.wmf");
			org.w3c.dom.Document doc = fo(pkg(wmf, "width:200pt;height:150pt"), flags);

			java.util.List<Element> pictures = svgPictures(doc);
			assertEquals(impl + "the object's preview should be SVG in an fo:instream-foreign-object",
					1, pictures.size());
			assertEquals(impl + "nothing should be left as an fo:external-graphic",
					0, doc.getElementsByTagNameNS(FO, "external-graphic").getLength());

			Element ifo = pictures.get(0);
			assertEquals(impl + "the size comes from the VML style, as for w:pict",
					"200pt", ifo.getAttribute("content-width"));
			assertEquals(impl + "the size comes from the VML style, as for w:pict",
					"150pt", ifo.getAttribute("content-height"));

			byte[] pdf = pdf(pkg(wmf, "width:200pt;height:150pt"), flags);
			double painted = ink(pdf) - ink(pdf(textOnlyPkg(), flags));
			assertTrue(impl + "the object preview was not painted, ink over the text alone="
					+ painted, painted > 0.002);
			assertTrue(impl + "the text beside the object was lost",
					pdfText(pdf).contains("beside the object"));
		}
	}

	/** A bitmap preview is the fo:external-graphic it would be in a w:pict. */
	@Test
	public void aBitmapPreviewIsAnExternalGraphic() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			byte[] png = png();
			org.w3c.dom.Document doc = fo(pkg(png, "width:84.75pt;height:28.5pt"), flags);

			NodeList graphics = doc.getElementsByTagNameNS(FO, "external-graphic");
			assertEquals(impl + "the object's preview should be one fo:external-graphic",
					1, graphics.getLength());
			Element graphic = (Element)graphics.item(0);
			assertEquals(impl + "the size comes from the VML style",
					"84.75pt", graphic.getAttribute("content-width"));
			assertEquals(impl + "the size comes from the VML style",
					"28.5pt", graphic.getAttribute("content-height"));
			assertTrue(impl + "a bitmap preview must not become SVG", svgPictures(doc).isEmpty());
			assertTrue(impl + "an inline object should stay in the line",
					positionedAncestor(graphic) == null);

			double painted = ink(pdf(pkg(png, "width:84.75pt;height:28.5pt"), flags))
					- ink(pdf(textOnlyPkg(), flags));
			assertTrue(impl + "the object preview was not painted, ink over the text alone="
					+ painted, painted > 0.002);
		}
	}

	/**
	 * A w:object whose shape is position:absolute is placed where Word places it - the
	 * same fo:block-container an absolutely positioned w:pict gets - instead of taking
	 * a line at the end of its paragraph.
	 */
	@Test
	public void aPositionedObjectIsAnchored() throws Exception {
		String style = "position:absolute;margin-left:-2.85pt;margin-top:.3pt;"
				+ "width:198.75pt;height:66pt;z-index:251657728";
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			org.w3c.dom.Document doc = fo(pkg(png(), style), flags);

			NodeList graphics = doc.getElementsByTagNameNS(FO, "external-graphic");
			assertEquals(impl + "one picture", 1, graphics.getLength());
			Element graphic = (Element)graphics.item(0);

			Element container = positionedAncestor(graphic);
			assertNotNull(impl + "an absolutely positioned object must not be laid out inline", container);
			assertEquals(impl + "absolute", "absolute", container.getAttribute("absolute-position"));
			assertEquals(impl + "the shape's margin-left, from the column's left edge",
					"-2.85pt", container.getAttribute("left"));
			assertEquals(impl + "the shape's margin-top, from the paragraph",
					"0.3pt", container.getAttribute("top"));
			assertEquals(impl + "198.75pt", "198.75pt", graphic.getAttribute("content-width"));
			assertEquals(impl + "66pt", "66pt", graphic.getAttribute("content-height"));
		}
	}

	/**
	 * A w:object with no v:imagedata has no preview to draw, so nothing is emitted for
	 * it - not a "[OLE object]" placeholder, which is not what Word shows either.  The
	 * rest of the document is unaffected.
	 */
	@Test
	public void anObjectWithoutAPreviewEmitsNothing() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			WordprocessingMLPackage pkg = body(WordprocessingMLPackage.createPackage(),
					"<w:p><w:r>" + objectWithoutImageData() + "</w:r>"
					+ "<w:r><w:t>beside the object</w:t></w:r></w:p>");
			org.w3c.dom.Document doc = fo(pkg, flags);

			assertEquals(impl + "there is no picture to draw",
					0, doc.getElementsByTagNameNS(FO, "external-graphic").getLength());
			assertTrue(impl + "there is no picture to draw", svgPictures(doc).isEmpty());
			assertTrue(impl + "the text beside the object was lost",
					doc.getDocumentElement().getTextContent().contains("beside the object"));
		}
	}
}
