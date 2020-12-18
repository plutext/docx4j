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
import java.util.Map;

/**
 * Declares methods to retrieve font information (glyph indices, widths, unicode values) from a CID font.
 */
public interface CIDSet {

    /**
     * Returns the original index of the glyph inside the (non-subset) font's glyph list. This
     * index can be used to access the character width information, for example.
     * @param index the subset index (character selector) to access the glyph
     * @return the original index (or -1 if no glyph index is available for the subset index)
     */
    int getOriginalGlyphIndex(int index);

    /**
     * Returns the Unicode value for a subset index (character selector). If there's no such
     * Unicode value, the "NOT A CHARACTER" (0xFFFF) is returned.
     * @param index the subset index (character selector)
     * @return the Unicode value or "NOT A CHARACTER" (0xFFFF)
     */
    int getUnicode(int index);

    /**
     * Gets the unicode character from the original font glyph index
     * @param glyphIndex The original glyph index of the character in the font
     * @return The character represented by the passed GID
     */
    char getUnicodeFromGID(int glyphIndex);

    /**
     * Returns the glyph index from the original font from a character
     * @param ch The character
     * @return The glyph index in the original font.
     */
    int getGIDFromChar(char ch);

    /**
     * Maps a character to a character selector for a font subset. If the character isn't in the
     * subset, yet, it is added and a new character selector returned. Otherwise, the already
     * allocated character selector is returned from the existing map/subset.
     * @param glyphIndex the glyph index of the character
     * @param unicode the Unicode index of the character
     * @return the subset index
     */
    int mapChar(int glyphIndex, char unicode);

    /**
     * Maps a character to a character selector for a font subset. If the character isn't in the
     * subset yet, it is added and a new character selector returned. Otherwise, the already
     * allocated character selector is returned from the existing map/subset.
     * @param glyphIndex the glyph index of the character
     * @param codePoint the Unicode index of the character
     * @return the subset index
     */
    int mapCodePoint(int glyphIndex, int codePoint);

    /**
     * Returns an unmodifiable Map of the font subset. It maps from glyph index to
     * character selector (i.e. the subset index in this case).
     * @return Map Map&lt;Integer, Integer&gt; of the font subset
     */
    Map<Integer, Integer> getGlyphs();

    /**
     * Returns a char array containing all Unicode characters that are in the subset.
     * @return a char array with all used Unicode characters
     */
    char[] getChars();

    /**
     * Returns the number of glyphs in the subset.
     * @return the number of glyphs in the subset
     */
    int getNumberOfGlyphs();

    /**
     * Returns a BitSet with bits set for each available glyph index in the subset.
     * @return a BitSet indicating available glyph indices
     */
    BitSet getGlyphIndices();

    /**
     * Return the array of widths.
     * <p>
     * This is used to get an array for inserting in an output format.
     * It should not be used for lookup.
     * @return an array of widths
     */
    int[] getWidths();

}
