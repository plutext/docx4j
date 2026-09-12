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
 * {@code w:numFmt ordinalText}: First, Second, Third, ... Twelfth, Twentieth,
 * Twenty-First, One Hundredth, One Hundred First, One Thousandth.  The cardinal
 * words with the last word made ordinal.
 *
 * @since 17.1.1
 */
public class NumberFormatOrdinalText extends LabelFormatter {

	private static final String[] ORDINAL_ONES = { "Zeroth", "First", "Second", "Third", "Fourth", "Fifth",
			"Sixth", "Seventh", "Eighth", "Ninth", "Tenth", "Eleventh", "Twelfth", "Thirteenth", "Fourteenth",
			"Fifteenth", "Sixteenth", "Seventeenth", "Eighteenth", "Nineteenth" };

	@Override
	public String format(int in) {
		if (in < 0) {
			throw new NumberFormatException("No words for " + in);
		}
		String cardinal = NumberFormatCardinalText.words(in);
		// the last word (after the last space or hyphen) becomes ordinal
		int cut = Math.max(cardinal.lastIndexOf(' '), cardinal.lastIndexOf('-'));
		String head = cut < 0 ? "" : cardinal.substring(0, cut + 1);
		String last = cardinal.substring(cut + 1);
		return head + ordinalWord(last);
	}

	private static String ordinalWord(String word) {
		for (int i = 0; i < NumberFormatCardinalText.ONES.length; i++) {
			if (NumberFormatCardinalText.ONES[i].equals(word)) return ORDINAL_ONES[i];
		}
		if (word.endsWith("y")) return word.substring(0, word.length() - 1) + "ieth"; // Twenty -> Twentieth
		return word + "th"; // Hundred, Thousand, Million
	}
}
