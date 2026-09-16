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
package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Where a line comes out the box {@code docx4j:line-box} declares, it takes the baseline
 * {@code docx4j:baseline} declares with it (CR-001 batch 44, M10).
 *
 * <p>The numbers are the ones a generated reproduction of a corpus document's shape
 * produced - a bulleted item whose Symbol label this machine substitutes with DejaVu Serif
 * and whose 9pt body run names Meiryo, which it lacks, so the paragraph's line box is the
 * East Asian one, 1.3 times Meiryo's usWin box, which at 9pt is 17.55pt. Traced with the
 * line manager's own debug log:
 *
 * <pre>
 * wordLine: box=17550 baseline=9541 -&gt; ascent=13689 descent=3861   the item's first line
 * wordLine: box=17550 baseline=9540 -&gt; ascent=9540  descent=8010   the label's line
 * </pre>
 *
 * <p>The label's line was right all along; the body's sat 4.148pt low, which is the 4.15pt
 * that document shows between 854 of its bullets and their text.
 */
public class DeclaredBaselineLineTest {

	/** The corpus shape: the line is the declared box, its baseline is not the declared one. */
	@Test
	public void aLineOfTheDeclaredBoxTakesTheDeclaredBaseline() {
		assertArrayEquals(new int[] { 9541, 8009 },
				WordLineLayoutManager.declaredBaselineLine(17550, 9541, 13689, 3861));
	}

	/** The label's own line, which already agrees: nothing to do. */
	@Test
	public void aLineWhichAlreadyHasItIsLeftAlone() {
		assertArrayEquals(new int[] { 9540, 8010 },
				WordLineLayoutManager.declaredBaselineLine(17550, 9540, 9540, 8010));
	}

	/**
	 * A line of smaller runs is genuinely shorter than the paragraph and has a baseline of
	 * its own - the shape {@link WordLayoutCustomizer#labelAscentAgainstBaseline()} is
	 * about. Here the block declares 14000/11000 and the line came out 7448 + 6552 = 14000
	 * only because its runs were measured at the paragraph's pitch; give it a line of
	 * 6475 + 3030 = 9505, well short of the declared 14000, and the rule must not fire.
	 */
	@Test
	public void aLineShorterThanTheDeclaredBoxKeepsItsOwnBaseline() {
		assertArrayEquals(new int[] { 6475, 3030 },
				WordLineLayoutManager.declaredBaselineLine(14000, 11000, 6475, 3030));
	}

	/** A block with no declared pair - most of them - is untouched. */
	@Test
	public void aBlockWithNoDeclaredPairIsUntouched() {
		assertArrayEquals(new int[] { 13689, 3861 },
				WordLineLayoutManager.declaredBaselineLine(0, 0, 13689, 3861));
		assertArrayEquals(new int[] { 13689, 3861 },
				WordLineLayoutManager.declaredBaselineLine(17550, 0, 13689, 3861));
	}

	/**
	 * {@code docx4j:line-box == docx4j:baseline} is how
	 * {@code WordLayoutFixups.imageOnlyLineBox} marks a paragraph whose only content is an
	 * inline picture, and it is handled above this rule; the rule leaves it alone.
	 */
	@Test
	public void anImageOnlyLineIsLeftAlone() {
		assertArrayEquals(new int[] { 17550, 0 },
				WordLineLayoutManager.declaredBaselineLine(17550, 17550, 17550, 0));
	}
}
