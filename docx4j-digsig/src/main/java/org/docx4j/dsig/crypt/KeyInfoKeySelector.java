/* NOTICE: This file has been changed by Plutext Pty Ltd for use with docx4j.
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

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt  
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */ 

package org.docx4j.dsig.crypt;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSR105 key selector implementation using the ds:KeyInfo data of the signature
 * itself.
 */
public class KeyInfoKeySelector extends KeySelector implements KeySelectorResult {

	// see https://github.com/apache/poi/blob/trunk/poi-ooxml/src/main/java/org/apache/poi/poifs/crypt/dsig/KeyInfoKeySelector.java
	
	private static Logger LOG = LoggerFactory.getLogger(KeyInfoKeySelector.class);	
	

    private List<X509Certificate> certChain = new ArrayList<X509Certificate>();

    @SuppressWarnings("unchecked")
    @Override
    public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
        LOG.debug( "select key");
        if (null == keyInfo) {
            throw new KeySelectorException("no ds:KeyInfo present");
        }
        List<XMLStructure> keyInfoContent = keyInfo.getContent();
        certChain.clear();
        for (XMLStructure keyInfoStructure : keyInfoContent) {
            if (!(keyInfoStructure instanceof X509Data)) {
                continue;
            }
            X509Data x509Data = (X509Data) keyInfoStructure;
            List<?> x509DataList = x509Data.getContent();
            for (Object x509DataObject : x509DataList) {
                if (!(x509DataObject instanceof X509Certificate)) {
                    continue;
                }
                X509Certificate certificate = (X509Certificate) x509DataObject;
                LOG.debug("certificate" + certificate.getSubjectX500Principal());
                certChain.add(certificate);
            }
        }
        if (certChain.isEmpty()) {
            throw new KeySelectorException("No key found!");
        }
        return this;
    }

    public Key getKey() {
        // The first certificate is presumably the signer.
        return certChain.isEmpty() ? null : certChain.get(0).getPublicKey();
    }

    /**
     * Gives back the X509 certificate used during the last signature
     * verification operation.
     * 
     * @return the certificate which was used to sign the xml content
     */
    public X509Certificate getSigner() {
        // The first certificate is presumably the signer.
        return certChain.isEmpty() ? null : certChain.get(0);
    }
    
    public List<X509Certificate> getCertChain() {
        return certChain;
    }
}
