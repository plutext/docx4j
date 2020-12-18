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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class provides utilities to distinguish various kinds of Unicode
 * whitespace and to get character widths in a given FontState.
 */
public class CharUtilities {

    /**
     * Character code used to signal a character boundary in
     * inline content, such as an inline with borders and padding
     * or a nested block object.
     */
    public static final char CODE_EOT = 0;

    /**
     * Character class: Unicode white space
     */
    public static final int UCWHITESPACE = 0;
    /**
     * Character class: Line feed
     */
    public static final int LINEFEED = 1;
    /**
     * Character class: Boundary between text runs
     */
    public static final int EOT = 2;
    /**
     * Character class: non-whitespace
     */
    public static final int NONWHITESPACE = 3;
    /**
     * Character class: XML whitespace
     */
    public static final int XMLWHITESPACE = 4;


    /** null char */
    public static final char NULL_CHAR = '\u0000';
    /** linefeed character */
    public static final char LINEFEED_CHAR = '\n';
    /** carriage return */
    public static final char CARRIAGE_RETURN = '\r';
    /** normal tab */
    public static final char TAB = '\t';
    /** normal space */
    public static final char SPACE = '\u0020';
    /** non-breaking space */
    public static final char NBSPACE = '\u00A0';
    /** next line control character */
    public static final char NEXT_LINE = '\u0085';
    /** zero-width space */
    public static final char ZERO_WIDTH_SPACE = '\u200B';
    /** word joiner */
    public static final char WORD_JOINER = '\u2060';
    /** zero-width joiner */
    public static final char ZERO_WIDTH_JOINER = '\u200D';
    /** left-to-right mark */
    public static final char LRM = '\u200E';
    /** right-to-left mark */
    public static final char RLM = '\u202F';
    /** left-to-right embedding */
    public static final char LRE = '\u202A';
    /** right-to-left embedding */
    public static final char RLE = '\u202B';
    /** pop directional formatting */
    public static final char PDF = '\u202C';
    /** left-to-right override */
    public static final char LRO = '\u202D';
    /** right-to-left override */
    public static final char RLO = '\u202E';
    /** zero-width no-break space (= byte order mark) */
    public static final char ZERO_WIDTH_NOBREAK_SPACE = '\uFEFF';
    /** soft hyphen */
    public static final char SOFT_HYPHEN = '\u00AD';
    /** line-separator */
    public static final char LINE_SEPARATOR = '\u2028';
    /** paragraph-separator */
    public static final char PARAGRAPH_SEPARATOR = '\u2029';
    /** missing ideograph */
    public static final char MISSING_IDEOGRAPH = '\u25A1';
    /** Ideogreaphic space */
    public static final char IDEOGRAPHIC_SPACE = '\u3000';
    /** Object replacement character */
    public static final char OBJECT_REPLACEMENT_CHARACTER = '\uFFFC';
    /** Unicode value indicating the the character is "not a character". */
    public static final char NOT_A_CHARACTER = '\uFFFF';

    /**
     * Utility class: Constructor prevents instantiating when subclassed.
     */
    protected CharUtilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the appropriate CharClass constant for the type
     * of the passed character.
     * @param c character to inspect
     * @return the determined character class
     */
    public static int classOf(int c) {
        switch (c) {
            case CODE_EOT:
                return EOT;
            case LINEFEED_CHAR:
                return LINEFEED;
            case SPACE:
            case CARRIAGE_RETURN:
            case TAB:
                return XMLWHITESPACE;
            default:
                return isAnySpace(c) ? UCWHITESPACE : NONWHITESPACE;
        }
    }


    /**
     * Helper method to determine if the character is a
     * space with normal behavior. Normal behavior means that
     * it's not non-breaking.
     * @param c character to inspect
     * @return True if the character is a normal space
     */
    public static boolean isBreakableSpace(int c) {
        return (c == SPACE || isFixedWidthSpace(c));
    }

    /**
     * Method to determine if the character is a zero-width space.
     * @param c the character to check
     * @return true if the character is a zero-width space
     */
    public static boolean isZeroWidthSpace(int c) {
        return c == ZERO_WIDTH_SPACE           // 200Bh
            || c == WORD_JOINER                // 2060h
            || c == ZERO_WIDTH_NOBREAK_SPACE;  // FEFFh (also used as BOM)
    }

    /**
     * Method to determine if the character is a (breakable) fixed-width space.
     * @param c the character to check
     * @return true if the character has a fixed-width
     */
    public static boolean isFixedWidthSpace(int c) {
        return (c >= '\u2000' && c <= '\u200B')
                || c == '\u3000';
//      c == '\u2000'                   // en quad
//      c == '\u2001'                   // em quad
//      c == '\u2002'                   // en space
//      c == '\u2003'                   // em space
//      c == '\u2004'                   // three-per-em space
//      c == '\u2005'                   // four-per-em space
//      c == '\u2006'                   // six-per-em space
//      c == '\u2007'                   // figure space
//      c == '\u2008'                   // punctuation space
//      c == '\u2009'                   // thin space
//      c == '\u200A'                   // hair space
//      c == '\u200B'                   // zero width space
//      c == '\u3000'                   // ideographic space
    }

    /**
     * Method to determine if the character is a nonbreaking
     * space.
     * @param c character to check
     * @return True if the character is a nbsp
     */
    public static boolean isNonBreakableSpace(int c) {
        return
            (c == NBSPACE       // no-break space
            || c == '\u202F'    // narrow no-break space
            || c == '\u3000'    // ideographic space
            || c == WORD_JOINER // word joiner
            || c == ZERO_WIDTH_NOBREAK_SPACE);  // zero width no-break space
    }

    /**
     * Method to determine if the character is an adjustable
     * space.
     * @param c character to check
     * @return True if the character is adjustable
     */
    public static boolean isAdjustableSpace(int c) {
        //TODO: are there other kinds of adjustable spaces?
        return
            (c == '\u0020'    // normal space
            || c == NBSPACE); // no-break space
    }

    /**
     * Determines if the character represents any kind of space.
     * @param c character to check
     * @return True if the character represents any kind of space
     */
    public static boolean isAnySpace(int c) {
        return (isBreakableSpace(c) || isNonBreakableSpace(c));
    }

    /**
     * Indicates whether a character is classified as "Alphabetic" by the Unicode standard.
     * @param c the character
     * @return true if the character is "Alphabetic"
     */
    public static boolean isAlphabetic(int c) {
        //http://www.unicode.org/Public/UNIDATA/UCD.html#Alphabetic
        //Generated from: Other_Alphabetic + Lu + Ll + Lt + Lm + Lo + Nl
        int generalCategory = Character.getType((char)c);
        switch (generalCategory) {
            case Character.UPPERCASE_LETTER: //Lu
            case Character.LOWERCASE_LETTER: //Ll
            case Character.TITLECASE_LETTER: //Lt
            case Character.MODIFIER_LETTER: //Lm
            case Character.OTHER_LETTER: //Lo
            case Character.LETTER_NUMBER: //Nl
                return true;
            default:
                //TODO if (ch in Other_Alphabetic) return true; (Probably need ICU4J for that)
                //Other_Alphabetic contains mostly more exotic characters
                return false;
        }
    }

    /**
     * Indicates whether the given character is an explicit break-character
     * @param c    the character to check
     * @return  true if the character represents an explicit break
     */
    public static boolean isExplicitBreak(int c) {
        return (c == LINEFEED_CHAR
            || c == CARRIAGE_RETURN
            || c == NEXT_LINE
            || c == LINE_SEPARATOR
            || c == PARAGRAPH_SEPARATOR);
    }

    /**
     * Convert a single unicode scalar value to an XML numeric character
     * reference. If in the BMP, four digits are used, otherwise 6 digits are used.
     * @param c a unicode scalar value
     * @return a string representing a numeric character reference
     */
    public static String charToNCRef(int c) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, nDigits = (c > 0xFFFF) ? 6 : 4; i < nDigits; i++, c >>= 4) {
            int d = c & 0xF;
            char hd;
            if (d < 10) {
                hd = (char) ((int) '0' + d);
            } else {
                hd = (char) ((int) 'A' + (d - 10));
            }
            sb.append(hd);
        }
        return "&#x" + sb.reverse() + ";";
    }

    /**
     * Convert a string to a sequence of ASCII or XML numeric character references.
     * @param s a java string (encoded in UTF-16)
     * @return a string representing a sequence of numeric character reference or
     * ASCII characters
     */
    public static String toNCRefs(String s) {
        StringBuffer sb = new StringBuffer();
        if (s != null) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if ((c >= 32) && (c < 127)) {
                    if (c == '<') {
                        sb.append("&lt;");
                    } else if (c == '>') {
                        sb.append("&gt;");
                    } else if (c == '&') {
                        sb.append("&amp;");
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(charToNCRef(c));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Pad a string S on left out to width W using padding character PAD.
     * @param s string to pad
     * @param width width of field to add padding
     * @param pad character to use for padding
     * @return padded string
     */
    public static String padLeft(String s, int width, char pad) {
        StringBuffer sb = new StringBuffer();
        for (int i = s.length(); i < width; i++) {
            sb.append(pad);
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Format character for debugging output, which it is prefixed with "0x", padded left with '0'
     * and either 4 or 6 hex characters in width according to whether it is in the BMP or not.
     * @param c character code
     * @return formatted character string
     */
    public static String format(int c) {
        if (c < 1114112) {
            return "0x" + padLeft(Integer.toString(c, 16), (c < 65536) ? 4 : 6, '0');
        } else {
            return "!NOT A CHARACTER!";
        }
    }

    /**
     * Determine if two character sequences contain the same characters.
     * @param cs1 first character sequence
     * @param cs2 second character sequence
     * @return true if both sequences have same length and same character sequence
     */
    public static boolean isSameSequence(CharSequence cs1, CharSequence cs2) {
        assert cs1 != null;
        assert cs2 != null;
        if (cs1.length() != cs2.length()) {
            return false;
        } else {
            for (int i = 0, n = cs1.length(); i < n; i++) {
                if (cs1.charAt(i) != cs2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Determine whether the specified character (Unicode code point) is in then Basic
     * Multilingual Plane (BMP). Such code points can be represented using a single {@code char}.
     *
     * @see Character#isBmpCodePoint(int) from Java 1.7
     * @param  codePoint the character (Unicode code point) to be tested
     * @return {@code true} if the specified code point is between  Character#MIN_VALUE and
     *          Character#MAX_VALUE} inclusive; {@code false} otherwise
     */
    public static boolean isBmpCodePoint(int codePoint) {
        return codePoint >>> 16 == 0;
    }

    /**
     * Returns 1 if codePoint not in the BMP. This function is particularly useful in for
     * loops over strings where, in presence of surrogate pairs, you need to skip one loop.
     *
     * @param codePoint 1 if codePoint &gt; 0xFFFF, 0 otherwise
     * @return 1 if codePoint &gt; 0xFFFF, 0 otherwise
     */
    public static int incrementIfNonBMP(int codePoint) {
        return isBmpCodePoint(codePoint) ? 0 : 1;
    }

    /**
     * Determine if the given characters is part of a surrogate pair.
     *
     * @param ch character to be checked
     * @return true if ch is an high surrogate or a low surrogate
     */
    public static boolean isSurrogatePair(char ch) {
        return Character.isHighSurrogate(ch) || Character.isLowSurrogate(ch);
    }

    /**
     * Tells whether there is a surrogate pair starting from the given index in the {@link CharSequence}. If the
     * character at index is an high surrogate then the character at index+1 is checked to be a low surrogate. If a
     * malformed surrogate pair is encountered then an {@link IllegalArgumentException} is thrown.
     * <pre>
     * high surrogate [0xD800 - 0xDC00]
     * low surrogate [0xDC00 - 0xE000]
     * </pre>
     *
     * @param chars CharSequence to check
     * @param index index in the CharSequqnce where to start the check
     * @throws IllegalArgumentException if there wrong usage of surrogate pairs
     * @return true if there is a well-formed surrogate pair at index
     */
    public static boolean containsSurrogatePairAt(CharSequence chars, int index) {
        char ch = chars.charAt(index);

        if (Character.isHighSurrogate(ch)) {
            if ((index + 1) > chars.length()) {
                throw new IllegalArgumentException(
                        "ill-formed UTF-16 sequence, contains isolated high surrogate at end of sequence");
            }

            if (Character.isLowSurrogate(chars.charAt(index + 1))) {
                return true;
            }

            throw new IllegalArgumentException(
                    "ill-formed UTF-16 sequence, contains isolated high surrogate at index " + index);

        } else if (Character.isLowSurrogate(ch)) {
            throw new IllegalArgumentException(
                    "ill-formed UTF-16 sequence, contains isolated low surrogate at index " + index);
        }

        return false;
    }

    /**
     * Creates an iterator to iter a {@link CharSequence} codepoints.
     *
     * @see #codepointsIter(CharSequence, int, int)
     * @param s {@link CharSequence} to iter
     * @return codepoint iterator for the given {@link CharSequence}.
     */
    public static Iterable<Integer> codepointsIter(final CharSequence s) {
        return codepointsIter(s, 0, s.length());
    }

    /**
     * Creates an iterator to iter a sub-CharSequence codepoints.
     *
     * @see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=5003547">Bug JDK-5003547</a>
     * @param s {@link CharSequence} to iter
     * @param beginIndex lower range
     * @param endIndex upper range
     * @return codepoint iterator for the given sub-CharSequence.
     */
    public static Iterable<Integer> codepointsIter(final CharSequence s, final int beginIndex, final int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > s.length()) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }

        return new Iterable<Integer>() {
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    int nextIndex = beginIndex;

                    public boolean hasNext() {
                        return nextIndex < endIndex;
                    }

                    public Integer next() {
                        if (!hasNext()) {
                            // Findbugs wants this: IT_NO_SUCH_ELEMENT
                            throw new NoSuchElementException();
                        }
                        int result = Character.codePointAt(s, nextIndex);
                        nextIndex += Character.charCount(result);
                        return result;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
