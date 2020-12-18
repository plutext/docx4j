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

package org.docx4j.fonts.fop.util;

/**
 * A helper class to create hex-encoded representations of numbers.
 */
public final class HexEncoder {

    private HexEncoder() { }

    /**
     * Returns an hex encoding of the given number as a string of the given length,
     * left-padded with zeros if necessary.
     *
     * @param n a number
     * @param width required length of the string
     * @return an hex-encoded representation of the number
     */
    public static String encode(int n, int width) {
        char[] digits = new char[width];
        for (int i = width - 1; i >= 0; i--) {
            int digit = n & 0xF;
            digits[i] = (char) (digit < 10 ? '0' + digit : 'A' + digit - 10);
            n >>= 4;
        }
        return new String(digits);
    }

    /**
     * Returns an hex encoding of the given character as:
     * <ul>
     *     <li>4-character string in case of non-BMP character</li>
     *     <li>6-character string in case of BMP character</li>
     * </ul>
     *
     * @param c a character
     * @return an hex-encoded representation of the character
     */
    public static String encode(int c) {
        if (CharUtilities.isBmpCodePoint(c)) {
            return encode(c, 4);
        } else {
            return encode(c, 6);
        }
    }
}
