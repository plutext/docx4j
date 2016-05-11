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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.*;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.StringUtil;

public class BinaryRC4Decryptor extends Decryptor {
    private long _length = -1L;
    
    private class BinaryRC4CipherInputStream extends ChunkedCipherInputStream {

        protected Cipher initCipherForBlock(Cipher existing, int block)
                throws GeneralSecurityException {
            return BinaryRC4Decryptor.initCipherForBlock(existing, block, builder, getSecretKey(), Cipher.DECRYPT_MODE);
        }

        public BinaryRC4CipherInputStream(DocumentInputStream stream, long size)
                throws GeneralSecurityException {
            super(stream, size, 512);
        }
    }

    protected BinaryRC4Decryptor(BinaryRC4EncryptionInfoBuilder builder) {
        super(builder);
    }

    public boolean verifyPassword(String password) {
        EncryptionVerifier ver = builder.getVerifier();
        SecretKey skey = generateSecretKey(password, ver);
        try {
            Cipher cipher = initCipherForBlock(null, 0, builder, skey, Cipher.DECRYPT_MODE);
            byte encryptedVerifier[] = ver.getEncryptedVerifier();
            byte verifier[] = new byte[encryptedVerifier.length];
            cipher.update(encryptedVerifier, 0, encryptedVerifier.length, verifier);
            setVerifier(verifier);
            byte encryptedVerifierHash[] = ver.getEncryptedVerifierHash();
            byte verifierHash[] = cipher.doFinal(encryptedVerifierHash);
            HashAlgorithm hashAlgo = ver.getHashAlgorithm();
            MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
            byte calcVerifierHash[] = hashAlg.digest(verifier);
            if (Arrays.equals(calcVerifierHash, verifierHash)) {
                setSecretKey(skey);
                return true;
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
        return false;
    }

    protected static Cipher initCipherForBlock(Cipher cipher, int block,
        EncryptionInfoBuilder builder, SecretKey skey, int encryptMode)
    throws GeneralSecurityException {
        EncryptionVerifier ver = builder.getVerifier();
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        byte blockKey[] = new byte[4];
        LittleEndian.putUInt(blockKey, 0, block);
        byte encKey[] = CryptoFunctions.generateKey(skey.getEncoded(), hashAlgo, blockKey, 16);
        SecretKey key = new SecretKeySpec(encKey, skey.getAlgorithm());
        if (cipher == null) {
            EncryptionHeader em = builder.getHeader();
            cipher = CryptoFunctions.getCipher(key, em.getCipherAlgorithm(), null, null, encryptMode);
        } else {
            cipher.init(encryptMode, key);
        }
        return cipher;
    }

    protected static SecretKey generateSecretKey(String password,
            EncryptionVerifier ver) {
        if (password.length() > 255)
            password = password.substring(0, 255);
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
        byte hash[] = hashAlg.digest(StringUtil.getToUnicodeLE(password));
        byte salt[] = ver.getSalt();
        hashAlg.reset();
        for (int i = 0; i < 16; i++) {
            hashAlg.update(hash, 0, 5);
            hashAlg.update(salt);
        }

        hash = new byte[5];
        System.arraycopy(hashAlg.digest(), 0, hash, 0, 5);
        SecretKey skey = new SecretKeySpec(hash, ver.getCipherAlgorithm().jceId);
        return skey;
    }

    public InputStream getDataStream(DirectoryNode dir) throws IOException,
            GeneralSecurityException {
        DocumentInputStream dis = dir.createDocumentInputStream(DEFAULT_POIFS_ENTRY);
        _length = dis.readLong();
        BinaryRC4CipherInputStream cipherStream = new BinaryRC4CipherInputStream(dis, _length);
        return cipherStream;
    }

    public long getLength() {
        if (_length == -1L) {
            throw new IllegalStateException("Decryptor.getDataStream() was not called");
        }
        
        return _length;
    }
}
