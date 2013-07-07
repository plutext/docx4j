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

/* $Id: LazyFont.java 746664 2009-02-22 12:40:44Z jeremias $ */

package org.docx4j.fonts.fop.fonts;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.docx4j.fonts.fop.apps.FOPException;

/**
 * This class is used to defer the loading of a font until it is really used.
 */
public class LazyFont extends Typeface implements FontDescriptor {

    private static Logger log = LoggerFactory.getLogger(LazyFont.class);

    private String metricsFileName = null;
    private String fontEmbedPath = null;
    private boolean useKerning = false;
    private EncodingMode encodingMode = EncodingMode.AUTO;
    private boolean embedded = true;
    private String subFontName = null;

    private boolean isMetricsLoaded = false;
    private Typeface realFont = null;
    private FontDescriptor realFontDescriptor = null;

    private FontResolver resolver = null;

    /**
     * Main constructor
     * @param fontInfo  the font info to embed
     * @param resolver the font resolver to handle font URIs
     */
    public LazyFont(EmbedFontInfo fontInfo, FontResolver resolver) {

        this.metricsFileName = fontInfo.getMetricsFile();
        this.fontEmbedPath = fontInfo.getEmbedFile();
        this.useKerning = fontInfo.getKerning();
        this.encodingMode = fontInfo.getEncodingMode();
        this.subFontName = fontInfo.getSubFontName();
        this.embedded = fontInfo.isEmbedded();
        this.resolver = resolver;
    }

    /** {@inheritDoc} */
    public String toString() {
        return ( "metrics-url=" + metricsFileName + ", embed-url=" + fontEmbedPath
                + ", kerning=" + useKerning );
    }

    private void load(boolean fail) {
        if (!isMetricsLoaded) {
            try {
                if (metricsFileName != null) {
                    /**@todo Possible thread problem here */
                    FontReader reader = null;
                    if (resolver != null) {
                        Source source = resolver.resolve(metricsFileName);
                        if (source == null) {
                            String err
                                = "Cannot load font: failed to create Source from metrics file "
                                    + metricsFileName;
                            if (fail) {
                                throw new RuntimeException(err);
                            } else {
                                log.error(err);
                            }
                            return;
                        }
                        InputStream in = null;
                        if (source instanceof StreamSource) {
                            in = ((StreamSource) source).getInputStream();
                        }
                        if (in == null && source.getSystemId() != null) {
                            in = new java.net.URL(source.getSystemId()).openStream();
                        }
                        if (in == null) {
                            String err = "Cannot load font: After URI resolution, the returned"
                                + " Source object does not contain an InputStream"
                                + " or a valid URL (system identifier) for metrics file: "
                                + metricsFileName;
                            if (fail) {
                                throw new RuntimeException(err);
                            } else {
                                log.error(err);
                            }
                            return;
                        }
                        InputSource src = new InputSource(in);
                        src.setSystemId(source.getSystemId());
                        reader = new FontReader(src);
                    } else {
                        reader = new FontReader(new InputSource(
                                    new URL(metricsFileName).openStream()));
                    }
                    reader.setKerningEnabled(useKerning);
                    if (this.embedded) {
                        reader.setFontEmbedPath(fontEmbedPath);
                    }
                    reader.setResolver(resolver);
                    realFont = reader.getFont();
                } else {
                    if (fontEmbedPath == null) {
                        throw new RuntimeException("Cannot load font. No font URIs available.");
                    }
                    realFont = FontLoader.loadFont(fontEmbedPath, this.subFontName,
                            this.embedded, this.encodingMode, useKerning, resolver);
                }
                if (realFont instanceof FontDescriptor) {
                    realFontDescriptor = (FontDescriptor) realFont;
                }
            } catch (FOPException fopex) {
                log.error("Failed to read font metrics file " + metricsFileName, fopex);
                if (fail) {
                    throw new RuntimeException(fopex.getMessage());
                }
            } catch (IOException ioex) {
                log.error("Failed to read font metrics file " + metricsFileName, ioex);
                if (fail) {
                    throw new RuntimeException(ioex.getMessage());
                }
            }
            realFont.setEventListener(this.eventListener);
            isMetricsLoaded = true;
        }
    }

    /**
     * Gets the real font.
     * @return the real font
     */
    public Typeface getRealFont() {
        load(false);
        return realFont;
    }

    // ---- Font ----
    /** {@inheritDoc} */
    public String getEncodingName() {
        load(true);
        return realFont.getEncodingName();
    }

    /**
     * {@inheritDoc}
     */
    public char mapChar(char c) {
        load(true);
        return realFont.mapChar(c);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hadMappingOperations() {
        load(true);
        return realFont.hadMappingOperations();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChar(char c) {
        load(true);
        return realFont.hasChar(c);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMultiByte() {
        load(true);
        return realFont.isMultiByte();
    }

    // ---- FontMetrics interface ----
    /** {@inheritDoc} */
    public String getFontName() {
        load(true);
        return realFont.getFontName();
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        load(true);
        return realFont.getEmbedFontName();
    }

    /** {@inheritDoc} */
    public String getFullName() {
        load(true);
        return realFont.getFullName();
    }

    /** {@inheritDoc} */
    public Set getFamilyNames() {
        load(true);
        return realFont.getFamilyNames();
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxAscent(int size) {
        load(true);
        return realFont.getMaxAscent(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender(int size) {
        load(true);
        return realFont.getAscender(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getCapHeight(int size) {
        load(true);
        return realFont.getCapHeight(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender(int size) {
        load(true);
        return realFont.getDescender(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getXHeight(int size) {
        load(true);
        return realFont.getXHeight(size);
    }

    /**
     * {@inheritDoc}
     */
    public int getWidth(int i, int size) {
        load(true);
        return realFont.getWidth(i, size);
    }

    /**
     * {@inheritDoc}
     */
    public int[] getWidths() {
        load(true);
        return realFont.getWidths();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasKerningInfo() {
        load(true);
        return realFont.hasKerningInfo();
    }

    /**
     * {@inheritDoc}
     */
    public Map getKerningInfo() {
        load(true);
        return realFont.getKerningInfo();
    }

    // ---- FontDescriptor interface ----
    /**
     * {@inheritDoc}
     */
    public int getCapHeight() {
        load(true);
        return realFontDescriptor.getCapHeight();
    }

    /**
     * {@inheritDoc}
     */
    public int getDescender() {
        load(true);
        return realFontDescriptor.getDescender();
    }

    /**
     * {@inheritDoc}
     */
    public int getAscender() {
        load(true);
        return realFontDescriptor.getAscender();
    }

    /** {@inheritDoc} */
    public int getFlags() {
        load(true);
        return realFontDescriptor.getFlags();
    }

    /** {@inheritDoc} */
    public boolean isSymbolicFont() {
        load(true);
        return realFontDescriptor.isSymbolicFont();
    }

    /**
     * {@inheritDoc}
     */
    public int[] getFontBBox() {
        load(true);
        return realFontDescriptor.getFontBBox();
    }

    /**
     * {@inheritDoc}
     */
    public int getItalicAngle() {
        load(true);
        return realFontDescriptor.getItalicAngle();
    }

    /**
     * {@inheritDoc}
     */
    public int getStemV() {
        load(true);
        return realFontDescriptor.getStemV();
    }

    /**
     * {@inheritDoc}
     */
    public FontType getFontType() {
        load(true);
        return realFontDescriptor.getFontType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmbeddable() {
        load(true);
        return realFontDescriptor.isEmbeddable();
    }

}

