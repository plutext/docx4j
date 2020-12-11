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

/* $Id: MutableFont.java 721430 2008-11-28 11:13:12Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;

import java.util.Map;
import java.util.Set;



/**
 * This interface is used to set the values of a font during configuration time.
 */
public interface MutableFont {

    /**
     * Sets the "PostScript" font name (Example: "Helvetica-BoldOblique").
     * @param name font name
     */
    void setFontName(String name);

    /**
     * Sets the font's full name (usually the one that the operating system displays). Example:
     * "Helvetica Bold Oblique".
     * @param name font' full name
     */
    void setFullName(String name);

    /**
     * Sets the font's family names (Example: "Helvetica").
     * @param names the font's family names (a Set of Strings)
     */
    void setFamilyNames(Set names);

    /**
     * Sets the path to the embeddable font file.
     * @param path URI to the file
     */
    void setEmbedFileName(String path);

    /**
     * Sets the resource name of the embeddable font file.
     * @param name resource name
     */
    void setEmbedResourceName(String name);

    /**
     * Sets the capital height value.
     * @param capHeight capital height
     */
    void setCapHeight(int capHeight);

    /**
     * Sets the ascent value.
     * @param ascender ascent height
     */
    void setAscender(int ascender);

    /**
     * Sets the descent value.
     * @param descender descent value
     */
    void setDescender(int descender);

    /**
     * Sets the font's bounding box
     * @param bbox bounding box
     */
    void setFontBBox(int[] bbox);

    /**
     * Sets the font's flags
     * @param flags flags
     */
    void setFlags(int flags);

    /**
     * Sets the font's StemV value.
     * @param stemV StemV
     */
    void setStemV(int stemV);

    /**
     * Sets the font's italic angle.
     * @param italicAngle italic angle
     */
    void setItalicAngle(int italicAngle);

    /**
     * Sets the font's default width
     * @param width default width
     */
    void setMissingWidth(int width);

    /**
     * Sets the font type.
     * @param fontType font type
     */
    void setFontType(FontType fontType);

    /**
     * Sets the index of the first character in the character table.
     * @param index index of first character
     */
    void setFirstChar(int index);

    /**
     * Sets the index of the last character in the character table.
     * @param index index of the last character
     */
    void setLastChar(int index);

    /**
     * Enables/disabled kerning.
     * @param enabled True if kerning should be enabled if available
     */
    void setKerningEnabled(boolean enabled);

    /**
     * Adds an entry to the kerning table.
     * @param key Kerning key
     * @param value Kerning value
     */
    void putKerningEntry(Integer key, Map value);

}
