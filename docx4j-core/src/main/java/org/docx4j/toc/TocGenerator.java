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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.finders.SectPrFindFirst;
import org.docx4j.model.bookmarks.BookmarksIntegrity;
import org.docx4j.model.bookmarks.BookmarksIntegrity.BookmarksStatus;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.services.client.ConversionException;
import org.docx4j.services.client.Converter;
import org.docx4j.services.client.ConverterHttp;
import org.docx4j.services.client.Format;
import org.docx4j.toc.switches.SwitchProcessor;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class creates and populates a table of contents.
 * 
 * If you want page numbers, getting this right (without opening 
 * the docx in Word to do so), is a challenge.
 * 
 * This class supports generating the page numbers using 
 * export-fo (or the no-longer-available Converter).
 * 
 * If you are using docx4j-export-documents4j-local|remote,
 * you can use that for ToC generation.  
 *  
 * @author jharrop
 *
 */
public class TocGenerator {
	
	private static Logger log = LoggerFactory.getLogger(TocGenerator.class);	

	private WordprocessingMLPackage wordMLPackage;
	
	public WordprocessingMLPackage getWordMLPackage() {
		return wordMLPackage;
	}

	
//	boolean skipPageNumbering;
//	
//	public void setSkipPageNumbering(boolean skipPageNumbering) {
//		this.skipPageNumbering = skipPageNumbering;
//	}

	/**
	 * From sectPr, we can get PageDimensions, which is used for
	 * calculating position of the right aligned tab for page numbers.
	 */
	private SectPr sectPr;
	
	private TocGenerator() {}

//	/**
//	 * @param docxFile
//	 * @throws TocException
//	 * @since 3.3.0
//	 */
//	public TocGenerator(File docxFile) throws TocException {
//		
//		// The problem with this, is how do you get your result back out?
//		
//		try {
//			wordMLPackage = WordprocessingMLPackage.load(docxFile);
//		} catch (Docx4JException e) {
//			throw new TocException(e.getMessage(),e);
//		}    	
//		this.tocStyles = getTocStyles(wordMLPackage.getMainDocumentPart());
//	}
	
	public TocGenerator(WordprocessingMLPackage wordMLPackage) throws TocException {
		
		this.wordMLPackage = wordMLPackage;
		this.tocStyles = TocStyles.getTocStyles(wordMLPackage.getMainDocumentPart());
	}
	
	private TocStyles tocStyles = null;	

    /**
     * Generate Table of Contents using default TOC instruction, adding it at the beginning of the document
     * 
     * To alter the ToC heading, use Toc.setTocHeadingText
     * 
     * @param body of document
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @return SdtBlock control
     */
    @Deprecated
    public static SdtBlock generateToc(WordprocessingMLPackage wordMLPackage, boolean skipPageNumbering) throws TocException {
        return (new TocGenerator(wordMLPackage)).generateToc( 0, TocHelper.DEFAULT_TOC_INSTRUCTION, skipPageNumbering);
    }
    /**
     * Generate Table of Contents using provided TOC instruction, adding at the given index in the body of document
     * 
     * It is an error if a ToC content control is already present; delete it or use updateToc.
     * 
     * To alter the ToC heading, use Toc.setTocHeadingText
     * 
     * @param body
     * @param index
     * @param instruction
     * @return SdtBlock control
     */
    @Deprecated
    public static SdtBlock generateToc(WordprocessingMLPackage wordMLPackage, int index, String instruction, boolean skipPageNumbering) throws TocException {
    	return (new TocGenerator(wordMLPackage)).generateToc( wordMLPackage,  index,  instruction,  skipPageNumbering);
    }
	
    /**
     * Generate Table of Contents using default TOC instruction, adding it at the beginning of the document
     * 
     * To alter the ToC heading, use Toc.setTocHeadingText
     * 
     * @param body of document
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @return SdtBlock control
     */
    public SdtBlock generateToc(boolean skipPageNumbering) throws TocException {
        return generateToc( 0, TocHelper.DEFAULT_TOC_INSTRUCTION, skipPageNumbering);
    }

    
    /**
     * Generate Table of Contents using provided TOC instruction, adding at the given index in the body of document,
     * using dot leader for page numbering (if any).
     * 
     * It is an error if a ToC content control is already present; delete it or use updateToc.
     * 
     * To alter the ToC heading, use Toc.setTocHeadingText
     * 
     * @param index
     * @param instruction
     * @param skipPageNumbering
     * @return SdtBlock control
     */
    public SdtBlock generateToc(int index, String instruction, boolean skipPageNumbering) throws TocException {
    	
        return generateToc(  index,  instruction, TocHelper.DEFAULT_TAB_LEADER, skipPageNumbering);
    }

    /**
     * Generate Table of Contents using provided TOC instruction, and specified leader (eg dots) before
     * page number, adding at the given index in the body of document
     * 
     * It is an error if a ToC content control is already present; delete it or use updateToc.
     * 
     * To alter the ToC heading, use Toc.setTocHeadingText
     * 
     * @param index
     * @param instruction
     * @param leader
     * @param skipPageNumbering
     * @return SdtBlock control
     * @since 3.3.2
     */
    public SdtBlock generateToc(int index, String instruction, STTabTlc leader, boolean skipPageNumbering) throws TocException {
    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        Document wmlDocumentEl = (Document)documentPart.getJaxbElement();
        Body body =  wmlDocumentEl.getBody();

    	TocFinder finder = new TocFinder();
		new TraversalUtil(body.getContent(), finder);
        SdtBlock sdt = finder.tocSDT;  
        if(sdt != null){
        	// OK
        	throw new TocException("ToC already present; use updateToc instead");
        }
        
        // Set sectPr, looking for it from index onwards
        List<Object> sublist = body.getContent().subList(index, body.getContent().size()); // end is exclusive       
    	SectPrFindFirst sf = new SectPrFindFirst();
		new TraversalUtil(sublist, sf);
        sectPr = sf.firstSectPr;
        
        sdt = TocSdtUtils.createSdt();
        body.getContent().add(index, sdt);
        return generateToc(  sdt,  instruction, leader, skipPageNumbering);
    }
    
    /**
     * Generate Table of Contents in the given sdt with generated heading, and given TOC instruction.
     * See http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/TOC.html for TOC instruction.
     * 
     * To alter the ToC heading, use Toc.setTocHeadingText
     * 
     * @param body
     * @param sdt - assumed already attached to the document
     * @param instruction
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @return SdtBlock control
     */
    protected SdtBlock generateToc(SdtBlock sdt, String instruction, STTabTlc leader, boolean skipPageNumbering) throws TocException {
    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        Document wmlDocumentEl = (Document)documentPart.getJaxbElement();
        Body body =  wmlDocumentEl.getBody();
        
        SdtContentBlock sdtContent = (SdtContentBlock)sdt.getSdtContent();
        if(sdtContent == null){
            sdtContent = TocSdtUtils.createSdtContent();
            sdt.setSdtContent(sdtContent);
        }
        
        if (Toc.getTocHeadingText()!=null) {
        
        	TocSdtUtils.addTocHeading(documentPart, tocStyles, sdtContent);        
        }
        
        populateToc(  
        		 sdtContent, 
        		 instruction,  leader, skipPageNumbering);
        
        return sdt;
        
    }


    
    /**
     * Generate Table of Contents in the given sdt with provided tocHeadingP, and given TOC instruction.
     * See http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/TOC.html for TOC instruction.
     * @param body
     * @param sdt
     * @param tocHeadingP  use this P as the ToC heading
     * @param instruction
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @return SdtBlock control
     */
    protected  SdtBlock generateToc(SdtBlock sdt, List<P> tocHeadingP, String instruction, STTabTlc leader, boolean skipPageNumbering) throws TocException {
    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        Document wmlDocumentEl = (Document)documentPart.getJaxbElement();
        Body body =  wmlDocumentEl.getBody();

        SdtContentBlock sdtContent = (SdtContentBlock)sdt.getSdtContent();
        if(sdtContent == null){
            sdtContent = TocSdtUtils.createSdtContent();
            sdt.setSdtContent(sdtContent);
        }
                
        // 1. Reuse existing Toc heading 
        if (tocHeadingP==null
        		|| tocHeadingP.size()==0) {
        	log.warn("No ToC header paragraph provided!");        	
        } else {
        	sdtContent.getContent().addAll(tocHeadingP);
        }
        
        populateToc( 
        		 sdtContent, 
        		 instruction, leader, skipPageNumbering);
        
        return sdt;

    }
    
	/**
	 * Provide a way to set the starting bookmark ID number.
	 * 
	 * For efficiency, user code needs to pass this value through.
	 * 
	 * If it isn't, the value will be calculated (less efficient).
	 *   
	 * @param bookmarkId
	 * @since 3.3.0
	 */
	public void setStartingIdForNewBookmarks(AtomicInteger bookmarkId) {
//		System.out.println("setStartingIdForNewBookmarks=" + bookmarkId.get());
		this.bookmarkId = bookmarkId;
		
	}	
	AtomicInteger bookmarkId = null;
    
    
    /**
     * Invoked after Toc header has been added (either created from scratch, or, in update case,
     * copied from existing ToC)
     */
    private  void populateToc(
    		SdtContentBlock sdtContent, 
    		String instruction, STTabTlc leader,
    		boolean skipPageNumbering) throws TocException {
    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        Document wmlDocumentEl = (Document)documentPart.getJaxbElement();
        Body body =  wmlDocumentEl.getBody();    	
        
        // Generate new TOC
        Toc toc = new Toc(instruction); // will throw an exception if it can't be parsed     
        
        // Process Toc switches
        
        // .. we need page dimensions for right aligned tab (for page numbers) 
        // It is reasonable to require there to be a sectPr containing the necessary info
        if (sectPr==null) {
        	log.debug("sectPr not set by tocFinder");  // will happen when we're adding (as opposed to updating) ToC
        	// it doesn't fall back to the body level one
        	sectPr = this.wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
        	if  (sectPr==null) {
        		throw new TocException("No sectPr following ToC");
        	}
        }
        PageDimensions pageDimensions = new PageDimensions(sectPr);
    	try {
    		pageDimensions.getWritableWidthTwips();
    	} catch (RuntimeException e) {
    		throw new TocException("margins or page width not defined in \n" + XmlUtils.marshaltoString(sectPr));        		
    	}
        
        List<TocEntry> tocEntries = new ArrayList<TocEntry>();

        @SuppressWarnings("unchecked")
        List<P> pList = (List<P>)(List<?>) TocHelper.getAllElementsFromObject(body, P.class);
        
        // Work out paragraph numbering
        Map<P, ResultTriple> pNumbersMap = numberParagraphs( pList);
        
        SwitchProcessor sp = new SwitchProcessor(pageDimensions, leader);
        sp.setStartingIdForNewBookmarks(bookmarkId);
        tocEntries.addAll(
        		sp.processSwitches(wordMLPackage, pList, toc.getSwitches(), pNumbersMap));
        
        
        if (tocEntries.size()==0) {
        	log.warn("No ToC entries generated!");
        	P p = new P();
        	p.getContent().addAll(toc.getTocInstruction());
            sdtContent.getContent().add(p);
            sdtContent.getContent().add(TocSdtUtils.getLastParagraph());
        } else {
	        // Prep: merge instruction into first tocEntry (avoiding an unwanted additional paragraph)
	        P firstEntry = tocEntries.get(0).getEntryParagraph(tocStyles);
	        firstEntry.getContent().addAll(0, toc.getTocInstruction());
	        
	        // Add Toc Entries paragraphs
	        for(TocEntry entry: tocEntries){
	            sdtContent.getContent().add(entry.getEntryParagraph(tocStyles));
	        }

	        // Add last toc paragraph
	        sdtContent.getContent().add(TocSdtUtils.getLastParagraph());
	        
	        // Add page numbers
	        if(!skipPageNumbering && sp.pageNumbers() ) {
	            Map<String, Integer> pageNumbersMap = getPageNumbersMap();
	            Integer pageNumber;
	            for(TocEntry entry: tocEntries){
	                pageNumber = pageNumbersMap.get(entry.getAnchorValue());
	                if(pageNumber == null){
	                	log.debug("null page number for key " + entry.getAnchorValue() );
	                } else {
	                    if(entry.isPageNumber()){
	                        entry.getEntryPageNumberText().setValue(Integer.toString((pageNumber)));
	                    }
	                }
	            }
	        }
        }

    }
    
    private  Map<P, ResultTriple> numberParagraphs(List<P> pList) {
    	
    	org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart numberingPart =
    			wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart();
        	
    	Map<P, ResultTriple> pNumbersMap = new HashMap<P, ResultTriple>(); 
    	if (numberingPart==null) {
    		return pNumbersMap;
    	}

    	numberingPart.getEmulator(true); // reset counters
    	
    	//PropertyResolver propertyResolver =  wordMLPackage.getMainDocumentPart().getPropertyResolver();   
    	
    	
    	for (P p : pList) {
    		
    		if (p.getPPr()!=null) {
    			
    			ResultTriple triple = Emulator.getNumber(wordMLPackage, p.getPPr());
    			pNumbersMap.put(p, triple);
    		}
    	}
    	return pNumbersMap;
    }

    /**
     * Update existing TOC in the document with TOC generated by generateToc() method, reusing the existing ToC heading paragraph.
     * @param body
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @return SdtBlock control, or null if no TOC was found
     */
    public  SdtBlock updateToc( boolean skipPageNumbering) throws TocException{
    	
    	return updateToc(  skipPageNumbering,  true);
    }

    /**
     * Update existing TOC in the document with TOC generated by generateToc() method, 
     * reusing the existing ToC heading paragraph.
     * Page numbers are updated.
     * @return SdtBlock control, or null if no TOC was found
     * @since 6.1.0
     */
    public  SdtBlock updateToc( ) throws TocException{
    	return updateToc(  false,  true);
    }
    
    @Deprecated
    public static SdtBlock updateToc(WordprocessingMLPackage wordMLPackage, boolean skipPageNumbering) throws TocException{
    	
    	return (new TocGenerator(wordMLPackage)).updateToc( skipPageNumbering);
    }
    
    

    /**
     * Update existing TOC in the document with TOC generated by generateToc() method. Attempt to identify/re-use existing dot leader.
     * @param body
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @param reuseExistingToCHeadingP  
     * @return SdtBlock control, or null if no TOC was found
     */
    public  SdtBlock updateToc( boolean skipPageNumbering, boolean reuseExistingToCHeadingP) throws TocException{
    	
    	return updateToc(  skipPageNumbering,  reuseExistingToCHeadingP,  null);

    }
    
    /**
     * Update existing TOC in the document with TOC generated by generateToc() method, using specified tab leader.
     * @param body
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @param reuseExistingToCHeadingP  
     * @param leader
     * @return SdtBlock control, or null if no TOC was found
     * @since 3.3.2
     */
    public  SdtBlock updateToc( boolean skipPageNumbering, boolean reuseExistingToCHeadingP, STTabTlc leader) throws TocException{
    	
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        Document wmlDocumentEl = (Document)documentPart.getJaxbElement();
        Body body =  wmlDocumentEl.getBody();

    	TocFinder finder = new TocFinder();
		new TraversalUtil(body.getContent(), finder);
        
        SdtBlock sdt = finder.tocSDT;
        sectPr = finder.sectPr;
        
        if(sdt == null){
            throw new TocException("No ToC content control found");
        }

        String instruction = finder.tocInstruction;
        if(instruction.isEmpty()){
            throw new TocException("TOC instruction text missing");
        }
        
        if (leader==null) {
        	leader = getExistingTabLeader(sdt.getSdtContent().getContent());
        }

        //remove old TOC
        sdt.getSdtContent().getContent().clear();

        if (finder.tocHeadingP==null 
        		|| finder.tocHeadingP.size()==0 ) {
        	log.warn("No ToC header paragraph found; generating..");
            generateToc( sdt, instruction, leader, skipPageNumbering);
            
        } else if ( !reuseExistingToCHeadingP ) {
            	log.debug("Generating ToC header paragraph ..");
                generateToc( sdt, instruction, leader, skipPageNumbering);
                
        } else {
        	log.debug("Reusing existing ToC header paragraph");
            generateToc( sdt, finder.tocHeadingP, instruction, leader, skipPageNumbering);        	
        }

        return sdt;
    }
    
    private STTabTlc getExistingTabLeader(List<Object> contents) {
    	
    	STTabTlc leader = null;
    	
    	/*  The simple-minded approach we take here is that we try to
    	 *  look in the second paragraph:
    	 *  
	      <w:sdtContent>
	      
	        <w:p>
	          <w:pPr>
	            <w:pStyle w:val="TOCHeading"/>
	          </w:pPr>
	          <w:r>
	            <w:t>Contents</w:t>
	          </w:r>
	        </w:p>
	        
	        <w:p>
	          <w:pPr>
	            <w:pStyle w:val="TOC1"/>
	            <w:tabs>
	              <w:tab w:val="right" w:leader="dot" w:pos="9629"/>
	            </w:tabs>
                	 */
    	
    	if (contents.size()>1) {
    		Object o = contents.get(1);
    		if (o instanceof P) {
    			P sampleP = (P)o;
    			if (sampleP.getPPr()!=null
    					&& sampleP.getPPr()!=null
    					&& sampleP.getPPr().getTabs()!=null) {
    				
    				Tabs sampleTabs = sampleP.getPPr().getTabs();
    				if (sampleTabs.getTab().size()>0) {
    					CTTabStop tab = sampleTabs.getTab().get(0);
    					leader = tab.getLeader();
    					if (leader==null) {
    						leader = STTabTlc.NONE;
        		        	log.debug("Reusing existing leader (in this case, STTabTlc.NONE)");
    					} else {
        		        	log.debug("Reusing existing leader");    						
    					}
    				}
    				
    			}
    		}
    	}
    	
    	// if can't work it out, log that then return dots
    	if (leader == null) {
    		log.info("Couldn't figure out leader from existing ToC; defaulting to dots");
    		leader = TocHelper.DEFAULT_TAB_LEADER;
    	}
    	return leader;
    }

    /**
     * Update existing TOC in the document with TOC generated by generateToc() method.
     * @param body
     * @param skipPageNumbering don't generate page numbers (useful for HTML output, or speed, or as a fallback in case of issues)
     * @param reuseExistingToCHeadingP  
     * @return SdtBlock control, or null if no TOC was founds
     */
    @Deprecated
    public  SdtBlock updateToc(WordprocessingMLPackage wordMLPackage, boolean skipPageNumbering, boolean reuseExistingToCHeadingP) throws TocException{
    	return (new TocGenerator(wordMLPackage)).updateToc(  skipPageNumbering,  reuseExistingToCHeadingP);
    }
    
    /**
     * Whether to use XSLT to generate page numbers in the TOC via FOP.  Of the three methods available,
     * XSLT (the default) is 2nd most featureful, but slowest.  From v3.3.0, DocumentServices is 
     * recommended as quickest and most accurate, and will be used unless docx4j-export-fo is present.
     * 
     * @param useXSLT
     */
    public void pageNumbersViaXSLT(boolean useXSLT) {
    	foViaXSLT = useXSLT;
    }
    
    private boolean foViaXSLT = true;
    

	/**
     * Calculate page numbers
     * 
     * @return
     * @throws TocException
     */
    private Map<String, Integer> getPageNumbersMap() throws TocException {
    	
    	// @since 6.1, check bookmarks are ok first
    	// what to do if not ok?
    	// - default behaviour is to fail
    	// - but can be configured to remediate:
    	boolean remediate = Docx4jProperties.getProperty("docx4j.toc.BookmarksIntegrity.remediate", false);
    	
    	// 
		BookmarksIntegrity bm = new BookmarksIntegrity();
		StringWriter sw = new StringWriter();
		bm.setWriter(sw);
		BookmarksStatus result = null;
		try {
			// Checks are performed on all bookmarks, not just those with
			// a name of the form "_Toc*".  We don't check for missing _Toc bookmarks.
			result = bm.check(wordMLPackage.getMainDocumentPart(), remediate);
		} catch (Exception e) { /* won't happen */}
    	if (result==BookmarksStatus.BROKEN) {
    		throw new TocException("Encountered broken bookmarks; not configured to remediate. \n" + sw.toString());
    	}
    	
    	if (Docx4J.pdfViaFO()) {
    		return getPageNumbersMapViaFOP();
    	} else {
    		try {
    			return getPageNumbersMapViaService();
    		} catch (TocException e) {
    			throw new TocException("Page number service not available; try using docx4j-export-documents4j-local|remote or docx4j-export-fo");
    		}
    	}
    }

    private Map<String, Integer> getPageNumbersMapViaService() throws TocException {
    	
    	log.debug("getPageNumbersMapViaService() starting..");
    	
    	// We always have to save the pkg, since we always update the entire table
    	// (which might alter the document length)
    	
//    	File tmpDocxFile = null;
		ByteArrayOutputStream tmpDocxFile = new ByteArrayOutputStream(); 
		try {
//			tmpDocxFile = TempDir.getTemporaryFile();
			Docx4J.save(wordMLPackage, tmpDocxFile, Docx4J.FLAG_SAVE_ZIP_FILE);
		} catch (Exception e) {
			throw new TocException("Error saving pkg as tmp file; " + e.getMessage(),e);
		}  
		
		// Not working?  Check your input
//		try {
//			File save_ToCEntries = new File( System.getProperty("java.io.tmpdir") 
//					+ "/foo_tocEntries.docx"); 
//			Docx4J.save(wordMLPackage, save_ToCEntries, Docx4J.FLAG_SAVE_ZIP_FILE);
//			log.debug("Saved: " + save_ToCEntries);
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
    	    	
    	// 
		String documentServicesEndpoint = Docx4jProperties.getProperty("com.plutext.converter.URL", 
				"http://localhost:9016/v1/00000000-0000-0000-0000-000000000000/convert");
		
		Converter converter = new ConverterHttp(documentServicesEndpoint); 

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			
		try {
			converter.convert(tmpDocxFile.toByteArray(), Format.DOCX, Format.TOC, baos);
			log.debug("page numbers successfully received from service");
		} catch (ConversionException e) {
			
			if (e.getResponse()!=null) {

				throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n HTTP response: " 
						+ e.getResponse().getStatusLine().getStatusCode()
						+ " " + e.getResponse().getStatusLine().getReasonPhrase() ,e);
				
			} else if (e.getMessage()==null){			
				throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n",e);
			} else {
				throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n" + e.getMessage(),e);				
			}
			
		} catch (Exception e) {
			throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n" + e.getMessage(),e);
		}
		

		/* DocumentServices 2.0-x
		 * {"bookmarks":{"_Toc299924107":"68","_Toc318272803":"1"}}
		 */
		
		byte[] json = baos.toByteArray();
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		ObjectMapper m = new ObjectMapper();
		
		JsonNode rootNode;
		try {
			rootNode = m.readTree(json);
		} catch (Exception e) {
			throw new TocException("Error reading toc json; \n" + json + "\n"+ e.getMessage(),e);
		}
		JsonNode bookmarksNode = rootNode.path("bookmarks");
		
		Iterator<Map.Entry<String, JsonNode>> bookmarksValueObj = bookmarksNode.fields();
		while (bookmarksValueObj.hasNext()) {
			Map.Entry<String, JsonNode> entry = bookmarksValueObj.next();
			if (entry.getValue()==null) {
				log.warn("Null page number computed for bookmark " + entry.getKey());
			}
			map.put(entry.getKey(), new Integer(entry.getValue().asInt()));
		}		
		
		log.debug("page number map size " + map.size());
			
		return map;

    }
    
    /**
     * Invoke FOP to calculate page numbers
     * 
     * @return
     * @throws TocException
     */
    private Map<String, Integer> getPageNumbersMapViaFOP() throws TocException {
    	
    	log.debug("getPageNumbersMapViaFOP() starting..");
    	
        long start = System.currentTimeMillis();
    	
        FOSettings foSettings = Docx4J.createFOSettings();
        foSettings.setOpcPackage(wordMLPackage);
        String MIME_FOP_AREA_TREE   = "application/X-fop-areatree"; // org.apache.fop.apps.MimeConstants
        foSettings.setApacheFopMime(MIME_FOP_AREA_TREE);
        
        foSettings.getFeatures().add(ConversionFeatures.PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM); // in 3.0.1, this is off by default
        
        if (log.isDebugEnabled()) {
        	foSettings.setFoDumpFile(new java.io.File(System.getProperty("user.dir") + "/Toc.fo"));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
        	if (foViaXSLT) {
        		Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        	} else {
        		Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);        		
        	}
            
            long end = System.currentTimeMillis();
            float timing = (end-start)/1000;
        	if (foViaXSLT) {
                log.debug("Time taken (AT via XSLT): " + Math.round(timing) + " sec");
        	} else {
                log.debug("Time taken (AT via non XSLT): " + Math.round(timing) + " sec");
        	}
            
//            start = System.currentTimeMillis();
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            TocPageNumbersHandler tpnh = new TocPageNumbersHandler();
			saxParser.parse(is, tpnh);
			
			// Negligible 
//            end = System.currentTimeMillis();
//            timing = (end-start)/1000;
//            log.debug("Time taken (parse step): " + Math.round(timing) + " sec");
			
	        return tpnh.getPageNumbers();
	        
		} catch (Exception e) {
			throw new TocException(e.getMessage(),e);
		}

    }
    
}
