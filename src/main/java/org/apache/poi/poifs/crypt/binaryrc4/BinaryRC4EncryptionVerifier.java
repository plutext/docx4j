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

package org.apache.poi.poifs.crypt.binaryrc4;

//import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.*;
import org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianInput;

public class BinaryRC4EncryptionVerifier extends EncryptionVerifier implements EncryptionRecord {

    protected BinaryRC4EncryptionVerifier() {
        setSpinCount(-1);
        setCipherAlgorithm(CipherAlgorithm.rc4);
        setChainingMode(null);
        setEncryptedKey(null);
        setHashAlgorithm(HashAlgorithm.md5);
    }

    protected BinaryRC4EncryptionVerifier(LittleEndianInput is) {
        byte salt[] = new byte[16];
        is.readFully(salt);
        setSalt(salt);
        byte encryptedVerifier[] = new byte[16];
        is.readFully(encryptedVerifier);
        setEncryptedVerifier(encryptedVerifier);
        byte encryptedVerifierHash[] = new byte[16];
        is.readFully(encryptedVerifierHash);
        setEncryptedVerifierHash(encryptedVerifierHash);
        setSpinCount(-1);
        setCipherAlgorithm(CipherAlgorithm.rc4);
        setChainingMode(null);
        setEncryptedKey(null);
        setHashAlgorithm(HashAlgorithm.md5);
    }

    protected void setSalt(byte salt[]) {
        if (salt == null || salt.length != 16) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        
        super.setSalt(salt);
    }

    protected void setEncryptedVerifier(byte encryptedVerifier[]) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    protected void setEncryptedVerifierHash(byte encryptedVerifierHash[]) {
        super.setEncryptedVerifierHash(encryptedVerifierHash);
    }

    public void write(LittleEndianByteArrayOutputStream bos) {
        byte salt[] = getSalt();
        assert (salt.length == 16);
        bos.write(salt);
        byte encryptedVerifier[] = getEncryptedVerifier();
        assert (encryptedVerifier.length == 16);
        bos.write(encryptedVerifier);
        byte encryptedVerifierHash[] = getEncryptedVerifierHash();
        assert (encryptedVerifierHash.length == 16);
        bos.write(encryptedVerifierHash);
    }

}
