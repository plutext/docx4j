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

import java.awt.Rectangle;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphPositioningTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.docx4j.fonts.fop.complexscripts.fonts.GlyphTable;
import org.docx4j.fonts.fop.complexscripts.fonts.Positionable;
import org.docx4j.fonts.fop.complexscripts.fonts.Substitutable;
import org.docx4j.fonts.fop.complexscripts.util.CharAssociation;
import org.docx4j.fonts.fop.complexscripts.util.CharNormalize;
import org.docx4j.fonts.fop.complexscripts.util.GlyphSequence;
import org.docx4j.fonts.fop.util.CharUtilities;

/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont implements Substitutable, Positionable {

    /** logging instance */
    private static final Logger log
        = LoggerFactory.getLogger(MultiByteFont.class);

    private String ttcName;
    private String encoding = "Identity-H";

    private int defaultWidth;
    private CIDFontType cidType = CIDFontType.CIDTYPE2;

    protected final CIDSet cidSet;

    /* advanced typographic support */
    private GlyphDefinitionTable gdef;
    private GlyphSubstitutionTable gsub;
    private GlyphPositioningTable gpos;

    /* dynamic private use (character) mappings */
    private int numMapped;
    private int numUnmapped;
    private int nextPrivateUse = 0xE000;
    private int firstPrivate;
    private int lastPrivate;
    private int firstUnmapped;
    private int lastUnmapped;

    /** Contains the character bounding boxes for all characters in the font */
    protected Rectangle[] boundingBoxes;

    private boolean isOTFFile;

    // since for most users the most likely glyphs are in the first cmap segments we store their mapping.
    private static final int NUM_MOST_LIKELY_GLYPHS = 256;
    private int[] mostLikelyGlyphs = new int[NUM_MOST_LIKELY_GLYPHS];

    //A map to store each used glyph from the CID set against the glyph name.
    private LinkedHashMap<Integer, String> usedGlyphNames = new LinkedHashMap<Integer, String>();

    /**
     * Default constructor
     */
    public MultiByteFont(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
        super(resourceResolver);
        setFontType(FontType.TYPE0);
        setEmbeddingMode(embeddingMode);
        if (embeddingMode != EmbeddingMode.FULL) {
            cidSet = new CIDSubset(this);
        } else {
            cidSet = new CIDFull(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getDefaultWidth() {
        return defaultWidth;
    }

    /** {@inheritDoc} */
    @Override
    public String getRegistry() {
        return "Adobe";
    }

    /** {@inheritDoc} */
    @Override
    public String getOrdering() {
        return "UCS";
    }

    /** {@inheritDoc} */
    @Override
    public int getSupplement() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public CIDFontType getCIDType() {
        return cidType;
    }

    public void setIsOTFFile(boolean isOTFFile) {
        this.isOTFFile = isOTFFile;
    }

    public boolean isOTFFile() {
        return this.isOTFFile;
    }

    /**
     * Sets the CIDType.
     * @param cidType The cidType to set
     */
    public void setCIDType(CIDFontType cidType) {
        this.cidType = cidType;
    }

    /** {@inheritDoc} */
    @Override
    public String getEmbedFontName() {
        if (isEmbeddable()) {
            return FontUtil.stripWhiteSpace(super.getFontName());
        } else {
            return super.getFontName();
        }
    }

    /** {@inheritDoc} */
    public boolean isEmbeddable() {
        return !(getEmbedFileURI() == null && getEmbedResourceName() == null);
    }

    public boolean isSubsetEmbedded() {
        if (getEmbeddingMode() == EmbeddingMode.FULL) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CIDSet getCIDSet() {
        return this.cidSet;
    }

    public void mapUsedGlyphName(int gid, String value) {
        usedGlyphNames.put(gid, value);
    }

    public LinkedHashMap<Integer, String> getUsedGlyphNames() {
        return usedGlyphNames;
    }

    /** {@inheritDoc} */
    @Override
    public String getEncodingName() {
        return encoding;
    }

    /** {@inheritDoc} */
    public int getWidth(int i, int size) {
        if (isEmbeddable()) {
            int glyphIndex = cidSet.getOriginalGlyphIndex(i);
            return size * width[glyphIndex];
        } else {
            return size * width[i];
        }
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length);
        return arr;
    }

    public Rectangle getBoundingBox(int glyphIndex, int size) {
        int index = isEmbeddable() ? cidSet.getOriginalGlyphIndex(glyphIndex) : glyphIndex;
        Rectangle bbox = boundingBoxes[index];
        return new Rectangle(bbox.x * size, bbox.y * size, bbox.width * size, bbox.height * size);
    }

    /**
     * Returns the glyph index for a Unicode character. The method returns 0 if there's no
     * such glyph in the character map.
     * @param c the Unicode character index
     * @return the glyph index (or 0 if the glyph is not available)
     */
    // [TBD] - needs optimization, i.e., change from linear search to binary search
    public int findGlyphIndex(int c) {
        int idx = c;
        int retIdx = SingleByteEncoding.NOT_FOUND_CODE_POINT;

        // for most users the most likely glyphs are in the first cmap segments (meaning the one with
        // the lowest unicode start values)
        if (idx < NUM_MOST_LIKELY_GLYPHS && mostLikelyGlyphs[idx] != 0) {
            return mostLikelyGlyphs[idx];
        }
        for (CMapSegment i : cmap) {
            if (retIdx == 0
                    && i.getUnicodeStart() <= idx
                    && i.getUnicodeEnd() >= idx) {
                retIdx = i.getGlyphStartIndex()
                    + idx
                    - i.getUnicodeStart();
                if (idx < NUM_MOST_LIKELY_GLYPHS) {
                    mostLikelyGlyphs[idx] = retIdx;
                }
                if (retIdx != 0) {
                    break;
                }
            }
        }
        return retIdx;
    }

    /**
     * Add a private use mapping {PU,GI} to the existing character map.
     * N.B. Does not insert in order, merely appends to end of existing map.
     */
    protected synchronized void addPrivateUseMapping(int pu, int gi) {
        assert findGlyphIndex(pu) == SingleByteEncoding.NOT_FOUND_CODE_POINT;
        cmap.add(new CMapSegment(pu, pu, gi));
    }

    /**
     * Given a glyph index, create a new private use mapping, augmenting the bfentries
     * table. This is needed to accommodate the presence of an (output) glyph index in a
     * complex script glyph substitution that does not correspond to a character in the
     * font's CMAP.  The creation of such private use mappings is deferred until an
     * attempt is actually made to perform the reverse lookup from the glyph index. This
     * is necessary in order to avoid exhausting the private use space on fonts containing
     * many such non-mapped glyph indices, if these mappings had been created statically
     * at font load time.
     * @param gi glyph index
     * @returns unicode scalar value
     */
    private int createPrivateUseMapping(int gi) {
        while ((nextPrivateUse < 0xF900)
                && (findGlyphIndex(nextPrivateUse) != SingleByteEncoding.NOT_FOUND_CODE_POINT)) {
            nextPrivateUse++;
        }
        if (nextPrivateUse < 0xF900) {
            int pu = nextPrivateUse;
            addPrivateUseMapping(pu, gi);
            if (firstPrivate == 0) {
                firstPrivate = pu;
            }
            lastPrivate = pu;
            numMapped++;
            if (log.isDebugEnabled()) {
                log.debug("Create private use mapping from "
                            + CharUtilities.format(pu)
                            + " to glyph index " + gi
                            + " in font '" + getFullName() + "'");
            }
            return pu;
        } else {
            if (firstUnmapped == 0) {
                firstUnmapped = gi;
            }
            lastUnmapped = gi;
            numUnmapped++;
            log.warn("Exhausted private use area: unable to map "
                       + numUnmapped + " glyphs in glyph index range ["
                       + firstUnmapped + "," + lastUnmapped
                       + "] (inclusive) of font '" + getFullName() + "'");
            return 0;
        }
    }

    /**
     * Returns the Unicode scalar value that corresponds to the glyph index. If more than
     * one correspondence exists, then the first one is returned (ordered by bfentries[]).
     * @param gi glyph index
     * @return unicode scalar value
     */
    // [TBD] - needs optimization, i.e., change from linear search to binary search
    private int findCharacterFromGlyphIndex(int gi, boolean augment) {
        int cc = 0;
        for (CMapSegment segment : cmap) {
            int s = segment.getGlyphStartIndex();
            int e = s + (segment.getUnicodeEnd() - segment.getUnicodeStart());
            if ((gi >= s) && (gi <= e)) {
                cc = segment.getUnicodeStart() + (gi - s);
                break;
            }
        }
        if ((cc == 0) && augment) {
            cc = createPrivateUseMapping(gi);
        }
        return cc;
    }

    private int findCharacterFromGlyphIndex(int gi) {
        return findCharacterFromGlyphIndex(gi, true);
    }

    protected BitSet getGlyphIndices() {
        BitSet bitset = new BitSet();
        bitset.set(0);
        bitset.set(1);
        bitset.set(2);
        for (CMapSegment i : cmap) {
            int start = i.getUnicodeStart();
            int end = i.getUnicodeEnd();
            int glyphIndex = i.getGlyphStartIndex();
            while (start++ < end + 1) {
                bitset.set(glyphIndex++);
            }
        }
        return bitset;
    }

    protected char[] getChars() {
        // the width array is set when the font is built
        char[] chars = new char[width.length];
        for (CMapSegment i : cmap) {
            int start = i.getUnicodeStart();
            int end = i.getUnicodeEnd();
            int glyphIndex = i.getGlyphStartIndex();
            while (start < end + 1) {
                chars[glyphIndex++] = (char) start++;
            }
        }
        return chars;
    }

    /** {@inheritDoc} */
    @Override
    public char mapChar(char c) {
        notifyMapOperation();
        int glyphIndex = findGlyphIndex(c);
        if (glyphIndex == SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            warnMissingGlyph(c);
            if (!isOTFFile) {
                glyphIndex = findGlyphIndex(Typeface.NOT_FOUND);
            }
        }
        if (isEmbeddable()) {
            glyphIndex = cidSet.mapChar(glyphIndex, c);
        }
        if (isCID() && glyphIndex > 256) {
            mapUnencodedChar(c);
        }
        return (char) glyphIndex;
    }

    /** {@inheritDoc} */
    @Override
    public int mapCodePoint(int cp) {
        notifyMapOperation();
        int glyphIndex = findGlyphIndex(cp);
        if (glyphIndex == SingleByteEncoding.NOT_FOUND_CODE_POINT) {

            for (char ch : Character.toChars(cp)) {
                //TODO better handling for non BMP
                warnMissingGlyph(ch);
            }

            if (!isOTFFile) {
                glyphIndex = findGlyphIndex(Typeface.NOT_FOUND);
            }
        }
        if (isEmbeddable()) {
            glyphIndex = cidSet.mapCodePoint(glyphIndex, cp);
        }
        return (char) glyphIndex;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChar(char c) {
        return hasCodePoint(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasCodePoint(int cp) {
        return (findGlyphIndex(cp) != SingleByteEncoding.NOT_FOUND_CODE_POINT);
    }

    /**
     * Sets the defaultWidth.
     * @param defaultWidth The defaultWidth to set
     */
    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    /**
     * Returns the TrueType Collection Name.
     * @return the TrueType Collection Name
     */
    public String getTTCName() {
        return ttcName;
    }

    /**
     * Sets the the TrueType Collection Name.
     * @param ttcName the TrueType Collection Name
     */
    public void setTTCName(String ttcName) {
        this.ttcName = ttcName;
    }

    /**
     * Sets the width array.
     * @param wds array of widths.
     */
    public void setWidthArray(int[] wds) {
        this.width = wds;
    }

    /**
     * Sets the bounding boxes array.
     * @param boundingBoxes array of bounding boxes.
     */
    public void setBBoxArray(Rectangle[] boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    /**
     * Returns a Map of used Glyphs.
     * @return Map Map of used Glyphs
     */
    public Map<Integer, Integer> getUsedGlyphs() {
        return cidSet.getGlyphs();
    }

    /**
     * Returns the character from it's original glyph index in the font
     * @param glyphIndex The original index of the character
     * @return The character
     */
    public char getUnicodeFromGID(int glyphIndex) {
        return cidSet.getUnicodeFromGID(glyphIndex);
    }

    /**
     * Gets the original glyph index in the font from a character.
     * @param ch The character
     * @return The glyph index in the font
     */
    public int getGIDFromChar(char ch) {
        return cidSet.getGIDFromChar(ch);
    }

    /**
     * Establishes the glyph definition table.
     * @param gdef the glyph definition table to be used by this font
     */
    public void setGDEF(GlyphDefinitionTable gdef) {
        if ((this.gdef == null) || (gdef == null)) {
            this.gdef = gdef;
        } else {
            throw new IllegalStateException("font already associated with GDEF table");
        }
    }

    /**
     * Obtain glyph definition table.
     * @return glyph definition table or null if none is associated with font
     */
    public GlyphDefinitionTable getGDEF() {
        return gdef;
    }

    /**
     * Establishes the glyph substitution table.
     * @param gsub the glyph substitution table to be used by this font
     */
    public void setGSUB(GlyphSubstitutionTable gsub) {
        if ((this.gsub == null) || (gsub == null)) {
            this.gsub = gsub;
        } else {
            throw new IllegalStateException("font already associated with GSUB table");
        }
    }

    /**
     * Obtain glyph substitution table.
     * @return glyph substitution table or null if none is associated with font
     */
    public GlyphSubstitutionTable getGSUB() {
        return gsub;
    }

    /**
     * Establishes the glyph positioning table.
     * @param gpos the glyph positioning table to be used by this font
     */
    public void setGPOS(GlyphPositioningTable gpos) {
        if ((this.gpos == null) || (gpos == null)) {
            this.gpos = gpos;
        } else {
            throw new IllegalStateException("font already associated with GPOS table");
        }
    }

    /**
     * Obtain glyph positioning table.
     * @return glyph positioning table or null if none is associated with font
     */
    public GlyphPositioningTable getGPOS() {
        return gpos;
    }

    /** {@inheritDoc} */
    public boolean performsSubstitution() {
        return gsub != null;
    }

    /** {@inheritDoc} */
    public CharSequence performSubstitution(CharSequence charSequence, String script, String language,
                                            List associations, boolean retainControls) {
        if (gsub != null) {
            charSequence = gsub.preProcess(charSequence, script, this, associations);
            GlyphSequence glyphSequence = charSequenceToGlyphSequence(charSequence, associations);
            GlyphSequence glyphSequenceSubstituted = gsub.substitute(glyphSequence, script, language);
            if (associations != null) {
                associations.clear();
                associations.addAll(glyphSequenceSubstituted.getAssociations());
            }
            if (!retainControls) {
                glyphSequenceSubstituted = elideControls(glyphSequenceSubstituted);
            }
            // may not contains all the characters that were in charSequence.
            // see: #createPrivateUseMapping(int gi)
            return mapGlyphsToChars(glyphSequenceSubstituted);
        } else {
            return charSequence;
        }
    }

    public GlyphSequence charSequenceToGlyphSequence(CharSequence charSequence, List associations) {
        CharSequence normalizedCharSequence = normalize(charSequence, associations);
        return mapCharsToGlyphs(normalizedCharSequence, associations);
    }

    /** {@inheritDoc} */
    public CharSequence reorderCombiningMarks(
        CharSequence cs, int[][] gpa, String script, String language, List associations) {
        if (gdef != null) {
            GlyphSequence igs = mapCharsToGlyphs(cs, associations);
            GlyphSequence ogs = gdef.reorderCombiningMarks(igs, getUnscaledWidths(igs), gpa, script, language);
            if (associations != null) {
                associations.clear();
                associations.addAll(ogs.getAssociations());
            }
            CharSequence ocs = mapGlyphsToChars(ogs);
            return ocs;
        } else {
            return cs;
        }
    }

    protected int[] getUnscaledWidths(GlyphSequence gs) {
        int[] widths = new int[gs.getGlyphCount()];
        for (int i = 0, n = widths.length; i < n; ++i) {
            if (i < width.length) {
                widths[i] = width[i];
            }
        }
        return widths;
    }

    /** {@inheritDoc} */
    public boolean performsPositioning() {
        return gpos != null;
    }

    /** {@inheritDoc} */
    public int[][]
        performPositioning(CharSequence cs, String script, String language, int fontSize) {
        if (gpos != null) {
            GlyphSequence gs = mapCharsToGlyphs(cs, null);
            int[][] adjustments = new int [ gs.getGlyphCount() ] [ 4 ];
            if (gpos.position(gs, script, language, fontSize, this.width, adjustments)) {
                return scaleAdjustments(adjustments, fontSize);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public int[][] performPositioning(CharSequence cs, String script, String language) {
        throw new UnsupportedOperationException();
    }


    private int[][] scaleAdjustments(int[][] adjustments, int fontSize) {
        if (adjustments != null) {
            for (int[] gpa : adjustments) {
                for (int k = 0; k < 4; k++) {
                    gpa[k] = (gpa[k] * fontSize) / 1000;
                }
            }
            return adjustments;
        } else {
            return null;
        }
    }

    /**
     * Map sequence CS, comprising a sequence of UTF-16 encoded Unicode Code Points, to
     * an output character sequence GS, comprising a sequence of Glyph Indices. N.B. Unlike
     * mapChar(), this method does not make use of embedded subset encodings.
     * @param cs a CharSequence containing UTF-16 encoded Unicode characters
     * @returns a CharSequence containing glyph indices
     */
    private GlyphSequence mapCharsToGlyphs(CharSequence cs, List associations) {
        IntBuffer cb = IntBuffer.allocate(cs.length());
        IntBuffer gb = IntBuffer.allocate(cs.length());
        int gi;
        int giMissing = findGlyphIndex(Typeface.NOT_FOUND);
        for (int i = 0, n = cs.length(); i < n; i++) {
            int cc = cs.charAt(i);
            if ((cc >= 0xD800) && (cc < 0xDC00)) {
                if ((i + 1) < n) {
                    int sh = cc;
                    int sl = cs.charAt(++i);
                    if ((sl >= 0xDC00) && (sl < 0xE000)) {
                        cc = 0x10000 + ((sh - 0xD800) << 10) + ((sl - 0xDC00) << 0);
                    } else {
                        throw new IllegalArgumentException(
                            "ill-formed UTF-16 sequence, "
                               + "contains isolated high surrogate at index " + i);
                    }
                } else {
                    throw new IllegalArgumentException(
                        "ill-formed UTF-16 sequence, "
                          + "contains isolated high surrogate at end of sequence");
                }
            } else if ((cc >= 0xDC00) && (cc < 0xE000)) {
                throw new IllegalArgumentException(
                    "ill-formed UTF-16 sequence, "
                      + "contains isolated low surrogate at index " + i);
            }
            notifyMapOperation();
            gi = findGlyphIndex(cc);
            if (gi == SingleByteEncoding.NOT_FOUND_CODE_POINT) {
                warnMissingGlyph((char) cc);
                gi = giMissing;
            }
            cb.put(cc);
            gb.put(gi);
        }
        cb.flip();
        gb.flip();
        if ((associations != null) && (associations.size() == cs.length())) {
            associations = new java.util.ArrayList(associations);
        } else {
            associations = null;
        }
        return new GlyphSequence(cb, gb, associations);
    }

    /**
     * Map sequence GS, comprising a sequence of Glyph Indices, to output sequence CS,
     * comprising a sequence of UTF-16 encoded Unicode Code Points.
     * @param gs a GlyphSequence containing glyph indices
     * @returns a CharSequence containing UTF-16 encoded Unicode characters
     */
    private CharSequence mapGlyphsToChars(GlyphSequence gs) {
        int ng = gs.getGlyphCount();
        int ccMissing = Typeface.NOT_FOUND;
        List<Character> chars = new ArrayList<Character>(gs.getUTF16CharacterCount());

        for (int i = 0, n = ng; i < n; i++) {
            int gi = gs.getGlyph(i);
            int cc = findCharacterFromGlyphIndex(gi);
            if ((cc == 0) || (cc > 0x10FFFF)) {
                cc = ccMissing;
                log.warn("Unable to map glyph index " + gi
                         + " to Unicode scalar in font '"
                         + getFullName() + "', substituting missing character '"
                         + (char) cc + "'");
            }
            if (cc > 0x00FFFF) {
                int sh;
                int sl;
                cc -= 0x10000;
                sh = ((cc >> 10) & 0x3FF) + 0xD800;
                sl = ((cc >>  0) & 0x3FF) + 0xDC00;
                chars.add((char) sh);
                chars.add((char) sl);
            } else {
                chars.add((char) cc);
            }
        }

        CharBuffer cb = CharBuffer.allocate(chars.size());

        for (char c : chars) {
            cb.put(c);
        }

        cb.flip();
        return cb;
    }

    private CharSequence normalize(CharSequence cs, List associations) {
        return hasDecomposable(cs) ? decompose(cs, associations) : cs;
    }

    private boolean hasDecomposable(CharSequence cs) {
        for (int i = 0, n = cs.length(); i < n; i++) {
            int cc = cs.charAt(i);
            if (CharNormalize.isDecomposable(cc)) {
                return true;
            }
        }
        return false;
    }

    private CharSequence decompose(CharSequence cs, List associations) {
        StringBuffer sb = new StringBuffer(cs.length());
        int[] daBuffer = new int[CharNormalize.maximumDecompositionLength()];
        for (int i = 0, n = cs.length(); i < n; i++) {
            int cc = cs.charAt(i);
            int[] da = CharNormalize.decompose(cc, daBuffer);
            for (int aDa : da) {
                if (aDa > 0) {
                    sb.append((char) aDa);
                } else {
                    break;
                }
            }
        }
        return sb;
    }

    /**
     * Removes the glyphs associated with elidable control characters.
     * All the characters in an association must be elidable in order
     * to remove the corresponding glyph.
     *
     * @param gs GlyphSequence that may contains the elidable glyphs
     * @return GlyphSequence without the elidable glyphs
     */
    private static GlyphSequence elideControls(GlyphSequence gs) {
        if (hasElidableControl(gs)) {
            int[] ca = gs.getCharacterArray(false);
            IntBuffer ngb = IntBuffer.allocate(gs.getGlyphCount());
            List nal = new java.util.ArrayList(gs.getGlyphCount());
            for (int i = 0, n = gs.getGlyphCount(); i < n; ++i) {
                CharAssociation a = gs.getAssociation(i);
                int s = a.getStart();
                int e = a.getEnd();
                while (s < e) {
                    int ch = ca [ s ];
                    if (!isElidableControl(ch)) {
                        break;
                    } else {
                        ++s;
                    }
                }
                // If there is at least one non-elidable character in the char
                // sequence then the glyph/association is kept.
                if (s != e) {
                    ngb.put(gs.getGlyph(i));
                    nal.add(a);
                }
            }
            ngb.flip();
            return new GlyphSequence(gs.getCharacters(), ngb, nal, gs.getPredications());
        } else {
            return gs;
        }
    }

    private static boolean hasElidableControl(GlyphSequence gs) {
        int[] ca = gs.getCharacterArray(false);
        for (int ch : ca) {
            if (isElidableControl(ch)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isElidableControl(int ch) {
        if (ch < 0x0020) {
            return true;
        } else if ((ch >= 0x80) && (ch < 0x00A0)) {
            return true;
        } else if ((ch >= 0x2000) && (ch <= 0x206F)) {
            if ((ch >= 0x200B) && (ch <= 0x200F)) {
                return true;
            } else if ((ch >= 0x2028) && (ch <= 0x202E)) {
                return true;
            } else if (ch >= 0x2066) {
                return true;
            } else {
                return ch == 0x2060;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean hasFeature(int tableType, String script, String language, String feature) {
        GlyphTable table;
        if (tableType == GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION) {
            table = getGSUB();
        } else if (tableType == GlyphTable.GLYPH_TABLE_TYPE_POSITIONING) {
            table = getGPOS();
        } else if (tableType == GlyphTable.GLYPH_TABLE_TYPE_DEFINITION) {
            table = getGDEF();
        } else {
            table = null;
        }
        return (table != null) && table.hasFeature(script, language, feature);
    }

    public Map<Integer, Integer> getWidthsMap() {
        return null;
    }

    public InputStream getCmapStream() {
        return null;
    }
}

