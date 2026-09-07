package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.images.DataUriConversionImageHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.junit.Test;

/**
 * An embedded object's preview picture reaches HTML, in both pathways.
 *
 * <p>Word writes an embedded object (an Equation Editor or MathType equation, an
 * embedded workbook or Visio drawing, an object shown as an icon) as a
 * {@code w:object} holding the VML shape whose {@code v:imagedata} points at the
 * picture it draws.  Until 17.1.0 neither HTML pathway matched {@code w:object}
 * either - {@code docx2xhtml-core.xslt} had a {@code w:pict} template and no
 * {@code w:object} one, so the object fell to the no-match template, and the visitor
 * fell to its catch-all - so the preview was lost.  Both now take the same path as
 * {@code w:pict} (CR-011).</p>
 *
 * <p>This module has docx4j-core but not docx4j-export-fo, so a metafile preview
 * becomes a PNG here rather than an inline {@code <svg>}; see {@code MetafileHtmlTest}
 * for why.</p>
 *
 * @since 17.1.0
 */
public class VmlObjectHtmlTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] PATHWAYS = {
			Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSLT" : "visitor") + ": ";
	}

	// ---------------------------------------------------------------- fixtures

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
		try (InputStream is = VmlObjectHtmlTest.class.getResourceAsStream("/metafiles/" + name)) {
			return org.apache.commons.io.IOUtils.toByteArray(is);
		}
	}

	private static String object(String relId) {
		return "<w:object w:dxaOrig=\"4000\" w:dyaOrig=\"3000\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
				+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<v:shapetype id=\"_x0000_t75\" o:spt=\"75\" coordsize=\"21600,21600\""
				+ " path=\"m@4@5l@4@11@9@11@9@5xe\"><v:stroke joinstyle=\"miter\"/></v:shapetype>"
				+ "<v:shape id=\"_x0000_i1025\" type=\"#_x0000_t75\" o:ole=\"\""
				+ " style=\"width:200pt;height:150pt\">"
				+ "<v:imagedata r:id=\"" + relId + "\" o:title=\"\"/>"
				+ "</v:shape>"
				+ "<o:OLEObject Type=\"Embed\" ProgID=\"Equation.3\" ShapeID=\"_x0000_i1025\""
				+ " DrawAspect=\"Content\" ObjectID=\"_1234567890\" r:id=\"" + relId + "\"/>"
				+ "</w:object>";
	}

	private static WordprocessingMLPackage pkg(byte[] image) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = BinaryPartAbstractImage.createImagePart(pkg, image)
				.getSourceRelationship().getId();
		return body(pkg, "<w:p><w:r>" + object(relId) + "</w:r>"
				+ "<w:r><w:t>beside the object</w:t></w:r></w:p>");
	}

	/**
	 * The same, with the metafile stored as the part Word stores it in.
	 *
	 * <p>{@code BinaryPartAbstractImage.createImagePart} sniffs the bytes and, where it
	 * recognises no image, reaches for ImageMagick - which is not what opening a docx
	 * that already holds the metafile does, and not available on every build machine.</p>
	 */
	private static WordprocessingMLPackage wmfPkg(String metafile) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart part =
				new org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart(
						new org.docx4j.openpackaging.parts.PartName("/word/media/image1.wmf"));
		part.setBinaryData(metafile(metafile));
		String relId = pkg.getMainDocumentPart().addTargetPart(part).getId();
		return body(pkg, "<w:p><w:r>" + object(relId) + "</w:r>"
				+ "<w:r><w:t>beside the object</w:t></w:r></w:p>");
	}

	private static WordprocessingMLPackage body(WordprocessingMLPackage pkg, String body)
			throws Exception {
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	private static String toHtml(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		settings.setImageHandler(new DataUriConversionImageHandler());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	private static java.util.List<String> srcList(String html) {
		java.util.List<String> out = new java.util.ArrayList<String>();
		java.util.regex.Matcher m = java.util.regex.Pattern
				.compile("<img[^>]*src=\"([^\"]*)\"").matcher(html);
		while (m.find()) out.add(m.group(1));
		return out;
	}

	// ---------------------------------------------------------------- tests

	/** A bitmap preview: the object becomes the {@code <img>} its v:imagedata names. */
	@Test
	public void aBitmapPreviewBecomesAnImg() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			String html = toHtml(pkg(png()), flags);

			java.util.List<String> srcs = srcList(html);
			assertEquals(impl + "the object's preview should be one <img>: " + html,
					1, srcs.size());
			assertTrue(impl + "the preview should be embedded as the PNG it is",
					srcs.get(0).startsWith("data:image/png;base64,"));
			assertTrue(impl + "the text beside the object was lost",
					html.contains("beside the object"));
		}
	}

	/**
	 * A metafile preview (what Word writes for an Equation Editor 3 equation) is drawn
	 * by the metafile renderer, so what the browser gets is a picture and not the
	 * .wmf it could not show.
	 */
	@Test
	public void aMetafilePreviewIsDrawn() throws Exception {
		for (int flags : PATHWAYS) {
			String impl = pathway(flags);
			String html = toHtml(wmfPkg("60677.wmf"), flags);

			java.util.List<String> srcs = srcList(html);
			assertEquals(impl + "the object's preview should be one <img>", 1, srcs.size());
			assertTrue(impl + "the metafile should have been rasterised (no SVG provider in this module)",
					srcs.get(0).startsWith("data:image/png;base64,"));
			assertTrue(impl + "the metafile's own bytes should not be embedded",
					!html.contains("data:image/x-wmf"));

			byte[] png = java.util.Base64.getDecoder().decode(
					srcs.get(0).substring("data:image/png;base64,".length()));
			java.awt.image.BufferedImage img = javax.imageio.ImageIO
					.read(new java.io.ByteArrayInputStream(png));
			assertTrue(impl + "the embedded PNG does not decode", img != null);
			assertTrue(impl + "the embedded PNG is blank", ink(img) > 0.001);
		}
	}

	/** fraction of the image that was painted (the canvas starts transparent) */
	private static double ink(java.awt.image.BufferedImage img) {
		long n = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if ((img.getRGB(x, y) >>> 24) != 0) n++;
			}
		}
		return n / (double)(img.getWidth() * (long)img.getHeight());
	}
}
