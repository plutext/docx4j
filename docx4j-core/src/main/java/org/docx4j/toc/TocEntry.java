/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.toc;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.paragraph.Indent;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TocEntry {
	
	private static Logger log = LoggerFactory.getLogger(TocEntry.class);	
	
	private TocEntry() {}
	
	public TocEntry(PropertyResolver propertyResolver, PageDimensions pageDimensions, STTabTlc leader) { 
		
		this.propertyResolver = propertyResolver;
		this.writableWidthTwips = pageDimensions.getWritableWidthTwips(); 
    	this.leader = leader;
	}
	
	
	private PropertyResolver propertyResolver;

    private static final String PRESERVE = "preserve";
    private static final String PAGEREF_MASK = "PAGEREF %s \\h";
    private static final String HYPERLINK = "Hyperlink";
    //private static final int DOTS_POSITION = 9345;
    
    /**
     * right tab pos= pg width – (gutter + left + right margin)
		best to put that explicitly in the ToC p, rather than the toc styles (which we just override)
		Word doesn’t update the right tab pos (where its in a ToC style) in response to a change in margin!
		
		@since 3.2.0
     */
    private int writableWidthTwips; 
    
    private STTabTlc leader;
    

//    private String entryValue;
    private List<R> entryValues = new ArrayList<R>();
    private String anchorValue;
    private String number = "";
    private int entryLevel = -1;

    private boolean hyperlink = false;
    private boolean pageNumber = true;
    
    /**
     * If this ToC entry has a paragraph number, we'll need to
     * allow for that in our tabs definition
     */
    private boolean isNumbered = false;

    private P entryP; // be careful; this is only set very late in the process
    private Text pageNumberText;
    
    ObjectFactory wmlObjectFactory = new ObjectFactory();

//    public void setEntryValue(String entryValue) {
//        this.entryValue = entryValue;
//    }
    public List<R> getEntryValue() {
        return entryValues;
    }
    
    public void setAnchorValue(String anchorValue) {
        this.anchorValue = anchorValue;
    }

    public String getAnchorValue() {
        return anchorValue;
    }

    public void makeHyperlink(boolean hyperlink) {
        this.hyperlink = hyperlink;
    }

    public void addPageNumber(boolean pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isPageNumber() {
        return pageNumber;
    }

    public int getEntryLevel() {
        return entryLevel;
    }

    public void setEntryLevel(int entryLevel) {
        this.entryLevel = entryLevel;
    }

    public P getEntryParagraph(TocStyles tocStyles){
        if(entryP == null){
            entryP = generateTocEntry(tocStyles);
        }

        return entryP;
    }
    
    public Text getEntryPageNumberText(){
    	
        if(pageNumberText == null && entryP != null){
        	// Get the last empty w:t 
            List<Object> texts = TocHelper.getAllElementsFromObject(entryP, Text.class);
            Text t;
            for(Object o: texts){
                t = (Text)o;
                if(t.getValue().isEmpty()){
                    pageNumberText = t;
                }
            }
        }

        return pageNumberText;
    }

    private P generateTocEntry(TocStyles tocStyles){
        // Create object for p
        P p3 = wmlObjectFactory.createP(); 
        p3.setPPr(generateTocEntryPPr(tocStyles));
        if(hyperlink){
            p3.getContent().add(generateTocEntryHyperlink());
        } else {
            p3.getContent().addAll(generateTocEntryContent());
        }

        return p3;
    }
    
    private Map<String, Ind> styleIndent = new HashMap<String, Ind>();
    private Ind getInd(String styleId) {
    	
    	if (styleId==null) return null;
    	
    	Ind ind = styleIndent.get(styleId);
    	if (ind == null) {
    		PPr ppr = propertyResolver.getEffectivePPr(styleId);
    		if (ppr==null) {
    			return null;
    		} else {
    			ind = ppr.getInd();
    			styleIndent.put(styleId, ind);
    		}    		
    	} 
		return ind;
    	
    }
    

    private PPr generateTocEntryPPr(TocStyles tocStyles){

        String styleId = tocStyles.getStyleIdForName(String.format(TocStyles.TOC_STYLE_MASK, entryLevel+1));
    	
        // Create object for pPr
        PPr ppr3 = wmlObjectFactory.createPPr(); 
        // Create object for rPr
        ParaRPr pararpr2 = wmlObjectFactory.createParaRPr(); 
        ppr3.setRPr(pararpr2); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue20 = wmlObjectFactory.createBooleanDefaultTrue(); 
        pararpr2.setNoProof(booleandefaulttrue20); 
        // Create object for tabs
        Tabs tabs2 = wmlObjectFactory.createTabs(); 
        ppr3.setTabs(tabs2);
        if (isNumbered) {
        	
        	// add a simple tab definition, as our first tab
            CTTabStop tabstop = wmlObjectFactory.createCTTabStop(); 
            tabs2.getTab().add(tabstop); 
                tabstop.setVal(org.docx4j.wml.STTabJc.LEFT);
                
            	// Work out how much indentation is required for our tab stop
            	Ind ind = getInd( styleId);
            	
            	// take width of number into account - it could be something like 'Appendix 1'!
        		int numWidth = 110 * numChars; // crude, similar to XsltFOFunctions
        		// .. ok for TNR 12
        		// KNOWN ISSUE: numbering ticking over from 9 to 10, or i to ii to iii
        		// will increase the width of the field.  Need LNE to tell us a numChars value which takes that into account
        		int paddedWidth = 330 + numWidth;         		
            	
            	if (ind!=null
            			&& ind.getLeft()!=null) {
                    tabstop.setPos( BigInteger.valueOf( ind.getLeft().intValue() + paddedWidth) );         	            		            		
            	} else {
                    tabstop.setPos( BigInteger.valueOf( paddedWidth) );         	            		
            	}
        }
        // Create object for tab
        CTTabStop tabstop2 = wmlObjectFactory.createCTTabStop();
        tabs2.getTab().add(tabstop2); 
        tabstop2.setVal(STTabJc.RIGHT);
        tabstop2.setPos(BigInteger.valueOf(writableWidthTwips) );  
        
        tabstop2.setLeader(leader);
        
        // Create object for pStyle
        if (styleId==null) {
        	log.warn("No style found for " + String.format(TocStyles.TOC_STYLE_MASK, entryLevel+1) );
        } else {
            PPrBase.PStyle pprbasepstyle3 = wmlObjectFactory.createPPrBasePStyle(); 
            ppr3.setPStyle(pprbasepstyle3);
            pprbasepstyle3.setVal(styleId);
        }

        return ppr3;
    }

    private JAXBElement<Hyperlink> generateTocEntryHyperlink(){
        // Create object for hyperlink (wrapped in JAXBElement) 
        Hyperlink phyperlink2 = wmlObjectFactory.createPHyperlink(); 
        JAXBElement<Hyperlink> phyperlinkWrapped2 = wmlObjectFactory.createPHyperlink(phyperlink2); 
        phyperlink2.setAnchor(anchorValue); 
        phyperlink2.getContent().addAll(generateTocEntryContent());

        return phyperlinkWrapped2;
    }

    private List<R> generateTocEntryContent(){
    	
//        List<R> rList = new ArrayList<R>();
//        
//        // Create object for r
//        R r13 = wmlObjectFactory.createR();
//        rList.add(r13);
//        // Create object for rPr
//        RPr rpr11 = wmlObjectFactory.createRPr(); 
//        r13.setRPr(rpr11);
//        if(hyperlink){
//            // Create object for rStyle
//            RStyle rstyle2 = wmlObjectFactory.createRStyle(); 
//            rpr11.setRStyle(rstyle2); 
//            rstyle2.setVal(HYPERLINK);
//        }
//        // Create object for noProof
//        BooleanDefaultTrue booleandefaulttrue21 = wmlObjectFactory.createBooleanDefaultTrue(); 
//        rpr11.setNoProof(booleandefaulttrue21);
//        
//        // Create object for t (wrapped in JAXBElement) 
//        Text text6 = wmlObjectFactory.createText(); 
//        JAXBElement<Text> textWrapped6 = wmlObjectFactory.createRT(text6); 
//        r13.getContent().add(textWrapped6); 
//        text6.setValue(entryValue); 
    	
    	List<R> rList = entryValues;
        
        generateTocEntryPageNumber(rList);
        
        return rList;
    } 
        
    private void generateTocEntryPageNumber(List<R> rList){
        
        // Create object for r
        R r14 = wmlObjectFactory.createR();
        rList.add(r14);
        // Create object for rPr
        RPr rpr12 = wmlObjectFactory.createRPr(); 
        r14.setRPr(rpr12); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue22 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr12.setNoProof(booleandefaulttrue22); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue23 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr12.setWebHidden(booleandefaulttrue23); 
        if(pageNumber){
            // Create object for tab (wrapped in JAXBElement) 
            R.Tab rtab2 = wmlObjectFactory.createRTab(); 
            JAXBElement<R.Tab> rtabWrapped2 = wmlObjectFactory.createRTab(rtab2); 
            r14.getContent().add(rtabWrapped2); 
        }
        // Create object for r
        R r15 = wmlObjectFactory.createR();
        rList.add(r15);
        // Create object for rPr
        RPr rpr13 = wmlObjectFactory.createRPr(); 
        r15.setRPr(rpr13); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue24 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr13.setNoProof(booleandefaulttrue24); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue25 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr13.setWebHidden(booleandefaulttrue25); 
        // Create object for fldChar (wrapped in JAXBElement) 
        FldChar fldchar6 = wmlObjectFactory.createFldChar(); 
        JAXBElement<FldChar> fldcharWrapped6 = wmlObjectFactory.createRFldChar(fldchar6); 
        r15.getContent().add( fldcharWrapped6); 
        fldchar6.setFldCharType(STFldCharType.BEGIN);
        // Create object for r
        R r16 = wmlObjectFactory.createR();
        rList.add(r16);
        // Create object for rPr
        RPr rpr14 = wmlObjectFactory.createRPr(); 
        r16.setRPr(rpr14); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue26 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr14.setNoProof(booleandefaulttrue26); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue27 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr14.setWebHidden(booleandefaulttrue27); 
        // Create object for instrText (wrapped in JAXBElement) 
        Text text7 = wmlObjectFactory.createText(); 
        JAXBElement<Text> textWrapped7 = wmlObjectFactory.createRInstrText(text7); 
        r16.getContent().add(textWrapped7); 
        text7.setValue(String.format(PAGEREF_MASK, anchorValue)); 
        text7.setSpace(PRESERVE); 
        // Create object for r
        R r17 = wmlObjectFactory.createR();
        rList.add(r17);
        // Create object for rPr
        RPr rpr15 = wmlObjectFactory.createRPr(); 
        r17.setRPr(rpr15); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue28 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr15.setNoProof(booleandefaulttrue28); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue29 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr15.setWebHidden(booleandefaulttrue29); 
        // Create object for r
        R r18 = wmlObjectFactory.createR();
        rList.add(r18);
        // Create object for rPr
        RPr rpr16 = wmlObjectFactory.createRPr(); 
        r18.setRPr(rpr16); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue30 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr16.setNoProof(booleandefaulttrue30); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue31 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr16.setWebHidden(booleandefaulttrue31); 
        // Create object for fldChar (wrapped in JAXBElement) 
        FldChar fldchar7 = wmlObjectFactory.createFldChar(); 
        JAXBElement<FldChar> fldcharWrapped7 = wmlObjectFactory.createRFldChar(fldchar7); 
        r18.getContent().add(fldcharWrapped7); 
        fldchar7.setFldCharType(STFldCharType.SEPARATE);
        // Create object for r
        R r19 = wmlObjectFactory.createR();
        rList.add(r19);
        // Create object for rPr
        RPr rpr17 = wmlObjectFactory.createRPr(); 
        r19.setRPr(rpr17); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue32 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr17.setNoProof(booleandefaulttrue32); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue33 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr17.setWebHidden(booleandefaulttrue33); 
        // Create object for t (wrapped in JAXBElement) 
        Text text8 = wmlObjectFactory.createText(); 
        JAXBElement<Text> textWrapped8 = wmlObjectFactory.createRT(text8); 
        r19.getContent().add(textWrapped8);
        text8.setValue(number);
        // Create object for r
        R r20 = wmlObjectFactory.createR();
        rList.add(r20);
        // Create object for rPr
        RPr rpr18 = wmlObjectFactory.createRPr(); 
        r20.setRPr(rpr18); 
        // Create object for noProof
        BooleanDefaultTrue booleandefaulttrue34 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr18.setNoProof(booleandefaulttrue34); 
        // Create object for webHidden
        BooleanDefaultTrue booleandefaulttrue35 = wmlObjectFactory.createBooleanDefaultTrue(); 
        rpr18.setWebHidden(booleandefaulttrue35); 
        // Create object for fldChar (wrapped in JAXBElement) 
        FldChar fldchar8 = wmlObjectFactory.createFldChar(); 
        JAXBElement<FldChar> fldcharWrapped8 = wmlObjectFactory.createRFldChar(fldchar8); 
        r20.getContent().add(fldcharWrapped8); 
        fldchar8.setFldCharType(STFldCharType.END);

    }
    
//    public void setEntryValue(P sourceP) {
//        
//        StringWriter sw = new StringWriter();
//        try {
//			TextUtils.extractText(sourceP, sw);
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//        String entryValue = sw.toString();
//        this.setEntryValue(entryValue);
//    }

    // since 3.1.0.5
    public void setEntryValue(P sourceP) {
    	
    	/*
			Certain Run formatting on entries is re-used, including:
			•	font face
			•	italic
			•	text highlight
			•	hidden (honoured)
			•	small caps
			but not
			•	font size
			•	font color
			•	underline
    	 */
    	
    	// Step 1: create a clone of the P
    	P clonedP = (P)XmlUtils.deepCopy(sourceP);
    	
    	// Step 2: make a List<R>, comprising the w:r/w:t contents,
    	// with styles resolved
        List<Object> runsFound = TocHelper.getAllElementsFromObject(clonedP, R.class);
        
        R lastRun = null;
        for( Object o : runsFound) {
        	
        	R r = (R)o;
        	List<Object> textsFound = TocHelper.getAllElementsFromObject(r, Text.class);
        	if (textsFound.size()>0) {
        		
        		R newR = new R();
        		
        		if (r.getRPr()==null) {
					newR.setRPr(wmlObjectFactory.createRPr());
        		} else {
            		// Resolve the formatting
//        			log.debug("\n\r getEffectiveRPr " + ((Text)textsFound.get(0)).getValue() );
					newR.setRPr(getEffectiveRPr(r.getRPr())); 
//					log.debug(XmlUtils.marshaltoString(newR.getRPr()));
					
			    	// Step 3: strip/filter unwanted run formatting
					nullify(newR.getRPr());
					
					if (newR.getRPr()==null) {
						newR.setRPr(wmlObjectFactory.createRPr());
					}
        		}

        		// apply hyperlink if appropriate
				if (hyperlink) {
					RStyle rstyle = wmlObjectFactory.createRStyle();
					newR.getRPr().setRStyle(rstyle);
					rstyle.setVal(HYPERLINK);
				}
				
        		newR.getContent().addAll(textsFound);
        		entryValues.add(newR);
        		lastRun = newR;
        	}
        	
        }
        
        // drop any trailing space off last run (Word does this)
        if (lastRun!=null) {
        	
        	int size = lastRun.getContent().size();
        	if (size>0) {
                Text lastText = (Text)lastRun.getContent().get(size-1);  
                String val = lastText.getValue();
                if (val!=null) {
                	lastText.setValue(StringUtils.stripEnd(val, null));
                }
        	}        	
        }

        // drop leading space off the first run
        R firstRun = null;
        if (runsFound.size()>0) {
        	firstRun = (R)runsFound.get(0);
        	List<Object> textsFound = TocHelper.getAllElementsFromObject(firstRun, Text.class);
        	int size = textsFound.size();
        	if (size>0) {
                Text firstText = (Text)textsFound.get(0);  
                String val = firstText.getValue();
                if (val!=null) {
                	firstText.setValue(StringUtils.stripStart(val, null));
                }
        	}        	
        }
        
    	
    }
        
    /**
     * Get the rPr to apply.  This intentionally ignores pStyle and doc defaults.
     * The TOC entry will ultimately use TOCn style; we're not interested in the
     * pStyle of this paragraph.
     * 
     * @param expressRPr
     * @return
     */
    private RPr getEffectiveRPr(RPr expressRPr) {
    	
		RPr effectiveRPr = Context.getWmlObjectFactory().createRPr();

		// Apply run style, ignoring doc default settings
		RPr resolvedRPr = null;
		String runStyleId;
		if (expressRPr != null && expressRPr.getRStyle() != null ) {
			runStyleId = expressRPr.getRStyle().getVal();			
			resolvedRPr = propertyResolver.getEffectiveRPr(runStyleId, false, false); 
			StyleUtil.apply(resolvedRPr, effectiveRPr);
		}
				
		//	Apply direct formatting (run properties not from styles).		
		if (propertyResolver.hasDirectRPrFormatting(expressRPr) ) {			
			StyleUtil.apply(expressRPr, effectiveRPr);
		} 
		return effectiveRPr;
    }
    
    private void nullify(RPr destination) {
    	
    	if (destination==null) return;
    	
    	// The following ARE used/preserved in the ToC
			//	setRFonts(null);
    		//	setB(null);
    		//	setBCs(null);
    		//	setICs(null);
			//	setI(null);
			//	setVanish(null);
			//	setCaps(null);
			//	setSmallCaps(null);
			//	setHighlight(null);

		// The following are definitely dropped
		destination.setRStyle(null);
		destination.setSz(null);
		destination.setSzCs(null);
		destination.setColor(null);
		destination.setU(null);

		// The following are unknown, so assume dropped
		destination.setLang(null);
		destination.setStrike(null);
		destination.setDstrike(null);
		destination.setOutline(null);
		destination.setShadow(null);
		destination.setEmboss(null);
		destination.setImprint(null);
		destination.setSnapToGrid(null);
		destination.setSpacing(null);
		destination.setW(null);
		destination.setKern(null);
		destination.setPosition(null);
		destination.setEffect(null);
		destination.setBdr(null);
		destination.setShd(null);
		destination.setVertAlign(null);
		destination.setRtl(null);
		destination.setCs(null);
		destination.setEm(null);
		destination.setSpecVanish(null);
		destination.setOMath(null);
    	
    }
    
    /**
     * Number this entry, if necessary
     * @param numberTriple
     */
    public void numberEntry(ResultTriple numberTriple) {
                
        if (numberTriple!=null && numberTriple.getNumString()!=null) {
        	
        	isNumbered = true; //signal that we need to define the tab setting
        	// it depends on the width of the number
        	if (numberTriple.getBullet()!=null ) {
        		numChars=1;		        		
        	} else if (numberTriple.getNumString()==null) {
        		numChars=0;		        		
        	} else {
        		numChars = numberTriple.getNumString().length();						
        	}
        	
          R r = wmlObjectFactory.createR();
          RPr rpr = wmlObjectFactory.createRPr(); 
          r.setRPr(rpr);
          if(hyperlink){
              // Create object for rStyle
              RStyle rstyle2 = wmlObjectFactory.createRStyle(); 
              rpr.setRStyle(rstyle2); 
              rstyle2.setVal(HYPERLINK);
          }
          
          // Create object for t (wrapped in JAXBElement) 
          Text t = wmlObjectFactory.createText(); 
          JAXBElement<Text> textWrapped6 = wmlObjectFactory.createRT(t); 
          r.getContent().add(textWrapped6); 
          t.setSpace("preserve");
          
          this.entryValues.add(0, r);
          
          /* Add a tab, but in a new run (since the tab isn't to be underlined),
           * unless w:lvl has
      		      <w:suff w:val="space"/>
      		or
      		      <w:suff w:val="nothing"/>
           */
          if (numberTriple.getLvl().getSuff()==null) {
              t.setValue(numberTriple.getNumString() ); 
	          this.entryValues.add(1, tabAfterPNumber());
          } else if ( numberTriple.getLvl().getSuff().getVal().equals("space")) {
              t.setValue(numberTriple.getNumString() + " ");         	  
          } else if ( numberTriple.getLvl().getSuff().getVal().equals("nothing")) {
              t.setValue(numberTriple.getNumString());         	          	  
          } else {
        	  // for anything else, ignore, and use a tab (mimicking Word 2010)
              t.setValue(numberTriple.getNumString() ); 
	          this.entryValues.add(1, tabAfterPNumber());
          }
          
        }
        
    }
    
    /** Add a tab, but in a new run (since the tab isn't to be underlined),
     * unless w:lvl has
		      <w:suff w:val="space"/>
		or
		      <w:suff w:val="nothing"/>
     */
    private R tabAfterPNumber() {
            
        R r = wmlObjectFactory.createR();
        
        R.Tab rtab = wmlObjectFactory.createRTab(); 
        JAXBElement<R.Tab> rtabWrapped = wmlObjectFactory.createRTab(rtab); 
        r.getContent().add(rtabWrapped);  
    	return r;
    }
    
	int numChars=1;	        		

}
