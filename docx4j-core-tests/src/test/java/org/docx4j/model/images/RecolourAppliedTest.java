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
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;
import org.junit.Test;

/**
 * Whether a picture's colour adjustments were drawn is what the image handler did, not its kind (a review of CR-084):
 * a linked picture, or one that can't be read, is written as stored, and its adjustments are then reported
 * ({@code render.effect.blip_recolour}) rather than taken as drawn. And a derived picture that can't be written leaves
 * the picture as stored, not lost.
 */
public class RecolourAppliedTest {

	/** A picture in HTML output (its variant is the adjustments alone) */
	static final class Picture extends AbstractWordXmlPicture {
		Picture(PictureRecolour recolour) {
			this.recolour = recolour;
			this.forXslFo = false;
		}
	}

	/** Writes files (named for their part), or fails to write a derived one */
	static final class Handler extends AbstractConversionImageHandler {
		final boolean failDerived;

		Handler(boolean failDerived) {
			super("images", false);
			this.failDerived = failDerived;
		}

		@Override
		protected String createStoredImage(BinaryPart part, byte[] bytes) throws Docx4JException {
			String name = part.getPartName().getName();
			if (failDerived && name.contains("-png-")) throw new Docx4JException("disk full");
			return "file:" + name;
		}
	}

	private static PictureRecolour grey() {
		org.docx4j.vml.CTImageData d = new org.docx4j.vml.CTImageData();
		d.setGrayscale(org.docx4j.vml.STTrueFalse.T);
		return PictureRecolour.ofVml(d);
	}

	private static BinaryPart part(byte[] bytes) throws Exception {
		BinaryPart part = new BinaryPart(new PartName("/word/media/image1.png"));
		part.setContentType(new ContentType("image/png"));
		part.setBinaryData(bytes);
		return part;
	}

	private static byte[] png() throws Exception {
		BufferedImage im = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) im.setRGB(x, y, 0xC00000);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(im, "png", out);
		return out.toByteArray();
	}

	private static Relationship rel(String target, boolean external) {
		Relationship r = new org.docx4j.relationships.ObjectFactory().createRelationship();
		r.setId("rId1");
		r.setTarget(target);
		if (external) r.setTargetMode("External");
		return r;
	}

	@Test
	public void anAdjustedPictureWrittenIsApplied() throws Exception {
		Handler handler = new Handler(false);
		BinaryPart part = part(png());
		Picture first = new Picture(grey()), second = new Picture(grey());
		String uri = handler.handleImage(first, rel("media/image1.png", false), part);
		assertTrue(uri, uri.startsWith("file:/word/media/image1-png-"));
		assertTrue(first.isRecolourApplied());
		// a second use of it, from the handler's map, too
		assertEquals(uri, handler.handleImage(second, rel("media/image1.png", false), part));
		assertTrue(second.isRecolourApplied());
	}

	@Test
	public void aLinkedPictureIsNotApplied() throws Exception {
		Picture picture = new Picture(grey());
		String uri = new Handler(false).handleImage(picture, rel("http://example.com/a.png", true), null);
		assertEquals("http://example.com/a.png", uri);
		assertFalse(picture.isRecolourApplied());
	}

	@Test
	public void aPictureThatCantBeReadIsNotApplied() throws Exception {
		Handler handler = new Handler(false);
		BinaryPart part = part("not a picture".getBytes(StandardCharsets.US_ASCII));
		Picture first = new Picture(grey()), second = new Picture(grey());
		assertEquals("file:/word/media/image1.png", handler.handleImage(first, rel("media/image1.png", false), part));
		assertFalse(first.isRecolourApplied());
		assertEquals("file:/word/media/image1.png", handler.handleImage(second, rel("media/image1.png", false), part));
		assertFalse(second.isRecolourApplied());
	}

	@Test
	public void aDerivedPictureThatCantBeWrittenLeavesTheStoredOne() throws Exception {
		Picture picture = new Picture(grey());
		String uri = new Handler(true).handleImage(picture, rel("media/image1.png", false), part(png()));
		assertEquals("file:/word/media/image1.png", uri);
		assertFalse(picture.isRecolourApplied());
	}
}
