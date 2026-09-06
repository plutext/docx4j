package org.docx4j.metafiles;

import static org.docx4j.metafiles.MetafileTestFiles.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.docx4j.org.apache.poi.hwmf.draw.HwmfImageRenderer;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFont;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPlaceableHeader;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecord;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRecordType;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.sl.usermodel.PictureData;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LocaleUtil;
import org.docx4j.org.apache.poi.util.RecordFormatException;
import org.junit.Test;

/**
 * Ported from Apache POI's {@code org.apache.poi.hwmf.TestHwmfParsing}
 * (poi-scratchpad, trunk), adapted to JUnit 4 and to the repackaged
 * {@code org.docx4j.org.apache.poi.hwmf}.
 *
 * <p>Four of POI's tests are <b>not</b> ported, because they assert bounds checks which
 * upstream added <em>after</em> the 5.5.1 release this copy is based on (POI commits
 * c60d26562d, d80ff2dbfb and friends, May-June 2026):
 * {@code testRejectsZeroUnitsPerInchAtParserBoundary} and
 * {@code testWmfCreateRegionInvalidScanCount} here, and
 * {@code testHeaderDescriptionBoundsAreValidated} / {@code testEmfPolyDrawInvalidCount}
 * in {@link HemfPictureTest}. See the package README for the back-port list.
 * {@code testCyrillic} is {@code @Disabled} upstream (it needs a file POI does not ship).
 *
 * CR-011 phase 1.
 */
public class HwmfParsingTest {

	@Test
	public void parseSanta() throws IOException {
		assertRecordCount("santa.wmf", 581);
	}

	/** POI bug 65063 */
	@Test
	public void parseEmptyPolygonClose() throws IOException {
		assertRecordCount("empty-polygon-close.wmf", 272);
	}

	@Test
	public void parseFile45() throws IOException {
		assertRecordCount("file-45.wmf", 1315);
	}

	private void assertRecordCount(String file, int expected) throws IOException {
		try (InputStream fis = MetafileTestFiles.open(file)) {
			HwmfPicture wmf = new HwmfPicture(fis);
			List<HwmfRecord> records = wmf.getRecords();
			assertEquals(file, expected, records.size());
		}
	}

	@Test
	public void infiniteLoopIsRejected() throws Exception {
		try (InputStream is = MetafileTestFiles.open("61338.wmf")) {
			try {
				new HwmfPicture(is);
				fail("expected RecordFormatException");
			} catch (RecordFormatException expected) {
				// as upstream
			}
		}
	}

	/** an invalid commentType should be logged and ignored, not fatal */
	@Test
	public void invalidCommentTypeIsIgnored() throws Exception {
		byte[] bytes;
		try (InputStream is = MetafileTestFiles.open("santa.wmf")) {
			bytes = IOUtils.toByteArray(is);
		}

		bytes[34] = (byte)255;
		bytes[35] = (byte)255;

		HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(bytes));
		assertEquals(581, wmf.getRecords().size());
	}

	/** derives from common crawl; see POI bug 60677 */
	@Test
	public void shiftJisText() throws Exception {
		final HwmfPicture wmf;
		try (InputStream fis = MetafileTestFiles.open("60677.wmf")) {
			wmf = new HwmfPicture(fis);
		}

		Charset charset = LocaleUtil.CHARSET_1252;
		StringBuilder sb = new StringBuilder();
		// pure hackery for specifying the font: this happens to work on this test file,
		// but real code needs to maintain the object stack the way HwmfGraphics does
		for (HwmfRecord r : wmf.getRecords()) {
			if (r.getWmfRecordType().equals(HwmfRecordType.createFontIndirect)) {
				HwmfFont font = ((HwmfText.WmfCreateFontIndirect)r).getFont();
				charset = (font.getCharset().getCharset() == null)
					? LocaleUtil.CHARSET_1252 : font.getCharset().getCharset();
			}
			if (r.getWmfRecordType().equals(HwmfRecordType.extTextOut)) {
				assertTrue(r instanceof HwmfText.WmfExtTextOut);
				HwmfText.WmfExtTextOut textOut = (HwmfText.WmfExtTextOut)r;
				sb.append(textOut.getText(charset)).append("\n");
			}
		}
		assertContains(sb.toString(),
			"\u822A\u7A7A\u60C5\u5831\u696D\u52D9\u3078\u306E\uFF27\uFF29\uFF33");
	}

	/**
	 * Both substring and length rely on char, not codepoints. This confirms the
	 * substring calls in HwmfText will not truncate even beyond-BMP data.
	 */
	@Test
	public void lengths() {
		// the last character (Deseret AY U+1040C) is 2 utf16 surrogates
		String s = "\u666E\u6797\u65AF\uD801\uDC0C";
		Charset utf16LE = StandardCharsets.UTF_16LE;
		byte[] bytes = s.getBytes(utf16LE);
		String rebuilt = new String(bytes, utf16LE);
		rebuilt = rebuilt.substring(0, Math.min(bytes.length, rebuilt.length()));
		assertEquals(s, rebuilt);
		assertEquals(5, rebuilt.length());
		assertEquals(4, rebuilt.codePoints().count());
	}

	@Test
	public void validUnitsProduceFiniteDimensions() throws IOException {
		byte[] benign = createMinimalPlaceableWmf(1440, 0, 0, 100, 100);
		HwmfImageRenderer renderer = new HwmfImageRenderer();
		renderer.loadImage(benign, PictureData.PictureType.WMF.contentType);

		assertTrue(Double.isFinite(renderer.getDimension().getWidth()));
		assertTrue(Double.isFinite(renderer.getDimension().getHeight()));
		assertTrue(renderer.getDimension().getWidth() > 0);
		assertTrue(renderer.getDimension().getHeight() > 0);
	}

	/**
	 * Minimal placeable WMF stream: placeable header + WMF header + EOF record.
	 * Small on purpose - this targets parser validation, not WMF drawing semantics.
	 */
	private static byte[] createMinimalPlaceableWmf(int unitsPerInch, int x1, int y1, int x2, int y2) {
		ByteBuffer bb = ByteBuffer.allocate(46).order(ByteOrder.LITTLE_ENDIAN);

		// placeable header
		bb.putInt(HwmfPlaceableHeader.WMF_HEADER_MAGIC);
		bb.putShort((short)0); // hwmf handle
		bb.putShort((short)x1);
		bb.putShort((short)y1);
		bb.putShort((short)x2);
		bb.putShort((short)y2);
		bb.putShort((short)unitsPerInch);
		bb.putInt(0);          // reserved
		bb.putShort((short)0); // checksum

		// WMF header (9 words)
		bb.putShort((short)1);      // memory metafile
		bb.putShort((short)9);      // header size in WORDs
		bb.putShort((short)0x0300); // version
		bb.putInt(0);               // file size in WORDs (unused by the parser)
		bb.putShort((short)0);      // number of objects
		bb.putInt(0);               // max record size
		bb.putShort((short)0);      // number of members

		// EOF record
		bb.putInt(3);               // record size in WORDs
		bb.putShort((short)HwmfRecordType.eof.id);

		return bb.array();
	}
}
