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
package org.docx4j.convert.out.fo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.convert.out.FORenderer;
import org.docx4j.utils.FoNumberFormatUtil;

public class AbstractPlaceholderLookup implements PlaceholderReplacementHandler.PlaceholderLookup {
	
	protected static final String PLACEHOLDER_PREFIX = FORenderer.PLACEHOLDER_PREFIX;
	protected static final String PLACEHOLDER_SUFFIX = FORenderer.PLACEHOLDER_SUFFIX;
	protected static final int PLACEHOLDER_PREFIX_LENGTH = PLACEHOLDER_PREFIX.length();
	protected static final int PLACEHOLDER_SUFFIX_LENGTH = PLACEHOLDER_SUFFIX.length();
			
	/* The only important thing about the dummy value for the first pass, is that it should be 
	 * about the same width as the resulting value.
	 */
	protected static final String FIRST_PASS_DUMMY_VALUE = "00";
	
	protected List<FORenderer.SectionPageInformation> pageNumberInformation = null;
	/* This Map contains the values of the placeholders:
	 * key: The placeholderID (with the prefix and suffix)
	 * value: in the first pass a dummy value, in the second pass the formatted page count
	 */
	protected Map<String, String> placeholderValues = new TreeMap<String, String>();
	
	protected AbstractPlaceholderLookup(List<FORenderer.SectionPageInformation>  pageNumberInformation) {
		this.pageNumberInformation = setupPageNumerInformation(pageNumberInformation);
	}
	
	protected List<FORenderer.SectionPageInformation> setupPageNumerInformation(List<FORenderer.SectionPageInformation> pageNumberInformation) {
	FORenderer.SectionPageInformation item = null;
		placeholderValues.clear();
		for (int i=0; i<pageNumberInformation.size(); i++) {
			item = pageNumberInformation.get(i);
			putValue(item.getDocumentPageCountID(), FIRST_PASS_DUMMY_VALUE);
			putValue(item.getSectionPageCountID(), FIRST_PASS_DUMMY_VALUE);
		}
		return pageNumberInformation;
	}

	protected void putDocumentPageCount(int pageCount) {
	String pageCountValue = null;
	String foFormat = null;
	String lastFoFormat = null;
	FORenderer.SectionPageInformation item = null;
		for (int i=0; i<pageNumberInformation.size(); i++) {
			item = pageNumberInformation.get(i);
			foFormat = item.getDocumentPageCountFoFormat();
			if (!foFormat.equals(lastFoFormat)) {
				pageCountValue = FoNumberFormatUtil.format(pageCount, foFormat);
				lastFoFormat = foFormat;
			}
			putValue(item.getDocumentPageCountID(), pageCountValue);
		}
	}

	protected void putSectionPageCount(int sectionIndex, int pageCount) {
	FORenderer.SectionPageInformation item = pageNumberInformation.get(sectionIndex);
	String pageCountValue = FoNumberFormatUtil.format(pageCount, item.getSectionPageCountFoFormat());
		placeholderValues.put(createPlaceholder(item.getSectionPageCountID()), pageCountValue);
	}

	protected void putValue(String placeholderID, String value) {
		placeholderValues.put(createPlaceholder(placeholderID), value);
	}

	protected String createPlaceholder(String placeholderID) {
		return PLACEHOLDER_PREFIX + placeholderID + PLACEHOLDER_SUFFIX;
	}

	@Override
	public boolean hasPlaceholders(StringBuilder buffer) {
		return (buffer.indexOf(PLACEHOLDER_PREFIX) > -1) ;
	}

	@Override
	public void replaceValues(StringBuilder buffer) {
	int stIdx = buffer.lastIndexOf(PLACEHOLDER_PREFIX);
	int enIdx = -1;
	String value = null;
		while (stIdx > -1) {
			enIdx = buffer.indexOf(PLACEHOLDER_SUFFIX, stIdx + PLACEHOLDER_PREFIX_LENGTH);
			if (enIdx > -1) {
				enIdx += PLACEHOLDER_SUFFIX_LENGTH;
				value = placeholderValues.get(buffer.substring(stIdx, enIdx));
				if (value != null) {//only replace known placeholders
					buffer.replace(stIdx, enIdx, value);
				}
			}
			stIdx = (stIdx > 0 ? buffer.lastIndexOf(PLACEHOLDER_PREFIX, stIdx - 1) : -1);
		}
	}

}
