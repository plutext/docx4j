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
package org.docx4j.org.apache.poi.poifs.crypt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.common.Duplicatable;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;

/**
 * Reads and processes OOXML Encryption Headers
 * The constants are largely based on ZIP constants.
 */
public abstract class EncryptionHeader implements GenericRecord, Duplicatable {
    private int flags;
    private int sizeExtra;
    private CipherAlgorithm cipherAlgorithm;
    private HashAlgorithm hashAlgorithm;
    private int keyBits;
    private int blockSize;
    private CipherProvider providerType;
    private ChainingMode chainingMode;
    private byte[] keySalt;
    private String cspName;

    protected EncryptionHeader() {}

    protected EncryptionHeader(EncryptionHeader other) {
        flags = other.flags;
        sizeExtra = other.sizeExtra;
        cipherAlgorithm = other.cipherAlgorithm;
        hashAlgorithm = other.hashAlgorithm;
        keyBits = other.keyBits;
        blockSize = other.blockSize;
        providerType = other.providerType;
        chainingMode = other.chainingMode;
        keySalt = (other.keySalt == null) ? null : other.keySalt.clone();
        cspName = other.cspName;
    }

    public ChainingMode getChainingMode() {
        return chainingMode;
    }

    protected void setChainingMode(ChainingMode chainingMode) {
        this.chainingMode = chainingMode;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getSizeExtra() {
        return sizeExtra;
    }

    public void setSizeExtra(int sizeExtra) {
        this.sizeExtra = sizeExtra;
    }

    public CipherAlgorithm getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
        if (cipherAlgorithm.allowedKeySize.length == 1) {
            setKeySize(cipherAlgorithm.defaultKeySize);
        }
    }

    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public int getKeySize() {
        return keyBits;
    }

    /**
     * Sets the keySize (in bits). Before calling this method, make sure
     * to set the cipherAlgorithm, as the amount of keyBits gets validated against
     * the list of allowed keyBits of the corresponding cipherAlgorithm
     */
    public void setKeySize(int keyBits) {
        this.keyBits = keyBits;
        for (int allowedBits : getCipherAlgorithm().allowedKeySize) {
            if (allowedBits == keyBits) {
                return;
            }
        }
        throw new EncryptedDocumentException("KeySize "+keyBits+" not allowed for cipher "+getCipherAlgorithm());
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public byte[] getKeySalt() {
        return keySalt;
    }

    public void setKeySalt(byte[] salt) {
        this.keySalt = (salt == null) ? null : salt.clone();
    }

    public CipherProvider getCipherProvider() {
        return providerType;
    }

    public void setCipherProvider(CipherProvider providerType) {
        this.providerType = providerType;
    }

    public String getCspName() {
        return cspName;
    }

    public void setCspName(String cspName) {
        this.cspName = cspName;
    }

    @Override
    public abstract EncryptionHeader copy();

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("flags", this::getFlags);
        m.put("sizeExtra", this::getSizeExtra);
        m.put("cipherAlgorithm", this::getCipherAlgorithm);
        m.put("hashAlgorithm", this::getHashAlgorithm);
        m.put("keyBits", this::getKeySize);
        m.put("blockSize", this::getBlockSize);
        m.put("providerType", this::getCipherProvider);
        m.put("chainingMode", this::getChainingMode);
        m.put("keySalt", this::getKeySalt);
        m.put("cspName", this::getCspName);
        return Collections.unmodifiableMap(m);
    }
}
