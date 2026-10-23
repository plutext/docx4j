package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A DATE field is formatted in the document's own language.
 *
 * <p>Word writes the month and day names of the field's language, not of the machine the
 * file is opened on.  Measured against Word 365 on two Turkish documents whose
 * {@code DATE} carries {@code \@ "d MMMM yyyy"}: Word prints "6 Eyl&uuml;l 2026" and the
 * extent is 270.1..325.1, where docx4j printed "7 September 2026" at 256.5..338.8; a
 * German one has Word's "06. Sep." against docx4j's "07. Sept." - CLDR's German
 * abbreviation, which carries a period Word's does not, so the format string's own
 * period doubled it.</p>
 *
 * @since 17.1.0
 */
public class DateFieldLanguageTest {

	private static Locale defaultLocale;

	@BeforeClass
	public static void setUpBeforeClass() {
		defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		Locale.setDefault(defaultLocale);
	}

	/** 6 September 2026 */
	private static Date date() {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2026, Calendar.SEPTEMBER, 6);
		return c.getTime();
	}

	private static FldSimpleModel model(String instr) throws Exception {
		FldSimpleModel m = new FldSimpleModel();
		m.build(instr);
		return m;
	}

	@Test
	public void theMonthNameIsTheDocumentsLanguage() throws Exception {
		FldSimpleModel m = model(" DATE \\@ \"d MMMM yyyy\" ");
		assertEquals("6 Eylül 2026", FormattingSwitchHelper.formatDate(m, date(), "tr-TR"));
		assertEquals("6 September 2026", FormattingSwitchHelper.formatDate(m, date(), null));
	}

	/** Word's abbreviated month names carry no trailing period; the format string
	 *  supplies the punctuation around them. */
	@Test
	public void anAbbreviatedMonthHasNoPeriodOfItsOwn() throws Exception {
		FldSimpleModel m = model(" DATE \\@ \"dd MMM yyyy\" ");
		// CLDR's German abbreviation is "Sept."; the trailing period is dropped because
		// the format string is where the punctuation comes from - Word's own abbreviation
		// is "Sep", which no installed locale data offers, so that much is a residual
		assertEquals("06 Sept 2026", FormattingSwitchHelper.formatDate(m, date(), "de-DE"));
	}

	/**
	 * Word's {@code h} and {@code hh} are the 12-hour clock's 1 to 12 (ECMA-376 17.16.4.1): noon is
	 * "12:05 PM" and five past midnight "12:05 AM". Until 17.3.5 they mapped to Java's {@code K}, 0 to
	 * 11, and noon printed "0:05 PM" (CR-058 phase 0, found on a bound date control). {@code am/pm} and
	 * {@code AM/PM} both give the upper-case names, as Word 365 does (bind-typed-formats DF2).
	 */
	@Test
	public void twelveHourClockRunsOneToTwelve() throws Exception {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2026, Calendar.OCTOBER, 22, 12, 5);
		assertEquals("12:05 PM", FormattingSwitchHelper.formatDate("h:mm am/pm", c.getTime(), "en-AU"));
		assertEquals("12:05 PM", FormattingSwitchHelper.formatDate("hh:mm AM/PM", c.getTime(), null));
		c.set(2026, Calendar.OCTOBER, 22, 0, 5);
		assertEquals("12:05 AM", FormattingSwitchHelper.formatDate("h:mm AM/PM", c.getTime(), "en-AU"));
		assertEquals("00:05", FormattingSwitchHelper.formatDate("HH:mm", c.getTime(), "en-AU"));
		c.set(2026, Calendar.OCTOBER, 22, 15, 5);
		assertEquals("3:05 PM", FormattingSwitchHelper.formatDate("h:mm am/pm", c.getTime(), "en-AU"));
		assertEquals("Thu 22 Oct 2026", FormattingSwitchHelper.formatDate("ddd d MMM yyyy", c.getTime(), "en-AU"));
	}
}
