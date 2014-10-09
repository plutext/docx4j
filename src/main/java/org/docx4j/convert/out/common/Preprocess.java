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
import org.docx4j.convert.out.common.preprocess.CoverPageSectPrMover;
import org.docx4j.convert.out.common.preprocess.FieldsCombiner;
import org.docx4j.convert.out.common.preprocess.FopWorkaroundDisablePageBreakOnFirstParagraph;
import org.docx4j.convert.out.common.preprocess.FopWorkaroundReplacePageBreakInEachList;
import org.docx4j.convert.out.common.preprocess.PageBreak;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.convert.out.common.preprocess.PartialDeepCopy;
import org.docx4j.convert.out.html.ListsToContentControls;
import org.docx4j.events.EventFinished;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class manages the preprocessing functionality for the conversion.<br>
 *  It contains two methods that get called from the conversions:
 *  <ul>
 *  <li>process: does the preprocessing and returns an OpcPackage or a WordprocessingMLPackage</li>
 *  <li>createSections: generates the sections depending on the selected features</li>
 *  </ul> 
 *   
 */
public class Preprocess extends ConversionFeatures {
	
	private static Logger log = LoggerFactory.getLogger(Preprocess.class);		
	
	
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
			if (ret instanceof WordprocessingMLPackage) {
				log.debug("Results of PP_COMMON_DEEP_COPY: " + ((WordprocessingMLPackage)ret).getMainDocumentPart().getXML());
			}
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
	 *  Features processed include, for example: 
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

//		log.debug(wmlPackage.getMainDocumentPart().getXML());		
		
		WordprocessingMLPackage ret = (WordprocessingMLPackage)process((OpcPackage)wmlPackage, features);
		
		StartEvent startEvent = new StartEvent( ret, WellKnownProcessSteps.CONVERT_PREPROCESS );
		startEvent.publish();
		

//		log.debug(ret.getMainDocumentPart().getXML());
	
		if (features.contains(PP_COMMON_COMBINE_FIELDS)) {
			log.debug("PP_COMMON_COMBINE_FIELDS");
			FieldsCombiner.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_COMMON_MOVE_BOOKMARKS)) {
			log.debug("PP_COMMON_MOVE_BOOKMARKS");
			BookmarkMover.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_COMMON_MOVE_PAGEBREAK)) {
			log.debug("PP_COMMON_MOVE_PAGEBREAK");
			PageBreak.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_PDF_COVERPAGE_MOVE_SECTPR)) {
			log.debug("PP_COMMON_COVERPAGE_MOVE_SECTPR");
			CoverPageSectPrMover.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_COMMON_CONTAINERIZATION)) {
			log.debug("PP_COMMON_CONTAINERIZATION");
			Containerization.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_HTML_COLLECT_LISTS)) {
			log.debug("PP_HTML_COLLECT_LISTS");
			ListsToContentControls.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_FIRST_PARAGRAPH)) {
			log.debug("PP_APACHEFOP_DISABLE_PAGEBREAK_FIRST_PARAGRAPH");
			FopWorkaroundDisablePageBreakOnFirstParagraph.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_PDF_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM)) {
			log.debug("PP_APACHEFOP_DISABLE_PAGEBREAK_LIST_ITEM");
			FopWorkaroundReplacePageBreakInEachList.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}
		if (features.contains(PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX)) {
			log.debug("PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX");
			ParagraphStylesInTableFix.process(ret);
//			log.debug(ret.getMainDocumentPart().getXML());
		}

		
		log.debug("Results of preprocessing: " + ret.getMainDocumentPart().getXML());
		
		new EventFinished(startEvent).publish();
		
		
		return ret;
	}

	

}
