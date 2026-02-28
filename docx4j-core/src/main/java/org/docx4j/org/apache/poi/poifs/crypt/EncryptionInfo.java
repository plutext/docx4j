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
import static org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode.xor;
import static org.docx4j.org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.LittleEndianInput;

/**
 * Wrapper for the EncryptionInfo node of encrypted documents
 */
public class EncryptionInfo implements GenericRecord {

    /**
     * Document entry name for encryption info xml descriptor
     */
    public static final String ENCRYPTION_INFO_ENTRY = "EncryptionInfo";

    /**
     * A flag that specifies whether CryptoAPI RC4 or ECMA-376 encryption
     * ECMA-376 is used. It MUST be 1 unless flagExternal is 1. If flagExternal is 1, it MUST be 0.
     */
    public static final BitField flagCryptoAPI = BitFieldFactory.getInstance(0x04);

    /**
     * A value that MUST be 0 if document properties are encrypted.
     * The encryption of document properties is specified in section 2.3.5.4.
     */
    @SuppressWarnings("WeakerAccess")
    public static final BitField flagDocProps = BitFieldFactory.getInstance(0x08);

    /**
     * A value that MUST be 1 if extensible encryption is used. If this value is 1,
     * the value of every other field in this structure MUST be 0.
     */
    @SuppressWarnings("WeakerAccess")
    public static final BitField flagExternal = BitFieldFactory.getInstance(0x10);

    /**
     * A value that MUST be 1 if the protected content is an ECMA-376 document
     * ECMA-376. If the fAES bit is 1, the fCryptoAPI bit MUST also be 1.
     */
    public static final BitField flagAES = BitFieldFactory.getInstance(0x20);

    private static final int[] FLAGS_MASKS = {
        0x04, 0x08, 0x10, 0x20
    };

    private static final String[] FLAGS_NAMES = {
        "CRYPTO_API", "DOC_PROPS", "EXTERNAL", "AES"
    };

    private final EncryptionMode encryptionMode;
    private final int versionMajor;
    private final int versionMinor;
    private final int encryptionFlags;

    private EncryptionHeader header;
    private EncryptionVerifier verifier;
    private Decryptor decryptor;
    private Encryptor encryptor;

    /**
     * Opens for decryption
     */
    public EncryptionInfo(POIFSFileSystem fs) throws IOException {
       this(fs.getRoot());
    }

    /**
     * Opens for decryption
     */
    public EncryptionInfo(DirectoryNode dir) throws IOException {
        this(dir.createDocumentInputStream(ENCRYPTION_INFO_ENTRY), null);
    }

    public EncryptionInfo(LittleEndianInput dis, EncryptionMode preferredEncryptionMode) throws IOException {
        if (preferredEncryptionMode == xor) {
            versionMajor = xor.versionMajor;
            versionMinor = xor.versionMinor;
        } else {
            versionMajor = dis.readUShort();
            versionMinor = dis.readUShort();
        }

        if (versionMajor == xor.versionMajor && versionMinor == xor.versionMinor) {
            encryptionMode = xor;
            encryptionFlags = -1;
        } else if (versionMajor == binaryRC4.versionMajor && versionMinor == binaryRC4.versionMinor) {
            encryptionMode = binaryRC4;
            encryptionFlags = -1;
        } else if (2 <= versionMajor && versionMajor <= 4 && versionMinor == 2) {
            encryptionFlags = dis.readInt();
            encryptionMode = (preferredEncryptionMode == cryptoAPI || !flagAES.isSet(encryptionFlags)) ? cryptoAPI : standard;
        } else if (versionMajor == agile.versionMajor && versionMinor == agile.versionMinor){
            encryptionMode = agile;
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
     *   internal use only, as its record based
     * @param cipherAlgorithm the cipher algorithm
     * @param hashAlgorithm the hash algorithm
     * @param keyBits the bit count of the key
     * @param blockSize the size of a cipher block
     * @param chainingMode the chaining mode
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
        this.encryptionMode = encryptionMode;
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
    }

    public EncryptionInfo(EncryptionInfo other) {
        encryptionMode = other.encryptionMode;
        versionMajor = other.versionMajor;
        versionMinor = other.versionMinor;
        encryptionFlags = other.encryptionFlags;

        header = (other.header == null) ? null : other.header.copy();
        verifier = (other.verifier == null) ? null : other.verifier.copy();
        if (other.decryptor != null) {
            decryptor = other.decryptor.copy();
            decryptor.setEncryptionInfo(this);
        }
        if (other.encryptor != null) {
            encryptor = other.encryptor.copy();
            encryptor.setEncryptionInfo(this);
        }
    }

    /**
     * Create the builder instance
     *
     * @param encryptionMode the encryption mode
     * @return an encryption info builder
     */
    private static EncryptionInfoBuilder getBuilder(EncryptionMode encryptionMode) {
        return encryptionMode.builder.get();
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

    public void setHeader(EncryptionHeader header) {
        this.header = header;
    }

    public void setVerifier(EncryptionVerifier verifier) {
        this.verifier = verifier;
    }

    public void setDecryptor(Decryptor decryptor) {
        this.decryptor = decryptor;
    }

    public void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    public EncryptionMode getEncryptionMode() {
        return encryptionMode;
    }

    /**
     * @return true, if Document Summary / Summary are encrypted and stored in the {@code EncryptedStream} stream,
     * otherwise the Summaries aren't encrypted and located in their usual streams
     */
    public boolean isDocPropsEncrypted() {
        return !flagDocProps.isSet(getEncryptionFlags());
    }

    public EncryptionInfo copy()  {
        return new EncryptionInfo(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("encryptionMode", this::getEncryptionMode);
        m.put("versionMajor", this::getVersionMajor);
        m.put("versionMinor", this::getVersionMinor);
        m.put("encryptionFlags", getBitsAsString(this::getEncryptionFlags, FLAGS_MASKS, FLAGS_NAMES));
        m.put("header", this::getHeader);
        m.put("verifier", this::getVerifier);
        m.put("decryptor", this::getDecryptor);
        m.put("encryptor", this::getEncryptor);
        return Collections.unmodifiableMap(m);
    }
}