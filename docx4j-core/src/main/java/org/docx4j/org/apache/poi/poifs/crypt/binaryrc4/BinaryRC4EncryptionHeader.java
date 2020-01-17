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

import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.CipherProvider;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;

public class BinaryRC4EncryptionHeader extends EncryptionHeader implements
        EncryptionRecord {

    protected BinaryRC4EncryptionHeader() {
        setCipherAlgorithm(CipherAlgorithm.rc4);
        setKeySize(40);
        setBlockSize(-1);
        setCipherProvider(CipherProvider.rc4);
        setHashAlgorithm(HashAlgorithm.md5);
        setSizeExtra(0);
        setFlags(0);
        setCspName("");
        setChainingMode(null);
    }

    public void write(LittleEndianByteArrayOutputStream littleendianbytearrayoutputstream) {
    }
}
