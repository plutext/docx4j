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

import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;

import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTDiagramDefinition;
import org.docx4j.jaxb.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class CreateWithSmartArtAbstract {
	
	private static Logger log = LoggerFactory.getLogger(CreateWithSmartArtAbstract.class);		
	
	protected CTDiagramDefinition diagramLayoutObj;
	protected Templates layoutTreeCreatorXslt;
	protected Templates layoutTree2DiagramDataXslt;
	
	public CreateWithSmartArtAbstract(CTDiagramDefinition diagramLayoutObj,
		Templates layoutTreeCreatorXslt,
		Templates layoutTree2DiagramDataXslt) {
		
		this.diagramLayoutObj = diagramLayoutObj;
		this.layoutTreeCreatorXslt = layoutTreeCreatorXslt;
		this.layoutTree2DiagramDataXslt = layoutTree2DiagramDataXslt;
		
	}
	
	
	public CTDataModel createDiagramData(DiagramDataPart data, Document xml) throws Exception {
		
		Source myList = new javax.xml.transform.dom.DOMSource(xml);

//		static Templates pictureLayoutTreeCreatorXslt;
//		static Templates pictureLayoutTree2DiagramDataXslt;
		
		// Generate hierarchical layout tree
		String tmpXslStr =  DiagramLayoutPart.generateLayoutTree(myList, 
				layoutTreeCreatorXslt);
		log.debug(tmpXslStr);
		
		// Finally, apply pictureLayoutTree2DiagramDataXslt to create DiagramData part
						
//		ByteArrayOutputStream layoutBAOS3 = new ByteArrayOutputStream();
//		Result result = new StreamResult(layoutBAOS3);
		JAXBResult result = new JAXBResult(Context.jc );		
		java.util.HashMap<String, Object> settings = new java.util.HashMap<String, Object>();
		
		settings.put("list", xml);
		settings.put("DiagramDataPart", data);
		XmlUtils.transform( 
				new javax.xml.transform.stream.StreamSource(
						new java.io.StringReader(tmpXslStr)), 
						layoutTree2DiagramDataXslt, settings, result);	
		// That use of StreamSource is OK from an XXE point of view,
		// since it originally comes from Document xml, which the docx4j
		// user should have created in an XXE-safe way before invoking createSmartArtDocx
		
		// What did we generate
//		tmpXslStr = layoutBAOS3.toString("UTF-8");
//		System.out.println(tmpXslStr);

		// Finally, inject this into your DiagramData part
		// .. first, we need to make the IDs Word friendly.
		Object ddJaxb = result.getResult();
		DiagramDataPart.setFriendlyIds(XmlUtils.unwrap(ddJaxb));
		
		//System.out.println(XmlUtils.marshaltoString(ddJaxb, false));
		return (CTDataModel)XmlUtils.unwrap(ddJaxb);
				
	}

}
