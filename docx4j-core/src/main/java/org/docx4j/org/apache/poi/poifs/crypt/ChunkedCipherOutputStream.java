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

import static org.docx4j.org.apache.poi.poifs.crypt.Decryptor.DEFAULT_POIFS_ENTRY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;


import com.zaxxer.sparsebits.SparseBitSet;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSWriterEvent;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSWriterListener;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.TempFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public abstract class ChunkedCipherOutputStream extends FilterOutputStream {
	
	protected static Logger log = LoggerFactory.getLogger(ChunkedCipherOutputStream.class);	
	
    private static final int STREAMING = -1;

    private final int chunkSize;
    private final int chunkBits;

    private final byte[] chunk;
    private final SparseBitSet plainByteFlags;
    private final File fileOut;
    private final DirectoryNode dir;

    private long pos;
    private long totalPos;
    private long written;

    // the cipher can't be final, because for the last chunk we change the padding
    // and therefore need to change the cipher too
    private Cipher cipher;
    private boolean isClosed;

    public ChunkedCipherOutputStream(DirectoryNode dir, int chunkSize) throws IOException, GeneralSecurityException {
        super(null);
        this.chunkSize = chunkSize;
        int cs = chunkSize == STREAMING ? 4096 : chunkSize;
        this.chunk = IOUtils.safelyAllocate(cs, CryptoFunctions.MAX_RECORD_LENGTH);
        this.plainByteFlags = new SparseBitSet(cs);
        this.chunkBits = Integer.bitCount(cs-1);
        this.fileOut = TempFile.createTempFile("encrypted_package", "crypt");
        this.out = Files.newOutputStream(fileOut.toPath());
        this.dir = dir;
        this.cipher = initCipherForBlock(null, 0, false);
    }

    public ChunkedCipherOutputStream(OutputStream stream, int chunkSize) throws IOException, GeneralSecurityException {
        super(stream);
        this.chunkSize = chunkSize;
        int cs = chunkSize == STREAMING ? 4096 : chunkSize;
        this.chunk = IOUtils.safelyAllocate(cs, CryptoFunctions.MAX_RECORD_LENGTH);
        this.plainByteFlags = new SparseBitSet(cs);
        this.chunkBits = Integer.bitCount(cs-1);
        this.fileOut = null;
        this.dir = null;
        this.cipher = initCipherForBlock(null, 0, false);
    }

    public final Cipher initCipherForBlock(int block, boolean lastChunk) throws IOException, GeneralSecurityException {
        return initCipherForBlock(cipher, block, lastChunk);
    }

    // helper method to break a recursion loop introduced because of an IBMJCE bug, i.e. not resetting on Cipher.doFinal()
    @Internal
    protected Cipher initCipherForBlockNoFlush(Cipher existing, int block, boolean lastChunk)
    throws IOException, GeneralSecurityException {
        return initCipherForBlock(cipher, block, lastChunk);
    }

    protected abstract Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk)
    throws IOException, GeneralSecurityException;

    protected abstract void calculateChecksum(File fileOut, int oleStreamSize)
    throws GeneralSecurityException, IOException;

    protected abstract void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
    throws IOException, GeneralSecurityException;

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte)b});
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        write(b, off, len, false);
    }

    public void writePlain(byte[] b, int off, int len) throws IOException {
        write(b, off, len, true);
    }

    protected void write(byte[] b, int off, int len, boolean writePlain) throws IOException {
        if (len == 0) {
            return;
        }

        if (len < 0 || b.length < off+len) {
            throw new IOException("not enough bytes in your input buffer");
        }

        final int chunkMask = getChunkMask();
        while (len > 0) {
            int posInChunk = (int)(pos & chunkMask);
            int nextLen = Math.min(chunk.length-posInChunk, len);
            System.arraycopy(b, off, chunk, posInChunk, nextLen);
            if (writePlain) {
                plainByteFlags.set(posInChunk, posInChunk+nextLen);
            }
            pos += nextLen;
            totalPos += nextLen;
            off += nextLen;
            len -= nextLen;
            if ((pos & chunkMask) == 0) {
                writeChunk(len > 0);
            }
        }
    }

    protected int getChunkMask() {
        return chunk.length-1;
    }

    protected void writeChunk(boolean continued) throws IOException {
        if (pos == 0 || totalPos == written) {
            return;
        }

        int posInChunk = (int)(pos & getChunkMask());

        // normally posInChunk is 0, i.e. on the next chunk (-> index-1)
        // but if called on close(), posInChunk is somewhere within the chunk data
        int index = (int)(pos >> chunkBits);
        boolean lastChunk;
        if (posInChunk==0) {
            index--;
            posInChunk = chunk.length;
            lastChunk = false;
        } else {
            // pad the last chunk
            lastChunk = true;
        }

        int ciLen;
        try {
            boolean doFinal = true;
            long oldPos = pos;
            // reset stream (not only) in case we were interrupted by plain stream parts
            // this also needs to be set to prevent an endless loop
            pos = 0;
            if (chunkSize == STREAMING) {
                if (continued) {
                    doFinal = false;
                }
            } else {
                cipher = initCipherForBlock(cipher, index, lastChunk);
                // restore pos - only streaming chunks will be reset
                pos = oldPos;
            }
            ciLen = invokeCipher(posInChunk, doFinal);
        } catch (GeneralSecurityException e) {
            throw new IOException("can't re-/initialize cipher", e);
        }

        out.write(chunk, 0, ciLen);
        plainByteFlags.clear();
        written += ciLen;
    }

    /**
     * Helper function for overriding the cipher invocation, i.e. XOR doesn't use a cipher
     * and uses its own implementation
     */
    protected int invokeCipher(int posInChunk, boolean doFinal) throws GeneralSecurityException, IOException {
        byte[] plain = (plainByteFlags.isEmpty()) ? null : chunk.clone();

        int ciLen = (doFinal)
            ? cipher.doFinal(chunk, 0, posInChunk, chunk)
            : cipher.update(chunk, 0, posInChunk, chunk);

        if (doFinal && "IBMJCE".equals(cipher.getProvider().getName()) && "RC4".equals(cipher.getAlgorithm())) {
            // workaround for IBMs cipher not resetting on doFinal

            int index = (int)(pos >> chunkBits);
            boolean lastChunk;
            if (posInChunk==0) {
                index--;
                posInChunk = chunk.length;
                lastChunk = false;
            } else {
                // pad the last chunk
                lastChunk = true;
            }

            cipher = initCipherForBlockNoFlush(cipher, index, lastChunk);
        }

        if (plain != null) {
            int i = plainByteFlags.nextSetBit(0);
            while (i >= 0 && i < posInChunk) {
                chunk[i] = plain[i];
                i = plainByteFlags.nextSetBit(i+1);
            }
        }

        return ciLen;
    }

    @Override
    public void close() throws IOException {
        if (isClosed) {
            log.debug("ChunkedCipherOutputStream was already closed - ignoring");
            return;
        }

        isClosed = true;

        try {
            writeChunk(false);

            super.close();

            if (fileOut != null) {
                int oleStreamSize = (int)(fileOut.length()+LittleEndianConsts.LONG_SIZE);
                calculateChecksum(fileOut, (int)pos);
                dir.createDocument(DEFAULT_POIFS_ENTRY, oleStreamSize, this::processPOIFSWriterEvent);
                createEncryptionInfoEntry(dir, fileOut);
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        } finally {
            if (fileOut != null) {
                if (!fileOut.delete()) {
                    //ignore
                }
            }
        }
    }

    protected byte[] getChunk() {
        return chunk;
    }

    protected SparseBitSet getPlainByteFlags() {
        return plainByteFlags;
    }

    protected long getPos() {
        return pos;
    }

    protected long getTotalPos() {
        return totalPos;
    }

    /**
     * Some ciphers (actually just XOR) are based on the record size,
     * which needs to be set before encryption
     *
     * @param recordSize the size of the next record
     * @param isPlain {@code true} if the record is unencrypted
     */
    public void setNextRecordSize(int recordSize, boolean isPlain) {
    }

    private void processPOIFSWriterEvent(POIFSWriterEvent event) {
        try {
            try (OutputStream os = event.getStream();
                 InputStream fis = Files.newInputStream(fileOut.toPath())) {

                // StreamSize (8 bytes): An unsigned integer that specifies the number of bytes used by data
                // encrypted within the EncryptedData field, not including the size of the StreamSize field.
                // Note that the actual size of the \EncryptedPackage stream (1) can be larger than this
                // value, depending on the block size of the chosen encryption algorithm
                byte[] buf = new byte[LittleEndianConsts.LONG_SIZE];
                LittleEndian.putLong(buf, 0, pos);
                os.write(buf);

                IOUtils.copy(fis, os);
            }

            if (!fileOut.delete()) {
                log.error("Can't delete temporary encryption file: {}", fileOut);
            }
        } catch (IOException e) {
            throw new EncryptedDocumentException(e);
        }
    }
}