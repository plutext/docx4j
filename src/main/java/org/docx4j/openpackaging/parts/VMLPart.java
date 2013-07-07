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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.vml.root.Xml;

public final class VMLPart extends JaxbXmlPartXPathAware<Xml>  {
	
	private static Logger log = LoggerFactory.getLogger(VMLPart.class);		
	
	public VMLPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public VMLPart() throws InvalidFormatException {
		super(new PartName("/xl/drawings/vmlDrawing1.vml"));
		init();
	}
	
	
	public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.VML_DRAWING));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.VML);
	}
	
}

