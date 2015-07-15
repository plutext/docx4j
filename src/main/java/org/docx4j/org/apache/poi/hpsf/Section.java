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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * <p>Represents a section in a {@link PropertySet}.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @author Drew Varner (Drew.Varner allUpIn sc.edu)
 */
public class Section
{

    /**
     * <p>Maps property IDs to section-private PID strings. These
     * strings can be found in the property with ID 0.</p>
     */
    protected Map<Long,String> dictionary;

    /**
     * <p>The section's format ID, {@link #getFormatID}.</p>
     */
    protected ClassID formatID;


    /**
     * <p>Returns the format ID. The format ID is the "type" of the
     * section. For example, if the format ID of the first {@link
     * Section} contains the bytes specified by
     * <code>org.docx4j.org.apache.poi.hpsf.wellknown.SectionIDMap.SUMMARY_INFORMATION_ID</code>
     * the section (and thus the property set) is a SummaryInformation.</p>
     *
     * @return The format ID
     */
    public ClassID getFormatID()
    {
        return formatID;
    }



    /**
     * @see #getOffset
     */
    protected long offset;


    /**
     * <p>Returns the offset of the section in the stream.</p>
     *
     * @return The offset of the section in the stream.
     */
    public long getOffset()
    {
        return offset;
    }



    /**
     * @see #getSize
     */
    protected int size;


    /**
     * <p>Returns the section's size in bytes.</p>
     *
     * @return The section's size in bytes.
     */
    public int getSize()
    {
        return size;
    }



    /**
     * <p>Returns the number of properties in this section.</p>
     *
     * @return The number of properties in this section.
     */
    public int getPropertyCount()
    {
        return properties.length;
    }



    /**
     * @see #getProperties
     */
    protected Property[] properties;


    /**
     * <p>Returns this section's properties.</p>
     *
     * @return This section's properties.
     */
    public Property[] getProperties()
    {
        return properties;
    }



    /**
     * <p>Creates an empty and uninitialized {@link Section}.
     */
    protected Section()
    { }



    /**
     * <p>Creates a {@link Section} instance from a byte array.</p>
     *
     * @param src Contains the complete property set stream.
     * @param offset The position in the stream that points to the
     * section's format ID.
     *
     * @exception UnsupportedEncodingException if the section's codepage is not
     * supported.
     */
    public Section(final byte[] src, final int offset)
    throws UnsupportedEncodingException
    {
        int o1 = offset;

        /*
         * Read the format ID.
         */
        formatID = new ClassID(src, o1);
        o1 += ClassID.LENGTH;

        /*
         * Read the offset from the stream's start and positions to
         * the section header.
         */
        this.offset = LittleEndian.getUInt(src, o1);
        o1 = (int) this.offset;

        /*
         * Read the section length.
         */
        size = (int) LittleEndian.getUInt(src, o1);
        o1 += LittleEndian.INT_SIZE;

        /*
         * Read the number of properties.
         */
        final int propertyCount = (int) LittleEndian.getUInt(src, o1);
        o1 += LittleEndian.INT_SIZE;

        /*
         * Read the properties. The offset is positioned at the first
         * entry of the property list. There are two problems:
         *
         * 1. For each property we have to find out its length. In the
         *    property list we find each property's ID and its offset relative
         *    to the section's beginning. Unfortunately the properties in the
         *    property list need not to be in ascending order, so it is not
         *    possible to calculate the length as
         *    (offset of property(i+1) - offset of property(i)). Before we can
         *    that we first have to sort the property list by ascending offsets.
         *
         * 2. We have to read the property with ID 1 before we read other
         *    properties, at least before other properties containing strings.
         *    The reason is that property 1 specifies the codepage. If it is
         *    1200, all strings are in Unicode. In other words: Before we can
         *    read any strings we have to know whether they are in Unicode or
         *    not. Unfortunately property 1 is not guaranteed to be the first in
         *    a section.
         *
         *    The algorithm below reads the properties in two passes: The first
         *    one looks for property ID 1 and extracts the codepage number. The
         *    seconds pass reads the other properties.
         */
        properties = new Property[propertyCount];

        /* Pass 1: Read the property list. */
        int pass1Offset = o1;
        final List<PropertyListEntry> propertyList = new ArrayList<PropertyListEntry>(propertyCount);
        PropertyListEntry ple;
        for (int i = 0; i < properties.length; i++)
        {
            ple = new PropertyListEntry();

            /* Read the property ID. */
            ple.id = (int) LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Offset from the section's start. */
            ple.offset = (int) LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Add the entry to the property list. */
            propertyList.add(ple);
        }

        /* Sort the property list by ascending offsets: */
        Collections.sort(propertyList);

        /* Calculate the properties' lengths. */
        for (int i = 0; i < propertyCount - 1; i++)
        {
            PropertyListEntry ple1 = propertyList.get(i);
            PropertyListEntry ple2 = propertyList.get(i + 1);
            ple1.length = ple2.offset - ple1.offset;
        }
        if (propertyCount > 0)
        {
            ple = propertyList.get(propertyCount - 1);
            ple.length = size - ple.offset;
        }

        /* Look for the codepage. */
        int codepage = -1;
        for (final Iterator<PropertyListEntry> i = propertyList.iterator();
             codepage == -1 && i.hasNext();)
        {
            ple = i.next();

            /* Read the codepage if the property ID is 1. */
            if (ple.id == PropertyIDMap.PID_CODEPAGE)
            {
                /* Read the property's value type. It must be
                 * VT_I2. */
                int o = (int) (this.offset + ple.offset);
                final long type = LittleEndian.getUInt(src, o);
                o += LittleEndian.INT_SIZE;

                if (type != Variant.VT_I2)
                    throw new HPSFRuntimeException
                        ("Value type of property ID 1 is not VT_I2 but " +
                         type + ".");

                /* Read the codepage number. */
                codepage = LittleEndian.getUShort(src, o);
            }
        }

        /* Pass 2: Read all properties - including the codepage property,
         * if available. */
        int i1 = 0;
        for (final Iterator<PropertyListEntry> i = propertyList.iterator(); i.hasNext();)
        {
            ple = i.next();
            Property p = new Property(ple.id, src,
                    this.offset + ple.offset,
                    ple.length, codepage);
            if (p.getID() == PropertyIDMap.PID_CODEPAGE)
                p = new Property(p.getID(), p.getType(), Integer.valueOf(codepage));
            properties[i1++] = p;
        }

        /*
         * Extract the dictionary (if available).
         */
        dictionary = (Map) getProperty(0);
    }



    /**
     * <p>Represents an entry in the property list and holds a property's ID and
     * its offset from the section's beginning.</p>
     */
    static class PropertyListEntry implements Comparable<PropertyListEntry>
    {
        int id;
        int offset;
        int length;

        /**
         * <p>Compares this {@link PropertyListEntry} with another one by their
         * offsets. A {@link PropertyListEntry} is "smaller" than another one if
         * its offset from the section's begin is smaller.</p>
         *
         * @see Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(final PropertyListEntry o)
        {
            final int otherOffset = o.offset;
            if (offset < otherOffset)
                return -1;
            else if (offset == otherOffset)
                return 0;
            else
                return 1;
        }

        public String toString()
        {
            final StringBuffer b = new StringBuffer();
            b.append(getClass().getName());
            b.append("[id=");
            b.append(id);
            b.append(", offset=");
            b.append(offset);
            b.append(", length=");
            b.append(length);
            b.append(']');
            return b.toString();
        }
    }



    /**
     * <p>Returns the value of the property with the specified ID. If
     * the property is not available, <code>null</code> is returned
     * and a subsequent call to {@link #wasNull} will return
     * <code>true</code>.</p>
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    public Object getProperty(final long id)
    {
        wasNull = false;
        for (int i = 0; i < properties.length; i++)
            if (id == properties[i].getID())
                return properties[i].getValue();
        wasNull = true;
        return null;
    }



    /**
     * <p>Returns the value of the numeric property with the specified
     * ID. If the property is not available, 0 is returned. A
     * subsequent call to {@link #wasNull} will return
     * <code>true</code> to let the caller distinguish that case from
     * a real property value of 0.</p>
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    protected int getPropertyIntValue(final long id)
    {
        final Number i;
        final Object o = getProperty(id);
        if (o == null)
            return 0;
        if (!(o instanceof Long || o instanceof Integer))
            throw new HPSFRuntimeException
                ("This property is not an integer type, but " +
                 o.getClass().getName() + ".");
        i = (Number) o;
        return i.intValue();
    }



    /**
     * <p>Returns the value of the boolean property with the specified
     * ID. If the property is not available, <code>false</code> is
     * returned. A subsequent call to {@link #wasNull} will return
     * <code>true</code> to let the caller distinguish that case from
     * a real property value of <code>false</code>.</p>
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    protected boolean getPropertyBooleanValue(final int id)
    {
        final Boolean b = (Boolean) getProperty(id);
        if (b == null) {
            return false;
        }
        return b.booleanValue();
        }



    /**
     * <p>This member is <code>true</code> if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else <code>false</code>.</p>
     */
    private boolean wasNull;


    /**
     * <p>Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using {@link
     * #wasNull} the caller can distiguish this case from a property's
     * real value of 0.</p>
     *
     * @return <code>true</code> if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else <code>false</code>.
     */
    public boolean wasNull()
    {
        return wasNull;
    }



    /**
     * <p>Returns the PID string associated with a property ID. The ID
     * is first looked up in the {@link Section}'s private
     * dictionary. If it is not found there, the method calls {@link
     * SectionIDMap#getPIDString}.</p>
     *
     * @param pid The property ID
     *
     * @return The property ID's string value
     */
    public String getPIDString(final long pid)
    {
        String s = null;
        if (dictionary != null)
            s = dictionary.get(Long.valueOf(pid));
        if (s == null)
            s = SectionIDMap.getPIDString(getFormatID().getBytes(), pid);
        if (s == null)
            s = SectionIDMap.UNDEFINED;
        return s;
    }



    /**
     * <p>Checks whether this section is equal to another object. The result is
     * <code>false</code> if one of the the following conditions holds:</p>
     *
     * <ul>
     *
     * <li><p>The other object is not a {@link Section}.</p></li>
     *
     * <li><p>The format IDs of the two sections are not equal.</p></li>
     *
     * <li><p>The sections have a different number of properties. However,
     * properties with ID 1 (codepage) are not counted.</p></li>
     *
     * <li><p>The other object is not a {@link Section}.</p></li>
     *
     * <li><p>The properties have different values. The order of the properties
     * is irrelevant.</p></li>
     *
     * </ul>
     *
     * @param o The object to compare this section with
     * @return <code>true</code> if the objects are equal, <code>false</code> if
     * not
     */
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof Section))
            return false;
        final Section s = (Section) o;
        if (!s.getFormatID().equals(getFormatID()))
            return false;

        /* Compare all properties except 0 and 1 as they must be handled
         * specially. */
        Property[] pa1 = new Property[getProperties().length];
        Property[] pa2 = new Property[s.getProperties().length];
        System.arraycopy(getProperties(), 0, pa1, 0, pa1.length);
        System.arraycopy(s.getProperties(), 0, pa2, 0, pa2.length);

        /* Extract properties 0 and 1 and remove them from the copy of the
         * arrays. */
        Property p10 = null;
        Property p20 = null;
        for (int i = 0; i < pa1.length; i++)
        {
            final long id = pa1[i].getID();
            if (id == 0)
            {
                p10 = pa1[i];
                pa1 = remove(pa1, i);
                i--;
            }
            if (id == 1)
            {
                // p11 = pa1[i];
                pa1 = remove(pa1, i);
                i--;
            }
        }
        for (int i = 0; i < pa2.length; i++)
        {
            final long id = pa2[i].getID();
            if (id == 0)
            {
                p20 = pa2[i];
                pa2 = remove(pa2, i);
                i--;
            }
            if (id == 1)
            {
                // p21 = pa2[i];
                pa2 = remove(pa2, i);
                i--;
            }
        }

        /* If the number of properties (not counting property 1) is unequal the
         * sections are unequal. */
        if (pa1.length != pa2.length)
            return false;

        /* If the dictionaries are unequal the sections are unequal. */
        boolean dictionaryEqual = true;
        if (p10 != null && p20 != null)
            dictionaryEqual = p10.getValue().equals(p20.getValue());
        else if (p10 != null || p20 != null)
            dictionaryEqual = false;
        if (dictionaryEqual) {
            return Util.equals(pa1, pa2);
        }
        return false;
    }



    /**
     * <p>Removes a field from a property array. The resulting array is
     * compactified and returned.</p>
     *
     * @param pa The property array.
     * @param i The index of the field to be removed.
     * @return the compactified array.
     */
    private Property[] remove(final Property[] pa, final int i)
    {
        final Property[] h = new Property[pa.length - 1];
        if (i > 0)
            System.arraycopy(pa, 0, h, 0, i);
        System.arraycopy(pa, i + 1, h, i, h.length - i);
        return h;
    }



    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        long hashCode = 0;
        hashCode += getFormatID().hashCode();
        final Property[] pa = getProperties();
        for (int i = 0; i < pa.length; i++)
            hashCode += pa[i].hashCode();
        final int returnHashCode = (int) (hashCode & 0x0ffffffffL);
        return returnHashCode;
    }



    /**
     * @see Object#toString()
     */
    public String toString()
    {
        final StringBuffer b = new StringBuffer();
        final Property[] pa = getProperties();
        b.append(getClass().getName());
        b.append('[');
        b.append("formatID: ");
        b.append(getFormatID());
        b.append(", offset: ");
        b.append(getOffset());
        b.append(", propertyCount: ");
        b.append(getPropertyCount());
        b.append(", size: ");
        b.append(getSize());
        b.append(", properties: [\n");
        for (int i = 0; i < pa.length; i++)
        {
            b.append(pa[i].toString());
            b.append(",\n");
        }
        b.append(']');
        b.append(']');
        return b.toString();
    }



    /**
     * <p>Gets the section's dictionary. A dictionary allows an application to
     * use human-readable property names instead of numeric property IDs. It
     * contains mappings from property IDs to their associated string
     * values. The dictionary is stored as the property with ID 0. The codepage
     * for the strings in the dictionary is defined by property with ID 1.</p>
     *
     * @return the dictionary or <code>null</code> if the section does not have
     * a dictionary.
     */
    public Map<Long,String> getDictionary()
    {
        return dictionary;
    }



    /**
     * <p>Gets the section's codepage, if any.</p>
     *
     * @return The section's codepage if one is defined, else -1.
     */
    public int getCodepage()
    {
        final Integer codepage =
            (Integer) getProperty(PropertyIDMap.PID_CODEPAGE);
        if (codepage == null)
            return -1;
        int cp = codepage.intValue();
        return cp;
    }

}
