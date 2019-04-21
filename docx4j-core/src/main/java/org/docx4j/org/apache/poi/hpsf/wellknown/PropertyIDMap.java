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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>This is a dictionary which maps property ID values to property
 * ID strings.</p>
 *
 * <p>The methods {@link #getSummaryInformationProperties} and {@link
 * #getDocumentSummaryInformationProperties} return singleton {@link
 * PropertyIDMap}s. An application that wants to extend these maps
 * should treat them as unmodifiable, copy them and modifiy the
 * copies.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class PropertyIDMap extends HashMap<Long,String> {

    /*
     * The following definitions are for property IDs in the first
     * (and only) section of the Summary Information property set.
     */

    /** <p>ID of the property that denotes the document's title</p> */
    public static final int PID_TITLE = 2;

    /** <p>ID of the property that denotes the document's subject</p> */
    public static final int PID_SUBJECT = 3;

    /** <p>ID of the property that denotes the document's author</p> */
    public static final int PID_AUTHOR = 4;

    /** <p>ID of the property that denotes the document's keywords</p> */
    public static final int PID_KEYWORDS = 5;

    /** <p>ID of the property that denotes the document's comments</p> */
    public static final int PID_COMMENTS = 6;

    /** <p>ID of the property that denotes the document's template</p> */
    public static final int PID_TEMPLATE = 7;

    /** <p>ID of the property that denotes the document's last author</p> */
    public static final int PID_LASTAUTHOR = 8;

    /** <p>ID of the property that denotes the document's revision number</p> */
    public static final int PID_REVNUMBER = 9;

    /** <p>ID of the property that denotes the document's edit time</p> */
    public static final int PID_EDITTIME = 10;

    /** <p>ID of the property that denotes the date and time the document was
     * last printed</p> */
    public static final int PID_LASTPRINTED = 11;

    /** <p>ID of the property that denotes the date and time the document was
     * created.</p> */
    public static final int PID_CREATE_DTM = 12;

    /** <p>ID of the property that denotes the date and time the document was
     * saved</p> */
    public static final int PID_LASTSAVE_DTM = 13;

    /** <p>ID of the property that denotes the number of pages in the
     * document</p> */
    public static final int PID_PAGECOUNT = 14;

    /** <p>ID of the property that denotes the number of words in the
     * document</p> */
    public static final int PID_WORDCOUNT = 15;

    /** <p>ID of the property that denotes the number of characters in the
     * document</p> */
    public static final int PID_CHARCOUNT = 16;

    /** <p>ID of the property that denotes the document's thumbnail</p> */
    public static final int PID_THUMBNAIL = 17;

    /** <p>ID of the property that denotes the application that created the
     * document</p> */
    public static final int PID_APPNAME = 18;

    /** <p>ID of the property that denotes whether read/write access to the
     * document is allowed or whether is should be opened as read-only. It can
     * have the following values:</p>
     *
     * <table>
     *  <tbody>
     *   <tr>
     *    <th><p>Value</p></th>
     *    <th><p>Description</p></th>
     *   </tr>
     *   <tr>
     *    <th><p>0</p></th>
     *    <th><p>No restriction</p></th>
     *   </tr>
     *   <tr>
     *    <th><p>2</p></th>
     *    <th><p>Read-only recommended</p></th>
     *   </tr>
     *   <tr>
     *    <th><p>4</p></th>
     *    <th><p>Read-only enforced</p></th>
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
     * <p>The entry is a dictionary.</p>
     */
    public static final int PID_DICTIONARY = 0;

    /**
     * <p>The entry denotes a code page.</p>
     */
    public static final int PID_CODEPAGE = 1;

    /**
     * <p>The entry is a string denoting the category the file belongs
     * to, e.g. review, memo, etc. This is useful to find documents of
     * same type.</p>
     */
    public static final int PID_CATEGORY = 2;

    /**
     * <p>Target format for power point presentation, e.g. 35mm,
     * printer, video etc.</p>
     */
    public static final int PID_PRESFORMAT = 3;

    /**
     * <p>Number of bytes.</p>
     */
    public static final int PID_BYTECOUNT = 4;

    /**
     * <p>Number of lines.</p>
     */
    public static final int PID_LINECOUNT = 5;

    /**
     * <p>Number of paragraphs.</p>
     */
    public static final int PID_PARCOUNT = 6;

    /**
     * <p>Number of slides in a power point presentation.</p>
     */
    public static final int PID_SLIDECOUNT = 7;

    /**
     * <p>Number of slides with notes.</p>
     */
    public static final int PID_NOTECOUNT = 8;

    /**
     * <p>Number of hidden slides.</p>
     */
    public static final int PID_HIDDENCOUNT = 9;

    /**
     * <p>Number of multimedia clips, e.g. sound or video.</p>
     */
    public static final int PID_MMCLIPCOUNT = 10;

    /**
     * <p>This entry is set to -1 when scaling of the thumbnail is
     * desired. Otherwise the thumbnail should be cropped.</p>
     */
    public static final int PID_SCALE = 11;

    /**
     * <p>This entry denotes an internally used property. It is a
     * vector of variants consisting of pairs of a string (VT_LPSTR)
     * and a number (VT_I4). The string is a heading name, and the
     * number tells how many document parts are under that
     * heading.</p>
     */
    public static final int PID_HEADINGPAIR = 12;

    /**
     * <p>This entry contains the names of document parts (word: names
     * of the documents in the master document, excel: sheet names,
     * power point: slide titles, binder: document names).</p>
     */
    public static final int PID_DOCPARTS = 13;

    /**
     * <p>This entry contains the name of the project manager.</p>
     */
    public static final int PID_MANAGER = 14;

    /**
     * <p>This entry contains the company name.</p>
     */
    public static final int PID_COMPANY = 15;

    /**
     * <p>If this entry is -1 the links are dirty and should be
     * re-evaluated.</p>
     */
    public static final int PID_LINKSDIRTY = 16;

    /**
     * <p>The highest well-known property ID. Applications are free to use higher values for custom purposes.</p>
     */
    public static final int PID_MAX = PID_LINKSDIRTY;



    /**
     * <p>Contains the summary information property ID values and
     * associated strings. See the overall HPSF documentation for
     * details!</p>
     */
    private static PropertyIDMap summaryInformationProperties;

    /**
     * <p>Contains the summary information property ID values and
     * associated strings. See the overall HPSF documentation for
     * details!</p>
     */
    private static PropertyIDMap documentSummaryInformationProperties;



    /**
     * <p>Creates a {@link PropertyIDMap}.</p>
     *
     * @param initialCapacity The initial capacity as defined for
     * {@link HashMap}
     * @param loadFactor The load factor as defined for {@link HashMap}
     */
    public PropertyIDMap(final int initialCapacity, final float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }



    /**
     * <p>Creates a {@link PropertyIDMap} backed by another map.</p>
     *
     * @param map The instance to be created is backed by this map.
     */
    public PropertyIDMap(final Map<Long,String> map)
    {
        super(map);
    }



    /**
     * <p>Puts a ID string for an ID into the {@link
     * PropertyIDMap}.</p>
     *
     * @param id The ID.
     * @param idString The ID string.
     * @return As specified by the {@link java.util.Map} interface, this method
     * returns the previous value associated with the specified
     * <var>id</var>, or <code>null</code> if there was no mapping for
     * key.
     */
    public Object put(final long id, final String idString)
    {
        return put(Long.valueOf(id), idString);
    }



    /**
     * <p>Gets the ID string for an ID from the {@link
     * PropertyIDMap}.</p>
     *
     * @param id The ID.
     * @return The ID string associated with <var>id</var>.
     */
    public Object get(final long id)
    {
        return get(Long.valueOf(id));
    }



    /**
     * @return the Summary Information properties singleton
     */
    public static PropertyIDMap getSummaryInformationProperties()
    {
        if (summaryInformationProperties == null)
        {
            PropertyIDMap m = new PropertyIDMap(18, (float) 1.0);
            m.put(PID_TITLE, "PID_TITLE");
            m.put(PID_SUBJECT, "PID_SUBJECT");
            m.put(PID_AUTHOR, "PID_AUTHOR");
            m.put(PID_KEYWORDS, "PID_KEYWORDS");
            m.put(PID_COMMENTS, "PID_COMMENTS");
            m.put(PID_TEMPLATE, "PID_TEMPLATE");
            m.put(PID_LASTAUTHOR, "PID_LASTAUTHOR");
            m.put(PID_REVNUMBER, "PID_REVNUMBER");
            m.put(PID_EDITTIME, "PID_EDITTIME");
            m.put(PID_LASTPRINTED, "PID_LASTPRINTED");
            m.put(PID_CREATE_DTM, "PID_CREATE_DTM");
            m.put(PID_LASTSAVE_DTM, "PID_LASTSAVE_DTM");
            m.put(PID_PAGECOUNT, "PID_PAGECOUNT");
            m.put(PID_WORDCOUNT, "PID_WORDCOUNT");
            m.put(PID_CHARCOUNT, "PID_CHARCOUNT");
            m.put(PID_THUMBNAIL, "PID_THUMBNAIL");
            m.put(PID_APPNAME, "PID_APPNAME");
            m.put(PID_SECURITY, "PID_SECURITY");
            summaryInformationProperties =
                new PropertyIDMap(Collections.unmodifiableMap(m));
        }
        return summaryInformationProperties;
    }



    /**
     * <p>Returns the Document Summary Information properties
     * singleton.</p>
     *
     * @return The Document Summary Information properties singleton.
     */
    public static PropertyIDMap getDocumentSummaryInformationProperties()
    {
        if (documentSummaryInformationProperties == null)
        {
            PropertyIDMap m = new PropertyIDMap(17, (float) 1.0);
            m.put(PID_DICTIONARY, "PID_DICTIONARY");
            m.put(PID_CODEPAGE, "PID_CODEPAGE");
            m.put(PID_CATEGORY, "PID_CATEGORY");
            m.put(PID_PRESFORMAT, "PID_PRESFORMAT");
            m.put(PID_BYTECOUNT, "PID_BYTECOUNT");
            m.put(PID_LINECOUNT, "PID_LINECOUNT");
            m.put(PID_PARCOUNT, "PID_PARCOUNT");
            m.put(PID_SLIDECOUNT, "PID_SLIDECOUNT");
            m.put(PID_NOTECOUNT, "PID_NOTECOUNT");
            m.put(PID_HIDDENCOUNT, "PID_HIDDENCOUNT");
            m.put(PID_MMCLIPCOUNT, "PID_MMCLIPCOUNT");
            m.put(PID_SCALE, "PID_SCALE");
            m.put(PID_HEADINGPAIR, "PID_HEADINGPAIR");
            m.put(PID_DOCPARTS, "PID_DOCPARTS");
            m.put(PID_MANAGER, "PID_MANAGER");
            m.put(PID_COMPANY, "PID_COMPANY");
            m.put(PID_LINKSDIRTY, "PID_LINKSDIRTY");
            documentSummaryInformationProperties =
                new PropertyIDMap(Collections.unmodifiableMap(m));
        }
        return documentSummaryInformationProperties;
    }



    /**
     * <p>For the most basic testing.</p>
     *
     * @param args The command-line arguments
     */
    public static void main(final String[] args)
    {
        PropertyIDMap s1 = getSummaryInformationProperties();
        PropertyIDMap s2 = getDocumentSummaryInformationProperties();
        System.out.println("s1: " + s1);
        System.out.println("s2: " + s2);
    }
}
