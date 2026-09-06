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
package org.docx4j.convert.out.common.preprocess;

import org.docx4j.wml.CTPageNumber;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.SectPr;

public class PageNumberInformation {
	protected String defaultNumberFormat = null;
	
	protected boolean pagePresent = false;
	protected String pageFormat = null;
	protected int pageStart = -1;
	
	protected boolean numpagesPresent = false;
	protected String numpagesFormat = null;
	
	protected boolean sectionpagesPresent = false;
	protected String sectionpagesFormat = null;
	
	public PageNumberInformation(SectPr sectPr) {
	CTPageNumber pageNumber = (sectPr != null ? sectPr.getPgNumType() : null);
	NumberFormat numberFormat = null;
		if (pageNumber != null) {
			if (pageNumber.getFmt() != null) {
				defaultNumberFormat = pageNumber.getFmt().value();
			}
			if (pageNumber.getStart() != null) {
				pageStart = pageNumber.getStart().intValue();
			}
		}
	}
	
	public boolean isPagePresent() {
		return pagePresent;
	}
	public void setPagePresent(boolean pagePresent) {
		this.pagePresent = pagePresent;
	}
	public boolean hasPageFormat() {
		return isNumberFormat(pageFormat);
	}

	/**
	 * Whether a field's <code>\\*</code> switch names a number format at all.
	 *
	 * <p>Measured against Word 365: a section declaring
	 * <code>&lt;w:pgNumType w:fmt="upperRoman"/&gt;</code> whose footers hold
	 * <code>PAGE</code> and <code>PAGE \\* MERGEFORMAT</code> has Word print
	 * <code>I</code>, <code>II</code>, ... on all 25 pages, where docx4j printed
	 * <code>1</code>: the switch collector had recorded the empty value of the bare
	 * <code>PAGE</code> and the <code>MERGEFORMAT</code> of the other, and either,
	 * being non-null, masked the section's own format.  <code>MERGEFORMAT</code> and
	 * <code>CHARFORMAT</code> say what to do with the field's <em>character</em>
	 * formatting when it is updated and name no number format, so neither overrides
	 * <code>w:pgNumType/@w:fmt</code>.
	 *
	 * @since 17.0.6
	 */
	private static boolean isNumberFormat(String formatSwitch) {
		if (formatSwitch == null) return false;
		String s = formatSwitch.trim();
		return s.length() > 0
				&& !"MERGEFORMAT".equalsIgnoreCase(s)
				&& !"CHARFORMAT".equalsIgnoreCase(s);
	}
	public String getPageFormat() {
		return (hasPageFormat() ? pageFormat : defaultNumberFormat);
	}
	public void setPageFormat(String pageFormat) {
		this.pageFormat = pageFormat;
	}
	public boolean hasPageStart() {
		return pageStart != -1;
	}
	public int getPageStart() {
		return pageStart;
	}
	public void setPageStart(int pageStart) {
		this.pageStart = pageStart;
	}
	public boolean isNumpagesPresent() {
		return numpagesPresent;
	}
	public void setNumpagesPresent(boolean numpagesPresent) {
		this.numpagesPresent = numpagesPresent;
	}
	public boolean hasNumpagesFormat() {
		return isNumberFormat(numpagesFormat);
	}
	public String getNumpagesFormat() {
		return (hasNumpagesFormat() ? numpagesFormat : defaultNumberFormat);
	}
	public void setNumpagesFormat(String numpagesFormat) {
		this.numpagesFormat = numpagesFormat;
	}
	public boolean isSectionpagesPresent() {
		return sectionpagesPresent;
	}
	public void setSectionpagesPresent(boolean sectionpagesPresent) {
		this.sectionpagesPresent = sectionpagesPresent;
	}
	public boolean hasSectionpagesFormat() {
		return isNumberFormat(sectionpagesFormat);
	}
	public String getSectionpagesFormat() {
		return (hasSectionpagesFormat() ? sectionpagesFormat : defaultNumberFormat);
	}
	public void setSectionpagesFormat(String sectionpagesFormat) {
		this.sectionpagesFormat = sectionpagesFormat;
	}
	
	public void reset() {
		defaultNumberFormat = null;
		
		pagePresent = false;
		pageFormat = null;
		pageStart = -1;
		
		numpagesPresent = false;
		numpagesFormat = null;
		
		sectionpagesPresent = false;
		sectionpagesFormat = null;
	}
}