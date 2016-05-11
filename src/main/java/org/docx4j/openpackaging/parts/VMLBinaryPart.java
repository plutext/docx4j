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

package org.docx4j.openpackaging.parts;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;


/**
 * A PPTX containing an embedded XLSX table,
 * then saved as Flat OPC XML, will contain something
 * like:
 * 
 *   <pkg:part pkg:name="/ppt/drawings/vmlDrawing1.vml" pkg:contentType="application/vnd.openxmlformats-officedocument.vmlDrawing">
    	<pkg:binaryData>PHhtbCB4bWxuczp
 * 
 * This part represents that.
 * Note that there is also a VMLPart extends JaxbXmlPart<Xml>,
 * which can be in a spreadsheet.
 * 
 * @author jharrop
 *
 */
public class VMLBinaryPart extends BinaryPart {
	
	public VMLBinaryPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public VMLBinaryPart() throws InvalidFormatException {
		super(new PartName("/ppt/drawings/vmlDrawing1.vml"));
		init();
	}
	
//	public VMLBinaryPart(ExternalTarget externalTarget) {
//		super(externalTarget);
//		init();
//	}	
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.VML_DRAWING));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.VML);
	}
	
		
}
