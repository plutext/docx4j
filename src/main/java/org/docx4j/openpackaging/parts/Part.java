/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */


package org.docx4j.openpackaging.parts;

import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.dom4j.Document;

import org.apache.log4j.Logger;


/**
 * An abstraction of an Open Packaging Convention (OPC) Part.
 * 
 * OPC Parts are either XML, or binary (or text) documents.
 * 
 * Most are XML documents.
 *  
 *  docx4j aims to represent XML parts using JAXB.  However, 
 *  at present we only have a JAXB representation for the main
 *  document part.  
 *  
 *  Until such time as a JAXB representation for an XML Part exists,
 *  the Part should extend this class.   
 * 
 * To instantiate a Part use (or create) an appropriate subclass.
 * When an existing document is being loaded, ContentTypeManager.getPart
 * will instantiate the appropriate subclass. 
 */
public abstract class Part extends Base {
	
	/**
	 * Logger.
	 */
	protected static Logger log = Logger.getLogger(Part.class);

	
	protected Package pack;
	
	
	/** The Namespace of this Part.  
	 *  Used when adding the Part to a relationship Part.
	 *  TODO: set this when the Part is constructed.
	 */
	private String relationshipType;

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}
	
	
	/** Every part is the target of some relationship,
	 * specified in a RelationshipsPart. Every part can also 
	 * have its own RelationshipsPart - for that, see Base 
	 * (since Package has one as well). 
	 */
	private RelationshipsPart owningRelationshipPart;
		
	public RelationshipsPart getOwningRelationshipPart() {
		return owningRelationshipPart;
	}

	public void setOwningRelationshipPart(
			RelationshipsPart owningRelationshipPart) {
		this.owningRelationshipPart = owningRelationshipPart;
	}
	
	public Part() {
		
	}

	/**
	 * Constructor.
	 * 
	 * @param pack
	 *            Parent package.
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @throws InvalidFormatException
	 *             If the specified URI is not valid.
	 */
	public Part(PartName partName)
			throws InvalidFormatException {
		this.partName = partName;
	}
	

	/**
	 * Constructor.
	 * 
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @throws InvalidFormatException
	 *             If the specified URI is not valid.
	 */
	public Part(PartName partName, Document document)
			throws InvalidFormatException {
		this.partName = partName;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param pack
	 *            Parent package.
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @param contentType
	 *            The Multipurpose Internet Mail Extensions (MIME) content type
	 *            of the part's data stream.
	 */
	public Part(PartName partName,
			String contentType) throws InvalidFormatException {
		this(partName);
		this.contentType = new ContentType(contentType);
	}
	

	public Package getPackage() {
		if (pack==null) {
			log.error("Package field null for this Part!");
		}
		return pack;
	}
	public void setPackage( Package pack) {
		this.pack = pack;
	}


	@Override
	public boolean setPartShortcut(Part part, String relationshipType) {
		return false;
	}


}