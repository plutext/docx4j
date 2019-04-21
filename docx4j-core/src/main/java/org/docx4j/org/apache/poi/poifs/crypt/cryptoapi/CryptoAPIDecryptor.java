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
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

//import org.docx4j.org.apache.poi.EncryptedDocumentException;


import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentNode;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.BoundedInputStream;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.StringUtil;

public class CryptoAPIDecryptor extends Decryptor {

    private long _length;
    
    private class SeekableByteArrayInputStream extends ByteArrayInputStream {
        Cipher cipher;
        byte oneByte[] = { 0 };
        
        public void seek(int pos) {
            if (pos > count) {
                throw new ArrayIndexOutOfBoundsException(pos);
            }
            
            this.pos = pos;
            mark = pos;
        }

        public void setBlock(int block) throws GeneralSecurityException {
            cipher = initCipherForBlock(cipher, block);
        }

        public synchronized int read() {
            int ch = super.read();
            if (ch == -1) return -1;
            oneByte[0] = (byte) ch;
            try {
                cipher.update(oneByte, 0, 1, oneByte);
            } catch (ShortBufferException e) {
                throw new EncryptedDocumentException(e);
            }
            return oneByte[0];
        }

        public synchronized int read(byte b[], int off, int len) {
            int readLen = super.read(b, off, len);
            if (readLen ==-1) return -1;
            try {
                cipher.update(b, off, readLen, b, off);
            } catch (ShortBufferException e) {
                throw new EncryptedDocumentException(e);
            }
            return readLen;
        }

        public SeekableByteArrayInputStream(byte buf[])
        throws GeneralSecurityException {
            super(buf);
            cipher = initCipherForBlock(null, 0);
        }
    }

    static class StreamDescriptorEntry {
        static BitField flagStream = BitFieldFactory.getInstance(1);
        
        int streamOffset;
        int streamSize;
        int block;
        int flags;
        int reserved2;
        String streamName;
    }

    protected CryptoAPIDecryptor(CryptoAPIEncryptionInfoBuilder builder) {
        super(builder);
        _length = -1L;
    }

    public boolean verifyPassword(String password) {
        EncryptionVerifier ver = builder.getVerifier();
        SecretKey skey = generateSecretKey(password, ver);
        try {
            Cipher cipher = initCipherForBlock(null, 0, builder, skey, Cipher.DECRYPT_MODE);
            byte encryptedVerifier[] = ver.getEncryptedVerifier();
            byte verifier[] = new byte[encryptedVerifier.length];
            cipher.update(encryptedVerifier, 0, encryptedVerifier.length, verifier);
            setVerifier(verifier);
            byte encryptedVerifierHash[] = ver.getEncryptedVerifierHash();
            byte verifierHash[] = cipher.doFinal(encryptedVerifierHash);
            HashAlgorithm hashAlgo = ver.getHashAlgorithm();
            MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
            byte calcVerifierHash[] = hashAlg.digest(verifier);
            if (Arrays.equals(calcVerifierHash, verifierHash)) {
                setSecretKey(skey);
                return true;
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
        return false;
    }

    /**
     * Initializes a cipher object for a given block index for decryption
     *
     * @param cipher may be null, otherwise the given instance is reset to the new block index
     * @param block the block index, e.g. the persist/slide id (hslf)
     * @return a new cipher object, if cipher was null, otherwise the reinitialized cipher
     * @throws GeneralSecurityException
     */
    public Cipher initCipherForBlock(Cipher cipher, int block)
    throws GeneralSecurityException {
        return initCipherForBlock(cipher, block, builder, getSecretKey(), Cipher.DECRYPT_MODE);
    }

    protected static Cipher initCipherForBlock(Cipher cipher, int block,
        EncryptionInfoBuilder builder, SecretKey skey, int encryptMode)
    throws GeneralSecurityException {
        EncryptionVerifier ver = builder.getVerifier();
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        byte blockKey[] = new byte[4];
        LittleEndian.putUInt(blockKey, 0, block);
        MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
        hashAlg.update(skey.getEncoded());
        byte encKey[] = hashAlg.digest(blockKey);
        EncryptionHeader header = builder.getHeader();
        int keyBits = header.getKeySize();
        encKey = CryptoFunctions.getBlock0(encKey, keyBits / 8);
        if (keyBits == 40) {
            encKey = CryptoFunctions.getBlock0(encKey, 16);
        }
        SecretKey key = new SecretKeySpec(encKey, skey.getAlgorithm());
        if (cipher == null) {
            cipher = CryptoFunctions.getCipher(key, header.getCipherAlgorithm(), null, null, encryptMode);
        } else {
            cipher.init(encryptMode, key);
        }
        return cipher;
    }

    protected static SecretKey generateSecretKey(String password, EncryptionVerifier ver) {
        if (password.length() > 255) {
            password = password.substring(0, 255);
        }
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
        hashAlg.update(ver.getSalt());
        byte hash[] = hashAlg.digest(StringUtil.getToUnicodeLE(password));
        SecretKey skey = new SecretKeySpec(hash, ver.getCipherAlgorithm().jceId);
        return skey;
    }

    /**
     * Decrypt the Document-/SummaryInformation and other optionally streams.
     * Opposed to other crypto modes, cryptoapi is record based and can't be used
     * to stream-decrypt a whole file
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd943321(v=office.12).aspx">2.3.5.4 RC4 CryptoAPI Encrypted Summary Stream</a>
     */
    @SuppressWarnings("unused")
    public InputStream getDataStream(DirectoryNode dir)
    throws IOException, GeneralSecurityException {
        NPOIFSFileSystem fsOut = new NPOIFSFileSystem();
        DocumentNode es = (DocumentNode) dir.getEntry("EncryptedSummary");
        DocumentInputStream dis = dir.createDocumentInputStream(es);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(dis, bos);
        dis.close();
        SeekableByteArrayInputStream sbis = new SeekableByteArrayInputStream(bos.toByteArray());
        LittleEndianInputStream leis = new LittleEndianInputStream(sbis);
        int streamDescriptorArrayOffset = (int) leis.readUInt();
        int streamDescriptorArraySize = (int) leis.readUInt();
        sbis.skip(streamDescriptorArrayOffset - 8);
        sbis.setBlock(0);
        int encryptedStreamDescriptorCount = (int) leis.readUInt();
        StreamDescriptorEntry entries[] = new StreamDescriptorEntry[encryptedStreamDescriptorCount];
        for (int i = 0; i < encryptedStreamDescriptorCount; i++) {
            StreamDescriptorEntry entry = new StreamDescriptorEntry();
            entries[i] = entry;
            entry.streamOffset = (int) leis.readUInt();
            entry.streamSize = (int) leis.readUInt();
            entry.block = leis.readUShort();
            int nameSize = leis.readUByte();
            entry.flags = leis.readUByte();
            boolean isStream = StreamDescriptorEntry.flagStream.isSet(entry.flags);
            entry.reserved2 = leis.readInt();
            entry.streamName = StringUtil.readUnicodeLE(leis, nameSize);
            leis.readShort();
            assert(entry.streamName.length() == nameSize);
        }

        for (StreamDescriptorEntry entry : entries) {
            sbis.seek(entry.streamOffset);
            sbis.setBlock(entry.block);
            InputStream is = new BoundedInputStream(sbis, entry.streamSize);
            fsOut.createDocument(is, entry.streamName);
        }

        leis.close();
        sbis = null;
        bos.reset();
        fsOut.writeFilesystem(bos);
        fsOut.close();
        _length = bos.size();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        return bis;
    }

    /**
     * @return the length of the stream returned by {@link #getDataStream(DirectoryNode)}
     */
    public long getLength() {
        if (_length == -1L) {
            throw new IllegalStateException("Decryptor.getDataStream() was not called");
        }
        return _length;
    }
}
