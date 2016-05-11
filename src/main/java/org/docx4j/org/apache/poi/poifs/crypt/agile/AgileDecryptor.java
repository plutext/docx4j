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
package org.docx4j.org.apache.poi.poifs.crypt.agile;

import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.generateIv;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.generateKey;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getBlock0;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getCipher;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getMessageDigest;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.hashPassword;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherInputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptionVerifier.AgileCertificateEntry;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * Decryptor implementation for Agile Encryption
 */
public class AgileDecryptor extends Decryptor {
    private long _length = -1;

    protected static final byte[] kVerifierInputBlock;
    protected static final byte[] kHashedVerifierBlock;
    protected static final byte[] kCryptoKeyBlock;
    protected static final byte[] kIntegrityKeyBlock;
    protected static final byte[] kIntegrityValueBlock;

    static {
        kVerifierInputBlock =
            new byte[] { (byte)0xfe, (byte)0xa7, (byte)0xd2, (byte)0x76,
                         (byte)0x3b, (byte)0x4b, (byte)0x9e, (byte)0x79 };
        kHashedVerifierBlock =
            new byte[] { (byte)0xd7, (byte)0xaa, (byte)0x0f, (byte)0x6d,
                         (byte)0x30, (byte)0x61, (byte)0x34, (byte)0x4e };
        kCryptoKeyBlock =
            new byte[] { (byte)0x14, (byte)0x6e, (byte)0x0b, (byte)0xe7,
                         (byte)0xab, (byte)0xac, (byte)0xd0, (byte)0xd6 };
        kIntegrityKeyBlock =
            new byte[] { (byte)0x5f, (byte)0xb2, (byte)0xad, (byte)0x01, 
                         (byte)0x0c, (byte)0xb9, (byte)0xe1, (byte)0xf6 };
        kIntegrityValueBlock =
            new byte[] { (byte)0xa0, (byte)0x67, (byte)0x7f, (byte)0x02,
                         (byte)0xb2, (byte)0x2c, (byte)0x84, (byte)0x33 };
    }

    protected AgileDecryptor(AgileEncryptionInfoBuilder builder) {
        super(builder);
    }
    
    /**
     * set decryption password
     */
    public boolean verifyPassword(String password) throws GeneralSecurityException {
        AgileEncryptionVerifier ver = (AgileEncryptionVerifier)builder.getVerifier();
        AgileEncryptionHeader header = (AgileEncryptionHeader)builder.getHeader(); 
        HashAlgorithm hashAlgo = header.getHashAlgorithmEx();
        CipherAlgorithm cipherAlgo = header.getCipherAlgorithm();
        int blockSize = header.getBlockSize();
        int keySize = header.getKeySize()/8;

        byte[] pwHash = hashPassword(password, ver.getHashAlgorithm(), ver.getSalt(), ver.getSpinCount());

        /**
         * encryptedVerifierHashInput: This attribute MUST be generated by using the following steps:
         * 1. Generate a random array of bytes with the number of bytes used specified by the saltSize
         *    attribute.
         * 2. Generate an encryption key as specified in section 2.3.4.11 by using the user-supplied password,
         *    the binary byte array used to create the saltValue attribute, and a blockKey byte array
         *    consisting of the following bytes: 0xfe, 0xa7, 0xd2, 0x76, 0x3b, 0x4b, 0x9e, and 0x79.
         * 3. Encrypt the random array of bytes generated in step 1 by using the binary form of the saltValue
         *    attribute as an initialization vector as specified in section 2.3.4.12. If the array of bytes is not an
         *    integral multiple of blockSize bytes, pad the array with 0x00 to the next integral multiple of
         *    blockSize bytes.
         * 4. Use base64 to encode the result of step 3.
         */
        byte verfierInputEnc[] = hashInput(builder, pwHash, kVerifierInputBlock, ver.getEncryptedVerifier(), Cipher.DECRYPT_MODE);
        setVerifier(verfierInputEnc);
        MessageDigest hashMD = getMessageDigest(hashAlgo);
        byte[] verifierHash = hashMD.digest(verfierInputEnc);

        /**
         * encryptedVerifierHashValue: This attribute MUST be generated by using the following steps:
         * 1. Obtain the hash value of the random array of bytes generated in step 1 of the steps for
         *    encryptedVerifierHashInput.
         * 2. Generate an encryption key as specified in section 2.3.4.11 by using the user-supplied password,
         *    the binary byte array used to create the saltValue attribute, and a blockKey byte array
         *    consisting of the following bytes: 0xd7, 0xaa, 0x0f, 0x6d, 0x30, 0x61, 0x34, and 0x4e.
         * 3. Encrypt the hash value obtained in step 1 by using the binary form of the saltValue attribute as
         *    an initialization vector as specified in section 2.3.4.12. If hashSize is not an integral multiple of
         *    blockSize bytes, pad the hash value with 0x00 to an integral multiple of blockSize bytes.
         * 4. Use base64 to encode the result of step 3.
         */
        byte verifierHashDec[] = hashInput(builder, pwHash, kHashedVerifierBlock, ver.getEncryptedVerifierHash(), Cipher.DECRYPT_MODE);
        verifierHashDec = getBlock0(verifierHashDec, hashAlgo.hashSize);
        
        /**
         * encryptedKeyValue: This attribute MUST be generated by using the following steps:
         * 1. Generate a random array of bytes that is the same size as specified by the
         *    Encryptor.KeyData.keyBits attribute of the parent element.
         * 2. Generate an encryption key as specified in section 2.3.4.11, using the user-supplied password,
         *    the binary byte array used to create the saltValue attribute, and a blockKey byte array
         *    consisting of the following bytes: 0x14, 0x6e, 0x0b, 0xe7, 0xab, 0xac, 0xd0, and 0xd6.
         * 3. Encrypt the random array of bytes generated in step 1 by using the binary form of the saltValue
         *    attribute as an initialization vector as specified in section 2.3.4.12. If the array of bytes is not an
         *    integral multiple of blockSize bytes, pad the array with 0x00 to an integral multiple of
         *    blockSize bytes.
         * 4. Use base64 to encode the result of step 3.
         */
        byte keyspec[] = hashInput(builder, pwHash, kCryptoKeyBlock, ver.getEncryptedKey(), Cipher.DECRYPT_MODE);
        keyspec = getBlock0(keyspec, keySize);
        SecretKeySpec secretKey = new SecretKeySpec(keyspec, ver.getCipherAlgorithm().jceId);

        /**
         * 1. Obtain the intermediate key by decrypting the encryptedKeyValue from a KeyEncryptor
         *    contained within the KeyEncryptors sequence. Use this key for encryption operations in the
         *    remaining steps of this section.
         * 2. Generate a random array of bytes, known as Salt, of the same length as the value of the
         *    KeyData.hashSize attribute.
         * 3. Encrypt the random array of bytes generated in step 2 by using the binary form of the
         *    KeyData.saltValue attribute and a blockKey byte array consisting of the following bytes: 0x5f,
         *    0xb2, 0xad, 0x01, 0x0c, 0xb9, 0xe1, and 0xf6 used to form an initialization vector as specified in
         *    section 2.3.4.12. If the array of bytes is not an integral multiple of blockSize bytes, pad the
         *    array with 0x00 to the next integral multiple of blockSize bytes.
         * 4. Assign the encryptedHmacKey attribute to the base64-encoded form of the result of step 3.
         */
        byte vec[] = CryptoFunctions.generateIv(hashAlgo, header.getKeySalt(), kIntegrityKeyBlock, blockSize); 
        Cipher cipher = getCipher(secretKey, cipherAlgo, ver.getChainingMode(), vec, Cipher.DECRYPT_MODE);
        
        byte hmacKey[] = cipher.doFinal(header.getEncryptedHmacKey());
        hmacKey = getBlock0(hmacKey, hashAlgo.hashSize);

        /**
         * 5. Generate an HMAC, as specified in [RFC2104], of the encrypted form of the data (message),
         *    which the DataIntegrity element will verify by using the Salt generated in step 2 as the key.
         *    Note that the entire EncryptedPackage stream (1), including the StreamSize field, MUST be
         *    used as the message.
         * 6. Encrypt the HMAC as in step 3 by using a blockKey byte array consisting of the following bytes:
         *    0xa0, 0x67, 0x7f, 0x02, 0xb2, 0x2c, 0x84, and 0x33.
         * 7. Assign the encryptedHmacValue attribute to the base64-encoded form of the result of step 6.
         */
        vec = CryptoFunctions.generateIv(hashAlgo, header.getKeySalt(), kIntegrityValueBlock, blockSize);
        
        cipher = getCipher(secretKey, cipherAlgo, ver.getChainingMode(), vec, Cipher.DECRYPT_MODE);
        byte hmacValue[] = cipher.doFinal(header.getEncryptedHmacValue());
        hmacValue = getBlock0(hmacValue, hashAlgo.hashSize);
        
        if (Arrays.equals(verifierHashDec, verifierHash)) {
            setSecretKey(secretKey);
            setIntegrityHmacKey(hmacKey);
            setIntegrityHmacValue(hmacValue);
            return true;
        } else {
            return false;
        }
    }

    /**
     * instead of a password, it's also possible to decrypt via certificate.
     * Warning: this code is experimental and hasn't been validated
     * 
     * @see <a href="http://social.msdn.microsoft.com/Forums/en-US/cc9092bb-0c82-4b5b-ae21-abf643bdb37c/agile-encryption-with-certificates">Agile encryption with certificates</a>
     *
     * @param keyPair
     * @param x509
     * @return true, when the data can be successfully decrypted with the given private key
     * @throws GeneralSecurityException
     */
    public boolean verifyPassword(KeyPair keyPair, X509Certificate x509) throws GeneralSecurityException {
        AgileEncryptionVerifier ver = (AgileEncryptionVerifier)builder.getVerifier();
        AgileEncryptionHeader header = (AgileEncryptionHeader)builder.getHeader();
        HashAlgorithm hashAlgo = header.getHashAlgorithmEx();
        CipherAlgorithm cipherAlgo = header.getCipherAlgorithm();
        int blockSize = header.getBlockSize();
        
        AgileCertificateEntry ace = null;
        for (AgileCertificateEntry aceEntry : ver.getCertificates()) {
            if (x509.equals(aceEntry.x509)) {
                ace = aceEntry;
                break;
            }
        }
        if (ace == null) return false;
        
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte keyspec[] = cipher.doFinal(ace.encryptedKey);
        SecretKeySpec secretKey = new SecretKeySpec(keyspec, ver.getCipherAlgorithm().jceId);
        
        Mac x509Hmac = CryptoFunctions.getMac(hashAlgo);
        x509Hmac.init(secretKey);
        byte certVerifier[] = x509Hmac.doFinal(ace.x509.getEncoded());

        byte vec[] = CryptoFunctions.generateIv(hashAlgo, header.getKeySalt(), kIntegrityKeyBlock, blockSize); 
        cipher = getCipher(secretKey, cipherAlgo, ver.getChainingMode(), vec, Cipher.DECRYPT_MODE);
        byte hmacKey[] = cipher.doFinal(header.getEncryptedHmacKey());
        hmacKey = getBlock0(hmacKey, hashAlgo.hashSize);

        vec = CryptoFunctions.generateIv(hashAlgo, header.getKeySalt(), kIntegrityValueBlock, blockSize);
        cipher = getCipher(secretKey, cipherAlgo, ver.getChainingMode(), vec, Cipher.DECRYPT_MODE);
        byte hmacValue[] = cipher.doFinal(header.getEncryptedHmacValue());
        hmacValue = getBlock0(hmacValue, hashAlgo.hashSize);
        
        
        if (Arrays.equals(ace.certVerifier, certVerifier)) {
            setSecretKey(secretKey);
            setIntegrityHmacKey(hmacKey);
            setIntegrityHmacValue(hmacValue);
            return true;
        } else {
            return false;
        }
    }

    protected static int getNextBlockSize(int inputLen, int blockSize) {
        int fillSize;
        for (fillSize=blockSize; fillSize<inputLen; fillSize+=blockSize);
        return fillSize;
    }

    protected static byte[] hashInput(EncryptionInfoBuilder builder, byte pwHash[], byte blockKey[], byte inputKey[], int cipherMode) {
        EncryptionVerifier ver = builder.getVerifier();
        AgileDecryptor dec = (AgileDecryptor)builder.getDecryptor();
        int keySize = dec.getKeySizeInBytes();
        int blockSize = dec.getBlockSizeInBytes();
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        byte[] salt = ver.getSalt();

        byte intermedKey[] = generateKey(pwHash, hashAlgo, blockKey, keySize);
        SecretKey skey = new SecretKeySpec(intermedKey, ver.getCipherAlgorithm().jceId);
        byte[] iv = generateIv(hashAlgo, salt, null, blockSize);
        Cipher cipher = getCipher(skey, ver.getCipherAlgorithm(), ver.getChainingMode(), iv, cipherMode);
        byte[] hashFinal;
        
        try {
            inputKey = getBlock0(inputKey, getNextBlockSize(inputKey.length, blockSize));
            hashFinal = cipher.doFinal(inputKey);
            return hashFinal;
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
    }

    public InputStream getDataStream(DirectoryNode dir) throws IOException, GeneralSecurityException {
        DocumentInputStream dis = dir.createDocumentInputStream(DEFAULT_POIFS_ENTRY);
        _length = dis.readLong();
        
        ChunkedCipherInputStream cipherStream = new AgileCipherInputStream(dis, _length);
        return cipherStream;
    }

    public long getLength(){
        if(_length == -1) throw new IllegalStateException("EcmaDecryptor.getDataStream() was not called");
        return _length;
    }


    protected static Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk, EncryptionInfoBuilder builder, SecretKey skey, int encryptionMode)
    throws GeneralSecurityException {
    	
        EncryptionHeader header = builder.getHeader();
        if (existing == null || lastChunk) {
            String padding = (lastChunk ? "PKCS5Padding" : "NoPadding");
            existing = getCipher(skey, header.getCipherAlgorithm(), header.getChainingMode(), 
            		header.getKeySalt(), encryptionMode, padding);
        }

        byte[] blockKey = new byte[4];
        LittleEndian.putInt(blockKey, 0, block);
        byte[] iv = generateIv(header.getHashAlgorithmEx(), header.getKeySalt(), blockKey, header.getBlockSize());

        AlgorithmParameterSpec aps;
        if (header.getCipherAlgorithm() == CipherAlgorithm.rc2) {
            aps = new RC2ParameterSpec(skey.getEncoded().length*8, iv);
        } else {
            aps = new IvParameterSpec(iv);
        }
            
        existing.init(encryptionMode, skey, aps);
        
        return existing;
    }

    /**
     * 2.3.4.15 Data Encryption (Agile Encryption)
     * 
     * The EncryptedPackage stream (1) MUST be encrypted in 4096-byte segments to facilitate nearly
     * random access while allowing CBC modes to be used in the encryption process.
     * The initialization vector for the encryption process MUST be obtained by using the zero-based
     * segment number as a blockKey and the binary form of the KeyData.saltValue as specified in
     * section 2.3.4.12. The block number MUST be represented as a 32-bit unsigned integer.
     * Data blocks MUST then be encrypted by using the initialization vector and the intermediate key
     * obtained by decrypting the encryptedKeyValue from a KeyEncryptor contained within the
     * KeyEncryptors sequence as specified in section 2.3.4.10. The final data block MUST be padded to
     * the next integral multiple of the KeyData.blockSize value. Any padding bytes can be used. Note
     * that the StreamSize field of the EncryptedPackage field specifies the number of bytes of
     * unencrypted data as specified in section 2.3.4.4.
     */
    private class AgileCipherInputStream extends ChunkedCipherInputStream {
        public AgileCipherInputStream(DocumentInputStream stream, long size)
        throws GeneralSecurityException {
            super(stream, size, 4096);
        }

        // TODO: calculate integrity hmac while reading the stream
        // for a post-validation of the data
        
        protected Cipher initCipherForBlock(Cipher cipher, int block)
        throws GeneralSecurityException {
            return AgileDecryptor.initCipherForBlock(cipher, block, false, builder, getSecretKey(), Cipher.DECRYPT_MODE);
        }
    }
}
