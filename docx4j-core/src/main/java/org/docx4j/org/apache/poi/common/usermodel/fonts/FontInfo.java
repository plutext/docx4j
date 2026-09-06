/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.common.usermodel.fonts;

import java.util.Collections;
import java.util.List;

import org.docx4j.org.apache.poi.util.Beta;

/**
 * A FontInfo object holds information about a font configuration.
 * It is roughly an equivalent to the LOGFONT structure in Windows GDI.<p>
 *
 * If an implementation doesn't provide a property, the getter will return {@code null} -
 * if the value is unset, a default value will be returned.<p>
 *
 * Setting an unsupported property results in an {@link UnsupportedOperationException}.
 *
 * @since POI 3.17-beta2
 *
 * @see <a href="https://msdn.microsoft.com/en-us/library/dd145037.aspx">LOGFONT structure</a>
 */
@SuppressWarnings("unused")
public interface FontInfo {

    /**
     * Get the index within the collection of Font objects
     * @return unique index number of the underlying record this Font represents
     *   (probably you don't care unless you're comparing which one is which)
     */
    default Integer getIndex() {
        return null;
    }

    /**
     * Sets the index within the collection of Font objects
     *
     * @param index the index within the collection of Font objects
     *
     * @throws UnsupportedOperationException if unsupported
     */
    default void setIndex(int index) {
        throw new UnsupportedOperationException("FontInfo is read-only.");
    }
    
    
    /**
     * @return the full name of the font, i.e. font family + type face
     */
    String getTypeface();

    /**
     * Sets the font name
     *
     * @param typeface the full name of the font, when {@code null} removes the font definition -
     *    removal is implementation specific
     * @throws UnsupportedOperationException can return UnsupportedOperationException when FontInfo is read-only
     */
    default void setTypeface(String typeface) {
        throw new UnsupportedOperationException("FontInfo is read-only.");
    }

    /**
     * @return the font charset
     */
    default FontCharset getCharset() {
        return FontCharset.ANSI;
    }

    /**
     * Sets the charset
     *
     * @param charset the charset
     * @throws UnsupportedOperationException can return UnsupportedOperationException when FontInfo is read-only
     */
    default void setCharset(FontCharset charset) {
        throw new UnsupportedOperationException("FontInfo is read-only.");
    }

    /**
     * @return the family class
     */
    default FontFamily getFamily() {
        return FontFamily.FF_DONTCARE;
    }

    /**
     * Sets the font family class
     *
     * @param family the font family class
     * @throws UnsupportedOperationException can return UnsupportedOperationException when FontInfo is read-only
     */
    default void setFamily(FontFamily family) {
        throw new UnsupportedOperationException("FontInfo is read-only.");
    }

    /**
     * @return the font pitch or {@code null} if unsupported
     */
    default FontPitch getPitch() {
        return null;
    }

    /**
     * Set the font pitch
     *
     * @param pitch the font pitch
     *
     * @throws UnsupportedOperationException if unsupported
     */
    default void setPitch(FontPitch pitch) {
        throw new UnsupportedOperationException("FontInfo is read-only.");
    }

    /**
     * @return panose info in binary form or {@code null} if unknown
     */
    default byte[] getPanose() {
        return null;
    }

    /**
     * Set the panose in binary form
     * @param panose the panose bytes
     * @throws UnsupportedOperationException can return UnsupportedOperationException when FontInfo is read-only
     */
    default void setPanose(byte[] panose) {
        throw new UnsupportedOperationException("FontInfo is read-only.");
    }


    /**
     * If font facets are embedded in the document, return the list of embedded facets.
     * The font embedding is experimental, therefore the API can change.
     * @return the list of embedded EOT font data
     */
    @Beta
    default List<? extends FontFacet> getFacets() {
        return Collections.emptyList();
    }
}