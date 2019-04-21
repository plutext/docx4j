/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

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

/* $Id: FontSetup.java 707627 2008-10-24 13:20:51Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;

// FOP (base 14 fonts)
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

//TODO remove small dependency on and refactor this

/**
 * Default fonts for FOP application; currently this uses PDF's fonts
 * by default.
 *
 * Assigns the font (with metrics) to internal names like "F1" and
 * assigns family-style-weight triplets to the fonts
 */
public class FontSetup {

    /**
     * logging instance
     */
    protected static Logger log = LoggerFactory.getLogger(FontSetup.class);

    /**
     * Sets up a font info
     * @param fontInfo font info
     */
    public static void setup(FontInfo fontInfo) {
        setup(fontInfo, null, null);
    }

    /**
     * Sets up the font info object.
     *
     * Adds metrics for basic fonts and useful family-style-weight
     * triplets for lookup.
     *
     * @param fontInfo the font info object to set up
     * @param embedFontInfoList a list of EmbedFontInfo objects
     * @param resolver the font resolver
     */
    public static void setup(FontInfo fontInfo, List embedFontInfoList, FontResolver resolver) {
        final boolean base14Kerning = false;
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
        addConfiguredFonts(fontInfo, embedFontInfoList, startNum, resolver);
    }

    /**
     * Add fonts from configuration file starting with internal name F<num>.
     * @param fontInfo the font info to set up
     * @param embedFontInfoList a list of EmbedFontInfo objects
     * @param num starting index for internal font numbering
     * @param resolver the font resolver
     */
    private static void addConfiguredFonts(FontInfo fontInfo,
            List/*<EmbedFontInfo>*/ embedFontInfoList, int num, FontResolver resolver) {
        if (embedFontInfoList == null) {
            return; //No fonts to process
        }

        if (resolver == null) {
            //Ensure that we have minimal font resolution capabilities
            resolver = createMinimalFontResolver();
        }

        String internalName = null;

        for (int i = 0; i < embedFontInfoList.size(); i++) {
            EmbedFontInfo embedFontInfo = (EmbedFontInfo)embedFontInfoList.get(i);

            internalName = "F" + num;
            num++;

            LazyFont font = new LazyFont(embedFontInfo, resolver);
            fontInfo.addMetrics(internalName, font);

            List triplets = embedFontInfo.getFontTriplets();
            for (int tripletIndex = 0; tripletIndex < triplets.size(); tripletIndex++) {
                FontTriplet triplet = (FontTriplet) triplets.get(tripletIndex);
                fontInfo.addFontProperties(internalName, triplet);
            }
        }
    }

    /** @return a new FontResolver to be used by the font subsystem */
    public static FontResolver createMinimalFontResolver() {
        return new FontResolver() {

            /** {@inheritDoc} */
            public Source resolve(String href) {
                //Minimal functionality here
                return new StreamSource(href);
            }
        };
    }
}
