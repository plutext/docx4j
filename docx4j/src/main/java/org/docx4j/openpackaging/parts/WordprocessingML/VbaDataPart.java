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


import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


public final class VbaDataPart extends XmlPart {
	
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

	@Override
	public org.w3c.dom.Document getDocument() throws Docx4JException {
		// Used when saving to JCR
		return doc;
	}
	
}
