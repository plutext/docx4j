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

import org.docx4j.model.PropertyResolver;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StyleBasedOnHelper {
	
	private static Logger log = LoggerFactory.getLogger(StyleBasedOnHelper.class);				
	
	private PropertyResolver propertyResolver;
    private static final String HEADING_STYLE = "Heading";
	
	public StyleBasedOnHelper(PropertyResolver propertyResolver) {
		this.propertyResolver = propertyResolver;
	}
	
	public boolean isBasedOn(Style thisStyle, String baseStyleId ) {
		
		if (thisStyle.getStyleId().equals(baseStyleId)) {
			return true;
		}
		
        if (thisStyle.getBasedOn()==null) {
        	return false;
        } else {
        	Style baseStyle =propertyResolver.getStyle(thisStyle.getBasedOn().getVal());
        	if (baseStyle==null) {
        		log.error(thisStyle.getStyleId() + " is based on missing " + thisStyle.getBasedOn().getVal());
        		return false;
        	}
        	return isBasedOn(baseStyle, baseStyleId);
        }
		
	}

	/**
	 * returns heading level, or -1 if not based on a heading
	 * @param s
	 * @return
	 */
	public int getBasedOnHeading(Style s) {
		
        int level = -1;
        if(s.getStyleId().startsWith(HEADING_STYLE)){
            level = getLvlFromHeadingStyle(s.getStyleId());
        } else if (s.getName()!=null
        		&& s.getName().getVal()!=null
        		&& s.getName().getVal().startsWith(HEADING_STYLE.toLowerCase()))
        {
        	// Contrary to the spec, http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/TOC.html
        	// Word 2010 uses style name!
        	
            level = getLvlFromHeadingStyle(s.getName().getVal());
        }  
        
        if(level == -1){
            //Probably not built-in style starting with Heading
            // Maybe based on Heading
            if(s.getBasedOn()==null){
            	return -1;
            } else {
                return getBasedOnHeading(propertyResolver.getStyle(s.getBasedOn().getVal()));
            }
        } else {
        	return level;
        }
		
		
	}
	
	
	
    private int getLvlFromHeadingStyle(String style){
    	// Note that this is done using the style ID, not its OutlineLevel,
    	// since Word does it purely on the name of the style!
        int level = -1;
        try {
            level = Integer.parseInt(style.substring(HEADING_STYLE.length(), style.length()).trim());
            if (level <1 || level >9) {
            	// there are only heading 1 to 9 in Word
            	level=-1;
            }
        } catch (NumberFormatException ex){
            //log.debug(style + " - what level is this? ");
        }
        

        return level;
    }
	
}
