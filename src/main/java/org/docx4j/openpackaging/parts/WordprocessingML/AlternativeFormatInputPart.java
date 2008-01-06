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

import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Dom4jXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.dom4j.Document;



public final class AlternativeFormatInputPart extends Dom4jXmlPart {
	
	public AlternativeFormatInputPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		 
		// ContentType will vary - see spec 11.3.1 

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.AF);
		
	}

	@Override
	public Document getDocument() {
		return document;
	}	
	
}
