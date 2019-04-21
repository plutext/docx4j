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
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;



//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSWriterEvent;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSWriterListener;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.TempFile;

@Internal
public abstract class ChunkedCipherOutputStream extends FilterOutputStream {
    protected final int chunkSize;
    protected final int chunkMask;
    protected final int chunkBits;
    
    private final byte[] _chunk;
    private final File fileOut;
    private final DirectoryNode dir;

    private long _pos = 0;
    private Cipher _cipher;
    
    public ChunkedCipherOutputStream(DirectoryNode dir, int chunkSize) throws IOException, GeneralSecurityException {
        super(null);
        this.chunkSize = chunkSize;
        chunkMask = chunkSize-1;
        chunkBits = Integer.bitCount(chunkMask);
        _chunk = new byte[chunkSize];

        fileOut = TempFile.createTempFile("encrypted_package", "crypt");
        fileOut.deleteOnExit();
        this.out = new FileOutputStream(fileOut);
        this.dir = dir;
        _cipher = initCipherForBlock(null, 0, false);
    }

    protected abstract Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk)
    throws GeneralSecurityException;    
    
    protected abstract void calculateChecksum(File fileOut, int oleStreamSize)
    throws GeneralSecurityException, IOException;
    
    protected abstract void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
    throws IOException, GeneralSecurityException;

    public void write(int b) throws IOException {
        write(new byte[]{(byte)b});
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len)
    throws IOException {
        if (len == 0) return;
        
        if (len < 0 || b.length < off+len) {
            throw new IOException("not enough bytes in your input buffer");
        }
        
        while (len > 0) {
            int posInChunk = (int)(_pos & chunkMask);
            int nextLen = Math.min(chunkSize-posInChunk, len);
            System.arraycopy(b, off, _chunk, posInChunk, nextLen);
            _pos += nextLen;
            off += nextLen;
            len -= nextLen;
            if ((_pos & chunkMask) == 0) {
                try {
                    writeChunk();
                } catch (GeneralSecurityException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    protected void writeChunk() throws IOException, GeneralSecurityException {
        int posInChunk = (int)(_pos & chunkMask);
        // normally posInChunk is 0, i.e. on the next chunk (-> index-1)
        // but if called on close(), posInChunk is somewhere within the chunk data
        int index = (int)(_pos >> chunkBits);
        boolean lastChunk;
        if (posInChunk==0) {
            index--;
            posInChunk = chunkSize;
            lastChunk = false;
        } else {
            // pad the last chunk
            lastChunk = true;
        }

        _cipher = initCipherForBlock(_cipher, index, lastChunk);

        int ciLen = _cipher.doFinal(_chunk, 0, posInChunk, _chunk);
        out.write(_chunk, 0, ciLen);
    }
    
    public void close() throws IOException {
        try {
            writeChunk();

            super.close();
            
            int oleStreamSize = (int)(fileOut.length()+LittleEndianConsts.LONG_SIZE);
            calculateChecksum(fileOut, (int)_pos);
            dir.createDocument(DEFAULT_POIFS_ENTRY, oleStreamSize, new EncryptedPackageWriter());
            createEncryptionInfoEntry(dir, fileOut);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private class EncryptedPackageWriter implements POIFSWriterListener {
        public void processPOIFSWriterEvent(POIFSWriterEvent event) {
            try {
                OutputStream os = event.getStream();
                byte buf[] = new byte[chunkSize];
    
                // StreamSize (8 bytes): An unsigned integer that specifies the number of bytes used by data 
                // encrypted within the EncryptedData field, not including the size of the StreamSize field. 
                // Note that the actual size of the \EncryptedPackage stream (1) can be larger than this 
                // value, depending on the block size of the chosen encryption algorithm
                LittleEndian.putLong(buf, 0, _pos);
                os.write(buf, 0, LittleEndian.LONG_SIZE);

                FileInputStream fis = new FileInputStream(fileOut);
                int readBytes;
                while ((readBytes = fis.read(buf)) != -1) {
                    os.write(buf, 0, readBytes);
                }
                fis.close();

                os.close();
                
                fileOut.delete();
            } catch (IOException e) {
                throw new EncryptedDocumentException(e);
            }
        }
    }
}
