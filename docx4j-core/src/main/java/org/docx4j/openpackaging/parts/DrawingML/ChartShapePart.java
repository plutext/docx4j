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
package org.docx4j.openpackaging.parts.DrawingML;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.chartDrawing.CTDrawing;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * org.docx4j.dml.chart's ObjectFactory contains:
 * 
 *    JAXBElement<CTDrawing> createUserShapes(org.docx4j.dml.chartDrawing.CTDrawing value)
 * 
 * @author jharrop
 * @since 3.2.0
 */
public class ChartShapePart extends JaxbDmlPart<JAXBElement<CTDrawing>> {
	// This part uses a JAXB content model from dml,
	// so we need to use that context.
	// Hence this class doesn't extend JaxbPmlPart.
		
	public ChartShapePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ChartShapePart() throws InvalidFormatException {
		super(new PartName("/xl/drawings/drawing2.xml"));
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_CHART_SHAPES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CHART_USER_SHAPES);
		
	}

}

