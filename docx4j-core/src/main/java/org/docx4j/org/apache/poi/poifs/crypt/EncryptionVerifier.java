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

import org.docx4j.org.apache.poi.common.Duplicatable;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;

/**
 * Used when checking if a key is valid for a document
 */
public abstract class EncryptionVerifier implements GenericRecord, Duplicatable {
    private byte[] salt;
    private byte[] encryptedVerifier;
    private byte[] encryptedVerifierHash;
    private byte[] encryptedKey;
    // protected int verifierHashSize;
    private int spinCount;
    private CipherAlgorithm cipherAlgorithm;
    private ChainingMode chainingMode;
    private HashAlgorithm hashAlgorithm;

    protected EncryptionVerifier() {}

    protected EncryptionVerifier(EncryptionVerifier other) {
        salt = (other.salt == null) ? null : other.salt.clone();
        encryptedVerifier = (other.encryptedVerifier == null) ? null : other.encryptedVerifier.clone();
        encryptedVerifierHash = (other.encryptedVerifierHash == null) ? null : other.encryptedVerifierHash.clone();
        encryptedKey = (other.encryptedKey == null) ? null : other.encryptedKey.clone();
        spinCount = other.spinCount;
        cipherAlgorithm = other.cipherAlgorithm;
        chainingMode = other.chainingMode;
        hashAlgorithm = other.hashAlgorithm;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getEncryptedVerifier() {
        return encryptedVerifier;
    }

    public byte[] getEncryptedVerifierHash() {
        return encryptedVerifierHash;
    }

    public int getSpinCount() {
        return spinCount;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public CipherAlgorithm getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    public ChainingMode getChainingMode() {
        return chainingMode;
    }

    public void setSalt(byte[] salt) {
        this.salt = (salt == null) ? null : salt.clone();
    }

    public void setEncryptedVerifier(byte[] encryptedVerifier) {
        this.encryptedVerifier = (encryptedVerifier == null) ? null : encryptedVerifier.clone();
    }

    public void setEncryptedVerifierHash(byte[] encryptedVerifierHash) {
        this.encryptedVerifierHash = (encryptedVerifierHash == null) ? null : encryptedVerifierHash.clone();
    }

    public void setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = (encryptedKey == null) ? null : encryptedKey.clone();
    }

    public void setSpinCount(int spinCount) {
        this.spinCount = spinCount;
    }

    public void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }

    public void setChainingMode(ChainingMode chainingMode) {
        this.chainingMode = chainingMode;
    }

    public void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public abstract EncryptionVerifier copy();

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("salt", this::getSalt);
        m.put("encryptedVerifier", this::getEncryptedVerifier);
        m.put("encryptedVerifierHash", this::getEncryptedVerifierHash);
        m.put("encryptedKey", this::getEncryptedKey);
        m.put("spinCount", this::getSpinCount);
        m.put("cipherAlgorithm", this::getCipherAlgorithm);
        m.put("chainingMode", this::getChainingMode);
        m.put("hashAlgorithm", this::getHashAlgorithm);
        return Collections.unmodifiableMap(m);
    }
}