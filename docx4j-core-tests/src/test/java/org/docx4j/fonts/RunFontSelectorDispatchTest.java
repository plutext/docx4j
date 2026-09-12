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

import static org.docx4j.fonts.FontsTestSupport.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.STHint;
import org.junit.Test;

/**
 * The character-range dispatch as a function of the [MS-OI29500] 17.3.2.26 table, with
 * the answers the CR-016 goldens gave (2026-09-12), and spans that join by chosen font.
 */
public class RunFontSelectorDispatchTest {

	private static final String CJK = "日本";      // 日本
	private static final String HEBREW = "שלום"; // שלום
	private static final String DOC = styles("<w:rFonts w:ascii=\"FontA\" w:hAnsi=\"FontH\" w:cs=\"FontC\" w:eastAsia=\"FontE\"/><w:sz w:val=\"22\"/>", null);

	private static WordprocessingMLPackage pkg(String bodyXml) throws Exception {
		return packageWith(DOC, null, null, bodyXml,
				"FontA", SANS, "FontH", SERIF, "FontC", SERIF, "FontE", SANS, "Times New Roman", SERIF);
	}

	/** fontFor as a bare table lookup: the document font names, not the physical ones */
	private static RunFontSelector selector() throws Exception {
		return xslFoSelector(pkg(p(null, "x")));
	}

	@Test
	public void theSpaceIsAscii() throws Exception {
		RunFontSelector rfs = selector();
		// the table: U+0020 is Basic Latin, and Word draws it in the ascii font between
		// two East Asian words (probe fonts-space-cjk: 2.6pt, not MS Gothic's 6)
		assertEquals("ascii", rfs.fontFor(' ', null, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("ea", rfs.fontFor(0x65E5, null, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("ascii", rfs.fontFor('2', null, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("ea", rfs.fontFor(0x3001, null, null, "ea", "ascii", "hAnsi", "cs")); // ideographic comma
	}

	@Test
	public void hebrewAndArabicTakeAscii() throws Exception {
		RunFontSelector rfs = selector();
		// probe fonts-hebrew-no-cs: the ascii font, not Times New Roman (until 17.1.1)
		assertEquals("ascii", rfs.fontFor(0x05E9, null, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("ascii", rfs.fontFor(0x0645, null, null, "ea", "ascii", "hAnsi", "cs"));
		// the Indic ranges: cs (issues 622, 666), hAnsi where the run names none
		assertEquals("cs", rfs.fontFor(0x0915, null, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0x0915, null, null, "ea", "ascii", "hAnsi", null));
	}

	@Test
	public void hintWithNoEastAsianFontIsHAnsi() throws Exception {
		RunFontSelector rfs = selector();
		assertEquals("ea", rfs.fontFor(0x0430, STHint.EAST_ASIA, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0x0430, STHint.EAST_ASIA, null, null, "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0x0430, null, null, "ea", "ascii", "hAnsi", "cs"));
		// Hangul Jamo, CJK, fullwidth forms with no East Asian font: hAnsi, never null
		assertEquals("hAnsi", rfs.fontFor(0x1100, null, null, null, "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0x65E5, null, null, null, "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0xFF1B, null, null, null, "ascii", "hAnsi", "cs"));
	}

	@Test
	public void latin1ExceptionsUnderTheHint() throws Exception {
		RunFontSelector rfs = selector();
		assertEquals("hAnsi", rfs.fontFor(0x00A7, null, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("ea", rfs.fontFor(0x00A7, STHint.EAST_ASIA, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0x00E9, STHint.EAST_ASIA, null, "ea", "ascii", "hAnsi", "cs"));
		assertEquals("ea", rfs.fontFor(0x00E9, STHint.EAST_ASIA, "zh-CN", "ea", "ascii", "hAnsi", "cs"));
		assertEquals("hAnsi", rfs.fontFor(0x00A7, STHint.EAST_ASIA, null, null, "ascii", "hAnsi", "cs"));
	}

	@Test
	public void asciiLettersReturnToAsciiAfterLatin1() throws Exception {
		// probe fonts-missing-slots (c): "caf" ascii, "é" hAnsi, "ber" ascii again; until
		// 17.1.1 the Latin-1 branch reset the range to ASCII and "ber" stayed in hAnsi
		WordprocessingMLPackage pkg = pkg(p(null, "café über"));
		assertEquals(Arrays.asList(SANS, SERIF, SANS, SERIF, SANS), families(pkg, 0));
	}

	@Test
	public void sameFontJoinsAcrossRanges() throws Exception {
		// Georgian (unlisted: hAnsi) then Cyrillic (listed: hAnsi) share a font but not a
		// script, so two spans (the coverage pass substitutes per script, top level - here
		// the Georgian gets a covering face, FontH's Liberation Serif having none); the
		// space (ascii) between two CJK words cuts them; Latin with its accents and
		// punctuation is one span
		WordprocessingMLPackage pkg = pkg(p(null, "გაда") + p(null, CJK + " " + CJK)
				+ p(null, "naïve café, 12"));
		List<String> gc = families(pkg, 0);
		assertEquals(2, gc.size());
		assertEquals(SERIF, gc.get(1));
		// FontA (ascii) and FontH (hAnsi) differ here, so the accented letters are hAnsi's
		// and the ASCII letters around them return to ascii (golden fonts-missing-slots (c));
		// the comma, space and digits are ASCII too
		assertEquals(Arrays.asList(SANS, SERIF, SANS, SERIF, SANS), families(pkg, 2));
		List<String> cjk = families(pkg, 1);
		assertEquals("CJK, space, CJK: three spans", 3, cjk.size());
		// the space keeps the ascii font; the CJK words are in FontE's Liberation Sans, or
		// in the covering face the coverage pass finds where that lacks CJK (this box)
		assertEquals(SANS, cjk.get(1));
		assertEquals(cjk.get(0), cjk.get(2));
	}

	@Test
	public void symbolFontNamesAreCaseInsensitive() throws Exception {
		assertEquals("Symbol", RunFontSelector.symbolFontName("symbol"));
		assertEquals("Wingdings 2", RunFontSelector.symbolFontName("WINGDINGS 2"));
		assertNull(RunFontSelector.symbolFontName("Calibri"));
		assertNull(RunFontSelector.symbolFontName(null));
	}

	@Test
	public void emojiAreTheirOwnCoverageGroup() {
		assertTrue(FontFallback.isEmoji(0x1F600));
		assertTrue(FontFallback.needsCoverage(new int[] { 'a', 0x1F600 }));
		assertFalse(FontFallback.needsCoverage(new int[] { 'a', ' ', '1' }));
		assertEquals(FontFallback.EMOJI_GROUP, FontFallback.coverageGroupOf(0x1F600));
		assertEquals(FontFallback.SYMBOL_GROUP, FontFallback.coverageGroupOf(0x2751));
		assertEquals("COMMON", FontFallback.coverageGroupOf(' '));
	}

	@Test
	public void arabicNumberingGate() {
		assertTrue(RunFontSelector.isArabicScriptLanguage("ar-SA"));
		assertTrue(RunFontSelector.isArabicScriptLanguage("ar-EG"));
		assertTrue(RunFontSelector.isArabicScriptLanguage("fa-IR"));
		assertTrue(RunFontSelector.isArabicScriptLanguage("ur-PK"));
		assertFalse(RunFontSelector.isArabicScriptLanguage("he-IL"));
		assertFalse(RunFontSelector.isArabicScriptLanguage("en-US"));
		assertFalse(RunFontSelector.isArabicScriptLanguage(null));
	}

	@Test
	public void hebrewInARunWithoutCsGetsTheAsciiFont() throws Exception {
		WordprocessingMLPackage pkg = pkg(p(null, HEBREW));
		List<String> f = families(pkg, 0);
		// FontA maps to Liberation Sans, which has Hebrew; nothing to substitute
		assertEquals(Arrays.asList(SANS), f);
	}
}
