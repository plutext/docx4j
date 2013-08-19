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

import java.util.Set;
import java.util.TreeSet;

import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.common.preprocess.BookmarkMover;
import org.docx4j.convert.out.common.preprocess.Containerization;
import org.docx4j.convert.out.common.preprocess.ConversionSectionWrapperFactory;
import org.docx4j.convert.out.common.preprocess.DisablePageBreakOnFirstParagraph;
import org.docx4j.convert.out.common.preprocess.FieldsCombiner;
import org.docx4j.convert.out.common.preprocess.PageBreak;
import org.docx4j.convert.out.common.preprocess.PartialDeepCopy;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/** This class manages the preprocessing functionality for the conversion.<br>
 *  It contains two methods that get called from the conversions:
 *  <ul>
 *  <li>process: does the preprocessing and returns an OpcPackage or a WordprocessingMLPackage</li>
 *  <li>createSections: generates the sections depending on the selected features</li>
 *  </ul> 
 *   
 */
public class Preprocess implements ConversionFeatures {
	
	/** This method applies those features in the preprocessing, that may be used with an
	 *  OpcPackage.<br>
	 *  Features processed: 
	 *  <ul>
	 *  <li>PP_COMMON_DEEP_COPY</li>
	 *  </ul> 
	 * 
	 * @param opcPackage, the package that should be preprocessed 
	 * @param features, the selected features
	 * @return a preprocessed OpcPackage
	 * @throws Docx4JException
	 */
	public static OpcPackage process(OpcPackage opcPackage, Set<String> features) throws Docx4JException {
	OpcPackage  ret = opcPackage;
	Set<String> relationshipTypes = null;
		checkParams(opcPackage, features);
		relationshipTypes = createRelationshipTypes(features);
		if (features.contains(PP_COMMON_DEEP_COPY)) {
			ret = PartialDeepCopy.process(opcPackage, relationshipTypes);
		}
		return ret;
	}

	/** Check what parts might be changed by the preprocessing, 
	 *  those parts need to be deep copied.
	 * 
	 * @param features, the selected features
	 * @return the affected parts
	 */
	protected static Set<String> createRelationshipTypes(Set<String> features) {
	Set<String> relationshipTypes = new TreeSet<String>();
		if (features.contains(PP_COMMON_MOVE_BOOKMARKS) || 
			features.contains(PP_COMMON_CONTAINERIZATION) ||
			features.contains(PP_COMMON_COMBINE_FIELDS)) {
			relationshipTypes.add(Namespaces.DOCUMENT);
			relationshipTypes.add(Namespaces.HEADER);
			relationshipTypes.add(Namespaces.FOOTER);
			//those are probably not affected but get visited by the 
			//default TraversalUtil.
			relationshipTypes.add(Namespaces.ENDNOTES);
			relationshipTypes.add(Namespaces.FOOTNOTES);
			relationshipTypes.add(Namespaces.COMMENTS);
		}
		if (features.contains(PP_COMMON_MOVE_PAGEBREAK)) {
			relationshipTypes.add(Namespaces.DOCUMENT);
		}
		return relationshipTypes;
	}

	/** This method applies those features in the preprocessing, that may be used with an
	 *  WordprocessingMLPackage. As the WordprocessingMLPackage is a OpcPackage it will
	 *  call process(OpcPackage).<br>
	 *  Features processed: 
	 *  <ul>
	 *  <li>PP_COMMON_MOVE_BOOKMARKS</li>
	 *  <li>PP_COMMON_MOVE_PAGEBREAK</li>
	 *  <li>PP_COMMON_CONTAINERIZATION</li>
	 *  <li>PP_COMMON_COMBINE_FIELDS</li>
	 *  <li>PP_APACHEFOP_DISABLE_PAGEBREAK_FIRST_PARAGRAPH</li>
	 *  </ul> 
	 * 
	 * @param wmlPackage, the package that should be preprocessed
	 * @param features, the selected features
	 * @return a preprocessed WordprocessingMLPackage
	 * @throws Docx4JException
	 */
	public static WordprocessingMLPackage process(WordprocessingMLPackage wmlPackage, Set<String> features) throws Docx4JException {
	WordprocessingMLPackage ret = (WordprocessingMLPackage)process((OpcPackage)wmlPackage, features);
	
		if (features.contains(PP_COMMON_COMBINE_FIELDS)) {
			FieldsCombiner.process(ret);
		}
		if (features.contains(PP_COMMON_MOVE_BOOKMARKS)) {
			BookmarkMover.process(ret);
		}
		if (features.contains(PP_COMMON_MOVE_PAGEBREAK)) {
			PageBreak.process(ret);
		}
		if (features.contains(PP_COMMON_CONTAINERIZATION)) {
			Containerization.process(ret);
		}
		if (features.contains(PP_APACHEFOP_DISABLE_PAGEBREAK_FIRST_PARAGRAPH)) {
			DisablePageBreakOnFirstParagraph.process(ret);
		}
		return ret;
	}

	/** This method creates the Sections for the conversion. The type of the created sections 
	 *  depend on the selected features.<br> 
	 *  Features processed: 
	 *  <ul>
	 *  <li>PP_COMMON_PAGE_NUMBERING</li>
	 *  <li>PP_COMMON_DUMMY_PAGE_NUMBERING</li>
	 *  <li>PP_COMMON_CREATE_SECTIONS</li>
	 *  <li>PP_COMMON_DUMMY_CREATE_SECTIONS</li>
	 *  </ul> 
	 * 
	 * @param wmlPackage, the package that should be preprocessed
	 * @param features, the selected features
	 * @return the created sections
	 * @throws Docx4JException
	 */
	public static ConversionSectionWrappers createWrappers(WordprocessingMLPackage wmlPackage, Set<String> features)  throws Docx4JException {
	ConversionSectionWrappers ret = null;
	boolean dummySections = false;
	boolean dummyPageNumbering = false;
		checkParams(wmlPackage, features);
		dummySections = !features.contains(PP_COMMON_CREATE_SECTIONS);
		dummyPageNumbering = !features.contains(PP_COMMON_PAGE_NUMBERING);
		ret = ConversionSectionWrapperFactory.process(wmlPackage, dummySections, dummyPageNumbering);
		return ret;
	}
	
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
		//PP_COMMON_DEEP_COPY, isn' required, no check
		//PP_COMMON_MOVE_BOOKMARKS, isn' required, no check
		//PP_COMMON_CONTAINERIZATION, isn' required, no check
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
