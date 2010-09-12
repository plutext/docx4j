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

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;

public class HeaderFooter {

	private static ObjectFactory objectFactory = new ObjectFactory();

	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.createPackage();

		MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
		
		Relationship relationship = createHeaderPart(wordMLPackage);
		
		createHeaderReference(wordMLPackage, relationship);

		wordMLPackage.save(new File(System.getProperty("user.dir"),
				"headerFooter.docx"));
//		mainDocumentPart.marshal(new FileOutputStream(new File(System
//				.getProperty("user.dir"), "headerfooter.xml")));

	}

	public static Relationship createHeaderPart(
			WordprocessingMLPackage wordprocessingMLPackage)
			throws Exception {
		
		HeaderPart headerPart = new HeaderPart();
		
		// Have to do this so that the next line can
		// add an image
		headerPart.setPackage(wordprocessingMLPackage);
		
		headerPart.setJaxbElement(getHdr(wordprocessingMLPackage, headerPart));
		return wordprocessingMLPackage.getMainDocumentPart()
				.addTargetPart(headerPart);

	}
	
	public static Hdr getHdr(WordprocessingMLPackage wordprocessingMLPackage,
			Part sourcePart) throws Exception {

		Hdr hdr = objectFactory.createHdr();
		hdr.getEGBlockLevelElts().add(
				newImage(wordprocessingMLPackage,
						sourcePart, getBytes(), "filename", "alttext", 1, 2
						)
		);
		return hdr;

	}
	
	public static byte[] getBytes() throws Exception {
		
		File file = new File("/home/dev/test.png" );
				
		java.io.InputStream is = new java.io.FileInputStream(file );
        long length = file.length();    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        if (length > Integer.MAX_VALUE) {
        	System.out.println("File too large!!");
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            System.out.println("Could not completely read file "+file.getName());
        }
        is.close();
        
        return bytes;				
		
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
    			id1, id2);
        
        // Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.P  p = factory.createP();
		org.docx4j.wml.R  run = factory.createR();		
		p.getParagraphContent().add(run);        
		org.docx4j.wml.Drawing drawing = factory.createDrawing();		
		run.getRunContent().add(drawing);		
		drawing.getAnchorOrInline().add(inline);
		
		return p;
		
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
	
}
