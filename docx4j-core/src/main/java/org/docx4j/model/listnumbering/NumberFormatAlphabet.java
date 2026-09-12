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
 * A format that counts through an alphabet the way Word does for every lettered
 * {@code w:numFmt} (ECMA-376 17.18.59): the n-th letter for n up to the alphabet's
 * length, then that letter repeated - after z comes aa, bb, cc, not ab.  The
 * alphabet is given as the letters in order; the Cyrillic, Arabic and Thai
 * instances are the letter sets Word's own labels use, which leave out some
 * letters of each script (Russian has no ё, й, ъ, ы, ь;
 * Thai omits the two obsolete consonants ฃ and ฅ).
 *
 * @since 17.1.1
 */
public class NumberFormatAlphabet extends LabelFormatter {

	/** {@code russianLower}. */
	public static final NumberFormatAlphabet RUSSIAN_LOWER = new NumberFormatAlphabet(
			"абвгдежзиклмнопрстуфхцчшщэюя");
	/** {@code russianUpper}. */
	public static final NumberFormatAlphabet RUSSIAN_UPPER = new NumberFormatAlphabet(
			"АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ");
	/** {@code arabicAlpha}: the 28 letters in alphabetical (hija'i) order. */
	public static final NumberFormatAlphabet ARABIC_ALPHA = new NumberFormatAlphabet(
			"أبتثجحخدذرزسشصضطظعغفقكلمنهوي");
	/** {@code thaiLetters}: the 42 consonants in use. */
	public static final NumberFormatAlphabet THAI_LETTERS = new NumberFormatAlphabet(
			"กขคฆงจฉชซฌญฎฏฐฑฒณดตถทธนบปผฝพฟภมยรลวศษสหฬอฮ");

	private final String letters;

	public NumberFormatAlphabet(String letters) {
		this.letters = letters;
	}

	@Override
	public String format(int in) {
		if (in < 1) {
			throw new NumberFormatException("No letter for " + in);
		}
		int len = letters.length();
		char letter = letters.charAt((in - 1) % len);
		int times = (in - 1) / len + 1;
		StringBuilder sb = new StringBuilder(times);
		for (int i = 0; i < times; i++) sb.append(letter);
		return sb.toString();
	}
}
