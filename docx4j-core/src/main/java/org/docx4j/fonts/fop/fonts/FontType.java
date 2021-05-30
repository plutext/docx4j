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

/**
 * This class enumerates all supported font types.
 */
public class FontType {

    /**
     * Collective identifier for "other" font types
     */
    public static final FontType OTHER       = new FontType("Other", 0);
    /**
     * Adobe Type 0 fonts (composite font)
     */
    public static final FontType TYPE0       = new FontType("Type0", 1);
    /**
     * Adobe Type 1 fonts
     */
    public static final FontType TYPE1       = new FontType("Type1", 2);
    /**
     * Adobe Multiple Master Type 1 fonts
     */
    public static final FontType MMTYPE1     = new FontType("MMType1", 3);
    /**
     * Adobe Type 3 fonts ("user-defined" fonts)
     */
    public static final FontType TYPE3       = new FontType("Type3", 4);
    /**
     * TrueType fonts
     */
    public static final FontType TRUETYPE    = new FontType("TrueType", 5);

    public static final FontType TYPE1C       = new FontType("Type1C", 6);

    public static final FontType CIDTYPE0       = new FontType("CIDFontType0", 7);

    private final String name;
    private final int value;


    /**
     * Construct a font type.
     * @param name a font type name
     * @param value a font type value
     */
    protected FontType(String name, int value) {
        this.name = name;
        this.value = value;
    }


    /**
     * Returns the FontType by name.
     * @param name Name of the font type to look up
     * @return the font type
     */
    public static FontType byName(String name) {
        if (name.equalsIgnoreCase(FontType.OTHER.getName())) {
            return FontType.OTHER;
        } else if (name.equalsIgnoreCase(FontType.TYPE0.getName())) {
            return FontType.TYPE0;
        } else if (name.equalsIgnoreCase(FontType.TYPE1.getName())) {
            return FontType.TYPE1;
        } else if (name.equalsIgnoreCase(FontType.MMTYPE1.getName())) {
            return FontType.MMTYPE1;
        } else if (name.equalsIgnoreCase(FontType.TYPE3.getName())) {
            return FontType.TYPE3;
        } else if (name.equalsIgnoreCase(FontType.TRUETYPE.getName())) {
            return FontType.TRUETYPE;
        } else {
            throw new IllegalArgumentException("Invalid font type: " + name);
        }
    }


    /**
     * Returns the FontType by value.
     * @param value Value of the font type to look up
     * @return the font type
     */
    public static FontType byValue(int value) {
        if (value == FontType.OTHER.getValue()) {
            return FontType.OTHER;
        } else if (value == FontType.TYPE0.getValue()) {
            return FontType.TYPE0;
        } else if (value == FontType.TYPE1.getValue()) {
            return FontType.TYPE1;
        } else if (value == FontType.MMTYPE1.getValue()) {
            return FontType.MMTYPE1;
        } else if (value == FontType.TYPE3.getValue()) {
            return FontType.TYPE3;
        } else if (value == FontType.TRUETYPE.getValue()) {
            return FontType.TRUETYPE;
        } else {
            throw new IllegalArgumentException("Invalid font type: " + value);
        }
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

}
