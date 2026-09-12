package org.docx4j.model.listnumbering;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.wml.NumberFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formats a list counter as the label text of its {@code w:numFmt}.
 *
 * <p>Since 17.1.1 (CR-014 phase 1) this is a registry of one stateless
 * {@link LabelFormatter} per {@link NumberFormat}, and it is fail-soft: a format
 * this class has no formatter for, or a value the formatter cannot express
 * (Roman above 3999, a circled digit above 20), gives the decimal label instead
 * of an exception ending the export, as Word does, with one warning per format
 * naming where it first happened.  {@link #register} adds or replaces a
 * formatter, for the counting styles docx4j does not ship (Japanese, Korean,
 * Vietnamese and most of the ideographic ones).
 */
public class NumberFormatter {
	
	protected static Logger log = LoggerFactory.getLogger(NumberFormatter.class);

	private static final Map<NumberFormat, LabelFormatter> REGISTRY =
			Collections.synchronizedMap(new EnumMap<NumberFormat, LabelFormatter>(NumberFormat.class));

	/** Formats warned about already (once per format, whatever the value). */
	private static final Set<NumberFormat> WARNED = ConcurrentHashMap.newKeySet();

	private static final LabelFormatter DECIMAL = new LabelFormatter() {
		@Override
		public String format(int in) { return Integer.toString(in); }
	};

	static {
		register(NumberFormat.DECIMAL, DECIMAL);
		register(NumberFormat.DECIMAL_HALF_WIDTH, DECIMAL);
		register(NumberFormat.NONE, new LabelFormatter() {
			@Override
			public String format(int in) { return ""; }
		});
		// TODO - revisit how this is handled: the code elsewhere for handling
		// bullets overlaps with this numFmt stuff.
		register(NumberFormat.BULLET, new LabelFormatter() {
			@Override
			public String format(int in) { return "*"; }
		});
		register(NumberFormat.UPPER_ROMAN, new NumberFormatRomanUpper());
		register(NumberFormat.LOWER_ROMAN, new NumberFormatRomanLower());
		register(NumberFormat.LOWER_LETTER, new NumberFormatLowerLetter());
		register(NumberFormat.UPPER_LETTER, new NumberFormatUpperLetter());
		register(NumberFormat.DECIMAL_ZERO, new NumberFormatDecimalZero());
		register(NumberFormat.ORDINAL, new NumberFormatOrdinal());
		register(NumberFormat.CARDINAL_TEXT, new NumberFormatCardinalText());
		register(NumberFormat.ORDINAL_TEXT, new NumberFormatOrdinalText());
		register(NumberFormat.HEX, new NumberFormatHex());
		register(NumberFormat.CHICAGO, new NumberFormatChicago());
		register(NumberFormat.NUMBER_IN_DASH, new NumberFormatNumberInDash());
		register(NumberFormat.DECIMAL_FULL_WIDTH, NumberFormatDigits.FULL_WIDTH);
		register(NumberFormat.DECIMAL_FULL_WIDTH_2, NumberFormatDigits.FULL_WIDTH);
		register(NumberFormat.THAI_NUMBERS, NumberFormatDigits.THAI);
		register(NumberFormat.HINDI_NUMBERS, NumberFormatDigits.HINDI);
		register(NumberFormat.RUSSIAN_LOWER, NumberFormatAlphabet.RUSSIAN_LOWER);
		register(NumberFormat.RUSSIAN_UPPER, NumberFormatAlphabet.RUSSIAN_UPPER);
		register(NumberFormat.ARABIC_ALPHA, NumberFormatAlphabet.ARABIC_ALPHA);
		register(NumberFormat.THAI_LETTERS, NumberFormatAlphabet.THAI_LETTERS);
		register(NumberFormat.HEBREW_1, new NumberFormatHebrew1());
		// These two are the same in Chinese, no need to be processed separately
		register(NumberFormat.CHINESE_COUNTING, new NumberFormatChineseLower());
		register(NumberFormat.CHINESE_COUNTING_THOUSAND, new NumberFormatChineseLower());
		// This one means use upper Chinese number characters
		register(NumberFormat.CHINESE_LEGAL_SIMPLIFIED, new NumberFormatChineseUpper());
		// These two are the same, just to adapt to documents in Chinese
		register(NumberFormat.DECIMAL_ENCLOSED_CIRCLE, new NumberFormatDecimalEnclosedCircle());
		register(NumberFormat.DECIMAL_ENCLOSED_CIRCLE_CHINESE, new NumberFormatDecimalEnclosedCircle());
	}

	/** Adds or replaces the formatter for a {@code w:numFmt}.  @since 17.1.1 */
	public static void register(NumberFormat numFmt, LabelFormatter formatter) {
		REGISTRY.put(numFmt, formatter);
	}

	/** The formatter registered for a {@code w:numFmt}, or null.  @since 17.1.1 */
	public static LabelFormatter get(NumberFormat numFmt) {
		return REGISTRY.get(numFmt);
	}

    /**
     * The current number, formatted using numFmt.
     */
    public static String getCurrentValueFormatted(NumberFormat numFmt, String num)
    {

    	try {
    		return getCurrentValueFormatted(numFmt, Integer.parseInt(num));
    	} catch (NumberFormatException e) {
    		log.error("'" + num + "' is NaN");
    		return "1";
    	}
    } 
    /**
     * The current number, formatted using numFmt.
     */
    public static String getCurrentValueFormatted(NumberFormat numFmt, int current)
    {
    	return getCurrentValueFormatted(numFmt, current, null);
    }

	/**
	 * The current number, formatted using numFmt; {@code where} (a numId/ilvl,
	 * say) is named in the one-time warning when the format is unsupported or
	 * the value is out of its range and the decimal label is used instead.
	 *
	 * @since 17.1.1
	 */
	public static String getCurrentValueFormatted(NumberFormat numFmt, int current, String where) {
		if (numFmt == null) {
			return Integer.toString(current);
		}
		LabelFormatter f = REGISTRY.get(numFmt);
		if (f == null) {
			warnOnce(numFmt, "no formatter for numFmt " + numFmt.value() + "; decimal labels used", where);
			return Integer.toString(current);
		}
		try {
			return f.format(current);
		} catch (NumberFormatException e) {
			warnOnce(numFmt, "numFmt " + numFmt.value() + " cannot express " + current + " (" + e.getMessage()
					+ "); decimal label used", where);
			return Integer.toString(current);
		}
	}

	private static void warnOnce(NumberFormat numFmt, String message, String where) {
		if (WARNED.add(numFmt)) {
			log.warn(message + (where == null ? "" : " (first at " + where + ")") + "; not reported again for this format");
		} else if (log.isDebugEnabled()) {
			log.debug(message + (where == null ? "" : " at " + where));
		}
	}
}
