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

/* $Id$ */

package org.docx4j.fonts.fop.fonts;

import org.docx4j.org.apache.fop.events.EventBroadcaster;
import org.docx4j.org.apache.fop.events.EventProducer;

/**
 * Event producer for fonts-related events.
 */
public interface FontEventProducer extends EventProducer {

    /**
     * Provider class for the event producer.
     */
    final class Provider {

        private Provider() { }

        /**
         * Returns an event producer.
         * @param broadcaster the event broadcaster to use
         * @return the event producer
         */
        public static FontEventProducer get(EventBroadcaster broadcaster) {
            return broadcaster.getEventProducerFor(FontEventProducer.class);
        }
    }

    /**
     * Notifies about a font being substituted as the requested one isn't available.
     * @param source the event source
     * @param requested the requested font triplet
     * @param effective the effective font triplet
     * @event.severity WARN
     */
    void fontSubstituted(Object source, FontTriplet requested, FontTriplet effective);

    /**
     * An error occurred while loading a font for auto-detection.
     * @param source the event source
     * @param fontURL the font URL
     * @param e the original exception
     * @event.severity WARN
     */
    void fontLoadingErrorAtAutoDetection(Object source, String fontURL, Exception e);

    /**
     * A glyph has been requested that is not available in the font.
     * @param source the event source
     * @param ch the character for which the glyph isn't available
     * @param fontName the name of the font
     * @event.severity WARN
     */
    void glyphNotAvailable(Object source, char ch, String fontName);

    /**
     * An error occurred trying to find the font directory specified in the config file.
     * @param source the event source
     * @param dir the directory in the config file
     * @event.severity WARN
     */
    void fontDirectoryNotFound(Object source, String dir);

    /**
     * The SVG text will be stroked as shapes.
     * @param source the event source
     * @param fontFamily the family name of the font that is being stroked
     * @event.severity WARN
     */
    void svgTextStrokedAsShapes(Object source, String fontFamily);

    /**
     * A method to warn the user that the feature they are trying to use is not supported with either the renderer or
     * other setting.
     * @param source
     * @param feature The feature that has caused the not supported issue
     * @param onlySupportedIn The renderer / setting that this feature works with.
     * @event.severity ERROR
     */
    void fontFeatureNotSuppprted(Object source, String feature, String onlySupportedIn);
}
