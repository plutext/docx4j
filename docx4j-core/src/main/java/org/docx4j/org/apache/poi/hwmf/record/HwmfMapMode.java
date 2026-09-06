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
 * A 16-bit unsigned integer that defines the mapping mode.
 *
 * The MapMode defines how logical units are mapped to physical units;
 * that is, assuming that the origins in both the logical and physical coordinate systems
 * are at the same point on the drawing surface, what is the physical coordinate (x',y')
 * that corresponds to logical coordinate (x,y).
 *
 * For example, suppose the mapping mode is MM_TEXT. Given the following definition of that
 * mapping mode, and an origin (0,0) at the top left corner of the drawing surface, logical
 * coordinate (4,5) would map to physical coordinate (4,5) in pixels.
 *
 * Now suppose the mapping mode is MM_LOENGLISH, with the same origin as the previous
 * example. Given the following definition of that mapping mode, logical coordinate (4,-5)
 * would map to physical coordinate (0.04,0.05) in inches.
 */
public enum HwmfMapMode {
    /**
     *  Each logical unit is mapped to one device pixel.
     *  Positive x is to the right; positive y is down.
     */
    MM_TEXT(0x0001, 0),
    
    /**
     *  Each logical unit is mapped to 0.1 millimeter.
     *  Positive x is to the right; positive y is up.
     */
    MM_LOMETRIC(0x0002, 254),
    
    /**
     *  Each logical unit is mapped to 0.01 millimeter.
     *  Positive x is to the right; positive y is up.
     */
    MM_HIMETRIC(0x0003, 2540),
    
    /**
     *  Each logical unit is mapped to 0.01 inch.
     *  Positive x is to the right; positive y is up.
     */
    MM_LOENGLISH(0x0004, 100),
    
    /**
     *  Each logical unit is mapped to 0.001 inch.
     *  Positive x is to the right; positive y is up.
     */
    MM_HIENGLISH(0x0005, 1000),
    
    /**
     *  Each logical unit is mapped to one twentieth (1/20) of a point.
     *  In printing, a point is 1/72 of an inch; therefore, 1/20 of a point is 1/1440 of an inch.
     *  This unit is also known as a "twip".
     *  Positive x is to the right; positive y is up.
     */
    MM_TWIPS(0x0006, 1440),
    
    /**
     *  Logical units are mapped to arbitrary device units with equally scaled axes;
     *  that is, one unit along the x-axis is equal to one unit along the y-axis.
     *  The META_SETWINDOWEXT and META_SETVIEWPORTEXT records specify the units and the
     *  orientation of the axes.
     *  The processing application SHOULD make adjustments as necessary to ensure the x and y
     *  units remain the same size. For example, when the window extent is set, the viewport
     *  SHOULD be adjusted to keep the units isotropic.
     */
    MM_ISOTROPIC(0x0007, -1),
    
    /**
     *  Logical units are mapped to arbitrary units with arbitrarily scaled axes.
     */
    MM_ANISOTROPIC(0x0008, -1);
    
    /**
     * native flag
     */
    public final int flag;
    
    /**
     * transformation units - usually scale relative to current dpi.
     * when scale == 0, then don't scale
     * when scale == -1, then scale relative to window dimension. 
     */
    public final int scale;
    
    HwmfMapMode(int flag, int scale) {
        this.flag = flag;
        this.scale = scale;
    }

    public static HwmfMapMode valueOf(int flag) {
        for (HwmfMapMode mm : values()) {
            if (mm.flag == flag) return mm;
        }
        return MM_ISOTROPIC;
    }        
}