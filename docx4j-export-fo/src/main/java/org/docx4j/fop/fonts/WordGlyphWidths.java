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
package org.docx4j.fop.fonts;

import java.net.URI;

import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.docx4j.Docx4jProperties;
import org.docx4j.fonts.GlyphAdvances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Glyph advances rounded to the nearest 1/1000 em, instead of truncated.
 *
 * FOP's <code>OpenFont.convertTTFUnit2PDFUnit</code> divides where it should
 * round, so every advance in the width table it builds is up to one unit short.
 * Over a line that is 0.07 - 0.09%: a 30-character line of 12pt Liberation
 * Serif measures 139.164pt where the font's own metrics give 139.295, and a
 * 74-character one 376.284 against 376.559 - enough that a line Word keeps
 * whole is broken here.  Word measures with the font's exact advances, and
 * writes rounded widths into its PDF's <code>/Widths</code>.
 *
 * docx4j's own copy of that font code rounds (so its {@code TextMeasurer}, and
 * with it the table autofit pass, measure as Word does), but FOP loads its
 * fonts with its own copy, so the width table of the font FOP has loaded is
 * corrected here from the same font file read through docx4j's.  The one array
 * feeds both FOP's line measure and the <code>/Widths</code> the PDF renderer
 * writes, so the text layer stays consistent with the glyph positions.
 *
 * Off with <code>docx4j.convert.out.fo.glyphWidths.round=false</code>.
 *
 * @since 17.0.6
 */
public class WordGlyphWidths {

	private static final Logger log = LoggerFactory.getLogger(WordGlyphWidths.class);

	private WordGlyphWidths() {}

	/** The property which turns the correction off. */
	public static final String PROPERTY = "docx4j.convert.out.fo.glyphWidths.round";

	public static boolean isEnabled() {
		return Docx4jProperties.getProperty(PROPERTY, true);
	}

	/**
	 * Whether the width FOP holds is this advance truncated, so that writing the rounded
	 * one changes nothing but the truncation.  Where the two disagree by more than a unit
	 * they are not the same glyph - FOP's simple-font encoding puts <code>quoteleft</code>
	 * at code 96 where the <code>/WinAnsiEncoding</code> it declares has <code>grave</code>,
	 * for one - and which glyph a code stands for is not this rule's business.
	 */
	private static boolean isTruncationOf(int rounded, int held) {
		return rounded - held == 0 || rounded - held == 1;
	}

	/**
	 * Correct the width table of a font FOP has loaded.  Idempotent in effect: the
	 * values written are the font file's own, so writing them twice changes nothing.
	 *
	 * @param realFont the font FOP loaded (a LazyFont's real font)
	 * @param embedURI the font file it was loaded from
	 * @param subFontName the sub-font of a collection, or null
	 */
	public static void correct(Typeface realFont, URI embedURI, String subFontName) {

		if (realFont == null || embedURI == null) return;

		try {
			if (realFont instanceof SingleByteFont) {

				// the simple TrueType case (and the +noliga twin): the widths are keyed by
				// code point in the font's own encoding, not by glyph index
				SingleByteFont sbf = (SingleByteFont) realFont;
				org.apache.fop.fonts.SingleByteEncoding enc = sbf.getEncoding();
				int[] widths = sbf.getWidths();
				// keyed by code, holding the character the code stands for
				char[] chars = enc == null ? null : enc.getUnicodeCharMap();
				if (widths == null || chars == null) return;

				int[] rounded = GlyphAdvances.forChars(embedURI, subFontName, chars);
				for (int i = 0; i < chars.length; i++) {
					if (rounded[i] < 0) continue;
					int code = enc.mapChar(chars[i]);
					if (code >= 0 && code < widths.length && isTruncationOf(rounded[i], widths[code])) {
						sbf.setWidth(code, rounded[i]);
					}
				}
				return;
			}

			int[] rounded = GlyphAdvances.forFont(embedURI, subFontName);
			if (rounded == null) return;

			if (realFont instanceof MultiByteFont) {

				MultiByteFont mbf = (MultiByteFont) realFont;
				int[] widths = mbf.getWidths();
				if (widths == null || widths.length != rounded.length) {
					// a different sub-font of a collection, or a font docx4j read differently
					log.debug("Not rounding advances for " + embedURI + ": "
							+ (widths == null ? "no widths"
									: widths.length + " glyphs against " + rounded.length));
					return;
				}
				for (int gid = 0; gid < widths.length; gid++) {
					if (isTruncationOf(rounded[gid], widths[gid])) widths[gid] = rounded[gid];
				}
				mbf.setWidthArray(widths);

			}
		} catch (Exception e) {
			// a font we cannot read twice is not worth failing a conversion for
			log.warn("Can't round glyph advances for " + embedURI + ": " + e.getMessage());
		}
	}

}
