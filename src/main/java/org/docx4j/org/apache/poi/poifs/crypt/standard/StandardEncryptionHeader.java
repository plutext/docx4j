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

import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo.flagAES;
import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo.flagCryptoAPI;

import java.io.IOException;
import java.io.InputStream;

import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.CipherProvider;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInput;
import org.docx4j.org.apache.poi.util.LittleEndianOutput;
import org.docx4j.org.apache.poi.util.StringUtil;

public class StandardEncryptionHeader extends EncryptionHeader implements EncryptionRecord {

    protected StandardEncryptionHeader(LittleEndianInput is) throws IOException {
        setFlags(is.readInt());
        setSizeExtra(is.readInt());
        setCipherAlgorithm(CipherAlgorithm.fromEcmaId(is.readInt()));
        setHashAlgorithm(HashAlgorithm.fromEcmaId(is.readInt()));
        int keySize = is.readInt();
        if (keySize == 0) {
            // for the sake of inheritance of the cryptoAPI classes
            // see 2.3.5.1 RC4 CryptoAPI Encryption Header
            // If set to 0x00000000, it MUST be interpreted as 0x00000028 bits.
            keySize = 0x28;
        }
        setKeySize(keySize);
        setBlockSize(getKeySize());
        setCipherProvider(CipherProvider.fromEcmaId(is.readInt()));

        is.readLong(); // skip reserved

        // CSPName may not always be specified
        // In some cases, the salt value of the EncryptionVerifier is the next chunk of data
        ((InputStream)is).mark(LittleEndianConsts.INT_SIZE+1);
        int checkForSalt = is.readInt();
        ((InputStream)is).reset();
        
        if (checkForSalt == 16) {
            setCspName("");
        } else {
            StringBuilder builder = new StringBuilder();
            while (true) {
                char c = (char) is.readShort();
                if (c == 0) break;
                builder.append(c);
            }
            setCspName(builder.toString());
        }
        
        setChainingMode(ChainingMode.ecb);
        setKeySalt(null);
    }

    protected StandardEncryptionHeader(CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(cipherAlgorithm);
        setHashAlgorithm(hashAlgorithm);
        setKeySize(keyBits);
        setBlockSize(blockSize);
        setCipherProvider(cipherAlgorithm.provider);
        setFlags(flagCryptoAPI.setBoolean(0, true)
                | flagAES.setBoolean(0, cipherAlgorithm.provider == CipherProvider.aes));
        // see http://msdn.microsoft.com/en-us/library/windows/desktop/bb931357(v=vs.85).aspx for a full list
        // setCspName("Microsoft Enhanced RSA and AES Cryptographic Provider");
    }
    
    /**
     * serializes the header 
     */
    public void write(LittleEndianByteArrayOutputStream bos) {
        int startIdx = bos.getWriteIndex();
        LittleEndianOutput sizeOutput = bos.createDelayedOutput(LittleEndianConsts.INT_SIZE);
        bos.writeInt(getFlags());
        bos.writeInt(0); // size extra
        bos.writeInt(getCipherAlgorithm().ecmaId);
        bos.writeInt(getHashAlgorithmEx().ecmaId);
        bos.writeInt(getKeySize());
        bos.writeInt(getCipherProvider().ecmaId);
        bos.writeInt(0); // reserved1
        bos.writeInt(0); // reserved2
        String cspName = getCspName();
        if (cspName == null) cspName = getCipherProvider().cipherProviderName;
        bos.write(StringUtil.getToUnicodeLE(cspName));
        bos.writeShort(0);
        int headerSize = bos.getWriteIndex()-startIdx-LittleEndianConsts.INT_SIZE;
        sizeOutput.writeInt(headerSize);        
    }
}
