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

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.truetype.OFFontLoader;
import org.docx4j.fonts.fop.fonts.type1.Type1FontLoader;

/**
 * Base class for font loaders.
 */
public abstract class FontLoader {

    /** logging instance */
    protected static final Log log = LogFactory.getLog(FontLoader.class);

    /** URI representing the font file */
    protected final URI fontFileURI;
    /** the resource resolver to use for font URI resolution */
    protected final InternalResourceResolver resourceResolver;
    /** the loaded font */
    protected CustomFont returnFont;

    /** true if the font has been loaded */
    protected boolean loaded;
    /** true if the font will be embedded, false if it will be referenced only. */
    protected boolean embedded;
    /** true if kerning information false be loaded if available. */
    protected boolean useKerning;
    /** true if advanced typographic information shall be loaded if available. */
    protected boolean useAdvanced;

    /**
     * Default constructor.
     * @param fontFileURI the URI to the PFB file of a Type 1 font
     * @param embedded indicates whether the font is embedded or referenced
     * @param useKerning indicates whether kerning information shall be loaded if available
     * @param useAdvanced indicates whether advanced typographic information shall be loaded if
     * available
     * @param resourceResolver the font resolver used to resolve URIs
     */
    public FontLoader(URI fontFileURI, boolean embedded, boolean useKerning,
            boolean useAdvanced, InternalResourceResolver resourceResolver) {
        this.fontFileURI = fontFileURI;
        this.embedded = embedded;
        this.useKerning = useKerning;
        this.useAdvanced = useAdvanced;
        this.resourceResolver = resourceResolver;
    }

    private static boolean isType1(FontUris fontUris) {
        return fontUris.getEmbed().toASCIIString().toLowerCase().endsWith(".pfb") || fontUris.getAfm() != null
            || fontUris.getPfm() != null;
    }

    /**
     * Loads a custom font from a URI. In the case of Type 1 fonts, the PFB file must be specified.
     * @param fontUris the URI to the font
     * @param subFontName the sub-fontname of a font (for TrueType Collections, null otherwise)
     * @param embedded indicates whether the font is embedded or referenced
     * @param embeddingMode the embedding mode of the font
     * @param encodingMode the requested encoding mode
     * @param useKerning indicates whether kerning information should be loaded if available
     * @param useAdvanced indicates whether advanced typographic information shall be loaded if
     * available
     * @param resourceResolver the font resolver to use when resolving URIs
     * @return the newly loaded font
     * @throws IOException In case of an I/O error
     */
    public static CustomFont loadFont(FontUris fontUris, String subFontName,
            boolean embedded, EmbeddingMode embeddingMode, EncodingMode encodingMode,
            boolean useKerning, boolean useAdvanced, InternalResourceResolver resourceResolver,
            boolean simulateStyle, boolean embedAsType1) throws IOException {
        boolean type1 = isType1(fontUris);
        FontLoader loader;
        if (type1) {
            if (encodingMode == EncodingMode.CID) {
                throw new IllegalArgumentException(
                        "CID encoding mode not supported for Type 1 fonts");
            }
            loader = new Type1FontLoader(fontUris, embedded, embeddingMode, useKerning,
                    resourceResolver);
        } else {
            loader = new OFFontLoader(fontUris.getEmbed(), subFontName, embedded, embeddingMode,
                    encodingMode, useKerning, useAdvanced, resourceResolver, simulateStyle, embedAsType1);
        }
        return loader.getFont();
    }

    /**
     * Reads/parses the font data.
     * @throws IOException In case of an I/O error
     */
    protected abstract void read() throws IOException;

    /**
     * Returns the custom font that was read using this instance of FontLoader.
     * @return the newly loaded font
     * @throws IOException if an I/O error occurs
     */
    public CustomFont getFont() throws IOException {
        if (!loaded) {
            read();
        }
        return this.returnFont;
    }
}
