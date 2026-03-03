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

package org.docx4j.org.apache.poi.poifs.crypt.xor;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherInputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.util.LittleEndian;

public class XORDecryptor extends Decryptor {
    private long length = -1L;
    private int chunkSize = 512;

    protected XORDecryptor() {}

    protected XORDecryptor(XORDecryptor other) {
        super(other);
        length = other.length;
        chunkSize = other.chunkSize;
    }

    @Override
    public boolean verifyPassword(String password) {
        XOREncryptionVerifier ver = (XOREncryptionVerifier)getEncryptionInfo().getVerifier();
        int keyVer = LittleEndian.getUShort(ver.getEncryptedKey());
        int verifierVer = LittleEndian.getUShort(ver.getEncryptedVerifier());
        int keyComp      = CryptoFunctions.createXorKey1(password);
        int verifierComp = CryptoFunctions.createXorVerifier1(password);
        if (keyVer == keyComp && verifierVer == verifierComp) {
            byte[] xorArray = CryptoFunctions.createXorArray1(password);
            setSecretKey(new SecretKeySpec(xorArray, "XOR"));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Cipher initCipherForBlock(Cipher cipher, int block)
    throws GeneralSecurityException {
        return null;
    }

    protected static Cipher initCipherForBlock(Cipher cipher, int block,
        EncryptionInfo encryptionInfo, SecretKey skey, int encryptMode)
    throws GeneralSecurityException {
        return null;
    }

    @Override
    public ChunkedCipherInputStream getDataStream(DirectoryNode dir) throws IOException, GeneralSecurityException {
        throw new EncryptedDocumentException("not supported");
    }

    @Override
    public InputStream getDataStream(InputStream stream, int size, int initialPos)
            throws IOException, GeneralSecurityException {
        return new XORCipherInputStream(stream, initialPos);
    }


    @Override
    public long getLength() {
        if (length == -1L) {
            throw new IllegalStateException("Decryptor.getDataStream() was not called");
        }

        return length;
    }

    @Override
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public XORDecryptor copy() {
        return new XORDecryptor(this);
    }

    private class XORCipherInputStream extends ChunkedCipherInputStream {
        private final int initialOffset;
        private int recordStart;
        private int recordEnd;

        public XORCipherInputStream(InputStream stream, int initialPos)
                throws GeneralSecurityException {
            super(stream, Integer.MAX_VALUE, chunkSize);
            this.initialOffset = initialPos;
        }

        @Override
        protected Cipher initCipherForBlock(Cipher existing, int block)
                throws GeneralSecurityException {
            return XORDecryptor.this.initCipherForBlock(existing, block);
        }

        @Override
        protected int invokeCipher(int totalBytes, boolean doFinal) {
            final int pos = (int)getPos();
            final byte[] xorArray = getEncryptionInfo().getDecryptor().getSecretKey().getEncoded();
            final byte[] chunk = getChunk();
            final byte[] plain = getPlain();
            final int posInChunk = pos & getChunkMask();

            /*
             * From: http://social.msdn.microsoft.com/Forums/en-US/3dadbed3-0e68-4f11-8b43-3a2328d9ebd5
             *
             * The initial value for XorArrayIndex is as follows:
             * XorArrayIndex = (FileOffset + Data.Length) % 16
             *
             * The FileOffset variable in this context is the stream offset into the Workbook stream at
             * the time we are about to write each of the bytes of the record data.
             * This (the value) is then incremented after each byte is written.
             */
            final int xorArrayIndex = initialOffset+recordEnd+(pos-recordStart);

            for (int i=0; pos+i < recordEnd && i < totalBytes; i++) {
                // The following is taken from the Libre Office implementation
                // It seems that the encrypt and decrypt method is mixed up
                // in the MS-OFFCRYPTO docs
                byte value = plain[posInChunk+i];
                value = rotateLeft(value, 3);
                value ^= xorArray[(xorArrayIndex+i) & 0x0F];
                chunk[posInChunk+i] = value;
            }

            // the other bytes will be encoded, when setNextRecordSize is called the next time
            return totalBytes;
        }

        private byte rotateLeft(byte bits, int shift) {
            return (byte)(((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
        }


        /**
         * Decrypts a xor obfuscated byte array.
         * The data is decrypted in-place
         *
         * @see <a href="http://msdn.microsoft.com/en-us/library/dd908506.aspx">2.3.7.3 Binary Document XOR Data Transformation Method 1</a>
         */
        @Override
        public void setNextRecordSize(int recordSize) {
            final int pos = (int)getPos();
            final byte[] chunk = getChunk();
            final int chunkMask = getChunkMask();
            recordStart = pos;
            recordEnd = recordStart+recordSize;
            int nextBytes = Math.min(recordSize, chunk.length-(pos & chunkMask));
            invokeCipher(nextBytes, true);
        }
    }
}
