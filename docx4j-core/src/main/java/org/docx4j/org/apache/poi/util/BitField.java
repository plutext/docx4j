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

/**
 * Manage operations dealing with bit-mapped fields.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */

public class BitField
{
    private final int _mask;
    private final int _shift_count;

    /**
     * Create a BitField instance
     *
     * @param mask the mask specifying which bits apply to this
     *             BitField. Bits that are set in this mask are the
     *             bits that this BitField operates on
     */

    public BitField(final int mask)
    {
        _mask = mask;
        int count       = 0;
        int bit_pattern = mask;

        if (bit_pattern != 0)
        {
            while ((bit_pattern & 1) == 0)
            {
                count++;
                bit_pattern >>= 1;
            }
        }
        _shift_count = count;
    }

    /**
     * Obtain the value for the specified BitField, appropriately
     * shifted right. Many users of a BitField will want to treat the
     * specified bits as an int value, and will not want to be aware
     * that the value is stored as a BitField (and so shifted left so
     * many bits)
     *
     * @param holder the int data containing the bits we're interested
     *               in
     *
     * @return the selected bits, shifted right appropriately
     */

    public int getValue(final int holder)
    {
        return getRawValue(holder) >>> _shift_count;
    }

    /**
     * Obtain the value for the specified BitField, appropriately
     * shifted right, as a short. Many users of a BitField will want
     * to treat the specified bits as an int value, and will not want
     * to be aware that the value is stored as a BitField (and so
     * shifted left so many bits)
     *
     * @param holder the short data containing the bits we're
     *               interested in
     *
     * @return the selected bits, shifted right appropriately
     */

    public short getShortValue(final short holder)
    {
        return ( short ) getValue(holder);
    }

    /**
     * Obtain the value for the specified BitField, unshifted
     *
     * @param holder the int data containing the bits we're interested
     *               in
     *
     * @return the selected bits
     */

    public int getRawValue(final int holder)
    {
        return (holder & _mask);
    }

    /**
     * Obtain the value for the specified BitField, unshifted
     *
     * @param holder the short data containing the bits we're
     *               interested in
     *
     * @return the selected bits
     */

    public short getShortRawValue(final short holder)
    {
        return ( short ) getRawValue(holder);
    }

    /**
     * Is the field set or not? This is most commonly used for a
     * single-bit field, which is often used to represent a boolean
     * value; the results of using it for a multi-bit field is to
     * determine whether *any* of its bits are set
     *
     * @param holder the int data containing the bits we're interested
     *               in
     *
     * @return true if any of the bits are set, else false
     */

    public boolean isSet(final int holder)
    {
        return (holder & _mask) != 0;
    }

    /**
     * Are all of the bits set or not? This is a stricter test than
     * isSet, in that all of the bits in a multi-bit set must be set
     * for this method to return true
     *
     * @param holder the int data containing the bits we're interested
     *               in
     *
     * @return true if all of the bits are set, else false
     */

    public boolean isAllSet(final int holder)
    {
        return (holder & _mask) == _mask;
    }

    /**
     * Replace the bits with new values.
     *
     * @param holder the int data containint the bits we're interested
     *               in
     * @param value the new value for the specified bits
     *
     * @return the value of holder with the bits from the value
     *         parameter replacing the old bits
     */

    public int setValue(final int holder, final int value)
    {
        return (holder & ~_mask) | ((value << _shift_count) & _mask);
    }

    /**
     * Replace the bits with new values.
     *
     * @param holder the short data containing the bits we're
     *               interested in
     * @param value the new value for the specified bits
     *
     * @return the value of holder with the bits from the value
     *         parameter replacing the old bits
     */

    public short setShortValue(final short holder, final short value)
    {
        return ( short ) setValue(holder, value);
    }

    /**
     * Clear the bits.
     *
     * @param holder the int data containing the bits we're interested
     *               in
     *
     * @return the value of holder with the specified bits cleared
     *         (set to 0)
     */

    public int clear(final int holder)
    {
        return holder & ~_mask;
    }

    /**
     * Clear the bits.
     *
     * @param holder the short data containing the bits we're
     *               interested in
     *
     * @return the value of holder with the specified bits cleared
     *         (set to 0)
     */

    public short clearShort(final short holder)
    {
        return ( short ) clear(holder);
    }

    /**
     * Clear the bits.
     *
     * @param holder the byte data containing the bits we're
     *               interested in
     *
     * @return the value of holder with the specified bits cleared
     *         (set to 0)
     */

    public byte clearByte(final byte holder)
    {
        return ( byte ) clear(holder);
    }

    /**
     * Set the bits.
     *
     * @param holder the int data containing the bits we're interested
     *               in
     *
     * @return the value of holder with the specified bits set to 1
     */

    public int set(final int holder)
    {
        return holder | _mask;
    }

    /**
     * Set the bits.
     *
     * @param holder the short data containing the bits we're
     *               interested in
     *
     * @return the value of holder with the specified bits set to 1
     */

    public short setShort(final short holder)
    {
        return ( short ) set(holder);
    }

    /**
     * Set the bits.
     *
     * @param holder the byte data containing the bits we're
     *               interested in
     *
     * @return the value of holder with the specified bits set to 1
     */

    public byte setByte(final byte holder)
    {
        return ( byte ) set(holder);
    }

    /**
     * Set a boolean BitField
     *
     * @param holder the int data containing the bits we're interested
     *               in
     * @param flag indicating whether to set or clear the bits
     *
     * @return the value of holder with the specified bits set or
     *         cleared
     */

    public int setBoolean(final int holder, final boolean flag)
    {
        return flag ? set(holder)
                    : clear(holder);
    }

    /**
     * Set a boolean BitField
     *
     * @param holder the short data containing the bits we're
     *               interested in
     * @param flag indicating whether to set or clear the bits
     *
     * @return the value of holder with the specified bits set or
     *         cleared
     */

    public short setShortBoolean(final short holder, final boolean flag)
    {
        return flag ? setShort(holder)
                    : clearShort(holder);
    }

    /**
     * Set a boolean BitField
     *
     * @param holder the byte data containing the bits we're
     *               interested in
     * @param flag indicating whether to set or clear the bits
     *
     * @return the value of holder with the specified bits set or
     *         cleared
     */

    public byte setByteBoolean(final byte holder, final boolean flag)
    {
        return flag ? setByte(holder)
                    : clearByte(holder);
    }
}   // end public class BitField

