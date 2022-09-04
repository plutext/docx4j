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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.fontbox.cff.CFFStandardString;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.CharStringCommand;
import org.apache.fontbox.cff.Type2CharString;
import org.docx4j.fonts.fop.fonts.MultiByteFont;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader.CFFIndexData;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader.DICTEntry;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader.FDSelect;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader.FontDict;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader.Format0FDSelect;
import org.docx4j.fonts.fop.fonts.cff.CFFDataReader.Format3FDSelect;
import org.docx4j.fonts.fop.fonts.type1.AdobeStandardEncoding;

/**
 * Reads an OpenType CFF file and generates a subset
 * The OpenType specification can be found at the Microsoft
 * Typography site: http://www.microsoft.com/typography/otspec/
 */
public class OTFSubSetFile extends OTFSubSetWriter {

    /** A map containing each glyph to be included in the subset
      * with their existing and new GID's **/
    protected Map<Integer, Integer> subsetGlyphs = new LinkedHashMap<Integer, Integer>();

    /** A map of the new GID to SID used to construct the charset table **/
    protected Map<Integer, Integer> gidToSID;

    protected CFFIndexData localIndexSubr;
    protected CFFIndexData globalIndexSubr;

    /** List of subroutines to write to the local / global indexes in the subset font **/
    protected List<byte[]> subsetLocalIndexSubr;
    protected List<byte[]> subsetGlobalIndexSubr;

    /** For fonts which have an FDSelect or ROS flag in Top Dict, this is used to store the
     * local subroutine indexes for each group as opposed to the above subsetLocalIndexSubr */
    protected List<List<byte[]>> fdSubrs;

    /** The subset FD Select table used to store the mappings between glyphs and their
     * associated FDFont object which point to a private dict and local subroutines. */
    private Map<Integer, FDIndexReference> subsetFDSelect;

    /** A list of unique subroutines from the global / local subroutine indexes */
    protected List<Integer> localUniques;
    protected List<Integer> globalUniques;

    /** A store of the number of subroutines each global / local subroutine will store **/
    protected int subsetLocalSubrCount;
    protected int subsetGlobalSubrCount;

    /** A list of char string data for each glyph to be stored in the subset font **/
    protected List<byte[]> subsetCharStringsIndex;

    /** The embedded name to change in the name table **/
    protected String embeddedName;

    /** An array used to hold the string index data for the subset font **/
    protected List<byte[]> stringIndexData = new ArrayList<byte[]>();

    /** The CFF reader object used to read data and offsets from the original font file */
    protected CFFDataReader cffReader;

    /** The class used to represent this font **/
    private MultiByteFont mbFont;

    /** The number of standard strings in CFF **/
    public static final int NUM_STANDARD_STRINGS = 391;
    /** The operator used to identify a local subroutine reference */
    private static final int LOCAL_SUBROUTINE = 10;
    /** The operator used to identify a global subroutine reference */
    private static final int GLOBAL_SUBROUTINE = 29;

    private static final String ACCENT_CMD = "seac";

    /** The parser used to parse type2 charstring */
    private Type2Parser type2Parser;

    public OTFSubSetFile() throws IOException {
        super();
    }

    public void readFont(FontFileReader in, String embeddedName, MultiByteFont mbFont) throws IOException {
        readFont(in, embeddedName, mbFont, mbFont.getUsedGlyphs());
    }

    /**
     * Reads and creates a subset of the font.
     *
     * @param in FontFileReader to read from
     * @param embeddedName Name to be checked for in the font file
     * @param usedGlyphs Map of glyphs (glyphs has old index as (Integer) key and
     * new index as (Integer) value)
     * @throws IOException in case of an I/O problem
     */
    void readFont(FontFileReader in, String embeddedName, MultiByteFont mbFont,
            Map<Integer, Integer> usedGlyphs) throws IOException {
        this.mbFont = mbFont;
        fontFile = in;
        this.embeddedName = embeddedName;
        initializeFont(in);
        cffReader = new CFFDataReader(fontFile);
        mapChars(usedGlyphs);
        //Sort by the new GID and store in a LinkedHashMap
        subsetGlyphs = sortByValue(usedGlyphs);
        //Create the CIDFontType0C data
        createCFF();
    }

    private void mapChars(Map<Integer, Integer> usedGlyphs) throws IOException {
        if (fileFont instanceof CFFType1Font) {
            CFFType1Font cffType1Font = (CFFType1Font) fileFont;
            subsetGlyphs = sortByValue(usedGlyphs);
            for (int gid : subsetGlyphs.keySet()) {
                Type2CharString type2CharString = cffType1Font.getType2CharString(gid);
                List<Number> stack = new ArrayList<Number>();
                for (Object obj : type2CharString.getType1Sequence()) {
                    if (obj instanceof CharStringCommand) {
                        String name = CharStringCommand.TYPE1_VOCABULARY.get(((CharStringCommand) obj).getKey());
                        if (ACCENT_CMD.equals(name)) {
                            int first = stack.get(3).intValue();
                            int second = stack.get(4).intValue();
                            mbFont.mapChar(AdobeStandardEncoding.getUnicodeFromCodePoint(first));
                            mbFont.mapChar(AdobeStandardEncoding.getUnicodeFromCodePoint(second));
                        }
                        stack.clear();
                    } else {
                        stack.add((Number) obj);
                    }
                }
            }
        }
    }

    private Map<Integer, Integer> sortByValue(Map<Integer, Integer> map) {
        List<Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
             public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                  return ((Comparable<Integer>) o1.getValue()).compareTo(o2.getValue());
             }
        });

       Map<Integer, Integer> result = new LinkedHashMap<Integer, Integer>();
       for (Entry<Integer, Integer> entry : list) {
           result.put(entry.getKey(), entry.getValue());
       }
       return result;
    }

    protected void createCFF() throws IOException {
        //Header
        writeBytes(cffReader.getHeader());

        //Name Index
        writeIndex(List.of(embedFontName.getBytes("UTF-8")));

        Offsets offsets = new Offsets();

        //Top DICT Index and Data
        offsets.topDictData = currentPos + writeTopDICT();

        boolean hasFDSelect = cffReader.getFDSelect() != null;

        //Create the char string index data and related local / global subroutines
        if (hasFDSelect) {
            createCharStringDataCID();
        } else {
            createCharStringData();
        }

        //If it is a CID-Keyed font, store each FD font and add each SID
        List<Integer> fontNameSIDs = null;
        List<Integer> subsetFDFonts = null;
        if (hasFDSelect) {
            subsetFDFonts = getUsedFDFonts();
            fontNameSIDs = storeFDStrings(subsetFDFonts);
        }

        //String index
        writeStringIndex();

        //Global subroutine index
        writeIndex(subsetGlobalIndexSubr);

        //Encoding
        offsets.encoding = currentPos;

        //Charset table
        offsets.charset = currentPos;
        writeCharsetTable(hasFDSelect);

        //FDSelect table
        offsets.fdSelect = currentPos;
        if (hasFDSelect) {
            writeFDSelect();
            if (!isCharStringBeforeFD()) {
                offsets.fdArray = writeFDArray(subsetFDFonts, fontNameSIDs);
            }
        }

        //Char Strings Index
        offsets.charString = currentPos;
        writeIndex(subsetCharStringsIndex);
        if (hasFDSelect) {
            if (isCharStringBeforeFD()) {
                offsets.fdArray = writeFDArray(subsetFDFonts, fontNameSIDs);
            }
            updateCIDOffsets(offsets);
        } else {
            //Keep offset to modify later with the local subroutine index offset
            offsets.privateDict = currentPos;
            writePrivateDict();

            //Local subroutine index
            offsets.localIndex = currentPos;
            writeIndex(subsetLocalIndexSubr);

            //Update the offsets
            updateOffsets(offsets);
        }
    }

    static class Offsets {
        Integer topDictData;
        Integer encoding;
        Integer charset;
        Integer fdSelect;
        Integer charString;
        Integer fdArray;
        Integer privateDict;
        Integer localIndex;
    }

    private int writeFDArray(List<Integer> subsetFDFonts, List<Integer> fontNameSIDs) throws IOException {
        List<Integer> privateDictOffsets = writeCIDDictsAndSubrs(subsetFDFonts);
        return writeFDArray(subsetFDFonts, privateDictOffsets, fontNameSIDs);
    }

    private boolean isCharStringBeforeFD() {
        LinkedHashMap<String, DICTEntry> entries = cffReader.getTopDictEntries();
        int len = entries.get("CharStrings").getOperandLength();
        if (entries.containsKey("FDArray")) {
            int len2 = entries.get("FDArray").getOperandLength();
            return len < len2;
        }
        return true;
    }

    protected List<Integer> storeFDStrings(List<Integer> uniqueNewRefs) throws IOException {
        List<Integer> fontNameSIDs = new ArrayList<Integer>();
        List<FontDict> fdFonts = cffReader.getFDFonts();
        for (int uniqueNewRef : uniqueNewRefs) {
            FontDict fdFont = fdFonts.get(uniqueNewRef);
            byte[] fdFontByteData = fdFont.getByteData();
            Map<String, DICTEntry> fdFontDict = cffReader.parseDictData(fdFontByteData);
            fontNameSIDs.add(stringIndexData.size() + NUM_STANDARD_STRINGS);
            stringIndexData.add(cffReader.getStringIndex().getValue(fdFontDict.get("FontName")
                    .getOperands().get(0).intValue() - NUM_STANDARD_STRINGS));
        }
        return fontNameSIDs;
    }

    protected int writeTopDICT() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        List<String> topDictStringEntries = Arrays.asList("version", "Notice", "Copyright",
                "FullName", "FamilyName", "Weight", "PostScript");
        ByteArrayOutputStream dict = new ByteArrayOutputStream();
        int offsetExtra = 0;
        for (Map.Entry<String, DICTEntry> dictEntry : topDICT.entrySet()) {
            String dictKey = dictEntry.getKey();
            DICTEntry entry = dictEntry.getValue();
            //If the value is an SID, update the reference but keep the size the same
            entry.setOffset(entry.getOffset() + offsetExtra);
            if (dictKey.equals("CharStrings") && entry.getOperandLength() < 5) {
                byte[] extra = new byte[5 - entry.getOperandLength()];
                offsetExtra += extra.length;
                dict.write(extra);
                dict.write(entry.getByteData());
                entry.setOperandLength(5);
            } else if (dictKey.equals("ROS")) {
                dict.write(writeROSEntry(entry));
            } else if (dictKey.equals("CIDCount")) {
                dict.write(writeCIDCount(entry));
            } else if (topDictStringEntries.contains(dictKey)) {
                if (entry.getOperandLength() < 2) {
                    entry.setOperandLength(2);
                    offsetExtra++;
                }
                dict.write(writeTopDictStringEntry(entry));
            } else {
                dict.write(entry.getByteData());
            }
        }
        byte[] topDictIndex = cffReader.getTopDictIndex().getByteData();
        int offSize = topDictIndex[2];
        return writeIndex(List.of(dict.toByteArray()), offSize) - dict.size();
    }

    private byte[] writeROSEntry(DICTEntry dictEntry) throws IOException {
        int sidA = dictEntry.getOperands().get(0).intValue();
        if (sidA > 390) {
            stringIndexData.add(cffReader.getStringIndex().getValue(sidA - NUM_STANDARD_STRINGS));
        }
        int sidAStringIndex = stringIndexData.size() + 390;
        int sidB = dictEntry.getOperands().get(1).intValue();
        if (sidB > 390) {
            stringIndexData.add("Identity".getBytes("UTF-8"));
        }
        int sidBStringIndex = stringIndexData.size() + 390;
        byte[] cidEntryByteData = dictEntry.getByteData();
        updateOffset(cidEntryByteData, 0, dictEntry.getOperandLengths().get(0),
                sidAStringIndex);
        updateOffset(cidEntryByteData, dictEntry.getOperandLengths().get(0),
                dictEntry.getOperandLengths().get(1), sidBStringIndex);
        updateOffset(cidEntryByteData, dictEntry.getOperandLengths().get(0)
                + dictEntry.getOperandLengths().get(1), dictEntry.getOperandLengths().get(2), 0);
        return cidEntryByteData;
    }

    protected byte[] writeCIDCount(DICTEntry dictEntry) throws IOException {
        byte[] cidCountByteData = dictEntry.getByteData();
        updateOffset(cidCountByteData, 0, dictEntry.getOperandLengths().get(0),
                subsetGlyphs.size());
        return cidCountByteData;
    }

    private byte[] writeTopDictStringEntry(DICTEntry dictEntry) throws IOException {
        int sid = dictEntry.getOperands().get(0).intValue();
        if (sid > 391) {
            stringIndexData.add(cffReader.getStringIndex().getValue(sid - 391));
        }
        return createNewRef(stringIndexData.size() + 390, dictEntry.getOperator(),
                dictEntry.getOperandLength(), true);
    }

    private void writeStringIndex() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        int charsetOffset = topDICT.get("charset").getOperands().get(0).intValue();

        gidToSID = new LinkedHashMap<Integer, Integer>();

        for (Entry<Integer, Integer> subsetGlyph : subsetGlyphs.entrySet()) {
            int gid = subsetGlyph.getKey();
            int v = subsetGlyph.getValue();
            int sid = cffReader.getSIDFromGID(charsetOffset, gid);
            //Check whether the SID falls into the standard string set
            if (sid < NUM_STANDARD_STRINGS) {
                gidToSID.put(v, sid);
                if (mbFont != null) {
                    mbFont.mapUsedGlyphName(v, CFFStandardString.getName(sid));
                }
            } else {
                int index = sid - NUM_STANDARD_STRINGS;
                //index is 0 based, should use < not <=
                if (index < cffReader.getStringIndex().getNumObjects()) {
                    byte[] value = cffReader.getStringIndex().getValue(index);
                    if (mbFont != null) {
                        mbFont.mapUsedGlyphName(v, new String(value, "UTF-8"));
                    }
                    gidToSID.put(v, stringIndexData.size() + 391);
                    stringIndexData.add(value);
                } else {
                    if (mbFont != null) {
                        mbFont.mapUsedGlyphName(v, ".notdef");
                    }
                    gidToSID.put(v, index);
                }
            }
        }
        //Write the String Index
        writeIndex(stringIndexData);
    }

    protected void createCharStringDataCID() throws IOException {
        CFFIndexData charStringsIndex = cffReader.getCharStringIndex();

        FDSelect fontDictionary = cffReader.getFDSelect();
        if (fontDictionary instanceof Format0FDSelect) {
            throw new UnsupportedOperationException("OTF CFF CID Format0 currently not implemented");
        } else if (fontDictionary instanceof Format3FDSelect) {
            Format3FDSelect fdSelect = (Format3FDSelect)fontDictionary;
            Map<Integer, Integer> subsetGroups = new HashMap<Integer, Integer>();

            List<Integer> uniqueGroups = new ArrayList<Integer>();
            Map<Integer, Integer> rangeMap = fdSelect.getRanges();
            Integer[] ranges = rangeMap.keySet().toArray(new Integer[rangeMap.size()]);
            for (int gid : subsetGlyphs.keySet()) {
                int i = 0;
                for (Entry<Integer, Integer> entry : rangeMap.entrySet()) {
                    int nextRange;
                    if (i < ranges.length - 1) {
                        nextRange = ranges[i + 1];
                    } else {
                        nextRange = fdSelect.getSentinelGID();
                    }
                    if (gid >= entry.getKey() && gid < nextRange) {
                        int r = entry.getValue();
                        subsetGroups.put(gid, r);
                        if (!uniqueGroups.contains(r)) {
                            uniqueGroups.add(r);
                        }
                    }
                    i++;
                }
            }

            //Prepare resources
            globalIndexSubr = cffReader.getGlobalIndexSubr();

            //Create the new char string index
            subsetCharStringsIndex = new ArrayList<byte[]>();

            globalUniques = new ArrayList<Integer>();

            subsetFDSelect = new LinkedHashMap<Integer, FDIndexReference>();

            List<List<Integer>> foundLocalUniques = uniqueGroups.stream().mapToInt(u -> u).mapToObj(u -> new ArrayList<Integer>()).collect(Collectors.toList());
            Map<Integer, Integer> gidHintMaskLengths = new HashMap<Integer, Integer>();
            for (Entry<Integer, Integer> subsetGlyph : subsetGlyphs.entrySet()) {
                int gid = subsetGlyph.getKey();
                int group = subsetGroups.get(gid);
                localIndexSubr = cffReader.getFDFonts().get(group).getLocalSubrData();
                localUniques = foundLocalUniques.get(uniqueGroups.indexOf(group));
                type2Parser = new Type2Parser();

                FDIndexReference newFDReference = new FDIndexReference(uniqueGroups.indexOf(group), group);
                subsetFDSelect.put(subsetGlyph.getValue(), newFDReference);
                byte[] data = charStringsIndex.getValue(gid);
                preScanForSubsetIndexSize(data);
                gidHintMaskLengths.put(gid, type2Parser.getMaskLength());
            }

            //Create the two lists which are to store the local and global subroutines
            subsetGlobalIndexSubr = new ArrayList<byte[]>();

            fdSubrs = new ArrayList<List<byte[]>>();
            subsetGlobalSubrCount = globalUniques.size();
            globalUniques.clear();
            localUniques = null;

            for (List<Integer> foundLocalUnique : foundLocalUniques) {
                fdSubrs.add(new ArrayList<byte[]>());
            }
            List<List<Integer>> foundLocalUniquesB = uniqueGroups.stream().mapToInt(u -> u).mapToObj(u -> new ArrayList<Integer>()).collect(Collectors.toList());
            for (Entry<Integer, Integer> subsetGlyph : subsetGlyphs.entrySet()) {
                int gid = subsetGlyph.getKey();
                int value = subsetGlyph.getValue();
                int group = subsetGroups.get(gid);
                localIndexSubr = cffReader.getFDFonts().get(group).getLocalSubrData();
                int newFDIndex = subsetFDSelect.get(value).getNewFDIndex();
                localUniques = foundLocalUniquesB.get(newFDIndex);
                byte[] data = charStringsIndex.getValue(gid);
                subsetLocalIndexSubr = fdSubrs.get(newFDIndex);
                subsetLocalSubrCount = foundLocalUniques.get(newFDIndex).size();
                type2Parser = new Type2Parser();
                type2Parser.setMaskLength(gidHintMaskLengths.get(gid));
                data = readCharStringData(data, subsetLocalSubrCount);
                subsetCharStringsIndex.add(data);
            }
        }
    }

    protected void writeFDSelect() {
        if (cffReader.getTopDictEntries().get("CharStrings").getOperandLength() == 2) {
            Map<Integer, Integer> indexs = getFormat3Index();
            writeByte(3); //Format
            writeCard16(indexs.size());
            int count = 0;
            for (Entry<Integer, Integer> x : indexs.entrySet()) {
                writeCard16(count);
                writeByte(x.getKey());
                count += x.getValue();
            }
            writeCard16(subsetFDSelect.size());
        } else {
            writeByte(0); //Format
            for (FDIndexReference e : subsetFDSelect.values()) {
                writeByte(e.getNewFDIndex());
            }
        }
    }

    private Map<Integer, Integer> getFormat3Index() {
        Map<Integer, Integer> indexs = new LinkedHashMap<Integer, Integer>();
        int last = -1;
        int count = 0;
        for (FDIndexReference e : subsetFDSelect.values()) {
            int i = e.getNewFDIndex();
            count++;
            if (i != last) {
                indexs.put(i, count);
                count = 1;
            }
            last = i;
        }
        indexs.put(last, count);
        return indexs;
    }

    protected List<Integer> getUsedFDFonts() {
        return subsetFDSelect.values().stream().mapToInt(FDIndexReference::getOldFDIndex).distinct().boxed().collect(Collectors.toList());
    }

    protected List<Integer> writeCIDDictsAndSubrs(List<Integer> uniqueNewRefs)
            throws IOException {
        List<Integer> privateDictOffsets = new ArrayList<Integer>();
        List<FontDict> fdFonts = cffReader.getFDFonts();
        int i = 0;
        for (int ref : uniqueNewRefs) {
            FontDict curFDFont = fdFonts.get(ref);
            byte[] fdPrivateDictByteData = curFDFont.getPrivateDictData();
            Map<String, DICTEntry> fdPrivateDict = cffReader.parseDictData(fdPrivateDictByteData);
            int privateDictOffset = currentPos;
            privateDictOffsets.add(privateDictOffset);
            DICTEntry subrs = fdPrivateDict.get("Subrs");
            if (subrs != null) {
                fdPrivateDictByteData = resizeToFitOpLen(fdPrivateDictByteData, subrs);
                updateOffset(fdPrivateDictByteData, subrs.getOffset(),
                        subrs.getOperandLength(),
                        fdPrivateDictByteData.length);
            }
            writeBytes(fdPrivateDictByteData);
            writeIndex(fdSubrs.get(i));
            i++;
        }
        return privateDictOffsets;
    }

    private byte[] resizeToFitOpLen(byte[] fdPrivateDictByteData, DICTEntry subrs) throws IOException {
        if (subrs.getOperandLength() == 2 && fdPrivateDictByteData.length < 108) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(fdPrivateDictByteData);
            bos.write(new byte[108 - fdPrivateDictByteData.length]);
            fdPrivateDictByteData = bos.toByteArray();
        }
        return fdPrivateDictByteData;
    }

    protected int writeFDArray(List<Integer> uniqueNewRefs, List<Integer> privateDictOffsets,
            List<Integer> fontNameSIDs)
            throws IOException {
        int offset = currentPos;
        List<FontDict> fdFonts = cffReader.getFDFonts();
        List<byte[]> index = new ArrayList<byte[]>();

        int i = 0;
        for (int ref : uniqueNewRefs) {
            FontDict fdFont = fdFonts.get(ref);
            byte[] fdFontByteData = fdFont.getByteData();
            Map<String, DICTEntry> fdFontDict = cffReader.parseDictData(fdFontByteData);
            //Update the SID to the FontName
            updateOffset(fdFontByteData, fdFontDict.get("FontName").getOffset() - 1,
                    fdFontDict.get("FontName").getOperandLengths().get(0),
                    fontNameSIDs.get(i));
            //Update the Private dict reference
            updateOffset(fdFontByteData, fdFontDict.get("Private").getOffset()
                    + fdFontDict.get("Private").getOperandLengths().get(0),
                    fdFontDict.get("Private").getOperandLengths().get(1),
                    privateDictOffsets.get(i));
            index.add(fdFontByteData);
            i++;
        }
        writeIndex(index);
        return offset;
    }

    private static class FDIndexReference {
        private int newFDIndex;
        private int oldFDIndex;

        public FDIndexReference(int newFDIndex, int oldFDIndex) {
            this.newFDIndex = newFDIndex;
            this.oldFDIndex = oldFDIndex;
        }

        public int getNewFDIndex() {
            return newFDIndex;
        }

        public int getOldFDIndex() {
            return oldFDIndex;
        }
    }

    private void createCharStringData() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();

        CFFIndexData charStringsIndex = cffReader.getCharStringIndex();

        DICTEntry privateEntry = topDICT.get("Private");
        if (privateEntry != null) {
            int privateOffset = privateEntry.getOperands().get(1).intValue();
            Map<String, DICTEntry> privateDICT = cffReader.getPrivateDict(privateEntry);

            if (privateDICT.get("Subrs") != null) {
                int localSubrOffset = privateOffset + privateDICT.get("Subrs").getOperands().get(0).intValue();
                localIndexSubr = cffReader.readIndex(localSubrOffset);
            } else {
                localIndexSubr = cffReader.readIndex(null);
            }
        }

        globalIndexSubr = cffReader.getGlobalIndexSubr();

        //Create the two lists which are to store the local and global subroutines
        subsetLocalIndexSubr = new ArrayList<byte[]>();
        subsetGlobalIndexSubr = new ArrayList<byte[]>();

        //Create the new char string index
        subsetCharStringsIndex = new ArrayList<byte[]>();

        localUniques = new ArrayList<Integer>();
        globalUniques = new ArrayList<Integer>();
        Map<Integer, Integer> gidHintMaskLengths = new HashMap<Integer, Integer>();
        for (int gid : subsetGlyphs.keySet()) {
            type2Parser = new Type2Parser();
            byte[] data = charStringsIndex.getValue(gid);
            preScanForSubsetIndexSize(data);
            gidHintMaskLengths.put(gid, type2Parser.getMaskLength());
        }

        //Store the size of each subset index and clear the unique arrays
        subsetLocalSubrCount = localUniques.size();
        subsetGlobalSubrCount = globalUniques.size();
        localUniques.clear();
        globalUniques.clear();

        for (int gid : subsetGlyphs.keySet()) {
            byte[] data = charStringsIndex.getValue(gid);
            type2Parser = new Type2Parser();
            //Retrieve modified char string data and fill local / global subroutine arrays
            type2Parser.setMaskLength(gidHintMaskLengths.get(gid));
            data = readCharStringData(data, subsetLocalSubrCount);
            subsetCharStringsIndex.add(data);
        }
    }

    static class Type2Parser {
        /**
         * logging instance
         */
        protected  Logger log = LoggerFactory.getLogger(Type2Parser.class);

        private List<BytesNumber> stack = new ArrayList<BytesNumber>();
        private int hstemCount;
        private int vstemCount;
        private int lastOp = -1;
        private int maskLength = -1;

        public void pushOperand(BytesNumber v) {
            stack.add(v);
        }

        public BytesNumber popOperand() {
            return stack.remove(stack.size() - 1);
        }

        public void clearStack() {
            stack.clear();
        }

        public int[] getOperands(int numbers) {
            int[] ret = new int[numbers];
            while (numbers > 0) {
                numbers--;
                ret[numbers] = this.popOperand().getNumber();
            }
            return ret;
        }

        public void setMaskLength(int maskLength) {
            this.maskLength = maskLength;
        }

        public int getMaskLength() {
            // The number of data bytes for mask is exactly the number needed, one
            // bit per hint, to reference the number of stem hints declared
            // at the beginning of the charstring program.
            if (maskLength > 0) {
                return maskLength;
            }
            return 1 + (hstemCount + vstemCount  - 1) / 8;
        }

        private int exec(int b0, byte[] input, int curPos) throws IOException {
            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            bis.skip(curPos + 1);
            return exec(b0, bis);
        }

        public int exec(int b0, InputStream data) throws IOException {
            int posDelta = 0;
            if ((b0 >= 0 && b0 <= 27) || (b0 >= 29 && b0 <= 31)) {
                if (b0 == 12) {
                    log.warn("May not guess the operand count correctly.");
                    posDelta = 1;
                } else if (b0 == 1 || b0 == 18) {
                    // hstem(hm) operator
                    hstemCount += stack.size() / 2;
                    clearStack();
                } else if (b0 == 19 || b0 == 20) {
                    if (lastOp == 1 || lastOp == 18) {
                        //If hstem and vstem hints are both declared at the beginning of
                        //a charstring, and this sequence is followed directly by the
                        //hintmask or cntrmask operators, the vstem hint operator need
                        //not be included.
                        vstemCount += stack.size() / 2;
                    }
                    clearStack();
                    posDelta = getMaskLength();
                } else if (b0 == 3 || b0 == 23) {
                    // vstem(hm) operator
                    vstemCount += stack.size() / 2;
                    clearStack();
                }
                if (b0 != 11 && b0 != 12) {
                    lastOp = b0;
                }
            } else if (b0 == 28 || (b0 >= 32 && b0 <= 255)) {
                BytesNumber operand = readNumber(b0, data);
                pushOperand(operand);
                posDelta = operand.getNumBytes() - 1;
            } else {
                throw new UnsupportedOperationException("Operator:" + b0 + " is not supported");
            }
            return posDelta;
        }

        private BytesNumber readNumber(int b0, InputStream input) throws IOException {
            if (b0 == 28) {
                int b1 = input.read();
                int b2 = input.read();
                return new BytesNumber((short) (b1 << 8 | b2), 3);
            } else if (b0 >= 32 && b0 <= 246) {
                return new BytesNumber(b0 - 139, 1);
            } else if (b0 >= 247 && b0 <= 250) {
                int b1 = input.read();
                return new BytesNumber(((b0 - 247) << 8) + b1 + 108, 2);
            } else if (b0 >= 251 && b0 <= 254) {
                int b1 = input.read();
                return new BytesNumber((-(b0 - 251) << 8) - b1 - 108, 2);
            } else if (b0 == 255) {
                int b1 = input.read();
                int b2 = input.read();
                int b3 = input.read();
                int b4 = input.read();
                return new BytesNumber((b1 << 24 | b2 << 16 | b3 << 8 | b4), 5);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
    private void preScanForSubsetIndexSize(byte[] data) throws IOException {
        boolean hasLocalSubroutines = localIndexSubr != null && localIndexSubr.getNumObjects() > 0;
        boolean hasGlobalSubroutines = globalIndexSubr != null && globalIndexSubr.getNumObjects() > 0;
        for (int dataPos = 0; dataPos < data.length; dataPos++) {
            int b0 = data[dataPos] & 0xff;
            if (b0 == LOCAL_SUBROUTINE && hasLocalSubroutines) {
                preScanForSubsetIndexSize(localIndexSubr, localUniques);
            } else if (b0 == GLOBAL_SUBROUTINE && hasGlobalSubroutines) {
                preScanForSubsetIndexSize(globalIndexSubr, globalUniques);
            } else  {
                dataPos += type2Parser.exec(b0, data, dataPos);
            }
        }
    }

    private void preScanForSubsetIndexSize(CFFIndexData indexSubr, List<Integer> uniques) throws IOException {
        int subrNumber = getSubrNumber(indexSubr.getNumObjects(), type2Parser.popOperand().getNumber());
        if (!uniques.contains(subrNumber) && subrNumber < indexSubr.getNumObjects()) {
            uniques.add(subrNumber);
        }
        if (subrNumber < indexSubr.getNumObjects()) {
            byte[] subr = indexSubr.getValue(subrNumber);
            preScanForSubsetIndexSize(subr);
        } else {
            throw new IllegalArgumentException("callgsubr out of range");
        }
    }

    private int getSubrNumber(int numSubroutines, int operand) {
        int bias = getBias(numSubroutines);
        return bias + operand;
    }

    private byte[] readCharStringData(byte[] data, int subsetLocalSubrCount) throws IOException {
        boolean hasLocalSubroutines = localIndexSubr != null && localIndexSubr.getNumObjects() > 0;
        boolean hasGlobalSubroutines = globalIndexSubr != null && globalIndexSubr.getNumObjects() > 0;
        for (int dataPos = 0; dataPos < data.length; dataPos++) {
            int b0 = data[dataPos] & 0xff;
            if (b0 == 10 && hasLocalSubroutines) {
                BytesNumber operand = type2Parser.popOperand();
                int subrNumber = getSubrNumber(localIndexSubr.getNumObjects(), operand.getNumber());

                int newRef = getNewRefForReference(subrNumber, localUniques, localIndexSubr, subsetLocalIndexSubr,
                        subsetLocalSubrCount);

                if (newRef != -1) {
                    byte[] newData = constructNewRefData(dataPos, data, operand, subsetLocalSubrCount,
                            newRef, new int[] {10});
                    dataPos -= data.length - newData.length;
                    data = newData;
                }
            } else if (b0 == 29 && hasGlobalSubroutines) {
                BytesNumber operand = type2Parser.popOperand();
                int subrNumber = getSubrNumber(globalIndexSubr.getNumObjects(), operand.getNumber());

                int newRef = getNewRefForReference(subrNumber, globalUniques, globalIndexSubr, subsetGlobalIndexSubr,
                        subsetGlobalSubrCount);

                if (newRef != -1) {
                    byte[] newData = constructNewRefData(dataPos, data, operand, subsetGlobalSubrCount,
                            newRef, new int[] {29});
                    dataPos -= data.length - newData.length;
                    data = newData;
                }
            } else {
                dataPos += type2Parser.exec(b0, data, dataPos);
            }
        }

        //Return the data with the modified references to our arrays
        return data;
    }

    private int getNewRefForReference(int subrNumber, List<Integer> uniquesArray,
            CFFIndexData indexSubr, List<byte[]> subsetIndexSubr, int subrCount) throws IOException {
        int newRef;
        if (!uniquesArray.contains(subrNumber)) {
            if (subrNumber < indexSubr.getNumObjects()) {
                byte[] subr = indexSubr.getValue(subrNumber);
                subr = readCharStringData(subr, subrCount);
                uniquesArray.add(subrNumber);
                subsetIndexSubr.add(subr);
                newRef = subsetIndexSubr.size() - 1;
            } else {
                throw new IllegalArgumentException("subrNumber out of range");
            }
        } else {
            newRef = uniquesArray.indexOf(subrNumber);
        }
        return newRef;
    }

    private int getBias(int subrCount) {
        if (subrCount < 1240) {
            return 107;
        } else if (subrCount < 33900) {
            return 1131;
        } else {
            return 32768;
        }
    }

    private byte[] constructNewRefData(int curDataPos, byte[] currentData, BytesNumber operand,
            int fullSubsetIndexSize, int curSubsetIndexSize, int[] operatorCode) throws IOException {
        //Create the new array with the modified reference
        ByteArrayOutputStream newData = new ByteArrayOutputStream();
        int startRef = curDataPos - operand.getNumBytes();
        int length = operand.getNumBytes() + 1;
        int newBias = getBias(fullSubsetIndexSize);
        int newRef = curSubsetIndexSize - newBias;
        byte[] newRefBytes = createNewRef(newRef, operatorCode, -1, false);
        newData.write(currentData, 0, startRef);
        newData.write(newRefBytes);
        newData.write(currentData, startRef + length, currentData.length - (startRef + length));
        return newData.toByteArray();
    }

    public static byte[] createNewRef(int newRef, int[] operatorCode, int forceLength, boolean isDict) {
        ByteArrayOutputStream newRefBytes = new ByteArrayOutputStream();
        if ((forceLength == -1 && newRef >= -107 && newRef <= 107) || forceLength == 1) {
            //The index values are 0 indexed
            newRefBytes.write(newRef + 139);
        } else if ((forceLength == -1 && newRef >= -1131 && newRef <= 1131) || forceLength == 2) {
            if (newRef <= -876) {
                newRefBytes.write(254);
            } else if (newRef <= -620) {
                newRefBytes.write(253);
            } else if (newRef <= -364) {
                newRefBytes.write(252);
            } else if (newRef <= -108) {
                newRefBytes.write(251);
            } else if (newRef <= 363) {
                newRefBytes.write(247);
            } else if (newRef <= 619) {
                newRefBytes.write(248);
            } else if (newRef <= 875) {
                newRefBytes.write(249);
            } else {
                newRefBytes.write(250);
            }
            if (newRef > 0) {
                newRefBytes.write(newRef - 108);
            } else {
                newRefBytes.write(-newRef - 108);
            }
        } else if ((forceLength == -1 && newRef >= -32768 && newRef <= 32767) || forceLength == 3) {
            newRefBytes.write(28);
            newRefBytes.write(newRef >> 8);
            newRefBytes.write(newRef);
        } else {
            if (isDict) {
                newRefBytes.write(29);
            } else {
                newRefBytes.write(255);
            }
            newRefBytes.write(newRef >> 24);
            newRefBytes.write(newRef >> 16);
            newRefBytes.write(newRef >> 8);
            newRefBytes.write(newRef);
        }
        for (int i : operatorCode) {
            newRefBytes.write(i);
        }
        return newRefBytes.toByteArray();
    }

    protected int writeIndex(List<byte[]> dataArray) {
        int totLength = 1;
        totLength += dataArray.stream().mapToInt(data -> data.length).sum();
        int offSize = getOffSize(totLength);
        return writeIndex(dataArray, offSize);
    }

    protected int writeIndex(List<byte[]> dataArray, int offSize) {
        int hdrTotal = 3;
        //2 byte number of items
        this.writeCard16(dataArray.size());
        //Offset Size: 1 byte = 256, 2 bytes = 65536 etc.
        //Offsets in the offset array are relative to the byte that precedes the object data.
        //Therefore the first element of the offset array is always 1.
        this.writeByte(offSize);
        //Count the first offset 1
        hdrTotal += offSize;
        int total = 0;
        int i = 0;
        for (byte[] data : dataArray) {
            hdrTotal += offSize;
            int length = data.length;
            switch (offSize) {
            case 1:
                if (i == 0) {
                    writeByte(1);
                }
                total += length;
                writeByte(total + 1);
                break;
            case 2:
                if (i == 0) {
                    writeCard16(1);
                }
                total += length;
                writeCard16(total + 1);
                break;
            case 3:
                if (i == 0) {
                    writeThreeByteNumber(1);
                }
                total += length;
                writeThreeByteNumber(total + 1);
                break;
            case 4:
                if (i == 0) {
                    writeULong(1);
                }
                total += length;
                writeULong(total + 1);
                break;
            default:
                throw new AssertionError("Offset Size was not an expected value.");
            }
            i++;
        }
        for (byte[] aDataArray : dataArray) {
            writeBytes(aDataArray);
        }
        return hdrTotal + total;
    }

    private int getOffSize(int totLength) {
        int offSize = 1;
        if (totLength < (1 << 8)) {
            offSize = 1;
        } else if (totLength < (1 << 16)) {
            offSize = 2;
        } else if (totLength < (1 << 24)) {
            offSize = 3;
        } else {
            offSize = 4;
        }
        return offSize;
    }
    /**
     * A class used to store the last number operand and also it's size in bytes
     */
    static class BytesNumber {
        private int number;
        private int numBytes;

        public BytesNumber(int number, int numBytes) {
            this.number = number;
            this.numBytes = numBytes;
        }

        public int getNumber() {
            return this.number;
        }

        public int getNumBytes() {
            return this.numBytes;
        }

        public void clearNumber() {
            this.number = -1;
            this.numBytes = -1;
        }

        public String toString() {
            return Integer.toString(number);
        }

        @Override
        public boolean equals(Object entry) {
            assert entry instanceof BytesNumber;
            BytesNumber bnEntry = (BytesNumber)entry;
            return this.number == bnEntry.getNumber()
                    && this.numBytes == bnEntry.getNumBytes();
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 17 + number;
            hash = hash * 31 + numBytes;
            return hash;
        }
    }

    private void writeCharsetTable(boolean cidFont) throws IOException {
        if (cidFont) {
            writeByte(2);
            for (int entry : gidToSID.keySet()) {
                if (entry == 0) {
                    continue;
                }
                writeCard16(entry);
                writeCard16(gidToSID.size() - 1);
                break;
            }
        } else {
            writeByte(0);
            for (int entry : gidToSID.values()) {
                if (entry == 0) {
                    continue;
                }
                writeCard16(entry);
            }
        }
    }

    protected void writePrivateDict() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();

        DICTEntry privateEntry = topDICT.get("Private");
        if (privateEntry != null) {
            writeBytes(cffReader.getPrivateDictBytes(privateEntry));
        }
    }

    protected void updateOffsets(Offsets offsets) throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        Map<String, DICTEntry> privateDICT = null;

        DICTEntry privateEntry = topDICT.get("Private");
        if (privateEntry != null) {
            privateDICT = cffReader.getPrivateDict(privateEntry);
        }

        updateFixedOffsets(topDICT, offsets);

        if (privateDICT != null) {
            //Private index offset in the top dict
            int oldPrivateOffset = offsets.topDictData + privateEntry.getOffset();
            updateOffset(oldPrivateOffset + privateEntry.getOperandLengths().get(0),
                    privateEntry.getOperandLengths().get(1), offsets.privateDict);

            //Update the local subroutine index offset in the private dict
            DICTEntry subroutines = privateDICT.get("Subrs");
            if (subroutines != null) {
                int oldLocalSubrOffset = offsets.privateDict + subroutines.getOffset();
                updateOffset(oldLocalSubrOffset, subroutines.getOperandLength(),
                        (offsets.localIndex - offsets.privateDict));
            }
        }
    }

    protected void updateFixedOffsets(Map<String, DICTEntry> topDICT, Offsets offsets) throws IOException {
        //Charset offset in the top dict
        DICTEntry charset = topDICT.get("charset");
        int oldCharsetOffset = offsets.topDictData + charset.getOffset();
        updateOffset(oldCharsetOffset, charset.getOperandLength(), offsets.charset);

        //Char string index offset in the private dict
        DICTEntry charString = topDICT.get("CharStrings");
        int oldCharStringOffset = offsets.topDictData + charString.getOffset();
        updateOffset(oldCharStringOffset, charString.getOperandLength(), offsets.charString);

        DICTEntry encodingEntry = topDICT.get("Encoding");
        if (encodingEntry != null && encodingEntry.getOperands().get(0).intValue() != 0
                && encodingEntry.getOperands().get(0).intValue() != 1) {
            int oldEncodingOffset = offsets.topDictData + encodingEntry.getOffset();
            updateOffset(oldEncodingOffset, encodingEntry.getOperandLength(), offsets.encoding);
        }
    }

    protected void updateCIDOffsets(Offsets offsets) throws IOException {
        Map<String, DICTEntry> topDict = cffReader.getTopDictEntries();

        DICTEntry fdArrayEntry = topDict.get("FDArray");
        if (fdArrayEntry != null) {
            updateOffset(offsets.topDictData + fdArrayEntry.getOffset() - 1,
                    fdArrayEntry.getOperandLength(), offsets.fdArray);
        }

        DICTEntry fdSelect = topDict.get("FDSelect");
        if (fdSelect != null) {
            updateOffset(offsets.topDictData + fdSelect.getOffset() - 1,
                    fdSelect.getOperandLength(), offsets.fdSelect);
        }

        updateFixedOffsets(topDict, offsets);
    }

    private void updateOffset(int position, int length, int replacement) throws IOException {
        byte[] outBytes = output.toByteArray();
        updateOffset(outBytes, position, length, replacement);
        output.reset();
        output.write(outBytes);
    }

    private void updateOffset(byte[] out, int position, int length, int replacement) {
        switch (length) {
        case 1:
            out[position] = (byte)(replacement + 139);
            break;
        case 2:
            assert replacement <= 1131;
            if (replacement <= -876) {
                out[position] = (byte)254;
            } else if (replacement <= -620) {
                out[position] = (byte)253;
            } else if (replacement <= -364) {
                out[position] = (byte)252;
            } else if (replacement <= -108) {
                out[position] = (byte)251;
            } else if (replacement <= 363) {
                out[position] = (byte)247;
            } else if (replacement <= 619) {
                out[position] = (byte)248;
            } else if (replacement <= 875) {
                out[position] = (byte)249;
            } else {
                out[position] = (byte)250;
            }
            if (replacement > 0) {
                out[position + 1] = (byte)(replacement - 108);
            } else {
                out[position + 1] = (byte)(-replacement - 108);
            }
            break;
        case 3:
            assert replacement <= 32767;
            out[position] = 28;
            out[position + 1] = (byte)((replacement >> 8) & 0xFF);
            out[position + 2] = (byte)(replacement & 0xFF);
            break;
        case 5:
            out[position] = 29;
            out[position + 1] = (byte)((replacement >> 24) & 0xFF);
            out[position + 2] = (byte)((replacement >> 16) & 0xFF);
            out[position + 3] = (byte)((replacement >> 8) & 0xFF);
            out[position + 4] = (byte)(replacement & 0xFF);
            break;
        default:
        }
    }

    /**
     * Returns the parsed CFF data for the original font.
     * @return The CFFDataReader contaiing the parsed data
     */
    public CFFDataReader getCFFReader() {
        return cffReader;
    }
}
