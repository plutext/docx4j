/*
 *  Copyright 2007-2019, Plutext Pty Ltd.
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

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.org.w3.x2003.inkML.InkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @since 8.1.0
 *
 */
public class InkmlPart extends JaxbXmlPart<InkType> {
	
	/*
		<inkml:ink xmlns:inkml="http://www.w3.org/2003/InkML">
		  <inkml:definitions>
		    <inkml:context xml:id="ctx0">
		      <inkml:inkSource xml:id="inkSrc0">
	 */
	
	
	private static Logger log = LoggerFactory.getLogger(InkmlPart.class);
	
	 /** 
	 * @throws InvalidFormatException
	 */
	public InkmlPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public InkmlPart() throws InvalidFormatException {
		super(new PartName("/ppt/ink/ink1.xml"));
		init();
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		// <Override ContentType="application/inkml+xml" PartName="/ppt/ink/ink1.xml"/>
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.INK_ML));

		// Used when this Part is added to a rels 
		// <Relationship Id="rId8" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml" Target="../ink
		setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
		
		setJAXBContext(Context.jc);						
		
	}
}

	
