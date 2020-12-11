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

package org.docx4j.openpackaging.parts.PresentationML;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.CTTableStyleList;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


public final class TableStylesPart extends JaxbXmlPart<JAXBElement<CTTableStyleList>> {
	// This part uses a JAXB content model from dml,
	// so we need to use that context.
	// Hence this class doesn't extent JaxbPmlPart.
	
	public TableStylesPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public TableStylesPart() throws InvalidFormatException {
		super(new PartName("/ppt/tableStyles.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_TABLE_STYLES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PRESENTATIONML_TABLE_STYLES);
		
	}

}
