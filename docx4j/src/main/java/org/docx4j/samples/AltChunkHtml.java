package org.docx4j.samples;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;

public class AltChunkHtml {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		String html = "<html><head><title>Import me</title></head><body><p>Hello World!</p></body></html>"; 
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/hw.html")); 
		afiPart.setBinaryData(html.getBytes()); 
		afiPart.setContentType(new ContentType("text/html")); 
		Relationship altChunkRel = wordMLPackage.getMainDocumentPart().addTargetPart(afiPart); 
		// .. the bit in document body 
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
		ac.setId(altChunkRel.getId() ); 
		wordMLPackage.getMainDocumentPart().addObject(ac); 
		// .. content type 
		wordMLPackage.getContentTypeManager().addDefaultContentType("html", "text/html"); 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/test.docx"));		
	}

}
