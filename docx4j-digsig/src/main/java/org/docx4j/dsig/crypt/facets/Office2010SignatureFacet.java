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

import java.io.ByteArrayInputStream;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;





import org.docx4j.Docx4jProperties;
//import org.apache.xmlbeans.XmlException;
import org.docx4j.XmlUtils;
//import org.docx4j.jaxb.Context;
import org.docx4j.org.apache.xml.security.Init;
import org.docx4j.org.apache.xml.security.c14n.Canonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.docx4j.dsig.crypt.DSigJAXBContext;
import org.docx4j.dsig.crypt.SignatureConfig;
import org.docx4j.org.etsi.uri.x01903.v13.QualifyingPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.UnsignedPropertiesType;
import org.docx4j.org.etsi.uri.x01903.v13.UnsignedSignaturePropertiesType;

/**
 * Work-around for Office2010 to accept the XAdES-BES/EPES signature.
 * 
 * xades:UnsignedProperties/xades:UnsignedSignatureProperties needs to be
 * present.
 * 
 * @author Frank Cornelis
 * 
 */
public class Office2010SignatureFacet extends SignatureFacet {
	
	private static Logger LOG = LoggerFactory.getLogger(Office2010SignatureFacet.class);
	
	/*
		  <Object>
		    <xd:QualifyingProperties Target="#idPackageSignature" xmlns:xd="http://uri.etsi.org/01903/v1.3.2#">
		      <xd:SignedProperties Id="idSignedProperties">
		        :
		      </xd:SignedProperties>
		      <xd:UnsignedProperties>
		      
		      	HERE
		      
		      </xd:UnsignedProperties>
		    </xd:QualifyingProperties>
		  </Object>
	 */
	

    @Override
    public void postSign(Document document)
    throws MarshalException {
        // check for XAdES-BES
        NodeList nl = document.getElementsByTagNameNS(XADES_132_NS, "QualifyingProperties");
        if (nl.getLength() != 1) {
        	
        	if (Docx4jProperties.getProperty("docx4j.dsig.XAdES.Level", SignatureConfig.XAdES_LEVEL_DEFAULT)>0) {        		
        		throw new MarshalException("no XAdES-BES extension present");
        	} else {
        		return;
        	}
        }

        QualifyingPropertiesType qualProps;
        try {
        	Unmarshaller u = DSigJAXBContext.jcXmlDSig.createUnmarshaller();
        	qualProps = (QualifyingPropertiesType)XmlUtils.unwrap(u.unmarshal(nl.item(0)));
            //qualProps = QualifyingPropertiesType.Factory.parse(nl.item(0));
        } catch (JAXBException e) {
            throw new MarshalException(e);
        }
        
        // create basic XML container structure
        UnsignedPropertiesType unsignedProps = qualProps.getUnsignedProperties();
        if (unsignedProps == null) {
        	unsignedProps = new UnsignedPropertiesType(); 
            //unsignedProps = qualProps.addNewUnsignedProperties();
        	qualProps.setUnsignedProperties(unsignedProps);
        	
        }
        UnsignedSignaturePropertiesType unsignedSigProps = unsignedProps.getUnsignedSignatureProperties();
        if (unsignedSigProps == null) {
        	unsignedSigProps = new UnsignedSignaturePropertiesType();
            //unsignedSigProps = unsignedProps.addNewUnsignedSignatureProperties();
        	unsignedProps.setUnsignedSignatureProperties(unsignedSigProps);
        }
        
        //Node child = XmlUtils.marshaltoW3CDomDocument(qualProps, Context.jcXmlDSig).getFirstChild();
        
        // Get rid of the superfluous namespaces; done already in XAdESSignatureFacet
        Node child = null;
        try {
			Document qualifyingPropertiesTmpDoc = XmlUtils.marshaltoW3CDomDocument(qualProps, DSigJAXBContext.jcXmlDSig);
			LOG.debug("Input to Canonicalizer: " + XmlUtils.w3CDomNodeToString(qualifyingPropertiesTmpDoc));
			
			Init.init();
			Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE);
			byte[] bytes = c.canonicalizeSubtree(qualifyingPropertiesTmpDoc);
			Document myDoc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(bytes));
	//		System.out.println("\n\n!! " + XmlUtils.w3CDomNodeToString(myDoc));
			child = myDoc.getDocumentElement();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
        
        Node n = document.importNode(child, true);
        
        nl.item(0).getParentNode().replaceChild(n, nl.item(0));
    }
}