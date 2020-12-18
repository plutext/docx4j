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

import java.util.List;

import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;

/**
 * Sets up a set of custom (embedded) fonts
 */
public class CustomFontCollection implements FontCollection {

    private final List<EmbedFontInfo> embedFontInfoList;
    private final InternalResourceResolver uriResolver;
    private final boolean useComplexScripts;

    /**
     * Main constructor.
     * @param fontResolver a font resolver
     * @param customFonts the list of custom fonts
     * @param useComplexScriptFeatures true if complex script features enabled
     */
    public CustomFontCollection(InternalResourceResolver fontResolver,
            List<EmbedFontInfo> customFonts, boolean useComplexScriptFeatures) {
        this.uriResolver = fontResolver;
        this.embedFontInfoList = customFonts;
        this.useComplexScripts = useComplexScriptFeatures;
    }

    /** {@inheritDoc} */
    public int setup(int num, FontInfo fontInfo) {
        if (embedFontInfoList == null) {
            return num; //No fonts to process
        }

        String internalName = null;

        for (EmbedFontInfo embedFontInfo : embedFontInfoList) {
            internalName = "F" + num;
            num++;

            LazyFont font = new LazyFont(embedFontInfo, this.uriResolver, useComplexScripts);
            fontInfo.addMetrics(internalName, font);

            List<FontTriplet> triplets = embedFontInfo.getFontTriplets();
            for (FontTriplet triplet : triplets) {
                fontInfo.addFontProperties(internalName, triplet);
            }
        }
        return num;
    }
}
