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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;



//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianInput;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@Internal
public abstract class ChunkedCipherInputStream extends LittleEndianInputStream {
    private final int chunkSize;
    private final int chunkMask;
    private final int chunkBits;
    
    private int _lastIndex = 0;
    private long _pos = 0;
    private long _size;
    private byte[] _chunk;
    private Cipher _cipher;

    public ChunkedCipherInputStream(LittleEndianInput stream, long size, int chunkSize)
        throws GeneralSecurityException {
        super((InputStream)stream);
        _size = size;
        this.chunkSize = chunkSize;
        chunkMask = chunkSize-1;
        chunkBits = Integer.bitCount(chunkMask);
        
        _cipher = initCipherForBlock(null, 0);
    }
    
    protected abstract Cipher initCipherForBlock(Cipher existing, int block)
    throws GeneralSecurityException;

    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b) == 1)
            return b[0];
        return -1;
    }

    // do not implement! -> recursion
    // public int read(byte[] b) throws IOException;

    public int read(byte[] b, int off, int len) throws IOException {
        int total = 0;
        
        if (available() <= 0) return -1;

        while (len > 0) {
            if (_chunk == null) {
                try {
                    _chunk = nextChunk();
                } catch (GeneralSecurityException e) {
                    throw new EncryptedDocumentException(e.getMessage(), e);
                }
            }
            int count = (int)(chunkSize - (_pos & chunkMask));
            int avail = available();
            if (avail == 0) {
                return total;
            }
            count = Math.min(avail, Math.min(count, len));
            System.arraycopy(_chunk, (int)(_pos & chunkMask), b, off, count);
            off += count;
            len -= count;
            _pos += count;
            if ((_pos & chunkMask) == 0)
                _chunk = null;
            total += count;
        }

        return total;
    }

    @Override
    public long skip(long n) throws IOException {
        long start = _pos;
        long skip = Math.min(available(), n);

        if ((((_pos + skip) ^ start) & ~chunkMask) != 0)
            _chunk = null;
        _pos += skip;
        return skip;
    }

    @Override
    public int available() {
        return (int)(_size - _pos);
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
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    private byte[] nextChunk() throws GeneralSecurityException, IOException {
        int index = (int)(_pos >> chunkBits);
        initCipherForBlock(_cipher, index);
        
        if (_lastIndex != index) {
            super.skip((index - _lastIndex) << chunkBits);
        }

        byte[] block = new byte[Math.min(super.available(), chunkSize)];
        super.read(block, 0, block.length);
        _lastIndex = index + 1;
        return _cipher.doFinal(block);
    }
}
