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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
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
	
	private List<SectionWrapper> sections = new ArrayList<SectionWrapper>(); 
	
	// Consider whether this class should store a reference to the
	// org.docx4j.wml.Document?  Not until there is a demonstrable
	// need.
	
	// At present, objects (eg w:p, w:tbl) don't know which
	// section they are in, and nor does a SectionWrapper
	// know which objects are in it.  
	// We will soon add objects to a SectionWrapper (but not
	// vice versa)
	
	public DocumentModel(WordprocessingMLPackage wordMLPackage) {
		
		RelationshipsPart rels = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
		
		// For now, we only capture the document level one
		
		Document doc = (Document)wordMLPackage.getMainDocumentPart().getJaxbElement();
		SectPr sectPr = doc.getBody().getSectPr();	
		// There might not be a sectPr, but we still add a SectionWrapper to
		// represent the body.
		SectionWrapper sw = new SectionWrapper(sectPr, rels);
		sections.add(sw);
		
	}

	/**
	 * @return the sections
	 */
	public List<SectionWrapper> getSections() {
		return sections;
	}

}
