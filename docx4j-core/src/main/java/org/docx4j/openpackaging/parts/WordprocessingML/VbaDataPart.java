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

package org.docx4j.openpackaging.parts.WordprocessingML;


import javax.xml.bind.JAXBElement;

import org.docx4j.com.microsoft.schemas.office.word.x2006.wordml.CTVbaSuppData;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/dd947889(v=office.12).aspx">[MS-OFFMACRO2]</a>
 * 
 * Since 3.0, this extends JaxbXmlPart<JAXBElement<CTVbaSuppData>>
 * Previously, it just extended XmlPart.
 * 
 * @author jharrop
 *
 */
public final class VbaDataPart extends JaxbXmlPart<JAXBElement<CTVbaSuppData>> {
	
	/*
	 * eg <wne:vbaSuppData xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml">
				<wne:mcds>
					<wne:mcd wne:macroName="PROJECT.THISDOCUMENT.AUTOOPEN" wne:name="Project.ThisDocument.AutoOpen" wne:bEncrypt="00" wne:cmg="56"/>
				</wne:mcds>
			</wne:vbaSuppData>

	 * 
	 */
	
	public VbaDataPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}		

	public VbaDataPart() throws InvalidFormatException {
		super( new PartName("/word/vbaData.xml") );
		init();
	}		
	
	
	public void init() {

		// Used if this Part is added to [Content_Types].xml
		setContentType(new org.docx4j.openpackaging.contenttype.ContentType(
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_VBA_DATA));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.VBA_DATA_WORD);
	}
	
}
