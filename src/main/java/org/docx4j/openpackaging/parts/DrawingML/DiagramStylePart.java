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

import org.docx4j.dml.diagram.CTStyleDefinition;
import org.docx4j.dml.diagram.CTStyleLabel;
import org.docx4j.dml.diagram.ObjectFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DiagramStylePart extends JaxbDmlPart<CTStyleDefinition> {
	//<xsd:element name="styleDef" type="CT_StyleDefinition">

	
	private static Logger log = LoggerFactory.getLogger(DiagramStylePart.class);			
	
	public DiagramStylePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public DiagramStylePart() throws InvalidFormatException {
		super(new PartName("/word/diagrams/quickStyle1.xml")); 
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DIAGRAM_STYLE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DRAWINGML_DIAGRAM_STYLE);
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
			
			CTStyleDefinition styleDef = factory.createCTStyleDefinition();
			styleDef.setUniqueId(uniqueId);
			
			CTStyleLabel styleLabel = factory.createCTStyleLabel();
			styleLabel.setName("node0");
			
			styleDef.getStyleLbl().add(styleLabel);
			
			this.setJaxbElement(styleDef);    	
	    }
	    
}
