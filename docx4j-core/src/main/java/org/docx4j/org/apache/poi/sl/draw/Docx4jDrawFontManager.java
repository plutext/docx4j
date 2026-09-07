/* NOTICE: This file is a docx4j shim, not a copy of an Apache POI source file.
 *
 * It is the default DrawFontManager for the repackaged HWMF/HEMF renderer,
 * resolving GDI LOGFONT face names through docx4j's own font machinery
 * (org.docx4j.fonts.PhysicalFonts) before falling back to java.awt.Font.
 * The name-mapping / symbol-charset behaviour it inherits comes from Apache
 * POI's DrawFontManagerDefault.
 *
 * Written by Plutext Pty Ltd for docx4j; licensed under the Apache License,
 * Version 2.0, the same terms as the Apache POI code it builds on.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.sl.draw;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.org.apache.poi.common.usermodel.fonts.FontInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * docx4j's default {@link DrawFontManager} for WMF / EMF / EMF+ rendering.
 * <p>
 * A metafile names its fonts the way the producing application did (a GDI
 * {@code LOGFONT} face name, for example "Times New Roman" or "Symbol"). AWT can
 * only use fonts it has itself registered, so a face name that is not installed
 * silently degrades to Dialog. This manager therefore:
 * <ol>
 *   <li>maps the face name through {@link PhysicalFonts} (case-insensitively), so
 *       that whatever docx4j's font discovery found for that name is used;</li>
 *   <li>if AWT does not know the resulting family, but docx4j has a font file for
 *       it, creates the {@link Font} from that file with {@link Font#createFont}
 *       (results cached per font file);</li>
 *   <li>otherwise falls back to POI's behaviour: {@code new Font(typeface, ..)},
 *       and SansSerif rather than Dialog.</li>
 * </ol>
 * Install a different implementation per {@link Graphics2D} with the
 * {@link Drawable#FONT_HANDLER} rendering hint.
 *
 * @since 17.1.0 (CR-011 phase 1)
 */
public class Docx4jDrawFontManager extends DrawFontManagerDefault {

    private static final Logger log = LoggerFactory.getLogger(Docx4jDrawFontManager.class);

    /** Fonts created from a docx4j font file, keyed by that file's URI. */
    private static final Map<String,Font> createdFonts = new ConcurrentHashMap<>();

    /** URIs {@link Font#createFont} has already failed on; don't retry. */
    private static final Set<String> failedFonts = Collections.synchronizedSet(new HashSet<>());

    private static volatile Set<String> awtFamilies;

    @Override
    public FontInfo getMappedFont(Graphics2D graphics, FontInfo fontInfo) {
        // an explicit FONT_MAP hint wins, as in POI
        FontInfo mapped = super.getMappedFont(graphics, fontInfo);
        if (mapped == null || mapped.getTypeface() == null) {
            return mapped;
        }
        if (mapped != fontInfo) {
            // the caller mapped it explicitly; don't second-guess
            return mapped;
        }

        PhysicalFont pf = physicalFont(mapped.getTypeface());
        if (pf == null || pf.getName() == null || pf.getName().equals(mapped.getTypeface())) {
            return mapped;
        }

        // only substitute the name if AWT (or our own font file cache) can actually use it
        if (isKnownToAWT(pf.getName()) || fontFromFile(pf) != null) {
            return new RetypefacedFontInfo(mapped, pf.getName());
        }
        return mapped;
    }

    /**
     * A {@link FontInfo} which is the delegate in every respect except the typeface,
     * so that the charset / pitch / family the metafile declared survive the mapping
     * (POI's {@code DrawFontInfo} carries the typeface alone, which would lose the
     * symbol-charset flag that {@link #mapFontCharset} relies on).
     */
    private static final class RetypefacedFontInfo implements FontInfo {
        private final FontInfo delegate;
        private final String typeface;

        RetypefacedFontInfo(FontInfo delegate, String typeface) {
            this.delegate = delegate;
            this.typeface = typeface;
        }

        @Override
        public String getTypeface() {
            return typeface;
        }

        @Override
        public org.docx4j.org.apache.poi.common.usermodel.fonts.FontCharset getCharset() {
            return delegate.getCharset();
        }

        @Override
        public org.docx4j.org.apache.poi.common.usermodel.fonts.FontFamily getFamily() {
            return delegate.getFamily();
        }

        @Override
        public org.docx4j.org.apache.poi.common.usermodel.fonts.FontPitch getPitch() {
            return delegate.getPitch();
        }

        @Override
        public byte[] getPanose() {
            return delegate.getPanose();
        }

        @Override
        public Integer getIndex() {
            return delegate.getIndex();
        }
    }

    @Override
    public Font createAWTFont(Graphics2D graphics, FontInfo fontInfo, double fontSize, boolean bold, boolean italic) {
        final String typeface = (fontInfo == null) ? null : fontInfo.getTypeface();
        final int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);

        if (typeface != null && !isKnownToAWT(typeface)) {
            PhysicalFont pf = physicalFont(typeface);
            Font f = (pf == null) ? null : fontFromFile(pf);
            if (f != null) {
                return f.deriveFont(style, (float)fontSize);
            }
        }

        return super.createAWTFont(graphics, fontInfo, fontSize, bold, italic);
    }

    private static PhysicalFont physicalFont(String typeface) {
        if (typeface == null || typeface.isEmpty()) {
            return null;
        }
        try {
            return PhysicalFonts.get(typeface);
        } catch (Throwable t) {
            // Throwable, not Exception: PhysicalFonts' static initialiser can fail with an
            // ExceptionInInitializerError / NoClassDefFoundError if the optional font
            // machinery is not fully on the classpath, and font discovery may never have
            // run. Rendering a metafile must not depend on either, so degrade to AWT's
            // own font resolution.
            log.debug("PhysicalFonts lookup failed for '{}': {}", typeface, t.toString());
            return null;
        }
    }

    /**
     * @return the AWT font created from this physical font's file, or {@code null}
     *         if there is no usable file (or it could not be loaded)
     */
    private static Font fontFromFile(PhysicalFont pf) {
        URI uri = pf.getEmbeddedURI();
        if (uri == null) {
            return null;
        }
        final String key = uri.toString();
        Font cached = createdFonts.get(key);
        if (cached != null) {
            return cached;
        }
        if (failedFonts.contains(key)) {
            return null;
        }
        if (!"file".equalsIgnoreCase(uri.getScheme())) {
            failedFonts.add(key);
            return null;
        }
        try {
            File f = new File(uri);
            // Font.createFont handles TrueType and Type1; it cannot handle .ttc / .otc collections
            int type = key.toLowerCase().endsWith(".pfb") ? Font.TYPE1_FONT : Font.TRUETYPE_FONT;
            Font font = Font.createFont(type, f);
            createdFonts.put(key, font);
            return font;
        } catch (FontFormatException | IOException | IllegalArgumentException e) {
            log.debug("can't create an AWT font from {}: {}", key, e.getMessage());
            failedFonts.add(key);
            return null;
        }
    }

    private static boolean isKnownToAWT(String family) {
        Set<String> fams = awtFamilies;
        if (fams == null) {
            synchronized (Docx4jDrawFontManager.class) {
                fams = awtFamilies;
                if (fams == null) {
                    Set<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    try {
                        s.addAll(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getAvailableFontFamilyNames()));
                    } catch (Throwable t) {
                        // headless environments without fontconfig
                        log.debug("can't enumerate AWT font families: {}", t.getMessage());
                    }
                    awtFamilies = fams = s;
                }
            }
        }
        return fams.contains(family);
    }

    /** for tests: forget the cached AWT family names and created fonts */
    public static void resetCaches() {
        awtFamilies = null;
        createdFonts.clear();
        failedFonts.clear();
    }
}
