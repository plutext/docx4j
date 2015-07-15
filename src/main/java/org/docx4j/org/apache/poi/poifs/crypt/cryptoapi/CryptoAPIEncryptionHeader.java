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




//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.CipherProvider;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.standard.StandardEncryptionHeader;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

public class CryptoAPIEncryptionHeader extends StandardEncryptionHeader {

    public CryptoAPIEncryptionHeader(LittleEndianInput is) throws IOException {
        super(is);
    }

    protected CryptoAPIEncryptionHeader(CipherAlgorithm cipherAlgorithm,
            HashAlgorithm hashAlgorithm, int keyBits, int blockSize,
            ChainingMode chainingMode) {
        super(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }

    public void setKeySize(int keyBits) {
        // Microsoft Base Cryptographic Provider is limited up to 40 bits
        // http://msdn.microsoft.com/en-us/library/windows/desktop/aa375599(v=vs.85).aspx
        boolean found = false;
        for (int size : getCipherAlgorithm().allowedKeySize) {
            if (size == keyBits) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new EncryptedDocumentException("invalid keysize "+keyBits+" for cipher algorithm "+getCipherAlgorithm());
        }
        super.setKeySize(keyBits);
        if (keyBits > 40) {
            setCspName("Microsoft Enhanced Cryptographic Provider v1.0");
        } else {
            setCspName(CipherProvider.rc4.cipherProviderName);
        }
    }
}
