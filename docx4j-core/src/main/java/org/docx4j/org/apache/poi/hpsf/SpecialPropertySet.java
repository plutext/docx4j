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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * <p>Abstract superclass for the convenience classes {@link
 * SummaryInformation} and {@link DocumentSummaryInformation}.</p>
 *
 * <p>The motivation behind this class is quite nasty if you look
 * behind the scenes, but it serves the application programmer well by
 * providing him with the easy-to-use {@link SummaryInformation} and
 * {@link DocumentSummaryInformation} classes. When parsing the data a
 * property set stream consists of (possibly coming from an {@link
 * java.io.InputStream}) we want to read and process each byte only
 * once. Since we don't know in advance which kind of property set we
 * have, we can expect only the most general {@link
 * PropertySet}. Creating a special subclass should be as easy as
 * calling the special subclass' constructor and pass the general
 * {@link PropertySet} in. To make things easy internally, the special
 * class just holds a reference to the general {@link PropertySet} and
 * delegates all method calls to it.</p>
 *
 * <p>A cleaner implementation would have been like this: The {@link
 * PropertySetFactory} parses the stream data into some internal
 * object first.  Then it finds out whether the stream is a {@link
 * SummaryInformation}, a {@link DocumentSummaryInformation} or a
 * general {@link PropertySet}.  However, the current implementation
 * went the other way round historically: the convenience classes came
 * only late to my mind.</p>
 */
public abstract class SpecialPropertySet extends MutablePropertySet
{
    /**
     * The id to name mapping of the properties
     *  in this set.
     */
    public abstract PropertyIDMap getPropertySetIDMap();

    /**
     * <p>The "real" property set <code>SpecialPropertySet</code>
     * delegates to.</p>
     */
    private MutablePropertySet delegate;



    /**
     * <p>Creates a <code>SpecialPropertySet</code>.
     *
     * @param ps The property set to be encapsulated by the
     * <code>SpecialPropertySet</code>
     */
    public SpecialPropertySet(final PropertySet ps)
    {
        delegate = new MutablePropertySet(ps);
    }



    /**
     * <p>Creates a <code>SpecialPropertySet</code>.
     *
     * @param ps The mutable property set to be encapsulated by the
     * <code>SpecialPropertySet</code>
     */
    public SpecialPropertySet(final MutablePropertySet ps)
    {
        delegate = ps;
    }



    /**
     * @see PropertySet#getByteOrder
     */
    public int getByteOrder()
    {
        return delegate.getByteOrder();
    }



    /**
     * @see PropertySet#getFormat
     */
    public int getFormat()
    {
        return delegate.getFormat();
    }



    /**
     * @see PropertySet#getOSVersion
     */
    public int getOSVersion()
    {
        return delegate.getOSVersion();
    }



    /**
     * @see PropertySet#getClassID
     */
    public ClassID getClassID()
    {
        return delegate.getClassID();
    }



    /**
     * @see PropertySet#getSectionCount
     */
    public int getSectionCount()
    {
        return delegate.getSectionCount();
    }



    /**
     * @see PropertySet#getSections
     */
    public List<Section> getSections()
    {
        return delegate.getSections();
    }



    /**
     * @see PropertySet#isSummaryInformation
     */
    public boolean isSummaryInformation()
    {
        return delegate.isSummaryInformation();
    }



    /**
     * @see PropertySet#isDocumentSummaryInformation
     */
    public boolean isDocumentSummaryInformation()
    {
        return delegate.isDocumentSummaryInformation();
    }



    /**
     * @see PropertySet#getSingleSection
     */
    public Section getFirstSection()
    {
        return delegate.getFirstSection();
    }


    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#addSection(org.docx4j.org.apache.poi.hpsf.Section)
     */
    public void addSection(final Section section)
    {
        delegate.addSection(section);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#clearSections()
     */
    public void clearSections()
    {
        delegate.clearSections();
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#setByteOrder(int)
     */
    public void setByteOrder(final int byteOrder)
    {
        delegate.setByteOrder(byteOrder);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#setClassID(org.docx4j.org.apache.poi.hpsf.ClassID)
     */
    public void setClassID(final ClassID classID)
    {
        delegate.setClassID(classID);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#setFormat(int)
     */
    public void setFormat(final int format)
    {
        delegate.setFormat(format);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#setOSVersion(int)
     */
    public void setOSVersion(final int osVersion)
    {
        delegate.setOSVersion(osVersion);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#toInputStream()
     */
    public InputStream toInputStream() throws IOException, WritingNotSupportedException
    {
        return delegate.toInputStream();
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#write(org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry, java.lang.String)
     */
    public void write(final DirectoryEntry dir, final String name) throws WritingNotSupportedException, IOException
    {
        delegate.write(dir, name);
    }

    /**
     * @see org.docx4j.org.apache.poi.hpsf.MutablePropertySet#write(java.io.OutputStream)
     */
    public void write(final OutputStream out) throws WritingNotSupportedException, IOException
    {
        delegate.write(out);
    }

    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#equals(java.lang.Object)
     */
    public boolean equals(final Object o)
    {
        return delegate.equals(o);
    }

    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#getProperties()
     */
    public Property[] getProperties() throws NoSingleSectionException
    {
        return delegate.getProperties();
    }

    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#getProperty(int)
     */
    protected Object getProperty(final int id) throws NoSingleSectionException
    {
        return delegate.getProperty(id);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#getPropertyBooleanValue(int)
     */
    protected boolean getPropertyBooleanValue(final int id) throws NoSingleSectionException
    {
        return delegate.getPropertyBooleanValue(id);
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#getPropertyIntValue(int)
     */
    protected int getPropertyIntValue(final int id) throws NoSingleSectionException
    {
        return delegate.getPropertyIntValue(id);
    }


    
    /**
     * Fetches the property with the given ID, then does its
     *  best to return it as a String
     * @return The property as a String, or null if unavailable
     */
    protected String getPropertyStringValue(final int propertyId) {
        Object propertyValue = getProperty(propertyId);
        return getPropertyStringValue(propertyValue);
    }
    protected static String getPropertyStringValue(final Object propertyValue) {
        // Normal cases
        if (propertyValue == null) return null;
        if (propertyValue instanceof String) return (String)propertyValue;
        
        // Do our best with some edge cases
        if (propertyValue instanceof byte[]) {
            byte[] b = (byte[])propertyValue;
            if (b.length == 0) {
                return "";
            }
            if (b.length == 1) {
                return Byte.toString(b[0]);
            }
            if (b.length == 2) {
                return Integer.toString( LittleEndian.getUShort(b) );
            }
            if (b.length == 4) {
                return Long.toString( LittleEndian.getUInt(b) );
            }
            // Maybe it's a string? who knows!
            return new String(b);
        }
        return propertyValue.toString();
    }


    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#hashCode()
     */
    public int hashCode()
    {
        return delegate.hashCode();
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#toString()
     */
    public String toString()
    {
        return delegate.toString();
    }



    /**
     * @see org.docx4j.org.apache.poi.hpsf.PropertySet#wasNull()
     */
    public boolean wasNull() throws NoSingleSectionException
    {
        return delegate.wasNull();
    }

}
