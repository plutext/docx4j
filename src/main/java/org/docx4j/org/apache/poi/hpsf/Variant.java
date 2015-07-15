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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The <em>Variant</em> types as defined by Microsoft's COM. I
 * found this information in <a
 * href="http://www.marin.clara.net/COM/variant_type_definitions.htm">
 * http://www.marin.clara.net/COM/variant_type_definitions.htm</a>.</p>
 *
 * <p>In the variant types descriptions the following shortcuts are
 * used: <strong> [V]</strong> - may appear in a VARIANT,
 * <strong>[T]</strong> - may appear in a TYPEDESC,
 * <strong>[P]</strong> - may appear in an OLE property set,
 * <strong>[S]</strong> - may appear in a Safe Array.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public class Variant
{

    /**
     * <p>[V][P] Nothing, i.e. not a single byte of data.</p>
     */
    public static final int VT_EMPTY = 0;

    /**
     * <p>[V][P] SQL style Null.</p>
     */
    public static final int VT_NULL = 1;

    /**
     * <p>[V][T][P][S] 2 byte signed int.</p>
     */
    public static final int VT_I2 = 2;

    /**
     * <p>[V][T][P][S] 4 byte signed int.</p>
     */
    public static final int VT_I4 = 3;

    /**
     * <p>[V][T][P][S] 4 byte real.</p>
     */
    public static final int VT_R4 = 4;

    /**
     * <p>[V][T][P][S] 8 byte real.</p>
     */
    public static final int VT_R8 = 5;

    /**
     * <p>[V][T][P][S] currency. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_CY = 6;

    /**
     * <p>[V][T][P][S] date. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_DATE = 7;

    /**
     * <p>[V][T][P][S] OLE Automation string. <span
     * style="background-color: #ffff00">How long is this? How is it
     * to be interpreted?</span></p>
     */
    public static final int VT_BSTR = 8;

    /**
     * <p>[V][T][P][S] IDispatch *. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_DISPATCH = 9;

    /**
     * <p>[V][T][S] SCODE. <span style="background-color: #ffff00">How
     * long is this? How is it to be interpreted?</span></p>
     */
    public static final int VT_ERROR = 10;

    /**
     * <p>[V][T][P][S] True=-1, False=0.</p>
     */
    public static final int VT_BOOL = 11;

    /**
     * <p>[V][T][P][S] VARIANT *. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_VARIANT = 12;

    /**
     * <p>[V][T][S] IUnknown *. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_UNKNOWN = 13;

    /**
     * <p>[V][T][S] 16 byte fixed point.</p>
     */
    public static final int VT_DECIMAL = 14;

    /**
     * <p>[T] signed char.</p>
     */
    public static final int VT_I1 = 16;

    /**
     * <p>[V][T][P][S] unsigned char.</p>
     */
    public static final int VT_UI1 = 17;

    /**
     * <p>[T][P] unsigned short.</p>
     */
    public static final int VT_UI2 = 18;

    /**
     * <p>[T][P] unsigned int.</p>
     */
    public static final int VT_UI4 = 19;

    /**
     * <p>[T][P] signed 64-bit int.</p>
     */
    public static final int VT_I8 = 20;

    /**
     * <p>[T][P] unsigned 64-bit int.</p>
     */
    public static final int VT_UI8 = 21;

    /**
     * <p>[T] signed machine int.</p>
     */
    public static final int VT_INT = 22;

    /**
     * <p>[T] unsigned machine int.</p>
     */
    public static final int VT_UINT = 23;

    /**
     * <p>[T] C style void.</p>
     */
    public static final int VT_VOID = 24;

    /**
     * <p>[T] Standard return type. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_HRESULT = 25;

    /**
     * <p>[T] pointer type. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_PTR = 26;

    /**
     * <p>[T] (use VT_ARRAY in VARIANT).</p>
     */
    public static final int VT_SAFEARRAY = 27;

    /**
     * <p>[T] C style array. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_CARRAY = 28;

    /**
     * <p>[T] user defined type. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_USERDEFINED = 29;

    /**
     * <p>[T][P] null terminated string.</p>
     */
    public static final int VT_LPSTR = 30;

    /**
     * <p>[T][P] wide (Unicode) null terminated string.</p>
     */
    public static final int VT_LPWSTR = 31;

    /**
     * <p>[P] FILETIME. The FILETIME structure holds a date and time
     * associated with a file. The structure identifies a 64-bit
     * integer specifying the number of 100-nanosecond intervals which
     * have passed since January 1, 1601. This 64-bit value is split
     * into the two dwords stored in the structure.</p>
     */
    public static final int VT_FILETIME = 64;

    /**
     * <p>[P] Length prefixed bytes.</p>
     */
    public static final int VT_BLOB = 65;

    /**
     * <p>[P] Name of the stream follows.</p>
     */
    public static final int VT_STREAM = 66;

    /**
     * <p>[P] Name of the storage follows.</p>
     */
    public static final int VT_STORAGE = 67;

    /**
     * <p>[P] Stream contains an object. <span
     * style="background-color: #ffff00"> How long is this? How is it
     * to be interpreted?</span></p>
     */
    public static final int VT_STREAMED_OBJECT = 68;

    /**
     * <p>[P] Storage contains an object. <span
     * style="background-color: #ffff00"> How long is this? How is it
     * to be interpreted?</span></p>
     */
    public static final int VT_STORED_OBJECT = 69;

    /**
     * <p>[P] Blob contains an object. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_BLOB_OBJECT = 70;

    /**
     * <p>[P] Clipboard format. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_CF = 71;

    /**
     * <p>[P] A Class ID.</p>
     *
     * <p>It consists of a 32 bit unsigned integer indicating the size
     * of the structure, a 32 bit signed integer indicating (Clipboard
     * Format Tag) indicating the type of data that it contains, and
     * then a byte array containing the data.</p>
     *
     * <p>The valid Clipboard Format Tags are:</p>
     *
     * <ul>
     *  <li>{@link Thumbnail#CFTAG_WINDOWS}</li>
     *  <li>{@link Thumbnail#CFTAG_MACINTOSH}</li>
     *  <li>{@link Thumbnail#CFTAG_NODATA}</li>
     *  <li>{@link Thumbnail#CFTAG_FMTID}</li>
     * </ul>
     *
     * <pre>typedef struct tagCLIPDATA {
     * // cbSize is the size of the buffer pointed to
     * // by pClipData, plus sizeof(ulClipFmt)
     * ULONG              cbSize;
     * long               ulClipFmt;
     * BYTE*              pClipData;
     * } CLIPDATA;</pre>
     *
     * <p>See <a
     * href="msdn.microsoft.com/library/en-us/com/stgrstrc_0uwk.asp"
     * target="_blank">
     * msdn.microsoft.com/library/en-us/com/stgrstrc_0uwk.asp</a>.</p>
     */
    public static final int VT_CLSID = 72;

    /**
     * "MUST be a VersionedStream. The storage representing the (non-simple)
     * property set MUST have a stream element with the name in the StreamName
     * field." -- [MS-OLEPS] -- v20110920; Object Linking and Embedding (OLE)
     * Property Set Data Structures; page 24 / 63
     */
    public static final int VT_VERSIONED_STREAM = 0x0049;

    /**
     * <p>[P] simple counted array. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_VECTOR = 0x1000;

    /**
     * <p>[V] SAFEARRAY*. <span style="background-color: #ffff00">How
     * long is this? How is it to be interpreted?</span></p>
     */
    public static final int VT_ARRAY = 0x2000;

    /**
     * <p>[V] void* for local use. <span style="background-color:
     * #ffff00">How long is this? How is it to be
     * interpreted?</span></p>
     */
    public static final int VT_BYREF = 0x4000;

    /**
     * <p>FIXME (3): Document this!</p>
     */
    public static final int VT_RESERVED = 0x8000;

    /**
     * <p>FIXME (3): Document this!</p>
     */
    public static final int VT_ILLEGAL = 0xFFFF;

    /**
     * <p>FIXME (3): Document this!</p>
     */
    public static final int VT_ILLEGALMASKED = 0xFFF;

    /**
     * <p>FIXME (3): Document this!</p>
     */
    public static final int VT_TYPEMASK = 0xFFF;



    /**
     * <p>Maps the numbers denoting the variant types to their corresponding
     * variant type names.</p>
     */
    private static Map<Long,String> numberToName;

    private static Map<Long,Integer> numberToLength;

    /**
     * <p>Denotes a variant type with a length that is unknown to HPSF yet.</p>
     */
    public static final Integer LENGTH_UNKNOWN = Integer.valueOf(-2);

    /**
     * <p>Denotes a variant type with a variable length.</p>
     */
    public static final Integer LENGTH_VARIABLE = Integer.valueOf(-1);

    /**
     * <p>Denotes a variant type with a length of 0 bytes.</p>
     */
    public static final Integer LENGTH_0 = Integer.valueOf(0);

    /**
     * <p>Denotes a variant type with a length of 2 bytes.</p>
     */
    public static final Integer LENGTH_2 = Integer.valueOf(2);

    /**
     * <p>Denotes a variant type with a length of 4 bytes.</p>
     */
    public static final Integer LENGTH_4 = Integer.valueOf(4);

    /**
     * <p>Denotes a variant type with a length of 8 bytes.</p>
     */
    public static final Integer LENGTH_8 = Integer.valueOf(8);



    static
    {
        /* Initialize the number-to-name map: */
        Map<Long,String> tm1 = new HashMap<Long,String>();
        tm1.put(Long.valueOf(0), "VT_EMPTY");
        tm1.put(Long.valueOf(1), "VT_NULL");
        tm1.put(Long.valueOf(2), "VT_I2");
        tm1.put(Long.valueOf(3), "VT_I4");
        tm1.put(Long.valueOf(4), "VT_R4");
        tm1.put(Long.valueOf(5), "VT_R8");
        tm1.put(Long.valueOf(6), "VT_CY");
        tm1.put(Long.valueOf(7), "VT_DATE");
        tm1.put(Long.valueOf(8), "VT_BSTR");
        tm1.put(Long.valueOf(9), "VT_DISPATCH");
        tm1.put(Long.valueOf(10), "VT_ERROR");
        tm1.put(Long.valueOf(11), "VT_BOOL");
        tm1.put(Long.valueOf(12), "VT_VARIANT");
        tm1.put(Long.valueOf(13), "VT_UNKNOWN");
        tm1.put(Long.valueOf(14), "VT_DECIMAL");
        tm1.put(Long.valueOf(16), "VT_I1");
        tm1.put(Long.valueOf(17), "VT_UI1");
        tm1.put(Long.valueOf(18), "VT_UI2");
        tm1.put(Long.valueOf(19), "VT_UI4");
        tm1.put(Long.valueOf(20), "VT_I8");
        tm1.put(Long.valueOf(21), "VT_UI8");
        tm1.put(Long.valueOf(22), "VT_INT");
        tm1.put(Long.valueOf(23), "VT_UINT");
        tm1.put(Long.valueOf(24), "VT_VOID");
        tm1.put(Long.valueOf(25), "VT_HRESULT");
        tm1.put(Long.valueOf(26), "VT_PTR");
        tm1.put(Long.valueOf(27), "VT_SAFEARRAY");
        tm1.put(Long.valueOf(28), "VT_CARRAY");
        tm1.put(Long.valueOf(29), "VT_USERDEFINED");
        tm1.put(Long.valueOf(30), "VT_LPSTR");
        tm1.put(Long.valueOf(31), "VT_LPWSTR");
        tm1.put(Long.valueOf(64), "VT_FILETIME");
        tm1.put(Long.valueOf(65), "VT_BLOB");
        tm1.put(Long.valueOf(66), "VT_STREAM");
        tm1.put(Long.valueOf(67), "VT_STORAGE");
        tm1.put(Long.valueOf(68), "VT_STREAMED_OBJECT");
        tm1.put(Long.valueOf(69), "VT_STORED_OBJECT");
        tm1.put(Long.valueOf(70), "VT_BLOB_OBJECT");
        tm1.put(Long.valueOf(71), "VT_CF");
        tm1.put(Long.valueOf(72), "VT_CLSID");
        Map<Long,String> tm2 = new HashMap<Long,String>(tm1.size(), 1.0F);
        tm2.putAll(tm1);
        numberToName = Collections.unmodifiableMap(tm2);

        /* Initialize the number-to-length map: */
        Map<Long,Integer> tm3 = new HashMap<Long,Integer>();
        tm3.put(Long.valueOf(0), LENGTH_0);
        tm3.put(Long.valueOf(1), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(2), LENGTH_2);
        tm3.put(Long.valueOf(3), LENGTH_4);
        tm3.put(Long.valueOf(4), LENGTH_4);
        tm3.put(Long.valueOf(5), LENGTH_8);
        tm3.put(Long.valueOf(6), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(7), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(8), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(9), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(10), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(11), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(12), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(13), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(14), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(16), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(17), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(18), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(19), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(20), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(21), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(22), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(23), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(24), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(25), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(26), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(27), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(28), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(29), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(30), LENGTH_VARIABLE);
        tm3.put(Long.valueOf(31), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(64), LENGTH_8);
        tm3.put(Long.valueOf(65), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(66), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(67), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(68), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(69), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(70), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(71), LENGTH_UNKNOWN);
        tm3.put(Long.valueOf(72), LENGTH_UNKNOWN);
        Map<Long,Integer> tm4 = new HashMap<Long,Integer>(tm1.size(), 1.0F);
        tm4.putAll(tm3);
        numberToLength = Collections.unmodifiableMap(tm4);
    }



    /**
     * <p>Returns the variant type name associated with a variant type
     * number.</p>
     *
     * @param variantType The variant type number
     * @return The variant type name or the string "unknown variant type"
     */
    public static String getVariantName(final long variantType)
    {
        final String name = numberToName.get(Long.valueOf(variantType));
        return name != null ? name : "unknown variant type";
    }

    /**
     * <p>Returns a variant type's length.</p>
     *
     * @param variantType The variant type number
     * @return The length of the variant type's data in bytes. If the length is
     * variable, i.e. the length of a string, -1 is returned. If HPSF does not
     * know the length, -2 is returned. The latter usually indicates an
     * unsupported variant type.
     */
    public static int getVariantLength(final long variantType)
    {
        final Long key = Long.valueOf((int) variantType);
        final Integer length = numberToLength.get(key);
        if (length == null)
            return -2;
        return length.intValue();
    }

}
