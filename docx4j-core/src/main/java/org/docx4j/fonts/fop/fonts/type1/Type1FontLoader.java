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

package org.docx4j.fonts.fop.fonts.type1;

import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.fonts.CodePointMapping;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.fonts.EmbeddingMode;
import org.docx4j.fonts.fop.fonts.FontLoader;
import org.docx4j.fonts.fop.fonts.FontType;
import org.docx4j.fonts.fop.fonts.FontUris;
import org.docx4j.fonts.fop.fonts.SingleByteEncoding;
import org.docx4j.fonts.fop.fonts.SingleByteFont;

/**
 * Loads a Type 1 font into memory directly from the original font file.
 */
public class Type1FontLoader extends FontLoader {

    private static final  Logger log = LoggerFactory.getLogger(Type1FontLoader.class);

    private SingleByteFont singleFont;

    private EmbeddingMode embeddingMode;

    private final FontUris fontUris;

    /**
     * Constructs a new Type 1 font loader.
     * @param fontUris the URI to the PFB file of a Type 1 font
     * @param embedded indicates whether the font is embedded or referenced
     * @param useKerning indicates whether to load kerning information if available
     * @param resourceResolver the font resolver used to resolve URIs
     * @throws IOException In case of an I/O error
     */
    public Type1FontLoader(FontUris fontUris, boolean embedded, EmbeddingMode embeddingMode,
            boolean useKerning, InternalResourceResolver resourceResolver) throws IOException {
        super(fontUris.getEmbed(), embedded, useKerning, true, resourceResolver);
        this.embeddingMode = embeddingMode;
        this.fontUris = fontUris;
    }

    private String getPFMURI(String pfbURI) {
        String pfbExt = pfbURI.substring(pfbURI.length() - 3, pfbURI.length());
        String pfmExt = pfbExt.substring(0, 2)
                + (Character.isUpperCase(pfbExt.charAt(2)) ? "M" : "m");
        return pfbURI.substring(0, pfbURI.length() - 4) + "." + pfmExt;
    }

    private static final String[] AFM_EXTENSIONS = new String[] {".AFM", ".afm", ".Afm"};

    /** {@inheritDoc} */
    @Override
    protected void read() throws IOException {
        AFMFile afm = null;
        PFMFile pfm = null;

        InputStream afmIn = null;
        String fontFileStr = fontFileURI.toASCIIString();
        String partialAfmUri = fontFileStr.substring(0, fontFileStr.length() - 4);
        String afmUri = (fontUris.getAfm() != null) ? fontUris.getAfm().toASCIIString() : null;
        if (afmUri == null) {
            for (String afmExtension : AFM_EXTENSIONS) {
                try {
                    afmUri = partialAfmUri + afmExtension;
                    afmIn = resourceResolver.getResource(afmUri);
                    if (afmIn != null) {
                        break;
                    }
                } catch (IOException ioe) {
                    // Ignore, AFM probably not available under the URI
                } catch (URISyntaxException e) {
                    // Ignore, AFM probably not available under the URI
                }
            }
        } else {
            try {
                afmIn = resourceResolver.getResource(afmUri);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        if (afmIn != null) {
            try {
                AFMParser afmParser = new AFMParser();
                afm = afmParser.parse(afmIn, afmUri);
            } finally {
                IOUtils.closeQuietly(afmIn);
            }
        }

        String pfmUri = (fontUris.getPfm() == null) ? getPFMURI(fontFileStr) : fontUris.getPfm()
                .toASCIIString();
        InputStream pfmIn = null;
        try {
            pfmIn = resourceResolver.getResource(pfmUri);
        } catch (IOException ioe) {
            // Ignore, PFM probably not available under the URI
        } catch (URISyntaxException e) {
            // Ignore, PFM probably not available under the URI
        }
        if (pfmIn != null) {
            try {
                pfm = new PFMFile();
                pfm.load(pfmIn);
            } catch (IOException ioe) {
                if (afm == null) {
                    // Ignore the exception if we have a valid PFM. PFM is only the fallback.
                    throw ioe;
                }
            } finally {
                IOUtils.closeQuietly(pfmIn);
            }
        }

        if (afm == null && pfm == null) {
            throw new java.io.FileNotFoundException(
                    "Neither an AFM nor a PFM file was found for " + this.fontFileURI);
        }
        buildFont(afm, pfm);
        this.loaded = true;
    }

    private void buildFont(AFMFile afm, PFMFile pfm) {
        if (afm == null && pfm == null) {
            throw new IllegalArgumentException("Need at least an AFM or a PFM!");
        }
        singleFont = new SingleByteFont(resourceResolver, embeddingMode);
        singleFont.setFontType(FontType.TYPE1);
        if (this.embedded) {
            singleFont.setEmbedURI(this.fontFileURI);
        }
        returnFont = singleFont;

        handleEncoding(afm, pfm);
        handleFontName(afm, pfm);
        handleMetrics(afm, pfm);
    }

    private void handleEncoding(AFMFile afm, PFMFile pfm) {
        // Encoding
        if (afm != null) {
            String encoding = afm.getEncodingScheme();
            singleFont.setUseNativeEncoding(true);
            if ("AdobeStandardEncoding".equals(encoding)) {
                singleFont.setEncoding(CodePointMapping.STANDARD_ENCODING);
                addUnencodedBasedOnEncoding(afm);
            } else {
                String effEncodingName;
                if ("FontSpecific".equals(encoding)) {
                    effEncodingName = afm.getFontName() + "Encoding";
                } else {
                    effEncodingName = encoding;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Unusual font encoding encountered: "
                            + encoding + " -> " + effEncodingName);
                }
                CodePointMapping mapping = buildCustomEncoding(effEncodingName, afm);
                singleFont.setEncoding(mapping);
                addUnencodedBasedOnAFM(afm);
            }
        } else {
            if (pfm.getCharSet() == 2 && !pfm.getCharSetName().equals("Symbol")) {
                int[] table = new int[256];
                String[] charNameMap = new String[256];
                int j = 0;
                for (int i = pfm.getFirstChar(); i < pfm.getLastChar(); i++) {
                    if (j < table.length) {
                        table[j] = i;
                        table[j + 1] = i;
                        j += 2;
                    }
                    charNameMap[i] = String.format("x%03o", i);
                }
                CodePointMapping mapping  = new CodePointMapping("custom", table, charNameMap);
                singleFont.setEncoding(mapping);
            } else if (pfm.getCharSet() >= 0 && pfm.getCharSet() <= 2) {
                singleFont.setEncoding(pfm.getCharSetName() + "Encoding");
            } else {
                log.warn("The PFM reports an unsupported encoding ("
                        + pfm.getCharSetName() + "). The font may not work as expected.");
                singleFont.setEncoding("WinAnsiEncoding"); // Try fallback, no guarantees!
            }
        }
    }

    private Set<String> toGlyphSet(String[] glyphNames) {
        Set<String> glyphSet = new java.util.HashSet<String>();
        Collections.addAll(glyphSet, glyphNames);
        return glyphSet;
    }

    /**
     * Adds characters not encoded in the font's primary encoding. This method is used when we
     * don't trust the AFM to expose the same encoding as the primary font.
     * @param afm the AFM file.
     */
    private void addUnencodedBasedOnEncoding(AFMFile afm) {
        SingleByteEncoding encoding = singleFont.getEncoding();
        Set<String> glyphNames = toGlyphSet(encoding.getCharNameMap());
        List<AFMCharMetrics> charMetrics = afm.getCharMetrics();
        for (AFMCharMetrics metrics : charMetrics) {
            String charName = metrics.getCharName();
            if (charName != null && !glyphNames.contains(charName)) {
                addUnencodedCharacter(singleFont, metrics);
            }
        }
    }

    private static void addUnencodedCharacter(SingleByteFont font, AFMCharMetrics metrics) {
        font.addUnencodedCharacter(metrics.getCharacter(),
                (int) Math.round(metrics.getWidthX()), metrics.getBBox());
    }

    /**
     * Adds characters not encoded in the font's primary encoding. This method is used when
     * the primary encoding is built based on the character codes in the AFM rather than
     * the specified encoding (ex. with symbolic fonts).
     * @param afm the AFM file
     */
    private void addUnencodedBasedOnAFM(AFMFile afm) {
        List charMetrics = afm.getCharMetrics();
        for (int i = 0, c = afm.getCharCount(); i < c; i++) {
            AFMCharMetrics metrics = (AFMCharMetrics)charMetrics.get(i);
            if (!metrics.hasCharCode() && metrics.getCharacter() != null) {
                addUnencodedCharacter(singleFont, metrics);
            }
        }
    }

    private void handleFontName(AFMFile afm, PFMFile pfm) {
        // Font name
        if (afm != null) {
            returnFont.setFontName(afm.getFontName()); // PostScript font name
            returnFont.setFullName(afm.getFullName());
            Set<String> names = new HashSet<String>();
            names.add(afm.getFamilyName());
            returnFont.setFamilyNames(names);
        } else {
            returnFont.setFontName(pfm.getPostscriptName());
            String fullName = pfm.getPostscriptName();
            fullName = fullName.replace('-', ' '); // Hack! Try to emulate full name
            returnFont.setFullName(fullName); // emulate afm.getFullName()
            Set<String> names = new HashSet<String>();
            names.add(pfm.getWindowsName()); // emulate afm.getFamilyName()
            returnFont.setFamilyNames(names);
        }
    }

    private void handleMetrics(AFMFile afm, PFMFile pfm) {
        // Basic metrics
        if (afm != null) {
            if (afm.getCapHeight() != null) {
                returnFont.setCapHeight(afm.getCapHeight().intValue());
            }
            if (afm.getXHeight() != null) {
                returnFont.setXHeight(afm.getXHeight().intValue());
            }
            if (afm.getAscender() != null) {
                returnFont.setAscender(afm.getAscender().intValue());
            }
            if (afm.getDescender() != null) {
                returnFont.setDescender(afm.getDescender().intValue());
            }

            returnFont.setFontBBox(afm.getFontBBoxAsIntArray());
            if (afm.getStdVW() != null) {
                returnFont.setStemV(afm.getStdVW().intValue());
            } else {
                returnFont.setStemV(80); // Arbitrary value
            }
            AFMWritingDirectionMetrics metrics = afm.getWritingDirectionMetrics(0);
            returnFont.setItalicAngle((int) metrics.getItalicAngle());
            returnFont.setUnderlinePosition(metrics.getUnderlinePosition().intValue());
            returnFont.setUnderlineThickness(metrics.getUnderlineThickness().intValue());
        } else {
            returnFont.setFontBBox(pfm.getFontBBox());
            returnFont.setStemV(pfm.getStemV());
            returnFont.setItalicAngle(pfm.getItalicAngle());
        }
        if (pfm != null) {
            // Sometimes the PFM has these metrics while the AFM doesn't (ex. Symbol)
            if (returnFont.getCapHeight() == 0) {
                returnFont.setCapHeight(pfm.getCapHeight());
            }
            if (returnFont.getXHeight(1) == 0) {
                returnFont.setXHeight(pfm.getXHeight());
            }
            if (returnFont.getAscender() == 0) {
                returnFont.setAscender(pfm.getLowerCaseAscent());
            }
            if (returnFont.getDescender() == 0) {
                returnFont.setDescender(pfm.getLowerCaseDescent());
            }
        }

        // Fallbacks when some crucial font metrics aren't available
        // (the following are all optional in AFM, but FontBBox is always available)
        if (returnFont.getXHeight(1) == 0) {
            int xHeight = 0;
            if (afm != null) {
                AFMCharMetrics chm = afm.getChar("x");
                if (chm != null) {
                    RectangularShape rect = chm.getBBox();
                    if (rect != null) {
                        xHeight = (int) Math.round(rect.getMinX());
                    }
                }
            }
            if (xHeight == 0) {
                xHeight = Math.round(returnFont.getFontBBox()[3] * 0.6f);
            }
            returnFont.setXHeight(xHeight);
        }
        if (returnFont.getAscender() == 0) {
            int asc = 0;
            if (afm != null) {
                AFMCharMetrics chm = afm.getChar("d");
                if (chm != null) {
                    RectangularShape rect = chm.getBBox();
                    if (rect != null) {
                        asc = (int) Math.round(rect.getMinX());
                    }
                }
            }
            if (asc == 0) {
                asc = Math.round(returnFont.getFontBBox()[3] * 0.9f);
            }
            returnFont.setAscender(asc);
        }
        if (returnFont.getDescender() == 0) {
            int desc = 0;
            if (afm != null) {
                AFMCharMetrics chm = afm.getChar("p");
                if (chm != null) {
                    RectangularShape rect = chm.getBBox();
                    if (rect != null) {
                        desc = (int) Math.round(rect.getMinX());
                    }
                }
            }
            if (desc == 0) {
                desc = returnFont.getFontBBox()[1];
            }
            returnFont.setDescender(desc);
        }
        if (returnFont.getCapHeight() == 0) {
            returnFont.setCapHeight(returnFont.getAscender());
        }

        if (afm != null) {
            String charSet = afm.getCharacterSet();
            int flags = 0;
            if ("Special".equals(charSet)) {
                flags |= 4; // bit 3: Symbolic
            } else {
                if (singleFont.getEncoding().mapChar('A') == 'A') {
                    // High likelyhood that the font is non-symbolic
                    flags |= 32; // bit 6: Nonsymbolic
                } else {
                    flags |= 4; // bit 3: Symbolic
                }
            }
            if (afm.getWritingDirectionMetrics(0).isFixedPitch()) {
                flags |= 1; // bit 1: FixedPitch
            }
            if (afm.getWritingDirectionMetrics(0).getItalicAngle() != 0.0) {
                flags |= 64; // bit 7: Italic
            }
            returnFont.setFlags(flags);

            returnFont.setFirstChar(afm.getFirstChar());
            returnFont.setLastChar(afm.getLastChar());
            for (AFMCharMetrics chm : afm.getCharMetrics()) {
                if (chm.hasCharCode()) {
                    singleFont.setWidth(chm.getCharCode(), (int) Math.round(chm.getWidthX()));
                    singleFont.setBoundingBox(chm.getCharCode(), chm.getBBox());
                }
            }
            if (useKerning) {
                returnFont.replaceKerningMap(afm.createXKerningMapEncoded());
            }
        } else {
            returnFont.setFlags(pfm.getFlags());
            returnFont.setFirstChar(pfm.getFirstChar());
            returnFont.setLastChar(pfm.getLastChar());
            for (short i = pfm.getFirstChar(); i <= pfm.getLastChar(); i++) {
                int cw = pfm.getCharWidth(i);
                singleFont.setWidth(i, cw);
                int[] bbox = pfm.getFontBBox();
                singleFont.setBoundingBox(i, new Rectangle(bbox[0], bbox[1], cw, bbox[3]));
            }
            if (useKerning) {
                returnFont.replaceKerningMap(pfm.getKerning());
            }
        }
    }

    private CodePointMapping buildCustomEncoding(String encodingName, AFMFile afm) {
        int mappingCount = 0;
        // Just count the first time...
        List<AFMCharMetrics> chars = afm.getCharMetrics();
        for (AFMCharMetrics charMetrics : chars) {
            if (charMetrics.getCharCode() >= 0) {
                mappingCount++;
            }
        }
        // ...and now build the table.
        int[] table = new int[mappingCount * 2];
        String[] charNameMap = new String[256];
        int idx = 0;
        for (AFMCharMetrics charMetrics : chars) {
            if (charMetrics.getCharCode() >= 0) {
                charNameMap[charMetrics.getCharCode()] = charMetrics.getCharName();
                String unicodes = charMetrics.getUnicodeSequence();
                if (unicodes == null) {
                    log.info("No Unicode mapping for glyph: " + charMetrics);
                    table[idx] = charMetrics.getCharCode();
                    idx++;
                    table[idx] = charMetrics.getCharCode();
                    idx++;
                } else if (unicodes.length() == 1) {
                    table[idx] = charMetrics.getCharCode();
                    idx++;
                    table[idx] = unicodes.charAt(0);
                    idx++;
                } else {
                    log.warn("Multi-character representation of glyph not currently supported: "
                            + charMetrics);
                }
            }
        }
        return new CodePointMapping(encodingName, table, charNameMap);
    }
}
