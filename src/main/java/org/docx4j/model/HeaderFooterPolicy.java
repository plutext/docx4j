package org.docx4j.model;

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


import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.Document;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.SectPr;
import org.w3c.dom.Node;

public class HeaderFooterPolicy {

	protected static Logger log = Logger.getLogger(HeaderFooterPolicy.class);	
	
	private HeaderPart firstHeader;
	private FooterPart firstFooter;
	
	private HeaderPart evenHeader;
	private FooterPart evenFooter;
	
	private HeaderPart defaultHeader;
	private FooterPart defaultFooter;
	
	
	/**
	 * Figures out the policy for the given document,
	 *  and creates any header and footer objects
	 *  as required.
	 */
	public HeaderFooterPolicy(WordprocessingMLPackage wordmlPackage) 
//		throws Exception
		{
		// Grab what headers and footers have been defined
		// For now, we don't care about different sectPr
		
		Document doc = (Document)wordmlPackage.getMainDocumentPart().getJaxbElement();
		
		SectPr sectPr = doc.getBody().getSectPr();
		for (CTRel rel : sectPr.getEGHdrFtrReferences()) {
			
			String relId = rel.getId();
			
			Part part = wordmlPackage.getMainDocumentPart().getRelationshipsPart().getPart(relId);
			
			if (rel instanceof HeaderReference  ) {
				
				HeaderReference headerReference = (HeaderReference)rel;
				
				if (headerReference.getType() == HdrFtrRef.FIRST) {
					log.debug("setting first page header");
					firstHeader = (HeaderPart)part;
				} else if (headerReference.getType() == HdrFtrRef.EVEN) {
					log.debug("setting even page header");
					evenHeader =  (HeaderPart)part; 
				}  else {
					log.debug("setting default page header");
					defaultHeader = (HeaderPart)part;
				}
			} else if (rel instanceof FooterReference  ) {
				
				FooterReference footerReference = (FooterReference)rel;
				
				if (footerReference.getType() == HdrFtrRef.FIRST) {
					log.debug("setting first page footer");
					firstFooter = (FooterPart)part;
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
		return firstHeader;
	}
	public FooterPart getFirstFooter() {
		return firstFooter;
	}
	/**
	 * Returns the odd page header. This is
	 *  also the same as the default one...
	 */
	public HeaderPart getOddHeader() {
		return defaultHeader;
	}
	/**
	 * Returns the odd page footer. This is
	 *  also the same as the default one...
	 */
	public FooterPart getOddFooter() {
		return defaultFooter;
	}
	public HeaderPart getEvenHeader() {
		return evenHeader;
	}
	public FooterPart getEvenFooter() {
		return evenFooter;
	}
	public HeaderPart getDefaultHeader() {
		return defaultHeader;
	}
	public FooterPart getDefaultFooter() {
		return defaultFooter;
	}

	/**
	 * Get the header that applies to the given
	 *  (1 based) page.
	 * @param pageNumber The one based page number
	 */
	public HeaderPart getHeader(int pageNumber) {
		if(pageNumber == 1 && firstHeader != null) {
			return firstHeader;
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
		if(pageNumber == 1 && firstFooter != null) {
			return firstFooter;
		}
		if(pageNumber % 2 == 0 && evenFooter != null) {
			return evenFooter;
		}
		return defaultFooter;
	}
	
	// -------------------------------------------------------------
	// XSLT extension functions, used by docx2fo.xslt
	// and DocX2Html.xslt
	
	public static boolean hasFirstHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getFirstHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getFirstFooter() ==null? false : true);     		
	}
	public static boolean hasEvenOrOddHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getOddHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getOddFooter() ==null && 
				wordmlPackage.getHeaderFooterPolicy().getEvenHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getEvenFooter() ==null? false : true);     		
	}
	public static boolean hasEvenHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getEvenHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getEvenFooter() ==null? false : true);     		
	}
	public static boolean hasOddHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getOddHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getOddFooter() ==null ? false : true);     		
	}
	public static boolean hasDefaultHeaderOrFooter(WordprocessingMLPackage wordmlPackage) {    		
		return (wordmlPackage.getHeaderFooterPolicy().getDefaultHeader()==null && 
				wordmlPackage.getHeaderFooterPolicy().getDefaultFooter() ==null ? false : true);     		
	}

	public static boolean hasFirstHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getFirstHeader() == null ? false
				: true);
	}

	public static boolean hasOddHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getOddHeader() == null ? false
				: true);
	}

	public static boolean hasEvenHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getEvenHeader() == null ? false
				: true);
	}

	public static boolean hasDefaultHeader(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getDefaultHeader() == null ? false
				: true);
	}

	public static boolean hasFirstFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getFirstFooter() == null ? false
				: true);
	}

	public static boolean hasOddFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getOddFooter() == null ? false
				: true);
	}

	public static boolean hasEvenFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getEvenFooter() == null ? false
				: true);
	}

	public static boolean hasDefaultFooter(WordprocessingMLPackage wordmlPackage) {
		return (wordmlPackage.getHeaderFooterPolicy().getDefaultFooter() == null ? false
				: true);
	}

	public static Node getFirstHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getFirstHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getFirstFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getFirstFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getOddHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getOddHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getOddFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getOddFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getEvenHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getEvenHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getEvenFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getEvenFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

	public static Node getDefaultHeader(WordprocessingMLPackage wordmlPackage) {

		Hdr hdr = (Hdr) wordmlPackage.getHeaderFooterPolicy()
				.getDefaultHeader().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(hdr);

	}

	public static Node getDefaultFooter(WordprocessingMLPackage wordmlPackage) {

		Ftr ftr = (Ftr) wordmlPackage.getHeaderFooterPolicy()
				.getDefaultFooter().getJaxbElement();
		return XmlUtils.marshaltoW3CDomDocument(ftr);

	}

}
