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

/* $Id: AFMCharMetrics.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.type1;

import java.awt.geom.RectangularShape;

import org.docx4j.fonts.fop.fonts.NamedCharacter;


/**
 * Holds the metrics of a single character from an AFM file.
 */
public class AFMCharMetrics {

    private int charCode = -1;
    private NamedCharacter character;
    private double widthX;
    private double widthY;
    private RectangularShape bBox;

    /**
     * Returns the character code.
     * @return the charCode (-1 if not part of the encoding)
     */
    public int getCharCode() {
        return charCode;
    }

    /**
     * Indicates whether the character has a character code, i.e. is part of the default encoding.
     * @return true if there is a character code.
     */
    public boolean hasCharCode() {
        return charCode >= 0;
    }

    /**
     * Sets the character code.
     * @param charCode the charCode to set
     */
    public void setCharCode(int charCode) {
        this.charCode = charCode;
    }

    /**
     * Returns the named character represented by this instance.
     * @return the named character (or null if no named character is associated)
     */
    public NamedCharacter getCharacter() {
        return this.character;
    }

    /**
     * Sets the named character represented by this instance.
     * @param ch the named character
     */
    public void setCharacter(NamedCharacter ch) {
        this.character = ch;
    }

    /**
     * Sets the named character represented by this instance.
     * @param charName the character name (as defined in the Adobe glyph list)
     * @param unicodeSequence the Unicode sequence
     */
    public void setCharacter(String charName, String unicodeSequence) {
        setCharacter(new NamedCharacter(charName, unicodeSequence));
    }

    /**
     * Returns the Unicode sequence for this character.
     * @return the Unicode characters
     *                  (or null if no such Unicode sequence exists for this character)
     */
    public String getUnicodeSequence() {
        return (getCharacter() != null ? getCharacter().getUnicodeSequence() : null);
    }

    /**
     * Returns the PostScript character name.
     * @return the charName (or null if no character name is associated)
     */
    public String getCharName() {
        return (getCharacter() != null ? getCharacter().getName() : null);
    }

    /**
     * Returns the progression dimension in x-direction.
     * @return the widthX
     */
    public double getWidthX() {
        return widthX;
    }

    /**
     * Sets the progression dimension in x-direction
     * @param widthX the widthX to set
     */
    public void setWidthX(double widthX) {
        this.widthX = widthX;
    }

    /**
     * Returns the progression dimension in y-direction.
     * @return the widthY
     */
    public double getWidthY() {
        return widthY;
    }

    /**
     * Sets the progression dimension in y-direction
     * @param widthY the widthY to set
     */
    public void setWidthY(double widthY) {
        this.widthY = widthY;
    }

    /**
     * Returns the character's bounding box.
     * @return the bounding box (or null if it isn't available)
     */
    public RectangularShape getBBox() {
        return bBox;
    }

    /**
     * Sets the character's bounding box.
     * @param box the bounding box
     */
    public void setBBox(RectangularShape box) {
        bBox = box;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer("AFM Char: ");
        sb.append(getCharCode());
        sb.append(" (");
        if (getUnicodeSequence() != null) {
            for (int i = 0, c = getUnicodeSequence().length(); i < c; i++) {
                sb.append("0x").append(Integer.toHexString(getUnicodeSequence().charAt(i)));
                sb.append(", ");
            }
        }
        sb.append(getCharName()).append(')');
        return sb.toString();
    }

}
