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

import static org.docx4j.org.apache.poi.util.LittleEndianConsts.LONG_SIZE;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;

import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;

/**
 * The Windows FILETIME structure holds a date and time associated with a
 * file. The structure identifies a 64-bit integer specifying the
 * number of 100-nanosecond intervals which have passed since
 * January 1, 1601, Coordinated Universal Time (UTC).
 */
@Internal
public class Filetime {
    /**
     * The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds.
     */
    private static final BigInteger EPOCH_DIFF = BigInteger.valueOf(-11_644_473_600_000L);

    /** Factor between filetime long and date milliseconds */
    private static final BigInteger NANO_100 = BigInteger.valueOf(10_000L);
    
    private long fileTime;

    public Filetime() {}

    public Filetime( Date date ) {
        fileTime = dateToFileTime(date);
    }

    public void read( LittleEndianByteArrayInputStream lei ) {
        fileTime = lei.readLong();
    }

    public byte[] toByteArray() {
        byte[] result = new byte[LONG_SIZE];
        LittleEndian.putLong( result, 0, fileTime);
        return result;
    }

    public int write( OutputStream out ) throws IOException {
        out.write(toByteArray());
        return LONG_SIZE;
    }

    public Date getJavaValue() {
        return filetimeToDate( fileTime );
    }
    
    /**
     * Converts a Windows FILETIME (in UTC) into a {@link Date} (in UTC).
     *
     * @param filetime The filetime to convert.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final long filetime) {
        final BigInteger bi = (filetime < 0) ? twoComplement(filetime) : BigInteger.valueOf(filetime);
        return new Date(bi.divide(NANO_100).add(EPOCH_DIFF).longValue());
    }

    /**
     * Converts a {@link Date} into a filetime.
     *
     * @param date The date to be converted
     * @return The filetime
     *
     * @see #filetimeToDate(long)
     */
    public static long dateToFileTime(final Date date) {
        return BigInteger.valueOf(date.getTime()).subtract(EPOCH_DIFF).multiply(NANO_100).longValue();
    }
    
    /**
     * Return {@code true} if the date is undefined
     *
     * @param date the date
     * @return {@code true} if the date is undefined
     */
    public static boolean isUndefined(Date date) {
        return (date == null || dateToFileTime(date) == 0);
    }

    private static BigInteger twoComplement(final long val) {
        // for negative BigInteger, top byte is negative
        final byte[] contents = {
                (byte)(val < 0 ? 0 : -1),
                (byte)((val >> 56) & 0xFF),
                (byte)((val >> 48) & 0xFF),
                (byte)((val >> 40) & 0xFF),
                (byte)((val >> 32) & 0xFF),
                (byte)((val >> 24) & 0xFF),
                (byte)((val >> 16) & 0xFF),
                (byte)((val >> 8) & 0xFF),
                (byte)(val & 0xFF),
        };

        return new BigInteger(contents);
    }
}
