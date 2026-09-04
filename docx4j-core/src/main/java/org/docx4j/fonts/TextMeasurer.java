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

import org.docx4j.fonts.fop.fonts.Typeface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advance width of text in a physical font, from the font's own metrics (the
 * same glyph widths FOP will use), for layout decisions docx4j has to make
 * before FOP sees the document: table autofit (CR-001 Phase 3).
 *
 * Kerning and ligatures are not applied, which matches Word's defaults; complex
 * script shaping is not applied either, so widths for Arabic or Indic text are
 * approximate.  A character the font lacks, or a font that cannot be loaded,
 * counts half an em.
 *
 * @since 17.0.5
 */
public final class TextMeasurer {

	private static final Logger log = LoggerFactory.getLogger(TextMeasurer.class);

	private TextMeasurer() {}

	/** Width in points of the string set in this font at this size. */
	public static double widthPt(String text, PhysicalFont pf, double sizePt) {
		if (text == null || text.isEmpty()) return 0;
		Typeface tf = typeface(pf);
		double w = 0;
		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			i += Character.charCount(cp);
			w += glyphWidthPt(tf, cp, sizePt);
		}
		return w;
	}

	/** Width in points of one code point (tab counts as a space). */
	public static double glyphWidthPt(Typeface tf, int cp, double sizePt) {
		if (cp == '\t') cp = ' ';
		if (tf != null && cp <= 0xFFFF && tf.hasChar((char) cp)) {
			// FontMetrics.getWidth(i, size) is widths[i] (1/1000 em) times size
			return tf.getWidth(tf.mapChar((char) cp), 1000) * sizePt / 1_000_000.0;
		}
		return 0.5 * sizePt;
	}

	public static Typeface typeface(PhysicalFont pf) {
		if (pf == null) return null;
		try {
			return GlyphCheck.getTypeface(pf);
		} catch (Exception e) {
			log.warn("Can't load " + pf.getName() + " for measuring: " + e.getMessage());
			return null;
		}
	}
}
