/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.fonts.fop.fonts;

/**
 * The interface defines a 1-byte character encoding (with 256 characters).
 */
public interface SingleByteEncoding {

    /** Code point that is used if no code point for a specific character has been found. */
    char NOT_FOUND_CODE_POINT = '\0';

    /**
     * Returns the encoding's name.
     * @return the name of the encoding
     */
    String getName();

    /**
     * Maps a Unicode character to a code point in the encoding.
     * @param c the Unicode character to map
     * @return the code point in the encoding or 0 (=.notdef) if not found
     */
    char mapChar(char c);

    /**
     * Returns the array of character names for this encoding.
     * @return the array of character names
     *                  (unmapped code points are represented by a ".notdef" value)
     */
    String[] getCharNameMap();

    /**
     * Returns a character array with Unicode scalar values which can be used to map encoding
     * code points to Unicode values. Note that this does not return all possible Unicode values
     * that the encoding maps.
     * @return a character array with Unicode scalar values
     */
    char[] getUnicodeCharMap();

}
