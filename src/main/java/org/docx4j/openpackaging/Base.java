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

package org.docx4j.openpackaging;

import java.net.URI;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.docx4j.openpackaging.exceptions.OpenXML4JException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.TargetMode;



public abstract class Base {

	protected static Logger log = Logger.getLogger(Base.class);
	
	/**
	 * relationships - the package and each part has one of these
	 * See eg 11. WordprocessingML [11.2 Package Structure]
	 */
	protected RelationshipsPart relationships;
	
	protected ContentType contentType;

	public String getContentType() {
		if (contentType!=null ) {
			return contentType.toString();
		} else {
			System.out.println("WARNING: content type was null. " +
					"You should set this before adding the part.");
			return null;
		}
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * The part name. (required by the specification [M1.1])
	 * 
	 * We will assume the Package has a part name of "/"
	 * 
	 */
	public PartName partName;

	/**
	 * @return the uri
	 */
	public PartName getPartName() {
		if (partName==null) {
			log.error("PartName was null!");
			try {
				return new PartName("/null");
			} catch (Exception e) {
				log.error(e.getMessage());
				// TODO: handle exception
			}
		}
		return partName;
	}

	public abstract Package getPackage(); 	
	
	/**
	 * Get the relationship part name of the specified part.
	 * 
	 * @param part
	 *            The part .
	 * @return The relationship part name of the specified part. Be careful,
	 *         only the correct name is returned, this method does not check if
	 *         the part really exist in a package !
	 * @throws InvalidOperationException
	 *             Throws if the specified part is a relationship part.
	 */
	public RelationshipsPart getRelationshipPart() {
//			throws InvalidOperationException {
		if (this instanceof org.docx4j.openpackaging.parts.relationships.RelationshipsPart) {
			// a relationship part can't have a relationship part
			// but should we throw an error here?
			//throw new InvalidOperationException();
			return null;
		} else {
			return relationships;
		}
	}

	public void setRelationships(RelationshipsPart relationships) {
		this.relationships = relationships;
	}

	
	/** Tell the source Part about this target Part, so the
	 *  source can use this part via a convenience method.
	 * @param part
	 * @param relationshipType
	 * @return true if the source Part does use the target Part
	 * in a convenience method.
	 */ 
	public abstract boolean setPartShortcut(Part part, String relationshipType);
	
	
//
//	/**
//	 * Retrives all relationships with the specified type.
//	 * 
//	 * @param relationshipType
//	 *            The filter specifying the relationship type.
//	 * @return All relationships with the specified relationship type.
//	 * @throws OpenXML4JException
//	 */
//	public RelationshipCollection getRelationshipsByType(
//			String relationshipType) throws IllegalArgumentException,
//			OpenXML4JException {
//		throwExceptionIfWriteOnly();
//		if (relationshipType == null) {
//			throw new IllegalArgumentException("relationshipType");
//		}
//		return getRelationshipsHelper(relationshipType);
//	}
//
//	/**
//	 * Retrieves all relationships with specified id (normally just one because
//	 * a relationship id is supposed to be unique).
//	 * 
//	 * @param id
//	 *            Id of the wanted relationship.
//	 * @throws OpenXML4JException
//	 */
//	private RelationshipCollection getRelationshipsHelper(String id)
//			throws OpenXML4JException {
//		throwExceptionIfWriteOnly();
//		ensureRelationships();
//		return this.packagerelationships.getRelationships(id);
//	}
//	
//	/**
//	 * Add a relationship to a part.
//	 * 
//	 * @param targetUri
//	 *            URI de la partie cible, attention celle-ci doit �tre relative
//	 *            par rapport au r�pertoire source de la partie.
//	 * @param targetMode
//	 *            mode [Internal|External].
//	 * @param relationshipType
//	 *            kind of relationship.
//	 * @return the added relationship
//	 */
//	public Relationship createRelationship(URI targetUri,
//			TargetMode targetMode, String relationshipType) {
//		if (targetMode == null) {
//			targetMode = TargetMode.INTERNAL;
//		}
//		try {
//			ensureRelationships();
//		} catch (OpenXML4JException e) {
//			// Do nothing. Normaly would not happen in this case.
//		}
//		return createRelationship(targetUri, targetMode, relationshipType, null);
//	}
//

//
//	/**
//	 * Delete the relationship specified by its id.
//	 * 
//	 * @param id
//	 *            The ID identified the part to delete.
//	 */
//	public void deleteRelationship(String id) {
//		this.container.throwExceptionIfReadOnly();
//		if (this.relationships != null)
//			this.relationships.removeRelationship(id);
//	}
//
//	/**
//	 * Retrieve all the relationships attached to this part.
//	 * 
//	 * @return This part's relationships.
//	 * @throws OpenXML4JException
//	 */
//	public RelationshipCollection getRelationships()
//			throws OpenXML4JException {
//		return getRelationshipsCore(null);
//	}
//
//	/**
//	 * Retrieves a package relationship from its id.
//	 * 
//	 * @param id
//	 *            ID of the package relationship to retrieve.
//	 * @return The package relationship
//	 */
//	public Relationship getRelationship(String id) {
//		return this.relationships.getRelationshipByID(id);
//	}
//
//	/**
//	 * Retrieve all relationships attached to this part which have the specified
//	 * type.
//	 * 
//	 * @param relationshipType
//	 *            Relationship type filter.
//	 * @return All relationships from this part that have the specified type.
//	 * @throws InvalidFormatException
//	 *             If an error occurs while parsing the part.
//	 * @throws InvalidOperationException
//	 *             If the package is open in write only mode.
//	 */
//	public RelationshipCollection getRelationshipsByType(
//			String relationshipType) throws InvalidFormatException {
//		container.throwExceptionIfWriteOnly();
//
//		return getRelationshipsCore(relationshipType);
//	}
//
//	/**
//	 * Implementation of the getRelationships method().
//	 * 
//	 * @param filter
//	 *            Relationship type filter. If <i>null</i> then the filter is
//	 *            disabled and return all the relationships.
//	 * @return All relationships from this part that have the specified type.
//	 * @throws InvalidFormatException
//	 *             Throws if an error occurs during parsing the relationships
//	 *             part.
//	 * @throws InvalidOperationException
//	 *             Throws if the package is open en write only mode.
//	 * @see org.openxml4j.opc.PackagePart.getRelationshipsByType()
//	 */
//	private RelationshipCollection getRelationshipsCore(String filter)
//			throws InvalidFormatException {
//		this.container.throwExceptionIfWriteOnly();
//		if (relationships == null) {
//			this.throwExceptionIfRelationship();
//			relationships = new RelationshipCollection(this);
//		}
//		return new RelationshipCollection(relationships, filter);
//	}
//
//	/**
//	 * Knows if the part have any relationships.
//	 * 
//	 * @return <b>true</b> if the part have at least one relationship else
//	 *         <b>false</b>.
//	 */
//	public boolean hasRelationships() {
//		if (relationships != null && relationships.size() > 0) {
//			return true;
//		} else {
//			return container.partExists(URIHelper
//					.getRelationshipPartName(partName));
//		}
//	}
//
//	/**
//	 * Checks if the specified relationship is part of this package part.
//	 * 
//	 * @param rel
//	 *            The relationship to check.
//	 * @return <b>true</b> if the specified relationship exists in this part,
//	 *         else returns <b>false</b>
//	 */
//	@SuppressWarnings("finally")
//	public boolean isRelationshipExists(Relationship rel) {
//		try {
//			for (Relationship r : this.getRelationships()) {
//				if (r == rel)
//					return true;
//			}
//		} finally {
//			return false;
//		}
//	}
	
	
	

}
