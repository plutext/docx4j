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

import java.util.Set;

import org.docx4j.openpackaging.packages.OpcPackage;

/** This interface contains flags that get passed to the conversion process.
 *  <ul>
 *  <li>PP_xx : Functions that get done in the preprocessing step</li>
 *  </ul> 
 *  <ul>
 *  <li>xx_COMMON_xx: Common functions, may be applied to HTML and PDF conversion.</li>
 *  <li>xx_HTML_xx: HTML functions, may be applied only to HTML conversion.</li>
 *  <li>xx_PDF_xx: PDF functions, may be applied only to PDF conversion.</li>
 *  </ul>
 */
public abstract class ConversionFeatures {
	
	/** During the conversion the document might be changed. If the caller continues to 
	 *  use the document after it, a deep copy should be done. This is not needed if 
	 *  the document gets discarded after the conversion process.
	 */
	public static final String PP_COMMON_DEEP_COPY = "pp.common.deepcopy";

	/** The conversion is only able to handle bookmarks that are in a paragraph. This 
	 *  flag that bookmarks are in paragraphs. This is not needed if 
	 *  the document doesn't contain bookmarks or page references.
	 */
	public static final String PP_COMMON_MOVE_BOOKMARKS = "pp.common.movebookmarks";

	/** Word might put some pagebreaks in a paragraph. In the conversion process the 
	 *  pagebreaks need to be at the beginning of a paragraph. This is not needed if 
	 *  the document doesn't contain explicit pagebreaks. 
	 */
	public static final String PP_COMMON_MOVE_PAGEBREAK = "pp.common.movepagebreak";
	
	/** Word combines the borders and background colors of two (or more) continuous paragraphs. 
	 *  This flag simulates this behaviour. This is not needed if paragraphs dont have a 
	 *  border or background color defined. 
	 */
	public static final String PP_COMMON_CONTAINERIZATION = "pp.common.containerization";

	/** Word may save a field (date, page numbers, references...) in two formats: a simple one
	 *  and a complex one. The conversion process only knows about the simple ones. This step
	 *  converts the complex fields (if possible) to simple ones. This step is only required
	 *  if the document contains page references.  
	 */
	public static final String PP_COMMON_COMBINE_FIELDS = "pp.common.combinefields";

	/** The page number formatting might be stored in different places, the section properties and the 
	 *  field definition. If the document contains page count fields (NUMPAGES, SECTIONPAGES) it might
	 *  be necessary to do a two pass conversion. This step collects the necessary information.  
	 *  A two pass conversion will only be done in PDF. This step isn't necessary if no page numbers are
	 *  used or no custom formatting has been applied to the page number field and if no page count fields 
	 *  got used.<br>
	 *  If this step is omitted then <code>PP_COMMON_DUMMY_PAGE_NUMBERING</code> will be used.
	 */
	public static final String PP_COMMON_PAGE_NUMBERING = "pp.common.pagenumbering";

	/** This step defines some dummy page number information. The required data will be taken only 
	 *  from the section properties. In PDF the conversion will always be one pass. The use of 
	 *  <code>PP_COMMON_PAGE_NUMBERING</code> in HTML doesn't offer any advantage. 
	 */
	public static final String PP_COMMON_DUMMY_PAGE_NUMBERING = "pp.common.dummypagenumbering";
	
	/** In PDF the conversion process needs to break up the document into the defined sections to handle
	 *  such things as headers and footers. This step does this processing. If the document only contains
	 *  one section this processing isn't necessary. In the conversion to HTML the document will be treated 
	 *  as if it only has one section, therefore this step isn't needed in HTML.<br>
	 *  If this step is omitted then <code>PP_COMMON_DUMMY_CREATE_SECTIONS</code> will be used.
	 */
	public static final String PP_COMMON_CREATE_SECTIONS = "pp.common.createsections";

	/** In HTML the conversion process can create lists (OL, UL). 
	 *  This step prepares for that, by inserting content controls around list items.
	 */
	public static final String PP_HTML_COLLECT_LISTS = "pp.html.collectlists";
	
	/** This step defines only one section for the complete document. The required data will be taken 
	 *  from the body section pointers. In PDF the document will be treated as having only one section. 
	 *  The use of <code>PP_COMMON_CREATE_SECTIONS</code> in HTML doesn't offer any advantage. 
	 */
	public static final String PP_COMMON_DUMMY_CREATE_SECTIONS = "pp.common.dummycreatesections";

	/** This step removes a pagebreak before the first paragraph of the document. This is a 
	 * workaround for https://issues.apache.org/bugzilla/show_bug.cgi?id=54094  
     * You can disable this step if you are using FOP post 1.1 or another FO renderer.
	 */
	public static final String PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_FIRST_PARAGRAPH = "pp.apachefop.disablepagebreakfirstparagraph";

	/** This step removes a pagebreak before the first list item in each list. This is a 
	 * workaround for https://issues.apache.org/bugzilla/show_bug.cgi?id=54094  
     * You can disable this step if you are using FOP post 1.1 or another FO renderer.
	 * @since 3.0.1
	 */
	public static final String PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM = "pp.apachefop.disablepagebreaklistitem";
	
	/** If the docx contains a cover page which starts with a sectPr, this step moves the
	 * sectPr to a new w:p inserted after.  This prevents the generation of an empty fo:flow.
	 * @since 3.0.1
	 */
	public static final String PP_PDF_COVERPAGE_MOVE_SECTPR = "pp.common.coverpagemovesectpr";
	
	/**
	 * In a cell, a paragraph uses the table's paragraph properties,
	 * plus the relevant paragraph style (Normal, by default).
	 * The relevant paragraph style trumps the values from the
	 * table's paragraph properties, so that would mean giving
	 * the doc defaults (which we've made part of our styles) priority
	 * over the table's paragraph properties, which is wrong.
	 * TO avoid this, this preprocessor creates a new style, which encapsulates the
	 * paragraph style, with DocDefaults given lower priority 
	 * than table style.  This created style has no w:basedOn setting.
	 * This preprocessor is required if paragraphs in tables are being styled incorrectly.
	 * It wouldn't be required but for our DocDefaults styles hack!
	 * @since 3.0.2
	 */
	public static final String PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX = "pp.common.tbl-p-style-fix";

	
	/** Default features, that get applied to a PDF conversion
	 */
	public static final String[] DEFAULT_PDF_FEATURES = {
		PP_COMMON_DEEP_COPY, 
		PP_COMMON_MOVE_BOOKMARKS,
		PP_COMMON_MOVE_PAGEBREAK,
		PP_PDF_COVERPAGE_MOVE_SECTPR, 
		PP_COMMON_CONTAINERIZATION,
		PP_COMMON_COMBINE_FIELDS,
		PP_COMMON_PAGE_NUMBERING,
		PP_COMMON_CREATE_SECTIONS,
		PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_FIRST_PARAGRAPH,
		PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX // 3.0.2 helps a bit (not as much as for HTML)
		, PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM // experimental in 3.0.1
	};

	/** Default features, that get applied to a HTML conversion
	 */
	public static final String[] DEFAULT_HTML_FEATURES = {
		PP_COMMON_DEEP_COPY, 
		PP_COMMON_MOVE_BOOKMARKS,
		PP_COMMON_MOVE_PAGEBREAK,
		PP_COMMON_CONTAINERIZATION,
		PP_HTML_COLLECT_LISTS, // post 3.2.1; implemented in via XSLT only; requires SdtToListSdtTagHandler to be configured
		PP_COMMON_COMBINE_FIELDS,
		PP_COMMON_DUMMY_PAGE_NUMBERING,
		PP_COMMON_DUMMY_CREATE_SECTIONS,
		PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX // 3.0.2
	};
	
	/** Check the package and requested features and append defaults if necessary
	 * 
	 * @param opcPackage
	 * @param features
	 */
	protected static void checkParams(OpcPackage opcPackage, Set<String> features) {
		if (opcPackage == null) {
			throw new IllegalArgumentException("The passed opcPackage is null.");
		}
		if (features == null) {
			throw new IllegalArgumentException("The set of the features is null.");
		}
		//PP_COMMON_DEEP_COPY, isn't required, no check
		//PP_COMMON_MOVE_BOOKMARKS, isn't required, no check
		//PP_COMMON_CONTAINERIZATION, isn't required, no check
		//PP_COMMON_COMBINE_FIELDS is required if PP_COMMON_PAGE_NUMBERING is selected
		if (features.contains(PP_COMMON_PAGE_NUMBERING)) {
			features.add(PP_COMMON_COMBINE_FIELDS);
		}
		//either PP_COMMON_PAGE_NUMBERING or PP_COMMON_DUMMY_PAGE_NUMBERING (Default) is required
		if (!features.contains(PP_COMMON_PAGE_NUMBERING)) {
			features.add(PP_COMMON_DUMMY_PAGE_NUMBERING);
		}
		//either PP_COMMON_CREATE_SECTIONS or PP_COMMON_DUMMY_CREATE_SECTIONS (Default) is required
		if (!features.contains(PP_COMMON_CREATE_SECTIONS)) {
			features.add(PP_COMMON_DUMMY_CREATE_SECTIONS);
		}
	}	
}
