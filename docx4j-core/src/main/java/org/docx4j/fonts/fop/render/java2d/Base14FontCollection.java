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

package org.docx4j.fonts.fop.render.java2d;

import org.docx4j.fonts.fop.fonts.Font;
import org.docx4j.fonts.fop.fonts.FontCollection;
import org.docx4j.fonts.fop.fonts.FontInfo;

/**
 * A base 14 font collection for graphics 2D
 */
public class Base14FontCollection implements FontCollection {

    /** required when creating new instances of SystemFontMetricsMapper */
    private final Java2DFontMetrics java2DFontMetrics;

    /**
     * Main constructor
     * @param java2DFontMetrics required when creating new instances of SystemFontMetricsMapper
     */
    public Base14FontCollection(Java2DFontMetrics java2DFontMetrics) {
        this.java2DFontMetrics = java2DFontMetrics;
    }

    /**
     * {@inheritDoc}
     */
    public int setup(int start, FontInfo fontInfo) {
        /*
         * available java fonts are:
         * Serif - bold, normal, italic, bold-italic
         * SansSerif - bold, normal, italic, bold-italic
         * MonoSpaced - bold, normal, italic, bold-italic
         */
        final int normal = java.awt.Font.PLAIN;
        final int bold = java.awt.Font.BOLD;
        final int italic = java.awt.Font.ITALIC;
        final int bolditalic = java.awt.Font.BOLD + java.awt.Font.ITALIC;

        FontMetricsMapper metric;
        metric = new SystemFontMetricsMapper("SansSerif", normal, java2DFontMetrics);
        // --> goes to  F1
        fontInfo.addMetrics("F1", metric);
        metric = new SystemFontMetricsMapper("SansSerif", italic, java2DFontMetrics);
        // --> goes to  F2
        fontInfo.addMetrics("F2", metric);
        metric = new SystemFontMetricsMapper("SansSerif", bold, java2DFontMetrics);
        // --> goes to  F3
        fontInfo.addMetrics("F3", metric);
        metric = new SystemFontMetricsMapper("SansSerif", bolditalic, java2DFontMetrics);
        // --> goes to  F4
        fontInfo.addMetrics("F4", metric);


        metric = new SystemFontMetricsMapper("Serif", normal, java2DFontMetrics);
        // --> goes to  F5
        fontInfo.addMetrics("F5", metric);
        metric = new SystemFontMetricsMapper("Serif", italic, java2DFontMetrics);
        // --> goes to  F6
        fontInfo.addMetrics("F6", metric);
        metric = new SystemFontMetricsMapper("Serif", bold, java2DFontMetrics);
        // --> goes to  F7
        fontInfo.addMetrics("F7", metric);
        metric = new SystemFontMetricsMapper("Serif", bolditalic, java2DFontMetrics);
        // --> goes to  F8
        fontInfo.addMetrics("F8", metric);

        metric = new SystemFontMetricsMapper("MonoSpaced", normal, java2DFontMetrics);
        // --> goes to  F9
        fontInfo.addMetrics("F9", metric);
        metric = new SystemFontMetricsMapper("MonoSpaced", italic, java2DFontMetrics);
        // --> goes to  F10
        fontInfo.addMetrics("F10", metric);
        metric = new SystemFontMetricsMapper("MonoSpaced", bold, java2DFontMetrics);
        // --> goes to  F11
        fontInfo.addMetrics("F11", metric);
        metric = new SystemFontMetricsMapper("MonoSpaced", bolditalic, java2DFontMetrics);
        // --> goes to  F12
        fontInfo.addMetrics("F12", metric);

        metric = new SystemFontMetricsMapper("Serif", normal, java2DFontMetrics);
        //"Symbol" doesn't seem to work here, but "Serif" does the job just fine. *shrug*
        // --> goes to  F13 and F14
        fontInfo.addMetrics("F13", metric);
        fontInfo.addMetrics("F14", metric);

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
                                   "normal", Font.WEIGHT_NORMAL);

        return 15;
    }
}
