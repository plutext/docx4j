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

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

/**
 * A FOP {@link LazyFont} whose glyph advances are rounded to the nearest
 * 1/1000 em rather than truncated ({@link WordGlyphWidths}).
 *
 * The correction is applied to the real font the first time it is loaded, so
 * a font the document never uses is never read; everything that reads a width -
 * FOP's line measure, and the <code>/Widths</code> the PDF renderer writes from
 * the same array - sees the corrected table.
 *
 * @since 17.1.0
 */
public class WordWidthsLazyFont extends LazyFont {

	private final URI embedURI;
	private final String subFontName;
	private volatile boolean corrected;

	public WordWidthsLazyFont(EmbedFontInfo fontInfo, InternalResourceResolver resourceResolver,
			boolean useComplexScripts) {
		super(fontInfo, resourceResolver, useComplexScripts);
		this.embedURI = fontInfo.getEmbedURI();
		this.subFontName = fontInfo.getSubFontName();
	}

	@Override
	public Typeface getRealFont() {
		Typeface real = super.getRealFont();
		if (!corrected) {
			synchronized (this) {
				if (!corrected) {
					corrected = true;
					WordGlyphWidths.correct(real, embedURI, subFontName);
				}
			}
		}
		return real;
	}

	// The width table has to be corrected before anything reads it.  LazyFont's own
	// accessors go straight to its private realFont field, so the ones that carry a
	// width take the real font through getRealFont() first.

	@Override
	public int getWidth(int i, int size) {
		getRealFont();
		return super.getWidth(i, size);
	}

	@Override
	public int[] getWidths() {
		getRealFont();
		return super.getWidths();
	}

	@Override
	public java.awt.Rectangle getBoundingBox(int glyphIndex, int size) {
		getRealFont();
		return super.getBoundingBox(glyphIndex, size);
	}

	@Override
	public char mapChar(char c) {
		getRealFont();
		return super.mapChar(c);
	}

}
