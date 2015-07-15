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

import org.docx4j.org.apache.poi.util.LittleEndian.BufferUnderrunException;

import java.io.*;
import java.nio.BufferUnderflowException;

/**
 * representation of a byte (8-bit) field at a fixed location within a
 * byte array
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public class ByteField
    implements FixedField
{
    private static final byte _default_value = 0;
    private byte              _value;
    private final int         _offset;

    /**
     * construct the ByteField with its offset into its containing
     * byte array and a default value of 0
     *
     * @param offset of the field within its byte array
     *
     * @exception ArrayIndexOutOfBoundsException if offset is negative
     */

    public ByteField(final int offset)
        throws ArrayIndexOutOfBoundsException
    {
        this(offset, _default_value);
    }

    /**
     * construct the ByteField with its offset into its containing
     * byte array and initialize its value
     *
     * @param offset of the field within its byte array
     * @param value the initial value
     *
     * @exception ArrayIndexOutOfBoundsException if offset is negative
     */

    public ByteField(final int offset, final byte value)
        throws ArrayIndexOutOfBoundsException
    {
        if (offset < 0)
        {
            throw new ArrayIndexOutOfBoundsException(
                "offset cannot be negative");
        }
        _offset = offset;
        set(value);
    }

    /**
     * Construct the ByteField with its offset into its containing
     * byte array and initialize its value from its byte array
     *
     * @param offset of the field within its byte array
     * @param data the byte array to read the value from
     *
     * @exception ArrayIndexOutOfBoundsException if the offset is not
     *            within the range of 0..(data.length - 1)
     */

    public ByteField(final int offset, final byte [] data)
        throws ArrayIndexOutOfBoundsException
    {
        this(offset);
        readFromBytes(data);
    }

    /**
     * construct the ByteField with its offset into its containing
     * byte array, initialize its value, and write its value to its
     * byte array
     *
     * @param offset of the field within its byte array
     * @param value the initial value
     * @param data the byte array to write the value to
     *
     * @exception ArrayIndexOutOfBoundsException if the offset is not
     *            within the range of 0..(data.length - 1)
     */

    public ByteField(final int offset, final byte value, final byte [] data)
        throws ArrayIndexOutOfBoundsException
    {
        this(offset, value);
        writeToBytes(data);
    }

    /**
     * get the ByteField's current value
     *
     * @return current value
     */

    public byte get()
    {
        return _value;
    }

    /**
     * set the ByteField's current value
     *
     * @param value to be set
     */

    public void set(final byte value)
    {
        _value = value;
    }

    /**
     * set the ByteField's current value and write it to a byte array
     *
     * @param value to be set
     * @param data the byte array to write the value to
     *
     * @exception ArrayIndexOutOfBoundsException if the offset is out
     *            of the byte array's range
     */

    public void set(final byte value, final byte [] data)
        throws ArrayIndexOutOfBoundsException
    {
        set(value);
        writeToBytes(data);
    }

    /* ********** START implementation of FixedField ********** */

    /**
     * set the value from its offset into an array of bytes
     *
     * @param data the byte array from which the value is to be read
     *
     * @exception ArrayIndexOutOfBoundsException if the offset is out
     *            of range of the bte array
     */

    public void readFromBytes(final byte [] data)
        throws ArrayIndexOutOfBoundsException
    {
        _value = data[ _offset ];
    }

    /**
     * set the value from an InputStream
     *
     * @param stream the InputStream from which the value is to be
     *               read
     *
     * @exception BufferUnderrunException if there is not enough data
     *            available from the InputStream
     * @exception IOException if an IOException is thrown from reading
     *            the InputStream
     */

    public void readFromStream(final InputStream stream)
        throws IOException, BufferUnderrunException
    {
    	// TODO - are these ~Field used / necessary
    	int ib = stream.read();
    	if (ib < 0) {
    		throw new BufferUnderflowException();
    	}
        _value = (byte) ib;
    }

    /**
     * write the value out to an array of bytes at the appropriate
     * offset
     *
     * @param data the array of bytes to which the value is to be
     *             written
     *
     * @exception ArrayIndexOutOfBoundsException if the offset is out
     *            of the byte array's range
     */

    public void writeToBytes(final byte [] data)
        throws ArrayIndexOutOfBoundsException
    {
        data[ _offset ] = _value;
    }

    /**
     * return the value as a String
     *
     * @return the value as a String
     */

    public String toString()
    {
        return String.valueOf(_value);
    }

    /* **********  END  implementation of FixedField ********** */
}   // end public class ByteField

