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

package org.docx4j.org.apache.poi.common.usermodel.fonts;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Charset represents the basic set of characters associated with a font (that it can display), and
 * corresponds to the ANSI codepage (8-bit or DBCS) of that character set used by a given language.
 *
 * @since POI 3.17-beta2
 */
@SuppressWarnings("java:S1192")
public enum FontCharset {
    /** Specifies the English character set. */
    ANSI(0x00000000, "Cp1252"),
    /**
     * Specifies a character set based on the current system locale;
     * for example, when the system locale is United States English,
     * the default character set is ANSI_CHARSET.
     */
    DEFAULT(0x00000001, "Cp1252"),
    /** Specifies a character set of symbols. */
    SYMBOL(0x00000002, ""),
    /** Specifies the Apple Macintosh character set. */
    MAC(0x0000004D, "MacRoman"),
    /** Specifies the Japanese character set. */
    SHIFTJIS(0x00000080, "Shift_JIS"),
    /** Also spelled "Hangeul". Specifies the Hangul Korean character set. */
    HANGUL(0x00000081, "cp949"),
    /** Also spelled "Johap". Specifies the Johab Korean character set. */
    JOHAB(0x00000082, "x-Johab"),
    /** Specifies the "simplified" Chinese character set for People's Republic of China. */
    GB2312(0x00000086, "GB2312"),
    /**
     * Specifies the "traditional" Chinese character set, used mostly in
     * Taiwan and in the Hong Kong and Macao Special Administrative Regions.
     */
    CHINESEBIG5(0x00000088, "Big5"),
    /** Specifies the Greek character set. */
    GREEK(0x000000A1, "Cp1253"),
    /** Specifies the Turkish character set. */
    TURKISH(0x000000A2, "Cp1254"),
    /** Specifies the Vietnamese character set. */
    VIETNAMESE(0x000000A3, "Cp1258"),
    /** Specifies the Hebrew character set. */
    HEBREW(0x000000B1, "Cp1255"),
    /** Specifies the Arabic character set. */
    ARABIC(0x000000B2, "Cp1256"),
    /** Specifies the Baltic (Northeastern European) character set. */
    BALTIC(0x000000BA, "Cp1257"),
    /** Specifies the Russian Cyrillic character set. */
    RUSSIAN(0x000000CC, "Cp1251"),
    /** Specifies the Thai character set. */
    THAI(0x000000DE, "x-windows-874"),
    /** Specifies a Eastern European character set. */
    EASTEUROPE(0x000000EE, "Cp1250"),
    /**
     * Specifies a mapping to one of the OEM code pages,
     * according to the current system locale setting.
     */
    OEM(0x000000FF, "Cp1252");

    private static FontCharset[] _table = new FontCharset[256];

    private int nativeId;
    private Charset charset;


    static {
        for (FontCharset c : values()) {
            _table[c.getNativeId()] = c;
        }
    }

    FontCharset(int flag, String javaCharsetName) {
        this.nativeId = flag;
        if (javaCharsetName.length() > 0) {
            try {
                charset = Charset.forName(javaCharsetName);
                return;
            } catch (UnsupportedCharsetException e) {
                Logger logger = LoggerFactory.getLogger(FontCharset.class);
                logger.atWarn().log("Unsupported charset: {}", javaCharsetName);
            }
        }
        charset = null;
    }

    /**
     *
     * @return charset for the font or <code>null</code> if there is no matching charset or
     *         if the charset is a &quot;default&quot;
     */
    public Charset getCharset() {
        return charset;
    }

    public int getNativeId() {
        return nativeId;
    }

    public static FontCharset valueOf(int value){
        return (value < 0 || value >= _table.length) ? null : _table[value];
    }
}