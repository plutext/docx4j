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
package org.docx4j.org.apache.poi.poifs.crypt.standard;

//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

/**
 * Used when checking if a key is valid for a document 
 */
public class StandardEncryptionVerifier extends EncryptionVerifier implements EncryptionRecord {
    private static final int SPIN_COUNT = 50000;
    private final int verifierHashSize;
    
    protected StandardEncryptionVerifier(LittleEndianInput is, StandardEncryptionHeader header) {
        int saltSize = is.readInt();

        if (saltSize!=16) {
            throw new RuntimeException("Salt size != 16 !?");
        }

        byte salt[] = new byte[16];
        is.readFully(salt);
        setSalt(salt);
        
        byte encryptedVerifier[] = new byte[16];
        is.readFully(encryptedVerifier);
        setEncryptedVerifier(encryptedVerifier);

        verifierHashSize = is.readInt();

        byte encryptedVerifierHash[] = new byte[header.getCipherAlgorithm().encryptedVerifierHashLength];
        is.readFully(encryptedVerifierHash);
        setEncryptedVerifierHash(encryptedVerifierHash);

        setSpinCount(SPIN_COUNT);
        setCipherAlgorithm(header.getCipherAlgorithm());
        setChainingMode(header.getChainingMode());
        setEncryptedKey(null);
        setHashAlgorithm(header.getHashAlgorithmEx()); 
    }
    
    protected StandardEncryptionVerifier(CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(cipherAlgorithm);
        setHashAlgorithm(hashAlgorithm);
        setChainingMode(chainingMode);
        setSpinCount(SPIN_COUNT);
        verifierHashSize = hashAlgorithm.hashSize;
    }

    // make method visible for this package
    protected void setSalt(byte salt[]) {
        if (salt == null || salt.length != 16) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        super.setSalt(salt);
    }
    
    // make method visible for this package
    protected void setEncryptedVerifier(byte encryptedVerifier[]) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    // make method visible for this package
    protected void setEncryptedVerifierHash(byte encryptedVerifierHash[]) {
        super.setEncryptedVerifierHash(encryptedVerifierHash);
    }
    
    public void write(LittleEndianByteArrayOutputStream bos) {
        // see [MS-OFFCRYPTO] - 2.3.4.9
        byte salt[] = getSalt();
        assert(salt.length == 16);
        bos.writeInt(salt.length); // salt size
        bos.write(salt);
        
        // The resulting Verifier value MUST be an array of 16 bytes.
        byte encryptedVerifier[] = getEncryptedVerifier(); 
        assert(encryptedVerifier.length == 16);
        bos.write(encryptedVerifier);

        // The number of bytes used by the decrypted Verifier hash is given by
        // the VerifierHashSize field, which MUST be 20
        bos.writeInt(20);

        // EncryptedVerifierHash: An array of bytes that contains the encrypted form of the hash of
        // the randomly generated Verifier value. The length of the array MUST be the size of the
        // encryption block size multiplied by the number of blocks needed to encrypt the hash of the
        // Verifier. If the encryption algorithm is RC4, the length MUST be 20 bytes. If the encryption
        // algorithm is AES, the length MUST be 32 bytes. After decrypting the EncryptedVerifierHash
        // field, only the first VerifierHashSize bytes MUST be used.
        byte encryptedVerifierHash[] = getEncryptedVerifierHash(); 
        assert(encryptedVerifierHash.length == getCipherAlgorithm().encryptedVerifierHashLength);
        bos.write(encryptedVerifierHash);
    }

    protected int getVerifierHashSize() {
        return verifierHashSize;
    }
}
