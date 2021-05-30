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

package org.docx4j.fonts.fop.fonts;

import java.awt.Rectangle;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Main interface for access to font metrics.
 */
public interface FontMetrics {

    /**
     * Returns the URI of the font file from which these metrics were loaded.
     * @return the font file's URI
     */
    URI getFontURI();

    /**
     * Returns the "PostScript" font name (Example: "Helvetica-BoldOblique").
     * @return the font name
     */
    String getFontName();

    /**
     * Returns the font's full name (Example: "Helvetica Bold Oblique").
     * @return the font's full name
     */
    String getFullName();

    /**
     * Returns the font's family names as a Set of Strings (Example: "Helvetica").
     * @return the font's family names (a Set of Strings)
     */
    Set<String> getFamilyNames();

    /**
     * Returns the font name for font embedding (may include a prefix, Example: "1E28bcArialMT").
     * @return the name for font embedding
     */
    String getEmbedFontName();

    /**
     * Returns the type of the font.
     * @return the font type
     */
    FontType getFontType();


    /**
     * Returns the maximum ascent of the font described by this
     * FontMetrics object. Note: This is not the same as getAscender().
     * @param size font size
     * @return ascent in milliponts
     */
    int getMaxAscent(int size);

    /**
     * Returns the ascent of the font described by this
     * FontMetrics object. It returns the nominal ascent within the em box.
     * @param size font size
     * @return ascent in milliponts
     */
    int getAscender(int size);

    /**
     * Returns the size of a capital letter measured from the font's baseline.
     * @param size font size
     * @return height of capital characters
     */
    int getCapHeight(int size);


    /**
     * Returns the descent of the font described by this
     * FontMetrics object.
     * @param size font size
     * @return descent in milliponts
     */
    int getDescender(int size);


    /**
     * Determines the typical font height of this
     * FontMetrics object
     * @param size font size
     * @return font height in millipoints
     */
    int getXHeight(int size);

    /**
     * Return the width (in 1/1000ths of point size) of the character at
     * code point i.
     * @param i code point index
     * @param size font size
     * @return the width of the character
     */
    int getWidth(int i, int size);

    /**
     * Return the array of widths.
     * <p>
     * This is used to get an array for inserting in an output format.
     * It should not be used for lookup.
     * @return an array of widths
     */
    int[] getWidths();

    /**
     * Returns the bounding box of the glyph at the given index, for the given font size.
     *
     * @param glyphIndex glyph index
     * @param size font size
     * @return the scaled bounding box scaled in 1/1000ths of the given size
     */
    Rectangle getBoundingBox(int glyphIndex, int size);

    /**
     * Indicates if the font has kerning information.
     * @return true if kerning is available.
     */
    boolean hasKerningInfo();

    /**
     * Returns the kerning map for the font.
     * @return the kerning map
     */
    Map<Integer, Map<Integer, Integer>> getKerningInfo();

    /**
     * Returns the distance from the baseline to the center of the underline (negative
     * value indicates below baseline).
     *
     * @param size font size
     * @return the position in 1/1000ths of the font size
     */
    int getUnderlinePosition(int size);

    /**
     * Returns the thickness of the underline.
     *
     * @param size font size
     * @return the thickness in 1/1000ths of the font size
     */
    int getUnderlineThickness(int size);

    /**
     * Returns the distance from the baseline to the center of the strikeout line
     * (negative value indicates below baseline).
     *
     * @param size font size
     * @return the position in 1/1000ths of the font size
     */
    int getStrikeoutPosition(int size);

    /**
     * Returns the thickness of the strikeout line.
     *
     * @param size font size
     * @return the thickness in 1/1000ths of the font size
     */
    int getStrikeoutThickness(int size);

    /**
     * Determine if metrics supports specific feature in specified font table.
     *
     * @param tableType type of table (GSUB, GPOS, ...), see GlyphTable.GLYPH_TABLE_TYPE_*
     * @param script to qualify feature lookup
     * @param language to qualify feature lookup
     * @param feature to test
     * @return true if feature supported (and has at least one lookup)
     */
    boolean hasFeature(int tableType, String script, String language, String feature);

    /**
     * Determines whether the font is a multibyte font.
     * @return True if it is multibyte
     */
    boolean isMultiByte();

}
