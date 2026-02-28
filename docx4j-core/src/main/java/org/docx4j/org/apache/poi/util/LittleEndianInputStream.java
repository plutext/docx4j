/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
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

package org.docx4j.org.apache.poi.util;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an {@link InputStream} providing {@link LittleEndianInput}<p>
 *
 * This class does not buffer any input, so the stream read position maintained
 * by this class is consistent with that of the inner stream.
 */
public class LittleEndianInputStream extends FilterInputStream implements LittleEndianInput {

    private static final int BUFFERED_SIZE = 8096;

    private static final int EOF = -1;
    private int readIndex = 0;
    private int markIndex = -1;

    public LittleEndianInputStream(InputStream is) {
        super(is.markSupported() ? is : new BufferedInputStream(is, BUFFERED_SIZE));
    }

    @Override
    @SuppressForbidden("just delegating")
    public int available() {
        try {
            return super.available();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public byte readByte() {
        return (byte)readUByte();
    }

    @Override
    public int readUByte() {
        byte[] buf = new byte[1];
        try {
            checkEOF(read(buf), 1);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return LittleEndian.getUByte(buf);
    }

    /**
     * get a float value, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java float
     *
     * @return the float (32-bit) value
     */
    public float readFloat() {
        return Float.intBitsToFloat( readInt() );
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public int readInt() {
        byte[] buf = new byte[LittleEndianConsts.INT_SIZE];
        try {
            checkEOF(read(buf), buf.length);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return LittleEndian.getInt(buf);
    }

    /**
     * get an unsigned int value from an InputStream
     *
     * @return the unsigned int (32-bit) value
     * @throws IllegalStateException
     *                wraps any IOException thrown from reading the stream.
     */
    //@Override
    public long readUInt() {
       long retNum = readInt();
       return retNum & 0x00FFFFFFFFL;
    }

    @Override
    public long readLong() {
        byte[] buf = new byte[LittleEndianConsts.LONG_SIZE];
        try {
            checkEOF(read(buf), LittleEndianConsts.LONG_SIZE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return LittleEndian.getLong(buf);
    }

    @Override
    public short readShort() {
        return (short)readUShort();
    }

    @Override
    public int readUShort() {
        byte[] buf = new byte[LittleEndianConsts.SHORT_SIZE];
        try {
            checkEOF(read(buf), LittleEndianConsts.SHORT_SIZE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return LittleEndian.getUShort(buf);
    }

    private static void checkEOF(int actualBytes, int expectedBytes) {
        if (expectedBytes != 0 && (actualBytes == -1 || actualBytes != expectedBytes)) {
            throw new IllegalStateException("Unexpected end-of-file");
        }
    }

    @Override
    public void readFully(byte[] buf) {
        readFully(buf, 0, buf.length);
    }

    @Override
    public void readFully(byte[] buf, int off, int len) {
        try {
            checkEOF(_read(buf, off, len), len);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readBytes = super.read(b, off, len);

        // only increase read-index when we did read some bytes
        readIndex += Math.max(0, readBytes);

        return readBytes;
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markIndex = readIndex;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        if (markIndex > -1) {
            readIndex = markIndex;
            markIndex = -1;
        }
    }

    public int getReadIndex() {
        return readIndex;
    }



    //Makes repeated calls to super.read() until length is read or EOF is reached
    private int _read(byte[] buffer, int offset, int length) throws IOException {
        //lifted directly from org.apache.commons.io.IOUtils 2.4
        int remaining = length;
        while (remaining > 0) {
            int location = length - remaining;
            int count = read(buffer, offset + location, remaining);
            if (EOF == count) {
                break;
            }
            remaining -= count;
        }

        return length - remaining;
    }

    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }


    public void skipFully(int len) throws IOException {
        if (len == 0) {
            return;
        }
        long skipped = IOUtils.skipFully(this, len);
        if (skipped > Integer.MAX_VALUE) {
            throw new IOException("can't skip further than "+Integer.MAX_VALUE);
        }
        checkEOF((int)skipped, len);
    }
}
