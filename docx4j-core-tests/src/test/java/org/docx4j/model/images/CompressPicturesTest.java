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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.docx4j.dml.CTRelativeRect;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;

/**
 * Word's Compress Pictures on a package (CR-029): crops deleted from the bitmaps, and the
 * bitmaps resampled for the largest size they are shown at.
 *
 * @since 17.3.0
 */
public class CompressPicturesTest {

	private static final long EMU_PER_INCH = 914400;

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

	private static Pic addPicture(WordprocessingMLPackage pkg, BinaryPartAbstractImage part,
			double inchesWide, double inchesHigh, CTRelativeRect srcRect) throws Exception {
		Inline inline = part.createImageInline("hint", "alt", 1, 2,
				Math.round(inchesWide * EMU_PER_INCH), Math.round(inchesHigh * EMU_PER_INCH), false);
		Pic pic = inline.getGraphic().getGraphicData().getPic();
		if (srcRect != null) pic.getBlipFill().setSrcRect(srcRect);
		ObjectFactory f = new ObjectFactory();
		P p = f.createP();
		R r = f.createR();
		Drawing d = f.createDrawing();
		d.getAnchorOrInline().add(inline);
		r.getContent().add(d);
		p.getContent().add(r);
		pkg.getMainDocumentPart().getContent().add(p);
		return pic;
	}

	private static CTRelativeRect srcRect(int l, int t, int r, int b) {
		CTRelativeRect rect = new CTRelativeRect();
		rect.setL(l); rect.setT(t); rect.setR(r); rect.setB(b);
		return rect;
	}

	private static BufferedImage image(BinaryPartAbstractImage part) throws Exception {
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(part.getBytes()));
		assertNotNull(img);
		return img;
	}

	private static boolean near(int rgb, Color c) {
		Color x = new Color(rgb, true);
		return Math.abs(x.getRed() - c.getRed()) < 40 && Math.abs(x.getGreen() - c.getGreen()) < 40
				&& Math.abs(x.getBlue() - c.getBlue()) < 40;
	}

	@Test
	public void cropsAreDeleted() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		Pic pic = addPicture(pkg, part, 1, 1, srcRect(50000, 0, 0, 50000)); // the top right quadrant
		CompressPictures.Report r = CompressPictures.compress(pkg, "high-fidelity", true);
		assertEquals(1, r.cropsDeleted);
		BufferedImage img = image(part);
		assertEquals(200, img.getWidth());
		assertEquals(200, img.getHeight());
		assertTrue(near(img.getRGB(100, 100), Color.GREEN));
		assertNull("nothing left to crop", pic.getBlipFill().getSrcRect());
	}

	@Test
	public void anEdgeWhichMovesOutStays() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		// out a quarter at the left, in a half at the right: the bitmap keeps its left half,
		// and the quarter becomes half of what remains
		Pic pic = addPicture(pkg, part, 1, 1, srcRect(-25000, 0, 50000, 0));
		CompressPictures.compress(pkg, "high-fidelity", true);
		assertEquals(200, image(part).getWidth());
		CTRelativeRect rest = pic.getBlipFill().getSrcRect();
		assertNotNull(rest);
		assertEquals(-50000, rest.getL());
		assertEquals(0, rest.getR());
	}

	@Test
	public void resampledForTheSizeShown() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, null);
		addPicture(pkg, part, 1, 0.5, null); // the same bitmap, smaller: the larger use decides
		CompressPictures.Report r = CompressPictures.compress(pkg, "220", true);
		assertEquals(1, r.parts.size());
		assertTrue(r.bytesAfter < r.bytesBefore);
		assertEquals("image/jpeg", part.getContentType());
		BufferedImage img = image(part);
		assertEquals(440, img.getWidth());
		assertEquals(220, img.getHeight());
	}

	@Test
	public void differentCropsOfOneBitmapStay() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, quadrants());
		Pic a = addPicture(pkg, part, 1, 1, srcRect(0, 0, 50000, 50000));
		Pic b = addPicture(pkg, part, 1, 1, srcRect(50000, 50000, 0, 0));
		byte[] before = part.getBytes();
		CompressPictures.Report r = CompressPictures.compress(pkg, "high-fidelity", true);
		assertEquals(0, r.cropsDeleted);
		assertArrayEquals(before, part.getBytes());
		assertEquals(50000, a.getBlipFill().getSrcRect().getR());
		assertEquals(50000, b.getBlipFill().getSrcRect().getL());
	}

	@Test
	public void documentModeHonoursDoNotCompress() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().getContents()
				.setDoNotAutoCompressPictures(new BooleanDefaultTrue());
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, null);
		byte[] before = part.getBytes();
		CompressPictures.Report r = CompressPictures.compress(pkg, null, false);
		assertEquals(0, r.parts.size());
		assertArrayEquals(before, part.getBytes());
	}

	@Test
	public void theSavedPackageReloads() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, photo());
		addPicture(pkg, part, 2, 1, srcRect(10000, 0, 10000, 0));
		CompressPictures.compress(pkg, "150", true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);
		WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(baos.toByteArray()));
		BinaryPartAbstractImage reloaded = (BinaryPartAbstractImage) again.getParts().get(part.getPartName());
		BufferedImage img = image(reloaded);
		assertEquals(300, img.getWidth());
		assertEquals(150, img.getHeight());
	}
}
