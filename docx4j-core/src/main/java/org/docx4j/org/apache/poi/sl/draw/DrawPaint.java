/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; in addition, only the two scRGB colour
 * space functions used by the repackaged HEMF code have been retained from
 * Apache POI's org.apache.poi.sl.draw.DrawPaint - the rest of that class
 * belongs to POI's slideshow ("sl") layer, which docx4j does not repackage.
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

package org.docx4j.org.apache.poi.sl.draw;

import java.awt.Color;

public final class DrawPaint {

    private DrawPaint() {}

    /**
     * Convert sRGB Color to scRGB [0..1] components (0:red,1:green,2:blue).
     * Alpha needs to be handled separately.
     *
     * @see <a href="https://referencesource.microsoft.com/#PresentationCore/Core/CSharp/System/Windows/Media/Color.cs,1075">.Net implementation sRgbToScRgb</a>
     */
    public static double[] RGB2SCRGB(Color color) {
        float[] rgb = color.getColorComponents(null);
        double[] scRGB = new double[3];
        for (int i=0; i<3; i++) {
            if (rgb[i] < 0) {
                scRGB[i] = 0;
            } else if (rgb[i] <= 0.04045) {
                scRGB[i] = rgb[i] / 12.92;
            } else if (rgb[i] <= 1) {
                scRGB[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
            } else {
                scRGB[i] = 1;
            }
        }
        return scRGB;
    }

    /**
     * Convert scRGB [0..1] components (0:red,1:green,2:blue) to sRGB Color.
     * Alpha needs to be handled separately.
     *
     * @see <a href="https://referencesource.microsoft.com/#PresentationCore/Core/CSharp/System/Windows/Media/Color.cs,1075">.Net implementation ScRgbTosRgb</a>
     */
    public static Color SCRGB2RGB(double... scRGB) {
        final double[] rgb = new double[3];
        for (int i=0; i<3; i++) {
            if (scRGB[i] < 0) {
                rgb[i] = 0;
            } else if (scRGB[i] <= 0.0031308) {
                rgb[i] = scRGB[i] * 12.92;
            } else if (scRGB[i] < 1) {
                rgb[i] = 1.055 * Math.pow(scRGB[i], 1.0 / 2.4) - 0.055;
            } else {
                rgb[i] = 1;
            }
        }
        return new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);
    }
}
