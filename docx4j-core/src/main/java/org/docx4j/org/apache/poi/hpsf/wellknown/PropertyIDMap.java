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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.docx4j.org.apache.poi.hpsf.DocumentSummaryInformation;
import org.docx4j.org.apache.poi.hpsf.SummaryInformation;

/**
 * This is a dictionary which maps property ID values to property
 * ID strings.
 *
 * The methods {@link #getSummaryInformationProperties} and {@link
 * #getDocumentSummaryInformationProperties} return singleton {@link
 * PropertyIDMap}s. An application that wants to extend these maps
 * should treat them as unmodifiable, copy them and modify the
 * copies.
 *
 * Trying to modify the map directly will cause exceptions
 * {@link UnsupportedOperationException} to be thrown.
 */
public class PropertyIDMap implements Map<Long,String> {

    /*
     * The following definitions are for property IDs in the first
     * (and only) section of the Summary Information property set.
     */

    /** ID of the property that denotes the document's title */
    public static final int PID_TITLE = 2;

    /** ID of the property that denotes the document's subject */
    public static final int PID_SUBJECT = 3;

    /** ID of the property that denotes the document's author */
    public static final int PID_AUTHOR = 4;

    /** ID of the property that denotes the document's keywords */
    public static final int PID_KEYWORDS = 5;

    /** ID of the property that denotes the document's comments */
    public static final int PID_COMMENTS = 6;

    /** ID of the property that denotes the document's template */
    public static final int PID_TEMPLATE = 7;

    /** ID of the property that denotes the document's last author */
    public static final int PID_LASTAUTHOR = 8;

    /** ID of the property that denotes the document's revision number */
    public static final int PID_REVNUMBER = 9;

    /** ID of the property that denotes the document's edit time */
    public static final int PID_EDITTIME = 10;

    /** ID of the property that denotes the date and time the document was
     * last printed */
    public static final int PID_LASTPRINTED = 11;

    /** ID of the property that denotes the date and time the document was
     * created. */
    public static final int PID_CREATE_DTM = 12;

    /** ID of the property that denotes the date and time the document was
     * saved */
    public static final int PID_LASTSAVE_DTM = 13;

    /** ID of the property that denotes the number of pages in the
     * document */
    public static final int PID_PAGECOUNT = 14;

    /** ID of the property that denotes the number of words in the
     * document */
    public static final int PID_WORDCOUNT = 15;

    /** ID of the property that denotes the number of characters in the
     * document */
    public static final int PID_CHARCOUNT = 16;

    /** ID of the property that denotes the document's thumbnail */
    public static final int PID_THUMBNAIL = 17;

    /** ID of the property that denotes the application that created the
     * document */
    public static final int PID_APPNAME = 18;

    /** ID of the property that denotes whether read/write access to the
     * document is allowed or whether is should be opened as read-only. It can
     * have the following values:
     *
     * <table>
     *  <caption>ID-property mapping</caption>
     *  <tbody>
     *   <tr>
     *    <th>Value</th>
     *    <th>Description</th>
     *   </tr>
     *   <tr>
     *    <th>0</th>
     *    <th>No restriction</th>
     *   </tr>
     *   <tr>
     *    <th>2</th>
     *    <th>Read-only recommended</th>
     *   </tr>
     *   <tr>
     *    <th>4</th>
     *    <th>Read-only enforced</th>
     *   </tr>
     *  </tbody>
     * </table>
     */
    public static final int PID_SECURITY = 19;



    /*
     * The following definitions are for property IDs in the first
     * section of the Document Summary Information property set.
     */

    /**
     * The entry is a dictionary.
     */
    public static final int PID_DICTIONARY = 0;

    /**
     * The entry denotes a code page.
     */
    public static final int PID_CODEPAGE = 1;

    /**
     * The entry is a string denoting the category the file belongs
     * to, e.g. review, memo, etc. This is useful to find documents of
     * same type.
     */
    public static final int PID_CATEGORY = 2;

    /**
     * Target format for power point presentation, e.g. 35mm,
     * printer, video etc.
     */
    public static final int PID_PRESFORMAT = 3;

    /**
     * Number of bytes.
     */
    public static final int PID_BYTECOUNT = 4;

    /**
     * Number of lines.
     */
    public static final int PID_LINECOUNT = 5;

    /**
     * Number of paragraphs.
     */
    public static final int PID_PARCOUNT = 6;

    /**
     * Number of slides in a power point presentation.
     */
    public static final int PID_SLIDECOUNT = 7;

    /**
     * Number of slides with notes.
     */
    public static final int PID_NOTECOUNT = 8;

    /**
     * Number of hidden slides.
     */
    public static final int PID_HIDDENCOUNT = 9;

    /**
     * Number of multimedia clips, e.g. sound or video.
     */
    public static final int PID_MMCLIPCOUNT = 10;

    /**
     * This entry is set to -1 when scaling of the thumbnail is
     * desired. Otherwise the thumbnail should be cropped.
     */
    public static final int PID_SCALE = 11;

    /**
     * This entry denotes an internally used property. It is a
     * vector of variants consisting of pairs of a string (VT_LPSTR)
     * and a number (VT_I4). The string is a heading name, and the
     * number tells how many document parts are under that
     * heading.
     */
    public static final int PID_HEADINGPAIR = 12;

    /**
     * This entry contains the names of document parts (word: names
     * of the documents in the master document, excel: sheet names,
     * power point: slide titles, binder: document names).
     */
    public static final int PID_DOCPARTS = 13;

    /**
     * This entry contains the name of the project manager.
     */
    public static final int PID_MANAGER = 14;

    /**
     * This entry contains the company name.
     */
    public static final int PID_COMPANY = 15;

    /**
     * If this entry is -1 the links are dirty and should be
     * re-evaluated.
     */
    public static final int PID_LINKSDIRTY = 0x10;

    /**
     * The entry specifies an estimate of the number of characters
     *  in the document, including whitespace, as an integer
     */
    public static final int PID_CCHWITHSPACES = 0x11;

    // 0x12 Unused
    // 0x13 GKPIDDSI_SHAREDDOC - Must be False
    // 0x14 GKPIDDSI_LINKBASE - Must not be written
    // 0x15 GKPIDDSI_HLINKS - Must not be written

    /**
     * This entry contains a boolean which marks if the User Defined
     *  Property Set has been updated outside of the Application, if so the
     *  hyperlinks should be updated on document load.
     */
    public static final int PID_HYPERLINKSCHANGED = 0x16;

    /**
     * This entry contains the version of the Application which wrote the
     *  Property set, stored with the two high order bytes having the major
     *  version number, and the two low order bytes the minor version number.
     */
    public static final int PID_VERSION = 0x17;

    /**
     * This entry contains the VBA digital signature for the VBA project
     *  embedded in the document.
     */
    public static final int PID_DIGSIG = 0x18;

    // 0x19 Unused

    /**
     * This entry contains a string of the content type of the file.
     */
    public static final int PID_CONTENTTYPE = 0x1A;

    /**
     * This entry contains a string of the document status.
     */
    public static final int PID_CONTENTSTATUS = 0x1B;

    /**
     * This entry contains a string of the document language, but
     *  normally should be empty.
     */
    public static final int PID_LANGUAGE = 0x1C;

    /**
     * This entry contains a string of the document version, but
     *  normally should be empty
     */
    public static final int PID_DOCVERSION = 0x1D;

    /**
     * The highest well-known property ID. Applications are free to use
     *  higher values for custom purposes. (This value is based on Office 12,
     *  earlier versions of Office had lower values)
     */
    public static final int PID_MAX = 0x1F;

    /**
     * The Locale property, if present, MUST have the property identifier 0x80000000,
     * MUST NOT have a property name, and MUST have type VT_UI4 (0x0013).
     * If present, its value MUST be a valid language code identifier as specified in [MS-LCID].
     * Its value is selected in an implementation-specific manner.
     */
    public static final int PID_LOCALE = 0x80000000;


    /**
     * The Behavior property, if present, MUST have the property identifier 0x80000003,
     * MUST NOT have a property name, and MUST have type VT_UI4 (0x0013).
     * A version 0 property set, indicated by the value 0x0000 for the Version field of
     * the PropertySetStream packet, MUST NOT have a Behavior property.
     * If the Behavior property is present, it MUST have one of the following values.
     *
     * <ul>
     * <li>0x00000000 = Property names are case-insensitive (default)
     * <li>0x00000001 = Property names are case-sensitive.
     * </ul>
     */
    public static final int PID_BEHAVIOUR = 0x80000003;

    /**
     * A property without a known name is described by this string.
     */
    public static final String UNDEFINED = "[undefined]";

    /**
     * Contains the summary information property ID values and
     * associated strings. See the overall HPSF documentation for
     * details!
     */
    private static PropertyIDMap summaryInformationProperties;
    private static final Object[][] summaryInformationIdValues = {
        { (long)PID_TITLE, "PID_TITLE" },
        { (long)PID_SUBJECT, "PID_SUBJECT" },
        { (long)PID_AUTHOR, "PID_AUTHOR" },
        { (long)PID_KEYWORDS, "PID_KEYWORDS" },
        { (long)PID_COMMENTS, "PID_COMMENTS" },
        { (long)PID_TEMPLATE, "PID_TEMPLATE" },
        { (long)PID_LASTAUTHOR, "PID_LASTAUTHOR" },
        { (long)PID_REVNUMBER, "PID_REVNUMBER" },
        { (long)PID_EDITTIME, "PID_EDITTIME" },
        { (long)PID_LASTPRINTED, "PID_LASTPRINTED" },
        { (long)PID_CREATE_DTM, "PID_CREATE_DTM" },
        { (long)PID_LASTSAVE_DTM, "PID_LASTSAVE_DTM" },
        { (long)PID_PAGECOUNT, "PID_PAGECOUNT" },
        { (long)PID_WORDCOUNT, "PID_WORDCOUNT" },
        { (long)PID_CHARCOUNT, "PID_CHARCOUNT" },
        { (long)PID_THUMBNAIL, "PID_THUMBNAIL" },
        { (long)PID_APPNAME, "PID_APPNAME" },
        { (long)PID_SECURITY, "PID_SECURITY" },
    };

    /**
     * Contains the summary information property ID values and
     * associated strings. See the overall HPSF documentation for
     * details!
     */
    private static PropertyIDMap documentSummaryInformationProperties;
    private static final Object[][] documentSummaryInformationIdValues = {
        { (long)PID_DICTIONARY, "PID_DICTIONARY" },
        { (long)PID_CODEPAGE, "PID_CODEPAGE" },
        { (long)PID_CATEGORY, "PID_CATEGORY" },
        { (long)PID_PRESFORMAT, "PID_PRESFORMAT" },
        { (long)PID_BYTECOUNT, "PID_BYTECOUNT" },
        { (long)PID_LINECOUNT, "PID_LINECOUNT" },
        { (long)PID_PARCOUNT, "PID_PARCOUNT" },
        { (long)PID_SLIDECOUNT, "PID_SLIDECOUNT" },
        { (long)PID_NOTECOUNT, "PID_NOTECOUNT" },
        { (long)PID_HIDDENCOUNT, "PID_HIDDENCOUNT" },
        { (long)PID_MMCLIPCOUNT, "PID_MMCLIPCOUNT" },
        { (long)PID_SCALE, "PID_SCALE" },
        { (long)PID_HEADINGPAIR, "PID_HEADINGPAIR" },
        { (long)PID_DOCPARTS, "PID_DOCPARTS" },
        { (long)PID_MANAGER, "PID_MANAGER" },
        { (long)PID_COMPANY, "PID_COMPANY" },
        { (long)PID_LINKSDIRTY, "PID_LINKSDIRTY" },
    };


    /**
     * Contains the fallback property ID values and associated strings.
     * This is only used for lookups and not for initializing a property set
     */
    private static PropertyIDMap fallbackProperties;
    private static final Object[][] fallbackIdValues = {
        { (long)PID_DICTIONARY, "PID_DICTIONARY" },
        { (long)PID_CODEPAGE, "PID_CODEPAGE" },
        { (long)PID_CATEGORY, "PID_CATEGORY" },
        { (long)PID_PRESFORMAT, "PID_PRESFORMAT" },
        { (long)PID_BYTECOUNT, "PID_BYTECOUNT" },
        { (long)PID_LINECOUNT, "PID_LINECOUNT" },
        { (long)PID_PARCOUNT, "PID_PARCOUNT" },
        { (long)PID_SLIDECOUNT, "PID_SLIDECOUNT" },
        { (long)PID_NOTECOUNT, "PID_NOTECOUNT" },
        { (long)PID_HIDDENCOUNT, "PID_HIDDENCOUNT" },
        { (long)PID_MMCLIPCOUNT, "PID_MMCLIPCOUNT" },
        { (long)PID_SCALE, "PID_SCALE" },
        { (long)PID_HEADINGPAIR, "PID_HEADINGPAIR" },
        { (long)PID_DOCPARTS, "PID_DOCPARTS" },
        { (long)PID_MANAGER, "PID_MANAGER" },
        { (long)PID_COMPANY, "PID_COMPANY" },
        { (long)PID_LINKSDIRTY, "PID_LINKSDIRTY" },
        { (long)PID_CCHWITHSPACES, "PID_CCHWITHSPACES" },
  // 0x12 Unused
  // 0x13 GKPIDDSI_SHAREDDOC - Must be False
  // 0x14 GKPIDDSI_LINKBASE - Must not be written
  // 0x15 GKPIDDSI_HLINKS - Must not be written
        { (long)PID_HYPERLINKSCHANGED, "PID_HYPERLINKSCHANGED" },
        { (long)PID_VERSION, "PID_VERSION" },
        { (long)PID_DIGSIG, "PID_DIGSIG" },
  // 0x19 Unused
        { (long)PID_CONTENTTYPE, "PID_CONTENTTYPE" },
        { (long)PID_CONTENTSTATUS, "PID_CONTENTSTATUS" },
        { (long)PID_LANGUAGE, "PID_LANGUAGE" },
        { (long)PID_DOCVERSION, "PID_DOCVERSION" },
        { (long)PID_MAX, "PID_MAX" },
        { (long)PID_LOCALE, "PID_LOCALE" },
        { (long)PID_BEHAVIOUR, "PID_BEHAVIOUR" },
    };

    private final Map<Long,String> idMap;


    /**
     * Creates a {@link PropertyIDMap} backed by another map.
     *
     * @param idValues an array of key/value pairs via nested Object[2] arrays
     */
    private PropertyIDMap(Object[][] idValues) {
        Map<Long,String> m = new HashMap<>(idValues.length);
        for (Object[] idValue : idValues) {
            m.put((Long)idValue[0], (String)idValue[1]);
        }
        idMap = Collections.unmodifiableMap(m);
    }

    /**
     * @return the Summary Information properties singleton
     */
    public static synchronized PropertyIDMap getSummaryInformationProperties() {
        if (summaryInformationProperties == null) {
            summaryInformationProperties = new PropertyIDMap(summaryInformationIdValues);
        }
        return summaryInformationProperties;
    }

    /**
     * @return The Document Summary Information properties singleton.
     */
    public static synchronized PropertyIDMap getDocumentSummaryInformationProperties() {
        if (documentSummaryInformationProperties == null) {
            documentSummaryInformationProperties = new PropertyIDMap(documentSummaryInformationIdValues);
        }
        return documentSummaryInformationProperties;
    }

    /**
     * Returns a property map, which is only used as a fallback, i.e. if available, the correct map
     * for {@link DocumentSummaryInformation} or {@link SummaryInformation} should be used.
     *
     * @return the resulting property map
     */
    public static synchronized PropertyIDMap getFallbackProperties() {
        if (fallbackProperties == null) {
            fallbackProperties = new PropertyIDMap(fallbackIdValues);
        }
        return fallbackProperties;
    }

    @Override
    public int size() {
        return idMap.size();
    }

    @Override
    public boolean isEmpty() {
        return idMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return idMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return idMap.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return idMap.get(key);
    }

    @Override
    public String put(Long key, String value) {
        //noinspection ConstantConditions
        return idMap.put(key, value);
    }

    @Override
    public String remove(Object key) {
        //noinspection ConstantConditions
        return idMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends Long, ? extends String> m) {
        idMap.putAll(m);
    }

    @Override
    public void clear() {
        idMap.clear();
    }

    @Override
    public Set<Long> keySet() {
        return idMap.keySet();
    }

    @Override
    public Collection<String> values() {
        return idMap.values();
    }

    @Override
    public Set<Entry<Long, String>> entrySet() {
        return idMap.entrySet();
    }

    /**
     * For the most basic testing.
     *
     * @param args The command-line arguments
     */
    public static void main(final String[] args) {
        PropertyIDMap s1 = getSummaryInformationProperties();
        PropertyIDMap s2 = getDocumentSummaryInformationProperties();
        System.out.println("s1: " + s1);
        System.out.println("s2: " + s2);
    }
}
