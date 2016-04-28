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

package org.docx4j.org.apache.poi.poifs.crypt.binaryrc4;

import java.io.IOException;

import org.docx4j.org.apache.poi.poifs.crypt.*;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

public class BinaryRC4EncryptionInfoBuilder implements EncryptionInfoBuilder {

    EncryptionInfo info;
    BinaryRC4EncryptionHeader header;
    BinaryRC4EncryptionVerifier verifier;
    BinaryRC4Decryptor decryptor;
    BinaryRC4Encryptor encryptor;

    public BinaryRC4EncryptionInfoBuilder() {
    }

    public void initialize(EncryptionInfo info, LittleEndianInput dis)
    throws IOException {
        this.info = info;
        int vMajor = info.getVersionMajor();
        int vMinor = info.getVersionMinor();
        assert (vMajor == 1 && vMinor == 1);

        header = new BinaryRC4EncryptionHeader();
        verifier = new BinaryRC4EncryptionVerifier(dis);
        decryptor = new BinaryRC4Decryptor(this);
        encryptor = new BinaryRC4Encryptor(this);
    }

    public void initialize(EncryptionInfo info,
        CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm,
        int keyBits, int blockSize, ChainingMode chainingMode) {
        this.info = info;
        header = new BinaryRC4EncryptionHeader();
        verifier = new BinaryRC4EncryptionVerifier();
        decryptor = new BinaryRC4Decryptor(this);
        encryptor = new BinaryRC4Encryptor(this);
    }

    public BinaryRC4EncryptionHeader getHeader() {
        return header;
    }

    public BinaryRC4EncryptionVerifier getVerifier() {
        return verifier;
    }

    public BinaryRC4Decryptor getDecryptor() {
        return decryptor;
    }

    public BinaryRC4Encryptor getEncryptor() {
        return encryptor;
    }

    public EncryptionInfo getEncryptionInfo() {
        return info;
    }
}
