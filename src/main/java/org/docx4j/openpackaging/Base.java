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


import org.apache.log4j.Logger;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;



public abstract class Base {

	protected static Logger log = Logger.getLogger(Base.class);

	public abstract Package getPackage(); 	

	
	/**
	 * relationships - the package and each part can have one of these
	 * See eg 11. WordprocessingML [11.2 Package Structure].
	 * (Each Part is also the _target_ of a RelationshipPart,
	 * so see Part for definition of a corresponding field.)
	 */
	protected RelationshipsPart relationships;

	/**
	 * Get the relationship part
	 * 
	 * @return The relationship part name.
	 */
	public RelationshipsPart getRelationshipsPart() {
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
				
		relationships.setPackage( getPackage() );
		
		this.relationships = relationships;
	}
	
	
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
	 * Note that in docx4J, part names should be resolved,
	 * before being set, so that they are absolute
	 * (ie start with '/').
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
	 * @param targetpart
	 *            The part to add to this part's relationships
	 */
	public Relationship addTargetPart(Part targetpart) throws InvalidFormatException {
		
		if ( this.getPackage()==null ) {
						
			throw new InvalidFormatException("Package not set");
		}

		// Create RelationshipsPart for this part if necessary
		if (this.getRelationshipsPart() == null ) {
			this.setRelationships(new RelationshipsPart( this ));
			
			// Make sure content manager knows how to handle .rels
			getPackage().getContentTypeManager().addDefaultContentType("rels", org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART);
			
		}
		
		// Now add the targetpart to the relationships
		Relationship rel = this.getRelationshipsPart().addPart(targetpart, getPackage().getContentTypeManager());
		
		// Finally, set part shortcut if there is one to set
		boolean shortcutSet = setPartShortcut(targetpart, targetpart.getRelationshipType());
		if (shortcutSet) {
			log.info("shortcut was set");			
		}
		
		return rel;
		
	}

	
	
}
