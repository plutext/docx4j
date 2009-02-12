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
	
	/* Currently (2009 02 13), docx4j *requires* that if a part has the main document part as its source 
	 * part, then its name should be relative to that part.
	 * 
	 * In other words, /word/customXML/item1.xml rather than /customXML/item1.xml,
	 * which gives:
	 * 
13.02.2009 14:50:46 *INFO * SaveToZipFile: For Relationship Id=rId1 Source is /word/document.xml, Target is customXML/item1.xml (SaveToZipFile.java, line 247)
13.02.2009 14:50:46 *INFO * SaveToZipFile: Getting part /word/customXML/item1.xml (SaveToZipFile.java, line 287)
13.02.2009 14:50:46 *DEBUG* PartName: Trying to create part name /word/customXML/item1.xml (PartName.java, line 150)
13.02.2009 14:50:46 *DEBUG* PartName: /word/customXML/item1.xml part name created. (PartName.java, line 170)
13.02.2009 14:50:46 *ERROR* SaveToZipFile: Part word/customXML/item1.xml not found! (SaveToZipFile.java, line 292)
	 * 
	 * Note that if you open a docx containing /word/customXML/item1.xml in Word 2007, and re-save it, then
	 * resulting docx will contain customXml/item1.xml (and it will also create itemProps1.xml etc)
	 * ie Word moves the custom xml to another place in the package.
	 * The rel has target "../customXml/item1.xml
	 * 
	 * If you use the WordML package (as opposed to the main document part) as the source to which
	 * you attach the custom xml part, note that when you re-save the document in Word 2007, 
	 * Word will drop the custom xml part entirely!!  
	 * 
	 * So it seems you are best to attach this part to your main document part, 
	 * rather than the package itself. 
	 * 
	 */
	
	
	public CustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
	public CustomXmlDataStoragePart() throws InvalidFormatException {
		super(new PartName("/customXML/item1.xml"));
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
