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


import org.apache.log4j.Logger;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * When present, will be a rel of document.xml,
 * and pointed to from dgm:dataModel
 *
 */
public final class DiagramDrawingPart extends JaxbDmlPart<CTDataModel> {
	
/*
 *     <dgm:extLst>
	    <a:ext uri="http://schemas.microsoft.com/office/drawing/2008/diagram">
	      <dsp:dataModelExt relId="rId8" minVer="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns=""/>
	    </a:ext>
	  </dgm:extLst>
	</dgm:dataModel>
*/
 	
	private static Logger log = Logger.getLogger(DiagramDrawingPart.class);			
	
	public DiagramDrawingPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public DiagramDrawingPart() throws InvalidFormatException {
		super(new PartName("/word/diagrams/drawing1.xml")); 
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DIAGRAM_DRAWING));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DRAWINGML_DIAGRAM_DRAWING);
	}
		
}
