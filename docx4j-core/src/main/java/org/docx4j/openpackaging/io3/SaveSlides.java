package org.docx4j.openpackaging.io3;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.pptx4j.pml.Presentation.SldIdLst;
import org.pptx4j.pml.Presentation.SldIdLst.SldId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save a PresentationMLPackage containing the specified slides only.
 * 
 * @return
 * @since 8.1.6
 */
public class SaveSlides extends Save {
	
	protected static Logger log = LoggerFactory.getLogger(PresentationMLPackage.class);

	private SaveSlides(OpcPackage p) {
		super(p);
	}

	public SaveSlides(PresentationMLPackage pmlPkg, int[] slideNumber) { // throws Pptx4jException {
		super(pmlPkg);

		/* Source presentation contains, for example:
		 * 
	        <p:presentation saveSubsetFonts="true" autoCompressPictures="false">

	            <p:sldIdLst>
	                <p:sldId id="256" r:id="rId2"/>
	                <p:sldId id="257" r:id="rId3"/>
	            </p:sldIdLst>
	            
	*/		
		
        MainPresentationPart presentation = pmlPkg.getMainPresentationPart();
        RelationshipsPart rels = presentation.getRelationshipsPart();
        existingldIdLst = presentation.getJaxbElement().getSldIdLst();

        // First, create a new SldIdLst, containing just the slides we want
        partialSldIdLst = new SldIdLst(); 		
		for (int i : slideNumber) {
			
	        SldId sldId = null;
	        try {
	        	sldId = existingldIdLst.getSldId().get(i);
	        } catch (ArrayIndexOutOfBoundsException e) {
	        	log.error("Presentation has fewer slides than (0-based) " + i);
	        	throw new RuntimeException("Presentation has fewer slides than (0-based) " + i);
	        }
	        
	        SldId sldIdClone = new SldId();
	        sldIdClone.setId(sldId.getId());
	        sldIdClone.setRid(sldId.getRid());
	        partialSldIdLst.getSldId().add(sldIdClone);
	        
	        slideRels.add(sldId.getRid()); 
			
		}
		
		// We'll use this:
		// 1. in the MainPres part we write, and
		// 2. when iterating through its rels
		
	}
	
	private SldIdLst existingldIdLst;
	private SldIdLst partialSldIdLst; 
	
	/**
	 * The relIds of the slides we want
	 */
	private Set<String> slideRels = new HashSet<String>();
	
	public void savePart(Part part)
			throws Docx4JException, IOException {
		
		// Drop the leading '/'
		String resolvedPartUri = part.getPartName().getName().substring(1);
		
		if (handled.get(resolvedPartUri)!=null) {
			log.debug(".. duplicate save avoided .." );
			return;
		}
				
		if (part instanceof BinaryPart ) {
			log.debug(".. saving binary stuff" );
			p.getTargetPartStore().saveBinaryPart( part );
			
		} else if (part instanceof MainPresentationPart ){
			log.info(".. special treatment saving MPP " );	
			// inject partialSldIdLst
			MainPresentationPart presentation = (MainPresentationPart)part;
			presentation.getJaxbElement().setSldIdLst(partialSldIdLst);

			// Save it in the usual way
			saveRawXmlPart( part );

			// revert, so as not to alter source presentation
			presentation.getJaxbElement().setSldIdLst(existingldIdLst);
			
		} else {
			saveRawXmlPart( part );
		}
		handled.put(resolvedPartUri, resolvedPartUri);
		
		// recurse via this parts relationships, if it has any
		RelationshipsPart rrp = part.getRelationshipsPart(false); //don't create
		if (rrp!= null ) {
			
			if (part instanceof MainPresentationPart ) {

				log.info(".. special treatment saving MPP rels" );
				
				Relationships existingRels = rrp.getRelationships();
				Relationships partialRels = new Relationships();
				
				// first, we need to modify the rels part; we build a rels list.
				for ( Relationship r : existingRels.getRelationship() ) {
				
					if (Namespaces.PRESENTATIONML_SLIDE.equals(r.getType())) {
					
						if (slideRels.contains(r.getId())) {
							// add it
							//log.debug("retaining slide with relId " + r.getId());
							Relationship clonedRel = XmlUtils.deepCopy(r, Context.jcRelationships);
							partialRels.getRelationship().add(clonedRel);
						} else {
							// ignore it
							//log.debug("skipping slide with relId " + r.getId());
						}
					} else {
						// add rels of other types
						Relationship clonedRel = XmlUtils.deepCopy(r, Context.jcRelationships);
						partialRels.getRelationship().add(clonedRel);
					}
				}
				
				rrp.setJaxbElement(partialRels); // inject it
				saveRawXmlPart(rrp );
				addPartsFromRelationships(rrp );
				rrp.setJaxbElement(existingRels); // revert :-)
				
			} else if (rrp.getRelationships().getRelationship().size()>0) {
				
				saveRawXmlPart(rrp );
				
				addPartsFromRelationships(rrp );
			}
		} 
	}

	
}
