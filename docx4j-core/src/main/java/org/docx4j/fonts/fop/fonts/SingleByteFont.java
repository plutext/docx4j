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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlgraphics.fonts.Glyphs;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.truetype.OpenFont.PostScriptVersion;
import org.docx4j.fonts.fop.util.CharUtilities;

/**
 * Generic SingleByte font
 */
public class SingleByteFont extends CustomFont {

    /** logger */
    private  static  Logger log = LoggerFactory.getLogger(SingleByteFont.class);

    protected SingleByteEncoding mapping;
    private boolean useNativeEncoding;

    protected int[] width;

    private Rectangle[] boundingBoxes;

    private Map<Character, Character> alternativeCodes;

    private PostScriptVersion ttPostScriptVersion;

    private int usedGlyphsCount;
    private LinkedHashMap<Integer, String> usedGlyphNames;
    private Map<Integer, Integer> usedGlyphs;
    private Map<Integer, Character> usedCharsIndex;
    private Map<Character, Integer> charGIDMappings;

    public SingleByteFont(InternalResourceResolver resourceResolver) {
        super(resourceResolver);
        setEncoding(CodePointMapping.WIN_ANSI_ENCODING);
    }

    public SingleByteFont(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
        this(resourceResolver);
        setEmbeddingMode(embeddingMode);
        if (embeddingMode != EmbeddingMode.FULL) {
            usedGlyphNames = new LinkedHashMap<Integer, String>();
            usedGlyphs = new HashMap<Integer, Integer>();
            usedCharsIndex = new HashMap<Integer, Character>();
            charGIDMappings = new HashMap<Character, Integer>();

            // The zeroth value is reserved for .notdef
            usedGlyphs.put(0, 0);
            usedGlyphsCount++;
        }
    }

    /** {@inheritDoc} */
    public boolean isEmbeddable() {
        return (!(getEmbedFileURI() == null
                && getEmbedResourceName() == null));
    }

    /** {@inheritDoc} */
    public boolean isSubsetEmbedded() {
        return getEmbeddingMode() != EmbeddingMode.FULL;
    }

    /** {@inheritDoc} */
    public String getEncodingName() {
        return this.mapping.getName();
    }

    /**
     * Returns the code point mapping (encoding) of this font.
     * @return the code point mapping
     */
    public SingleByteEncoding getEncoding() {
        return this.mapping;
    }

    /** {@inheritDoc} */
    public int getWidth(int i, int size) {
        if (i < 256) {
            int idx = i - getFirstChar();
            if (idx >= 0 && idx < width.length) {
                return size * width[idx];
            }
        } else if (this.additionalEncodings != null) {
            int encodingIndex = (i / 256) - 1;
            SimpleSingleByteEncoding encoding = getAdditionalEncoding(encodingIndex);
            int codePoint = i % 256;
            NamedCharacter nc = encoding.getCharacterForIndex(codePoint);
            UnencodedCharacter uc
                = this.unencodedCharacters.get(nc.getSingleUnicodeValue());
            return size * uc.getWidth();
        }
        return 0;
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length);
        return arr;
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        Rectangle bbox = null;
        if (glyphIndex < 256) {
            int idx = glyphIndex - getFirstChar();
            if (idx >= 0 && idx < boundingBoxes.length) {
                bbox =  boundingBoxes[idx];
            }
        } else if (this.additionalEncodings != null) {
            int encodingIndex = (glyphIndex / 256) - 1;
            SimpleSingleByteEncoding encoding = getAdditionalEncoding(encodingIndex);
            int codePoint = glyphIndex % 256;
            NamedCharacter nc = encoding.getCharacterForIndex(codePoint);
            UnencodedCharacter uc = this.unencodedCharacters.get(nc.getSingleUnicodeValue());
            bbox = uc.getBBox();
        }
        return bbox == null ? null : new Rectangle(bbox.x * size, bbox.y * size, bbox.width * size, bbox.height * size);
    }

    /**
     * Lookup a character using its alternative names. If found, cache it so we
     * can speed up lookups.
     * @param c the character
     * @return the suggested alternative character present in the font
     */
    private char findAlternative(char c) {
        char d;
        if (alternativeCodes == null) {
            alternativeCodes = new java.util.HashMap<Character, Character>();
        } else {
            Character alternative = alternativeCodes.get(c);
            if (alternative != null) {
                return alternative;
            }
        }
        String charName = Glyphs.charToGlyphName(c);
        String[] charNameAlternatives = Glyphs.getCharNameAlternativesFor(charName);
        if (charNameAlternatives != null && charNameAlternatives.length > 0) {
            for (String charNameAlternative : charNameAlternatives) {
                if (log.isDebugEnabled()) {
                    log.debug("Checking alternative for char " + c + " (charname="
                            + charName + "): " + charNameAlternative);
                }
                String s = Glyphs.getUnicodeSequenceForGlyphName(charNameAlternative);
                if (s != null) {
                    d = lookupChar(s.charAt(0));
                    if (d != SingleByteEncoding.NOT_FOUND_CODE_POINT) {
                        alternativeCodes.put(c, d);
                        return d;
                    }
                }
            }
        }

        return SingleByteEncoding.NOT_FOUND_CODE_POINT;
    }

    private char lookupChar(char c) {
        char d = mapping.mapChar(c);
        if (d != SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            return d;
        }

        // Check unencoded characters which are available in the font by
        // character name
        d = mapUnencodedChar(c);
        return d;
    }

    private boolean isSubset() {
        return getEmbeddingMode() == EmbeddingMode.SUBSET;
    }

    /** {@inheritDoc} */
    @Override
    public char mapChar(char c) {
        notifyMapOperation();
        char d = lookupChar(c);
        if (d == SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            // Check for alternative
            d = findAlternative(c);
            if (d != SingleByteEncoding.NOT_FOUND_CODE_POINT) {
                return d;
            } else {
                this.warnMissingGlyph(c);
                return Typeface.NOT_FOUND;
            }
        }
        if (isEmbeddable() && isSubset()) {
            mapChar(d, c);
        }
        return d;
    }

    private int mapChar(int glyphIndex, char unicode) {
        // Reencode to a new subset font or get the reencoded value
        // IOW, accumulate the accessed characters and build a character map for them
        Integer subsetCharSelector = usedGlyphs.get(glyphIndex);
        if (subsetCharSelector == null) {
            int selector = usedGlyphsCount;
            usedGlyphs.put(glyphIndex, selector);
            usedCharsIndex.put(selector, unicode);
            charGIDMappings.put(unicode, glyphIndex);
            usedGlyphsCount++;
            return selector;
        } else {
            return subsetCharSelector;
        }
    }

    private char getUnicode(int index) {
        Character mapValue = usedCharsIndex.get(index);
        if (mapValue != null) {
            return mapValue;
        } else {
            return CharUtilities.NOT_A_CHARACTER;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChar(char c) {
        char d = mapping.mapChar(c);
        if (d != SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            return true;
        }
        //Check unencoded characters which are available in the font by character name
        d = mapUnencodedChar(c);
        if (d != SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            return true;
        }
        // Check if an alternative exists
        d = findAlternative(c);
        if (d != SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            return true;
        }
        return false;
    }

    /* ---- single byte font specific setters --- */

    /**
     * Updates the mapping variable based on the encoding.
     * @param encoding the name of the encoding
     */
    protected void updateMapping(String encoding) {
        try {
            this.mapping = CodePointMapping.getMapping(encoding);
        } catch (UnsupportedOperationException e) {
            log.error("Font '" + super.getFontName() + "': " + e.getMessage());
        }
    }

    /**
     * Sets the encoding of the font.
     * @param encoding the encoding (ex. "WinAnsiEncoding" or "SymbolEncoding")
     */
    public void setEncoding(String encoding) {
        updateMapping(encoding);
    }

    /**
     * Sets the encoding of the font.
     * @param encoding the encoding information
     */
    public void setEncoding(CodePointMapping encoding) {
        this.mapping = encoding;
    }

    /**
     * Controls whether the font is configured to use its native encoding or if it
     * may need to be re-encoded for the target format.
     * @param value true indicates that the configured encoding is the font's native encoding
     */
    public void setUseNativeEncoding(boolean value) {
        this.useNativeEncoding = value;
    }

    /**
     * Indicates whether this font is configured to use its native encoding. This
     * method is used to determine whether the font needs to be re-encoded.
     * @return true if the font uses its native encoding.
     */
    public boolean isUsingNativeEncoding() {
        return this.useNativeEncoding;
    }

    /**
     * Sets a width for a character.
     * @param index index of the character
     * @param w the width of the character
     */
    public void setWidth(int index, int w) {
        if (this.width == null) {
            this.width = new int[getLastChar() - getFirstChar() + 1];
        }
        this.width[index - getFirstChar()] = w;
    }

    public void setBoundingBox(int index, Rectangle bbox) {
        if (this.boundingBoxes == null) {
            this.boundingBoxes = new Rectangle[getLastChar() - getFirstChar() + 1];
        }
        this.boundingBoxes[index - getFirstChar()] = bbox;
    }

    /**
     * Adds an unencoded character (one that is not supported by the primary encoding).
     * @param ch the named character
     * @param width the width of the character
     */
    public void addUnencodedCharacter(NamedCharacter ch, int width, Rectangle bbox) {
        if (this.unencodedCharacters == null) {
            this.unencodedCharacters = new HashMap<Character, UnencodedCharacter>();
        }
        if (ch.hasSingleUnicodeValue()) {
            UnencodedCharacter uc = new UnencodedCharacter(ch, width, bbox);
            this.unencodedCharacters.put(ch.getSingleUnicodeValue(), uc);
        } else {
            //Cannot deal with unicode sequences, so ignore this character
        }
    }

    /**
     * Makes all unencoded characters available through additional encodings. This method
     * is used in cases where the fonts need to be encoded in the target format before
     * all text of the document is processed (for example in PostScript when resource optimization
     * is disabled).
     */
    public void encodeAllUnencodedCharacters() {
        if (this.unencodedCharacters != null) {
            Set<Character> sortedKeys = new TreeSet<Character>(this.unencodedCharacters.keySet());
            for (Character ch : sortedKeys) {
                char mapped = mapChar(ch);
                assert mapped != Typeface.NOT_FOUND;
            }
        }
    }

    /**
     * Returns an array with the widths for an additional encoding.
     * @param index the index of the additional encoding
     * @return the width array
     */
    public int[] getAdditionalWidths(int index) {
        SimpleSingleByteEncoding enc = getAdditionalEncoding(index);
        int[] arr = new int[enc.getLastChar() - SimpleSingleByteEncoding.getFirstChar() + 1];
        for (int i = 0, c = arr.length; i < c; i++) {
            NamedCharacter nc = enc.getCharacterForIndex(SimpleSingleByteEncoding.getFirstChar() + i);
            UnencodedCharacter uc = this.unencodedCharacters.get(
                    nc.getSingleUnicodeValue());
            arr[i] = uc.getWidth();
        }
        return arr;
    }

    protected static final class UnencodedCharacter {

        private final NamedCharacter character;
        private final int width;
        private final Rectangle bbox;

        public UnencodedCharacter(NamedCharacter character, int width, Rectangle bbox) {
            this.character = character;
            this.width = width;
            this.bbox = bbox;
        }

        public NamedCharacter getCharacter() {
            return this.character;
        }

        public int getWidth() {
            return this.width;
        }

        public Rectangle getBBox() {
            return bbox;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return getCharacter().toString();
        }
    }

    /**
     * Sets the version of the PostScript table stored in the TrueType font represented by
     * this instance.
     *
     * @param version version of the post table
     */
    public void setTrueTypePostScriptVersion(PostScriptVersion version) {
        ttPostScriptVersion = version;
    }

    /**
     * Returns the version of the PostScript table stored in the TrueType font represented by
     * this instance.
     *
     * @return the version of the post table
     */
    public PostScriptVersion getTrueTypePostScriptVersion() {
        assert getFontType() == FontType.TRUETYPE;
        return ttPostScriptVersion;
    }

    /**
     * Returns a Map of used Glyphs.
     * @return Map Map of used Glyphs
     */
    public Map<Integer, Integer> getUsedGlyphs() {
        return Collections.unmodifiableMap(usedGlyphs);
    }

    public char getUnicodeFromSelector(int selector) {
        return getUnicode(selector);
    }

    public int getGIDFromChar(char ch) {
        return charGIDMappings.get(ch);
    }

    public char getUnicodeFromGID(int glyphIndex) {
        int selector = usedGlyphs.get(glyphIndex);
        return usedCharsIndex.get(selector);
    }

    public void mapUsedGlyphName(int gid, String value) {
        usedGlyphNames.put(gid, value);
    }

    public Map<Integer, String> getUsedGlyphNames() {
        return usedGlyphNames;
    }

    public String getGlyphName(int idx) {
        if (idx < mapping.getCharNameMap().length) {
            return mapping.getCharNameMap()[idx];
        } else {
            int selector = usedGlyphs.get(idx);
            char theChar = usedCharsIndex.get(selector);
            return unencodedCharacters.get(theChar).getCharacter().getName();
        }
    }
}

