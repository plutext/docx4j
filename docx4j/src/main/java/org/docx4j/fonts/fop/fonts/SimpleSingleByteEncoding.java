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

/* $Id: SimpleSingleByteEncoding.java 731479 2009-01-05 07:47:02Z jeremias $ */

package org.docx4j.fonts.fop.fonts;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.xmlgraphics.fonts.Glyphs;

import org.docx4j.fonts.fop.util.CharUtilities;

/**
 * A simple implementation of the OneByteEncoding mostly used for encodings that are constructed
 * on-the-fly.
 */
public class SimpleSingleByteEncoding implements SingleByteEncoding {

    private String name;
    private List mapping = new java.util.ArrayList();
    //List<NamedCharacter>
    private Map charMap = new java.util.HashMap();
    //Map<Character(Unicode), Character(code point)>

    /**
     * Main constructor.
     * @param name the encoding's name
     */
    public SimpleSingleByteEncoding(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        Character nc = (Character)charMap.get(new Character(c));
        if (nc != null) {
            return nc.charValue();
        }
        return NOT_FOUND_CODE_POINT;
    }

    /** {@inheritDoc} */
    public String[] getCharNameMap() {
        String[] map = new String[getSize()];
        Arrays.fill(map, Glyphs.NOTDEF);
        for (int i = getFirstChar(); i <= getLastChar(); i++) {
            NamedCharacter ch = (NamedCharacter)this.mapping.get(i - 1);
            map[i] = ch.getName();
        }
        return map;
    }

    /**
     * Returns the index of the first defined character.
     * @return the index of the first defined character (always 1 for this class)
     */
    public int getFirstChar() {
        return 1;
    }

    /**
     * Returns the index of the last defined character.
     * @return the index of the last defined character
     */
    public int getLastChar() {
        return this.mapping.size();
    }

    /**
     * Returns the number of characters defined by this encoding.
     * @return the number of characters
     */
    public int getSize() {
        return this.mapping.size() + 1;
    }

    /**
     * Indicates whether the encoding is full (with 256 code points).
     * @return true if the encoding is full
     */
    public boolean isFull() {
        return (getSize() == 256);
    }

    /**
     * Adds a new character to the encoding.
     * @param ch the named character
     * @return the code point assigned to the character
     */
    public char addCharacter(NamedCharacter ch) {
        if (!ch.hasSingleUnicodeValue()) {
            throw new IllegalArgumentException("Only NamedCharacters with a single Unicode value"
                    + " are currently supported!");
        }
        if (isFull()) {
            throw new IllegalStateException("Encoding is full!");
        }
        char newSlot = (char)(getLastChar() + 1);
        this.mapping.add(ch);
        this.charMap.put(new Character(ch.getSingleUnicodeValue()), new Character(newSlot));
        return newSlot;
    }

    /**
     * Returns the named character at a given code point in the encoding.
     * @param codePoint the code point of the character
     * @return the NamedCharacter (or null if no character is at this position)
     */
    public NamedCharacter getCharacterForIndex(int codePoint) {
        if (codePoint < 0 || codePoint > 255) {
            throw new IllegalArgumentException("codePoint must be between 0 and 255");
        }
        if (codePoint <= getLastChar()) {
            return (NamedCharacter)this.mapping.get(codePoint - 1);
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public char[] getUnicodeCharMap() {
        char[] map = new char[getLastChar() + 1];
        for (int i = 0; i < getFirstChar(); i++) {
            map[i] = CharUtilities.NOT_A_CHARACTER;
        }
        for (int i = getFirstChar(); i <= getLastChar(); i++) {
            map[i] = getCharacterForIndex(i).getSingleUnicodeValue();
        }
        return map;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName() + " (" + getSize() + " chars)";
    }

}
