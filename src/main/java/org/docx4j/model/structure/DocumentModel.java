/*
 *  Copyright 2009, Plutext Pty Ltd.
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

package org.docx4j.model.structure;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Document;
import org.docx4j.wml.SectPr;


/**
 * Forgetting about sub documents, a docx
 * is made up of a number of sections,
 * each with their own section properties
 * (which define page dimensions, margins
 * etc).
 * 
 * So it is natural to think of a document
 * in terms of sections. But since in Open XML,
 * all sectPr elements - except the
 * document level one - are hidden in
 * paragraphs, our JAXB object model doesn't 
 * expose this structure.
 * 
 * This class does that. 
 * 
 * @author jharrop
 *
 */
public class DocumentModel {
	
	protected static Logger log = Logger.getLogger(DocumentModel.class);		
	
	private List<SectionWrapper> sections; 	
	private WordprocessingMLPackage wordMLPackage;
	
	// At present, objects (eg w:p, w:tbl) don't know which
	// section they are in, and nor does a SectionWrapper
	// know which objects are in it.  
	// Should we add objects to a SectionWrapper? 
	// PDF via XSL FO pre-processes the document to wrap
	// section containers, but that is only done in the PDF stuff.
	
	public DocumentModel(WordprocessingMLPackage wordMLPackage) {
		
		this.wordMLPackage = wordMLPackage;
		refresh();
	}
	
	/**
	 * If you have added/deleted sections from your WordprocessingMLPackage,
	 * you'll need to call this method in order for the changes to be
	 * reflected in the DocumentModel.
	 */
	public void refresh() {
		
		RelationshipsPart rels = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
				
		Document doc = (Document)wordMLPackage.getMainDocumentPart().getJaxbElement();
		
		HeaderFooterPolicy previousHF = null;
		BooleanDefaultTrue evenAndOddHeaders = null;
		
		if ((wordMLPackage.getMainDocumentPart().getDocumentSettingsPart() != null) &&
			(wordMLPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement() != null)) {
			evenAndOddHeaders = wordMLPackage.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement().getEvenAndOddHeaders();
		}
		sections = new ArrayList<SectionWrapper>();
		for (Object o : doc.getBody().getContent() ) {
			if (o instanceof org.docx4j.wml.P) {
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {
						SectionWrapper sw = new SectionWrapper(
								ppr.getSectPr(), previousHF, rels, evenAndOddHeaders); 
						sections.add(sw);
						previousHF = sw.getHeaderFooterPolicy();
						log.debug( "registered sectpr");						
					}
				}
			}
		}
		
		
		SectPr sectPr = doc.getBody().getSectPr();	
		// There might not be a sectPr, but we still add a SectionWrapper to
		// represent the body.
		SectionWrapper sw = new SectionWrapper(sectPr, previousHF, rels, evenAndOddHeaders);
		sections.add(sw);
		
	}

	/**
	 * @return the sections
	 */
	public List<SectionWrapper> getSections() {
		return sections;
	}

}
