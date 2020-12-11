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

/* $Id: NamedCharacter.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts;

import org.apache.xmlgraphics.fonts.Glyphs;

import org.docx4j.fonts.fop.util.CharUtilities;

/**
 * Represents an named character with character name (from the Adobe glyph list) and a Unicode
 * sequence that this character represents.
 */
public class NamedCharacter {

    private String charName;
    private String unicodeSequence;

    /**
     * Main constructor.
     * @param charName the character name
     * @param unicodeSequence the Unicode sequence associated with this character
     */
    public NamedCharacter(String charName, String unicodeSequence) {
        if (charName == null) {
            throw new NullPointerException("charName must not be null");
        }
        this.charName = charName;
        if (unicodeSequence != null) {
            this.unicodeSequence = unicodeSequence;
        } else {
            this.unicodeSequence = Glyphs.getUnicodeSequenceForGlyphName(charName);
        }
    }

    /**
     * Simple constructor.
     * @param charName the character name
     */
    public NamedCharacter(String charName) {
        this(charName, null);
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((charName == null) ? 0 : charName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamedCharacter other = (NamedCharacter)obj;
        return charName.equals(other.charName);
    }

    /**
     * Returns the character name (as defined by the Adobe glyph list).
     * @return the character name
     */
    public String getName() {
        return this.charName;
    }

    /**
     * Returns the Unicode sequence associated with this character.
     * @return the Unicode sequence (or null if no Unicode sequence is associated)
     */
    public String getUnicodeSequence() {
        return this.unicodeSequence;
    }

    /**
     * Indicates whether a single Unicode value is associated with this character.
     * @return true if exactly one Unicode value is associated with this character, false otherwise
     */
    public boolean hasSingleUnicodeValue() {
        return (this.unicodeSequence != null && this.unicodeSequence.length() == 1);
    }

    /**
     * Returns the single Unicode value associated with this named character. Check
     * {@link #hasSingleUnicodeValue()} before you call this method because an
     * IllegalStateException is thrown is a Unicode sequence with more than one character is
     * associated with this character.
     * @return the single Unicode value (or FFFF ("NOT A CHARACTER") if no Unicode value is
     *             available)
     * @throws IllegalStateException if a Unicode sequence with more than one value is associated
     *             with the named character
     */
    public char getSingleUnicodeValue() throws IllegalStateException {
        if (this.unicodeSequence == null) {
            return CharUtilities.NOT_A_CHARACTER;
        }
        if (this.unicodeSequence.length() > 1) {
            throw new IllegalStateException("getSingleUnicodeValue() may not be called for a"
                    + " named character that has more than one Unicode value (a sequence)"
                    + " associated with the named character!");
        }
        return this.unicodeSequence.charAt(0);
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(this.unicodeSequence);
        sb.append(" (");
        if (this.unicodeSequence != null) {
            for (int i = 0, c = this.unicodeSequence.length(); i < c; i++) {
                sb.append("0x").append(Integer.toHexString(this.unicodeSequence.charAt(0)));
            }
            sb.append(", ");
        }
        sb.append(getName()).append(')');
        return sb.toString();
    }
}