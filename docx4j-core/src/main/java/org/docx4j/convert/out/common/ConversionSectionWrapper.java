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
	 * margins, header or footer distances, or own headers and footers (CR-031 phase 3,
	 * {@link #getPartHeaderFooterPolicies()}) differ: for each part at which they change,
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

	/** The own headers and footers of the parts in {@link #getPartVerticalMargins()} whose
	 *  pages show headers or footers other than the page-sequence's (CR-031 phase 3): their
	 *  masters have region names and static content of their own.  @since 17.3.1 */
	protected java.util.Map<Integer, HeaderFooterPolicy> partHeaderFooterPolicies = java.util.Collections.emptyMap();

	/** @since 17.3.1 */
	public java.util.Map<Integer, HeaderFooterPolicy> getPartHeaderFooterPolicies() {
		return partHeaderFooterPolicies;
	}

	/** @since 17.3.1 */
	public void setPartHeaderFooterPolicies(java.util.Map<Integer, HeaderFooterPolicy> partHeaderFooterPolicies) {
		this.partHeaderFooterPolicies = partHeaderFooterPolicies == null
				? java.util.Collections.<Integer, HeaderFooterPolicy>emptyMap() : partHeaderFooterPolicies;
	}

	/** Where this page-sequence is a merged run of continuous sections: the page number each
	 *  part restarts at (w:pgNumType/@w:start), by part number from 1; null where nothing is
	 *  merged.  The page-sequence's own initial-page-number is the first part's where the FO
	 *  renderer restarts the others itself (fork CR-017.2, CR-031 phase 3); otherwise, as
	 *  before, the first restart any part declares.  @since 17.3.1 */
	protected java.util.Map<Integer, Integer> partPageStarts = null;

	/** @since 17.3.1 */
	public java.util.Map<Integer, Integer> getPartPageStarts() {
		return partPageStarts;
	}

	/** @since 17.3.1 */
	public void setPartPageStarts(java.util.Map<Integer, Integer> partPageStarts) {
		this.partPageStarts = partPageStarts;
	}

	/** The space-after, in twips, of the paragraph which carries this section's w:sectPr, where
	 *  that paragraph is empty and so not rendered (Word gives an empty section-break paragraph
	 *  no line); null where it is rendered, or where its space-after is automatic.  The next
	 *  section's first paragraph reduces its space-before by it at the top of the new page, as
	 *  Word does, so the FO exporter passes it on.  @since 17.3.1 */
	protected java.math.BigInteger droppedMarkSpaceAfter = null;

	/** @since 17.3.1 */
	public java.math.BigInteger getDroppedMarkSpaceAfter() {
		return droppedMarkSpaceAfter;
	}

	/** @since 17.3.1 */
	public void setDroppedMarkSpaceAfter(java.math.BigInteger droppedMarkSpaceAfter) {
		this.droppedMarkSpaceAfter = droppedMarkSpaceAfter;
	}

	/** @since 17.3.1 */
	public void setPartVerticalMargins(java.util.Map<Integer, SectPr.PgMar> partVerticalMargins) {
		this.partVerticalMargins = partVerticalMargins == null
				? java.util.Collections.<Integer, SectPr.PgMar>emptyMap() : partVerticalMargins;
	}
}
