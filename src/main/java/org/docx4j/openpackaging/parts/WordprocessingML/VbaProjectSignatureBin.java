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

package org.docx4j.openpackaging.parts.WordprocessingML;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * @since 3.2.0
 */
public class VbaProjectSignatureBin extends OleObjectBinaryPart {
	
	/* For what its worth...
	 * 
		Root Entry -
		  (no children)POIFS FileSystem
		  Property: "Root Entry"
		    Name          = "Root Entry"
		    Property Type = 5
		    Node Color    = 1
		    Time 1        = 0
		    Time 2        = 0
    	 */

	private static Logger log = LoggerFactory.getLogger(VbaProjectSignatureBin.class);		
	
	public VbaProjectSignatureBin(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public VbaProjectSignatureBin() throws InvalidFormatException {
		super( new PartName("/word/vbaProjectSignature.bin") );
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_VBA_PROJECT_SIGNATURE));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.VBA_PROJECT_SIGNATURE);
		
	}

	
	
}
