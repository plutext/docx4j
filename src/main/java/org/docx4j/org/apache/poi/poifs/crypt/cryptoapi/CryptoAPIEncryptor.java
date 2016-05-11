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

package org.docx4j.org.apache.poi.poifs.crypt.cryptoapi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;




//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.hpsf.DocumentSummaryInformation;
import org.docx4j.org.apache.poi.hpsf.PropertySetFactory;
import org.docx4j.org.apache.poi.hpsf.SummaryInformation;
import org.docx4j.org.apache.poi.hpsf.WritingNotSupportedException;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.DataSpaceMapUtils;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.Encryptor;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIDecryptor.StreamDescriptorEntry;
import org.docx4j.org.apache.poi.poifs.crypt.standard.EncryptionRecord;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.docx4j.org.apache.poi.util.StringUtil;

public class CryptoAPIEncryptor extends Encryptor {
    private final CryptoAPIEncryptionInfoBuilder builder;

    protected CryptoAPIEncryptor(CryptoAPIEncryptionInfoBuilder builder) {
        this.builder = builder;
    }

    public void confirmPassword(String password) {
        Random r = new SecureRandom();
        byte salt[] = new byte[16];
        byte verifier[] = new byte[16];
        r.nextBytes(salt);
        r.nextBytes(verifier);
        confirmPassword(password, null, null, verifier, salt, null);
    }

    public void confirmPassword(String password, byte keySpec[],
            byte keySalt[], byte verifier[], byte verifierSalt[],
            byte integritySalt[]) {
        assert(verifier != null && verifierSalt != null);
        CryptoAPIEncryptionVerifier ver = builder.getVerifier();
        ver.setSalt(verifierSalt);
        SecretKey skey = CryptoAPIDecryptor.generateSecretKey(password, ver);
        setSecretKey(skey);
        try {
            Cipher cipher = initCipherForBlock(null, 0);
            byte encryptedVerifier[] = new byte[verifier.length];
            cipher.update(verifier, 0, verifier.length, encryptedVerifier);
            ver.setEncryptedVerifier(encryptedVerifier);
            HashAlgorithm hashAlgo = ver.getHashAlgorithm();
            MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
            byte calcVerifierHash[] = hashAlg.digest(verifier);
            byte encryptedVerifierHash[] = cipher.doFinal(calcVerifierHash);
            ver.setEncryptedVerifierHash(encryptedVerifierHash);
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException("Password confirmation failed", e);
        }
    }

    /**
     * Initializes a cipher object for a given block index for encryption
     *
     * @param cipher may be null, otherwise the given instance is reset to the new block index
     * @param block the block index, e.g. the persist/slide id (hslf)
     * @return a new cipher object, if cipher was null, otherwise the reinitialized cipher
     * @throws GeneralSecurityException
     */
    public Cipher initCipherForBlock(Cipher cipher, int block)
    throws GeneralSecurityException {
        return CryptoAPIDecryptor.initCipherForBlock(cipher, block, builder, getSecretKey(), Cipher.ENCRYPT_MODE);
    }    
    
    /**
     * Encrypt the Document-/SummaryInformation and other optionally streams.
     * Opposed to other crypto modes, cryptoapi is record based and can't be used
     * to stream-encrypt a whole file
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd943321(v=office.12).aspx">2.3.5.4 RC4 CryptoAPI Encrypted Summary Stream</a>
     */
    public OutputStream getDataStream(DirectoryNode dir)
    throws IOException, GeneralSecurityException {
        CipherByteArrayOutputStream bos = new CipherByteArrayOutputStream();
        byte buf[] = new byte[8];
        
        bos.write(buf, 0, 8); // skip header
        String entryNames[] = {
            SummaryInformation.DEFAULT_STREAM_NAME,
            DocumentSummaryInformation.DEFAULT_STREAM_NAME
        };
        
        List<StreamDescriptorEntry> descList = new ArrayList<StreamDescriptorEntry>();

        int block = 0;
        for (String entryName : entryNames) {
            if (!dir.hasEntry(entryName)) continue;
            StreamDescriptorEntry descEntry = new StreamDescriptorEntry();
            descEntry.block = block;
            descEntry.streamOffset = bos.size();
            descEntry.streamName = entryName;
            descEntry.flags = StreamDescriptorEntry.flagStream.setValue(0, 1);
            descEntry.reserved2 = 0;
            
            bos.setBlock(block);
            DocumentInputStream dis = dir.createDocumentInputStream(entryName);
            IOUtils.copy(dis, bos);
            dis.close();
            
            descEntry.streamSize = bos.size() - descEntry.streamOffset;
            descList.add(descEntry);
            
            dir.getEntry(entryName).delete();
            
            block++;
        }
        
        int streamDescriptorArrayOffset = bos.size();
        
        bos.setBlock(0);
        LittleEndian.putUInt(buf, 0, descList.size());
        bos.write(buf, 0, 4);
        
        for (StreamDescriptorEntry sde : descList) {
            LittleEndian.putUInt(buf, 0, sde.streamOffset);
            bos.write(buf, 0, 4);
            LittleEndian.putUInt(buf, 0, sde.streamSize);
            bos.write(buf, 0, 4);
            LittleEndian.putUShort(buf, 0, sde.block);
            bos.write(buf, 0, 2);
            LittleEndian.putUByte(buf, 0, (short)sde.streamName.length());
            bos.write(buf, 0, 1);
            LittleEndian.putUByte(buf, 0, (short)sde.flags);
            bos.write(buf, 0, 1);
            LittleEndian.putUInt(buf, 0, sde.reserved2);
            bos.write(buf, 0, 4);
            byte nameBytes[] = StringUtil.getToUnicodeLE(sde.streamName);
            bos.write(nameBytes, 0, nameBytes.length);
            LittleEndian.putShort(buf, 0, (short)0); // null-termination
            bos.write(buf, 0, 2);
        }
        
        int savedSize = bos.size();
        int streamDescriptorArraySize = savedSize - streamDescriptorArrayOffset;
        LittleEndian.putUInt(buf, 0, streamDescriptorArrayOffset);
        LittleEndian.putUInt(buf, 4, streamDescriptorArraySize);

        bos.reset();
        bos.setBlock(0);
        bos.write(buf, 0, 8);
        bos.setSize(savedSize);
        
        dir.createDocument("EncryptedSummary", new ByteArrayInputStream(bos.getBuf(), 0, savedSize));
        DocumentSummaryInformation dsi = PropertySetFactory.newDocumentSummaryInformation();
        
        try {
            dsi.write(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        } catch (WritingNotSupportedException e) {
            throw new IOException(e);
        }
        
        return bos;
    }

    protected int getKeySizeInBytes() {
        return builder.getHeader().getKeySize() / 8;
    }

    protected void createEncryptionInfoEntry(DirectoryNode dir) throws IOException {
        DataSpaceMapUtils.addDefaultDataSpace(dir);
        final EncryptionInfo info = builder.getEncryptionInfo();
        final CryptoAPIEncryptionHeader header = builder.getHeader();
        final CryptoAPIEncryptionVerifier verifier = builder.getVerifier();
        EncryptionRecord er = new EncryptionRecord() {
            public void write(LittleEndianByteArrayOutputStream bos) {
                bos.writeShort(info.getVersionMajor());
                bos.writeShort(info.getVersionMinor());
                header.write(bos);
                verifier.write(bos);
            }
        };
        DataSpaceMapUtils.createEncryptionEntry(dir, "EncryptionInfo", er);
    }

    private class CipherByteArrayOutputStream extends ByteArrayOutputStream {
        Cipher cipher;
        byte oneByte[] = { 0 };

        public CipherByteArrayOutputStream() throws GeneralSecurityException {
            setBlock(0);
        }
        
        public byte[] getBuf() {
            return buf;
        }
        
        public void setSize(int count) {
            this.count = count;
        }
        
        public void setBlock(int block) throws GeneralSecurityException {
            cipher = initCipherForBlock(cipher, block);
        }
        
        public void write(int b) {
            try {
                oneByte[0] = (byte)b;
                cipher.update(oneByte, 0, 1, oneByte, 0);
                super.write(oneByte);
            } catch (Exception e) {
                throw new EncryptedDocumentException(e);
            }
        }

        public void write(byte[] b, int off, int len) {
            try {
                cipher.update(b, off, len, b, off);
                super.write(b, off, len);
            } catch (Exception e) {
                throw new EncryptedDocumentException(e);
            }
        }

    }
}
