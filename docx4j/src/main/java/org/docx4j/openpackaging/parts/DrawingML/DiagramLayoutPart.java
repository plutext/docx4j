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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.util.JAXBResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTDiagramDefinition;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


public final class DiagramLayoutPart extends JaxbDmlPart<CTDiagramDefinition> {
	// <xsd:element name="layoutDef" type="CT_DiagramDefinition">
	
	private static Logger log = Logger.getLogger(DiagramLayoutPart.class);			
	
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

	
	
	public static void main(String[] args) throws Exception {
		
		// Need the source doc as a DOM for later, and also
		// as XSLT input
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
										.newDocumentBuilder();
		Document doc = docBuilder.parse(
				new InputSource(new java.io.StringReader("<node id=\"0\">"
						+"<node id=\"1\" val=\"1\">"
						+"    <node id=\"2\" val=\"21\"/>"
						+"    <node id=\"3\" val=\"22\">"
					      +"<node id=\"4\" val=\"221\" >"
					      +"<node id=\"5\" val=\"2211\" />"
//					      +"<node id=\"6\" val=\"2212\" />"  
					        +"</node>"
					        +"</node>"
					        +"</node>"
						+"</node>")));

		Source myList = new javax.xml.transform.dom.DOMSource(doc);
			
		// We need a source layout part
		// Could get it from a docx, or a glox
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(
						System.getProperty("user.dir")
						+ "/SmartArt/boss-slave.docx"));		
		Relationship r = wordMLPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByType(Namespaces.DRAWINGML_DIAGRAM_LAYOUT);		
		if (r==null) {
			System.out.println("No DLP!");
			return;
		}
		DiagramLayoutPart thisPart = (DiagramLayoutPart)wordMLPackage.getMainDocumentPart().getRelationshipsPart().getPart(r);
		
		// Generate XSLT
		System.out.println("Generating xslt..");
		Templates xsltMeta;	
		{
			try {
				Source xsltSource  = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/openpackaging/parts/DrawingML/DiagramLayoutMeta.xslt"));
				xsltMeta = XmlUtils.getTransformerTemplate(xsltSource);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} 
		}

		Document source = XmlUtils.marshaltoW3CDomDocument(thisPart.getJaxbElement(),
				Context.jc);
		ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
		Result intermediateResult = new StreamResult(intermediate);
		XmlUtils.transform(source, xsltMeta, null, intermediateResult);

		// What did we generate
		String tmpXslStr = intermediate.toString("UTF-8");
		System.out.println(tmpXslStr);

		Source tmpXslSource = new StreamSource(new StringReader(tmpXslStr));
		
		// Generate hierarchical layout tree
		System.out.println("Generating layout tree..");
		Templates xslLayoutNodeTree;	
		try {
			xslLayoutNodeTree = XmlUtils.getTransformerTemplate(tmpXslSource);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} 
		ByteArrayOutputStream layoutBAOS = new ByteArrayOutputStream();
		Result layoutResult = new StreamResult(layoutBAOS);
		XmlUtils.transform(myList, xslLayoutNodeTree, null, layoutResult);

		Templates xsltGenerateIds;	
		{
			try {
				
				xsltGenerateIds = XmlUtils.getTransformerTemplate(
						new StreamSource(
							org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/openpackaging/parts/DrawingML/GenerateIds.xslt")));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} 
		}
		ByteArrayOutputStream layoutBAOS2 = new ByteArrayOutputStream();
		Result layoutResult2 = new StreamResult(layoutBAOS2);
		XmlUtils.transform( 
				new javax.xml.transform.stream.StreamSource(
						new java.io.StringReader(layoutBAOS.toString("UTF-8"))), 
				xsltGenerateIds, null, layoutResult2);		
		
		// What did we generate
		tmpXslStr = layoutBAOS2.toString("UTF-8");
		System.out.println(tmpXslStr);
		
		// Finally, apply your LT2DD to create DiagramData part
		System.out.println("Creating DiagramData part..");
		Templates xsltLT2DD;	
		{
			try {
				
				xsltLT2DD = XmlUtils.getTransformerTemplate(
						new StreamSource(
							org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/openpackaging/parts/DrawingML/DiagramLayoutTree4pictureOrgChart2DiagramData.xslt")));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} 
		}
						
		ByteArrayOutputStream layoutBAOS3 = new ByteArrayOutputStream();
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
