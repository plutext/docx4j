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

/* $Id: FontEventListener.java 721430 2008-11-28 11:13:12Z acumiskey $ */

package org.docx4j.fonts.fop.fonts;

/**
 * Event listener interface for font-related events.
 */
public interface FontEventListener {

    /**
     * Notifies about a font being substituted as the requested one isn't available.
     * @param source the event source
     * @param requested the requested font triplet
     * @param effective the effective font triplet
     */
    void fontSubstituted(Object source, FontTriplet requested, FontTriplet effective);

    /**
     * An error occurred while loading a font for auto-detection.
     * @param source the event source
     * @param fontURL the font URL
     * @param e the original exception
     */
    void fontLoadingErrorAtAutoDetection(Object source, String fontURL, Exception e);

    /**
     * A glyph has been requested that is not available in the font.
     * @param source the event source
     * @param ch the character for which the glyph isn't available
     * @param fontName the name of the font
     */
    void glyphNotAvailable(Object source, char ch, String fontName);

}
