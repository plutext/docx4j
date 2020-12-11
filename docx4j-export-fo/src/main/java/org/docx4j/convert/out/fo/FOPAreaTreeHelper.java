package org.docx4j.convert.out.fo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.fop.apps.MimeConstants;
import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.finders.SectPrFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;
import org.plutext.jaxb.xslfo.LayoutMasterSet;
import org.plutext.jaxb.xslfo.SimplePageMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Helper to correctly size header/footer areas in PDF output.
 * 
 * @author jharrop
 * @since 3.1.0
 *
 */
public class FOPAreaTreeHelper {
	
	protected static Logger log = LoggerFactory.getLogger(FOPAreaTreeHelper.class);
	

    /**
     * Since we start with headers/footers which each take up approx half the page,
     * there is little room for the body content (which would result in many pages,
     * and unnecessary processing).
     * 
     * At the same time, we need enough body content to produce first page, odd page,
     * and even page for each section.
     * 
     * So this method replaces the existing body content with content which is sufficient
     * for our needs.  This method isn't essential, but it should make things faster.
     * 
     * It leaves the headers/footers untouched, since it is those which we're 
     * most interested in at this point.
     *  
     * @param hfPkg
     */
    static void trimContent(WordprocessingMLPackage hfPkg)   {
    	
    	// Find the sectPrs
    	SectPrFinder sf = new SectPrFinder(hfPkg.getMainDocumentPart());
		try {
			new TraversalUtil(hfPkg.getMainDocumentPart().getContents(), sf);
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}  
		
		List<SectPr> sectPrList = sf.getSectPrList();
		
		// Was there a body level one?
		if (hfPkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr()!=null) {
			//then delete the first entry (which is where SectPrFinder put it)
			sectPrList.remove(0);  
		}
		
		// Now generate content; let's use
		P filler = createFillerP();
		List<Object> contents = hfPkg.getMainDocumentPart().getContent();
		contents.clear();
		
		for (SectPr sectPr : sectPrList) {
			
			contents.add(filler);
			contents.add(filler);
			contents.add(filler);
			contents.add(filler);
			
			// We expect to cause, in due course, something like:
			// WARN org.apache.fop.apps.FOUserAgent .processEvent line 97 - 
			//          The contents of fo:region-body on page 6 exceed its viewport 
			//          by 29068 millipoints. (See position 1:1038)


			// now add the sectPr
	    	P p = Context.getWmlObjectFactory().createP(); 
    	    PPr ppr = Context.getWmlObjectFactory().createPPr(); 
    	    p.setPPr(ppr);
    	    ppr.setSectPr(sectPr);
    	    
			contents.add(p);
			
		}
		
		// Add content before the body level sectPr
		if (hfPkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr()!=null) {

			contents.add(filler);
			contents.add(filler);
			contents.add(filler);
			contents.add(filler);
			
		}
    }
    
    private static P createFillerP() {

    	org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

    	P p = wmlObjectFactory.createP(); 
    	    // Create object for pPr
    	    PPr ppr = wmlObjectFactory.createPPr(); 
    	    p.setPPr(ppr); 
    	        // Create object for rPr
    	        ParaRPr pararpr = wmlObjectFactory.createParaRPr(); 

    	        // Create object for spacing
    	        PPrBase.Spacing pprbasespacing = wmlObjectFactory.createPPrBaseSpacing(); 
    	        ppr.setSpacing(pprbasespacing); 
    	            pprbasespacing.setBefore( BigInteger.valueOf( 800) ); 
    	            pprbasespacing.setAfter( BigInteger.valueOf( 800) ); 
    	    // Create object for r
    	    R r = wmlObjectFactory.createR(); 
    	    p.getContent().add( r); 
    	        // Create object for rPr
    	        RPr rpr = wmlObjectFactory.createRPr(); 
    	        r.setRPr(rpr); 
    	            // Create object for sz
    	            HpsMeasure hpsmeasure3 = wmlObjectFactory.createHpsMeasure(); 
    	            rpr.setSz(hpsmeasure3); 
    	                hpsmeasure3.setVal( BigInteger.valueOf( 96) ); 

    	        // Create object for t (wrapped in JAXBElement) 
    	        Text text = wmlObjectFactory.createText(); 
    	        JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text); 
    	        r.getContent().add( textWrapped); 
    	            text.setValue( "BODY CONTENT"); 

    	return p;
    }    
	
    
    static org.w3c.dom.Document getAreaTreeViaFOP(WordprocessingMLPackage hfPkg, boolean useXSLT) throws Docx4JException, ParserConfigurationException, SAXException, IOException  {

    	  // Currently FOP dependent!  But an Antenna House version ought to be feasible.
    	
        FOSettings foSettings = Docx4J.createFOSettings();
        foSettings.setOpcPackage(hfPkg);
        foSettings.setApacheFopMime(MimeConstants.MIME_FOP_AREA_TREE);
        
        foSettings.setLayoutMasterSetCalculationInProgress(true); // avoid recursion
        
//        foSettings.getFeatures().add(ConversionFeatures.PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM); // in 3.0.1, this is off by default
        
        // Since hfPkg is already a clone, we don't need PP_COMMON_DEEP_COPY
        // Plus it invokes setFontMapper, which does processEmbeddings again, and those fonts aren't much use to us here
        foSettings.getFeatures().remove(ConversionFeatures.PP_COMMON_DEEP_COPY);
        
        if (log.isDebugEnabled()) {
        	foSettings.setFoDumpFile(new java.io.File(System.getProperty("user.dir") + "/hf.fo"));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        if (useXSLT) {
        	Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        } else {
        	Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);        	
        }
        
        InputStream is = new ByteArrayInputStream(os.toByteArray());
		DocumentBuilder builder = XmlUtils.getNewDocumentBuilder();
		return builder.parse(is);

    }	
    
    /**
     * 
     * The area tree contains the information required to calculate how tall each header and footer should be.
     * 
     * The method performs the calculation (summing block/@bpda) for each region.
     * 
     * @param areaTree
     * @param headerBpda
     * @param footerBpda
     */
    static void calculateHFExtents(org.w3c.dom.Document areaTree, Map<String, Integer> headerBpda, Map<String, Integer> footerBpda) {
    	
    	if (log.isDebugEnabled()) {
    		log.debug(XmlUtils.w3CDomNodeToString(areaTree));
    	}
    	
    	/*
			<areaTree version="2.0">
			  <pageSequence>
			    <pageViewport bounds="0 0 595440 841680" formatted-nr="1" key="P1" nr="1" simple-page-master-name="s1-default">
			      <page>
			        <regionViewport bap="0 0 0 0" bpd="4067716" bpda="4067716" ipd="451440" ipda="451440" is-viewport-area="true" rect="72000 18000 451440 4067716">
			          <regionBefore bap="0 0 0 0" bpd="4067716" bpda="4067716" ctm="[1.0 0.0 0.0 1.0 72000.0 18000.0]" ipd="451440" ipda="451440" is-reference-area="true" name="xsl-region-before-default">
			            <block bap="0 0 0 0" bpd="51888" bpda="63226" ipd="451440" ipda="451440" space-after="11338">
			              <lineArea bap="0 0 0 0" bpd="49863" bpda="51888" ipd="451440" ipda="451440" space-after="1013" space-before="1012" start-indent="331440">
			                <inlineparent bap="0 0 0 0" bpd="8325" bpda="8325" ipd="120000" ipda="120000" offset="41538">
			                  <viewport bap="0 0 0 0" bpd="48000" bpda="48000" ipd="120000" ipda="120000" offset="-41538" pos="0 0 120000 48000">
			                    <image bap="0 0 0 0" bpd="0" ipd="0" url="file:/C:/Users/jharrop/AppData/Local/Temp/e0f5fd14-fbda-4d1a-8172-735c5aeec001image1.jpeg"/>
			                  </viewport>
			                </inlineparent>
			              </lineArea>
			            </block>
			            <block bap="0 0 0 0" bpd="10350" bpda="10350" ipd="451440" ipda="451440">
			              <lineArea bap="0 0 0 0" bpd="8325" bpda="10350" end-indent="448938" ipd="451440" ipda="451440" space-after="1013" space-before="1012">
			                <text bap="0 0 0 0" baseline="6462" bpd="8325" bpda="8325" color="#000000" font-name="sans-serif" font-size="9000" font-style="normal" font-weight="400" ipd="2502" ipda="2502" offset="0">
			                  <space> </space>
			                </text>
			              </lineArea>
			            </block>
			          </regionBefore>
			        </regionViewport>
			        <regionViewport bap="0 0 0 0" bpd="411023" bpda="411023" ipd="451440" ipda="451440" is-viewport-area="true" rect="72000 412657 451440 411023">
			          <regionAfter bap="0 0 0 0" bpd="411023" bpda="411023" ctm="[1.0 0.0 0.0 1.0 72000.0 412657.0]" ipd="451440" ipda="451440" is-reference-area="true" name="xsl-region-after-default">
    	 */
    	
		// for each pageSequence
    	for (int i = 0 ; i <areaTree.getDocumentElement().getChildNodes().getLength(); i++ ) {
    		
    		Node pageSequence = areaTree.getDocumentElement().getChildNodes().item(i);
    		
    		if (pageSequence instanceof Element ) {
    			
    			if (!pageSequence.getLocalName().equals("pageSequence")) {
    				log.error("Unexpected element: " + pageSequence.getLocalName());
    				continue;
    			}
    			
    			// for each pageViewport
    	    	for (int j = 0 ; j <pageSequence.getChildNodes().getLength(); j++ ) {

    	    		Node pageViewport = pageSequence.getChildNodes().item(j);
    	    		
    	    		if (pageViewport instanceof Element ) {
    	    		
	        			if (!pageViewport.getLocalName().equals("pageViewport")) {
	        				log.error("Unexpected element: " + pageViewport.getLocalName());
	        				continue;
	        			}
	        			
	        			String simplePageMasterName = ((Element)pageViewport).getAttribute("simple-page-master-name");
	        			
	        			log.debug("processing simple-page-master-name: " + simplePageMasterName);
	        			
	        			if (headerBpda.containsKey(simplePageMasterName)) {
	        				// already done this one
	        				log.debug(".. dupe .. ignore");
	        				continue;
	        			}
	        			
	        			// We just need to look at the first page 
	        			Element page = (Element)pageViewport.getFirstChild();
	        			
	        			// for each regionViewport
	        	    	for (int k = 0 ; k <page.getChildNodes().getLength(); k++ ) {

	        	    		Node regionViewport = page.getChildNodes().item(k);
	        	    		
	        	    		if (regionViewport instanceof Element ) {
	        	    			
	    	        			Element region = (Element)regionViewport.getFirstChild();

	    	        			int bpda = 0;
	    	        	    	if (region.getLocalName().equals("regionBefore")
	    	        	    			|| region.getLocalName().equals("regionAfter")) {
	    	        			
		    	        			// Now for each block child, sum the @bpda 
		    	        			// for each block
		    	        	    	for (int m = 0 ; m <region.getChildNodes().getLength(); m++ ) {
		    	        	    		
		    	        	    		Element block = (Element)region.getChildNodes().item(m);
		    	        	    		if (block.getLocalName().equals("block")) {
		    	        	    			try {
	    	        	    					bpda += Integer.parseInt(block.getAttribute("bpda"));
		    	        	    			} catch (java.lang.NumberFormatException nfe) {
		    	        	    				// safe to ignore?
		    	        	    				log.error("For @bpda, \n"+ XmlUtils.w3CDomNodeToString(block));
		    	        	    				log.error(nfe.getMessage(), nfe);
		    	        	    			}
		    	        	    			
		    	        	    		} else {
		    	        	    			// eg beforeFloat, mainReference, footnote
		    	            				log.debug(simplePageMasterName + " - Unexpected element: " + block.getLocalName());
		    	            			}
		    	        	    	}
	    	        	    	}
	    	        	    	
	    	        	    	if (region.getLocalName().equals("regionBefore")) {
	    	        	    		headerBpda.put(simplePageMasterName, Integer.valueOf(bpda));
	    	        	    		
	    	        	    	} else if (region.getLocalName().equals("regionAfter")) {
	    	        	    		footerBpda.put(simplePageMasterName, Integer.valueOf(bpda));
	    	        	    		
	    	        	    	} else if (region.getLocalName().equals("regionBody")) {
	    	        	    		// not interested
	    	        	    	} else {
	    	        	    		log.error("unexpected region: " + region.getLocalName());
	    	        	    	}
	        	    			
	        	    		}
	        			
	        	    	}
    	    		}
    			
    	    	}
    		}
    		
    		
    	}
    	
    }
    

    /**
     * Inject the calculated heights for each header and footer, and adjust the region body margins to fit them.
     * 
     * @param layoutMasterSet
     * @param headerBpda
     * @param footerBpda
     */
    static void adjustLayoutMasterSet(LayoutMasterSet layoutMasterSet, ConversionSectionWrappers conversionSectionWrappers, Map<String, Integer> headerBpda, Map<String, Integer> footerBpda) {
    	
		List<ConversionSectionWrapper> sections = conversionSectionWrappers.getList();
		ConversionSectionWrapper section = null;
    	
    	for (Object o : layoutMasterSet.getSimplePageMasterOrPageSequenceMaster()) {
    		
    		if (o instanceof SimplePageMaster) {
    			
    			/*
    			 *     <simple-page-master margin-bottom="0.25in" margin-left="1in" margin-right="1in" margin-top="0.25in" master-name="s1-default" page-height="11.69in" page-width="8.27in">
					      <region-body margin-bottom="145mm" margin-left="0mm" margin-right="0mm" margin-top="145mm"/>
					      <region-before extent="1435mm" region-name="xsl-region-before-default"/>
					      <region-after extent="145mm" region-name="xsl-region-after-default"/>
					    </simple-page-master>
    			 */
    			
    			SimplePageMaster spm =((SimplePageMaster)o);
    			
    			String simplePageMasterName = spm.getMasterName();  // eg s1-first page
    			
    			// We'll need the corresponding ConversionSectionWrapper
    			int index = -1 + Integer.parseInt(
    					simplePageMasterName.substring(1, simplePageMasterName.indexOf("-")));
    			PageDimensions page = null;
    			if (sections.get(index)==null) {
    				log.error("Couldn't find section " + index + " from " + simplePageMasterName);
    			} else {
    				page = sections.get(index).getPageDimensions();
    			}
    			
    			// Region before
    			if (spm.getRegionBefore()!=null) {
    				Integer hBpdaMilliPts = headerBpda.get(simplePageMasterName);
    				if (hBpdaMilliPts==null) {
    					// No headerBpda for s1-default
    					log.error("No headerBpda for " + simplePageMasterName);
    					// You need to debug to find out why
    					
    				} else {
	        			float hBpdaPts = hBpdaMilliPts/1000;
		    			spm.getRegionBefore().setExtent(hBpdaPts+"pt");
		    			spm.getRegionBody().setMarginTop(hBpdaPts+"pt");
		    			
		    			// If the top margin in Word > what we have, then pad with margin top
		    			float totalHeight = (page.getHeaderMargin()/20 ) // twips to points
		    								+ hBpdaPts;
		    			
		    			float extraMargin = (page.getPgMar().getTop().intValue()/20) - totalHeight;  
		    			
		    			if (extraMargin>0) {
		    				float required = (page.getPgMar().getTop().intValue()-page.getHeaderMargin())/20;
			    			spm.getRegionBody().setMarginTop(required+"pt");	    				
		    			} // otherwise, we've expanded to the extent of the header already
    				}
    			}
    			
    			// Region after
    			if (spm.getRegionAfter()!=null) {
    				Integer fBpdaMilliPts = footerBpda.get(simplePageMasterName);
    				if (fBpdaMilliPts==null) {
    					log.error("No footerBpda for " + simplePageMasterName);
    					
    				} else {    				
		    			float fBpdaPts = fBpdaMilliPts/1000;
		    			spm.getRegionAfter().setExtent(fBpdaPts+"pt");
		    			spm.getRegionBody().setMarginBottom(fBpdaPts+"pt");
		    			
		    			// If the bottom margin in Word > what we have, then pad with margin bottom
		    			float totalHeight = (page.getFooterMargin()/20 ) // twips to points
		    								+ fBpdaPts;
		    			
		    			float extraMargin = (page.getPgMar().getBottom().intValue()/20) - totalHeight;  
		    			
		    			if (extraMargin>0) {
		    				float required = (page.getPgMar().getBottom().intValue()-page.getFooterMargin())/20;
			    			spm.getRegionBody().setMarginBottom(required+"pt");	    				
		    			} // otherwise, we've expanded to the extent of the footer already
	    			
    				}
    			}    			
    			
    		}
    		
    	}
    	
    }    
    

}
