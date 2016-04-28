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


/**
 * Reads and processes OOXML Encryption Headers
 * The constants are largely based on ZIP constants.
 */
public abstract class EncryptionHeader {
    public static final int ALGORITHM_RC4 = CipherAlgorithm.rc4.ecmaId;
    public static final int ALGORITHM_AES_128 = CipherAlgorithm.aes128.ecmaId;
    public static final int ALGORITHM_AES_192 = CipherAlgorithm.aes192.ecmaId;
    public static final int ALGORITHM_AES_256 = CipherAlgorithm.aes256.ecmaId;
    
    public static final int HASH_NONE   = HashAlgorithm.none.ecmaId;
    public static final int HASH_SHA1   = HashAlgorithm.sha1.ecmaId;
    public static final int HASH_SHA256 = HashAlgorithm.sha256.ecmaId;
    public static final int HASH_SHA384 = HashAlgorithm.sha384.ecmaId;
    public static final int HASH_SHA512 = HashAlgorithm.sha512.ecmaId;

    public static final int PROVIDER_RC4 = CipherProvider.rc4.ecmaId;
    public static final int PROVIDER_AES = CipherProvider.aes.ecmaId;

    public static final int MODE_ECB = ChainingMode.ecb.ecmaId;
    public static final int MODE_CBC = ChainingMode.cbc.ecmaId;
    public static final int MODE_CFB = ChainingMode.cfb.ecmaId;
    
    private int flags;
    private int sizeExtra;
    private CipherAlgorithm cipherAlgorithm;
    private HashAlgorithm hashAlgorithm;
    private int keyBits;
    private int blockSize;
    private CipherProvider providerType;
    private ChainingMode chainingMode;
    private byte[] keySalt;
    private String cspName;
    
    protected EncryptionHeader() {}

    /**
     * @deprecated use getChainingMode().ecmaId
     */
    public int getCipherMode() {
        return chainingMode.ecmaId;
    }
    
    public ChainingMode getChainingMode() {
        return chainingMode;
    }
    
    protected void setChainingMode(ChainingMode chainingMode) {
        this.chainingMode = chainingMode;
    }

    public int getFlags() {
        return flags;
    }
    
    protected void setFlags(int flags) {
        this.flags = flags;
    }

    public int getSizeExtra() {
        return sizeExtra;
    }
    
    protected void setSizeExtra(int sizeExtra) {
        this.sizeExtra = sizeExtra;
    }

    /**
     * @deprecated use getCipherAlgorithm()
     */
    public int getAlgorithm() {
        return cipherAlgorithm.ecmaId;
    }

    public CipherAlgorithm getCipherAlgorithm() {
        return cipherAlgorithm;
    }
    
    protected void setCipherAlgorithm(CipherAlgorithm cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }
    
    /**
     * @deprecated use getHashAlgorithmEx()
     */
    public int getHashAlgorithm() {
        return hashAlgorithm.ecmaId;
    }
    
    public HashAlgorithm getHashAlgorithmEx() {
        return hashAlgorithm;
    }
    
    protected void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public int getKeySize() {
        return keyBits;
    }
    
    protected void setKeySize(int keyBits) {
        this.keyBits = keyBits;
    }

    public int getBlockSize() {
    	return blockSize;
    }
    
    protected void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    
    public byte[] getKeySalt() {
        return keySalt;
    }
    
    protected void setKeySalt(byte salt[]) {
        this.keySalt = salt;
    }

    /**
     * @deprecated use getCipherProvider()
     */
    public int getProviderType() {
        return providerType.ecmaId;
    }

    public CipherProvider getCipherProvider() {
        return providerType;
    }    

    protected void setCipherProvider(CipherProvider providerType) {
        this.providerType = providerType;
    }
    
    public String getCspName() {
        return cspName;
    }
    
    protected void setCspName(String cspName) {
        this.cspName = cspName;
    }
}
