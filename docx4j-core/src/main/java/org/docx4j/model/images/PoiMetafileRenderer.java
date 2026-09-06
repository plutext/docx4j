/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.

 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.docx4j.org.apache.poi.hemf.usermodel.HemfPicture;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.sl.draw.Docx4jDrawFontManager;
import org.docx4j.org.apache.poi.sl.draw.Drawable;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MetafileRenderer} over docx4j's repackaged Apache POI HWMF / HEMF
 * code ({@code org.docx4j.org.apache.poi.h{w,e}mf}, see CR-011 phase 1 and that
 * package's README).
 *
 * <h3>Robustness</h3>
 * Everything here catches {@link Throwable}, not {@link Exception}.  That is
 * deliberate, and the package README says why: the copied parsers use a bare
 * {@code assert} in about twenty places, so a malformed metafile raises
 * {@link AssertionError} under {@code -ea} (which Surefire turns on) where in
 * production it would have reached a bounds check instead.  A picture inside a
 * document must never take a PDF or HTML conversion down, whichever way the JVM
 * is started.
 *
 * <h3>Fonts</h3>
 * A metafile names its fonts as the producing application did - a GDI
 * {@code LOGFONT} face name.  {@link Docx4jDrawFontManager} is installed through
 * the {@link Drawable#FONT_HANDLER} rendering hint so those names resolve through
 * {@link org.docx4j.fonts.PhysicalFonts}, the same machinery the rest of docx4j
 * uses, rather than silently degrading to AWT's Dialog.
 *
 * @since 17.0.6 (CR-011 phase 2)
 */
public class PoiMetafileRenderer implements MetafileRenderer {

	private static Logger log = LoggerFactory.getLogger(PoiMetafileRenderer.class);

	/** the content type the repackaged POI code uses for WMF */
	public static final String WMF_CONTENT_TYPE = "image/x-wmf";
	/** the content type the repackaged POI code uses for EMF */
	public static final String EMF_CONTENT_TYPE = "image/x-emf";

	/** Windows' own default; the charset a metafile's text is in when it doesn't say. */
	private static final Charset DEFAULT_CHARSET = Charset.forName("windows-1252");

	private static final PoiMetafileRenderer INSTANCE = new PoiMetafileRenderer();

	/** The renderer docx4j's picture output uses. */
	public static PoiMetafileRenderer getInstance() {
		return INSTANCE;
	}

	// ------------------------------------------------------------------ detection

	/**
	 * The metafile content type these bytes actually are, whatever the part claims.
	 *
	 * <p>Word writes the content type from the file extension it was given, so a
	 * part declared {@code image/png} can hold an EMF (and does, in documents from
	 * some producers).  The signatures are the ones
	 * {@link org.docx4j.org.apache.poi.poifs.filesystem.FileMagic} knows: an Aldus
	 * placeable header (D7 CD C6 9A), a bare WMF header (01 00 09 00), and an EMF
	 * record 1 whose bytes 40..43 are " EMF".</p>
	 *
	 * @return {@link #WMF_CONTENT_TYPE}, {@link #EMF_CONTENT_TYPE}, or null
	 */
	public static String sniff(byte[] head) {
		if (head == null) return null;
		int n = head.length;
		if (n >= 4 && (head[0] & 0xFF) == 0xD7 && (head[1] & 0xFF) == 0xCD
				&& (head[2] & 0xFF) == 0xC6 && (head[3] & 0xFF) == 0x9A) {
			return WMF_CONTENT_TYPE; // WMF, Aldus placeable header (what Word writes)
		}
		if (n >= 44 && head[0] == 1 && head[1] == 0 && head[2] == 0 && head[3] == 0
				&& head[40] == ' ' && head[41] == 'E' && head[42] == 'M' && head[43] == 'F') {
			return EMF_CONTENT_TYPE;
		}
		if (n >= 6 && head[0] == 1 && head[1] == 0 && head[2] == 9 && head[3] == 0
				&& head[4] == 0 && head[5] == 3) {
			return WMF_CONTENT_TYPE; // WMF without the placeable header
		}
		return null;
	}

	/** The bytes {@link #sniff(byte[])} needs to decide. */
	public static final int SNIFF_LENGTH = 44;

	/**
	 * The content type to render these bytes as: what they actually are if that is a
	 * metafile, otherwise the declared type if that is one this renderer handles.
	 *
	 * @return null where this is not a metafile at all
	 */
	public static String contentTypeOf(String declared, byte[] head) {
		String sniffed = sniff(head);
		if (sniffed != null) return sniffed;
		return normalize(declared);
	}

	/**
	 * The POI content type for one of the several spellings of "WMF" / "EMF" that
	 * turn up in [Content_Types].xml and in ImageIO, or null.
	 */
	public static String normalize(String contentType) {
		if (contentType == null) return null;
		String ct = contentType.trim().toLowerCase(java.util.Locale.ROOT);
		int semi = ct.indexOf(';');
		if (semi >= 0) ct = ct.substring(0, semi).trim();
		if (ct.equals("image/x-wmf") || ct.equals("image/wmf")
				|| ct.equals("application/x-msmetafile") || ct.equals("image/x-msmetafile")
				|| ct.equals("windows/metafile")) {
			return WMF_CONTENT_TYPE;
		}
		if (ct.equals("image/x-emf") || ct.equals("image/emf")
				|| ct.equals("application/emf") || ct.equals("application/x-emf")
				|| ct.equals("image/x-mgx-emf")) {
			return EMF_CONTENT_TYPE;
		}
		return null;
	}

	@Override
	public boolean canRender(String contentType) {
		return normalize(contentType) != null;
	}

	// ------------------------------------------------------------------ parsing

	/** Either a {@link HwmfPicture} or a {@link HemfPicture}, whichever the bytes are. */
	private static Object parse(byte[] data) {
		if (data == null || data.length < 4) return null;
		String ct = sniff(java.util.Arrays.copyOf(data, Math.min(data.length, SNIFF_LENGTH)));
		try {
			if (EMF_CONTENT_TYPE.equals(ct)) {
				HemfPicture emf = new HemfPicture(new ByteArrayInputStream(data));
				emf.setDefaultCharset(DEFAULT_CHARSET);
				emf.getHeader(); // throws where record 0 is not the header
				return emf;
			}
			if (WMF_CONTENT_TYPE.equals(ct)) {
				HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(data));
				wmf.setDefaultCharset(DEFAULT_CHARSET);
				return wmf;
			}
		} catch (Throwable t) {
			// AssertionError included: see the class javadoc
			log.warn("Metafile could not be parsed (" + ct + "): " + t);
		}
		return null;
	}

	/** The metafile's own bounds, in points. */
	private static Rectangle2D boundsInPoints(Object picture) {
		try {
			if (picture instanceof HemfPicture) return ((HemfPicture)picture).getBoundsInPoints();
			if (picture instanceof HwmfPicture) return ((HwmfPicture)picture).getBoundsInPoints();
		} catch (Throwable t) {
			log.warn("Metafile has no usable bounds: " + t);
		}
		return null;
	}

	// ------------------------------------------------------------------ MetafileRenderer

	@Override
	public Dimension2D getSizeInPoints(byte[] data) {
		Object picture = parse(data);
		if (picture == null) return null;
		Rectangle2D b = boundsInPoints(picture);
		if (b == null) return null;
		double w = Math.abs(b.getWidth());
		double h = Math.abs(b.getHeight());
		if (!(w > 0) || !(h > 0) || !Double.isFinite(w) || !Double.isFinite(h)) return null;
		return new Dimension2DDouble(w, h);
	}

	@Override
	public void draw(byte[] data, Graphics2D target, Rectangle2D bounds) {
		Object picture = parse(data);
		if (picture == null || target == null || bounds == null) return;
		draw(picture, target, bounds);
	}

	private static void draw(Object picture, Graphics2D target, Rectangle2D bounds) {
		if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) return;
		try {
			target.setRenderingHint(Drawable.FONT_HANDLER, new Docx4jDrawFontManager());
			target.setRenderingHint(Drawable.DEFAULT_CHARSET, DEFAULT_CHARSET);
			target.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			target.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			target.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			if (picture instanceof HemfPicture) {
				HemfPicture emf = (HemfPicture)picture;
				/* An EMF states three rectangles - the header's frame, the window and the
				 * viewport - and POI picks between them heuristically because "sometimes
				 * winBounds are totally off".  Where the document's frame has the same
				 * shape as the header's frame, though, the header is what the producer
				 * sized the picture from, and preferring it is what Word does; the
				 * heuristic can otherwise crop or letterbox a picture that was right.
				 */
				Rectangle2D header = boundsInPoints(emf);
				if (header != null && sameAspect(header, bounds)) {
					target.setRenderingHint(Drawable.EMF_FORCE_HEADER_BOUNDS, Boolean.TRUE);
				}
				emf.draw(target, bounds);
			} else {
				/* HwmfPicture.draw does not swallow a per-record failure the way
				 * HemfPicture.draw does, so one bad record aborts the rest of the file;
				 * what was painted before it stays on the canvas.  (POI 5.5.1 behaviour,
				 * pinned in the package README.) */
				((HwmfPicture)picture).draw(target, bounds);
			}
		} catch (Throwable t) {
			// AssertionError included: see the class javadoc.  Whatever was painted stays.
			log.warn("Metafile only partly drawn: " + t);
		}
	}

	/** Within 2%: the frames are the same shape, so the header's frame is the one used. */
	private static boolean sameAspect(Rectangle2D a, Rectangle2D b) {
		double aw = Math.abs(a.getWidth()), ah = Math.abs(a.getHeight());
		double bw = Math.abs(b.getWidth()), bh = Math.abs(b.getHeight());
		if (!(aw > 0) || !(ah > 0) || !(bw > 0) || !(bh > 0)) return false;
		return Math.abs((aw / ah) - (bw / bh)) <= 0.02 * (bw / bh);
	}

	@Override
	public BufferedImage toImage(byte[] data, double dpi) {
		Object picture = parse(data);
		if (picture == null) return null;
		Rectangle2D b = boundsInPoints(picture);
		if (b == null) return null;
		if (!(dpi > 0) || !Double.isFinite(dpi)) dpi = 96;

		int w = clamp(Math.abs(b.getWidth()) * dpi / 72d);
		int h = clamp(Math.abs(b.getHeight()) * dpi / 72d);

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			draw(picture, g, new Rectangle2D.Double(0, 0, w, h));
		} finally {
			g.dispose();
		}
		return img;
	}

	/** so that a degenerate or hostile metafile cannot ask for an unbounded image */
	private static final int MIN_PX = 1;
	private static final int MAX_PX = 5000;

	private static int clamp(double v) {
		if (!Double.isFinite(v) || v <= 0) return MIN_PX;
		return Math.max(MIN_PX, Math.min(MAX_PX, (int)Math.rint(v)));
	}

	/**
	 * The metafile as PNG bytes, for an {@code <img src="data:...">} or for a
	 * {@link ConversionImageHandler} that writes files.
	 *
	 * @return null if the metafile cannot be rendered
	 */
	public byte[] toPngBytes(byte[] data, double dpi) {
		BufferedImage img = toImage(data, dpi);
		if (img == null) return null;
		try {
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			if (!javax.imageio.ImageIO.write(img, "png", out)) return null;
			return out.toByteArray();
		} catch (Throwable t) {
			log.warn("Could not encode the metafile as PNG: " + t);
			return null;
		}
	}
}
