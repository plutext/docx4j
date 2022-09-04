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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * This class represents an entry to a TrueType font's Dir Tab.
 */
public class OFDirTabEntry {

    private byte[] tag = new byte[4];
    private long checksum;
    private long offset;
    private long length;

    public OFDirTabEntry() {
    }

    public OFDirTabEntry(long offset, long length) {
        this.offset = offset;
        this.length = length;
    }

    /**
     * Read Dir Tab.
     * @param in font file reader
     * @return tag name
     * @throws IOException upon I/O exception
     */
    public String read(FontFileReader in) throws IOException {
        tag[0] = in.readTTFByte();
        tag[1] = in.readTTFByte();
        tag[2] = in.readTTFByte();
        tag[3] = in.readTTFByte();

        checksum = in.readTTFLong();
        offset = in.readTTFULong();
        length = in.readTTFULong();

        return getTagString();
    }


    @Override
    public String toString() {
        return "Read dir tab [" + Arrays.toString(tag) + ']'
            + " offset: " + offset
            + " length: " + length
            + " name: " + getTagString();
    }

    /**
     * Returns the checksum.
     * @return int
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * Returns the length.
     * @return long
     */
    public long getLength() {
        return length;
    }

    /**
     * Returns the offset.
     * @return long
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the tag bytes.
     * @return byte[]
     */
    public byte[] getTag() {
        return tag;
    }

    /**
     * Returns the tag bytes.
     * @return byte[]
     */
    public String getTagString() {
        try {
            return new String(tag, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return this.toString(); // Should never happen.
        }
    }

}
