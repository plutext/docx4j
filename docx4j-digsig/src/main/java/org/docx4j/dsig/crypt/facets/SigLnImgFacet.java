/*
 *  Copyright 2016, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.dsig.crypt.facets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.DigitalSignatureException;


/**
 * A visual signature must have images for valid and invalid signatures imposed on signature line;
 * this is what is actually displayed in the document.
 * These are base64 encoded (EMF and PNG both work in Word 2010).
 * 
 * @author jharrop
 * @see <a href="https://msdn.microsoft.com/en-us/library/ff535519(v=office.12).aspx">https://msdn.microsoft.com/en-us/library/ff535519(v=office.12).aspx</a>
 */
public class SigLnImgFacet extends SignatureFacet {

	private static Logger LOG = LoggerFactory.getLogger(SigLnImgFacet.class);	
	
	/*

		    <Reference URI="#idValidSigLnImg" Type="http://www.w3.org/2000/09/xmldsig#Object">
		      <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
		      <DigestValue>sOPzHFDzFjQjmn8Jhz667PJBjYA=</DigestValue>
		    </Reference>
		    <Reference URI="#idInvalidSigLnImg" Type="http://www.w3.org/2000/09/xmldsig#Object">
		      <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
		      <DigestValue>qErbInhCYtZh6dACoSVTh3CbgT4=</DigestValue>
		    </Reference>
		
		  <Object Id="idValidSigLnImg">AQAAAGwAAAAAAAA...AAAAQAAAAFAAAAA==</Object>  (base64 encoded; must it be EMF?)
		  <Object Id="idInvalidSigLnImg">AQAAAGwAAAAA...AAAQAAAAFAAAAA==</Object>
  
  	 */
	
    @Override
    public void preSign(Document document, List<Reference> references, List<XMLObject> objects) throws XMLSignatureException, DigitalSignatureException {
    	
        LOG.debug( "pre sign");
        addValidSigLnImgObject(document, references, objects);
    }

	protected void addValidSigLnImgObject(Document document,
			List<Reference> references, List<XMLObject> objects)
			throws XMLSignatureException, DigitalSignatureException {
		
        SignatureDetail signatureDetail = signatureConfig.getCurrentSignatureDetail();
        
        String validSigLnImgBase64 = signatureDetail.getValidSigLnImgBase64();
        if (validSigLnImgBase64==null) {
        	LOG.debug( "ValidSigLnImg not set.");
        	return;
        }
        
        Document tmp = XmlUtils.neww3cDomDocument();
        
        
        // idValidSigLnImg
        String objectId = "idValidSigLnImg"; 
        DOMStructure structure = new DOMStructure(tmp.createTextNode(validSigLnImgBase64)); 
        List objectContent = new ArrayList();
        objectContent.add(structure);        
        objects.add(getSignatureFactory().newXMLObject(objectContent, objectId, null, null));        

        Reference reference = newReference("#" + objectId, null, XML_DIGSIG_NS+"Object", null, null);
        references.add(reference);

        
        // idinvalidSigLnImg
        objectId = "idInvalidSigLnImg"; 
        try {
			structure = new DOMStructure(tmp.createTextNode(signatureDetail.getInvalidSigLnImg()));
		} catch (IOException e) {
			throw new DigitalSignatureException(e.getMessage(), e);
		} 
        objectContent = new ArrayList();
        objectContent.add(structure);        
        objects.add(getSignatureFactory().newXMLObject(objectContent, objectId, null, null));        

        reference = newReference("#" + objectId, null, XML_DIGSIG_NS+"Object", null, null);
        references.add(reference);
        
    }
    
    

}