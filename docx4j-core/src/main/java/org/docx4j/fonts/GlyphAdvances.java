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

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.apps.io.ResourceResolverFactory;
import org.docx4j.fonts.fop.fonts.EmbeddingMode;
import org.docx4j.fonts.fop.fonts.EncodingMode;
import org.docx4j.fonts.fop.fonts.FontLoader;
import org.docx4j.fonts.fop.fonts.FontUris;
import org.docx4j.fonts.fop.fonts.MultiByteFont;
import org.docx4j.fonts.fop.fonts.Typeface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The advance of every glyph of a font file, in 1/1000 em, keyed by glyph index -
 * as {@code OpenFont.convertTTFUnit2PDFUnit} gives it, which is to say rounded to
 * the nearest unit rather than truncated.
 *
 * Used by docx4j-export-fo to correct the width table of a font FOP has loaded
 * with its own copy of that code, which truncates; see
 * {@code org.docx4j.fop.fonts.WordGlyphWidths}.
 *
 * @since 17.1.0
 */
public class GlyphAdvances {

	private static final Logger log = LoggerFactory.getLogger(GlyphAdvances.class);

	private GlyphAdvances() {}

	/**
	 * An int per glyph, so these are small (tens of KB for a large font) beside the
	 * Typeface they come from; a bounded number of them is kept.
	 */
	private static final Map<String, int[]> cache =
			Collections.synchronizedMap(new LinkedHashMap<String, int[]>(16, 0.75f, true) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, int[]> eldest) {
					return size() > 64;
				}
			});

	/**
	 * @param embedURI the font file
	 * @param subFontName the sub-font of a collection, or null
	 * @return the advances, or null where the font cannot be read as a TrueType or
	 *         OpenType font
	 */
	public static int[] forFont(URI embedURI, String subFontName) {

		if (embedURI == null) return null;

		String key = embedURI.toString() + '#' + subFontName;
		int[] cached = cache.get(key);
		if (cached != null) return cached.length == 0 ? null : cached;

		int[] widths = widthsOf(font(embedURI, subFontName));
		cache.put(key, widths == null ? new int[0] : widths);
		return widths;
	}

	/**
	 * The advance of each of these characters, in 1/1000 em, or -1 where the font
	 * does not have the character.  For a font FOP loaded as a simple TrueType
	 * font, whose widths are keyed by code point in an encoding rather than by
	 * glyph index.
	 */
	public static int[] forChars(URI embedURI, String subFontName, char[] unicodes) {

		int[] advances = new int[unicodes.length];
		java.util.Arrays.fill(advances, -1);

		MultiByteFont tf = font(embedURI, subFontName);
		int[] widths = widthsOf(tf);
		if (widths == null) return advances;

		for (int i = 0; i < unicodes.length; i++) {
			if (unicodes[i] == 0) continue;
			// findGlyphIndex, not mapChar: mapChar returns the subset index, and
			// registers the character in a CIDSubset we have no business touching
			int gid = tf.findGlyphIndex(unicodes[i]);
			if (gid > 0 && gid < widths.length) advances[i] = widths[gid];
		}
		return advances;
	}

	/** This font file, read through docx4j's own copy of FOP's font code. */
	private static MultiByteFont font(URI embedURI, String subFontName) {

		// a system font docx4j has already parsed: its Typeface is cached, so this is free
		Typeface tf = typefaceOf(embedURI, subFontName);

		if (!(tf instanceof MultiByteFont)) {
			// eg a font embedded in the docx, which is deliberately not in PhysicalFonts
			try {
				tf = FontLoader.loadFont(new FontUris(embedURI, null), subFontName,
						true, EmbeddingMode.AUTO, EncodingMode.AUTO,
						false, false, resolver(), false, false, false);
			} catch (Exception e) {
				log.debug("Can't read " + embedURI + " for its advances: " + e.getMessage());
			}
		}
		return (tf instanceof MultiByteFont) ? (MultiByteFont) tf : null;
	}

	private static int[] widthsOf(MultiByteFont tf) {
		return tf == null ? null : tf.getWidths();
	}

	/** The PhysicalFont docx4j loaded from this file, if there is one. */
	private static Typeface typefaceOf(URI embedURI, String subFontName) {
		Map<String, PhysicalFont> fonts = PhysicalFonts.getPhysicalFonts();
		if (fonts == null) return null;
		synchronized (fonts) {
			for (PhysicalFont pf : fonts.values()) {
				if (!embedURI.equals(pf.getEmbeddedURI())) continue;
				String sub = pf.getEmbedFontInfo() == null ? null
						: pf.getEmbedFontInfo().getSubFontName();
				if (subFontName == null ? sub == null : subFontName.equals(sub)) {
					return pf.getTypeface();
				}
			}
		}
		return null;
	}

	private static volatile InternalResourceResolver resolver;

	private static InternalResourceResolver resolver() {
		if (resolver == null) {
			resolver = ResourceResolverFactory
					.createDefaultInternalResourceResolver(new File(".").toURI());
		}
		return resolver;
	}

}
