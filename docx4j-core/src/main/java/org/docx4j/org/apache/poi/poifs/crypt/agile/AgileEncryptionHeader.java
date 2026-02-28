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

import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;

public class AgileEncryptionHeader extends EncryptionHeader {
    private byte[] encryptedHmacKey;
    private byte[] encryptedHmacValue;

    public AgileEncryptionHeader(String descriptor) {
        this(AgileEncryptionInfoBuilder.parseDescriptor(descriptor));
    }

    public AgileEncryptionHeader(AgileEncryptionHeader other) {
        super(other);
        encryptedHmacKey = (other.encryptedHmacKey == null) ? null : other.encryptedHmacKey.clone();
        encryptedHmacValue = (other.encryptedHmacValue == null) ? null : other.encryptedHmacValue.clone();
    }

    protected AgileEncryptionHeader(EncryptionDocument ed) {
        KeyData keyData;
        try {
            keyData = ed.getKeyData();
            if (keyData == null) {
                throw new NullPointerException("keyData not set");
            }
        } catch (Exception e) {
            throw new EncryptedDocumentException("Unable to parse keyData");
        }

        int keyBits = keyData.getKeyBits();

        CipherAlgorithm ca = keyData.getCipherAlgorithm();
        setCipherAlgorithm(ca);
        setCipherProvider(ca.provider);

        setKeySize(keyBits);
        setFlags(0);
        setSizeExtra(0);
        setCspName(null);
        setBlockSize(keyData.getBlockSize() == null ? 0 : keyData.getBlockSize());

        setChainingMode(keyData.getCipherChaining());

        if (getChainingMode() != ChainingMode.cbc && getChainingMode() != ChainingMode.cfb) {
            throw new EncryptedDocumentException("Unsupported chaining mode - "+ keyData.getCipherChaining());
        }

        Integer hashSizeObj = keyData.getHashSize();
        if (hashSizeObj == null) {
            throw new EncryptedDocumentException("Invalid hash size: " + hashSizeObj);
        }
        int hashSize = hashSizeObj;

        HashAlgorithm ha = keyData.getHashAlgorithm();
        setHashAlgorithm(ha);

        if (getHashAlgorithm().hashSize != hashSize) {
            throw new EncryptedDocumentException("Unsupported hash algorithm: " +
                    keyData.getHashAlgorithm() + " @ " + hashSize + " bytes");
        }

        if (keyData.getSaltSize() == null) {
            throw new EncryptedDocumentException("Invalid salt length: " + keyData.getSaltSize());
        }

        int saltLength = keyData.getSaltSize();
        setKeySalt(keyData.getSaltValue());
        if (getKeySalt().length != saltLength) {
            throw new EncryptedDocumentException("Invalid salt length: " + getKeySalt().length + " and " + saltLength);
        }

        DataIntegrity di = ed.getDataIntegrity();
        setEncryptedHmacKey(di.getEncryptedHmacKey());
        setEncryptedHmacValue(di.getEncryptedHmacValue());
    }


    public AgileEncryptionHeader(CipherAlgorithm algorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(algorithm);
        setHashAlgorithm(hashAlgorithm);
        setKeySize(keyBits);
        setBlockSize(blockSize);
        setChainingMode(chainingMode);
    }

    // make method visible for this package
    @Override
    public void setKeySalt(byte[] salt) {
        if (salt == null || salt.length != getBlockSize()) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        super.setKeySalt(salt);
    }

    public byte[] getEncryptedHmacKey() {
        return encryptedHmacKey;
    }

    protected void setEncryptedHmacKey(byte[] encryptedHmacKey) {
        this.encryptedHmacKey = (encryptedHmacKey == null) ? null : encryptedHmacKey.clone();
    }

    public byte[] getEncryptedHmacValue() {
        return encryptedHmacValue;
    }

    protected void setEncryptedHmacValue(byte[] encryptedHmacValue) {
        this.encryptedHmacValue = (encryptedHmacValue == null) ? null : encryptedHmacValue.clone();
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "encryptedHmacKey", this::getEncryptedHmacKey,
            "encryptedHmacValue", this::getEncryptedHmacValue
        );
    }

    @Override
    public AgileEncryptionHeader copy() {
        return new AgileEncryptionHeader(this);
    }
}
