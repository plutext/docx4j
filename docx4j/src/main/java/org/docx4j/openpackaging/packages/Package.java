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
		
		corePart.setContentType(new org.docx4j.openpackaging.contenttype.ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml") );		
		
		// Create the PackageRelationships part	
		RelationshipsPart rp = new RelationshipsPart( new PartName("/_rels/.rels") );
		// Add it to the package
		p.setRelationships(rp);
				
		// Add it to the collection of parts
		rp.addPart(corePart);
		
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
