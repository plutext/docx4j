/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.model.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.docx4j.XmlUtils;
import org.docx4j.dml.picture.Pic;
import org.junit.Test;

/**
 * A picture's colour adjustments ({@code a:blip}'s children): {@link PictureRecolour} reads them in the order Word
 * applies them and applies them in CR-029's step, crop, recolour, resample (CR-084 phase 1; Word's arithmetic is
 * {@link PictureRecolourWordTest}'s). In HTML output they aren't drawn yet (CR-084 phase 4), and are reported as
 * {@code render.effect.blip_recolour} (CR-081 D9 and phase 6), the first one named.
 */
public class PictureRecolourTest {

	private static final String NS = " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";

	private static Pic pic(String blipChildren) throws Exception {
		String xml = "<pic:pic" + NS + "><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"p\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"rId1\">" + blipChildren + "</a:blip><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"914400\" cy=\"914400\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>";
		return (Pic) XmlUtils.unwrap(XmlUtils.unmarshalString(xml));
	}

	@Test
	public void aPictureWithoutAdjustmentsIsNotReported() throws Exception {
		assertNull(PictureEffects.recoloured(pic("")));
		assertNull(PictureEffects.recoloured(null));
	}

	@Test
	public void theFirstAdjustmentIsNamed() throws Exception {
		assertEquals("grayscl", PictureEffects.recoloured(pic("<a:grayscl/>")));
		assertEquals("alphaModFix", PictureEffects.recoloured(pic("<a:alphaModFix amt=\"50000\"/><a:lum bright=\"20000\"/>")));
		assertEquals("duotone", PictureEffects.recoloured(pic("<a:duotone><a:prstClr val=\"black\"/><a:srgbClr val=\"FFFFFF\"/></a:duotone>")));
	}

	@Test
	public void noAdjustmentIsNone() throws Exception {
		assertSame(PictureRecolour.NONE, PictureRecolour.of(pic("").getBlipFill().getBlip(), (org.docx4j.model.color.ThemeColors) null));
		assertSame(PictureRecolour.NONE, PictureRecolour.of(null, (org.docx4j.model.color.ThemeColors) null));
		assertTrue(PictureRecolour.NONE.isNone());
		assertEquals("", PictureRecolour.NONE.signature());
		// an adjustment that changes nothing
		assertTrue(recolour("<a:lum bright=\"0\" contrast=\"0\"/><a:alphaModFix amt=\"100000\"/>").isNone());
	}

	private static PictureRecolour recolour(String children) throws Exception {
		return PictureRecolour.of(pic(children).getBlipFill().getBlip(), (org.docx4j.model.color.ThemeColors) null);
	}

	@Test
	public void writtenOrderButBiLevelBeforeDuotone() throws Exception {
		String duotone = "<a:duotone><a:prstClr val=\"black\"/><a:srgbClr val=\"FF0000\"/></a:duotone>";
		assertEquals("grayscl;lum30000,30000", recolour("<a:grayscl/><a:lum bright=\"30000\" contrast=\"30000\"/>").signature());
		assertEquals("lum30000,30000;grayscl", recolour("<a:lum bright=\"30000\" contrast=\"30000\"/><a:grayscl/>").signature());
		PictureRecolour a = recolour("<a:biLevel thresh=\"50000\"/>" + duotone);
		PictureRecolour b = recolour(duotone + "<a:biLevel thresh=\"50000\"/>");
		assertEquals(a.signature(), b.signature());
		assertTrue(a.signature().startsWith("biLevel50000;duotone"));
	}

	@Test
	public void whatCanMakeAPictureTransparent() throws Exception {
		assertFalse(recolour("<a:grayscl/>").mayAddAlpha());
		assertTrue(recolour("<a:alphaModFix amt=\"50000\"/>").mayAddAlpha());
		assertTrue(recolour("<a:clrChange><a:clrFrom><a:srgbClr val=\"FFFFFF\"/></a:clrFrom>"
				+ "<a:clrTo><a:srgbClr val=\"FFFFFF\"><a:alpha val=\"0\"/></a:srgbClr></a:clrTo></a:clrChange>").mayAddAlpha());
		assertFalse(recolour("<a:clrChange useA=\"0\"><a:clrFrom><a:srgbClr val=\"FFFFFF\"/></a:clrFrom>"
				+ "<a:clrTo><a:srgbClr val=\"FFFFFF\"><a:alpha val=\"0\"/></a:srgbClr></a:clrTo></a:clrChange>").mayAddAlpha());
	}

	@Test
	public void aGreyPicturesLevelsAreTheStoredOnes() throws Exception {
		// getRGB would lighten a grey picture's 128 to 188 (it takes the levels as linear); Word doesn't
		BufferedImage grey = new BufferedImage(2, 1, BufferedImage.TYPE_BYTE_GRAY);
		grey.getRaster().setSample(0, 0, 0, 128);
		grey.getRaster().setSample(1, 0, 0, 30);
		ByteArrayOutputStream png = new ByteArrayOutputStream();
		ImageIO.write(grey, "png", png);
		BufferedImage read = ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
		BufferedImage out = recolour("<a:grayscl/>").apply(read, 0);
		assertEquals(0x808080, out.getRGB(0, 0) & 0xFFFFFF);
		assertEquals(0x1E1E1E, out.getRGB(1, 0) & 0xFFFFFF);
		assertEquals("an opaque result is RGB", BufferedImage.TYPE_INT_RGB, out.getType());
	}

	@Test
	public void aFullyTransparentPixelHasNoColour() throws Exception {
		BufferedImage im = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
		im.setRGB(0, 0, 0x00FF0000); // red at alpha 0: Word stores it premultiplied, so black
		im.setRGB(1, 0, 0x80FF0000);
		BufferedImage out = recolour("<a:alphaRepl a=\"50000\"/>").apply(im, 0);
		assertEquals(0x80000000, out.getRGB(0, 0));
		assertEquals(0x80FF0000, out.getRGB(1, 0));
		assertEquals(BufferedImage.TYPE_INT_ARGB, out.getType());
	}

	/** Set Transparent Color's tolerance is the picture format's (phase 1b): a JPEG's 16 levels, a GIF's none, a PNG's 1
	 *  (none with useA="0") */
	@Test
	public void setTransparentColoursToleranceIsTheFormats() throws Exception {
		String toClear = "<a:clrChange><a:clrFrom><a:srgbClr val=\"FFFFFF\"/></a:clrFrom>"
				+ "<a:clrTo><a:srgbClr val=\"FFFFFF\"><a:alpha val=\"0\"/></a:srgbClr></a:clrTo></a:clrChange>";
		int[] colours = { 0xFFFFFF, 0xFEFFFF, 0xFDFFFF, 0xEFFFFF, 0xEEFFFF };
		BufferedImage im = new BufferedImage(colours.length, 1, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < colours.length; i++) im.setRGB(i, 0, colours[i]);
		assertEquals("png", "11000", cleared(recolour(toClear).apply(im, 0, "png")));
		assertEquals("no format: a lossless picture's", "11000", cleared(recolour(toClear).apply(im, 0)));
		assertEquals("jpeg", "11110", cleared(recolour(toClear).apply(im, 0, "jpeg")));
		assertEquals("gif", "10000", cleared(recolour(toClear).apply(im, 0, "gif")));
		String toBlueNoA = "<a:clrChange useA=\"0\"><a:clrFrom><a:srgbClr val=\"FFFFFF\"/></a:clrFrom>"
				+ "<a:clrTo><a:srgbClr val=\"0000FF\"/></a:clrTo></a:clrChange>";
		BufferedImage blue = recolour(toBlueNoA).apply(im, 0, "png");
		assertEquals(0xFF0000FF, blue.getRGB(0, 0));
		assertEquals("useA 0 on a PNG: the exact colour only", 0xFFFEFFFF, blue.getRGB(1, 0));
	}

	/** Which pixels are transparent, as 1s and 0s */
	private static String cleared(BufferedImage im) {
		StringBuilder b = new StringBuilder();
		for (int x = 0; x < im.getWidth(); x++) b.append((im.getRGB(x, 0) >>> 24) == 0 ? '1' : '0');
		return b.toString();
	}

	@Test
	public void aBlurIsAGaussianAndReported() throws Exception {
		BufferedImage im = new BufferedImage(40, 1, BufferedImage.TYPE_INT_RGB);
		for (int x = 20; x < 40; x++) im.setRGB(x, 0, 0xFFFFFF);
		// 2 pt at 1 pixel a point: sigma 2/3 pt
		BufferedImage out = recolour("<a:blur rad=\"25400\"/>").apply(im, 1);
		int left = out.getRGB(19, 0) & 0xFF, right = out.getRGB(20, 0) & 0xFF;
		assertTrue(left > 0 && left < 128 && right > 128 && right < 255);
		assertEquals(0, out.getRGB(0, 0) & 0xFF);
	}

	/** A 64 by 64 picture of four colours, the bytes in the format given */
	private static byte[] picture(String format) throws Exception {
		BufferedImage im = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) im.setRGB(x, y, x < 32 ? (y < 32 ? 0xFF0000 : 0x00FF00) : (y < 32 ? 0x0000FF : 0xFFFFFF));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(im, format, out);
		return out.toByteArray();
	}

	@Test
	public void compressAppliesTheAdjustmentsBetweenTheCropAndTheResampling() throws Exception {
		PictureCompression.Resolution hifi = PictureCompression.Resolution.HIGH_FIDELITY;
		byte[] png = picture("png"), jpeg = picture("jpeg");
		// nothing to do: the picture as stored
		assertNull(PictureCompression.compress(png, PictureCrop.NONE, PictureRecolour.NONE, 48, 48, hifi));
		// a JPEG adjusted stays a JPEG; made transparent, a PNG
		PictureCompression.Result r = PictureCompression.compress(jpeg, PictureCrop.NONE, recolour("<a:grayscl/>"), 48, 48, hifi);
		assertNotNull(r);
		assertEquals("image/jpeg", r.contentType);
		r = PictureCompression.compress(jpeg, PictureCrop.NONE, recolour("<a:alphaModFix amt=\"50000\"/>"), 48, 48, hifi);
		assertEquals("image/png", r.contentType);
		BufferedImage im = ImageIO.read(new ByteArrayInputStream(r.bytes));
		assertTrue(Math.abs((im.getRGB(10, 10) >>> 24) - 128) <= 1);
		// cropped to the top left quarter, then adjusted: red's luma
		r = PictureCompression.compress(png, new PictureCrop(0, 0, 0.5, 0.5), recolour("<a:grayscl/>"), 48, 48, hifi);
		im = ImageIO.read(new ByteArrayInputStream(r.bytes));
		assertEquals(32, im.getWidth());
		assertEquals(0x363636, im.getRGB(16, 16) & 0xFFFFFF); // 0.2126 * 255 = 54.2
		// then resampled: 64 px in 9 pt is 512 ppi, above Word's 300, so 200 ppi: 25 px
		r = PictureCompression.compress(png, PictureCrop.NONE, recolour("<a:grayscl/>"), 9, 9, PictureCompression.resolution(null, "word"));
		assertEquals(25, r.width);
	}

	/** Two pictures differing only by their extension, compressed alike, get two files (phase 1b's round found one
	 *  overwriting the other) */
	@Test
	public void aDerivedPicturesNameKeepsItsSourcesExtension() {
		String png = AbstractConversionImageHandler.derivedName("/word/media/logo.png", "v", "png");
		String gif = AbstractConversionImageHandler.derivedName("/word/media/logo.gif", "v", "png");
		assertFalse(png.equals(gif));
		assertTrue(png, png.startsWith("/word/media/logo-png-") && png.endsWith(".png"));
		assertTrue(AbstractConversionImageHandler.derivedName(null, "v", "jpg").startsWith("/word/media/picture-"));
	}

	/** A wide blur is computed on a reduced copy (a review of CR-084: a radius at the schema's maximum took minutes);
	 *  within 6 levels of the exact Gaussian, a fifth of a level on average */
	@Test
	public void aWideBlurIsComputedReducedNearTheExact() {
		int w = 120, h = 90;
		int[] src = new int[w * h];
		for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
			boolean in = x > 30 && x < 80 && y > 20 && y < 60;
			src[y * w + x] = in ? 0xFFC00000 : (x > 100 ? 0x800000FF : 0xFFFFFFFF);
		}
		for (double sigma : new double[] { 12, 20, 30, 60 }) {
			int[] exact = src.clone(), reduced = src.clone();
			PictureRecolour.Blur.blur(exact, w, h, sigma, 1e9);
			PictureRecolour.Blur.blur(reduced, w, h, sigma, PictureRecolour.Blur.MAX_SIGMA);
			int worst = 0;
			long sum = 0;
			for (int i = 0; i < exact.length; i++) for (int sh = 0; sh < 32; sh += 8) {
				int d = Math.abs(((exact[i] >>> sh) & 0xFF) - ((reduced[i] >>> sh) & 0xFF));
				worst = Math.max(worst, d);
				sum += d;
			}
			// the worst at a corner, where the edge is extended from a block rather than a pixel
			assertTrue("sigma " + sigma + ": " + worst + " levels", worst <= 6);
			assertTrue("sigma " + sigma + ": mean " + sum / (4.0 * exact.length), sum / (4.0 * exact.length) < 0.25);
		}
		// at or below MAX_SIGMA, exactly as before
		int[] a = src.clone(), b = src.clone();
		PictureRecolour.Blur.blur(a, w, h, 6, 1e9);
		PictureRecolour.Blur.blur(b, w, h, 6, PictureRecolour.Blur.MAX_SIGMA);
		assertTrue(java.util.Arrays.equals(a, b));
	}

	@Test(timeout = 30000)
	public void aBlurAtTheSchemasMaximumIsQuick() throws Exception {
		BufferedImage im = new BufferedImage(1500, 1500, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 1500; y++) for (int x = 750; x < 1500; x++) im.setRGB(x, y, 0xFFFFFF);
		BufferedImage out = recolour("<a:blur rad=\"27273042316900\"/>").apply(im, 1);
		assertEquals(1500, out.getWidth());
		// blurred over the whole picture: mid-grey at its middle
		assertTrue(Math.abs((out.getRGB(750, 750) & 0xFF) - 128) < 40);
	}

	/** A bitmap claiming more pixels than the bound is read subsampled, its crop and adjustments as before */
	@Test
	public void compressReadsALargeBitmapSubsampled() throws Exception {
		BufferedImage im = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 300; y++) for (int x = 0; x < 400; x++) im.setRGB(x, y, x < 200 ? 0xFF0000 : 0x0000FF);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(im, "png", bytes);
		PictureCompression.Result r = PictureCompression.compress(bytes.toByteArray(), new PictureCrop(0, 0, 0.5, 0),
				recolour("<a:grayscl/>"), 300, 225, PictureCompression.Resolution.HIGH_FIDELITY, 10000);
		assertNotNull(r);
		// 120,000 px at most 10,000: read at a quarter, 100 by 75, then the left half
		assertEquals(50, r.width);
		assertEquals(75, r.height);
		BufferedImage out = ImageIO.read(new ByteArrayInputStream(r.bytes));
		assertEquals(0x363636, out.getRGB(25, 37) & 0xFFFFFF);
		// within the bound, read whole
		r = PictureCompression.compress(bytes.toByteArray(), PictureCrop.NONE, recolour("<a:grayscl/>"), 300, 225,
				PictureCompression.Resolution.HIGH_FIDELITY, 120000);
		assertEquals(400, r.width);
	}

	/** VML's chromakey: six or three hex digits, a name, rgb(); a colour it can't read is reported, not dropped */
	@Test
	public void aVmlChromakeysColour() {
		assertEquals("FFFFFF", PictureRecolour.vmlColour("#FFF"));
		assertEquals("AABBCC", PictureRecolour.vmlColour("abc"));
		assertEquals("FF0080", PictureRecolour.vmlColour("rgb(255, 0, 128)"));
		assertEquals("FFFFFF", PictureRecolour.vmlColour("white"));
		assertEquals("FFFFFF", PictureRecolour.vmlColour("#FFFFFF [9]"));
		assertNull(PictureRecolour.vmlColour("rgb(300,0,0)"));
		assertNull(PictureRecolour.vmlColour("fill darken(50)"));
		org.docx4j.vml.CTImageData d = new org.docx4j.vml.CTImageData();
		d.setChromakey("#FFF");
		assertTrue(PictureRecolour.ofVml(d).signature(), PictureRecolour.ofVml(d).signature().contains("FFFFFF"));
		d.setChromakey("fill darken(50)");
		PictureRecolour unread = PictureRecolour.ofVml(d);
		assertTrue(unread.isNone());
		assertFalse(unread == PictureRecolour.NONE); // it carries the departure, reported
		d.setChromakey("none");
		assertSame(PictureRecolour.NONE, PictureRecolour.ofVml(d));
	}
}
