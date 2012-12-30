/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.openpackaging.parts.digitalsignature;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.plutext.jaxb.xmldsig.SignatureType;

/**
 * @author jharrop
 * @since 2.8

 */
public class XmlSignaturePart extends JaxbXmlPart<JAXBElement<SignatureType>> {

	public XmlSignaturePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();		
	}

	public XmlSignaturePart() throws InvalidFormatException {
		super(new PartName("/_xmlsignatures/sig1.xml"));
		init();		
	}
	
	public void init() {		
			
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DIGITAL_SIGNATURE);
		
		this.setJAXBContext(Context.jcXmlDSig);
		
	}
	
}
