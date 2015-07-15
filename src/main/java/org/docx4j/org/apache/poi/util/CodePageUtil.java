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

package org.docx4j.org.apache.poi.util;

import java.io.UnsupportedEncodingException;

/**
 * Utilities for working with Microsoft CodePages.
 * 
 * <p>Provides constants for understanding numeric codepages,
 *  along with utilities to translate these into Java Character Sets.</p>
 */
public class CodePageUtil
{
    /** <p>Codepage 037, a special case</p> */
    public static final int CP_037 = 37;

    /** <p>Codepage for SJIS</p> */
    public static final int CP_SJIS = 932;

    /** <p>Codepage for GBK, aka MS936</p> */
    public static final int CP_GBK = 936;

    /** <p>Codepage for MS949</p> */
    public static final int CP_MS949 = 949;

    /** <p>Codepage for UTF-16</p> */
    public static final int CP_UTF16 = 1200;

    /** <p>Codepage for UTF-16 big-endian</p> */
    public static final int CP_UTF16_BE = 1201;

    /** <p>Codepage for Windows 1250</p> */
    public static final int CP_WINDOWS_1250 = 1250;

    /** <p>Codepage for Windows 1251</p> */
    public static final int CP_WINDOWS_1251 = 1251;

    /** <p>Codepage for Windows 1252</p> */
    public static final int CP_WINDOWS_1252 = 1252;
    public static final int CP_WINDOWS_1252_BIFF23 = 32769;

    /** <p>Codepage for Windows 1253</p> */
    public static final int CP_WINDOWS_1253 = 1253;

    /** <p>Codepage for Windows 1254</p> */
    public static final int CP_WINDOWS_1254 = 1254;

    /** <p>Codepage for Windows 1255</p> */
    public static final int CP_WINDOWS_1255 = 1255;

    /** <p>Codepage for Windows 1256</p> */
    public static final int CP_WINDOWS_1256 = 1256;

    /** <p>Codepage for Windows 1257</p> */
    public static final int CP_WINDOWS_1257 = 1257;

    /** <p>Codepage for Windows 1258</p> */
    public static final int CP_WINDOWS_1258 = 1258;

    /** <p>Codepage for Johab</p> */
    public static final int CP_JOHAB = 1361;

    /** <p>Codepage for Macintosh Roman (Java: MacRoman)</p> */
    public static final int CP_MAC_ROMAN = 10000;
    public static final int CP_MAC_ROMAN_BIFF23 = 32768;

    /** <p>Codepage for Macintosh Japan (Java: unknown - use SJIS, cp942 or
     * cp943)</p> */
    public static final int CP_MAC_JAPAN = 10001;

    /** <p>Codepage for Macintosh Chinese Traditional (Java: unknown - use Big5,
     * MS950, or cp937)</p> */
    public static final int CP_MAC_CHINESE_TRADITIONAL = 10002;

    /** <p>Codepage for Macintosh Korean (Java: unknown - use EUC_KR or
     * cp949)</p> */
    public static final int CP_MAC_KOREAN = 10003;

    /** <p>Codepage for Macintosh Arabic (Java: MacArabic)</p> */
    public static final int CP_MAC_ARABIC = 10004;

    /** <p>Codepage for Macintosh Hebrew (Java: MacHebrew)</p> */
    public static final int CP_MAC_HEBREW = 10005;

    /** <p>Codepage for Macintosh Greek (Java: MacGreek)</p> */
    public static final int CP_MAC_GREEK = 10006;

    /** <p>Codepage for Macintosh Cyrillic (Java: MacCyrillic)</p> */
    public static final int CP_MAC_CYRILLIC = 10007;

    /** <p>Codepage for Macintosh Chinese Simplified (Java: unknown - use
     * EUC_CN, ISO2022_CN_GB, MS936 or cp935)</p> */
    public static final int CP_MAC_CHINESE_SIMPLE = 10008;

    /** <p>Codepage for Macintosh Romanian (Java: MacRomania)</p> */
    public static final int CP_MAC_ROMANIA = 10010;

    /** <p>Codepage for Macintosh Ukrainian (Java: MacUkraine)</p> */
    public static final int CP_MAC_UKRAINE = 10017;

    /** <p>Codepage for Macintosh Thai (Java: MacThai)</p> */
    public static final int CP_MAC_THAI = 10021;

    /** <p>Codepage for Macintosh Central Europe (Latin-2)
     * (Java: MacCentralEurope)</p> */
    public static final int CP_MAC_CENTRAL_EUROPE = 10029;

    /** <p>Codepage for Macintosh Iceland (Java: MacIceland)</p> */
    public static final int CP_MAC_ICELAND = 10079;

    /** <p>Codepage for Macintosh Turkish (Java: MacTurkish)</p> */
    public static final int CP_MAC_TURKISH = 10081;

    /** <p>Codepage for Macintosh Croatian (Java: MacCroatian)</p> */
    public static final int CP_MAC_CROATIAN = 10082;

    /** <p>Codepage for US-ASCII</p> */
    public static final int CP_US_ACSII = 20127;

    /** <p>Codepage for KOI8-R</p> */
    public static final int CP_KOI8_R = 20866;

    /** <p>Codepage for ISO-8859-1</p> */
    public static final int CP_ISO_8859_1 = 28591;

    /** <p>Codepage for ISO-8859-2</p> */
    public static final int CP_ISO_8859_2 = 28592;

    /** <p>Codepage for ISO-8859-3</p> */
    public static final int CP_ISO_8859_3 = 28593;

    /** <p>Codepage for ISO-8859-4</p> */
    public static final int CP_ISO_8859_4 = 28594;

    /** <p>Codepage for ISO-8859-5</p> */
    public static final int CP_ISO_8859_5 = 28595;

    /** <p>Codepage for ISO-8859-6</p> */
    public static final int CP_ISO_8859_6 = 28596;

    /** <p>Codepage for ISO-8859-7</p> */
    public static final int CP_ISO_8859_7 = 28597;

    /** <p>Codepage for ISO-8859-8</p> */
    public static final int CP_ISO_8859_8 = 28598;

    /** <p>Codepage for ISO-8859-9</p> */
    public static final int CP_ISO_8859_9 = 28599;

    /** <p>Codepage for ISO-2022-JP</p> */
    public static final int CP_ISO_2022_JP1 = 50220;

    /** <p>Another codepage for ISO-2022-JP</p> */
    public static final int CP_ISO_2022_JP2 = 50221;

    /** <p>Yet another codepage for ISO-2022-JP</p> */
    public static final int CP_ISO_2022_JP3 = 50222;

    /** <p>Codepage for ISO-2022-KR</p> */
    public static final int CP_ISO_2022_KR = 50225;

    /** <p>Codepage for EUC-JP</p> */
    public static final int CP_EUC_JP = 51932;

    /** <p>Codepage for EUC-KR</p> */
    public static final int CP_EUC_KR = 51949;

    /** <p>Codepage for GB2312</p> */
    public static final int CP_GB2312 = 52936;

    /** <p>Codepage for GB18030</p> */
    public static final int CP_GB18030 = 54936;

    /** <p>Another codepage for US-ASCII</p> */
    public static final int CP_US_ASCII2 = 65000;

    /** <p>Codepage for UTF-8</p> */
    public static final int CP_UTF8 = 65001;

    /** <p>Codepage for Unicode</p> */
    public static final int CP_UNICODE = CP_UTF16;
    
    /**
     * Converts a string into bytes, in the equivalent character encoding
     *  to the supplied codepage number.
     * @param string The string to convert
     * @param codepage The codepage number
     */
    public static byte[] getBytesInCodePage(final String string, final int codepage) 
    throws UnsupportedEncodingException 
    {
        String encoding = codepageToEncoding(codepage);
        return string.getBytes(encoding);
    }

    /**
     * Converts the bytes into a String, based on the equivalent character encoding
     *  to the supplied codepage number.
     * @param string The byte of the string to convert
     * @param codepage The codepage number
     */
    public static String getStringFromCodePage(final byte[] string, final int codepage)
    throws UnsupportedEncodingException 
    {
        return getStringFromCodePage(string, 0, string.length, codepage);
    }
    
    /**
     * Converts the bytes into a String, based on the equivalent character encoding
     *  to the supplied codepage number.
     * @param string The byte of the string to convert
     * @param codepage The codepage number
     */
    public static String getStringFromCodePage(final byte[] string, final int offset, 
            final int length, final int codepage) throws UnsupportedEncodingException 
    {
        String encoding = codepageToEncoding(codepage);
        return new String(string, offset, length, encoding);
    }
    
    /**
     * <p>Turns a codepage number into the equivalent character encoding's
     * name (in Java NIO canonical naming format).</p>
     *
     * @param codepage The codepage number
     *
     * @return The character encoding's name. If the codepage number is 65001,
     * the encoding name is "UTF-8". All other positive numbers are mapped to
     * their Java NIO names, normally either "windows-" followed by the number, 
     * eg "windows-1251", or "cp" followed by the number, e.g. if the codepage 
     * number is 1252 the returned character encoding name will be "cp1252".
     *
     * @exception UnsupportedEncodingException if the specified codepage is
     * less than zero.
     */
    public static String codepageToEncoding(final int codepage)
    throws UnsupportedEncodingException
    {
        return codepageToEncoding(codepage, false);
    }
    
    /**
     * <p>Turns a codepage number into the equivalent character encoding's
     * name, in either Java NIO or Java Lang canonical naming.</p>
     *
     * @param codepage The codepage number
     * @param javaLangFormat Should Java Lang or Java NIO naming be used?
     *
     * @return The character encoding's name, in either Java Lang format 
     *  (eg Cp1251, ISO8859_5) or Java NIO format (eg windows-1252, ISO-8859-9)
     *  
     * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/intl/encoding.doc.html">Supported Encodings</a>
     *
     * @exception UnsupportedEncodingException if the specified codepage is
     * less than zero.
     */
    public static String codepageToEncoding(final int codepage, boolean javaLangFormat)
    throws UnsupportedEncodingException
    {
        if (codepage <= 0)
            throw new UnsupportedEncodingException("Codepage number may not be " + codepage);
        
        switch (codepage) {
            case CP_UTF16:
                return "UTF-16";
            case CP_UTF16_BE:
                return "UTF-16BE";
            case CP_UTF8:
                return "UTF-8";
            case CP_037:
                return "cp037";
            case CP_GBK:
                return "GBK";
            case CP_MS949:
                return "ms949";
            case CP_WINDOWS_1250:
                if (javaLangFormat)
                    return "Cp1250";
                else
                    return "windows-1250";
            case CP_WINDOWS_1251:
                if (javaLangFormat)
                    return "Cp1251";
                else
                    return "windows-1251";
            case CP_WINDOWS_1252:
            case CP_WINDOWS_1252_BIFF23:
                if (javaLangFormat)
                    return "Cp1252";
                else
                    return "windows-1252";
            case CP_WINDOWS_1253:
                if (javaLangFormat)
                    return "Cp1253";
                else
                    return "windows-1253";
            case CP_WINDOWS_1254:
                if (javaLangFormat)
                    return "Cp1254";
                else
                    return "windows-1254";
            case CP_WINDOWS_1255:
                if (javaLangFormat)
                    return "Cp1255";
                else
                    return "windows-1255";
            case CP_WINDOWS_1256:
                if (javaLangFormat)
                    return "Cp1255";
                else
                    return "windows-1256";
            case CP_WINDOWS_1257:
                if (javaLangFormat)
                    return "Cp1257";
                else
                    return "windows-1257";
            case CP_WINDOWS_1258:
                if (javaLangFormat)
                    return "Cp1258";
                else
                    return "windows-1258";
            case CP_JOHAB:
                return "johab";
            case CP_MAC_ROMAN:
            case CP_MAC_ROMAN_BIFF23:
                return "MacRoman";
            case CP_MAC_JAPAN:
                return "SJIS";
            case CP_MAC_CHINESE_TRADITIONAL:
                return "Big5";
            case CP_MAC_KOREAN:
                return "EUC-KR";
            case CP_MAC_ARABIC:
                return "MacArabic";
            case CP_MAC_HEBREW:
                return "MacHebrew";
            case CP_MAC_GREEK:
                return "MacGreek";
            case CP_MAC_CYRILLIC:
                return "MacCyrillic";
            case CP_MAC_CHINESE_SIMPLE:
                return "EUC_CN";
            case CP_MAC_ROMANIA:
                return "MacRomania";
            case CP_MAC_UKRAINE:
                return "MacUkraine";
            case CP_MAC_THAI:
                return "MacThai";
            case CP_MAC_CENTRAL_EUROPE:
                return "MacCentralEurope";
            case CP_MAC_ICELAND:
                  return "MacIceland";
            case CP_MAC_TURKISH:
                return "MacTurkish";
            case CP_MAC_CROATIAN:
                return "MacCroatian";
            case CP_US_ACSII:
            case CP_US_ASCII2:
                return "US-ASCII";
            case CP_KOI8_R:
                return "KOI8-R";
            case CP_ISO_8859_1:
                if (javaLangFormat)
                    return "ISO8859_1";
                else
                    return "ISO-8859-1";
            case CP_ISO_8859_2:
                if (javaLangFormat)
                    return "ISO8859_2";
                else
                    return "ISO-8859-2";
            case CP_ISO_8859_3:
                if (javaLangFormat)
                    return "ISO8859_3";
                else
                    return "ISO-8859-3";
            case CP_ISO_8859_4:
                if (javaLangFormat)
                    return "ISO8859_4";
                else
                    return "ISO-8859-4";
            case CP_ISO_8859_5:
                if (javaLangFormat)
                    return "ISO8859_5";
                else
                    return "ISO-8859-5";
            case CP_ISO_8859_6:
                if (javaLangFormat)
                    return "ISO8859_6";
                else
                    return "ISO-8859-6";
            case CP_ISO_8859_7:
                if (javaLangFormat)
                    return "ISO8859_7";
                else
                    return "ISO-8859-7";
            case CP_ISO_8859_8:
                if (javaLangFormat)
                    return "ISO8859_8";
                else
                    return "ISO-8859-8";
            case CP_ISO_8859_9:
                if (javaLangFormat)
                    return "ISO8859_9";
                else
                    return "ISO-8859-9";
            case CP_ISO_2022_JP1:
            case CP_ISO_2022_JP2:
            case CP_ISO_2022_JP3:
                return "ISO-2022-JP";
            case CP_ISO_2022_KR:
                return "ISO-2022-KR";
            case CP_EUC_JP:
                return "EUC-JP";
            case CP_EUC_KR:
                return "EUC-KR";
            case CP_GB2312:
                return "GB2312";
            case CP_GB18030:
                return "GB18030";
            case CP_SJIS:
                return "SJIS";
            default:
                return "cp" + codepage;
        }
    }
}
