/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.dml.CTRelativeRect;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.images.PictureCompression;
import org.docx4j.model.images.PictureCrop;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.After;
import org.junit.Test;

/**
 * Pictures in PDF output as Word's Compress Pictures leaves them (CR-029): the crop
 * applied, and the bitmap resampled to the document's resolution for the size it is
 * shown at. Read back from the image files the FO points at.
 *
 * @since 17.3.0
 */
public class PictureCompressionTest {

	private static final long EMU_PER_INCH = 914400;

	@After
	public void restore() {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "word");
		Docx4jProperties.setProperty(PictureCompression.CROP_PROPERTY, "true");
	}

	// ------------------------------------------------------------------ bitmaps

	/** 400 x 400: red top left, green top right, blue bottom left, yellow bottom right. */
	private static byte[] quadrants() throws Exception {
		BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.RED); g.fillRect(0, 0, 200, 200);
		g.setColor(Color.GREEN); g.fillRect(200, 0, 200, 200);
		g.setColor(Color.BLUE); g.fillRect(0, 200, 200, 200);
		g.setColor(Color.YELLOW); g.fillRect(200, 200, 200, 200);
		g.dispose();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		return baos.toByteArray();
	}

	/** photo(), at JPEG quality 0.95. */
	private static byte[] photoQ95() throws Exception {
		BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(photo()));
		javax.imageio.ImageWriter w = ImageIO.getImageWritersByFormatName("jpeg").next();
		javax.imageio.ImageWriteParam param = w.getDefaultWriteParam();
		param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(0.95f);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (javax.imageio.stream.ImageOutputStream out = ImageIO.createImageOutputStream(baos)) {
			w.setOutput(out);
			w.write(null, new javax.imageio.IIOImage(img, null, null), param);
		}
		w.dispose();
		return baos.toByteArray();
	}

	/** A 2000 x 1000 JPEG with enough detail that resampling it saves bytes. */
	private static byte[] photo() throws Exception {
		BufferedImage img = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB);
		Random rnd = new Random(29);
		for (int y = 0; y < 1000; y++) {
			for (int x = 0; x < 2000; x++) {
				int n = rnd.nextInt(40);
				img.setRGB(x, y, new Color((x / 8 + n) % 256, (y / 4 + n) % 256, (x + y) / 12 % 256).getRGB());
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpeg", baos);
		return baos.toByteArray();
	}

	// ------------------------------------------------------------------ documents

	private static WordprocessingMLPackage pkg() throws Exception {
		return WordprocessingMLPackage.createPackage();
	}

	/** Adds a paragraph holding the picture, cx x cy inches, with this srcRect (or none). */
	private static void addPicture(WordprocessingMLPackage pkg, BinaryPartAbstractImage part,
			double inchesWide, double inchesHigh, CTRelativeRect srcRect) throws Exception {
		Inline inline = part.createImageInline("hint", "alt", 1, 2,
				Math.round(inchesWide * EMU_PER_INCH), Math.round(inchesHigh * EMU_PER_INCH), false);
		if (srcRect != null) inline.getGraphic().getGraphicData().getPic().getBlipFill().setSrcRect(srcRect);
		ObjectFactory f = new ObjectFactory();
		P p = f.createP();
		R r = f.createR();
		Drawing d = f.createDrawing();
		d.getAnchorOrInline().add(inline);
		r.getContent().add(d);
		p.getContent().add(r);
		pkg.getMainDocumentPart().getContent().add(p);
	}

	private static CTRelativeRect srcRect(int l, int t, int r, int b) {
		CTRelativeRect rect = new CTRelativeRect();
		rect.setL(l); rect.setT(t); rect.setR(r); rect.setB(b);
		return rect;
	}

	private static CTSettings settings(WordprocessingMLPackage pkg) throws Exception {
		return pkg.getMainDocumentPart().getDocumentSettingsPart().getContents();
	}

	/** The image files the FO points at, in document order. */
	private static List<File> pictures(WordprocessingMLPackage pkg) throws Exception {
		return pictures(pkg, null, null);
	}

	private static List<File> pictures(WordprocessingMLPackage pkg, String resolution, Boolean crop) throws Exception {
		File dir = Files.createTempDirectory("cr029").toFile();
		dir.deleteOnExit();
		FOSettings settings = new FOSettings(pkg);
		settings.setImageResolution(resolution);
		settings.setImageCrop(crop);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		settings.setImageDirPath(dir.getPath());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		Matcher m = Pattern.compile("external-graphic[^>]*?src=\"([^\"]+)\"").matcher(baos.toString("UTF-8"));
		List<File> files = new ArrayList<File>();
		while (m.find()) {
			String src = m.group(1).replaceAll("^url\\(['\"]?|['\"]?\\)$", "");
			files.add(new File(new URI(src)));
		}
		assertTrue("the fo draws a picture", files.size() > 0);
		return files;
	}

	/** The first fo:external-graphic element of the FO, as text. */
	private static String graphic(WordprocessingMLPackage pkg) throws Exception {
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		settings.setImageDirPath(Files.createTempDirectory("cr029").toFile().getPath());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		Matcher m = Pattern.compile("<(?:fo:)?external-graphic[^>]*>").matcher(baos.toString("UTF-8"));
		assertTrue("an external-graphic", m.find());
		return m.group();
	}

	private static BufferedImage read(File f) throws Exception {
		BufferedImage img = ImageIO.read(f);
		assertTrue("readable: " + f, img != null);
		return img;
	}

	private static boolean near(int rgb, Color c) {
		Color x = new Color(rgb, true);
		return Math.abs(x.getRed() - c.getRed()) < 40 && Math.abs(x.getGreen() - c.getGreen()) < 40
				&& Math.abs(x.getBlue() - c.getBlue()) < 40;
	}

	// ------------------------------------------------------------------ crops

	@Test
	public void theCropIsWhatIsDrawn() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		// keep the top right quadrant: 50% off the left, 50% off the bottom
		addPicture(pkg, part, 1, 1, srcRect(50000, 0, 0, 50000));
		BufferedImage img = read(pictures(pkg).get(0));
		assertEquals(200, img.getWidth());
		assertEquals(200, img.getHeight());
		assertTrue("green, the top right quadrant", near(img.getRGB(100, 100), Color.GREEN));
		assertTrue("green at the corner too", near(img.getRGB(5, 195), Color.GREEN));
	}

	@Test
	public void aNegativeEdgeInsetsThePictureInItsFrame() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "high-fidelity"); // the geometry alone
		byte[] original = quadrants();
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		// the left edge moves out by a quarter of the bitmap: Word draws the picture inset,
		// the bitmap itself untouched (only edges which move in are cut from it)
		addPicture(pkg, part, 1.25, 1, srcRect(-25000, 0, 0, 0));
		assertArrayEquals(original, Files.readAllBytes(pictures(pkg).get(0).toPath()));
		String g = graphic(pkg);
		assertTrue(g, g.contains("content-width=\"72pt\""));
		assertTrue(g, g.contains("padding-left=\"18pt\""));
		assertTrue(g, g.contains("content-height=\"72pt\""));
		assertTrue("no padding where no edge moves out: " + g, !g.contains("padding-right") && !g.contains("padding-top"));
	}

	@Test
	public void outAndInTogether() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "high-fidelity");
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		// out a quarter at the top, in a half at the right: the bitmap is the left half
		addPicture(pkg, part, 1, 1, srcRect(0, -25000, 50000, 0));
		BufferedImage img = read(pictures(pkg).get(0));
		assertEquals(200, img.getWidth());
		assertEquals(400, img.getHeight());
		assertTrue(near(img.getRGB(100, 100), Color.RED));
		// the frame is 1.25 bitmap-heights tall, so the top fifth is empty: 14.4pt of 72
		String g = graphic(pkg);
		assertTrue(g, g.contains("padding-top=\"14.4pt\""));
		assertTrue(g, g.contains("content-height=\"57.6pt\""));
	}

	@Test
	public void theCropIsResampledToo() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		// the top right quadrant (200 px square) shown half an inch square is 400 ppi, over
		// Word's 300: 100 px at 200 ppi
		addPicture(pkg, part, 0.5, 0.5, srcRect(50000, 0, 0, 50000));
		BufferedImage img = read(pictures(pkg).get(0));
		assertEquals(100, img.getWidth());
		assertEquals(100, img.getHeight());
		assertTrue(near(img.getRGB(50, 50), Color.GREEN));
	}

	@Test
	public void theCropCanBeTurnedOff() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.CROP_PROPERTY, "false");
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "high-fidelity");
		byte[] original = quadrants();
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 1, 1, srcRect(50000, 0, 0, 50000));
		assertArrayEquals("the bitmap as stored", original, Files.readAllBytes(pictures(pkg).get(0).toPath()));
	}

	@Test
	public void twoCropsOfOnePictureAreTwoFiles() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		addPicture(pkg, part, 1, 1, srcRect(0, 0, 50000, 50000));    // red
		addPicture(pkg, part, 1, 1, srcRect(50000, 50000, 0, 0));    // yellow
		addPicture(pkg, part, 1, 1, srcRect(0, 0, 50000, 50000));    // red again: the first file
		List<File> files = pictures(pkg);
		assertEquals(3, files.size());
		assertNotEquals(files.get(0), files.get(1));
		assertEquals(files.get(0), files.get(2));
		assertTrue(near(read(files.get(0)).getRGB(100, 100), Color.RED));
		assertTrue(near(read(files.get(1)).getRGB(100, 100), Color.YELLOW));
	}

	@Test
	public void vmlCropValues() {
		PictureCrop c = PictureCrop.ofVml("16384f", "25%", "0.25", null);
		assertEquals(0.25, c.left, 1e-9);
		assertEquals(0.25, c.top, 1e-9);
		assertEquals(0.25, c.right, 1e-9);
		assertEquals(0, c.bottom, 1e-9);
		assertTrue("a crop which leaves nothing is ignored", PictureCrop.ofVml("0.6", null, "0.5", null).isNone());
	}

	// ------------------------------------------------------------------ resolution

	@Test
	public void theDefaultIsWordsPdfExport() throws Exception {
		byte[] original = photo();
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 2, 1, null); // 1000 ppi: over 300, so 200
		File f = pictures(pkg).get(0);
		BufferedImage img = read(f);
		assertEquals(400, img.getWidth());
		assertEquals(200, img.getHeight());
		assertTrue("still a JPEG", f.getName().endsWith(".jpg"));
		assertTrue("smaller", f.length() < original.length);
	}

	@Test
	public void wordsThresholdIs300ppi() throws Exception {
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2000 / 300d, 1000 / 300d, null); // exactly 300 ppi: left
		addPicture(pkg, part, 2000 / 350d, 1000 / 350d, null); // 350 ppi: to 200
		List<File> files = pictures(pkg);
		assertEquals(2000, read(files.get(0)).getWidth());
		assertEquals("rounded down, as Word does (1142.86)", 1142, read(files.get(1)).getWidth());
	}

	@Test
	public void wordReencodesAHighQualityJpegAtItsSize() throws Exception {
		byte[] original = photoQ95();
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 10, 5, null); // 200 ppi: not resampled
		File f = pictures(pkg).get(0);
		assertEquals(2000, read(f).getWidth());
		assertTrue("re-encoded smaller: " + f.length() + " < " + original.length, f.length() < original.length * 0.9);
	}

	@Test
	public void aJpegAlreadyAtWordsQualityIsLeftAlone() throws Exception {
		byte[] original = photo(); // ImageIO's default quality, 0.75
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 10, 5, null);
		assertArrayEquals(original, Files.readAllBytes(pictures(pkg).get(0).toPath()));
	}

	@Test
	public void wordsPdfExportIgnoresDoNotCompress() throws Exception {
		// "Do not compress images in file" governs Word's saves, not its PDF export: the
		// corpus's Word PDFs resample to 200 ppi in documents which set it
		WordprocessingMLPackage pkg = pkg();
		settings(pkg).setDoNotAutoCompressPictures(new BooleanDefaultTrue());
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, null);
		assertEquals(400, read(pictures(pkg).get(0)).getWidth());
	}

	@Test
	public void documentModeDoNotCompressKeepsTheOriginal() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "document");
		byte[] original = photo();
		WordprocessingMLPackage pkg = pkg();
		settings(pkg).setDoNotAutoCompressPictures(new BooleanDefaultTrue());
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 2, 1, null);
		assertArrayEquals(original, Files.readAllBytes(pictures(pkg).get(0).toPath()));
	}

	@Test
	public void documentModeDefaultIs220() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "document");
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, null);
		BufferedImage img = read(pictures(pkg).get(0));
		assertEquals(440, img.getWidth());
		assertEquals(220, img.getHeight());
	}

	@Test
	public void documentModeTheDocumentsDefaultResolution() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "document");
		WordprocessingMLPackage pkg = pkg();
		org.docx4j.w14.CTDefaultImageDpi dpi = new org.docx4j.w14.CTDefaultImageDpi();
		dpi.setVal(BigInteger.valueOf(96));
		settings(pkg).setDefaultImageDpi(dpi);
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, null);
		BufferedImage img = read(pictures(pkg).get(0));
		assertEquals(192, img.getWidth());
		assertEquals(96, img.getHeight());
	}

	@Test
	public void thePropertyOverridesTheDocument() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "150");
		WordprocessingMLPackage pkg = pkg();
		settings(pkg).setDoNotAutoCompressPictures(new BooleanDefaultTrue());
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, null);
		BufferedImage img = read(pictures(pkg).get(0));
		assertEquals(300, img.getWidth());
		assertEquals(150, img.getHeight());
	}

	@Test
	public void foSettingsOverrideTheProperties() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "high-fidelity");
		Docx4jProperties.setProperty(PictureCompression.CROP_PROPERTY, "false");
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage photo = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, photo, 2, 1, null);
		BinaryPartAbstractImage quads = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		addPicture(pkg, quads, 1, 1, srcRect(50000, 0, 0, 50000));
		List<File> files = pictures(pkg, "96", Boolean.TRUE);
		assertEquals(192, read(files.get(0)).getWidth());
		BufferedImage q = read(files.get(1));
		assertEquals("cropped, and at 96 ppi", 96, q.getWidth());
		assertTrue(near(q.getRGB(48, 48), Color.GREEN));
	}

	@Test
	public void highFidelityKeepsTheOriginal() throws Exception {
		Docx4jProperties.setProperty(PictureCompression.RESOLUTION_PROPERTY, "high-fidelity");
		byte[] original = photo();
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 2, 1, null);
		assertArrayEquals(original, Files.readAllBytes(pictures(pkg).get(0).toPath()));
	}

	@Test
	public void aSmallPictureIsLeftAlone() throws Exception {
		byte[] original = quadrants(); // 400 px shown 2 inches wide: 200 ppi, under 220
		WordprocessingMLPackage pkg = pkg();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, original);
		addPicture(pkg, part, 2, 2, null);
		assertArrayEquals(original, Files.readAllBytes(pictures(pkg).get(0).toPath()));
	}
}
