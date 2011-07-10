package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.Binder;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Hdr;
import org.junit.Test;
import org.w3c.dom.Node;

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
			+ "/sample-docs/word/2010/2010-mcAlternateContent.docx";	    	
		
		
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
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback; it tests the unmarshal
	 * method in MainDocumentPart
	 */
	@Test
	public void testAlternateContentMDP() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/sample-docs/word/2010/2010-mcAlternateContent-MDP.docx";	    	
		
		
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
			+ "/sample-docs/word/2010/2010-glow-then-AlternateContent.docx";	    	
		
		
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
			+ "/sample-docs/word/2010/2010-mcAlternateContent-in-header.docx";	    	
		
		
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
	// method in JaxbXmlPart, but I did test that by commenting out
	// the override in HeaderPart, and running the below test.
	
	/**
	 * This tests that an oval wrapped in AlternateContent 
	 * is resolved to the Fallback; it tests unmarshal(org.w3c.dom.Element el)
	 * in HeaderPart.
	 */
	@Test
	public void testHeaderFlatOPC() throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") 
			+ "/sample-docs/word/2010/2010-mcAlternateContent-in-header.xml";	    			
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));				

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
		org.w3c.dom.Document xmlNode = XmlUtils.marshaltoW3CDomDocument(header
				.getJaxbElement());
		Binder<Node> binder = Context.jc.createBinder();
		Object jaxbElement =  (Hdr) binder.unmarshal(xmlNode);
						
		List<Object> list = XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, 
				"//w:pict/v:oval", false );		
		int count = list.size();
		
		assertTrue("expected oval but got " + count, count==1 );
				
	}
	
}
