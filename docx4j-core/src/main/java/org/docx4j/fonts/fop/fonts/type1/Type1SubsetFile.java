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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.fonts.SingleByteFont;
import org.docx4j.fonts.fop.fonts.type1.PostscriptParser.PSDictionary;
import org.docx4j.fonts.fop.fonts.type1.PostscriptParser.PSElement;
import org.docx4j.fonts.fop.fonts.type1.PostscriptParser.PSFixedArray;
import org.docx4j.fonts.fop.fonts.type1.PostscriptParser.PSSubroutine;
import org.docx4j.fonts.fop.fonts.type1.PostscriptParser.PSVariable;

public class Type1SubsetFile {

    protected static final  Logger log = LoggerFactory.getLogger(Type1SubsetFile.class);
    /* The subset list of char strings */
    protected HashMap<String, byte[]> subsetCharStrings;
    /* The list of character names in the subset font */
    protected List<String> charNames;
    /* A list of unique subroutines references */
    protected LinkedHashMap<Integer, byte[]> uniqueSubs;
    private SingleByteFont sbfont;
    /* New line character */
    protected String eol = "\n";
    /* An option to determine whether the subroutines are subset */
    protected boolean subsetSubroutines = true;
    private byte[] fullFont;
    //List of parsed Postscript elements
    protected List<PSElement> headerSection;
    protected List<PSElement> mainSection;
    //Determines whether the current font uses standard encoding
    protected boolean standardEncoding;

    //Type 1 operators
    private static final int OP_SEAC = 6;
    private static final int OP_CALLSUBR = 10;
    private static final int OP_CALLOTHERSUBR = 16;

    public byte[] createSubset(InputStream in, SingleByteFont sbfont) throws IOException {
        fullFont = IOUtils.toByteArray(in);
        byte[] subsetFont = createSubset(sbfont, true);
        //This should never happen but ensure that subset is shorter than original font
        return (subsetFont.length == 0 || subsetFont.length > fullFont.length)
                ? fullFont : subsetFont;
    }

    /**
     * Creates a new subset from the given type 1 font input stream
     * @param sbfont The font object containing information such as the
     * characters from which to create the subset
     * @param subsetSubroutines This option will force the subset to include all
     * subroutines.
     * @return Returns the subset as a byte array
     * @throws IOException
     */
    private byte[] createSubset(SingleByteFont sbfont, boolean subsetSubroutines) throws IOException {
        this.subsetSubroutines = subsetSubroutines;
        InputStream in = new ByteArrayInputStream(fullFont);
        //Initialise resources used for the font creation
        this.sbfont = sbfont;
        PFBParser pfbParser = new PFBParser();
        PFBData pfbData = pfbParser.parsePFB(in);

        PostscriptParser psParser = new PostscriptParser();
        charNames = new ArrayList<String>();

        //Parse the header section of the font
        if (headerSection == null) {
            headerSection = psParser.parse(pfbData.getHeaderSegment());
        }

        //Read the encoding section
        PSElement encoding = getElement("/Encoding", headerSection);
        if (encoding.getFoundUnexpected()) {
            //Fully embed the font as we're unable to interpret postscript on arrays
            return new byte[0];
        }
        List<String> subsetEncodingEntries = readEncoding(encoding);

        //Decode the main section in preparation for parsing
        byte[] decoded = BinaryCoder.decodeBytes(pfbData.getEncryptedSegment(), 55665, 4);

        //Initialise the resources used to hold the subset data
        uniqueSubs = new LinkedHashMap<Integer, byte[]>();
        subsetCharStrings = new HashMap<String, byte[]>();

        //Parse the encoded main font section for elements
        if (mainSection == null) {
            mainSection = psParser.parse(decoded);
        }

        //Process and write the main section
        PSElement charStrings = getElement("/CharStrings", mainSection);
        boolean result = readMainSection(mainSection, decoded, subsetEncodingEntries, charStrings);
        if (!result) {
            /* This check handles the case where a font uses a postscript method to return a
             * subroutine index. As there is currently no java postscript interpreter and writing
             * one would be very difficult it prevents us from handling this eventuality. The way
             * this issue is being handled is to restart the subset process and include all
             * subroutines. */
            uniqueSubs.clear();
            subsetCharStrings.clear();
            charNames.clear();
            return createSubset(sbfont, false);
        }

        //Write header section
        ByteArrayOutputStream boasHeader = writeHeader(pfbData, encoding);

        ByteArrayOutputStream boasMain = writeMainSection(decoded, mainSection, charStrings);
        byte[] mainSectionBytes = boasMain.toByteArray();
        mainSectionBytes = BinaryCoder.encodeBytes(mainSectionBytes, 55665, 4);
        boasMain.reset();
        boasMain.write(mainSectionBytes);

        ByteArrayOutputStream baosTrailer = new ByteArrayOutputStream();
        baosTrailer.write(pfbData.getTrailerSegment(), 0, pfbData.getTrailerSegment().length);

        return stitchFont(boasHeader, boasMain, baosTrailer);
    }

    public byte[] stitchFont(ByteArrayOutputStream boasHeader, ByteArrayOutputStream boasMain,
                             ByteArrayOutputStream boasTrailer) throws IOException {
        int headerLength = boasHeader.size();
        int mainLength = boasMain.size();

        boasMain.write(128);
        boasMain.write(1);
        updateSectionSize(boasTrailer.size()).writeTo(boasMain);
        boasTrailer.write(128);
        boasTrailer.write(3);

        boasTrailer.writeTo(boasMain);

        boasHeader.write(128);
        boasHeader.write(2);
        //You need to encode the main section first before getting it's size!!!
        updateSectionSize(mainLength).writeTo(boasHeader);
        boasMain.writeTo(boasHeader);

        ByteArrayOutputStream fullFont = new ByteArrayOutputStream();
        fullFont.write(128);
        fullFont.write(1);
        updateSectionSize(headerLength).writeTo(fullFont);
        boasHeader.writeTo(fullFont);

        return fullFont.toByteArray();
    }

    private List<String> readEncoding(PSElement encoding) {
        Map<Integer, Integer> usedGlyphs = sbfont.getUsedGlyphs();
        List<Integer> glyphs = new ArrayList<Integer>(usedGlyphs.keySet());
        Collections.sort(glyphs);
        List<String> subsetEncodingEntries = new ArrayList<String>();
        //Handle custom encoding
        if (encoding instanceof PSFixedArray) {
            PSFixedArray encodingArray = (PSFixedArray)encoding;
            for (int glyph : glyphs) {
                /* Search for matching entries in the original font encoding table to add
                 * to the subset. As there may be more than one entry for a character (as
                 * was the case in a font where some glyphs were duplicated), a name search is
                 * performed and all matching entries are added. */
                List<String> matches = searchEntries(encodingArray.getEntries(), glyph);
                /* If no matches are found, create a new entry for the character so
                 * that it can be added even if it's not in the current encoding. */
                if (matches.size() == 0) {
                    matches.clear();
                    if (glyph == 0) {
                        matches.add("dup 0 /.notdef put");
                    } else {
                        matches.add(String.format("dup %d /%s put", glyph,
                                sbfont.getGlyphName(glyph)));
                    }
                }
                for (String match : matches) {
                    subsetEncodingEntries.add(match);
                    addToCharNames(match);
                }
            }
        //Handle fixed encoding
        } else if (encoding instanceof PSVariable) {
            if (((PSVariable) encoding).getValue().equals("StandardEncoding")) {
                standardEncoding = true;
                sbfont.mapUsedGlyphName(0, "/.notdef");
                for (int glyph : glyphs) {
                    //Retrieve the character name and alternates for the given glyph
                    String name = sbfont.getGlyphName(glyph);
                    if (glyph != 0 && name != null && !name.trim().equals("")) {
                        sbfont.mapUsedGlyphName(glyph, "/" + name);
                    }
                }
            } else {
                log.warn("Only Custom or StandardEncoding is supported when creating a Type 1 subset.");
            }
        }
        return subsetEncodingEntries;
    }

    protected List<String> searchEntries(HashMap<Integer, String> encodingEntries, int glyph) {
        List<String> matches = new ArrayList<String>();
        for (Entry<Integer, String> entry : encodingEntries.entrySet()) {
            String tag = getEntryPart(entry.getValue(), 3);
            String name = sbfont.getGlyphName(sbfont.getUsedGlyphs().get(glyph));
            if (name.equals(tag)) {
                matches.add(entry.getValue());
            }
        }
        return matches;
    }

    protected ByteArrayOutputStream writeHeader(PFBData pfbData, PSElement encoding) throws IOException {
        ByteArrayOutputStream boasHeader = new ByteArrayOutputStream();
        boasHeader.write(pfbData.getHeaderSegment(), 0, encoding.getStartPoint() - 1);

        if (!standardEncoding) {
            //Write out the new encoding table for the subset font
            String encodingArray = eol + "/Encoding 256 array" + eol
                    + "0 1 255 {1 index exch /.notdef put } for" + eol;
            byte[] encodingDefinition = encodingArray.getBytes("ASCII");
            boasHeader.write(encodingDefinition, 0, encodingDefinition.length);
            Set<Entry<Integer, String>> entrySet = sbfont.getUsedGlyphNames().entrySet();
            for (Entry<Integer, String> entry : entrySet) {
                String arrayEntry = String.format("dup %d %s put", entry.getKey(),
                        entry.getValue());
                writeString(arrayEntry + eol, boasHeader);
            }
            writeString("readonly def" + eol, boasHeader);
        } else {
            String theEncoding = eol + "/Encoding StandardEncoding def" + eol;
            boasHeader.write(theEncoding.getBytes("ASCII"));
        }
        boasHeader.write(pfbData.getHeaderSegment(), encoding.getEndPoint(),
                pfbData.getHeaderSegment().length - encoding.getEndPoint());

        return boasHeader;
    }

    ByteArrayOutputStream updateSectionSize(int size) throws IOException {
        //Update the size in the header for the previous section
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        byte[] lowOrderSize = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(
                size).array();
        boas.write(lowOrderSize);
        return boas;
    }

    private boolean readMainSection(List<PSElement> mainSection, byte[] decoded,
            List<String> subsetEncodingEntries, PSElement charStrings) {
        subsetEncodingEntries.add(0, "dup 0 /.notdef put");
        /* Reads and parses the charStrings section to subset the charString
         * and it's referenced subroutines found in the main section for each glyph. */
        PSDictionary charStringsDict = (PSDictionary)charStrings;
        for (String tag : sbfont.getUsedGlyphNames().values()) {
            if (!tag.equals("/.notdef")) {
                charNames.add(tag);
            }

            int[] location = charStringsDict.getBinaryEntries().get(tag);
            if (location == null) {
                continue;
            }
            byte[] charStringEntry = getBinaryEntry(location, decoded);

            int skipBytes = 4;
            PSElement element = getElement("lenIV", mainSection);
            if (element != null && element instanceof PSVariable) {
                PSVariable lenIV = (PSVariable)element;
                try {
                    skipBytes = Integer.parseInt(lenIV.getValue());
                } catch (NumberFormatException ex) {
                    log.warn(String.format("Invalid value `%s` for lenIV found in font %s", lenIV.getValue(),
                            sbfont.getEmbedFileURI().toString()));
                }
            }

            charStringEntry = BinaryCoder.decodeBytes(charStringEntry, 4330, skipBytes);
            PSFixedArray subroutines = (PSFixedArray)getElement("/Subrs", mainSection);
            if (subsetSubroutines) {
                /* Recursively scan the charString array for subroutines and if found, copy the
                 * entry to our subset entries and update any references. */
                charStringEntry = createSubsetCharStrings(decoded, charStringEntry, subroutines,
                        subsetEncodingEntries);
            }
            if (charStringEntry.length == 0) {
                return false;
            }
            charStringEntry = BinaryCoder.encodeBytes(charStringEntry, 4330, skipBytes);
            subsetCharStrings.put(tag, charStringEntry);
        }
        return true;
    }

    private byte[] createSubsetCharStrings(byte[] decoded, byte[] data, PSFixedArray subroutines,
            List<String> subsetEncodingEntries) {
        List<BytesNumber> operands = new ArrayList<BytesNumber>();
        for (int i = 0; i < data.length; i++) {
            int cur = data[i] & 0xFF;
            if (cur <= 31) {
                int dataLength = data.length;
                if (cur == OP_CALLSUBR) {
                    //Found subroutine. Read subroutine and recursively scan and update references
                    if (operands.size() == 0) {
                        continue;
                    }
                    if (uniqueSubs.get(operands.get(operands.size() - 1).getNumber()) == null) {
                        uniqueSubs.put(operands.get(operands.size() - 1).getNumber(), new byte[0]);
                        data = addSubroutine(subroutines, operands, decoded, subsetEncodingEntries,
                                data, i, 1, -1, operands.get(
                                        operands.size() - 1).getNumber());
                    } else {
                        data = addSubroutine(subroutines, operands, decoded, subsetEncodingEntries,
                                data, i, 1, getSubrIndex(operands.get(
                                        operands.size() - 1).getNumber()), operands.get(
                                                operands.size() - 1).getNumber());
                    }
                } else if (cur == 12) {
                    int next = data[++i] & 0xFF;
                    if (next == OP_SEAC) {
                        /* This charString references two other glyphs which must also be included
                         * for this character to be displayed properly. */
                        int first = operands.get(operands.size() - 2).getNumber();
                        int second = operands.get(operands.size() - 1).getNumber();
                        String charFirst = AdobeStandardEncoding.getCharFromCodePoint(first);
                        String charSecond = AdobeStandardEncoding.getCharFromCodePoint(second);
                        subsetEncodingEntries.add(String.format("dup %d /%s put",
                                first, charFirst));
                        subsetEncodingEntries.add(String.format("dup %d /%s put",
                                second, charSecond));
                        sbfont.mapUsedGlyphName(first, "/" + charFirst);
                        sbfont.mapUsedGlyphName(second, "/" + charSecond);
                    } else if (next == OP_CALLOTHERSUBR) {
                        /* Search for a specific operator chain which results in a referenced
                         * subroutine being returned from a postscript method. If it's found then
                         * return null so the subset process can be restarted and all subroutines
                         * can be included. */
                        int[] pattern = {12, 17, 10};
                        int count = 0;
                        boolean matchesPattern = true;
                        if (data.length > i + 4) {
                            for (int pos = i + 1; pos < i + 4; pos++) {
                                if (data[pos] != pattern[count++]) {
                                    matchesPattern = false;
                                }
                            }
                        }
                        if (matchesPattern) {
                            return new byte[0];
                        }
                        data = addSubroutine(subroutines, operands, decoded, subsetEncodingEntries,
                                data, i, 2, -1, operands.get(0).getNumber());
                    }
                }
                if (data.length == 0) {
                    return new byte[0];
                }
                i -= dataLength - data.length;
                operands.clear();
            } else if (cur <= 246) {
                operands.add(new BytesNumber(cur - 139, 1));
            } else if (cur <= 250) {
                operands.add(new BytesNumber((cur - 247) * 256 + (data[i + 1] & 0xFF) + 108, 2));
                i++;
            } else if (cur <= 254) {
                operands.add(new BytesNumber(-(cur - 251) * 256 - (data[i + 1] & 0xFF) - 108, 2));
                i++;
            } else if (cur == 255) {
                int b1 = data[i + 1] & 0xFF;
                int b2 = data[i + 2] & 0xFF;
                int b3 = data[i + 3] & 0xFF;
                int b4 = data[i + 4] & 0xFF;
                int value = b1 << 24 | b2 << 16 | b3 << 8 | b4;
                operands.add(new BytesNumber(value, 5));
                i += 4;
            }
        }
        return data;
    }

    private int getSubrIndex(int subID) {
        int count = 0;
        for (Integer key : uniqueSubs.keySet()) {
            if (key == subID) {
                return count;
            }
            count++;
        }
        return -1;
    }

    private byte[] addSubroutine(PSFixedArray subroutines, List<BytesNumber> operands, byte[] decoded,
            List<String> subsetEncodingEntries, byte[] data, int i, int opLength,
            int existingSubrRef, int subrID) {
        if (existingSubrRef == -1) {
            int[] subrData = subroutines.getBinaryEntryByIndex(subrID);
            byte[] subroutine = getBinaryEntry(subrData, decoded);
            subroutine = BinaryCoder.decodeBytes(subroutine, 4330, 4);
            subroutine = createSubsetCharStrings(decoded, subroutine, subroutines,
                    subsetEncodingEntries);
            if (subroutine.length == 0) {
                return new byte[0];
            }
            //Encode data
            subroutine = BinaryCoder.encodeBytes(subroutine, 4330, 4);
            uniqueSubs.put(subrID, subroutine);
        }
        int subRef = (existingSubrRef != -1) ? existingSubrRef : uniqueSubs.size() - 1;
        data = constructNewRefData(i, data, operands, 1, subRef, opLength);
        return data;
    }

    protected ByteArrayOutputStream writeMainSection(byte[] decoded, List<PSElement> mainSection,
                                                     PSElement charStrings) throws IOException {
        ByteArrayOutputStream main = new ByteArrayOutputStream();
        PSElement subrs = getElement("/Subrs", mainSection);

        //Find the ID of the three most commonly subroutines defined in Type 1 fonts
        String rd = findVariable(decoded, mainSection, new String[]
                {"string currentfile exch readstring pop"}, "RD");
        String nd = findVariable(decoded, mainSection, new String[]
                {"def", "noaccess def"}, "noaccess def");
        String np = findVariable(decoded, mainSection, new String[]
                {"put", "noaccess put"}, "noaccess put");

        main.write(decoded, 0, subrs.getStartPoint());
        //Write either the subset or full list of subroutines
        if (subsetSubroutines) {
            writeString(eol + String.format("/Subrs %d array", uniqueSubs.size()), main);
            int count = 0;
            for (Entry<Integer, byte[]> entry : uniqueSubs.entrySet()) {
                writeString(eol + String.format("dup %d %d %s ", count++, entry.getValue().length, rd), main);
                main.write(entry.getValue());
                writeString(" " + np, main);
            }
            writeString(eol + nd, main);
        } else {
            int fullSubrsLength = subrs.getEndPoint() - subrs.getStartPoint();
            main.write(decoded, subrs.getStartPoint(), fullSubrsLength);
        }
        main.write(decoded, subrs.getEndPoint(), charStrings.getStartPoint() - subrs.getEndPoint());
        //Write the subset charString array
        writeString(eol + String.format("/CharStrings %d dict dup begin",
                subsetCharStrings.size()), main);
        for (Entry<String, byte[]> entry : subsetCharStrings.entrySet()) {
            writeString(eol + String.format("%s %d %s ", entry.getKey(),
                    entry.getValue().length, rd),
                    main);
            main.write(entry.getValue());
            writeString(" " + nd, main);
        }
        writeString(eol + "end", main);
        main.write(decoded, charStrings.getEndPoint(), decoded.length - charStrings.getEndPoint());

        return main;
    }

    protected String findVariable(byte[] decoded, List<PSElement> elements, String[] matches,
                                  String fallback) throws UnsupportedEncodingException {
        for (PSElement element : elements) {
            if (element instanceof PSSubroutine) {
                byte[] var = new byte[element.getEndPoint() - element.getStartPoint()];
                System.arraycopy(decoded, element.getStartPoint(), var, 0, element.getEndPoint()
                        - element.getStartPoint());
                String found = readVariableContents(new String(var, "ASCII")).trim();
                for (String match : matches) {
                    if (match.equals(found)) {
                        return element.getOperator().substring(1, element.getOperator().length());
                    }
                }
            }
        }
        return fallback;
    }

    String readVariableContents(String variable) {
        int level = 0;
        String result = "";
        int start = 0;
        int end = 0;
        boolean reading = false;
        List<Integer> results = new ArrayList<Integer>();
        for (int i = 0; i < variable.length(); i++) {
            char curChar = variable.charAt(i);
            boolean sectionEnd = false;
            if (curChar == '{') {
                level++;
                sectionEnd = true;
            } else if (curChar == '}') {
                level--;
                sectionEnd = true;
            } else if (level == 1) {
                if (!reading) {
                    reading = true;
                    start = i;
                }
                end = i;
            }
            if (sectionEnd && reading) {
                results.add(start);
                results.add(end);
                reading = false;
            }
        }
        for (int i = 0; i < results.size(); i += 2) {
            result = result.concat(variable.substring(results.get(i), results.get(i + 1) + 1));
        }
        return result;
    }

    private void addToCharNames(String encodingEntry) {
        int spaceCount = 0;
        int lastSpaceIndex = 0;
        int charIndex = 0;
        String charName = "";
        //Extract the character name from an encoding entry
        for (int i = 0; i < encodingEntry.length(); i++) {
            boolean isSpace = encodingEntry.charAt(i) == ' ';
            if (isSpace) {
                spaceCount++;
                switch (spaceCount - 1) {
                case 1: charIndex = Integer.parseInt(encodingEntry.substring(lastSpaceIndex + 1,
                        i)); break;
                case 2: charName = encodingEntry.substring(lastSpaceIndex + 1, i); break;
                default: break;
                }
            }
            if (isSpace) {
                lastSpaceIndex = i;
            }
        }
        sbfont.mapUsedGlyphName(charIndex, charName);
    }

    protected void writeString(String entry, ByteArrayOutputStream boas)
            throws IOException {
        byte[] byteEntry = entry.getBytes("ASCII");
        boas.write(byteEntry);
    }

    /**
     * A class used to store the last number operand and also it's size in bytes
     */
    public static final class BytesNumber {
        private int number;
        private int numBytes;
        private String name;

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

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    private byte[] constructNewRefData(int curDataPos, byte[] currentData,
            List<BytesNumber> operands, int opNum, int curSubsetIndexSize, int operatorLength) {
        //Create the new array with the modified reference
        byte[] newData;
        int operandsLenth = getOperandsLength(operands);
        int startRef = curDataPos - operandsLenth + getOpPosition(opNum, operands)
                + (1 - operatorLength);
        byte[] preBytes = new byte[startRef];
        System.arraycopy(currentData, 0, preBytes, 0, startRef);
        byte[] newRefBytes = createNewRef(curSubsetIndexSize, -1);
        newData = concatArray(preBytes, newRefBytes);
        byte[] postBytes = new byte[currentData.length - (startRef
                + operands.get(opNum - 1).getNumBytes())];
        System.arraycopy(currentData, startRef + operands.get(opNum - 1).getNumBytes(), postBytes, 0,
                currentData.length - (startRef + operands.get(opNum - 1).getNumBytes()));
        return concatArray(newData, postBytes);
    }

    int getOpPosition(int opNum, List<BytesNumber> operands) {
        int byteCount = 0;
        for (int i = 0; i < opNum - 1; i++) {
            byteCount += operands.get(i).getNumBytes();
        }
        return byteCount;
    }

    int getOperandsLength(List<BytesNumber> operands) {
        int length = 0;
        for (BytesNumber number : operands) {
            length += number.getNumBytes();
        }
        return length;
    }

    private byte[] createNewRef(int newRef, int forceLength) {
        byte[] newRefBytes;
        if ((forceLength == -1 && newRef <= 107) || forceLength == 1) {
            newRefBytes = new byte[1];
            newRefBytes[0] = (byte)(newRef + 139);
        } else if ((forceLength == -1 && newRef <= 1131) || forceLength == 2) {
            newRefBytes = new byte[2];
            if (newRef <= 363) {
                newRefBytes[0] = (byte)247;
            } else if (newRef <= 619) {
                newRefBytes[0] = (byte)248;
            } else if (newRef <= 875) {
                newRefBytes[0] = (byte)249;
            } else {
                newRefBytes[0] = (byte)250;
            }
            newRefBytes[1] = (byte)(newRef - 108);
        } else {
            newRefBytes = new byte[5];
            newRefBytes[0] = (byte)255;
            newRefBytes[1] = (byte)(newRef >> 24);
            newRefBytes[2] = (byte)(newRef >> 16);
            newRefBytes[3] = (byte)(newRef >> 8);
            newRefBytes[4] = (byte)newRef;
        }
        return newRefBytes;
    }

    /**
     * Concatenate two byte arrays together
     * @param a The first array
     * @param b The second array
     * @return The concatenated array
     */
    byte[] concatArray(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * Returns a section of a byte array determined by it's start and
     * end position.
     * @param position An array containing both the start and end position
     * of the section to copy.
     * @param decoded The array from which to copy a section of data
     * @return Returns the copy of the data section
     */
    protected byte[] getBinaryEntry(int[] position, byte[] decoded) {
        int start = position[0];
        int finish = position[1];
        byte[] line = new byte[finish - start];
        System.arraycopy(decoded, start, line, 0, finish - start);
        return line;
    }

    protected String getEntryPart(String entry, int part) {
        Scanner s = new Scanner(entry).useDelimiter(" ");
        for (int i = 1; i < part; i++) {
            s.next();
        }
        return s.next();
    }

    protected PSElement getElement(String elementID, List<PSElement> elements) {
        for (PSElement element : elements) {
            if (element.getOperator().equals(elementID)) {
                return element;
            }
        }
        return null;
    }

    /**
     * A class to encode and decode sections of a type 1 font file. See Adobe
     * Type 1 Font Format Section 7.2 for more details.
     */
    public static class BinaryCoder {
        public static byte[] decodeBytes(byte[] in, int inR, int n) {
            byte[] out = new byte[in.length - n];
            int r = inR;
            int c1 = 52845;
            int c2 = 22719;
            for (int i = 0; i < in.length; i++) {
                int cypher = in[i] & 0xFF;
                int plain = cypher ^ (r >> 8);
                if (i >= n) {
                    out[i - n] = (byte)plain;
                }
                r = (cypher + r) * c1 + c2 & 0xFFFF;
            }
            return out;
        }

        public static byte[] encodeBytes(byte[] in, int inR, int n) {
            byte[] buffer = new byte[in.length + n];
            for (int i = 0; i < n; i++) {
                buffer[i] = 0;
            }
            int r = inR;
            int c1 = 52845;
            int c2 = 22719;
            System.arraycopy(in, 0, buffer, n, buffer.length - n);
            byte[] out = new byte[buffer.length];
            for (int i = 0; i < buffer.length; i++) {
                int plain = buffer[i] & 0xff;
                int cipher = plain ^ r >> 8;
                out[i] = (byte) cipher;
                r = (cipher + r) * c1 + c2 & 0xffff;
            }
            return out;
        }
    }
}
