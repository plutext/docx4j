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

package org.xlsx4j.samples;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;


public class PartsList {
	
	private static Logger log = LoggerFactory.getLogger(PartsList.class);						

	private static List<WorksheetPart> worksheets = new ArrayList<WorksheetPart>();
	
	private static SharedStrings sharedStrings = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/pivot.xlsm";
			
		// Open a document from the file system
		// 1. Load the Package - .docx or Flat OPC .xml
		org.docx4j.openpackaging.packages.OpcPackage xlsxPkg = org.docx4j.openpackaging.packages.OpcPackage.load(new java.io.File(inputfilepath));		
		
		// List the parts by walking the rels tree
		RelationshipsPart rp = xlsxPkg.getRelationshipsPart();
		StringBuilder sb = new StringBuilder();
		printInfo(rp, sb, "");
		traverseRelationships(xlsxPkg, rp, sb, "    ");
		
		System.out.println(sb.toString());

		// Now lets print the cell content
		for(WorksheetPart sheet: worksheets) {
			System.out.println(sheet.getPartName().getName() );
			Worksheet ws = sheet.getJaxbElement();
			SheetData data = ws.getSheetData();
			for (Row r : data.getRow() ) {
				System.out.println("row " + r.getR() );				
				for (Cell c : r.getC() ) {
					if (c.getT().equals(STCellType.S)) {
						System.out.println( "  " + c.getR() + " contains " +
								sharedStrings.getJaxbElement().getSi().get(Integer.parseInt(c.getV())).getT()
										);
					} else {
						// TODO: handle other cell types
						System.out.println( "  " + c.getR() + " contains " + c.getV() );
					}
				}
			}
		}
		
	}
	
	public static void  printInfo(Part p, StringBuilder sb, String indent) {
		sb.append("\n" + indent + "Part " + p.getPartName() + " [" + p.getClass().getName() + "] " );		
		if (p instanceof JaxbXmlPart) {
			Object o = ((JaxbXmlPart)p).getJaxbElement();
			if (o instanceof javax.xml.bind.JAXBElement) {
				sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
			} else {
				sb.append(" containing JaxbElement:"  + o.getClass().getName() );
			}
		}
		if (p instanceof WorksheetPart) {
			worksheets.add((WorksheetPart)p);
		} else if (p instanceof SharedStrings) {
			sharedStrings = (SharedStrings)p;
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
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			log.info("For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() );
		
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				sb.append("\n" + indent + "external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				continue;				
			}
			
			Part part = rp.getPart(r);
			
			
			printInfo(part, sb, indent);
			if (handled.get(part)!=null) {
				sb.append(" [additional reference] ");
				continue;
			}
			handled.put(part, part);
			if (part.getRelationshipsPart()==null) {
				// sb.append(".. no rels" );						
			} else {
				traverseRelationships(wordMLPackage, part.getRelationshipsPart(), sb, indent + "    ");
			}
					
		}
		
		
	}
	
	
}
