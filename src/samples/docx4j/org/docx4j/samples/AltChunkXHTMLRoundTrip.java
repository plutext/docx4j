package org.docx4j.samples;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * Create a docx containing an XHTML AltChunk,
 * and then convert that to normal docx content.
 * @author jharrop
 *
 */
public class AltChunkXHTMLRoundTrip {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("Paragraph 1");

		// Add the XHTML altChunk
		String xhtml = "<html><head><title>Import me</title></head><body><p>Hello World!</p></body></html>"; 
		mdp.addAltChunk(AltChunkType.Xhtml, xhtml.getBytes());
		
		mdp.addParagraphOfText("Paragraph 3");
		
		// Round trip
		WordprocessingMLPackage pkgOut = mdp.convertAltChunks();
		
		// Display result
		System.out.println(
				XmlUtils.marshaltoString(pkgOut.getMainDocumentPart().getJaxbElement(), true, true));
		
	}

}
