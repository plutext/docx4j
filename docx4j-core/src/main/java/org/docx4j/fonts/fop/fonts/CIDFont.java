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

import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;

//Java

/**
 * Abstract base class for CID fonts.
 */
public abstract class CIDFont extends CustomFont {

    /** Contains the character widths for all characters in the font */
    protected int[] width;

    /**
     * @param resourceResolver the URI resolver for controlling file access
     */
    public CIDFont(InternalResourceResolver resourceResolver) {
        super(resourceResolver);
    }

    // ---- Required ----
    /**
     * Returns the type of the CID font.
     * @return the type of the CID font
     */
    public abstract CIDFontType getCIDType();

    /**
     * Returns the name of the issuer of the font.
     * @return a String identifying an issuer of character collections -
     * for example, Adobe
     */
    public abstract String getRegistry();

    /**
     * Returns a font name for use within a registry.
     * @return a String that uniquely names a character collection issued by
     * a specific registry - for example, Japan1.
     */
    public abstract String getOrdering();

    /**
     * Returns the supplement number of the character collection.
     * @return the supplement number
     */
    public abstract int getSupplement();

    /**
     * Returns the subset information for this font.
     * @return the subset information
     */
    public abstract CIDSet getCIDSet();

    /**
     * Determines whether this font contains a particular code point/glyph.
     * @param cp character to check
     * @return True if the character is supported, False otherwise
     */
    public abstract boolean hasCodePoint(int cp);

    /**
     * Map a Unicode code point to a code point in the font.
     * @param cp code point to map
     * @return the mapped code point
     */
    public abstract int mapCodePoint(int cp);

    // ---- Optional ----
    /**
     * Returns the default width for this font.
     * @return the default width
     */
    public int getDefaultWidth() {
        return 0;
    }

    /** {@inheritDoc} */
    public boolean isMultiByte() {
        return getFontType() != FontType.TYPE1C;
    }

}
