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
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;

/**
 * Used when checking if a key is valid for a document
 */
public class AgileEncryptionVerifier extends EncryptionVerifier {

    private int keyBits = -1;
    private int blockSize = -1;

    @SuppressWarnings("unused")
    public AgileEncryptionVerifier(String descriptor) {
        this(AgileEncryptionInfoBuilder.parseDescriptor(descriptor));
    }

    protected AgileEncryptionVerifier(EncryptionDocument ed) {
        PasswordKeyEncryptor keyData = null;
        for (KeyEncryptor ke : ed.getKeyEncryptors()) {
            keyData = ke.getPasswordKeyEncryptor();
            if (keyData != null) {
                break;
            }
        }

        if (keyData == null || keyData.getHashSize() == null) {
            throw new IllegalArgumentException("encryptedKey not set");
        }

        setCipherAlgorithm(keyData.getCipherAlgorithm());
        setKeySize(keyData.getKeyBits());

        Integer blockSize = keyData.getBlockSize();
        if (blockSize == null) {
            throw new IllegalArgumentException("blockSize not set");
        }
        setBlockSize(blockSize);

        Integer hashSize = keyData.getHashSize();
        if (hashSize == null) {
            throw new IllegalArgumentException("hashSize not set");
        }

        HashAlgorithm ha = keyData.getHashAlgorithm();
        setHashAlgorithm(ha);

        if (getHashAlgorithm().hashSize != hashSize) {
            throw new EncryptedDocumentException("Unsupported hash algorithm: " +
                    keyData.getHashAlgorithm() + " @ " + hashSize + " bytes");
        }

        Integer spinCount = keyData.getSpinCount();
        if (spinCount != null) {
            setSpinCount(spinCount);
        }
        setEncryptedVerifier(keyData.getEncryptedVerifierHashInput());
        setSalt(keyData.getSaltValue());
        setEncryptedKey(keyData.getEncryptedKeyValue());
        setEncryptedVerifierHash(keyData.getEncryptedVerifierHashValue());

        Integer saltSize = keyData.getSaltSize();
        if (saltSize == null || saltSize != getSalt().length) {
            throw new EncryptedDocumentException("Invalid salt size");
        }

        setChainingMode(keyData.getCipherChaining());
        if (keyData.getCipherChaining() != ChainingMode.cbc && keyData.getCipherChaining() != ChainingMode.cfb) {
            throw new EncryptedDocumentException("Unsupported chaining mode - "+ keyData.getCipherChaining());
        }
    }

    public AgileEncryptionVerifier(CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(cipherAlgorithm);
        setHashAlgorithm(hashAlgorithm);
        setChainingMode(chainingMode);
        setKeySize(keyBits);
        setBlockSize(blockSize);
        setSpinCount(100000); // TODO: use parameter
    }

    public AgileEncryptionVerifier(AgileEncryptionVerifier other) {
        super(other);
        keyBits = other.keyBits;
        blockSize = other.blockSize;
    }

    @Override
    public void setSalt(byte[] salt) {
        if (salt == null || salt.length != getCipherAlgorithm().blockSize) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        super.setSalt(salt);
    }

    // make method visible for this package
    @Override
    public void setEncryptedVerifier(byte[] encryptedVerifier) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    // make method visible for this package
    @Override
    public void setEncryptedVerifierHash(byte[] encryptedVerifierHash) {
        super.setEncryptedVerifierHash(encryptedVerifierHash);
    }

    // make method visible for this package
    @Override
    public void setEncryptedKey(byte[] encryptedKey) {
        super.setEncryptedKey(encryptedKey);
    }

    @Override
    public AgileEncryptionVerifier copy() {
        return new AgileEncryptionVerifier(this);
    }


    /**
     * The keysize (in bits) of the verifier data. This usually equals the keysize of the header,
     * but only on a few exceptions, like files generated by Office for Mac, can be
     * different.
     *
     * @return the keysize (in bits) of the verifier.
     */
    public int getKeySize() {
        return keyBits;
    }


    /**
     * The blockSize (in bytes) of the verifier data.
     * This usually equals the blocksize of the header.
     *
     * @return the blockSize (in bytes) of the verifier,
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Sets the keysize (in bits) of the verifier
     *
     * @param keyBits the keysize (in bits)
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


    /**
     * Sets the blockSize (in bytes) of the verifier
     *
     * @param blockSize the blockSize (in bytes)
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public final void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        super.setCipherAlgorithm(cipherAlgorithm);
        if (cipherAlgorithm.allowedKeySize.length == 1) {
            setKeySize(cipherAlgorithm.defaultKeySize);
        }
    }
}
