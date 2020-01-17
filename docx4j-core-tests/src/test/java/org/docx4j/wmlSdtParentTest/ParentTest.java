package org.docx4j.wmlSdtParentTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.junit.Test;

public class ParentTest {
	
	/**
	 * Unmarshalling sets sdtcontentblock.getParent() correctly.
	 * 
	 * @throws InvalidFormatException
	 * @throws JAXBException
	 */ 
	@Test
	public void testUnmarshalledSdtContentBlockParent() throws InvalidFormatException, JAXBException {
		
	
		String openXML = "<w:document mc:Ignorable=\"w14 wp14\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\" xmlns:wp14=\"http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing\">"
	            + "<w:body>"
	                    + "<w:sdt>" // SdtBlock
	                            + "<w:sdtPr>"
	                                    + "<w:id w:val=\"-738631422\"/>"
	                            + "</w:sdtPr>"
	
	                            + "<w:sdtContent>" // SdtContentBlock
	                                    + "<w:p>"
	                                            + "<w:r>"
	                                                    + "<w:t>Some content</w:t>"
	                                            + "</w:r>"	
	                                    + "</w:p>"
	                            + "</w:sdtContent>"
	
	                    + "</w:sdt>"
	            + "</w:body>"
	
		    + "</w:document>"
		;
		Document document = (Document)XmlUtils.unmarshalString(openXML);
	
	
        SdtBlock sdtblock = (SdtBlock)document.getContent().get(0);
        
        SdtContentBlock sdtcontentblock = (SdtContentBlock)sdtblock.getSdtContent();
        
        assertEquals(sdtblock, sdtcontentblock.getParent());
        
        // same for sdtpr
        SdtPr sdtPr = sdtblock.getSdtPr();
        assertEquals(sdtblock, sdtPr.getParent());
        
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(document);

        assertEquals(sdtblock, sdtcontentblock.getParent());
        assertEquals(sdtblock, sdtPr.getParent());
        
	}

	@Test
	public void testJAXBElementInSdtContentBlock() throws InvalidFormatException, JAXBException {
		
		
		String openXML = "<w:document mc:Ignorable=\"w14 wp14\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\" xmlns:wp14=\"http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing\">"
	            + "<w:body>"
	                    + "<w:sdt>" // SdtBlock
	                            + "<w:sdtPr>"
	                                    + "<w:id w:val=\"-738631422\"/>"
	                            + "</w:sdtPr>"
	
	                            + "<w:sdtContent>" // SdtContentBlock
	                                    + "<w:p>"
	                                            + "<w:r>"
	                                                    + "<w:t>Some content</w:t>"
	                                            + "</w:r>"	
	                                    + "</w:p>"
	                            + "</w:sdtContent>"
	
	                    + "</w:sdt>"
	            + "</w:body>"
	
		    + "</w:document>"
		;
		Document document = (Document)XmlUtils.unmarshalString(openXML);
	
	
        SdtBlock sdtblock = (SdtBlock)document.getContent().get(0);
        
        SdtContentBlock sdtcontentblock = (SdtContentBlock)sdtblock.getSdtContent();

        JAXBElement<CTBookmark> myBookmark = Context.getWmlObjectFactory().createSdtContentBlockBookmarkStart(new CTBookmark());
        
        sdtcontentblock.getContent().add(myBookmark);

        assertEquals(sdtcontentblock, myBookmark.getValue().getParent());
        
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(document);

        assertEquals(sdtcontentblock, myBookmark.getValue().getParent());
        
	}
	
	@Test
	public void testJAXBElementInBody() throws InvalidFormatException, JAXBException {
		
		
		Document doc = new Document();
		Body body = new Body();
		doc.setBody(body);

        JAXBElement<CTBookmark> myBookmark = Context.getWmlObjectFactory().createSdtContentBlockBookmarkStart(new CTBookmark());
        
        body.getContent().add(myBookmark);
        
        assertEquals(body, myBookmark.getValue().getParent());
        
        // Now, check that traversing doesn't change anything
        (new TestCallback()).walkJAXBElements(doc);

        assertEquals(body, myBookmark.getValue().getParent());
        
	}
	
	@Test
	public void testJAXBElement() throws InvalidFormatException, JAXBException {
		
		
		CTBookmark myBookmark = new CTBookmark();
		
        JAXBElement<CTBookmark> myJAXBElement = Context.getWmlObjectFactory().createSdtContentBlockBookmarkStart(myBookmark);
        
        assertNull(myBookmark.getParent());
        
        R r = new R();
        r.getContent().add(myJAXBElement);
        
        assertEquals(r, myBookmark.getParent());
        
	}
	
	
	@Test
	public void testSdtContentBlockParent() throws InvalidFormatException,
			JAXBException {
		
		SdtBlock sdtBlock = Context.getWmlObjectFactory().createSdtBlock();
		SdtContentBlock sdtContentBlock = Context.getWmlObjectFactory()
				.createSdtContentBlock();
		sdtBlock.setSdtContent(sdtContentBlock);
		
        SdtPr sdtpr = Context.getWmlObjectFactory().createSdtPr(); 
        sdtBlock.setSdtPr(sdtpr);		
		
//		P p = Context.getWmlObjectFactory().createP();
//		sdtContentBlock.getContent().add(p);
		
        assertEquals(sdtBlock, sdtContentBlock.getParent());

        // same for sdtpr
        SdtPr sdtPr = sdtBlock.getSdtPr();
        assertEquals(sdtBlock, sdtPr.getParent());
        
	}

	@Test
	public void testRunParent() throws InvalidFormatException, JAXBException {
		
		P p = Context.getWmlObjectFactory().createP();
		R r = Context.getWmlObjectFactory().createR();
		p.getContent().add(r);
		
        assertEquals(p, r.getParent());
				
		String result = XmlUtils.marshaltoString(p, true);
		P p2 = (P) XmlUtils.unmarshalString(result);
		R r2 = (R) p2.getContent().get(0);

        assertEquals(p2, r2.getParent());
	}
	
	class TestCallback extends CallbackImpl {

		@Override
		public List<Object> apply(Object o) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}