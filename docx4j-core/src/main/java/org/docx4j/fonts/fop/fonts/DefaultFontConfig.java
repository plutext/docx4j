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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.apps.FOPException;
import org.docx4j.fonts.fop.configuration.Configuration;
import org.docx4j.fonts.fop.configuration.ConfigurationException;
import org.docx4j.fonts.fop.util.LogUtil;
import org.docx4j.org.apache.fop.events.EventProducer;

/**
 * The font configuration data for the more generic fonts such as TTF and Type1, that are used by
 * most the renderers.
 */
public final class DefaultFontConfig implements FontConfig {

    private static final  Logger log = LoggerFactory.getLogger(DefaultFontConfig.class);

    private final List<Directory> directories = new ArrayList<Directory>();

    private final List<Font> fonts = new ArrayList<Font>();

    private final List<String> referencedFontFamilies = new ArrayList<String>();

    private final boolean autoDetectFonts;

    private DefaultFontConfig(boolean autoDetectFonts) {
        this.autoDetectFonts = autoDetectFonts;
    }

    /**
     * Parses the morge generic font information.
     */
    public static final class DefaultFontConfigParser implements FontConfig.FontConfigParser {
        /**
         * Parses the font configuration and return the configuration object.
         *
         * @param cfg the configuration data
         * @param strict whether or not to enforce strict validation
         * @return the font configuration object
         * @throws FOPException if an error occurs when creating the configuration object
         */
        public DefaultFontConfig parse(Configuration cfg, boolean strict) throws FOPException {
            return new ParserHelper(cfg, strict).instance;
        }

        /** {@inheritDoc} */
        public DefaultFontConfig parse(Configuration cfg, boolean strict,
                                       FontEventAdapter eventAdapter) throws FOPException {
            return new ParserHelper(cfg, strict, eventAdapter).instance;
        }

        /** {@inheritDoc} */
        public FontConfig parse(Configuration cfg, FontManager fontManager, boolean strict,
                EventProducer eventProducer) throws FOPException {
            return parse(cfg, strict);
        }
    }

    private  static final class ParserHelper {

        private  boolean strict;

        private Configuration config;
        private  Configuration fontInfoCfg;
        private FontEventAdapter eventAdapter;

        private  DefaultFontConfig instance;

        private ParserHelper(Configuration cfg, boolean strict) throws FOPException {
            this(cfg, strict, null);
        }

        private ParserHelper(Configuration cfg, boolean strict, FontEventAdapter eventAdapter)
                throws FOPException {
            this.eventAdapter = eventAdapter;
            if (cfg == null || cfg.getChild("fonts", false) == null) {
                instance = null;
            } else {
                this.strict = strict;
                this.config = cfg;
                this.fontInfoCfg = cfg.getChild("fonts", false);
                instance = new DefaultFontConfig(fontInfoCfg.getChild("auto-detect", false) != null);
                parse();
            }
        }

        private void parse() throws FOPException {
            parseFonts();
            parseReferencedFonts();
            parseDirectories();
        }

        private void parseFonts() throws FOPException {
            for (Configuration fontCfg : fontInfoCfg.getChildren("font")) {
                String embed = fontCfg.getAttribute("embed-url", null);
                if (embed == null) {
                    LogUtil.handleError(log, "Font configuration without embed-url attribute",
                            strict);
                    continue;
                }
                Font font = new Font(fontCfg.getAttribute("metrics-url", null), embed,
                        fontCfg.getAttribute("embed-url-afm", null),
                        fontCfg.getAttribute("embed-url-pfm", null),
                        fontCfg.getAttribute("sub-font", null),
                        fontCfg.getAttributeAsBoolean("kerning", true),
                        fontCfg.getAttributeAsBoolean("advanced", true),
                        fontCfg.getAttribute("encoding-mode", EncodingMode.AUTO.getName()),
                        fontCfg.getAttribute("embedding-mode", EncodingMode.AUTO.getName()),
                        fontCfg.getAttributeAsBoolean("simulate-style", false),
                        fontCfg.getAttributeAsBoolean("embed-as-type1", false));
                instance.fonts.add(font);
                boolean hasTriplets = false;
                for (Configuration tripletCfg : fontCfg.getChildren("font-triplet")) {
                    FontTriplet fontTriplet = getFontTriplet(tripletCfg, strict);
                    font.tripletList.add(fontTriplet);
                    hasTriplets = true;
                }
                // no font triplet info
                if (!hasTriplets) {
                    LogUtil.handleError(log, "font without font-triplet", strict);
                }
                try {
                    if (eventAdapter != null && font.getSimulateStyle()
                            && !config.getAttribute("mime").equals("application/pdf")) {
                        eventAdapter.fontFeatureNotSuppprted(this, "simulate-style", "PDF");
                    }
                    if (eventAdapter != null && font.getEmbedAsType1()
                            && !config.getAttribute("mime").equals("application/postscript")) {
                        throw new FOPException("The embed-as-type1 attribute is only supported in postscript");
                    }
                } catch (ConfigurationException ex) {
                    LogUtil.handleException(log, ex, true);
                }
            }
        }

        private void parseReferencedFonts() throws FOPException {
            Configuration referencedFontsCfg = fontInfoCfg.getChild("referenced-fonts", false);
            if (referencedFontsCfg != null) {
                for (Configuration match : referencedFontsCfg.getChildren("match")) {
                    try {
                        instance.referencedFontFamilies.add(match.getAttribute("font-family"));
                    } catch (ConfigurationException ce) {
                        LogUtil.handleException(log, ce, strict);
                        continue;
                    }
                }
            }
        }

        private void parseDirectories() throws FOPException {
            for (Configuration directoriesCfg : fontInfoCfg.getChildren("directory")) {
                boolean recursive = directoriesCfg.getAttributeAsBoolean("recursive", false);
                String directory;
                try {
                    directory = directoriesCfg.getValue();
                } catch (ConfigurationException e) {
                    LogUtil.handleException(log, e, strict);
                    continue;
                }
                if (directory == null) {
                    LogUtil.handleException(log,
                            new FOPException("directory defined without value"), strict);
                    continue;
                }
                instance.directories.add(new Directory(directory, recursive));
            }
        }

        /**
         * Creates a new FontTriplet given a triple Configuration
         *
         * @param tripletCfg a triplet configuration
         * @return a font triplet font key
         * @throws FOPException thrown if a FOP exception occurs
         */
        private FontTriplet getFontTriplet(Configuration tripletCfg, boolean strict)
                throws FOPException {
            try {
                String name = tripletCfg.getAttribute("name");
                if (name == null) {
                    LogUtil.handleError(log, "font-triplet without name", strict);
                    return null;
                }
                String weightStr = tripletCfg.getAttribute("weight");
                if (weightStr == null) {
                    LogUtil.handleError(log, "font-triplet without weight", strict);
                    return null;
                }
                int weight = FontUtil.parseCSS2FontWeight(FontUtil.stripWhiteSpace(weightStr));
                String style = tripletCfg.getAttribute("style");
                if (style == null) {
                    LogUtil.handleError(log, "font-triplet without style", strict);
                    return null;
                } else {
                    style = FontUtil.stripWhiteSpace(style);
                }
                return FontInfo.createFontKey(name, style, weight);
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, strict);
            }
            return null;
        }

    }

    /**
     * Returns the list of fonts that were parsed.
     * @return a list of fonts
     */
    public List<Font> getFonts() {
        return Collections.unmodifiableList(fonts);
    }

    /**
     * Returns a list of directories that were parsed.
     * @return a list of directories
     */
    public List<Directory> getDirectories() {
        return Collections.unmodifiableList(directories);
    }

    /**
     * Returns a list of referenced font families.
     * @return the referenced font families
     */
    public List<String> getReferencedFontFamily() {
        return Collections.unmodifiableList(referencedFontFamilies);
    }

    /**
     * Whether or not to enable auto-detecting of fonts in the system.
     * @return true to enable auto-detect
     */
    public boolean isAutoDetectFonts() {
        return autoDetectFonts;
    }

    /**
     * The directory to find fonts within.
     */
    public static final class Directory {

        private final String directory;

        private final boolean recursive;

        private Directory(String directory, boolean recurse) {
            this.directory = directory;
            this.recursive = recurse;
        }

        /**
         * Returns a String representing the directory to find fonts within.
         * @return the directory
         */
        public String getDirectory() {
            return directory;
        }

        /**
         * Returns whether or not to recurse through the directory when finding fonts.
         * @return true to recurse through the directory and sub-directories
         */
        public boolean isRecursive() {
            return recursive;
        }
    }

    /**
     * Represents a font object within the FOP conf.
     */
    public static final class Font {

        private final String metrics;

        private final String embedUri;

        private String afm;

        private String pfm;

        private final String subFont;

        private final boolean kerning;

        private final boolean advanced;

        private final String encodingMode;

        private final String embeddingMode;

        public String getEncodingMode() {
            return encodingMode;
        }

        private final boolean embedAsType1;
        private final boolean simulateStyle;

        private final List<FontTriplet> tripletList = new ArrayList<FontTriplet>();

        public List<FontTriplet> getTripletList() {
            return Collections.unmodifiableList(tripletList);
        }

        private Font(String metrics, String embed, String afm, String pfm, String subFont, boolean kerning,
                     boolean advanced, String encodingMode, String embeddingMode, boolean simulateStyle,
                     boolean embedAsType1) {
            this.metrics = metrics;
            this.embedUri = embed;
            this.afm = afm;
            this.pfm = pfm;
            this.subFont = subFont;
            this.kerning = kerning;
            this.advanced = advanced;
            this.encodingMode = encodingMode;
            this.embeddingMode = embeddingMode;
            this.simulateStyle = simulateStyle;
            this.embedAsType1 = embedAsType1;
        }

        /**
         * Whether or not to allow kerning of glyphs.
         * @return true to allow glyph kerning
         */
        public boolean isKerning() {
            return kerning;
        }

        public boolean isAdvanced() {
            return advanced;
        }

        /**
         * Gets the String representing the metrics file.
         * @return the metrics file
         */
        public String getMetrics() {
            return metrics;
        }

        /**
         * Gets the URI of the font to embed.
         * @return the font URI
         */
        public String getEmbedURI() {
            return embedUri;
        }

        /**
         * Gets the sub font within, for example, a TTC.
         * @return the sub font name
         */
        public String getSubFont() {
            return subFont;
        }

        public String getEmbeddingMode() {
            return embeddingMode;
        }

        public String getAfm() {
            return afm;
        }

        public String getPfm() {
            return pfm;
        }

        public boolean getSimulateStyle() {
            return simulateStyle;
        }

        public boolean getEmbedAsType1() {
            return embedAsType1;
        }
    }
}
