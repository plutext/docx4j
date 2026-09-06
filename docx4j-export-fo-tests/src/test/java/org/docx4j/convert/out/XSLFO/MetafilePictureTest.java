package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
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
import org.w3c.dom.NodeList;

/**
 * A WMF / EMF / EMF+ picture is drawn into the PDF as vectors: docx4j replays the
 * metafile's GDI records onto Batik's SVGGraphics2D and puts the SVG in an
 * {@code fo:instream-foreign-object}, which FOP paints.
 *
 * <p>Before 17.0.6 the FO named the {@code .wmf}/{@code .emf} in an
 * {@code fo:external-graphic}: FOP has a loader for neither (Batik's WMF loader
 * covers some WMF), so the picture was drawn as nothing at all, or - with
 * {@code WordLayoutFixups.reserveUnpaintablePictures} - as a transparent placeholder
 * holding the space open.  Those remain, for a metafile the renderer cannot draw.
 * See CR-011.</p>
 *
 * <p>Every assertion is run against both pathways, the visitor
 * ({@code FLAG_NONE}) and the XSLT one ({@code FLAG_EXPORT_PREFER_XSL}); the two
 * serialize the SVG differently and one of them (Xalan) drops namespace
 * declarations, which is the kind of difference this pairing exists to catch.</p>
 *
 * @since 17.0.6
 */
public class MetafilePictureTest extends AbstractXSLFOTest {

	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG = "http://www.w3.org/2000/svg";
	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT") + ": ";
	}

	// ------------------------------------------------------------------ fixtures

	private static byte[] metafile(String name) throws Exception {
		try (InputStream is = MetafilePictureTest.class.getResourceAsStream("/metafiles/" + name)) {
			return org.apache.commons.io.IOUtils.toByteArray(is);
		}
	}

	private static WordprocessingMLPackage sample(String name) throws Exception {
		try (InputStream is = MetafilePictureTest.class.getResourceAsStream("/metafiles/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	/** A package holding one inline picture of the given metafile, at 200 x 150pt. */
	private static WordprocessingMLPackage inlinePkg(String metafile) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = BinaryPartAbstractImage.createImagePart(pkg, metafile(metafile))
				.getSourceRelationship().getId();
		String body = "<w:p><w:r>" + drawing(relId, false) + "</w:r></w:p>"
				+ "<w:p><w:r><w:t>text below the picture</w:t></w:r></w:p>";
		return withBody(pkg, body);
	}

	/** A package holding one <em>anchored</em> (floating) picture of the given metafile. */
	private static WordprocessingMLPackage anchoredPkg(String metafile) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = BinaryPartAbstractImage.createImagePart(pkg, metafile(metafile))
				.getSourceRelationship().getId();
		StringBuilder body = new StringBuilder();
		body.append("<w:p><w:r>").append(drawing(relId, true)).append("</w:r>")
			.append("<w:r><w:t>").append(PROSE).append("</w:t></w:r></w:p>");
		for (int i = 0; i < 6; i++) {
			body.append("<w:p><w:r><w:t>").append(PROSE).append("</w:t></w:r></w:p>");
		}
		return withBody(pkg, body.toString());
	}

	/**
	 * A package holding a VML {@code w:pict} whose {@code v:imagedata} points at the
	 * metafile - the shape Word writes for an embedded object's preview (an Equation
	 * Editor or MathType equation, a pasted Excel range, an OLE icon).
	 *
	 * <p>Word wraps such a preview in {@code w:object} as often as in {@code w:pict}.
	 * This covers the {@code w:pict} form; {@code w:object}, which neither FO exporter
	 * matched until 17.0.6, is covered by {@link VmlObjectPictureTest}.  The metafile
	 * pipeline is the same one.</p>
	 */
	private static WordprocessingMLPackage vmlPkg(String metafile) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = BinaryPartAbstractImage.createImagePart(pkg, metafile(metafile))
				.getSourceRelationship().getId();
		String body = "<w:p><w:r><w:pict xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<v:shape id=\"_x0000_i1025\" type=\"#_x0000_t75\" style=\"width:200pt;height:150pt\">"
				+ "<v:imagedata r:id=\"" + relId + "\" o:title=\"\"/></v:shape>"
				+ "</w:pict></w:r></w:p>";
		return withBody(pkg, body);
	}

	/**
	 * A package holding one inline picture whose part is added by hand.
	 *
	 * <p>{@code BinaryPartAbstractImage.createImagePart} sniffs the bytes and, finding
	 * no image it recognises, tries to convert them with ImageMagick - which is not what
	 * a docx already holding a broken metafile does.  Word stored the bytes; so does
	 * this.</p>
	 */
	private static WordprocessingMLPackage rawWmfPkg(String metafile) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart part =
				new org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart(
						new org.docx4j.openpackaging.parts.PartName("/word/media/image99.wmf"));
		part.setBinaryData(metafile(metafile));
		String relId = pkg.getMainDocumentPart().addTargetPart(part).getId();
		String body = "<w:p><w:r>" + drawing(relId, false) + "</w:r></w:p>"
				+ "<w:p><w:r><w:t>text below the picture</w:t></w:r></w:p>";
		return withBody(pkg, body);
	}

	private static final String PROSE = "The quick brown fox jumps over the lazy dog, and again, "
			+ "so that the paragraph is long enough to wrap beside the picture. ";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage withBody(WordprocessingMLPackage pkg, String body)
			throws Exception {
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/** 200 x 150pt = 2540000 x 1905000 EMU */
	private static String drawing(String relId, boolean anchored) {
		String frame = anchored
				? "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
					+ " relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
					+ "<wp:simplePos x=\"0\" y=\"0\"/>"
					+ "<wp:positionH relativeFrom=\"margin\"><wp:align>right</wp:align></wp:positionH>"
					+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
					+ "<wp:extent cx=\"2540000\" cy=\"1905000\"/><wp:wrapSquare wrapText=\"bothSides\"/>"
					+ "<wp:docPr id=\"1\" name=\"p1\"/>"
				: "<wp:inline><wp:extent cx=\"2540000\" cy=\"1905000\"/><wp:docPr id=\"1\" name=\"p1\"/>";
		return "<w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ frame
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"9\" name=\"p1\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2540000\" cy=\"1905000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic>"
				+ (anchored ? "</wp:anchor>" : "</wp:inline>")
				+ "</w:drawing>";
	}

	// ------------------------------------------------------------------ conversion

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static byte[] pdf(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg); // builds the fop config from the fonts in use
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toByteArray();
	}

	/** The pictures: fo:instream-foreign-object elements holding an svg:svg. */
	private static java.util.List<Element> svgPictures(org.w3c.dom.Document doc) {
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		NodeList ifos = doc.getElementsByTagNameNS(FO, "instream-foreign-object");
		for (int i = 0; i < ifos.getLength(); i++) {
			Element ifo = (Element)ifos.item(i);
			for (org.w3c.dom.Node n = ifo.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element && SVG.equals(n.getNamespaceURI())
						&& "svg".equals(n.getLocalName())) {
					out.add(ifo);
					break;
				}
			}
		}
		return out;
	}

	/** Fraction of the rendered page that is not white. */
	private static double ink(byte[] pdfBytes, int page) throws Exception {
		try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
			java.awt.image.BufferedImage img =
					new org.apache.pdfbox.rendering.PDFRenderer(d).renderImageWithDPI(page, 72);
			long n = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					if ((img.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) n++;
				}
			}
			return n / (double)(img.getWidth() * (long)img.getHeight());
		}
	}

	private static String pdfText(byte[] pdfBytes) throws Exception {
		try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
			return new org.apache.pdfbox.text.PDFTextStripper().getText(d);
		}
	}

	// ------------------------------------------------------------------ tests

	/** docx4j's own sample document, two EMF pictures. */
	@Test
	public void emfSampleIsDrawnAsSvg() throws Exception {
		samplePicturesAreSvg("EMF.docx");
	}

	/** docx4j's own sample document, two WMF pictures (one with an embedded bitmap). */
	@Test
	public void wmfSampleIsDrawnAsSvg() throws Exception {
		samplePicturesAreSvg("WMF.docx");
	}

	private void samplePicturesAreSvg(String docx) throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags) + docx + ": ";
			org.w3c.dom.Document doc = fo(sample(docx), flags);

			java.util.List<Element> pictures = svgPictures(doc);
			assertEquals(impl + "both pictures should be SVG in an fo:instream-foreign-object",
					2, pictures.size());
			assertEquals(impl + "nothing should be left as an fo:external-graphic FOP cannot decode",
					0, doc.getElementsByTagNameNS(FO, "external-graphic").getLength());

			for (Element ifo : pictures) {
				assertTrue(impl + "the picture must keep the frame the document gives it",
						ifo.getAttribute("content-width").endsWith("pt")
						&& ifo.getAttribute("content-height").endsWith("pt"));
			}

			byte[] pdf = pdf(sample(docx), flags);
			assertTrue(impl + "the page came out blank", ink(pdf, 0) > 0.01);
		}
	}

	/**
	 * The metafile's own text reaches the PDF as text, not as glyph outlines: the SVG
	 * carries {@code <text>} elements and FOP resolves the font.  This is what makes a
	 * pasted chart or an equation preview searchable.
	 */
	@Test
	public void metafileTextIsSearchableInThePdf() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			byte[] pdf = pdf(inlinePkg("SimpleEMF_windows.emf"), flags);
			String text = pdfText(pdf).replaceAll("\\s+", "");
			assertTrue(impl + "the EMF's text should be text in the PDF, was: " + text,
					text.contains("testPDF.pdf"));
			assertTrue(impl + "the page came out blank", ink(pdf, 0) > 0.01);
		}
	}

	/** A WMF whose content is an embedded bitmap: Batik writes it as
	 *  {@code <image xlink:href="data:...">}, which needs the prefix bound. */
	@Test
	public void aWmfHoldingABitmapIsDrawn() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			WordprocessingMLPackage pkg = inlinePkg("star_picture_save_as.wmf");
			org.w3c.dom.Document doc = fo(pkg, flags);
			assertEquals(impl + "the picture should be SVG", 1, svgPictures(doc).size());

			/* Batik references the embedded bitmap as <image xlink:href="data:...">, and
			 * Batik's own bridge - which is what FOP hands the SVG to - reads only that
			 * attribute.  The prefix therefore has to be bound where the FO is parsed,
			 * and Xalan drops the SVG's own declaration in the XSLT pathway, so fo:root
			 * carries one. */
			NodeList images = doc.getElementsByTagNameNS(SVG, "image");
			assertEquals(impl + "the WMF's bitmap should be an svg:image", 1, images.getLength());
			Element image = (Element)images.item(0);
			assertTrue(impl + "the bitmap reference was lost",
					image.hasAttributeNS("http://www.w3.org/1999/xlink", "href"));
			assertEquals(impl + "the xlink prefix is not bound, so the FO is not well-formed",
					"http://www.w3.org/1999/xlink", image.lookupNamespaceURI("xlink"));
			assertTrue(impl + "the bitmap in the WMF was not painted",
					ink(pdf(inlinePkg("star_picture_save_as.wmf"), flags), 0) > 0.02);
		}
	}

	/**
	 * An anchored (floating) metafile picture is positioned as Word positions it -
	 * WordLayoutFixups turns the anchor hints into an fo:float or a block-container -
	 * exactly as an anchored bitmap is.  The hints travel on the
	 * fo:instream-foreign-object now, not only on fo:external-graphic.
	 */
	@Test
	public void anAnchoredMetafileIsPositioned() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			org.w3c.dom.Document doc = fo(anchoredPkg("pptx.emf"), flags);

			java.util.List<Element> pictures = svgPictures(doc);
			assertEquals(impl + "the picture should be SVG", 1, pictures.size());
			Element ifo = pictures.get(0);
			assertTrue(impl + "the anchor hints should have been consumed and removed",
					!ifo.hasAttribute("docx4j-anchor"));

			// it was lifted out of its run into a float or a block-container
			boolean positioned = false;
			for (org.w3c.dom.Node p = ifo.getParentNode(); p instanceof Element; p = p.getParentNode()) {
				String ln = p.getLocalName();
				if (FO.equals(p.getNamespaceURI()) && ("float".equals(ln) || "block-container".equals(ln))) {
					positioned = true;
					break;
				}
			}
			assertTrue(impl + "the anchored metafile was left in the flow", positioned);
			assertTrue(impl + "the page came out blank",
					ink(pdf(anchoredPkg("pptx.emf"), flags), 0) > 0.01);
		}
	}

	/**
	 * The preview of an embedded object (w:object / v:imagedata) is a metafile in most
	 * documents Word writes - an Equation Editor 3 equation is a WMF, MathType and
	 * pasted Office content usually EMF - and reaches the same picture pipeline.
	 */
	@Test
	public void aVmlObjectPreviewIsDrawn() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			org.w3c.dom.Document doc = fo(vmlPkg("vector_image.emf"), flags);
			assertEquals(impl + "the object's preview should be SVG", 1, svgPictures(doc).size());
			assertEquals(impl + "nothing should be left as an fo:external-graphic",
					0, doc.getElementsByTagNameNS(FO, "external-graphic").getLength());
			assertTrue(impl + "the page came out blank",
					ink(pdf(vmlPkg("vector_image.emf"), flags), 0) > 0.005);
		}
	}

	/**
	 * A metafile which fails part way through drawing keeps whatever was painted, and
	 * the export finishes.  file-45.wmf is POI bug 69927's reproducer: its bitmap
	 * header asks for an absurd allocation, which the guard rejects - and
	 * {@code HwmfPicture.draw} does not swallow a per-record failure, so the rest of
	 * the file is abandoned.  The picture must still take the space the document gives
	 * it, or every line below it moves up.
	 */
	@Test
	public void aMetafileThatFailsMidDrawDoesNotFailTheExport() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			org.w3c.dom.Document doc = fo(inlinePkg("file-45.wmf"), flags);
			Element picture = onlyPicture(doc);
			assertEquals(impl + "the picture lost the width the document declares",
					"200pt", picture.getAttribute("content-width"));
			assertEquals(impl + "the picture lost the height the document declares",
					"150pt", picture.getAttribute("content-height"));
			assertTrue(impl + "the text below the picture was lost",
					pdfText(pdf(inlinePkg("file-45.wmf"), flags)).contains("text below the picture"));
		}
	}

	/**
	 * A metafile which cannot be parsed at all falls back to what docx4j did before:
	 * an fo:external-graphic, which WordLayoutFixups then converts with ImageMagick if
	 * one is configured and otherwise replaces with a transparent placeholder of the
	 * declared extent.  (18 bytes: a truncated WMF header, from POI's fuzzer corpus.)
	 */
	@Test
	public void anUnparseableMetafileFallsBack() throws Exception {
		String name = "clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-6060921738035200.wmf";
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			org.w3c.dom.Document doc = fo(rawWmfPkg(name), flags);
			assertEquals(impl + "an unparseable metafile should not become SVG",
					0, svgPictures(doc).size());
			Element picture = onlyPicture(doc);
			assertEquals(impl + "the fallback is the fo:external-graphic docx4j always emitted",
					"external-graphic", picture.getLocalName());
			assertEquals(impl + "the space Word gives the picture must still be reserved",
					"200pt", picture.getAttribute("content-width"));
			assertTrue(impl + "the text below the picture was lost",
					pdfText(pdf(rawWmfPkg(name), flags)).contains("text below the picture"));
		}
	}

	/** The document's one picture, however it is represented. */
	private static Element onlyPicture(org.w3c.dom.Document doc) {
		NodeList graphics = doc.getElementsByTagNameNS(FO, "external-graphic");
		NodeList ifos = doc.getElementsByTagNameNS(FO, "instream-foreign-object");
		assertEquals("expected exactly one picture", 1, graphics.getLength() + ifos.getLength());
		return (Element)(graphics.getLength() == 1 ? graphics.item(0) : ifos.item(0));
	}
}
