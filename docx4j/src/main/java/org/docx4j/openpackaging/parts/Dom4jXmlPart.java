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
 *  at present there are some parts for which we don't have
 *  JAXB representations. 
 *  
 *  It used to be the case that where a JAXB representation for an XML Part 
 *  didn't exist, it would extend this class.
 *  
 *  However, all dependencies on dom4j are being removed from docx4j.
 *  See instead XmlPart.   
 *  
 *  Dom4jXmlPart will be removed from docx4j for the docx4j v3 release.
 * */
@Deprecated
public abstract class Dom4jXmlPart extends Part {
	
	public Dom4jXmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}

	public Dom4jXmlPart() throws InvalidFormatException {
		super();
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

	public void setDocument(Document document) {
		
		this.document = document;
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
