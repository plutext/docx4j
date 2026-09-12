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
 * {@code w:numFmt hebrew1}: Hebrew letters as numerals, א ב ג ... י (10),
 * יא (11), טו and טז for 15 and 16 (never יה or
 * יו), כ (20), ק (100) ... ת (400), and ת repeated above that.
 * No geresh or gershayim, as in Word's labels.
 *
 * @since 17.1.1
 */
public class NumberFormatHebrew1 extends LabelFormatter {

	private static final char[] UNITS = { 'א', 'ב', 'ג', 'ד', 'ה', 'ו', 'ז',
			'ח', 'ט' };
	private static final char[] TENS = { 'י', 'כ', 'ל', 'מ', 'נ', 'ס', 'ע',
			'פ', 'צ' };
	private static final char[] HUNDREDS = { 'ק', 'ר', 'ש', 'ת' };

	@Override
	public String format(int in) {
		if (in < 1) {
			throw new NumberFormatException("No Hebrew numeral for " + in);
		}
		StringBuilder sb = new StringBuilder();
		int n = in;
		while (n >= 400) { sb.append('ת'); n -= 400; }
		if (n >= 100) { sb.append(HUNDREDS[n / 100 - 1]); n %= 100; }
		if (n == 15) return sb.append("טו").toString();
		if (n == 16) return sb.append("טז").toString();
		if (n >= 10) { sb.append(TENS[n / 10 - 1]); n %= 10; }
		if (n > 0) sb.append(UNITS[n - 1]);
		return sb.toString();
	}
}
