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

/* $Id: MultiByteFont.java 721430 2008-11-28 11:13:12Z acumiskey $ */

/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

package org.docx4j.fonts.fop.fonts;

//Java
import java.text.DecimalFormat;
import java.util.Map;


/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont {

    private static int uniqueCounter = -1;

    private String ttcName = null;
    private String encoding = "Identity-H";

    private int defaultWidth = 0;
    private CIDFontType cidType = CIDFontType.CIDTYPE2;

    private String namePrefix = null;    // Quasi unique prefix

    private CIDSubset subset = new CIDSubset();

    /** A map from Unicode indices to glyph indices */
    private BFEntry[] bfentries = null;

    /**
     * Default constructor
     */
    public MultiByteFont() {
        // Make sure that the 3 first glyphs are included
        subset.setupFirstThreeGlyphs();

        // Create a quasiunique prefix for fontname
        synchronized (this.getClass()) {
            uniqueCounter++;
            if (uniqueCounter > 99999 || uniqueCounter < 0) {
                uniqueCounter = 0; //We need maximum 5 character then we start again
            }
        }
        DecimalFormat counterFormat = new DecimalFormat("00000");
        String cntString = counterFormat.format(uniqueCounter);

        //Subset prefix as described in chapter 5.5.3 of PDF 1.4
        StringBuffer sb = new StringBuffer("E");
        for (int i = 0, c = cntString.length(); i < c; i++) {
            //translate numbers to uppercase characters
            sb.append((char)(cntString.charAt(i) + (65 - 48)));
        }
        sb.append("+");
        namePrefix = sb.toString();

        setFontType(FontType.TYPE0);
    }

    /** {@inheritDoc} */
    public int getDefaultWidth() {
        return defaultWidth;
    }

    /** {@inheritDoc} */
    public String getRegistry() {
        return "Adobe";
    }

    /** {@inheritDoc} */
    public String getOrdering() {
        return "UCS";
    }

    /** {@inheritDoc} */
    public int getSupplement() {
        return 0;
    }

    /** {@inheritDoc} */
    public CIDFontType getCIDType() {
        return cidType;
    }

    /**
     * Sets the CIDType.
     * @param cidType The cidType to set
     */
    public void setCIDType(CIDFontType cidType) {
        this.cidType = cidType;
    }

    private String getPrefixedFontName() {
        return namePrefix + FontUtil.stripWhiteSpace(super.getFontName());
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        if (isEmbeddable()) {
            return getPrefixedFontName();
        } else {
            return super.getFontName();
        }
    }

// Comment out - this overrides the value derived by TTFFile, which 
// is not what we want.    
    
//    /** {@inheritDoc} */
//    public boolean isEmbeddable() {
//        return !(getEmbedFileName() == null && getEmbedResourceName() == null);
//    }

    /** {@inheritDoc} */
    public CIDSubset getCIDSubset() {
        return this.subset;
    }

    /** {@inheritDoc} */
    public String getEncodingName() {
        return encoding;
    }

    /** {@inheritDoc} */
    public int getWidth(int i, int size) {
        if (isEmbeddable()) {
            int glyphIndex = subset.getGlyphIndexForSubsetIndex(i);
            return size * width[glyphIndex];
        } else {
            return size * width[i];
        }
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        return arr;
    }

    /**
     * Returns the glyph index for a Unicode character. The method returns 0 if there's no
     * such glyph in the character map.
     * @param c the Unicode character index
     * @return the glyph index (or 0 if the glyph is not available)
     */
    private int findGlyphIndex(char c) {
        int idx = (int)c;
        int retIdx = SingleByteEncoding.NOT_FOUND_CODE_POINT;

        for (int i = 0; (i < bfentries.length) && retIdx == 0; i++) {
            if (bfentries[i].getUnicodeStart() <= idx
                    && bfentries[i].getUnicodeEnd() >= idx) {

                retIdx = bfentries[i].getGlyphStartIndex()
                    + idx
                    - bfentries[i].getUnicodeStart();
            }
        }
        return retIdx;
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        notifyMapOperation();
        int glyphIndex = findGlyphIndex(c);
        if (glyphIndex == SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            warnMissingGlyph(c);
            glyphIndex = findGlyphIndex(Typeface.NOT_FOUND);
        }
        if (isEmbeddable()) {
            glyphIndex = subset.mapSubsetChar(glyphIndex, c);
        }
        return (char)glyphIndex;
    }

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return (findGlyphIndex(c) != SingleByteEncoding.NOT_FOUND_CODE_POINT);
    }

    /**
     * Sets the array of BFEntry instances which constitutes the Unicode to glyph index map for
     * a font. ("BF" means "base font")
     * @param entries the Unicode to glyph index map
     */
    public void setBFEntries(BFEntry[] entries) {
        this.bfentries = entries;
    }

    /**
     * Sets the defaultWidth.
     * @param defaultWidth The defaultWidth to set
     */
    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    /**
     * Returns the TrueType Collection Name.
     * @return the TrueType Collection Name
     */
    public String getTTCName() {
        return ttcName;
    }

    /**
     * Sets the the TrueType Collection Name.
     * @param ttcName the TrueType Collection Name
     */
    public void setTTCName(String ttcName) {
        this.ttcName = ttcName;
    }

    /**
     * Sets the width array.
     * @param wds array of widths.
     */
    public void setWidthArray(int[] wds) {
        this.width = wds;
    }

    /**
     * Returns a Map of used Glyphs.
     * @return Map Map of used Glyphs
     */
    public Map getUsedGlyphs() {
        return subset.getSubsetGlyphs();
    }

    /** {@inheritDoc} */
    public char[] getCharsUsed() {
        if (!isEmbeddable()) {
            return null;
        }
        return subset.getSubsetChars();
    }
}

