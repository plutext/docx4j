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

import java.io.IOException;
import java.util.List;

import org.apache.fontbox.cff.CFFDataInput;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.CFFType1Font;

public class OTFFile extends OpenFont {

    protected CFFFont fileFont;

    public OTFFile() throws IOException {
        this(true, false);
    }

    public OTFFile(boolean useKerning, boolean useAdvanced) throws IOException {
        super(useKerning, useAdvanced);
        checkForFontbox();
    }

    private void checkForFontbox() throws IOException {
        try {
            Class.forName("org.apache.fontbox.cff.CFFFont");
        } catch (ClassNotFoundException ex) {
            throw new IOException("The Fontbox jar was not found in the classpath. This is "
                                   + "required for OTF CFF ssupport.");
        }
    }

    @Override
    protected void updateBBoxAndOffset() throws IOException {
        Object bbox = fileFont.getTopDict().get("FontBBox");
        if (bbox != null) {
            List bboxList = (List) bbox;
            int[] bboxInt = new int[4];
            for (int i = 0; i < bboxInt.length; i++) {
                bboxInt[i] = (Integer) bboxList.get(i);
            }
            for (OFMtxEntry o : mtxTab) {
                o.setBoundingBox(bboxInt);
            }
        }
    }

    private static class Mapping {
        private int sid;
        private String name;
        private byte[] bytes;

        public void setSID(int sid) {
            this.sid = sid;
        }

        public int getSID() {
            return sid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }


    @Override
    protected void initializeFont(FontFileReader in) throws IOException {
        fontFile = in;
        fontFile.seekSet(0);
        CFFParser parser = new CFFParser();
        fileFont = parser.parse(in.getAllBytes()).get(0);
        embedFontName = fileFont.getName();
    }

    protected void readName() throws IOException {
        Object familyName = fileFont.getTopDict().get("FamilyName");
        if (familyName != null && !familyName.equals("")) {
            familyNames.add(familyName.toString());
            fullName = familyName.toString();
        } else {
            fullName = fileFont.getName();
            familyNames.add(fullName);
        }
    }

    /**
     * Reads the CFFData from a given font file
     * @param fontFile The font file being read
     * @return The byte data found in the CFF table
     */
    public static byte[] getCFFData(FontFileReader fontFile) throws IOException {
        byte[] cff = fontFile.getAllBytes();
        CFFDataInput input = new CFFDataInput(fontFile.getAllBytes());
        input.readBytes(4); //OTTO
        short numTables = input.readShort();
        input.readShort(); //searchRange
        input.readShort(); //entrySelector
        input.readShort(); //rangeShift

        for (int q = 0; q < numTables; q++) {
            String tagName = new String(input.readBytes(4));
            readLong(input); //Checksum
            long offset = readLong(input);
            long length = readLong(input);
            if (tagName.equals("CFF ")) {
                cff = new byte[(int)length];
                System.arraycopy(fontFile.getAllBytes(), (int)offset, cff, 0, cff.length);
                break;
            }
        }
        return cff;
    }

    private static long readLong(CFFDataInput input) throws IOException {
        return (input.readCard16() << 16) | input.readCard16();
    }

    public boolean isType1() {
        return fileFont instanceof CFFType1Font;
    }
}
