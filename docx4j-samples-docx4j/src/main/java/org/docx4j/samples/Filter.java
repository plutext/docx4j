/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.samples;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * Remove proof error markup, rsids etc.
 */
public class Filter {

	public static void main(String[] args) throws Exception {

		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/invoice.docx";
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		
		WordprocessingMLPackage wmlPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		// Before .. note attributes w:rsidRDefault="00D15781" w:rsidR="00D15781"
		System.out.println(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		// Apply the filter
		WordprocessingMLPackage.FilterSettings filterSettings = new WordprocessingMLPackage.FilterSettings();
		filterSettings.setRemoveProofErrors(true);
		filterSettings.setRemoveContentControls(true);
		filterSettings.setRemoveRsids(true);
		wmlPackage.filter(filterSettings);
		// Note the filter is deprecated, since its questionable whether this
		// is important enough to live in WordprocessingMLPackage,
		// and in any case probably should be replaced with a TraversalUtil
		// approach (which wouldn't involve marshal/unmarshall, and 
		// so should be more efficient).
		
		// After .. those yucky attributes are all gone.
		System.out.println(XmlUtils.marshaltoString(wmlPackage.getMainDocumentPart().getJaxbElement(), true, true));
				
	}

}
