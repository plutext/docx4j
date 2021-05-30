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

package org.docx4j.fonts.fop.fonts.autodetect;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.CustomFont;
import org.docx4j.fonts.fop.fonts.EmbedFontInfo;
import org.docx4j.fonts.fop.fonts.EmbeddingMode;
import org.docx4j.fonts.fop.fonts.EncodingMode;
import org.docx4j.fonts.fop.fonts.Font;
import org.docx4j.fonts.fop.fonts.FontCache;
import org.docx4j.fonts.fop.fonts.FontEventListener;
import org.docx4j.fonts.fop.fonts.FontLoader;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.fonts.fop.fonts.FontUris;
import org.docx4j.fonts.fop.fonts.FontUtil;
import org.docx4j.fonts.fop.fonts.MultiByteFont;
import org.docx4j.fonts.fop.fonts.truetype.FontFileReader;
import org.docx4j.fonts.fop.fonts.truetype.OFFontLoader;
import org.docx4j.fonts.fop.fonts.truetype.TTFFile;

/**
 * Attempts to determine correct FontInfo
 */
public class FontInfoFinder {

    /** logging instance */
    private final  Logger log = LoggerFactory.getLogger(FontInfoFinder.class);

    private FontEventListener eventListener;

    /**
     * Sets the font event listener that can be used to receive events about particular events
     * in this class.
     * @param listener the font event listener
     */
    public void setEventListener(FontEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * Attempts to determine FontTriplets from a given CustomFont.
     * It seems to be fairly accurate but will probably require some tweaking over time
     *
     * @param customFont CustomFont
     * @param triplets Collection that will take the generated triplets
     */
    private void generateTripletsFromFont(CustomFont customFont, Collection<FontTriplet> triplets) {
        if (log.isTraceEnabled()) {
            log.trace("Font: " + customFont.getFullName()
                    + ", family: " + customFont.getFamilyNames()
                    + ", PS: " + customFont.getFontName()
                    + ", EmbedName: " + customFont.getEmbedFontName());
        }

        // default style and weight triplet vales (fallback)
        String strippedName = stripQuotes(customFont.getStrippedFontName());
        //String subName = customFont.getFontSubName();
        String fullName = stripQuotes(customFont.getFullName());
        String searchName = fullName.toLowerCase();

        String style = guessStyle(customFont, searchName);
        int weight; //= customFont.getWeight();
        int guessedWeight = FontUtil.guessWeight(searchName);
        //We always take the guessed weight for now since it yield much better results.
        //OpenType's OS/2 usWeightClass value proves to be unreliable.
        weight = guessedWeight;

        //Full Name usually includes style/weight info so don't use these traits
        //If we still want to use these traits, we have to make FontInfo.fontLookup() smarter
        triplets.add(new FontTriplet(fullName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL));
        if (!fullName.equals(strippedName)) {
            triplets.add(new FontTriplet(strippedName, Font.STYLE_NORMAL, Font.WEIGHT_NORMAL));
        }
        Set<String> familyNames = customFont.getFamilyNames();
        for (String familyName : familyNames) {
            familyName = stripQuotes(familyName);
            if (!fullName.equals(familyName)) {
                /* Heuristic:
                 *   The more similar the family name to the full font name,
                 *   the higher the priority of its triplet.
                 * (Lower values indicate higher priorities.) */
                int priority = fullName.startsWith(familyName)
                    ? fullName.length() - familyName.length()
                    : fullName.length();
                triplets.add(new FontTriplet(familyName, style, weight, priority));
            }
        }
    }

    private final Pattern quotePattern = Pattern.compile("'");

    private String stripQuotes(String name) {
        return quotePattern.matcher(name).replaceAll("");
    }

    private String guessStyle(CustomFont customFont, String fontName) {
        // style
        String style = Font.STYLE_NORMAL;
        if (customFont.getItalicAngle() > 0) {
            style = Font.STYLE_ITALIC;
        } else {
            style = FontUtil.guessStyle(fontName);
        }
        return style;
    }

    /**
     * Attempts to determine FontInfo from a given custom font
     * @param fontUri the font URI
     * @param customFont the custom font
     * @param fontCache font cache (may be null)
     * @return FontInfo from the given custom font
     */
    private EmbedFontInfo getFontInfoFromCustomFont(URI fontUri, CustomFont customFont,
            FontCache fontCache, InternalResourceResolver resourceResolver) {
        FontUris fontUris = new FontUris(fontUri, null);
        List<FontTriplet> fontTripletList = new java.util.ArrayList<FontTriplet>();
        generateTripletsFromFont(customFont, fontTripletList);
        String subFontName = null;
        if (customFont instanceof MultiByteFont) {
            subFontName = ((MultiByteFont) customFont).getTTCName();
        }
        EmbedFontInfo fontInfo = new EmbedFontInfo(fontUris, customFont.isKerningEnabled(),
                customFont.isAdvancedEnabled(), fontTripletList, subFontName);
        fontInfo.setPostScriptName(customFont.getFontName());
        if (fontCache != null) {
            fontCache.addFont(fontInfo, resourceResolver);
        }
        return fontInfo;
    }

    /**
     * Attempts to determine EmbedFontInfo from a given font file.
     *
     * @param fontURI the URI of the font resource
     * @param resourceResolver font resolver used to resolve font
     * @param fontCache font cache (may be null)
     * @return an array of newly created embed font info. Generally, this array
     *         will have only one entry, unless the fontUrl is a TrueType Collection
     */
    public EmbedFontInfo[] find(URI fontURI, InternalResourceResolver resourceResolver, FontCache fontCache) {
        URI embedUri = resourceResolver.resolveFromBase(fontURI);
        String embedStr = embedUri.toASCIIString();
        boolean useKerning = true;
        boolean useAdvanced = true;

        long fileLastModified = -1;
        if (fontCache != null) {
            fileLastModified = FontCache.getLastModified(fontURI);
            // firstly try and fetch it from cache before loading/parsing the font file
            if (fontCache.containsFont(embedStr)) {
                EmbedFontInfo[] fontInfos = fontCache.getFontInfos(embedStr, fileLastModified);
                if (fontInfos != null) {
                    return fontInfos;
                }
            // is this a previously failed parsed font?
            } else if (fontCache.isFailedFont(embedStr, fileLastModified)) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping font file that failed to load previously: " + embedUri);
                }
                return null;
            }
        }


        // try to determine triplet information from font file
        CustomFont customFont = null;
        if (fontURI.toASCIIString().toLowerCase().endsWith(".ttc")) {
            // Get a list of the TTC Font names
            List<String> ttcNames = null;
            InputStream in = null;
            try {
                in = resourceResolver.getResource(fontURI);
                TTFFile ttf = new TTFFile(false, false);
                FontFileReader reader = new FontFileReader(in);
                ttcNames = ttf.getTTCnames(reader);
            } catch (Exception e) {
                if (this.eventListener != null) {
                    this.eventListener.fontLoadingErrorAtAutoDetection(this,
                            fontURI.toASCIIString(), e);
                }
                return null;
            } finally {
                IOUtils.closeQuietly(in);
            }

            List<EmbedFontInfo> embedFontInfoList = new java.util.ArrayList<EmbedFontInfo>();

            // For each font name ...
            for (String fontName : ttcNames) {
                if (log.isDebugEnabled()) {
                    log.debug("Loading " + fontName);
                }
                try {
                    OFFontLoader ttfLoader = new OFFontLoader(fontURI, fontName, true,
                            EmbeddingMode.AUTO, EncodingMode.AUTO, useKerning, useAdvanced,
                            resourceResolver, false, false);
                    customFont = ttfLoader.getFont();
                    if (this.eventListener != null) {
                        customFont.setEventListener(this.eventListener);
                    }
                } catch (Exception e) {
                    if (fontCache != null) {
                        fontCache.registerFailedFont(embedUri.toASCIIString(), fileLastModified);
                    }
                    if (this.eventListener != null) {
                        this.eventListener.fontLoadingErrorAtAutoDetection(this,
                                embedUri.toASCIIString(), e);
                    }
                    continue;
                }
                EmbedFontInfo fi = getFontInfoFromCustomFont(fontURI, customFont, fontCache,
                        resourceResolver);
                if (fi != null) {
                    embedFontInfoList.add(fi);
                }
            }
            return embedFontInfoList.toArray(
                    new EmbedFontInfo[embedFontInfoList.size()]);
        } else {
            // The normal case
            try {
                FontUris fontUris = new FontUris(fontURI, null);
                customFont = FontLoader.loadFont(fontUris, null, true, EmbeddingMode.AUTO, EncodingMode.AUTO,
                        useKerning, useAdvanced, resourceResolver, false, false);
                if (this.eventListener != null) {
                    customFont.setEventListener(this.eventListener);
                }
            } catch (Exception e) {
                if (fontCache != null) {
                    fontCache.registerFailedFont(embedUri.toASCIIString(), fileLastModified);
                }
                if (this.eventListener != null) {
                    this.eventListener.fontLoadingErrorAtAutoDetection(this,
                            embedUri.toASCIIString(), e);
                }
                return null;
            }
            EmbedFontInfo fi = getFontInfoFromCustomFont(fontURI, customFont, fontCache, resourceResolver);
            if (fi != null) {
                return new EmbedFontInfo[] {fi};
            } else {
                return null;
            }
        }

    }

}
