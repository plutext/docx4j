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

import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.Style;

/**
 * Uses paragraphs formatted with (or based on) styles other than the built-in heading styles. 
 * text in this switch's field-argument specifies those styles as a set of comma-separated doublets, 
 * with each doublet being a comma-separated set of style name and table of content level. 
 * \t can be combined with \o.
 *
 */
public class TSwitch extends AbstractSwitch{

    public static final String ID = "\\t";
    private static final int PRIORITY = 9;
    
    private static final String DOCX4J_FIELDS_TOC_SWITCH_T_SEPARATOR = "docx4j.Fields.TOC.SwitchT.Separator";
    private static final String COMMA = ",";

    @Override
    public void process(Style s, SwitchProcessor sp) {
        if(sp.isStyleFound()){
            return;
        }
        TocEntry te = sp.getEntry();
        Map<String, Integer> styleLevelMap = getStyleLevelMap();
        for(String styleMapValue: styleLevelMap.keySet()){
            if( sp.styleBasedOnHelper.isBasedOn(s, styleMapValue)){
                te.setEntryLevel(styleLevelMap.get(styleMapValue));
                sp.setStyleFound(true);
                break;
            }
        }
    }

    @Override
    public boolean isStyleSwitch() {
        return true;
    }

    @Override
    public String parseFieldArgument(String fieldArgument) {
        this.fieldArgument = fieldArgument;
        tSwitchSeparator = Docx4jProperties.getProperty(DOCX4J_FIELDS_TOC_SWITCH_T_SEPARATOR, COMMA);
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

    
    
    
}
