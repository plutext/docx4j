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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.docx4j.fonts.fop.util.CharUtilities;

/**
 * Provides methods to get font information.
 * Naming:
 * glyph index: original index of the glyph in the non-subset font (!= unicode index)
 * character selector: index into a set of glyphs. For subset CID fonts, this starts at 0. For non-subset
 * fonts, this is the same as the glyph index.
 * Unicode index: The Unicode codepoint of a character.
 * Glyph name: the Adobe glyph name (as found in Glyphs.java)
 */
public class CIDFull implements CIDSet {

    private BitSet glyphIndices;
    private final MultiByteFont font;

    public CIDFull(MultiByteFont mbf) {
        font = mbf;
    }

    private void initGlyphIndices() {
        // this cannot be called in the constructor since the font is not ready...
        if (glyphIndices == null) {
            glyphIndices = font.getGlyphIndices();
        }
    }

    /** {@inheritDoc} */
    public int getOriginalGlyphIndex(int index) {
        return index;
    }

    /** {@inheritDoc} */
    @Override
    public char getUnicodeFromGID(int glyphIndex) {
        return ' ';
    }

    /** {@inheritDoc} */
    @Override
    public int getGIDFromChar(char ch) {
        return ch;
    }

    /** {@inheritDoc} */
    public int getUnicode(int index) {
        initGlyphIndices();
        if (glyphIndices.get(index)) {
            return index;
        } else {
            return CharUtilities.NOT_A_CHARACTER;
        }
    }

    /** {@inheritDoc} */
    public int mapChar(int glyphIndex, char unicode) {
        return glyphIndex;
    }

    /** {@inheritDoc} */
    public int mapCodePoint(int glyphIndex, int codePoint) {
        return glyphIndex;
    }

    /** {@inheritDoc} */
    public Map<Integer, Integer> getGlyphs() {
        // this is never really called for full embedded fonts but the equivalent map would be the identity
        initGlyphIndices();
        Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();
        int nextBitSet = 0;
        for (int j = 0; j < glyphIndices.cardinality(); j++) {
            nextBitSet = glyphIndices.nextSetBit(nextBitSet);
            glyphs.put(nextBitSet, nextBitSet);
            nextBitSet++;
        }
        return Collections.unmodifiableMap(glyphs);
    }

    /** {@inheritDoc} */
    public char[] getChars() {
        return font.getChars();
    }

    /** {@inheritDoc} */
    public int getNumberOfGlyphs() {
        initGlyphIndices();
        // note: the real number of glyphs is given by the cardinality() method (not the length()) but since
        // we will pad gaps in the indices with zeros we really want the length() here. this method is only
        // called when embedding a font in PostScript and this will be the value of the CIDCount entry
        return glyphIndices.length();
    }

    /** {@inheritDoc} */
    public BitSet getGlyphIndices() {
        initGlyphIndices();
        return glyphIndices;
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        return font.getWidths();
    }
}
