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


package org.docx4j.openpackaging.packages;

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

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

	private static Logger log = Logger.getLogger(Package.class);

	/**
	 * Package parts collection.  This is a collection of _all_
	 * parts in the package (_except_ relationship parts), 
	 * not just those referred to by the package-level relationships.
	 */
	protected Parts parts = new Parts();

	/**
	 * Retrieve the Parts object.
	 */
	public Parts getParts() {

		return parts;		
	}
	
	protected ContentTypeManager contentTypeManager;

	public ContentTypeManager getContentTypeManager() {
		return contentTypeManager;
	}

	public void setContentTypeManager(ContentTypeManager contentTypeManager) {
		this.contentTypeManager = contentTypeManager;
	}
	
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */
	public Package() {
		try {
			partName = new PartName("/", false);
			
			contentTypeManager = new ContentTypeManagerImpl();
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}

	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public Package(ContentTypeManager contentTypeManager) {
		try {
			partName = new PartName("/", false);
			
			this.contentTypeManager = contentTypeManager;
		} catch (Exception e) {
			log.error(e.getMessage());
			// TODO: handle exception
		}
	}
	
	public Package getPackage() {
		return this;
	}
		
	
	protected DocPropsCorePart docPropsCorePart;

	protected DocPropsExtendedPart docPropsExtendedPart;
	
	protected DocPropsCustomPart docPropsCustomPart;

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
		if (docPropsCorePart==null) {
			try {
				Part docPropsCorePart = new org.docx4j.openpackaging.parts.DocPropsCorePart();
				this.addTargetPart(docPropsCorePart);
				org.docx4j.docProps.core.ObjectFactory factory = 
					new org.docx4j.docProps.core.ObjectFactory();				
				org.docx4j.docProps.core.CoreProperties properties = factory.createCoreProperties();
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCorePart).setJaxbElement((Object)properties);
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return docPropsCorePart;
	}

	public DocPropsExtendedPart getDocPropsExtendedPart() {
		if (docPropsExtendedPart==null) {
			try {
				Part docPropsExtendedPart = new org.docx4j.openpackaging.parts.DocPropsExtendedPart();
				this.addTargetPart(docPropsExtendedPart);
				org.docx4j.docProps.extended.ObjectFactory factory = 
					new org.docx4j.docProps.extended.ObjectFactory();				
				org.docx4j.docProps.extended.Properties properties = factory.createProperties();
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsExtendedPart).setJaxbElement((Object)properties);
				
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return docPropsExtendedPart;
	}

	public DocPropsCustomPart getDocPropsCustomPart() {
		
		if (docPropsCustomPart==null) {
			try {
				Part docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
				this.addTargetPart(docPropsCustomPart);
				
				org.docx4j.docProps.custom.ObjectFactory factory = 
					new org.docx4j.docProps.custom.ObjectFactory();
				
				org.docx4j.docProps.custom.Properties properties = factory.createProperties();
				((org.docx4j.openpackaging.parts.JaxbXmlPart)docPropsCustomPart).setJaxbElement((Object)properties);
				
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		return docPropsCustomPart;
	}




}
