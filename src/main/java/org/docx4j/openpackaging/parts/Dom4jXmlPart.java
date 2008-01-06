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

package org.docx4j.openpackaging.parts;


import java.io.InputStream;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/** OPC Parts are either XML, or binary (or text) documents.
 * 
 *  Most are XML documents.
 *  
 *  docx4j aims to represent XML parts using JAXB.  However, 
 *  at present we only have a JAXB representation for the main
 *  document part.  
 *  
 *  Until such time as a JAXB representation for an XML Part exists,
 *  the Part should extend this class.   
 * */
public abstract class Dom4jXmlPart extends Part {
	
	public Dom4jXmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}
	
	/**
	 * This part's XML contents.  Not guaranteed to be up to date.
	 * Whether it is or not will depend on how the class which extends
	 * Part chooses to treat it.  It may be that the class uses some
	 * other internal representation for its data. 
	 */
	public Document document;	

	public void setDocument(InputStream in ) {
		SAXReader xmlReader = new SAXReader();
		try {
			document = xmlReader.read(in);
//			log.info("\n\n" + partName + "\n ===================");
//			debugPrint(contents);
			
		} catch (DocumentException e) {
			log.error("DocumentException on " + partName + " . Check this is binary content."); 
			e.printStackTrace() ;
		}		
	}
	
	public abstract Document getDocument();
	
	public org.w3c.dom.Document getW3cDocument() {
		
		org.w3c.dom.Document w3cDoc = null;
		
		try {

			// convert dom4j node to real W3C dom node
			w3cDoc = new org.dom4j.io.DOMWriter()
					.write(getDocument());

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return w3cDoc;
		
		
	}
	
	
}
