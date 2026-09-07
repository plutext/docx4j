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
}
