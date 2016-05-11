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

public enum CipherProvider {
    rc4("RC4", 1, "Microsoft Base Cryptographic Provider v1.0"),
    aes("AES", 0x18, "Microsoft Enhanced RSA and AES Cryptographic Provider");

    public static CipherProvider fromEcmaId(int ecmaId) {
        for (CipherProvider cp : CipherProvider.values()) {
            if (cp.ecmaId == ecmaId) return cp;
        }
        throw new EncryptedDocumentException("cipher provider not found");
    }    
    
    public final String jceId;
    public final int ecmaId;
    public final String cipherProviderName;
    CipherProvider(String jceId, int ecmaId, String cipherProviderName) {
        this.jceId = jceId;
        this.ecmaId = ecmaId;
        this.cipherProviderName = cipherProviderName;
    }
}