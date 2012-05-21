package org.docx4j.samples;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;

/**
 * Add HTML, in a way that leaves it up to downstream 
 * application (eg Word) to convert the content to docx content.
 * 
 * Unless you really want the HTML converted by Word,
 * consider converting your HTML to well-formed XHTML,
 * then using docx4j's XHTMLImporter instead.
 * 
 * @author jharrop
 *
 */
public class AltChunkAddOfTypeHtml {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		String html = "<html><head><title>Import me</title></head><body><p>Hello World!</p></body></html>"; 

		wordMLPackage.getMainDocumentPart().addAltChunk(AltChunkType.Html, html.getBytes()); 

		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/test.docx"));		
	}

}
