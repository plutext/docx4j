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

import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.common.wrappers.ConversionSectionWrapperFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class  generates the sections depending on the selected features
 *   
 */
public class CreateWrappers  extends ConversionFeatures {
	
	private static Logger log = LoggerFactory.getLogger(CreateWrappers.class);		
	


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
	public static ConversionSectionWrappers process(WordprocessingMLPackage wmlPackage, Set<String> features)  throws Docx4JException {
		
		ConversionSectionWrappers ret = null;
		boolean dummySections = false;
		boolean dummyPageNumbering = false;
		checkParams(wmlPackage, features);
		dummySections = !features.contains(PP_COMMON_CREATE_SECTIONS);
		dummyPageNumbering = !features.contains(PP_COMMON_PAGE_NUMBERING);
		ret = ConversionSectionWrapperFactory.process(wmlPackage, dummySections, dummyPageNumbering);
		return ret;
	}
	

}
