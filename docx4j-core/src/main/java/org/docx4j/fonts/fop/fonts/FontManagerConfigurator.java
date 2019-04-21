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

/* $Id: FontManagerConfigurator.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts;

import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Pattern;

import org.docx4j.fonts.fop.configuration.Configuration;
import org.docx4j.fonts.fop.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.docx4j.fonts.fop.apps.FOPException;
import org.docx4j.fonts.fop.fonts.substitute.FontSubstitutions;
import org.docx4j.fonts.fop.fonts.substitute.FontSubstitutionsConfigurator;
import org.docx4j.fonts.fop.util.LogUtil;

/**
 * Configurator of the FontManager
 */
public class FontManagerConfigurator {

    /** logger instance */
    private static Logger log = LoggerFactory.getLogger(FontManagerConfigurator.class);

    private Configuration cfg;

    /**
     * Main constructor
     * @param cfg the font manager configuration object
     */
    public FontManagerConfigurator(Configuration cfg) {
        this.cfg = cfg;
    }

    /**
     * Initializes font settings from the user configuration
     * @param fontManager a font manager
     * @param strict true if strict checking of the configuration is enabled
     * @throws FOPException if an exception occurs while processing the configuration
     */
    public void configure(FontManager fontManager, boolean strict) throws FOPException {

        // caching (fonts)
        if (cfg.getChild("use-cache", false) != null) {
            try {
                fontManager.setUseCache(
                        cfg.getChild("use-cache").getValueAsBoolean());
            } catch (ConfigurationException mfue) {
                LogUtil.handleException(log, mfue, true);
            }
        }
        if (cfg.getChild("font-base", false) != null) {
            try {
                fontManager.setFontBaseURL(
                        cfg.getChild("font-base").getValue(null));
            } catch (MalformedURLException mfue) {
                LogUtil.handleException(log, mfue, true);
            }
        }

        // global font configuration
        Configuration fontsCfg = cfg.getChild("fonts", false);
        if (fontsCfg != null) {
            // font substitution
            Configuration substitutionsCfg = fontsCfg.getChild("substitutions", false);
            if (substitutionsCfg != null) {
                FontSubstitutionsConfigurator fontSubstitutionsConfigurator
                        = new FontSubstitutionsConfigurator(substitutionsCfg);
                FontSubstitutions substitutions = new FontSubstitutions();
                fontSubstitutionsConfigurator.configure(substitutions);
                fontManager.setFontSubstitutions(substitutions);
            }

            // referenced fonts (fonts which are not to be embedded)
            Configuration referencedFontsCfg = fontsCfg.getChild("referenced-fonts", false);
            if (referencedFontsCfg != null) {
                createReferencedFontsMatcher(referencedFontsCfg, strict, fontManager);
            }

        }
    }

    private static void createReferencedFontsMatcher(Configuration referencedFontsCfg,
            boolean strict, FontManager fontManager) throws FOPException {
        List matcherList = new java.util.ArrayList();
        Configuration[] matches = referencedFontsCfg.getChildren("match");
        for (int i = 0; i < matches.length; i++) {
            try {
                matcherList.add(new FontFamilyRegExFontTripletMatcher(
                        matches[i].getAttribute("font-family")));
            } catch (ConfigurationException ce) {
                LogUtil.handleException(log, ce, strict);
                continue;
            }
        }
        FontTriplet.Matcher orMatcher = new OrFontTripletMatcher(
                (FontTriplet.Matcher[])matcherList.toArray(
                        new FontTriplet.Matcher[matcherList.size()]));
        fontManager.setReferencedFontsMatcher(orMatcher);
    }

    private static class OrFontTripletMatcher implements FontTriplet.Matcher {

        private FontTriplet.Matcher[] matchers;

        public OrFontTripletMatcher(FontTriplet.Matcher[] matchers) {
            this.matchers = matchers;
        }

        /** {@inheritDoc} */
        public boolean matches(FontTriplet triplet) {
            for (int i = 0, c = matchers.length; i < c; i++) {
                if (matchers[i].matches(triplet)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class FontFamilyRegExFontTripletMatcher implements FontTriplet.Matcher {

        private Pattern regex;

        public FontFamilyRegExFontTripletMatcher(String regex) {
            this.regex = Pattern.compile(regex);
        }

        /** {@inheritDoc} */
        public boolean matches(FontTriplet triplet) {
            return regex.matcher(triplet.getName()).matches();
        }

    }

}
