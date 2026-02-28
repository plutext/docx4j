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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;

/**
 * Supports reading and writing of variant data.<p>
 *
 * <strong>FIXME (3):</strong> Reading and writing should be made more
 * uniform than it is now. The following items should be resolved:
 *
 * <ul>
 *
 * <li>Reading requires a length parameter that is 4 byte greater than the
 * actual data, because the variant type field is included.
 *
 * <li>Reading reads from a byte array while writing writes to an byte array
 * output stream.
 *
 * </ul>
 */
public class VariantSupport extends Variant {
    /**
     * HPSF is able to read these {@link Variant} types.
     */
    public static final int[] SUPPORTED_TYPES = { Variant.VT_EMPTY,
            Variant.VT_I2, Variant.VT_I4, Variant.VT_I8, Variant.VT_R8,
            Variant.VT_FILETIME, Variant.VT_LPSTR, Variant.VT_LPWSTR,
            Variant.VT_CF, Variant.VT_BOOL };


	private static Logger logger = LoggerFactory.getLogger(VariantSupport.class);	
	
    private static boolean logUnsupportedTypes;

    /**
     * Keeps a list of the variant types an "unsupported" message has already
     * been issued for.
     */
    private static final List<Long> unsupportedMessage = new LinkedList<>();

    private static final byte[] paddingBytes = new byte[3];


    /**
     * Specifies whether warnings about unsupported variant types are to be
     * written to {@code System.err} or not.
     *
     * @param logUnsupportedTypes If {@code true} warnings will be written,
     * if {@code false} they won't.
     */
    public static void setLogUnsupportedTypes(final boolean logUnsupportedTypes) {
        VariantSupport.logUnsupportedTypes = logUnsupportedTypes;
    }

    /**
     * Checks whether logging of unsupported variant types warning is turned
     * on or off.
     *
     * @return {@code true} if logging is turned on, else
     * {@code false}.
     */
    public static boolean isLogUnsupportedTypes() {
        return logUnsupportedTypes;
    }



    /**
     * Writes a warning to {@code System.err} that a variant type is
     * unsupported by HPSF. Such a warning is written only once for each variant
     * type. Log messages can be turned on or off by
     *
     * @param ex The exception to log
     */
    protected static void writeUnsupportedTypeMessage(final UnsupportedVariantTypeException ex) {
        if (isLogUnsupportedTypes()) {
            final Long vt = ex.getVariantType();
            boolean needsLogging = false;
            synchronized (unsupportedMessage) {
                if (!unsupportedMessage.contains(vt)) {
                    needsLogging = true;
                    unsupportedMessage.add(vt);
                }
            }
            if (needsLogging) {
                logger.error("Unsupported type", ex);
            }
        }
    }



    /**
     * Checks whether HPSF supports the specified variant type. Unsupported
     * types should be implemented included in the {@link #SUPPORTED_TYPES}
     * array.
     *
     * @see Variant
     * @param variantType the variant type to check
     * @return {@code true} if HPFS supports this type, else
     *         {@code false}
     */
    public boolean isSupportedType(final int variantType) {
        for (int st : SUPPORTED_TYPES) {
            if (variantType == st) {
                return true;
            }
        }
        return false;
    }



    /**
     * Reads a variant type from a byte array.
     *
     * @param src The byte array
     * @param offset The offset in the byte array where the variant starts
     * @param length The length of the variant including the variant type field
     * @param type The variant type to read
     * @param codepage The codepage to use for non-wide strings
     * @return A Java object that corresponds best to the variant field. For
     *         example, a VT_I4 is returned as a {@link Long}, a VT_LPSTR as a
     *         {@link String}.
     * @throws ReadingNotSupportedException if a property is to be written
     *            who's variant type HPSF does not yet support
     * @throws UnsupportedEncodingException if the specified codepage is not
     *            supported.
     * @see Variant
     */
    public static Object read( final byte[] src, final int offset,
            final int length, final long type, final int codepage )
    throws ReadingNotSupportedException, UnsupportedEncodingException {
        LittleEndianByteArrayInputStream lei = new LittleEndianByteArrayInputStream(src, offset);
        return read( lei, length, type, codepage );
    }

    public static Object read( LittleEndianByteArrayInputStream lei,
            final int length, final long type, final int codepage )
    throws ReadingNotSupportedException, UnsupportedEncodingException {
        final int offset = lei.getReadIndex();
        TypedPropertyValue typedPropertyValue = new TypedPropertyValue( (int) type, null );
        try {
            typedPropertyValue.readValue(lei);
        } catch ( UnsupportedOperationException exc ) {
            try {
                final byte[] v = IOUtils.toByteArray(lei, length, CodePageString.getMaxRecordLength());
                throw new ReadingNotSupportedException( type, v );
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        switch ( (int) type ) {
            /*
             * we have more property types that can be converted into Java
             * objects, but current API need to be preserved, and it returns
             * other types as byte arrays. In future major versions it shall be
             * changed -- sergey
             */
            case Variant.VT_EMPTY:
            case Variant.VT_I1:
            case Variant.VT_UI1:
            case Variant.VT_UI2:
            case Variant.VT_I4:
            case Variant.VT_UI4:
            case Variant.VT_I8:
            case Variant.VT_UI8:
            case Variant.VT_R4:
            case Variant.VT_R8:
                return typedPropertyValue.getValue();

            /*
             * also for backward-compatibility with prev. versions of POI
             * --sergey
             */
            case Variant.VT_I2:
                return ( (Short) typedPropertyValue.getValue() ).intValue();

            case Variant.VT_FILETIME:
                Filetime filetime = (Filetime) typedPropertyValue.getValue();
                return filetime.getJavaValue();

            case Variant.VT_LPSTR:
                CodePageString cpString = (CodePageString) typedPropertyValue.getValue();
                return cpString.getJavaValue( codepage );

            case Variant.VT_LPWSTR:
                UnicodeString uniString = (UnicodeString) typedPropertyValue.getValue();
                return uniString.toJavaString();

            // if(l1 < 0) {
            /*
             * YK: reading the ClipboardData packet (VT_CF) is not quite
             * correct. The size of the data is determined by the first four
             * bytes of the packet while the current implementation calculates
             * it in the Section constructor. Test files in Bugzilla 42726 and
             * 45583 clearly show that this approach does not always work. The
             * workaround below attempts to gracefully handle such cases instead
             * of throwing exceptions.
             *
             * August 20, 2009
             */
            // l1 = LittleEndian.getInt(src, o1); o1 += LittleEndianConts.INT_SIZE;
            // }
            // final byte[] v = new byte[l1];
            // System.arraycopy(src, o1, v, 0, v.length);
            // value = v;
            // break;
            case Variant.VT_CF:
                ClipboardData clipboardData = (ClipboardData) typedPropertyValue.getValue();
                return clipboardData.toByteArray();

            case Variant.VT_BOOL:
                VariantBool bool = (VariantBool) typedPropertyValue.getValue();
                return bool.getValue();

            /*
             * it is not very good, but what can do without breaking current
             * API? --sergey
             */
            default:
                final int unpadded = lei.getReadIndex()-offset;
                lei.setReadIndex(offset);
                final byte[] v = IOUtils.safelyAllocate(unpadded, CodePageString.getMaxRecordLength());
                lei.readFully( v, 0, unpadded );
                throw new ReadingNotSupportedException( type, v );
        }
    }

    /**
     * Writes a variant value to an output stream. This method ensures that
     * always a multiple of 4 bytes is written.
     *
     * @param out The stream to write the value to.
     * @param type The variant's type.
     * @param value The variant's value.
     * @param codepage The codepage to use to write non-wide strings
     * @return The number of entities that have been written. In many cases an
     * "entity" is a byte but this is not always the case.
     * @throws IOException if an I/O exceptions occurs
     * @throws WritingNotSupportedException if a property is to be written
     * who's variant type HPSF does not yet support
     */
    public static int write(final OutputStream out, final long type,
                            final Object value, final int codepage)
    throws IOException, WritingNotSupportedException {
        int length = -1;
        switch ((int) type) {
            case Variant.VT_BOOL: {
                if (value instanceof Boolean) {
                    int bb = ((Boolean)value) ? 0xff : 0x00;
                    out.write(bb);
                    out.write(bb);
                    length = 2;
                }
                break;
            }

            case Variant.VT_LPSTR:
                if (value instanceof String) {
                    CodePageString codePageString = new CodePageString();
                    codePageString.setJavaValue( (String)value, codepage );
                    length = codePageString.write( out );
                }
                break;

            case Variant.VT_LPWSTR:
                if (value instanceof String) {
                    UnicodeString uniString = new UnicodeString();
                    uniString.setJavaValue((String)value);
                    length = uniString.write(out);
                }
                break;

            case Variant.VT_CF:
                if (value instanceof byte[]) {
                    final byte[] cf = (byte[]) value;
                    out.write(cf);
                    length = cf.length;
                }
                break;

            case Variant.VT_EMPTY:
                LittleEndian.putUInt(Variant.VT_EMPTY, out);
                length = LittleEndianConsts.INT_SIZE;
                break;

            case Variant.VT_I2:
                if (value instanceof Number) {
                    LittleEndian.putShort( out, ((Number)value).shortValue() );
                    length = LittleEndianConsts.SHORT_SIZE;
                }
                break;

            case Variant.VT_UI2:
                if (value instanceof Number) {
                    LittleEndian.putUShort( ((Number)value).intValue(), out );
                    length = LittleEndianConsts.SHORT_SIZE;
                }
                break;

            case Variant.VT_I4:
                if (value instanceof Number) {
                    LittleEndian.putInt( ((Number)value).intValue(), out);
                    length = LittleEndianConsts.INT_SIZE;
                }
                break;

            case Variant.VT_UI4:
                if (value instanceof Number) {
                    LittleEndian.putUInt( ((Number)value).longValue(), out);
                    length = LittleEndianConsts.INT_SIZE;
                }
                break;

            case Variant.VT_I8:
                if (value instanceof Number) {
                    LittleEndian.putLong( ((Number)value).longValue(), out);
                    length = LittleEndianConsts.LONG_SIZE;
                }
                break;

            case Variant.VT_UI8: {
                if (value instanceof Number) {
                    BigInteger bi = (value instanceof BigInteger) ? (BigInteger)value : BigInteger.valueOf(((Number)value).longValue());
                    if (bi.bitLength() > 64) {
                        throw new WritingNotSupportedException(type, value);
                    }

                    byte[] biBytesBE = bi.toByteArray(), biBytesLE = new byte[LittleEndianConsts.LONG_SIZE];
                    int i=biBytesBE.length;
                    for (byte b : biBytesBE) {
                        if (i<=LittleEndianConsts.LONG_SIZE) {
                            biBytesLE[i-1] = b;
                        }
                        i--;
                    }

                    out.write(biBytesLE);
                    length = LittleEndianConsts.LONG_SIZE;
                }
                break;
            }

            case Variant.VT_R4: {
                if (value instanceof Number) {
                    int floatBits = Float.floatToIntBits(((Number)value).floatValue());
                    LittleEndian.putInt(floatBits, out);
                    length = LittleEndianConsts.INT_SIZE;
                }
                break;
            }

            case Variant.VT_R8:
                if (value instanceof Number) {
                    LittleEndian.putDouble( ((Number)value).doubleValue(), out);
                    length = LittleEndianConsts.DOUBLE_SIZE;
                }
                break;

            case Variant.VT_FILETIME:
                Filetime filetimeValue = (value instanceof Date) ? new Filetime((Date)value) : new Filetime();
                length = filetimeValue.write( out );
                break;

            default:
                break;
        }

        /* The variant type is not supported yet. However, if the value
         * is a byte array we can write it nevertheless. */
        if (length == -1) {
            if (value instanceof byte[]) {
                final byte[] b = (byte[]) value;
                out.write(b);
                length = b.length;
                writeUnsupportedTypeMessage(new WritingNotSupportedException(type, value));
            } else {
                throw new WritingNotSupportedException(type, value);
            }
        }

        /* pad values to 4-bytes */
        int padding = (4-(length & 0x3)) & 0x3;
        out.write(paddingBytes, 0, padding);

        return length + padding;
    }
}
