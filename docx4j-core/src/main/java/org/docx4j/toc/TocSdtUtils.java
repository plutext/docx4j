package org.docx4j.toc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTSdtDocPart;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TocSdtUtils {
	
	private static Logger log = LoggerFactory.getLogger(TocSdtUtils.class);		
	
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
	
	public static void addTocHeading(MainDocumentPart documentPart, TocStyles tocStyles, SdtContentBlock sdtContent) throws TocException {

		addTocHeading(documentPart, tocStyles, sdtContent, Toc.DEFAULT_TOC_HEADING);
	}
	
	public static void addTocHeading(MainDocumentPart documentPart, TocStyles tocStyles, SdtContentBlock sdtContent, String tocHeading) throws TocException {
		/* Is there already a style with *name* (not @styleId): 
		 * 
		 *   <w:style w:type="paragraph" w:styleId="CabealhodoSumrio">
				<w:name w:val="TOC Heading"/>
		 *
		 * If not, fall back to default, or failing that, create it from the XML given here. 
		 * 
		 * NB: avoid basedOn since that points to Style ID, which is language dependent
		 * (ie Heading 1 in English is something different in French, German etc) 
		 */
		String TOC_HEADING_STYLE = tocStyles.getStyleIdForName(TocStyles.TOC_HEADING);
		if (TOC_HEADING_STYLE==null) {
			log.warn("No definition found for TOC Heading style");
		
			String HEADING1_STYLE= tocStyles.getStyleIdForName(TocStyles.HEADING_1);
		
		    try {
		        if (TOC_HEADING_STYLE==null) {
		        	// We need to create it. 
		        	if (HEADING1_STYLE==null) {
		        		Style style = (Style)XmlUtils.unmarshalString(XML_TOCHeading_BasedOn_Nothing);
		        		style.getBasedOn().setVal(HEADING1_STYLE);
		        		documentPart.getStyleDefinitionsPart().getContents().getStyle().add(style);
		        		
		        	} else {
		        		// There is a heading 1 style, so use a simple style based on that
		        		Style style = (Style)XmlUtils.unmarshalString(XML_TOCHeading_BasedOn_Heading1);
		        		style.getBasedOn().setVal(HEADING1_STYLE);
		        		documentPart.getStyleDefinitionsPart().getContents().getStyle().add(style);
		        	}
		        	
		        	// either way,
		        	TOC_HEADING_STYLE = "TOCHeading";
		        }
			} catch (Exception e) {
				throw new TocException(e.getMessage(), e);
			}
		}
		
		// 1. Add Toc Heading (eg "Contents" in TOCHeading style)
		sdtContent.getContent().add(generateTocHeading(TOC_HEADING_STYLE, tocHeading));
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
    
    static P generateTocHeading(String headingStyleId) {
    	return generateTocHeading( headingStyleId, Toc.DEFAULT_TOC_HEADING) ;
    }
    
    private static P generateTocHeading(String headingStyleId, String tocHeading) {
        // Create object for p
        P p = wmlObjectFactory.createP();
        // Create object for pPr
        PPr ppr = wmlObjectFactory.createPPr();
        p.setPPr(ppr);
        // Create object for pStyle
        PPrBase.PStyle pprbasepstyle = wmlObjectFactory.createPPrBasePStyle();
        ppr.setPStyle(pprbasepstyle);
        pprbasepstyle.setVal(headingStyleId);
        // Create object for r
        R r = wmlObjectFactory.createR();
        p.getContent().add(r);
        // Create object for t (wrapped in JAXBElement)
        Text text = wmlObjectFactory.createText();
        JAXBElement<Text> textWrapped = wmlObjectFactory.createRT(text);
        r.getContent().add(textWrapped);
        text.setValue(tocHeading);
        return p;
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

    // Note these are only used if the style is not defined in the docx,
    // nor in the default styles read by TocStyles.
    private static String XML_TOCHeading_BasedOn_Nothing = "<w:style w:styleId=\"TOCHeading\" w:type=\"paragraph\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
            + "<w:name w:val=\"TOC Heading\"/>"
            // + "<w:basedOn w:val=\"Heading1\"/>"  // would be ok if provided already present, since 
            + "<w:next w:val=\"Normal\"/>"
            + "<w:uiPriority w:val=\"39\"/>"
            + "<w:semiHidden/>"
            + "<w:unhideWhenUsed/>"
            + "<w:qFormat/>"
            + "<w:pPr>"
	            + "<w:keepNext/>"
	            + "<w:keepLines/>"
	            + "<w:spacing w:after=\"0\" w:before=\"480\"/>"
                + "<w:outlineLvl w:val=\"9\"/>"
            +"</w:pPr>"
            + "<w:rPr>"
	            + "<w:rFonts w:asciiTheme=\"majorHAnsi\" w:cstheme=\"majorBidi\" w:eastAsiaTheme=\"majorEastAsia\" w:hAnsiTheme=\"majorHAnsi\"/>"
	            + "<w:b/>"
	            + "<w:bCs/>"
	            + "<w:color w:themeColor=\"accent1\" w:themeShade=\"BF\" w:val=\"365F91\"/>"
	            + "<w:sz w:val=\"28\"/>"
	            + "<w:szCs w:val=\"28\"/>"
            +"</w:rPr>"
        +"</w:style>";

    private static String XML_TOCHeading_BasedOn_Heading1 = "<w:style w:styleId=\"TOCHeading\" w:type=\"paragraph\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
            + "<w:name w:val=\"TOC Heading\"/>"
            + "<w:basedOn w:val=\"Heading1\"/>" // we'll overwrite with the style id 
            + "<w:next w:val=\"Normal\"/>"
            + "<w:uiPriority w:val=\"39\"/>"
            + "<w:semiHidden/>"
            + "<w:unhideWhenUsed/>"
            + "<w:qFormat/>"
            + "<w:pPr>"
                + "<w:outlineLvl w:val=\"9\"/>"
            +"</w:pPr>"
        +"</w:style>";    
    
}
