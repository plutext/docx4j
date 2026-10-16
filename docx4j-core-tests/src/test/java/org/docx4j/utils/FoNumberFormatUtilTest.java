package org.docx4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.docx4j.model.fields.FormattingSwitchHelper;
import org.junit.Test;

/** Page number format tokens, including the digit based non Latin ones
 *  (see FormattingSwitchHelper.FORMAT_PAGE_TO_FO).
 */
public class FoNumberFormatUtilTest {

	private static final String THAI_ONE = "\u0E51";
	private static final String DEVANAGARI_ONE = "\u0967";
	private static final String FULLWIDTH_ONE = "\uFF11";

	@Test
	public void testDecimal() {
		assertEquals("4", FoNumberFormatUtil.format(4, "1"));
		assertEquals("1", FoNumberFormatUtil.format(1, "1"));
		assertEquals("123", FoNumberFormatUtil.format(123, "1"));
	}

	@Test
	public void testRoman() {
		assertEquals("iv", FoNumberFormatUtil.format(4, "i"));
		assertEquals("IV", FoNumberFormatUtil.format(4, "I"));
	}

	@Test
	public void testAlpha() {
		assertEquals("b", FoNumberFormatUtil.format(2, "a"));
		assertEquals("B", FoNumberFormatUtil.format(2, "A"));
	}

	@Test
	public void testThaiDigits() {
		assertEquals("\u0E57", FoNumberFormatUtil.format(7, THAI_ONE));
		assertEquals("\u0E51\u0E52", FoNumberFormatUtil.format(12, THAI_ONE));
		assertEquals(THAI_ONE, FoNumberFormatUtil.format(1, THAI_ONE));
	}

	@Test
	public void testDevanagariDigits() {
		assertEquals("\u096B", FoNumberFormatUtil.format(5, DEVANAGARI_ONE));
	}

	@Test
	public void testFullWidthDigits() {
		assertEquals("\uFF12\uFF13", FoNumberFormatUtil.format(23, FULLWIDTH_ONE));
	}

	@Test
	public void testZeroPadded() {
		assertEquals("01", FoNumberFormatUtil.format(1, "01"));
		assertEquals("03", FoNumberFormatUtil.format(3, "01"));
		assertEquals("12", FoNumberFormatUtil.format(12, "01"));
		assertEquals("123", FoNumberFormatUtil.format(123, "01"));
	}

	@Test
	public void testNegativePageNumber() {
		assertNull(FoNumberFormatUtil.format(-1, "1"));
		assertNull(FoNumberFormatUtil.format(-1, THAI_ONE));
		assertNull(FoNumberFormatUtil.format(-1, null));
	}

	@Test
	public void testWordFormatToFo() {
		assertEquals(THAI_ONE, FormattingSwitchHelper.getFoPageNumberFormat("thaiNumbers"));
		assertEquals(DEVANAGARI_ONE, FormattingSwitchHelper.getFoPageNumberFormat("hindiNumbers"));
		assertEquals(FULLWIDTH_ONE, FormattingSwitchHelper.getFoPageNumberFormat("decimalFullWidth"));
		assertEquals(FULLWIDTH_ONE, FormattingSwitchHelper.getFoPageNumberFormat("decimalFullWidth2"));
		assertEquals("1", FormattingSwitchHelper.getFoPageNumberFormat("decimalHalfWidth"));
		assertEquals("01", FormattingSwitchHelper.getFoPageNumberFormat("decimalZero"));
	}

	@Test
	public void testWordFormatToFoUnchanged() {
		assertEquals("i", FormattingSwitchHelper.getFoPageNumberFormat("lowerRoman"));
		// a letter/counting system we don't support; falls back to the default
		assertEquals("1", FormattingSwitchHelper.getFoPageNumberFormat("chineseCounting"));
		assertNull(FormattingSwitchHelper.getFoPageNumberFormat("none"));
		assertNull(FormattingSwitchHelper.getFoPageNumberFormat(null));
	}
}
