/*
 *  Copyright 2010-2011, Plutext Pty Ltd.
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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Map.Entry;

import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDiagramDefinition;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public final class DiagramLayoutPart extends JaxbDmlPart<CTDiagramDefinition> {
	// <xsd:element name="layoutDef" type="CT_DiagramDefinition">
	
	private static Logger log = LoggerFactory.getLogger(DiagramLayoutPart.class);			
	
	public DiagramLayoutPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public DiagramLayoutPart() throws InvalidFormatException {
		super(new PartName("/word/diagrams/layout1.xml")); 
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DIAGRAM_LAYOUT));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DRAWINGML_DIAGRAM_LAYOUT);
	}

	static Templates xsltGenerateIds;	
	static {
		
		{
			try {
				
				xsltGenerateIds = XmlUtils.getTransformerTemplate(
						new StreamSource(
							org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/openpackaging/parts/DrawingML/GenerateIds.xslt")));
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
		
		
	public static Templates generateLayoutTreeXSLT(CTDiagramDefinition pictureLayout) throws Exception {
		
		log.debug(XmlUtils.marshaltoString(pictureLayout, true));
		
//			System.out.println("Generating xslt..");
		Source xsltSource  = new StreamSource(
				org.docx4j.utils.ResourceUtils.getResource(
						"org/docx4j/openpackaging/parts/DrawingML/DiagramLayoutMeta.xslt"));
		Templates xsltMeta = XmlUtils.getTransformerTemplate(xsltSource);

		Document source = XmlUtils.marshaltoW3CDomDocument(pictureLayout,
				Context.jc);
		ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
		Result intermediateResult = new StreamResult(intermediate);
		XmlUtils.transform(source, xsltMeta, null, intermediateResult);

		String tmpXslStr = intermediate.toString("UTF-8");
		log.debug(tmpXslStr);
		
		return XmlUtils.getTransformerTemplate(
					new StreamSource(new StringReader(tmpXslStr)));
	}
	
	public static String generateLayoutTree(Source myList, Templates layoutTreeCreatorXslt) throws Exception {
		
		// Generate hierarchical layout tree
		log.info("Generating layout tree..");
		ByteArrayOutputStream layoutBAOS = new ByteArrayOutputStream();
		Result layoutResult = new StreamResult(layoutBAOS);
		XmlUtils.transform(myList, layoutTreeCreatorXslt, null, layoutResult);

//		tmpXslStr = layoutBAOS.toString("UTF-8");
//		System.out.println(tmpXslStr);
		
		ByteArrayOutputStream layoutBAOS2 = new ByteArrayOutputStream();
		Result layoutResult2 = new StreamResult(layoutBAOS2);
		XmlUtils.transform( 
				new javax.xml.transform.stream.StreamSource(
						new java.io.StringReader(layoutBAOS.toString("UTF-8"))), 
				xsltGenerateIds, null, layoutResult2);		
		
		return layoutBAOS2.toString("UTF-8");
	}
	
	
	public static void main(String[] args) throws Exception {
		
		// Need the source doc as a DOM for later, and also
		// as XSLT input
		Document doc = XmlUtils.getNewDocumentBuilder().parse(
				new File(System.getProperty("user.dir")+ "/SmartArt/12hi.xml"  ) );		
		
		Source myList = new javax.xml.transform.dom.DOMSource(doc);
			
		// We need a source layout part
		// Could get it from a docx, or a glox
		String layoutSrcFilePath = System.getProperty("user.dir")
		+ "/sample-docs/glox/extracted/chevron1.glox";
		
		OpcPackage opcPackage = OpcPackage.load(new java.io.File(layoutSrcFilePath));
		DiagramLayoutPart thisPart = null;
		for (Entry<PartName,Part> entry : opcPackage.getParts().getParts().entrySet() ) {
			
			if (entry.getValue().getContentType().equals( 
					ContentTypes.DRAWINGML_DIAGRAM_LAYOUT )) {
				thisPart = (DiagramLayoutPart)entry.getValue();
				break;
			}
		}
		if (thisPart==null) {
			System.out.println("No SmartArt found in " + layoutSrcFilePath);
			return;	
		}
		
		// Generate XSLT .. can save this for reuse
		System.out.println("Generating xslt..");
		Templates xslLayoutNodeTree = generateLayoutTreeXSLT(thisPart.getJaxbElement());	
		
		// Generate hierarchical layout tree
		String tmpXslStr =  generateLayoutTree(myList, xslLayoutNodeTree);
		System.out.println(tmpXslStr);
		
		// Finally, apply your LT2DD to create DiagramData part
		System.out.println("Creating DiagramData part..");
		Templates xsltLT2DD;
		try {

			xsltLT2DD = XmlUtils.getTransformerTemplate(
					new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
							"org/docx4j/openpackaging/parts/DrawingML/DiagramLayoutTree4pictureOrgChart2DiagramData.xslt")));
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
						
//		ByteArrayOutputStream layoutBAOS3 = new ByteArrayOutputStream();
//		Result result = new StreamResult(layoutBAOS3);
		JAXBResult result = new JAXBResult(Context.jc );		
		java.util.HashMap<String, Object> settings = new java.util.HashMap<String, Object>();
		
		settings.put("list", doc);
		XmlUtils.transform( 
				new javax.xml.transform.stream.StreamSource(
						new java.io.StringReader(tmpXslStr)), 
						xsltLT2DD, settings, result);		
		
		// What did we generate
//		tmpXslStr = layoutBAOS3.toString("UTF-8");
//		System.out.println(tmpXslStr);

		// Finally, inject this into your DiagramData part
		// .. first, we need to make the IDs Word friendly.
		Object ddJaxb = result.getResult();
		DiagramDataPart.setFriendlyIds(XmlUtils.unwrap(ddJaxb));
		
		System.out.println(XmlUtils.marshaltoString(ddJaxb, false));
	}	
}
