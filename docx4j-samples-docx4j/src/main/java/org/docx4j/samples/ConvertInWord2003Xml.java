package org.docx4j.samples;

import java.io.File;
import java.io.IOException;

import jakarta.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.docx4j.convert.in.word2003xml.Word2003XmlConverter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * This is a simple proof of concept of
 * converting Word 2003 XML to ECMA 376 docx.
 */
public class ConvertInWord2003Xml {

	public static void main(String[] args) throws IOException, JAXBException, Docx4JException {
		
		boolean save = true;
		
		File file = new File(System.getProperty("user.dir")
				+ "/sample-docs/2003/word2003xml.xml");
			// It works for this document, but that's the only one tested so far.  
			// This is currently just a proof of concept, but contributed improvements are welcome.
		
		Source source = new StreamSource(FileUtils.openInputStream(file));
		// Beware that StreamSource(is) is vulnerable to XXE, but this is just demo code here
		
		Word2003XmlConverter conv = new Word2003XmlConverter(source);
		
		WordprocessingMLPackage wordMLPackage = conv.getWordprocessingMLPackage();
		
	   	// Pretty print the main document part
//		System.out.println(
//				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true) );
		
		// Optionally save it
		if (save) {
			String filename = System.getProperty("user.dir") + "/OUT_FromWord2003XML.docx";
			wordMLPackage.save(new java.io.File(filename) );
			System.out.println("Saved " + filename);
		}
		

	}
}
