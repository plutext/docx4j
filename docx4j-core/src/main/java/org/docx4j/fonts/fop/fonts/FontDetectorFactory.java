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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.xmlgraphics.util.ClasspathResource;
import org.docx4j.fonts.fop.apps.FOPException;
import org.docx4j.fonts.fop.fonts.autodetect.FontFileFinder;
import org.docx4j.fonts.fop.util.LogUtil;

/**
 * A factory that provides the font detecting machanism.
 */
public final class FontDetectorFactory {
    private FontDetectorFactory() {
    }

    /**
     * Creates the default font detector
     * @return the default font detector
     */
    public static FontDetector createDefault() {
        return new DefaultFontDetector();
    }

    /**
     * Creates a disabled font detector which, by definition, does nothing to detect fonts.
     * @return the completely restricted font detector
     */
    public static FontDetector createDisabled() {
        return new DisabledFontDetector();
    }


    private static class DisabledFontDetector implements FontDetector {
        public void detect(FontManager fontManager, FontAdder fontAdder, boolean strict,
                FontEventListener eventListener, List<EmbedFontInfo> fontInfoList)
                throws FOPException {
            // nop
        }
    }

    /**
     * Detector of operating system and classpath fonts
     */
    private static class DefaultFontDetector implements FontDetector {
        private static  Logger log = LoggerFactory.getLogger(DefaultFontDetector.class);

        private static final String[] FONT_MIMETYPES = {
            "application/x-font", "application/x-font-truetype"
        };

        /**
         * Detect installed fonts on the system
         * @param fontInfoList a list of fontinfo to populate
         * @throws FOPException thrown if a problem occurred during detection
         */
        public void detect(FontManager fontManager, FontAdder fontAdder, boolean strict,
                FontEventListener eventListener, List<EmbedFontInfo> fontInfoList)
                throws FOPException {
            try {
                // search in font base if it is defined and
                // is a directory but don't recurse
                FontFileFinder fontFileFinder = new FontFileFinder(eventListener);
                URI fontBaseURI = fontManager.getResourceResolver().getBaseURI();
                File fontBase = FileUtils.toFile(fontBaseURI.toURL());
                if (fontBase != null) {
                    List<URL> fontURLList = fontFileFinder.find(fontBase.getAbsolutePath());
                    fontAdder.add(fontURLList, fontInfoList);

                    //Can only use the font base URL if it's a file URL
                }

                // native o/s font directory finding
                List<URL> systemFontList;
                systemFontList = fontFileFinder.find();
                fontAdder.add(systemFontList, fontInfoList);

                // classpath font finding
                ClasspathResource resource = ClasspathResource.getInstance();
                for (String mimeTypes : FONT_MIMETYPES) {
                    fontAdder.add(resource.listResourcesOfMimeType(mimeTypes), fontInfoList);
                }
            } catch (IOException e) {
                LogUtil.handleException(log, e, strict);
            } catch (URISyntaxException use) {
                LogUtil.handleException(log, use, strict);
            }
        }
    }
}
