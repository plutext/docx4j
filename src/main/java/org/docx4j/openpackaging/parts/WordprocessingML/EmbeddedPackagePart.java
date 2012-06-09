/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.openpackaging.parts.WordprocessingML;
// Ideally this part would have been created in the parts 
// package itself, since it is also used when a 
// chart is embedded in a pptx.  ie there is nothing
// about it which is specific to WML.

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

public class EmbeddedPackagePart extends BinaryPart { // maybe should extend EmbeddedPackagePart

	private static Logger log = Logger.getLogger(EmbeddedPackagePart.class);		
	
	public EmbeddedPackagePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public EmbeddedPackagePart() throws InvalidFormatException {
		super( new PartName("/word/embeddings/foo") ); // eg Microsoft_Office_Powerpoint_Presentation.pptx
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
//		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
//				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATION));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.EMBEDDED_PKG);
		
		
	}


}
