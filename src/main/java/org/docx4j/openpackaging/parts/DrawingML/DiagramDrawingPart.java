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


import java.util.HashMap;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram2008.CTShape;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The last successful layout for a diagram".
 * See MS-ODRAWXML.
 *  
 * When present, will be a rel of document.xml,
 * and pointed to from dgm:dataModel
 * 
 * In MS-ODRAWXML, this part is called "Diagram
 * Layout Part".  Is that a typo, or just
 * confusing?  I'm not gonna call it that.
 * 
 * At http://msdn.microsoft.com/en-us/library/documentformat.openxml.office.drawing.drawing.aspx
 * it seems to be called DiagramPersistLayoutPart,
 * which would be better.
 */
public final class DiagramDrawingPart extends JaxbDmlPart<CTDataModel> {
	
/* From http://msdn.microsoft.com/en-us/library/dd909877(v=office.12).aspx
 * 
 * The last successful layout for a diagram is stored in documents as an extension 
 * by using a Diagram Layout part. The part is referenced by the relationship id 
 * attribute of a DataModelExt extension to the Data Model. For more information, 
 * see [ISO/IEC-29500-1] section 21.4.2.10. The content of the part contains XML 
 * as defined by Diagram Layout (section 2.1.1).
 * 
 * The DataModelExt contains a version URI that represents the minimum version 
 * required to run the layout. If an application version is insufficient to perform 
 * layout, the Diagram Layout can be used to display the diagram.
 * 
 * See also http://msdn.microsoft.com/en-us/library/dd910819(v=office.12).aspx
 * 	
 */
	
/*
 *     <dgm:extLst>
	    <a:ext uri="http://schemas.microsoft.com/office/drawing/2008/diagram">
	      <dsp:dataModelExt relId="rId8" minVer="http://schemas.openxmlformats.org/drawingml/2006/diagram" xmlns:dsp="http://schemas.microsoft.com/office/drawing/2008/diagram" xmlns=""/>
	    </a:ext>
	  </dgm:extLst>
	</dgm:dataModel>
*/
 	
	private static Logger log = LoggerFactory.getLogger(DiagramDrawingPart.class);			
	
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
		
		
	@Deprecated
	public void setFriendlyIds(final HashMap<String, String> map) {

		new TraversalUtil(getJaxbElement(),

			new Callback() {

				@Override
				public List<Object> apply(Object o) {
					
					if (o instanceof org.docx4j.dml.diagram2008.CTShape) {
						
						CTShape sp = (CTShape)o;
						if (sp.getModelId()!=null) {
							sp.setModelId( 
								map.get(sp.getModelId() ));
						}						
					}
					
					return null;
				}

				@Override
				public boolean shouldTraverse(Object o) {
					return true;
				}

				// Depth first
				@Override
				public void walkJAXBElements(Object parent) {


					List children = getChildren(parent);
					if (children != null) {

						for (Object o : children) {

							// if its wrapped in javax.xml.bind.JAXBElement, get its
							// value
							o = XmlUtils.unwrap(o);

							this.apply(o);

							if (this.shouldTraverse(o)) {
								walkJAXBElements(o);
							}

						}
					}

				}

				@Override
				public List<Object> getChildren(Object o) {
					return TraversalUtil.getChildrenImpl(o);
				}

			}

			);
	}
		
}
