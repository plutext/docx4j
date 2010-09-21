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

/* $Id: Base14FontCollection.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.base14;

import org.docx4j.fonts.fop.fonts.Font;
import org.docx4j.fonts.fop.fonts.FontCollection;
import org.docx4j.fonts.fop.fonts.FontInfo;

/**
 * Sets up Base 14 fonts
 */
public class Base14FontCollection implements FontCollection {

    private boolean kerning = false;

    /**
     * Main constructor
     *
     * @param kerning set to true when font kerning is enabled
     */
    public Base14FontCollection(boolean kerning) {
        this.kerning  = kerning;
    }

    /**
     * {@inheritDoc}
     */
    public int setup(int start, FontInfo fontInfo) {
        fontInfo.addMetrics("F1", new Helvetica(kerning));
        fontInfo.addMetrics("F2", new HelveticaOblique(kerning));
        fontInfo.addMetrics("F3", new HelveticaBold(kerning));
        fontInfo.addMetrics("F4", new HelveticaBoldOblique(kerning));
        fontInfo.addMetrics("F5", new TimesRoman(kerning));
        fontInfo.addMetrics("F6", new TimesItalic(kerning));
        fontInfo.addMetrics("F7", new TimesBold(kerning));
        fontInfo.addMetrics("F8", new TimesBoldItalic(kerning));
        fontInfo.addMetrics("F9", new Courier(kerning));
        fontInfo.addMetrics("F10", new CourierOblique(kerning));
        fontInfo.addMetrics("F11", new CourierBold(kerning));
        fontInfo.addMetrics("F12", new CourierBoldOblique(kerning));
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

        return 15;
    }
}
