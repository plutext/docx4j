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
		this(sectPr, previousHF, rels, evenAndOddHeaders, id, content, false);
	}

	/** @param startsPage whether a continuous section begins a page of its own, so that
	 *  the headers and footers it declares apply (HeaderFooterPolicy).  @since 17.2.0 */
	public ConversionSectionWrapper(SectPr sectPr, HeaderFooterPolicy previousHF, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, String id, List<Object> content, boolean startsPage) {
		super(sectPr, previousHF, rels, evenAndOddHeaders, startsPage);
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

	/** The page masters this page-sequence's merged continuous sections need beside its own,
	 *  by the number (from 1) of the part whose vertical margins they take; see
	 *  {@link #getPartVerticalMargins()}.  @since 17.3.1 */
	protected java.util.Map<Integer, SectPr.PgMar> partVerticalMargins = java.util.Collections.emptyMap();

	/**
	 * Where this page-sequence is a merged run of continuous sections whose top or bottom
	 * margins, or header or footer distances, differ: for each part at which they change,
	 * the w:pgMar of the part whose values its pages take, keyed by that part's number (1 for
	 * the first).  Word gives a page the vertical margins of the section owning its first line
	 * (CR-031 D1), so each such part gets page masters of its own (named
	 * {@code <id>-p<number>}), chosen by content where the FO renderer can
	 * ({@code fox:page-sequence-master-reference}).  Empty otherwise.
	 *
	 * @since 17.3.1
	 */
	public java.util.Map<Integer, SectPr.PgMar> getPartVerticalMargins() {
		return partVerticalMargins;
	}

	/** @since 17.3.1 */
	public void setPartVerticalMargins(java.util.Map<Integer, SectPr.PgMar> partVerticalMargins) {
		this.partVerticalMargins = partVerticalMargins == null
				? java.util.Collections.<Integer, SectPr.PgMar>emptyMap() : partVerticalMargins;
	}
}
