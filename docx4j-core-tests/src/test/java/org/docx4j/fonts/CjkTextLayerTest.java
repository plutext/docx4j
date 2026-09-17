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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.CMapSegment;
import org.docx4j.fonts.fop.fonts.EmbeddingMode;
import org.docx4j.fonts.fop.fonts.MultiByteFont;
import org.junit.Test;

/**
 * What a PDF reader is told a CJK character is: the two ways docx4j used to lose it.
 *
 * <p>Both are about the text layer alone - the glyph on the page was right either way -
 * and both were measured on two corpus documents whose East Asian font is not installed
 * here, against Word's own PDFs of them (CR-001 batch 45, P19).</p>
 *
 * @since 17.1.1
 */
public class CjkTextLayerTest {

	/**
	 * A font whose cmap maps a Kangxi radical and an ideograph to <b>one glyph</b> is
	 * found, because FOP reads a substituted glyph's character back out of the cmap and
	 * takes the first code point which reaches it - the radical, which is the lower one.
	 * The document's ideograph then reaches the PDF's ToUnicode as the radical.
	 */
	@Test
	public void aRadicalSharingAnIdeographsGlyphIsFound() {
		assertTrue("a radical and an ideograph on one glyph: the reverse lookup takes the radical",
				GlyphCheck.reverseLookupTakesACjkRadical(
						font(seg(0x2F63, 0x2F63, 100), seg(0x751F, 0x751F, 100))));
	}

	/** A radical with a glyph of its own is no trouble: the lookup can only find it. */
	@Test
	public void aRadicalWithItsOwnGlyphIsNot() {
		assertFalse("the radical has its own glyph, so nothing can be taken for it",
				GlyphCheck.reverseLookupTakesACjkRadical(
						font(seg(0x2F63, 0x2F63, 100), seg(0x751F, 0x751F, 200))));
	}

	/** Nor is a font with no radicals at all - every Latin face. */
	@Test
	public void aFontWithNoRadicalsIsNot() {
		assertFalse("no radical block, no round trip to lose",
				GlyphCheck.reverseLookupTakesACjkRadical(
						font(seg(0x0020, 0x007E, 3), seg(0x00A0, 0x00FF, 100))));
	}

	/** The ranges are ranges: a segment covering a run of radicals meets a segment covering
	 *  a run of ideographs at the glyph, not at the code point. */
	@Test
	public void theRangesAreCompared() {
		assertTrue("glyphs 100..120 against 110..130 overlap",
				GlyphCheck.reverseLookupTakesACjkRadical(
						font(seg(0x2F00, 0x2F14, 100), seg(0x4E00, 0x4E14, 110))));
		assertFalse("glyphs 100..120 against 200..220 do not",
				GlyphCheck.reverseLookupTakesACjkRadical(
						font(seg(0x2F00, 0x2F14, 100), seg(0x4E00, 0x4E14, 200))));
	}

	/**
	 * An East Asian punctuation mark is {@code Character.UnicodeScript.COMMON}, and a
	 * Latin face has none of them, so the coverage pass has to look at a span which holds
	 * one - which is how such a mark is commonly written, a run of its own between two
	 * East Asian runs.  Until 17.1.1 a COMMON character counted as always covered, the
	 * pass returned without looking, and FOP painted its not-found character for it.
	 */
	@Test
	public void anEastAsianPunctuationMarkIsLookedAt() {
		assertTrue("a fullwidth comma", FontFallback.needsCoverage(new int[] { 0xFF0C }));
		assertTrue("an ideographic comma", FontFallback.needsCoverage(new int[] { 0x3001 }));
		assertTrue("a fullwidth colon", FontFallback.needsCoverage(new int[] { 0xFF1A }));
		assertTrue("a vertical form", FontFallback.needsCoverage(new int[] { 0xFE10 }));
	}

	/** Latin text is not, so nothing else is asked to do more work than before. */
	@Test
	public void ordinaryLatinTextIsNot() {
		assertFalse("a comma, a space and a digit",
				FontFallback.needsCoverage(new int[] { ',', ' ', '4' }));
		assertFalse("an en dash and a right quote",
				FontFallback.needsCoverage(new int[] { 0x2013, 0x2019 }));
	}

	private static CMapSegment seg(int unicodeStart, int unicodeEnd, int glyphStart) {
		return new CMapSegment(unicodeStart, unicodeEnd, glyphStart);
	}

	private static MultiByteFont font(CMapSegment... cmap) {
		MultiByteFont f = new MultiByteFont((InternalResourceResolver) null, EmbeddingMode.AUTO);
		f.setCMap(cmap);
		return f;
	}
}
