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


import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;


public class PartsList {
	
	private static Logger log = Logger.getLogger(PartsList.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		

		String inputfilepath = System.getProperty("user.dir") 
				+ "/sample-docs/test-docs/header-footer/header_sections_some-linked.xml";
			
		// Open a document from the file system
		// 1. Load the Package - .docx or Flat OPC .xml
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		
		// List the parts by walking the rels tree
		RelationshipsPart rp = wordMLPackage.getRelationshipsPart();
		StringBuilder sb = new StringBuilder();
		printInfo(rp, sb, "");
		traverseRelationships(wordMLPackage, rp, sb, "    ");
		
		System.out.println(sb.toString());
	}
	
	public static void  printInfo(Part p, StringBuilder sb, String indent) {
		sb.append("\n" + indent + p.getPartName() + " [" + p.getClass().getName() + "] " );				
	}
	
	public static void traverseRelationships(WordprocessingMLPackage wordMLPackage, 
			RelationshipsPart rp, 
			StringBuilder sb, String indent) {
		
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
			if (part.getRelationshipsPart()==null) {
				sb.append(".. no rels" );						
			} else {
				traverseRelationships(wordMLPackage, part.getRelationshipsPart(), sb, indent + "    ");
			}
					
		}
		
		
	}
	
	
}
