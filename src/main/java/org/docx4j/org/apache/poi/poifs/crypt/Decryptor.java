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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;



//import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode;
import org.docx4j.org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;

public abstract class Decryptor {
    public static final String DEFAULT_PASSWORD="VelvetSweatshop";
    public static final String DEFAULT_POIFS_ENTRY="EncryptedPackage";
    
    protected final EncryptionInfoBuilder builder;
    private SecretKey secretKey;
    private byte[] verifier, integrityHmacKey, integrityHmacValue;

    protected Decryptor(EncryptionInfoBuilder builder) {
        this.builder = builder;
    }
    
    /**
     * Return a stream with decrypted data.
     * <p>
     * Use {@link #getLength()} to get the size of that data that can be safely read from the stream.
     * Just reading to the end of the input stream is not sufficient because there are
     * normally padding bytes that must be discarded
     * </p>
     *
     * @param dir the node to read from
     * @return decrypted stream
     */
    public abstract InputStream getDataStream(DirectoryNode dir)
        throws IOException, GeneralSecurityException;

    public abstract boolean verifyPassword(String password)
        throws GeneralSecurityException;

    /**
     * Returns the length of the encrypted data that can be safely read with
     * {@link #getDataStream(org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode)}.
     * Just reading to the end of the input stream is not sufficient because there are
     * normally padding bytes that must be discarded
     *
     * <p>
     *    The length variable is initialized in {@link #getDataStream(org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode)},
     *    an attempt to call getLength() prior to getDataStream() will result in IllegalStateException.
     * </p>
     *
     * @return length of the encrypted data
     * @throws IllegalStateException if {@link #getDataStream(org.docx4j.org.apache.poi.poifs.filesystem.DirectoryNode)}
     * was not called
     */
    public abstract long getLength();

    public static Decryptor getInstance(EncryptionInfo info) {
        Decryptor d = info.getDecryptor();
        if (d == null) {
            throw new EncryptedDocumentException("Unsupported version");
        }
        return d;
    }

    public InputStream getDataStream(NPOIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
    }
    public InputStream getDataStream(OPOIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
    }
    public InputStream getDataStream(POIFSFileSystem fs) throws IOException, GeneralSecurityException {
        return getDataStream(fs.getRoot());
    }
    
    // for tests
    public byte[] getVerifier() {
        return verifier;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
    
    public byte[] getIntegrityHmacKey() {
        return integrityHmacKey;
    }

    public byte[] getIntegrityHmacValue() {
        return integrityHmacValue;
    }

    protected void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    protected void setVerifier(byte[] verifier) {
        this.verifier = verifier;
    }

    protected void setIntegrityHmacKey(byte[] integrityHmacKey) {
        this.integrityHmacKey = integrityHmacKey;
    }

    protected void setIntegrityHmacValue(byte[] integrityHmacValue) {
        this.integrityHmacValue = integrityHmacValue;
    }

    protected int getBlockSizeInBytes() {
        return builder.getHeader().getBlockSize();
    }
    
    protected int getKeySizeInBytes() {
        return builder.getHeader().getKeySize()/8;
    }
}