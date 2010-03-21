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


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;


public class StripParts {
	
	private static Logger log = Logger.getLogger(StripParts.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// Do we want to save output? 
		boolean save = true;
		boolean flatOpcXmlOutput = true;
		
		String dir = System.getProperty("user.dir") + "/foo/";
		
		String file = "bar";
		String inputfilepath = dir + file + ".docx";
				
		// If so, whereto?
		String outputfilepath = null;
		if (save) {
			if (flatOpcXmlOutput) {
				outputfilepath = dir + file + "_OUT.xml";
			} else {
				outputfilepath = dir + file + "_OUT.docx";				
			}
		}
			
		// Open a document from the file system
		// 1. Load the Package - .docx or Flat OPC .xml
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		
		// List the parts by walking the rels tree
		RelationshipsPart rp = wordMLPackage.getRelationshipsPart();
		StringBuilder sb = new StringBuilder();
		printInfo(rp, sb, "");
		traverseRelationships(wordMLPackage, rp, sb, "    ");
		
		System.out.println(sb.toString());
		
		if (save) {
			wordMLPackage.save(new java.io.File(outputfilepath));
			System.out.println("Saved stripped to " + outputfilepath);
		} else {
			System.out.println("Stripped parts from " + inputfilepath);
		}
	}
	
	public static void  printInfo(Part p, StringBuilder sb, String indent) {
		sb.append("\n" + indent + p.getPartName() + " [" + p.getClass().getName() + "] " );				
	}
	
	public static void traverseRelationships(WordprocessingMLPackage wordMLPackage, 
			RelationshipsPart rp, 
			StringBuilder sb, String indent)
	 throws Docx4JException {
		
		boolean stripPropertiesParts = true;
		boolean keepStyles = false;
		boolean defaultToDelete = false;
		
		List<Relationship> deletions = new ArrayList<Relationship>();
		
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
			
			try {
				String resolvedPartUri = URIHelper.resolvePartUri(rp.getSourceURI(), new URI(r.getTarget() ) ).toString();		
				resolvedPartUri = resolvedPartUri.substring(1);	
				
				Part part = wordMLPackage.getParts().get(new PartName("/" + resolvedPartUri));
				
				// Or could just have done:
				// Part p = rp.getPart(r);
				
				// TEMP
//				if ()

				if (part!=null) {
					printInfo(part, sb, indent);					
				}
				
				if (part==null) {
					sb.append("Part " + resolvedPartUri + " not found! \n");
				} else if ( part instanceof org.docx4j.openpackaging.parts.ThemePart
							|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart
							|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart) {				
					deletions.add(r );						
					sb.append(".. DELETED" );						
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart) {
					
					if (!keepStyles) {
						deletions.add(r );						
						sb.append(".. DELETED" );						
					} else {
						sb.append(".. KEEPING" );						
					}
					
				} else {					
					if (stripPropertiesParts
							&& ( part instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart
									|| part instanceof org.docx4j.openpackaging.parts.DocPropsCorePart
									|| part instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart
									|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.WebSettingsPart
									|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart)) {
						
						deletions.add(r );
						sb.append(".. DELETED" );						
						
					} else if ( part instanceof org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
							|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart
							|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart) {
						sb.append(".. KEEPING" );
						if (part.getRelationshipsPart()==null) {
							sb.append(".. no rels" );						
						} else {
							traverseRelationships(wordMLPackage, part.getRelationshipsPart(), sb, indent + "    ");
						}
					} 
					else if (defaultToDelete) {
						// Delete it
						deletions.add(r );
						sb.append(".. DELETED" );
					}
				}				
					
			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships", e);				
			}
						
		}
		
		for ( Relationship r : deletions) {
			rp.removeRelationship(r);
		}
		
	}
	
	
}
