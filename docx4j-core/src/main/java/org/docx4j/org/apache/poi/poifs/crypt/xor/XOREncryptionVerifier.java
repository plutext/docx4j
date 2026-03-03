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

package org.docx4j.org.apache.poi.poifs.crypt.xor;

import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

public class XOREncryptionVerifier extends EncryptionVerifier implements EncryptionRecord {

    protected XOREncryptionVerifier() {
        setEncryptedKey(new byte[2]);
        setEncryptedVerifier(new byte[2]);
    }

    protected XOREncryptionVerifier(LittleEndianInput is) {
        /**
         * key (2 bytes): An unsigned integer that specifies the obfuscation key.
         * See [MS-OFFCRYPTO], 2.3.6.2 section, the first step of initializing XOR
         * array where it describes the generation of 16-bit XorKey value.
         */
        byte[] key = new byte[2];
        is.readFully(key);
        setEncryptedKey(key);

        /**
         * verificationBytes (2 bytes): An unsigned integer that specifies
         * the password verification identifier.
         */
        byte[] verifier = new byte[2];
        is.readFully(verifier);
        setEncryptedVerifier(verifier);
    }

    protected XOREncryptionVerifier(XOREncryptionVerifier other) {
        super(other);
    }

    @Override
    public void write(LittleEndianByteArrayOutputStream bos) {
        bos.write(getEncryptedKey());
        bos.write(getEncryptedVerifier());
    }

    @Override
    public XOREncryptionVerifier copy() {
        return new XOREncryptionVerifier(this);
    }

    @Override
    public final void setEncryptedVerifier(byte[] encryptedVerifier) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    @Override
    public final void setEncryptedKey(byte[] encryptedKey) {
        super.setEncryptedKey(encryptedKey);
    }
}
