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
package org.docx4j.fidelity.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * The token a leading symbol bullet is collapsed to.
 *
 * <p>Word's PDF gives a {@code w:sym} bullet the symbol font's own code point, in the
 * private-use area; docx4j gives the Unicode character the glyph stands for.  Measured over
 * one corpus: {@code U+F02A} against {@code U+2217} on three documents (62, 30 and 14
 * lines), {@code U+F077} against {@code U+2B25} on a fourth (61), and {@code U+F072},
 * {@code U+F0A8}, {@code U+F06F} and {@code U+F0EA} against their own substitutes
 * elsewhere.  Neither name is worth defending - Word's copies as noise - so the metric
 * stops asking, on both sides alike.</p>
 *
 * @since 17.2.0 (CR-001 batch 45, P18)
 */
public class BulletNormaliseTest {

	private static final String B = "⁃";

	/** Word's private-use bullet and docx4j's substitute for it come to one token. */
	@Test
	public void twoNamesForOneBulletBecomeOneToken() {
		assertEquals(B + "Siegel", PdfLayout.Line.normaliseBullet(" Siegel"));
		assertEquals(B + "Siegel", PdfLayout.Line.normaliseBullet("∗Siegel"));
		assertEquals(B + "Item", PdfLayout.Line.normaliseBullet(" Item"));
		assertEquals(B + "Item", PdfLayout.Line.normaliseBullet("⬥ Item"));
	}

	/** A bullet both renders agree on is collapsed too, so nothing that paired stops. */
	@Test
	public void anAgreedBulletIsCollapsedOnBothSides() {
		assertEquals(B + "Analyzed pelt", PdfLayout.Line.normaliseBullet("▪ Analyzed pelt"));
		assertEquals(B + "Item", PdfLayout.Line.normaliseBullet("• Item"));
		assertEquals(B + "Item", PdfLayout.Line.normaliseBullet("· Item"));
	}

	/**
	 * Nothing a sentence is written with may be collapsed: a hyphen or a full stop typed
	 * as text, an opening bracket or quotation mark, a currency amount, arithmetic, and a
	 * bullet with no word after it.
	 */
	@Test
	public void textIsUntouched() {
		String[] left = {
			"Siegel", "- Siegel", ". Siegel", "(a) Siegel", "“Siegel”",
			"# 5 Siegel", "€ 5,00", "+ 5 = 6", "1. Siegel", "• • •",
			"•", "• – dash", "",
		};
		for (String s : left) {
			assertEquals(s, PdfLayout.Line.normaliseBullet(s));
		}
	}

	/**
	 * And the harness's own placeholders are not bullets, although two of them are
	 * symbols: a key which opens with the date placeholder has to survive being joined by
	 * the merge pass, or a table row of two dates stops pairing with the two cells we
	 * read it as (measured: 11 matched lines of one corpus document).
	 */
	@Test
	public void theHarnessOwnPlaceholdersAreNotBullets() {
		assertEquals("￼d ￼d", PdfLayout.Line.joinedKey("￼d ￼d"));
		assertEquals("￼t now", PdfLayout.Line.normaliseBullet("￼t now"));
		assertEquals("� broken", PdfLayout.Line.normaliseBullet("� broken"));
		assertEquals("… 5", PdfLayout.Line.normaliseBullet("… 5"));
	}

	/** A run the merge pass joins goes through the rule <b>as a whole</b> and not piece by
	 *  piece, so it comes to the same key as the single line it has to pair with. */
	@Test
	public void aJoinedRunIsNormalisedAsAWhole() {
		assertEquals(B + "Physical access", PdfLayout.Line.joinedKey("\u2022 Physical access"));
		assertEquals(B + "Physical access", PdfLayout.Line.joinedKey("\u2022Physical access"));
		// the single line, and the run joined out of its two pieces, agree: a corpus table
		// row Word reads as one line and we read as two, each with its own bullet
		PdfLayout.Line whole = new PdfLayout.Line();
		whole.text = "\u2022 Ecz \u2022 Thy";
		PdfLayout.Line a = new PdfLayout.Line();
		a.text = "\u2022 Ecz";
		PdfLayout.Line b = new PdfLayout.Line();
		b.text = "\u2022 Thy";
		assertEquals(whole.key(), PdfLayout.Line.joinedKey(
				PdfLayout.Line.mergePiece(a) + " " + PdfLayout.Line.mergePiece(b)));
	}

	/** And a bullet on one side only is still a difference. */
	@Test
	public void aMissingBulletStillFails() {
		assertNotEquals(PdfLayout.Line.normaliseBullet(" Siegel"),
				PdfLayout.Line.normaliseBullet("Siegel"));
	}

	/** The token travels through the rest of key(): the leader run still collapses. */
	@Test
	public void theTokenTravelsWithTheLeaderNormalisation() {
		PdfLayout.Line l = new PdfLayout.Line();
		l.text = " Siegel ..... 1";
		assertEquals(B + "Siegel…1", l.key());
	}
}
