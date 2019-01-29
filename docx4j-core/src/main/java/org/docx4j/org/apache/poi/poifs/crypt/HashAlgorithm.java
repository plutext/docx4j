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

public enum HashAlgorithm {
    none     (         "", 0x0000,           "",  0,               "", false),
    sha1     (    "SHA-1", 0x8004,       "SHA1", 20,       "HmacSHA1", false),
    sha256   (  "SHA-256", 0x800C,     "SHA256", 32,     "HmacSHA256", false),
    sha384   (  "SHA-384", 0x800D,     "SHA384", 48,     "HmacSHA384", false),
    sha512   (  "SHA-512", 0x800E,     "SHA512", 64,     "HmacSHA512", false),
    /* only for agile encryption */
    md5      (      "MD5",     -1,        "MD5", 16,        "HmacMD5", false),
    // although sunjc2 supports md2, hmac-md2 is only supported by bouncycastle
    md2      (      "MD2",     -1,        "MD2", 16,       "Hmac-MD2", true),
    md4      (      "MD4",     -1,        "MD4", 16,       "Hmac-MD4", true),
    ripemd128("RipeMD128",     -1, "RIPEMD-128", 16, "HMac-RipeMD128", true),
    ripemd160("RipeMD160",     -1, "RIPEMD-160", 20, "HMac-RipeMD160", true),
    whirlpool("Whirlpool",     -1,  "WHIRLPOOL", 64, "HMac-Whirlpool", true),
    // only for xml signing
    sha224   (  "SHA-224",     -1,     "SHA224", 28,     "HmacSHA224", true);
    ;

    public final String jceId;
    public final int ecmaId;
    public final String ecmaString;
    public final int hashSize;
    public final String jceHmacId;
    public final boolean needsBouncyCastle;
    
    HashAlgorithm(String jceId, int ecmaId, String ecmaString, int hashSize, String jceHmacId, boolean needsBouncyCastle) {
        this.jceId = jceId;
        this.ecmaId = ecmaId;
        this.ecmaString = ecmaString;
        this.hashSize = hashSize;
        this.jceHmacId = jceHmacId;
        this.needsBouncyCastle = needsBouncyCastle;
    }
    
    public static HashAlgorithm fromEcmaId(int ecmaId) {
        for (HashAlgorithm ha : values()) {
            if (ha.ecmaId == ecmaId) return ha;
        }
        throw new EncryptedDocumentException("hash algorithm not found");
    }    
    
    public static HashAlgorithm fromEcmaId(String ecmaString) {
        for (HashAlgorithm ha : values()) {
            if (ha.ecmaString.equals(ecmaString)) return ha;
        }
        throw new EncryptedDocumentException("hash algorithm not found '" + ecmaString + "'");
    }
    
    public static HashAlgorithm fromString(String string) {
        for (HashAlgorithm ha : values()) {
            if (ha.ecmaString.equalsIgnoreCase(string) || ha.jceId.equalsIgnoreCase(string)) return ha;
        }
        throw new EncryptedDocumentException("hash algorithm not found '" + string + "'");
    }
}