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

import java.util.HashMap;
import java.util.Map;

import org.docx4j.model.PropertyResolver;
import org.docx4j.toc.TocEntry;
import org.docx4j.toc.TocHelper;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.PPrBase.OutlineLvl;

/**
 * This switch indicates to consider the outline level set
 * on the paragraph.
 * 
 * That value trumps heading style (eg if outline level is set to
 * body text, a heading won't appear in the outline), 
 * EXCEPT where the outline level is set in the heading style definition
 * (which is ignored - the style name is parsed for the level!).
 * 
 * If style X is based on heading style, and style X has an outline level
 * setting, that setting is considered.
 *
 */
public class USwitch extends AbstractSwitch {

    public static final String ID = "\\u";
    private static final int PRIORITY = 8;
    
    private Map<String, OutlineLvl> knownOutlineLevels = new HashMap<String, OutlineLvl>(); // per docx
    
    private static OutlineLvl LEVEL_9 = new OutlineLvl();
    
    @Override
    public boolean hasFieldArgument() {
        return false;
    }

    @Override
    public void process(Style s, SwitchProcessor sp) {
    	// Not used
    }

    public void process(Style s, SwitchProcessor sp, PPr pPr, OSwitch oSwitch) {
    	
    	// TODO, need actual pPr, since it could have an outline level defined on it!
    	
    	int cutOff=9;
    	if (oSwitch!=null && oSwitch.fieldArgument!=null) {
    		cutOff=oSwitch.getEndLevel();
    	}
    	
    	int level = getOutlineLvl(pPr, sp, s, cutOff); 
    	
        if( level == 9){
            sp.proceed(false);
        } else {
            TocEntry te = sp.getEntry(); // creates it       	
            te.setEntryLevel(level);
        	sp.setStyleFound(true);
        }
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    public int getOutlineLvl(PPr pPr, SwitchProcessor sp, Style s, int cutOff) {
        // Heading 1 is lvl 0
        // There are 9 levels, so 9 will be lvl 8
        // So return 9 for normal text
    	OutlineLvl outlineLvl = null;
    	if (pPr!=null) {
    		outlineLvl = pPr.getOutlineLvl();
    	}
		
    	// If not direct, look in styles
    	if (outlineLvl==null) {

    		if (s == null) return 9;

			// Special case: a Heading style outside the range in the o switch
    		// is never included. That is suppose this is H3, and 
    		// we have \o "1-2".  
//			if (s.getStyleId().startsWith("Heading")) {
		        int hLevel = sp.styleBasedOnHelper.getBasedOnHeading(s);
				if (hLevel>cutOff) {
					return 9;
				}
//			}
    		
    		outlineLvl = knownOutlineLevels.get(s.getStyleId());
    		
    		if (outlineLvl==null) { 
    			
		        PPr effectivePPr = sp.propertyResolver.getEffectivePPr(s.getStyleId());
		        	// that takes care of any unexpected outline level found in a heading style,
		        	// by overwriting it (see fillPPrStack)
	        	outlineLvl = effectivePPr.getOutlineLvl();
	        	if (outlineLvl==null) {
	        		outlineLvl = LEVEL_9;
	        	}
	        	knownOutlineLevels.put(s.getStyleId(), outlineLvl);
    		}
    	}
    	
        if (outlineLvl == null // shouldn't happen
				|| outlineLvl.getVal()==null  // eg LEVEL_9
        		) return 9;

        return outlineLvl.getVal().intValue();
    }    
    
}
