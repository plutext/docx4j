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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.zaxxer.sparsebits.SparseBitSet;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.Encryptor;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.util.LittleEndian;

public class XOREncryptor extends Encryptor {
    protected XOREncryptor() {}

    protected XOREncryptor(XOREncryptor other) {
        super(other);
    }

    @Override
    public void confirmPassword(String password) {
        int keyComp      = CryptoFunctions.createXorKey1(password);
        int verifierComp = CryptoFunctions.createXorVerifier1(password);
        byte[] xorArray = CryptoFunctions.createXorArray1(password);

        byte[] shortBuf = new byte[2];
        XOREncryptionVerifier ver = (XOREncryptionVerifier)getEncryptionInfo().getVerifier();
        LittleEndian.putUShort(shortBuf, 0, keyComp);
        ver.setEncryptedKey(shortBuf);
        LittleEndian.putUShort(shortBuf, 0, verifierComp);
        ver.setEncryptedVerifier(shortBuf);
        setSecretKey(new SecretKeySpec(xorArray, "XOR"));
    }

    @Override
    public void confirmPassword(String password, byte[] keySpec,
                                byte[] keySalt, byte[] verifier, byte[] verifierSalt,
                                byte[] integritySalt) {
        confirmPassword(password);
    }

    @Override
    public OutputStream getDataStream(DirectoryNode dir)
    throws IOException, GeneralSecurityException {
        return new XORCipherOutputStream(dir);
    }

    @Override
    public XORCipherOutputStream getDataStream(OutputStream stream, int initialOffset)
    throws IOException, GeneralSecurityException {
        return new XORCipherOutputStream(stream, initialOffset);
    }

    protected int getKeySizeInBytes() {
        return -1;
    }

    @Override
    public void setChunkSize(int chunkSize) {
        // chunkSize is irrelevant
    }

    @Override
    public XOREncryptor copy() {
        return new XOREncryptor(this);
    }

    private class XORCipherOutputStream extends ChunkedCipherOutputStream {
        private int recordStart;
        private int recordEnd;

        public XORCipherOutputStream(OutputStream stream, int initialPos) throws IOException, GeneralSecurityException {
            super(stream, -1);
        }

        public XORCipherOutputStream(DirectoryNode dir) throws IOException, GeneralSecurityException {
            super(dir, -1);
        }

        @Override
        protected Cipher initCipherForBlock(Cipher cipher, int block, boolean lastChunk)
        throws GeneralSecurityException {
            return XORDecryptor.initCipherForBlock(cipher, block, getEncryptionInfo(), getSecretKey(), Cipher.ENCRYPT_MODE);
        }

        @Override
        protected void calculateChecksum(File file, int i) {
        }

        @Override
        protected void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile) {
            throw new EncryptedDocumentException("createEncryptionInfoEntry not supported");
        }

        @Override
        public void setNextRecordSize(int recordSize, boolean isPlain) {
            if (recordEnd > 0 && !isPlain) {
                // encrypt last record
                invokeCipher((int)getPos(), true);
            }
            recordStart = (int)getTotalPos()+4;
            recordEnd = recordStart+recordSize;
        }

        @Override
        public void flush() throws IOException {
            setNextRecordSize(0, true);
            super.flush();
        }

        @Override
        protected int invokeCipher(int posInChunk, boolean doFinal) {
            if (posInChunk == 0) {
                return 0;
            }

            final int start = Math.max(posInChunk-(recordEnd-recordStart), 0);

            final SparseBitSet plainBytes = getPlainByteFlags();
            final byte[] xorArray = getEncryptionInfo().getEncryptor().getSecretKey().getEncoded();
            final byte[] chunk = getChunk();
            final byte[] plain = (plainBytes.isEmpty()) ? null : chunk.clone();

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
            // ... also need to handle invocation in case of a filled chunk
            int xorArrayIndex = recordEnd+(start-recordStart);

            for (int i=start; i < posInChunk; i++) {
                byte value = chunk[i];
                value ^= xorArray[(xorArrayIndex++) & 0x0F];
                value = rotateLeft(value, 8-3);
                chunk[i] = value;
            }

            if (plain != null) {
                int i = plainBytes.nextSetBit(start);
                while (i >= 0 && i < posInChunk) {
                    chunk[i] = plain[i];
                    i = plainBytes.nextSetBit(i + 1);
                }
            }

            return posInChunk;
        }

        private byte rotateLeft(byte bits, int shift) {
            return (byte)(((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
        }


    }
}
