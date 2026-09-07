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
 * @since 17.1.0
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
	 * they are not the same glyph, and outside a declared encoding
	 * (see {@link #declaredEncoding}) which glyph a code stands for is not this rule's
	 * business.
	 */
	private static boolean isTruncationOf(int rounded, int held) {
		return rounded - held == 0 || rounded - held == 1;
	}

	/**
	 * Whether this encoding is one the PDF <em>declares</em>, so that it - and not the
	 * table FOP happens to have built - settles which glyph each code stands for.
	 *
	 * <p>FOP builds a simple font's width table from {@code Glyphs.WINANSI_ENCODING},
	 * which is Adobe's <em>original PostScript</em> WinAnsi vector: its code 96 is
	 * {@code quoteleft} and its 0x98 {@code asciitilde}, where the
	 * {@code /WinAnsiEncoding} it writes into the PDF - and which the viewer reads the
	 * code by, and which FOP's own {@code CodePointMapping} uses to decide which code to
	 * emit - has {@code grave} and {@code tilde}.  {@code OpenFont.initAnsiWidths} indexes
	 * by unicode, so U+2018's advance lands at both 0x60 and 0x91 and U+0060's is never
	 * stored at all.  Measured over the 449 renders of the three corpora: <b>1403 of the
	 * 2514 embedded simple-font width arrays hold quoteleft's advance at code 96</b>
	 * (Carlito is 42/1000 em out, Arimo 111, Caladea -70, DejaVu 183, Noto Sans 106) and
	 * nearly all of them hold asciitilde's at 0x98 (Arimo 251 out, Tinos 208, DejaVu 338,
	 * Caladea 399).  Word's own PDFs write the real glyph's advance in both.
	 *
	 * <p>Those are the only two live disagreements - the rest of the two tables agree, and
	 * the codes where they do not (0x7F, 0x81, 0x8D, 0x8F, 0x90, 0x9D, 0xA0, 0xAD) are
	 * codes nothing maps to.  Where the encoding is declared, then, the character it gives
	 * a code is authoritative and the advance is written whether or not FOP's value is a
	 * truncation of it.  A symbol-encoded or custom-encoded font keeps the truncation-only
	 * rule, since there docx4j's own glyph lookup and FOP's may not agree on the glyph.
	 *
	 * <p>What it costs on real documents is small but real: 4 of the 449 documents paint a
	 * grave or a small tilde at all, and there the line is 0.46 to 1.10pt out (up to 4.3%
	 * of a short line).  What it fixes outright is the {@code /Widths} docx4j writes.
	 *
	 * @since 17.1.0
	 */
	private static boolean declaredEncoding(org.apache.fop.fonts.SingleByteEncoding enc) {
		if (enc == null || enc.getName() == null) return false;
		String name = enc.getName();
		return "WinAnsiEncoding".equals(name)
				|| "StandardEncoding".equals(name)
				|| "MacRomanEncoding".equals(name);
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

				// the encoding the PDF declares settles which glyph a code stands for;
				// elsewhere only the truncation is undone (@since 17.1.0)
				boolean declared = declaredEncoding(enc);
				// getWidths() is offset by getFirstChar(), setWidth(code, w) is not
				int first = sbf.getFirstChar();
				int[] rounded = GlyphAdvances.forChars(embedURI, subFontName, chars);
				for (int code = 0; code < chars.length; code++) {
					if (rounded[code] < 0) continue; // the font has no glyph for that character
					int i = code - first;
					if (i < 0 || i >= widths.length) continue;
					if (declared || isTruncationOf(rounded[code], widths[i])) {
						sbf.setWidth(code, rounded[code]);
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
