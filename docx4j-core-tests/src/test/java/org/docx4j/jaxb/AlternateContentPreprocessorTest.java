package org.docx4j.jaxb;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.xml.bind.Binder;

import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

/**
 * TODO: As of docx4j 3.3.8, mc:AlternateContent
 * is supported in w:r, so these tests need to be re-worked.
 *
 */
public class AlternateContentPreprocessorTest {

	
	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback; it tests the usual unmarshal
	 * method in JaxbXmlPart ie unmarshal( java.io.InputStream is )
	 * since CommentsPart uses that.
	 */
	@Test
	public void testAlternateContent() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/2010/2010-mcAlternateContent.docx";	    	
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				

		// Since the JAXB binding stuff seems to remember
		// old artifacts, we'll first create a 'clean' object here
		// from something we've marshalled to.  
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(
				wordMLPackage.getMainDocumentPart().getCommentsPart().getJaxbElement());				
		Binder<Node> binder = Context.jc.createBinder();
		Object jaxbElement =  (Comments) binder.unmarshal(xmlNode);
						
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:pict/v:oval", false );		
		int count = list.size();
		
		assertTrue("expected oval but got " + count, count==1 );				
	}

	/**
	 * Test we can recover from "Errors limit exceeded"
	 * with recent (non-MOXy) JAXB implementations (eg RI 2.2.11)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAlternateContentErrorLimit() throws Exception {
		
		int ERROR_LIMIT = 10;
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/2010/2010-mcAlternateContent.docx";	    	
		
		for (int i=0; i< (ERROR_LIMIT+5); i++) {

			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				
			org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(
					wordMLPackage.getMainDocumentPart().getCommentsPart().getJaxbElement());				
			
			Binder<Node> binder = Context.jc.createBinder();
			Object jaxbElement =  (Comments) binder.unmarshal(xmlNode);
							
			List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
					"//w:pict/v:oval", false );		
			int count = list.size();
			
			assertTrue("expected oval but got " + count, count==1 );
		}
	}	
	
	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback; it tests the unmarshal
	 * method in MainDocumentPart
	 */
	@Test
	public void testAlternateContentMDP() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/2010/2010-mcAlternateContent-MDP.docx";	    	
		
		
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


	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback, even in the presence
	 * of other unrecognised content (eg glow); it uses the unmarshal
	 * method in MainDocumentPart
	 */
	@Test
	public void testGlowAndAlternateContent() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/2010/2010-glow-then-AlternateContent.docx";	    	
		
		
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
	
	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback; it tests 
	 * unmarshal( java.io.InputStream is )
	 * in HeaderPart 
	 */
	@Test
	public void testHeaderDocx() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/2010/2010-mcAlternateContent-in-header.docx";	    	
		
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));

		List<SectionWrapper> sectionWrappers = wordMLPackage.getDocumentModel()
				.getSections();
		HeaderPart header = null;
		for (SectionWrapper sw : sectionWrappers) {
			HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
			if (hfp.getDefaultHeader() != null) {
				header = hfp.getDefaultHeader();
			}
		}
		
		// Since the JAXB binding stuff seems to remember
		// old artifacts, we'll first create a 'clean' object here
		// from something we've marshalled to.  
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(
				header.getJaxbElement());				
		Binder<Node> binder = Context.jc.createBinder();
		Object jaxbElement =  (Hdr) binder.unmarshal(xmlNode);
						
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:pict/v:oval", false );		
		int count = list.size();
		
		assertTrue("expected oval but got " + count, count==1 );
				
	}

	// There is currently no test for unmarshal(org.w3c.dom.Element el)
	// method in JaxbXml/Part, but I did test that by commenting out
	// the override in HeaderPart, and running the below test.
	
	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback; it tests unmarshal(org.w3c.dom.Element el)
	 * in HeaderPart.
	 */
	@Test
	public void testHeaderFlatOPC() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/2010/2010-mcAlternateContent-in-header.xml";	    			
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));	
		
		/*
		 * That unmarshalled via binder
		 * 
		 * DEBUG org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware .unmarshal line 559 - 
		 * For org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart, unmarshall via binder
		 * 
		 * but MOXy 2.5.2 and 2.6.3 seems to ignore event handler for that?
		 * 
		 * http://stackoverflow.com/questions/37225221/moxy-validationevents-triggered-by-unmarshaller-but-not-binder
		 * 
		 * docx4j 3.3.1 has a workaround for this in JaxbXmlPartXPathAware
		 */

		List<SectionWrapper> sectionWrappers = wordMLPackage.getDocumentModel()
				.getSections();
		HeaderPart header = null;
		for (SectionWrapper sw : sectionWrappers) {
			HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
			if (hfp.getDefaultHeader() != null) {
				header = hfp.getDefaultHeader();
			}
		}

		String headerXML = header.getXML();
		assertTrue(headerXML.contains("v:oval"));
		
		// Since the JAXB binding stuff seems to remember
		// old artifacts, we'll first create a 'clean' object here
		// from something we've marshalled to.
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(header
				.getJaxbElement());
		Binder<Node> binder = Context.jc.createBinder();
		JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
		binder.setEventHandler(eventHandler);
		Object jaxbElement =  (Hdr) binder.unmarshal(xmlNode);
						
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:pict/v:oval", false );		
		int count = list.size();
		
		assertTrue("expected oval but got " + count, count==1 );
				
	}

	@Test
	@Ignore
	public void testAltContentMarkSupported() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/parts/document_altContent.xml";
		
		// mark supported
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(new File(inputfilepath)));
		Document doc = (Document)XmlUtils.unmarshal(new ByteArrayInputStream(bytes));
		//System.out.println(XmlUtils.marshaltoString(doc));
		
		P p = (P)doc.getBody().getContent().get(0);
		R r = (R)p.getContent().get(0);
		
		Assert.assertTrue(
				XmlUtils.unwrap(r.getContent().get(0)) 
					instanceof org.docx4j.wml.Pict);		
	}
	
	@Test
	@Ignore
	public void testAltContentMarkNotSupported() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/src/test/resources/parts/document_altContent.xml";
		
		// mark not supported
		try {
			
			Object o = XmlUtils.unmarshal(new FileInputStream(new File(inputfilepath)));			
			System.out.println(XmlUtils.marshaltoString(o));
			Assert.fail("Expect an exception, since mark not supported");
			
		} catch (javax.xml.bind.UnmarshalException e){
			System.out.println("OK " + e.getMessage() + " OK");
		}
		

		
	}
	
}
