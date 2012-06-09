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

import org.apache.avalon.framework.configuration.Configuration;
import org.docx4j.fonts.fop.apps.FOPException;

/**
 * Configures a font substitution catalog
 */
public class FontSubstitutionsConfigurator {

    private Configuration cfg = null;

    /**
     * Main constructor
     *
     * @param cfg a configuration
     */
    public FontSubstitutionsConfigurator(Configuration cfg) {
        this.cfg = cfg;
    }

    private static FontQualifier getQualfierFromConfiguration(Configuration cfg)
    throws FOPException {
        String fontFamily = cfg.getAttribute("font-family", null);
        if (fontFamily == null) {
            throw new FOPException("substitution qualifier must have a font-family");
        }
        FontQualifier qualifier = new FontQualifier();
        qualifier.setFontFamily(fontFamily);
        String fontWeight = cfg.getAttribute("font-weight", null);
        if (fontWeight != null) {
            qualifier.setFontWeight(fontWeight);
        }
        String fontStyle = cfg.getAttribute("font-style", null);
        if (fontStyle != null) {
            qualifier.setFontStyle(fontStyle);
        }
        return qualifier;
    }

    /**
     * Configures a font substitution catalog
     *
     * @param substitutions font substitutions
     * @throws FOPException if something's wrong with the config data
     */
    public void configure(FontSubstitutions substitutions) throws FOPException {
        Configuration[] substitutionCfgs = cfg.getChildren("substitution");
        for (int i = 0; i < substitutionCfgs.length; i++) {
            Configuration fromCfg = substitutionCfgs[i].getChild("from", false);
            if (fromCfg == null) {
                throw new FOPException("'substitution' element without child 'from' element");
            }
            Configuration toCfg = substitutionCfgs[i].getChild("to", false);
            if (fromCfg == null) {
                throw new FOPException("'substitution' element without child 'to' element");
            }
            FontQualifier fromQualifier = getQualfierFromConfiguration(fromCfg);
            FontQualifier toQualifier = getQualfierFromConfiguration(toCfg);
            FontSubstitution substitution = new FontSubstitution(fromQualifier, toQualifier);
            substitutions.add(substitution);
        }
    }
}
