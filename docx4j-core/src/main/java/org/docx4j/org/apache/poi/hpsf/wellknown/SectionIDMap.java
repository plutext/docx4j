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

package org.docx4j.org.apache.poi.hpsf.wellknown;

import java.util.HashMap;

/**
 * <p>Maps section format IDs to {@link PropertyIDMap}s. It is
 * initialized with two well-known section format IDs: those of the
 * <tt>\005SummaryInformation</tt> stream and the
 * <tt>\005DocumentSummaryInformation</tt> stream.</p>
 *
 * <p>If you have a section format ID you can use it as a key to query
 * this map.  If you get a {@link PropertyIDMap} returned your section
 * is well-known and you can query the {@link PropertyIDMap} for PID
 * strings. If you get back <code>null</code> you are on your own.</p>
 *
 * <p>This {@link java.util.Map} expects the byte arrays of section format IDs
 * as keys. A key maps to a {@link PropertyIDMap} describing the
 * property IDs in sections with the specified section format ID.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public class SectionIDMap extends HashMap {

    /**
     * <p>The SummaryInformation's section's format ID.</p>
     */
    public static final byte[] SUMMARY_INFORMATION_ID = new byte[]
    {
        (byte) 0xF2, (byte) 0x9F, (byte) 0x85, (byte) 0xE0,
        (byte) 0x4F, (byte) 0xF9, (byte) 0x10, (byte) 0x68,
        (byte) 0xAB, (byte) 0x91, (byte) 0x08, (byte) 0x00,
        (byte) 0x2B, (byte) 0x27, (byte) 0xB3, (byte) 0xD9
    };

    /**
     * <p>The DocumentSummaryInformation's first and second sections' format
     * ID.</p>
     */
    public static final byte[][] DOCUMENT_SUMMARY_INFORMATION_ID = new byte[][]
    {
        {
            (byte) 0xD5, (byte) 0xCD, (byte) 0xD5, (byte) 0x02,
            (byte) 0x2E, (byte) 0x9C, (byte) 0x10, (byte) 0x1B,
            (byte) 0x93, (byte) 0x97, (byte) 0x08, (byte) 0x00,
            (byte) 0x2B, (byte) 0x2C, (byte) 0xF9, (byte) 0xAE
        },
        {
            (byte) 0xD5, (byte) 0xCD, (byte) 0xD5, (byte) 0x05,
            (byte) 0x2E, (byte) 0x9C, (byte) 0x10, (byte) 0x1B,
            (byte) 0x93, (byte) 0x97, (byte) 0x08, (byte) 0x00,
            (byte) 0x2B, (byte) 0x2C, (byte) 0xF9, (byte) 0xAE
        }
    };

    /**
     * <p>A property without a known name is described by this string.</p>
     */
    public static final String UNDEFINED = "[undefined]";

    /**
     * <p>The default section ID map. It maps section format IDs to
     * {@link PropertyIDMap}s.</p>
     */
    private static SectionIDMap defaultMap;



    /**
     * <p>Returns the singleton instance of the default {@link
     * SectionIDMap}.</p>
     *
     * @return The instance value
     */
    public static SectionIDMap getInstance()
    {
        if (defaultMap == null)
        {
            final SectionIDMap m = new SectionIDMap();
            m.put(SUMMARY_INFORMATION_ID,
                  PropertyIDMap.getSummaryInformationProperties());
            m.put(DOCUMENT_SUMMARY_INFORMATION_ID[0],
                  PropertyIDMap.getDocumentSummaryInformationProperties());
            defaultMap = m;
        }
        return defaultMap;
    }



    /**
     * <p>Returns the property ID string that is associated with a
     * given property ID in a section format ID's namespace.</p>
     *
     * @param sectionFormatID Each section format ID has its own name
     * space of property ID strings and thus must be specified.
     * @param  pid The property ID
     * @return The well-known property ID string associated with the
     * property ID <var>pid</var> in the name space spanned by <var>
     * sectionFormatID</var> . If the <var>pid</var>
     * /<var>sectionFormatID </var> combination is not well-known, the
     * string "[undefined]" is returned.
     */
    public static String getPIDString(final byte[] sectionFormatID,
                                      final long pid)
    {
        final PropertyIDMap m = getInstance().get(sectionFormatID);
        if (m == null) {
            return UNDEFINED;
        }
        final String s = (String) m.get(pid);
        if (s == null)
            return UNDEFINED;
        return s;
    }



    /**
     * <p>Returns the {@link PropertyIDMap} for a given section format
     * ID.</p>
     *
     * @param sectionFormatID the section format ID
     * @return the property ID map
     */
    public PropertyIDMap get(final byte[] sectionFormatID)
    {
        return (PropertyIDMap) super.get(new String(sectionFormatID));
    }



    /**
     * <p>Returns the {@link PropertyIDMap} for a given section format
     * ID.</p>
     *
     * @param sectionFormatID A section format ID as a <tt>byte[]</tt> .
     * @deprecated Use {@link #get(byte[])} instead!
     * @return the property ID map
     */
    public Object get(final Object sectionFormatID)
    {
        return get((byte[]) sectionFormatID);
    }



    /**
     * <p>Associates a section format ID with a {@link
     * PropertyIDMap}.</p>
     *
     * @param sectionFormatID the section format ID
     * @param propertyIDMap the property ID map
     * @return as defined by {@link java.util.Map#put}
     */
    public Object put(final byte[] sectionFormatID,
                      final PropertyIDMap propertyIDMap)
    {
        return super.put(new String(sectionFormatID), propertyIDMap);
    }



    /**
     * @deprecated Use {@link #put(byte[], PropertyIDMap)} instead!
     *
     * @see #put(byte[], PropertyIDMap)
     *
     * @param key This parameter remains undocumented since the method is
     * deprecated.
     * @param value This parameter remains undocumented since the method is
     * deprecated.
     * @return The return value remains undocumented since the method is
     * deprecated.
     */
    public Object put(final Object key, final Object value)
    {
        return put((byte[]) key, (PropertyIDMap) value);
    }

}
