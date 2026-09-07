package org.docx4j.metafiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.model.images.MetafileRenderer;
import org.docx4j.model.images.MetafileSvgProvider;
import org.docx4j.model.images.PoiMetafileRenderer;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafilePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileWmfPart;
import org.junit.Test;

/**
 * The public conversion API for metafiles: {@link MetafileRenderer} and the
 * {@code toPNG} / {@code toSVG} methods on the parts (CR-011 phase 2, §4.2 item 2).
 *
 * <p>docx4j-core has no SVG generator - Batik's {@code SVGGraphics2D} is a
 * dependency of docx4j-export-fo - so here {@code toSVG()} is expected to say so
 * rather than to work; that it does work with that module present is asserted in
 * docx4j-export-fo-tests.  Rasterising needs nothing beyond the JDK, so
 * {@code toPNG} works either way.</p>
 *
 * @since 17.1.0
 */
public class MetafileConversionApiTest {

	private static final MetafileRenderer RENDERER = PoiMetafileRenderer.getInstance();

	private static MetafilePart part(String name) throws Exception {
		MetafilePart p = MetafileTestFiles.isWmf(name)
				? new MetafileWmfPart(new PartName("/word/media/image1.wmf"))
				: new MetafileEmfPart(new PartName("/word/media/image1.emf"));
		p.setBinaryData(MetafileTestFiles.bytes(name));
		return p;
	}

	// ------------------------------------------------------------ content types

	@Test
	public void theRendererClaimsTheContentTypesWordAndFopUse() {
		for (String ct : new String[] {"image/x-wmf", "image/wmf", "image/x-emf", "image/emf",
				"application/x-msmetafile", "IMAGE/X-EMF"}) {
			assertTrue(ct + " should be renderable", RENDERER.canRender(ct));
		}
		for (String ct : new String[] {"image/png", "image/jpeg", "application/pdf", null}) {
			assertTrue(ct + " is not a metafile", !RENDERER.canRender(ct));
		}
	}

	/**
	 * The bytes decide, not the content type: Word writes the content type from the
	 * file extension it was given, so a part declared image/png can hold an EMF.
	 */
	@Test
	public void theSignatureOverridesADeclaredContentType() throws Exception {
		for (String name : MetafileTestFiles.allNames()) {
			byte[] head = MetafileTestFiles.bytes(name);
			String sniffed = PoiMetafileRenderer.sniff(
					java.util.Arrays.copyOf(head, Math.min(head.length, PoiMetafileRenderer.SNIFF_LENGTH)));
			if (sniffed != null) {
				assertEquals(name + " was sniffed as the wrong format",
						MetafileTestFiles.contentTypeOf(name), sniffed);
				assertEquals(name + ": what it is beats what it says",
						sniffed, PoiMetafileRenderer.contentTypeOf("image/png", head));
			}
		}
		assertNull("a PNG is not a metafile", PoiMetafileRenderer.sniff(
				new byte[] {(byte)0x89, 'P', 'N', 'G', 13, 10, 26, 10}));
		assertNull("empty is not a metafile", PoiMetafileRenderer.sniff(new byte[0]));
	}

	// ------------------------------------------------------------ toPNG

	/**
	 * Every metafile that can be parsed rasterises, and the ones that draw something
	 * produce ink.  A file which draws nothing is listed in
	 * {@link MetafileTestFiles#rendersBlank}, so an improvement there is noticed.
	 */
	@Test
	public void everyParsableMetafileRastersises() throws Exception {
		List<String> blank = new ArrayList<String>();
		for (String name : MetafileTestFiles.renderableNames()) {
			BufferedImage img = part(name).toPNG(96);
			assertNotNull(name + " did not rasterise", img);
			assertTrue(name + " came out with no pixels", img.getWidth() > 0 && img.getHeight() > 0);
			if (ink(img) == 0) blank.add(name);
		}
		for (String name : blank) {
			assertTrue(name + " rendered blank, and is not on the known-blank list;"
					+ " if that is an improvement, take it off MetafileTestFiles.RENDERS_BLANK",
					MetafileTestFiles.rendersBlank(name) || MetafileTestFiles.throwsOnRender(name));
		}
	}

	/** The bytes of an actual PNG file, for a caller who wants to write one out. */
	@Test
	public void toPngBytesIsAPngFile() throws Exception {
		byte[] png = part("SimpleEMF_windows.emf").toPNGBytes(96);
		assertTrue("not a PNG signature", png.length > 8 && (png[0] & 0xFF) == 0x89
				&& png[1] == 'P' && png[2] == 'N' && png[3] == 'G');
		BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));
		assertNotNull("the PNG does not decode", img);
		assertTrue("the PNG is blank", ink(img) > 0);
	}

	/** Resolution scales the raster; the metafile's own size in points does not change. */
	@Test
	public void dpiScalesTheRaster() throws Exception {
		MetafilePart p = part("pptx.emf");
		BufferedImage low = p.toPNG(72);
		BufferedImage high = p.toPNG(144);
		assertTrue("144dpi should be about twice 72dpi, was " + low.getWidth() + " and " + high.getWidth(),
				Math.abs(high.getWidth() - 2 * low.getWidth()) <= 2);
	}

	// ------------------------------------------------------------ robustness

	/**
	 * A metafile docx4j cannot parse returns null rather than throwing - including the
	 * ones which raise {@code AssertionError} from POI's bare asserts under
	 * {@code -ea}, which Surefire turns on (see the package README).
	 */
	@Test
	public void anUnusableMetafileDegradesQuietly() throws Exception {
		for (String name : MetafileTestFiles.allNames()) {
			if (!MetafileTestFiles.isUnusable(name)) continue;
			byte[] data = MetafileTestFiles.bytes(name);
			assertNull(name + " should not have a size", RENDERER.getSizeInPoints(data));
			assertNull(name + " should not rasterise", RENDERER.toImage(data, 96));
			// and drawing it paints nothing, without throwing
			BufferedImage canvas = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = canvas.createGraphics();
			try {
				RENDERER.draw(data, g, new java.awt.geom.Rectangle2D.Double(0, 0, 20, 20));
			} finally {
				g.dispose();
			}
			assertEquals(name + " painted something", 0.0, ink(canvas), 0.0);
		}
	}

	/**
	 * A metafile which fails part way through drawing keeps what it had already
	 * painted, and does not throw: file-45.wmf's DIB header asks for an allocation the
	 * guard refuses, and {@code HwmfPicture.draw} abandons the rest of the file.
	 */
	@Test
	public void aMetafileThatFailsMidDrawDoesNotThrow() throws Exception {
		byte[] data = MetafileTestFiles.bytes("file-45.wmf");
		assertNotNull("it has a size, even though drawing it fails", RENDERER.getSizeInPoints(data));
		assertNotNull("it should still produce an image", RENDERER.toImage(data, 96));
	}

	// ------------------------------------------------------------ toSVG

	/**
	 * Without docx4j-export-fo there is no SVG generator, and {@code toSVG()} says so
	 * instead of failing obscurely.  (With that module the same call returns the SVG;
	 * see docx4j-export-fo-tests.)
	 */
	@Test
	public void toSvgNeedsTheExportFoModule() throws Exception {
		if (MetafileSvgProvider.getProvider() != null) {
			// docx4j-export-fo is on the classpath after all: then it must work
			assertNotNull(part("pptx.emf").toSVG().getDomDocument());
			return;
		}
		for (String name : new String[] {"pptx.emf", "star_picture_save_as.wmf"}) {
			try {
				part(name).toSVG();
				fail(name + ": expected toSVG() to report that no provider is available");
			} catch (org.docx4j.openpackaging.exceptions.Docx4JException e) {
				MetafileTestFiles.assertContains(e.getMessage(), "docx4j-export-fo");
			}
		}
	}

	// ------------------------------------------------------------ helpers

	/**
	 * Fraction of the image that was painted at all.
	 *
	 * <p>{@code toImage} draws onto a transparent canvas, so "painted" is alpha, not
	 * colour: a metafile may legitimately draw in white (empty-polygon-close.wmf does),
	 * which a non-white test would call blank.</p>
	 */
	private static double ink(BufferedImage img) {
		long n = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if ((img.getRGB(x, y) >>> 24) != 0) n++;
			}
		}
		return n / (double)(img.getWidth() * (long)img.getHeight());
	}
}
