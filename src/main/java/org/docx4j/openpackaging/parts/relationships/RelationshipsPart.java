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

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;





/**
 * Represents a Relationship Part, which contains the relationships for a 
 * given PackagePart or the Package.
 * 
 * @author Julien Chable, CDubettier
 * @version 0.1
 */
public final class RelationshipsPart extends JaxbXmlPart<Relationships> { 
	// implements Iterable<Relationship> {

	private static Logger logger = Logger.getLogger(RelationshipsPart.class);
	
	/* Example:
	 * 
	 * Package relationships:
	 * 
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
			<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
			<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties"   Target="docProps/core.xml"/>
			<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"      Target="word/document.xml"/>
		</Relationships>


		 word/_rels/document.xml.rels:
		 
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
			<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings" Target="webSettings.xml"/>
			<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>
			<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
			<Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml"/>
			<Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable" Target="fontTable.xml"/>
		</Relationships>
		
		More complex version:
		
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Target="customXml/item1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml"/>
  <Relationship Id="rId10" Target="header2.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header"/>
  <Relationship Id="rId11" Target="footer1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer"/>
  <Relationship Id="rId12" Target="footer2.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer"/>
  <Relationship Id="rId13" Target="header3.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header"/>
  <Relationship Id="rId14" Target="footer3.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer"/>
  <Relationship Id="rId15" Target="fontTable.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable"/>
  <Relationship Id="rId16" Target="glossary/document.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/glossaryDocument"/>
  <Relationship Id="rId17" Target="theme/theme1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme"/>
  <Relationship Id="rId2" Target="numbering.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering"/>
  <Relationship Id="rId3" Target="styles.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"/>
  <Relationship Id="rId4" Target="settings.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings"/>
  <Relationship Id="rId5" Target="webSettings.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings"/>
  <Relationship Id="rId6" Target="footnotes.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes"/>
  <Relationship Id="rId7" Target="endnotes.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes"/>
  <Relationship Id="rId8" Target="comments.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments"/>
  <Relationship Id="rId9" Target="header1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header"/>
</Relationships>		
		

	 */
	

	/**
	 * Constructor.
	 */
	public RelationshipsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	// NB partName is the partName of this relationship part,
	// not the source Part.  sourceP above has the 
	// sourcePartName, which will be required in order to resolve 
	// relative targets

	public RelationshipsPart() throws InvalidFormatException {
		super(new PartName("/rels/.rels"));
		init();
	}
	
	/**
	 * Constructor.  Creates an appropriately named .rels XML document.
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
				sourceP.getPartName().getName() )) );
		
		this.sourceP = sourceP;
		init();
				
		sourceP.setRelationships(this);
			// TODO - use setRelationships from here 
			// like this in other constructors
			// in this class.
						
		org.docx4j.relationships.ObjectFactory factory =
			new org.docx4j.relationships.ObjectFactory();
		
		jaxbElement = factory.createRelationships();		
	}
		
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART));

		setJAXBContext(Context.jcRelationships);				
	}
	
	public static RelationshipsPart createRelationshipsPartForPart(
			Base sourcePart) {

		if (sourcePart.getRelationshipsPart() != null)
			return sourcePart.getRelationshipsPart();

		RelationshipsPart rp = null;
		try {
			rp = new RelationshipsPart(sourcePart);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rp.setPackage(sourcePart.getPackage());

		// sourcePart.setRelationships(rp);

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
    	log.info(id + " points to " + r.getTarget());
		
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
	

	public Part getPart(Relationship r ) {
		
		log.info(" source is  " + sourceP.getPartName().toString() );
    	// eg rId1 points to fonts/font1.odttf
    		
		if (r.getTargetMode() == null
				|| !r.getTargetMode().equals("External") ) {
			
			// Usual case
			URI uri = null;
	
			try {
				uri = org.docx4j.openpackaging.URIHelper
						.resolvePartUri(sourceP.partName.getURI(), new URI(
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
		nextId++;
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
				log.error("Couldn't process id: " + id);
				return;
			}			
		}
		nextId = highestId+1;		
		logger.debug("nextId reset to : " + nextId);
		
	}
	
	// ----------------------------------------------------------
	
	/**
	 * Loads a pre-existing target part.
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
		log.info("Loading part " + partName.getName() );
		
		part.setOwningRelationshipPart(this);
		part.setSourceRelationship(sourceRelationship);

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
	public Relationship addPart(Part part, boolean overwriteExistingTarget, 
			ContentTypeManager ctm) {
		return this.addPart(part, overwriteExistingTarget, ctm, null);
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
	public Relationship addPart(Part part, boolean overwriteExistingTarget, 
			ContentTypeManager ctm, String relId) {
		
		// Now add a new relationship

		URI tobeRelativized = part.getPartName().getURI();
		URI relativizeAgainst = sourceP.getPartName().getURI();
		
		log.debug("Relativising target " + tobeRelativized 
				+ " against source " + relativizeAgainst);
		
		String result = URIHelper.relativizeURI(relativizeAgainst, 
				tobeRelativized).toString(); 
		
		if (relativizeAgainst.getPath().equals("/")
				&& result.startsWith("/")) {
			
			/*
			 * Relativising target /word/document.xml against source / 
			 * Result /word/document.xml
			 * 
			 * but we want word/document.xml
			 */		
			
			result = result.substring(1);
		}
				
		log.debug("Result " + result); 
		
		org.docx4j.relationships.ObjectFactory factory =
			new org.docx4j.relationships.ObjectFactory();
		
		Relationship rel = factory.createRelationship();
		
		rel.setTarget(result.toString() );
		//rel.setTargetMode( TargetMode.INTERNAL );
		rel.setType( part.getRelationshipType() );
		
		if (relId!=null) {
			rel.setId( relId );			
		}

		loadPart(part, rel);
		
		if (overwriteExistingTarget) {
			// Is more than one rel with the same target 
			// ever permitted. For example, an image?
			// ECMA-376 Part 2 8.3 only says that
			// Id must be unique.
			// (But we don't test for that here; the id
			//  is only assigned in addRelationship further
			//  below)			
			
			// Word fails to load a document if it has 2 copies of the styles part
			// (each with a separate rels entry).
			
			// We ensure there is just a single entry
			// iff overwriteExistingTarget is set
			// NB, this does not recursively remove parts
			// (compare removePart method below)
			// In fact, it only removes the rel, it leaves the
			// part in the Parts hashmap, but that's ok
			// since the docx is constructed by walking the
			// rels tree. And loadPart above will overwrite
			// any existing part which has the same name.
			
			
			Relationship relToBeRemoved = null;
			for (Relationship relic : jaxbElement.getRelationship() ) {
				
				if (relic.getTarget().equals( rel.getTarget() )) {
					
					log.info("True - will delete relationship with target " + rel.getTarget());
					relToBeRemoved = relic; // Avoid java.util.ConcurrentModificationException
					break;
				}
				
			}
			if (relToBeRemoved!=null) {
				removeRelationship(relToBeRemoved);				
			}		
		}
		
//		Relationship rel = new Relationship(sourceP, result, 
//				TargetMode.INTERNAL, part.getRelationshipType(), id);
		addRelationship(rel );
		
		// Add an override to ContentTypeManager
		if ( part.getContentType().equals( ContentTypes.IMAGE_JPEG) ) {
			ctm.addDefaultContentType("jpeg",ContentTypes.IMAGE_JPEG );
		} else if ( part.getContentType().equals( ContentTypes.EXTENSION_GIF ) ) {
			ctm.addDefaultContentType("gif", ContentTypes.EXTENSION_GIF);
		} else if ( part.getContentType().equals( ContentTypes.EXTENSION_PNG ) ) {
			ctm.addDefaultContentType("png", ContentTypes.IMAGE_PNG);
		} else {
			ctm.addOverrideContentType(part.getPartName().getURI(), part.getContentType());
		}
		
		return rel;

	}

	/**
	 * Add the specified relationship to the collection.
	 * 
	 * @param rel
	 *            The relationship to add.
	 */
	public boolean addRelationship(Relationship rel) 
		throws InvalidOperationException {

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
		return true;
	}
	
	
	 /** Remove all parts from this relationships
	 *   part */ 
	public void removeParts() {

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

				removePart(new PartName(resolvedPartUri));
			} catch (URISyntaxException e) {
				log.error("Cannot convert " + r.getTarget()
						+ " in a valid relationship URI-> ignored", e);
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
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
	public void removePart(PartName partName) {
		
		log.info("trying to removePart " + partName.getName() );
		
		if (partName == null)
			throw new IllegalArgumentException("partName was null");
		
		Part part = getPackage().getParts().get(partName);
		
		if (part!=null) {

			// Remove the relationship for which it is a target from here
			// Throw an error if this can't be found!
			Relationship relToBeRemoved = null;
//			for (Relationship rel : relationshipsByID.values() ) {
			for (Relationship rel : jaxbElement.getRelationship() ) {
				
				if (rel.getTargetMode() !=null
						&& rel.getTargetMode().equals("External") ) {
					// This method can't be used to remove external resources
					continue;
				}
								
				URI resolvedTargetURI = null;

				try {
					resolvedTargetURI = org.docx4j.openpackaging.URIHelper
							.resolvePartUri(sourceP.partName.getURI(), new URI(
									rel.getTarget()));
				} catch (URISyntaxException e) {
					log.error("Cannot convert " + rel.getTarget()
							+ " in a valid relationship URI-> ignored", e);
				}		

				log.debug("Comparing " + resolvedTargetURI + " == " + partName.getName());
				
				if (partName.getName().equals(resolvedTargetURI.toString()) ) { 
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
				throw new IllegalArgumentException(partName + " is not a target of " + this.partName );
			} else {
				removeRelationship(relToBeRemoved);				
			}
						
			// Remove parts it references
			if (part.getRelationshipsPart()!=null) {
				part.getRelationshipsPart().removeParts();
				
				// part.setRelationships(null);  // Unnecessary
			}			

			// Remove from Content Type Manager
				// TODO			
			
			// Delete the specified part from the package.
			getPackage().getParts().remove(partName);						
		}

//		this.isDirty = true;
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
		
		jaxbElement.getRelationship().remove(rel);

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
			
//			if (jc==null) {
//				setJAXBContext(Context.jc);				
//			}
		    		    
			Unmarshaller u = jc.createUnmarshaller();
			
			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			log.info("unmarshalling " + this.getClass().getName() + " \n\n" );									
						
			jaxbElement = (Relationships) u.unmarshal( is );
			
			
			log.info("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
		}
		
		jaxbElement = (Relationships)jaxbElement;		
		resetIdAllocator();
		    	
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
    

	
}
