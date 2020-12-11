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
package org.docx4j.convert.out.common.wrappers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.convert.out.common.preprocess.PageNumberInformation;
import org.docx4j.convert.out.common.preprocess.PageNumberInformationCollector;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgSz;
import org.jvnet.jaxb2_commons.ppp.Child;

public class ConversionSectionWrapperFactory {
	
	protected static Logger log = LoggerFactory.getLogger(ConversionSectionWrapperFactory.class);
	
	protected static class SdtBlockFinder extends CallbackImpl {

		List<SdtBlock> sdtBlocks = new ArrayList<SdtBlock>();
		
		// Need a stack of these; only if we encounter a sectPr,
		// then copy contents of stack to list we remove.
		LinkedList<SdtBlock> ll = new LinkedList<SdtBlock>();
		
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof org.docx4j.wml.P
				&& ((org.docx4j.wml.P)o).getPPr() != null 
				&& ((org.docx4j.wml.P)o).getPPr().getSectPr() != null ) {
				
				// this sdt contains a sectPr, so add it
				// and all its ancestor sdts to the ones we need to delete
				
				for (SdtBlock sdt : ll) {
					if (!sdtBlocks.contains(sdt) ) {
						sdtBlocks.add((SdtBlock)sdt);
					}
				}
				
			}

			return null;
		}
		
		@Override
		public void walkJAXBElements(Object parent) {
			
			List children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {
					
					// if its wrapped in javax.xml.bind.JAXBElement, get its
					// value; this is ok, provided the results of the Callback
					// won't be marshalled
					o = XmlUtils.unwrap(o);
										
					this.apply(o);
					
					if (o instanceof SdtBlock) {
						ll.addLast((SdtBlock)o);
					}

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

					if (o instanceof SdtBlock) {
						ll.removeLast();
					}
					
				}
			}
		}
		
	}
	
	
	public static ConversionSectionWrappers process(WordprocessingMLPackage wmlPackage, boolean dummySections, boolean dummyPageNumbering) throws Docx4JException {
		
		List<ConversionSectionWrapper> conversionSections = null;
		Document document = wmlPackage.getMainDocumentPart().getContents();
		RelationshipsPart rels = wmlPackage.getMainDocumentPart().getRelationshipsPart();
		BooleanDefaultTrue evenAndOddHeaders = null;

		if ((wmlPackage.getMainDocumentPart().getDocumentSettingsPart() != null) &&
			(wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getContents() != null)) {
			evenAndOddHeaders = wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getContents().getEvenAndOddHeaders();
		}
		
		if (dummySections) {
			conversionSections = processDummy(wmlPackage, document, rels, evenAndOddHeaders, dummyPageNumbering);
		}
		else {
			conversionSections = processComplete(wmlPackage, document, rels, evenAndOddHeaders, dummyPageNumbering);
		}
		return new ConversionSectionWrappers(conversionSections);				
	}

	/** The dummy section wrappers only contains one section with all the document. Therefore
	 *  any sections within the document are ignored in the conversion process. As it doesn't 
	 *  need to check for sections it is faster and the html-Output only uses one section.<br>
	 *  It will use the Header/Footer of the body sectPr. This isn't correct, if there are 
	 *  several Sections in the document, but to find the correct SectPr it would need to check
	 *  the document content - and the aim of this method is a low overhead.  
	 * 
	 * @param wmlPackage
	 * @param dummyPageNumbering
	 * @return
	 */
	protected static List<ConversionSectionWrapper> processDummy(WordprocessingMLPackage wmlPackage, Document document, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, boolean dummyPageNumbering) {
		
		log.debug("starting");
		
		List<ConversionSectionWrapper> conversionSections = new ArrayList<ConversionSectionWrapper>();
		ConversionSectionWrapper currentSectionWrapper = null;
		HeaderFooterPolicy previousHF =
				new HeaderFooterPolicy(document.getBody().getSectPr(), null, rels, evenAndOddHeaders);
	
		// SectionWrapper does work where sectPr is null (ie document has no body level sectPr),
		// so document.getBody().getSectPr() is ok
	
		currentSectionWrapper = createSectionWrapper(
				document.getBody().getSectPr(), previousHF, rels, evenAndOddHeaders,
				1, document.getBody().getContent(), dummyPageNumbering); 		
				conversionSections.add(currentSectionWrapper);
		return conversionSections;
	}
	
	/**
	 * @param sectPr
	 * @param headerFooterPolicy
	 * @param rels
	 * @param evenAndOddHeaders  from the document settings part
	 * @param conversionSectionIndex
	 * @param content
	 * @param dummyPageNumbering
	 * @return
	 */
	protected static ConversionSectionWrapper createSectionWrapper(
			SectPr sectPr, HeaderFooterPolicy headerFooterPolicy, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, 
			int conversionSectionIndex, List<Object> content,
			boolean dummyPageNumbering) {
		
		ConversionSectionWrapper csw = 
					new ConversionSectionWrapper(sectPr, headerFooterPolicy, rels, evenAndOddHeaders,
					"s" + Integer.toString(conversionSectionIndex), content);
		
		PageNumberInformation pageNumberInformation = 
				PageNumberInformationCollector.process(csw, dummyPageNumbering);
		csw.setPageNumberInformation(pageNumberInformation);
		
		return csw;
	}
	
	protected static List<ConversionSectionWrapper> processComplete(WordprocessingMLPackage wmlPackage, Document document, 
			RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, boolean dummyPageNumbering) {
		
		log.debug("starting");

		// Get a list of all the sectPrs
		removeContentControls( document);
		List<SectPr> sectPrs =  getSectPrs(document);

		// Local vars
		List<ConversionSectionWrapper> conversionSections = new ArrayList<ConversionSectionWrapper>();
		ConversionSectionWrapper currentSectionWrapper = null;
		HeaderFooterPolicy previousHF = null;
		int conversionSectionIndex = 0;
		List<Object> sectionContent = new ArrayList<Object>();
		
		// Now go through the document content,
		
		int sectPrIndex = 0; // includes continuous ones
		for (Object o : document.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {

						/* If the *following* section is continuous, 
						 * don't add *this* section, because we want our
						 * sectionWrapper to surround the content preceding
						 * both sectPr.
						 * 
						 * When we do eventually create that section wrapper,
						 * it must use the header/footer settings from 
						 * *this* section.
						 * 
						 * There is a special case to consider:
						 *  
						        <w:sectPr>
						          <w:type w:val="continuous"/>
						          <w:pgSz <---- different values from previous sectPr
						 *
						 * In this case, Word will render a page break, 
						 * but:
						 * 1. still show it as continuous
						 * 2. still use the headers/footers from this section
						 *  
						 */
						
						boolean ignoreThisSection = false;
						SectPr followingSectPr = sectPrs.get(++sectPrIndex);
						if ( followingSectPr.getType()!=null
								     && followingSectPr.getType().getVal().equals("continuous")) {

							log.info("following sectPr is continuous; this section wrapper must include its contents ");
							ignoreThisSection = true;
							
						} 
						
						
						if (ignoreThisSection) {
							// In case there are some headers/footers that apply to both this content and the 
							// content before the continuous sectPr,
							// or that need to get inherited by the section after the continuous sectPr 
							previousHF = new HeaderFooterPolicy(ppr.getSectPr(), previousHF, rels, evenAndOddHeaders);

							
							PgSz pgSzThis = ppr.getSectPr().getPgSz();
							PgSz pgSzNext = followingSectPr.getPgSz();
							if (insertPageBreak( pgSzThis,  pgSzNext)) {
								
								ppr.setPageBreakBefore(new BooleanDefaultTrue());
							}
							//ppr.setSectPr(null); // Don't do this, since we have to process the docx (inc sectPrs) multiple times for a single PDF output
							
						} else {
							currentSectionWrapper = createSectionWrapper(
									ppr.getSectPr(), previousHF, rels, evenAndOddHeaders, 
									++conversionSectionIndex, sectionContent, dummyPageNumbering); 		
							conversionSections.add(currentSectionWrapper);
							previousHF = currentSectionWrapper.getHeaderFooterPolicy();
							sectionContent = new ArrayList<Object>();
							
						}
					}
				}				
			} 
			sectionContent.add(o);
//			System.out.println(XmlUtils.marshaltoString(o, true));
		}
		
		// Section wrapper for the document level sectPr, containing remaining content
		currentSectionWrapper = createSectionWrapper(
				document.getBody().getSectPr(), previousHF, rels, evenAndOddHeaders,
				++conversionSectionIndex, sectionContent, dummyPageNumbering); 		
		conversionSections.add(currentSectionWrapper);
		return conversionSections;
	}
	
	private static boolean insertPageBreak(PgSz pgSzThis, PgSz pgSzNext) {

		boolean insertPageBreak = false;
		
		// If the w:pgSz on the two sections differs, 
		// then Word inserts a page break (ie doesn't treat it as continuous).
		// If no w:pgSz element is present, then Word defaults
		// (presumably to Legal? TODO CHECK. There is no default setting in the docx).
		// Word always inserts a w:pgSz element?

		
		if (pgSzThis!=null && pgSzNext!=null) {
			
			if (pgSzThis.getH().compareTo(pgSzNext.getH())!=0) {
				insertPageBreak = true;
			}
			if (pgSzThis.getW().compareTo(pgSzNext.getW())!=0) {
				insertPageBreak = true;
			}
			
			// Orientation:default is portrait
			boolean portraitThis = true;
			if (pgSzThis.getOrient()!=null) {
				portraitThis=pgSzThis.getOrient().equals(STPageOrientation.PORTRAIT);
			}
			boolean portraitNext = true;
			if (pgSzNext.getOrient()!=null) {
				portraitNext=pgSzNext.getOrient().equals(STPageOrientation.PORTRAIT);
			}
			if (portraitThis!=portraitNext) {
				insertPageBreak = true;									
			}
			
		}
		// TODO: handle cases where one or both pgSz elements are missing,
		// or H or W is missing.
		// Treat pgSz element missing as Legal size?
		return insertPageBreak;
	}
	
	
	private static void removeContentControls(Document document) {

		// First, remove content controls, 
		// since the P could be in a content control.
		// (It is easier to remove content controls, than
		//  to make the code below TraversalUtil based)
		// RemovalHandler is an XSLT-based way of doing this,
		// but here we avoid introducing a dependency on
		// XSLT (Xalan) for PDF output.
		SdtBlockFinder sbr = new SdtBlockFinder();
		new TraversalUtil(document.getContent(), sbr);
		for( int i=sbr.sdtBlocks.size()-1 ; i>=0; i--) {
			// Have to process in reverse order
			// so that parentList is correct for nested sdt
			
			SdtBlock sdtBlock = sbr.sdtBlocks.get(i);
			List<Object> parentList = null;
			if (sdtBlock.getParent() instanceof ArrayList) {
				parentList = (ArrayList)sdtBlock.getParent();
			} else {
				log.error("Handle " + sdtBlock.getParent().getClass().getName());
			}
			int index = parentList.indexOf(sdtBlock);
			parentList.remove(index);
			parentList.addAll(index, sdtBlock.getSdtContent().getContent());				
		}
		
//		if (log.isDebugEnabled()) {
//			log.debug(XmlUtils.marshaltoString(document, true, true));
//		}
		
	}
	
	private static List<SectPr> getSectPrs(Document document) {

		// According to the ECMA-376 2ed, if type is not specified, read it as next page
		// However Word 2007 sometimes treats it as continuous, and sometimes doesn't??	
		// 20130216 Review above comment: !  In the Word UI, the Word "continuous" is shown where it is effective
		// (except 20140517 where the page sizes differ, so that it says continuous but inserts a break!)
		// In the XML, it is stored in the next following sectPr.

		
		// Make a list, so it is easy to look at the following sectPr,
		// which we need to do to handle continuous sections properly
		List<SectPr> sectPrs = new ArrayList<SectPr>();
		for (Object o : document.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {
						sectPrs.add(ppr.getSectPr());
					}
				}				
			} 
		}
		
		if (document.getBody().getSectPr()!=null) {
			// usual case
			sectPrs.add(document.getBody().getSectPr()); 
			
		} else {
			log.debug("No body level sectPr in document");
			
			// OK if the last object is w:p and it contains a sectPr.
			List<Object> all = document.getBody().getContent();
	    	Object last = all.get( all.size()-1 );
	    	if (last instanceof P 
	    			&& ((P) last).getPPr()!=null 
	    				&& ((P) last).getPPr().getSectPr() !=null) {
	    			// ok
				log.debug(".. but last p contains sectPr .. move it"); // so our assumption later about there being a following section is correct

				SectPr thisSectPr = ((P) last).getPPr().getSectPr();
				document.getBody().setSectPr(thisSectPr);
				((P) last).getPPr().setSectPr(null);
				sectPrs.remove(thisSectPr);
	    	
	    	} else {			
				document.getBody().setSectPr(Context.getWmlObjectFactory().createSectPr());
				sectPrs.add(document.getBody().getSectPr()); 
	    	}
		}
		
		return sectPrs;
	}
	

}
