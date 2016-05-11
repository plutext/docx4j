package org.docx4j.model.structure;

/*
 * Inspired/converted from org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy,
 *  which
 *  
 *  ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */


import java.util.List;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderFooterPolicy {

	protected static Logger log = LoggerFactory.getLogger(HeaderFooterPolicy.class);	
	
	private HeaderPart firstHeaderActive;
	private HeaderPart firstHeader;  // Need this so it can be copied in next section, even if not used in this one
	private FooterPart firstFooterActive;
	private FooterPart firstFooter;
	
	private HeaderPart evenHeader;
	private FooterPart evenFooter;
	
	private HeaderPart defaultHeader;
	private FooterPart defaultFooter;

	//dummyHeader and dummyFooter are only used when evenAndOddHeaders == true.
	//they should only be used in "read-mode" and never changed
	//they are not added to the rels
	private static Object dummyHeaderFooterMutex = new Object();
	private static HeaderPart dummyHeader;
	private static FooterPart dummyFooter;
	
	/* "same as previous" functionality:
	 * 
	 * When "same as previous" is set, the sectPr Word emits contains 
	 * no w:headerReference at all. Absence of such implies settings 
	 * are inherited from the previous sectPr.
	 * 
	 * In the Word UI, you can change headers in either section, and
	 * see them reflected in the other.
	 * 
	 * This does not apply to a first page header.  That remains 
	 * independent! 
	 * 
	 * If you say you want no header in a sectPr, Word inserts something like:
	 * 
	 *   <w:sectPr><w:headerReference w:type="default" r:id="rId8"/>
	 *   
	 * with that w:hdr containing an empty paragraph. 
	 * 
	 * Note that "same as previous" in header behaves independently from 
	 * footer.
	 * 
	 * 
	 */
	
	private HeaderFooterPolicy() {}
	
	/**
	 * Figures out the policy for the given section,
	 *  and creates any header and footer objects
	 *  as required.
	 */
	public HeaderFooterPolicy(SectPr sectPr, HeaderFooterPolicy previousHF, 
			RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders) 
//		throws Exception
		{
		// Grab what headers and footers have been defined		
		if (sectPr == null) {
			log.error("Passed null sectPr?!");
			return;
		}

		List<CTRel> hdrFtrRefs = null;
		BooleanDefaultTrue titlePage = null;
		
		if (sectPr.getType()!=null 
				&& "continuous".equals(sectPr.getType().getVal())) {
			// If this is a continuous section, use the headers/footers from the previous section!
			log.debug("this is a continuous section");

			if (previousHF!=null) {
				
				// for a continuous sectPr, ignore the stuff in this sectPr, 
				// by taking our settings from previousHF

				firstHeaderActive=previousHF.firstHeaderActive;
				firstHeader=previousHF.firstHeader;  
				firstFooterActive=previousHF.firstFooterActive;
				firstFooter=previousHF.firstFooter;
				
				evenHeader=previousHF.evenHeader;
				evenFooter=previousHF.evenFooter;
				
				defaultHeader=previousHF.defaultHeader;
				defaultFooter=previousHF.defaultFooter;				
				
				return;
			}
		}

		// The usual non-continuous case
		// (or first sectPr in docx is continuous - maybe the docx starts with columns?)

		if (previousHF==null) {
			log.debug("previousHF==null");
			previousHF= new HeaderFooterPolicy();
			
		} 		
		hdrFtrRefs = sectPr.getEGHdrFtrReferences();
		titlePage = sectPr.getTitlePg();
		
		// Headers. 
		// Init from previousHF
		firstHeader   = previousHF.firstHeader;
		if (titlePage!=null && titlePage.isVal() ) {
			firstHeaderActive   = previousHF.firstHeader;
		}
		
		defaultHeader = previousHF.defaultHeader;
		evenHeader    =  previousHF.evenHeader; 
		// and overwrite with whatever we have
		// specific to this sectPr
		setHeaderReferences(hdrFtrRefs, rels, titlePage );
		
		// Now, same for Footers. 
		// Init from previousHF
		firstFooter   = previousHF.firstFooter;
		if (titlePage!=null && titlePage.isVal() ) {
			firstFooterActive   = previousHF.firstFooter;
		}
		defaultFooter = previousHF.defaultFooter;
		evenFooter    =  previousHF.evenFooter; 
		// and overwrite with whatever we have
		// specific to this sectPr
		setFooterReferences(hdrFtrRefs, rels, titlePage );
		
		if ((titlePage != null) && (titlePage.isVal())) {
			if (firstHeaderActive == null) {
				firstHeaderActive = getDummyHeader();
			}
			if (firstFooterActive == null) {
				firstFooterActive = getDummyFooter();
			}
		}
		
		if (evenAndOddHeaders == null) {
			
			log.debug("evenAndOddHeader setting missing; defaults to false");
			
			// per spec, assume false, so use odd only:-
			
			//Any even header/footer will be ignored
			//As the setting is on the document level it is not necessary to
			//keep any defined header/footer for inheritance
			evenHeader = null;
			evenFooter = null;
			
		} else {
			
			log.debug("evenAndOddHeader: "+ evenAndOddHeaders.isVal());
			
			if (evenAndOddHeaders.isVal()) {
				
				// If true, per the spec, use both even and odd
				
				//If there is only a default/odd header/footer present, then 
				//the even header/footer is always a dummy empty header/footer
				if (evenHeader == null) {
					evenHeader = getDummyHeader();
				}
				if (evenFooter == null) {
					evenFooter = getDummyFooter();
				}
				
				// Experimental: 2014 08 01; required for FOPAreaTreeHelper;
				// (without this, where no header is defined in the docx,
				//  xsl-region-before-default is not created)
				if (defaultHeader == null) {
					defaultHeader = getDummyHeader();
				}
				if (defaultFooter == null) {
					defaultFooter = getDummyFooter();
				}
			}
			else {
				// Per spec, use odd only.  Any even header/footer will be ignored
				//As the setting is on the document level it is not necessary to
				//keep any defined header/footer for inheritance
				evenHeader = null;
				evenFooter = null;
			}
		}
	}

	private HeaderPart getDummyHeader() {
		if (dummyHeader == null) {
			createDummyHeaderFooter();
		}
		return dummyHeader;
	}

	private FooterPart getDummyFooter() {
		if (dummyFooter == null) {
			createDummyHeaderFooter();
		}
		return dummyFooter;
	}
	
	

	private void createDummyHeaderFooter() {
		synchronized (dummyHeaderFooterMutex) {
			if (dummyHeader == null) {
				ObjectFactory factory = new ObjectFactory();
				try {
					dummyHeader = new HeaderPart(new PartName("/word/dummyheader.xml"));
					dummyFooter = new FooterPart(new PartName("/word/dummyfooter.xml"));
				} catch (InvalidFormatException e) {
					//should not happen
				}
				dummyHeader.setJaxbElement(factory.createHdr());
				dummyHeader.getJaxbElement().getContent().add(factory.createP());
				dummyFooter.setJaxbElement(factory.createFtr());
				dummyFooter.getJaxbElement().getContent().add(factory.createP());
			}
		}
	}

//	private boolean hasHdrRef(List<CTRel> hdrFtrRefs) {
//		
//		if (hdrFtrRefs==null) return false;
//		for (CTRel rel : hdrFtrRefs) {
//			if (rel instanceof HeaderReference ) return true; 
//		}
//		return false;
//	}
//	private boolean hasFtrRef(List<CTRel> hdrFtrRefs) {
//		
//		if (hdrFtrRefs==null) return false;
//		for (CTRel rel : hdrFtrRefs) {
//			if (rel instanceof FooterReference ) return true; 
//		}
//		return false;
//	}

	private void setHeaderReferences(List<CTRel> hdrFtrRefs, RelationshipsPart rels,
			BooleanDefaultTrue titlePage) {
		
		for (CTRel rel : hdrFtrRefs) {
			String relId = rel.getId();
			log.debug("for h|f relId: " + relId);
			
			Part part = rels.getPart(relId);
			if (rel instanceof HeaderReference  ) {
				
				HeaderReference headerReference = (HeaderReference)rel;
				
				if (headerReference.getType() == HdrFtrRef.FIRST) {
					firstHeader = (HeaderPart)part;
					if (titlePage!=null && titlePage.isVal()) {
						log.debug("setting first page header");
						firstHeaderActive = (HeaderPart)part;
					} 
				} else if (headerReference.getType() == HdrFtrRef.EVEN) {
					log.debug("setting even page header");
					evenHeader =  (HeaderPart)part; 
				}  else {
					log.debug("setting default page header");
					defaultHeader = (HeaderPart)part;
				}
			}
		}
	}
	
	private void setFooterReferences(List<CTRel> hdrFtrRefs, RelationshipsPart rels,
			BooleanDefaultTrue titlePage) {
		
		for (CTRel rel : hdrFtrRefs) {
			String relId = rel.getId();
			log.debug("for h|f relId: " + relId);
			
			Part part = rels.getPart(relId);
			if (rel instanceof FooterReference  ) {
				
				FooterReference footerReference = (FooterReference)rel;
				
				if (footerReference.getType() == HdrFtrRef.FIRST) {
					firstFooter = (FooterPart)part;
					if (titlePage!=null && titlePage.isVal()) {								
						log.debug("setting first page footer");
						firstFooterActive = (FooterPart)part;
					}
				} else if (footerReference.getType() == HdrFtrRef.EVEN) {
					log.debug("setting even page footer");
					evenFooter =  (FooterPart)part; 
				}  else {
					log.debug("setting default page footer");
					defaultFooter = (FooterPart)part;
				}
			}
		}
	}
	
	public HeaderPart getFirstHeader() {
		return firstHeaderActive;
	}
	public FooterPart getFirstFooter() {
		return firstFooterActive;
	}
	public HeaderPart getEvenHeader() {
		return evenHeader;
	}
	public FooterPart getEvenFooter() {
		return evenFooter;
	}
	/** If an even header is present this is the odd header 
	 *  otherwise it is both, even and odd header
	 */
	public HeaderPart getDefaultHeader() {
		return defaultHeader;
	}
	/** If an even footer is present this is the odd footer 
	 *  otherwise it is both, even and odd footer
	 */
	public FooterPart getDefaultFooter() {
		return defaultFooter;
	}

	/**
	 * Get the header that applies to the given
	 *  (1 based) page.
	 * @param pageNumber The one based page number
	 */
	public HeaderPart getHeader(int pageNumber) {
		if(pageNumber == 1 && firstHeaderActive != null) {
			return firstHeaderActive;
		}
		if(pageNumber % 2 == 0 && evenHeader != null) {
			return evenHeader;
		}
		return defaultHeader;
	}
	/**
	 * Get the footer that applies to the given
	 *  (1 based) page.
	 * @param pageNumber The one based page number
	 */
	public FooterPart getFooter(int pageNumber) {
		if(pageNumber == 1 && firstFooterActive != null) {
			return firstFooterActive;
		}
		if(pageNumber % 2 == 0 && evenFooter != null) {
			return evenFooter;
		}
		return defaultFooter;
	}
	
}
