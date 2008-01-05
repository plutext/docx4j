/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4j.openpackaging.parts.WordprocessingML;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.dom4j.Document;



public final class GlossaryDocumentPart extends DocumentPart  {

	
	// A glossary document is a collection of docPart objects
	
			// TO BE IMPLEMENTED
	
	/* A copy of the original document, for debug purposes only */
	private org.dom4j.Document document;

	public GlossaryDocumentPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}
	
	
    public void marshal( org.w3c.dom.Node node )
    throws JAXBException {}

    
    public  Object unmarshal( java.io.InputStream is ) throws JAXBException {
		// TODO Auto-generated method stub
    	return null;
	}
	
}
