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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.convert.in.FlatOpcXmlImporter;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.AlteredParts.Alteration;
import org.docx4j.openpackaging.parts.relationships.AlteredParts.Alterations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Patcher {

	private static Logger log = LoggerFactory.getLogger(Patcher.class);
	
	
	public static void apply(WordprocessingMLPackage otherPackage, 
			Alterations alterations) throws Docx4JException, JAXBException {
		
		if (alterations.getContentTypes()!=null) {
			log.info("replacing [Content_Types].xml");
			ContentTypeManager newCTM = new ContentTypeManager();
			newCTM.parseContentTypesFile(
					new ByteArrayInputStream(alterations.getContentTypes()));
			otherPackage.setContentTypeManager(newCTM);
		}
		
		if (alterations.isEmpty() ) return;
		
		// -- Deletions
		List<PartName> removedParts = new ArrayList<PartName>();
		
		for (Alteration a : alterations.getPartsDeleted() ) {
			
			org.docx4j.xmlPackage.Part xmlpart = a.getPart();
			
			// These deleted parts are likely to be attached to
			// to otherPackage, but there is no requirement for 
			// that to be so.  In other words, you could try
			// to apply the alterations to some third docx.
			
			// It might have already been deleted as a consequence
			// of recursive deletion...
			if (removedParts.contains(xmlpart.getName())) continue;
						
			
			// Design decision: how to find owning rels part?
			// Since part might not be from this package, can't do:
			// part.getOwningRelationshipPart().removePart(p.getPartName())
			// We could store the info when AlteredParts is run,
			// but lets try to get away without that...
			// If a part has been deleted, we know its owning rels will
			// have been modified or deleted.  So look for that.
			// BUT IT WON'T BE THERE IF ITS BEEN DELETED! DOH!
			// So we have to store the info when AlteredParts is run
			// after all.

			Part parentPart = otherPackage.getParts().get(a.getSourcePartName());
			if (a.getPart().getContentType().equals(ContentTypes.RELATIONSHIPS_PART)) {
				parentPart.setRelationships(null);
			} else {
				removedParts.addAll(
						parentPart.getRelationshipsPart().removePart(
								new PartName(xmlpart.getName())) );
			}
			
		}
		
		
		// -- Modifications
		for (Alteration a : alterations.getPartsModified() ) {
			
			log.info("Applying modifications to part: " + a.getPart().getName() );
			
			if (a.getPart().getContentType().equals(ContentTypes.RELATIONSHIPS_PART)) {
				
				
				RelationshipsPart newRP = null; //FlatOpcXmlImporter.createRelationshipsPart(a.getPart());
				
				if (a.getSourcePartName().getName().equals("/")) {
					newRP = otherPackage.getRelationshipsPart(true);
//					otherPackage.setRelationships(newRP);
//					newRP.setSourceP(otherPackage);					
				} else {
					Part parentPart = otherPackage.getParts().get(a.getSourcePartName());
					newRP = parentPart.getRelationshipsPart(true);
//					parentPart.setRelationships(newRP);
//					newRP.setSourceP(parentPart);					
				}
				FlatOpcXmlImporter.populateRelationshipsPart(newRP,  a.getPart().getXmlData().getAny());
				
			} else {

				Part targetPart = otherPackage.getParts().get(
						new PartName(a.getPart().getName()));
				
				if (targetPart==null) {
					log.error("Couldn't find " +  a.getPart().getName() + " @ " + a.getSourcePartName().getName() );
					continue;
				}
				
				Part tmpPart = FlatOpcXmlImporter.getRawPart(otherPackage.getContentTypeManager(), 
						a.getPart(), null); 
				
				if (targetPart instanceof JaxbXmlPart) {
					((JaxbXmlPart)targetPart).setJaxbElement(
							((JaxbXmlPart)tmpPart).getJaxbElement() );
					
				} else if (targetPart instanceof XmlPart) {
					
					((XmlPart)targetPart).setDocument(
							((XmlPart)tmpPart).getDocument() );
				
				} else if (targetPart instanceof CustomXmlDataStoragePart) {
					
					((CustomXmlDataStoragePart)targetPart).setData(
							((CustomXmlDataStoragePart)tmpPart).getData() );
						// TODO: check that
					
				} else if (targetPart instanceof BinaryPart) {

					((BinaryPart)targetPart).setBinaryData(
							((BinaryPart)tmpPart).getBuffer() );
					
				} else {
					log.error("TODO: handle " + targetPart.getClass().getName() );
				}
			}
		}
		
		// -- Additions
		for (Alteration a : alterations.getPartsAdded() ) {
			
			log.info("Adding part: " + a.getPart().getName() );
			
			if (a.getPart().getContentType().equals(ContentTypes.RELATIONSHIPS_PART)) {
				
				RelationshipsPart newRP = null; //FlatOpcXmlImporter.createRelationshipsPart(a.getPart());
				
				if (a.getSourcePartName().getName().equals("/")) {
					newRP = otherPackage.getRelationshipsPart(true);
//					otherPackage.setRelationships(newRP);
//					newRP.setSourceP(otherPackage);
				} else {
					Part parentPart = otherPackage.getParts().get(a.getSourcePartName());
					newRP = parentPart.getRelationshipsPart(true);
//					parentPart.setRelationships(newRP);
//					newRP.setSourceP(parentPart);
				}
				FlatOpcXmlImporter.populateRelationshipsPart(newRP,  a.getPart().getXmlData().getAny());
				
				
			} else {

				Part parentPart = otherPackage.getParts().get(a.getSourcePartName());
				Part newPart = FlatOpcXmlImporter.getRawPart(otherPackage.getContentTypeManager(), 
						a.getPart(), null); 
				
				// There will already be a rel for the new part,
				// since we will already have modified or added the rels part
				// so don't do AddTargetPart (which will probably create a new rel id,
				// which will cause problems)
				
				newPart.setOwningRelationshipPart(parentPart.getRelationshipsPart());
				newPart.getSourceRelationships().add(
						parentPart.getRelationshipsPart().getRel(new PartName(a.getPart().getName())));
				otherPackage.getParts().put(newPart);
				newPart.setPackage( otherPackage );

				// TODO: add content type if necessary
			}			
			
		}	
		
	}
	
//	private static void extractRelationshipParts(List<RelationshipsPart> relsParts, 
//	List<org.docx4j.xmlPackage.Part> flatOpcParts) throws InvalidFormatException, JAXBException {
//
//for (org.docx4j.xmlPackage.Part x : flatOpcParts ) {
//	
//	if (x.getContentType().equals(ContentTypes.RELATIONSHIPS_PART)) {
//		relsParts.add(FlatOpcXmlImporter.createRelationshipsPart(x));
//	}
//}
//
//}
	
}
