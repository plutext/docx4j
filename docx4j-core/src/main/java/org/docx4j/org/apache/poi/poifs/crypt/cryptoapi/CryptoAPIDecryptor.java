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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.input.BoundedInputStream;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChunkedCipherInputStream;
import org.docx4j.org.apache.poi.poifs.crypt.CryptoFunctions;
import org.docx4j.org.apache.poi.poifs.crypt.Decryptor;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionHeader;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.StringUtil;

public class CryptoAPIDecryptor extends Decryptor {

    private long length = -1L;
    private int chunkSize = -1;

    static class StreamDescriptorEntry {
        static final BitField flagStream = BitFieldFactory.getInstance(1);

        int streamOffset;
        int streamSize;
        int block;
        int flags;
        int reserved2;
        String streamName;
    }

    protected CryptoAPIDecryptor() {}

    protected CryptoAPIDecryptor(CryptoAPIDecryptor other) {
        super(other);
        length = other.length;
        chunkSize = other.chunkSize;
    }

    @Override
    public boolean verifyPassword(String password) {
        EncryptionVerifier ver = getEncryptionInfo().getVerifier();
        SecretKey skey = generateSecretKey(password, ver);
        try {
            Cipher cipher = initCipherForBlock(null, 0, getEncryptionInfo(), skey, Cipher.DECRYPT_MODE);
            byte[] encryptedVerifier = ver.getEncryptedVerifier();
            byte[] verifier = new byte[encryptedVerifier.length];
            cipher.update(encryptedVerifier, 0, encryptedVerifier.length, verifier);
            setVerifier(verifier);
            byte[] encryptedVerifierHash = ver.getEncryptedVerifierHash();
            byte[] verifierHash = cipher.doFinal(encryptedVerifierHash);
            HashAlgorithm hashAlgo = ver.getHashAlgorithm();
            MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
            byte[] calcVerifierHash = hashAlg.digest(verifier);
            if (Arrays.equals(calcVerifierHash, verifierHash)) {
                setSecretKey(skey);
                return true;
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException(e);
        }
        return false;
    }

    @Override
    public Cipher initCipherForBlock(Cipher cipher, int block)
    throws GeneralSecurityException {
        EncryptionInfo ei = getEncryptionInfo();
        SecretKey sk = getSecretKey();
        return initCipherForBlock(cipher, block, ei, sk, Cipher.DECRYPT_MODE);
    }

    protected static Cipher initCipherForBlock(Cipher cipher, int block,
        EncryptionInfo encryptionInfo, SecretKey skey, int encryptMode)
    throws GeneralSecurityException {
        EncryptionVerifier ver = encryptionInfo.getVerifier();
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        byte[] blockKey = new byte[4];
        LittleEndian.putUInt(blockKey, 0, block);
        MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
        hashAlg.update(skey.getEncoded());
        byte[] encKey = hashAlg.digest(blockKey);
        EncryptionHeader header = encryptionInfo.getHeader();
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
        if (password == null) {
            throw new IllegalArgumentException("Did not receive a password");
        }
        if (password.length() > 255) {
            password = password.substring(0, 255);
        }
        HashAlgorithm hashAlgo = ver.getHashAlgorithm();
        MessageDigest hashAlg = CryptoFunctions.getMessageDigest(hashAlgo);
        hashAlg.update(ver.getSalt());
        byte[] hash = hashAlg.digest(StringUtil.getToUnicodeLE(password));
        return new SecretKeySpec(hash, ver.getCipherAlgorithm().jceId);
    }

    @Override
    public ChunkedCipherInputStream getDataStream(DirectoryNode dir)
    throws IOException, GeneralSecurityException {
        throw new IOException("not supported");
    }

    @Override
    public ChunkedCipherInputStream getDataStream(InputStream stream, int size, int initialPos)
            throws IOException, GeneralSecurityException {
        return new CryptoAPICipherInputStream(stream, size, initialPos);
    }

    /**
     * Decrypt the Document-/SummaryInformation and other optionally streams.
     * Opposed to other crypto modes, cryptoapi is record based and can't be used
     * to stream-decrypt a whole file.<p>
     *
     * Summary entries are only encrypted within cryptoapi encrypted files.
     * Binary RC4 encrypted files use non-encrypted/default property sets
     *
     * @param root root directory node of the OLE file containing the encrypted properties
     * @param encryptedStream name of the encrypted stream -
     *      "encryption" for HSSF/HWPF, "encryptedStream" (or encryptedSummary?) for HSLF
     *
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd943321(v=office.12).aspx">2.3.5.4 RC4 CryptoAPI Encrypted Summary Stream</a>
     */
    public POIFSFileSystem getSummaryEntries(DirectoryNode root, String encryptedStream)
    throws IOException, GeneralSecurityException {
        POIFSFileSystem fsOut = null;
        try (
                DocumentInputStream dis = root.createDocumentInputStream(root.getEntryCaseInsensitive(encryptedStream));
                CryptoAPIDocumentInputStream sbis = new CryptoAPIDocumentInputStream(this, IOUtils.toByteArray(dis));
                LittleEndianInputStream leis = new LittleEndianInputStream(sbis)
        ) {
            int streamDescriptorArrayOffset = (int) leis.readUInt();
            /* int streamDescriptorArraySize = (int) */ leis.readUInt();
            long skipN = streamDescriptorArrayOffset - 8L;
            if (sbis.skip(skipN) < skipN) {
                throw new EOFException("buffer underrun");
            }
            sbis.setBlock(0);
            int encryptedStreamDescriptorCount = (int) leis.readUInt();
            StreamDescriptorEntry[] entries = new StreamDescriptorEntry[encryptedStreamDescriptorCount];
            for (int i = 0; i < encryptedStreamDescriptorCount; i++) {
                StreamDescriptorEntry entry = new StreamDescriptorEntry();
                entries[i] = entry;
                entry.streamOffset = (int) leis.readUInt();
                entry.streamSize = (int) leis.readUInt();
                entry.block = leis.readUShort();
                int nameSize = leis.readUByte();
                entry.flags = leis.readUByte();
                // boolean isStream = StreamDescriptorEntry.flagStream.isSet(entry.flags);
                entry.reserved2 = leis.readInt();
                entry.streamName = StringUtil.readUnicodeLE(leis, nameSize);
                leis.readShort();
                assert(entry.streamName.length() == nameSize);
            }

            fsOut = new POIFSFileSystem(); // NOSONAR
            for (StreamDescriptorEntry entry : entries) {
                sbis.seek(entry.streamOffset);
                sbis.setBlock(entry.block);
                try (InputStream is = BoundedInputStream.builder().setInputStream(sbis).setMaxCount(entry.streamSize).get()) {
                    fsOut.createDocument(is, entry.streamName);
                }
            }
        } catch (Exception e) {
            IOUtils.closeQuietly(fsOut);
            if (e instanceof GeneralSecurityException) {
                throw (GeneralSecurityException)e;
            } else if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                throw new IOException("summary entries can't be read", e);
            }
        }
        return fsOut;
    }

    /**
     * @return the length of the stream returned by {@link #getDataStream(DirectoryNode)}
     */
    @Override
    public long getLength() {
        if (length == -1L) {
            throw new IllegalStateException("Decryptor.getDataStream() was not called");
        }
        return length;
    }

    @Override
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public CryptoAPIDecryptor copy() {
        return new CryptoAPIDecryptor(this);
    }

    private class CryptoAPICipherInputStream extends ChunkedCipherInputStream {

        @Override
        protected Cipher initCipherForBlock(Cipher existing, int block)
                throws GeneralSecurityException {
            return CryptoAPIDecryptor.this.initCipherForBlock(existing, block);
        }

        public CryptoAPICipherInputStream(InputStream stream, long size, int initialPos)
                throws GeneralSecurityException {
            super(stream, size, chunkSize, initialPos);
        }
    }
}
