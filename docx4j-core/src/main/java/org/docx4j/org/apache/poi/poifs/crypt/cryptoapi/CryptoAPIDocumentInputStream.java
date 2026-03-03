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
package org.docx4j.org.apache.poi.poifs.crypt.cryptoapi;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.util.Internal;

/**
 * A seekable InputStream, which is used to decrypt/extract the document entries
 * within the encrypted stream
 */
@Internal
/* package */ class CryptoAPIDocumentInputStream extends ByteArrayInputStream {
    private Cipher cipher;
    private final CryptoAPIDecryptor decryptor;
    private byte[] oneByte = {0};

    public void seek(int newpos) {
        if (newpos > count) {
            throw new ArrayIndexOutOfBoundsException(newpos);
        }

        this.pos = newpos;
        mark = newpos;
    }

    public void setBlock(int block) throws GeneralSecurityException {
        cipher = decryptor.initCipherForBlock(cipher, block);
    }

    @Override
    public synchronized int read() {
        int ch = super.read();
        if (ch == -1) {
            return -1;
        }
        oneByte[0] = (byte) ch;
        try {
            cipher.update(oneByte, 0, 1, oneByte);
        } catch (ShortBufferException e) {
            throw new EncryptedDocumentException(e);
        }
        return oneByte[0] & 0xFF;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {
        int readLen = super.read(b, off, len);
        if (readLen ==-1) {
            return -1;
        }
        try {
            cipher.update(b, off, readLen, b, off);
        } catch (ShortBufferException e) {
            throw new EncryptedDocumentException(e);
        }
        return readLen;
    }

    public CryptoAPIDocumentInputStream(CryptoAPIDecryptor decryptor, byte[] buf)
    throws GeneralSecurityException {
        super(buf);
        this.decryptor = decryptor;
        cipher = decryptor.initCipherForBlock(null, 0);
    }
}