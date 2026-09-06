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

package org.docx4j.org.apache.poi.hwmf.record;

/**
 * A 16-bit unsigned integer that defines the brush style. The legal values for this
 * field are defined as follows: if the value is not BS_PATTERN, BS_DIBPATTERNPT MUST be
 * assumed.
 */
public enum HwmfBrushStyle {
    /**
     * A brush that paints a single, constant color, either solid or dithered.
     */
    BS_SOLID(0x0000),
    /**
     * A brush that does nothing. Using a BS_NULL brush in a graphics operation
     * MUST have the same effect as using no brush at all.
     */
    BS_NULL(0x0001),
    /**
     * A brush that paints a predefined simple pattern, or "hatch", onto a solid background.
     */
    BS_HATCHED(0x0002),
    /**
     * A brush that paints a pattern defined by a bitmap, which MAY be a Bitmap16
     * Object or a DeviceIndependentBitmap (DIB) Object.
     */
    BS_PATTERN(0x0003),
    /**
     * Not supported
     */
    BS_INDEXED(0x0004),
    /**
     * A pattern brush specified by a DIB.
     */
    BS_DIBPATTERN(0x0005),
    /**
     * A pattern brush specified by a DIB.
     */
    BS_DIBPATTERNPT(0x0006),
    /**
     * Not supported
     */
    BS_PATTERN8X8(0x0007),
    /**
     * Not supported
     */
    BS_DIBPATTERN8X8(0x0008),
    /**
     * Not supported
     */
    BS_MONOPATTERN(0x0009),
    /**
     * (POI arbitrary:) EMF/EMF+ specific value for linear gradient paint
     */
    BS_LINEAR_GRADIENT(0x0100);

    int flag;
    HwmfBrushStyle(int flag) {
        this.flag = flag;
    }

    public static HwmfBrushStyle valueOf(int flag) {
        for (HwmfBrushStyle bs : values()) {
            if (bs.flag == flag) return bs;
        }
        return null;
    }
}
