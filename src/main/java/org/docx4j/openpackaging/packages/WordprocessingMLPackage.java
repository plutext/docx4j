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

package org.docx4j.openpackaging.packages;


import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypeManagerImpl;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;






public class WordprocessingMLPackage extends Package {
	
	// What is a Word document these days?
	//
	// Well, a package is a logical entity which holds a collection of parts	
	// And a word document is exactly a WordProcessingML package	
	// Which has a Main Document Part, and optionally, a Glossary Document Part

	/* So its a Word doc if:
	 * 1. _rels/.rels tells you where to find an office document
	 * 2. [Content_Types].xml tells you that office document is   
	 *    of content type application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml
	
	 * A minimal docx has:
	 * 
	 * [Content_Types].xml containing:
	 * 1. <Default Extension="rels" ...
	 * 2. <Override PartName="/word/document.xml"...
	 * 
	 * _rels/.rels with a target for word/document.xml
	 * 
	 * word/document.xml
	 */
	
		
	
	// Main document
	protected MainDocumentPart mainDoc;
	
	// (optional) Glossary document
	protected GlossaryDocumentPart glossaryDoc;
	
	public WordprocessingMLPackage() {
		super();
	}
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.DOCUMENT)) {
			mainDoc = (MainDocumentPart)part;
			return true;			
		} else {	
			return false;
		}
	}
	
	public MainDocumentPart getMainDocumentPart() {
		return mainDoc;
	}

	public static WordprocessingMLPackage createTestPackage() throws InvalidFormatException {
		
				
		// Create a package
		WordprocessingMLPackage p = new WordprocessingMLPackage();

		// Add a ContentTypeManager to it
		ContentTypeManager ctm = new ContentTypeManagerImpl();
		p.setContentTypeManager(ctm);
		

		// Create main document part content
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("Hello world, from docx4j");

		org.docx4j.wml.R  run = factory.createR();
		run.getRunContent().add(t);		
		
		org.docx4j.wml.P  para = factory.createP();
		para.getParagraphContent().add(run);
		
		org.docx4j.wml.Body  body = factory.createBody();
		body.getBlockLevelElements().add(para);
		
		org.docx4j.wml.Document wmlDocumentEl = factory.createDocument();
		wmlDocumentEl.setBody(body);
		

		// Create main document part
		Part corePart = new org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart();
		
		// Put the content in the part
		((org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart)corePart).setJaxbElement(wmlDocumentEl);
				
		// Make getMainDocumentPart() work
		p.setPartShortcut(corePart, corePart.getRelationshipType());
		
		// Create the PackageRelationships part	
		RelationshipsPart rp = new RelationshipsPart( new PartName("/_rels/.rels"), p );
		
		// Make sure content manager knows how to handle .rels
		ctm.addDefaultContentType("rels", org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART);
		
		// Add it to the package
		p.setRelationships(rp);
			// TODO - this should happen automatically?
				
		// Add the main document part to the package relationships
		// and to [Content_Types].xml
		rp.addPart(corePart, p.getContentTypeManager());
		
		
		// Create a styles part
		Part stylesPart = new org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart();
		try {
			((org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart) stylesPart)
					.unmarshalDefaultStyles();
			
			// Now we need to add it the main document part's relationships
			RelationshipsPart docRels = new RelationshipsPart( new PartName("/word/_rels/document.xml.rels"), corePart );
				// TODO - should construct the relationships part name from the source part name
			corePart.setRelationships(docRels);
			// TODO - this should happen automatically?
			docRels.addPart(stylesPart, p.getContentTypeManager());			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();			
		}
		// Return the new package
		return p;

		
	}

	
}
