package org.docx4j.jaxb;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.junit.Test;

public class W15Test {

	/**
	 * Example of setting mc:Ignorable="w15",
	 * which you must do if you add eg w15:collapsed
	 * and want Word <=14 to be happy.
	 * 
	 * @throws InvalidFormatException
	 * @throws JAXBException
	 */
	@Test
	public void testIgnorableSet() throws InvalidFormatException, JAXBException {

		Document document = new Document();
		
		Body body = new Body();
		document.setBody(body);
		
		P p = new P();
		PPr ppr = new PPr();
		p.setPPr(ppr);
		body.getContent().add(p);
		
		ppr.setCollapsed(new BooleanDefaultTrue() );
		/*
		 * if you do setCollapsed, you must do:
		 */
		document.setIgnorable("w15");
		/*
		 * otherwise, Word 14 and lower will complain
		 * that the docx is invalid!
		 * 
		 * Similar consideration apply if you set w14 attributes 
		 * eg <w:p w14:textId="3313beef" w14:paraId="3313beef">
		 */
		
//		System.out.println(XmlUtils.marshaltoString(body, true, true));
		
		MainDocumentPart mdp = new MainDocumentPart(); 
		mdp.setJaxbElement(document);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		mdp.marshal(baos);
		
		String result = (new String(baos.toByteArray()));
		
		assertTrue(result.contains("mc:Ignorable=\"w15\"") );
	}

}
