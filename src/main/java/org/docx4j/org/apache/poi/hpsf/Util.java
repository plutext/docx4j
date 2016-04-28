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

package org.docx4j.org.apache.poi.hpsf;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;

/**
 * <p>Provides various static utility methods.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public class Util
{

    /**
     * <p>Checks whether two byte arrays <var>a</var> and <var>b</var>
     * are equal. They are equal</p>
     *
     * <ul>
     *
     *  <li><p>if they have the same length and</p></li>
     *
     *  <li><p>if for each <var>i</var> with
     *  <var>i</var>&nbsp;&gt;=&nbsp;0 and
     *  <var>i</var>&nbsp;&lt;&nbsp;<var>a.length</var> holds
     *  <var>a</var>[<var>i</var>]&nbsp;== <var>b</var>[<var>i</var>].</p></li>
     *
     * </ul>
     *
     * @param a The first byte array
     * @param b The first byte array
     * @return <code>true</code> if the byte arrays are equal, else
     * <code>false</code>
     */
    public static boolean equal(final byte[] a, final byte[] b)
    {
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i])
                return false;
        return true;
    }



    /**
     * <p>Copies a part of a byte array into another byte array.</p>
     *
     * @param src The source byte array.
     * @param srcOffset Offset in the source byte array.
     * @param length The number of bytes to copy.
     * @param dst The destination byte array.
     * @param dstOffset Offset in the destination byte array.
     */
    public static void copy(final byte[] src, final int srcOffset,
                            final int length, final byte[] dst,
                            final int dstOffset)
    {
        for (int i = 0; i < length; i++)
            dst[dstOffset + i] = src[srcOffset + i];
    }



    /**
     * <p>Concatenates the contents of several byte arrays into a
     * single one.</p>
     *
     * @param byteArrays The byte arrays to be concatened.
     * @return A new byte array containing the concatenated byte
     * arrays.
     */
    public static byte[] cat(final byte[][] byteArrays)
    {
        int capacity = 0;
        for (int i = 0; i < byteArrays.length; i++)
            capacity += byteArrays[i].length;
        final byte[] result = new byte[capacity];
        int r = 0;
        for (int i = 0; i < byteArrays.length; i++)
            for (int j = 0; j < byteArrays[i].length; j++)
                result[r++] = byteArrays[i][j];
        return result;
    }



    /**
     * <p>Copies bytes from a source byte array into a new byte
     * array.</p>
     *
     * @param src Copy from this byte array.
     * @param offset Start copying here.
     * @param length Copy this many bytes.
     * @return The new byte array. Its length is number of copied bytes.
     */
    public static byte[] copy(final byte[] src, final int offset,
                              final int length)
    {
        final byte[] result = new byte[length];
        copy(src, offset, length, result, 0);
        return result;
    }



    /**
     * <p>The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds: 11644473600000L. (Use your favorite spreadsheet
     * program to verify the correctness of this value. By the way,
     * did you notice that you can tell from the epochs which
     * operating system is the modern one? :-))</p>
     */
    public static final long EPOCH_DIFF = 11644473600000L;


    /**
     * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601. This 64-bit value is split into the two double
     * words stored in the structure.</p>
     *
     * @param high The higher double word of the FILETIME structure.
     * @param low The lower double word of the FILETIME structure.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final int high, final int low)
    {
        final long filetime = ((long) high) << 32 | (low & 0xffffffffL);
        return filetimeToDate(filetime);
    }

    /**
     * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601.</p>
     *
     * @param filetime The filetime to convert.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final long filetime)
    {
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }



    /**
     * <p>Converts a {@link Date} into a filetime.</p>
     *
     * @param date The date to be converted
     * @return The filetime
     *
     * @see #filetimeToDate(long)
     * @see #filetimeToDate(int, int)
     */
    public static long dateToFileTime(final Date date)
    {
        long ms_since_19700101 = date.getTime();
        long ms_since_16010101 = ms_since_19700101 + EPOCH_DIFF;
        return ms_since_16010101 * (1000 * 10);
    }


    /**
     * <p>Checks whether two collections are equal. Two collections
     * C<sub>1</sub> and C<sub>2</sub> are equal, if the following conditions
     * are true:</p>
     *
     * <ul>
     *
     * <li><p>For each c<sub>1<em>i</em></sub> (element of C<sub>1</sub>) there
     * is a c<sub>2<em>j</em></sub> (element of C<sub>2</sub>), and
     * c<sub>1<em>i</em></sub> equals c<sub>2<em>j</em></sub>.</p></li>
     *
     * <li><p>For each c<sub>2<em>i</em></sub> (element of C<sub>2</sub>) there
     * is a c<sub>1<em>j</em></sub> (element of C<sub>1</sub>) and
     * c<sub>2<em>i</em></sub> equals c<sub>1<em>j</em></sub>.</p></li>
     *
     * </ul>
     *
     * @param c1 the first collection
     * @param c2 the second collection
     * @return <code>true</code> if the collections are equal, else
     * <code>false</code>.
     */
    public static boolean equals(Collection<?> c1, Collection<?> c2)
    {
        Object[] o1 = c1.toArray();
        Object[] o2 = c2.toArray();
        return internalEquals(o1, o2);
    }



    /**
     * <p>Compares to object arrays with regarding the objects' order. For
     * example, [1, 2, 3] and [2, 1, 3] are equal.</p>
     *
     * @param c1 The first object array.
     * @param c2 The second object array.
     * @return <code>true</code> if the object arrays are equal,
     * <code>false</code> if they are not.
     */
    public static boolean equals(Object[] c1, Object[] c2)
    {
        final Object[] o1 = c1.clone();
        final Object[] o2 = c2.clone();
        return internalEquals(o1, o2);
    }

    private static boolean internalEquals(Object[] o1, Object[] o2)
    {
        for (int i1 = 0; i1 < o1.length; i1++)
        {
            final Object obj1 = o1[i1];
            boolean matchFound = false;
            for (int i2 = 0; !matchFound && i2 < o1.length; i2++)
            {
                final Object obj2 = o2[i2];
                if (obj1.equals(obj2))
                {
                    matchFound = true;
                    o2[i2] = null;
                }
            }
            if (!matchFound)
                return false;
        }
        return true;
    }



    /**
     * <p>Pads a byte array with 0x00 bytes so that its length is a multiple of
     * 4.</p>
     *
     * @param ba The byte array to pad.
     * @return The padded byte array.
     */
    public static byte[] pad4(final byte[] ba)
    {
        final int PAD = 4;
        final byte[] result;
        int l = ba.length % PAD;
        if (l == 0)
            result = ba;
        else
        {
            l = PAD - l;
            result = new byte[ba.length + l];
            System.arraycopy(ba, 0, result, 0, ba.length);
        }
        return result;
    }



    /**
     * <p>Pads a character array with 0x0000 characters so that its length is a
     * multiple of 4.</p>
     *
     * @param ca The character array to pad.
     * @return The padded character array.
     */
    public static char[] pad4(final char[] ca)
    {
        final int PAD = 4;
        final char[] result;
        int l = ca.length % PAD;
        if (l == 0)
            result = ca;
        else
        {
            l = PAD - l;
            result = new char[ca.length + l];
            System.arraycopy(ca, 0, result, 0, ca.length);
        }
        return result;
    }



    /**
     * <p>Pads a string with 0x0000 characters so that its length is a
     * multiple of 4.</p>
     *
     * @param s The string to pad.
     * @return The padded string as a character array.
     */
    public static char[] pad4(final String s)
    {
        return pad4(s.toCharArray());
    }



    /**
     * <p>Returns a textual representation of a {@link Throwable}, including a
     * stacktrace.</p>
     *
     * @param t The {@link Throwable}
     *
     * @return a string containing the output of a call to
     * <code>t.printStacktrace()</code>.
     */
    public static String toString(final Throwable t)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        try
        {
            sw.close();
            return sw.toString();
        }
        catch (IOException e)
        {
            final StringBuffer b = new StringBuffer(t.getMessage());
            b.append("\n");
            b.append("Could not create a stacktrace. Reason: ");
            b.append(e.getMessage());
            return b.toString();
        }
    }

}
