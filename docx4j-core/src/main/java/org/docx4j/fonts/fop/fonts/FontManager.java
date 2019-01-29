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

/* $Id: FontManager.java 677543 2008-07-17 09:11:09Z jeremias $ */

package org.docx4j.fonts.fop.fonts;

import java.net.MalformedURLException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.fonts.fop.fonts.FontTriplet.Matcher;
import org.docx4j.fonts.fop.fonts.substitute.FontSubstitutions;

// TODO: Refactor fonts package so major font activities (autodetection etc)
// are all centrally managed and delegated from this class, also remove dependency on FopFactory
// and start using POJO config/properties type classes

/**
 * The manager of fonts. The class holds a reference to the font cache and information about
 * font substitution, referenced fonts and similar.
 */
public class FontManager {
    /** Use cache (record previously detected font triplet info) */
    public static final boolean DEFAULT_USE_CACHE = true;

    /** The base URL for all font URL resolutions. */
    private String fontBase = null;

    /** Font cache to speed up auto-font configuration (null if disabled) */
    private FontCache fontCache = null;

    /** Font substitutions */
    private FontSubstitutions fontSubstitutions = null;

    /** Allows enabling kerning on the base 14 fonts, default is false */
    private boolean enableBase14Kerning = false;

    /** FontTriplet matcher for fonts that shall be referenced rather than embedded. */
    private FontTriplet.Matcher referencedFontsMatcher;

    /**
     * Main constructor
     */
    public FontManager() {
        setUseCache(DEFAULT_USE_CACHE);
    }

    /**
     * Sets the font base URL.
     * @param fontBase font base URL
     * @throws MalformedURLException if there's a problem with a URL
     */
    public void setFontBaseURL(String fontBase) throws MalformedURLException {
        this.fontBase = fontBase;
    }

    /**
     * Returns the font base URL.
     * @return the font base URL (or null if none was set)
     */
    public String getFontBaseURL() {
        return this.fontBase;
    }

    /** @return true if kerning on base 14 fonts is enabled */
    public boolean isBase14KerningEnabled() {
        return this.enableBase14Kerning;
    }

    /**
     * Controls whether kerning is activated on base 14 fonts.
     * @param value true if kerning should be activated
     */
    public void setBase14KerningEnabled(boolean value) {
        this.enableBase14Kerning = value;
    }

    /**
     * Sets the font substitutions
     * @param substitutions font substitutions
     */
    public void setFontSubstitutions(FontSubstitutions substitutions) {
        this.fontSubstitutions = substitutions;
    }

    /**
     * Returns the font substitution catalog
     * @return the font substitution catalog
     */
    protected FontSubstitutions getFontSubstitutions() {
        if (fontSubstitutions == null) {
            this.fontSubstitutions = new FontSubstitutions();
        }
        return fontSubstitutions;
    }

    /**
     * Whether or not to cache results of font triplet detection/auto-config
     * @param useCache use cache or not
     */
    public void setUseCache(boolean useCache) {
        if (useCache) {
            this.fontCache = FontCache.load();
            if (this.fontCache == null) {
                this.fontCache = new FontCache();
            }
        } else {
            this.fontCache = null;
        }
    }

    /**
     * Cache results of font triplet detection/auto-config?
     * @return true if this font manager uses the cache
     */
    public boolean useCache() {
        return (this.fontCache != null);
    }

    /**
     * Returns the font cache instance used by this font manager.
     * @return the font cache
     */
    public FontCache getFontCache() {
        return this.fontCache;
    }

    /**
     * Sets up the fonts on a given FontInfo object. The fonts to setup are defined by an
     * array of {@code FontCollection} objects.
     * @param fontInfo the FontInfo object to set up
     * @param fontCollections the array of font collections/sources
     */
    public void setup(FontInfo fontInfo, FontCollection[] fontCollections) {
        int startNum = 1;

        for (int i = 0, c = fontCollections.length; i < c; i++) {
            startNum = fontCollections[i].setup(startNum, fontInfo);
        }
        // Make any defined substitutions in the font info
        getFontSubstitutions().adjustFontInfo(fontInfo);
    }

    /** @return a new FontResolver to be used by the font subsystem */
    public static FontResolver createMinimalFontResolver() {
        return new FontResolver() {

            /** {@inheritDoc} */
            public Source resolve(String href) {
                //Minimal functionality here
                return new StreamSource(href);
            }
        };
    }

    /**
     * Sets the {@link FontTriplet.Matcher} that can be used to identify the fonts that shall
     * be referenced rather than embedded.
     * @param matcher the font triplet matcher
     */
    public void setReferencedFontsMatcher(FontTriplet.Matcher matcher) {
        this.referencedFontsMatcher = matcher;
    }

    /**
     * Gets the {@link FontTriplet.Matcher} that can be used to identify the fonts that shall
     * be referenced rather than embedded.
     * @return the font triplet matcher (or null if none is set)
     */
    public Matcher getReferencedFontsMatcher() {
        return this.referencedFontsMatcher;
    }
}
