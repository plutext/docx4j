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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * a utility class for handling little-endian numbers, which the 80x86 world is
 * replete with. The methods are all static, and input/output is from/to byte
 * arrays, or from InputStreams.
 * 
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew Oliver (acoliver at apache dot org)
 */
public class LittleEndian implements LittleEndianConsts
{

    /**
     * Exception to handle buffer underruns
     * 
     * @author Marc Johnson (mjohnson at apache dot org)
     */
    public static final class BufferUnderrunException extends IOException
    {
        /**
         * Serial version UID
         * 
         * @see Serializable
         */
        private static final long serialVersionUID = 8736973884877006145L;

        BufferUnderrunException()
        {
            super( "buffer underrun" );
        }
    }

    /**
     * Copy a portion of a byte array
     * 
     * @param data
     *            the original byte array
     * @param offset
     *            Where to start copying from.
     * @param size
     *            Number of bytes to copy.
     * @return The byteArray value
     * @throws IndexOutOfBoundsException
     *             - if copying would cause access of data outside array bounds.
     */
    public static byte[] getByteArray( byte[] data, int offset, int size )
    {
        byte[] copy = new byte[size];
        System.arraycopy( data, offset, copy, 0, size );

        return copy;
    }

    /**
     * get a double value from a byte array, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java double
     * 
     * @param data
     *            the byte array
     * @return the double (64-bit) value
     */
    public static double getDouble( byte[] data )
    {
        return Double.longBitsToDouble( getLong( data, 0 ) );
    }

    /**
     * get a double value from a byte array, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java double
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the double (64-bit) value
     */
    public static double getDouble( byte[] data, int offset )
    {
        return Double.longBitsToDouble( getLong( data, offset ) );
    }

    /**
     * get a float value from a byte array, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java float
     * 
     * @param data
     *            the byte array
     * @return the double (64-bit) value
     */
    public static float getFloat( byte[] data )
    {
        return getFloat( data, 0 );
    }

    /**
     * get a float value from a byte array, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java float
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the double (64-bit) value
     */
    public static float getFloat( byte[] data, int offset )
    {
        return Float.intBitsToFloat( getInt( data, offset ) );
    }

    /**
     * get an int value from the beginning of a byte array
     * 
     * @param data
     *            the byte array
     * @return the int (32-bit) value
     */
    public static int getInt( byte[] data )
    {
        return getInt( data, 0 );
    }

    /**
     * get an int value from a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the int (32-bit) value
     */
    public static int getInt( byte[] data, int offset )
    {
        int i = offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        return ( b3 << 24 ) + ( b2 << 16 ) + ( b1 << 8 ) + ( b0 << 0 );
    }

    /**
     * get a long value from a byte array
     * 
     * @param data
     *            the byte array
     * @return the long (64-bit) value
     */
    public static long getLong( byte[] data )
    {
        return getLong( data, 0 );
    }

    /**
     * get a long value from a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the long (64-bit) value
     */
    public static long getLong( byte[] data, int offset )
    {
        long result = 0xff & data[offset + 7];

        for ( int j = offset + LONG_SIZE - 1; j >= offset; j-- )
        {
            result <<= 8;
            result |= 0xff & data[j];
        }
        return result;
    }

    /**
     * get a short value from the beginning of a byte array
     * 
     * @param data
     *            the byte array
     * @return the short (16-bit) value
     */
    public static short getShort( byte[] data )
    {
        return getShort( data, 0 );
    }

    /**
     * get a short value from a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the short (16-bit) value
     */
    public static short getShort( byte[] data, int offset )
    {
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset + 1] & 0xFF;
        return (short) ( ( b1 << 8 ) + ( b0 << 0 ) );
    }

    /**
     * Read short array
     * 
     * @param data
     *            the original byte array
     * @param offset
     *            Where to start copying from.
     * @param size
     *            Number of bytes to copy.
     * @throws IndexOutOfBoundsException
     *             - if read would cause access of data outside array bounds.
     */
    public static short[] getShortArray( byte[] data, int offset, int size )
    {
        short[] result = new short[size / SHORT_SIZE];
        for ( int i = 0; i < result.length; i++ )
        {
            result[i] = getShort( data, offset + i * SHORT_SIZE );
        }
        return result;
    }

    /**
     * get the unsigned value of a byte.
     * 
     * @param data
     *            the byte array.
     * @return the unsigned value of the byte as a 16 bit short
     */
    public static short getUByte( byte[] data )
    {
        return (short) ( data[0] & 0xFF );
    }

    /**
     * get the unsigned value of a byte.
     * 
     * @param data
     *            the byte array.
     * @param offset
     *            a starting offset into the byte array.
     * @return the unsigned value of the byte as a 16 bit short
     */
    public static short getUByte( byte[] data, int offset )
    {
        return (short) ( data[offset] & 0xFF );
    }

    /**
     * get an unsigned int value from a byte array
     * 
     * @param data
     *            the byte array
     * @return the unsigned int (32-bit) value in a long
     */
    public static long getUInt( byte[] data )
    {
        return getUInt( data, 0 );
    }

    /**
     * get an unsigned int value from a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the unsigned int (32-bit) value in a long
     */
    public static long getUInt( byte[] data, int offset )
    {
        long retNum = getInt( data, offset );
        return retNum & 0x00FFFFFFFFl;
    }

    /**
     * get the unsigned value of a byte.
     * 
     * @param data
     *            the byte array.
     * @param offset
     *            a starting offset into the byte array.
     * @return the unsigned value of the byte as a 32 bit integer
     * @deprecated Use {@link #getUByte(byte[], int)} instead
     */
    @Deprecated
    public static int getUnsignedByte( byte[] data, int offset )
    {
        return data[offset] & 0xFF;
    }

    /**
     * get an unsigned short value from the beginning of a byte array
     * 
     * @param data
     *            the byte array
     * @return the unsigned short (16-bit) value in an int
     */
    public static int getUShort( byte[] data )
    {
        return getUShort( data, 0 );
    }

    /**
     * get an unsigned short value from a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @return the unsigned short (16-bit) value in an integer
     */
    public static int getUShort( byte[] data, int offset )
    {
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset + 1] & 0xFF;
        return ( b1 << 8 ) + ( b0 << 0 );
    }

    /**
     * executes:
     * <p/>
     * <code>
     * data[offset] = (byte)value;
     * </code>
     * </p>
     * Added for consistency with other put~() methods
     */
    public static void putByte( byte[] data, int offset, int value )
    {
        data[offset] = (byte) value;
    }

    /**
     * put a double value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the double (64-bit) value
     */
    public static void putDouble( byte[] data, int offset, double value )
    {
        putLong( data, offset, Double.doubleToLongBits( value ) );
    }

    /**
     * put a double value into a byte array
     * 
     * @param value
     *            the double (64-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putDouble( double value, OutputStream outputStream )
            throws IOException
    {
        putLong( Double.doubleToLongBits( value ), outputStream );
    }

    /**
     * put a float value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the float (32-bit) value
     */
    public static void putFloat( byte[] data, int offset, float value )
    {
        putInt( data, offset, Float.floatToIntBits( value ) );
    }

    /**
     * put a float value into a byte array
     * 
     * @param value
     *            the float (32-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putFloat( float value, OutputStream outputStream )
            throws IOException
    {
        putInt( Float.floatToIntBits( value ), outputStream );
    }

    /**
     * put an int value into beginning of a byte array
     * 
     * @param data
     *            the byte array
     * @param value
     *            the int (32-bit) value
     * @deprecated Use {@link #putInt(byte[], int, int)} instead
     */
    @Deprecated
    public static void putInt( byte[] data, int value )
    {
        putInt( data, 0, value );
    }

    /**
     * put an int value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the int (32-bit) value
     */
    public static void putInt( byte[] data, int offset, int value )
    {
        int i = offset;
        data[i++] = (byte) ( ( value >>> 0 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 8 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 16 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 24 ) & 0xFF );
    }

    /**
     * Put int into output stream
     * 
     * @param value
     *            the int (32-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putInt( int value, OutputStream outputStream )
            throws IOException
    {
        outputStream.write( (byte) ( ( value >>> 0 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 8 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 16 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 24 ) & 0xFF ) );
    }

    /**
     * put a long value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the long (64-bit) value
     */
    public static void putLong( byte[] data, int offset, long value )
    {
        data[offset + 0] = (byte) ( ( value >>> 0 ) & 0xFF );
        data[offset + 1] = (byte) ( ( value >>> 8 ) & 0xFF );
        data[offset + 2] = (byte) ( ( value >>> 16 ) & 0xFF );
        data[offset + 3] = (byte) ( ( value >>> 24 ) & 0xFF );
        data[offset + 4] = (byte) ( ( value >>> 32 ) & 0xFF );
        data[offset + 5] = (byte) ( ( value >>> 40 ) & 0xFF );
        data[offset + 6] = (byte) ( ( value >>> 48 ) & 0xFF );
        data[offset + 7] = (byte) ( ( value >>> 56 ) & 0xFF );
    }

    /**
     * Put long into output stream
     * 
     * @param value
     *            the long (64-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putLong( long value, OutputStream outputStream )
            throws IOException
    {
        outputStream.write( (byte) ( ( value >>> 0 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 8 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 16 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 24 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 32 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 40 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 48 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 56 ) & 0xFF ) );
    }

    /**
     * put a short value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the short (16-bit) value
     */
    public static void putShort( byte[] data, int offset, short value )
    {
        int i = offset;
        data[i++] = (byte) ( ( value >>> 0 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 8 ) & 0xFF );
    }

    /**
     * put a short value into beginning of a byte array
     * 
     * @param data
     *            the byte array
     * @param value
     *            the short (16-bit) value
     * @deprecated Use {@link #putShort(byte[], int, short)} instead
     */
    @Deprecated
    public static void putShort( byte[] data, short value )
    {
        putShort( data, 0, value );
    }

    /**
     * Put signed short into output stream
     * 
     * @param value
     *            the short (16-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putShort( OutputStream outputStream, short value )
            throws IOException
    {
        outputStream.write( (byte) ( ( value >>> 0 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 8 ) & 0xFF ) );
    }

    /**
     * Stores short array in buffer
     * 
     * @param data
     *            the byte array
     * @param startOffset
     *            a starting offset into the byte array
     * @param value
     *            the short (16-bit) values
     */
    public static void putShortArray( byte[] data, int startOffset,
            short[] value )
    {
        int offset = startOffset;
        for ( short s : value )
        {
            putShort( data, offset, s );
            offset += SHORT_SIZE;
        }
    }

    /**
     * put an unsigned byte value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the short (16-bit) value
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                may be thrown
     */
    public static void putUByte( byte[] data, int offset, short value )
    {
        data[offset] = (byte) ( value & 0xFF );
    }

    /**
     * put an unsigned int value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the int (32-bit) value
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                may be thrown
     */
    public static void putUInt( byte[] data, int offset, long value )
    {
        int i = offset;
        data[i++] = (byte) ( ( value >>> 0 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 8 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 16 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 24 ) & 0xFF );
    }

    /**
     * put an unsigned int value into beginning of a byte array
     * 
     * @param data
     *            the byte array
     * @param value
     *            the int (32-bit) value
     * @deprecated Use {@link #putUInt(byte[], int, long)} instead
     */
    @Deprecated
    public static void putUInt( byte[] data, long value )
    {
        putUInt( data, 0, value );
    }

    /**
     * Put unsigned int into output stream
     * 
     * @param value
     *            the int (32-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putUInt( long value, OutputStream outputStream )
            throws IOException
    {
        outputStream.write( (byte) ( ( value >>> 0 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 8 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 16 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 24 ) & 0xFF ) );
    }

    /**
     * put an unsigned short value into a byte array
     * 
     * @param data
     *            the byte array
     * @param offset
     *            a starting offset into the byte array
     * @param value
     *            the short (16-bit) value
     * 
     * @exception ArrayIndexOutOfBoundsException
     *                may be thrown
     */
    public static void putUShort( byte[] data, int offset, int value )
    {
        int i = offset;
        data[i++] = (byte) ( ( value >>> 0 ) & 0xFF );
        data[i++] = (byte) ( ( value >>> 8 ) & 0xFF );
    }

    /**
     * Put unsigned short into output stream
     * 
     * @param value
     *            the unsigned short (16-bit) value
     * @param outputStream
     *            output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void putUShort( int value, OutputStream outputStream )
            throws IOException
    {
        outputStream.write( (byte) ( ( value >>> 0 ) & 0xFF ) );
        outputStream.write( (byte) ( ( value >>> 8 ) & 0xFF ) );
    }

    /**
     * get an int value from an InputStream
     * 
     * @param stream
     *            the InputStream from which the int is to be read
     * @return the int (32-bit) value
     * @exception IOException
     *                will be propagated back to the caller
     * @exception BufferUnderrunException
     *                if the stream cannot provide enough bytes
     */
    public static int readInt( InputStream stream ) throws IOException,
            BufferUnderrunException
    {
        int ch1 = stream.read();
        int ch2 = stream.read();
        int ch3 = stream.read();
        int ch4 = stream.read();
        if ( ( ch1 | ch2 | ch3 | ch4 ) < 0 )
        {
            throw new BufferUnderrunException();
        }
        return ( ch4 << 24 ) + ( ch3 << 16 ) + ( ch2 << 8 ) + ( ch1 << 0 );
    }
    
    /**
     * get an unsigned int value from an InputStream
     * 
     * @param stream
     *            the InputStream from which the int is to be read
     * @return the unsigned int (32-bit) value
     * @exception IOException
     *                will be propagated back to the caller
     * @exception BufferUnderrunException
     *                if the stream cannot provide enough bytes
     */
    public static long readUInt( InputStream stream ) throws IOException,
            BufferUnderrunException
    {
       long retNum = readInt(stream);
       return retNum & 0x00FFFFFFFFl;
    }

    /**
     * get a long value from an InputStream
     * 
     * @param stream
     *            the InputStream from which the long is to be read
     * @return the long (64-bit) value
     * @exception IOException
     *                will be propagated back to the caller
     * @exception BufferUnderrunException
     *                if the stream cannot provide enough bytes
     */
    public static long readLong( InputStream stream ) throws IOException,
            BufferUnderrunException
    {
        int ch1 = stream.read();
        int ch2 = stream.read();
        int ch3 = stream.read();
        int ch4 = stream.read();
        int ch5 = stream.read();
        int ch6 = stream.read();
        int ch7 = stream.read();
        int ch8 = stream.read();
        if ( ( ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8 ) < 0 )
        {
            throw new BufferUnderrunException();
        }

        return ( (long) ch8 << 56 ) + ( (long) ch7 << 48 )
                + ( (long) ch6 << 40 ) + ( (long) ch5 << 32 )
                + ( (long) ch4 << 24 ) + // cast to long to preserve bit 31
                                         // (sign bit for ints)
                ( ch3 << 16 ) + ( ch2 << 8 ) + ( ch1 << 0 );
    }

    /**
     * get a short value from an InputStream
     * 
     * @param stream
     *            the InputStream from which the short is to be read
     * @return the short (16-bit) value
     * @exception IOException
     *                will be propagated back to the caller
     * @exception BufferUnderrunException
     *                if the stream cannot provide enough bytes
     */
    public static short readShort( InputStream stream ) throws IOException,
            BufferUnderrunException
    {
        return (short) readUShort( stream );
    }

    public static int readUShort( InputStream stream ) throws IOException,
            BufferUnderrunException
    {
        int ch1 = stream.read();
        int ch2 = stream.read();
        if ( ( ch1 | ch2 ) < 0 )
        {
            throw new BufferUnderrunException();
        }
        return ( ch2 << 8 ) + ( ch1 << 0 );
    }

    /**
     * Convert an 'unsigned' byte to an integer. ie, don't carry across the
     * sign.
     * 
     * @param b
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    public static int ubyteToInt( byte b )
    {
        return b & 0xFF;
    }

    private LittleEndian()
    {
        // no instances of this class
    }
}
