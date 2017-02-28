package org.docx4j.wml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
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
	                    + "<w:sdt>"
	                            + "<w:sdtPr>"
	                                    + "<w:id w:val=\"-738631422\"/>"
	                            + "</w:sdtPr>"
	
	                            + "<w:sdtContent>"
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
}