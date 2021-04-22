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

package org.docx4j.openpackaging.parts.relationships;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.relationships.Relationship;

/**
 * Output is Flat OPC XML format:
 * 
 * <pkg:part pkg:name="/word/document.xml" pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml">
        <pkg:xmlData>
            <w:document
            
   and
   
   <pkg:part pkg:name="/word/_rels/document.xml.rels" pkg:contentType="application/vnd.openxmlformats-package.relationships+xml" pkg:padding="256">

 */
public class AlteredParts {
	
	private static Logger log = LoggerFactory.getLogger(AlteredParts.class);
		
	public static Alterations start(
									WordprocessingMLPackage thisPackage, 
									WordprocessingMLPackage otherPackage)
			throws Docx4JException {

		Alterations alterations = new Alterations();
		
		// First, handle [Content_Types].xml
		if (!thisPackage.getContentTypeManager().isContentEqual(otherPackage.getContentTypeManager())) {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	    	try {
				thisPackage.getContentTypeManager().marshal(baos);
			} catch (JAXBException e) {
				// Shouldn't happen
				throw new Docx4JException("Can't marshall content type", e);
			}
		}
		
		// Second, handle parts
		RelationshipsPart packageRels = thisPackage.getRelationshipsPart();
		RelationshipsPart otherPackageRels = otherPackage.getRelationshipsPart();
		recurse(alterations, packageRels, otherPackageRels);

		return alterations;
	}	

	public static void recurse(Alterations alterations,
			RelationshipsPart thisRP, RelationshipsPart otherRP)
			throws Docx4JException {
		
		log.info("######### @" + thisRP.getPartName().getName() + "#########");

		log.info("uniques -------");
		List<Relationship> uniques = thisRP.uniqueToThis(otherRP);
		addPartsForRels(alterations.getPartsAdded(), uniques, thisRP  );		

		List<Relationship> missings = thisRP.uniqueToOther(otherRP);
		addPartsForRels(alterations.getPartsDeleted(), missings, otherRP  );		
		
		// is this rels part itself altered?
		if (!thisRP.isContentEqual(otherRP)) {
			alterations.getPartsModified().add( 
					new Alteration(thisRP, toStorageFormat(thisRP)));			
		}
		
		log.info("content -------");
		List<Relationship> altered = thisRP.differingContent(otherRP);
		addPartsForRels(alterations.getPartsModified(), altered, thisRP  );	
		
		// Now recurse all rels
		log.info("recurse ------- ");
		for ( Relationship r : thisRP.getJaxbElement().getRelationship()  ) {

			if (r.getTargetMode() !=null && r.getTargetMode().equals("External")) {
				// do nothing				
			} else {
				if (uniques.contains(r)) {
					// add tree, including any external parts	
					// (we already have the part itself)
					addTree(alterations.getPartsAdded(), thisRP.getPart(r).getRelationshipsPart() );
										
				} else if (missings.contains(r)) {
					addTree(alterations.getPartsDeleted(), thisRP.getPart(r).getRelationshipsPart() );
				} else {
					// its present in both trees.
					// irrespective of whether content of part is the same, content of a rel could still have changed
					Part thisPart = thisRP.getPart(r);
					Part otherPart = otherRP.getPart( 
							RelationshipsPart.getRelationshipByTarget(otherRP, r.getTarget()) );
					
					if (thisPart.getRelationshipsPart()==null) {
						
						if (otherPart.getRelationshipsPart()!=null) {
							// add tree, including any external parts	
							alterations.getPartsDeleted().add( 
									new Alteration( thisPart.getPartName(), toStorageFormat(thisPart.getRelationshipsPart())));												
							addTree(alterations.getPartsDeleted(), thisPart.getRelationshipsPart() );
						}
						
					} else {
						
						if (otherPart.getRelationshipsPart()==null) {
							// add tree, including any external parts	
							alterations.getPartsAdded().add( 
									new Alteration( thisPart.getPartName(), toStorageFormat(thisPart.getRelationshipsPart())));												
							addTree(alterations.getPartsAdded(), thisPart.getRelationshipsPart() );
							
						} else {
							recurse(alterations, thisPart.getRelationshipsPart(), otherPart.getRelationshipsPart());
						}
						
					}
					
				}
			}
		}
	}

	private static void addPartsForRels(List<Alteration> list, List<Relationship> rels, RelationshipsPart rp  ) throws Docx4JException {

		for( Relationship r : rels) {
			if (r.getTargetMode() !=null && r.getTargetMode().equals("External") ) {
				log.debug( r.getTarget() + " is external");
				// Have everything we need info wise in transmitting the rels part
			} else {
				list.add( 
						new Alteration( rp, toStorageFormat(rp.getPart(r)) ));
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
	private static void addTree(List<Alteration> list,
			RelationshipsPart rp)
			throws Docx4JException {
		
		if (rp==null) return;
		
		for( Relationship r : rp.getJaxbElement().getRelationship() ) {
			if (r.getTargetMode() !=null && r.getTargetMode().equals("External") ) {
				log.debug( r.getTarget() + " is external");
				// Have everything we need info wise in transmitting the rels part
			} else {
				list.add( 
						new Alteration( rp, toStorageFormat(rp.getPart(r)) ));
				log.debug("add tree: " + r.getTarget() );				
				
				// recurse
				Part nextSourcePart = rp.getPart(r);
				RelationshipsPart nextRP = nextSourcePart.getRelationshipsPart();
				if (nextRP!=null) {
					list.add( 
							new Alteration( nextSourcePart.getPartName(), toStorageFormat(nextRP)));												
					addTree(list, nextRP );
				}
			}
		}		
		
	}
	
	private static org.docx4j.xmlPackage.Part toStorageFormat(Part part)  throws Docx4JException { 
		
		if (part instanceof BinaryPart ) {
			log.info(".. saving binary stuff" );
			return FlatOpcXmlCreator.createRawBinaryPart( part );
			
		} else {
			return FlatOpcXmlCreator.getRawXmlPart(part );
		}
	}
	
	
	public static class Alterations {
		
		// There is no Flat OPC part for [Content_Types].xml
		// so its up to us to choose a representation
		private byte[] contentTypes;
		public void setContentTypes(ByteArrayOutputStream baos) {
			contentTypes = baos.toByteArray();
		}
		public byte[] getContentTypes() {
			return contentTypes;
		}

		private List<Alteration> partsAdded = new ArrayList<Alteration>();
		private List<Alteration> partsDeleted = new ArrayList<Alteration>();
		private List<Alteration> partsModified = new ArrayList<Alteration>();

		public List<Alteration> getPartsAdded() {
			return partsAdded;
		}
		public List<Alteration> getPartsDeleted() {
			return partsDeleted;
		}
		public List<Alteration> getPartsModified() {
			return partsModified;
		}
		
		public void debug() {
			System.out.println("- Additions -------");
			for (Alteration a : partsAdded) {
				System.out.println(a.getPart().getName() + " @ " + a.getSourcePartName().getName() );
			}

			System.out.println("- Modifications -------");
			for (Alteration a : partsModified) {
				System.out.println(a.getPart().getName() + " @ " + a.getSourcePartName().getName() );
			}
			
			System.out.println("- Deletions -------");
			for (Alteration a : partsDeleted) {
				System.out.println(a.getPart().getName() + " @ " + a.getSourcePartName().getName() );
			}
		}
		
		public boolean isEmpty() {
			
			return partsAdded.isEmpty() && partsDeleted.isEmpty() && partsModified.isEmpty();
		}
	}

	public static class Alteration {
		
		// Required in order to apply patch
		private PartName sourcePartName;
		public PartName getSourcePartName() {
			return sourcePartName;
		}

		private org.docx4j.xmlPackage.Part part;
		public org.docx4j.xmlPackage.Part getPart() {
			return part;
		}

		
//		public Alteration(String point, org.docx4j.xmlPackage.Part part) {
//			this.point = point;
//			this.part = part;
//		}
		
		public Alteration(RelationshipsPart rp, org.docx4j.xmlPackage.Part part) {
			this.sourcePartName = rp.getSourceP().getPartName();
			this.part = part;
		}
		public Alteration(PartName sourcePartName, org.docx4j.xmlPackage.Part part) {
			this.sourcePartName = sourcePartName;
			this.part = part;
		}
		
	}
	
}
