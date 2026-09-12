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

/**
 * {@code w:numFmt cardinalText}: One, Two, Three, ... Twenty-One, One Hundred,
 * One Hundred One, One Thousand.  Each word capitalised, no "and", the way Word
 * labels a list (its CARDTEXT field switch lower-cases the same words).
 *
 * @since 17.1.1
 */
public class NumberFormatCardinalText extends LabelFormatter {

	static final String[] ONES = { "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
			"Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
			"Eighteen", "Nineteen" };
	static final String[] TENS = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty",
			"Ninety" };
	static final String[] SCALES = { "", "Thousand", "Million", "Billion" };

	@Override
	public String format(int in) {
		if (in < 0) {
			throw new NumberFormatException("No words for " + in);
		}
		return words(in);
	}

	/** The cardinal words for n >= 0, in groups of three digits from the top. */
	static String words(int n) {
		if (n < 20) return ONES[n];
		StringBuilder sb = new StringBuilder();
		int scale = 0;
		int[] groups = new int[4];
		int g = 0;
		while (n > 0) { groups[g++] = n % 1000; n /= 1000; }
		for (int i = g - 1; i >= 0; i--) {
			if (groups[i] == 0) continue;
			if (sb.length() > 0) sb.append(' ');
			sb.append(belowThousand(groups[i]));
			if (i > 0) sb.append(' ').append(SCALES[i]);
		}
		return sb.toString();
	}

	private static String belowThousand(int n) {
		StringBuilder sb = new StringBuilder();
		if (n >= 100) {
			sb.append(ONES[n / 100]).append(" Hundred");
			n %= 100;
			if (n > 0) sb.append(' ');
		}
		if (n >= 20) {
			sb.append(TENS[n / 10]);
			if (n % 10 > 0) sb.append('-').append(ONES[n % 10]);
		} else if (n > 0) {
			sb.append(ONES[n]);
		}
		return sb.toString();
	}
}
