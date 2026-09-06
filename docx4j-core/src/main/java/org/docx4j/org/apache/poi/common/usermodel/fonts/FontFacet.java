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

import org.docx4j.org.apache.poi.util.Beta;

/**
 * A FontFacet holds the font data for a shape of a font, i.e. a regular,
 * italic, bold or bold-italic version of a Font.
 */
@SuppressWarnings("unused")
@Beta
public interface FontFacet {
    /**
     * Get the font weight.<p>
     *
     * The weight of the font in the range 0 through 1000.
     * For example, 400 is normal and 700 is bold.
     * If this value is zero, a default weight is used.
     *
     * @return the font weight
     *
     * @since POI 4.1.0
     */
    default int getWeight() {
        return FontHeader.REGULAR_WEIGHT;
    }

    /**
     * Set the font weight
     *
     * @param weight the font weight
     * @throws UnsupportedOperationException can return UnsupportedOperationException when FontFacet is read-only
     */
    default void setWeight(int weight) {
        throw new UnsupportedOperationException("FontFacet is read-only.");
    }

    /**
     * @return {@code true}, if the font is italic
     */
    default boolean isItalic() {
        return false;
    }

    /**
     * Set the font posture
     *
     * @param italic {@code true} for italic, {@code false} for regular
     * @throws UnsupportedOperationException can return UnsupportedOperationException when FontFacet is read-only
     */
    default void setItalic(boolean italic) {
        throw new UnsupportedOperationException("FontFacet is read-only.");
    }

    /**
     * @return the wrapper object holding the font data
     */
    default Object getFontData() {
        return null;
    }
}
