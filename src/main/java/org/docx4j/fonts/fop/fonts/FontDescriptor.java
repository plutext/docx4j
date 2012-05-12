/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: FontDescriptor.java 721430 2008-11-28 11:13:12Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;


/**
 * This interface enhances the font metrics interface with access methods to
 * value needed to register fonts in various target formats like PDF or
 * PostScript.
 */
public interface FontDescriptor extends FontMetrics {

    /**
     * Returns the ascender value of the font. (Ascent in pdf spec)
     * @return the ascender
     */
    int getAscender();


    /**
     * Returns the capital height of the font.
     * @return the capital height
     */
    int getCapHeight();


    /**
     * Returns the descender value of the font. (Descent in pdf spec)
     * @return the descender value
     */
    int getDescender();


    /**
     * Returns the flags for the font. (See pdf spec)
     * @return the flags
     */
    int getFlags();

    /**
     * Indicates whether the font is a symbolic font.
     * @return true if the font is a symbolic font (i.e. Symbol or ZapfDingbats)
     */
    boolean isSymbolicFont();
    /**
     * Returns the font's bounding box.
     * @return the bounding box
     */
    int[] getFontBBox();


    /**
     * Returns the italic angle for the font.
     * @return the italic angle
     */
    int getItalicAngle();


    /**
     * Returns the vertical stem width for the font.
     * @return the vertical stem width
     */
    int getStemV();


    /**
     * Indicates if this font may be embedded.
     * @return True, if embedding is possible/permitted
     */
    boolean isEmbeddable();


}
