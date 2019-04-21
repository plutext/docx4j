/*
 *  Copyright 2007-2010, Plutext Pty Ltd.
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

package org.docx4j.samples;

import java.io.File;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.BufferUtil;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;

/**
 * Create a WordML Pkg and add a header to it.
 * Output is Flat OPC XML.
 * Notice:
 * 1. the Header part
 * 2. the contents of the sectPr element
 * 
 * @author jharrop
 *
 */
public class HeaderFooterCreate {

	private static ObjectFactory objectFactory = new ObjectFactory();

	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.createPackage();
		// Delete the Styles part, since it clutters up our output
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		Relationship styleRel = mdp.getStyleDefinitionsPart().getSourceRelationships().get(0);
		mdp.getRelationshipsPart().removeRelationship(styleRel);		

		// OK, the guts of this sample:
		// The 2 things you need:
		// 1. the Header part
		Relationship relationship = createHeaderPart(wordMLPackage);
		// 2. an entry in SectPr
		createHeaderReference(wordMLPackage, relationship);

		// Display the result as Flat OPC XML
//		FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
//		worker.marshal(System.out);
		
		Docx4J.save(wordMLPackage, new File("OUT_HeaderFooterCreate.docx") );
	}
	
	public static Relationship createHeaderPart(
			WordprocessingMLPackage wordprocessingMLPackage)
			throws Exception {
		
		HeaderPart headerPart = new HeaderPart();
		Relationship rel =  wordprocessingMLPackage.getMainDocumentPart()
				.addTargetPart(headerPart);
		
		// After addTargetPart, so image can be added properly
		headerPart.setJaxbElement(getHdr(wordprocessingMLPackage, headerPart));

		return rel;
	}

	public static void createHeaderReference(
			WordprocessingMLPackage wordprocessingMLPackage,
			Relationship relationship )
			throws InvalidFormatException {

		List<SectionWrapper> sections = wordprocessingMLPackage.getDocumentModel().getSections();
		   
		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr==null ) {
			sectPr = objectFactory.createSectPr();
			wordprocessingMLPackage.getMainDocumentPart().addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}

		HeaderReference headerReference = objectFactory.createHeaderReference();
		headerReference.setId(relationship.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);// add header or
		// footer references

	}
	
	public static Hdr getHdr(WordprocessingMLPackage wordprocessingMLPackage,
			Part sourcePart) throws Exception {

		Hdr hdr = objectFactory.createHdr();
		
		File file = new File(System.getProperty("user.dir") 
				+ "/src/test/resources/images/greentick.png" );
		java.io.InputStream is = new java.io.FileInputStream(file );
		
		hdr.getContent().add(
				newImage(wordprocessingMLPackage,
						sourcePart, 
						BufferUtil.getBytesFromInputStream(is), 
						"filename", "alttext", 1, 2
						)
		);
		return hdr;

	}
	
	

//	public static P getP() {
//		P headerP = objectFactory.createP();
//		R run1 = objectFactory.createR();
//		Text text = objectFactory.createText();
//		text.setValue("123head123");
//		run1.getRunContent().add(text);
//		headerP.getParagraphContent().add(run1);
//		return headerP;
//	}
	
	public static org.docx4j.wml.P newImage( WordprocessingMLPackage wordMLPackage,
			Part sourcePart,
			byte[] bytes,
			String filenameHint, String altText, 
			int id1, int id2) throws Exception {
		
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, 
        		sourcePart, bytes);
		
        Inline inline = imagePart.createImageInline( filenameHint, altText, 
    			id1, id2, false);
        
        // Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.P  p = factory.createP();
		org.docx4j.wml.R  run = factory.createR();		
		p.getContent().add(run);        
		org.docx4j.wml.Drawing drawing = factory.createDrawing();		
		run.getContent().add(drawing);		
		drawing.getAnchorOrInline().add(inline);
		
		return p;
		
	}	
	
	
	
}
