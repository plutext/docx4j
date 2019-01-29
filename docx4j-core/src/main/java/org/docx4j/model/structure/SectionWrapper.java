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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.SectPr;

public class SectionWrapper {
	
	protected static Logger log = LoggerFactory.getLogger(SectionWrapper.class);		
	
	protected SectionWrapper(SectPr sectPr, HeaderFooterPolicy previousHF, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders) {
		// This should work even if sectPr is null
		this.sectPr = sectPr;
		if (sectPr==null) {
			log.warn("No (document level?) sectPr!");
		}
		this.headerFooterPolicy = new HeaderFooterPolicy(sectPr, previousHF, rels, evenAndOddHeaders); 
		
		page = new PageDimensions(sectPr);
		
	}
	
	private PageDimensions page; 
	public PageDimensions getPageDimensions() {
		return page;
	}
	
	private HeaderFooterPolicy headerFooterPolicy;
	/**
	 * @return the headerFooterPolicy
	 */
	public HeaderFooterPolicy getHeaderFooterPolicy() {
		return headerFooterPolicy;
	}

//	/**
//	 * @param headerFooterPolicy the headerFooterPolicy to set
//	 */
//	public void setHeaderFooterPolicy(HeaderFooterPolicy headerFooterPolicy) {
//		this.headerFooterPolicy = headerFooterPolicy;
//	}

	/**
	 * So we can access the stuff which we don't explicitly
	 * handle here.
	 */
	private SectPr sectPr;
	/**
	 * @return the sectPr
	 */
	public SectPr getSectPr() {
		return sectPr;
	}
	public void setSectPr(SectPr sectPr) {
		this.sectPr = sectPr;
	}
	
//    protected List<Object> egBlockLevelElts;
//	
//    public List<Object> getEGBlockLevelElts() {
//        if (egBlockLevelElts == null) {
//            egBlockLevelElts = new ArrayList<Object>();
//        }
//        return this.egBlockLevelElts;
//    }
	
			
}
