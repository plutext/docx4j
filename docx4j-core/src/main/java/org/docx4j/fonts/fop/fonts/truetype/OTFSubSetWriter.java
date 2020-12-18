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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OTFSubSetWriter extends OTFFile {
    protected int currentPos;
    protected ByteArrayOutputStream output = new ByteArrayOutputStream();

    public OTFSubSetWriter() throws IOException {
        super();
    }

    public static byte[] concatArray(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * Appends a byte to the output array,
     * updates currentPost but not realSize
     */
    protected void writeByte(int b) {
        output.write(b);
        currentPos++;
    }

    /**
     * Appends a USHORT to the output array,
     * updates currentPost but not realSize
     */
    protected void writeCard16(int s) {
        byte b1 = (byte)((s >> 8) & 0xff);
        byte b2 = (byte)(s & 0xff);
        writeByte(b1);
        writeByte(b2);
    }

    protected void writeThreeByteNumber(int s) {
        byte b1 = (byte)((s >> 16) & 0xFF);
        byte b2 = (byte)((s >> 8) & 0xFF);
        byte b3 = (byte)(s & 0xFF);
        writeByte(b1);
        writeByte(b2);
        writeByte(b3);
    }

    /**
     * Appends a ULONG to the output array,
     * at the given position
     */
    protected void writeULong(int s) {
        byte b1 = (byte)((s >> 24) & 0xff);
        byte b2 = (byte)((s >> 16) & 0xff);
        byte b3 = (byte)((s >> 8) & 0xff);
        byte b4 = (byte)(s & 0xff);
        writeByte(b1);
        writeByte(b2);
        writeByte(b3);
        writeByte(b4);
    }


    protected void writeBytes(byte[] out) {
        for (byte anOut : out) {
            writeByte(anOut);
        }
    }

    /**
     * Returns a subset of the fonts (readFont() MUST be called first in order to create the
     * subset).
     * @return byte array
     */
    public byte[] getFontSubset() {
        return output.toByteArray();
    }
}
