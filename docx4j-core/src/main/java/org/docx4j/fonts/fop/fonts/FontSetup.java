/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.docx4j.fonts.fop.fonts;

// FOP (base 14 fonts)
import java.util.List;

import org.docx4j.fonts.fop.fonts.base14.Courier;
import org.docx4j.fonts.fop.fonts.base14.CourierBold;
import org.docx4j.fonts.fop.fonts.base14.CourierBoldOblique;
import org.docx4j.fonts.fop.fonts.base14.CourierOblique;
import org.docx4j.fonts.fop.fonts.base14.Helvetica;
import org.docx4j.fonts.fop.fonts.base14.HelveticaBold;
import org.docx4j.fonts.fop.fonts.base14.HelveticaBoldOblique;
import org.docx4j.fonts.fop.fonts.base14.HelveticaOblique;
import org.docx4j.fonts.fop.fonts.base14.Symbol;
import org.docx4j.fonts.fop.fonts.base14.TimesBold;
import org.docx4j.fonts.fop.fonts.base14.TimesBoldItalic;
import org.docx4j.fonts.fop.fonts.base14.TimesItalic;
import org.docx4j.fonts.fop.fonts.base14.TimesRoman;
import org.docx4j.fonts.fop.fonts.base14.ZapfDingbats;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;

//TODO remove small dependency on and refactor this

/**
 * Default fonts for FOP application; currently this uses PDF's fonts
 * by default.
 *
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts
 */
public final class FontSetup {

    private FontSetup() {
    }

    /**
     * Sets up a font info
     * @param fontInfo font info
     * @param base14Kerning true if base14 kerning applies
     */
    public static void setup(FontInfo fontInfo, boolean base14Kerning) {
        setup(fontInfo, null, null, base14Kerning);
    }

    /**
     * Sets up the font info object.
     *
     * Adds metrics for basic fonts and useful family-style-weight
     * triplets for lookup.
     *
     * @param fontInfo the font info object to set up
     * @param embedFontInfoList a list of EmbedFontInfo objects
     * @param resourceResolver the font resolver
     * @param base14Kerning true if base14 kerning applies
     */
    public static void setup(FontInfo fontInfo, List embedFontInfoList,
            InternalResourceResolver resourceResolver, boolean base14Kerning) {
        fontInfo.addMetrics("F1", new Helvetica(base14Kerning));
        fontInfo.addMetrics("F2", new HelveticaOblique(base14Kerning));
        fontInfo.addMetrics("F3", new HelveticaBold(base14Kerning));
        fontInfo.addMetrics("F4", new HelveticaBoldOblique(base14Kerning));
        fontInfo.addMetrics("F5", new TimesRoman(base14Kerning));
        fontInfo.addMetrics("F6", new TimesItalic(base14Kerning));
        fontInfo.addMetrics("F7", new TimesBold(base14Kerning));
        fontInfo.addMetrics("F8", new TimesBoldItalic(base14Kerning));
        fontInfo.addMetrics("F9", new Courier(base14Kerning));
        fontInfo.addMetrics("F10", new CourierOblique(base14Kerning));
        fontInfo.addMetrics("F11", new CourierBold(base14Kerning));
        fontInfo.addMetrics("F12", new CourierBoldOblique(base14Kerning));
        fontInfo.addMetrics("F13", new Symbol());
        fontInfo.addMetrics("F14", new ZapfDingbats());

        // Custom type 1 fonts step 1/2
        // fontInfo.addMetrics("F15", new OMEP());
        // fontInfo.addMetrics("F16", new GaramondLightCondensed());
        // fontInfo.addMetrics("F17", new BauerBodoniBoldItalic());

        /* any is treated as serif */
        fontInfo.addFontProperties("F5", "any", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "any", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "any", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "any", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "any", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "any", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);

        fontInfo.addFontProperties("F1", "sans-serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "sans-serif", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F3", "sans-serif", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "sans-serif", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F1", "SansSerif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "SansSerif", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "SansSerif", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F3", "SansSerif", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "SansSerif", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "SansSerif", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F5", "serif", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "serif", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "serif", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "serif", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "serif", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "serif", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "monospace", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "monospace", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "monospace", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F11", "monospace", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "monospace", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "monospace", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "Monospaced", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "Monospaced", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "Monospaced", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F11", "Monospaced", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "Monospaced", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "Monospaced", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);

        fontInfo.addFontProperties("F1", "Helvetica", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F2", "Helvetica", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F3", "Helvetica", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F4", "Helvetica", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F5", "Times", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "Times", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "Courier", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "Courier", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F10", "Courier", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F11", "Courier", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "Courier", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F12", "Courier", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F13", "Symbol", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F14", "ZapfDingbats", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);

        // Custom type 1 fonts step 2/2
        // fontInfo.addFontProperties("F15", "OMEP", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F16", "Garamond-LightCondensed", "normal", FontInfo.NORMAL);
        // fontInfo.addFontProperties("F17", "BauerBodoni", "italic", FontInfo.BOLD);

        /* for compatibility with PassiveTex */
        fontInfo.addFontProperties("F5", "Times-Roman", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times-Roman", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "Times-Roman", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times-Roman", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F5", "Times Roman", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", Font.STYLE_OBLIQUE, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F6", "Times Roman", Font.STYLE_ITALIC, Font.WEIGHT_NORMAL);
        fontInfo.addFontProperties("F7", "Times Roman", Font.STYLE_NORMAL, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", Font.STYLE_OBLIQUE, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F8", "Times Roman", Font.STYLE_ITALIC, Font.WEIGHT_BOLD);
        fontInfo.addFontProperties("F9", "Computer-Modern-Typewriter",
                                                        Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);

        // All base 14 configured now, so any custom embedded fonts start from 15
        final int startNum = 15;

        /* Add configured fonts */
        addConfiguredFonts(fontInfo, embedFontInfoList, startNum, resourceResolver, base14Kerning);
    }

    /**
     * Add fonts from configuration file starting with internal name F<num>.
     * @param fontInfo the font info to set up
     * @param embedFontInfoList a list of EmbedFontInfo objects
     * @param num starting index for internal font numbering
     * @param resourceResolver the font resolver
     */
    private static void addConfiguredFonts(FontInfo fontInfo,
            List<EmbedFontInfo> embedFontInfoList, int num,
            InternalResourceResolver resourceResolver,
            boolean base14Kerning) {
        if (embedFontInfoList == null) {
            return; //No fonts to process
        }
        assert resourceResolver != null;

        String internalName = null;

        for (EmbedFontInfo embedFontInfo : embedFontInfoList) {
            internalName = "F" + num;
            num++;

            LazyFont font = new LazyFont(embedFontInfo, resourceResolver, false);
            fontInfo.addMetrics(internalName, font);

            List<FontTriplet> triplets = embedFontInfo.getFontTriplets();
            for (FontTriplet triplet : triplets) {
                fontInfo.addFontProperties(internalName, triplet);
            }
        }
    }
}
