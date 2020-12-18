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
 * This class enumerates all supported CID font types.
 */
public enum CIDFontType {

    /**
     * CID Font Type 0 (based on Type 1 format)
     */
    CIDTYPE0("CIDFontType0", 0),

    /**
     * CID Font Type 2 (based on TrueType format)
     */
    CIDTYPE2("CIDFontType2", 2);

    private final String name;
    private final int value;

    /**
     * Construct a CID font type.
     * @param name a type name
     * @param value a type value
     */
    CIDFontType(String name, int value) {
        this.name = name;
        this.value = value;
    }


    /**
     * Returns the CIDFontType by name.
     * @param name Name of the CID font type to look up
     * @return FontType the CID font type
     */
    public static CIDFontType byName(String name) {
        if (name.equalsIgnoreCase(CIDFontType.CIDTYPE0.getName())) {
            return CIDFontType.CIDTYPE0;
        } else if (name.equalsIgnoreCase(CIDFontType.CIDTYPE2.getName())) {
            return CIDFontType.CIDTYPE2;
        } else {
            throw new IllegalArgumentException("Invalid CID font type: " + name);
        }
    }


    /**
     * Returns the CID FontType by value.
     * @param value Value of the CID font type to look up
     * @return FontType the CID font type
     */
    public static CIDFontType byValue(int value) {
        if (value == CIDFontType.CIDTYPE0.getValue()) {
            return CIDFontType.CIDTYPE0;
        } else if (value == CIDFontType.CIDTYPE2.getValue()) {
            return CIDFontType.CIDTYPE2;
        } else {
            throw new IllegalArgumentException("Invalid CID font type: " + value);
        }
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
