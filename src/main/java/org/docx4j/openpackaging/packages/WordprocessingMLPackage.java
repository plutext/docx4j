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
	
	
	// 20070812 - nb although io.LoadFromJCR will 
	// detect a WordprocessingMLPackage and instantiate
	// this class, at present this class doesn't 
	// do anything that Package doesn't do!
	
	
	// Main document
	protected MainDocumentPart mainDoc;
	
	// (optional) Glossary document
	protected GlossaryDocumentPart glossaryDoc;
	
	public WordprocessingMLPackage() {
		super();
		try {	
			
			//for (PackagePart part : p.getParts() ) {
			//	log.debug(part.getPartName() + " --- " + part.getContentType());
			//}
			
			// Have to do this for side effect or org.openxml4j.opc.Package.getRelationshipsByType
			// will later give a NullPointerException
//			PackageRelationshipCollection packageRelationshipCollection = 
//				p.getRelationships();
//	
//			// CORE_DOCUMENT
//			PackageRelationship pRelationship = p
//					.getRelationshipsByType(
//							PackageRelationshipTypes.CORE_DOCUMENT )
//					.getRelationship(0);
//			// Get the Core Document part from the relationship
//			PackagePart mainDocPart = p
//					.getPart(pRelationship);
//			InputStream inStream = mainDocPart.getInputStream();
//			mainDoc = new MainDocumentPart();
			
			// can't just do 
			//	MainDocumentPart mainDoc = (MainDocumentPart)p.getPart(pRelationship);
			//	InputStream inStream = mainDoc.getInputStream();
			// since this is casting ZipPackagePart to MainDocumentPart
			// Should I make MainDocumentPart extend ZipPackagePart? 
			
//			mainDoc.load(inStream);
//			inStream.close();

			// STYLE_DOCUMENT
/*
  			PackageRelationshipCollection mainDocRelationshipCollection = 
				new PackageRelationshipCollection(mainDocPart);

			// Add those to the relationships known to the package
			for (Iterator i = mainDocRelationshipCollection.iterator(); i.hasNext(); ) {
				// add the relation to the other PCR
				PackageRelationship rel = (PackageRelationship)i.next();
				log.debugprintln("Adding " + (rel.getTargetURI()).toString() + rel.getTargetMode() 
						+ rel.getRelationshipType() + rel.getId());
				p.addRelationship(new PackagePartName(rel.getTargetURI(), true), rel.getTargetMode(), 
						rel.getRelationshipType());
			}
*/
			
/*			//getPart gives NullPointerException
			pRelationship = p
					.getRelationshipsByType(
							PackageRelationshipTypes.STYLE_PART )
					.getRelationship(0);
			PackagePart pPart = p
					.getPart(pRelationship);
*/		 
//			PackagePart pPart = p.getPart(new PackagePartName("/word/styles.xml", true));
//			inStream = pPart.getInputStream();
//			StyleDefinitionsPart stylePart = 
//				new StyleDefinitionsPart(p, pPart.getPartName());
//			stylePart.load(inStream);
//			//mainDoc.setStyleDefinitionsPart( stylePart );
//			inStream.close();			
			
			/** Then we want to do something like:
			 * 
			 *  coreDoc.setNumberingDefinitionsPart ( xxx );
			 * 
			 */
			
			
			// Add a paragraph to the end of the document
			
			
			
			/* Now save the document.
			 * 
			 *  How to do this?
			 *  
			 *  We simply use the methods in the existing Parts.
			 *  
			 *  Alternative approaches which rely on my Parts extending
			 *  the existing Parts.
			 *  
			 *  Approach 1:
			 *  -----------
			 *  // remove the original CORE_DOCUMENT
			 *  removePart(PackagePartName partName)
			 *  // add our new one in its place
			 *  addPackagePart(PackagePart part)
			 *  // (Then what about its rel listing?)
			 *  
			 *  Approach 2:
			 *  -----------
			 *  Keep the original Part (ie mainDocPart), but replace its contents
			 *  
			*/
			
			//p.setPackageAccess(PackageAccess.READ_WRITE); // had to create this if not opened it READ_WRITE in the first place!

			// Save the XML structure into the part
//			mainDoc.save(mainDocPart.getOutputStream());
			
			// The document will save using:
			//p.save(new java.io.File("/home/jharrop/text.docx"));
			
			
		} catch (Exception e ) {
			e.printStackTrace();
		}
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
		org.docx4j.jaxb.document.ObjectFactory factory = new org.docx4j.jaxb.document.ObjectFactory();

		org.docx4j.jaxb.document.Text  t = factory.createText();
		t.setValue("Hello world, from docx4j");

		org.docx4j.jaxb.document.R  run = factory.createR();
		run.getRunContent().add(t);		
		
		org.docx4j.jaxb.document.P  para = factory.createP();
		para.getParagraphContent().add(run);
		
		org.docx4j.jaxb.document.Body  body = factory.createBody();
		body.getBlockLevelElements().add(para);
		
		org.docx4j.jaxb.document.Document wmlDocumentEl = factory.createDocument();
		wmlDocumentEl.setBody(body);
		

		// Create main document part
		Part corePart = new org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart(new PartName("/word/document.xml"));
		
		// Put the content in the part
		((org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart)corePart).setDocumentObj(wmlDocumentEl);
		
		corePart.setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_DOCUMENT));
		corePart.setRelationshipType(Namespaces.DOCUMENT);
		
		// Make getMainDocumentPart() work
		p.setPartShortcut(corePart, corePart.getRelationshipType());
		
		// Create the PackageRelationships part	
		RelationshipsPart rp = new RelationshipsPart( new PartName("/_rels/.rels"), p );
		
		// Make sure content manager knows how to handle .rels
		ctm.addDefaultContentType("rels", org.docx4j.openpackaging.contenttype.ContentTypes.RELATIONSHIPS_PART);
		
		// Add it to the package
		p.setRelationships(rp);
				
		// Add the main document part to the package relationships
		rp.addPart(corePart, p.getContentTypeManager());
		
		// Return the new package
		return p;

		
	}

	
}
