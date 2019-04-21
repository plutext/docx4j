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

package org.docx4j.org.apache.poi.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * dump data in hexadecimal format; derived from a HexDump utility I
 * wrote in June 2001.
 *
 * @author Marc Johnson
 * @author Glen Stampoultzis  (glens at apache.org)
 */
public class HexDump {
    public static final String EOL = System.getProperty("line.separator");
    private static final char _hexcodes[] = "0123456789ABCDEF".toCharArray();
    private static final int _shifts[]   =
    {
        60, 56, 52, 48, 44, 40, 36, 32, 28, 24, 20, 16, 12, 8, 4, 0
    };

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
     * @exception IOException is thrown if anything goes wrong writing
     *            the data to stream
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @exception IllegalArgumentException if the output stream is
     *            null
     */
    public static void dump(final byte [] data, final long offset,
                            final OutputStream stream, final int index, final int length)
            throws IOException, ArrayIndexOutOfBoundsException,
                    IllegalArgumentException
    {
        if (data.length == 0)
        {
            stream.write( ("No Data" + EOL).getBytes() );
            stream.flush();
            return;
        }
        if ((index < 0) || (index >= data.length))
        {
            throw new ArrayIndexOutOfBoundsException(
                "illegal index: " + index + " into array of length "
                + data.length);
        }
        if (stream == null)
        {
            throw new IllegalArgumentException("cannot write to nullstream");
        }

        long         display_offset = offset + index;
        StringBuffer buffer         = new StringBuffer(74);


        int data_length = Math.min(data.length,index+length);
        for (int j = index; j < data_length; j += 16)
        {
            int chars_read = data_length - j;

            if (chars_read > 16)
            {
                chars_read = 16;
            }
            buffer.append(
                        dump(display_offset)
                         ).append(' ');
            for (int k = 0; k < 16; k++)
            {
                if (k < chars_read)
                {
                    buffer.append(dump(data[ k + j ]));
                }
                else
                {
                    buffer.append("  ");
                }
                buffer.append(' ');
            }
            for (int k = 0; k < chars_read; k++)
            {
                if ((data[ k + j ] >= ' ') && (data[ k + j ] < 127))
                {
                    buffer.append(( char ) data[ k + j ]);
                }
                else
                {
                    buffer.append('.');
                }
            }
            buffer.append(EOL);
            stream.write(buffer.toString().getBytes());
            stream.flush();
            buffer.setLength(0);
            display_offset += chars_read;
        }

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
     * @exception IOException is thrown if anything goes wrong writing
     *            the data to stream
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @exception IllegalArgumentException if the output stream is
     *            null
     */

    public synchronized static void dump(final byte [] data, final long offset,
                            final OutputStream stream, final int index)
        throws IOException, ArrayIndexOutOfBoundsException,
                IllegalArgumentException
    {
        dump(data, offset, stream, index, data.length-index);
    }

    /**
     * dump an array of bytes to a String
     *
     * @param data the byte array to be dumped
     * @param offset its offset, whatever that might mean
     * @param index initial index into the byte array
     *
     * @exception ArrayIndexOutOfBoundsException if the index is
     *            outside the data array's bounds
     * @return output string
     */

    public static String dump(final byte [] data, final long offset,
                            final int index) {
        StringBuffer buffer;
        if ((index < 0) || (index >= data.length))
        {
            throw new ArrayIndexOutOfBoundsException(
                "illegal index: " + index + " into array of length "
                + data.length);
        }
        long         display_offset = offset + index;
        buffer         = new StringBuffer(74);

        for (int j = index; j < data.length; j += 16)
        {
            int chars_read = data.length - j;

            if (chars_read > 16)
            {
                chars_read = 16;
            }
            buffer.append(dump(display_offset)).append(' ');
            for (int k = 0; k < 16; k++)
            {
                if (k < chars_read)
                {
                    buffer.append(dump(data[ k + j ]));
                }
                else
                {
                    buffer.append("  ");
                }
                buffer.append(' ');
            }
            for (int k = 0; k < chars_read; k++)
            {
                if ((data[ k + j ] >= ' ') && (data[ k + j ] < 127))
                {
                    buffer.append(( char ) data[ k + j ]);
                }
                else
                {
                    buffer.append('.');
                }
            }
            buffer.append(EOL);
            display_offset += chars_read;
        }
        return buffer.toString();
    }


    private static String dump(final long value)
    {
        StringBuffer buf = new StringBuffer();
        buf.setLength(0);
        for (int j = 0; j < 8; j++)
        {
            buf.append( _hexcodes[ (( int ) (value >> _shifts[ j + _shifts.length - 8 ])) & 15 ]);
        }
        return buf.toString();
    }

    private static String dump(final byte value)
    {
        StringBuffer buf = new StringBuffer();
        buf.setLength(0);
        for (int j = 0; j < 2; j++)
        {
            buf.append(_hexcodes[ (value >> _shifts[ j + 6 ]) & 15 ]);
        }
        return buf.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          A String representing the array of bytes
     */
    public static String toHex(final byte[] value)
    {
        StringBuffer retVal = new StringBuffer();
        retVal.append('[');
        for(int x = 0; x < value.length; x++)
        {
            if (x>0) {
                retVal.append(", ");
            }
            retVal.append(toHex(value[x]));
        }
        retVal.append(']');
        return retVal.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          A String representing the array of shorts
     */
    public static String toHex(final short[] value)
    {
        StringBuffer retVal = new StringBuffer();
        retVal.append('[');
        for(int x = 0; x < value.length; x++)
        {
            if (x>0) {
                retVal.append(", ");
            }
            retVal.append(toHex(value[x]));
        }
        retVal.append(']');
        return retVal.toString();
    }

    /**
     * <p>Converts the parameter to a hex value breaking the results into
     * lines.</p>
     *
     * @param value        The value to convert
     * @param bytesPerLine The maximum number of bytes per line. The next byte
     *                     will be written to a new line
     * @return             A String representing the array of bytes
     */
    public static String toHex(final byte[] value, final int bytesPerLine)
    {
        final int digits =
            (int) Math.round(Math.log(value.length) / Math.log(10) + 0.5);
        final StringBuffer formatString = new StringBuffer();
        for (int i = 0; i < digits; i++)
            formatString.append('0');
        formatString.append(": ");
        final DecimalFormat format = new DecimalFormat(formatString.toString());
        StringBuffer retVal = new StringBuffer();
        retVal.append(format.format(0));
        int i = -1;
        for(int x = 0; x < value.length; x++)
        {
            if (++i == bytesPerLine)
            {
                retVal.append('\n');
                retVal.append(format.format(x));
                i = 0;
            } else if (x>0) {
                retVal.append(", ");
            }
            retVal.append(toHex(value[x]));
        }
        return retVal.toString();
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(final short value)
    {
        return toHex(value, 4);
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(final byte value)
    {
        return toHex(value, 2);
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(final int value)
    {
        return toHex(value, 8);
    }

    /**
     * Converts the parameter to a hex value.
     *
     * @param value     The value to convert
     * @return          The result right padded with 0
     */
    public static String toHex(final long value)
    {
        return toHex(value, 16);
    }


    private static String toHex(final long value, final int digits)
    {
        StringBuffer result = new StringBuffer(digits);
        for (int j = 0; j < digits; j++)
        {
            result.append( _hexcodes[ (int) ((value >> _shifts[ j + (16 - digits) ]) & 15)]);
        }
        return result.toString();
    }

    /**
     * Dumps <code>bytesToDump</code> bytes to an output stream.
     *
     * @param in          The stream to read from
     * @param out         The output stream
     * @param start       The index to use as the starting position for the left hand side label
     * @param bytesToDump The number of bytes to output.  Use -1 to read until the end of file.
     */
    public static void dump( InputStream in, PrintStream out, int start, int bytesToDump ) throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        if (bytesToDump == -1)
        {
            int c = in.read();
            while (c != -1)
            {
                buf.write(c);
                c = in.read();
            }
        }
        else
        {
            int bytesRemaining = bytesToDump;
            while (bytesRemaining-- > 0)
            {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                buf.write(c);
            }
        }

        byte[] data = buf.toByteArray();
        dump(data, 0, out, start, data.length);
    }
    /**
     * @return char array of uppercase hex chars, zero padded and prefixed with '0x'
     */
    private static char[] toHexChars(long pValue, int nBytes) {
        int charPos = 2 + nBytes*2;
        // The return type is char array because most callers will probably append the value to a
        // StringBuffer, or write it to a Stream / Writer so there is no need to create a String;
        char[] result = new char[charPos];

        long value = pValue;
        do {
            result[--charPos] = _hexcodes[(int) (value & 0x0F)];
            value >>>= 4;
        } while (charPos > 1);

        // Prefix added to avoid ambiguity
        result[0] = '0';
        result[1] = 'x';
        return result;
    }
    /**
     * @return char array of 4 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static char[] longToHex(long value) {
        return toHexChars(value, 8);
    }
    /**
     * @return char array of 4 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static char[] intToHex(int value) {
        return toHexChars(value, 4);
    }
    /**
     * @return char array of 2 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static char[] shortToHex(int value) {
        return toHexChars(value, 2);
    }
    /**
     * @return char array of 1 (zero padded) uppercase hex chars and prefixed with '0x'
     */
    public static char[] byteToHex(int value) {
        return toHexChars(value, 1);
    }

    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        byte[] b = new byte[(int)file.length()];
        in.read(b);
        System.out.println(HexDump.dump(b, 0, 0));
        in.close();
    }
}
