package org.docx4j.toc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTSdtDocPart;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;

public class TocSdtUtils {
	
	private static final String PRESERVE = "preserve";

	private static ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
	

    public static SdtBlock createSdt(){

    	org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

    	SdtBlock sdtBlock = wmlObjectFactory.createSdtBlock();

    	SdtPr sdtpr = wmlObjectFactory.createSdtPr(); 
    	    // Create object for docPartObj (wrapped in JAXBElement) 
    	    CTSdtDocPart sdtdocpart = wmlObjectFactory.createCTSdtDocPart(); 
    	    JAXBElement<org.docx4j.wml.CTSdtDocPart> sdtdocpartWrapped = wmlObjectFactory.createSdtPrDocPartObj(sdtdocpart); 
    	    sdtpr.getRPrOrAliasOrLock().add( sdtdocpartWrapped); 
    	        // Create object for docPartGallery
    	        CTSdtDocPart.DocPartGallery sdtdocpartdocpartgallery = wmlObjectFactory.createCTSdtDocPartDocPartGallery(); 
    	        sdtdocpart.setDocPartGallery(sdtdocpartdocpartgallery); 
    	            sdtdocpartdocpartgallery.setVal( "Table of Contents"); 
    	        // Create object for docPartUnique
    	        BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory.createBooleanDefaultTrue(); 
    	        sdtdocpart.setDocPartUnique(booleandefaulttrue); 
    	    // Create object for id
	        sdtpr.setId();

		sdtBlock.setSdtPr(sdtpr);
		
		return sdtBlock;
    }

    public static SdtContentBlock createSdtContent(){
        return Context.getWmlObjectFactory().createSdtContentBlock(); 
    }
	
    /**
     * @return
     */
    public static List<R> getTocInstruction(String tocInstruction) {
    	
        List<R> fieldRs = new ArrayList<R>();
        
        // Create object for r
        R r2 = wmlObjectFactory.createR();
        fieldRs.add(r2);
        // Create object for fldChar (wrapped in JAXBElement)
        FldChar fldchar = wmlObjectFactory.createFldChar();
        JAXBElement<FldChar> fldcharWrapped = wmlObjectFactory.createRFldChar(fldchar);
        r2.getContent().add(fldcharWrapped);
        fldchar.setFldCharType(STFldCharType.BEGIN);
        // Create object for r
        R r3 = wmlObjectFactory.createR();
        fieldRs.add(r3);
        // Create object for instrText (wrapped in JAXBElement)
        Text text2 = wmlObjectFactory.createText();
        JAXBElement<Text> textWrapped2 = wmlObjectFactory.createRInstrText(text2);
        r3.getContent().add(textWrapped2);
        text2.setValue(tocInstruction);
        text2.setSpace(PRESERVE);
        // Create object for r
        R r4 = wmlObjectFactory.createR();
        fieldRs.add(r4);
        // Create object for fldChar (wrapped in JAXBElement)
        FldChar fldchar2 = wmlObjectFactory.createFldChar();
        JAXBElement<FldChar> fldcharWrapped2 = wmlObjectFactory.createRFldChar(fldchar2);
        r4.getContent().add(fldcharWrapped2);
        fldchar2.setFldCharType(STFldCharType.SEPARATE);
        
        return fieldRs;
        
    }
    
    public static P getLastParagraph() {
        // Create object for p
        P p = wmlObjectFactory.createP();
        // Create object for r
        R r2 = wmlObjectFactory.createR();
        p.getContent().add(r2);
        // Create object for fldChar (wrapped in JAXBElement)
        FldChar fldchar = wmlObjectFactory.createFldChar();
        JAXBElement<FldChar> fldcharWrapped = wmlObjectFactory.createRFldChar(fldchar);
        r2.getContent().add(fldcharWrapped);
        fldchar.setFldCharType(STFldCharType.END);
        return p;
    }
    
}
