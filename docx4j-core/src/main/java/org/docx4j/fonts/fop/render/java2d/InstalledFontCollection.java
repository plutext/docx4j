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

/* $Id$ */

package org.docx4j.fonts.fop.render.java2d;

import java.awt.GraphicsEnvironment;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.fonts.Font;
import org.docx4j.fonts.fop.fonts.FontCollection;
import org.docx4j.fonts.fop.fonts.FontInfo;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.fonts.fop.fonts.FontUtil;

/**
 * A custom AWT font collection
 */
public class InstalledFontCollection implements FontCollection {

    private static  Logger log = LoggerFactory.getLogger(InstalledFontCollection.class);

    private static final Set<String> HARDCODED_FONT_NAMES;

    static {
        HARDCODED_FONT_NAMES = new java.util.HashSet<String>();
        HARDCODED_FONT_NAMES.add("any");
        HARDCODED_FONT_NAMES.add("sans-serif");
        HARDCODED_FONT_NAMES.add("serif");
        HARDCODED_FONT_NAMES.add("monospace");

        HARDCODED_FONT_NAMES.add("Helvetica");
        HARDCODED_FONT_NAMES.add("Times");
        HARDCODED_FONT_NAMES.add("Courier");
        HARDCODED_FONT_NAMES.add("Symbol");
        HARDCODED_FONT_NAMES.add("ZapfDingbats");
        HARDCODED_FONT_NAMES.add("Times Roman");
        HARDCODED_FONT_NAMES.add("Times-Roman");
        HARDCODED_FONT_NAMES.add("Computer-Modern-Typewriter");
    }

    /** Required by new instances of FontMetricsMapper */
    private final Java2DFontMetrics java2DFontMetrics;

    /**
     * Main constructor
     *
     * @param java2DFontMetrics required by new instances of FontMetricsMapper
     */
    public InstalledFontCollection(Java2DFontMetrics java2DFontMetrics) {
        this.java2DFontMetrics  = java2DFontMetrics;
    }

    /**
     * {@inheritDoc}
     */
    public int setup(int start, FontInfo fontInfo) {
        int num = start;
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

        java.awt.Font[] fonts = env.getAllFonts();
        for (java.awt.Font f : fonts) {
            if (HARDCODED_FONT_NAMES.contains(f.getName())) {
                continue; //skip
            }

            if (log.isTraceEnabled()) {
                log.trace("AWT Font: " + f.getFontName()
                        + ", family: " + f.getFamily()
                        + ", PS: " + f.getPSName()
                        + ", Name: " + f.getName()
                        + ", Angle: " + f.getItalicAngle()
                        + ", Style: " + f.getStyle());
            }

            String searchName = FontUtil.stripWhiteSpace(f.getName()).toLowerCase();
            String guessedStyle = FontUtil.guessStyle(searchName);
            int guessedWeight = FontUtil.guessWeight(searchName);

            num++;
            String fontKey = "F" + num;
            int style = convertToAWTFontStyle(guessedStyle, guessedWeight);
            addFontMetricsMapper(fontInfo, f.getName(), fontKey, java2DFontMetrics, style);

            //Register appropriate font triplets matching the font. Two different strategies:
            //Example: "Arial Bold", normal, normal
            addFontTriplet(fontInfo, f.getName(),
                    Font.STYLE_NORMAL, Font.WEIGHT_NORMAL, fontKey);
            if (!f.getName().equals(f.getFamily())) {
                //Example: "Arial", bold, normal
                addFontTriplet(fontInfo, f.getFamily(),
                        guessedStyle, guessedWeight, fontKey);
            }
        }
        return num;
    }

    private static void addFontTriplet(FontInfo fontInfo, String fontName, String fontStyle,
            int fontWeight, String fontKey) {
        FontTriplet triplet = FontInfo.createFontKey(fontName, fontStyle, fontWeight);
        fontInfo.addFontProperties(fontKey, triplet);
    }

    private static void addFontMetricsMapper(FontInfo fontInfo, String family, String fontKey,
            Java2DFontMetrics java2DFontMetrics, int style) {
        FontMetricsMapper metric = new SystemFontMetricsMapper(family, style, java2DFontMetrics);
        fontInfo.addMetrics(fontKey, metric);
    }

    private static int convertToAWTFontStyle(String fontStyle, int fontWeight) {
        int style = java.awt.Font.PLAIN;
        if (fontWeight >= Font.WEIGHT_BOLD) {
            style |= java.awt.Font.BOLD;
        }
        if (!"normal".equals(fontStyle)) {
            style |= java.awt.Font.ITALIC;
        }
        return style;
    }
}
