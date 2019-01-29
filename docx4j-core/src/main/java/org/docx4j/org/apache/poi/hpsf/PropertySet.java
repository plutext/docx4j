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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * <p>Represents a property set in the Horrible Property Set Format
 * (HPSF). These are usually metadata of a Microsoft Office
 * document.</p>
 *
 * <p>An application that wants to access these metadata should create
 * an instance of this class or one of its subclasses by calling the
 * factory method {@link PropertySetFactory#create} and then retrieve
 * the information its needs by calling appropriate methods.</p>
 *
 * <p>{@link PropertySetFactory#create} does its work by calling one
 * of the constructors {@link PropertySet#PropertySet(InputStream)} or
 * {@link PropertySet#PropertySet(byte[])}. If the constructor's
 * argument is not in the Horrible Property Set Format, i.e. not a
 * property set stream, or if any other error occurs, an appropriate
 * exception is thrown.</p>
 *
 * <p>A {@link PropertySet} has a list of {@link Section}s, and each
 * {@link Section} has a {@link Property} array. Use {@link
 * #getSections} to retrieve the {@link Section}s, then call {@link
 * Section#getProperties} for each {@link Section} to get hold of the
 * {@link Property} arrays.</p> Since the vast majority of {@link
 * PropertySet}s contains only a single {@link Section}, the
 * convenience method {@link #getProperties} returns the properties of
 * a {@link PropertySet}'s {@link Section} (throwing a {@link
 * NoSingleSectionException} if the {@link PropertySet} contains more
 * (or less) than exactly one {@link Section}).</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @author Drew Varner (Drew.Varner hanginIn sc.edu)
 */
public class PropertySet
{

    /**
     * <p>The "byteOrder" field must equal this value.</p>
     */
    static final byte[] BYTE_ORDER_ASSERTION =
        new byte[] {(byte) 0xFE, (byte) 0xFF};

    /**
     * <p>Specifies this {@link PropertySet}'s byte order. See the
     * HPFS documentation for details!</p>
     */
    protected int byteOrder;

    /**
     * <p>Returns the property set stream's low-level "byte order"
     * field. It is always <tt>0xFFFE</tt> .</p>
     *
     * @return The property set stream's low-level "byte order" field.
     */
    public int getByteOrder()
    {
        return byteOrder;
    }



    /**
     * <p>The "format" field must equal this value.</p>
     */
    static final byte[] FORMAT_ASSERTION =
        new byte[]{(byte) 0x00, (byte) 0x00};

    /**
     * <p>Specifies this {@link PropertySet}'s format. See the HPFS
     * documentation for details!</p>
     */
    protected int format;

    /**
     * <p>Returns the property set stream's low-level "format"
     * field. It is always <tt>0x0000</tt> .</p>
     *
     * @return The property set stream's low-level "format" field.
     */
    public int getFormat()
    {
        return format;
    }


 
    /**
     * <p>Specifies the version of the operating system that created
     * this {@link PropertySet}. See the HPFS documentation for
     * details!</p>
     */
    protected int osVersion;


    /**
     * <p>If the OS version field holds this value the property set stream was
     * created on a 16-bit Windows system.</p>
     */
    public static final int OS_WIN16     = 0x0000;

    /**
     * <p>If the OS version field holds this value the property set stream was
     * created on a Macintosh system.</p>
     */
    public static final int OS_MACINTOSH = 0x0001;

    /**
     * <p>If the OS version field holds this value the property set stream was
     * created on a 32-bit Windows system.</p>
     */
    public static final int OS_WIN32     = 0x0002;

    /**
     * <p>Returns the property set stream's low-level "OS version"
     * field.</p>
     *
     * @return The property set stream's low-level "OS version" field.
     */
    public int getOSVersion()
    {
        return osVersion;
    }



    /**
     * <p>Specifies this {@link PropertySet}'s "classID" field. See
     * the HPFS documentation for details!</p>
     */
    protected ClassID classID;

    /**
     * <p>Returns the property set stream's low-level "class ID"
     * field.</p>
     *
     * @return The property set stream's low-level "class ID" field.
     */
    public ClassID getClassID()
    {
        return classID;
    }



    /**
     * <p>Returns the number of {@link Section}s in the property
     * set.</p>
     *
     * @return The number of {@link Section}s in the property set.
     */
    public int getSectionCount()
    {
        return sections.size();
    }



    /**
     * <p>The sections in this {@link PropertySet}.</p>
     */
    protected List<Section> sections;

    /**
     * <p>Returns the {@link Section}s in the property set.</p>
     *
     * @return The {@link Section}s in the property set.
     */
    public List<Section> getSections()
    {
        return sections;
    }



    /**
     * <p>Creates an empty (uninitialized) {@link PropertySet}.</p>
     *
     * <p><strong>Please note:</strong> For the time being this
     * constructor is protected since it is used for internal purposes
     * only, but expect it to become public once the property set's
     * writing functionality is implemented.</p>
     */
    protected PropertySet()
    { }



    /**
     * <p>Creates a {@link PropertySet} instance from an {@link
     * InputStream} in the Horrible Property Set Format.</p>
     *
     * <p>The constructor reads the first few bytes from the stream
     * and determines whether it is really a property set stream. If
     * it is, it parses the rest of the stream. If it is not, it
     * resets the stream to its beginning in order to let other
     * components mess around with the data and throws an
     * exception.</p>
     *
     * @param stream Holds the data making out the property set
     * stream.
     * @throws MarkUnsupportedException if the stream does not support
     * the {@link InputStream#markSupported} method.
     * @throws IOException if the {@link InputStream} cannot not be
     * accessed as needed.
     * @exception NoPropertySetStreamException if the input stream does not
     * contain a property set.
     * @exception UnsupportedEncodingException if a character encoding is not
     * supported.
     */
    public PropertySet(final InputStream stream)
        throws NoPropertySetStreamException, MarkUnsupportedException,
               IOException, UnsupportedEncodingException
    {
        if (isPropertySetStream(stream))
        {
            final int avail = stream.available();
            final byte[] buffer = new byte[avail];
            stream.read(buffer, 0, buffer.length);
            init(buffer, 0, buffer.length);
        }
        else
            throw new NoPropertySetStreamException();
    }



    /**
     * <p>Creates a {@link PropertySet} instance from a byte array
     * that represents a stream in the Horrible Property Set
     * Format.</p>
     *
     * @param stream The byte array holding the stream data.
     * @param offset The offset in <var>stream</var> where the stream
     * data begin. If the stream data begin with the first byte in the
     * array, the <var>offset</var> is 0.
     * @param length The length of the stream data.
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     * 
     * @exception UnsupportedEncodingException if the codepage is not supported.
     */
    public PropertySet(final byte[] stream, final int offset, final int length)
        throws NoPropertySetStreamException, UnsupportedEncodingException
    {
        if (isPropertySetStream(stream, offset, length))
            init(stream, offset, length);
        else
            throw new NoPropertySetStreamException();
    }



    /**
     * <p>Creates a {@link PropertySet} instance from a byte array
     * that represents a stream in the Horrible Property Set
     * Format.</p>
     *
     * @param stream The byte array holding the stream data. The
     * complete byte array contents is the stream data.
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     * 
     * @exception UnsupportedEncodingException if the codepage is not supported.
     */
    public PropertySet(final byte[] stream)
    throws NoPropertySetStreamException, UnsupportedEncodingException
    {
        this(stream, 0, stream.length);
    }



    /**
     * <p>Checks whether an {@link InputStream} is in the Horrible
     * Property Set Format.</p>
     *
     * @param stream The {@link InputStream} to check. In order to
     * perform the check, the method reads the first bytes from the
     * stream. After reading, the stream is reset to the position it
     * had before reading. The {@link InputStream} must support the
     * {@link InputStream#mark} method.
     * @return <code>true</code> if the stream is a property set
     * stream, else <code>false</code>.
     * @throws MarkUnsupportedException if the {@link InputStream}
     * does not support the {@link InputStream#mark} method.
     * @exception IOException if an I/O error occurs
     */
    public static boolean isPropertySetStream(final InputStream stream)
        throws MarkUnsupportedException, IOException
    {
        /*
         * Read at most this many bytes.
         */
        final int BUFFER_SIZE = 50;

        /*
         * Mark the current position in the stream so that we can
         * reset to this position if the stream does not contain a
         * property set.
         */
        if (!stream.markSupported())
            throw new MarkUnsupportedException(stream.getClass().getName());
        stream.mark(BUFFER_SIZE);

        /*
         * Read a couple of bytes from the stream.
         */
        final byte[] buffer = new byte[BUFFER_SIZE];
        final int bytes =
            stream.read(buffer, 0,
                        Math.min(buffer.length, stream.available()));
        final boolean isPropertySetStream =
            isPropertySetStream(buffer, 0, bytes);
        stream.reset();
        return isPropertySetStream;
    }



    /**
     * <p>Checks whether a byte array is in the Horrible Property Set
     * Format.</p>
     *
     * @param src The byte array to check.
     * @param offset The offset in the byte array.
     * @param length The significant number of bytes in the byte
     * array. Only this number of bytes will be checked.
     * @return <code>true</code> if the byte array is a property set
     * stream, <code>false</code> if not.
     */
    public static boolean isPropertySetStream(final byte[] src,
                                              final int offset,
                                              final int length)
    {
        /* FIXME (3): Ensure that at most "length" bytes are read. */

        /*
         * Read the header fields of the stream. They must always be
         * there.
         */
        int o = offset;
        final int byteOrder = LittleEndian.getUShort(src, o);
        o += LittleEndian.SHORT_SIZE;
        byte[] temp = new byte[LittleEndian.SHORT_SIZE];
        LittleEndian.putShort(temp, 0, (short) byteOrder);
        if (!Util.equal(temp, BYTE_ORDER_ASSERTION))
            return false;
        final int format = LittleEndian.getUShort(src, o);
        o += LittleEndian.SHORT_SIZE;
        temp = new byte[LittleEndian.SHORT_SIZE];
        LittleEndian.putShort(temp, 0, (short) format);
        if (!Util.equal(temp, FORMAT_ASSERTION))
            return false;
        // final long osVersion = LittleEndian.getUInt(src, offset);
        o += LittleEndian.INT_SIZE;
        // final ClassID classID = new ClassID(src, offset);
        o += ClassID.LENGTH;
        final long sectionCount = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;
        if (sectionCount < 0)
            return false;
        return true;
    }



    /**
     * <p>Initializes this {@link PropertySet} instance from a byte
     * array. The method assumes that it has been checked already that
     * the byte array indeed represents a property set stream. It does
     * no more checks on its own.</p>
     *
     * @param src Byte array containing the property set stream
     * @param offset The property set stream starts at this offset
     * from the beginning of <var>src</var>
     * @param length Length of the property set stream.
     * @throws UnsupportedEncodingException if HPSF does not (yet) support the
     * property set's character encoding.
     */
    private void init(final byte[] src, final int offset, final int length)
    throws UnsupportedEncodingException
    {
        /* FIXME (3): Ensure that at most "length" bytes are read. */
        
        /*
         * Read the stream's header fields.
         */
        int o = offset;
        byteOrder = LittleEndian.getUShort(src, o);
        o += LittleEndian.SHORT_SIZE;
        format = LittleEndian.getUShort(src, o);
        o += LittleEndian.SHORT_SIZE;
        osVersion = (int) LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;
        classID = new ClassID(src, o);
        o += ClassID.LENGTH;
        final int sectionCount = LittleEndian.getInt(src, o);
        o += LittleEndian.INT_SIZE;
        if (sectionCount < 0)
            throw new HPSFRuntimeException("Section count " + sectionCount +
                                           " is negative.");

        /*
         * Read the sections, which are following the header. They
         * start with an array of section descriptions. Each one
         * consists of a format ID telling what the section contains
         * and an offset telling how many bytes from the start of the
         * stream the section begins.
         */
        /*
         * Most property sets have only one section. The Document
         * Summary Information stream has 2. Everything else is a rare
         * exception and is no longer fostered by Microsoft.
         */
        sections = new ArrayList<Section>( sectionCount );

        /*
         * Loop over the section descriptor array. Each descriptor
         * consists of a ClassID and a DWord, and we have to increment
         * "offset" accordingly.
         */
        for (int i = 0; i < sectionCount; i++)
        {
            final Section s = new Section(src, o);
            o += ClassID.LENGTH + LittleEndian.INT_SIZE;
            sections.add(s);
        }
    }



    /**
     * <p>Checks whether this {@link PropertySet} represents a Summary
     * Information.</p>
     *
     * @return <code>true</code> if this {@link PropertySet}
     * represents a Summary Information, else <code>false</code>.
     */
    public boolean isSummaryInformation()
    {
        if (sections.size() <= 0)
            return false;
        return Util.equal(sections.get(0).getFormatID().getBytes(),
                          SectionIDMap.SUMMARY_INFORMATION_ID);
    }



    /**
     * <p>Checks whether this {@link PropertySet} is a Document
     * Summary Information.</p>
     *
     * @return <code>true</code> if this {@link PropertySet}
     * represents a Document Summary Information, else <code>false</code>.
     */
    public boolean isDocumentSummaryInformation()
    {
        if (sections.size() <= 0)
            return false;
        return Util.equal(sections.get(0).getFormatID().getBytes(),
                          SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[0]);
    }



    /**
     * <p>Convenience method returning the {@link Property} array
     * contained in this property set. It is a shortcut for getting
     * the {@link PropertySet}'s {@link Section}s list and then
     * getting the {@link Property} array from the first {@link
     * Section}.</p>
     *
     * @return The properties of the only {@link Section} of this
     * {@link PropertySet}.
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    public Property[] getProperties()
        throws NoSingleSectionException
    {
        return getFirstSection().getProperties();
    }



    /**
     * <p>Convenience method returning the value of the property with
     * the specified ID. If the property is not available,
     * <code>null</code> is returned and a subsequent call to {@link
     * #wasNull} will return <code>true</code> .</p>
     *
     * @param id The property ID
     * @return The property value
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    protected Object getProperty(final int id) throws NoSingleSectionException
    {
        return getFirstSection().getProperty(id);
    }



    /**
     * <p>Convenience method returning the value of a boolean property
     * with the specified ID. If the property is not available,
     * <code>false</code> is returned. A subsequent call to {@link
     * #wasNull} will return <code>true</code> to let the caller
     * distinguish that case from a real property value of
     * <code>false</code>.</p>
     *
     * @param id The property ID
     * @return The property value
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    protected boolean getPropertyBooleanValue(final int id)
        throws NoSingleSectionException
    {
        return getFirstSection().getPropertyBooleanValue(id);
    }



    /**
     * <p>Convenience method returning the value of the numeric
     * property with the specified ID. If the property is not
     * available, 0 is returned. A subsequent call to {@link #wasNull}
     * will return <code>true</code> to let the caller distinguish
     * that case from a real property value of 0.</p>
     *
     * @param id The property ID
     * @return The propertyIntValue value
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    protected int getPropertyIntValue(final int id)
        throws NoSingleSectionException
    {
        return getFirstSection().getPropertyIntValue(id);
    }



    /**
     * <p>Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using {@link
     * #wasNull}, the caller can distiguish this case from a
     * property's real value of 0.</p>
     *
     * @return <code>true</code> if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else <code>false</code>.
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more than one {@link Section}.
     */
    public boolean wasNull() throws NoSingleSectionException
    {
        return getFirstSection().wasNull();
    }



    /**
     * <p>Gets the {@link PropertySet}'s first section.</p>
     *
     * @return The {@link PropertySet}'s first section.
     */
    public Section getFirstSection()
    {
        if (getSectionCount() < 1)
            throw new MissingSectionException("Property set does not contain any sections.");
        return sections.get(0);
    }



    /**
     * <p>If the {@link PropertySet} has only a single section this
     * method returns it.</p>
     *
     * @return The singleSection value
     */
    public Section getSingleSection()
    {
        final int sectionCount = getSectionCount();
        if (sectionCount != 1)
            throw new NoSingleSectionException
                ("Property set contains " + sectionCount + " sections.");
        return sections.get(0);
    }



    /**
     * <p>Returns <code>true</code> if the <code>PropertySet</code> is equal
     * to the specified parameter, else <code>false</code>.</p>
     *
     * @param o the object to compare this <code>PropertySet</code> with
     * 
     * @return <code>true</code> if the objects are equal, <code>false</code>
     * if not
     */
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof PropertySet))
            return false;
        final PropertySet ps = (PropertySet) o;
        int byteOrder1 = ps.getByteOrder();
        int byteOrder2 = getByteOrder();
        ClassID classID1 = ps.getClassID();
        ClassID classID2 = getClassID();
        int format1 = ps.getFormat();
        int format2 = getFormat();
        int osVersion1 = ps.getOSVersion();
        int osVersion2 = getOSVersion();
        int sectionCount1 = ps.getSectionCount();
        int sectionCount2 = getSectionCount();
        if (byteOrder1 != byteOrder2      ||
            !classID1.equals(classID2)    ||
            format1 != format2            ||
            osVersion1 != osVersion2      ||
            sectionCount1 != sectionCount2)
            return false;

        /* Compare the sections: */
        return Util.equals(getSections(), ps.getSections());
    }



    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        throw new UnsupportedOperationException("FIXME: Not yet implemented.");
    }



    /**
     * @see Object#toString()
     */
    public String toString()
    {
        final StringBuffer b = new StringBuffer();
        final int sectionCount = getSectionCount();
        b.append(getClass().getName());
        b.append('[');
        b.append("byteOrder: ");
        b.append(getByteOrder());
        b.append(", classID: ");
        b.append(getClassID());
        b.append(", format: ");
        b.append(getFormat());
        b.append(", OSVersion: ");
        b.append(getOSVersion());
        b.append(", sectionCount: ");
        b.append(sectionCount);
        b.append(", sections: [\n");
        for (Section section: getSections())
            b.append(section);
        b.append(']');
        b.append(']');
        return b.toString();
    }
}
