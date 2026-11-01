package org.docx4j.metafiles;

import static org.docx4j.metafiles.MetafileTestFiles.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataFormat;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataMultiformats;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfHeader;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfRecord;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordType;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfText;
import org.docx4j.org.apache.poi.hemf.usermodel.HemfPicture;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecord;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfEmbedded;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfEmbeddedType;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;
import org.junit.Test;

/**
 * Ported from Apache POI's {@code org.apache.poi.hemf.usermodel.TestHemfPicture}
 * (poi-scratchpad, trunk), adapted to JUnit 4 and to the repackaged
 * {@code org.docx4j.org.apache.poi.hemf}.
 *
 * <p>The tests of the bounds checks upstream added after the 5.5.1 release this copy is
 * based on came with their back-ports (the package README lists them):
 * {@code testHeaderDescriptionBoundsAreValidated} and {@code testEmfPolyDrawInvalidCount}
 * (September 2026), and the November 2026 ones at the end of this class. The record-level
 * ones that need package access are in {@code org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordBoundsTest}.
 *
 * CR-011 phase 1.
 */
public class HemfPictureTest {

	@Test
	public void basicWindows() throws Exception {
		try (InputStream is = MetafileTestFiles.open("SimpleEMF_windows.emf")) {
			HemfPicture pic = new HemfPicture(is);
			HemfHeader header = pic.getHeader();
			assertEquals(27864, header.getBytes());
			assertEquals(31, header.getRecords());
			assertEquals(3, header.getHandles());
			assertEquals(346000, header.getMicroDimension().getWidth(), 0);
			assertEquals(194000, header.getMicroDimension().getHeight(), 0);

			assertEquals(31, pic.getRecords().size());
		}
	}

	@Test
	public void basicMac() throws Exception {
		try (InputStream is = MetafileTestFiles.open("SimpleEMF_mac.emf")) {
			HemfPicture pic = new HemfPicture(is);
			HemfHeader header = pic.getHeader();

			int records = 0;
			boolean extractedData = false;
			for (HemfRecord record : pic) {
				if (record.getEmfRecordType() == HemfRecordType.comment) {
					HemfComment.EmfCommentData comment = ((EmfComment)record).getCommentData();
					if (comment instanceof EmfCommentDataMultiformats) {
						for (EmfCommentDataFormat d : ((EmfCommentDataMultiformats)comment).getFormats()) {
							byte[] data = d.getRawData();
							// header starts at 0
							assertEquals('%', data[0]);
							assertEquals('P', data[1]);
							assertEquals('D', data[2]);
							assertEquals('F', data[3]);

							// byte array ends at EOF\n
							assertEquals('E', data[data.length - 4]);
							assertEquals('O', data[data.length - 3]);
							assertEquals('F', data[data.length - 2]);
							assertEquals('\n', data[data.length - 1]);
							extractedData = true;
						}
					}
				}
				records++;
			}
			assertTrue(extractedData);
			assertEquals(header.getRecords(), records);
		}
	}

	@Test
	public void macText() throws Exception {
		try (InputStream is = MetafileTestFiles.open("SimpleEMF_mac.emf")) {
			HemfPicture pic = new HemfPicture(is);
			String txt = extractText(pic, null);
			assertContains(txt, "Tika http://incubator.apache.org");
			assertContains(txt, "Latest News\n");
		}
	}

	@Test
	public void wmfInsideEmf() throws Exception {
		byte[] wmfData = null;
		try (InputStream is = MetafileTestFiles.open("63327.emf")) {
			HemfPicture pic = new HemfPicture(is);
			for (HemfRecord record : pic) {
				if (record.getEmfRecordType() == HemfRecordType.comment) {
					HemfComment.EmfCommentData emfCommentData =
						((HemfComment.EmfComment)record).getCommentData();
					if (emfCommentData instanceof HemfComment.EmfCommentDataWMF) {
						wmfData = ((HemfComment.EmfCommentDataWMF)emfCommentData).getWMFData();
					}
				}
			}
		}
		assertNotNull(wmfData);
		assertEquals(230, wmfData.length);

		HwmfPicture pict = new HwmfPicture(new ByteArrayInputStream(wmfData));
		String embedded = null;
		for (HwmfRecord r : pict.getRecords()) {
			if (r instanceof HwmfText.WmfTextOut) {
				embedded = ((HwmfText.WmfTextOut)r).getText(StandardCharsets.US_ASCII);
			}
		}
		assertNotNull(embedded);
		assertEquals("Hw.txt", embedded);
	}

	@Test
	public void windowsText() throws Exception {
		try (InputStream is = MetafileTestFiles.open("SimpleEMF_windows.emf")) {
			HemfPicture pic = new HemfPicture(is);
			Set<String> expectedParts = new HashSet<>();
			expectedParts.add("C:\\Users\\tallison\\");
			expectedParts.add("testPDF.pdf");
			Set<String> found = new HashSet<>();

			String txt = extractText(pic, found);

			assertContains(txt, "C:\\Users\\tallison\\\n");
			assertContains(txt, "asf2-git-1.x\\tika-\n");
			found.retainAll(expectedParts);
			assertEquals(expectedParts.size(), found.size());
		}
	}

	@Test
	public void infiniteLoopOnFileIsRejected() throws Exception {
		try (InputStream is = MetafileTestFiles.open("61294.emf")) {
			HemfPicture pic = new HemfPicture(is);
			try {
				pic.forEach(r -> { });
				fail("expected RecordFormatException");
			} catch (RecordFormatException expected) {
				// as upstream
			}
		}
	}

	@Test
	public void infiniteLoopOnByteArrayIsRejected() throws Exception {
		byte[] data = MetafileTestFiles.bytes("61294.emf");
		HemfPicture pic = new HemfPicture(new ByteArrayInputStream(data));
		try {
			pic.forEach(r -> { });
			fail("expected RecordFormatException");
		} catch (RecordFormatException expected) {
			// as upstream
		}
	}

	@Test
	public void nestedWmfEmf() throws Exception {
		try (InputStream is = MetafileTestFiles.open("nested_wmf.emf")) {
			HemfPicture emf1 = new HemfPicture(is);
			List<HwmfEmbedded> embeds = new ArrayList<>();
			emf1.getEmbeddings().forEach(embeds::add);
			assertEquals(1, embeds.size());
			assertEquals(HwmfEmbeddedType.WMF, embeds.get(0).getEmbeddedType());

			HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(embeds.get(0).getRawData()));
			embeds.clear();
			wmf.getEmbeddings().forEach(embeds::add);
			assertEquals(3, embeds.size());
			assertEquals(HwmfEmbeddedType.EMF, embeds.get(0).getEmbeddedType());

			HemfPicture emf2 = new HemfPicture(new ByteArrayInputStream(embeds.get(0).getRawData()));
			embeds.clear();
			emf2.getEmbeddings().forEach(embeds::add);
			assertTrue(embeds.isEmpty());
		}
	}

	/**
	 * Reassembles the extTextOutW runs into something line-like, the way POI's tests do
	 * (the fudge factor stands in for real font metrics).
	 *
	 * @param collect if non-null, every individual run is added to it
	 */
	private static String extractText(HemfPicture pic, Set<String> collect) throws Exception {
		double lastY = -1;
		double lastX = -1;
		final long fudgeFactorX = 1000;
		StringBuilder sb = new StringBuilder();
		for (HemfRecord record : pic) {
			if (record.getEmfRecordType().equals(HemfRecordType.extTextOutW)) {
				HemfText.EmfExtTextOutW extTextOutW = (HemfText.EmfExtTextOutW)record;
				Point2D reference = extTextOutW.getReference();
				if (lastY > -1 && lastY != reference.getY()) {
					sb.append("\n");
					lastX = -1;
				}
				if (lastX > -1 && reference.getX() - lastX > fudgeFactorX) {
					sb.append(' ');
				}
				String t = extTextOutW.getText();
				if (collect != null) {
					collect.add(t);
				}
				sb.append(t);
				lastY = reference.getY();
				lastX = reference.getX();
			}
		}
		return sb.toString();
	}

	// ---------------------------------------------------------------- bounds checks back-ported from POI

	/** POI d80ff2dbfb (September 2026): the description's bounds are checked before allocation. */
	@Test
	public void headerDescriptionBoundsAreValidated() throws Exception {
		HemfHeader header = new HemfHeader();
		byte[] data = headerRecordData(0x40000001L, 88);
		try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
			header.init(leis, data.length, HemfRecordType.header.id);
			fail("expected RecordFormatException");
		} catch (RecordFormatException expected) {
			// as upstream
		}
	}

	/** POI c60d26562d (September 2026) */
	@Test
	public void emfPolyDrawInvalidCount() throws Exception {
		assertPolyDrawRejects(Integer.MAX_VALUE);
	}

	/** POI 941f09ecce (November 2026): an unsigned count is checked while it is still a long. */
	@Test
	public void emfPolyDrawCountAboveIntMax() throws Exception {
		assertPolyDrawRejects(0xFFFFFFFFL);
	}

	private static void assertPolyDrawRejects(long count) throws Exception {
		HemfDraw.EmfPolyDraw record = new HemfDraw.EmfPolyDraw();
		byte[] data = new byte[20];
		LittleEndian.putUInt(data, 16, count);
		try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
			record.init(leis, data.length, HemfRecordType.polyDraw.id);
			fail("expected RecordFormatException");
		} catch (RecordFormatException expected) {
			// as upstream
		}
	}

	/**
	 * POI 941f09ecce (November 2026): EMR_POLYGON and EMR_POLYBEZIER cap the point count at 16K
	 * and ignore the rest, so a count above Integer.MAX_VALUE is clamped, not rejected.
	 */
	@Test
	public void emfPolygonCountAboveIntMaxIsClamped() throws Exception {
		assertPointCountClamped(new HemfDraw.EmfPolygon(), HemfRecordType.polygon.id);
		assertPointCountClamped(new HemfDraw.EmfPolyBezier(), HemfRecordType.polyBezier.id);
	}

	private static void assertPointCountClamped(HemfRecord record, long recordId) throws Exception {
		// 16 bytes of bounds, a 4 byte count, then room for the 16K points the cap allows
		byte[] data = new byte[16 + 4 + 16384 * 8];
		LittleEndian.putUInt(data, 16, 0xFFFFFFFFL);
		try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
			long size = record.init(leis, data.length, recordId);
			assertTrue("read " + size + " bytes of a " + data.length + " byte record", size <= data.length);
		}
	}

	/** POI 941f09ecce (November 2026) */
	@Test
	public void emfCommentWmfSizeAboveIntMax() throws Exception {
		HemfComment.EmfCommentDataWMF record = new HemfComment.EmfCommentDataWMF();
		byte[] data = new byte[20];
		LittleEndian.putUInt(data, 0, HemfComment.HemfCommentRecordType.emfWMF.id & 0xFFFFFFFFL);
		LittleEndian.putUInt(data, 16, 0xFFFFFFFFL);
		try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
			record.init(leis, data.length);
			fail("expected RecordFormatException");
		} catch (RecordFormatException expected) {
			// as upstream
		}
	}

	/** POI 941f09ecce (November 2026) */
	@Test
	public void emfCommentBeginGroupDescriptionAboveIntMax() throws Exception {
		HemfComment.EmfCommentDataBeginGroup record = new HemfComment.EmfCommentDataBeginGroup();
		byte[] data = new byte[24];
		LittleEndian.putUInt(data, 0, HemfComment.HemfCommentRecordType.emfBeginGroup.id & 0xFFFFFFFFL);
		LittleEndian.putUInt(data, 20, 0xFFFFFFFFL);
		try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
			record.init(leis, data.length);
			fail("expected RecordFormatException");
		} catch (RecordFormatException expected) {
			// as upstream
		}
	}

	/** POI d4178c1d5c (November 2026): a fuzzed EMF's bitmap offsets raise, not an assertion. */
	@Test
	public void fuzzedBitmapOffsetsRaise() throws Exception {
		HemfPicture pic = new HemfPicture(new ByteArrayInputStream(
				MetafileTestFiles.bytes("clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-5318384433823744.emf")));
		try {
			pic.forEach(r -> {});
			fail("expected RecordFormatException");
		} catch (RecordFormatException expected) {
			// as upstream
		}
	}

	/** An EMR_HEADER record's data (after type and size) with the given description count and offset. */
	private static byte[] headerRecordData(long nDescription, long offDescription) {
		ByteBuffer bb = ByteBuffer.allocate(8 * 4 + 4 + 4 + 4 + 4 + 2 + 2 + 4 + 4 + 4 + 4 * 4).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < 8; i++) bb.putInt(0);
		bb.putInt(0x464D4520);
		bb.putInt(0x00010000);
		bb.putInt(0);
		bb.putInt(1);
		bb.putShort((short) 1);
		bb.putShort((short) 0);
		bb.putInt((int) nDescription);
		bb.putInt((int) offDescription);
		bb.putInt(0);
		for (int i = 0; i < 4; i++) bb.putInt(0);
		return bb.array();
	}
}
