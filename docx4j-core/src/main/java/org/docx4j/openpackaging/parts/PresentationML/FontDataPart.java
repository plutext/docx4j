/*
 *  Copyright 2014, Plutext Pty Ltd.
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

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Referenced from main presentation part; see http://webapp.docx4java.org/OnlineDemo/ecma376/PresentationML/embeddedFontLst.html
 * 
 * @author jharrop
 * @since 3.2.1
 */
public class FontDataPart extends BinaryPart {

	private static Logger log = LoggerFactory.getLogger(FontDataPart.class);		
	
	public FontDataPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public FontDataPart() throws InvalidFormatException {
		super( new PartName("/ppt/fonts/font1.fntdata") );
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATIONML_FONT_DATA));
			// should be this, unless it contains eg a doc stored directly (ie a non-generic OLE object)

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.PRESENTATIONML_FONT_DATA);
		
		
	}

	
}
