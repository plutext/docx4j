package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.DocumentModel;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
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

public class LayoutMasterSetBuilder {
	
	private static org.plutext.jaxb.xslfo.ObjectFactory factory;
	
	public static DocumentFragment getLayoutMasterSetFragment(WordprocessingMLPackage wordMLPackage) {

		LayoutMasterSet lms = getFoLayoutMasterSet(wordMLPackage);
		
		org.w3c.dom.Document document = XmlUtils.marshaltoW3CDomDocument(lms, Context.getXslFoContext() );
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());
		return docfrag;		
	}
	
	private static LayoutMasterSet getFoLayoutMasterSet(WordprocessingMLPackage wordMLPackage) {

		factory = new ObjectFactory();		
		LayoutMasterSet lms = factory.createLayoutMasterSet();
		
		// For each sectPr
		DocumentModel documentModel = wordMLPackage.getDocumentModel();		
		List<SectionWrapper> sections = documentModel.getSections();
		
		int i = 1;
		for( SectionWrapper sw : sections) {
			
			HeaderFooterPolicy hf = sw.getHeaderFooterPolicy();
			
			String sectionName = "s" + i;
			
			// FIRST, create simple-page-masters
			
			// has first header or footer?
			if (hf.getFirstHeader()!=null || hf.getFirstFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-firstpage", "firstpage",
						(hf.getFirstHeader()!=null),
						(hf.getFirstFooter()!=null) ));
			}
			
			// has default header or footer?
			// if this is present, any "even" present is ignored! 
			if (hf.getDefaultHeader()!=null 
					|| hf.getDefaultFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-default", "default",
						(hf.getDefaultHeader()!=null),
						(hf.getDefaultFooter()!=null) ));				
			}
			// has even or odd header or footer?
			else if (hf.getEvenHeader()!=null 
					|| hf.getOddHeader()!=null
					|| hf.getEvenFooter()!=null
					|| hf.getOddFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-evenpage", "evenpage",
						(hf.getEvenHeader()!=null),
						(hf.getEvenFooter()!=null) ));
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
						createSimplePageMaster(sectionName + "-oddpage", "default",
							(hf.getOddHeader()!=null),
							(hf.getOddFooter()!=null) ));				
			}

			
			// simple
			lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-simple", "simple",
						true, true));
			
			// SECOND, create page-sequence-masters
			lms.getSimplePageMasterOrPageSequenceMaster().add(
					createPageSequenceMaster(hf, sectionName )  );
			
			i++;
		}	
		
		return lms;
	}
	
	private static PageSequenceMaster createPageSequenceMaster(HeaderFooterPolicy hf, String sectionName ) {
		
		PageSequenceMaster psm = factory.createPageSequenceMaster();
		psm.setMasterName(sectionName);
		
		RepeatablePageMasterAlternatives rpma = factory.createRepeatablePageMasterAlternatives();
		
		psm.getSinglePageMasterReferenceOrRepeatablePageMasterReferenceOrRepeatablePageMasterAlternatives().add(rpma);
		
		// has first header or footer?
		if (hf.getFirstHeader()!=null || hf.getFirstFooter()!=null) {			
			ConditionalPageMasterReference cpmr1 = factory.createConditionalPageMasterReference();
			cpmr1.setMasterReference(sectionName+"-firstpage");
			cpmr1.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr1);			
		}

		// has default header or footer?
		// if this is present, any "even" present is ignored! 
		if (hf.getDefaultHeader()!=null || hf.getDefaultFooter()!=null) {
			
			ConditionalPageMasterReference cpmr4 = factory.createConditionalPageMasterReference();
			cpmr4.setMasterReference(sectionName+"-default");
			//cpmr4.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr4);			
			
		} else		
		if (hf.getEvenHeader()!=null || hf.getEvenFooter()!=null) {

			ConditionalPageMasterReference cpmr2 = factory.createConditionalPageMasterReference();
			cpmr2.setMasterReference(sectionName+"-evenpage");
			//cpmr2.setPagePosition(PagePositionType.FIRST);
			cpmr2.setOddOrEven(OddOrEvenType.EVEN);
			rpma.getConditionalPageMasterReference().add(cpmr2);			
			
			ConditionalPageMasterReference cpmr3 = factory.createConditionalPageMasterReference();
			cpmr3.setMasterReference(sectionName+"-oddpage");
			//cpmr3.setPagePosition(PagePositionType.FIRST);
			cpmr3.setOddOrEven(OddOrEvenType.ODD);
			rpma.getConditionalPageMasterReference().add(cpmr3);			
			
		} 
		
			ConditionalPageMasterReference cpmr5 = factory.createConditionalPageMasterReference();
			cpmr5.setMasterReference(sectionName+"-simple");
			//cpmr5.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr5);						
		
		return psm;
	}
	
	private static SimplePageMaster createSimplePageMaster( 
			String masterName, String appendRegionName, boolean needBefore, boolean needAfter) {
		
		SimplePageMaster spm = factory.createSimplePageMaster();
		spm.setMasterName(masterName);
		
		// dimensions.  TODO. Read these from the document.
		spm.setPageHeight("297mm");
		spm.setPageWidth("210mm");
		spm.setMarginTop("10mm");
		spm.setMarginBottom("10mm");
		spm.setMarginLeft("10mm");
		spm.setMarginRight("10mm");
		
		RegionBody rb = factory.createRegionBody();
		rb.setMarginTop("20mm");
		rb.setMarginBottom("20mm");
		rb.setMarginLeft("0mm");
		rb.setMarginRight("0mm");
		spm.setRegionBody(rb);
		
		if (needBefore) {
			RegionBefore rBefore = factory.createRegionBefore();
			rBefore.setRegionName("xsl-region-before-"+appendRegionName);
			rBefore.setExtent("10mm");
			spm.setRegionBefore(rBefore);
		}

		if (needAfter) {
			RegionAfter rAfter = factory.createRegionAfter();
			rAfter.setRegionName("xsl-region-after-"+appendRegionName);
			rAfter.setExtent("10mm");
			spm.setRegionAfter(rAfter);
		}
		
		return spm;
	}
	

}
