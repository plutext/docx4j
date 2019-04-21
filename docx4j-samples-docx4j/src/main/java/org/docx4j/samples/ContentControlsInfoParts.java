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


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This sample is similar to PartsList, but
 * it provides info specific to your CustomXML parts.
 * 
 * @author jharrop
 *
 */
public class ContentControlsInfoParts extends AbstractSample {
	
	private static Logger log = LoggerFactory.getLogger(ContentControlsInfoParts.class);						

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		

		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
//			inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/invoice.docx";
//			inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/CountryRegions.xml";
			inputfilepath = System.getProperty("user.dir") + "/OUT_VariableReplace.docx";

		}
		
			
		// Open a document from the file system
		// 1. Load the Package - .docx or Flat OPC .xml
		org.docx4j.openpackaging.packages.OpcPackage opcPackage = org.docx4j.openpackaging.packages.OpcPackage.load(new java.io.File(inputfilepath));		
		
		// List the parts by walking the rels tree
		RelationshipsPart rp = opcPackage.getRelationshipsPart();
		StringBuilder sb = new StringBuilder();
		traverseRelationships(opcPackage, rp, sb, "    ");
		
		approach2(opcPackage, sb);
		
		System.out.println(sb.toString());
	}
	
	
	public static void  printInfo(Part p, StringBuilder sb, String indent) throws Docx4JException {
		
		String relationshipType = "";
		if (p.getSourceRelationships().size()>0 ) {
			relationshipType = p.getSourceRelationships().get(0).getType();
		}
		
//		sb.append("\n" + indent + "Part " + p.getPartName() + " [" + p.getClass().getName() + "] " + relationshipType );
				
		if (p instanceof CustomXmlDataStoragePart)  {
			sb.append("\n" + indent + p.getClass().getName() + ": " + indent + p.getPartName().getName()  );	
			sb.append("\n" + indent + "root element: " + ((CustomXmlDataStoragePart)p).getData().getDocument().getDocumentElement().getLocalName() );
		} else if ( p instanceof JaxbCustomXmlDataStoragePart) {
				sb.append("\n" + indent + p.getClass().getName() + ": " + indent + p.getPartName().getName()  );
		} else if (p instanceof CustomXmlDataStoragePropertiesPart) {	
			CustomXmlDataStoragePropertiesPart pp = (CustomXmlDataStoragePropertiesPart)p;
			sb.append("\n" + indent +  p.getPartName().getName() 
					+  "  item id: " + pp.getItemId()   );
		}
	}
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	public static HashMap<Part, Part> handled = new HashMap<Part, Part>();
	
	public static void traverseRelationships(org.docx4j.openpackaging.packages.OpcPackage wordMLPackage, 
			RelationshipsPart rp, 
			StringBuilder sb, String indent) throws Docx4JException {
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			if (r.getType().contains("customXml")) {
			sb.append("\n" + indent + "For Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() 
					+ " of type " + r.getType() );
			}
			
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {				
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
	
	public static void approach2(OpcPackage pkg, StringBuilder sb) {
		
		sb.append("\n\n Approach 2:");
		
		HashMap<PartName, Part> parts = pkg.getParts().getParts();
		HashMap<String, Object> tmp = new HashMap<String, Object> ();
		
		Collection col = parts.values();
		Iterator iterator = col.iterator();
		while( iterator.hasNext() ) {
			Part entry = (Part)iterator.next();
			
			if (entry instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart
					|| entry instanceof JaxbCustomXmlDataStoragePart) {
				sb.append("\nFound a CustomXmlDataStoragePart, named " + entry.getPartName().getName() );
				if (entry.getRelationshipsPart()==null) { continue; }
				sb.append("\n.. it has a rels part");
				// Look in its rels for rel of @Type customXmlProps (eg @Target="itemProps1.xml")
				Relationship r = entry.getRelationshipsPart().getRelationshipByType(
						Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES);
				if (r==null) {
					sb.append("\n.. but that doesn't point to a  customXmlProps part");
					continue;
				}
				CustomXmlDataStoragePropertiesPart customXmlProps = 
					(CustomXmlDataStoragePropertiesPart)entry.getRelationshipsPart().getPart(r);
				if (customXmlProps==null) {
					sb.append("\n.. but the target seems to be missing?");
					continue;
				}
				String itemId = customXmlProps.getItemId().toLowerCase();
				sb.append("\nIdentified/registered ds:itemId " + itemId);
				if (tmp.get(itemId.toLowerCase())!=null) {
					sb.append("\nDuplicate CustomXML itemId " + itemId + "; check your source docx!");
				}
				tmp.put(itemId, entry );
			}
		}			
		
		
		
		
	}
	
}
