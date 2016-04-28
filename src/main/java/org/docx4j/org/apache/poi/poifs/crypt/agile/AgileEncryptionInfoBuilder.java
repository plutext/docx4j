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

import javax.xml.bind.JAXBException;

//import org.apache.xmlbeans.XmlException;



import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.CTEncryption;
import org.docx4j.jaxb.Context;
import org.docx4j.org.apache.poi.EncryptedDocumentException;
import org.docx4j.org.apache.poi.poifs.crypt.ChainingMode;
import org.docx4j.org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfo;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionInfoBuilder;
import org.docx4j.org.apache.poi.poifs.crypt.EncryptionMode;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.docx4j.org.apache.poi.util.LittleEndianInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AgileEncryptionInfoBuilder implements EncryptionInfoBuilder {
	
	private static Logger log = LoggerFactory.getLogger(AgileEncryptionInfoBuilder.class);
	
    
    EncryptionInfo info;
    AgileEncryptionHeader header;
    AgileEncryptionVerifier verifier;
    AgileDecryptor decryptor;
    AgileEncryptor encryptor;

    public void initialize(EncryptionInfo info, LittleEndianInput dis) throws IOException {
        this.info = info;
        
        CTEncryption ed = parseDescriptor((InputStream)dis);
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
    
    protected static CTEncryption parseDescriptor(String descriptor) {
//        try {
//            return EncryptionDocument.Factory.parse(descriptor);
//        } catch (XmlException e) {
//            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
//        }
    	
    	log.error(descriptor);
        
        CTEncryption encryption = null;
		try {
			encryption = (CTEncryption)XmlUtils.unmarshalString(descriptor, Context.jcEncryption, CTEncryption.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return encryption;
        
    }

    protected static CTEncryption parseDescriptor(InputStream descriptor) {
//        try {
//            return EncryptionDocument.Factory.parse(descriptor);
//        } catch (Exception e) {
//            throw new EncryptedDocumentException("Unable to parse encryption descriptor", e);
//        }
    	
//    	try {
//        	descriptor.mark(0);
//        	System.out.println(IOUtils.toString(descriptor, "UTF-8") );
//			descriptor.reset();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
        
        CTEncryption encryption = null;
		try {
			encryption = (CTEncryption)XmlUtils.unwrap(
					XmlUtils.unmarshal(descriptor, Context.jcEncryption));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return encryption;
        
    }
}
