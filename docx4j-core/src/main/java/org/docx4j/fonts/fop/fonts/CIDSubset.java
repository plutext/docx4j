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

/* $Id: CIDSubset.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts;

import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.docx4j.fonts.fop.util.CharUtilities;

//Naming:
//glyph index: original index of the glyph in the non-subset font (!= unicode index)
//character selector: index into a set of glyphs. For subset CID fonts, this starts at 0. For
//  non-subset fonts, this is the same as the glyph index.
//Unicode index: The Unicode codepoint of a character.
//Glyph name: the Adobe glyph name (as found in Glyphs.java)

/**
 * Keeps track of the glyphs used in a document. This information is later used to build
 * a subset of a font.
 */
public class CIDSubset {

    /**
     * usedGlyphs contains orginal, new glyph index (glyph index -> char selector)
     */
    private Map/*<Integer, Integer>*/ usedGlyphs = new java.util.HashMap();

    /**
     * usedGlyphsIndex contains new glyph, original index (char selector -> glyph index)
     */
    private Map/*<Integer, Integer>*/ usedGlyphsIndex = new java.util.HashMap();
    private int usedGlyphsCount = 0;

    /**
     * usedCharsIndex contains new glyph, original char (char selector -> Unicode)
     */
    private Map/*<Integer, Character>*/ usedCharsIndex = new java.util.HashMap();

    public CIDSubset() {
    }

    /**
     * Adds the initial 3 glyphs which are the same for all CID subsets.
     */
    public void setupFirstThreeGlyphs() {
        // Make sure that the 3 first glyphs are included
        usedGlyphs.put(new Integer(0), new Integer(0));
        usedGlyphsIndex.put(new Integer(0), new Integer(0));
        usedGlyphsCount++;
        usedGlyphs.put(new Integer(1), new Integer(1));
        usedGlyphsIndex.put(new Integer(1), new Integer(1));
        usedGlyphsCount++;
        usedGlyphs.put(new Integer(2), new Integer(2));
        usedGlyphsIndex.put(new Integer(2), new Integer(2));
        usedGlyphsCount++;
    }

    /**
     * Returns the original index of the glyph inside the (non-subset) font's glyph list. This
     * index can be used to access the character width information, for example.
     * @param subsetIndex the subset index (character selector) to access the glyph
     * @return the original index (or -1 if no glyph index is available for the subset index)
     */
    public int getGlyphIndexForSubsetIndex(int subsetIndex) {
        Integer glyphIndex = (Integer)usedGlyphsIndex.get(new Integer(subsetIndex));
        if (glyphIndex != null) {
            return glyphIndex.intValue();
        } else {
            return -1;
        }
    }

    /**
     * Returns the Unicode value for a subset index (character selector). If there's no such
     * Unicode value, the "NOT A CHARACTER" (0xFFFF) is returned.
     * @param subsetIndex the subset index (character selector)
     * @return the Unicode value or "NOT A CHARACTER" (0xFFFF)
     */
    public char getUnicodeForSubsetIndex(int subsetIndex) {
        Character mapValue = (Character)usedCharsIndex.get(new Integer(subsetIndex));
        if (mapValue != null) {
            return mapValue.charValue();
        } else {
            return CharUtilities.NOT_A_CHARACTER;
        }
    }

    /**
     * Maps a character to a character selector for a font subset. If the character isn't in the
     * subset, yet, it is added and a new character selector returned. Otherwise, the already
     * allocated character selector is returned from the existing map/subset.
     * @param glyphIndex the glyph index of the character
     * @param unicode the Unicode index of the character
     * @return the subset index
     */
    public int mapSubsetChar(int glyphIndex, char unicode) {
        // Reencode to a new subset font or get the reencoded value
        // IOW, accumulate the accessed characters and build a character map for them
        Integer subsetCharSelector = (Integer)usedGlyphs.get(new Integer(glyphIndex));
        if (subsetCharSelector == null) {
            int selector = usedGlyphsCount;
            usedGlyphs.put(new Integer(glyphIndex),
                           new Integer(selector));
            usedGlyphsIndex.put(new Integer(selector),
                                new Integer(glyphIndex));
            usedCharsIndex.put(new Integer(selector),
                                new Character(unicode));
            usedGlyphsCount++;
            return selector;
        } else {
            return subsetCharSelector.intValue();
        }
    }

    /**
     * Returns an unmodifiable Map of the font subset. It maps from glyph index to
     * character selector (i.e. the subset index in this case).
     * @return Map Map&lt;Integer, Integer&gt; of the font subset
     */
    public Map/*<Integer, Integer>*/ getSubsetGlyphs() {
        return Collections.unmodifiableMap(this.usedGlyphs);
    }

    /**
     * Returns a char array containing all Unicode characters that are in the subset.
     * @return a char array with all used Unicode characters
     */
    public char[] getSubsetChars() {
        char[] charArray = new char[usedGlyphsCount];
        for (int i = 0; i < usedGlyphsCount; i++) {
            charArray[i] = getUnicodeForSubsetIndex(i);
        }
        return charArray;
    }

    /**
     * Returns the number of glyphs in the subset.
     * @return the number of glyphs in the subset
     */
    public int getSubsetSize() {
        return this.usedGlyphsCount;
    }

    /**
     * Returns a BitSet with bits set for each available glyph index.
     * @return a BitSet indicating available glyph indices
     */
    public BitSet getGlyphIndexBitSet() {
        BitSet bitset = new BitSet();
        Iterator iter = usedGlyphs.keySet().iterator();
        while (iter.hasNext()) {
            Integer cid = (Integer)iter.next();
            bitset.set(cid.intValue());
        }
        return bitset;
    }

}
