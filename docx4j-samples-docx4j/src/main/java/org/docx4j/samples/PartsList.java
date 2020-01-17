/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.samples;


import java.io.IOException;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.packages.Filetype;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaDataPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PartsList extends AbstractSample {
	
	private static Logger log = LoggerFactory.getLogger(PartsList.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// Configuration options
		boolean printContentTypes = true;
		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/chart.docx";
		}

			
		// Load the Package as an OpcPackage, since this 
		// works for docx, pptx, and xlsx
		OpcPackage opcPackage = OpcPackage.load(new java.io.File(inputfilepath), Filetype.ZippedPackage);
		
		handlePkg(opcPackage, printContentTypes);
		
		// Uncomment if you want to look at any temp fonts before they are deleted!
		// Thread.sleep(60000);
	}
	
	
	
	public static void handlePkg(OpcPackage opcPackage, boolean printContentTypes) {

		if (printContentTypes)
			printContentTypes(opcPackage);
		
		// List the parts by walking the rels tree
		RelationshipsPart rp = opcPackage.getRelationshipsPart();
		StringBuilder sb = new StringBuilder();
		printInfo(rp.getPartName().getName(), null, rp, sb, "");
		traverseRelationships(opcPackage, rp, sb, "    ");
		
		System.out.println(sb.toString());
		
//		opcPackage.save( new File(System.getProperty("user.dir") + "/OUT_chart_drawing.xlsx")); 
		
		
	}
	
	/**
	 * It is often useful to see this [Content_Types].xml
	 */
	public static void printContentTypes(org.docx4j.openpackaging.packages.OpcPackage p) {
		
		ContentTypeManager ctm = p.getContentTypeManager();
		//ctm.listTypes(); 
		System.out.println(
				ctm.toString() );		
	}
	
	public static void  printInfo(String parentName, Relationship r, Part p, StringBuilder sb, String indent) {
		
		String relationshipType = "";
		if (p.getSourceRelationships().size()>0 ) {
			relationshipType = p.getSourceRelationships().get(0).getType();
		}
		
		if (r==null) {
			sb.append("\n" + indent + "Part " + p.getPartName() + " [" + p.getClass().getName() + "] " + relationshipType );			
		} else {
			sb.append("\n" + indent +  parentName + "'s " + r.getId() + " is " + p.getPartName() + " [" + p.getClass().getName() + "] " + relationshipType );
		}
		
		
//		System.out.println("//" + p.getPartName() );
//		System.out.println("public final static String XX =");
//		System.out.println("\"" +  relationshipType +  "\";");

		if (p instanceof JaxbXmlPart) {
			Object o = ((JaxbXmlPart)p).getJaxbElement();
			if (o instanceof javax.xml.bind.JAXBElement) {
				sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
			} else {
				sb.append(" containing:"  + o.getClass().getName() );
			}
		} else if (p instanceof DefaultXmlPart) {
			try {
				org.w3c.dom.Document doc = ((DefaultXmlPart)p).getDocument();
				try {
					Object o = XmlUtils.unmarshal(doc);
					if (o instanceof javax.xml.bind.JAXBElement) {
						sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
					} else {
						sb.append(" containing:"  + o.getClass().getName() );
					}				
				} catch (javax.xml.bind.UnmarshalException e) {
					sb.append(" containing raw root element:" + doc.getDocumentElement().getLocalName()); 
//					sb.append(e.getMessage()); 
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
//		sb.append("\n content type: " + p.getContentType() + "\n");
//		sb.append("\n reltype: " + p.getRelationshipType() + "\n");
		
		if (p instanceof OleObjectBinaryPart) {
			
			try {
				((OleObjectBinaryPart)p).viewFile(false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (p instanceof VbaDataPart) {
			System.out.println( ((VbaDataPart)p).getXML() );
		}
		
		if (p instanceof FontTablePart) {
			// What fonts are embedded?
			// To see output, log org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart
			// at level INFO, or look in the temp font dir (before the program terminates) 
			((FontTablePart)p).processEmbeddings();
		}
		
	}
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	public static HashMap<Part, Part> handled = new HashMap<Part, Part>();
	
	public static void traverseRelationships(org.docx4j.openpackaging.packages.OpcPackage wordMLPackage, 
			RelationshipsPart rp, 
			StringBuilder sb, String indent) {
		
		// TODO: order by rel id
		
//		if (rp.getRelationships().getRelationship().size()==0) {
//			System.out.println("In rels part .. empty");
//		}
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.info("\nFor Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() 
					+ " type " + r.getType() + "\n");
		
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				sb.append("\n" + indent + "external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				continue;				
			}
			
			Part part = rp.getPart(r);
						
			
			printInfo(rp.getSourceP().getPartName().getName(), r, part, sb, indent);
			if (handled.get(part)!=null) {
				sb.append(" [additional reference] ");
				continue;
			}
			handled.put(part, part);
			if (part.getRelationshipsPart(false)==null) {
				// sb.append(".. no rels" );						
			} else {
				traverseRelationships(wordMLPackage, part.getRelationshipsPart(false), sb, indent + "    ");
			}
					
		}
		
		
	}
	
	
}
