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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.docx4j.org.apache.poi.util.CodePageUtil;
import org.docx4j.org.apache.poi.util.HexDump;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LocaleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A property in a {@link Section} of a {@link PropertySet}.<p>
 *
 * The property's {@code ID} gives the property a meaning
 * in the context of its {@link Section}. Each {@link Section} spans
 * its own name space of property IDs.<p>
 *
 * The property's {@code type} determines how its
 * {@code value} is interpreted. For example, if the type is
 * {@link Variant#VT_LPSTR} (byte string), the value consists of a
 * DWord telling how many bytes the string contains. The bytes follow
 * immediately, including any null bytes that terminate the
 * string. The type {@link Variant#VT_I4} denotes a four-byte integer
 * value, {@link Variant#VT_FILETIME} some date and time (of a file).<p>
 *
 * Please note that not all {@link Variant} types yet. This might change
 * over time but largely depends on your feedback so that the POI team knows
 * which variant types are really needed. So please feel free to submit error
 * reports or patches for the types you need.
 *
 * @see Section
 * @see Variant
 * @see <a href="https://msdn.microsoft.com/en-us/library/dd942421.aspx">
 * [MS-OLEPS]: Object Linking and Embedding (OLE) Property Set Data Structures</a>
 */
public class Property {

	protected static Logger log = LoggerFactory.getLogger(Property.class);
	
    /**
     * Default codepage for {@link CodePageString CodePageStrings}
     */
    public static final int DEFAULT_CODEPAGE = CodePageUtil.CP_WINDOWS_1252;

    /** The property's ID. */
    private long id;

    /** The property's type. */
    private long type;

    /** The property's value. */
    private Object value;


    /**
     * Creates an empty property. It must be filled using the set method to be usable.
     */
    public Property() {
    }

    /**
     * Creates a {@code Property} as a copy of an existing {@code Property}.
     *
     * @param p The property to copy.
     */
    public Property(Property p) {
        this(p.id, p.type, p.value);
    }

    /**
     * Creates a property.
     *
     * @param id the property's ID.
     * @param type the property's type, see {@link Variant}.
     * @param value the property's value. Only certain types are allowed, see
     *        {@link Variant}.
     */
    public Property(final long id, final long type, final Object value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    /**
     * Creates a Property instance by reading its bytes
     * from the property set stream.
     *
     * @param id The property's ID.
     * @param src The bytes the property set stream consists of.
     * @param offset The property's type/value pair's offset in the
     * section.
     * @param length The property's type/value pair's length in bytes.
     * @param codepage The section's and thus the property's
     * codepage. It is needed only when reading string values.
     * @throws UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    public Property(final long id, final byte[] src, final long offset, final int length, final int codepage)
    throws UnsupportedEncodingException {
        this.id = id;

        /*
         * ID 0 is a special case since it specifies a dictionary of
         * property IDs and property names.
         */
        if (id == 0) {
            throw new UnsupportedEncodingException("Dictionary not allowed here");
        }

        int o = (int) offset;
        type = LittleEndian.getUInt(src, o);
        o += LittleEndianConsts.INT_SIZE;

        try {
            value = VariantSupport.read(src, o, length, (int) type, codepage);
        } catch (UnsupportedVariantTypeException ex) {
            VariantSupport.writeUnsupportedTypeMessage(ex);
            value = ex.getValue();
        }
    }

    /**
     * Creates a Property instance by reading its bytes
     * from the property set stream.
     *
     * @param id The property's ID.
     * @param leis The bytes the property set stream consists of.
     * @param length The property's type/value pair's length in bytes.
     * @param codepage The section's and thus the property's
     * codepage. It is needed only when reading string values.
     * @throws UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    public Property(final long id, LittleEndianByteArrayInputStream leis, final int length, final int codepage)
    throws UnsupportedEncodingException {
        this.id = id;

        /*
         * ID 0 is a special case since it specifies a dictionary of
         * property IDs and property names.
         */
        if (id == 0) {
            throw new UnsupportedEncodingException("Dictionary not allowed here");
        }

        type = leis.readUInt();

        try {
            value = VariantSupport.read(leis, length, (int) type, codepage);
        } catch (UnsupportedVariantTypeException ex) {
            VariantSupport.writeUnsupportedTypeMessage(ex);
            value = ex.getValue();
        }
    }


    /**
     * Returns the property's ID.
     *
     * @return The ID value
     */
    public long getID() {
        return id;
    }

    /**
     * Sets the property's ID.
     *
     * @param id the ID
     */
    public void setID(final long id) {
        this.id = id;
    }

    /**
     * Returns the property's type.
     *
     * @return The type value
     */
    public long getType() {
        return type;
    }

    /**
     * Sets the property's type.
     *
     * @param type the property's type
     */
    public void setType(final long type) {
        this.type = type;
    }

    /**
     * Returns the property's value.
     *
     * @return The property's value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the property's value.
     *
     * @param value the property's value
     */
    public void setValue(final Object value) {
        this.value = value;
    }








    /**
     * Returns the property's size in bytes. This is always a multiple of 4.
     *
     * @param property The integer property to check
     *
     * @return the property's size in bytes
     *
     * @throws WritingNotSupportedException if HPSF does not yet support the
     * property's variant type.
     */
    protected int getSize(int property) throws WritingNotSupportedException
    {
        int length = Variant.getVariantLength(type);
        if (length >= 0  || type == Variant.VT_EMPTY) {
            /* Fixed length */
            return length;
        }
        if (length == -2) {
            /* Unknown length */
            throw new WritingNotSupportedException(type, null);
        }

        /* Variable length: */
        if (type == Variant.VT_LPSTR || type == Variant.VT_LPWSTR) {
            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            try {
                length = write(bos, property) - 2*LittleEndianConsts.INT_SIZE;
                /* Pad to multiples of 4. */
                length += (4 - (length & 0x3)) & 0x3;
                return length;
            } catch (IOException e) {
                throw new WritingNotSupportedException(type, this.value);
            }
        }

        throw new WritingNotSupportedException(type, this.value);
    }



    /**
     * Compares two properties.<p>
     *
     * Please beware that a property with
     * ID == 0 is a special case: It does not have a type, and its value is the
     * section's dictionary. Another special case are strings: Two properties
     * may have the different types Variant.VT_LPSTR and Variant.VT_LPWSTR;
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Property)) {
            return false;
        }
        final Property p = (Property) o;
        final Object pValue = p.getValue();
        final long pId = p.getID();
        if (id != pId || (id != 0 && !typesAreEqual(type, p.getType()))) {
            return false;
        }
        if (value == null && pValue == null) {
            return true;
        }
        if (value == null || pValue == null) {
            return false;
        }

        /* It's clear now that both values are non-null. */
        final Class<?> valueClass = value.getClass();
        final Class<?> pValueClass = pValue.getClass();
        if (!(valueClass.isAssignableFrom(pValueClass)) &&
            !(pValueClass.isAssignableFrom(valueClass))) {
            return false;
        }

        if (value instanceof byte[]) {
            // compare without padding bytes
            byte[] thisVal = (byte[]) value, otherVal = (byte[]) pValue;
            int len = unpaddedLength(thisVal);
            if (len != unpaddedLength(otherVal)) {
                return false;
            }
            for (int i=0; i<len; i++) {
                if (thisVal[i] != otherVal[i]) {
                    return false;
                }
            }
            return true;
        }

        return value.equals(pValue);
    }

    /**
     * Byte arrays can be 0-padded up to 3 bytes to form a full quad array.
     * This returns the truncated length without the potentially 0-padded bytes
     *
     * @param buf the bytes
     * @return the truncated size with a maximum of 4 bytes shorter (3 bytes + trailing 0 of strings)
     */
    private static int unpaddedLength(byte[] buf) {
        final int end = (buf.length-(buf.length+3)%4);
        for (int i = buf.length; i>end; i--) {
            if (buf[i-1] != 0) {
                return i;
            }
        }
        return end;
    }


    private boolean typesAreEqual(final long t1, final long t2) {
        return (t1 == t2 ||
            (t1 == Variant.VT_LPSTR && t2 == Variant.VT_LPWSTR) ||
            (t2 == Variant.VT_LPSTR && t1 == Variant.VT_LPWSTR));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,type,value);

    }

    @Override
    public String toString() {
        return toString(Property.DEFAULT_CODEPAGE, null);
    }

    public String toString(int codepage, PropertyIDMap idMap) {
        final StringBuilder b = new StringBuilder();
        b.append("Property[");
        b.append("id: ");
        b.append(id);
        String idName = (idMap == null) ? null : idMap.get(id);
        if (idName == null) {
            idName = PropertyIDMap.getFallbackProperties().get(id);
        }
        if (idName != null) {
            b.append(" (");
            b.append(idName);
            b.append(")");
        }
        b.append(", type: ");
        b.append(getType());
        b.append(" (");
        b.append(getVariantName());
        b.append(") ");
        final Object value = getValue();
        b.append(", value: ");
        if (value instanceof String) {
            b.append((String)value);
            b.append("\n");
            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            try {
                write(bos, codepage);
            } catch (Exception e) {
                log.warn("can't serialize string", e);
            }

            // skip length field
            if(bos.size() > 2*LittleEndianConsts.INT_SIZE) {
                final String hex = HexDump.dump(bos.toByteArray(), -2L*LittleEndianConsts.INT_SIZE, 2*LittleEndianConsts.INT_SIZE);
                b.append(hex);
            }
        } else if (value instanceof byte[]) {
            b.append("\n");
            byte[] bytes = (byte[])value;
            if(bytes.length > 0) {
                String hex = HexDump.dump(bytes, 0L, 0);
                b.append(hex);
            }
        } else if (value instanceof Date) {
            Date d = (Date)value;
            long filetime = Filetime.dateToFileTime(d);
            if (Filetime.isUndefined(d)) {
                b.append("<undefined>");
            } else if ((filetime >>> 32) == 0) {
                // if the upper dword isn't set, we deal with time intervals
                long l = filetime*100;
                TimeUnit tu = TimeUnit.NANOSECONDS;
                final long hr  = tu.toHours(l);
                l -= TimeUnit.HOURS.toNanos(hr);
                final long min = tu.toMinutes(l);
                l -= TimeUnit.MINUTES.toNanos(min);
                final long sec = tu.toSeconds(l);
                l -= TimeUnit.SECONDS.toNanos(sec);
                final long ms  = tu.toMillis(l);

                String str = String.format(Locale.ROOT, "%02d:%02d:%02d.%03d",hr,min,sec,ms);
                b.append(str);
            } else {
                // use ISO-8601 timestamp format
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
                df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
                b.append(df.format(d));
            }
        } else if (type == Variant.VT_EMPTY || type == Variant.VT_NULL || value == null) {
            b.append("null");
        } else {
            b.append(value);

            String decoded = decodeValueFromID();
            if (decoded != null) {
                b.append(" (");
                b.append(decoded);
                b.append(")");
            }
        }
        b.append(']');
        return b.toString();
    }

    private String getVariantName() {
        if (getID() == 0) {
            return "dictionary";
        }
        return Variant.getVariantName(getType());
    }

    private String decodeValueFromID() {
        try {
            switch((int)getID()) {
                case PropertyIDMap.PID_CODEPAGE:
                    return CodePageUtil.codepageToEncoding(((Number)value).intValue());
                case PropertyIDMap.PID_LOCALE:
                    return LocaleUtil.getLocaleFromLCID(((Number)value).intValue());
            }
        } catch (Exception e) {
            log.warn("Can't decode id {}", getID());
        }
        return null;
    }

    /**
     * Writes the property to an output stream.
     *
     * @param out The output stream to write to.
     * @param codepage The codepage to use for writing non-wide strings
     * @return the number of bytes written to the stream
     *
     * @throws IOException if an I/O error occurs
     * @throws WritingNotSupportedException if a variant type is to be
     * written that is not yet supported
     */
    public int write(final OutputStream out, final int codepage)
    throws IOException, WritingNotSupportedException {
        int length = 0;
        long variantType = getType();

        /* Ensure that wide strings are written if the codepage is Unicode. */
//        if (codepage == CodePageUtil.CP_UNICODE && variantType == Variant.VT_LPSTR) {
//            variantType = Variant.VT_LPWSTR;
//        }

        if (variantType == Variant.VT_LPSTR && codepage != CodePageUtil.CP_UTF16) {
            String csStr = CodePageUtil.codepageToEncoding(codepage > 0 ? codepage : Property.DEFAULT_CODEPAGE);
            if (!Charset.forName(csStr).newEncoder().canEncode((String)value)) {
                variantType = Variant.VT_LPWSTR;
            }
        }

        LittleEndian.putUInt(variantType, out);
        length += LittleEndianConsts.INT_SIZE;
        length += VariantSupport.write(out, variantType, getValue(), codepage);
        return length;
    }

}
