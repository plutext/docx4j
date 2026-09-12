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
 * A decimal label written in another script's digits: {@code decimalFullWidth}
 * (１２), {@code thaiNumbers} (๑๒), {@code hindiNumbers}
 * (१२).  Ten digit characters, 0 to 9, replace the ASCII ones.
 *
 * @since 17.1.1
 */
public class NumberFormatDigits extends LabelFormatter {

	/** {@code decimalFullWidth} and {@code decimalFullWidth2}. */
	public static final NumberFormatDigits FULL_WIDTH = new NumberFormatDigits("０１２３４５６７８９");
	/** {@code thaiNumbers}. */
	public static final NumberFormatDigits THAI = new NumberFormatDigits("๐๑๒๓๔๕๖๗๘๙");
	/** {@code hindiNumbers} (Devanagari digits). */
	public static final NumberFormatDigits HINDI = new NumberFormatDigits("०१२३४५६७८९");

	private final String digits;

	public NumberFormatDigits(String digits) {
		if (digits.length() != 10) throw new IllegalArgumentException("ten digits expected");
		this.digits = digits;
	}

	@Override
	public String format(int in) {
		String dec = Integer.toString(in);
		StringBuilder sb = new StringBuilder(dec.length());
		for (int i = 0; i < dec.length(); i++) {
			char c = dec.charAt(i);
			sb.append(c >= '0' && c <= '9' ? digits.charAt(c - '0') : c);
		}
		return sb.toString();
	}
}
