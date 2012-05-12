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

/* $Id: AbstractCodePointMapping.java 731479 2009-01-05 07:47:02Z jeremias $ */

package org.docx4j.fonts.fop.fonts;

import java.util.Arrays;
import java.util.Map;

import org.apache.xmlgraphics.fonts.Glyphs;

import org.docx4j.fonts.fop.util.CharUtilities;

/**
 * Abstract base class for code point mapping classes (1-byte character encodings).
 */
public class AbstractCodePointMapping implements SingleByteEncoding {

    private String name;
    private char[] latin1Map;
    private char[] characters;
    private char[] codepoints;
    private char[] unicodeMap; //code point to Unicode char
    private String[] charNameMap; //all character names in the encoding
    private Map fallbackMap; //Here we accumulate all mappings we have found through substitution

    /**
     * Main constructor.
     * @param name the name of the encoding
     * @param table the table ([code point, unicode scalar value]+) with the mapping
     */
    public AbstractCodePointMapping(String name, int[] table) {
        this(name, table, null);
    }

    /**
     * Extended constructor.
     * @param name the name of the encoding
     * @param table the table ([code point, unicode scalar value]+) with the mapping
     * @param charNameMap all character names in the encoding (a value of null will be converted
     *                  to ".notdef")
     */
    public AbstractCodePointMapping(String name, int[] table, String[] charNameMap) {
        this.name = name;
        buildFromTable(table);
        if (charNameMap != null) {
            this.charNameMap = new String[256];
            for (int i = 0; i < 256; i++) {
                String charName = charNameMap[i];
                if (charName == null) {
                    this.charNameMap[i] = Glyphs.NOTDEF;
                } else {
                    this.charNameMap[i] = charName;
                }
            }
        }
    }

    /**
     * Builds the internal lookup structures based on a given table.
     * @param table the table ([code point, unicode scalar value]+) with the mapping
     */
    protected void buildFromTable(int[] table) {
        int nonLatin1 = 0;
        latin1Map = new char[256];
        unicodeMap = new char[256];
        Arrays.fill(unicodeMap, CharUtilities.NOT_A_CHARACTER);
        for (int i = 0; i < table.length; i += 2) {
            char unicode = (char)table[i + 1];
            if (unicode < 256) {
                if (latin1Map[unicode] == 0) {
                    latin1Map[unicode] = (char) table[i];
                }
            } else {
                ++nonLatin1;
            }
            if (unicodeMap[table[i]] == CharUtilities.NOT_A_CHARACTER) {
                unicodeMap[table[i]] = unicode;
            }
        }
        characters = new char[nonLatin1];
        codepoints = new char[nonLatin1];
        int top = 0;
        for (int i = 0; i < table.length; i += 2) {
            char c = (char) table[i + 1];
            if (c >= 256) {
               ++top;
               for (int j = top - 1; j >= 0; --j) {
                   if (j > 0 && characters[j - 1] >= c) {
                       characters[j] = characters[j - 1];
                       codepoints[j] = codepoints[j - 1];
                   } else {
                       characters[j] = c;
                       codepoints[j] = (char) table[i];
                       break;
                   }
               }
            }
        }
    }

    /** {@inheritDoc} */
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    public final char mapChar(char c) {
        if (c < 256) {
            char latin1 = latin1Map[c];
            if (latin1 > 0) {
                return latin1;
            }
        }
        int bot = 0, top = characters.length - 1;
        while (top >= bot) {
            int mid = (bot + top) / 2;
            char mc = characters[mid];

            if (c == mc) {
                return codepoints[mid];
            } else if (c < mc) {
                top = mid - 1;
            } else {
                bot = mid + 1;
            }
        }

        //Fallback: using cache
        synchronized (this) {
            if (fallbackMap != null) {
                Character fallback = (Character)fallbackMap.get(new Character(c));
                if (fallback != null) {
                    return fallback.charValue();
                }
            }
        }
        //Fallback: find alternatives (slow!)
        String glyphName = Glyphs.charToGlyphName(c);
        if (glyphName.length() > 0) {
            String[] alternatives = Glyphs.getCharNameAlternativesFor(glyphName);
            if (alternatives != null) {
                for (int i = 0, ic = alternatives.length; i < ic; i++) {
                    int idx = getCodePointForGlyph(alternatives[i]);
                    if (idx >= 0) {
                        putFallbackCharacter(c, (char)idx);
                        return (char)idx;
                    }
                }
            }
        }

        putFallbackCharacter(c, NOT_FOUND_CODE_POINT);
        return NOT_FOUND_CODE_POINT;
    }

    private void putFallbackCharacter(char c, char mapTo) {
        synchronized (this) {
            if (this.fallbackMap == null) {
                this.fallbackMap = new java.util.HashMap();
            }
            this.fallbackMap.put(new Character(c), new Character(mapTo));
        }
    }

    /**
     * Returns the main Unicode value that is associated with the given code point in the encoding.
     * Note that multiple Unicode values can theoretically be mapped to one code point in the
     * encoding.
     * @param idx the code point in the encoding
     * @return the Unicode value (or \uFFFF (NOT A CHARACTER) if no Unicode value is at that point)
     */
    public final char getUnicodeForIndex(int idx) {
        return this.unicodeMap[idx];
    }

    /** {@inheritDoc} */
    public final char[] getUnicodeCharMap() {
        char[] copy = new char[this.unicodeMap.length];
        System.arraycopy(this.unicodeMap, 0, copy, 0, this.unicodeMap.length);
        return copy;
    }

    /**
     * Returns the index of a character/glyph with the given name. Note that this
     * method is relatively slow and should only be used for fallback operations.
     * @param charName the character name
     * @return the index of the character in the encoding or -1 if it doesn't exist
     */
    public short getCodePointForGlyph(String charName) {
        String[] names = this.charNameMap;
        if (names == null) {
            names = getCharNameMap();
        }
        for (short i = 0, c = (short)names.length; i < c; i++) {
            if (names[i].equals(charName)) {
                return i;
            }
        }
        return -1;
    }

    /** {@inheritDoc} */
    public String[] getCharNameMap() {
        if (this.charNameMap != null) {
            String[] copy = new String[this.charNameMap.length];
            System.arraycopy(this.charNameMap, 0, copy, 0, this.charNameMap.length);
            return copy;
        } else {
            //Note: this is suboptimal but will probably never be used.
            String[] derived = new String[256];
            Arrays.fill(derived, Glyphs.NOTDEF);
            for (int i = 0; i < 256; i++) {
                char c = getUnicodeForIndex(i);
                if (c != CharUtilities.NOT_A_CHARACTER) {
                    String charName = Glyphs.charToGlyphName(c);
                    if (charName.length() > 0) {
                        derived[i] = charName;
                    }
                }
            }
            return derived;
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }
}
