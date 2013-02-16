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
package org.docx4j.convert.out;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.jaxb.ObjectFactory;
import org.docx4j.model.structure.jaxb.Sections;
import org.docx4j.model.structure.jaxb.Sections.Section;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Document;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgSz;
import org.w3c.dom.Element;

public class ConversionSectionWrappers {
	
	protected static Logger log = Logger.getLogger(ConversionSectionWrappers.class);
	
	protected List<ConversionSectionWrapper> conversionSections = null;
	protected int currentSectionIndex = -1;
	protected ConversionSectionWrapper currentSection = null;
	
	protected ConversionSectionWrappers(List<ConversionSectionWrapper> conversionSections) {
		this.conversionSections = conversionSections;
	}
	
	public static class SdtBlockFinder extends CallbackImpl {

		List<SdtBlock> sdtBlocks = new ArrayList<SdtBlock>();
		
		@Override
		public List<Object> apply(Object o) {

			if (o instanceof SdtBlock) {
				sdtBlocks.add((SdtBlock)o);
			}
			return null;
		}
	}
	
    
	public static ConversionSectionWrappers build(Document doc, WordprocessingMLPackage wmlPackage) {
		
		List<ConversionSectionWrapper> conversionSections = new ArrayList<ConversionSectionWrapper>();
		List<Object> sectionContent = new ArrayList<Object>();
		ConversionSectionWrapper currentSectionWrapper = null;
		RelationshipsPart rels = wmlPackage.getMainDocumentPart().getRelationshipsPart();
		HeaderFooterPolicy previousHF = null;
		BooleanDefaultTrue evenAndOddHeaders = null;				
		int conversionSectionIndex = 0;

		if ((wmlPackage.getMainDocumentPart().getDocumentSettingsPart() != null) &&
			(wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement() != null)) {
			evenAndOddHeaders = wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement().getEvenAndOddHeaders();
		}
		
		// According to the ECMA-376 2ed, if type is not specified, read it as next page
		// However Word 2007 sometimes treats it as continuous, and sometimes doesn't??	
		// 20130216 Review above comment: !  In the Word UI, the Word "continuous" is shown where it is effective.  
		// In the XML, it is stored in the next following sectPr.

		// First, remove content controls, 
		// since the P could be in a content control.
		// (It is easier to remove content controls, than
		//  to make the code below TraversalUtil based)
		SdtBlockFinder sbr = new SdtBlockFinder();
		new TraversalUtil(doc.getContent(), sbr);
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
		
		System.out.println(XmlUtils.marshaltoString(doc, true, true));
		
		// Make a list, so it is easy to look at the following sectPr,
		// which we need to do to handle continuous sections properly
		List<SectPr> sectPrs = new ArrayList<SectPr>();
		for (Object o : doc.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {
						sectPrs.add(ppr.getSectPr());
					}
				}				
			} 
		}
		sectPrs.add(doc.getBody().getSectPr());
		
		int sectPrIndex = 0; // includes continuous ones
		for (Object o : doc.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {

						// If the *following* section is continuous, don't add *this* section
						boolean ignoreThisSection = false;
						SectPr followingSectPr = sectPrs.get(++sectPrIndex);
						if ( followingSectPr.getType()!=null
								     && followingSectPr.getType().getVal().equals("continuous")) {
							
							ignoreThisSection = true;
							
							// If the w:pgSz on the two sections differs, 
							// then Word inserts a page break (ie doesn't treat it as continuous).
							// If no w:pgSz element is present, then Word defaults
							// (presumably to Legal? TODO CHECK. There is no default setting in the docx).
							// Word always inserts a w:pgSz element?

							PgSz pgSzThis = ppr.getSectPr().getPgSz();
							PgSz pgSzNext = followingSectPr.getPgSz();
							
							if (pgSzThis!=null && pgSzNext!=null) {
								
								if (pgSzThis.getH().compareTo(pgSzNext.getH())!=0) {
									ignoreThisSection = false;
								}
								if (pgSzThis.getW().compareTo(pgSzNext.getW())!=0) {
									ignoreThisSection = false;
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
									ignoreThisSection = false;									
								}
								
							}
							// TODO: handle cases where one or both pgSz elements are missing,
							// or H or W is missing.
							// Treat pgSz element missing as Legal size?
						} 
						
						if (ignoreThisSection) {
							// In case there are some headers/footers that get inherited by the next section
							previousHF = new HeaderFooterPolicy(ppr.getSectPr(), previousHF, rels, evenAndOddHeaders);
							
						} else {
							currentSectionWrapper = new ConversionSectionWrapper(
									ppr.getSectPr(), previousHF, rels, evenAndOddHeaders, 
									"s" + Integer.toString(++conversionSectionIndex), 
									sectionContent); 
							conversionSections.add(currentSectionWrapper);
							previousHF = currentSectionWrapper.getHeaderFooterPolicy();
							sectionContent = new ArrayList<Object>();
							
						}
					}
				}				
			} 
			sectionContent.add(o);
			System.out.println(XmlUtils.marshaltoString(o, true));
		}
		
		currentSectionWrapper = new ConversionSectionWrapper(
				doc.getBody().getSectPr(), previousHF, rels, evenAndOddHeaders, 
				"s" + Integer.toString(++conversionSectionIndex), 
				sectionContent); 		
		conversionSections.add(currentSectionWrapper);

		
		return new ConversionSectionWrappers(conversionSections);				
	}

	public Sections createSections() {
		
		ObjectFactory factory = new ObjectFactory();
		
		Sections ret = factory.createSections();
		for (int i=0; i<conversionSections.size(); i++) {
			ret.getSection().add(createSection(factory, conversionSections.get(i)));
		}
		return ret;
	}
    
	private Section createSection(ObjectFactory factory, ConversionSectionWrapper conversionSectionWrapper) {
		
		Section ret = factory.createSectionsSection();
		ret.setName(conversionSectionWrapper.getId());
		for (int i=0; i<conversionSectionWrapper.getContent().size(); i++) {
			// TODO: since the section model knows nothing about WML,
			// we have to marshall each object separately.
			// To fix this, next time wml is generated, include the section model there!
			ret.getAny().add(marshall(conversionSectionWrapper.getContent().get(i)));
		}
		return ret;
	}

	private static Element marshall(Object o) {
		
		try {
			org.w3c.dom.Document w3cDoc = 
				XmlUtils.marshaltoW3CDomDocument(o);
			
			
				/* Force the RelationshipsPart to be marshalled using
				 * the normal non-rels part NamespacePrefixMapper,
				 * since otherwise (because we'd be using 2 namespace
				 * prefix mappers?) we end up with errant xmlns="",
				 * which is wrong and stops Word 2007 from loading the
				 * document.
				 * 
				 * Note that xmlPackage.xsd defines:
				 * 	<xsd:complexType name="CT_XmlData">
						<xsd:sequence>
							<xsd:any processContents="skip" />
						</xsd:sequence>
				 *
				 * Note also that marshaltoString uses 
				 * just the normal non-rels part NamespacePrefixMapper,
				 * so if/when this is marshalled again, that could
				 * have been causing problems as well?? 
				 */
	        return w3cDoc.getDocumentElement();		        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		        
		return null;
		
	}

	public List<ConversionSectionWrapper> getList() {
		return conversionSections;
	}
	
	public void start() {
		currentSectionIndex = -1;
	}
	
	public void next() {
		
		currentSectionIndex++;
		currentSection = null;
		if ((currentSectionIndex >= 0) && (currentSectionIndex < conversionSections.size())) {
			currentSection = conversionSections.get(currentSectionIndex);
		}
		
	}
	
	public ConversionSectionWrapper getCurrentSection() {
		
		if (currentSection != null) 
			return currentSection;
		if (currentSectionIndex < 0) {
			throw new IllegalArgumentException("Trying to access a section, but moveNext wasn't called");
		}
		else if (currentSectionIndex >= conversionSections.size()) {
			throw new IllegalArgumentException("Trying to access more sections than avaiable");
		}
		else {
			throw new IllegalArgumentException("There is no current section"); //?????
		}
	}
}
