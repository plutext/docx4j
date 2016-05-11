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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.docx4j.org.apache.poi.util.CodePageUtil;
//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Supports reading and writing of variant data.</p>
 *
 * <p><strong>FIXME (3):</strong> Reading and writing should be made more
 * uniform than it is now. The following items should be resolved:
 *
 * <ul>
 *
 * <li><p>Reading requires a length parameter that is 4 byte greater than the
 * actual data, because the variant type field is included. </p></li>
 *
 * <li><p>Reading reads from a byte array while writing writes to an byte array
 * output stream.</p></li>
 *
 * </ul>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class VariantSupport extends Variant
{
	private static Logger logger = LoggerFactory.getLogger(VariantSupport.class);	
//	private static final POILogger logger = POILogFactory.getLogger(VariantSupport.class);
    private static boolean logUnsupportedTypes = false;

    /**
     * <p>Specifies whether warnings about unsupported variant types are to be
     * written to <code>System.err</code> or not.</p>
     *
     * @param logUnsupportedTypes If <code>true</code> warnings will be written,
     * if <code>false</code> they won't.
     */
    public static void setLogUnsupportedTypes(final boolean logUnsupportedTypes)
    {
        VariantSupport.logUnsupportedTypes = logUnsupportedTypes;
    }

    /**
     * <p>Checks whether logging of unsupported variant types warning is turned
     * on or off.</p>
     *
     * @return <code>true</code> if logging is turned on, else
     * <code>false</code>.
     */
    public static boolean isLogUnsupportedTypes()
    {
        return logUnsupportedTypes;
    }



    /**
     * <p>Keeps a list of the variant types an "unsupported" message has already
     * been issued for.</p>
     */
    protected static List<Long> unsupportedMessage;

    /**
     * <p>Writes a warning to <code>System.err</code> that a variant type is
     * unsupported by HPSF. Such a warning is written only once for each variant
     * type. Log messages can be turned on or off by </p>
     *
     * @param ex The exception to log
     */
    protected static void writeUnsupportedTypeMessage
        (final UnsupportedVariantTypeException ex)
    {
        if (isLogUnsupportedTypes())
        {
            if (unsupportedMessage == null)
                unsupportedMessage = new LinkedList<Long>();
            Long vt = Long.valueOf(ex.getVariantType());
            if (!unsupportedMessage.contains(vt))
            {
            	logger.error( ex.getMessage());
                unsupportedMessage.add(vt);
            }
        }
    }


    /**
     * <p>HPSF is able to read these {@link Variant} types.</p>
     */
    final static public int[] SUPPORTED_TYPES = { Variant.VT_EMPTY,
            Variant.VT_I2, Variant.VT_I4, Variant.VT_I8, Variant.VT_R8,
            Variant.VT_FILETIME, Variant.VT_LPSTR, Variant.VT_LPWSTR,
            Variant.VT_CF, Variant.VT_BOOL };



    /**
     * <p>Checks whether HPSF supports the specified variant type. Unsupported
     * types should be implemented included in the {@link #SUPPORTED_TYPES}
     * array.</p>
     *
     * @see Variant
     * @param variantType the variant type to check
     * @return <code>true</code> if HPFS supports this type, else
     *         <code>false</code>
     */
    public boolean isSupportedType(final int variantType)
    {
        for (int i = 0; i < SUPPORTED_TYPES.length; i++)
            if (variantType == SUPPORTED_TYPES[i])
                return true;
        return false;
    }



    /**
     * <p>Reads a variant type from a byte array.</p>
     *
     * @param src The byte array
     * @param offset The offset in the byte array where the variant starts
     * @param length The length of the variant including the variant type field
     * @param type The variant type to read
     * @param codepage The codepage to use for non-wide strings
     * @return A Java object that corresponds best to the variant field. For
     *         example, a VT_I4 is returned as a {@link Long}, a VT_LPSTR as a
     *         {@link String}.
     * @exception ReadingNotSupportedException if a property is to be written
     *            who's variant type HPSF does not yet support
     * @exception UnsupportedEncodingException if the specified codepage is not
     *            supported.
     * @see Variant
     */
    public static Object read( final byte[] src, final int offset,
            final int length, final long type, final int codepage )
            throws ReadingNotSupportedException, UnsupportedEncodingException
    {
        TypedPropertyValue typedPropertyValue = new TypedPropertyValue(
                (int) type, null );
        int unpadded;
        try
        {
            unpadded = typedPropertyValue.readValue( src, offset );
        }
        catch ( UnsupportedOperationException exc )
        {
            int propLength = Math.min( length, src.length - offset );
            final byte[] v = new byte[propLength];
            System.arraycopy( src, offset, v, 0, propLength );
            throw new ReadingNotSupportedException( type, v );
        }

        switch ( (int) type )
        {
        case Variant.VT_EMPTY:
        case Variant.VT_I4:
        case Variant.VT_I8:
        case Variant.VT_R8:
            /*
             * we have more property types that can be converted into Java
             * objects, but current API need to be preserved, and it returns
             * other types as byte arrays. In future major versions it shall be
             * changed -- sergey
             */
            return typedPropertyValue.getValue();

        case Variant.VT_I2:
        {
            /*
             * also for backward-compatibility with prev. versions of POI
             * --sergey
             */
            return Integer.valueOf( ( (Short) typedPropertyValue.getValue() )
                    .intValue() );
        }
        case Variant.VT_FILETIME:
        {
            Filetime filetime = (Filetime) typedPropertyValue.getValue();
            return Util.filetimeToDate( (int) filetime.getHigh(),
                    (int) filetime.getLow() );
        }
        case Variant.VT_LPSTR:
        {
            CodePageString string = (CodePageString) typedPropertyValue
                    .getValue();
            return string.getJavaValue( codepage );
        }
        case Variant.VT_LPWSTR:
        {
            UnicodeString string = (UnicodeString) typedPropertyValue
                    .getValue();
            return string.toJavaString();
        }
        case Variant.VT_CF:
        {
            // if(l1 < 0) {
            /**
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
            // l1 = LittleEndian.getInt(src, o1); o1 += LittleEndian.INT_SIZE;
            // }
            // final byte[] v = new byte[l1];
            // System.arraycopy(src, o1, v, 0, v.length);
            // value = v;
            // break;
            ClipboardData clipboardData = (ClipboardData) typedPropertyValue
                    .getValue();
            return clipboardData.toByteArray();
        }

        case Variant.VT_BOOL:
        {
            VariantBool bool = (VariantBool) typedPropertyValue.getValue();
            return Boolean.valueOf( bool.getValue() );
        }

        default:
        {
            /*
             * it is not very good, but what can do without breaking current
             * API? --sergey
             */
            final byte[] v = new byte[unpadded];
            System.arraycopy( src, offset, v, 0, unpadded );
            throw new ReadingNotSupportedException( type, v );
        }
        }
    }

    /**
     * <p>Turns a codepage number into the equivalent character encoding's
     * name.</p>
     *
     * @param codepage The codepage number
     *
     * @return The character encoding's name. If the codepage number is 65001,
     * the encoding name is "UTF-8". All other positive numbers are mapped to
     * "cp" followed by the number, e.g. if the codepage number is 1252 the
     * returned character encoding name will be "cp1252".
     *
     * @exception UnsupportedEncodingException if the specified codepage is
     * less than zero.
     */
    public static String codepageToEncoding(final int codepage)
    throws UnsupportedEncodingException
    {
        return CodePageUtil.codepageToEncoding(codepage);
    }


    /**
     * <p>Writes a variant value to an output stream. This method ensures that
     * always a multiple of 4 bytes is written.</p>
     *
     * <p>If the codepage is UTF-16, which is encouraged, strings
     * <strong>must</strong> always be written as {@link Variant#VT_LPWSTR}
     * strings, not as {@link Variant#VT_LPSTR} strings. This method ensure this
     * by converting strings appropriately, if needed.</p>
     *
     * @param out The stream to write the value to.
     * @param type The variant's type.
     * @param value The variant's value.
     * @param codepage The codepage to use to write non-wide strings
     * @return The number of entities that have been written. In many cases an
     * "entity" is a byte but this is not always the case.
     * @exception IOException if an I/O exceptions occurs
     * @exception WritingNotSupportedException if a property is to be written
     * who's variant type HPSF does not yet support
     */
    public static int write(final OutputStream out, final long type,
                            final Object value, final int codepage)
        throws IOException, WritingNotSupportedException
    {
        int length = 0;
        switch ((int) type)
        {
            case Variant.VT_BOOL:
            {
                if ( ( (Boolean) value ).booleanValue() )
                {
                    out.write( 0xff );
                    out.write( 0xff );
                }
                else
                {
                    out.write( 0x00 );
                    out.write( 0x00 );
                }
                length += 2;
                break;
            }
            case Variant.VT_LPSTR:
            {
                CodePageString codePageString = new CodePageString( (String) value,
                        codepage );
                length += codePageString.write( out );
                break;
            }
            case Variant.VT_LPWSTR:
            {
                final int nrOfChars = ( (String) value ).length() + 1;
                length += TypeWriter.writeUIntToStream( out, nrOfChars );
                char[] s = ( (String) value ).toCharArray();
                for ( int i = 0; i < s.length; i++ )
                {
                    final int high = ( ( s[i] & 0x0000ff00 ) >> 8 );
                    final int low = ( s[i] & 0x000000ff );
                    final byte highb = (byte) high;
                    final byte lowb = (byte) low;
                    out.write( lowb );
                    out.write( highb );
                    length += 2;
                }
                // NullTerminator
                out.write( 0x00 );
                out.write( 0x00 );
                length += 2;
                break;
            }
            case Variant.VT_CF:
            {
                final byte[] b = (byte[]) value;
                out.write(b);
                length = b.length;
                break;
            }
            case Variant.VT_EMPTY:
            {
                length += TypeWriter.writeUIntToStream( out, Variant.VT_EMPTY );
                break;
            }
            case Variant.VT_I2:
            {
                length += TypeWriter.writeToStream( out,
                        ( (Integer) value ).shortValue() );
                break;
            }
            case Variant.VT_I4:
            {
                if (!(value instanceof Integer))
                {
                    throw new ClassCastException("Could not cast an object to "
                            + Integer.class.toString() + ": "
                            + value.getClass().toString() + ", "
                            + value.toString());
                }
                length += TypeWriter.writeToStream(out,
                          ((Integer) value).intValue());
                break;
            }
            case Variant.VT_I8:
            {
                length += TypeWriter.writeToStream(out, ((Long) value).longValue());
                break;
            }
            case Variant.VT_R8:
            {
                length += TypeWriter.writeToStream(out,
                          ((Double) value).doubleValue());
                break;
            }
            case Variant.VT_FILETIME:
            {
                long filetime = Util.dateToFileTime((Date) value);
                int high = (int) ((filetime >> 32) & 0x00000000FFFFFFFFL);
                int low = (int) (filetime & 0x00000000FFFFFFFFL);
                Filetime filetimeValue = new Filetime( low, high);
                length += filetimeValue.write( out );
                break;
            }
            default:
            {
                /* The variant type is not supported yet. However, if the value
                 * is a byte array we can write it nevertheless. */
                if (value instanceof byte[])
                {
                    final byte[] b = (byte[]) value;
                    out.write(b);
                    length = b.length;
                    writeUnsupportedTypeMessage
                        (new WritingNotSupportedException(type, value));
                }
                else
                    throw new WritingNotSupportedException(type, value);
                break;
            }
        }

        /* pad values to 4-bytes */
        while ( ( length & 0x3 ) != 0 )
        {
            out.write( 0x00 );
            length++;
        }

        return length;
    }
}
