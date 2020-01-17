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

/*
 * Portions Copyright (c) 2006, Wygwam
 * With respect to those portions:
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * - Neither the name of Wygwam nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without 
 * specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.docx4j.openpackaging.parts.relationships;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a Relationship Part, which contains the relationships for a 
 * given PackagePart or the Package.
 */
public final class RelationshipsPart extends JaxbXmlPart<Relationships> { 

	private static Logger log = LoggerFactory.getLogger(RelationshipsPart.class);

	/**
	 * Constructor.  Creates an appropriately named .rels XML document.
	 * 
	 * Often invoked via sourceP.getRelationshipsPart(true)  // create rels part
	 * 
	 * @param sourceP
	 * 			  Source part for these relationships
	 *             
	 * @throws InvalidFormatException
	 *             If the specified URI is not valid.
	 */
	public RelationshipsPart(Base sourceP)
			throws InvalidFormatException {
		
		super(new PartName(PartName.getRelationshipsPartName(
				sourceP.getPartName().getName() )) ); // though we won't be using that, since it is dynamic
		
		this.sourceP = sourceP;
		init();
				
		sourceP.setRelationships(this);
			// TODO - use setRelationships from here 
			// like this in other constructors
			// in this class.
						
		org.docx4j.relationships.ObjectFactory factory =
			new org.docx4j.relationships.ObjectFactory();
		
		jaxbElement = factory.createRelationships();
		
		if (log.isDebugEnabled() ) {
			log.debug("Constructed rp for " + sourceP.getPartName().getName()  );
		}
	}

		
	/**
	 * You probably don't want this one (though it is useful for reflection). If you do use it, you need to setPartName
	 * @throws InvalidFormatException
	 */
	public RelationshipsPart() throws InvalidFormatException {
		super(new PartName("/blagh"));  
		init();
		
		if (log.isDebugEnabled() ) {
			log.debug("Constructed rp (no-arg)"   );
		}
		
	}
	
	/**
	 * @return
	 * @throws InvalidFormatException
	 * @since 3.2.0, at which time related constructor was made private
	 */
	public static RelationshipsPart createPackageRels() throws InvalidFormatException {
		RelationshipsPart rp = new RelationshipsPart(new PartName("/_rels/.rels"));
		return rp;
	}
	private RelationshipsPart(PartName partname) throws InvalidFormatException {
		super(partname);
		init();
		
		if (log.isDebugEnabled() ) {
			log.debug("Constructed rp using partname " + partname.getName()  );
		}
		
	}

	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART));

		setJAXBContext(Context.jcRelationships);				
	}
	

	@Override
	public PartName getPartName() {
		
		// Calculate rels part name dynamically where possible;
		// this ensures it is named appropriately
		// if its source part's name changes 
		// (eg AddPartBehaviour.RENAME_IF_NAME_EXISTS)
		if (this.getSourceP()!=null
				&& (!(this.getSourceP() instanceof OpcPackage))) {
			try {
				return new PartName(PartName.getRelationshipsPartName(
						sourceP.getPartName().getName() ));
			} catch (InvalidFormatException e) {
				throw new RuntimeException(e);
			}
		} else {
			return super.getPartName();
		}
	}
	
	
	public static RelationshipsPart createRelationshipsPartForPart(
			Base sourcePart) {

		if (sourcePart.relationships != null)
			return sourcePart.relationships;

		RelationshipsPart rp = null;
		try {
			rp = new RelationshipsPart(sourcePart);
		} catch (InvalidFormatException e) {
			// shouldn't happen
			log.error(e.getMessage(), e);
		}
		rp.setPackage(sourcePart.getPackage());

		// Make sure content manager knows how to handle .rels
		sourcePart
				.getPackage()
				.getContentTypeManager()
				.addDefaultContentType(
						"rels",
						org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART);

		return rp;
	}	
	
	public Relationships getRelationships() {
		return jaxbElement;
	}

	public void setRelationships(Relationships jaxbElement) {
		this.jaxbElement = jaxbElement;
	}	
	
	/**
	 * Source part for these relationships
	 */
	private Base sourceP;
	
	public Base getSourceP() {
		return sourceP;
	}
	public void setSourceP( Base sourcePart) {
		sourceP = sourcePart;
	}
	
	public URI getSourceURI() {
		if (sourceP == null) {
			return URIHelper.PACKAGE_ROOT_URI;
		}
		return sourceP.getPartName().getURI();
	}
		
	/** This Relationship Part is the package relationship part
	 * if its source is the Package. 
	 */	 
	public boolean isPackageRelationshipPart() {
		return (sourceP instanceof OpcPackage);
	}

	/** Gets a loaded Part by its id */
	public Part getPart(String id) {

		log.debug("looking for: " + id);
		
		Relationship r = getRelationshipByID(id);
    	log.debug(id + " points to " + r.getTarget());
		
		return getPart(r);		
	}
	
	/**
	 * Retrieves a package relationship based on its id.
	 * 
	 * @param id
	 *            ID of the package relationship to retrieve.
	 * @return The package relationship identified by the specified id.
	 */
	public Relationship getRelationshipByID(String id) {
		
		for ( Relationship r : jaxbElement.getRelationship()  ) {
			
			if (r.getId().equals(id) ) {
				return r;
			}			
		}		
		return null;
	}

	/**
	 * Get the first rel with specified relationship type
	 * (see Namespaces for pre-defined constants)
	 * @param type
	 * @return
	 */
	public Relationship getRelationshipByType(String type) {
		
		for ( Relationship r : jaxbElement.getRelationship()  ) {
			
			if (r.getType().equals(type) ) {
				return r;
			}			
		}		
		return null;
	}

	/**
	 * @param type
	 * @return
	 * @since 3.3.0
	 */
	public List<Relationship> getRelationshipsByType(String type) {
		
		List<Relationship> rels = new ArrayList<Relationship>();
		
		for ( Relationship r : jaxbElement.getRelationship()  ) {
			
			if (r.getType().equals(type) ) {
				rels.add(r);
			}			
		}		
		return rels;
	}
	

	public Part getPart(Relationship r ) {
		
//		log.debug(" source is  " + sourceP.getPartName().toString() );
    	// eg rId1 points to fonts/font1.odttf
    		
		if (r.getTargetMode() == null
				|| !r.getTargetMode().equals("External") ) {
			
			// Usual case
			URI uri = null;
	
			try {
				uri = org.docx4j.openpackaging.URIHelper
						.resolvePartUri(sourceP.getPartName().getURI(), new URI(
								r.getTarget()));
			} catch (URISyntaxException e) {
				log.error("Cannot convert " + r.getTarget()
						+ " in a valid relationship URI-> ignored", e);
			}		
	    		    	
	    	try {
				return getPackage().getParts().get( new PartName(uri, true ));
			} catch (InvalidFormatException e) {
				log.error("Couldn't get part using PartName: " + uri, e);
				return null;
			}
			
		} else {
			// EXTERNAL
			return getPackage().getExternalResources().get(
					new ExternalTarget( r.getTarget() ) );
			// TODO - doesn't handle a relative reference,
			// because the keys are absolute references
		}
	}
	
	// ----------------------------------------------------------
	

	/* Requirements for Id allocation: 
	 * 
	 * 1. uniqueness
	 * 
	 * 2. if a rel is removed, best not to reuse it
	 *    (ie don't just use size() 
	 * */
	private int nextId = 1;

	public String getNextId() {
		
		// Relationship part always 
		// determines the Relationship Id		
		String id = "rId" + nextId;
		
    	do {
    		id = "rId" + nextId;
    		nextId++;
    		
    	} while (isRelIdOccupied(id));
		
		return id;
		
	}
	
	public boolean isRelIdOccupied(String relId) {
		
		for (Relationship existing : jaxbElement.getRelationship() ) {			
			if (existing.getId().equals(relId) ) {
				log.debug(relId + " already in use, with target " + existing.getTarget() );
				return true;
			}			
		}
		return false;
	}
	
	/**
	 * Assumes relationship ids are all of the form
	 * 'rIdn' where n is a positive integer.
	 */
	public void resetIdAllocator() {
		
		int highestId = 0;
		for (Relationship rel : jaxbElement.getRelationship() ) {

			String id = rel.getId();
			try {
				String idNum = id.substring(3);
				
				int current = Integer.parseInt(idNum);
				
				if (current > highestId) {
					highestId = current;
				}
			} catch (Exception e) {
				// Not a number, not a problem
				log.warn("Couldn't process id: " + id);
			}			
		}
		nextId = highestId+1;		
		log.debug("nextId reset to : " + nextId);
		
	}
	
	// ----------------------------------------------------------
	
	/**
	 * Loads a pre-existing target part into the package
	 * (but does not load its contents as such; that is
	 *  done elsewhere).
	 * 
	 * The target part is assumed to be specified already in this 
	 * relationship part. 
	 * 
	 * Generally this will be used by io.load classes.
	 * 
	 * @param part
	 *            The part to add.
	 */
	public void loadPart(Part part, Relationship sourceRelationship) {

		if (part == null) {
			log.error("Failed trying to load null part." );			
			throw new IllegalArgumentException("part");
		}
		
		PartName partName = part.getPartName();
		log.debug("Loading part " + partName.getName() );
		
		part.setOwningRelationshipPart(this);
		part.getSourceRelationships().add(sourceRelationship);  

		// All (non-relationship) parts are stored in a collection
		// in the package, even though conceptually this loadPart
		// method should be invoked on the relationship source.		
	
		getPackage().getParts().put(part);
		
		// Tell the part what package it belongs to!
		// TODO - do this in the Part constructor.  It can be too late
		// leaving it until the Part is added to the Package.
		part.setPackage( getPackage() );
		
	}

	
	/**
	 * Add a newly created part, a relationship and the content type.
	 * 
	 * @param part
	 *            The part to add.
	 * @param overwriteExistingTarget
	 *            Whether to replace any part with the same target
	 * @param ctm
	 *            Content type manager
	 * @return The Relationship
	 */
	public Relationship addPart(Part part, AddPartBehaviour mode, 
			ContentTypeManager ctm) throws InvalidFormatException {
		return this.addPart(part, mode, ctm, null);
	}
	
	/**
	 * Add a newly created part, a relationship and the content type.
	 * 
	 * @param part
	 *            The part to add.
	 * @param overwriteExistingTarget
	 *            Whether to replace any part with the same target
	 * @param ctm
	 *            Content type manager
	 * @param relId
	 *            the relId we wish to use (provided it is not in use)
	 *            
	 * @return The Relationship
	 */
	public Relationship addPart(Part part, AddPartBehaviour mode, 
			ContentTypeManager ctm, String relId) throws InvalidFormatException {
		
		PartName newPartName = part.getPartName(); 
		log.info("adding part with proposed name: " + newPartName.getName());
		
		if (this.getPackage().getParts().get( newPartName )!=null) {
			
			if (mode.equals(AddPartBehaviour.REUSE_EXISTING)) {
				
				part = this.getPackage().getParts().get( newPartName );
				
				// if rel already exists, return that				
				// if rel doesn't exist (the part does), create a rel to it
				if (log.isDebugEnabled()) {
					boolean exists = (part!=null);
					log.debug("part exists: " + exists);
				}
			}
			
			if (mode.equals(AddPartBehaviour.RENAME_IF_NAME_EXISTS)) {

				// it is not enough to be unique in just the rp (eg media parts)

				String proposedName = part.getPartName().getName();
				log.debug("Detected duplicate partname: " + proposedName );
				if (proposedName.indexOf(".")>0) {
					// TODO: strip trailing numerals off prefix
					// eg footer1 should become footer
					newPartName = getNewPartName( proposedName.substring(0, proposedName.indexOf(".")), 
							"." + part.getPartName().getExtension(), 
							this.getPackage().getParts().getParts() );				
				} else {
					newPartName = getNewPartName( proposedName, 
							"." , 
							this.getPackage().getParts().getParts() );										
				}
				part.setPartName(newPartName); // access directly
				log.debug(".. renamed to " + newPartName );
				
				// this partname is globally unique in the docx
				// so rel won't exist
				
				// Could have added in 3.2.0, but not required, since instead we work out the name dynamically  
				//	if (part.getRelationshipsPart()!=null) {
				//		part.getRelationshipsPart().setPartName(
				//				new PartName(PartName.getRelationshipsPartName( newPartName.getName() )));					
				//	}
								
			}				
		}
				
		// First work out what the target would be

		URI tobeRelativized = part.getPartName().getURI();
		URI relativizeAgainst = sourceP.getPartName().getURI();
		
		log.debug("Relativising target " + tobeRelativized 
				+ " against source " + relativizeAgainst);
		
		String target = URIHelper.relativizeURI(relativizeAgainst, 
				tobeRelativized).toString(); 
		
		if (relativizeAgainst.getPath().equals("/")
				&& target.startsWith("/")) {
			
			/*
			 * Relativising target /word/document.xml against source / 
			 * Result /word/document.xml
			 * 
			 * but we want word/document.xml
			 */		
			
			target = target.substring(1);
		}
				
		log.debug("Result " + target); 
		
		// Check whether we already have a rel with this target
		// This code is a bit more efficient than getRel, since it
		// doesn't unrelativise each existing rel!
		Relationship existsAlready = null;
		for (Relationship rel : jaxbElement.getRelationship() ) {
			if (rel.getTarget().equals(target)) {
				existsAlready = rel;
				break;  // Assumes at most 1 existing rel with this target
			}
		}

		if (log.isDebugEnabled()) {
			boolean exists = (existsAlready!=null);
			log.debug("rel exists: " + exists);
		}
		
		if (existsAlready!=null /* in this rels part */
				&& mode.equals(AddPartBehaviour.REUSE_EXISTING)) {
			log.debug("Returning preexisting rel");
			return existsAlready;
		}
		
		// sanity check
		if (existsAlready!=null && mode.equals(AddPartBehaviour.RENAME_IF_NAME_EXISTS)) {
			// Shouldn't happen
			throw new InvalidFormatException("Found existing rel, and yet constructed part name should be globally unique!");
		}
		
		if (this.getPackage().getParts().get( newPartName )!=null
				&& mode.equals(AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS)) {
			
			// ie we have the same part in the package already, not
			// necessarily referenced by a rel in this rp.
			
			// overwrite the part
			if (existsAlready!=null) {
				
				// we reuse it. can't assume its the same type, though, so
				existsAlready.setType( part.getRelationshipType() );				
				
				loadPart(part, existsAlready);
				return existsAlready;
			}
						
			// case where rel doesn't exist (it might not in this rp), create a rel to it
			// is handled below
		}		
		
		// OK, create the new rel
		
		org.docx4j.relationships.ObjectFactory factory =
			new org.docx4j.relationships.ObjectFactory();
		
		Relationship rel = factory.createRelationship();
		
		rel.setTarget(target );
		//rel.setTargetMode( TargetMode.INTERNAL );
		rel.setType( part.getRelationshipType() );
		
		if (relId!=null) {
			rel.setId( relId );			
		}
		
		addRelationship(rel );
		
    	String ext = part.getPartName().getExtension();
		
		// Add an override to ContentTypeManager
		if ( part.getContentType().equals( ContentTypes.IMAGE_JPEG) ) {
			ctm.addDefaultContentType(ext,ContentTypes.IMAGE_JPEG );
		} else if ( part.getContentType().equals( ContentTypes.EXTENSION_GIF ) ) {
			ctm.addDefaultContentType(ext, ContentTypes.EXTENSION_GIF);
		} else if ( part.getContentType().equals( ContentTypes.EXTENSION_PNG ) ) {
			ctm.addDefaultContentType(ext, ContentTypes.IMAGE_PNG);			
			// TODO - other content types!
		} else {
			ctm.addOverrideContentType(part.getPartName().getURI(), part.getContentType());
		}
		
		if (this.getPackage().getParts().get( newPartName )!=null) {

			if (mode.equals(AddPartBehaviour.REUSE_EXISTING)) {
				// just return rel;
			}		
			
			if (mode.equals(AddPartBehaviour.RENAME_IF_NAME_EXISTS)) {
				loadPart(part, rel);
			}			
			
			if (mode.equals(AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS)) {
				loadPart(part, rel);
			}
			return rel;	
			
		} else {
			// Usual case
			
			loadPart(part, rel);
			return rel;				
			
		}
		
		// Is more than one rel with the same target 
		// ever permitted. For example, an image? YES
				
		// Word fails to load a document if it has 2 copies of the styles part
		// (each with a separate rels entry).
		
	}
	

	/**
	 * Add the specified relationship to the collection.
	 * 
	 * @param rel
	 *            The relationship to add.
	 */
	public boolean addRelationship(Relationship rel) 
		throws InvalidOperationException {

		// ECMA-376 Part 2 8.3 says that Id must be unique.
		
		if ( rel.getId()==null) {
			String id = getNextId();
			rel.setId( id );
		}
		
		String relId = rel.getId();
		
		// Only add it if there is no rel with the same
		// id there already
		if (isRelIdOccupied(relId)) {
			log.error("Refusing to add another rel with id " + relId 
					+ ". Target is " + rel.getTarget() );
			throw new InvalidOperationException(
					"Refusing to add another rel with id " + relId 
					+ ". Target is " + rel.getTarget() );
		}
		jaxbElement.getRelationship().add(rel);
		rel.setParent(jaxbElement);
		return true;
	}
	
	
	 /** Remove all parts from this relationships
	 *   part */ 
	public List<PartName> removeParts() {

		List<PartName> removedParts = new ArrayList<PartName>();
		
		// Make a list in order to avoid concurrent modification exception
		java.util.ArrayList<Relationship> relationshipsToGo = new java.util.ArrayList<Relationship>();
		for (Relationship r : jaxbElement.getRelationship() ) {
			relationshipsToGo.add(r);
		}

		for (Relationship r : relationshipsToGo ) {
			
			try {
				String resolvedPartUri = URIHelper.resolvePartUri(
						getSourceURI(), new URI(r.getTarget())).toString();

				log.info("Removing part: " + resolvedPartUri);

				removedParts.addAll(
						removePart(new PartName(resolvedPartUri)) );
				
			} catch (URISyntaxException e) {
				log.error("Cannot convert " + r.getTarget()
						+ " in a valid relationship URI-> ignored", e);
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		return removedParts;
	}
	
	/**
	 * Remove a part from this package, including its relationships
	 * part and all target parts. Do so recursively. 
	 * 
	 * If this part is relationship part, then
	 * delete all relationships in the source part.
	 * 
	 * @param partName
	 *            The part name of the part to remove.
	 */
	public List<PartName> removePart(PartName partName) {
		
		log.info("trying to removePart " + partName.getName() );
		
		List<PartName> removedParts = new ArrayList<PartName>();
		
		if (partName == null)
			throw new IllegalArgumentException("partName was null");
		
		Part part = getPackage().getParts().get(partName);
		
		if (part!=null) {

			removeRelationship(partName);
			
			// Remove parts it references
			if (part.getRelationshipsPart()!=null) {
				removedParts.addAll(
						part.getRelationshipsPart().removeParts() ); // the recursive bit
				
				// part.setRelationships(null);  // Unnecessary
			}			

			// Remove from Content Type Manager
				// TODO			
			
			// Delete the specified part from the package.
			getPackage().getParts().remove(partName);
			removedParts.add(partName);
		}

		return removedParts;
		
//		this.isDirty = true;
	}

	/**
	 * @see getRel
	 */
	@Deprecated // introduced in 1295, and used in MergeDocx v1.0-1.1
	public boolean isATarget(PartName partName) {
		return (getRel(partName)!=null);
	}
	
	/**
	 * Is partname a target of any of these rels?
	 * @param partName
	 * @return
	 */
	public Relationship getRel(PartName partName) { // introduced after 2.6.0

		
		for (Relationship rel : jaxbElement.getRelationship() ) {
			
			if (rel.getTargetMode() !=null && rel.getTargetMode().equals("External")) {
				// This method can't be used to get external relationships
				continue;
			}
				
			// TODO 20110902: it would be more efficient to relativise the partName
			// just once, and compare using that (see addPart, which
			// works this way).
			
			if (isTarget(partName, rel) ) {
				return rel;
			}
		}
		return null;		
	}	
	
	/**
	 * Is partName the target of the specified rel?
	 * 
	 * @since 2.6.0
	 * 
	 * @param partName
	 * @param rel
	 * @return
	 */
	public boolean isTarget(PartName partName, Relationship rel) { // introduced for 2.6.0, in 1295
		
		URI resolvedTargetURI = null;

		try {
			if (sourceP==null) {
				// This rels part isn't attached!
				resolvedTargetURI = org.docx4j.openpackaging.URIHelper
				.resolvePartUri(new URI(inferSourcePartName(this.getPartName().getName())), new URI(
						rel.getTarget()));				
			} else {
				resolvedTargetURI = org.docx4j.openpackaging.URIHelper
						.resolvePartUri(sourceP.getPartName().getURI(), new URI(
								rel.getTarget()));
			}
		} catch (URISyntaxException e) {
			log.error("Cannot convert " + rel.getTarget()
					+ " in a valid relationship URI-> ignored", e);
		}		

		//log.debug("Comparing " + resolvedTargetURI + " == " + partName.getName());
		
		return partName.getName().equals(resolvedTargetURI.toString()) ; 
		
	}
	

	/**
	 * Remove a relationship by its reference.
	 * 
	 * @param rel
	 *            The relationship to delete.
	 */
	public void removeRelationship(Relationship rel) {
		if (rel == null)
			throw new IllegalArgumentException("rel");
		
		if (jaxbElement.getRelationship().remove(rel)) {
			// removed ok
		} else {
			log.warn("Couldn't find rel " + rel.getId() + " " + rel.getTarget());
		}

	}
	
	public void removeRelationship(PartName partName) {
		
		// Remove the relationship for which it is a target from here
		// Throw an error if this can't be found!
		Relationship relToBeRemoved = null;
		for (Relationship rel : jaxbElement.getRelationship() ) {
			
			if (rel.getTargetMode() !=null
					&& rel.getTargetMode().equals("External") ) {
				// This method can't be used to remove external resources
				continue;
			}
						
			if (isTarget(partName, rel) ) {
				// was rel.getTargetURI()
				
				log.info("True - will delete relationship with id " + rel.getId() 
						+ " and target " + rel.getTarget());
				relToBeRemoved = rel; // Avoid java.util.ConcurrentModificationException
				break;
			}
			
		}
		if (relToBeRemoved==null) {
			// The Part may be in the package somewhere, but its not
			// a target of this relationships part!
			throw new IllegalArgumentException(partName + " is not a target of " + this.getPartName() );
		} else {
			removeRelationship(relToBeRemoved);				
		}
		
	}
	
	
	/**
	 * Remove relationships by type (eg Namespaces.PRESENTATIONML_SLIDE_LAYOUT) 
	 * 
	 * @param type
	 * @since 3.2.0
	 */
	public void removeRelationshipsByType(String type) {
		
		List<Relationship> relsToClear = new ArrayList<Relationship>();
		for (Relationship r : this.getRelationships().getRelationship()) {
			if (r.getType().equals(type)) {
				relsToClear.add(r);
			}
		}
		for (Relationship r : relsToClear) {
			this.getRelationships().getRelationship().remove(r);
		}	
	}
	
	/**
	 * Get the number of relationships in the collection.
	 */
	public int size() {
		return jaxbElement.getRelationship().size();
	}
	
    /**
     * Unmarshal XML data from the specified InputStream and return the 
     * resulting content tree.  Validation event location information may
     * be incomplete when using this form of the unmarshal API.
     *
     * <p>
     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
     * 
     * @param is the InputStream to unmarshal XML data from
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
	@Override
    public Relationships unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
			
	        XMLInputFactory xif = XMLInputFactory.newInstance();
	        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
	        XMLStreamReader xsr = xif.createXMLStreamReader(is);												
			
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			log.debug("unmarshalling " + this.getClass().getName() );									
			jaxbElement = (Relationships) u.unmarshal( xsr );

		} catch (Exception e ) {
			e.printStackTrace();
		}
		
		resetIdAllocator();
		
		// @since 8.1.0, so we can get from a Rel to source part.
		if (this instanceof RelationshipsPart) {
			((Relationships)jaxbElement).setParent(this); 			
		}
		    	
		return jaxbElement;
    	
    }
	
	@Override // need this so that resetIdAllocator() is called 
    public Relationships unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {

			Unmarshaller u = jc.createUnmarshaller();
						
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			jaxbElement = (Relationships) u.unmarshal( el );
			
		} catch (JAXBException e) {
//			e.printStackTrace();
			log.error(e.getMessage(), e);
			throw e;
		}
		
		resetIdAllocator();
		
		// @since 8.1.0, so we can get from a Rel to source part.
		if (this instanceof RelationshipsPart) {
			((Relationships)jaxbElement).setParent(this); 			
		}
		
		return jaxbElement;
		
	}
	
    
    public void marshal(org.w3c.dom.Node node) throws JAXBException {
		marshal(node, 
				NamespacePrefixMapperUtils.getPrefixMapperRelationshipsPart() );
	}
    
    
    public void marshal(java.io.OutputStream os) throws JAXBException {
		marshal( os, 
				NamespacePrefixMapperUtils.getPrefixMapperRelationshipsPart() ); 
	}
    

    /**
     * Identify the rels in this relationships part which aren't
     * in the other
     * @param otherRP the other RelationshipsPart
     */
    public List<Relationship> uniqueToThis(RelationshipsPart otherRP) {
    	
    	List<Relationship> results = new ArrayList<Relationship>();
    	    	
		for ( Relationship r : jaxbElement.getRelationship()  ) {
			if (getRelationshipByTarget(otherRP, r.getTarget())==null ) {
				log.debug("Unique: " + r.getTarget() );
				results.add(r);
			}			
		}		
    	return results;
    }

    /**
     * Identify the rels in this relationships part which aren't
     * in the other
     * @param otherRP the other RelationshipsPart
     */
    public List<Relationship> uniqueToOther(RelationshipsPart otherRP) {
    	
    	List<Relationship> results = new ArrayList<Relationship>();
    	    	
		for ( Relationship r : otherRP.jaxbElement.getRelationship()  ) {
			if (getRelationshipByTarget(this, r.getTarget())==null ) {
				results.add(r);
			}			
		}		
    	return results;
    }
    
    /**
     * Identify rels common to both parts, but where rels have different
     * content.
     * @param otherRP
     * @return
     */
    public List<Relationship> differingContent(RelationshipsPart otherRP) throws Docx4JException {
    	
    	List<Relationship> results = new ArrayList<Relationship>();
    	    	
		for ( Relationship r : jaxbElement.getRelationship()  ) {
			Relationship otherR = getRelationshipByTarget(otherRP, r.getTarget());
			if (otherR!=null ) {
				
				if (r.getTargetMode() !=null && r.getTargetMode().equals("External") ) {
					if (otherR.getTargetMode() !=null && otherR.getTargetMode().equals("External")) {
						// Usual case
						if (!r.getTarget().equals(otherR.getTarget()) ) {
							// Should never happen, since we matched on target
							throw new Docx4JException("broken logic!");
							//results.add(r);
							//log.debug("External: " + r.getTarget() );							
						}
						
					} else {
						// eg an image changed from embedded to linked
						results.add(r);
						log.debug("External: " + r.getTarget() );						
					}
					continue;
				}
				
				// Compare content
				Part thisPart = this.getPart(r);
				Part otherPart = otherRP.getPart(otherR);	
				if (!thisPart.isContentEqual( otherPart)) {
					results.add(r);
					log.debug("Different: " + r.getTarget() );	
				}
			}			
		}		
    	return results;
    }    
    
	public static Relationship getRelationshipByTarget(RelationshipsPart rp, String relativeTarget) {
		
		for ( Relationship r : rp.jaxbElement.getRelationship()  ) {			
			if (r.getTarget().equals(relativeTarget) ) {
				return r;
			}			
		}		
		return null;
	}
	
	/**
	 * Infer the source part's name from this rels part name.
	 * eg word/_rels/document.xml.rels gives word/document.xml 
	 * and /_rels/.rels gives /
	 * @param relationshipsPartName
	 * @return
	 */
	static public String inferSourcePartName(String relationshipsPartName) {

		String result = relationshipsPartName;
		
		if (result.endsWith(".rels"))
			result = result.substring(0, result.length()-5);
		
		if (result.contains("_rels")) 
			result = result.substring(0, result.indexOf("_rels")) + result.substring(result.indexOf("_rels")+6);
		
		return result;

	}
	
	
    private PartName getNewPartName(String prefix, String suffix, 
    		HashMap<PartName, Part> parts) throws InvalidFormatException {
    	
    	PartName proposed = null;
    	int i=1;
    	do {
    		
    		if (i>1) {
    			proposed = new PartName( prefix + i + suffix);
    		} else {
    			proposed = new PartName( prefix + suffix);        			
    		}
    		i++;
    		
    	} while (parts.get(proposed)!=null);
    	
    	return proposed;
    	
    }
	

	  public enum AddPartBehaviour {

		    OVERWRITE_IF_NAME_EXISTS("overwrite"),
		    REUSE_EXISTING("reuse"), 
		    RENAME_IF_NAME_EXISTS("rename");
		    
		    private final String value;

		    AddPartBehaviour(String v) {
		        value = v;
		    }

		    public String value() {
		        return value;
		    }
	  } 	
	
//	public static void main(String[] args) throws Exception {
//		
//		System.out.println(inferSourcePartName("word/_rels/document.xml.rels"));
//		System.out.println(inferSourcePartName("/_rels/.rels"));
//	}
	
}
