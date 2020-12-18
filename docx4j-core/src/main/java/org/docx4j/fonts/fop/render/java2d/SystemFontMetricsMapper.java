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

// Java
import java.awt.Rectangle;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.Typeface;


/**
 * This class implements org.docx4j.fonts.fop.layout.FontMetrics and
 * is added to the hash table in FontInfo. It  deferes the
 * actual calculation of the metrics to
 * Java2DFontMetrics.  It only keeps the java name and
 * style as member varibles
 */

public class SystemFontMetricsMapper extends Typeface implements FontMetricsMapper {

    /**
     * This is a Java2DFontMetrics that does the real calculation.
     * It is only one class that dynamically determines the font-size.
     */
    private final Java2DFontMetrics java2DFontMetrics;

    private final URI fontFileURI;

    /**
     * The java name of the font.
     * #  Make the family name immutable.
     */
    private final String family;

    /**
     * The java style of the font.
     * #  Make the style immutable.
     */
    private final int style;

    /**
     * Constructs a new Font-metrics.
     * @param family the family name of the font (java value)
     * @param style the java type style value of the font
     * @param java2DFontMetrics metric calculations delegated to this
     */
    public SystemFontMetricsMapper(String family, int style, Java2DFontMetrics java2DFontMetrics) {
        URI uri;
        try {
            uri = new URI("system:" + family.toLowerCase());
        } catch (java.net.URISyntaxException e) {
            uri = null;
        }
        this.fontFileURI = uri;
        this.family = family;
        this.style = style;
        this.java2DFontMetrics = java2DFontMetrics;
    }

    /** {@inheritDoc} */
    public final URI getFontURI() {
        return null;
    }

    /** {@inheritDoc} */
    public String getFontName() {
        return family;
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public String getFullName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public Set getFamilyNames() {
        Set s = new java.util.HashSet();
        s.add(this.family);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    public FontType getFontType() {
        return FontType.OTHER;
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxAscent(int size) {
        return java2DFontMetrics.getMaxAscent(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender(int size) {
        return java2DFontMetrics.getAscender(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight(int size) {
        return java2DFontMetrics.getCapHeight(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender(int size) {
        return java2DFontMetrics.getDescender(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getXHeight(int size) {
        return java2DFontMetrics.getXHeight(family, style, size);
    }

    public int getUnderlinePosition(int size) {
        return java2DFontMetrics.getUnderlinePosition(family, style, size);
    }

    public int getUnderlineThickness(int size) {
        return java2DFontMetrics.getUnderlineThickness(family, style, size);
    }

    public int getStrikeoutPosition(int size) {
        return java2DFontMetrics.getStrikeoutPosition(family, style, size);
    }

    public int getStrikeoutThickness(int size) {
        return java2DFontMetrics.getStrikeoutThickness(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth(int i, int size) {
        return java2DFontMetrics.width(i, family, style, size);
    }


    /**
     * {@inheritDoc}
     */
    public int[] getWidths() {
        return java2DFontMetrics.getWidths(family, style, Java2DFontMetrics.FONT_SIZE);
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    public java.awt.Font getFont(int size) {
        return java2DFontMetrics.getFont(family, style, size);
    }

    /**
     * {@inheritDoc}
     */
    public Map getKerningInfo() {
        return java.util.Collections.EMPTY_MAP;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasKerningInfo() {
        return false;
    }

    /** {@inheritDoc} */
    public String getEncodingName() {
        return null; //Not applicable to Java2D rendering
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        return c;
    }

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return java2DFontMetrics.hasChar(family, style, Java2DFontMetrics.FONT_SIZE, c);
    }

}





