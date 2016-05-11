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

import org.docx4j.org.apache.poi.EncryptedDocumentException;

//import org.docx4j.org.apache.poi.EncryptedDocumentException;

public enum CipherAlgorithm {
    // key size for rc4: 0x00000028 - 0x00000080 (inclusive) with 8-bit increments
    // no block size, because its a streaming cipher
    rc4(CipherProvider.rc4,    "RC4", 0x6801, 0x40, new int[]{0x28,0x30,0x38,0x40,0x48,0x50,0x58,0x60,0x68,0x70,0x78,0x80}, -1, 20, "RC4", false),
    // aes has always a block size of 128 - only its keysize may vary
    aes128(CipherProvider.aes, "AES", 0x660E, 128, new int[]{128}, 16, 32, "AES", false),
    aes192(CipherProvider.aes, "AES", 0x660F, 192, new int[]{192}, 16, 32, "AES", false),
    aes256(CipherProvider.aes, "AES", 0x6610, 256, new int[]{256}, 16, 32, "AES", false),
    rc2(null, "RC2", -1, 0x80, new int[]{0x28,0x30,0x38,0x40,0x48,0x50,0x58,0x60,0x68,0x70,0x78,0x80}, 8, 20, "RC2", false),
    des(null, "DES", -1, 64, new int[]{64}, 8/*for 56-bit*/, 32, "DES", false),
    // desx is not supported. Not sure, if it can be simulated by des3 somehow
    des3(null, "DESede", -1, 192, new int[]{192}, 8, 32, "3DES", false),
    // need bouncycastle provider for this one ...
    // see http://stackoverflow.com/questions/4436397/3des-des-encryption-using-the-jce-generating-an-acceptable-key
    des3_112(null, "DESede", -1, 128, new int[]{128}, 8, 32, "3DES_112", true),
    // only for digital signatures
    rsa(null, "RSA", -1, 1024, new int[]{1024, 2048, 3072, 4096}, -1, -1, "", false);
    ;
    
    public final CipherProvider provider;
    public final String jceId;
    public final int ecmaId;
    public final int defaultKeySize;
    public final int allowedKeySize[];
    public final int blockSize;
    public final int encryptedVerifierHashLength;
    public final String xmlId;
    public final boolean needsBouncyCastle;
    
    CipherAlgorithm(CipherProvider provider, String jceId, int ecmaId, int defaultKeySize, int allowedKeySize[], int blockSize, int encryptedVerifierHashLength, String xmlId, boolean needsBouncyCastle) {
        this.provider = provider;
        this.jceId = jceId;
        this.ecmaId = ecmaId;
        this.defaultKeySize = defaultKeySize;
        this.allowedKeySize = allowedKeySize;
        this.blockSize = blockSize;
        this.encryptedVerifierHashLength = encryptedVerifierHashLength;
        this.xmlId = xmlId;
        this.needsBouncyCastle = needsBouncyCastle;
    }

    public static CipherAlgorithm fromEcmaId(int ecmaId) {
        for (CipherAlgorithm ca : CipherAlgorithm.values()) {
            if (ca.ecmaId == ecmaId) return ca;
        }
        throw new EncryptedDocumentException("cipher algorithm " + ecmaId + " not found");
    }
    
    public static CipherAlgorithm fromXmlId(String xmlId, int keySize) {
        for (CipherAlgorithm ca : CipherAlgorithm.values()) {
            if (!ca.xmlId.equals(xmlId)) continue;
            for (int ks : ca.allowedKeySize) {
                if (ks == keySize) return ca;
            }
        }
        throw new EncryptedDocumentException("cipher algorithm " + xmlId + "/" + keySize + " not found");
    }
}