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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * dump data in hexadecimal format
 */
@Internal
public final class HexDump {
    public static final String EOL = System.getProperty("line.separator");
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private HexDump() {
        // all static methods, so no need for a public constructor
    }

    /**
     * dump an array of bytes to an OutputStream
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param stream the OutputStream to which the data is to be
     *               written
     * @param index initial index into the byte array
     * @param length number of characters to output
     *
     * @throws IOException is thrown if anything goes wrong writing
     *            the data to stream
     * @throws ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @throws IllegalArgumentException if the output stream is
     *            null
     */
    public static void dump(final byte [] data, final long offset,
        final OutputStream stream, final int index, final int length)
    throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (stream == null) {
            throw new IllegalArgumentException("cannot write to nullstream");
        }

        OutputStreamWriter osw = new OutputStreamWriter(stream, UTF8);
        osw.write(dump(data, offset, index, length));
        osw.flush();
    }

    /**
     * dump an array of bytes to an OutputStream
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param stream the OutputStream to which the data is to be
     *               written
     * @param index initial index into the byte array
     *
     * @throws IOException is thrown if anything goes wrong writing
     *            the data to stream
     * @throws ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @throws IllegalArgumentException if the output stream is
     *            null
     */

    public synchronized static void dump(final byte [] data, final long offset,
            final OutputStream stream, final int index)
    throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException {
        dump(data, offset, stream, index, Integer.MAX_VALUE);
    }

    /**
     * dump an array of bytes to a String
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param index initial index into the byte array
     *
     * @throws ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @return output string
     */

    public static String dump(final byte [] data, final long offset, final int index) {
        return dump(data, offset, index, Integer.MAX_VALUE);
    }

    /**
     * dump an array of bytes to a String
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param index initial index into the byte array
     * @param length number of characters to output
     *
     * @throws ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @return output string
     */

    public static String dump(final byte [] data, final long offset, final int index, final int length) {
        if (data == null || data.length == 0) {
            return "No Data"+EOL;
        }

        int data_length = (length == Integer.MAX_VALUE || length < 0 || index+length < 0)
            ? data.length
            : Math.min(data.length,index+length);


        if ((index < 0) || (index >= data.length)) {
            String err = "illegal index: "+index+" into array of length "+data.length;
            throw new ArrayIndexOutOfBoundsException(err);
        }

        long  display_offset = offset + index;
        StringBuilder buffer = new StringBuilder(74);

        for (int j = index; j < data_length; j += 16) {
            int chars_read = data_length - j;

            if (chars_read > 16) {
                chars_read = 16;
            }

            writeHex(buffer, display_offset, 8, "");
            for (int k = 0; k < 16; k++) {
                if (k < chars_read) {
                    writeHex(buffer, data[ k + j ], 2, " ");
                } else {
                    buffer.append("   ");
                }
            }
            buffer.append(' ');
            for (int k = 0; k < chars_read; k++) {
                buffer.append(toAscii(data[ k + j ]));
            }
            buffer.append(EOL);
            display_offset += chars_read;
        }
        return buffer.toString();
    }

    public static char toAscii(int dataB) {
        char charB = (char)(dataB & 0xFF);
        if (Character.isISOControl(charB)) {
            return '.';
        }

        switch (charB) {
            // printable, but not compilable with current compiler encoding
            case 0xFF:
            case 0xDD:
                charB = '.';
                break;
            default:
                break;
        }
        return charB;
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          A String representing the array of bytes
     */
    public static String toHex(final byte[] value)
    {
        StringBuilder retVal = new StringBuilder();
        retVal.append('[');
        if (value != null && value.length > 0)
        {
            for(int x = 0; x < value.length; x++)
            {
                if (x>0) {
                    retVal.append(", ");
                }
                retVal.append(toHex(value[x]));
            }
        }
        retVal.append(']');
        return retVal.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(short value) {
        StringBuilder sb = new StringBuilder(4);
        writeHex(sb, value & 0xFFFF, 4, "");
        return sb.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(byte value) {
        StringBuilder sb = new StringBuilder(2);
        writeHex(sb, value & 0xFF, 2, "");
        return sb.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(int value) {
        StringBuilder sb = new StringBuilder(8);
        writeHex(sb, value & 0xFFFFFFFFL, 8, "");
        return sb.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(long value) {
        StringBuilder sb = new StringBuilder(16);
        writeHex(sb, value, 16, "");
        return sb.toString();
    }

    /**
     * @return string of 16 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static String longToHex(long value) {
        StringBuilder sb = new StringBuilder(18);
        writeHex(sb, value, 16, "0x");
        return sb.toString();
    }

    /**
     * @return string of 8 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static String intToHex(int value) {
        StringBuilder sb = new StringBuilder(10);
        writeHex(sb, value & 0xFFFFFFFFL, 8, "0x");
        return sb.toString();
    }

    /**
     * @return string of 4 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static String shortToHex(int value) {
        StringBuilder sb = new StringBuilder(6);
        writeHex(sb, value & 0xFFFFL, 4, "0x");
        return sb.toString();
    }

    /**
     * @return string of 2 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static String byteToHex(int value) {
        StringBuilder sb = new StringBuilder(4);
        writeHex(sb, value & 0xFFL, 2, "0x");
        return sb.toString();
    }

    /**
     * @see Integer#toHexString(int)
     * @see Long#toHexString(long)
     */
    private static void writeHex(StringBuilder sb, long value, int nDigits, String prefix) {
        sb.append(prefix);
        char[] buf = new char[nDigits];
        long acc = value;
        for(int i=nDigits-1; i>=0; i--) {
            int digit = (int) (acc & 0x0F);
            buf[i] = (char) (digit < 10 ? ('0' + digit) : ('A' + digit - 10));
            acc >>>= 4;
        }
        sb.append(buf);
    }
}
