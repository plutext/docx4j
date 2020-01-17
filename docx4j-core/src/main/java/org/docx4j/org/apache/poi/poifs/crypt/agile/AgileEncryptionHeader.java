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

import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTDataIntegrity;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTKeyData;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.STCipherChaining;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;

public class AgileEncryptionHeader extends EncryptionHeader {
    private byte encryptedHmacKey[], encryptedHmacValue[];
    
    public AgileEncryptionHeader(String descriptor) {
        this(AgileEncryptionInfoBuilder.parseDescriptor(descriptor));
    }
    
    protected AgileEncryptionHeader(CTEncryption ed) {
        CTKeyData keyData;
        try {
            keyData = ed.getKeyData();
            if (keyData == null) {
                throw new NullPointerException("keyData not set");
            }
        } catch (Exception e) {
            throw new EncryptedDocumentException("Unable to parse keyData");
        }

        setKeySize((int)keyData.getKeyBits());
        setFlags(0);
        setSizeExtra(0);
        setCspName(null);
        setBlockSize((int)keyData.getBlockSize());

        int keyBits = (int)keyData.getKeyBits();
        
        CipherAlgorithm ca = CipherAlgorithm.fromXmlId(keyData.getCipherAlgorithm().toString(), keyBits);
        setCipherAlgorithm(ca);
        setCipherProvider(ca.provider);

        if (keyData.getCipherChaining()==STCipherChaining.CHAINING_MODE_CBC) {
            setChainingMode(ChainingMode.cbc);
        } else if (keyData.getCipherChaining()==STCipherChaining.CHAINING_MODE_CFB) {
            setChainingMode(ChainingMode.cfb);
        } else {
            throw new EncryptedDocumentException("Unsupported chaining mode - "+keyData.getCipherChaining().toString());
        }
    
        int hashSize = (int)keyData.getHashSize();
        
        HashAlgorithm ha = HashAlgorithm.fromEcmaId(keyData.getHashAlgorithm().value());
        setHashAlgorithm(ha);

        if (getHashAlgorithmEx().hashSize != hashSize) {
            throw new EncryptedDocumentException("Unsupported hash algorithm: " + 
                    keyData.getHashAlgorithm().value() + " @ " + hashSize + " bytes");
        }

        int saltLength = (int)keyData.getSaltSize();
        setKeySalt(keyData.getSaltValue());
        if (getKeySalt().length != saltLength) {
            throw new EncryptedDocumentException("Invalid salt length");
        }
        
        CTDataIntegrity di = ed.getDataIntegrity();
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
    protected void setKeySalt(byte salt[]) {
        if (salt == null || salt.length != getBlockSize()) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        super.setKeySalt(salt);
    }

    public byte[] getEncryptedHmacKey() {
        return encryptedHmacKey;
    }

    protected void setEncryptedHmacKey(byte[] encryptedHmacKey) {
        this.encryptedHmacKey = encryptedHmacKey;
    }

    public byte[] getEncryptedHmacValue() {
        return encryptedHmacValue;
    }

    protected void setEncryptedHmacValue(byte[] encryptedHmacValue) {
        this.encryptedHmacValue = encryptedHmacValue;
    }
}
