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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.EmptyFileException;
import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.Entry;
import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianOutputStream;
import org.docx4j.org.apache.poi.util.NotImplemented;

/**
 * Represents a property set in the Horrible Property Set Format
 * (HPSF). These are usually metadata of a Microsoft Office
 * document.<p>
 *
 * An application that wants to access these metadata should create
 * an instance of this class or one of its subclasses by calling the
 * factory method {@link PropertySetFactory#create} and then retrieve
 * the information its needs by calling appropriate methods.<p>
 *
 * {@link PropertySetFactory#create} does its work by calling one
 * of the constructors {@link PropertySet#PropertySet(InputStream)} or
 * {@link PropertySet#PropertySet(byte[])}. If the constructor's
 * argument is not in the Horrible Property Set Format, i.e. not a
 * property set stream, or if any other error occurs, an appropriate
 * exception is thrown.<p>
 *
 * A {@link PropertySet} has a list of {@link Section}s, and each
 * {@link Section} has a {@link Property} array. Use {@link
 * #getSections} to retrieve the {@link Section}s, then call {@link
 * Section#getProperties} for each {@link Section} to get hold of the
 * {@link Property} arrays.<p>
 *
 * Since the vast majority of {@link PropertySet}s contains only a single
 * {@link Section}, the convenience method {@link #getProperties} returns
 * the properties of a {@link PropertySet}'s {@link Section} (throwing a
 * {@link NoSingleSectionException} if the {@link PropertySet} contains
 * more (or less) than exactly one {@link Section}).
 */
public class PropertySet {
    /**
     * If the OS version field holds this value the property set stream was
     * created on a 16-bit Windows system.
     */
    public static final int OS_WIN16     = 0x0000;

    /**
     * If the OS version field holds this value the property set stream was
     * created on a Macintosh system.
     */
    public static final int OS_MACINTOSH = 0x0001;

    /**
     * If the OS version field holds this value the property set stream was
     * created on a 32-bit Windows system.
     */
    public static final int OS_WIN32     = 0x0002;

    /**
     * The "byteOrder" field must equal this value.
     */
    /* package */ static final int BYTE_ORDER_ASSERTION = 0xFFFE;

    /**
     * The "format" field must equal this value.
     */
    /* package */ static final int FORMAT_ASSERTION = 0x0000;

    /**
     * The length of the property set stream header.
     */
    /* package */ static final int OFFSET_HEADER =
        LittleEndianConsts.SHORT_SIZE + /* Byte order    */
        LittleEndianConsts.SHORT_SIZE + /* Format        */
        LittleEndianConsts.INT_SIZE +   /* OS version    */
        ClassID.LENGTH +                /* Class ID      */
        LittleEndianConsts.INT_SIZE;    /* Section count */


    /**
     * Specifies this {@link PropertySet}'s byte order. See the
     * HPFS documentation for details!
     */
    private int byteOrder;

    /**
     * Specifies this {@link PropertySet}'s format. See the HPFS
     * documentation for details!
     */
    private int format;

    /**
     * Specifies the version of the operating system that created this
     * {@link PropertySet}. See the HPFS documentation for details!
     */
    private int osVersion;

    /**
     * Specifies this {@link PropertySet}'s "classID" field. See
     * the HPFS documentation for details!
     */
    private ClassID classID;

    /**
     * The sections in this {@link PropertySet}.
     */
    private final List<Section> sections = new ArrayList<>();


    /**
     * Constructs a {@code PropertySet} instance. Its
     * primary task is to initialize the field with their proper values.
     * It also sets fields that might change to reasonable defaults.
     */
    public PropertySet() {
        /* Initialize the "byteOrder" field. */
        byteOrder = BYTE_ORDER_ASSERTION;

        /* Initialize the "format" field. */
        format = FORMAT_ASSERTION;

        /* Initialize "osVersion" field as if the property has been created on
         * a Win32 platform, whether this is the case or not. */
        osVersion = (OS_WIN32 << 16) | 0x0A04;

        /* Initialize the "classID" field. */
        classID = new ClassID();

        /* Initialize the sections. Since property set must have at least
         * one section it is added right here. */
        addSection(new Section());
    }



    /**
     * Creates a PropertySet instance from an {@link
     * InputStream} in the Horrible Property Set Format.<p>
     *
     * The constructor reads the first few bytes from the stream
     * and determines whether it is really a property set stream. If
     * it is, it parses the rest of the stream. If it is not, it
     * resets the stream to its beginning in order to let other
     * components mess around with the data and throws an
     * exception.
     *
     * @param stream Holds the data making out the property set
     * stream.
     * @throws IOException
     *    if the {@link InputStream} cannot be accessed as needed.
     * @throws NoPropertySetStreamException
     *    if the input stream does not contain a property set.
     * @throws UnsupportedEncodingException
     *    if a character encoding is not supported.
     */
    public PropertySet(final InputStream stream)
    throws NoPropertySetStreamException, IOException {
        if (!isPropertySetStream(stream)) {
            throw new NoPropertySetStreamException();
        }

        final byte[] buffer = IOUtils.toByteArray(stream);
        init(buffer, 0, buffer.length);
    }



    /**
     * Creates a PropertySet instance from a byte array that
     * represents a stream in the Horrible Property Set Format.
     *
     * @param stream The byte array holding the stream data.
     * @param offset The offset in {@code stream} where the stream
     * data begin. If the stream data begin with the first byte in the
     * array, the {@code offset} is 0.
     * @param length The length of the stream data.
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     *
     * @throws UnsupportedEncodingException if the codepage is not supported.
     */
    public PropertySet(final byte[] stream, final int offset, final int length)
    throws NoPropertySetStreamException, UnsupportedEncodingException {
        if (!isPropertySetStream(stream, offset, length)) {
            throw new NoPropertySetStreamException();
        }
        init(stream, offset, length);
    }

    /**
     * Creates a PropertySet instance from a byte array
     * that represents a stream in the Horrible Property Set Format.
     *
     * @param stream The byte array holding the stream data. The
     * complete byte array contents is the stream data.
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     *
     * @throws UnsupportedEncodingException if the codepage is not supported.
     */
    public PropertySet(final byte[] stream)
    throws NoPropertySetStreamException, UnsupportedEncodingException {
        this(stream, 0, stream.length);
    }

    /**
     * Constructs a {@code PropertySet} by doing a deep copy of
     * an existing {@code PropertySet}. All nested elements, i.e.
     * {@code Section}s and {@code Property} instances, will be their
     * counterparts in the new {@code PropertySet}.
     *
     * @param ps The property set to copy
     */
    public PropertySet(PropertySet ps) {
        setByteOrder(ps.getByteOrder());
        setFormat(ps.getFormat());
        setOSVersion(ps.getOSVersion());
        setClassID(ps.getClassID());
        for (final Section section : ps.getSections()) {
            sections.add(new Section(section));
        }
    }


    /**
     * @return The property set stream's low-level "byte order" field. It is always {@code 0xFFFE}.
     */
    public int getByteOrder() {
        return byteOrder;
    }

    /**
     * Returns the property set stream's low-level "byte order" field.
     *
     * @param byteOrder The property set stream's low-level "byte order" field.
     */
    @SuppressWarnings("WeakerAccess")
    public void setByteOrder(int byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * @return The property set stream's low-level "format" field. It is always {@code 0x0000}.
     */
    public int getFormat() {
        return format;
    }

    /**
     * Sets the property set stream's low-level "format" field.
     *
     * @param format The property set stream's low-level "format" field.
     */
    public void setFormat(int format) {
        this.format = format;
    }

    /**
     * @return The property set stream's low-level "OS version" field.
     */
    public int getOSVersion() {
        return osVersion;
    }

    /**
     * Sets the property set stream's low-level "OS version" field.
     *
     * @param osVersion The property set stream's low-level "OS version" field.
     */
    @SuppressWarnings("WeakerAccess")
    public void setOSVersion(int osVersion) {
        this.osVersion = osVersion;
    }


    /**
     * @return The property set stream's low-level "class ID" field.
     */
    public ClassID getClassID() {
        return classID;
    }

    /**
     * Sets the property set stream's low-level "class ID" field.
     *
     * @param classID The property set stream's low-level "class ID" field.
     */
    @SuppressWarnings("WeakerAccess")
    public void setClassID(ClassID classID) {
        this.classID = classID;
    }

    /**
     * @return The number of {@link Section}s in the property set.
     */
    public int getSectionCount() {
        return sections.size();
    }

    /**
     * @return The unmodifiable list of {@link Section}s in the property set.
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }



    /**
     * Adds a section to this property set.
     *
     * @param section The {@link Section} to add. It will be appended
     * after any sections that are already present in the property set
     * and thus become the last section.
     */
    public void addSection(final Section section) {
        sections.add(section);
    }

    /**
     * Removes all sections from this property set.
     */
    public void clearSections() {
        sections.clear();
    }

    /**
     * The id to name mapping of the properties in this set.
     *
     * @return the id to name mapping of the properties in this set or {@code null} if not applicable
     */
    public PropertyIDMap getPropertySetIDMap() {
        return null;
    }


    /**
     * Checks whether an {@link InputStream} is in the Horrible
     * Property Set Format.
     *
     * @param stream The {@link InputStream} to check. In order to
     * perform the check, the method reads the first bytes from the
     * stream. After reading, the stream is reset to the position it
     * had before reading. The {@link InputStream} must support the
     * {@link InputStream#mark} method.
     * @return {@code true} if the stream is a property set
     * stream, else {@code false}.
     * @throws IOException if an I/O error occurs
     */
    public static boolean isPropertySetStream(final InputStream stream) throws IOException {
        /*
         * Read at most this many bytes.
         */
        final int BUFFER_SIZE = 50;

        /*
         * Read a couple of bytes from the stream.
         */
        try {
            final byte[] buffer = IOUtils.peekFirstNBytes(stream, BUFFER_SIZE);
            return isPropertySetStream(buffer, 0, buffer.length);
        } catch (EmptyFileException e) {
            return false;
        }
    }



    /**
     * Checks whether a byte array is in the Horrible Property Set Format.
     *
     * @param src The byte array to check.
     * @param offset The offset in the byte array.
     * @param length The significant number of bytes in the byte
     * array. Only this number of bytes will be checked.
     * @return {@code true} if the byte array is a property set
     * stream, {@code false} if not.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static boolean isPropertySetStream(final byte[] src, final int offset, final int length) {
        LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream(src, offset, length);

        /*
         * Read the header fields of the stream. They must always be
         * there.
         */
        try {
            final int byteOrder = leis.readUShort();
            if (byteOrder != BYTE_ORDER_ASSERTION) {
                return false;
            }
            final int format = leis.readUShort();
            if (format != FORMAT_ASSERTION) {
                return false;
            }
            final long osVersion = leis.readUInt();
            if (leis.skip(ClassID.LENGTH) != ClassID.LENGTH) {
                return false;
            }
            final long sectionCount = leis.readUInt();
            return (sectionCount >= 0);
        } catch (RuntimeException e) {
            return false;
        }
    }



    /**
     * Initializes this PropertySet instance from a byte
     * array. The method assumes that it has been checked already that
     * the byte array indeed represents a property set stream. It does
     * no more checks on its own.
     *
     * @param src Byte array containing the property set stream
     * @param offset The property set stream starts at this offset
     * from the beginning of {@code src}
     * @param length Length of the property set stream.
     * @throws UnsupportedEncodingException if HPSF does not (yet) support the
     * property set's character encoding.
     */
    private void init(final byte[] src, final int offset, final int length)
    throws UnsupportedEncodingException {
        /* FIXME (3): Ensure that at most "length" bytes are read. */

        /*
         * Read the stream's header fields.
         */
        int o = offset;
        byteOrder = LittleEndian.getUShort(src, o);
        o += LittleEndianConsts.SHORT_SIZE;
        format = LittleEndian.getUShort(src, o);
        o += LittleEndianConsts.SHORT_SIZE;
        osVersion = (int) LittleEndian.getUInt(src, o);
        o += LittleEndianConsts.INT_SIZE;
        classID = new ClassID(src, o);
        o += ClassID.LENGTH;
        final int sectionCount = LittleEndian.getInt(src, o);
        o += LittleEndianConsts.INT_SIZE;
        if (sectionCount < 0) {
            throw new HPSFRuntimeException("Section count " + sectionCount + " is negative.");
        }

        /*
         * Read the sections, which are following the header. They
         * start with an array of section descriptions. Each one
         * consists of a format ID telling what the section contains
         * and an offset telling how many bytes from the start of the
         * stream the section begins.
         *
         * Most property sets have only one section. The Document
         * Summary Information stream has 2. Everything else is a rare
         * exception and is no longer fostered by Microsoft.
         */

        /*
         * Loop over the section descriptor array. Each descriptor
         * consists of a ClassID and a DWord, and we have to increment
         * "offset" accordingly.
         */
        for (int i = 0; i < sectionCount; i++) {
            final Section s = new Section(src, o);
            o += ClassID.LENGTH + LittleEndianConsts.INT_SIZE;
            sections.add(s);
        }
    }

    /**
     * Writes the property set to an output stream.
     *
     * @param out the output stream to write the section to
     * @throws IOException if an error when writing to the output stream
     * occurs
     * @throws WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public void write(final OutputStream out) throws IOException, WritingNotSupportedException {

        out.write(toBytes());

        /* Indicate that we're done */
        out.close();
    }

    private byte[] toBytes() throws WritingNotSupportedException, IOException {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            LittleEndianOutputStream leos = new LittleEndianOutputStream(bos)) {

            /* Write the number of sections in this property set stream. */
            final int nrSections = getSectionCount();

            /* Write the property set's header. */
            leos.writeShort(getByteOrder());
            leos.writeShort(getFormat());
            leos.writeInt(getOSVersion());
            putClassId(bos, getClassID());
            leos.writeInt(nrSections);

            assert (bos.size() == OFFSET_HEADER);

            final int[][] offsets = new int[getSectionCount()][2];

            /* Write the section list, i.e. the references to the sections. Each
             * entry in the section list consist of the section's class ID and the
             * section's offset relative to the beginning of the stream. */
            int secCnt = 0;
            for (final Section section : getSections()) {
                final ClassID formatID = section.getFormatID();
                if (formatID == null) {
                    throw new NoFormatIDException();
                }
                putClassId(bos, formatID);
                offsets[secCnt++][0] = bos.size();
                // offset dummy - filled later
                leos.writeInt(-1);
            }

            /* Write the sections themselves. */
            secCnt = 0;
            for (final Section section : getSections()) {
                offsets[secCnt++][1] = bos.size();
                section.write(bos);
            }

            byte[] result = bos.toByteArray();
            for (int[] off : offsets) {
                LittleEndian.putInt(result, off[0], off[1]);
            }

            return result;
        }
    }

    /**
     * Writes a property set to a document in a POI filesystem directory.
     *
     * @param dir The directory in the POI filesystem to write the document to.
     * @param name The document's name. If there is already a document with the
     * same name in the directory the latter will be overwritten.
     *
     * @throws WritingNotSupportedException if the filesystem doesn't support writing
     * @throws IOException if the old entry can't be deleted or the new entry be written
     */
    public void write(final DirectoryEntry dir, final String name)
    throws WritingNotSupportedException, IOException {
        /* If there is already an entry with the same name, remove it. */
        if (dir.hasEntryCaseInsensitive(name)) {
            final Entry e = dir.getEntryCaseInsensitive(name);
            e.delete();
        }

        /* Create the new entry. */
        dir.createDocument(name, toInputStream());
    }

    /**
     * Returns the contents of this property set stream as an input stream.
     * The latter can be used for example to write the property set into a POIFS
     * document. The input stream represents a snapshot of the property set.
     * If the latter is modified while the input stream is still being
     * read, the modifications will not be reflected in the input stream but in
     * the PropertySet only.
     *
     * @return the contents of this property set stream
     *
     * @throws WritingNotSupportedException if HPSF does not yet support writing
     * of a property's variant type.
     * @throws IOException if an I/O exception occurs.
     */
    public InputStream toInputStream() throws WritingNotSupportedException, IOException {
        return UnsynchronizedByteArrayInputStream.builder().setByteArray(toBytes()).get();
    }

    /**
     * Fetches the property with the given ID, then does its
     *  best to return it as a String
     *
     * @param propertyId the property id
     *
     * @return The property as a String, or null if unavailable
     */
    String getPropertyStringValue(final int propertyId) {
        Object propertyValue = getProperty(propertyId);
        return getPropertyStringValue(propertyValue);
    }

    /**
     * Return the string representation of a property value
     *
     * @param propertyValue the property value
     *
     * @return The property value as a String, or null if unavailable
     */
    public static String getPropertyStringValue(final Object propertyValue) {
        // Normal cases
        if (propertyValue == null) {
            return null;
        }
        if (propertyValue instanceof String) {
            return (String)propertyValue;
        }

        // Do our best with some edge cases
        if (propertyValue instanceof byte[]) {
            byte[] b = (byte[])propertyValue;
            switch (b.length) {
                case 0:
                    return "";
                case 1:
                    return Byte.toString(b[0]);
                case 2:
                    return Integer.toString( LittleEndian.getUShort(b) );
                case 4:
                    return Long.toString( LittleEndian.getUInt(b) );
                default:
                    // Maybe it's a string? who knows!
                    try {
                        return CodePageUtil.getStringFromCodePage(b, Property.DEFAULT_CODEPAGE);
                    } catch (UnsupportedEncodingException e) {
                        // doesn't happen ...
                        return "";
                    }
            }
        }
        return propertyValue.toString();
    }

    /**
     * Checks whether this PropertySet represents a Summary Information.
     *
     * @return {@code true} if this PropertySet
     * represents a Summary Information, else {@code false}.
     */
    public boolean isSummaryInformation() {
        return !sections.isEmpty() && matchesSummary(getFirstSection().getFormatID(), SummaryInformation.FORMAT_ID);
    }

    /**
     * Checks whether this PropertySet is a Document Summary Information.
     *
     * @return {@code true} if this PropertySet
     * represents a Document Summary Information, else {@code false}.
     */
    public boolean isDocumentSummaryInformation() {
        return !sections.isEmpty() && matchesSummary(getFirstSection().getFormatID(), DocumentSummaryInformation.FORMAT_ID);
    }

    /* package */ static boolean matchesSummary(ClassID actual, ClassID... expected) {
        for (ClassID sum : expected) {
            if (sum.equals(actual) || sum.equalsInverted(actual)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method returning the {@link Property} array contained in this
     * property set. It is a shortcut for getting he PropertySets
     * {@link Section}s list and then getting the {@link Property} array from the
     * first {@link Section}.
     *
     * @return The properties of the only {@link Section} of this
     * PropertySet.
     * @throws NoSingleSectionException if the PropertySet has
     * more or less than one {@link Section}.
     */
    public Property[] getProperties() throws NoSingleSectionException {
        return getFirstSection().getProperties();
    }



    /**
     * Convenience method returning the value of the property with the specified ID.
     * If the property is not available, {@code null} is returned and a subsequent
     * call to {@link #wasNull} will return {@code true}.
     *
     * @param id The property ID
     * @return The property value
     * @throws NoSingleSectionException if the PropertySet has
     * more or less than one {@link Section}.
     */
    protected Object getProperty(final int id) throws NoSingleSectionException {
        return getFirstSection().getProperty(id);
    }



    /**
     * Convenience method returning the value of a boolean property with the
     * specified ID. If the property is not available, {@code false} is returned.
     * A subsequent call to {@link #wasNull} will return {@code true} to let the
     * caller distinguish that case from a real property value of {@code false}.
     *
     * @param id The property ID
     * @return The property value
     * @throws NoSingleSectionException if the PropertySet has
     * more or less than one {@link Section}.
     */
    boolean getPropertyBooleanValue(final int id) throws NoSingleSectionException {
        return getFirstSection().getPropertyBooleanValue(id);
    }



    /**
     * Convenience method returning the value of the numeric
     * property with the specified ID. If the property is not
     * available, 0 is returned. A subsequent call to {@link #wasNull}
     * will return {@code true} to let the caller distinguish
     * that case from a real property value of 0.
     *
     * @param id The property ID
     * @return The propertyIntValue value
     * @throws NoSingleSectionException if the PropertySet has
     * more or less than one {@link Section}.
     */
    int getPropertyIntValue(final int id) throws NoSingleSectionException {
        return getFirstSection().getPropertyIntValue(id);
    }



    /**
     * Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using wasNull,
     * the caller can distinguish this case from a
     * property's real value of 0.
     *
     * @return {@code true} if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else {@code false}.
     * @throws NoSingleSectionException if the PropertySet has
     * more than one {@link Section}.
     */
    public boolean wasNull() throws NoSingleSectionException {
        return getFirstSection().wasNull();
    }



    /**
     * Gets the PropertySets first section.
     *
     * @return The PropertySets first section.
     */
    @SuppressWarnings("WeakerAccess")
    public Section getFirstSection() {
        if (sections.isEmpty()) {
            throw new MissingSectionException("Property set does not contain any sections.");
        }
        return sections.get(0);
    }


    /**
     * Returns {@code true} if the {@code PropertySet} is equal
     * to the specified parameter, else {@code false}.
     *
     * @param o the object to compare this {@code PropertySet} with
     *
     * @return {@code true} if the objects are equal, {@code false}
     * if not
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof PropertySet)) {
            return false;
        }
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
            sectionCount1 != sectionCount2) {
            return false;
        }

        /* Compare the sections: */
        return getSections().containsAll(ps.getSections());
    }

    @NotImplemented
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("FIXME: Not yet implemented.");
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
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
        for (Section section: getSections()) {
            b.append(section.toString(getPropertySetIDMap()));
        }
        b.append(']');
        b.append(']');
        return b.toString();
    }


    void remove1stProperty(long id) {
        getFirstSection().removeProperty(id);
    }

    void set1stProperty(long id, String value) {
        getFirstSection().setProperty((int)id, value);
    }

    void set1stProperty(long id, int value) {
        getFirstSection().setProperty((int)id, value);
    }

    void set1stProperty(long id, boolean value) {
        getFirstSection().setProperty((int)id, value);
    }

    @SuppressWarnings("SameParameterValue")
    void set1stProperty(long id, byte[] value) {
        getFirstSection().setProperty((int)id, value);
    }

    private static void putClassId(final UnsynchronizedByteArrayOutputStream out, final ClassID n) {
        byte[] b = new byte[16];
        n.write(b, 0);
        out.write(b, 0, b.length);
    }
}
