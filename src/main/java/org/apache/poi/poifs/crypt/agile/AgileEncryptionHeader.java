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
package org.apache.poi.poifs.crypt.agile;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.EncryptionHeader;
import org.apache.poi.poifs.crypt.HashAlgorithm;

import com.microsoft.schemas.office.x2006.encryption.CTDataIntegrity;
import com.microsoft.schemas.office.x2006.encryption.CTKeyData;
import com.microsoft.schemas.office.x2006.encryption.STCipherChaining;

public class AgileEncryptionHeader extends EncryptionHeader {
    private byte encryptedHmacKey[], encryptedHmacValue[];
    
    public AgileEncryptionHeader(String descriptor) {
        this(AgileEncryptionInfoBuilder.parseDescriptor(descriptor));
    }
    
    protected AgileEncryptionHeader(EncryptionDocument ed) {
        CTKeyData keyData;
        try {
            keyData = ed.getEncryption().getKeyData();
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
        
        HashAlgorithm ha = HashAlgorithm.fromEcmaId(keyData.getHashAlgorithm().toString());
        setHashAlgorithm(ha);

        if (getHashAlgorithmEx().hashSize != hashSize) {
            throw new EncryptedDocumentException("Unsupported hash algorithm: " + 
                    keyData.getHashAlgorithm() + " @ " + hashSize + " bytes");
        }

        int saltLength = (int)keyData.getSaltSize();
        setKeySalt(keyData.getSaltValue());
        if (getKeySalt().length != saltLength) {
            throw new EncryptedDocumentException("Invalid salt length");
        }
        
        CTDataIntegrity di = ed.getEncryption().getDataIntegrity();
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
