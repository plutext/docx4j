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

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.autodetect.FontInfoFinder;

/**
 * Adds a list of fonts to a given font info list
 */
public class FontAdder {
    private final FontEventListener listener;
    private final InternalResourceResolver resourceResolver;
    private final FontManager manager;

    /**
     * Main constructor
     * @param manager a font manager
     * @param resourceResolver a font resolver
     * @param listener a font event handler
     */
    public FontAdder(FontManager manager, InternalResourceResolver resourceResolver,
            FontEventListener listener) {
        this.manager = manager;
        this.resourceResolver = resourceResolver;
        this.listener = listener;
    }

    /**
     * Iterates over font url list adding to font info list
     * @param fontURLList font file list
     * @param fontInfoList a configured font info list
     * @throws URISyntaxException if a URI syntax error is found
     */
    public void add(List<URL> fontURLList, List<EmbedFontInfo> fontInfoList)
            throws URISyntaxException {
        FontCache cache = manager.getFontCache();
        FontInfoFinder finder = new FontInfoFinder();
        finder.setEventListener(listener);

        for (URL fontURL : fontURLList) {
            EmbedFontInfo[] embedFontInfos = finder.find(fontURL.toURI(), resourceResolver, cache);
            if (embedFontInfos == null) {
                continue;
            }
            for (EmbedFontInfo fontInfo : embedFontInfos) {
                if (fontInfo != null) {
                    fontInfoList.add(fontInfo);
                }
            }
        }
    }
}
