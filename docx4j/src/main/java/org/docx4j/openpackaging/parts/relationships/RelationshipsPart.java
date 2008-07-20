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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

import org.docx4j.openpackaging.parts.Dom4jXmlPart;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
//import org.openxml4j.opc.PackageAccess;



/**
 * Represents a Relationship Part, which contains the relationships for a 
 * given PackagePart or the Package.
 * 
 * @author Julien Chable, CDubettier
 * @version 0.1
 */
public final class RelationshipsPart extends Dom4jXmlPart implements
		Iterable<Relationship> {

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
	
	
	private static Logger logger = Logger.getLogger(RelationshipsPart.class);

	/**
	 * Package relationships ordered by ID.
	 */
	private TreeMap<String, Relationship> relationshipsByID;

	/**
	 * Package relationships ordered by type.
	 */
	private TreeMap<String, Relationship> relationshipsByType;


	/**
	 * Source part for these relationships
	 */
	private Base sourceP;
	
	public Base getSourceP() {
		return sourceP;
	}
	
	/** This Relationship Part is the package relationship part
	 * if its source is the Package. 
	 */	 
	public boolean isPackageRelationshipPart() {
		return (sourceP instanceof Package);
	}

//	public void setPackageRelationshipPart(boolean isPackageRelationshipPart) {
//		this.isPackageRelationshipPart = isPackageRelationshipPart;
//	}
	

	/**
	 * Constructor.
	 * 
	 * DO NOT USE.  Doesn't set source Part!
	 */
	public RelationshipsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		// NB partName is the partName of this relationship part,
		// not the source Part.  sourceP above has the 
		// sourcePartName, which will be required in order to resolve 
		// relative targets
		init();
		
		//throw new InvalidFormatException();
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
		
	}
	
	
	/**
	 * Constructor.  Parses the .rels XML document.
	 * 
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @param contents
	 *            The XML Document contents of the part.
	 * @throws InvalidFormatException
	 *             If the specified URI is not valid.
	 */
	public RelationshipsPart(Base sourceP, PartName partName, InputStream in)
			throws InvalidFormatException {
		super(partName);
		setDocument(in);
		this.sourceP = sourceP;
		init();
		Element root = document.getRootElement();		
		parseRelationshipsDocument(root);
		
//		this.container = (Package) pack;
//		isRelationshipPart = partName.isRelationshipPartURI();
	}

	// This constructor used when input is a Word 2007 Xml Package file
	public RelationshipsPart(Base sourceP, PartName partName, Element root)
			throws InvalidFormatException {
		
		super(partName);
		
		// setDocument(in);  // nb - not set
		this.sourceP = sourceP;
		init();
		parseRelationshipsDocument(root);

		// this.container = (Package) pack;
		// isRelationshipPart = partName.isRelationshipPartURI();
	}
	
	
	private void init() {
		
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART));
		
		relationshipsByID = new TreeMap<String, Relationship>();
		relationshipsByType = new TreeMap<String, Relationship>();		
	}
	
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
	public void loadPart(Part part) {

		if (part == null) {
			throw new IllegalArgumentException("part");
		}
		
		PartName partName = part.getPartName();
		log.info("Loading part " + partName.getName() );
		
		part.setOwningRelationshipPart(this);

		// All (non-relationship) parts are stored in a collection
		// in the package, even though conceptually this loadPart
		// method should be invoked on the relationship source.		
	
		getPackage().getParts().put(part);
		
		// Tell the part what package it belongs to!
		// TODO - do this in the Part constructor.  It can be too late
		// leaving it until the Part is added to the Package.
		part.setPackage( getPackage() );
		
	}
	
	/** Gets a loaded Part by its id */
	public Part getPart(String id) {

		log.debug("looking for: " + id);
		
		Relationship r = getRelationshipByID(id);
		
		if (r==null) {
			log.warn("couldn't find part with id: " + id);
			return null;
		}
		
		log.info(" source is  " + r.getSourceURI() );
    	log.info(id + " points to " + r.getTargetURI());
    	// eg rId1 points to fonts/font1.odttf
    	
    	URI uri = org.docx4j.openpackaging.URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI());
		
    	try {
			return getPackage().getParts().get( new PartName(uri, true ));
		} catch (InvalidFormatException e) {
			log.error("Couldn't get part using PartName: " + uri, e);
			return null;
		}
	}
	
	/**
	 * Add a newly created part, a relationship and the content type.
	 *  
	 * 
	 * @param part
	 *            The part to add.
	 * @return The part added to the package, the same as the one specified.
	 */
	public void addPart(Part part, ContentTypeManager ctm) {
		
		loadPart(part);
		
		// Now add a new relationship
		int num = size() + 1;
		String id = "rId" + num;

		URI tobeRelativized = part.getPartName().getURI();
		URI relativizeAgainst = sourceP.getPartName().getURI();
		
		log.debug("Relativising " + tobeRelativized 
				+ " against " + relativizeAgainst);
		
		URI result = org.docx4j.openpackaging.URIHelper.relativizeURI(tobeRelativized, relativizeAgainst); 
		
		log.debug("Result " + result); 
		
		Relationship rel = new Relationship(sourceP, result, 
				TargetMode.INTERNAL, part.getRelationshipType(), id);
		addRelationship(rel );
		
		// Add an override to ContentTypeManager
		ctm.addOverrideContentType(part.getPartName().getURI(), part.getContentType());

	}


	
	 /** Remove all parts from this relationships
	 *   part */ 
	public void removeParts() {

		// Make a list in order to avoid concurrent modification exception
		java.util.ArrayList<Relationship> relationshipsToGo = new java.util.ArrayList<Relationship>();
		for (Relationship r : relationshipsByID.values() ) {
			relationshipsToGo.add(r);
		}

		for (Relationship r : relationshipsToGo ) {
			
			String resolvedPartUri = URIHelper.resolvePartUri(r.getSourceURI(), r.getTargetURI() ).toString();
			
			log.info("Removing part: " + resolvedPartUri);
			
			try {
				removePart(new PartName(resolvedPartUri));
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
			for (Relationship rel : relationshipsByID.values() ) {
				
				URI resolvedTargetURI = org.docx4j.openpackaging.URIHelper.resolvePartUri(   sourceP.partName.getURI(), rel.getTargetURI() );
				log.debug("Comparing " + resolvedTargetURI + " == " + partName.getName());
				
				if (partName.getName().equals(resolvedTargetURI.toString()) ) { // was rel.getTargetURI()
					
					log.info("True - will delete relationship with target " + rel.getTargetURI());
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
		 * Parse the relationship part and add all relationship in this collection.
		 * 
		 * @param relPart
		 *            The package part to parse.
		 * @throws InvalidFormatException
		 *             Throws if the relationship part is invalid.
		 */
		private void parseRelationshipsDocument(Element root)
				throws InvalidFormatException {
			try {
					
				// Browse default types
	
				// Check OPC compliance M4.1 rule
				boolean fCorePropertiesRelationship = false;
	
				for (Iterator i = root
						.elementIterator(Relationship.RELATIONSHIP_TAG_NAME); 
						i.hasNext();) {
					Element element = (Element) i.next();

					Relationship rel = new Relationship(sourceP, element, fCorePropertiesRelationship);
					
					if (rel.getId() == null) {
						// Generate a unique ID if id parameter is null.
						int j = 0;
						do {
							rel.setId("rId" + ++j);
						} while (relationshipsByID.get(rel.getId()) != null);
					}
					
					//addRelationship(target, targetMode, type, id);
					addRelationship(rel);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				throw new InvalidFormatException(e.getMessage());
			}
		}
		
		/**
		 * Return an XML representation of this part.
		 */
		public Document getDocument() {
			
			// Building xml
			Document xmlOutDoc = DocumentHelper.createDocument();
			// make something like <Relationships
			// xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
			Namespace dfNs = Namespace.get("", Namespaces.RELATIONSHIPS);
			Element root = xmlOutDoc.addElement(new QName(
					Relationship.RELATIONSHIPS_TAG_NAME, dfNs));

			// <Relationship
			// TargetMode="External"
			// Id="rIdx"
			// Target="http://www.custom.com/images/pic1.jpg"
			// Type="http://www.custom.com/external-resource"/>

			log.debug("Partname is " + partName);
//			URI sourcePartURI = URIHelper
//					.getSourcePartUriFromRelationshipPartUri(partName.getURI());

			for (Relationship rel : relationshipsByID.values()) {
				// The relationship element
				Element relElem = root
						.addElement(Relationship.RELATIONSHIP_TAG_NAME);
				
				//rel.marshall(sourcePartURI, relElem);

				rel.marshall(relElem);
				
			}

		
		 xmlOutDoc.normalize();
		 
		 return xmlOutDoc;
	}

	/**
	 * Add the specified relationship to the collection.
	 * 
	 * @param rel
	 *            The relationship to add.
	 */
	public void addRelationship(Relationship rel) {
		relationshipsByID.put(rel.getId(), rel);
		relationshipsByType.put(rel.getRelationshipType(), rel);
	}


	
	
	/**
	 * Remove a relationship by its ID.
	 * 
	 * @param id
	 *            The relationship ID to remove.
	 */
//	private void removeRelationship(String id) {
//		if (relationshipsByID != null && relationshipsByType != null) {
//			Relationship rel = relationshipsByID.get(id);
//			if (rel != null) {
//				relationshipsByID.remove(rel.getId());
//				relationshipsByType.values().remove(rel);
//			}
//		}
//	}

	/**
	 * Remove a relationship by its reference.
	 * 
	 * @param rel
	 *            The relationship to delete.
	 */
	public void removeRelationship(Relationship rel) {
		if (rel == null)
			throw new IllegalArgumentException("rel");

		relationshipsByID.values().remove(rel);
		relationshipsByType.values().remove(rel);
	}

	/**
	 * Retrieves a relationship by its index in the collection.
	 * 
	 * @param index
	 *            Must be a value between [0-relationships_count-1]
	 */
	public Relationship getRelationship(int index) {
		if (index < 0 || index > relationshipsByID.values().size())
			throw new IllegalArgumentException("index");

		Relationship retRel = null;
		int i = 0;
		for (Relationship rel : relationshipsByID.values()) {
			if (index == i++)
				return rel;
		}
		return retRel;
	}

	/**
	 * Retrieves a package relationship based on its id.
	 * 
	 * @param id
	 *            ID of the package relationship to retrieve.
	 * @return The package relationship identified by the specified id.
	 */
	public Relationship getRelationshipByID(String id) {
		return relationshipsByID.get(id);
	}

	public Relationship getRelationshipByType(String type) {
		return relationshipsByType.get(type);
	}
	
	/**
	 * Get the number of relationships in the collection.
	 */
	public int size() {
		return relationshipsByID.values().size();
	}

	/**
	 * Get this collection's iterator.
	 */
	public Iterator<Relationship> iterator() {
		return relationshipsByID.values().iterator();
	}

	/**
	 * Get an iterator of a collection with all relationship with the specified
	 * type.
	 * 
	 * @param typeFilter
	 *            Type filter.
	 * @return An iterator to a collection containing all relationships with the
	 *         specified type contain in this collection.
	 */
	public Iterator<Relationship> iterator(String typeFilter) {
		ArrayList<Relationship> retArr = new ArrayList<Relationship>();
		for (Relationship rel : relationshipsByID.values()) {
			if (rel.getRelationshipType().equals(typeFilter))
				retArr.add(rel);
		}
		return retArr.iterator();
	}

	/**
	 * Clear all relationships.
	 */
	public void clear() {
		relationshipsByID.clear();
		relationshipsByType.clear();
	}

	@Override
	public String toString() {
		 	
		String str;
		if (relationshipsByID == null) {
			str = "relationshipsByID=null";
		} else {
			str = relationshipsByID.size() + " relationship(s) = [ \n";
		}
		
		ArrayList<Relationship> retArr = new ArrayList<Relationship>();
		for (Relationship rel : relationshipsByID.values()) {
			str = str + rel.toString() + "\n";
		}
		
//		if ( (partName != null)) {
//			str = str + "," + partName;
//		} else {
//			str = str + ",relationshipPart=null";
//		}
//
//		// Source of this relationship
//		if ((sourcePart != null) && (sourcePart.getPartName() != null)) {
//			str = str + "," + sourcePart.getPartName();
//		} else {
//			str = str + ",sourcePart=null";
//		}
//		if (partName != null) {
//			str = str + "," + partName;
//		} else {
//			str = str + ",uri=null)";
//		}
		return str + "]";
	}

}
