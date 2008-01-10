/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
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
