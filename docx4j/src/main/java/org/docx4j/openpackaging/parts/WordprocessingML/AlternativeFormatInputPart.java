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
import org.docx4j.openpackaging.exceptions.OpenXML4JException;
import org.docx4j.openpackaging.parts.Part;
import org.dom4j.Document;



public final class AlternativeFormatInputPart extends Part {
	
	public AlternativeFormatInputPart() throws InvalidFormatException {
		//super(null,null);		
	}

	@Override
	public Document getDocument() {
		return marshall();
	}
	private Document marshall() {
		// TODO Auto-generated method stub
		return document;
	}
	

	@Override
	public void setDocument(Document document) {
		this.document = document;
		unmarshall(document);
	}

	private void unmarshall(Document doc) {
		// TODO Auto-generated method stub
	}
	
	
}
