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

import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getBlock0;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getCipher;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.getMessageDigest;
import static org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions.hashPassword;
import static org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils.createEncryptionEntry;
import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo.ENCRYPTION_INFO_ENTRY;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.getNextBlockSize;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.hashInput;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kCryptoKeyBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kHashedVerifierBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kIntegrityKeyBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kIntegrityValueBlock;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.AgileDecryptor.kVerifierInputBlock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherOutputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.Encryptor;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.RandomSingleton;
import org.docx4j.org.apache.poi.util.XMLHelper;
import org.w3c.dom.Document;

public class AgileEncryptor extends Encryptor {

    private byte[] integritySalt;
    private byte[] pwHash;

    protected AgileEncryptor() {}

    protected AgileEncryptor(AgileEncryptor other) {
        super(other);
        integritySalt = (other.integritySalt == null) ? null : other.integritySalt.clone();
        pwHash = (other.pwHash == null) ? null : other.pwHash.clone();
    }

    @Override
    public void confirmPassword(String password) {
        // see [MS-OFFCRYPTO] - 2.3.3 EncryptionVerifier
        AgileEncryptionHeader header = (AgileEncryptionHeader)getEncryptionInfo().getHeader();
        int blockSize = header.getBlockSize();
        int keySize = header.getKeySize()/8;
        int hashSize = header.getHashAlgorithm().hashSize;

        int maxLen = CryptoFunctions.getMaxRecordLength();
        byte[] newVerifierSalt = IOUtils.safelyAllocate(blockSize, maxLen)
             , newVerifier = IOUtils.safelyAllocate(blockSize, maxLen)
             , newKeySalt = IOUtils.safelyAllocate(blockSize, maxLen)
             , newKeySpec = IOUtils.safelyAllocate(keySize, maxLen)
             , newIntegritySalt = IOUtils.safelyAllocate(hashSize, maxLen);

        // using a java.security.SecureRandom (and avoid allocating a new SecureRandom for each random number needed).
        SecureRandom r = RandomSingleton.getInstance();
        r.nextBytes(newVerifierSalt); // blocksize
        r.nextBytes(newVerifier); // blocksize
        r.nextBytes(newKeySalt); // blocksize
        r.nextBytes(newKeySpec); // keysize
        r.nextBytes(newIntegritySalt); // hashsize

        confirmPassword(password, newKeySpec, newKeySalt, newVerifierSalt, newVerifier, newIntegritySalt);
    }

    @Override
    public void confirmPassword(String password, byte[] keySpec, byte[] keySalt, byte[] verifier, byte[] verifierSalt, byte[] integritySalt) {
        AgileEncryptionVerifier ver = (AgileEncryptionVerifier)getEncryptionInfo().getVerifier();
        AgileEncryptionHeader header = (AgileEncryptionHeader)getEncryptionInfo().getHeader();

        ver.setSalt(verifierSalt);
        header.setKeySalt(keySalt);

        int blockSize = header.getBlockSize();

        pwHash = hashPassword(password, ver.getHashAlgorithm(), verifierSalt, ver.getSpinCount());

        /*
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
        byte[] encryptedVerifier = hashInput(ver, pwHash, kVerifierInputBlock, verifier, Cipher.ENCRYPT_MODE);
        ver.setEncryptedVerifier(encryptedVerifier);


        /*
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
        MessageDigest hashMD = getMessageDigest(ver.getHashAlgorithm());
        byte[] hashedVerifier = hashMD.digest(verifier);
        byte[] encryptedVerifierHash = hashInput(ver, pwHash, kHashedVerifierBlock, hashedVerifier, Cipher.ENCRYPT_MODE);
        ver.setEncryptedVerifierHash(encryptedVerifierHash);

        /*
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
        byte[] encryptedKey = hashInput(ver, pwHash, kCryptoKeyBlock, keySpec, Cipher.ENCRYPT_MODE);
        ver.setEncryptedKey(encryptedKey);

        SecretKey secretKey = new SecretKeySpec(keySpec, header.getCipherAlgorithm().jceId);
        setSecretKey(secretKey);

        /*
         * 2.3.4.14 DataIntegrity Generation (Agile Encryption)
         *
         * The DataIntegrity element contained within an Encryption element MUST be generated by using
         * the following steps:
         * 1. Obtain the intermediate key by decrypting the encryptedKeyValue from a KeyEncryptor
         *    contained within the KeyEncryptors sequence. Use this key for encryption operations in the
         *    remaining steps of this section.
         * 2. Generate a random array of bytes, known as Salt, of the same length as the value of the
         *    KeyData.hashSize attribute.
         * 3. Encrypt the random array of bytes generated in step 2 by using the binary form of the
         *    KeyData.saltValue attribute and a blockKey byte array consisting of the following bytes:
         *    0x5f, 0xb2, 0xad, 0x01, 0x0c, 0xb9, 0xe1, and 0xf6 used to form an initialization vector as
         *    specified in section 2.3.4.12. If the array of bytes is not an integral multiple of blockSize
         *    bytes, pad the array with 0x00 to the next integral multiple of blockSize bytes.
         * 4. Assign the encryptedHmacKey attribute to the base64-encoded form of the result of step 3.
         * 5. Generate an HMAC, as specified in [RFC2104], of the encrypted form of the data (message),
         *    which the DataIntegrity element will verify by using the Salt generated in step 2 as the key.
         *    Note that the entire EncryptedPackage stream (1), including the StreamSize field, MUST be
         *    used as the message.
         * 6. Encrypt the HMAC as in step 3 by using a blockKey byte array consisting of the following bytes:
         *    0xa0, 0x67, 0x7f, 0x02, 0xb2, 0x2c, 0x84, and 0x33.
         * 7.  Assign the encryptedHmacValue attribute to the base64-encoded form of the result of step 6.
         */
        this.integritySalt = integritySalt.clone();

        try {
            byte[] vec = CryptoFunctions.generateIv(header.getHashAlgorithm(), header.getKeySalt(), kIntegrityKeyBlock, header.getBlockSize());
            Cipher cipher = getCipher(secretKey, header.getCipherAlgorithm(), header.getChainingMode(), vec, Cipher.ENCRYPT_MODE);
            byte[] hmacKey = getBlock0(this.integritySalt, getNextBlockSize(this.integritySalt.length, blockSize));
            byte[] encryptedHmacKey = cipher.doFinal(hmacKey);
            header.setEncryptedHmacKey(encryptedHmacKey);
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
    }

    @Override
    public OutputStream getDataStream(DirectoryNode dir)
            throws IOException, GeneralSecurityException {
        // TODO: initialize headers
        return new AgileCipherOutputStream(dir);
    }

    /**
     * Generate an HMAC, as specified in [RFC2104], of the encrypted form of the data (message),
     * which the DataIntegrity element will verify by using the Salt generated in step 2 as the key.
     * Note that the entire EncryptedPackage stream (1), including the StreamSize field, MUST be
     * used as the message.
     *
     * Encrypt the HMAC as in step 3 by using a blockKey byte array consisting of the following bytes:
     * 0xa0, 0x67, 0x7f, 0x02, 0xb2, 0x2c, 0x84, and 0x33.
     **/
    protected void updateIntegrityHMAC(File tmpFile, int oleStreamSize) throws GeneralSecurityException, IOException {
        // as the integrity hmac needs to contain the StreamSize,
        // it's not possible to calculate it on-the-fly while buffering
        // TODO: add stream size parameter to getDataStream()
        AgileEncryptionHeader header = (AgileEncryptionHeader)getEncryptionInfo().getHeader();
        int blockSize = header.getBlockSize();
        HashAlgorithm hashAlgo = header.getHashAlgorithm();
        Mac integrityMD = CryptoFunctions.getMac(hashAlgo);
        byte[] hmacKey = getBlock0(this.integritySalt, getNextBlockSize(this.integritySalt.length, blockSize));
        integrityMD.init(new SecretKeySpec(hmacKey, hashAlgo.jceHmacId));

        byte[] buf = new byte[1024];
        LittleEndian.putLong(buf, 0, oleStreamSize);
        integrityMD.update(buf, 0, LittleEndianConsts.LONG_SIZE);

        try (InputStream fis = Files.newInputStream(tmpFile.toPath())) {
            int readBytes;
            while ((readBytes = fis.read(buf)) != -1) {
                integrityMD.update(buf, 0, readBytes);
            }
        }

        byte[] hmacValue = integrityMD.doFinal();
        byte[] hmacValueFilled = getBlock0(hmacValue, getNextBlockSize(hmacValue.length, blockSize));

        byte[] iv = CryptoFunctions.generateIv(header.getHashAlgorithm(), header.getKeySalt(), kIntegrityValueBlock, blockSize);
        Cipher cipher = CryptoFunctions.getCipher(getSecretKey(), header.getCipherAlgorithm(), header.getChainingMode(), iv, Cipher.ENCRYPT_MODE);
        byte[] encryptedHmacValue = cipher.doFinal(hmacValueFilled);

        header.setEncryptedHmacValue(encryptedHmacValue);
    }

    protected EncryptionDocument createEncryptionDocument() {
        AgileEncryptionVerifier ver = (AgileEncryptionVerifier)getEncryptionInfo().getVerifier();
        AgileEncryptionHeader header = (AgileEncryptionHeader)getEncryptionInfo().getHeader();

        EncryptionDocument ed = new EncryptionDocument();
        KeyData keyData = new KeyData();
        ed.setKeyData(keyData);

        KeyEncryptor keyEnc = new KeyEncryptor();
        ed.getKeyEncryptors().add(keyEnc);

        PasswordKeyEncryptor keyPass = new PasswordKeyEncryptor();
        keyEnc.setPasswordKeyEncryptor(keyPass);

        keyPass.setSpinCount(ver.getSpinCount());

        keyData.setSaltSize(header.getBlockSize());
        keyPass.setSaltSize(ver.getBlockSize());

        keyData.setBlockSize(header.getBlockSize());
        keyPass.setBlockSize(ver.getBlockSize());

        keyData.setKeyBits(header.getKeySize());
        keyPass.setKeyBits(ver.getKeySize());

        keyData.setHashSize(header.getHashAlgorithm().hashSize);
        keyPass.setHashSize(ver.getHashAlgorithm().hashSize);

        // header and verifier have to have the same cipher algorithm
        if (!header.getCipherAlgorithm().xmlId.equals(ver.getCipherAlgorithm().xmlId)) {
            throw new EncryptedDocumentException("Cipher algorithm of header and verifier have to match");
        }

        keyData.setCipherAlgorithm(header.getCipherAlgorithm());
        keyPass.setCipherAlgorithm(header.getCipherAlgorithm());

        keyData.setCipherChaining(header.getChainingMode());
        keyPass.setCipherChaining(header.getChainingMode());

        keyData.setHashAlgorithm(header.getHashAlgorithm());
        keyPass.setHashAlgorithm(ver.getHashAlgorithm());

        keyData.setSaltValue(header.getKeySalt());
        keyPass.setSaltValue(ver.getSalt());
        keyPass.setEncryptedVerifierHashInput(ver.getEncryptedVerifier());
        keyPass.setEncryptedVerifierHashValue(ver.getEncryptedVerifierHash());
        keyPass.setEncryptedKeyValue(ver.getEncryptedKey());

        DataIntegrity hmacData = new DataIntegrity();
        ed.setDataIntegrity(hmacData);
        hmacData.setEncryptedHmacKey(header.getEncryptedHmacKey());
        hmacData.setEncryptedHmacValue(header.getEncryptedHmacValue());

        return ed;
    }

    protected void marshallEncryptionDocument(EncryptionDocument ed, LittleEndianByteArrayOutputStream os) {
        Document doc = XMLHelper.newDocumentBuilder().newDocument();
        ed.write(doc);

        try {
            Transformer trans = XMLHelper.newTransformer();
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            trans.setOutputProperty(OutputKeys.INDENT, "no");
            trans.setOutputProperty(OutputKeys.STANDALONE, "yes");
            trans.transform(new DOMSource(doc), new StreamResult(os));
        } catch (TransformerException e) {
            throw new EncryptedDocumentException("error marshalling encryption info document", e);
        }
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
    private class AgileCipherOutputStream extends ChunkedCipherOutputStream {
        public AgileCipherOutputStream(DirectoryNode dir) throws IOException, GeneralSecurityException {
            super(dir, 4096);
        }

        @Override
        protected Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk)
        throws GeneralSecurityException {
            return AgileDecryptor.initCipherForBlock(existing, block, lastChunk, getEncryptionInfo(), getSecretKey(), Cipher.ENCRYPT_MODE);
        }

        @Override
        protected void calculateChecksum(File fileOut, int oleStreamSize)
        throws GeneralSecurityException, IOException {
            // integrityHMAC needs to be updated before the encryption document is created
            updateIntegrityHMAC(fileOut, oleStreamSize);
        }

        @Override
        protected void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
        throws IOException {
            DataSpaceMapUtils.addDefaultDataSpace(dir);
            createEncryptionEntry(dir, ENCRYPTION_INFO_ENTRY, this::marshallEncryptionRecord);
        }

        private void marshallEncryptionRecord(LittleEndianByteArrayOutputStream bos) {
            final EncryptionInfo info = getEncryptionInfo();

            // EncryptionVersionInfo (4 bytes): A Version structure (section 2.1.4), where
            // Version.vMajor MUST be 0x0004 and Version.vMinor MUST be 0x0004
            bos.writeShort(info.getVersionMajor());
            bos.writeShort(info.getVersionMinor());
            // Reserved (4 bytes): A value that MUST be 0x00000040
            bos.writeInt(info.getEncryptionFlags());

            EncryptionDocument ed = createEncryptionDocument();
            marshallEncryptionDocument(ed, bos);
        }
    }

    @Override
    public AgileEncryptor copy() {
        return new AgileEncryptor(this);
    }
}
