/*
 *  Copyright 2013, Plutext Pty Ltd.
 *   
 *  This file is part of the Commercial Edition of docx4j,
 *  which is licensed under the Plutext Commercial License (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  
 *  In particular, this source code is CONFIDENTIAL, and you must ensure it
 *  stays that way. 
 *
 *  You may obtain a copy of the License at 
 *
 *      http://www.plutext.com/license/Plutext_Commercial_License.pdf 
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package org.docx4j.toc.switches;

import org.docx4j.model.PropertyResolver;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.Style;

public class NSwitch extends AbstractSwitch {

    public static final String ID = "\\n";
    private static final int PRIORITY = 7;

    @Override
    public boolean hasFieldArgument() {
        return true;
    }

    @Override
    public void process(Style s, SwitchProcessor sp) {
        TocEntry te = sp.getEntry();
        if(fieldArgument == null){
            sp.setPageNumbers(false);
            te.addPageNumber(false);
            return;
        }
        int entryLevel = te.getEntryLevel();
        if(entryLevel != -1){
            if(!pageNumbers()){
                te.addPageNumber(false);
            }
            if(entryLevel >= getStartLevel() && entryLevel <= getEndLevel()){
                te.addPageNumber(false); // ???
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
    
    public boolean pageNumbers(){
        return fieldArgument != null;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

}
