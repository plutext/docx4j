/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
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

package org.docx4j.openpackaging.packages;

import java.net.URI;

import org.apache.log4j.Logger;

import org.docx4j.Namespaces;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.exceptions.Docx4JRuntimeException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.TargetMode;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;


/**
 * Represent a Package as defined in the Open Packaging Specification.
 * 
 * @author Jason Harrop
 * @version 0.1
 */
public class Package extends Base {

	private static Logger logger = Logger.getLogger(Package.class);


	/**
	 * Package parts collection.  This is a collection of _all_
	 * parts in the package (_except_ relationship parts), 
	 * not just those referred to by the package-level relationships.
	 */
	protected Parts parts = new Parts();


	/**
	 * Flag if an modification is done to the document.
	 */
	protected boolean isDirty = false;
	
	
	
	protected ContentTypeManager contentTypeManager;

	public ContentTypeManager getContentTypeManager() {
		return contentTypeManager;
	}

	public void setContentTypeManager(ContentTypeManager contentTypeManager) {
		this.contentTypeManager = contentTypeManager;
	}
	
	
	/**
	 * Constructor.
	 * 
	 * Note that Load.createPackage() returns appropriate subclass eg 
	 * WordprocessingMLPackage.
	 * 
	 */
	public Package() {
		try {
			partName = new PartName("/", false);
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}
	
	public Package getPackage() {
		return this;
	}
		

	/**
	 * Retrieve a part identified by its name, or null if it
	 * doesn't exist.
	 * 
	 * @param partName
	 *            Part name of the part to retrieve.
	 * @return The part with the specified name, else <b>null</b>.
	 */
	public Part getPart(PartName partName) {

		if (partName == null)
			throw new IllegalArgumentException("partName");

		return parts.get(partName);
		
	}


	/**
	 * Get the target part from the specified relationship.
	 * 
	 * @param partRel
	 *            The part relationship uses to retrieve the part.
	 */
//	public Part getPart(Relationship partRel) {
//		Part retPart = null;
////		ensureRelationships();
//		for (Relationship rel : relationships) {
//			if (rel.getRelationshipType().equals(partRel.getRelationshipType())) {
//				try {
//					retPart = getPart(URIHelper.createPartName(rel
//							.getTargetURI()));
//				} catch (InvalidFormatException e) {
//					continue;
//				}
//				break;
//			}
//		}
//		return retPart;
//	}

	/**
	 * 	 * 
	 * @return All this package's parts.
	 */
//	public HashMap<Part> getParts() throws InvalidFormatException {

	

	/**
	 * Add a part to the package.  Note that this method doesn't
	 * add the part to any relationship part. You need to do that
	 * as well if the part is to be of any use!
	 * 
	 * @param part
	 *            The part to add.
	 * @return The part added to the package, the same as the one specified.
	 */
	public Part addPart(Part part) {
		if (part == null) {
			throw new IllegalArgumentException("part");
		}
		
		PartName partName = part.getPartName();

		/* Check OPC compliance */

		// Rule [M4.1]: The format designer shall specify and the format
		// producer
		// shall create at most one core properties relationship for a package.
		// A format consumer shall consider more than one core properties
		// relationship for a package to be an error. If present, the
		// relationship shall target the Core Properties part.
		
// Consider reinstating this check if I do end up storing each parts
// content type within it!
		
//		if (part.getContentType() == ContentTypes.CORE_PROPERTIES_PART) {
//			if (relationships != null)
//				throw new InvalidOperationException(
//						"OPC Compliance error [M4.1]: you try to add more than one core properties relationship in the package !");
//		}

		/* End check OPC compliance */
		if (parts.get(partName)!=null ) {
			log.warn("Overwriting existing part " + partName.getName() );
//			throw new InvalidOperationException("Part " + partName
//					+ " already exist !");
		}
//		this.contentTypeManager.addContentType(partName, contentType);
		this.parts.put(part);
		
		// Tell the part what package it belongs to!
		// TODO - do this in the Part constructor.  It can be too late
		// leaving it until the Part is added to the Package.
		part.setPackage(this);
		
		this.isDirty = true;
		return part;
	}

	
	/**
	 * Remove a part from this package. If this part is relationship part, then
	 * delete all relationships in the source part.
	 * 
	 * @param partName
	 *            The part name of the part to remove.
	 */
	public void removePart(PartName partName) {
		if (partName == null)
			throw new IllegalArgumentException("partName");

		// Delete the specified part from the package.
		if (parts.get(partName)!=null) {
			//parts.get(partName).setDeleted(true);
//			removePartImpl(partName);
			parts.remove(partName);
		} else {
//			removePartImpl(partName);
		}

		// If this part is a relationship part, then delete all relationships of
		// the source part.
		if (partName.isRelationshipPartURI()) {
			URI sourceURI = URIHelper
					.getSourcePartUriFromRelationshipPartUri(partName.getURI());
			PartName sourcePartName;
			try {
				sourcePartName = URIHelper.createPartName(sourceURI);
			} catch (InvalidFormatException e) {
				throw new Docx4JRuntimeException(
						"The part name "
								+ sourceURI
								+ " is not valid but this exception should not have been raise by OpenXML4J ! Please send a mail to the developers team.");
			}
			if (sourcePartName.getURI().equals(
					URIHelper.PACKAGE_ROOT_URI)) {
//				clearRelationships();
			} else if (partExists(sourcePartName)) {
				Part part = getPart(sourcePartName);
				if (part != null) {
//					part.clearRelationships();
				}
			}
		}

		this.isDirty = true;
	}

	/**
	 * Remove a part from this package as well as its relationship part, if one
	 * exists, and all parts listed in the relationship part.
	 * 
	 * [But the part could be used in some other part via some other relationship?]
	 * 
	 * @param partName
	 *            The name of the part to delete.
	 * @throws InvalidFormatException
	 *             Throws if the associated relationship part of the specified
	 *             part is not valid.
	 */
//	public void removePartRecursive(PartName partName)
//			throws InvalidFormatException {
//		// Retrieves relationship part, if one exists
//		Part relPart = this.parts.get(PackagingURIHelper
//				.getRelationshipPartName(partName));
//		// Retrieves PackagePart object from the package
//		Part partToRemove = this.parts.get(partName);
//
//		if (relPart != null) {
//			RelationshipCollection partRels = new RelationshipCollection(
//					partToRemove);
//			for (Relationship rel : partRels) {
//				PartName partNameToRemove = PackagingURIHelper
//						.createPartName(PackagingURIHelper.resolvePartUri(rel
//								.getSourceURI(), rel.getTargetURI()));
//				removePart(partNameToRemove);
//			}
//
//			// Finally delete its relationship part if one exists
//			this.removePart(relPart.partName);
//		}
//
//		// Delete the specified part
//		this.removePart(partToRemove.partName);
//	}

	/**
	 * Check if a part already exists in this package from its name.
	 * 
	 * @param partName
	 *            Part name to check.
	 * @return <i>true</i> if the part is logically added to this package, else
	 *         <i>false</i>.
	 */
	public boolean partExists(PartName partName) {
		return (this.getPart(partName) != null);
	}

	
	
	/**
	 * Validates the package compliance with the OPC specifications.
	 * 
	 * @return <b>true</b> if the package is valid else <b>false</b>
	 */
	public boolean validatePackage(Package pkg) throws InvalidFormatException {
		throw new InvalidOperationException("Not implemented yet !!!");
	}

	
	public static Package createTestPackage() throws InvalidFormatException {
		
		// Create a package
		Package p = new Package();

		// Add a ContentTypeManager to it
		ContentTypeManager ctm = new ContentTypeManagerImpl();
		p.setContentTypeManager(ctm);
		
		/* Main part */
		PartName corePartName = URIHelper
				.createPartName("/word/document.xml");

		// Create main document part content
		Document doc = DocumentHelper.createDocument();
		Namespace nsWordprocessinML = new Namespace("w",
				"http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		Element elDocument = doc.addElement(new QName("document",
				nsWordprocessinML));
		Element elBody = elDocument.addElement(new QName("body",
				nsWordprocessinML));
		Element elParagraph = elBody.addElement(new QName("p",
				nsWordprocessinML));
		Element elRun = elParagraph
				.addElement(new QName("r", nsWordprocessinML));
		Element elText = elRun.addElement(new QName("t", nsWordprocessinML));
		elText.setText("Hello world");

		// Create main document part
		//Part corePart = new Part(corePartName, doc);
		Part corePart = new org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart(corePartName);
		corePart.setDocument(doc);

		// Add it to the collection of parts
		p.addPart(corePart);

		ctm.addContentType(corePartName, 
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml");
		
		
		// Create the PackageRelationships part	
		RelationshipsPart rp = new RelationshipsPart( new PartName("/_rels/.rels") );
		// Add it to the package
		p.setRelationships(rp);
		
		// Create main part relationship
		rp.addRelationship(corePartName, TargetMode.INTERNAL,
				Namespaces.DOCUMENT, "rId1");
		
		
		// Return the new package
		return p;

		
	}
	
	DocPropsCorePart docPropsCorePart;

	DocPropsExtendedPart docPropsExtendedPart;
	
	DocPropsCustomPart docPropsCustomPart;

	@Override
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			return true;			
		} else {	
			return false;
		}
	}

	public DocPropsCorePart getDocPropsCorePart() {
		return docPropsCorePart;
	}

	public DocPropsExtendedPart getDocPropsExtendedPart() {
		return docPropsExtendedPart;
	}

	public DocPropsCustomPart getDocPropsCustomPart() {
		return docPropsCustomPart;
	}



}
