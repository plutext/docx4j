package org.docx4j.metafiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.docx4j.org.apache.poi.hemf.draw.HemfImageRenderer;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfImageRenderer;
import org.docx4j.org.apache.poi.sl.draw.Drawable;
import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;
import org.docx4j.org.apache.poi.sl.draw.ImageRendererFactory;

/**
 * Shared "render a metafile to a BufferedImage" helper for the CR-011 tests.
 *
 * The repackaged POI renderers report their natural size in 96 DPI pixels
 * ({@code ImageRenderer.getDimension()}), so rendering at that size is rendering at
 * 96 DPI. The size is capped so that a stray huge metafile cannot allocate an
 * unbounded image in the test JVM.
 *
 * @since 17.0.6 (CR-011 phase 1)
 */
public class MetafileRendering {

	/** the fixed resolution the tests render at */
	public static final int DPI = 96;

	/** cap on either dimension of the rendered image, in pixels */
	public static final int MAX_PX = 2000;

	/** minimum image dimension, so that a degenerate metafile still produces something */
	public static final int MIN_PX = 8;

	public static ImageRenderer rendererFor(String name, byte[] data) throws IOException {
		String contentType = MetafileTestFiles.contentTypeOf(name);
		ImageRenderer r = MetafileTestFiles.isWmf(name)
			? new HwmfImageRenderer() : new HemfImageRenderer();
		r.loadImage(data, contentType);
		return r;
	}

	/** the renderer the production dispatch would pick for this content type */
	public static ImageRenderer dispatch(Graphics2D g, String name) {
		return ImageRendererFactory.getImageRenderer(g, MetafileTestFiles.contentTypeOf(name));
	}

	/**
	 * A background colour no real metafile is likely to paint, used when the question is
	 * "did anything at all get drawn" - some metafiles legitimately draw white shapes,
	 * which are invisible against a white page (empty-polygon-close.wmf does exactly that).
	 */
	public static final Color PROBE_BACKGROUND = Color.MAGENTA;

	/**
	 * Renders the metafile onto an opaque image of the given background colour, at
	 * {@value #DPI} DPI.
	 *
	 * @return the image; never null
	 */
	public static BufferedImage render(String name, byte[] data, Color background) throws IOException {
		ImageRenderer r = rendererFor(name, data);
		Dimension2D dim = r.getDimension();

		int w = clamp(dim.getWidth());
		int h = clamp(dim.getHeight());

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setColor(background);
			g.fillRect(0, 0, w, h);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setRenderingHint(Drawable.DEFAULT_CHARSET, java.nio.charset.Charset.forName("windows-1252"));
			r.drawImage(g, new java.awt.geom.Rectangle2D.Double(0, 0, w, h));
		} finally {
			g.dispose();
		}
		return img;
	}

	private static int clamp(double v) {
		if (!Double.isFinite(v) || v <= 0) {
			return MIN_PX;
		}
		return Math.max(MIN_PX, Math.min(MAX_PX, (int)Math.rint(v)));
	}

	/** fraction of pixels which are not the given background colour */
	public static double inkFraction(BufferedImage img, Color background) {
		final int bg = background.getRGB() & 0xFFFFFF;
		final int w = img.getWidth(), h = img.getHeight();
		long ink = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if ((img.getRGB(x, y) & 0xFFFFFF) != bg) {
					ink++;
				}
			}
		}
		return ink / (double)(w * (long)h);
	}
}
