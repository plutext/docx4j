package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.Binder;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.w3c.dom.Node;

public class AlternateContentPreprocessor {

	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback.
	 */
	@Test
	public void testInvoice() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/sample-docs/word/2010/2010-mcAlternateContent.docx";	    	
		
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				

		// Since the JAXB binding stuff seems to remember
		// old artifacts, we'll first create a 'clean' object here
		// from something we've marshalled to.  
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(
				wordMLPackage.getMainDocumentPart().getJaxbElement());				
		Binder<Node> binder = Context.jc.createBinder();
		Object jaxbElement =  (org.docx4j.wml.Document) binder.unmarshal(xmlNode);
						
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:pict/v:oval", false );		
		int count = list.size();
		
		assertTrue("expected oval but got " + count, count==1 );
				
	}

	
	
}
