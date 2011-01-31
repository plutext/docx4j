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

package org.docx4j.openpackaging.parts.relationships;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.relationships.Relationship;


public class AlteredParts {
	
	private static Logger log = Logger.getLogger(AlteredParts.class);
		
	public static List<Part> start(
									WordprocessingMLPackage thisPackage, 
									WordprocessingMLPackage otherPackage)
			throws Docx4JException {

		List<Part> alteredParts = new ArrayList<Part>();
		
		RelationshipsPart packageRels = thisPackage.getRelationshipsPart();
		RelationshipsPart otherPackageRels = otherPackage.getRelationshipsPart();
		
		recurse(alteredParts, packageRels, otherPackageRels);

		return alteredParts;
	}	

	public static void recurse(List<Part> alteredParts,
			RelationshipsPart thisRP, RelationshipsPart otherRP)
			throws Docx4JException {
		
		log.info(thisRP.partName.getName() + "#########");

		log.info("uniques -------");
		List<Relationship> uniques = thisRP.uniqueToThis(otherRP);
		addPartsForRels(alteredParts, uniques, thisRP  );		

		List<Relationship> missings = thisRP.uniqueToOther(otherRP);
		
		// is this rels part itself altered?
		if (uniques.size()>0 || missings.size()>0) {
			alteredParts.add(thisRP);
		}
		
		log.info("content -------");
		List<Relationship> altered = thisRP.differingContent(otherRP);
		addPartsForRels(alteredParts, altered, thisRP  );	
		
		// Now recurse all rels
		log.info("recurse ------- ");
		for ( Relationship r : thisRP.getJaxbElement().getRelationship()  ) {

			if (r.getTargetMode() !=null && r.getTargetMode().equals("External")) {
				// do nothing				
			} else {
				if (uniques.contains(r)) {
					// add tree, including any external parts	
					// (we already have the part itself)
					addTree(alteredParts, thisRP.getPart(r).getRelationshipsPart() );
										
				} else {
					// its present in both trees.
					// irrespective of whether content of part is the same, content of a rel could still have changed
					Part thisPart = thisRP.getPart(r);
					Part otherPart = otherRP.getPart( 
							RelationshipsPart.getRelationshipByTarget(otherRP, r.getTarget()) );
					
					if (thisPart.getRelationshipsPart()!=null) {
						
						if (otherPart.getRelationshipsPart()==null) {
							// add tree, including any external parts					
							addTree(alteredParts, thisPart.getRelationshipsPart() );
							
						} else {
							recurse(alteredParts, thisPart.getRelationshipsPart(), otherPart.getRelationshipsPart());
						}
						
					}
					
				}
			}
		}
	}

	private static void addPartsForRels(List<Part> alteredParts, List<Relationship> rels, RelationshipsPart rp  ) {
		for( Relationship r : rels) {
			if (r.getTargetMode() !=null && r.getTargetMode().equals("External") ) {
				log.debug( r.getTarget() + " is external");
				// Have everything we need info wise in transmitting the rels part
			} else {
				alteredParts.add( rp.getPart(r) );
				log.debug("added part: " + r.getTarget() );								
			}
		}		
	}
		
	/**
	 * 
	 * @param alteredParts
	 * @param theseRels
	 * @throws Docx4JException
	 */
	private static void addTree(List<Part> alteredParts,
			RelationshipsPart rp)
			throws Docx4JException {
		
		if (rp==null) return;
		
		for( Relationship r : rp.getJaxbElement().getRelationship() ) {
			if (r.getTargetMode().equals("External") ) {
				log.debug( r.getTarget() + " is external");
				// Have everything we need info wise in transmitting the rels part
			} else {
				alteredParts.add( rp.getPart(r) );
				log.debug("add tree: " + r.getTarget() );				
				
				// recurse
				RelationshipsPart nextRP = rp.getPart(r).getRelationshipsPart();
				if (nextRP!=null) {
					alteredParts.add(nextRP);					
					addTree(alteredParts, nextRP );
				}
			}
		}		
		
	}
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//
//		String thisfilepath = "/home/dev/workspace/docx4j/sample-docs/differencing_newer.docx";
//		String otherfilepath = "/home/dev/workspace/docx4j/sample-docs/differencing_older.docx";
//						
//		WordprocessingMLPackage thisPackage = WordprocessingMLPackage.load(new java.io.File(thisfilepath));
//		WordprocessingMLPackage otherPackage = WordprocessingMLPackage.load(new java.io.File(otherfilepath));
//		
//		start(thisPackage, otherPackage);
//	}
	
}
