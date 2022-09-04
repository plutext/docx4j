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

package org.docx4j.fonts.fop.fonts.type1;

import java.awt.geom.Dimension2D;
import java.awt.geom.RectangularShape;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.xmlgraphics.java2d.Dimension2DDouble;
import org.docx4j.fonts.fop.fonts.NamedCharacter;
import org.docx4j.fonts.fop.fonts.SingleByteEncoding;

/**
 * Represents the contents of a Type 1 AFM font metrics file.
 */
public class AFMFile {

    /** logging instance */
    private static final  Logger log = LoggerFactory.getLogger(AFMFile.class);

    private String fontName;
    private String fullName;
    private String familyName;

    private String weight;
    private RectangularShape fontBBox;

    private String encodingScheme;
    private String characterSet;

    private Number capHeight;
    private Number xHeight;
    private Number ascender;
    private Number descender;
    private Number stdHW;
    private Number stdVW;

    private AFMWritingDirectionMetrics[] writingDirectionMetrics
        = new AFMWritingDirectionMetrics[3];

    private List<AFMCharMetrics> charMetrics = new java.util.ArrayList<AFMCharMetrics>();
    private Map<String, AFMCharMetrics> charNameToMetrics
                = new java.util.HashMap<String, AFMCharMetrics>();
    private int firstChar = -1;
    private int lastChar = -1;

    private Map<String, Map<String, Dimension2D>> kerningMap;

    /**
     * Default constructor.
     */
    public AFMFile() {
        //nop
    }

    /**
     * Returns the FontName value.
     * @return the font name
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Sets the FontName value.
     * @param fontName the font name to set
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
     * Returns the FullName value.
     * @return the full name of the font
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the FullName value.
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the FamilyName value.
     * @return the family name of the font
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Sets the FamilyName value.
     * @param familyName the family name to set
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     * Returns the Weight value.
     * @return the weight
     */
    public String getWeight() {
        return weight;
    }

    /**
     * Sets the Weight value.
     * @param weight the weight to set
     */
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     * Returns the FontBBox value.
     * @return the font's bounding box
     */
    public RectangularShape getFontBBox() {
        return fontBBox;
    }

    /**
     * Returns the FontBBox value as integer array.
     * @return the font's bounding box
     */
    public int[] getFontBBoxAsIntArray() {
        RectangularShape rect = getFontBBox();
        return new int[] {
                (int)Math.floor(rect.getMinX()), (int)Math.floor(rect.getMinY()),
                (int)Math.ceil(rect.getMaxX()), (int)Math.ceil(rect.getMaxY())};
    }

    /**
     * Sets the FontBBox value.
     * @param fontBBox the fontBBox to set
     */
    public void setFontBBox(RectangularShape fontBBox) {
        this.fontBBox = fontBBox;
    }

    /**
     * Returns the EncodingScheme value.
     * @return the encoding scheme
     */
    public String getEncodingScheme() {
        return encodingScheme;
    }

    /**
     * Sets the EncodingScheme value
     * @param encodingScheme the encodingScheme to set
     */
    public void setEncodingScheme(String encodingScheme) {
        this.encodingScheme = encodingScheme;
    }

    /**
     * Returns the CharacterSet value.
     * @return the characterSet
     */
    public String getCharacterSet() {
        return characterSet;
    }

    /**
     * Sets the CharacterSet value.
     * @param characterSet the characterSet to set
     */
    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    /**
     * Returns the CapHeight value.
     * @return the capHeight
     */
    public Number getCapHeight() {
        return capHeight;
    }

    /**
     * Sets the CapHeight value.
     * @param capHeight the capHeight to set
     */
    public void setCapHeight(Number capHeight) {
        this.capHeight = capHeight;
    }

    /**
     * Returns the XHeight value.
     * @return the xHeight
     */
    public Number getXHeight() {
        return xHeight;
    }

    /**
     * Sets the XHeight value.
     * @param height the xHeight to set
     */
    public void setXHeight(Number height) {
        xHeight = height;
    }

    /**
     * Returns the Ascender value.
     * @return the ascender
     */
    public Number getAscender() {
        return ascender;
    }

    /**
     * Sets the Ascender value.
     * @param ascender the ascender to set
     */
    public void setAscender(Number ascender) {
        this.ascender = ascender;
    }

    /**
     * Returns the Descender value.
     * @return the descender
     */
    public Number getDescender() {
        return descender;
    }

    /**
     * Sets the Descender value.
     * @param descender the descender to set
     */
    public void setDescender(Number descender) {
        this.descender = descender;
    }

    /**
     * Returns the StdHW value.
     * @return the descender
     */
    public Number getStdHW() {
        return stdHW;
    }

    /**
     * Sets the StdHW value.
     * @param stdHW the StdHW to set
     */
    public void setStdHW(Number stdHW) {
        this.stdHW = stdHW;
    }

    /**
     * Returns the StdVW value.
     * @return the descender
     */
    public Number getStdVW() {
        return stdVW;
    }

    /**
     * Sets the StdVW value.
     * @param stdVW the StdVW to set
     */
    public void setStdVW(Number stdVW) {
        this.stdVW = stdVW;
    }

    /**
     * Gets writing direction metrics.
     * @param index the writing direction (0, 1 or 2)
     * @return the writing direction metrics
     */
    public AFMWritingDirectionMetrics getWritingDirectionMetrics(int index) {
        return this.writingDirectionMetrics[index];
    }

    /**
     * Sets writing direction metrics.
     * @param index the writing direction (0, 1 or 2)
     * @param metrics the writing direction metrics
     */
    public void setWritingDirectionMetrics(int index, AFMWritingDirectionMetrics metrics) {
        this.writingDirectionMetrics[index] = metrics;
    }

    /**
     * Adds new character metrics.
     * @param metrics the character metrics
     */
    public void addCharMetrics(AFMCharMetrics metrics) {
        String name = metrics.getCharName();
        if (metrics.getUnicodeSequence() == null && name.equals(".notdef")) {
            //Ignore as no Unicode assignment is possible
            return;
        }
        this.charMetrics.add(metrics);
        if (name != null) {
            this.charNameToMetrics.put(name, metrics);
        }
        int idx = metrics.getCharCode();
        if (idx >= 0) { //Only if the character is part of the encoding
            if (firstChar < 0 || idx < firstChar) {
                firstChar = idx;
            }
            if (lastChar < 0 || idx > lastChar) {
                lastChar = idx;
            }
        }
    }

    /**
     * Returns the number of character available for this font.
     * @return the number of character
     */
    public int getCharCount() {
        return this.charMetrics.size();
    }

    /**
     * Returns the first character index in the encoding that has a glyph.
     * @return the first character index with a glyph
     */
    public int getFirstChar() {
        return this.firstChar;
    }

    /**
     * Returns the last character index in the encoding that has a glyph.
     * @return the last character index with a glyph
     */
    public int getLastChar() {
        return this.lastChar;
    }

    /**
     * Returns the character metrics associated with the character name.
     * @param name the character name
     * @return the character metrics or null if there's no such character
     */
    public AFMCharMetrics getChar(String name) {
        return this.charNameToMetrics.get(name);
    }

    /**
     * Returns the list of AFMCharMetrics instances representing all the available characters.
     * @return a List of AFMCharMetrics instances
     */
    public List<AFMCharMetrics> getCharMetrics() {
        return Collections.unmodifiableList(this.charMetrics);
    }

    /**
     * Adds a X-kerning entry.
     * @param name1 the name of the first character
     * @param name2 the name of the second character
     * @param kx kerning value in x-direction
     */
    public void addXKerning(String name1, String name2, double kx) {
        if (this.kerningMap == null) {
            this.kerningMap = new java.util.HashMap<String, Map<String, Dimension2D>>();
        }
        Map<String, Dimension2D> entries = this.kerningMap.get(name1);
        if (entries == null) {
            entries = new java.util.HashMap<String, Dimension2D>();
            this.kerningMap.put(name1, entries);
        }
        entries.put(name2, new Dimension2DDouble(kx, 0));
    }

    /**
     * Indicates whether the font has kerning information.
     * @return true if there is kerning information
     */
    public boolean hasKerning() {
        return this.kerningMap != null;
    }

    /**
     * Creates and returns a kerning map for writing mode 0 (ltr) with character codes.
     * @return the kerning map or null if there is no kerning information.
     */
    public Map<Integer, Map<Integer, Integer>> createXKerningMapEncoded() {
        if (!hasKerning()) {
            return null;
        }
        Map<Integer, Map<Integer, Integer>> m
                    = new java.util.HashMap<>();
        for (Map.Entry<String, Map<String, Dimension2D>> entryFrom : this.kerningMap.entrySet()) {
            String name1 = entryFrom.getKey();
            AFMCharMetrics chm1 = getChar(name1);
            if (chm1 == null || !chm1.hasCharCode()) {
                continue;
            }
            Map<Integer, Integer> container = null;
            Map<String, Dimension2D> entriesTo = entryFrom.getValue();
            for (Map.Entry<String, Dimension2D> entryTo : entriesTo.entrySet()) {
                String name2 = entryTo.getKey();
                AFMCharMetrics chm2 = getChar(name2);
                if (chm2 == null || !chm2.hasCharCode()) {
                    continue;
                }
                if (container == null) {
                    Integer k1 = chm1.getCharCode();
                    container = m.computeIfAbsent(k1, k -> new java.util.HashMap<>());
                }
                Dimension2D dim = entryTo.getValue();
                container.put(chm2.getCharCode(),
                        (int) Math.round(dim.getWidth()));
            }
        }
        return m;
    }

    /**
     * The character codes in an AFM cannot always be trusted to be the same values as in the
     * font's primary encoding. Therefore, we provide a way to override this primary encoding.
     * @param encoding the encoding to replace the one given in the AFM
     */
    public void overridePrimaryEncoding(SingleByteEncoding encoding) {
        if (log.isDebugEnabled()) {
            log.debug("Overriding primary encoding of " + getFontName() + " with: " + encoding);
        }
        AFMCharMetrics[] mapped = new AFMCharMetrics[256];
        for (AFMCharMetrics cm : this.charMetrics) {
            NamedCharacter nc = cm.getCharacter();
            if (nc.hasSingleUnicodeValue()) {
                int codePoint = encoding.mapChar(nc.getSingleUnicodeValue());
                if (codePoint > 0) {
                    if (mapped[codePoint] != null) {
                        if (log.isDebugEnabled()) {
                            AFMCharMetrics other = mapped[codePoint];
                            StringBuilder msgBuilder = new StringBuilder();
                            msgBuilder.append("Not mapping character ")
                                    .append(nc)
                                    .append(" to code point ")
                                    .append(codePoint)
                                    .append(" (")
                                    .append(Integer.toHexString(codePoint))
                                    .append(") in ")
                                    .append(encoding)
                                    .append(". ")
                                    .append(other)
                                    .append(" has already been assigned that code point.");
                            if (other.getUnicodeSequence()
                                    .equals(nc.getUnicodeSequence())) {
                                msgBuilder.append(" This is a specialized glyph for the")
                                        .append(" same Unicode character.");
                                //TODO should these be mapped to a private Unicode area to make
                                //them accessible?
                            } else {
                                msgBuilder.append(" This is a similar character.");
                            }
                            if (Double.compare(cm.getWidthX(), other.getWidthX()) != 0) {
                                msgBuilder.append(" They have differing widths: ")
                                        .append(cm.getWidthX())
                                        .append(" vs. ")
                                        .append(other.getWidthX());
                            }
                            log.debug(msgBuilder.toString());
                        }
                    } else {
                        cm.setCharCode(codePoint);
                        mapped[codePoint] = cm;
                    }
                } else {
                    cm.setCharCode(-1);
                }
            } else {
                //No Unicode equivalent
                cm.setCharCode(-1);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "AFM: " + getFullName();
    }

}
