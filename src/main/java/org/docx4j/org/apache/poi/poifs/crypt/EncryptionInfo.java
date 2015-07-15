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

import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode.agile;
import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode.binaryRC4;
import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode.cryptoAPI;
import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode.standard;

import java.io.IOException;



//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

/**
 */
public class EncryptionInfo {
    private final int versionMajor;
    private final int versionMinor;
    private final int encryptionFlags;
    
    private final EncryptionHeader header;
    private final EncryptionVerifier verifier;
    private final Decryptor decryptor;
    private final Encryptor encryptor;

    /**
     * A flag that specifies whether CryptoAPI RC4 or ECMA-376 encryption
     * ECMA-376 is used. It MUST be 1 unless flagExternal is 1. If flagExternal is 1, it MUST be 0.
     */
    public static BitField flagCryptoAPI = BitFieldFactory.getInstance(0x04);

    /**
     * A value that MUST be 0 if document properties are encrypted.
     * The encryption of document properties is specified in section 2.3.5.4.
     */
    public static BitField flagDocProps = BitFieldFactory.getInstance(0x08);
    
    /**
     * A value that MUST be 1 if extensible encryption is used. If this value is 1,
     * the value of every other field in this structure MUST be 0.
     */
    public static BitField flagExternal = BitFieldFactory.getInstance(0x10);
    
    /**
     * A value that MUST be 1 if the protected content is an ECMA-376 document
     * ECMA-376. If the fAES bit is 1, the fCryptoAPI bit MUST also be 1.
     */
    public static BitField flagAES = BitFieldFactory.getInstance(0x20);
    
    
    /**
     * Opens for decryption
     */
    public EncryptionInfo(POIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    /**
     * Opens for decryption
     */
    public EncryptionInfo(OPOIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    /**
     * Opens for decryption
     */
    public EncryptionInfo(NPOIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }
    /**
     * Opens for decryption
     */
    public EncryptionInfo(DirectoryNode dir) throws IOException {
        this(dir.createDocumentInputStream("EncryptionInfo"), false);
    }

    public EncryptionInfo(LittleEndianInput dis, boolean isCryptoAPI) throws IOException {
        final EncryptionMode encryptionMode;
        versionMajor = dis.readShort();
        versionMinor = dis.readShort();

        if (!isCryptoAPI
            && versionMajor == binaryRC4.versionMajor
            && versionMinor == binaryRC4.versionMinor) {
            encryptionMode = binaryRC4;
            encryptionFlags = -1;
        } else if (!isCryptoAPI
            && versionMajor == agile.versionMajor
            && versionMinor == agile.versionMinor){
            encryptionMode = agile;
            encryptionFlags = dis.readInt();
        } else if (!isCryptoAPI
            && 2 <= versionMajor && versionMajor <= 4
            && versionMinor == standard.versionMinor) {
            encryptionMode = standard;
            encryptionFlags = dis.readInt();
        } else if (isCryptoAPI
            && 2 <= versionMajor && versionMajor <= 4
            && versionMinor == cryptoAPI.versionMinor) {
            encryptionMode = cryptoAPI;
            encryptionFlags = dis.readInt();
        } else {
            encryptionFlags = dis.readInt();
            throw new EncryptedDocumentException(
                "Unknown encryption: version major: "+versionMajor+
                " / version minor: "+versionMinor+
                " / fCrypto: "+flagCryptoAPI.isSet(encryptionFlags)+
                " / fExternal: "+flagExternal.isSet(encryptionFlags)+
                " / fDocProps: "+flagDocProps.isSet(encryptionFlags)+
                " / fAES: "+flagAES.isSet(encryptionFlags));
        }
        
        EncryptionInfoBuilder eib;
        try {
            eib = getBuilder(encryptionMode);
        } catch (Exception e) {
            throw new IOException(e);
        }

        eib.initialize(this, dis);
        header = eib.getHeader();
        verifier = eib.getVerifier();
        decryptor = eib.getDecryptor();
        encryptor = eib.getEncryptor();
    }
    
    /**
     * @deprecated Use {@link #EncryptionInfo(EncryptionMode)} (fs parameter no longer required)
     */
    @Deprecated
    public EncryptionInfo(POIFSFileSystem fs, EncryptionMode encryptionMode) {
        this(encryptionMode);
    }
     
    /**
     * @deprecated Use {@link #EncryptionInfo(EncryptionMode)} (fs parameter no longer required)
     */
    @Deprecated
    public EncryptionInfo(NPOIFSFileSystem fs, EncryptionMode encryptionMode) {
        this(encryptionMode);
    }
     
    /**
     * @deprecated Use {@link #EncryptionInfo(EncryptionMode)} (dir parameter no longer required)
     */
    @Deprecated
    public EncryptionInfo(DirectoryNode dir, EncryptionMode encryptionMode) {
        this(encryptionMode);
    }
    
    /**
     * @deprecated use {@link #EncryptionInfo(EncryptionMode, CipherAlgorithm, HashAlgorithm, int, int, ChainingMode)}
     */
    @Deprecated
    public EncryptionInfo(
        POIFSFileSystem fs
      , EncryptionMode encryptionMode
      , CipherAlgorithm cipherAlgorithm
      , HashAlgorithm hashAlgorithm
      , int keyBits
      , int blockSize
      , ChainingMode chainingMode
    ) {
        this(encryptionMode, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }
    
    /**
     * @deprecated use {@link #EncryptionInfo(EncryptionMode, CipherAlgorithm, HashAlgorithm, int, int, ChainingMode)}
     */
    @Deprecated
    public EncryptionInfo(
        NPOIFSFileSystem fs
      , EncryptionMode encryptionMode
      , CipherAlgorithm cipherAlgorithm
      , HashAlgorithm hashAlgorithm
      , int keyBits
      , int blockSize
      , ChainingMode chainingMode
    ) {
        this(encryptionMode, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }
        
    /**
     * @deprecated use {@link #EncryptionInfo(EncryptionMode, CipherAlgorithm, HashAlgorithm, int, int, ChainingMode)}
     */
    @Deprecated
    public EncryptionInfo(
          DirectoryNode dir
        , EncryptionMode encryptionMode
        , CipherAlgorithm cipherAlgorithm
        , HashAlgorithm hashAlgorithm
        , int keyBits
        , int blockSize
        , ChainingMode chainingMode
    ) {
        this(encryptionMode, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
    }        

    /**
     * Prepares for encryption, using the given Encryption Mode, and
     *  all other parameters as default.
     * @see #EncryptionInfo(EncryptionMode, CipherAlgorithm, HashAlgorithm, int, int, ChainingMode)
     */
    public EncryptionInfo(EncryptionMode encryptionMode) {
        this(encryptionMode, null, null, -1, -1, null);
    }
    
    /**
     * Constructs an EncryptionInfo from scratch
     *
     * @param encryptionMode see {@link EncryptionMode} for values, {@link EncryptionMode#cryptoAPI} is for
     *   internal use only, as it's record based
     * @param cipherAlgorithm
     * @param hashAlgorithm
     * @param keyBits
     * @param blockSize
     * @param chainingMode
     * 
     * @throws EncryptedDocumentException if the given parameters mismatch, e.g. only certain combinations
     *   of keyBits, blockSize are allowed for a given {@link CipherAlgorithm}
     */
    public EncryptionInfo(
            EncryptionMode encryptionMode
          , CipherAlgorithm cipherAlgorithm
          , HashAlgorithm hashAlgorithm
          , int keyBits
          , int blockSize
          , ChainingMode chainingMode
      ) {
        versionMajor = encryptionMode.versionMajor;
        versionMinor = encryptionMode.versionMinor;
        encryptionFlags = encryptionMode.encryptionFlags;

        EncryptionInfoBuilder eib;
        try {
            eib = getBuilder(encryptionMode);
        } catch (Exception e) {
            throw new EncryptedDocumentException(e);
        }
        
        eib.initialize(this, cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
        
        header = eib.getHeader();
        verifier = eib.getVerifier();
        decryptor = eib.getDecryptor();
        encryptor = eib.getEncryptor();
    }

    protected static EncryptionInfoBuilder getBuilder(EncryptionMode encryptionMode)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        EncryptionInfoBuilder eib;
        eib = (EncryptionInfoBuilder)cl.loadClass(encryptionMode.builder).newInstance();
        return eib;
    }
    
    public int getVersionMajor() {
        return versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public int getEncryptionFlags() {
        return encryptionFlags;
    }

    public EncryptionHeader getHeader() {
        return header;
    }

    public EncryptionVerifier getVerifier() {
        return verifier;
    }
    
    public Decryptor getDecryptor() {
        return decryptor;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }
}
