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
 * {@code w:numFmt chicago}: the footnote symbols * † ‡ §, then each doubled
 * (** †† ...), then tripled (ECMA-376 17.18.59: "one or more occurrences of a
 * single character" from that set).
 *
 * @since 17.1.1
 */
public class NumberFormatChicago extends LabelFormatter {

	private static final char[] SYMBOLS = { '*', '†', '‡', '§' };

	@Override
	public String format(int in) {
		if (in < 1) {
			throw new NumberFormatException("No symbol for " + in);
		}
		char symbol = SYMBOLS[(in - 1) % SYMBOLS.length];
		int times = (in - 1) / SYMBOLS.length + 1;
		StringBuilder sb = new StringBuilder(times);
		for (int i = 0; i < times; i++) sb.append(symbol);
		return sb.toString();
	}
}
