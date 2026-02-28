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
package org.docx4j.org.apache.poi.poifs.crypt;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@Internal
public abstract class ChunkedCipherInputStream extends LittleEndianInputStream {

    private final int chunkSize;
    private final int chunkBits;

    private final long size;
    private final byte[] chunk, plain;
    private final Cipher cipher;

    private int lastIndex;
    private long pos;
    private boolean chunkIsValid;

    public ChunkedCipherInputStream(InputStream stream, long size, int chunkSize)
    throws GeneralSecurityException {
        this(stream, size, chunkSize, 0);
    }

    public ChunkedCipherInputStream(InputStream stream, long size, int chunkSize, int initialPos)
    throws GeneralSecurityException {
        super(stream);
        this.size = size;
        this.pos = initialPos;
        this.chunkSize = chunkSize;
        int cs = chunkSize == -1 ? 4096 : chunkSize;
        this.chunk = IOUtils.safelyAllocate(cs, CryptoFunctions.MAX_RECORD_LENGTH);
        this.plain = IOUtils.safelyAllocate(cs, CryptoFunctions.MAX_RECORD_LENGTH);
        this.chunkBits = Integer.bitCount(chunk.length-1);
        this.lastIndex = (int)(pos >> chunkBits);
        this.cipher = initCipherForBlock(null, lastIndex);
    }

    public final Cipher initCipherForBlock(int block) throws IOException, GeneralSecurityException {
        if (chunkSize != -1) {
            throw new GeneralSecurityException("the cipher block can only be set for streaming encryption, e.g. CryptoAPI...");
        }

        chunkIsValid = false;
        return initCipherForBlock(cipher, block);
    }

    protected abstract Cipher initCipherForBlock(Cipher existing, int block)
    throws GeneralSecurityException;

    @Override
    public int read() throws IOException {
        byte[] b = { 0 };
        return (read(b) == 1) ? (b[0] & 0xFF) : -1;
    }

    // do not implement! -> recursion
    // public int read(byte[] b) throws IOException;

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return read(b, off, len, false);
    }

    private int read(byte[] b, int off, int len, boolean readPlain) throws IOException {
        int total = 0;

        if (remainingBytes() <= 0) {
            return -1;
        }

        final int chunkMask = getChunkMask();
        while (len > 0) {
            if (!chunkIsValid) {
                try {
                    nextChunk();
                    chunkIsValid = true;
                } catch (GeneralSecurityException e) {
                    throw new EncryptedDocumentException(e.getMessage(), e);
                }
            }
            int count = (int)(chunk.length - (pos & chunkMask));
            int avail = remainingBytes();
            if (avail == 0) {
                return total;
            }
            count = Math.min(avail, Math.min(count, len));

            System.arraycopy(readPlain ? plain : chunk, (int)(pos & chunkMask), b, off, count);

            off += count;
            len -= count;
            pos += count;
            if ((pos & chunkMask) == 0) {
                chunkIsValid = false;
            }
            total += count;
        }

        return total;
    }

    @Override
    public long skip(final long n) {
        long start = pos;
        long skip = Math.min(remainingBytes(), n);

        if ((((pos + skip) ^ start) & ~getChunkMask()) != 0) {
            chunkIsValid = false;
        }
        pos += skip;
        return skip;
    }

    @Override
    public int available() {
        return remainingBytes();
    }

    /**
     * Helper method for forbidden available call - we know the size beforehand, so it's ok ...
     *
     * @return the remaining byte until EOF
     */
    private int remainingBytes() {
        return (int)(size - pos);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void reset() {
        throw new UnsupportedOperationException();
    }

    protected int getChunkMask() {
        return chunk.length-1;
    }

    private void nextChunk() throws GeneralSecurityException, IOException {
        if (chunkSize != -1) {
            int index = (int) (pos >> chunkBits);
            initCipherForBlock(cipher, index);

            if (lastIndex != index) {
                long skipN = ((long) index - lastIndex) << chunkBits;
                if (super.skip(skipN) < skipN) {
                    throw new EOFException("buffer underrun");
                }
            }

            lastIndex = index + 1;
        }

        final int todo = (int)Math.min(size, chunk.length);
        int readBytes, totalBytes = 0;
        do {
            readBytes = super.read(plain, totalBytes, todo-totalBytes);
            totalBytes += Math.max(0, readBytes);
        } while (readBytes != -1 && totalBytes < todo);

        if (readBytes == -1 && pos+totalBytes < size && size < Integer.MAX_VALUE) {
            throw new EOFException("buffer underrun");
        }

        // encrypted data is processed in chunks of 16 bytes,
        // so try to read some more data if the current data is not a
        // multiple of 16 bytes
        if (totalBytes % 16 != 0) {
            int toRead = 16 - totalBytes % 16;
            int read = super.read(plain, totalBytes, toRead);
            if (read > 0) {
                totalBytes += read;
            }
        }

        System.arraycopy(plain, 0, chunk, 0, totalBytes);

        invokeCipher(totalBytes, totalBytes == chunkSize);
    }

    /**
     * Helper function for overriding the cipher invocation, i.e. XOR doesn't use a cipher
     * and uses its own implementation
     */
    protected int invokeCipher(int totalBytes, boolean doFinal) throws GeneralSecurityException {
        if (doFinal) {
            return cipher.doFinal(chunk, 0, totalBytes, chunk);
        } else {
            return cipher.update(chunk, 0, totalBytes, chunk);
        }
    }

    /**
     * Used when BIFF header fields (sid, size) are being read. The internal
     * {@link Cipher} instance must step even when unencrypted bytes are read
     *
     */
    @Override
    public void readPlain(byte[] b, int off, int len) {
        if (len <= 0) {
            return;
        }

        try {
            int readBytes, total = 0;
            do {
                readBytes = read(b, off, len, true);
                total += Math.max(0, readBytes);
            } while (readBytes > -1 && total < len);

            if (total < len) {
                throw new EOFException("buffer underrun");
            }
        } catch (IOException e) {
            // need to wrap checked exception, because of LittleEndianInput interface :(
            throw new IllegalStateException(e);
        }
    }

    /**
     * Some ciphers (actually just XOR) are based on the record size,
     * which needs to be set before decryption
     *
     * @param recordSize the size of the next record
     */
    public void setNextRecordSize(int recordSize) {
    }

    /**
     * @return the chunk bytes
     */
    protected byte[] getChunk() {
        return chunk;
    }

    /**
     * @return the plain bytes
     */
    protected byte[] getPlain() {
        return plain;
    }

    /**
     * @return the absolute position in the stream
     */
    public long getPos() {
        return pos;
    }
}