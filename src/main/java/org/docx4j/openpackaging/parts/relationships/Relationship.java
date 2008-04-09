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

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.Part;
import org.dom4j.Attribute;
import org.dom4j.Element;




/**
 * A part relationship.  This is not a part as such.
 * But it belongs in the Relationships part.
 * 
 * @author Julien Chable
 * @version 1.0
 */
public final class Relationship {

	private static Logger log = Logger.getLogger(Relationship.class);
	
	private static URI containerRelationshipPart;

	static {
		try {
			containerRelationshipPart = new URI("/_rels/.rels");
		} catch (URISyntaxException e) {
			// Do nothing
		}
	}

	/* XML markup */

	public static final String ID_ATTRIBUTE_NAME = "Id";

	public static final String RELATIONSHIPS_TAG_NAME = "Relationships";

	public static final String RELATIONSHIP_TAG_NAME = "Relationship";

	public static final String TARGET_ATTRIBUTE_NAME = "Target";

	public static final String TARGET_MODE_ATTRIBUTE_NAME = "TargetMode";

	public static final String TYPE_ATTRIBUTE_NAME = "Type";

	/* End XML markup */

	/**
	 * L'ID de la relation.
	 */
	private String id;


	/**
	 * Type de relation.
	 */
	private String relationshipType;

	/**
	 * Source of the relation - usually
	 * a Part, but will be Package for
	 * the Package level relationships.
	 */
	private Base source;

	/**
	 * Le mode de ciblage [Internal|External]
	 */
	private TargetMode targetMode;

	/**
	 * URI de la partie cible.
	 */
	private URI targetUri;
	
	
	public Relationship(Base sourceP, Element element, boolean fCorePropertiesRelationship) throws InvalidFormatException {
		
		source = sourceP;
		
		// Relationship ID
		id = element.attribute(
				Relationship.ID_ATTRIBUTE_NAME).getValue();
		// Relationship type
		relationshipType = element.attribute(
				Relationship.TYPE_ATTRIBUTE_NAME).getValue();

		/* Check OPC Compliance */
		// Check Rule M4.1
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE))
			// TODO - fix this, when this constructor is invoked,
			// the boolean is always false?
			if (!fCorePropertiesRelationship)
				fCorePropertiesRelationship = true;
			else
				throw new InvalidFormatException(
						"OPC Compliance error [M4.1]: there is more than one core properties relationship in the package !");

		/* End OPC Compliance */

		// TargetMode (default value "Internal")
		Attribute targetModeAttr = element
				.attribute(Relationship.TARGET_MODE_ATTRIBUTE_NAME);
		targetMode = TargetMode.INTERNAL;
		if (targetModeAttr != null) {
			targetMode = targetModeAttr.getValue().toLowerCase()
					.equals("internal") ? TargetMode.INTERNAL
					: TargetMode.EXTERNAL;
		}

		// Target converted in URI
		String value = "";
		try {
			value = element.attribute(
					Relationship.TARGET_ATTRIBUTE_NAME)
					.getValue();

			if (value.indexOf("\\") != -1) {
				log
						.info("target contains \\ therefore not a valid URI"
								+ value + " replaced by /");
				value = value.replaceAll("\\\\", "/");
				// word can save external relationship with a \ instead
				// of /
			}

			targetUri = new URI(value);
		} catch (URISyntaxException e) {
			log.error("Cannot convert " + value
					+ " in a valid relationship URI-> ignored", e);
		}		
		
		
	}
	

	/**
	 * Constructor.
	 * 
	 * @param packageParent
	 * @param sourcePart
	 * 
	 *  TODO - think through whether to have sourcePart here, source PartName
	 *  or neither!
	 * 
	 * @param targetUri
	 * @param targetMode
	 * @param relationshipType
	 * @param id
	 */
	public Relationship(Base sourceP,
			URI targetUri, TargetMode targetMode, String relationshipType,
			String id) {
		if (targetUri == null)
			throw new IllegalArgumentException("targetUri");
		if (relationshipType == null)
			throw new IllegalArgumentException("relationshipType");
		if (id == null)
			throw new IllegalArgumentException("id");

		this.source = sourceP;
		this.targetUri = targetUri;
		this.targetMode = targetMode;
		this.relationshipType = relationshipType;
		this.id = id;
	}
	
	/* Create an XML element from the Java object tree
	 * 
	 */
	public Element marshall(Element relElem) {		
		
		// L'attribut ID
		relElem.addAttribute(Relationship.ID_ATTRIBUTE_NAME, getId());

		// L'attribut Type
		relElem.addAttribute(Relationship.TYPE_ATTRIBUTE_NAME, getRelationshipType());

		// L'attribut Target
		String targetValue;
		URI uri = getTargetURI();
		if (getTargetMode() == TargetMode.EXTERNAL) {
			// we have to add where the file can be found: http, file ...
			targetValue = uri.getScheme() + "://" + uri.getPath();

			// add TargetMode attribute (as it is external link external)
			relElem.addAttribute(
					Relationship.TARGET_MODE_ATTRIBUTE_NAME,
					"External");
		} else {

			// WARNING: if you make any changes here, ensure it can still
			// handle something like:
			//    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml" Target="../customXml/item1.xml"/>
			// ie with ..
			// You can use parts-test.docx to test.
			
			
			targetValue = targetUri.toString();
		}
		relElem.addAttribute(Relationship.TARGET_ATTRIBUTE_NAME,
				targetValue);	
		
		return relElem;
		
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Relationship)) {
			return false;
		}
		Relationship rel = (Relationship) obj;
		return (this.id == rel.id
				&& this.relationshipType == rel.relationshipType
				&& (rel.source != null ? rel.source.equals(this.source) : true)
				&& this.targetMode == rel.targetMode && this.targetUri
				.equals(rel.targetUri));
	}

	@Override
	public int hashCode() {
		return this.id.hashCode() + this.relationshipType.hashCode()
				+ this.source.hashCode() + this.targetMode.hashCode()
				+ this.targetUri.hashCode();
	}

	/* Getters */

	public URI getContainerPartRelationship() {
		return containerRelationshipPart;
	}

	/**
	 * @return the container
	 */
//	public Package getPackage() {
//		return container;
//	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id=id;
	}
	
	
	/**
	 * @return the relationshipType
	 */
	public String getRelationshipType() {
		return relationshipType;
	}

	/**
	 * @return the source
	 */
	public Base getSource() {
		return source;
	}

	/**
	 * 
	 * @return
	 */
	public URI getSourceURI() {
		if (source == null) {
			return URIHelper.PACKAGE_ROOT_URI;
		}
		return source.getPartName().getURI();
	}

	/**
	 * public URI getSourceUri(){ }
	 * 
	 * @return the targetMode
	 */
	public TargetMode getTargetMode() {
		return targetMode;
	}

	/**
	 * @return the targetUri
	 */
	public URI getTargetURI() {
/*		
		if (!targetUri.toASCIIString().startsWith("/")) {
			// So it's a relative part name, try to resolve it
			log.info("resolving " + targetUri + " against " + getSourceURI());
			log.info("RESULT: " + URIHelper.resolvePartUri(getSourceURI(), targetUri) );
			return URIHelper.resolvePartUri(getSourceURI(), targetUri);
		}*/
		return targetUri;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id == null ? "id=null" : "id=" + id);
//		sb.append(container == null ? " - container=null" : " - container="
//				+ container.toString());
		sb.append(relationshipType == null ? " - relationshipType=null"
				: " - relationshipType=" + relationshipType.toString());
		sb.append(source == null ? " - source=null" : " - source="
				+ getSourceURI().toASCIIString());
		sb.append(targetUri == null ? " - target=null" : " - target="
				+ getTargetURI().toASCIIString());
		sb.append(targetMode == null ? ",targetMode=null" : ",targetMode="
				+ targetMode.toString());
		return sb.toString();
	}
}