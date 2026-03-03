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
package org.docx4j.org.apache.poi.poifs.crypt.agile;

import java.io.IOException;
import java.io.InputStream;

import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.util.LittleEndianInput;
import org.docx4j.org.apache.poi.util.XMLHelper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AgileEncryptionInfoBuilder implements EncryptionInfoBuilder {

    @Override
    public void initialize(EncryptionInfo info, LittleEndianInput dis) throws IOException {
        if (!(dis instanceof InputStream)) {
            throw new IllegalArgumentException("Had unexpected type of input: " + (dis == null ? "<null>" : dis.getClass()));
        }

        EncryptionDocument ed = parseDescriptor((InputStream)dis);
        info.setHeader(new AgileEncryptionHeader(ed));
        info.setVerifier(new AgileEncryptionVerifier(ed));
        if (info.getVersionMajor() == EncryptionMode.agile.versionMajor
            && info.getVersionMinor() == EncryptionMode.agile.versionMinor) {
            AgileDecryptor dec = new AgileDecryptor();
            dec.setEncryptionInfo(info);
            info.setDecryptor(dec);
            AgileEncryptor enc = new AgileEncryptor();
            enc.setEncryptionInfo(info);
            info.setEncryptor(enc);
        }
    }

    @Override
    public void initialize(EncryptionInfo info, CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        if (cipherAlgorithm == null) {
            cipherAlgorithm = CipherAlgorithm.aes128;
        }
        if (cipherAlgorithm == CipherAlgorithm.rc4) {
            throw new EncryptedDocumentException("RC4 must not be used with agile encryption.");
        }
        if (hashAlgorithm == null) {
            hashAlgorithm = HashAlgorithm.sha1;
        }
        if (chainingMode == null) {
            chainingMode = ChainingMode.cbc;
        }
        if (!(chainingMode == ChainingMode.cbc || chainingMode == ChainingMode.cfb)) {
            throw new EncryptedDocumentException("Agile encryption only supports CBC/CFB chaining.");
        }
        if (keyBits == -1) {
            keyBits = cipherAlgorithm.defaultKeySize;
        }
        if (blockSize == -1) {
            blockSize = cipherAlgorithm.blockSize;
        }
        boolean found = false;
        for (int ks : cipherAlgorithm.allowedKeySize) {
            found |= (ks == keyBits);
        }
        if (!found) {
            throw new EncryptedDocumentException("KeySize "+keyBits+" not allowed for Cipher "+ cipherAlgorithm);
        }
        info.setHeader(new AgileEncryptionHeader(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode));
        info.setVerifier(new AgileEncryptionVerifier(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode));
        AgileDecryptor dec = new AgileDecryptor();
        dec.setEncryptionInfo(info);
        info.setDecryptor(dec);
        AgileEncryptor enc = new AgileEncryptor();
        enc.setEncryptionInfo(info);
        info.setEncryptor(enc);
    }

    protected static EncryptionDocument parseDescriptor(String descriptor) {
        return parseDescriptor(new InputSource(descriptor));
    }

    protected static EncryptionDocument parseDescriptor(InputStream descriptor) {
        return parseDescriptor(new InputSource(descriptor));
    }

    private static EncryptionDocument parseDescriptor(InputSource descriptor) {
        try {
            Document doc = XMLHelper.newDocumentBuilder().parse(descriptor);
            EncryptionDocument ed = new EncryptionDocument();
            ed.parse(doc);
            return ed;
        } catch (SAXException|IOException e) {
            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
        }
    }
}
