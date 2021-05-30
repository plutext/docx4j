/*
 *  Copyright 2021, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.WordprocessingML;


import javax.xml.bind.JAXBElement;

import org.docx4j.com.microsoft.schemas.office.word.x2006.wordml.CTTcg;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * @see [MS-OI29500]
 * 
 * @author jharrop
 * @since 8.2.8
 *
 */
public final class KeyMapCustomizationsPart extends JaxbXmlPart<JAXBElement<CTTcg>> {
	
	/*
	 * eg <wne:tcg xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml">
			  <wne:keymaps>
			    <wne:keymap wne:kcmPrimary="0434">
			      <wne:wch wne:val="000020AC"/>
			    </wne:keymap>
			  </wne:keymaps>
			</wne:tcg>

	 * 
	 */
	
	public KeyMapCustomizationsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}		

	public KeyMapCustomizationsPart() throws InvalidFormatException {
		super( new PartName("/word/customizations.xml") );
		init();
	}		
	
	
	public void init() {

		// Used if this Part is added to [Content_Types].xml
		setContentType(new org.docx4j.openpackaging.contenttype.ContentType(
				org.docx4j.openpackaging.contenttype.ContentTypes.MS_WORD_KEYMAP));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.KEYMAP);
	}
	
}
