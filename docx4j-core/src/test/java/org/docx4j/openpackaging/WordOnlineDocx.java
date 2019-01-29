package org.docx4j.openpackaging;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBException;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

public class WordOnlineDocx {

	@Test
	public void openDocx() throws JAXBException, Docx4JException {
		
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File("src/test/resources/HelloWordOnline.docx"));

//		wordMLPackage.getMainDocumentPart().addParagraphOfText("hello");
//		wordMLPackage.getMainDocumentPart().marshal(System.out);
		
		assertTrue(wordMLPackage.getMainDocumentPart()!=null);
		assertTrue(wordMLPackage.getMainDocumentPart().getJaxbElement()!=null);
		
		// Note docx4j 6.0.x will write out the same dodgy rel:
		//     <Relationship Target="/word/document22.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Id="rId1"/>
		//                           ^
		// No attempt is made to normalise/fix this; tracking at https://github.com/plutext/docx4j/issues/332

	}

}
