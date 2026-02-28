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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

/**
 * Wrapper of InputStream which provides Run Length Encoding (RLE)
 *  decompression on the fly. Uses MS-OVBA decompression algorithm. See
 * http://download.microsoft.com/download/2/4/8/24862317-78F0-4C4B-B355-C7B2C1D997DB/[MS-OVBA].pdf
 */
public class RLEDecompressingInputStream extends InputStream {

    /**
     * Bitmasks for performance
     */
    private static final int[] POWER2 = new int[] {
            0x0001, // 2^0
            0x0002, // 2^1
            0x0004, // 2^2
            0x0008, // 2^3
            0x0010, // 2^4
            0x0020, // 2^5
            0x0040, // 2^6
            0x0080, // 2^7
            0x0100, // 2^8
            0x0200, // 2^9
            0x0400, // 2^10
            0x0800, // 2^11
            0x1000, // 2^12
            0x2000, // 2^13
            0x4000, // 2^14
            0x8000  // 2^15
    };

    /** the wrapped inputstream */
    private final InputStream in;

    /** a byte buffer with size 4096 for storing a single chunk */
    private final byte[] buf;

    /** the current position in the byte buffer for reading */
    private int pos;

    /** the number of bytes in the byte buffer */
    private int len;

    /**
     * Creates a new wrapper RLE Decompression InputStream.
     *
     * @param in The stream to wrap with the RLE Decompression
     */
    public RLEDecompressingInputStream(InputStream in) throws IOException {
        this.in = in;
        buf = new byte[4096];
        pos = 0;
        int header = in.read();
        if (header != 0x01) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Header byte 0x01 expected, received 0x%02X", header & 0xFF));
        }
        len = readChunk();
    }

    @Override
    public int read() throws IOException {
        if (len == -1) {
            return -1;
        }
        if (pos >= len) {
            if ((len = readChunk()) == -1) {
                return -1;
            }
        }
        return buf[pos++]& 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int l) throws IOException {
        Objects.requireNonNull(b, "b == null");
        Objects.checkFromIndexSize(off, l, b.length);
        int offset = off;
        int length = l;
        while (length > 0) {
            if (pos >= len) {
                if ((len = readChunk()) == -1) {
                    return offset > off ? offset - off : -1;
                }
            }
            int c = Math.min(length, len - pos);
            System.arraycopy(buf, pos, b, offset, c);
            pos += c;
            length -= c;
            offset += c;
        }
        return l;
    }

    @Override
    public long skip(long n) throws IOException {
        //this relies on readChunk's readFully to skipFully
        long length = n;
        while (length > 0) {
            if (pos >= len) {
                if ((len = readChunk()) == -1) {
                    return -1;
                }
            }
            int c = (int) Math.min(n, len - (long)pos);
            pos += c;
            length -= c;
        }
        return n;
    }

    @Override
    public int available() {
        return (len > 0 ? len - pos : 0);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    /**
     * Reads a single chunk from the underlying inputstream.
     *
     * @return number of bytes that were read, or -1 if the end of the stream was reached.
     */
    private int readChunk() throws IOException {
        pos = 0;
        int w = readShort(in);
        if (w == -1 || w == 0) {
            return -1;
        }
        int chunkSize = (w & 0x0FFF) + 1; // plus 3 bytes minus 2 for the length
        if ((w & 0x7000) != 0x3000) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Chunksize header A should be 0x3000, received 0x%04X", w & 0xE000));
        }
        boolean rawChunk = (w & 0x8000) == 0;
        if (rawChunk) {
            if (IOUtils.readFully(in, buf, 0, chunkSize) < chunkSize) {
                throw new IllegalStateException(String.format(Locale.ROOT, "Not enough bytes read, expected %d", chunkSize));
            }
            return chunkSize;
        } else {
            int inOffset = 0;
            int outOffset = 0;
            while (inOffset < chunkSize) {
                int tokenFlags = in.read();
                inOffset++;
                if (tokenFlags == -1) {
                    break;
                }
                for (int n = 0; n < 8; n++) {
                    if (inOffset >= chunkSize) {
                        break;
                    }
                    if ((tokenFlags & POWER2[n]) == 0) {
                        // literal
                        final int b = in.read();
                        if (b == -1) {
                            return -1;
                        }
                        buf[outOffset++] = (byte) b;
                        inOffset++;
                    } else {
                        // compressed token
                        int token = readShort(in);
                        if (token == -1) {
                            return -1;
                        }
                        inOffset += 2;
                        int copyLenBits = getCopyLenBits(outOffset - 1);
                        int copyOffset = (token >> (copyLenBits)) + 1;
                        int copyLen = (token & (POWER2[copyLenBits] - 1)) + 3;
                        int startPos = outOffset - copyOffset;
                        int endPos = startPos + copyLen;
                        for (int i = startPos; i < endPos; i++) {
                            buf[outOffset++] = buf[i];
                        }
                    }
                }
            }
            return outOffset;
        }
    }

    /**
     * Helper method to determine how many bits in the CopyToken are used for the CopyLength.
     *
     * @return returns the number of bits in the copy token (a value between 4 and 12)
     */
    static int getCopyLenBits(int offset) {
        for (int n = 11; n >= 4; n--) {
            if ((offset & POWER2[n]) != 0) {
                return 15 - n;
            }
        }
        return 12;
    }

    /**
     * Convenience method for read a 2-bytes short in little endian encoding.
     *
     * @return short value from the stream, -1 if end of stream is reached
     */
    public int readShort() throws IOException {
        return readShort(this);
    }

    /**
     * Convenience method for read a 4-bytes int in little endian encoding.
     *
     * @return integer value from the stream, -1 if end of stream is reached
     */
    public int readInt() throws IOException {
        return readInt(this);
    }

    private int readShort(InputStream stream) throws IOException {
        int b0, b1;
        if ((b0 = stream.read()) == -1) {
            return -1;
        }
        if ((b1 = stream.read()) == -1) {
            return -1;
        }
        return (b0 & 0xFF) | ((b1 & 0xFF) << 8);
    }

    private int readInt(InputStream stream) throws IOException {
        int b0, b1, b2, b3;
        if ((b0 = stream.read()) == -1) {
            return -1;
        }
        if ((b1 = stream.read()) == -1) {
            return -1;
        }
        if ((b2 = stream.read()) == -1) {
            return -1;
        }
        if ((b3 = stream.read()) == -1) {
            return -1;
        }
        return (b0 & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 24);
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        return decompress(compressed, 0, compressed.length);
    }

    public static byte[] decompress(byte[] compressed, int offset, int length) throws IOException {
        try (UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
             InputStream instream = UnsynchronizedByteArrayInputStream.builder().setByteArray(compressed).setOffset(offset).setLength(length).get();
             InputStream stream = new RLEDecompressingInputStream(instream)) {

            IOUtils.copy(stream, out);
            return out.toByteArray();
        }
    }
}
