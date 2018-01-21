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
package org.docx4j.toc.switches;

import org.docx4j.toc.TocEntry;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses paragraphs formatted with (or based on) all or the specified range of built-in heading styles. 
 * Headings in a style range are specified by text in this switch's field-argument 
 * using the notation specified as for \l, where each integer corresponds to the style 
 * with a style ID of HeadingX (e.g. 1 corresponds to Heading1). 
 * If no heading range is specified, all heading levels used in the document are listed.
 * 
 * Actually Word 2010 does this based on style's w:name (TODO confirm its that or id, not instead of id)
 *
 */
public class OSwitch extends AbstractSwitch {
	
	private static Logger log = LoggerFactory.getLogger(OSwitch.class);				
	

    public static final String ID = "\\o";
    private static final int PRIORITY = 10;


    @Override
    public void process(Style s, SwitchProcessor sp) {
        if(sp.isStyleFound()){
            return;
        }
        TocEntry te = sp.getEntry();

        int level = sp.styleBasedOnHelper.getBasedOnHeading(s);
        
        if(level != -1){
        	
            if(fieldArgument == null){
            	
                te.setEntryLevel(level);
                sp.setStyleFound(true);
                
            } else if (level >= getStartLevel() && level <= getEndLevel()){
            	
                te.setEntryLevel(level);
                sp.setStyleFound(true);
            }
        }
    }

    @Override
    public String parseFieldArgument(String fieldArgument) {
        this.fieldArgument = fieldArgument;
        if(fieldArgument != null){
            if(getStartLevel() == -1 || getEndLevel() == -1){
                return ERROR_NOT_VALID_HEADING_LEVEL;
            }
        }
        return EMPTY;
    }

    @Override
    public boolean hasFieldArgument() {
        return true;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean isStyleSwitch() {
        return true;
    }

}
