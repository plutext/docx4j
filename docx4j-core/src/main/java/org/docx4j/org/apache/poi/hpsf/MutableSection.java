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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * <p>Adds writing capability to the {@link Section} class.</p>
 *
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link Section} class at a later time, so the API will change.</p>
 */
public class MutableSection extends Section
{
    /**
     * <p>If the "dirty" flag is true, the section's size must be
     * (re-)calculated before the section is written.</p>
     */
    private boolean dirty = true;



    /**
     * <p>List to assemble the properties. Unfortunately a wrong
     * decision has been taken when specifying the "properties" field
     * as an Property[]. It should have been a {@link java.util.List}.</p>
     */
    private List<Property> preprops;



    /**
     * <p>Contains the bytes making out the section. This byte array is
     * established when the section's size is calculated and can be reused
     * later. It is valid only if the "dirty" flag is false.</p>
     */
    private byte[] sectionBytes;



    /**
     * <p>Creates an empty mutable section.</p>
     */
    public MutableSection()
    {
        dirty = true;
        formatID = null;
        offset = -1;
        preprops = new LinkedList<Property>();
    }



    /**
     * <p>Constructs a <code>MutableSection</code> by doing a deep copy of an
     * existing <code>Section</code>. All nested <code>Property</code>
     * instances, will be their mutable counterparts in the new
     * <code>MutableSection</code>.</p>
     *
     * @param s The section set to copy
     */
    public MutableSection(final Section s)
    {
        setFormatID(s.getFormatID());
        final Property[] pa = s.getProperties();
        final MutableProperty[] mpa = new MutableProperty[pa.length];
        for (int i = 0; i < pa.length; i++)
            mpa[i] = new MutableProperty(pa[i]);
        setProperties(mpa);
        setDictionary(s.getDictionary());
    }



    /**
     * <p>Sets the section's format ID.</p>
     *
     * @param formatID The section's format ID
     *
     * @see #setFormatID(byte[])
     * @see Section#getFormatID
     */
    public void setFormatID(final ClassID formatID)
    {
        this.formatID = formatID;
    }



    /**
     * <p>Sets the section's format ID.</p>
     *
     * @param formatID The section's format ID as a byte array. It components
     * are in big-endian format.
     *
     * @see #setFormatID(ClassID)
     * @see Section#getFormatID
     */
    public void setFormatID(final byte[] formatID)
    {
        ClassID fid = getFormatID();
        if (fid == null)
        {
            fid = new ClassID();
            setFormatID(fid);
        }
        fid.setBytes(formatID);
    }



    /**
     * <p>Sets this section's properties. Any former values are overwritten.</p>
     *
     * @param properties This section's new properties.
     */
    public void setProperties(final Property[] properties)
    {
        this.properties = properties;
        preprops = new LinkedList<Property>();
        for (int i = 0; i < properties.length; i++)
            preprops.add(properties[i]);
        dirty = true;
    }



    /**
     * <p>Sets the string value of the property with the specified ID.</p>
     *
     * @param id The property's ID
     * @param value The property's value. It will be written as a Unicode
     * string.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final String value)
    {
        setProperty(id, Variant.VT_LPWSTR, value);
        dirty = true;
    }



    /**
     * <p>Sets the int value of the property with the specified ID.</p>
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final int value)
    {
        setProperty(id, Variant.VT_I4, Integer.valueOf(value));
        dirty = true;
    }



    /**
     * <p>Sets the long value of the property with the specified ID.</p>
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final long value)
    {
        setProperty(id, Variant.VT_I8, Long.valueOf(value));
        dirty = true;
    }



    /**
     * <p>Sets the boolean value of the property with the specified ID.</p>
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final boolean value)
    {
        setProperty(id, Variant.VT_BOOL, Boolean.valueOf(value));
        dirty = true;
    }



    /**
     * <p>Sets the value and the variant type of the property with the
     * specified ID. If a property with this ID is not yet present in
     * the section, it will be added. An already present property with
     * the specified ID will be overwritten. A default mapping will be
     * used to choose the property's type.</p>
     *
     * @param id The property's ID.
     * @param variantType The property's variant type.
     * @param value The property's value.
     *
     * @see #setProperty(int, String)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final int id, final long variantType,
                            final Object value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(id);
        p.setType(variantType);
        p.setValue(value);
        setProperty(p);
        dirty = true;
    }



    /**
     * <p>Sets a property.</p>
     *
     * @param p The property to be set.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final Property p)
    {
        final long id = p.getID();
        removeProperty(id);
        preprops.add(p);
        dirty = true;
    }



    /**
     * <p>Removes a property.</p>
     *
     * @param id The ID of the property to be removed
     */
    public void removeProperty(final long id)
    {
        for (final Iterator<Property> i = preprops.iterator(); i.hasNext();)
            if (i.next().getID() == id)
            {
                i.remove();
                break;
            }
        dirty = true;
    }



    /**
     * <p>Sets the value of the boolean property with the specified
     * ID.</p>
     *
     * @param id The property's ID
     * @param value The property's value
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     * @see Variant
     */
    protected void setPropertyBooleanValue(final int id, final boolean value)
    {
        setProperty(id, Variant.VT_BOOL, Boolean.valueOf(value));
    }



    /**
     * <p>Returns the section's size.</p>
     *
     * @return the section's size.
     */
    public int getSize()
    {
        if (dirty)
        {
            try
            {
                size = calcSize();
                dirty = false;
            }
            catch (HPSFRuntimeException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new HPSFRuntimeException(ex);
            }
        }
        return size;
    }



    /**
     * <p>Calculates the section's size. It is the sum of the lengths of the
     * section's header (8), the properties list (16 times the number of
     * properties) and the properties themselves.</p>
     *
     * @return the section's length in bytes.
     * @throws WritingNotSupportedException
     * @throws IOException
     */
    private int calcSize() throws WritingNotSupportedException, IOException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out);
        out.close();
        /* Pad to multiple of 4 bytes so that even the Windows shell (explorer)
         * shows custom properties. */
        sectionBytes = Util.pad4(out.toByteArray());
        return sectionBytes.length;
    }



    /**
     * <p>Writes this section into an output stream.</p>
     *
     * <p>Internally this is done by writing into three byte array output
     * streams: one for the properties, one for the property list and one for
     * the section as such. The two former are appended to the latter when they
     * have received all their data.</p>
     *
     * @param out The stream to write into.
     *
     * @return The number of bytes written, i.e. the section's size.
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public int write(final OutputStream out)
        throws WritingNotSupportedException, IOException
    {
        /* Check whether we have already generated the bytes making out the
         * section. */
        if (!dirty && sectionBytes != null)
        {
            out.write(sectionBytes);
            return sectionBytes.length;
        }

        /* The properties are written to this stream. */
        final ByteArrayOutputStream propertyStream =
            new ByteArrayOutputStream();

        /* The property list is established here. After each property that has
         * been written to "propertyStream", a property list entry is written to
         * "propertyListStream". */
        final ByteArrayOutputStream propertyListStream =
            new ByteArrayOutputStream();

        /* Maintain the current position in the list. */
        int position = 0;

        /* Increase the position variable by the size of the property list so
         * that it points behind the property list and to the beginning of the
         * properties themselves. */
        position += 2 * LittleEndian.INT_SIZE +
                    getPropertyCount() * 2 * LittleEndian.INT_SIZE;

        /* Writing the section's dictionary it tricky. If there is a dictionary
         * (property 0) the codepage property (property 1) must be set, too. */
        int codepage = -1;
        if (getProperty(PropertyIDMap.PID_DICTIONARY) != null)
        {
            final Object p1 = getProperty(PropertyIDMap.PID_CODEPAGE);
            if (p1 != null)
            {
                if (!(p1 instanceof Integer))
                    throw new IllegalPropertySetDataException
                        ("The codepage property (ID = 1) must be an " +
                         "Integer object.");
            }
            else
                /* Warning: The codepage property is not set although a
                 * dictionary is present. In order to cope with this problem we
                 * add the codepage property and set it to Unicode. */
                setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                            Integer.valueOf(CodePageUtil.CP_UNICODE));
            codepage = getCodepage();
        }

        /* Sort the property list by their property IDs: */
        Collections.sort(preprops, new Comparator<Property>()
            {
                public int compare(final Property p1, final Property p2)
                {
                    if (p1.getID() < p2.getID())
                        return -1;
                    else if (p1.getID() == p2.getID())
                        return 0;
                    else
                        return 1;
                }
            });

        /* Write the properties and the property list into their respective
         * streams: */
        for (final ListIterator<Property> i = preprops.listIterator(); i.hasNext();)
        {
            final MutableProperty p = (MutableProperty) i.next();
            final long id = p.getID();

            /* Write the property list entry. */
            TypeWriter.writeUIntToStream(propertyListStream, p.getID());
            TypeWriter.writeUIntToStream(propertyListStream, position);

            /* If the property ID is not equal 0 we write the property and all
             * is fine. However, if it equals 0 we have to write the section's
             * dictionary which has an implicit type only and an explicit
             * value. */
            if (id != 0)
                /* Write the property and update the position to the next
                 * property. */
                position += p.write(propertyStream, getCodepage());
            else
            {
                if (codepage == -1)
                    throw new IllegalPropertySetDataException
                        ("Codepage (property 1) is undefined.");
                position += writeDictionary(propertyStream, dictionary,
                                            codepage);
            }
        }
        propertyStream.close();
        propertyListStream.close();

        /* Write the section: */
        byte[] pb1 = propertyListStream.toByteArray();
        byte[] pb2 = propertyStream.toByteArray();

        /* Write the section's length: */
        TypeWriter.writeToStream(out, LittleEndian.INT_SIZE * 2 +
                                      pb1.length + pb2.length);

        /* Write the section's number of properties: */
        TypeWriter.writeToStream(out, getPropertyCount());

        /* Write the property list: */
        out.write(pb1);

        /* Write the properties: */
        out.write(pb2);

        int streamLength = LittleEndian.INT_SIZE * 2 + pb1.length + pb2.length;
        return streamLength;
    }



    /**
     * <p>Writes the section's dictionary.</p>
     *
     * @param out The output stream to write to.
     * @param dictionary The dictionary.
     * @param codepage The codepage to be used to write the dictionary items.
     * @return The number of bytes written
     * @exception IOException if an I/O exception occurs.
     */
    private static int writeDictionary(final OutputStream out,
                                       final Map<Long,String> dictionary, final int codepage)
        throws IOException
    {
        int length = TypeWriter.writeUIntToStream(out, dictionary.size());
        for (final Iterator<Long> i = dictionary.keySet().iterator(); i.hasNext();)
        {
            final Long key = i.next();
            final String value = dictionary.get(key);

            if (codepage == CodePageUtil.CP_UNICODE)
            {
                /* Write the dictionary item in Unicode. */
                int sLength = value.length() + 1;
                if (sLength % 2 == 1)
                    sLength++;
                length += TypeWriter.writeUIntToStream(out, key.longValue());
                length += TypeWriter.writeUIntToStream(out, sLength);
                final byte[] ca = CodePageUtil.getBytesInCodePage(value, codepage);
                for (int j = 2; j < ca.length; j += 2)
                {
                    out.write(ca[j+1]);
                    out.write(ca[j]);
                    length += 2;
                }
                sLength -= value.length();
                while (sLength > 0)
                {
                    out.write(0x00);
                    out.write(0x00);
                    length += 2;
                    sLength--;
                }
            }
            else
            {
                /* Write the dictionary item in another codepage than
                 * Unicode. */
                length += TypeWriter.writeUIntToStream(out, key.longValue());
                length += TypeWriter.writeUIntToStream(out, value.length() + 1);
                final byte[] ba = CodePageUtil.getBytesInCodePage(value, codepage);
                for (int j = 0; j < ba.length; j++)
                {
                    out.write(ba[j]);
                    length++;
                }
                out.write(0x00);
                length++;
            }
        }
        return length;
    }



    /**
     * <p>Overwrites the super class' method to cope with a redundancy:
     * the property count is maintained in a separate member variable, but
     * shouldn't.</p>
     *
     * @return The number of properties in this section
     */
    public int getPropertyCount()
    {
        return preprops.size();
    }



    /**
     * <p>Gets this section's properties.</p>
     *
     * @return this section's properties.
     */
    public Property[] getProperties()
    {
        properties = preprops.toArray(new Property[0]);
        return properties;
    }



    /**
     * <p>Gets a property.</p>
     *
     * @param id The ID of the property to get
     * @return The property or <code>null</code> if there is no such property
     */
    public Object getProperty(final long id)
    {
        /* Calling getProperties() ensures that properties and preprops are in
         * sync.</p> */
        getProperties();
        return super.getProperty(id);
    }



    /**
     * <p>Sets the section's dictionary. All keys in the dictionary must be
     * {@link java.lang.Long} instances, all values must be
     * {@link java.lang.String}s. This method overwrites the properties with IDs
     * 0 and 1 since they are reserved for the dictionary and the dictionary's
     * codepage. Setting these properties explicitly might have surprising
     * effects. An application should never do this but always use this
     * method.</p>
     *
     * @param dictionary The dictionary
     *
     * @exception IllegalPropertySetDataException if the dictionary's key and
     * value types are not correct.
     *
     * @see Section#getDictionary()
     */
    public void setDictionary(final Map<Long,String> dictionary)
        throws IllegalPropertySetDataException
    {
        if (dictionary != null)
        {
            this.dictionary = dictionary;

            /* Set the dictionary property (ID 0). Please note that the second
             * parameter in the method call below is unused because dictionaries
             * don't have a type. */
            setProperty(PropertyIDMap.PID_DICTIONARY, -1, dictionary);

            /* If the codepage property (ID 1) for the strings (keys and
             * values) used in the dictionary is not yet defined, set it to
             * Unicode. */
            final Integer codepage =
                (Integer) getProperty(PropertyIDMap.PID_CODEPAGE);
            if (codepage == null)
                setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                            Integer.valueOf(CodePageUtil.CP_UNICODE));
        }
        else
            /* Setting the dictionary to null means to remove property 0.
             * However, it does not mean to remove property 1 (codepage). */
            removeProperty(PropertyIDMap.PID_DICTIONARY);
    }



    /**
     * <p>Sets a property.</p>
     *
     * @param id The property ID.
     * @param value The property's value. The value's class must be one of those
     *        supported by HPSF.
     */
    public void setProperty(final int id, final Object value)
    {
        if (value instanceof String)
            setProperty(id, (String) value);
        else if (value instanceof Long)
            setProperty(id, ((Long) value).longValue());
        else if (value instanceof Integer)
            setProperty(id, ((Integer) value).intValue());
        else if (value instanceof Short)
            setProperty(id, ((Short) value).intValue());
        else if (value instanceof Boolean)
            setProperty(id, ((Boolean) value).booleanValue());
        else if (value instanceof Date)
            setProperty(id, Variant.VT_FILETIME, value);
        else
            throw new HPSFRuntimeException(
                    "HPSF does not support properties of type " +
                    value.getClass().getName() + ".");
    }



    /**
     * <p>Removes all properties from the section including 0 (dictionary) and
     * 1 (codepage).</p>
     */
    public void clear()
    {
        final Property[] properties = getProperties();
        for (int i = 0; i < properties.length; i++)
        {
            final Property p = properties[i];
            removeProperty(p.getID());
        }
    }

    /**
     * <p>Sets the codepage.</p>
     *
     * @param codepage the codepage
     */
    public void setCodepage(final int codepage)
    {
        setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                Integer.valueOf(codepage));
    }
}
