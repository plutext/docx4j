/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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
package org.docx4j.toc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.SdtElement;


/**
 * This is Helper class for working with elements of TOC of docx documents.
 */
public class TocHelper {

    public static final String TOC_INSTRUCTION_MASK = " TOC ";
    public static final String DEFAULT_TOC_INSTRUCTION = " TOC \\o \"1-3\" \\h \\z \\u ";
    
    public static final STTabTlc DEFAULT_TAB_LEADER = STTabTlc.DOT;
    
//    public static final String TOC_HEADING_STYLE = "TOCHeading";
    public static final String HEADING_STYLE = "Heading";
    public static final String TOC_STYLE_MASK = "TOC%s";

    private TocHelper(){
    }

    /**
     * A ContentAccessor-based approach to fetching objects of the specified
     * type from 
     * 
     * @param obj
     * @param toSearch
     * @return
     */
    public static List<Object> getAllElementsFromObject(Object obj, Class<?> toSearch) {
    	
        // TODO FIXME not as thorough as TraversalUtils approach
        // though since Tbl, Tr, Tc are all instance ContentAccessor,
        // at least w:p in table cell will get included as necessary.
    	// Would miss content in a text box.
    	
        List<Object> result = new ArrayList<Object>();

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement<?>) obj).getValue();
        }
        
        if (obj instanceof SdtElement) {
        	obj = ((SdtElement)obj).getSdtContent(); // implements ContentAccessor    			
        }

        if (obj.getClass().equals(toSearch)) {
            result.add(obj);
        } else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementsFromObject(child, toSearch));
            }
        } 
        
        

        return result;
    }



}
