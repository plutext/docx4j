package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.ConversionSectionWrapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.PageDimensions;
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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * A description of how this stuff works
 * can be found at /docs/headers_footers.docx
 * 
 * @author jharrop
 *
 */
public class LayoutMasterSetBuilder {

	protected static Logger log = Logger.getLogger(LayoutMasterSetBuilder.class);
	
	private static org.plutext.jaxb.xslfo.ObjectFactory factory;
		
	public static DocumentFragment getLayoutMasterSetFragment(AbstractWmlConversionContext context) {

		LayoutMasterSet lms = getFoLayoutMasterSet(context);		
		
		org.w3c.dom.Document document = XmlUtils.marshaltoW3CDomDocument(lms, Context.getXslFoContext() );
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());
		return docfrag;		
	}
	
    /**
     * For XSLFOExporterNonXSLT
     * @since 3.0
     * 
     */	
	public static void appendLayoutMasterSetFragment(AbstractWmlConversionContext context, Node foRoot) {

		LayoutMasterSet lms = getFoLayoutMasterSet(context);		
		
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

			// simple (no headers and footers)
			if ((hf.getFirstHeader() == null) && (hf.getFirstFooter() == null) &&
				(hf.getDefaultHeader() == null) && (hf.getDefaultFooter() == null) &&
				(hf.getEvenHeader() == null) && (hf.getEvenFooter() == null)) {
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
		
		return lms;
	}
	
	private static PageSequenceMaster createPageSequenceMaster(HeaderFooterPolicy hf, 
			String sectionName ) {
		boolean noHeadersFooters = true;
		
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
			noHeadersFooters = false;
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
			
			noHeadersFooters = false;
		} else if (hf.getDefaultHeader()!=null || hf.getDefaultFooter()!=null) {
			
			ConditionalPageMasterReference cpmr4 = getFactory().createConditionalPageMasterReference();
			cpmr4.setMasterReference(sectionName+"-default");
			//cpmr4.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr4);			
			
			noHeadersFooters = false;
		}
		
		if (noHeadersFooters) {
			ConditionalPageMasterReference cpmr5 = getFactory().createConditionalPageMasterReference();
			cpmr5.setMasterReference(sectionName+"-simple");
			//cpmr5.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr5);						
		}
		
		return psm;
	}
	
	private static final int HEADER_PADDING_TWIP = 360;
	private static final int FOOTER_PADDING_TWIP = 360;
	private static final int MIN_PAGE_MARGIN = 360;
	
	private static SimplePageMaster createSimplePageMaster( 
			String masterName, PageDimensions page, String appendRegionName, 
			boolean needBefore, boolean needAfter) {
		
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
		
		
		spm.setRegionBody(rb);
		
		if (needBefore) {
			//Header
			RegionBefore rBefore = getFactory().createRegionBefore();
			rBefore.setRegionName("xsl-region-before-"+appendRegionName);
			spm.setRegionBefore(rBefore);

			// Make margin smaller, because header takes up space it would otherwise occupy			
			int marginTopTwips 
				= page.getPgMar().getTop().intValue() 
					- (HEADER_PADDING_TWIP + page.getHeaderExtent() + page.getHeaderMargin() );
			if (marginTopTwips<MIN_PAGE_MARGIN) marginTopTwips=MIN_PAGE_MARGIN;				
			spm.setMarginTop( UnitsOfMeasurement.twipToBest(marginTopTwips ) );
			
			// Size header manually
			rBefore.setExtent( UnitsOfMeasurement.twipToBest(page.getHeaderExtent() ));
			
			// Leave room for this region in body margin
			rb.setMarginTop(UnitsOfMeasurement.twipToBest(page.getHeaderExtent()+ HEADER_PADDING_TWIP ));
			
		} else {
			// No header
			spm.setMarginTop( UnitsOfMeasurement.twipToBest(page.getPgMar().getTop().intValue() ) );
		}

		if (needAfter) {
			// Footer
			RegionAfter rAfter = getFactory().createRegionAfter();
			rAfter.setRegionName("xsl-region-after-"+appendRegionName);
			spm.setRegionAfter(rAfter);
			
			// Make margin smaller, because footer takes up space it would otherwise occupy  
			int marginBottomTwips
					= page.getPgMar().getBottom().intValue()
						- (FOOTER_PADDING_TWIP + page.getFooterExtent() + page.getFooterMargin() );
			if (marginBottomTwips<MIN_PAGE_MARGIN) marginBottomTwips=MIN_PAGE_MARGIN;			
			log.debug("marginBottomTwips: " + marginBottomTwips );
			spm.setMarginBottom( UnitsOfMeasurement.twipToBest(marginBottomTwips) );
			
			// Size footer manually
			rAfter.setExtent( UnitsOfMeasurement.twipToBest(page.getFooterExtent() ) );
					
			// Leave room for this region in body margin
			rb.setMarginBottom(UnitsOfMeasurement.twipToBest(page.getFooterExtent() + FOOTER_PADDING_TWIP ) );
			
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
