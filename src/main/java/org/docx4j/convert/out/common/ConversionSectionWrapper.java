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
package org.docx4j.convert.out.common;

import java.util.List;

import org.docx4j.convert.out.common.preprocess.PageNumberInformation;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.SectPr;

public class ConversionSectionWrapper extends SectionWrapper {
	protected List<Object> content = null;
	protected String id = null;
	protected PageNumberInformation pageNumberInformation = null;
	
	public ConversionSectionWrapper(SectPr sectPr, HeaderFooterPolicy previousHF, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, String id, List<Object> content) {
		super(sectPr, previousHF, rels, evenAndOddHeaders);
		this.id = id;
		this.content = content;
	}
	
	public String getId() {
		return id;
	}
	
	public List<Object> getContent() {
		return content;
	}
	
	public void setPageNumberInformation(PageNumberInformation pageNumberInformation) {
		this.pageNumberInformation = pageNumberInformation;
	}
	
	public PageNumberInformation getPageNumberInformation() {
		return pageNumberInformation;
	}
}
