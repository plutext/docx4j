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
package org.docx4j.org.apache.poi.poifs.crypt.standard;

import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.hashPassword;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;




//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.util.BoundedInputStream;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 */
public class StandardDecryptor extends Decryptor {
    private long _length = -1;

    protected StandardDecryptor(EncryptionInfoBuilder builder) {
        super(builder);
    }

    public boolean verifyPassword(String password) {
        EncryptionVerifier ver = builder.getVerifier();
        SecretKey skey = generateSecretKey(password, ver, getKeySizeInBytes());
        Cipher cipher = getCipher(skey);

        try {
            byte encryptedVerifier[] = ver.getEncryptedVerifier();
            byte verifier[] = cipher.doFinal(encryptedVerifier);
            setVerifier(verifier);
            MessageDigest sha1 = CryptoFunctions.getMessageDigest(ver.getHashAlgorithm());
            byte[] calcVerifierHash = sha1.digest(verifier);
            byte encryptedVerifierHash[] = ver.getEncryptedVerifierHash();
            byte decryptedVerifierHash[] = cipher.doFinal(encryptedVerifierHash);

            // see 2.3.4.9 Password Verification (Standard Encryption)
            // ... The number of bytes used by the encrypted Verifier hash MUST be 32 ...
            // TODO: check and trim/pad the hashes to 32
            byte[] verifierHash = Arrays.copyOf(decryptedVerifierHash, calcVerifierHash.length);
    
            if (Arrays.equals(calcVerifierHash, verifierHash)) {
                setSecretKey(skey);
                return true;
            } else {
                return false;
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
    }
    
    protected static SecretKey generateSecretKey(String password, EncryptionVerifier ver, int keySize) {
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();

        byte pwHash[] = hashPassword(password, hashAlgo, ver.getSalt(), ver.getSpinCount());

        byte[] blockKey = new byte[4];
        LittleEndian.putInt(blockKey, 0, 0);

        byte[] finalHash = CryptoFunctions.generateKey(pwHash, hashAlgo, blockKey, hashAlgo.hashSize);
        byte x1[] = fillAndXor(finalHash, (byte) 0x36);
        byte x2[] = fillAndXor(finalHash, (byte) 0x5c);

        byte[] x3 = new byte[x1.length + x2.length];
        System.arraycopy(x1, 0, x3, 0, x1.length);
        System.arraycopy(x2, 0, x3, x1.length, x2.length);
        
        byte[] key = Arrays.copyOf(x3, keySize);

        SecretKey skey = new SecretKeySpec(key, ver.getCipherAlgorithm().jceId);
        return skey;
    }

    protected static byte[] fillAndXor(byte hash[], byte fillByte) {
        byte[] buff = new byte[64];
        Arrays.fill(buff, fillByte);

        for (int i=0; i<hash.length; i++) {
            buff[i] = (byte) (buff[i] ^ hash[i]);
        }

        MessageDigest sha1 = CryptoFunctions.getMessageDigest(HashAlgorithm.sha1);
        return sha1.digest(buff);
    }

    private Cipher getCipher(SecretKey key) {
        EncryptionHeader em = builder.getHeader();
        ChainingMode cm = em.getChainingMode();
        assert(cm == ChainingMode.ecb);
        return CryptoFunctions.getCipher(key, em.getCipherAlgorithm(), cm, null, Cipher.DECRYPT_MODE);
    }

    public InputStream getDataStream(DirectoryNode dir) throws IOException {
        DocumentInputStream dis = dir.createDocumentInputStream(DEFAULT_POIFS_ENTRY);

        _length = dis.readLong();

        // limit wrong calculated ole entries - (bug #57080)
        // standard encryption always uses aes encoding, so blockSize is always 16 
        // http://stackoverflow.com/questions/3283787/size-of-data-after-aes-encryption
        int blockSize = builder.getHeader().getCipherAlgorithm().blockSize;
        long cipherLen = (_length/blockSize + 1) * blockSize;
        Cipher cipher = getCipher(getSecretKey());
        
        InputStream boundedDis = new BoundedInputStream(dis, cipherLen);
        return new BoundedInputStream(new CipherInputStream(boundedDis, cipher), _length);
    }

    /**
     * @return the length of the stream returned by {@link #getDataStream(DirectoryNode)}
     */
    public long getLength(){
        if(_length == -1) throw new IllegalStateException("Decryptor.getDataStream() was not called");
        return _length;
    }
}
