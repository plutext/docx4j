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


package org.docx4j.openpackaging;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.ExternalTarget;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;



public abstract class Base {

	protected static Logger log = LoggerFactory.getLogger(Base.class);

	public abstract OpcPackage getPackage(); 
	
	private Map<String, Object> userData = new HashMap<String, Object>();
	/**
	 * @param key
	 * @return
	 * @ since 3.0.0
	 */
	public Object getUserData(String key) {
		return userData.get(key);
	}
	/**
	 * An object allowing the user of the docx4j
	 * API to associate arbitrary data with this
	 * package/part while the package is in memory. 
	 * Note that the data is not saved when the package is
	 * saved.
	 * @param key
	 * @param value
	 * @ since 3.0.0
	 */
	public void setUserData(String key, Object value) {
		userData.put(key, value);
	}

	/**
	 * relationships - the package and each part can have one of these
	 * See eg 11. WordprocessingML [11.2 Package Structure].
	 * (Each Part is also the _target_ of a RelationshipPart,
	 * so see Part for definition of a corresponding field.)
	 */
	public RelationshipsPart relationships;

	/**
	 * Get the relationship part.
	 * From 2.7.1, the part will be created if it
	 * doesn't exist.  (SaveToZipFile will only
	 * save it if it contains rels) 
	 * 
	 * @return The relationship part name.
	 */
	public RelationshipsPart getRelationshipsPart() {
		return getRelationshipsPart(true);
	}
	
	/**
	 * Get the relationship part.
	 * @since 2.7.1
	 * 
	 * @param create  whether to create it if it doesn't exist
	 * @return
	 */
	public RelationshipsPart getRelationshipsPart(boolean createIfAbsent) {
//			throws InvalidOperationException {
		if (this instanceof org.docx4j.openpackaging.parts.relationships.RelationshipsPart) {
			// a relationship part can't have a relationship part
			// but should we throw an error here?
			//throw new InvalidOperationException();
			return null;
		} 
		
		if (relationships == null 
				&& createIfAbsent) {
			relationships = RelationshipsPart.createRelationshipsPartForPart(this);
		}
		
		return relationships;
	}

	public void setRelationships(RelationshipsPart relationships) {
				
		if (relationships!=null) {
			relationships.setPackage( getPackage() );
		}
		
		this.relationships = relationships;
	}
	
	
	protected ContentType contentType;

	public String getContentType() {
		if (contentType!=null ) {
			return contentType.toString();
		} else {
			log.warn("WARNING: content type was null. " +
					"You should set this before adding the part.");
			return null;
		}
	}

	public void setContentType(ContentType contentType) {
		log.debug("\nSet contentType " + contentType + " on part " + this.getPartName().getName() +"\n\n");
		this.contentType = contentType;
	}
	

	/**
	 * The part name. (required by the specification [M1.1])
	 * 
	 * You should use the getter/setter, rather than accessing this field directly!
	 * 
	 * Note that in docx4J, part names should be resolved,
	 * before being set, so that they are absolute
	 * (ie start with '/').
	 * 
	 * We will assume the Package has a part name of "/"
	 * 
	 */
	@Deprecated
	public PartName partName;

	/**
	 * @param partName
	 * @since 3.2.0
	 */
	public void setPartName(PartName partName) {
		this.partName = partName;
	}
	
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
	
	
	/** Tell the source Part about this target Part, so the
	 *  source can use this part via a convenience method.
	 * @param part
	 * @param relationshipType
	 * @return true if the source Part does use the target Part
	 * in a convenience method.
	 */ 
	public abstract boolean setPartShortcut(Part part, String relationshipType);

//	/**
//	 * Flag if a modification has been made.  
//	 * The semantics are as follows:
//	 * - on a Part: the Part, its relationships part, or a target part
//	 *   has been modified
//	 * - on the Package: its relationships part, or a target part
//	 *   has been modified. 
//	 */
//	protected boolean isDirty = false;
//	
//	public boolean isDirty() {
//		return isDirty;
//	}
//
//	public void setDirty(boolean isDirty) {
//		this.isDirty = isDirty;
//	}

	/**
	 * Convenience method to add a part to this Part's
	 * relationships.  The package must be set on this
	 * part in order for this to work.
	 * 
	 * @since 2.7.1
	 * 
	 * @param targetpart The part to add to this part's relationships
	 * @param mode whether to overwrite, rename or abort if the part name already exists
	 * @param proposedRelId
	 * @return
	 * @throws InvalidFormatException
	 */	
	public Relationship addTargetPart(Part targetpart, AddPartBehaviour mode
			) throws InvalidFormatException {
		return addTargetPart(targetpart, mode, null);
	}
	
	/**
	 * Convenience method to add a part to this Part's
	 * relationships.  The package must be set on this
	 * part in order for this to work.
	 * 
	 * The added part will replace any existing part
	 * with the same name (ie same target in the rels 
	 * part).  In other words, if you want to use the
	 * one image as the target of 2 rels, don't use
	 * this method. 
	 * 
	 * @param targetpart
	 *            The part to add to this part's relationships
	 */
	public Relationship addTargetPart(Part targetpart) throws InvalidFormatException {
		return this.addTargetPart(targetpart, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS, null);
	}

	/**
	 * Convenience method to add a part to this Part's
	 * relationships.  The package must be set on this
	 * part in order for this to work.
	 * 
	 * The added part will replace any existing part
	 * with the same name (ie same target in the rels 
	 * part).  In other words, if you want to use the
	 * one image as the target of 2 rels, don't use
	 * this method. 
	 * 
	 * @param targetpart
	 *            The part to add to this part's relationships
	 */
	public Relationship addTargetPart(Part targetpart, String proposedRelId
			) throws InvalidFormatException {
		
		return addTargetPart( targetpart, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS, proposedRelId);
	}
	
	
	/**
	 * Convenience method to add a part to this Part's
	 * relationships.  The package must be set on this
	 * part in order for this to work.
	 * 
	 * Whether the added part will replace any existing part
	 * with the same name depends on AddPartBehaviour setting.
	 * 
	 * @since 2.7.1
	 * 
	 * @param targetpart The part to add to this part's relationships
	 * @param mode whether to overwrite, rename or abort if the part name already exists
	 * @param proposedRelId
	 * @return
	 * @throws InvalidFormatException
	 */
	public Relationship addTargetPart(Part targetpart, AddPartBehaviour mode, String proposedRelId
			) throws InvalidFormatException {
		
		if (targetpart==null) {
			throw new InvalidFormatException("Your targetpart is null.");			
		}
		if ( this.getPackage()==null ) {						
			throw new InvalidFormatException("Package not set; if you are adding part2 to part1, make sure part1 is added first.");
		}
		if ( this instanceof RelationshipsPart ) {			
			throw new InvalidFormatException("You should add your part to the target part, not the target part's relationships part.");
		}
		
		// Now add the targetpart to the relationships
		Relationship rel = this.getRelationshipsPart().addPart(targetpart, mode, 
				getPackage().getContentTypeManager(), proposedRelId);
		
		// Finally, set part shortcut if there is one to set
		boolean shortcutSet = setPartShortcut(targetpart, targetpart.getRelationshipType());
		if (shortcutSet) {
			log.debug("shortcut was set");			
		}
		
		return rel;
		
	}

	/**
	 * Reinit fields so this pkg object can be re-used.
	 * @since 3.3.7
	 */
	public void reset() {
		userData = new HashMap<String, Object>();
		relationships=null;
		contentType=null;
		partName=null;
		log.info("reset complete");
	}
	
}
