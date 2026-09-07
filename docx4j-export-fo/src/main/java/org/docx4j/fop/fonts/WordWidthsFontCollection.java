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

import java.util.List;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;

/**
 * FOP's <code>CustomFontCollection</code>, registering a {@link WordWidthsLazyFont}
 * for each declared font so that its glyph advances are rounded rather than
 * truncated ({@link WordGlyphWidths}).
 *
 * @since 17.1.0
 */
public class WordWidthsFontCollection implements FontCollection {

	private final InternalResourceResolver uriResolver;
	private final List<EmbedFontInfo> embedFontInfoList;
	private final boolean useComplexScripts;

	public WordWidthsFontCollection(InternalResourceResolver uriResolver,
			List<EmbedFontInfo> customFonts, boolean useComplexScriptFeatures) {
		this.uriResolver = uriResolver;
		this.embedFontInfoList = customFonts;
		this.useComplexScripts = useComplexScriptFeatures;
	}

	public int setup(int start, FontInfo fontInfo) {
		int num = start;
		if (embedFontInfoList == null) {
			return num; // No fonts to process
		}

		for (EmbedFontInfo configFontInfo : embedFontInfoList) {

			String internalName = "F" + num;
			num++;

			LazyFont font = WordGlyphWidths.isEnabled()
					? new WordWidthsLazyFont(configFontInfo, uriResolver, useComplexScripts)
					: new LazyFont(configFontInfo, uriResolver, useComplexScripts);
			fontInfo.addMetrics(internalName, font);

			List<FontTriplet> triplets = configFontInfo.getFontTriplets();
			for (FontTriplet triplet : triplets) {
				fontInfo.addFontProperties(internalName, triplet);
			}
		}
		return num;
	}

}
