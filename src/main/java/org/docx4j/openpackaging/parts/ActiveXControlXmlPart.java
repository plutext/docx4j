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


import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.w3c.dom.Document;

/** OPC Parts are either XML, or binary (or text) documents.
 * 
 *  Most are XML documents.
 *  
 *  docx4j aims to represent XML parts using JAXB.  However, 
 *  at present there are some parts for which we don't have
 *  JAXB representations. 
 *  
 *  Until such time as a JAXB representation for an XML Part exists,
 *  the Part should extend this class.   
 *  
 * */
public class ActiveXControlXmlPart extends XmlPart {
	
	public ActiveXControlXmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}

	/* Note, you also need
	 * 
            <rel:Relationships xmlns:rel="http://schemas.openxmlformats.org/package/2006/relationships">
                <rel:Relationship xmlns="" Id="rId1" Target="activeX1.bin" Type="http://schemas.microsoft.com/office/2006/relationships/activeXControlBinary"/>
            </rel:Relationships>
	 *  
	 */

	public ActiveXControlXmlPart() throws InvalidFormatException {
		super(new PartName("/word/activeX/activeX1.xml"));  
		init();		
	}			
	
	public void init() {
	
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_ACTIVEX_XML_OBJECT));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.ACTIVEX_XML_OBJECT);
	}

	@Override
	public Document getDocument() throws Docx4JException {
		return doc;
	}
		
}
