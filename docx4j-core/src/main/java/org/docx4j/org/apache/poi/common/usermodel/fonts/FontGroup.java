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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Text runs can contain characters which will be handled (if configured) by a different font,
 * because the default font (latin) doesn't contain corresponding glyphs.
 *
 * @since POI 3.17-beta2
 *
 * @see <a href="https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/">Office Open XML Themes, Schemes, and Fonts</a>
 */
public enum FontGroup {
    /** type for latin charset (default) - also used for unicode fonts like MS Arial Unicode */
    LATIN,
    /** type for east asian charsets - usually set as fallback for the latin font, e.g. something like MS Gothic or MS Mincho */
    EAST_ASIAN,
    /** type for symbol fonts */
    SYMBOL,
    /** type for complex scripts - see https://msdn.microsoft.com/en-us/library/windows/desktop/dd317698 */
    COMPLEX_SCRIPT
    ;


    public static class FontGroupRange {
        private final FontGroup fontGroup;
        private int len = 0;

        FontGroupRange(FontGroup fontGroup) {
            this.fontGroup = fontGroup;
        }

        public int getLength() {
            return len;
        }

        public FontGroup getFontGroup( ) {
            return fontGroup;
        }

        void increaseLength(int len) {
            this.len += len;
        }
    }

    private static class Range {
        private final int upper;
        private final FontGroup fontGroup;
        Range(int upper, FontGroup fontGroup) {
            this.upper = upper;
            this.fontGroup = fontGroup;
        }

        int getUpper() { return upper; }
        FontGroup getFontGroup() { return fontGroup; }
    }

    private static NavigableMap<Integer,Range> UCS_RANGES;

    static {
        UCS_RANGES = new TreeMap<>();
        UCS_RANGES.put(0x0000,  new Range(0x007F, LATIN));
        UCS_RANGES.put(0x0080,  new Range(0x00A6, LATIN));
        UCS_RANGES.put(0x00A9,  new Range(0x00AF, LATIN));
        UCS_RANGES.put(0x00B2,  new Range(0x00B3, LATIN));
        UCS_RANGES.put(0x00B5,  new Range(0x00D6, LATIN));
        UCS_RANGES.put(0x00D8,  new Range(0x00F6, LATIN));
        UCS_RANGES.put(0x00F8,  new Range(0x058F, LATIN));
        UCS_RANGES.put(0x0590,  new Range(0x074F, COMPLEX_SCRIPT));
        UCS_RANGES.put(0x0780,  new Range(0x07BF, COMPLEX_SCRIPT));
        UCS_RANGES.put(0x0900,  new Range(0x109F, COMPLEX_SCRIPT));
        UCS_RANGES.put(0x10A0,  new Range(0x10FF, LATIN));
        UCS_RANGES.put(0x1200,  new Range(0x137F, LATIN));
        UCS_RANGES.put(0x13A0,  new Range(0x177F, LATIN));
        UCS_RANGES.put(0x1D00,  new Range(0x1D7F, LATIN));
        UCS_RANGES.put(0x1E00,  new Range(0x1FFF, LATIN));
        UCS_RANGES.put(0x1780,  new Range(0x18AF, COMPLEX_SCRIPT));
        UCS_RANGES.put(0x2000,  new Range(0x200B, LATIN));
        UCS_RANGES.put(0x200C,  new Range(0x200F, COMPLEX_SCRIPT));
        // For the quote characters in the range U+2018 - U+201E, use the East Asian font
        // if the text has one of the following language identifiers:
        // ii-CN, ja-JP, ko-KR, zh-CN,zh-HK, zh-MO, zh-SG, zh-TW
        UCS_RANGES.put(0x2010,  new Range(0x2029, LATIN));
        UCS_RANGES.put(0x202A,  new Range(0x202F, COMPLEX_SCRIPT));
        UCS_RANGES.put(0x2030,  new Range(0x2046, LATIN));
        UCS_RANGES.put(0x204A,  new Range(0x245F, LATIN));
        UCS_RANGES.put(0x2670,  new Range(0x2671, COMPLEX_SCRIPT));
        UCS_RANGES.put(0x27C0,  new Range(0x2BFF, LATIN));
        UCS_RANGES.put(0x3099,  new Range(0x309A, EAST_ASIAN));
        UCS_RANGES.put(0xD835,  new Range(0xD835, LATIN));
        UCS_RANGES.put(0xF000,  new Range(0xF0FF, SYMBOL));
        UCS_RANGES.put(0xFB00,  new Range(0xFB17, LATIN));
        UCS_RANGES.put(0xFB1D,  new Range(0xFB4F, COMPLEX_SCRIPT));
        UCS_RANGES.put(0xFE50,  new Range(0xFE6F, LATIN));
        // All others EAST_ASIAN
    }


    /**
     * Try to guess the font group based on the codepoint
     *
     * @param runText the text which font groups are to be analyzed
     * @return the FontGroup
     */
    public static List<FontGroupRange> getFontGroupRanges(final String runText) {
        final List<FontGroupRange> ttrList = new ArrayList<>();
        if (runText == null || runText.isEmpty()) {
            return ttrList;
        }
        FontGroupRange ttrLast = null;
        final int rlen = runText.length();
        for(int cp, i = 0, charCount; i < rlen; i += charCount) {
            cp = runText.codePointAt(i);
            charCount = Character.charCount(cp);

            // don't switch the font group for a few default characters supposedly available in all fonts
            final FontGroup tt;
            if (ttrLast != null && " \n\r".indexOf(cp) > -1) {
                tt = ttrLast.fontGroup;
            } else {
                tt = lookup(cp);
            }

            if (ttrLast == null || ttrLast.fontGroup != tt) {
                ttrLast = new FontGroupRange(tt);
                ttrList.add(ttrLast);
            }
            ttrLast.increaseLength(charCount);
        }
        return ttrList;
    }

    public static FontGroup getFontGroupFirst(String runText) {
        return (runText == null || runText.isEmpty()) ? LATIN : lookup(runText.codePointAt(0));
    }

    private static FontGroup lookup(int codepoint) {
        // Do a lookup for a match in UCS_RANGES
        Map.Entry<Integer, Range> entry = UCS_RANGES.floorEntry(codepoint);
        Range range = (entry != null) ? entry.getValue() : null;
        return (range != null && codepoint <= range.getUpper()) ? range.getFontGroup() : EAST_ASIAN;
    }
}