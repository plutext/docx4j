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
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.complexscripts.fonts.Positionable;
import org.docx4j.fonts.fop.complexscripts.fonts.Substitutable;

/**
 * This class is used to defer the loading of a font until it is really used.
 */
public class LazyFont extends Typeface implements FontDescriptor, Substitutable, Positionable {

    private static  Logger log = LoggerFactory.getLogger(LazyFont.class);

    private final FontUris fontUris;

    private final boolean useKerning;
    private final boolean useAdvanced;
    private boolean simulateStyle;
    private boolean embedAsType1;
    private final EncodingMode encodingMode;
    private final EmbeddingMode embeddingMode;
    private final String subFontName;
    private final boolean embedded;
    private final InternalResourceResolver resourceResolver;

    private boolean isMetricsLoaded;
    private Typeface realFont;
    private FontDescriptor realFontDescriptor;

    /**
     * Main constructor
     * @param fontInfo  the font info to embed
     * @param resourceResolver the font resolver to handle font URIs
     */
    public LazyFont(EmbedFontInfo fontInfo, InternalResourceResolver resourceResolver,
            boolean useComplexScripts) {

        this.fontUris = fontInfo.getFontUris();
        this.useKerning = fontInfo.getKerning();
        if (resourceResolver != null) {
            this.useAdvanced = useComplexScripts;
        } else {
            this.useAdvanced = fontInfo.getAdvanced();
        }
        this.simulateStyle = fontInfo.getSimulateStyle();
        this.embedAsType1 = fontInfo.getEmbedAsType1();
        this.encodingMode = fontInfo.getEncodingMode() != null ? fontInfo.getEncodingMode()
                : EncodingMode.AUTO;
        this.embeddingMode = fontInfo.getEmbeddingMode() != null ? fontInfo.getEmbeddingMode()
                : EmbeddingMode.AUTO;
        this.subFontName = fontInfo.getSubFontName();
        this.embedded = fontInfo.isEmbedded();
        this.resourceResolver = resourceResolver;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuilder sbuf = new StringBuilder(super.toString());
        sbuf.append('{');
        sbuf.append("metrics-url=").append(fontUris.getMetrics());
        sbuf.append(",embed-url=").append(fontUris.getEmbed());
        sbuf.append(",kerning=").append(useKerning);
        sbuf.append(",advanced=").append(useAdvanced);
        sbuf.append('}');
        return sbuf.toString();
    }

    private void load(boolean fail) {
        if (!isMetricsLoaded) {
            try {
                if (fontUris.getMetrics() != null) {
                    // Use of XML based font metrics is DEPRECATED!
                    // @todo Possible thread problem here
                    XMLFontMetricsReader reader = null;
                    InputStream in = resourceResolver.getResource(fontUris.getMetrics());
                    InputSource src = new InputSource(in);
                    src.setSystemId(fontUris.getMetrics().toASCIIString());
                    reader = new XMLFontMetricsReader(src, resourceResolver);
                    reader.setKerningEnabled(useKerning);
                    reader.setAdvancedEnabled(useAdvanced);
                    if (this.embedded) {
                        reader.setFontEmbedURI(fontUris.getEmbed());
                    }
                    realFont = reader.getFont();
                } else {
                    if (fontUris.getEmbed() == null) {
                        throw new RuntimeException("Cannot load font. No font URIs available.");
                    }
                    realFont = FontLoader.loadFont(fontUris, subFontName, embedded, embeddingMode,
                                encodingMode, useKerning, useAdvanced, resourceResolver, simulateStyle, embedAsType1);
                }
                if (realFont instanceof FontDescriptor) {
                    realFontDescriptor = (FontDescriptor) realFont;
                }
            } catch (RuntimeException e) {
                String error = "Failed to read font file " + fontUris.getEmbed() + ' ' + e.getMessage();
                throw new RuntimeException(error, e);
            } catch (Exception e) {
                String error = "Failed to read font file " + fontUris.getEmbed() + ' ' + e.getMessage();
                log.error(error, e);
                if (fail) {
                    throw new RuntimeException(error, e);
                }
            }
            realFont.setEventListener(this.eventListener);
            isMetricsLoaded = true;
        }
    }

    /**
     * Gets the real font.
     * @return the real font
     */
    public Typeface getRealFont() {
        load(false);
        return realFont;
    }

    // ---- Font ----
    /** {@inheritDoc} */
    public String getEncodingName() {
        load(true);
        return realFont.getEncodingName();
    }

    /**
     * {@inheritDoc}
     */
    public char mapChar(char c) {
        if (!isMetricsLoaded) {
            load(true);
        }
        return realFont.mapChar(c);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hadMappingOperations() {
        load(true);
        return realFont.hadMappingOperations();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChar(char c) {
        if (!isMetricsLoaded) {
            load(true);
        }
        return realFont.hasChar(c);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMultiByte() {
        load(true);
        return realFont.isMultiByte();
    }

    // ---- FontMetrics interface ----
    /** {@inheritDoc} */
    public URI getFontURI() {
        load(true);
        return realFont.getFontURI();
    }

    /** {@inheritDoc} */
    public String getFontName() {
        load(true);
        return realFont.getFontName();
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        load(true);
        return realFont.getEmbedFontName();
    }

    /** {@inheritDoc} */
    public String getFullName() {
        load(true);
        return realFont.getFullName();
    }

    /** {@inheritDoc} */
    public Set<String> getFamilyNames() {
        load(true);
        return realFont.getFamilyNames();
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxAscent(int size) {
        load(true);
        return realFont.getMaxAscent(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender(int size) {
        load(true);
        return realFont.getAscender(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight(int size) {
        load(true);
        return realFont.getCapHeight(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender(int size) {
        load(true);
        return realFont.getDescender(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getXHeight(int size) {
        load(true);
        return realFont.getXHeight(size);
    }

    public int getUnderlinePosition(int size) {
        load(true);
        return realFont.getUnderlinePosition(size);
    }

    public int getUnderlineThickness(int size) {
        load(true);
        return realFont.getUnderlineThickness(size);
    }

    public int getStrikeoutPosition(int size) {
        load(true);
        return realFont.getStrikeoutPosition(size);
    }

    public int getStrikeoutThickness(int size) {
        load(true);
        return realFont.getStrikeoutThickness(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth(int i, int size) {
        if (!isMetricsLoaded) {
            load(true);
        }
        return realFont.getWidth(i, size);
    }

    /**
     * {@inheritDoc}
     */
    public int[] getWidths() {
        load(true);
        return realFont.getWidths();
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        load(true);
        return realFont.getBoundingBox(glyphIndex, size);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasKerningInfo() {
        load(true);
        return realFont.hasKerningInfo();
    }

    /**
     * {@inheritDoc}
     */
    public Map<Integer, Map<Integer, Integer>> getKerningInfo() {
        load(true);
        return realFont.getKerningInfo();
    }

    /** {@inheritDoc} */
    public boolean hasFeature(int tableType, String script, String language, String feature) {
        load(true);
        return realFont.hasFeature(tableType, script, language, feature);
    }

    // ---- FontDescriptor interface ----
    /**
     * {@inheritDoc}
     */
    public int getCapHeight() {
        load(true);
        return realFontDescriptor.getCapHeight();
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender() {
        load(true);
        return realFontDescriptor.getDescender();
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender() {
        load(true);
        return realFontDescriptor.getAscender();
    }

    /** {@inheritDoc} */
    public int getFlags() {
        load(true);
        return realFontDescriptor.getFlags();
    }

    /** {@inheritDoc} */
    public boolean isSymbolicFont() {
        load(true);
        return realFontDescriptor.isSymbolicFont();
    }

    /**
     * {@inheritDoc}
     */
    public int[] getFontBBox() {
        load(true);
        return realFontDescriptor.getFontBBox();
    }

    /**
     * {@inheritDoc}
     */
    public int getItalicAngle() {
        load(true);
        return realFontDescriptor.getItalicAngle();
    }

    /**
     * {@inheritDoc}
     */
    public int getStemV() {
        load(true);
        return realFontDescriptor.getStemV();
    }

    /**
     * {@inheritDoc}
     */
    public FontType getFontType() {
        load(true);
        return realFontDescriptor.getFontType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmbeddable() {
        load(true);
        return realFontDescriptor.isEmbeddable();
    }

    /**
     * {@inheritDoc}
     */
    public boolean performsSubstitution() {
        load(true);
        if (realFontDescriptor instanceof Substitutable) {
            return ((Substitutable)realFontDescriptor).performsSubstitution();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharSequence performSubstitution(CharSequence cs, String script, String language, List associations,
                                            boolean retainControls) {
        load(true);
        if (realFontDescriptor instanceof Substitutable) {
            return ((Substitutable)realFontDescriptor).performSubstitution(cs,
                script, language, associations, retainControls);
        } else {
            return cs;
        }
    }

    /**
     * {@inheritDoc}
     */
    public CharSequence reorderCombiningMarks(
        CharSequence cs, int[][] gpa, String script, String language, List associations) {
        if (!isMetricsLoaded) {
            load(true);
        }
        if (realFontDescriptor instanceof Substitutable) {
            return ((Substitutable)realFontDescriptor)
                .reorderCombiningMarks(cs, gpa, script, language, associations);
        } else {
            return cs;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean performsPositioning() {
        if (!isMetricsLoaded) {
            load(true);
        }
        if (realFontDescriptor instanceof Positionable) {
            return ((Positionable)realFontDescriptor).performsPositioning();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[][]
        performPositioning(CharSequence cs, String script, String language, int fontSize) {
        if (!isMetricsLoaded) {
            load(true);
        }
        if (realFontDescriptor instanceof Positionable) {
            return ((Positionable)realFontDescriptor)
                .performPositioning(cs, script, language, fontSize);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[][]
        performPositioning(CharSequence cs, String script, String language) {
        if (!isMetricsLoaded) {
            load(true);
        }
        if (realFontDescriptor instanceof Positionable) {
            return ((Positionable)realFontDescriptor)
                .performPositioning(cs, script, language);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSubsetEmbedded() {
        load(true);
        if (realFont.isMultiByte() && this.embeddingMode == EmbeddingMode.FULL) {
            return false;
        }
        return realFont.isMultiByte();
    }
}

