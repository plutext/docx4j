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


import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.dom4j.Document;



public final class CustomXmlDataStoragePart extends Dom4jXmlPart {
	
	public CustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
//	public CustomXmlDataStoragePart() throws InvalidFormatException {
//		super(new PartName("/customXml/item1.xml"));
//		init();
//	}

	/**
	 * @param parts The parts present in the package to which this will be added.
	 * If for example /customXml/item1.xml already exists, this allows
	 * the name /customXml/item2.xml to be generated.
	 * @throws InvalidFormatException
	 */
	public CustomXmlDataStoragePart(Parts parts) throws InvalidFormatException {
		
		int partNum = 1;
		if (parts!=null) {
			while (parts.get(new PartName("/customXml/item" + partNum + ".xml"))!=null) {
				partNum++;			
			}
		}
		
		this.partName = new PartName("/customXml/item" + partNum + ".xml");
		log.info("Using PartName /customXml/item" + partNum + ".xml");
		init();
	}
	
	
	public void init() {		
	
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
		
	}

	@Override
	public Document getDocument() {
		return document;
	}	
	
}
