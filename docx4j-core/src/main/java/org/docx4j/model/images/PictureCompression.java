/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A picture as Word's Compress Pictures would leave it: cropped areas deleted, and the
 * bitmap resampled to a resolution for the size it is shown at (CR-029).
 *
 * <p>Word offers High fidelity (the original), HD 330 ppi, Print 220 ppi, Web 150 ppi and
 * E-mail 96 ppi, and a document default (File &gt; Options &gt; Advanced &gt; Image Size
 * and Quality) which {@code word/settings.xml} records as {@code w14:defaultImageDpi},
 * with {@code w:doNotAutoCompressPictures} for "Do not compress images in file".</p>
 *
 * <p>Resampling only ever reduces: a bitmap at or below the target is left as it is. A
 * JPEG stays a JPEG, anything else becomes a PNG (which keeps transparency). A picture
 * ImageIO cannot read (a CMYK JPEG, an animated GIF, a metafile) is left alone.</p>
 *
 * @since 17.3.0
 */
public final class PictureCompression {

	private static final Logger log = LoggerFactory.getLogger(PictureCompression.class);

	/**
	 * The resolution pictures are drawn at in PDF output:
	 * <ul>
	 * <li>{@code word} (the default): as Word's PDF export draws them, a picture above
	 *     300 pixels per inch at the size it is shown resampled to 200, the rest left as
	 *     they are. Measured 2026-09-29 on the Word PDFs of docx4j's real-document corpus
	 *     (124 pictures paired with the stored ones): every picture Word resampled came out
	 *     at 200 ppi, from 398 ppi and up; every one it kept was 300 ppi or less. Word does
	 *     this whatever the document's own compression settings say, which govern Compress
	 *     Pictures when Word saves, not its PDF export. It also re-encodes a JPEG it does not
	 *     resample, at quality 75; so does this, where that saves a tenth of the bytes.</li>
	 * <li>{@code document}: as Word's Compress Pictures would with the document's settings:
	 *     none where it says not to compress ({@code w:doNotAutoCompressPictures}), else
	 *     its default resolution ({@code w14:defaultImageDpi}, 0 there being High
	 *     fidelity), else 220.</li>
	 * <li>{@code high-fidelity}: the original bitmap.</li>
	 * <li>a number of pixels per inch (Word's choices are 330, 220, 150 and 96): any picture
	 *     above it resampled to it.</li>
	 * </ul>
	 */
	public static final String RESOLUTION_PROPERTY = "docx4j.convert.out.fo.images.resolution";

	/** Whether a picture's crop is applied ({@code true}, the default); {@code false} draws the
	 *  whole bitmap stretched into the frame, as docx4j did before 17.3.0. */
	public static final String CROP_PROPERTY = "docx4j.convert.out.fo.images.crop";

	/** The JPEG quality (0 to 1) a resampled or cropped JPEG is written at: 0.75 by default,
	 *  which is what Word's PDF export uses (its JPEGs' quantisation tables say quality 75,
	 *  the cropped ones included). */
	public static final String JPEG_QUALITY_PROPERTY = "docx4j.convert.out.fo.images.jpegQuality";

	/** Word's Compress Pictures default resolution when the document names none (Print). */
	public static final int DEFAULT_PPI = 220;

	/** Word's PDF export: pictures above {@link #WORD_PDF_THRESHOLD} ppi are drawn at this. */
	public static final int WORD_PDF_PPI = 200;

	/** Word's PDF export resamples a picture only above this many pixels per inch. */
	public static final int WORD_PDF_THRESHOLD = 300;

	/** Below this reduction in pixels, a picture is not re-encoded for its resolution alone. */
	private static final double WORTH_RESAMPLING = 0.9;

	private PictureCompression() {
	}

	/** A resolution rule: pictures above {@link #threshold} pixels per inch (at the size they
	 *  are shown) are resampled to {@link #target}; a target of 0 is high fidelity. */
	public static final class Resolution {
		public static final Resolution HIGH_FIDELITY = new Resolution(0, 0);
		public final int target, threshold;
		/** Whether a JPEG left at its size is still re-encoded at the JPEG quality, where that
		 *  makes it smaller by a tenth: Word's PDF export does (for 402 JPEGs at the same pixel
		 *  size in its PDFs and docx4j's, Word's came to 15.1 MB and the originals to 22.6). */
		public final boolean reencodeJpeg;

		public Resolution(int target, int threshold) {
			this(target, threshold, false);
		}

		public Resolution(int target, int threshold, boolean reencodeJpeg) {
			this.target = target;
			this.threshold = threshold;
			this.reencodeJpeg = reencodeJpeg;
		}

		public boolean isHighFidelity() {
			return target <= 0;
		}

		@Override
		public String toString() {
			return isHighFidelity() ? "high-fidelity" : target + "ppi>" + threshold + (reencodeJpeg ? "+q" : "");
		}
	}

	/** The resolution rule for this package's pictures in PDF output; see {@link #RESOLUTION_PROPERTY}. */
	public static Resolution resolution(WordprocessingMLPackage pkg) {
		return resolution(pkg, null);
	}

	/**
	 * The resolution rule for this package's pictures: {@code spec} (one of the values of
	 * {@link #RESOLUTION_PROPERTY}) where it is given, else the property.
	 */
	public static Resolution resolution(WordprocessingMLPackage pkg, String spec) {
		String p = (spec != null ? spec : Docx4jProperties.getProperty(RESOLUTION_PROPERTY, "word"))
				.trim().toLowerCase(Locale.ROOT);
		if (p.isEmpty() || p.equals("word")) return new Resolution(WORD_PDF_PPI, WORD_PDF_THRESHOLD, true);
		if (p.equals("high-fidelity") || p.equals("high fidelity")) return Resolution.HIGH_FIDELITY;
		if (p.equals("document")) return documentResolution(pkg);
		try {
			int ppi = Integer.parseInt(p);
			if (ppi > 0) return new Resolution(ppi, ppi);
			if (ppi == 0) return Resolution.HIGH_FIDELITY;
		} catch (NumberFormatException e) {
			// fall through
		}
		log.warn(RESOLUTION_PROPERTY + "=" + p + " is not word, document, high-fidelity or a number; using word");
		return new Resolution(WORD_PDF_PPI, WORD_PDF_THRESHOLD, true);
	}

	/**
	 * What Word's Compress Pictures would do with this document's settings: nothing where
	 * it says not to compress, else its {@code w14:defaultImageDpi} (0 being High
	 * fidelity), else {@link #DEFAULT_PPI}.
	 */
	public static Resolution documentResolution(WordprocessingMLPackage pkg) {
		CTSettings settings = settings(pkg);
		if (settings != null) {
			if (settings.getDoNotAutoCompressPictures() != null && settings.getDoNotAutoCompressPictures().isVal()) {
				return Resolution.HIGH_FIDELITY;
			}
			if (settings.getDefaultImageDpi() != null && settings.getDefaultImageDpi().getVal() != null) {
				int v = settings.getDefaultImageDpi().getVal().intValue();
				return (v <= 0 || v >= 32767) ? Resolution.HIGH_FIDELITY : new Resolution(v, v);
			}
		}
		return new Resolution(DEFAULT_PPI, DEFAULT_PPI);
	}

	private static CTSettings settings(WordprocessingMLPackage pkg) {
		try {
			if (pkg == null || pkg.getMainDocumentPart() == null) return null;
			DocumentSettingsPart dsp = pkg.getMainDocumentPart().getDocumentSettingsPart();
			return dsp == null ? null : dsp.getContents();
		} catch (Exception e) {
			log.warn("Couldn't read the document settings: " + e.getMessage());
			return null;
		}
	}

	/** Whether crops are applied; see {@link #CROP_PROPERTY}. */
	public static boolean cropEnabled() {
		return Docx4jProperties.getProperty(CROP_PROPERTY, true);
	}

	private static float jpegQuality() {
		String q = Docx4jProperties.getProperty(JPEG_QUALITY_PROPERTY, "0.75");
		try {
			float f = Float.parseFloat(q.trim());
			if (f > 0 && f <= 1) return f;
		} catch (NumberFormatException e) {
			// fall through
		}
		log.warn(JPEG_QUALITY_PROPERTY + "=" + q + " is not a number from 0 to 1; using 0.75");
		return 0.75f;
	}

	/** A picture's new bitmap. */
	public static final class Result {
		public final byte[] bytes;
		/** image/jpeg or image/png */
		public final String contentType;
		/** jpg or png */
		public final String extension;
		public final int width, height;

		Result(byte[] bytes, boolean jpeg, int width, int height) {
			this.bytes = bytes;
			this.contentType = jpeg ? "image/jpeg" : "image/png";
			this.extension = jpeg ? "jpg" : "png";
			this.width = width;
			this.height = height;
		}
	}

	/**
	 * The picture cropped and resampled.
	 *
	 * @param bytes the stored bitmap
	 * @param crop what of it is drawn ({@link PictureCrop#NONE} for all of it)
	 * @param widthPt the frame's width, in points; 0 where not known (no resampling then)
	 * @param heightPt the frame's height, in points
	 * @param resolution the rule (a picture above its threshold is resampled to its target)
	 * @return the new bitmap; or null where nothing changes (no crop, and the bitmap is not
	 *         enough above the resolution to be worth re-encoding, or re-encoding would not
	 *         make it smaller), or where the bitmap cannot be read
	 */
	public static Result compress(byte[] bytes, PictureCrop crop, double widthPt, double heightPt, Resolution resolution) {
		int ppi = (resolution == null || resolution.isHighFidelity()) ? 0 : resolution.target;
		// only the edges which move in are cut from the bitmap; one which moves out is
		// where the picture sits in its frame (PictureCrop#framePadding), not pixels
		if (crop != null) crop = crop.positive();
		boolean cropping = crop != null && !crop.isNone();
		boolean sized = widthPt > 0 && heightPt > 0;
		boolean reencode = resolution != null && resolution.reencodeJpeg;
		if (bytes == null || (!cropping && !reencode && (ppi <= 0 || !sized))) return null;

		BufferedImage image;
		String format;
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (!readers.hasNext()) return null; // not a bitmap ImageIO knows (eg a metafile)
			ImageReader reader = readers.next();
			try {
				reader.setInput(in);
				format = reader.getFormatName().toLowerCase(Locale.ROOT);
				if (format.equals("gif") && reader.getNumImages(true) > 1) return null; // animated
				image = reader.read(0);
			} finally {
				reader.dispose();
			}
		} catch (Exception | LinkageError e) {
			// a CMYK or YCCK JPEG, a TIFF variant ImageIO lacks: leave the picture as it is
			log.info("Picture left as stored (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
			return null;
		}
		if (image == null) return null;

		if (cropping) image = crop(image, crop);

		int w = image.getWidth(), h = image.getHeight();
		int tw = w, th = h;
		if (ppi > 0 && sized && Math.max(w / (widthPt / 72d), h / (heightPt / 72d)) > resolution.threshold) {
			// rounded down, as Word does: measured on its PDFs, a frame 402.5 px wide at 200 ppi
			// is 402 px there, and 902.97 is 902 (15 of 28 resampled corpus pictures; rounding
			// to nearest matched 1)
			tw = Math.min(w, Math.max(1, (int) Math.floor(widthPt / 72d * ppi + 1e-6)));
			th = Math.min(h, Math.max(1, (int) Math.floor(heightPt / 72d * ppi + 1e-6)));
		}
		boolean resampling = tw < w || th < h;
		boolean isJpeg = format.equals("jpeg") || format.equals("jpg");
		boolean worthResampling = resampling && (double) tw * th / ((double) w * h) <= WORTH_RESAMPLING;
		boolean reencoding = !cropping && !worthResampling && reencode && isJpeg;
		if (!cropping && !worthResampling && !reencoding) return null;
		if (worthResampling) image = scale(image, tw, th);

		boolean jpeg = isJpeg && !image.getColorModel().hasAlpha();
		byte[] out;
		try {
			out = jpeg ? writeJpeg(image, jpegQuality()) : writePng(image);
		} catch (Exception e) {
			log.warn("Couldn't write the resampled picture (" + e.getMessage() + "); left as stored");
			return null;
		}
		if (out == null) return null;
		// resolution alone is about size: never make the picture bigger for it; and a JPEG
		// re-encoded at its own size is kept only where that saves a tenth, which leaves one
		// already at (or below) the quality alone
		if (!cropping && out.length >= bytes.length) return null;
		if (reencoding && out.length > bytes.length * 0.9) return null;
		return new Result(out, jpeg, image.getWidth(), image.getHeight());
	}

	/** The cropped bitmap (the crop's edges all move in). */
	static BufferedImage crop(BufferedImage src, PictureCrop c) {
		int w = src.getWidth(), h = src.getHeight();
		double x0 = c.left * w, y0 = c.top * h;
		double x1 = w - c.right * w, y1 = h - c.bottom * h;
		int nw = Math.max(1, (int) Math.round(x1 - x0)), nh = Math.max(1, (int) Math.round(y1 - y0));
		boolean alpha = src.getColorModel().hasAlpha();
		BufferedImage out = new BufferedImage(nw, nh, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		Graphics2D g = out.createGraphics();
		try {
			g.drawImage(src, (int) Math.round(-x0), (int) Math.round(-y0), null);
		} finally {
			g.dispose();
		}
		return out;
	}

	/** Resampled down to tw x th, halving at most at each step so every source pixel counts. */
	static BufferedImage scale(BufferedImage src, int tw, int th) {
		int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage cur = src;
		int w = src.getWidth(), h = src.getHeight();
		do {
			w = (w > tw) ? Math.max(tw, w / 2) : tw;
			h = (h > th) ? Math.max(th, h / 2) : th;
			BufferedImage next = new BufferedImage(w, h, type);
			Graphics2D g = next.createGraphics();
			try {
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.drawImage(cur, 0, 0, w, h, null);
			} finally {
				g.dispose();
			}
			cur = next;
		} while (w != tw || h != th);
		return cur;
	}

	private static byte[] writeJpeg(BufferedImage image, float quality) throws java.io.IOException {
		BufferedImage rgb = image;
		if (image.getType() != BufferedImage.TYPE_INT_RGB) {
			rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = rgb.createGraphics();
			try {
				g.drawImage(image, 0, 0, null);
			} finally {
				g.dispose();
			}
		}
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
		if (!writers.hasNext()) return null;
		ImageWriter writer = writers.next();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ImageOutputStream out = ImageIO.createImageOutputStream(baos)) {
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
			writer.setOutput(out);
			writer.write(null, new IIOImage(rgb, null, null), param);
		} finally {
			writer.dispose();
		}
		return baos.toByteArray();
	}

	private static byte[] writePng(BufferedImage image) throws java.io.IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (!ImageIO.write(image, "png", baos)) return null;
		return baos.toByteArray();
	}
}
