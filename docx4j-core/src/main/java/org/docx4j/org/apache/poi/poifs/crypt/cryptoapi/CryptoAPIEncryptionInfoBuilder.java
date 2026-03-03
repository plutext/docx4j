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

package org.docx4j.org.apache.poi.poifs.crypt.cryptoapi;

import java.io.IOException;

import org.docx4j.org.apache.poi.poifs.crypt.*;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

public class CryptoAPIEncryptionInfoBuilder implements EncryptionInfoBuilder {
    public CryptoAPIEncryptionInfoBuilder() {
    }

    /**
     * initialize the builder from a stream
     */
    @Override
    public void initialize(EncryptionInfo info, LittleEndianInput dis)
    throws IOException {
        /* int hSize = */ dis.readInt();
        CryptoAPIEncryptionHeader header = new CryptoAPIEncryptionHeader(dis);
        info.setHeader(header);
        info.setVerifier(new CryptoAPIEncryptionVerifier(dis, header));
        CryptoAPIDecryptor dec = new CryptoAPIDecryptor();
        dec.setEncryptionInfo(info);
        info.setDecryptor(dec);
        CryptoAPIEncryptor enc = new CryptoAPIEncryptor();
        enc.setEncryptionInfo(info);
        info.setEncryptor(enc);
    }

    /**
     * initialize the builder from scratch
     */
    @Override
    public void initialize(EncryptionInfo info,
            CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm,
            int keyBits, int blockSize, ChainingMode chainingMode) {
        if (cipherAlgorithm == null) {
            cipherAlgorithm = CipherAlgorithm.rc4;
        }
        if (hashAlgorithm == null) {
            hashAlgorithm = HashAlgorithm.sha1;
        }
        if (keyBits == -1) {
            keyBits = 0x28;
        } 
        assert(cipherAlgorithm == CipherAlgorithm.rc4 && hashAlgorithm == HashAlgorithm.sha1);
        
        info.setHeader(new CryptoAPIEncryptionHeader(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode));
        info.setVerifier(new CryptoAPIEncryptionVerifier(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode));
        CryptoAPIDecryptor dec = new CryptoAPIDecryptor();
        dec.setEncryptionInfo(info);
        info.setDecryptor(dec);
        CryptoAPIEncryptor enc = new CryptoAPIEncryptor();
        enc.setEncryptionInfo(info);
        info.setEncryptor(enc);
    }
}
