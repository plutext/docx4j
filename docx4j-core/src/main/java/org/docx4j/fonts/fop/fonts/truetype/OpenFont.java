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

package org.docx4j.fonts.fop.fonts.truetype;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.xmlgraphics.fonts.Glyphs;
import org.docx4j.fonts.fop.complexscripts.fonts.AdvancedTypographicTableFormatException;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphPositioningTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.OTFAdvancedTypographicTableReader;
import org.docx4j.fonts.fop.fonts.CMapSegment;
import org.docx4j.fonts.fop.fonts.FontUtil;
import org.docx4j.fonts.fop.fonts.MultiByteFont;

public abstract class OpenFont {

    static final byte NTABS = 24;
    static final int MAX_CHAR_CODE = 255;
    static final int ENC_BUF_SIZE = 1024;

    private static final String[] MAC_GLYPH_ORDERING = {
        /* 0x000 */
        ".notdef",          ".null",         "nonmarkingreturn", "space",
        "exclam",           "quotedbl",      "numbersign",       "dollar",
        "percent",          "ampersand",     "quotesingle",      "parenleft",
        "parenright",       "asterisk",      "plus",             "comma",
        /* 0x010 */
        "hyphen",           "period",        "slash",            "zero",
        "one",              "two",           "three",            "four",
        "five",             "six",           "seven",            "eight",
        "nine",             "colon",         "semicolon",        "less",
        /* 0x020 */
        "equal",            "greater",       "question",         "at",
        "A",                "B",             "C",                "D",
        "E",                "F",             "G",                "H",
        "I",                "J",             "K",                "L",
        /* 0x030 */
        "M",                "N",             "O",                "P",
        "Q",                "R",             "S",                "T",
        "U",                "V",             "W",                "X",
        "Y",                "Z",             "bracketleft",      "backslash",
        /* 0x040 */
        "bracketright",     "asciicircum",   "underscore",       "grave",
        "a",                "b",             "c",                "d",
        "e",                "f",             "g",                "h",
        "i",                "j",             "k",                "l",
        /* 0x050 */
        "m",                "n",             "o",                "p",
        "q",                "r",             "s",                "t",
        "u",                "v",             "w",                "x",
        "y",                "z",             "braceleft",        "bar",
        /* 0x060 */
        "braceright",       "asciitilde",    "Adieresis",        "Aring",
        "Ccedilla",         "Eacute",        "Ntilde",           "Odieresis",
        "Udieresis",        "aacute",        "agrave",           "acircumflex",
        "adieresis",        "atilde",        "aring",            "ccedilla",
        /* 0x070 */
        "eacute",           "egrave",        "ecircumflex",      "edieresis",
        "iacute",           "igrave",        "icircumflex",      "idieresis",
        "ntilde",           "oacute",        "ograve",           "ocircumflex",
        "odieresis",        "otilde",        "uacute",           "ugrave",
        /* 0x080 */
        "ucircumflex",      "udieresis",     "dagger",           "degree",
        "cent",             "sterling",      "section",          "bullet",
        "paragraph",        "germandbls",    "registered",       "copyright",
        "trademark",        "acute",         "dieresis",         "notequal",
        /* 0x090 */
        "AE",               "Oslash",        "infinity",         "plusminus",
        "lessequal",        "greaterequal",  "yen",              "mu",
        "partialdiff",      "summation",     "product",          "pi",
        "integral",         "ordfeminine",   "ordmasculine",     "Omega",
        /* 0x0A0 */
        "ae",               "oslash",        "questiondown",     "exclamdown",
        "logicalnot",       "radical",       "florin",           "approxequal",
        "Delta",            "guillemotleft", "guillemotright",   "ellipsis",
        "nonbreakingspace", "Agrave",        "Atilde",           "Otilde",
        /* 0x0B0 */
        "OE",               "oe",            "endash",           "emdash",
        "quotedblleft",     "quotedblright", "quoteleft",        "quoteright",
        "divide",           "lozenge",       "ydieresis",        "Ydieresis",
        "fraction",         "currency",      "guilsinglleft",    "guilsinglright",
        /* 0x0C0 */
        "fi",               "fl",            "daggerdbl",        "periodcentered",
        "quotesinglbase",   "quotedblbase",  "perthousand",      "Acircumflex",
        "Ecircumflex",      "Aacute",        "Edieresis",        "Egrave",
        "Iacute",           "Icircumflex",   "Idieresis",        "Igrave",
        /* 0x0D0 */
        "Oacute",           "Ocircumflex",   "apple",            "Ograve",
        "Uacute",           "Ucircumflex",   "Ugrave",           "dotlessi",
        "circumflex",       "tilde",         "macron",           "breve",
        "dotaccent",        "ring",          "cedilla",          "hungarumlaut",
        /* 0x0E0 */
        "ogonek",           "caron",         "Lslash",           "lslash",
        "Scaron",           "scaron",        "Zcaron",           "zcaron",
        "brokenbar",        "Eth",           "eth",              "Yacute",
        "yacute",           "Thorn",         "thorn",            "minus",
        /* 0x0F0 */
        "multiply",         "onesuperior",   "twosuperior",      "threesuperior",
        "onehalf",          "onequarter",    "threequarters",    "franc",
        "Gbreve",           "gbreve",        "Idotaccent",       "Scedilla",
        "scedilla",         "Cacute",        "cacute",           "Ccaron",
        /* 0x100 */
        "ccaron",           "dcroat"
    };

    /** The FontFileReader used to read this TrueType font. */
    protected FontFileReader fontFile;

    /** Set to true to get even more debug output than with level DEBUG */
    public static final boolean TRACE_ENABLED = false;

    private static final String ENCODING = "WinAnsiEncoding";    // Default encoding

    private static final short FIRST_CHAR = 0;

    protected boolean useKerning;
    private boolean isEmbeddable = true;
    private boolean hasSerifs = true;
    /**
     * Table directory
     */
    protected Map<OFTableName, OFDirTabEntry> dirTabs;

    private Map<Integer, Map<Integer, Integer>> kerningTab; // for CIDs
    private Map<Integer, Map<Integer, Integer>> ansiKerningTab; // For winAnsiEncoding
    private List<CMapSegment> cmaps;
    protected List<UnicodeMapping> unicodeMappings;

    private int upem;                                // unitsPerEm from "head" table
    protected int nhmtx;                               // Number of horizontal metrics
    private PostScriptVersion postScriptVersion;
    protected int locaFormat;
    /**
     * Offset to last loca
     */
    protected long lastLoca;
    protected int numberOfGlyphs; // Number of glyphs in font (read from "maxp" table)

    /**
     * Contains glyph data
     */
    protected OFMtxEntry[] mtxTab;                  // Contains glyph data

    protected String postScriptName = "";
    protected String fullName = "";
    protected String embedFontName = "";
    protected String notice = "";
    protected final Set<String> familyNames = new HashSet<String>();
    protected String subFamilyName = "";
    protected boolean cid = true;

    private long italicAngle;
    private long isFixedPitch;
    private int fontBBox1;
    private int fontBBox2;
    private int fontBBox3;
    private int fontBBox4;
    private int capHeight;
    private int os2CapHeight;
    private int underlinePosition;
    private int underlineThickness;
    private int strikeoutPosition;
    private int strikeoutThickness;
    private int xHeight;
    private int os2xHeight;
    //Effective ascender/descender
    private int ascender;
    private int descender;
    //Ascender/descender from hhea table
    private int hheaAscender;
    private int hheaDescender;
    //Ascender/descender from OS/2 table
    private int os2Ascender;
    private int os2Descender;
    private int usWeightClass;

    private short lastChar;

    private int[] ansiWidth;
    private Map<Integer, List<Integer>> ansiIndex;

    // internal mapping of glyph indexes to unicode indexes
    // used for quick mappings in this class
    private final Map<Integer, Integer> glyphToUnicodeMap = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> unicodeToGlyphMap = new HashMap<Integer, Integer>();

    private boolean isCFF;

    // advanced typographic table support
    protected boolean useAdvanced;
    protected OTFAdvancedTypographicTableReader advancedTableReader;

    /**
     * Version of the PostScript table (post) contained in this font.
     */
    public static enum PostScriptVersion {
        /** PostScript table version 1.0. */
        V1,
        /** PostScript table version 2.0. */
        V2,
        /** PostScript table version 3.0. */
        V3,
        /** Unknown version of the PostScript table. */
        UNKNOWN;
    }

    /**
     * logging instance
     */
    protected  Logger log = LoggerFactory.getLogger(TTFFile.class);

    public OpenFont() {
        this(true, false);
    }

    /**
     * Constructor
     * @param useKerning true if kerning data should be loaded
     * @param useAdvanced true if advanced typographic tables should be loaded
     */
    public OpenFont(boolean useKerning, boolean useAdvanced) {
        this.useKerning = useKerning;
        this.useAdvanced = useAdvanced;
    }

    /**
     * Key-value helper class.
     */
    static final class UnicodeMapping implements Comparable {

        private final int unicodeIndex;
        private final int glyphIndex;

        UnicodeMapping(OpenFont font, int glyphIndex, int unicodeIndex) {
            this.unicodeIndex = unicodeIndex;
            this.glyphIndex = glyphIndex;
            font.glyphToUnicodeMap.put(glyphIndex, unicodeIndex);
            font.unicodeToGlyphMap.put(unicodeIndex, glyphIndex);
        }

        /**
         * Returns the glyphIndex.
         * @return the glyph index
         */
        public int getGlyphIndex() {
            return glyphIndex;
        }

        /**
         * Returns the unicodeIndex.
         * @return the Unicode index
         */
        public int getUnicodeIndex() {
            return unicodeIndex;
        }


        /** {@inheritDoc} */
        public int hashCode() {
            int hc = unicodeIndex;
            hc = 19 * hc + (hc ^ glyphIndex);
            return hc;
        }

        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if (o instanceof UnicodeMapping) {
                UnicodeMapping m = (UnicodeMapping) o;
                if (unicodeIndex != m.unicodeIndex) {
                    return false;
                } else {
                    return (glyphIndex == m.glyphIndex);
                }
            } else {
                return false;
            }
        }

        /** {@inheritDoc} */
        public int compareTo(Object o) {
            if (o instanceof UnicodeMapping) {
                UnicodeMapping m = (UnicodeMapping) o;
                if (unicodeIndex > m.unicodeIndex) {
                    return 1;
                } else if (unicodeIndex < m.unicodeIndex) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        }
    }

    /**
     * Obtain directory table entry.
     * @param name (tag) of entry
     * @return a directory table entry or null if none found
     */
    public OFDirTabEntry getDirectoryEntry(OFTableName name) {
        return dirTabs.get(name);
    }

    /**
     * Position inputstream to position indicated
     * in the dirtab offset + offset
     * @param in font file reader
     * @param tableName (tag) of table
     * @param offset from start of table
     * @return true if seek succeeded
     * @throws IOException if I/O exception occurs during seek
     */
    public boolean seekTab(FontFileReader in, OFTableName tableName,
                  long offset) throws IOException {
        OFDirTabEntry dt = dirTabs.get(tableName);
        if (dt == null) {
            log.info("Dirtab " + tableName.getName() + " not found.");
            return false;
        } else {
            in.seekSet(dt.getOffset() + offset);
        }
        return true;
    }

    /**
     * Convert from truetype unit to pdf unit based on the
     * unitsPerEm field in the "head" table
     * @param n truetype unit
     * @return pdf unit
     */
    public int convertTTFUnit2PDFUnit(int n) {
        int ret;
        if (n < 0) {
            long rest1 = n % upem;
            long storrest = 1000 * rest1;
            long ledd2 = (storrest != 0 ? rest1 / storrest : 0);
            ret = -((-1000 * n) / upem - (int)ledd2);
        } else {
            ret = (n / upem) * 1000 + ((n % upem) * 1000) / upem;
        }

        return ret;
    }

    /**
     * Read the cmap table,
     * return false if the table is not present or only unsupported
     * tables are present. Currently only unicode cmaps are supported.
     * Set the unicodeIndex in the TTFMtxEntries and fills in the
     * cmaps vector.
     *
     * @see <a href="https://developer.apple.com/fonts/TrueType-Reference-Manual/RM06/Chap6cmap.html">
     *          TrueType-Reference-Manual
     *      </a>
     */
    protected boolean readCMAP() throws IOException {

        unicodeMappings = new ArrayList<OpenFont.UnicodeMapping>();

        if (!seekTab(fontFile, OFTableName.CMAP, 2)) {
            return true;
        }
        int numCMap = fontFile.readTTFUShort();    // Number of cmap subtables
        long cmapUniOffset = 0;
        long symbolMapOffset = 0;
        long surrogateMapOffset = 0;

        if (log.isDebugEnabled()) {
            log.debug(numCMap + " cmap tables");
        }

        //Read offset for all tables. We are only interested in the unicode table
        for (int i = 0; i < numCMap; i++) {
            int cmapPID = fontFile.readTTFUShort();
            int cmapEID = fontFile.readTTFUShort();
            long cmapOffset = fontFile.readTTFLong();

            if (log.isDebugEnabled()) {
                log.debug("Platform ID: " + cmapPID + " Encoding: " + cmapEID);
            }

            if (cmapPID == 3 && cmapEID == 1) {
                cmapUniOffset = cmapOffset;
            }
            if (cmapPID == 3 && cmapEID == 0) {
                symbolMapOffset = cmapOffset;
            }
            if (cmapPID == 3 && cmapEID == 10) {
                surrogateMapOffset = cmapOffset;
            }
        }

       if (surrogateMapOffset > 0) {
            // TODO maybe for SingleByte fonts instances we should not reach this branch
            return readUnicodeCmap(surrogateMapOffset, 10);
        } else if (cmapUniOffset > 0) {
            return readUnicodeCmap(cmapUniOffset, 1);
        } else if (symbolMapOffset > 0) {
            return readUnicodeCmap(symbolMapOffset, 0);
        } else {
            log.error("Unsupported TrueType font: No Unicode or Symbol cmap table"
                    + " not present. Aborting");
            return false;
        }
    }

    private boolean readUnicodeCmap(long cmapUniOffset, int encodingID)
            throws IOException {
        //Read CMAP table and correct mtxTab.index
        int mtxPtr = 0;

        // Read unicode cmap
        seekTab(fontFile, OFTableName.CMAP, cmapUniOffset);
        int cmapFormat = fontFile.readTTFUShort();

        if (cmapFormat < 8) {
            fontFile.readTTFUShort(); //skip cmap length
            fontFile.readTTFUShort(); //skip cmap version
        } else {
            fontFile.readTTFUShort(); //skip 2 bytes to read a Fixed32
            fontFile.readTTFULong(); //skip cmap length
            fontFile.readTTFULong(); //skip cmap version
        }

        if (log.isDebugEnabled()) {
            log.debug("CMAP format: " + cmapFormat);
        }

        if (cmapFormat == 4) {
            int cmapSegCountX2 = fontFile.readTTFUShort();
            int cmapSearchRange = fontFile.readTTFUShort();
            int cmapEntrySelector = fontFile.readTTFUShort();
            int cmapRangeShift = fontFile.readTTFUShort();

            if (log.isDebugEnabled()) {
                log.debug("segCountX2   : " + cmapSegCountX2);
                log.debug("searchRange  : " + cmapSearchRange);
                log.debug("entrySelector: " + cmapEntrySelector);
                log.debug("rangeShift   : " + cmapRangeShift);
            }


            int[] cmapEndCounts = new int[cmapSegCountX2 / 2];
            int[] cmapStartCounts = new int[cmapSegCountX2 / 2];
            int[] cmapDeltas = new int[cmapSegCountX2 / 2];
            int[] cmapRangeOffsets = new int[cmapSegCountX2 / 2];

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapEndCounts[i] = fontFile.readTTFUShort();
            }

            fontFile.skip(2);    // Skip reservedPad

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapStartCounts[i] = fontFile.readTTFUShort();
            }

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapDeltas[i] = fontFile.readTTFShort();
            }

            //int startRangeOffset = in.getCurrentPos();

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapRangeOffsets[i] = fontFile.readTTFUShort();
            }

            int glyphIdArrayOffset = fontFile.getCurrentPos();

            BitSet eightBitGlyphs = new BitSet(256);

            // Insert the unicode id for the glyphs in mtxTab
            // and fill in the cmaps ArrayList
            for (int i = 0; i < cmapStartCounts.length; i++) {

                if (log.isTraceEnabled()) {
                    log.trace(i + ": " + cmapStartCounts[i]
                                                         + " - " + cmapEndCounts[i]);
                }
                if (log.isDebugEnabled()) {
                    if (isInPrivateUseArea(cmapStartCounts[i], cmapEndCounts[i])) {
                        log.debug("Font contains glyphs in the Unicode private use area: "
                                + Integer.toHexString(cmapStartCounts[i]) + " - "
                                + Integer.toHexString(cmapEndCounts[i]));
                    }
                }

                for (int j = cmapStartCounts[i]; j <= cmapEndCounts[i]; j++) {

                    // Update lastChar
                    if (j < 256 && j > lastChar) {
                        lastChar = (short)j;
                    }

                    if (j < 256) {
                        eightBitGlyphs.set(j);
                    }

                    if (mtxPtr < mtxTab.length) {
                        int glyphIdx;
                        // the last character 65535 = .notdef
                        // may have a range offset
                        if (cmapRangeOffsets[i] != 0 && j != 65535) {
                            int glyphOffset = glyphIdArrayOffset
                                + ((cmapRangeOffsets[i] / 2)
                                    + (j - cmapStartCounts[i])
                                    + (i)
                                    - cmapSegCountX2 / 2) * 2;
                            fontFile.seekSet(glyphOffset);
                            glyphIdx = (fontFile.readTTFUShort() + cmapDeltas[i])
                                       & 0xffff;
                            //mtxTab[glyphIdx].setName(mtxTab[glyphIdx].getName() + " - "+(char)j);
                            unicodeMappings.add(new UnicodeMapping(this, glyphIdx, j));
                            mtxTab[glyphIdx].getUnicodeIndex().add(j);

                            if (encodingID == 0 && j >= 0xF020 && j <= 0xF0FF) {
                                //Experimental: Mapping 0xF020-0xF0FF to 0x0020-0x00FF
                                //Tested with Wingdings and Symbol TTF fonts which map their
                                //glyphs in the region 0xF020-0xF0FF.
                                int mapped = j - 0xF000;
                                if (!eightBitGlyphs.get(mapped)) {
                                    //Only map if Unicode code point hasn't been mapped before
                                    unicodeMappings.add(new UnicodeMapping(this, glyphIdx, mapped));
                                    mtxTab[glyphIdx].getUnicodeIndex().add(mapped);
                                }
                            }

                            // Also add winAnsiWidth
                            List<Integer> v = ansiIndex.get(j);
                            if (v != null) {
                                for (Integer aIdx : v) {
                                    ansiWidth[aIdx]
                                        = mtxTab[glyphIdx].getWx();

                                    if (log.isTraceEnabled()) {
                                        log.trace("Added width "
                                                + mtxTab[glyphIdx].getWx()
                                                + " uni: " + j
                                                + " ansi: " + aIdx);
                                    }
                                }
                            }

                            if (log.isTraceEnabled()) {
                                log.trace("Idx: "
                                        + glyphIdx
                                        + " Delta: " + cmapDeltas[i]
                                        + " Unicode: " + j
                                        + " name: " + mtxTab[glyphIdx].getName());
                            }
                        } else {
                            glyphIdx = (j + cmapDeltas[i]) & 0xffff;

                            if (glyphIdx < mtxTab.length) {
                                mtxTab[glyphIdx].getUnicodeIndex().add(j);
                            } else {
                                log.debug("Glyph " + glyphIdx
                                                   + " out of range: "
                                                   + mtxTab.length);
                            }

                            unicodeMappings.add(new UnicodeMapping(this, glyphIdx, j));
                            if (glyphIdx < mtxTab.length) {
                                mtxTab[glyphIdx].getUnicodeIndex().add(j);
                            } else {
                                log.debug("Glyph " + glyphIdx
                                                   + " out of range: "
                                                   + mtxTab.length);
                            }

                            // Also add winAnsiWidth
                            List<Integer> v = ansiIndex.get(j);
                            if (v != null) {
                                for (Integer aIdx : v) {
                                    ansiWidth[aIdx] = mtxTab[glyphIdx].getWx();
                                }
                            }

                            //getLogger().debug("IIdx: " +
                            //    mtxPtr +
                            //    " Delta: " + cmap_deltas[i] +
                            //    " Unicode: " + j +
                            //    " name: " +
                            //    mtxTab[(j+cmap_deltas[i]) & 0xffff].name);

                        }
                        if (glyphIdx < mtxTab.length) {
                            if (mtxTab[glyphIdx].getUnicodeIndex().size() < 2) {
                                mtxPtr++;
                            }
                        }
                    }
                }
            }
        } else if (cmapFormat == 12) {
            long nGroups = fontFile.readTTFULong();

            for (long i = 0; i < nGroups; ++i) {
                long startCharCode = fontFile.readTTFULong();
                long endCharCode = fontFile.readTTFULong();
                long startGlyphCode = fontFile.readTTFULong();

                if (startCharCode < 0 || startCharCode > 0x10FFFFL) {
                    log.warn("startCharCode outside Unicode range");
                    continue;
                }

                if (startCharCode >= 0xD800 && startCharCode <= 0xDFFF) {
                    log.warn("startCharCode is a surrogate pair: " + startCharCode);
                }

                //endCharCode outside unicode range or is surrogate pair.
                if (endCharCode > 0 && endCharCode < startCharCode || endCharCode > 0x10FFFFL) {
                    log.warn("startCharCode outside Unicode range");
                    continue;
                }

                if (endCharCode >= 0xD800 && endCharCode <= 0xDFFF) {
                    log.warn("endCharCode is a surrogate pair: " + startCharCode);
                }

                for (long offset = 0; offset <= endCharCode - startCharCode; ++offset) {
                    long glyphIndexL = startGlyphCode + offset;
                    long charCodeL = startCharCode + offset;

                    if (glyphIndexL >= numberOfGlyphs) {
                        log.warn("Format 12 cmap contains an invalid glyph index");
                        break;
                    }

                    if (charCodeL > 0x10FFFFL) {
                        log.warn("Format 12 cmap contains character beyond UCS-4");
                    }

                    if (glyphIndexL > Integer.MAX_VALUE) {
                        log.error("glyphIndex > Integer.MAX_VALUE");
                        continue;
                    }

                    if (charCodeL > Integer.MAX_VALUE) {
                        log.error("startCharCode + j > Integer.MAX_VALUE");
                        continue;
                    }

                    // Update lastChar
                    if (charCodeL < 0xFF && charCodeL > lastChar) {
                        lastChar = (short) charCodeL;
                    }

                    int charCode = (int) charCodeL;
                    int glyphIndex = (int) glyphIndexL;

                    // Also add winAnsiWidth.
                    List<Integer> ansiIndexes = null;

                    if (charCodeL <= Character.MAX_VALUE) {
                        ansiIndexes = ansiIndex.get((int) charCodeL);
                    }

                    unicodeMappings.add(new UnicodeMapping(this, glyphIndex, charCode));
                    mtxTab[glyphIndex].getUnicodeIndex().add(charCode);

                    if (ansiIndexes == null) {
                        continue;
                    }

                    for (Integer aIdx : ansiIndexes) {
                        ansiWidth[aIdx] = mtxTab[glyphIndex].getWx();

                        if (log.isTraceEnabled()) {
                            log.trace("Added width "
                                    + mtxTab[glyphIndex].getWx()
                                    + " uni: " + offset
                                    + " ansi: " + aIdx);
                        }
                    }
                }
            }
        } else {
            log.error("Cmap format not supported: " + cmapFormat);
            return false;
        }
        return true;
    }

    private boolean isInPrivateUseArea(int start, int end) {
        return (isInPrivateUseArea(start) || isInPrivateUseArea(end));
    }

    private boolean isInPrivateUseArea(int unicode) {
        return (unicode >= 0xE000 && unicode <= 0xF8FF);
    }

    /**
     *
     * @return mmtx data
     */
    public List<OFMtxEntry> getMtx() {
        return Collections.unmodifiableList(Arrays.asList(mtxTab));
    }

    /**
     * Print first char/last char
     */
    /* not used
    private void printMaxMin() {
        int min = 255;
        int max = 0;
        for (int i = 0; i < mtxTab.length; i++) {
            if (mtxTab[i].getIndex() < min) {
                min = mtxTab[i].getIndex();
            }
            if (mtxTab[i].getIndex() > max) {
                max = mtxTab[i].getIndex();
            }
        }
        log.info("Min: " + min);
        log.info("Max: " + max);
    }
    */


    /**
     * Reads the font using a FontFileReader.
     *
     * @param in The FontFileReader to use
     * @throws IOException In case of an I/O problem
     */
    public void readFont(FontFileReader in, String header) throws IOException {
        readFont(in, header, (String)null);
    }

    /**
     * initialize the ansiWidths array (for winAnsiEncoding)
     * and fill with the missingwidth
     */
    protected void initAnsiWidths() {
        ansiWidth = new int[256];
        for (int i = 0; i < 256; i++) {
            ansiWidth[i] = mtxTab[0].getWx();
        }

        // Create an index hash to the ansiWidth
        // Can't just index the winAnsiEncoding when inserting widths
        // same char (eg bullet) is repeated more than one place
        ansiIndex = new HashMap<Integer, List<Integer>>();
        for (int i = 32; i < Glyphs.WINANSI_ENCODING.length; i++) {
            Integer ansi = i;
            Integer uni = (int) Glyphs.WINANSI_ENCODING[i];

            List<Integer> v = ansiIndex.get(uni);
            if (v == null) {
                v = new ArrayList<Integer>();
                ansiIndex.put(uni, v);
            }
            v.add(ansi);
        }
    }

    /**
     * Read the font data.
     * If the fontfile is a TrueType Collection (.ttc file)
     * the name of the font to read data for must be supplied,
     * else the name is ignored.
     *
     * @param in The FontFileReader to use
     * @param name The name of the font
     * @return boolean Returns true if the font is valid
     * @throws IOException In case of an I/O problem
     */
    public boolean readFont(FontFileReader in, String header, String name) throws IOException {
        initializeFont(in);
        /*
         * Check if TrueType collection, and that the name
         * exists in the collection
         */
        if (!checkTTC(header, name)) {
            if (name == null) {
                throw new IllegalArgumentException(
                    "For TrueType collection you must specify which font "
                    + "to select (-ttcname)");
            } else {
                throw new IOException(
                    "Name does not exist in the TrueType collection: " + name);
            }
        }

        readDirTabs();
        readFontHeader();
        getNumGlyphs();
        if (log.isDebugEnabled()) {
            log.debug("Number of glyphs in font: " + numberOfGlyphs);
        }
        readHorizontalHeader();
        readHorizontalMetrics();
        initAnsiWidths();
        readPostScript();
        readOS2();
        determineAscDesc();

        readName();
        boolean pcltFound = readPCLT();
        // Read cmap table and fill in ansiwidths
        boolean valid = readCMAP();
        if (!valid) {
            return false;
        }

        // Create cmaps for bfentries
        createCMaps();
        updateBBoxAndOffset();

        if (useKerning) {
            readKerning();
        }
        handleCharacterSpacing(in);

        guessVerticalMetricsFromGlyphBBox();
        return true;
    }

    /**
     * Reads a font.
     *
     * @param in FontFileReader to read from
     * @throws IOException in case of an I/O problem
     */
    public void readFont(FontFileReader in, String header, MultiByteFont mbfont) throws IOException {
        readFont(in, header, mbfont.getTTCName());
    }

    protected abstract void updateBBoxAndOffset() throws IOException;

    protected abstract void readName() throws IOException;

    protected abstract void initializeFont(FontFileReader in) throws IOException;

    protected void handleCharacterSpacing(FontFileReader in) throws IOException {
        // Read advanced typographic tables.
        if (useAdvanced) {
            try {
                OTFAdvancedTypographicTableReader atr
                    = new OTFAdvancedTypographicTableReader(this, in);
                atr.readAll();
                this.advancedTableReader = atr;
            } catch (AdvancedTypographicTableFormatException e) {
                log.warn(
                    "Encountered format constraint violation in advanced (typographic) table (AT) "
                    + "in font '" + getFullName() + "', ignoring AT data: "
                    + e.getMessage()
                );
            }
        }

    }

    protected void createCMaps() {
        cmaps = new ArrayList<CMapSegment>();
        int unicodeStart;
        int glyphStart;
        int unicodeEnd;
        if (unicodeMappings.isEmpty()) {
            return;
        }
        Iterator<UnicodeMapping> e = unicodeMappings.iterator();
        UnicodeMapping um = e.next();
        UnicodeMapping lastMapping = um;

        unicodeStart = um.getUnicodeIndex();
        glyphStart = um.getGlyphIndex();

        while (e.hasNext()) {
            um = e.next();
            if (((lastMapping.getUnicodeIndex() + 1) != um.getUnicodeIndex())
                    || ((lastMapping.getGlyphIndex() + 1) != um.getGlyphIndex())) {
                unicodeEnd = lastMapping.getUnicodeIndex();
                cmaps.add(new CMapSegment(unicodeStart, unicodeEnd, glyphStart));
                unicodeStart = um.getUnicodeIndex();
                glyphStart = um.getGlyphIndex();
            }
            lastMapping = um;
        }

        unicodeEnd = lastMapping.getUnicodeIndex();
        cmaps.add(new CMapSegment(unicodeStart, unicodeEnd, glyphStart));
    }

    /**
     * Returns the PostScript name of the font.
     * @return String The PostScript name
     */
    public String getPostScriptName() {
        if (postScriptName.length() == 0) {
            return FontUtil.stripWhiteSpace(getFullName());
        } else {
            return postScriptName;
        }
    }

    PostScriptVersion getPostScriptVersion() {
        return postScriptVersion;
    }

    /**
     * Returns the font family names of the font.
     * @return Set The family names (a Set of Strings)
     */
    public Set<String> getFamilyNames() {
        return familyNames;
    }

    /**
     * Returns the font sub family name of the font.
     * @return String The sub family name
     */
    public String getSubFamilyName() {
        return subFamilyName;
    }

    /**
     * Returns the full name of the font.
     * @return String The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the name of the character set used.
     * @return String The caracter set
     */
    public String getCharSetName() {
        return ENCODING;
    }

    /**
     * Returns the CapHeight attribute of the font.
     * @return int The CapHeight
     */
    public int getCapHeight() {
        return convertTTFUnit2PDFUnit(capHeight);
    }

    /**
     * Returns the XHeight attribute of the font.
     * @return int The XHeight
     */
    public int getXHeight() {
        return convertTTFUnit2PDFUnit(xHeight);
    }

    /**
     * Returns the number of bytes necessary to pad the currentPosition so that a table begins
     * on a 4-byte boundary.
     * @param currentPosition the position to pad.
     * @return int the number of bytes to pad.
     */
    protected int getPadSize(int currentPosition) {
        int padSize = 4 - (currentPosition % 4);
        return padSize < 4 ? padSize : 0;
    }

    /**
     * Returns the Flags attribute of the font.
     * @return int The Flags
     */
    public int getFlags() {
        int flags = 32;    // Use Adobe Standard charset
        if (italicAngle != 0) {
            flags |=  64;
        }
        if (isFixedPitch != 0) {
            flags |=  2;
        }
        if (hasSerifs) {
            flags |= 1;
        }
        return flags;
    }

    /**
     * Returns the weight class of this font. Valid values are 100, 200....,800, 900.
     * @return the weight class value (or 0 if there was no OS/2 table in the font)
     */
    public int getWeightClass() {
        return this.usWeightClass;
    }

    /**
     * Returns the StemV attribute of the font.
     * @return String The StemV
     */
    public String getStemV() {
        return "0";
    }

    /**
     * Returns the ItalicAngle attribute of the font.
     * @return String The ItalicAngle
     */
    public String getItalicAngle() {
        String ia = Short.toString((short)(italicAngle / 0x10000));

        // This is the correct italic angle, however only int italic
        // angles are supported at the moment so this is commented out.
        /*
         * if ((italicAngle % 0x10000) > 0 )
         * ia=ia+(comma+Short.toString((short)((short)((italicAngle % 0x10000)*1000)/0x10000)));
         */
        return ia;
    }

    /**
     * @return int[] The font bbox
     */
    public int[] getFontBBox() {
        final int[] fbb = new int[4];
        fbb[0] = convertTTFUnit2PDFUnit(fontBBox1);
        fbb[1] = convertTTFUnit2PDFUnit(fontBBox2);
        fbb[2] = convertTTFUnit2PDFUnit(fontBBox3);
        fbb[3] = convertTTFUnit2PDFUnit(fontBBox4);

        return fbb;
    }

    /**
     * Returns the original bounding box values from the HEAD table
     * @return An array of bounding box values
     */
    public int[] getBBoxRaw() {
        int[] bbox = {fontBBox1, fontBBox2, fontBBox3, fontBBox4};
        return bbox;
    }

    /**
     * Returns the LowerCaseAscent attribute of the font.
     * @return int The LowerCaseAscent
     */
    public int getLowerCaseAscent() {
        return convertTTFUnit2PDFUnit(ascender);
    }

    /**
     * Returns the LowerCaseDescent attribute of the font.
     * @return int The LowerCaseDescent
     */
    public int getLowerCaseDescent() {
        return convertTTFUnit2PDFUnit(descender);
    }

    /**
     * Returns the index of the last character, but this is for WinAnsiEncoding
     * only, so the last char is &lt; 256.
     * @return short Index of the last character (&lt;256)
     */
    public short getLastChar() {
        return lastChar;
    }

    /**
     * Returns the index of the first character.
     * @return short Index of the first character
     */
    public short getFirstChar() {
        return FIRST_CHAR;
    }

    /**
     * Returns an array of character widths.
     * @return int[] The character widths
     */
    public int[] getWidths() {
        int[] wx = new int[mtxTab.length];
        for (int i = 0; i < wx.length; i++) {
            wx[i] = convertTTFUnit2PDFUnit(mtxTab[i].getWx());
        }
        return wx;
    }

    public Rectangle[] getBoundingBoxes() {
        Rectangle[] boundingBoxes = new Rectangle[mtxTab.length];
        for (int i = 0; i < boundingBoxes.length; i++) {
            int[] boundingBox = mtxTab[i].getBoundingBox();
            boundingBoxes[i] = new Rectangle(
                    convertTTFUnit2PDFUnit(boundingBox[0]),
                    convertTTFUnit2PDFUnit(boundingBox[1]),
                    convertTTFUnit2PDFUnit(boundingBox[2] - boundingBox[0]),
                    convertTTFUnit2PDFUnit(boundingBox[3] - boundingBox[1]));
        }
        return boundingBoxes;
    }

    /**
     * Returns an array (xMin, yMin, xMax, yMax) for a glyph.
     *
     * @param glyphIndex the index of the glyph
     * @return int[] Array defining bounding box.
     */
    public int[] getBBox(int glyphIndex) {
        int[] bbox = new int[4];
        if (glyphIndex < mtxTab.length) {
            int[] bboxInTTFUnits = mtxTab[glyphIndex].getBoundingBox();
            for (int i = 0; i < 4; i++) {
                bbox[i] = convertTTFUnit2PDFUnit(bboxInTTFUnits[i]);
            }
        }
        return bbox;
    }

    /**
     * Returns the width of a given character.
     * @param idx Index of the character
     * @return int Standard width
     */
    public int getCharWidth(int idx) {
        return convertTTFUnit2PDFUnit(ansiWidth[idx]);
    }

    /**
     * Returns the width of a given character in raw units
     * @param idx Index of the character
     * @return int Width in it's raw form stored in the font
     */
    public int getCharWidthRaw(int idx) {
        if (ansiWidth != null) {
            return ansiWidth[idx];
        }
        return -1;
    }

    /**
     * Returns the kerning table.
     * @return Map The kerning table
     */
    public Map<Integer, Map<Integer, Integer>> getKerning() {
        return kerningTab;
    }

    /**
     * Returns the ANSI kerning table.
     * @return Map The ANSI kerning table
     */
    public Map<Integer, Map<Integer, Integer>> getAnsiKerning() {
        return ansiKerningTab;
    }

    public int getUnderlinePosition() {
        return convertTTFUnit2PDFUnit(underlinePosition);
    }

    public int getUnderlineThickness() {
        return convertTTFUnit2PDFUnit(underlineThickness);
    }

    public int getStrikeoutPosition() {
        return convertTTFUnit2PDFUnit(strikeoutPosition);
    }

    public int getStrikeoutThickness() {
        return convertTTFUnit2PDFUnit(strikeoutThickness);
    }

    /**
     * Indicates if the font may be embedded.
     * @return boolean True if it may be embedded
     */
    public boolean isEmbeddable() {
        return isEmbeddable;
    }

    /**
     * Indicates whether or not the font is an OpenType
     * CFF font (rather than a TrueType font).
     * @return true if the font is in OpenType CFF format.
     */
    public boolean isCFF() {
       return this.isCFF;
    }

    /**
     * Read Table Directory from the current position in the
     * FontFileReader and fill the global HashMap dirTabs
     * with the table name (String) as key and a TTFDirTabEntry
     * as value.
     * @throws IOException in case of an I/O problem
     */
    protected void readDirTabs() throws IOException {
        int sfntVersion = fontFile.readTTFLong(); // TTF_FIXED_SIZE (4 bytes)
        switch (sfntVersion) {
        case 0x10000:
            log.debug("sfnt version: OpenType 1.0");
            break;
        case 0x4F54544F: //"OTTO"
            this.isCFF = true;
            log.debug("sfnt version: OpenType with CFF data");
            break;
        case 0x74727565: //"true"
            log.debug("sfnt version: Apple TrueType");
            break;
        case 0x74797031: //"typ1"
            log.debug("sfnt version: Apple Type 1 housed in sfnt wrapper");
            break;
        default:
            log.debug("Unknown sfnt version: " + Integer.toHexString(sfntVersion));
            break;
        }
        int ntabs = fontFile.readTTFUShort();
        fontFile.skip(6);    // 3xTTF_USHORT_SIZE

        dirTabs = new HashMap<OFTableName, OFDirTabEntry>();
        OFDirTabEntry[] pd = new OFDirTabEntry[ntabs];
        log.debug("Reading " + ntabs + " dir tables");

        for (int i = 0; i < ntabs; i++) {
            pd[i] = new OFDirTabEntry();
            String tableName = pd[i].read(fontFile);
            dirTabs.put(OFTableName.getValue(tableName), pd[i]);
        }
        dirTabs.put(OFTableName.TABLE_DIRECTORY,
                new OFDirTabEntry(0L, fontFile.getCurrentPos()));
        log.debug("dir tables: " + dirTabs.keySet());
    }

    /**
     * Read the "head" table, this reads the bounding box and
     * sets the upem (unitsPerEM) variable
     * @throws IOException in case of an I/O problem
     */
    protected void readFontHeader() throws IOException {
        seekTab(fontFile, OFTableName.HEAD, 2 * 4 + 2 * 4);
        int flags = fontFile.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug("flags: " + flags + " - " + Integer.toString(flags, 2));
        }
        upem = fontFile.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug("unit per em: " + upem);
        }

        fontFile.skip(16);

        fontBBox1 = fontFile.readTTFShort();
        fontBBox2 = fontFile.readTTFShort();
        fontBBox3 = fontFile.readTTFShort();
        fontBBox4 = fontFile.readTTFShort();
        if (log.isDebugEnabled()) {
            log.debug("font bbox: xMin=" + fontBBox1
                    + " yMin=" + fontBBox2
                    + " xMax=" + fontBBox3
                    + " yMax=" + fontBBox4);
        }

        fontFile.skip(2 + 2 + 2);

        locaFormat = fontFile.readTTFShort();
    }

    /**
     * Read the number of glyphs from the "maxp" table
     * @throws IOException in case of an I/O problem
     */
    protected void getNumGlyphs() throws IOException {
        seekTab(fontFile, OFTableName.MAXP, 4);
        numberOfGlyphs = fontFile.readTTFUShort();
    }


    /**
     * Read the "hhea" table to find the ascender and descender and
     * size of "hmtx" table, as a fixed size font might have only
     * one width.
     * @throws IOException in case of an I/O problem
     */
    protected void readHorizontalHeader()
            throws IOException {
        seekTab(fontFile, OFTableName.HHEA, 4);
        hheaAscender = fontFile.readTTFShort();
        hheaDescender = fontFile.readTTFShort();

        fontFile.skip(2 + 2 + 3 * 2 + 8 * 2);
        nhmtx = fontFile.readTTFUShort();

        if (log.isDebugEnabled()) {
            log.debug("hhea.Ascender: " + formatUnitsForDebug(hheaAscender));
            log.debug("hhea.Descender: " + formatUnitsForDebug(hheaDescender));
            log.debug("Number of horizontal metrics: " + nhmtx);
        }
    }

    /**
     * Read "hmtx" table and put the horizontal metrics
     * in the mtxTab array. If the number of metrics is less
     * than the number of glyphs (eg fixed size fonts), extend
     * the mtxTab array and fill in the missing widths
     * @throws IOException in case of an I/O problem
     */
    protected void readHorizontalMetrics()
            throws IOException {
        seekTab(fontFile, OFTableName.HMTX, 0);

        int mtxSize = Math.max(numberOfGlyphs, nhmtx);
        mtxTab = new OFMtxEntry[mtxSize];

        if (log.isTraceEnabled()) {
            log.trace("*** Widths array: \n");
        }
        for (int i = 0; i < mtxSize; i++) {
            mtxTab[i] = new OFMtxEntry();
        }
        for (int i = 0; i < nhmtx; i++) {
            mtxTab[i].setWx(fontFile.readTTFUShort());
            mtxTab[i].setLsb(fontFile.readTTFUShort());

            if (log.isTraceEnabled()) {
                log.trace("   width[" + i + "] = "
                          + convertTTFUnit2PDFUnit(mtxTab[i].getWx()) + ";");
            }
        }

        if (cid && nhmtx < mtxSize) {
            // Fill in the missing widths
            int lastWidth = mtxTab[nhmtx - 1].getWx();
            for (int i = nhmtx; i < mtxSize; i++) {
                mtxTab[i].setWx(lastWidth);
                mtxTab[i].setLsb(fontFile.readTTFUShort());
            }
        }
    }


    /**
     * Read the "post" table
     * containing the PostScript names of the glyphs.
     */
    protected void readPostScript() throws IOException {
        seekTab(fontFile, OFTableName.POST, 0);
        int postFormat = fontFile.readTTFLong();
        italicAngle = fontFile.readTTFULong();
        underlinePosition = fontFile.readTTFShort();
        underlineThickness = fontFile.readTTFShort();
        isFixedPitch = fontFile.readTTFULong();

        //Skip memory usage values
        fontFile.skip(4 * 4);

        log.debug("PostScript format: 0x" + Integer.toHexString(postFormat));
        switch (postFormat) {
        case 0x00010000:
            log.debug("PostScript format 1");
            postScriptVersion = PostScriptVersion.V1;
            for (int i = 0; i < MAC_GLYPH_ORDERING.length; i++) {
                mtxTab[i].setName(MAC_GLYPH_ORDERING[i]);
            }
            break;
        case 0x00020000:
            log.debug("PostScript format 2");
            postScriptVersion = PostScriptVersion.V2;
            int numGlyphStrings = 257;

            // Read Number of Glyphs
            int l = fontFile.readTTFUShort();

            // Read indexes
            for (int i = 0; i < l; i++) {
                mtxTab[i].setIndex(fontFile.readTTFUShort());

                if (mtxTab[i].getIndex() > numGlyphStrings && mtxTab[i].getIndex() <= 32767) {
                    numGlyphStrings = mtxTab[i].getIndex();
                }

                if (log.isTraceEnabled()) {
                    log.trace("PostScript index: " + mtxTab[i].getIndexAsString());
                }
            }

            // firstChar=minIndex;
            String[] psGlyphsBuffer = new String[numGlyphStrings - 257];
            if (log.isDebugEnabled()) {
                log.debug("Reading " + numGlyphStrings
                        + " glyphnames, that are not in the standard Macintosh"
                        + " set. Total number of glyphs=" + l);
            }
            for (int i = 0; i < psGlyphsBuffer.length; i++) {
                psGlyphsBuffer[i] = fontFile.readTTFString(fontFile.readTTFUByte());
            }

            //Set glyph names
            for (int i = 0; i < l; i++) {
                if (mtxTab[i].getIndex() < MAC_GLYPH_ORDERING.length) {
                    mtxTab[i].setName(MAC_GLYPH_ORDERING[mtxTab[i].getIndex()]);
                } else {
                    if (!mtxTab[i].isIndexReserved()) {
                        int k = mtxTab[i].getIndex() - MAC_GLYPH_ORDERING.length;

                        if (log.isTraceEnabled()) {
                            log.trace(k + " i=" + i + " mtx=" + mtxTab.length
                                + " ps=" + psGlyphsBuffer.length);
                        }

                        mtxTab[i].setName(psGlyphsBuffer[k]);
                    }
                }
            }

            break;
        case 0x00030000:
            // PostScript format 3 contains no glyph names
            log.debug("PostScript format 3");
            postScriptVersion = PostScriptVersion.V3;
            break;
        default:
            log.error("Unknown PostScript format: " + postFormat);
            postScriptVersion = PostScriptVersion.UNKNOWN;
        }
    }


    /**
     * Read the "OS/2" table
     */
    protected void readOS2() throws IOException {
        // Check if font is embeddable
        OFDirTabEntry os2Entry = dirTabs.get(OFTableName.OS2);
        if (os2Entry != null) {
            seekTab(fontFile, OFTableName.OS2, 0);
            int version = fontFile.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug("OS/2 table: version=" + version
                        + ", offset=" + os2Entry.getOffset() + ", len=" + os2Entry.getLength());
            }
            fontFile.skip(2); //xAvgCharWidth
            this.usWeightClass = fontFile.readTTFUShort();

            // usWidthClass
            fontFile.skip(2);

            int fsType = fontFile.readTTFUShort();
            if (fsType == 2) {
                isEmbeddable = false;
            } else {
                isEmbeddable = true;
            }
            fontFile.skip(8 * 2);
            strikeoutThickness = fontFile.readTTFShort();
            strikeoutPosition = fontFile.readTTFShort();
            fontFile.skip(2);
            fontFile.skip(10); //panose array
            fontFile.skip(4 * 4); //unicode ranges
            fontFile.skip(4);
            fontFile.skip(3 * 2);
            int v;
            os2Ascender = fontFile.readTTFShort(); //sTypoAscender
            os2Descender = fontFile.readTTFShort(); //sTypoDescender
            if (log.isDebugEnabled()) {
                log.debug("sTypoAscender: " + os2Ascender
                        + " -> internal " + convertTTFUnit2PDFUnit(os2Ascender));
                log.debug("sTypoDescender: " + os2Descender
                        + " -> internal " + convertTTFUnit2PDFUnit(os2Descender));
            }
            v = fontFile.readTTFShort(); //sTypoLineGap
            if (log.isDebugEnabled()) {
                log.debug("sTypoLineGap: " + v);
            }
            v = fontFile.readTTFUShort(); //usWinAscent
            if (log.isDebugEnabled()) {
                log.debug("usWinAscent: " + formatUnitsForDebug(v));
            }
            v = fontFile.readTTFUShort(); //usWinDescent
            if (log.isDebugEnabled()) {
                log.debug("usWinDescent: " + formatUnitsForDebug(v));
            }

            //version 1 OS/2 table might end here
            if (os2Entry.getLength() >= 78 + (2 * 4) + (2 * 2)) {
                fontFile.skip(2 * 4);
                this.os2xHeight = fontFile.readTTFShort(); //sxHeight
                this.os2CapHeight = fontFile.readTTFShort(); //sCapHeight
                if (log.isDebugEnabled()) {
                    log.debug("sxHeight: " + this.os2xHeight);
                    log.debug("sCapHeight: " + this.os2CapHeight);
                }
            }

        } else {
            isEmbeddable = true;
        }
    }

    /**
     * Read the "PCLT" table to find xHeight and capHeight.
     * @throws IOException In case of a I/O problem
     */
    protected boolean readPCLT() throws IOException {
        OFDirTabEntry dirTab = dirTabs.get(OFTableName.PCLT);
        if (dirTab != null) {
            fontFile.seekSet(dirTab.getOffset() + 4 + 4 + 2);
            xHeight = fontFile.readTTFUShort();
            log.debug("xHeight from PCLT: " + formatUnitsForDebug(xHeight));
            fontFile.skip(2 * 2);
            capHeight = fontFile.readTTFUShort();
            log.debug("capHeight from PCLT: " + formatUnitsForDebug(capHeight));
            fontFile.skip(2 + 16 + 8 + 6 + 1 + 1);

            int serifStyle = fontFile.readTTFUByte();
            serifStyle = serifStyle >> 6;
            serifStyle = serifStyle & 3;
            if (serifStyle == 1) {
                hasSerifs = false;
            } else {
                hasSerifs = true;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines the right source for the ascender and descender values. The problem here is
     * that the interpretation of these values is not the same for every font. There doesn't seem
     * to be a uniform definition of an ascender and a descender. In some fonts
     * the hhea values are defined after the Apple interpretation, but not in every font. The
     * same problem is in the OS/2 table. FOP needs the ascender and descender to determine the
     * baseline so we need values which add up more or less to the "em box". However, due to
     * accent modifiers a character can grow beyond the em box.
     */
    protected void determineAscDesc() {
        int hheaBoxHeight = hheaAscender - hheaDescender;
        int os2BoxHeight = os2Ascender - os2Descender;
        if (os2Ascender > 0 && os2BoxHeight <= upem) {
            ascender = os2Ascender;
            descender = os2Descender;
        } else if (hheaAscender > 0 && hheaBoxHeight <= upem) {
            ascender = hheaAscender;
            descender = hheaDescender;
        } else {
            if (os2Ascender > 0) {
                //Fall back to info from OS/2 if possible
                ascender = os2Ascender;
                descender = os2Descender;
            } else {
                ascender = hheaAscender;
                descender = hheaDescender;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Font box height: " + (ascender - descender));
            if (ascender - descender > upem) {
                log.debug("Ascender and descender together are larger than the em box.");
            }
        }
    }

    protected void guessVerticalMetricsFromGlyphBBox() {
        // Approximate capHeight from height of "H"
        // It's most unlikely that a font misses the PCLT table
        // This also assumes that postscriptnames exists ("H")
        // Should look it up in the cmap (that wouldn't help
        // for charsets without H anyway...)
        // Same for xHeight with the letter "x"
        int localCapHeight = 0;
        int localXHeight = 0;
        int localAscender = 0;
        int localDescender = 0;
        for (OFMtxEntry aMtxTab : mtxTab) {
            if ("H".equals(aMtxTab.getName())) {
                localCapHeight = aMtxTab.getBoundingBox()[3];
            } else if ("x".equals(aMtxTab.getName())) {
                localXHeight = aMtxTab.getBoundingBox()[3];
            } else if ("d".equals(aMtxTab.getName())) {
                localAscender = aMtxTab.getBoundingBox()[3];
            } else if ("p".equals(aMtxTab.getName())) {
                localDescender = aMtxTab.getBoundingBox()[1];
            } else {
                // OpenType Fonts with a version 3.0 "post" table don't have glyph names.
                // Use Unicode indices instead.
                List unicodeIndex = aMtxTab.getUnicodeIndex();
                if (unicodeIndex.size() > 0) {
                    //Only the first index is used
                    char ch = (char) ((Integer) unicodeIndex.get(0)).intValue();
                    if (ch == 'H') {
                        localCapHeight = aMtxTab.getBoundingBox()[3];
                    } else if (ch == 'x') {
                        localXHeight = aMtxTab.getBoundingBox()[3];
                    } else if (ch == 'd') {
                        localAscender = aMtxTab.getBoundingBox()[3];
                    } else if (ch == 'p') {
                        localDescender = aMtxTab.getBoundingBox()[1];
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Ascender from glyph 'd': " + formatUnitsForDebug(localAscender));
            log.debug("Descender from glyph 'p': " + formatUnitsForDebug(localDescender));
        }
        if (ascender - descender > upem) {
            log.debug("Replacing specified ascender/descender with derived values to get values"
                    + " which fit in the em box.");
            ascender = localAscender;
            descender = localDescender;
        }

        if (log.isDebugEnabled()) {
            log.debug("xHeight from glyph 'x': " + formatUnitsForDebug(localXHeight));
            log.debug("CapHeight from glyph 'H': " + formatUnitsForDebug(localCapHeight));
        }
        if (capHeight == 0) {
            capHeight = localCapHeight;
            if (capHeight == 0) {
                capHeight = os2CapHeight;
            }
            if (capHeight == 0) {
                log.debug("capHeight value could not be determined."
                        + " The font may not work as expected.");
            }
        }
        if (xHeight == 0) {
            xHeight = localXHeight;
            if (xHeight == 0) {
                xHeight = os2xHeight;
            }
            if (xHeight == 0) {
                log.debug("xHeight value could not be determined."
                        + " The font may not work as expected.");
            }
        }
    }

    /**
     * Read the kerning table, create a table for both CIDs and
     * winAnsiEncoding.
     * @throws IOException In case of a I/O problem
     */
    protected void readKerning() throws IOException {
        // Read kerning
        kerningTab = new HashMap<Integer, Map<Integer, Integer>>();
        ansiKerningTab = new HashMap<Integer, Map<Integer, Integer>>();
        OFDirTabEntry dirTab = dirTabs.get(OFTableName.KERN);
        if (dirTab != null) {
            seekTab(fontFile, OFTableName.KERN, 2);
            for (int n = fontFile.readTTFUShort(); n > 0; n--) {
                fontFile.skip(2 * 2);
                int k = fontFile.readTTFUShort();
                if (!((k & 1) != 0) || (k & 2) != 0 || (k & 4) != 0) {
                    return;
                }
                if ((k >> 8) != 0) {
                    continue;
                }

                k = fontFile.readTTFUShort();
                fontFile.skip(3 * 2);
                while (k-- > 0) {
                    int i = fontFile.readTTFUShort();
                    int j = fontFile.readTTFUShort();
                    int kpx = fontFile.readTTFShort();
                    if (kpx != 0) {
                        // CID kerning table entry, using unicode indexes
                        final Integer iObj = glyphToUnicode(i);
                        final Integer u2 = glyphToUnicode(j);
                        if (iObj == null) {
                            // happens for many fonts (Ubuntu font set),
                            // stray entries in the kerning table??
                            log.debug("Ignoring kerning pair because no Unicode index was"
                                    + " found for the first glyph " + i);
                        } else if (u2 == null) {
                            log.debug("Ignoring kerning pair because Unicode index was"
                                    + " found for the second glyph " + i);
                        } else {
                            Map<Integer, Integer> adjTab = kerningTab.get(iObj);
                            if (adjTab == null) {
                                adjTab = new HashMap<Integer, Integer>();
                            }
                            adjTab.put(u2, convertTTFUnit2PDFUnit(kpx));
                            kerningTab.put(iObj, adjTab);
                        }
                    }
                }
            }

            // Create winAnsiEncoded kerning table from kerningTab
            // (could probably be simplified, for now we remap back to CID indexes and
            // then to winAnsi)

            for (Map.Entry<Integer, Map<Integer, Integer>> e1 : kerningTab.entrySet()) {
                Integer unicodeKey1 = e1.getKey();
                Integer cidKey1 = unicodeToGlyph(unicodeKey1);
                Map<Integer, Integer> akpx = new HashMap<Integer, Integer>();
                Map<Integer, Integer> ckpx = e1.getValue();

                for (Map.Entry<Integer, Integer> e : ckpx.entrySet()) {
                    Integer unicodeKey2 = e.getKey();
                    Integer cidKey2 = unicodeToGlyph(unicodeKey2);
                    Integer kern = e.getValue();

                    for (Object o : mtxTab[cidKey2].getUnicodeIndex()) {
                        Integer unicodeKey = (Integer) o;
                        Integer[] ansiKeys = unicodeToWinAnsi(unicodeKey);
                        for (Integer ansiKey : ansiKeys) {
                            akpx.put(ansiKey, kern);
                        }
                    }
                }

                if (akpx.size() > 0) {
                    for (Object o : mtxTab[cidKey1].getUnicodeIndex()) {
                        Integer unicodeKey = (Integer) o;
                        Integer[] ansiKeys = unicodeToWinAnsi(unicodeKey);
                        for (Integer ansiKey : ansiKeys) {
                            ansiKerningTab.put(ansiKey, akpx);
                        }
                    }
                }
            }
        }
    }

    /**
     * Streams a font.
     * @param ttfOut The interface for streaming TrueType tables.
     * @exception IOException file write error
     */
    public void stream(TTFOutputStream ttfOut) throws IOException {
        SortedSet<Map.Entry<OFTableName, OFDirTabEntry>> sortedDirTabs = sortDirTabMap(dirTabs);
        byte[] file = fontFile.getAllBytes();
        TTFTableOutputStream tableOut = ttfOut.getTableOutputStream();
        TTFGlyphOutputStream glyphOut = ttfOut.getGlyphOutputStream();
        ttfOut.startFontStream();
        for (Map.Entry<OFTableName, OFDirTabEntry> entry : sortedDirTabs) {
            int offset = (int) entry.getValue().getOffset();
            int paddedLength = (int) entry.getValue().getLength();
            paddedLength += getPadSize(offset + paddedLength);
            if (entry.getKey().equals(OFTableName.GLYF)) {
                streamGlyf(glyphOut, file, offset, paddedLength);
            } else {
                tableOut.streamTable(file, offset, paddedLength);
            }
        }
        ttfOut.endFontStream();
    }

    private void streamGlyf(TTFGlyphOutputStream glyphOut, byte[] fontFile, int tableOffset,
            int tableLength) throws IOException {
        //Stream all but the last glyph
        int glyphStart = 0;
        int glyphEnd = 0;
        glyphOut.startGlyphStream();
        for (int i = 0; i < mtxTab.length - 1; i++) {
            glyphStart = (int) mtxTab[i].getOffset() + tableOffset;
            glyphEnd = (int) mtxTab[i + 1].getOffset() + tableOffset;
            glyphOut.streamGlyph(fontFile, glyphStart, glyphEnd - glyphStart);
        }
        glyphOut.streamGlyph(fontFile, glyphEnd, (tableOffset + tableLength) - glyphEnd);
        glyphOut.endGlyphStream();
    }

    /**
     * Returns the order in which the tables in a TrueType font should be written to file.
     * @param directoryTabs the map that is to be sorted.
     * @return TTFTablesNames[] an array of table names sorted in the order they should appear in
     * the TTF file.
     */
    SortedSet<Map.Entry<OFTableName, OFDirTabEntry>>
                        sortDirTabMap(Map<OFTableName, OFDirTabEntry> directoryTabs) {
        SortedSet<Map.Entry<OFTableName, OFDirTabEntry>> sortedSet
            = new TreeSet<Map.Entry<OFTableName, OFDirTabEntry>>(
                    new Comparator<Map.Entry<OFTableName, OFDirTabEntry>>() {

            public int compare(Entry<OFTableName, OFDirTabEntry> o1,
                    Entry<OFTableName, OFDirTabEntry> o2) {
                return (int) (o1.getValue().getOffset() - o2.getValue().getOffset());
            }
        });
        // @SuppressFBWarnings("DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS")
        sortedSet.addAll(directoryTabs.entrySet());
        return sortedSet;
    }

    /**
     * Returns this font's character to glyph mapping.
     *
     * @return the font's cmap
     */
    public List<CMapSegment> getCMaps() {
        return cmaps;
    }

    /**
     * Check if this is a TrueType collection and that the given
     * name exists in the collection.
     * If it does, set offset in fontfile to the beginning of
     * the Table Directory for that font.
     * @param name The name to check
     * @return True if not collection or font name present, false otherwise
     * @throws IOException In case of an I/O problem
     */
    protected final boolean checkTTC(String tag, String name) throws IOException {
        if ("ttcf".equals(tag)) {
            // This is a TrueType Collection
            fontFile.skip(4);

            // Read directory offsets
            int numDirectories = (int)fontFile.readTTFULong();
            // int numDirectories=in.readTTFUShort();
            long[] dirOffsets = new long[numDirectories];
            for (int i = 0; i < numDirectories; i++) {
                dirOffsets[i] = fontFile.readTTFULong();
            }

            log.info("This is a TrueType collection file with "
                                   + numDirectories + " fonts");
            log.info("Containing the following fonts: ");
            // Read all the directories and name tables to check
            // If the font exists - this is a bit ugly, but...
            boolean found = false;

            // Iterate through all name tables even if font
            // Is found, just to show all the names
            long dirTabOffset = 0;
            for (int i = 0; (i < numDirectories); i++) {
                fontFile.seekSet(dirOffsets[i]);
                readDirTabs();

                readName();

                if (fullName.equals(name)) {
                    found = true;
                    dirTabOffset = dirOffsets[i];
                    log.info(fullName + " <-- selected");
                } else {
                    log.info(fullName);
                }

                // Reset names
                notice = "";
                fullName = "";
                familyNames.clear();
                postScriptName = "";
                subFamilyName = "";
            }

            fontFile.seekSet(dirTabOffset);
            return found;
        } else {
            fontFile.seekSet(0);
            return true;
        }
    }

    /**
     * Return TTC font names
     * @param in FontFileReader to read from
     * @return True if not collection or font name present, false otherwise
     * @throws IOException In case of an I/O problem
     */
    public final List<String> getTTCnames(FontFileReader in) throws IOException {
        this.fontFile = in;

        List<String> fontNames = new ArrayList<String>();
        String tag = in.readTTFString(4);

        if ("ttcf".equals(tag)) {
            // This is a TrueType Collection
            in.skip(4);

            // Read directory offsets
            int numDirectories = (int)in.readTTFULong();
            long[] dirOffsets = new long[numDirectories];
            for (int i = 0; i < numDirectories; i++) {
                dirOffsets[i] = in.readTTFULong();
            }

            log.info("This is a TrueType collection file with "
                      + numDirectories + " fonts");
            log.info("Containing the following fonts: ");

            for (int i = 0; (i < numDirectories); i++) {
                in.seekSet(dirOffsets[i]);
                readDirTabs();

                readName();

                log.info(fullName);
                fontNames.add(fullName);

                // Reset names
                notice = "";
                fullName = "";
                familyNames.clear();
                postScriptName = "";
                subFamilyName = "";
            }

            in.seekSet(0);
            return fontNames;
        } else {
            log.error("Not a TTC!");
            return null;
        }
    }

    /*
     * Helper classes, they are not very efficient, but that really
     * doesn't matter...
     */
    private Integer[] unicodeToWinAnsi(int unicode) {
        List<Integer> ret = new ArrayList<Integer>();
        for (int i = 32; i < Glyphs.WINANSI_ENCODING.length; i++) {
            if (unicode == Glyphs.WINANSI_ENCODING[i]) {
                ret.add(i);
            }
        }
        return ret.toArray(new Integer[ret.size()]);
    }

    /**
     * Dumps a few informational values to System.out.
     */
    public void printStuff() {
        System.out.println("Font name:   " + postScriptName);
        System.out.println("Full name:   " + fullName);
        System.out.println("Family name: " + familyNames);
        System.out.println("Subfamily name: " + subFamilyName);
        System.out.println("Notice:      " + notice);
        System.out.println("xHeight:     " + convertTTFUnit2PDFUnit(xHeight));
        System.out.println("capheight:   " + convertTTFUnit2PDFUnit(capHeight));

        int italic = (int)(italicAngle >> 16);
        System.out.println("Italic:      " + italic);
        System.out.print("ItalicAngle: " + (short)(italicAngle / 0x10000));
        if ((italicAngle % 0x10000) > 0) {
            System.out.print("."
                             + (short)((italicAngle % 0x10000) * 1000)
                               / 0x10000);
        }
        System.out.println();
        System.out.println("Ascender:    " + convertTTFUnit2PDFUnit(ascender));
        System.out.println("Descender:   " + convertTTFUnit2PDFUnit(descender));
        System.out.println("FontBBox:    [" + convertTTFUnit2PDFUnit(fontBBox1)
                           + " " + convertTTFUnit2PDFUnit(fontBBox2) + " "
                           + convertTTFUnit2PDFUnit(fontBBox3) + " "
                           + convertTTFUnit2PDFUnit(fontBBox4) + "]");
    }

    private String formatUnitsForDebug(int units) {
        return units + " -> " + convertTTFUnit2PDFUnit(units) + " internal units";
    }

    /**
     * Map a glyph index to the corresponding unicode code point
     *
     * @param glyphIndex
     * @return unicode code point
     */
    private Integer glyphToUnicode(int glyphIndex) {
        return glyphToUnicodeMap.get(glyphIndex);
    }

    /**
     * Map a unicode code point to the corresponding glyph index
     *
     * @param unicodeIndex unicode code point
     * @return glyph index
     */
    private Integer unicodeToGlyph(int unicodeIndex) throws IOException {
        final Integer result
            = unicodeToGlyphMap.get(unicodeIndex);
        if (result == null) {
            throw new IOException(
                    "Glyph index not found for unicode value " + unicodeIndex);
        }
        return result;
    }

    String getGlyphName(int glyphIndex) {
        return mtxTab[glyphIndex].getName();
    }

    /**
     * Determine if advanced (typographic) table is present.
     * @return true if advanced (typographic) table is present
     */
    public boolean hasAdvancedTable() {
        if (advancedTableReader != null) {
            return  advancedTableReader.hasAdvancedTable();
        } else {
            return false;
        }
    }

    /**
     * Returns the GDEF table or null if none present.
     * @return the GDEF table
     */
    public GlyphDefinitionTable getGDEF() {
        if (advancedTableReader != null) {
            return  advancedTableReader.getGDEF();
        } else {
            return null;
        }
    }

    /**
     * Returns the GSUB table or null if none present.
     * @return the GSUB table
     */
    public GlyphSubstitutionTable getGSUB() {
        if (advancedTableReader != null) {
            return  advancedTableReader.getGSUB();
        } else {
            return null;
        }
    }

    /**
     * Returns the GPOS table or null if none present.
     * @return the GPOS table
     */
    public GlyphPositioningTable getGPOS() {
        if (advancedTableReader != null) {
            return  advancedTableReader.getGPOS();
        } else {
            return null;
        }
    }

    /**
     * Static main method to get info about a TrueType font.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        InputStream stream = null;
        try {
            boolean useKerning = true;
            boolean useAdvanced = true;

            stream = new FileInputStream(args[0]);
            FontFileReader reader = new FontFileReader(stream);

            String name = null;
            if (args.length >= 2) {
                name = args[1];
            }

            String header = OFFontLoader.readHeader(reader);
            boolean isCFF = header.equals("OTTO");
            OpenFont otfFile = (isCFF) ? new OTFFile() : new TTFFile(useKerning, useAdvanced);
            otfFile.readFont(reader, header, name);
            otfFile.printStuff();

        } catch (IOException ioe) {
            System.err.println("Problem reading font: " + ioe.toString());
            ioe.printStackTrace(System.err);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public String getEmbedFontName() {
        return embedFontName;
    }

    public String getCopyrightNotice() {
        return notice;
    }
}
