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

import java.io.ByteArrayInputStream;

/**
 * Adapts a plain byte array to {@link LittleEndianInput}
 */
public class LittleEndianByteArrayInputStream extends ByteArrayInputStream implements LittleEndianInput {
    /**
     * Creates <code>LittleEndianByteArrayInputStream</code>
     * that uses <code>buf</code> as its
     * buffer array. The initial value of <code>pos</code>
     * is <code>offset</code> and the initial value
     * of <code>count</code> is the minimum of <code>offset+length</code>
     * and <code>buf.length</code>.
     * The buffer array is not copied. The buffer's mark is
     * set to the specified offset.
     *
     * @param   buf      the input buffer.
     * @param   offset   the offset in the buffer of the first byte to read.
     * @param   length   the maximum number of bytes to read from the buffer.
     */
    public LittleEndianByteArrayInputStream(byte[] buf, int offset, int length) { // NOSONAR
        super(buf, offset, length);
    }

    /**
     * Creates <code>LittleEndianByteArrayInputStream</code>
     * that uses <code>buf</code> as its
     * buffer array. The initial value of <code>pos</code>
     * is <code>offset</code> and the initial value
     * of <code>count</code> is the minimum of <code>offset+buf.length</code>
     * and <code>buf.length</code>.
     * The buffer array is not copied. The buffer's mark is
     * set to the specified offset.
     *
     * @param   buf      the input buffer.
     * @param   offset   the offset in the buffer of the first byte to read.
     */
    public LittleEndianByteArrayInputStream(byte[] buf, int offset) {
        this(buf, offset, buf.length - offset);
    }

    /**
     * Creates a <code>LittleEndianByteArrayInputStream</code>
     * so that it uses <code>buf</code> as its
     * buffer array.
     * The buffer array is not copied.
     * The initial value of <code>pos</code>
     * is <code>0</code> and the initial value
     * of <code>count</code> is the length of
     * <code>buf</code>.
     *
     * @param   buf   the input buffer.
     */
    public LittleEndianByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    protected void checkPosition(int i) {
        if (i > count - pos) {
            throw new IllegalStateException("Buffer overrun, having " + count + " bytes in the stream and position is at " + pos +
                    ", but trying to increment position by " + i);
        }
    }

    public int getReadIndex() {
        return pos;
    }

    public void setReadIndex(int pos) {
       if (pos < 0 || pos >= count) {
            throw new IndexOutOfBoundsException("Invalid position: " + pos + " with count " + count);
       }
       this.pos = pos;
    }


    @Override
    public byte readByte() {
        checkPosition(1);
        return (byte)read();
    }

    @Override
    public int readInt() {
        final int size = LittleEndianConsts.INT_SIZE;
        checkPosition(size);
        int le = LittleEndian.getInt(buf, pos);
        long skipped = super.skip(size);
        assert skipped == size : "Buffer overrun";
        return le;
    }

    @Override
    public long readLong() {
        final int size = LittleEndianConsts.LONG_SIZE;
        checkPosition(size);
        long le = LittleEndian.getLong(buf, pos);
        long skipped = super.skip(size);
        assert skipped == size : "Buffer overrun";
        return le;
    }

    @Override
    public short readShort() {
        final int size = LittleEndianConsts.SHORT_SIZE;
        checkPosition(size);
        short le = LittleEndian.getShort(buf, pos);
        long skipped = super.skip(size);
        assert skipped == size : "Buffer overrun";
        return le;
    }

    @Override
    public int readUByte() {
        return readByte() & 0x00FF;
    }

    @Override
    public int readUShort() {
        return readShort() & 0x00FFFF;
    }

    public long readUInt() {
        return readInt() & 0x00FFFFFFFFL;
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public void readFully(byte[] buffer, int off, int len) {
        checkPosition(len);
        read(buffer, off, len);
    }

    @Override
    public void readFully(byte[] buffer) {
        checkPosition(buffer.length);
        read(buffer, 0, buffer.length);
    }

    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }

    /**
     * Change the limit of the ByteArrayInputStream
     * @param size the new limit - is truncated to length of internal buffer
     */
    public void limit(int size) {
        count = Math.min(size, buf.length);
    }
}
