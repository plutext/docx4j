/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; in addition, only the PaintModifier enum,
 * which the repackaged geometry engine (org.docx4j.org.apache.poi.sl.draw.geom)
 * uses for a path's fill mode, has been retained from Apache POI's
 * org.apache.poi.sl.usermodel.PaintStyle - the rest of that interface belongs to
 * POI's slideshow ("sl") usermodel, which docx4j does not repackage.
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

package org.docx4j.org.apache.poi.sl.usermodel;

public interface PaintStyle {
    /**
     * The PaintStyle can be modified by secondary sources, e.g. the attributes in the preset shapes.
     * These modifications need to be taken into account when the final color is determined.
     */
    enum PaintModifier {
        /** don't use any paint/fill */
        NONE,
        /** use the paint/filling as-is */
        NORM,
        /** lighten the paint/filling */
        LIGHTEN,
        /** lighten (... a bit less) the paint/filling */
        LIGHTEN_LESS,
        /** darken the paint/filling */
        DARKEN,
        /** darken (... a bit less) the paint/filling */
        DARKEN_LESS
    }

}
