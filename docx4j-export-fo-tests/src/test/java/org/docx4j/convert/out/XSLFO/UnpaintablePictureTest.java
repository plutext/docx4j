package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Word draws every picture; FOP paints only the formats it has a loader for, and when
 * it has none it drops the viewport too - so the space Word gives the picture
 * collapses and everything below it moves up the page.
 *
 * <p>Two kinds of picture hit that in a real corpus: EMF, which FOP can size but not
 * paint (there is no EMF loader for PDF output), and parts whose bytes are no image at
 * all - Word stores the web server's 404 page as the picture when a linked picture
 * cannot be fetched, and one document in 157 had eighteen of those.  Both are now
 * pointed at a transparent 1x1 PNG and scaled non-uniformly, so the extent the
 * document declares is reserved, which is what the layout needs.</p>
 *
 * <p>The formats FOP does paint with what docx4j depends on are also checked here:
 * PNG, JPEG (baseline, progressive and CMYK), GIF, BMP and TIFF all reach the area
 * tree, so no extra ImageIO plugin is needed for them.</p>
 *
 * @since 17.0.6
 */
public class UnpaintablePictureTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** 100 x 75pt, in EMU */
	private static final long CX = 1270000L, CY = 952500L;

	// ---------------------------------------------------------------- test images

	/** A valid, minimal EMF: the ENHMETAHEADER record and EMR_EOF, nothing drawn. */
	static byte[] emf() {
		ByteBuffer b = ByteBuffer.allocate(108).order(ByteOrder.LITTLE_ENDIAN);
		b.putInt(1).putInt(88);                       // EMR_HEADER, record size
		b.putInt(0).putInt(0).putInt(99).putInt(74);  // rclBounds, device units
		b.putInt(0).putInt(0).putInt(2646).putInt(1984); // rclFrame, hundredths of a mm
		b.put(" EMF".getBytes(StandardCharsets.US_ASCII)); // dSignature
		b.putInt(0x00010000);                         // nVersion
		b.putInt(108);                                // nBytes
		b.putInt(2);                                  // nRecords
		b.putShort((short)0).putShort((short)0);      // nHandles, sReserved
		b.putInt(0).putInt(0).putInt(0);              // nDescription, offDescription, nPalEntries
		b.putInt(1920).putInt(1080);                  // szlDevice
		b.putInt(508).putInt(285);                    // szlMillimeters
		b.putInt(14).putInt(20).putInt(0).putInt(16).putInt(20); // EMR_EOF
		return b.array();
	}

	/** What Word stores when a linked picture cannot be fetched. */
	static byte[] notAnImage() {
		return ("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n"
				+ "<html><head><title>404 Not Found</title></head><body>"
				+ "<h1>Not Found</h1></body></html>\n").getBytes(StandardCharsets.US_ASCII);
	}

	private static java.awt.image.BufferedImage image(int type) {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(60, 45, type);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 60, 45);
		g.dispose();
		return img;
	}

	static byte[] written(String format) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertTrue("no ImageIO writer for " + format,
				javax.imageio.ImageIO.write(image(java.awt.image.BufferedImage.TYPE_INT_RGB), format, out));
		return out.toByteArray();
	}

	static byte[] progressiveJpeg() throws Exception {
		javax.imageio.ImageWriter w = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
		javax.imageio.ImageWriteParam p = w.getDefaultWriteParam();
		p.setProgressiveMode(javax.imageio.ImageWriteParam.MODE_DEFAULT);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(out)) {
			w.setOutput(ios);
			w.write(null, new javax.imageio.IIOImage(
					image(java.awt.image.BufferedImage.TYPE_INT_RGB), null, null), p);
		} finally {
			w.dispose();
		}
		return out.toByteArray();
	}

	/** Four components, as a CMYK scan from a print workflow has. */
	static byte[] cmykJpeg() throws Exception {
		java.awt.image.WritableRaster raster = java.awt.image.Raster.createInterleavedRaster(
				java.awt.image.DataBuffer.TYPE_BYTE, 60, 45, 4, new java.awt.Point(0, 0));
		for (int y = 0; y < 45; y++) {
			for (int x = 0; x < 60; x++) {
				raster.setPixel(x, y, new int[] { 20, 40, 60, 10 });
			}
		}
		javax.imageio.ImageWriter w = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(out)) {
			w.setOutput(ios);
			w.write(null, new javax.imageio.IIOImage(raster, null, null), w.getDefaultWriteParam());
		} finally {
			w.dispose();
		}
		return out.toByteArray();
	}

	// ---------------------------------------------------------------- the document

	private static String picture(String relId) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
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
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
	}

	/**
	 * A package with one picture, whose part is built by hand so the bytes reach the
	 * export exactly as the document holds them (createImagePart would try to convert
	 * anything it does not recognise).
	 */
	private static WordprocessingMLPackage pkg(byte[] bytes, String contentType, String ext) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPart part = (BinaryPart)pkg.getContentTypeManager()
				.newPartForContentType(contentType, "/word/media/image1." + ext, null);
		part.setBinaryData(bytes);
		String relId = pkg.getMainDocumentPart().addTargetPart(part).getId();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p>" + picture(relId) + "</w:p>"
				+ "<w:p><w:r><w:t>below the picture</w:t></w:r></w:p>"
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

	private static Element graphic(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "external-graphic");
		assertEquals(1, nl.getLength());
		return (Element)nl.item(0);
	}

	// ---------------------------------------------------------------- the space is reserved

	private void spaceIsReserved(byte[] bytes, String contentType, String ext, int flags) throws Exception {
		Element g = graphic(fo(pkg(bytes, contentType, ext), flags));
		assertTrue("a picture FOP cannot paint should be replaced by a transparent one, "
				+ "so that its space is kept: " + g.getAttribute("src"),
				g.getAttribute("src").startsWith("data:image/png;base64,"));
		assertEquals("a 1x1 stand-in would be scaled square without this",
				"non-uniform", g.getAttribute("scaling"));
		// the declared extent is untouched.  wp:extent is in EMU and 12700 EMU is one
		// point, so from 17.0.6 the size is written in points, and fractionally: it used
		// to be rounded to a whole "px" (FOP reads px at 72dpi, so a px was a point).
		assertEquals("100pt", g.getAttribute("content-width"));
		assertEquals("75pt", g.getAttribute("content-height"));
	}

	@Test
	public void emfSpaceIsReservedVisitor() throws Exception {
		spaceIsReserved(emf(), ContentTypes.IMAGE_EMF, "emf", Docx4J.FLAG_NONE);
	}

	@Test
	public void emfSpaceIsReservedXslt() throws Exception {
		spaceIsReserved(emf(), ContentTypes.IMAGE_EMF, "emf", Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void bytesWhichAreNoImageReserveTheirSpaceVisitor() throws Exception {
		spaceIsReserved(notAnImage(), ContentTypes.IMAGE_PNG, "png", Docx4J.FLAG_NONE);
	}

	@Test
	public void bytesWhichAreNoImageReserveTheirSpaceXslt() throws Exception {
		spaceIsReserved(notAnImage(), ContentTypes.IMAGE_PNG, "png", Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** The point of it: the declared extent is on the page, so nothing below moves up. */
	@Test
	public void theReservedSpaceIsOnThePage() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(emf(), ContentTypes.IMAGE_EMF, "emf"), Docx4J.FLAG_NONE);
		Element viewport = null;
		NodeList nl = areaTree.getElementsByTagName("viewport");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element)nl.item(i);
			if (el.getElementsByTagName("image").getLength() > 0) viewport = el;
		}
		assertNotNull("the picture reserved no space at all", viewport);
		assertEquals("100000", viewport.getAttribute("ipd"));
		assertEquals("75000", viewport.getAttribute("bpd"));
	}

	// ---------------------------------------------------------------- what FOP can paint

	private void isPainted(byte[] bytes, String contentType, String ext) throws Exception {
		Element g = graphic(fo(pkg(bytes, contentType, ext), Docx4J.FLAG_NONE));
		assertTrue(ext + " should be left for FOP to paint, not replaced",
				g.getAttribute("src").startsWith("file:"));
		org.w3c.dom.Document areaTree = areaTree(pkg(bytes, contentType, ext), Docx4J.FLAG_NONE);
		assertTrue(ext + " did not reach the area tree",
				areaTree.getElementsByTagName("image").getLength() > 0);
	}

	@Test
	public void pngIsPainted() throws Exception {
		isPainted(written("png"), ContentTypes.IMAGE_PNG, "png");
	}

	@Test
	public void jpegIsPainted() throws Exception {
		isPainted(written("jpeg"), ContentTypes.IMAGE_JPEG, "jpeg");
	}

	@Test
	public void progressiveJpegIsPainted() throws Exception {
		isPainted(progressiveJpeg(), ContentTypes.IMAGE_JPEG, "jpeg");
	}

	@Test
	public void cmykJpegIsPainted() throws Exception {
		isPainted(cmykJpeg(), ContentTypes.IMAGE_JPEG, "jpeg");
	}

	@Test
	public void gifIsPainted() throws Exception {
		isPainted(written("gif"), ContentTypes.IMAGE_GIF, "gif");
	}

	@Test
	public void bmpIsPainted() throws Exception {
		isPainted(written("bmp"), ContentTypes.IMAGE_BMP, "bmp");
	}

	@Test
	public void tiffIsPainted() throws Exception {
		isPainted(written("tiff"), ContentTypes.IMAGE_TIFF, "tiff");
	}
}
