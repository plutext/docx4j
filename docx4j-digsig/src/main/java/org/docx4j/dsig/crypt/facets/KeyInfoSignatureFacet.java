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

package org.docx4j.dsig.crypt.facets;

import java.security.Key;
import java.security.KeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.crypt.SignatureConfig;

/**
 * Signature Facet implementation that adds ds:KeyInfo to the XML signature.
 * 
 * @author Frank Cornelis
 * 
 */
public class KeyInfoSignatureFacet extends SignatureFacet {
	
	// See https://github.com/apache/poi/blob/trunk/poi-ooxml/src/main/java/org/apache/poi/poifs/crypt/dsig/facets/KeyInfoSignatureFacet.java

//    private static final POILogger LOG = POILogFactory.getLogger(KeyInfoSignatureFacet.class);
	private static Logger LOG = LoggerFactory.getLogger(KeyInfoSignatureFacet.class);	
	
	/*
		  </SignatureValue>
		  <KeyInfo>
		    <X509Data>
		      <X509Certificate>
		        MIIFGDCCBACgAwIBAgIQf9Hy8UJoFT4iDZtsk+3cSjANBgkqhkiG9w0BAQUFADB7MQswCQYDVQQG
		        :
		        LAko3ltSTG+zSz7wys2aLMpR8ROGuBhFeRmgp6Dq6/xZTL3Z5nrx1yDnr+yPB3FAEeq87tkZ
		      </X509Certificate>
		    </X509Data>
		  </KeyInfo>
		  <Object Id="idPackageObject" xmlns:mdssi="http://schemas.openxmlformats.org/package/2006/digital-signature">

	 * 
	 */
    
    @Override
    public void postSign(Document document) 
    throws MarshalException {
        LOG.debug( "postSign");

        NodeList nl = document.getElementsByTagNameNS(XML_DIGSIG_NS, "Object");
        
        /*
         * Make sure we insert right after the ds:SignatureValue element, just
         * before the first ds:Object element.
         */
        Node nextSibling = (nl.getLength() == 0) ? null : nl.item(0);

        /*
         * Construct the ds:KeyInfo element using JSR 105.
         */
        KeyInfoFactory keyInfoFactory = signatureConfig.getKeyInfoFactory();
        List<Object> x509DataObjects = new ArrayList<Object>();
        X509Certificate signingCertificate = signatureConfig.getCurrentSignatureDetail().getSigningCertificateChain().get(0);

        List<XMLStructure> keyInfoContent = new ArrayList<>();
        
        if (signatureConfig.isIncludeKeyValue()) {
            KeyValue keyValue;
            try {
                keyValue = keyInfoFactory.newKeyValue(signingCertificate.getPublicKey());
            } catch (KeyException e) {
                throw new RuntimeException("key exception: " + e.getMessage(), e);
            }
            keyInfoContent.add(keyValue);
        }

        if (signatureConfig.isIncludeIssuerSerial()) {
            x509DataObjects.add(keyInfoFactory.newX509IssuerSerial(
                signingCertificate.getIssuerX500Principal().toString(),
                signingCertificate.getSerialNumber()));
        }

        if (signatureConfig.isIncludeEntireCertificateChain()) {
            x509DataObjects.addAll(signatureConfig.getCurrentSignatureDetail().getSigningCertificateChain());
        } else {
            x509DataObjects.add(signingCertificate);
        }

        if (!x509DataObjects.isEmpty()) {
            X509Data x509Data = keyInfoFactory.newX509Data(x509DataObjects);
            keyInfoContent.add(x509Data);
        }
        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(keyInfoContent);
        DOMKeyInfo domKeyInfo = (DOMKeyInfo)keyInfo; 

        Key key = new Key() {
            private static final long serialVersionUID = 1L;

            public String getAlgorithm() {
                return null;
            }

            public byte[] getEncoded() {
                return null;
            }

            public String getFormat() {
                return null;
            }
        };

        Element n = document.getDocumentElement();
        DOMSignContext domSignContext = new DOMSignContext(key, n, nextSibling);
        for (Map.Entry<String,String> me : signatureConfig.getNamespacePrefixes().entrySet()) {
            domSignContext.putNamespacePrefix(me.getKey(), me.getValue());
        }
        
        DOMStructure domStructure = new DOMStructure(n);
        domKeyInfo.marshal(domStructure, domSignContext);
        
        // move keyinfo into the right place
        if (nextSibling != null) {
            NodeList kiNl = document.getElementsByTagNameNS(XML_DIGSIG_NS, "KeyInfo");
            if (kiNl.getLength() != 1) {
                throw new RuntimeException("KeyInfo wasn't set");
            }
            nextSibling.getParentNode().insertBefore(kiNl.item(0), nextSibling);
        }
    }
}