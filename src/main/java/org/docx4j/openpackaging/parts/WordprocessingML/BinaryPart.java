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

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

import org.dom4j.Document;


public class BinaryPart extends Part {
	
	public BinaryPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Can't setContentType or setRelationshipType, since 
		// these will differ depending on the nature of the data.
		// Common binary parts should extend this class to 
		// provide that information.
		
	}
	
	private InputStream binaryData;

	public InputStream getBinaryData() {
		return binaryData;
	}

	public void setBinaryData(InputStream binaryData) {
		this.binaryData = binaryData;
	}		
		

}
