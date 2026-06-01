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

import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.OutlineLvl;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * paragraph oUtline switch
 * 
 * This switch indicates to consider the outline level set
 * on the paragraph.
 * 
 * Old (Word 2007?): That value trumps heading style (eg if outline level is set to
 * body text, a heading won't appear in the outline), 
 * EXCEPT where the outline level is set in the heading style definition
 * (which is ignored - the style name is parsed for the level!).
 * 
 * 2025 07:  Current Word Version 2506 (Build 18925.20076), Office 2013 and Word 2010
 * (14.0.7194.5000) won't let you set an outline level 
 * on P with a heading style (tested heading 1).  And if you open a docx eg  
 * 
 *    <w:pPr>
        <w:pStyle w:val="Heading1"/>
        <w:outlineLvl w:val="4"/>
      </w:pPr>      
 * 
 * it removes the w:outlineLvl setting. So nowadays this conflict is theoretical. 
 * 
 * Also, the Word UI won't allow you to build a ToC from arbitrary styles and outline level; 
 * it is XOR.
 * 
 * But if you manually construct such a TOC, then outline level 
 * trumps style? <w:instrText xml:space="preserve"> TOC \o "1-3" \h \z \t "MyStyle,1" \\u </w:instrText> 
 * Word seems to get confused by such a TOC definition
 * (it ignores certain paragraphs which by style should be in the TOC!). 
 * 
 * Since I can't readily construct a TOC in which the switches conflict,
 * it is not easy to determine whether the switches are applied in the
 * other in which they appear, or by priority.  (But nor does it much matter?) 
 * 
 * Old: If style X is based on heading style, and style X has an outline level
 * setting, that setting is considered.
 *
 */
public class USwitch extends SelectorSwitch {

	private static Logger log = LoggerFactory.getLogger(USwitch.class);					
	
    public static final String ID = "\\u";
    private static final int PRIORITY = 8;
    
    private Map<String, OutlineLvl> knownOutlineLevels = new HashMap<String, OutlineLvl>(); // per docx
    
    private static OutlineLvl LEVEL_9 = new OutlineLvl();
    
    @Override
    public boolean hasFieldArgument() {
        return false;
    }

    @Override
    public void process(Style s, SwitchProcessorInterface sp) {
    	// Not used
    }

    public void process(Style s, SwitchProcessorInterface sp, PPr pPr, OSwitch oSwitch) throws CyclicStylesException {
    	
    	// TODO, need actual pPr, since it could have an outline level defined on it!
    	
    	int cutOff=9;
    	if (oSwitch!=null && oSwitch.fieldArgument!=null) {
    		cutOff=oSwitch.getEndLevel();
    	}
    	
    	int level = getOutlineLvl(pPr, sp, s, cutOff); 
    	if (log.isDebugEnabled()) {
    		log.debug("outline level " + level);
    	}
    	
        if( level == 9){
            sp.setSelected(false);  // this is the only case where a switch can cause a P to be excluded 
        } else {
            TocEntry te = sp.getEntry(); // creates it       	
            te.setEntryLevel(level);
        	sp.setSelected(true);
        }
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    public int getOutlineLvl(PPr pPr, SwitchProcessorInterface sp, Style s, int cutOff) throws CyclicStylesException {
        // Heading 1 is lvl 0
        // There are 9 levels, so 9 will be lvl 8
        // So return 9 for normal text
    	OutlineLvl outlineLvl = null;
    	if (pPr!=null) {
    		outlineLvl = pPr.getOutlineLvl();
//        	log.debug("outline level from ppr" );
    	}
		
    	// If not direct, look in styles
    	if (outlineLvl==null) {

    		if (s == null) return 9;

			// Special case: a Heading style outside the range in the o switch
    		// is never included. That is suppose this is H3, and 
    		// we have \o "1-2".  
//			if (s.getStyleId().startsWith("Heading")) {
		        int hLevel = sp.getStyleBasedOnHelper().getBasedOnHeading(s);
//	        	log.debug("hlevel " +  hLevel);
		        
				if (hLevel>cutOff) {
					return 9;
				}
//			}
    		
    		outlineLvl = knownOutlineLevels.get(s.getStyleId());
    		
    		if (outlineLvl==null) { 
    			
		        PPr effectivePPr = sp.getPropertyResolver().getEffectivePPr(s.getStyleId());
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
