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

//import org.docx4j.org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;

/**
 * Office supports various encryption modes.
 * The encryption is either based on the whole container ({@link #agile}, {@link #standard} or {@link #binaryRC4})
 * or record based ({@link #cryptoAPI}). The record based encryption can't be accessed directly, but will be
 * invoked by using the {@link Biff8EncryptionKey#setCurrentUserPassword(String)} before saving the document.
 */
public enum EncryptionMode {
    /* @see <a href="http://msdn.microsoft.com/en-us/library/dd907466(v=office.12).aspx">2.3.6 Office Binary Document RC4 Encryption</a> */
    binaryRC4("org.docx4j.org.apache.poi.poifs.crypt.binaryrc4.BinaryRC4EncryptionInfoBuilder", 1, 1, 0x0),
    /* @see <a href="http://msdn.microsoft.com/en-us/library/dd905225(v=office.12).aspx">2.3.5 Office Binary Document RC4 CryptoAPI Encryption</a> */
    cryptoAPI("org.docx4j.org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptionInfoBuilder", 4, 2, 0x04),
    /* @see <a href="http://msdn.microsoft.com/en-us/library/dd906097(v=office.12).aspx">2.3.4.5 \EncryptionInfo Stream (Standard Encryption)</a> */
    standard("org.docx4j.org.apache.poi.poifs.crypt.standard.StandardEncryptionInfoBuilder", 4, 2, 0x24),
    /* @see <a href="http://msdn.microsoft.com/en-us/library/dd925810(v=office.12).aspx">2.3.4.10 \EncryptionInfo Stream (Agile Encryption)</a> */
    agile("org.docx4j.org.apache.poi.poifs.crypt.agile.AgileEncryptionInfoBuilder", 4, 4, 0x40)
    ;
    
    public final String builder;
    public final int versionMajor;
    public final int versionMinor;
    public final int encryptionFlags;
    
    EncryptionMode(String builder, int versionMajor, int versionMinor, int encryptionFlags) {
        this.builder = builder;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.encryptionFlags = encryptionFlags;
    }
}
