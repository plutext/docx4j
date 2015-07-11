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
package org.apache.poi.poifs.crypt.agile;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.LittleEndianInput;
//import org.apache.xmlbeans.XmlException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;

import com.microsoft.schemas.office.x2006.encryption.CTEncryption;


public class AgileEncryptionInfoBuilder implements EncryptionInfoBuilder {
    
    EncryptionInfo info;
    AgileEncryptionHeader header;
    AgileEncryptionVerifier verifier;
    AgileDecryptor decryptor;
    AgileEncryptor encryptor;

    public void initialize(EncryptionInfo info, LittleEndianInput dis) throws IOException {
        this.info = info;
        
        EncryptionDocument ed = parseDescriptor((InputStream)dis);
        header = new AgileEncryptionHeader(ed);
        verifier = new AgileEncryptionVerifier(ed);
        if (info.getVersionMajor() == EncryptionMode.agile.versionMajor
            && info.getVersionMinor() == EncryptionMode.agile.versionMinor) {
            decryptor = new AgileDecryptor(this);
            encryptor = new AgileEncryptor(this);
        }
    }

    public void initialize(EncryptionInfo info, CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        this.info = info;

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
            throw new EncryptedDocumentException("KeySize "+keyBits+" not allowed for Cipher "+cipherAlgorithm.toString());
        }
        header = new AgileEncryptionHeader(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
        verifier = new AgileEncryptionVerifier(cipherAlgorithm, hashAlgorithm, keyBits, blockSize, chainingMode);
        decryptor = new AgileDecryptor(this);
        encryptor = new AgileEncryptor(this);
    }
    
    public AgileEncryptionHeader getHeader() {
        return header;
    }

    public AgileEncryptionVerifier getVerifier() {
        return verifier;
    }

    public AgileDecryptor getDecryptor() {
        return decryptor;
    }

    public AgileEncryptor getEncryptor() {
        return encryptor;
    }

    protected EncryptionInfo getInfo() {
        return info;
    }
    
    protected static EncryptionDocument parseDescriptor(String descriptor) {
//        try {
//            return EncryptionDocument.Factory.parse(descriptor);
//        } catch (XmlException e) {
//            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
//        }
        
        CTEncryption encryption = null;
		try {
			encryption = (CTEncryption)XmlUtils.unmarshalString(descriptor, Context.jcXmlDSig, CTEncryption.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        EncryptionDocument ed = new EncryptionDocument();
        ed.setEncryption(encryption);
        return ed;
        
    }

    protected static EncryptionDocument parseDescriptor(InputStream descriptor) {
//        try {
//            return EncryptionDocument.Factory.parse(descriptor);
//        } catch (Exception e) {
//            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
//        }
        
        CTEncryption encryption = null;
		try {
			encryption = (CTEncryption)XmlUtils.unmarshal(descriptor, Context.jcXmlDSig);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        EncryptionDocument ed = new EncryptionDocument();
        ed.setEncryption(encryption);
        return ed;
        
    }
}
