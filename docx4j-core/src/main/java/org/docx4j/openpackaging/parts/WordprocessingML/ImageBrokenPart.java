/*
 *  Copyright 2022, Plutext Pty Ltd.
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

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * Placeholder part used where we know there is supposed to
 * be an image, but we can't determine its content type
 * from the rel target.
 * 
 * @author jharrop
 * @since 11.4.9
 */
public class ImageBrokenPart extends BinaryPartAbstractImage {
	
	
	public ImageBrokenPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
	public ImageBrokenPart(ExternalTarget externalTarget) {
		super(externalTarget);
		init();
	}	
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
//		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.IMAGE);
	}
	
	
		
}
