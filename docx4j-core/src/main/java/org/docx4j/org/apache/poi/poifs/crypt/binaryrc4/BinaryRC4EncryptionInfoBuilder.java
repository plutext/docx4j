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

    public BinaryRC4EncryptionInfoBuilder() {
    }

    @Override
    public void initialize(EncryptionInfo info, LittleEndianInput dis)
    throws IOException {
        int vMajor = info.getVersionMajor();
        int vMinor = info.getVersionMinor();
        assert (vMajor == 1 && vMinor == 1);

        info.setHeader(new BinaryRC4EncryptionHeader());
        info.setVerifier(new BinaryRC4EncryptionVerifier(dis));
        Decryptor dec = new BinaryRC4Decryptor();
        dec.setEncryptionInfo(info);
        info.setDecryptor(dec);
        Encryptor enc = new BinaryRC4Encryptor();
        enc.setEncryptionInfo(info);
        info.setEncryptor(enc);
    }

    @Override
    public void initialize(EncryptionInfo info,
        CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm,
        int keyBits, int blockSize, ChainingMode chainingMode) {
        info.setHeader(new BinaryRC4EncryptionHeader());
        info.setVerifier(new BinaryRC4EncryptionVerifier());
        Decryptor dec = new BinaryRC4Decryptor();
        dec.setEncryptionInfo(info);
        info.setDecryptor(dec);
        Encryptor enc = new BinaryRC4Encryptor();
        enc.setEncryptionInfo(info);
        info.setEncryptor(enc);
    }
}
