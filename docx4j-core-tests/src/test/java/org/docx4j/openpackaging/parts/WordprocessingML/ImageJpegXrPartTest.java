package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.junit.Test;

/**
 * Issues 560/693: a .wdp (JPEG XR, image/vnd.ms-photo) part — written by
 * Word itself for the pre-effects original of an edited picture, or kept
 * as-is when a .wdp is inserted — used to load as a plain BinaryPart with
 * an ERROR logged ("No subclass found ... defaulting to binary").
 */
public class ImageJpegXrPartTest {

	/** The load-time dispatch: content type → typed image part. */
	@Test
	public void contentTypeDispatch() throws Exception {
		Part part = new ContentTypeManager().newPartForContentType(
				ContentTypes.IMAGE_JPEG_XR, "/word/media/hdphoto1.wdp", null);
		assertTrue(part instanceof ImageJpegXrPart);
		assertTrue(part instanceof BinaryPartAbstractImage);
		assertEquals(ContentTypes.IMAGE_JPEG_XR, part.getContentType());
	}

	/** A package containing a .wdp part survives save + reload, typed. */
	@Test
	public void roundTrip() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();

		// JPEG XR magic (II BC 01) followed by filler; content isn't sniffed
		byte[] bytes = new byte[] { 0x49, 0x49, (byte) 0xBC, 0x01, 0x20, 0x00, 0x00, 0x00 };
		ImageJpegXrPart imagePart = new ImageJpegXrPart(new PartName("/word/media/image1.wdp"));
		imagePart.setBinaryData(bytes);
		pkg.getMainDocumentPart().addTargetPart(imagePart);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pkg.save(baos);

		WordprocessingMLPackage reloaded = WordprocessingMLPackage.load(
				new ByteArrayInputStream(baos.toByteArray()));
		Part part = reloaded.getParts().get(new PartName("/word/media/image1.wdp"));
		assertTrue("expected ImageJpegXrPart, got " + part.getClass().getName(),
				part instanceof ImageJpegXrPart);

		assertArrayEquals(bytes, ((ImageJpegXrPart) part).getBytes());
	}

}
