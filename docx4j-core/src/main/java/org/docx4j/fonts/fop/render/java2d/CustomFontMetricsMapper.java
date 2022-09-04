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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.fonts.fop.complexscripts.fonts.Positionable;
import org.docx4j.fonts.fop.complexscripts.fonts.Substitutable;
import org.docx4j.fonts.fop.fonts.CustomFont;
import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.LazyFont;
import org.docx4j.fonts.fop.fonts.Typeface;

/**
 * FontMetricsMapper that delegates most methods to an underlying
 * {@link org.docx4j.fonts.fop.fonts.FontMetrics} instance. This class was designed to allow
 * the underlying {@link Font} to be loaded from a
 * user-configured file not registered in the current graphics environment.
 */
public class CustomFontMetricsMapper extends Typeface implements FontMetricsMapper, Substitutable,
        Positionable {

    /**
     * Font metrics for the font this class models.
     */
    private Typeface typeface;

    /**
     * The font required by the Java2D renderer.
     */
    private java.awt.Font font;

    /**
     * Maintains the most recently requested size.
     */
    private float size = 1;

    /**
     * Construction of this class results in the immediate construction
     * of the underlying {@link java.awt.Font}.
     * @param fontMetrics the metrics of the custom font
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final CustomFont fontMetrics)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        initialize(fontMetrics.getInputStream());
    }

    /**
     * Construction of this class results in the immediate construction
     * of the underlying {@link java.awt.Font}.
     * @param fontMetrics the font
     * @param fontSource the font source to access the font
     * @throws FontFormatException if a bad font is loaded
     * @throws IOException if an I/O error occurs
     */
    public CustomFontMetricsMapper(final LazyFont fontMetrics, final InputStream fontSource)
            throws FontFormatException, IOException {
        this.typeface = fontMetrics;
        initialize(fontSource);
    }

    private static final int TYPE1_FONT = 1; //Defined in Java 1.5

    /**
     * Loads the java.awt.Font
     * @param inStream
     * @throws FontFormatException
     * @throws IOException
     */
    private void initialize(final InputStream inStream)
                throws FontFormatException, IOException {
        int type = Font.TRUETYPE_FONT;
        if (FontType.TYPE1.equals(typeface.getFontType())) {
            type = TYPE1_FONT; //Font.TYPE1_FONT; only available in Java 1.5
        }
        this.font = Font.createFont(type, inStream);
        inStream.close();
    }

    /** {@inheritDoc} */
    public final String getEncodingName() {
        return null; //Not applicable to Java2D rendering
    }

    /** {@inheritDoc} */
    public final boolean hasChar(final char c) {
        return font.canDisplay(c);
    }

    /** {@inheritDoc} */
    public final char mapChar(final char c) {
        return typeface.mapChar(c);
    }

    /** {@inheritDoc} */
    public final Font getFont(final int size) {
        if ( Float.compare(this.size, size) == 0) {
            return font;
        }

        this.size = size / 1000f;
        font = font.deriveFont(this.size);
        return font;
    }

    /** {@inheritDoc} */
    public final int getAscender(final int size) {
        return typeface.getAscender(size);
    }

    /** {@inheritDoc} */
    public final int getCapHeight(final int size) {
        return typeface.getCapHeight(size);
    }

    /** {@inheritDoc} */
    public final int getDescender(final int size) {
        return typeface.getDescender(size);
    }

    /** {@inheritDoc} */
    public final String getEmbedFontName() {
        return typeface.getEmbedFontName();
    }

    /** {@inheritDoc} */
    public final Set<String> getFamilyNames() {
        return typeface.getFamilyNames();
    }

    /** {@inheritDoc} */
    public final String getFontName() {
        return typeface.getFontName();
    }

    /** {@inheritDoc} */
    public final URI getFontURI() {
        return typeface.getFontURI();
    }

    /** {@inheritDoc} */
    public final FontType getFontType() {
        return typeface.getFontType();
    }

    /** {@inheritDoc} */
    public final String getFullName() {
        return typeface.getFullName();
    }

    /** {@inheritDoc} */
    public final Map<Integer, Map <Integer, Integer>> getKerningInfo() {
        return typeface.getKerningInfo();
    }

    /** {@inheritDoc} */
    public final int getWidth(final int i, final int size) {
        return typeface.getWidth(i, size);
    }

    /** {@inheritDoc} */
    public final int[] getWidths() {
        return typeface.getWidths();
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        return typeface.getBoundingBox(glyphIndex, size);
    }

    /** {@inheritDoc} */
    public final int getXHeight(final int size) {
        return typeface.getXHeight(size);
    }

    public int getUnderlinePosition(int size) {
        return typeface.getUnderlinePosition(size);
    }

    public int getUnderlineThickness(int size) {
        return typeface.getUnderlineThickness(size);
    }

    public int getStrikeoutPosition(int size) {
        return typeface.getStrikeoutPosition(size);
    }

    public int getStrikeoutThickness(int size) {
        return typeface.getStrikeoutThickness(size);
    }

    /** {@inheritDoc} */
    public final boolean hasKerningInfo() {
        return typeface.hasKerningInfo();
    }

    /** {@inheritDoc} */
    public boolean isMultiByte() {
        return typeface.isMultiByte();
    }

    /**
     * {@inheritDoc}
     */
    public boolean performsPositioning() {
        if (typeface instanceof Positionable) {
            return ((Positionable) typeface).performsPositioning();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[][] performPositioning(CharSequence cs, String script, String language, int fontSize) {
        if (typeface instanceof Positionable) {
            return ((Positionable) typeface).performPositioning(cs, script, language, fontSize);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[][] performPositioning(CharSequence cs, String script, String language) {
        if (typeface instanceof Positionable) {
            return ((Positionable) typeface).performPositioning(cs, script, language);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean performsSubstitution() {
        if (typeface instanceof Substitutable) {
            return ((Substitutable) typeface).performsSubstitution();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharSequence performSubstitution(CharSequence cs, String script, String language, List associations,
                                            boolean retainControls) {
        if (typeface instanceof Substitutable) {
            return ((Substitutable) typeface).performSubstitution(cs, script, language, associations, retainControls);
        } else {
            return cs;
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa,
        String script, String language, List associations) {
        if (typeface instanceof Substitutable) {
            return ((Substitutable) typeface).reorderCombiningMarks(cs, gpa, script, language, associations);
        } else {
            return cs;
        }
    }

    public Typeface getRealFont() {
        return typeface;
    }

}
