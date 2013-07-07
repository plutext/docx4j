/*
 *  Copyright 2011, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.DrawingML;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.dml.diagram.CTDiagramDefinitionHeader;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;



/**
 * Used in a SmartArt layout (.glox) package.
 * @see org.glox4j.openpackaging.packages.GloxPackage
 * 
 * @author jharrop
 * 
 * @since 2.7.0
 *
 */
public final class DiagramLayoutHeaderPart extends JaxbDmlPart<CTDiagramDefinitionHeader> {
	
	private static Logger log = LoggerFactory.getLogger(DiagramLayoutHeaderPart.class);			
	
	public DiagramLayoutHeaderPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public DiagramLayoutHeaderPart() throws InvalidFormatException {
		super(new PartName("/diagrams/layoutheader1.xml")); 
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DIAGRAM_LAYOUT_HEADER));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DRAWINGML_DIAGRAM_LAYOUT_HEADER);
	}
		
}
