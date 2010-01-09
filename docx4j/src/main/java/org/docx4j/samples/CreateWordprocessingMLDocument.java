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


package org.docx4j.samples;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.contenttype.CTDefault;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.contenttype.ObjectFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.Tbl;

/**
 * Creates a WordprocessingML document from scratch. 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CreateWordprocessingMLDocument {

	public static void main(String[] args) throws Exception {
		
		boolean save = true;
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		wordMLPackage.getMainDocumentPart()
			.addStyledParagraphOfText("Title", "Hello world");

		wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");
		
		// To get bold text, you must set the run's rPr@w:b,
	    // so you can't use the createParagraphOfText convenience method

		//org.docx4j.wml.P p = wordMLPackage.getMainDocumentPart().createParagraphOfText("text");
		
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.P  p = factory.createP();

		org.docx4j.wml.Text  t = factory.createText();
		t.setValue("text");

		org.docx4j.wml.R  run = factory.createR();
		run.getRunContent().add(t);		
		
		p.getParagraphContent().add(run);
		
		
		org.docx4j.wml.RPr rpr = factory.createRPr();		
		org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
	    b.setVal(true);	    
	    rpr.setB(b);
	    
		run.setRPr(rpr);
		
		// Optionally, set pPr/rPr@w:b		
	    org.docx4j.wml.PPr ppr = factory.createPPr();	    
	    p.setPPr( ppr );
	    org.docx4j.wml.ParaRPr paraRpr = factory.createParaRPr();
	    ppr.setRPr(paraRpr);	    
	    rpr.setB(b);
	    
	            
	    wordMLPackage.getMainDocumentPart().addObject(p);
	    
	    
	    // Here is an easier way:
	    String str = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" ><w:r><w:rPr><w:b /></w:rPr><w:t>Bold, just at w:r level</w:t></w:r></w:p>";
	    
	    wordMLPackage.getMainDocumentPart().addObject(
	    			org.docx4j.XmlUtils.unmarshalString(str) );
	    
	    // Let's add a table
	    int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
	    int cols = 3;
	    int cellWidthTwips = new Double( 
	    							Math.floor( (writableWidthTwips/cols ))
	    								).intValue();
	    
	    Tbl tbl = TblFactory.createTable(3, 3, cellWidthTwips);
	    wordMLPackage.getMainDocumentPart().addObject(tbl);
	    
		
	    // Add an altChunk
	    // .. the part
	    String html = "<html><head><title>Import me</title></head><body><p>Hello World!</p></body></html>";
	    AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/hw.html") ); 
	    afiPart.setBinaryData(html.getBytes());
	    afiPart.setContentType(new ContentType("text/html"));
	    Relationship altChunkRel = wordMLPackage.getMainDocumentPart().addTargetPart(afiPart);
	    // .. the bit in document body
	    CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
	    ac.setId(altChunkRel.getId() );
	    wordMLPackage.getMainDocumentPart().addObject(ac);

	    // .. content type
	    wordMLPackage.getContentTypeManager().addDefaultContentType("html", "text/html");
	    
		//injectDocPropsCustomPart(wordMLPackage);
		
		// Now save it
		if (save) {
			System.out.println("Saved.");
			wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/ad.docx") );
		} else {
		   	// Create a org.docx4j.wml.Package object
			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
			org.docx4j.xmlPackage.Package pkg = worker.get();
	    	
	    	// Now marshall it
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			System.out.println( "\n\n OUTPUT " );
			System.out.println( "====== \n\n " );	
			marshaller.marshal(pkg, System.out);				
			
		}
		
		System.out.println("Done.");
				
	}
	
	public static void injectDocPropsCustomPart(WordprocessingMLPackage wordMLPackage) {
		
		try {
			org.docx4j.openpackaging.parts.DocPropsCustomPart docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
			
			java.io.InputStream is = new java.io.FileInputStream("/tmp/custompart.xml" );
			
			docPropsCustomPart.unmarshal(is);
			
			wordMLPackage.addTargetPart(docPropsCustomPart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
