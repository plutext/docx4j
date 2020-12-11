/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.fo;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.preprocess.PartialDeepCopy;
import org.docx4j.events.EventFinished;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.plutext.jaxb.xslfo.ConditionalPageMasterReference;
import org.plutext.jaxb.xslfo.LayoutMasterSet;
import org.plutext.jaxb.xslfo.ObjectFactory;
import org.plutext.jaxb.xslfo.OddOrEvenType;
import org.plutext.jaxb.xslfo.PagePositionType;
import org.plutext.jaxb.xslfo.PageSequenceMaster;
import org.plutext.jaxb.xslfo.RegionAfter;
import org.plutext.jaxb.xslfo.RegionBefore;
import org.plutext.jaxb.xslfo.RegionBody;
import org.plutext.jaxb.xslfo.RepeatablePageMasterAlternatives;
import org.plutext.jaxb.xslfo.SimplePageMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;



/**
 * A description of how this stuff works
 * can be found at /docs/headers_footers.docx
 * 
 * Its not possible to let FOP set the height (@extent) of the header and footer regions 
 * automatically: http://apache-fop.1065347.n5.nabble.com/Auto-size-header-to-fit-with-content-td4455.html 
 * 
 * So we need to set the height (@extent) of the header and footer regions explicitly.
 * 
 * We do that by creating a temp FO file which contains essentially just the headers/footers,
 * then interrogating FOP's area tree representation to find the height of each.
 * 
 * So:
 * 1. create LayoutMasterSet (with large extents)
 * 2. generate area tree from that
 * 3. return LayoutMasterSet (with required extents)
 * 
 * @author jharrop
 *
 */
public class LayoutMasterSetBuilder {

	protected static Logger log = LoggerFactory.getLogger(LayoutMasterSetBuilder.class);
	
	private static org.plutext.jaxb.xslfo.ObjectFactory factory;
		
	public static DocumentFragment getLayoutMasterSetFragment(AbstractWmlConversionContext context) {

		LayoutMasterSet lms = getFoLayoutMasterSet(context);	
		
		// Set suitable extents, for which we need area tree 
		FOSettings foSettings = (FOSettings)context.getConversionSettings();
		if ( !foSettings.lsLayoutMasterSetCalculationInProgress()) // Avoid infinite loop
			// Can't just do it where foSettings.getApacheFopMime() is not MimeConstants.MIME_FOP_AREA_TREE,
			// since TOC functionality uses that.
		{
			fixExtents( lms, context, true);
		}
		
		org.w3c.dom.Document document = XmlUtils.marshaltoW3CDomDocument(lms, Context.getXslFoContext() );
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());
		
		
		return docfrag;		
	}
	
	private static void fixExtents(LayoutMasterSet lms, AbstractWmlConversionContext context, boolean useXSLT) {
		
		WordprocessingMLPackage wordMLPackage = context.getWmlPackage();

		StartEvent startEvent = new StartEvent( wordMLPackage, WellKnownProcessSteps.FO_EXTENTS );
		startEvent.publish();
		
//		log.debug(wordMLPackage.getMainDocumentPart().getXML());

        if(log.isDebugEnabled()) {
            log.debug("incoming LMS: " + XmlUtils.marshaltoString(lms, Context.getXslFoContext()));
        }
		
		// Make a copy of it
		Set<String> relationshipTypes = new TreeSet<String>();
			relationshipTypes.add(Namespaces.DOCUMENT);
			relationshipTypes.add(Namespaces.HEADER);
			relationshipTypes.add(Namespaces.FOOTER);
			//those are probably not affected but get visited by the 
			//default TraversalUtil.
			relationshipTypes.add(Namespaces.ENDNOTES);
			relationshipTypes.add(Namespaces.FOOTNOTES);
			relationshipTypes.add(Namespaces.COMMENTS);
			
		WordprocessingMLPackage hfPkg;
		try {
			hfPkg = (WordprocessingMLPackage) PartialDeepCopy.process(wordMLPackage, relationshipTypes);
			
			FOPAreaTreeHelper.trimContent(hfPkg);
			
			org.w3c.dom.Document areaTree = FOPAreaTreeHelper.getAreaTreeViaFOP( hfPkg, useXSLT);
			
			log.debug(XmlUtils.w3CDomNodeToString(areaTree));
			
			Map<String, Integer> headerBpda = new HashMap<String, Integer>();
			Map<String, Integer> footerBpda = new HashMap<String, Integer>();
			
			FOPAreaTreeHelper.calculateHFExtents(areaTree,  headerBpda,  footerBpda);
			
			FOPAreaTreeHelper.adjustLayoutMasterSet(lms, context.getSections(), headerBpda, footerBpda);				
			
			/* Don't deleteEmbeddedFontTempFiles here, since they may be required by FOP later.
			 * And they should get deleted later anyway...
			 * 
				FontTablePart ftp = hfPkg.getMainDocumentPart().getFontTablePart();
				if (ftp!=null) {
					ftp.deleteEmbeddedFontTempFiles();
				}
			*/
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        if(log.isDebugEnabled()) {
            log.debug("resulting LMS: " + XmlUtils.marshaltoString(lms, Context.getXslFoContext()));
        }
		
		new EventFinished(startEvent).publish();
		
	}
	
    /**
     * For XSLFOExporterNonXSLT
     * @since 3.0
     * 
     */	
	public static void appendLayoutMasterSetFragment(AbstractWmlConversionContext context, Node foRoot) {

		LayoutMasterSet lms = getFoLayoutMasterSet(context);	
		
		// Set suitable extents, for which we need area tree 
		FOSettings foSettings = (FOSettings)context.getConversionSettings();
		if ( !foSettings.lsLayoutMasterSetCalculationInProgress()) // Avoid infinite loop
			// Can't just do it where foSettings.getApacheFopMime() is not MimeConstants.MIME_FOP_AREA_TREE,
			// since TOC functionality uses that.
		{
			fixExtents( lms, context, false);
		}
		
		org.w3c.dom.Document document = XmlUtils.marshaltoW3CDomDocument(lms, Context.getXslFoContext() );
		XmlUtils.treeCopy(document.getDocumentElement(), foRoot);
	}

	
	private static LayoutMasterSet getFoLayoutMasterSet(AbstractWmlConversionContext context) {
		
		
		LayoutMasterSet lms = getFactory().createLayoutMasterSet();
		List<ConversionSectionWrapper> sections = context.getSections().getList();
		ConversionSectionWrapper section = null;
		
		for(int i=0; i<sections.size(); i++) {
			
			section = sections.get(i);
			HeaderFooterPolicy hf = section.getHeaderFooterPolicy();
			String sectionName = "s" + Integer.toString(i + 1);
			
			// FIRST, create simple-page-masters
			// has first header or footer?
			if (hf.getFirstHeader()!=null || hf.getFirstFooter()!=null) {
				// per spec, HeaderFooterPolicy checks the titlePg elememt
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-firstpage", 
							section.getPageDimensions(), 
							"firstpage",
						(hf.getFirstHeader()!=null),
						(hf.getFirstFooter()!=null) ));
			}
			
			// has even or odd header or footer?
    		/*
    		 *       <w:headerReference w:type="even" r:id="rId12"/>
    		 *       <w:headerReference w:type="default" r:id="rId13"/>
    		 *       
    		 *       the default one is treated as odd.
    		 */
			if (hf.getEvenHeader()!=null || hf.getEvenFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-evenpage",  
							section.getPageDimensions(), 
							"evenpage",
						(hf.getEvenHeader()!=null),
						(hf.getEvenFooter()!=null) ));
				
				// the xslt outputs a "-default" page as the odd-page
			}
			
			if (hf.getDefaultHeader()!=null 
					|| hf.getDefaultFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-default",  
							section.getPageDimensions(), 
							"default",
						(hf.getDefaultHeader()!=null),
						(hf.getDefaultFooter()!=null) ));				
			}

			// simple: no headers and footers - after the first page anyway/
			// We still need this where there is just a first page header/footer,
			// since otherwise there'd be no page sequence for any content 
			// after the first page, and you'd get: 
			//    org.apache.fop.fo.pagination.PageProductionException: 
			//    Subsequences exhausted in page-sequence-master ..., cannot recover.
			//
			// <w:sectPr>
			//   <w:headerReference w:type="first" r:id="rId7"/>
			// </w:sectPr>			
			if (
				(hf.getDefaultHeader() == null) && (hf.getDefaultFooter() == null)) {
				lms.getSimplePageMasterOrPageSequenceMaster().add(
						createSimplePageMaster(sectionName + "-simple",  
								section.getPageDimensions(), 
								"simple",
							true, true));
			}
			
			// SECOND, create page-sequence-masters
			lms.getSimplePageMasterOrPageSequenceMaster().add(
					createPageSequenceMaster(hf, sectionName )  );
		}
		
		// 
		
		return lms;
	}
	
	private static PageSequenceMaster createPageSequenceMaster(HeaderFooterPolicy hf, 
			String sectionName ) {
		
		boolean noHeadersFootersAfterFirstPage = true;
		
		PageSequenceMaster psm = getFactory().createPageSequenceMaster();
		psm.setMasterName(sectionName);
		
		RepeatablePageMasterAlternatives rpma = getFactory().createRepeatablePageMasterAlternatives();
		
		psm.getSinglePageMasterReferenceOrRepeatablePageMasterReferenceOrRepeatablePageMasterAlternatives().add(rpma);
		
		// has first header or footer?
		if (hf.getFirstHeader()!=null || hf.getFirstFooter()!=null) {			
			ConditionalPageMasterReference cpmr1 = getFactory().createConditionalPageMasterReference();
			cpmr1.setMasterReference(sectionName+"-firstpage");
			cpmr1.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr1);
		}

		if (hf.getEvenHeader()!=null || hf.getEvenFooter()!=null) {

			ConditionalPageMasterReference cpmr2 = getFactory().createConditionalPageMasterReference();
			cpmr2.setMasterReference(sectionName+"-evenpage");
			//cpmr2.setPagePosition(PagePositionType.FIRST);
			cpmr2.setOddOrEven(OddOrEvenType.EVEN);
			rpma.getConditionalPageMasterReference().add(cpmr2);			
			
			// the xslt outputs a "-default" page as the odd-page
			ConditionalPageMasterReference cpmr3 = getFactory().createConditionalPageMasterReference();
			cpmr3.setMasterReference(sectionName+"-default");
			//cpmr3.setPagePosition(PagePositionType.FIRST);
			cpmr3.setOddOrEven(OddOrEvenType.ODD);
			rpma.getConditionalPageMasterReference().add(cpmr3);			
			
			noHeadersFootersAfterFirstPage = false;
		} else if (hf.getDefaultHeader()!=null || hf.getDefaultFooter()!=null) {
			
			ConditionalPageMasterReference cpmr4 = getFactory().createConditionalPageMasterReference();
			cpmr4.setMasterReference(sectionName+"-default");
			//cpmr4.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr4);			
			
			noHeadersFootersAfterFirstPage = false;
		}
		
		if (noHeadersFootersAfterFirstPage) {
			ConditionalPageMasterReference cpmr5 = getFactory().createConditionalPageMasterReference();
			cpmr5.setMasterReference(sectionName+"-simple");
			//cpmr5.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr5);						
		}
		
		return psm;
	}
	
	
	private static SimplePageMaster createSimplePageMaster( 
			String masterName, PageDimensions page, String appendRegionName, 
			boolean needBefore, boolean needAfter) {
		
		// This method uses dummy large extents
		// A later step fixes them.
		
		SimplePageMaster spm = getFactory().createSimplePageMaster();
		spm.setMasterName(masterName);
		
		// dimensions.  
		//   <w:pgSz w:w="12240" w:h="15840"/>
        //   <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>

		spm.setPageHeight( UnitsOfMeasurement.twipToBest(page.getPgSz().getH().intValue() ));
		spm.setPageWidth(  UnitsOfMeasurement.twipToBest(page.getPgSz().getW().intValue() ));
		
		spm.setMarginLeft( UnitsOfMeasurement.twipToBest(page.getPgMar().getLeft().intValue() ) );
		spm.setMarginRight( UnitsOfMeasurement.twipToBest(page.getPgMar().getRight().intValue()) );
		
		/* 
		 * Region before & after live in region body margins:
		 * 
		 * Per http://www.w3.org/TR/xsl/#fo_region-body
		 * 
		 * The body region should be sized and positioned within the fo:simple-page-master 
		 * so that there is room for the areas returned by the flow that is assigned to the 
		 * fo:region-body and for any desired side regions, that is, fo:region-before, 
		 * fo:region-after, fo:region-start and fo:region-end's that are to be placed on the same page. 
		 * 
		 * These side regions are positioned within the content-rectangle of the page-reference-area. 
		 * The margins on the fo:region-body are used to position the region-viewport-area for the 
		 * fo:region-body and to leave space for the other regions that surround the fo:region-body.
		 *                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		 *                    
		 * The spacing between the last four regions and the fo:region-body is determined by subtracting 
		 * the relevant extent trait on the side regions from the trait that corresponds to the "margin-x" 
		 * property on the fo:region-body.
		 */
		RegionBody rb = getFactory().createRegionBody();		
		rb.setMarginLeft("0mm");
		rb.setMarginRight("0mm");

		rb.setColumnCount(String.valueOf(page.getColsNum())); //Number of Equal Width Columns
		rb.setColumnGap(UnitsOfMeasurement.twipToBest(page.getColsSpacing())); //Spacing Between Equal Width Columns

		float halfPageHeight = page.getPgSz().getH().intValue()/40; // convert from twips, then * 0.5
		String halfPageHeightPts = halfPageHeight + "pt";  
		
		spm.setRegionBody(rb);
		
		if (needBefore) {
			//Header
			RegionBefore rBefore = getFactory().createRegionBefore();
			rBefore.setRegionName("xsl-region-before-"+appendRegionName);
			spm.setRegionBefore(rBefore);
			
			// Margin top on SPM is space between the page edge and the start of the header			
			int marginTopTwips 
				=  page.getHeaderMargin();
			spm.setMarginTop( UnitsOfMeasurement.twipToBest(marginTopTwips ) );
			
			// Size header manually
			rBefore.setExtent( halfPageHeightPts); // A4 portrait is 297mm high
			
			
			// Leave room for this region in body margin
			rb.setMarginTop(halfPageHeightPts);
			
			
		} else {
			// No header
			spm.setMarginTop( UnitsOfMeasurement.twipToBest(page.getPgMar().getTop().intValue() ) );
		}

		if (needAfter) {
			// Footer
			RegionAfter rAfter = getFactory().createRegionAfter();
			rAfter.setRegionName("xsl-region-after-"+appendRegionName);
			spm.setRegionAfter(rAfter);
			
			int marginBottomTwips= page.getFooterMargin();
			spm.setMarginBottom( UnitsOfMeasurement.twipToBest(marginBottomTwips) );
			
			// Size footer manually
			rAfter.setExtent( halfPageHeightPts); // A4 portrait is 297mm high
					
			// Leave room for this region in body margin
			rb.setMarginBottom(halfPageHeightPts );
			
		} else {
			// No footer
			spm.setMarginBottom( UnitsOfMeasurement.twipToBest(page.getPgMar().getBottom().intValue()) );
		}
		
		return spm;
	}


	private static ObjectFactory getFactory() {
		if (factory == null) factory = new ObjectFactory();
		return factory;
	}

}
