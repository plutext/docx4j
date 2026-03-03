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

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.*;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.KeyEncryptor.PASS_NS;

public class PasswordKeyEncryptor {

    /**
     * An unsigned integer that specifies the number of bytes used by a salt. It MUST be at least 1 and no greater than 65,536.
     */
    private Integer saltSize;

    /**
     * An unsigned integer that specifies the number of bytes used to encrypt one block of data.
     * It MUST be at least 2, no greater than 4096, and a multiple of 2.
     */
    private Integer blockSize;

    /**
     * An unsigned integer that specifies the number of bits used by an encryption algorithm.
     * It MUST be at least 8 and a multiple of 8.
     */
    private Integer keyBits;

    /**
     * An unsigned integer that specifies the number of bytes used by a hash value.
     * It MUST be at least 1, no greater than 65,536, and the same number of bytes as the hash algorithm emits.
     */
    private Integer hashSize;

    /**
     * A CipherAlgorithm that specifies the cipher algorithm for a PasswordKeyEncryptor.
     * The cipher algorithm specified MUST be the same as the cipher algorithm specified for the Encryption.keyData element.
     */
    private CipherAlgorithm cipherAlgorithm;

    /**
     * A CipherChaining that specifies the cipher chaining mode for a PasswordKeyEncryptor.
     */
    private ChainingMode cipherChaining;

    /**
     * A HashAlgorithm that specifies the hashing algorithm for a PasswordKeyEncryptor.
     * The hashing algorithm specified MUST be the same as the hashing algorithm specified for the Encryption.keyData element.
     */
    private HashAlgorithm hashAlgorithm;

    /**
     * A base64-encoded binary byte array that specifies the salt value for a PasswordKeyEncryptor.
     * The number of bytes required by the decoded form of this element MUST be saltSize.
     */
    private byte[] saltValue;

    /**
     * A SpinCount that specifies the spin count for a PasswordKeyEncryptor.
     */
    private Integer spinCount;

    /**
     * A base64-encoded value that specifies the encrypted verifier hash input for a
     * PasswordKeyEncryptor used in password verification.
     */
    private byte[] encryptedVerifierHashInput;

    /**
     * A base64-encoded value that specifies the encrypted verifier hash value for a
     * PasswordKeyEncryptor used in password verification.
     */
    private byte[] encryptedVerifierHashValue;

    /**
     * A base64-encoded value that specifies the encrypted form of the intermediate key.
     */
    private byte[] encryptedKeyValue;

    public PasswordKeyEncryptor() {

    }

    public PasswordKeyEncryptor(Element passwordKey) {
        if (passwordKey == null) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        saltSize = getIntAttr(passwordKey, "saltSize");
        blockSize = getIntAttr(passwordKey, "blockSize");
        keyBits = getIntAttr(passwordKey, "keyBits");
        hashSize = getIntAttr(passwordKey, "hashSize");
        cipherAlgorithm = CipherAlgorithm.fromXmlId(passwordKey.getAttribute("cipherAlgorithm"), keyBits == null ? -1 : keyBits);
        cipherChaining = ChainingMode.fromXmlId(passwordKey.getAttribute("cipherChaining"));
        hashAlgorithm = HashAlgorithm.fromEcmaId(passwordKey.getAttribute("hashAlgorithm"));
        saltValue = getBinAttr(passwordKey, "saltValue");
        spinCount = getIntAttr(passwordKey, "spinCount");
        encryptedVerifierHashInput = getBinAttr(passwordKey, "encryptedVerifierHashInput");
        encryptedVerifierHashValue = getBinAttr(passwordKey, "encryptedVerifierHashValue");
        encryptedKeyValue = getBinAttr(passwordKey, "encryptedKeyValue");
    }

    void write(Element encryption) {
        Document doc = encryption.getOwnerDocument();
        Element keyEncryptor = (Element) encryption.appendChild(doc.createElementNS(ENC_NS, "keyEncryptor"));
        keyEncryptor.setAttribute("uri", PASS_NS);
        Element encryptedKey = (Element) keyEncryptor.appendChild(doc.createElementNS(PASS_NS, "p:encryptedKey"));

        setIntAttr(encryptedKey, "saltSize", saltSize);
        setIntAttr(encryptedKey, "blockSize", blockSize);
        setIntAttr(encryptedKey, "keyBits", keyBits);
        setIntAttr(encryptedKey, "hashSize", hashSize);
        setAttr(encryptedKey, "cipherAlgorithm", cipherAlgorithm == null ? null : cipherAlgorithm.xmlId);
        setAttr(encryptedKey, "cipherChaining", cipherChaining == null ? null : cipherChaining.xmlId);
        setAttr(encryptedKey, "hashAlgorithm", hashAlgorithm == null ? null : hashAlgorithm.ecmaString);
        setBinAttr(encryptedKey, "saltValue", saltValue);
        setIntAttr(encryptedKey, "spinCount", spinCount);
        setBinAttr(encryptedKey, "encryptedVerifierHashInput", encryptedVerifierHashInput);
        setBinAttr(encryptedKey, "encryptedVerifierHashValue", encryptedVerifierHashValue);
        setBinAttr(encryptedKey, "encryptedKeyValue", encryptedKeyValue);

    }

    public Integer getSaltSize() {
        return saltSize;
    }

    public void setSaltSize(Integer saltSize) {
        this.saltSize = saltSize;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public Integer getKeyBits() {
        return keyBits;
    }

    public void setKeyBits(Integer keyBits) {
        this.keyBits = keyBits;
    }

    public Integer getHashSize() {
        return hashSize;
    }

    public void setHashSize(Integer hashSize) {
        this.hashSize = hashSize;
    }

    public CipherAlgorithm getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }

    public ChainingMode getCipherChaining() {
        return cipherChaining;
    }

    public void setCipherChaining(ChainingMode cipherChaining) {
        this.cipherChaining = cipherChaining;
    }

    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public byte[] getSaltValue() {
        return saltValue;
    }

    public void setSaltValue(byte[] saltValue) {
        this.saltValue = saltValue;
    }

    public Integer getSpinCount() {
        return spinCount;
    }

    public void setSpinCount(Integer spinCount) {
        this.spinCount = spinCount;
    }

    public byte[] getEncryptedVerifierHashInput() {
        return encryptedVerifierHashInput;
    }

    public void setEncryptedVerifierHashInput(byte[] encryptedVerifierHashInput) {
        this.encryptedVerifierHashInput = encryptedVerifierHashInput;
    }

    public byte[] getEncryptedVerifierHashValue() {
        return encryptedVerifierHashValue;
    }

    public void setEncryptedVerifierHashValue(byte[] encryptedVerifierHashValue) {
        this.encryptedVerifierHashValue = encryptedVerifierHashValue;
    }

    public byte[] getEncryptedKeyValue() {
        return encryptedKeyValue;
    }

    public void setEncryptedKeyValue(byte[] encryptedKeyValue) {
        this.encryptedKeyValue = encryptedKeyValue;
    }
}
