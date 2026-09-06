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

/**
 * A property of a font that describes its general appearance.
 * 
 * @since POI 3.17-beta2
 */
public enum FontFamily {
    /**
     * The default font is specified, which is implementation-dependent.
     */
    FF_DONTCARE (0x00),
    /**
     * Fonts with variable stroke widths, which are proportional to the actual widths of
     * the glyphs, and which have serifs. "MS Serif" is an example.
     */
    FF_ROMAN (0x01),
    /**
     * Fonts with variable stroke widths, which are proportional to the actual widths of the
     * glyphs, and which do not have serifs. "MS Sans Serif" is an example.
     */
    FF_SWISS (0x02),
    /**
     * Fonts with constant stroke width, with or without serifs. Fixed-width fonts are
     * usually modern. "Pica", "Elite", and "Courier New" are examples.
     */
    FF_MODERN (0x03),
    /**
     * Fonts designed to look like handwriting. "Script" and "Cursive" are examples.
     */
    FF_SCRIPT (0x04),
    /**
     * Novelty fonts. "Old English" is an example.
     */
    FF_DECORATIVE (0x05);
    
    private int nativeId;
    private FontFamily(int nativeId) {
        this.nativeId = nativeId;
    }
    
    public int getFlag() {
        return nativeId;
    }

    public static FontFamily valueOf(int nativeId) {
        for (FontFamily ff : values()) {
            if (ff.nativeId == nativeId) {
                return ff;
            }
        }
        return null;
    }

    /**
     * Get FontFamily from combined native id
     *
     * @param pitchAndFamily The PitchFamily to decode.
     *
     * @return The resulting FontFamily
     */
    public static FontFamily valueOfPitchFamily(byte pitchAndFamily) {
        return valueOf(pitchAndFamily >>> 4);
    }
}
