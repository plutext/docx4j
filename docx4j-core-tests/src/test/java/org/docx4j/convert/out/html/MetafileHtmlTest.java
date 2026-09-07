package org.docx4j.convert.out.html;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.images.DataUriConversionImageHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A WMF / EMF / EMF+ picture reaches HTML as something a browser can show.
 *
 * <p>Until 17.1.0 the {@code <img src>} named the {@code .wmf}/{@code .emf} itself,
 * which no browser renders - and the branch in {@code AbstractWordXmlPicture} that
 * was meant to convert it had never been reachable, because the field it tested was
 * never assigned (CR-011 §2).  docx4j now replays the metafile: as an inline
 * {@code <svg>} where the image handler embeds images in the document anyway, and
 * otherwise as a PNG put through that same handler.</p>
 *
 * <p><b>This module has docx4j-core but not docx4j-export-fo</b>, and the SVG
 * generator (Batik's {@code SVGGraphics2D}) lives there - so what these tests pin is
 * the core-only behaviour: every metafile becomes a PNG, whichever handler is in
 * use.  The inline-{@code <svg>} case is covered in docx4j-export-fo-tests
 * ({@code MetafileHtmlSvgTest}), where a {@code MetafileSvgProvider} is present.</p>
 *
 * <p>Both HTML pathways are asserted, the XSLT one and the visitor one.</p>
 *
 * @since 17.1.0
 */
public class MetafileHtmlTest {

	private static final int[] PATHWAYS = {
			Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static String pathway(int flags) {
		return (flags == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSLT" : "visitor") + ": ";
	}

	private static WordprocessingMLPackage sample(String name) throws Exception {
		try (InputStream is = MetafileHtmlTest.class.getResourceAsStream("/metafiles/docs/" + name)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	private static String toHtml(WordprocessingMLPackage pkg, int flags,
			org.docx4j.model.images.ConversionImageHandler handler) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		if (handler != null) {
			settings.setImageHandler(handler);
		} else {
			settings.setImageDirPath(imageDir().getAbsolutePath());
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	private static java.io.File imageDir() throws Exception {
		java.io.File dir = new java.io.File(System.getProperty("java.io.tmpdir"),
				"docx4j-metafile-html-test");
		dir.mkdirs();
		return dir;
	}

	/**
	 * A file-writing image handler gets a PNG to write - not the {@code .emf} it used
	 * to be handed, which would have been written out and then not displayed.
	 */
	@Test
	public void aFileHandlerWritesAPng() throws Exception {
		for (String docx : new String[] {"EMF.docx", "WMF.docx"}) {
			for (int flags : PATHWAYS) {
				String impl = pathway(flags) + docx + ": ";
				String html = toHtml(sample(docx), flags, null);

				assertTrue(impl + "the picture should be a PNG the browser can show, not the metafile: "
						+ srcs(html), srcs(html).contains(".png"));
				assertTrue(impl + "the raw metafile should not be referenced: " + srcs(html),
						!srcs(html).contains(".emf\"") && !srcs(html).contains(".wmf\""));

				for (String src : srcList(html)) {
					java.io.File f = new java.io.File(imageDir(), src);
					assertTrue(impl + "no file was written for " + src, f.isFile() && f.length() > 0);
					java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
					assertTrue(impl + src + " is not a readable image", img != null);
					assertTrue(impl + src + " is blank", ink(img) > 0.001);
				}
			}
		}
	}

	/** A handler which embeds images gets a PNG data URI, and it decodes. */
	@Test
	public void aDataUriHandlerEmbedsAPng() throws Exception {
		for (String docx : new String[] {"EMF.docx", "WMF.docx"}) {
			for (int flags : PATHWAYS) {
				String impl = pathway(flags) + docx + ": ";
				String html = toHtml(sample(docx), flags, new DataUriConversionImageHandler());

				assertTrue(impl + "the metafile should have been rasterised to a PNG data URI",
						html.contains("data:image/png;base64,"));
				assertTrue(impl + "the metafile's own bytes should not be embedded",
						!html.contains("data:image/x-emf") && !html.contains("data:image/x-wmf"));

				java.util.regex.Matcher m = java.util.regex.Pattern
						.compile("data:image/png;base64,([A-Za-z0-9+/=]+)").matcher(html);
				int found = 0;
				while (m.find()) {
					byte[] png = java.util.Base64.getDecoder().decode(m.group(1));
					java.awt.image.BufferedImage img = javax.imageio.ImageIO
							.read(new java.io.ByteArrayInputStream(png));
					assertTrue(impl + "the embedded PNG does not decode", img != null);
					assertTrue(impl + "the embedded PNG is blank", ink(img) > 0.001);
					found++;
				}
				assertTrue(impl + "both pictures should be embedded, found " + found, found == 2);
			}
		}
	}

	// ---------------------------------------------------------------- helpers

	private static String srcs(String html) {
		return String.join(" ", srcList(html));
	}

	private static java.util.List<String> srcList(String html) {
		java.util.List<String> out = new java.util.ArrayList<String>();
		java.util.regex.Matcher m = java.util.regex.Pattern
				.compile("<img[^>]*src=\"([^\"]*)\"").matcher(html);
		while (m.find()) out.add(m.group(1));
		return out;
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
