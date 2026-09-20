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

			/* FOP drops the per-font "advanced" attribute wherever a resource resolver
			 * is given, which is every PDF run (LazyFont's constructor: "if
			 * (resourceResolver != null) this.useAdvanced = useComplexScripts"), so a
			 * font declared with advanced="false" has its OpenType layout read and
			 * applied all the same.  The flag is passed in here instead, which is where
			 * FOP reads it from; complex scripts still have to be on for the run's font
			 * to shape at all.  @since 17.2.0 */
			boolean advanced = useComplexScripts && configFontInfo.getAdvanced();

			LazyFont font = WordGlyphWidths.isEnabled()
					? new WordWidthsLazyFont(configFontInfo, uriResolver, advanced)
					: new LazyFont(configFontInfo, uriResolver, advanced);
			fontInfo.addMetrics(internalName, font);

			List<FontTriplet> triplets = configFontInfo.getFontTriplets();
			for (FontTriplet triplet : triplets) {
				fontInfo.addFontProperties(internalName, triplet);
			}
		}
		return num;
	}

}
