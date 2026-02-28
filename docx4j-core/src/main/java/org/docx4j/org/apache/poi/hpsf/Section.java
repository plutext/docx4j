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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianOutputStream;
import org.docx4j.fonts.microsoft.MicrosoftFontsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a section in a {@link PropertySet}.
 */
public class Section {

	protected static Logger log = LoggerFactory.getLogger(Section.class);

    /**
     * Maps property IDs to section-private PID strings. These
     * strings can be found in the property with ID 0.
     */
    private Map<Long,String> dictionary;

    /**
     * The section's format ID, {@link #getFormatID}.
     */
    private ClassID formatID;

    /**
     * Contains the bytes making out the section. This byte array is
     * established when the section's size is calculated and can be reused
     * later. If the array is empty, the section was modified and the bytes need to be regenerated.
     */
    private final UnsynchronizedByteArrayOutputStream sectionBytes = UnsynchronizedByteArrayOutputStream.builder().get();

    /**
     * The offset of the section in the stream.
     */
    private final long _offset;

    /**
     * This section's properties.
     */
    private final Map<Long,Property> properties = new LinkedHashMap<>();

    /**
     * This member is {@code true} if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else {@code false}.
     */
    private transient boolean wasNull;

    /**
     * Creates an empty Section.
     */
    public Section() {
        this._offset = -1;
    }

    /**
     * Constructs a {@code Section} by doing a deep copy of an
     * existing {@code Section}. All nested {@code Property}
     * instances, will be their mutable counterparts in the new
     * {@code MutableSection}.
     *
     * @param s The section set to copy
     */
    public Section(final Section s) {
        this._offset = -1;
        setFormatID(s.getFormatID());
        for (Property p : s.properties.values()) {
            properties.put(p.getID(), new Property(p));
        }
        setDictionary(s.getDictionary());
    }



    /**
     * Creates a Section instance from a byte array.
     *
     * @param src Contains the complete property set stream.
     * @param offset The position in the stream that points to the
     * section's format ID.
     *
     * @throws UnsupportedEncodingException if the section's codepage is not
     * supported.
     */
    public Section(final byte[] src, final int offset) throws UnsupportedEncodingException {
        /*
         * Read the format ID.
         */
        formatID = new ClassID(src, offset);

        /*
         * Read the offset from the stream's start and positions to
         * the section header.
         */
        int offFix = (int)LittleEndian.getUInt(src, offset + ClassID.LENGTH);

        // some input files have an invalid (padded?) offset, which need to be fixed
        // search for beginning of size field
        if (src[offFix] == 0) {
            for (int i=0; i<3 && src[offFix] == 0; i++,offFix++);
            // cross check with propertyCount field and the property list field
            for (int i=0; i<3 && (src[offFix+3] != 0 || src[offFix+7] != 0 || src[offFix+11] != 0); i++,offFix--);
        }

        this._offset = offFix;

        LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream(src, offFix);

        /*
         * Read the section length.
         */
        int size = (int)Math.min(leis.readUInt(), src.length-_offset);

        /*
         * Read the number of properties.
         */
        final int propertyCount = (int)leis.readUInt();

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
        /* Pass 1: Read the property list. */
        final TreeBidiMap<Long,Long> offset2Id = new TreeBidiMap<>();
        for (int i = 0; i < propertyCount; i++) {
            /* Read the property ID. */
            long id = leis.readUInt();

            /* Offset from the section's start. */
            long off = leis.readUInt();

            offset2Id.put(off, id);
        }

        Long cpOffset = offset2Id.getKey((long)PropertyIDMap.PID_CODEPAGE);

        /* Look for the codepage. */
        int codepage = -1;
        if (cpOffset != null) {
            /* Read the property's value type. It must be VT_I2. */
            leis.setReadIndex(Math.toIntExact(this._offset + cpOffset));
            final long type = leis.readUInt();

            if (type != Variant.VT_I2) {
                throw new HPSFRuntimeException
                    ("Value type of property ID 1 is not VT_I2 but " + type + ".");
            }

            /* Read the codepage number. */
            codepage =  leis.readUShort();
            setCodepage(codepage);
        }


        /* Pass 2: Read all properties - including the codepage property,
         * if available. */
        for (Map.Entry<Long,Long> me : offset2Id.entrySet()) {
            long off = me.getKey();
            long id = me.getValue();

            if (id == PropertyIDMap.PID_CODEPAGE) {
                continue;
            }

            int pLen = propLen(offset2Id, off, size);
            leis.setReadIndex(Math.toIntExact(this._offset + off));

            if (id == PropertyIDMap.PID_DICTIONARY) {
                leis.mark(100000);
                if (!readDictionary(leis, pLen, codepage)) {
                    // there was an error reading the dictionary, maybe because the pid (0) was used wrong
                    // try reading a property instead
                    leis.reset();
                    try {
                        // fix id
                        id = Math.max(PropertyIDMap.PID_MAX, offset2Id.inverseBidiMap().lastKey())+1;
                        setProperty(new Property(id, leis, pLen, codepage));
                    } catch (RuntimeException e) {
                        log.info("Dictionary fallback failed - ignoring property");
                    }
                }
            } else {
                setProperty(new Property(id, leis, pLen, codepage));
            }
        }

        sectionBytes.write(src, Math.toIntExact(_offset), size);
        padSectionBytes();
    }

    /**
     * Retrieves the length of the given property (by key)
     *
     * @param offset2Id the offset to id map
     * @param entryOffset the current entry key
     * @param maxSize the maximum offset/size of the section stream
     * @return the length of the current property
     */
    private static int propLen(
        TreeBidiMap<Long,Long> offset2Id,
        Long entryOffset,
        long maxSize) {
        Long nextKey = offset2Id.nextKey(entryOffset);
        long begin = entryOffset;
        long end = (nextKey != null) ? nextKey : maxSize;
        return Math.toIntExact(end - begin);
    }


    /**
     * Returns the format ID. The format ID is the "type" of the
     * section. For example, if the format ID of the first {@link
     * Section} contains the bytes specified by
     * {@code org.apache.poi.hpsf.wellknown.SectionIDMap.SUMMARY_INFORMATION_ID}
     * the section (and thus the property set) is a SummaryInformation.
     *
     * @return The format ID
     */
    public ClassID getFormatID() {
        return formatID;
    }

    /**
     * Sets the section's format ID.
     *
     * @param formatID The section's format ID
     */
    public void setFormatID(final ClassID formatID) {
        this.formatID = formatID;
    }

    /**
     * Sets the section's format ID.
     *
     * @param formatID The section's format ID as a byte array. It components
     * are in big-endian format.
     */
    @SuppressWarnings("WeakerAccess")
    public void setFormatID(final byte[] formatID) {
        ClassID fid = getFormatID();
        if (fid == null) {
            fid = new ClassID();
            setFormatID(fid);
        }
        fid.setBytes(formatID);
    }

    /**
     * Returns the offset of the section in the stream.
     *
     * @return The offset of the section in the stream.
     */
    public long getOffset() {
        return _offset;
    }

    /**
     * Returns the number of properties in this section.
     *
     * @return The number of properties in this section.
     */
    public int getPropertyCount() {
        return properties.size();
    }

    /**
     * Returns this section's properties.
     *
     * @return This section's properties.
     */
    public Property[] getProperties() {
        return properties.values().toArray(new Property[0]);
    }

    /**
     * Sets this section's properties. Any former values are overwritten.
     *
     * @param properties This section's new properties.
     */
    public void setProperties(final Property[] properties) {
        this.properties.clear();
        for (Property p : properties) {
            setProperty(p);
        }
    }

    /**
     * Returns the value of the property with the specified ID. If
     * the property is not available, {@code null} is returned
     * and a subsequent call to {@link #wasNull} will return
     * {@code true}.
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    public Object getProperty(final long id) {
        wasNull = !properties.containsKey(id);
        return (wasNull) ? null : properties.get(id).getValue();
    }

    /**
     * Sets the string value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     */
    public void setProperty(final int id, final String value) {
        setProperty(id, Variant.VT_LPSTR, value);
    }

    /**
     * Sets the int value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final int value) {
        setProperty(id, Variant.VT_I4, value);
    }



    /**
     * Sets the long value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final long value) {
        setProperty(id, Variant.VT_I8, value);
    }



    /**
     * Sets the boolean value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final boolean value) {
        setProperty(id, Variant.VT_BOOL, value);
    }



    /**
     * Sets the value and the variant type of the property with the
     * specified ID. If a property with this ID is not yet present in
     * the section, it will be added. An already present property with
     * the specified ID will be overwritten. A default mapping will be
     * used to choose the property's type.
     *
     * @param id The property's ID.
     * @param variantType The property's variant type.
     * @param value The property's value.
     *
     * @see #setProperty(int, String)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final int id, final long variantType, final Object value) {
        setProperty(new Property(id, variantType, value));
    }



    /**
     * Sets a property.
     *
     * @param p The property to be set.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final Property p) {
        Property old = properties.get(p.getID());
        if (old == null || !old.equals(p)) {
            properties.put(p.getID(), p);
            sectionBytes.reset();
        }
    }

    /**
     * Sets a property.
     *
     * @param id The property ID.
     * @param value The property's value. The value's class must be one of those
     *        supported by HPSF.
     */
    public void setProperty(final int id, final Object value) {
        if (value instanceof String) {
            setProperty(id, (String) value);
        } else if (value instanceof Long) {
            setProperty(id, ((Long) value).longValue());
        } else if (value instanceof Integer) {
            setProperty(id, ((Integer) value).intValue());
        } else if (value instanceof Short) {
            setProperty(id, ((Short) value).intValue());
        } else if (value instanceof Boolean) {
            setProperty(id, ((Boolean) value).booleanValue());
        } else if (value instanceof Date) {
            setProperty(id, Variant.VT_FILETIME, value);
        } else {
            throw new HPSFRuntimeException(
                    "HPSF does not support properties of type " +
                    value.getClass().getName() + ".");
        }
    }

    /**
     * Returns the value of the numeric property with the specified
     * ID. If the property is not available, 0 is returned. A
     * subsequent call to {@link #wasNull} will return
     * {@code true} to let the caller distinguish that case from
     * a real property value of 0.
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    int getPropertyIntValue(final long id) {
        final Number i;
        final Object o = getProperty(id);
        if (o == null) {
            return 0;
        }
        if (!(o instanceof Long || o instanceof Integer)) {
            throw new HPSFRuntimeException
                ("This property is not an integer type, but " +
                 o.getClass().getName() + ".");
        }
        i = (Number) o;
        return i.intValue();
    }

    /**
     * Returns the value of the boolean property with the specified
     * ID. If the property is not available, {@code false} is
     * returned. A subsequent call to {@link #wasNull} will return
     * {@code true} to let the caller distinguish that case from
     * a real property value of {@code false}.
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    boolean getPropertyBooleanValue(final int id) {
        final Boolean b = (Boolean) getProperty(id);
        return b != null && b;
    }

    /**
     * Sets the value of the boolean property with the specified
     * ID.
     *
     * @param id The property's ID
     * @param value The property's value
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     * @see Variant
     */
    @SuppressWarnings("unused")
    protected void setPropertyBooleanValue(final int id, final boolean value) {
        setProperty(id, Variant.VT_BOOL, value);
    }

    /**
     * @return the section's size in bytes.
     */
    public int getSize() {
        int size = sectionBytes.size();
        if (size > 0) {
            return size;
        }
        try {
            return calcSize();
        } catch (HPSFRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new HPSFRuntimeException(ex);
        }
    }

    /**
     * Calculates the section's size. It is the sum of the lengths of the
     * section's header (8), the properties list (16 times the number of
     * properties) and the properties themselves.
     *
     * @return the section's length in bytes.
     * @throws WritingNotSupportedException If the document is opened read-only.
     * @throws IOException If an error happens while writing.
     */
    private int calcSize() throws WritingNotSupportedException, IOException {
        sectionBytes.reset();
        write(sectionBytes);
        padSectionBytes();
        return sectionBytes.size();
    }

    private void padSectionBytes() {
        byte[] padArray = { 0, 0, 0 };
        /* Pad to multiple of 4 bytes so that even the Windows shell (explorer)
         * shows custom properties. */
        int pad = (4 - (sectionBytes.size() & 0x3)) & 0x3;
        sectionBytes.write(padArray, 0, pad);
    }


    /**
     * Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using {@link
     * #wasNull} the caller can distiguish this case from a property's
     * real value of 0.
     *
     * @return {@code true} if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else {@code false}.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean wasNull() {
        return wasNull;
    }



    /**
     * Returns the PID string associated with a property ID. The ID
     * is first looked up in the Sections private dictionary.
     * If it is not found there, the property PID string is taken
     * from sections format IDs namespace.
     * If the PID is also undefined there, i.e. it is not well-known,
     * {@code "[undefined]"} is returned.
     *
     * @param pid The property ID
     *
     * @return The well-known property ID string associated with the
     * property ID {@code pid}
     */
    public String getPIDString(final long pid) {
        Map<Long,String> dic = getDictionary();
        if (dic == null || !dic.containsKey(pid)) {
            ClassID fmt = getFormatID();
            if (SummaryInformation.FORMAT_ID.equals(fmt)) {
                dic = PropertyIDMap.getSummaryInformationProperties();
            } else if (DocumentSummaryInformation.FORMAT_ID[0].equals(fmt)) {
                dic = PropertyIDMap.getDocumentSummaryInformationProperties();
            }
        }

        return (dic != null && dic.containsKey(pid)) ? dic.get(pid) : PropertyIDMap.UNDEFINED;
    }

    /**
     * Removes all properties from the section including 0 (dictionary) and 1 (codepage).
     */
    public void clear() {
        for (Property p : getProperties()) {
            removeProperty(p.getID());
        }
    }

    /**
     * Checks whether this section is equal to another object. The result is
     * {@code false} if one of the following conditions holds:
     *
     * <ul>
     *
     * <li>The other object is not a Section.
     *
     * <li>The format IDs of the two sections are not equal.
     *
     * <li>The sections have a different number of properties. However,
     * properties with ID 1 (codepage) are not counted.
     *
     * <li>The other object is not a Section.
     *
     * <li>The properties have different values. The order of the properties
     * is irrelevant.
     *
     * </ul>
     *
     * @param o The object to compare this section with
     * @return {@code true} if the objects are equal, {@code false} if
     * not
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Section)) {
            return false;
        }
        final Section s = (Section) o;
        if (!s.getFormatID().equals(getFormatID())) {
            return false;
        }

        /* Compare all properties except the dictionary (id 0) and
         * the codepage (id 1 / ignored) as they must be handled specially. */
        Set<Long> propIds = new HashSet<>(properties.keySet());
        propIds.addAll(s.properties.keySet());
        propIds.remove(0L);
        propIds.remove(1L);

        for (Long id : propIds) {
            Property p1 = properties.get(id);
            Property p2 = s.properties.get(id);
            if (p1 == null || !p1.equals(p2)) {
                return false;
            }
        }

        /* If the dictionaries are unequal the sections are unequal. */
        Map<Long,String> d1 = getDictionary();
        Map<Long,String> d2 = s.getDictionary();

        return (d1 == null && d2 == null) || (d1 != null && d1.equals(d2));
    }

    /**
     * Removes a property.
     *
     * @param id The ID of the property to be removed
     */
    @SuppressWarnings("WeakerAccess")
    public void removeProperty(final long id) {
        if (properties.remove(id) != null) {
            sectionBytes.reset();
        }
    }

    /**
     * Writes this section into an output stream.<p>
     *
     * Internally this is done by writing into three byte array output
     * streams: one for the properties, one for the property list and one for
     * the section as such. The two former are appended to the latter when they
     * have received all their data.
     *
     * @param out The stream to write into.
     *
     * @return The number of bytes written, i.e. the section's size.
     * @throws IOException if an I/O error occurs
     * @throws WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public int write(final OutputStream out) throws WritingNotSupportedException, IOException {
        /* Check whether we have already generated the bytes making out the
         * section. */
        if (sectionBytes.size() > 0) {
            sectionBytes.writeTo(out);
            return sectionBytes.size();
        }

        /* Writing the section's dictionary it tricky. If there is a dictionary
         * (property 0) the codepage property (property 1) must be set, too. */
        int codepage = getCodepage();
        if (codepage == -1) {
            log.warn("The codepage property is not set although a dictionary is present. " +
                    "Defaulting to ISO-8859-1.");
            codepage = Property.DEFAULT_CODEPAGE;
        }

        final int[][] offsets = new int[properties.size()][2];
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            LittleEndianOutputStream leos = new LittleEndianOutputStream(bos)) {

            /* Write the section's length - dummy value, fixed later */
            leos.writeInt(-1);

            /* Write the section's number of properties: */
            leos.writeInt(properties.size());

            int propCnt = 0;
            for (Property p : properties.values()) {
                /* Write the property list entry. */
                leos.writeUInt(p.getID());
                // dummy offset to be fixed later
                offsets[propCnt++][0] = bos.size();
                leos.writeInt(-1);
            }


            /* Write the properties and the property list into their respective
             * streams: */
            propCnt = 0;
            for (Property p : properties.values()) {
                offsets[propCnt++][1] = bos.size();
                /* If the property ID is not equal 0 we write the property and all
                 * is fine. However, if it equals 0 we have to write the section's
                 * dictionary which has an implicit type only and an explicit
                 * value. */
                if (p.getID() != 0) {
                    /* Write the property and update the position to the next
                     * property. */
                    p.write(bos, codepage);
                } else {
                    writeDictionary(bos, codepage);
                }
            }

            byte[] result = bos.toByteArray();
            LittleEndian.putInt(result, 0, bos.size());

            for (int[] off : offsets) {
                LittleEndian.putUInt(result, off[0], off[1]);
            }

            out.write(result);

            return bos.size();
        }
    }

    /**
     * Reads a dictionary.
     *
     * @param leis The byte stream containing the bytes making out the dictionary.
     * @param length The dictionary contains at most this many bytes.
     * @param codepage The codepage of the string values.
     *
     * @return {@code true} if dictionary was read successful, {@code false} otherwise
     */
    private boolean readDictionary(LittleEndianByteArrayInputStream leis, final int length, final int codepage) {
        Map<Long,String> dic = new HashMap<>();

        /*
         * Read the number of dictionary entries.
         */
        final long nrEntries = leis.readUInt();

        long id = -1;
        boolean isCorrupted = false;
        for (int i = 0; i < nrEntries; i++) {
            String errMsg =
                "The property set's dictionary contains bogus data. "
                + "All dictionary entries starting with the one with ID "
                + id + " will be ignored.";

            /* The key. */
            id = leis.readUInt();

            /* The value (a string). The length is the either the
             * number of (two-byte) characters if the character set is Unicode
             * or the number of bytes if the character set is not Unicode.
             * The length includes terminating 0x00 bytes which we have to strip
             * off to create a Java string. */
            long sLength = leis.readUInt();

            /* Read the string - Strip 0x00 characters from the end of the string. */
            int cp = (codepage == -1) ? Property.DEFAULT_CODEPAGE : codepage;
            int nrBytes = Math.toIntExact(((sLength-1) * (cp == CodePageUtil.CP_UNICODE ? 2 : 1)));
            if (nrBytes > 0xFFFFFF) {
                log.warn(errMsg);
                isCorrupted = true;
                break;
            }

            try {
                byte[] buf = IOUtils.safelyAllocate(nrBytes, CodePageString.getMaxRecordLength());
                leis.readFully(buf, 0, nrBytes);
                final String str = CodePageUtil.getStringFromCodePage(buf, 0, nrBytes, cp);

                int pad = 1;
                if (cp == CodePageUtil.CP_UNICODE) {
                    pad = 2+((4 - ((nrBytes+2) & 0x3)) & 0x3);
                }
                IOUtils.skipFully(leis, pad);

                dic.put(id, str);
            } catch (RuntimeException|IOException ex) {
                log.warn(errMsg, ex);
                isCorrupted = true;
                break;
            }
        }
        setDictionary(dic);
        return !isCorrupted;
    }


    /**
     * Writes the section's dictionary.
     *
     * @param out The output stream to write to.
     * @param codepage The codepage to be used to write the dictionary items.
     * @throws IOException if an I/O exception occurs.
     */
    private void writeDictionary(final OutputStream out, final int codepage)
    throws IOException {
        final byte[] padding = new byte[4];
        final Map<Long,String> dic = getDictionary();

        LittleEndian.putUInt(dic.size(), out);
        int length = LittleEndianConsts.INT_SIZE;
        for (Map.Entry<Long,String> ls : dic.entrySet()) {

            LittleEndian.putUInt(ls.getKey(), out);
            length += LittleEndianConsts.INT_SIZE;

            final String value = ls.getValue()+"\0";
            final byte[] bytes = CodePageUtil.getBytesInCodePage(value, codepage);
            final int len = (codepage == CodePageUtil.CP_UNICODE) ? value.length() : bytes.length;

            LittleEndian.putUInt( len, out );
            length += LittleEndianConsts.INT_SIZE;

            out.write(bytes);
            length += bytes.length;

            final int pad = (codepage == CodePageUtil.CP_UNICODE) ? ((4 - (length & 0x3)) & 0x3) : 0;
            out.write(padding, 0, pad);
            length += pad;
        }

        final int pad = (4 - (length & 0x3)) & 0x3;
        out.write(padding, 0, pad);
    }

    /**
     * Sets the section's dictionary. All keys in the dictionary must be
     * {@link Long} instances, all values must be
     * {@link String}s. This method overwrites the properties with IDs
     * 0 and 1 since they are reserved for the dictionary and the dictionary's
     * codepage. Setting these properties explicitly might have surprising
     * effects. An application should never do this but always use this
     * method.
     *
     * @param dictionary The dictionary
     *
     * @throws IllegalPropertySetDataException if the dictionary's key and
     * value types are not correct.
     *
     * @see Section#getDictionary()
     */
    public void setDictionary(final Map<Long,String> dictionary) throws IllegalPropertySetDataException {
        if (dictionary != null) {
            if (this.dictionary == null) {
                this.dictionary = new TreeMap<>();
            }
            this.dictionary.putAll(dictionary);

            /* If the codepage property (ID 1) for the strings (keys and values)
             * used in the dictionary is not yet defined, set it to ISO-8859-1. */
            int cp = getCodepage();
            if (cp == -1) {
                setCodepage(Property.DEFAULT_CODEPAGE);
            }

            /* Set the dictionary property (ID 0). Please note that the second
             * parameter in the method call below is unused because dictionaries
             * don't have a type. */
            setProperty(PropertyIDMap.PID_DICTIONARY, -1, dictionary);
        } else {
            /* Setting the dictionary to null means to remove property 0.
             * However, it does not mean to remove property 1 (codepage). */
            removeProperty(PropertyIDMap.PID_DICTIONARY);
            this.dictionary = null;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{getFormatID(),getProperties()});
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(PropertyIDMap idMap) {
        final StringBuilder b = new StringBuilder();
        final Property[] pa = getProperties();
        b.append("\n\n\n");
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
        int codepage = getCodepage();
        if (codepage == -1) {
            codepage = Property.DEFAULT_CODEPAGE;
        }
        for (Property p : pa) {
            b.append(p.toString(codepage, idMap));
            b.append(",\n");
        }
        b.append(']');
        b.append(']');
        return b.toString();
    }



    /**
     * Gets the section's dictionary. A dictionary allows an application to
     * use human-readable property names instead of numeric property IDs. It
     * contains mappings from property IDs to their associated string
     * values. The dictionary is stored as the property with ID 0. The codepage
     * for the strings in the dictionary is defined by property with ID 1.
     *
     * @return the dictionary or {@code null} if the section does not have
     * a dictionary.
     */
    @SuppressWarnings("unchecked")
    public Map<Long,String> getDictionary() {
        if (dictionary == null) {
            dictionary = (Map<Long,String>) getProperty(PropertyIDMap.PID_DICTIONARY);
        }

        return dictionary;
    }



    /**
     * Gets the section's codepage, if any.
     *
     * @return The section's codepage if one is defined, else -1.
     */
    public int getCodepage() {
        final Integer codepage = (Integer) getProperty(PropertyIDMap.PID_CODEPAGE);
        return (codepage == null) ? -1 : codepage;
    }

    /**
     * Sets the codepage.
     *
     * @param codepage the codepage
     */
    public void setCodepage(final int codepage) {
        setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2, codepage);
    }
}
