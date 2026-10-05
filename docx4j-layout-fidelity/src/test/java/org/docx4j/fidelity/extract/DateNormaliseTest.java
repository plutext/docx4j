package org.docx4j.fidelity.extract;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The date/time placeholder the LCS pairs on: a DATE, CREATEDATE, PRINTDATE,
 * SAVEDATE or TIME field prints the day the PDF was made, so a golden cut on one
 * day would never pair with a render made on the next.
 */
public class DateNormaliseTest {

	private static final String D = "￼d";
	private static final String T = "￼t";

	@Test
	public void numericDates() {
		assertEquals(D, PdfLayout.Line.normaliseDates("05.09.2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5.9.2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("2026-09-05"));
		assertEquals(D, PdfLayout.Line.normaliseDates("9/5/2026"));
		assertEquals("Datum: " + D, PdfLayout.Line.normaliseDates("Datum: 05.09.2026"));
	}

	@Test
	public void monthNameDates() {
		assertEquals(D, PdfLayout.Line.normaliseDates("September 5, 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("September 6, 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5 September 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5. September 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5 septembre 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5 de septiembre de 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5 settembre 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5 сентября 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("9 Eylül 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("5 Ekim 2026"));
		assertEquals(D, PdfLayout.Line.normaliseDates("12 MAYIS 2026"));
	}

	/** Word's SAVEDATE "d-MMMM-yyyy" and footer DATE "dd.MM.yy", refreshed at export (ledger8). */
	@Test
	public void hyphenAndShortYearDates() {
		assertEquals(D + " " + T, PdfLayout.Line.normaliseDates("09-September-2026 8:10 PM"));
		assertEquals(D, PdfLayout.Line.normaliseDates("20-March-2017"));
		assertEquals("Stand: " + D, PdfLayout.Line.normaliseDates("Stand: 05.10.26"));
		assertEquals(D + ".", PdfLayout.Line.normaliseDates("09.09.26."));
	}

	@Test
	public void times() {
		assertEquals(T, PdfLayout.Line.normaliseDates("14:05"));
		assertEquals(T, PdfLayout.Line.normaliseDates("14:05:32"));
		assertEquals(T, PdfLayout.Line.normaliseDates("2:05 PM"));
		assertEquals(D + " " + T, PdfLayout.Line.normaliseDates("05.09.2026 14:05"));
	}

	/** Nothing that is not a date may be collapsed: a section number, a money amount,
	 *  a bare year, a page range. */
	@Test
	public void notDates() {
		assertEquals("1.2.34", PdfLayout.Line.normaliseDates("1.2.34"));
		assertEquals("2.1.4 Scope", PdfLayout.Line.normaliseDates("2.1.4 Scope"));
		assertEquals("1.234.567,89", PdfLayout.Line.normaliseDates("1.234.567,89"));
		assertEquals("2026", PdfLayout.Line.normaliseDates("2026"));
		assertEquals("pages 5-2026", PdfLayout.Line.normaliseDates("pages 5-2026"));
		assertEquals("September", PdfLayout.Line.normaliseDates("September"));
		assertEquals("Article 5 of 2026", PdfLayout.Line.normaliseDates("Article 5 of 2026"));
		assertEquals("1.10.11.12", PdfLayout.Line.normaliseDates("1.10.11.12"));
		assertEquals("10.11.12.3", PdfLayout.Line.normaliseDates("10.11.12.3"));
		assertEquals("32.01.26", PdfLayout.Line.normaliseDates("32.01.26"));
		assertEquals("05.13.26", PdfLayout.Line.normaliseDates("05.13.26"));
		assertEquals("05.10-26", PdfLayout.Line.normaliseDates("05.10-26"));
	}

	/** A line with no digit at all short-circuits without running a regex. */
	@Test
	public void noDigitIsUntouched() {
		String s = "Terms and Conditions";
		assertEquals(s, PdfLayout.Line.normaliseDates(s));
	}
}
