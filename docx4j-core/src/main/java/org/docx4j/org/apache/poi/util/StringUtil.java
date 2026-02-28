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

package org.docx4j.org.apache.poi.util;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * Collection of string handling utilities
 */
@Internal
public final class StringUtil {
    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 10000000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    public static final Charset UTF16LE = StandardCharsets.UTF_16LE;
    public static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final Charset WIN_1252 = Charset.forName("cp1252");

    /**
     * @param length the max record length allowed for StringUtil
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for StringUtil
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    private StringUtil() {
        // no instances of this class
    }

    /**
     * Given a byte array of 16-bit unicode characters in Little Endian
     * format (most important byte last), return a Java String representation
     * of it.
     * <p>
     * { 0x16, 0x00 } -0x16
     *
     * @param string the byte array to be converted
     * @param offset the initial offset into the
     *               byte array. it is assumed that string[ offset ] and string[ offset +
     *               1 ] contain the first 16-bit unicode character
     * @param len    the length of the final string
     * @return the converted string, never {@code null}.
     * @throws ArrayIndexOutOfBoundsException if offset is out of bounds for
     *                                        the byte array (i.e., is negative or is greater than or equal to
     *                                        string.length)
     * @throws IllegalArgumentException       if len is too large (i.e.,
     *                                        there is not enough data in string to create a String of that
     *                                        length)
     */
    public static String getFromUnicodeLE(
            final byte[] string,
            final int offset,
            final int len)
            throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (len == 0) {
            return "";
        }
        if ((offset < 0) || (offset >= string.length)) {
            throw new ArrayIndexOutOfBoundsException("Illegal offset " + offset + " (String data is of length " + string.length + ")");
        }
        if ((len < 0) || (((string.length - offset) / 2) < len)) {
            throw new IllegalArgumentException("Illegal length " + len);
        }

        return new String(string, offset, len * 2, UTF16LE);
    }

    /**
     * Given a byte array of 16-bit unicode characters in little endian
     * format (most important byte last), return a Java String representation
     * of it.
     * <p>
     * { 0x16, 0x00 } -0x16
     *
     * @param string the byte array to be converted
     * @return the converted string, never {@code null}
     */
    public static String getFromUnicodeLE(byte[] string) {
        if (string.length == 0) {
            return "";
        }
        return getFromUnicodeLE(string, 0, string.length / 2);
    }

    /**
     * Convert String to 16-bit unicode characters in little endian format
     *
     * @param string the string
     * @return the byte array of 16-bit unicode characters
     */
    public static byte[] getToUnicodeLE(String string) {
        return string.getBytes(UTF16LE);
    }

    /**
     * Read 8 bit data (in ISO-8859-1 codepage) into a (unicode) Java
     * String and return.
     * (In Excel terms, read compressed 8 bit unicode as a string)
     *
     * @param string byte array to read
     * @param offset offset to read byte array
     * @param len    length to read byte array
     * @return String generated String instance by reading byte array (ISO-8859-1)
     */
    public static String getFromCompressedUnicode(
            final byte[] string,
            final int offset,
            final int len) {
        int len_to_use = Math.min(len, string.length - offset);
        return new String(string, offset, len_to_use, ISO_8859_1);
    }

    /**
     * Read 8 bit data (in UTF-8 codepage) into a (unicode) Java
     * String and return.
     * (In Excel terms, read compressed 8 bit unicode as a string)
     *
     * @param string byte array to read
     * @param offset offset to read byte array
     * @param len    length to read byte array
     * @return String generated String instance by reading byte array (UTF-8)
     */
    public static String getFromCompressedUTF8(
            final byte[] string,
            final int offset,
            final int len) {
        int len_to_use = Math.min(len, string.length - offset);
        return new String(string, offset, len_to_use, UTF_8);
    }

    /**
     * @param in stream,
     * @param nChars number pf chars
     * @return ISO_8859_1 encoded result
     */
    public static String readCompressedUnicode(LittleEndianInput in, int nChars) {
        byte[] buf = IOUtils.safelyAllocate(nChars, MAX_RECORD_LENGTH);
        in.readFully(buf);
        return new String(buf, ISO_8859_1);
    }

    /**
     * InputStream {@code in} is expected to contain:
     * <ol>
     * <li>ushort nChars</li>
     * <li>byte is16BitFlag</li>
     * <li>byte[]/char[] characterData</li>
     * </ol>
     * For this encoding, the is16BitFlag is always present even if nChars==0.
     * <p>
     * This structure is also known as a XLUnicodeString.
     */
    public static String readUnicodeString(LittleEndianInput in) {

        int nChars = in.readUShort();
        byte flag = in.readByte();
        if ((flag & 0x01) == 0) {
            return readCompressedUnicode(in, nChars);
        }
        return readUnicodeLE(in, nChars);
    }

    /**
     * InputStream {@code in} is expected to contain:
     * <ol>
     * <li>byte is16BitFlag</li>
     * <li>byte[]/char[] characterData</li>
     * </ol>
     * For this encoding, the is16BitFlag is always present even if nChars==0.
     * <br>
     * This method should be used when the nChars field is <em>not</em> stored
     * as a ushort immediately before the is16BitFlag. Otherwise, {@link
     * #readUnicodeString(LittleEndianInput)} can be used.
     */
    public static String readUnicodeString(LittleEndianInput in, int nChars) {
        byte is16Bit = in.readByte();
        if ((is16Bit & 0x01) == 0) {
            return readCompressedUnicode(in, nChars);
        }
        return readUnicodeLE(in, nChars);
    }

    /**
     * OutputStream {@code out} will get:
     * <ol>
     * <li>ushort nChars</li>
     * <li>byte is16BitFlag</li>
     * <li>byte[]/char[] characterData</li>
     * </ol>
     * For this encoding, the is16BitFlag is always present even if nChars==0.
     */
    public static void writeUnicodeString(LittleEndianOutput out, String value) {
        int nChars = value.length();
        out.writeShort(nChars);
        boolean is16Bit = hasMultibyte(value);
        out.writeByte(is16Bit ? 0x01 : 0x00);
        if (is16Bit) {
            putUnicodeLE(value, out);
        } else {
            putCompressedUnicode(value, out);
        }
    }

    /**
     * OutputStream {@code out} will get:
     * <ol>
     * <li>byte is16BitFlag</li>
     * <li>byte[]/char[] characterData</li>
     * </ol>
     * For this encoding, the is16BitFlag is always present even if nChars==0.
     * <br>
     * This method should be used when the nChars field is <em>not</em> stored
     * as a ushort immediately before the is16BitFlag. Otherwise, {@link
     * #writeUnicodeString(LittleEndianOutput, String)} can be used.
     */
    public static void writeUnicodeStringFlagAndData(LittleEndianOutput out, String value) {
        boolean is16Bit = hasMultibyte(value);
        out.writeByte(is16Bit ? 0x01 : 0x00);
        if (is16Bit) {
            putUnicodeLE(value, out);
        } else {
            putCompressedUnicode(value, out);
        }
    }

    /**
     * @return the number of bytes that would be written by {@link #writeUnicodeString(LittleEndianOutput, String)}
     */
    public static int getEncodedSize(String value) {
        int result = 2 + 1;
        result += value.length() * (StringUtil.hasMultibyte(value) ? 2 : 1);
        return result;
    }

    /**
     * Takes a unicode (java) string, and returns it as 8 bit data (in ISO-8859-1
     * codepage).
     * (In Excel terms, write compressed 8 bit unicode)
     *
     * @param input  the String containing the data to be written
     * @param output the byte array to which the data is to be written
     * @param offset an offset into the byte arrat at which the data is start
     *               when written
     */
    public static void putCompressedUnicode(String input, byte[] output, int offset) {
        byte[] bytes = input.getBytes(ISO_8859_1);
        System.arraycopy(bytes, 0, output, offset, bytes.length);
    }

    public static void putCompressedUnicode(String input, LittleEndianOutput out) {
        byte[] bytes = input.getBytes(ISO_8859_1);
        out.write(bytes);
    }

    /**
     * Takes a unicode string, and returns it as little endian (most
     * important byte last) bytes in the supplied byte array.
     * (In Excel terms, write uncompressed unicode)
     *
     * @param input  the String containing the unicode data to be written
     * @param output the byte array to hold the uncompressed unicode, should be twice the length of the String
     * @param offset the offset to start writing into the byte array
     */
    public static void putUnicodeLE(String input, byte[] output, int offset) {
        byte[] bytes = input.getBytes(UTF16LE);
        System.arraycopy(bytes, 0, output, offset, bytes.length);
    }

    public static void putUnicodeLE(String input, LittleEndianOutput out) {
        byte[] bytes = input.getBytes(UTF16LE);
        out.write(bytes);
    }

    public static String readUnicodeLE(LittleEndianInput in, int nChars) {
        byte[] bytes = IOUtils.safelyAllocate(nChars * 2L, MAX_RECORD_LENGTH);
        in.readFully(bytes);
        return new String(bytes, UTF16LE);
    }

    /**
     * @return the encoding we want to use, currently hardcoded to ISO-8859-1
     */
    public static String getPreferredEncoding() {
        return ISO_8859_1.name();
    }

    /**
     * check the parameter has multibyte character
     *
     * @param value string to check
     * @return boolean result true:string has at least one multibyte character
     */
    public static boolean hasMultibyte(String value) {
        if (value == null) {
            return false;
        }
        for (char c : value.toCharArray()) {
            if (c > 0xFF) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if the string starts with the specified prefix, ignoring case consideration.
     */
    public static boolean startsWithIgnoreCase(String haystack, String prefix) {
        return haystack.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Tests if the string ends with the specified suffix, ignoring case consideration.
     */
    public static boolean endsWithIgnoreCase(String haystack, String suffix) {
        int length = suffix.length();
        int start = haystack.length() - length;
        return haystack.regionMatches(true, start, suffix, 0, length);
    }

    @Internal
    public static String toLowerCase(char c) {
        return Character.toString(c).toLowerCase(Locale.ROOT);
    }

    @Internal
    public static String toUpperCase(char c) {
        return Character.toString(c).toUpperCase(Locale.ROOT);
    }

    @Internal
    public static boolean isUpperCase(char c) {
        String s = Character.toString(c);
        return s.toUpperCase(Locale.ROOT).equals(s);
    }

    /**
     * Some strings may contain encoded characters of the unicode private use area.
     * Currently the characters of the symbol fonts are mapped to the corresponding
     * characters in the normal unicode range.
     *
     * @param string the original string
     * @return the string with mapped characters
     * @see <a href="http://www.alanwood.net/unicode/private_use_area.html#symbol">Private Use Area (symbol)</a>
     * @see <a href="http://www.alanwood.net/demos/symbol.html">Symbol font - Unicode alternatives for Greek and special characters in HTML</a>
     */
    public static String mapMsCodepointString(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        int[] cps = string.codePoints().map(StringUtil::mapMsCodepoint).toArray();
        return new String(cps, 0, cps.length);
    }

    private static int mapMsCodepoint(int cp) {
        if (0xf020 <= cp && cp <= 0xf07f) {
            return symbolMap_f020[cp - 0xf020];
        } else if (0xf0a0 <= cp && cp <= 0xf0ff) {
            return symbolMap_f0a0[cp - 0xf0a0];
        }
        return cp;
    }

    private static final int[] symbolMap_f020 = {
            ' ', // 0xf020 space
            '!', // 0xf021 exclam
            8704, // 0xf022 universal
            '#', // 0xf023 numbersign
            8707, // 0xf024 existential
            '%', // 0xf025 percent
            '&', // 0xf026 ampersand
            8717, // 0xf027 suchthat
            '(', // 0xf028 parenleft
            ')', // 0xf029 parentright
            8727, // 0xf02a asteriskmath
            '+', // 0xf02b plus
            ',', // 0xf02c comma
            8722, // 0xf02d minus sign (long -)
            '.', // 0xf02e period
            '/', // 0xf02f slash
            '0', // 0xf030 0
            '1', // 0xf031 1
            '2', // 0xf032 2
            '3', // 0xf033 3
            '4', // 0xf034 4
            '5', // 0xf035 5
            '6', // 0xf036 6
            '7', // 0xf037 7
            '8', // 0xf038 8
            '9', // 0xf039 9
            ':', // 0xf03a colon
            ';', // 0xf03b semicolon
            '<', // 0xf03c less
            '=', // 0xf03d equal
            '>', // 0xf03e greater
            '?', // 0xf03f question
            8773, // 0xf040 congruent
            913, // 0xf041 alpha (upper)
            914, // 0xf042 beta (upper)
            935, // 0xf043 chi (upper)
            916, // 0xf044 delta (upper)
            917, // 0xf045 epsilon (upper)
            934, // 0xf046 phi (upper)
            915, // 0xf047 gamma (upper)
            919, // 0xf048 eta (upper)
            921, // 0xf049 iota (upper)
            977, // 0xf04a theta1 (lower)
            922, // 0xf04b kappa (upper)
            923, // 0xf04c lambda (upper)
            924, // 0xf04d mu (upper)
            925, // 0xf04e nu (upper)
            927, // 0xf04f omicron (upper)
            928, // 0xf050 pi (upper)
            920, // 0xf051 theta (upper)
            929, // 0xf052 rho (upper)
            931, // 0xf053 sigma (upper)
            932, // 0xf054 tau (upper)
            933, // 0xf055 upsilon (upper)
            962, // 0xf056 simga1 (lower)
            937, // 0xf057 omega (upper)
            926, // 0xf058 xi (upper)
            936, // 0xf059 psi (upper)
            918, // 0xf05a zeta (upper)
            '[', // 0xf05b bracketleft
            8765, // 0xf05c therefore
            ']', // 0xf05d bracketright
            8869, // 0xf05e perpendicular
            '_', // 0xf05f underscore
            ' ', // 0xf060 radicalex (doesn't exist in unicode)
            945, // 0xf061 alpha (lower)
            946, // 0xf062 beta (lower)
            967, // 0xf063 chi (lower)
            948, // 0xf064 delta (lower)
            949, // 0xf065 epsilon (lower)
            966, // 0xf066 phi (lower)
            947, // 0xf067 gamma (lower)
            951, // 0xf068 eta (lower)
            953, // 0xf069 iota (lower)
            981, // 0xf06a phi1 (lower)
            954, // 0xf06b kappa (lower)
            955, // 0xf06c lambda (lower)
            956, // 0xf06d mu (lower)
            957, // 0xf06e nu (lower)
            959, // 0xf06f omnicron (lower)
            960, // 0xf070 pi (lower)
            952, // 0xf071 theta (lower)
            961, // 0xf072 rho (lower)
            963, // 0xf073 sigma (lower)
            964, // 0xf074 tau (lower)
            965, // 0xf075 upsilon (lower)
            982, // 0xf076 piv (lower)
            969, // 0xf077 omega (lower)
            958, // 0xf078 xi (lower)
            968, // 0xf079 psi (lower)
            950, // 0xf07a zeta (lower)
            '{', // 0xf07b braceleft
            '|', // 0xf07c bar
            '}', // 0xf07d braceright
            8764, // 0xf07e similar '~'
            ' ', // 0xf07f not defined
    };

    private static final int[] symbolMap_f0a0 = {
            8364, // 0xf0a0 not defined / euro symbol
            978, // 0xf0a1 upsilon1 (upper)
            8242, // 0xf0a2 minute
            8804, // 0xf0a3 lessequal
            8260, // 0xf0a4 fraction
            8734, // 0xf0a5 infinity
            402, // 0xf0a6 florin
            9827, // 0xf0a7 club
            9830, // 0xf0a8 diamond
            9829, // 0xf0a9 heart
            9824, // 0xf0aa spade
            8596, // 0xf0ab arrowboth
            8591, // 0xf0ac arrowleft
            8593, // 0xf0ad arrowup
            8594, // 0xf0ae arrowright
            8595, // 0xf0af arrowdown
            176, // 0xf0b0 degree
            177, // 0xf0b1 plusminus
            8243, // 0xf0b2 second
            8805, // 0xf0b3 greaterequal
            215, // 0xf0b4 multiply
            181, // 0xf0b5 proportional
            8706, // 0xf0b6 partialdiff
            8729, // 0xf0b7 bullet
            247, // 0xf0b8 divide
            8800, // 0xf0b9 notequal
            8801, // 0xf0ba equivalence
            8776, // 0xf0bb approxequal
            8230, // 0xf0bc ellipsis
            9168, // 0xf0bd arrowvertex
            9135, // 0xf0be arrowhorizex
            8629, // 0xf0bf carriagereturn
            8501, // 0xf0c0 aleph
            8475, // 0xf0c1 Ifraktur
            8476, // 0xf0c2 Rfraktur
            8472, // 0xf0c3 weierstrass
            8855, // 0xf0c4 circlemultiply
            8853, // 0xf0c5 circleplus
            8709, // 0xf0c6 emptyset
            8745, // 0xf0c7 intersection
            8746, // 0xf0c8 union
            8835, // 0xf0c9 propersuperset
            8839, // 0xf0ca reflexsuperset
            8836, // 0xf0cb notsubset
            8834, // 0xf0cc propersubset
            8838, // 0xf0cd reflexsubset
            8712, // 0xf0ce element
            8713, // 0xf0cf notelement
            8736, // 0xf0d0 angle
            8711, // 0xf0d1 gradient
            174, // 0xf0d2 registerserif
            169, // 0xf0d3 copyrightserif
            8482, // 0xf0d4 trademarkserif
            8719, // 0xf0d5 product
            8730, // 0xf0d6 radical
            8901, // 0xf0d7 dotmath
            172, // 0xf0d8 logicalnot
            8743, // 0xf0d9 logicaland
            8744, // 0xf0da logicalor
            8660, // 0xf0db arrowdblboth
            8656, // 0xf0dc arrowdblleft
            8657, // 0xf0dd arrowdblup
            8658, // 0xf0de arrowdblright
            8659, // 0xf0df arrowdbldown
            9674, // 0xf0e0 lozenge
            9001, // 0xf0e1 angleleft
            174, // 0xf0e2 registersans
            169, // 0xf0e3 copyrightsans
            8482, // 0xf0e4 trademarksans
            8721, // 0xf0e5 summation
            9115, // 0xf0e6 parenlefttp
            9116, // 0xf0e7 parenleftex
            9117, // 0xf0e8 parenleftbt
            9121, // 0xf0e9 bracketlefttp
            9122, // 0xf0ea bracketleftex
            9123, // 0xf0eb bracketleftbt
            9127, // 0xf0ec bracelefttp
            9128, // 0xf0ed braceleftmid
            9129, // 0xf0ee braceleftbt
            9130, // 0xf0ef braceex
            ' ', // 0xf0f0 not defined
            9002, // 0xf0f1 angleright
            8747, // 0xf0f2 integral
            8992, // 0xf0f3 integraltp
            9134, // 0xf0f4 integralex
            8993, // 0xf0f5 integralbt
            9118, // 0xf0f6 parenrighttp
            9119, // 0xf0f7 parenrightex
            9120, // 0xf0f8 parenrightbt
            9124, // 0xf0f9 bracketrighttp
            9125, // 0xf0fa bracketrightex
            9126, // 0xf0fb bracketrightbt
            9131, // 0xf0fc bracerighttp
            9132, // 0xf0fd bracerightmid
            9133, // 0xf0fe bracerightbt
            ' ', // 0xf0ff not defined
    };


    // Could be replaced with org.apache.commons.lang3.StringUtils#join
    @Internal
    public static String join(Object[] array, String separator) {
        if (array == null || array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    @Internal
    public static String join(Object[] array) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : array) {
            sb.append(o);
        }
        return sb.toString();
    }

    @Internal
    public static String join(String separator, Object... array) {
        return join(array, separator);
    }

    /**
     * Count number of occurrences of needle in haystack
     * Has same signature as org.apache.commons.lang3.StringUtils#countMatches
     *
     * @param haystack the CharSequence to check, may be null
     * @param needle   the character to count the quantity of
     * @return the number of occurrences, 0 if the CharSequence is null
     */
    public static int countMatches(CharSequence haystack, char needle) {
        if (haystack == null) {
            return 0;
        }
        int count = 0;
        final int length = haystack.length();
        for (int i = 0; i < length; i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }


    /**
     * Given a byte array of 16-bit unicode characters in Little Endian
     * format (most important byte last), return a Java String representation
     * of it.
     *
     * Scans the byte array for two continous 0 bytes and returns the string before.
     * <p>
     *
     * #61881: there seem to be programs out there, which write the 0-termination also
     * at the beginning of the string. Check if the next two bytes contain a valid ascii char
     * and correct the _recdata with a '?' char
     *
     *
     * @param string the byte array to be converted
     * @param offset the initial offset into the
     *               byte array. it is assumed that string[ offset ] and string[ offset +
     *               1 ] contain the first 16-bit unicode character
     * @param len    the max. length of the final string
     * @return the converted string, never {@code null}.
     * @throws ArrayIndexOutOfBoundsException if offset is out of bounds for
     *                                        the byte array (i.e., is negative or is greater than or equal to
     *                                        string.length)
     * @throws IllegalArgumentException       if len is too large (i.e.,
     *                                        there is not enough data in string to create a String of that
     *                                        length)
     */
    public static String getFromUnicodeLE0Terminated(
            final byte[] string,
            final int offset,
            final int len)
            throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if ((offset < 0) || (offset >= string.length)) {
            throw new ArrayIndexOutOfBoundsException("Illegal offset " + offset + " (String data is of length " + string.length + ")");
        }

        if ((len < 0) || (((string.length - offset) / 2) < len)) {
            throw new IllegalArgumentException("Illegal length " + len);
        }

        final int newOffset;
        final int newMaxLen;
        final String prefix;

        // #61881 - for now we only check the first char
        if (len > 0 && offset < (string.length - 1) && string[offset] == 0 && string[offset+1] == 0) {
            newOffset = offset+2;
            prefix = "?";

            // check if the next char is garbage and limit the len if necessary
            final int cp = (len > 1) ? LittleEndian.getShort(string, offset+2) : 0;
            newMaxLen = Character.isJavaIdentifierPart(cp) ? len-1 : 0;
        } else {
            newOffset = offset;
            prefix = "";
            newMaxLen = len;
        }

        int newLen = 0;

        // loop until we find a null-terminated end
        for(; newLen < newMaxLen; newLen++) {
            if (string[newOffset + newLen * 2] == 0 && string[newOffset + newLen * 2 + 1] == 0) {
                break;
            }
        }
        newLen = Math.min(newLen, newMaxLen);

        return prefix + ((newLen == 0) ? "" : new String(string, newOffset, newLen * 2, UTF16LE));
    }


    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     *
     * copied from commons-lang3
     *
     * @param cs
     *            a CharSequence or {@code null}
     * @return CharSequence length or {@code 0} if the CharSequence is
     *         {@code null}.
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtil.isBlank(null)      = true
     * StringUtil.isBlank("")        = true
     * StringUtil.isBlank(" ")       = true
     * StringUtil.isBlank("bob")     = false
     * StringUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * copied from commons-lang3
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtil.isNotBlank(null)      = false
     * StringUtil.isNotBlank("")        = false
     * StringUtil.isNotBlank(" ")       = false
     * StringUtil.isNotBlank("bob")     = true
     * StringUtil.isNotBlank("  bob  ") = true
     * </pre>
     *
     * copied from commons-lang3
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *  not empty and not null and not whitespace only
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtil.repeat('e', 0)  = ""
     * StringUtil.repeat('e', 3)  = "eee"
     * StringUtil.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method does not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * </p>
     *
     * copied from commons-lang3
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }

}
