/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.fonts.fop.fonts.substitute;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.docx4j.fonts.fop.fonts.FontInfo;
import org.docx4j.fonts.fop.fonts.FontTriplet;

/**
 * Font substitutions
 */
public class FontSubstitutions extends java.util.ArrayList/*<Substitutions>*/ {

    private static final long serialVersionUID = -9173104935431899722L;

    /** logging instance */
    protected static Log log = LogFactory.getLog(FontSubstitutions.class);

    /**
     * Adjusts a given fontInfo using this font substitution catalog
     * @param fontInfo font info
     */
    public void adjustFontInfo(FontInfo fontInfo) {
        for (Iterator/*<FontSubstitution>*/ subsIt = super.iterator(); subsIt.hasNext();) {
            FontSubstitution substitution = (FontSubstitution)subsIt.next();

            // find the best matching font triplet
            FontQualifier toQualifier = substitution.getToQualifier();
            FontTriplet fontTriplet = toQualifier.bestMatch(fontInfo);
            if (fontTriplet == null) {
                log.error("Unable to match font substitution for destination qualifier "
                        + toQualifier);
                continue;
            }
            String internalFontKey = fontInfo.getInternalFontKey(fontTriplet);

            FontQualifier fromQualifier = substitution.getFromQualifier();
            List/*<FontTriplet>*/ tripletList = fromQualifier.getTriplets();
            for (Iterator tripletit = tripletList.iterator(); tripletit.hasNext();) {
                FontTriplet triplet = (FontTriplet) tripletit.next();
                fontInfo.addFontProperties(internalFontKey, triplet);
            }
        }
    }
}
