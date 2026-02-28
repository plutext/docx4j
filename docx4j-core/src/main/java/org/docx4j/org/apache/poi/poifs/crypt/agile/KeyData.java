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

import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.ENC_NS;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.getBinAttr;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.getIntAttr;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.getTag;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.setAttr;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.setBinAttr;
import static org.docx4j.org.apache.poi.poifs.crypt.agile.EncryptionDocument.setIntAttr;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A complex type that specifies the encryption used within this element. The saltValue attribute is a base64-encoded
 * binary value that is randomly generated. The number of bytes required to decode the saltValue attribute MUST be equal
 * to the value of the saltSize attribute.
 */
public class KeyData {
    private Integer saltSize;
    private Integer blockSize;
    private Integer keyBits;
    private Integer hashSize;
    private CipherAlgorithm cipherAlgorithm;
    private ChainingMode cipherChaining;
    private HashAlgorithm hashAlgorithm;
    private byte[] saltValue;

    public KeyData() {

    }

    public KeyData(Element parent) {
        Element keyData = getTag(parent, ENC_NS, "keyData");
        if (keyData == null) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor");
        }
        saltSize = getIntAttr(keyData, "saltSize");
        blockSize = getIntAttr(keyData, "blockSize");
        keyBits = getIntAttr(keyData, "keyBits");
        hashSize = getIntAttr(keyData, "hashSize");
        cipherAlgorithm = CipherAlgorithm.fromXmlId(keyData.getAttribute("cipherAlgorithm"), keyBits == null ? -1 : keyBits);
        cipherChaining = ChainingMode.fromXmlId(keyData.getAttribute("cipherChaining"));
        hashAlgorithm = HashAlgorithm.fromEcmaId(keyData.getAttribute("hashAlgorithm"));
        if (cipherAlgorithm == null || cipherChaining == null || hashAlgorithm == null) {
            throw new EncryptedDocumentException("Cipher algorithm, chaining mode or hash algorithm was null");
        }
        saltValue = getBinAttr(keyData, "saltValue");
    }

    void write(Element encryption) {
        Document doc = encryption.getOwnerDocument();
        Element keyData = (Element)encryption.appendChild(doc.createElementNS(ENC_NS, "keyData"));
        setIntAttr(keyData, "saltSize", saltSize);
        setIntAttr(keyData, "blockSize", blockSize);
        setIntAttr(keyData, "keyBits", keyBits);
        setIntAttr(keyData, "hashSize", hashSize);
        setAttr(keyData, "cipherAlgorithm", cipherAlgorithm == null ? null : cipherAlgorithm.xmlId);
        setAttr(keyData, "cipherChaining", cipherChaining == null ? null : cipherChaining.xmlId);
        setAttr(keyData, "hashAlgorithm", hashAlgorithm == null ? null : hashAlgorithm.ecmaString);
        setBinAttr(keyData, "saltValue", saltValue);
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
        this.saltValue = (saltValue == null) ? null : saltValue.clone();
    }
}
