/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.WordprocessingML;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.dom4j.Document;



public final class GlossaryDocumentPart extends DocumentPart  {

	
	// A glossary document is a collection of docPart objects
	
			// TO BE IMPLEMENTED
	
	/* A copy of the original document, for debug purposes only */
	private org.dom4j.Document document;

	public GlossaryDocumentPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_GLOSSARYDOCUMENT));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.GLOSSARY_DOCUMENT);
		
	}
	
	
    public void marshal( org.w3c.dom.Node node )
    throws JAXBException {}

    
    public  Object unmarshal( java.io.InputStream is ) throws JAXBException {
		// TODO Auto-generated method stub
    	return null;
	}
	
}
