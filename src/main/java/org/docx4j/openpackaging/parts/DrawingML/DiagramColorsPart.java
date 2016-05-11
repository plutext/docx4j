/*
 *  Copyright 2010, Plutext Pty Ltd.
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


import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.docx4j.dml.diagram.CTColorTransform;
import org.docx4j.dml.diagram.ObjectFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DiagramColorsPart extends JaxbDmlPart<CTColorTransform> {
	// <xsd:element name="colorsDef" type="CT_ColorTransform">
	
	private static Logger log = LoggerFactory.getLogger(DiagramColorsPart.class);			
	
	public DiagramColorsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public DiagramColorsPart() throws InvalidFormatException {
		super(new PartName("/word/diagrams/colors1.xml")); 
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DIAGRAM_COLORS));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DRAWINGML_DIAGRAM_COLORS);
	}

    public Object unmarshal(String filename) throws JAXBException {
    	
		java.io.InputStream is = null;
		try {
			is = org.docx4j.utils.ResourceUtils.getResource(
					"org/docx4j/openpackaging/parts/DrawingML/" + filename);
		} catch (IOException e) {
			e.printStackTrace();
		}    		
    	
    	return unmarshal( is );    // side-effect is to set jaxbElement 	
    }
    
    public void CreateMinimalContent(String uniqueId) {

		ObjectFactory factory = new ObjectFactory(); 
		
		CTColorTransform colorsDef = factory.createCTColorTransform();
		colorsDef.setUniqueId(uniqueId);
		
		this.setJaxbElement(colorsDef);    	
    }
		
}
