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

/* $Id: PFMInputStream.java 693462 2008-09-09 13:35:13Z acumiskey $ */

package org.docx4j.fonts.fop.fonts.type1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;

/**
 * This is a helper class for reading PFM files. It defines functions for
 * extracting specific values out of the stream.
 */
public class PFMInputStream extends java.io.FilterInputStream {

    private final DataInputStream datain;

    /**
     * Constructs a PFMInputStream based on an InputStream representing the
     * PFM file.
     *
     * @param     in The stream from which to read the PFM file
     */
    public PFMInputStream(InputStream in) {
        super(in);
        datain = new DataInputStream(in);
    }

    /**
     * Parses a one byte value out of the stream.
     *
     * @return The value extracted
     * @throws IOException In case of an I/O problem
     */
    public short readByte() throws IOException {
        short s = datain.readByte();
        // Now, we've got to trick Java into forgetting the sign
        int s1 = (((s & 0xF0) >>> 4) << 4) + (s & 0x0F);
        return (short)s1;
    }

    /**
     * Parses a two byte value out of the stream.
     *
     * @return The value extracted
     * @throws IOException In case of an I/O problem
     */
    public int readShort() throws IOException {
        int i = datain.readShort();

        // Change byte order
        int high = (i & 0xFF00) >>> 8;
        int low = (i & 0x00FF) << 8;
        return low + high;
    }

    /**
     * Parses a four byte value out of the stream.
     *
     * @return The value extracted
     * @throws IOException In case of an I/O problem
     */
    public long readInt() throws IOException {
        int i = datain.readInt();

        // Change byte order
        int i1 = (i & 0xFF000000) >>> 24;
        int i2 = (i & 0x00FF0000) >>> 8;
        int i3 = (i & 0x0000FF00) << 8;
        int i4 = (i & 0x000000FF) << 24;
        return i1 + i2 + i3 + i4;
    }

    /**
     * Parses a zero-terminated string out of the stream.
     *
     * @return The value extracted
     * @throws IOException In case of an I/O problem
     */
    public String readString() throws IOException {
        InputStreamReader reader = new InputStreamReader(in, "ISO-8859-1");
        StringBuffer buf = new StringBuffer();

        int ch = reader.read();
        while (ch > 0) {
            buf.append((char)ch);
            ch = reader.read();
        }
        if (ch == -1) {
            throw new EOFException("Unexpected end of stream reached");
        }
        return buf.toString();
    }

}
