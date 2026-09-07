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
package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;

import org.docx4j.fonts.fop.fonts.truetype.OpenFont;
import org.junit.Test;

/**
 * A glyph advance is rounded to the nearest 1/1000 em, not truncated (CR-001 §10).
 *
 * FOP computes <code>(n / upem) * 1000 + ((n % upem) * 1000) / upem</code>, integer
 * division throughout, so every advance is up to one unit short and every line it
 * measures up to about 0.1% narrow.  Word measures with the font's exact advances and
 * writes rounded widths into its PDF's <code>/Widths</code>.
 *
 * The values below are Liberation Serif's, whose units per em is 2048.
 *
 * @since 17.1.0
 */
public class GlyphAdvanceRoundingTest {

	private static final int UPEM = 2048;

	/** trunc == round: nothing to tell apart. */
	@Test
	public void exactMultiplesAreUnchanged() {
		assertEquals(0, OpenFont.convertUnit2PDFUnit(0, UPEM));
		assertEquals(1000, OpenFont.convertUnit2PDFUnit(2048, UPEM));
		assertEquals(500, OpenFont.convertUnit2PDFUnit(1024, UPEM));  // 'o'
		assertEquals(250, OpenFont.convertUnit2PDFUnit(512, UPEM));   // space
	}

	@Test
	public void fractionsRoundToNearest() {
		// 'e' is 909 units = 443.848 thousandths; truncation gave 443
		assertEquals(444, OpenFont.convertUnit2PDFUnit(909, UPEM));
		// 'i' 569 = 277.832; 'L' 1251 = 610.840; 'r' 682 = 333.008; 'P' 1139 = 556.152
		assertEquals(278, OpenFont.convertUnit2PDFUnit(569, UPEM));
		assertEquals(611, OpenFont.convertUnit2PDFUnit(1251, UPEM));
		assertEquals(333, OpenFont.convertUnit2PDFUnit(682, UPEM));
		assertEquals(556, OpenFont.convertUnit2PDFUnit(1139, UPEM));
	}

	@Test
	public void roundsHalfAway() {
		// 1024 units at 2000 upem is exactly 512.0; 1025 is 512.5
		assertEquals(512, OpenFont.convertUnit2PDFUnit(1024, 2000));
		assertEquals(513, OpenFont.convertUnit2PDFUnit(1025, 2000));
	}

	/** A negative value - a bounding box or an underline position - is symmetric. */
	@Test
	public void negativesRoundSymmetrically() {
		assertEquals(-444, OpenFont.convertUnit2PDFUnit(-909, UPEM));
		assertEquals(-278, OpenFont.convertUnit2PDFUnit(-569, UPEM));
		assertEquals(-1000, OpenFont.convertUnit2PDFUnit(-2048, UPEM));
	}

	/** 1000 * n overflows an int for a large bounding box; the arithmetic is long. */
	@Test
	public void largeValuesDoNotOverflow() {
		assertEquals(1000000, OpenFont.convertUnit2PDFUnit(2048 * 1000, UPEM));
		assertEquals(-1000000, OpenFont.convertUnit2PDFUnit(-2048 * 1000, UPEM));
	}

	/**
	 * The whole point: over a line, truncation is systematically short by half a unit
	 * per glyph where rounding is not.  "incididunt ut labore et dolore" set in 12pt
	 * Liberation Serif is 139.295pt by the font's own metrics, 139.164 truncated.
	 */
	@Test
	public void aLineIsNoLongerNarrow() {
		int[] units = {  // i n c i d i d u n t _ u t _ l a b o r e _ e t _ d o l o r e
				569, 1024, 909, 569, 1024, 569, 1024, 1024, 1024, 569, 512,
				1024, 569, 512, 569, 909, 1024, 1024, 682, 909, 512,
				909, 569, 512, 1024, 1024, 569, 1024, 682, 909 };
		double exact = 0, rounded = 0, truncated = 0;
		for (int u : units) {
			exact += u * 12.0 / UPEM;
			rounded += OpenFont.convertUnit2PDFUnit(u, UPEM) * 12.0 / 1000.0;
			truncated += ((u * 1000) / UPEM) * 12.0 / 1000.0;
		}
		assertEquals(139.295, exact, 0.001);
		assertEquals(139.164, truncated, 0.001);
		assertEquals(exact, rounded, 0.03);
		// truncation is a tenth of a point short over thirty characters
		assertEquals(0.131, exact - truncated, 0.005);
	}

}
