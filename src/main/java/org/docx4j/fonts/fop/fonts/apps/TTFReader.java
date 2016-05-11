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

/* $Id: TTFReader.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.apps;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TTFReader extends AbstractFontReader {

    /** Used to detect incompatible versions of the generated XML files */
    public static final String METRICS_VERSION_ATTR = "metrics-version";
    /** Current version number for the metrics file */
    public static final int METRICS_VERSION = 2;


    /**
     * Bugzilla 40739, check that attr has a metrics-version attribute
     * compatible with ours.
     * @param attr attributes read from the root element of a metrics XML file
     * @throws SAXException if incompatible
     */
    public static void checkMetricsVersion(Attributes attr) throws SAXException {
        String err = null;
        final String str = attr.getValue(METRICS_VERSION_ATTR);
        if (str == null) {
            err = "Missing " + METRICS_VERSION_ATTR + " attribute";
        } else {
            int version = 0;
            try {
                version = Integer.parseInt(str);
                if (version < METRICS_VERSION) {
                    err = "Incompatible " + METRICS_VERSION_ATTR
                        + " value (" + version + ", should be " + METRICS_VERSION
                        + ")"
                     ;
                }
            } catch (NumberFormatException e) {
                err = "Invalid " + METRICS_VERSION_ATTR
                    + " attribute value (" + str + ")";
            }
        }

        if (err != null) {
            throw new SAXException(
                err
                + " - please regenerate the font metrics file with "
                + "a more recent version of FOP."
            );
        }
    }

}

