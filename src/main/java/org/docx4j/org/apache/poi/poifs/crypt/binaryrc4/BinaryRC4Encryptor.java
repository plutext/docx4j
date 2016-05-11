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

package org.docx4j.org.apache.poi.poifs.crypt.binaryrc4;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

//import org.docx4j.org.apache.poi.EncryptedDocumentException;


import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.Encryptor;
import org.docx4j.org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;

public class BinaryRC4Encryptor extends Encryptor {

    private final BinaryRC4EncryptionInfoBuilder builder;
    
    protected class BinaryRC4CipherOutputStream extends ChunkedCipherOutputStream {

        protected Cipher initCipherForBlock(Cipher cipher, int block, boolean lastChunk)
        throws GeneralSecurityException {
            return BinaryRC4Decryptor.initCipherForBlock(cipher, block, builder, getSecretKey(), Cipher.ENCRYPT_MODE);
        }

        protected void calculateChecksum(File file, int i) {
        }

        protected void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
        throws IOException, GeneralSecurityException {
            BinaryRC4Encryptor.this.createEncryptionInfoEntry(dir);
        }

        public BinaryRC4CipherOutputStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException {
            super(dir, 512);
        }
    }

    protected BinaryRC4Encryptor(BinaryRC4EncryptionInfoBuilder builder) {
        this.builder = builder;
    }

    public void confirmPassword(String password) {
        Random r = new SecureRandom();
        byte salt[] = new byte[16];
        byte verifier[] = new byte[16];
        r.nextBytes(salt);
        r.nextBytes(verifier);
        confirmPassword(password, null, null, verifier, salt, null);
    }

    public void confirmPassword(String password, byte keySpec[],
            byte keySalt[], byte verifier[], byte verifierSalt[],
            byte integritySalt[]) {
        BinaryRC4EncryptionVerifier ver = builder.getVerifier();
        ver.setSalt(verifierSalt);
        SecretKey skey = BinaryRC4Decryptor.generateSecretKey(password, ver);
        setSecretKey(skey);
        try {
            Cipher cipher = BinaryRC4Decryptor.initCipherForBlock(null, 0, builder, skey, Cipher.ENCRYPT_MODE);
            byte encryptedVerifier[] = new byte[16];
            cipher.update(verifier, 0, 16, encryptedVerifier);
            ver.setEncryptedVerifier(encryptedVerifier);
            org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm hashAlgo = ver
                    .getHashAlgorithm();
            MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
            byte calcVerifierHash[] = hashAlg.digest(verifier);
            byte encryptedVerifierHash[] = cipher.doFinal(calcVerifierHash);
            ver.setEncryptedVerifierHash(encryptedVerifierHash);
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException("Password confirmation failed", e);
        }
    }

    public OutputStream getDataStream(DirectoryNode dir)
    throws IOException, GeneralSecurityException {
        OutputStream countStream = new BinaryRC4CipherOutputStream(dir);
        return countStream;
    }

    protected int getKeySizeInBytes() {
        return builder.getHeader().getKeySize() / 8;
    }

    protected void createEncryptionInfoEntry(DirectoryNode dir) throws IOException {
        DataSpaceMapUtils.addDefaultDataSpace(dir);
        final EncryptionInfo info = builder.getEncryptionInfo();
        final BinaryRC4EncryptionHeader header = builder.getHeader();
        final BinaryRC4EncryptionVerifier verifier = builder.getVerifier();
        EncryptionRecord er = new EncryptionRecord() {
            public void write(LittleEndianByteArrayOutputStream bos) {
                bos.writeShort(info.getVersionMajor());
                bos.writeShort(info.getVersionMinor());
                header.write(bos);
                verifier.write(bos);
            }
        };
        DataSpaceMapUtils.createEncryptionEntry(dir, "EncryptionInfo", er);
    }
}
