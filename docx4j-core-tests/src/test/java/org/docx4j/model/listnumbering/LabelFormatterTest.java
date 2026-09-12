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
package org.docx4j.model.listnumbering;

import static org.docx4j.wml.NumberFormat.*;
import static org.junit.Assert.assertEquals;

import org.docx4j.wml.NumberFormat;
import org.junit.Test;

/**
 * One table for every {@code w:numFmt} docx4j formats: the format, a counter
 * value, the label.  The boundaries are the interesting rows: where a
 * formatter runs out (Roman 3999, the circled digits at 20), where letters
 * repeat (26/27/28, 52/53), the teens and hundreds of the English words, and
 * the Hebrew exceptions for 15 and 16.  A value a format cannot express gives
 * the decimal label (CR-014 phase 1).
 */
public class LabelFormatterTest {

	private static final Object[][] CASES = {
		{ DECIMAL, 1, "1" }, { DECIMAL, 100, "100" }, { DECIMAL_HALF_WIDTH, 10, "10" },
		{ DECIMAL_ZERO, 1, "01" }, { DECIMAL_ZERO, 9, "09" }, { DECIMAL_ZERO, 10, "10" }, { DECIMAL_ZERO, 100, "100" },
		{ NONE, 5, "" }, { BULLET, 5, "*" },

		{ UPPER_ROMAN, 1, "I" }, { UPPER_ROMAN, 4, "IV" }, { UPPER_ROMAN, 1994, "MCMXCIV" },
		{ UPPER_ROMAN, 3999, "MMMCMXCIX" }, { UPPER_ROMAN, 4000, "4000" },
		{ LOWER_ROMAN, 1, "i" }, { LOWER_ROMAN, 49, "xlix" }, { LOWER_ROMAN, 0, "0" },

		{ LOWER_LETTER, 1, "a" }, { LOWER_LETTER, 26, "z" }, { LOWER_LETTER, 27, "aa" }, { LOWER_LETTER, 28, "bb" },
		{ LOWER_LETTER, 52, "zz" }, { LOWER_LETTER, 53, "aaa" }, { LOWER_LETTER, 0, "0" },
		{ UPPER_LETTER, 1, "A" }, { UPPER_LETTER, 26, "Z" }, { UPPER_LETTER, 27, "AA" }, { UPPER_LETTER, 28, "BB" },

		{ ORDINAL, 1, "1st" }, { ORDINAL, 2, "2nd" }, { ORDINAL, 3, "3rd" }, { ORDINAL, 4, "4th" },
		{ ORDINAL, 11, "11th" }, { ORDINAL, 12, "12th" }, { ORDINAL, 13, "13th" }, { ORDINAL, 21, "21st" },
		{ ORDINAL, 22, "22nd" }, { ORDINAL, 23, "23rd" }, { ORDINAL, 101, "101st" }, { ORDINAL, 111, "111th" },
		{ ORDINAL, 112, "112th" }, { ORDINAL, 113, "113th" },

		{ CARDINAL_TEXT, 0, "Zero" }, { CARDINAL_TEXT, 1, "One" }, { CARDINAL_TEXT, 10, "Ten" },
		{ CARDINAL_TEXT, 13, "Thirteen" }, { CARDINAL_TEXT, 20, "Twenty" }, { CARDINAL_TEXT, 21, "Twenty-One" },
		{ CARDINAL_TEXT, 99, "Ninety-Nine" }, { CARDINAL_TEXT, 100, "One Hundred" },
		{ CARDINAL_TEXT, 101, "One Hundred One" }, { CARDINAL_TEXT, 110, "One Hundred Ten" },
		{ CARDINAL_TEXT, 1000, "One Thousand" }, { CARDINAL_TEXT, 1001, "One Thousand One" },
		{ CARDINAL_TEXT, 1234, "One Thousand Two Hundred Thirty-Four" },
		{ CARDINAL_TEXT, 2000000, "Two Million" },

		{ ORDINAL_TEXT, 1, "First" }, { ORDINAL_TEXT, 2, "Second" }, { ORDINAL_TEXT, 3, "Third" },
		{ ORDINAL_TEXT, 4, "Fourth" }, { ORDINAL_TEXT, 5, "Fifth" }, { ORDINAL_TEXT, 8, "Eighth" },
		{ ORDINAL_TEXT, 9, "Ninth" }, { ORDINAL_TEXT, 11, "Eleventh" }, { ORDINAL_TEXT, 12, "Twelfth" },
		{ ORDINAL_TEXT, 13, "Thirteenth" }, { ORDINAL_TEXT, 19, "Nineteenth" }, { ORDINAL_TEXT, 20, "Twentieth" },
		{ ORDINAL_TEXT, 21, "Twenty-First" }, { ORDINAL_TEXT, 30, "Thirtieth" }, { ORDINAL_TEXT, 42, "Forty-Second" },
		{ ORDINAL_TEXT, 100, "One Hundredth" }, { ORDINAL_TEXT, 101, "One Hundred First" },
		{ ORDINAL_TEXT, 112, "One Hundred Twelfth" }, { ORDINAL_TEXT, 120, "One Hundred Twentieth" },
		{ ORDINAL_TEXT, 1000, "One Thousandth" }, { ORDINAL_TEXT, 1001, "One Thousand First" },

		{ HEX, 1, "1" }, { HEX, 10, "A" }, { HEX, 255, "FF" }, { HEX, 256, "100" },
		{ CHICAGO, 1, "*" }, { CHICAGO, 2, "\u2020" }, { CHICAGO, 3, "\u2021" }, { CHICAGO, 4, "\u00a7" },
		{ CHICAGO, 5, "**" }, { CHICAGO, 8, "\u00a7\u00a7" }, { CHICAGO, 9, "***" },
		{ NUMBER_IN_DASH, 1, "- 1 -" }, { NUMBER_IN_DASH, 12, "- 12 -" },

		{ DECIMAL_FULL_WIDTH, 1, "\uff11" }, { DECIMAL_FULL_WIDTH, 10, "\uff11\uff10" },
		{ DECIMAL_FULL_WIDTH_2, 7, "\uff17" },
		{ THAI_NUMBERS, 10, "\u0e51\u0e50" }, { HINDI_NUMBERS, 10, "\u0967\u0966" },

		{ RUSSIAN_LOWER, 1, "\u0430" }, { RUSSIAN_LOWER, 10, "\u043a" }, { RUSSIAN_LOWER, 28, "\u044f" },
		{ RUSSIAN_LOWER, 29, "\u0430\u0430" }, { RUSSIAN_UPPER, 1, "\u0410" }, { RUSSIAN_UPPER, 30, "\u0411\u0411" },
		{ ARABIC_ALPHA, 1, "\u0623" }, { ARABIC_ALPHA, 28, "\u064a" }, { ARABIC_ALPHA, 29, "\u0623\u0623" },
		{ THAI_LETTERS, 1, "\u0e01" }, { THAI_LETTERS, 42, "\u0e2e" }, { THAI_LETTERS, 43, "\u0e01\u0e01" },

		{ HEBREW_1, 1, "\u05d0" }, { HEBREW_1, 10, "\u05d9" }, { HEBREW_1, 11, "\u05d9\u05d0" },
		{ HEBREW_1, 15, "\u05d8\u05d5" }, { HEBREW_1, 16, "\u05d8\u05d6" }, { HEBREW_1, 17, "\u05d9\u05d6" },
		{ HEBREW_1, 20, "\u05db" }, { HEBREW_1, 99, "\u05e6\u05d8" }, { HEBREW_1, 100, "\u05e7" },
		{ HEBREW_1, 400, "\u05ea" }, { HEBREW_1, 500, "\u05ea\u05e7" }, { HEBREW_1, 999, "\u05ea\u05ea\u05e7\u05e6\u05d8" },
		{ HEBREW_1, 0, "0" },

		{ DECIMAL_ENCLOSED_CIRCLE, 1, "\u2460" }, { DECIMAL_ENCLOSED_CIRCLE, 20, "\u2473" },
		{ DECIMAL_ENCLOSED_CIRCLE, 21, "21" }, { DECIMAL_ENCLOSED_CIRCLE_CHINESE, 2, "\u2461" },
		{ CHINESE_COUNTING, 1, "\u4e00" }, { CHINESE_LEGAL_SIMPLIFIED, 1, "\u58f9" },

		// no formatter registered: the decimal label
		{ JAPANESE_COUNTING, 7, "7" }, { KOREAN_DIGITAL, 3, "3" },
	};

	@Test
	public void table() {
		StringBuilder failures = new StringBuilder();
		for (Object[] c : CASES) {
			NumberFormat fmt = (NumberFormat) c[0];
			int value = (Integer) c[1];
			String expected = (String) c[2];
			String actual = NumberFormatter.getCurrentValueFormatted(fmt, value);
			if (!expected.equals(actual)) {
				failures.append(fmt.value()).append(' ').append(value).append(": expected '").append(expected)
						.append("' got '").append(actual).append("'\n");
			}
		}
		assertEquals("", failures.toString());
	}

	@Test
	public void nullFormatIsDecimal() {
		assertEquals("3", NumberFormatter.getCurrentValueFormatted((NumberFormat) null, 3));
	}

	@Test
	public void stringOverloadStillParses() {
		assertEquals("iv", NumberFormatter.getCurrentValueFormatted(LOWER_ROMAN, "4"));
		assertEquals("1", NumberFormatter.getCurrentValueFormatted(LOWER_ROMAN, "four"));
	}

	/** The formatters themselves still say when a value is out of range. */
	@Test(expected = NumberFormatException.class)
	public void formatterThrowsOutOfRange() {
		new NumberFormatRomanUpper().format(4000);
	}

	@Test
	public void registerReplaces() {
		LabelFormatter before = NumberFormatter.get(KOREAN_DIGITAL);
		try {
			NumberFormatter.register(KOREAN_DIGITAL, new LabelFormatter() {
				@Override
				public String format(int in) { return "k" + in; }
			});
			assertEquals("k3", NumberFormatter.getCurrentValueFormatted(KOREAN_DIGITAL, 3));
		} finally {
			if (before == null) {
				NumberFormatter.register(KOREAN_DIGITAL, null);
			} else {
				NumberFormatter.register(KOREAN_DIGITAL, before);
			}
		}
	}
}
