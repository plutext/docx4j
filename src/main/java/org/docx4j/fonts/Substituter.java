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
package org.docx4j.fonts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Substitute fonts used in the document with fonts which are 
 * physically available.
 * 
 * This is useful for:
 * 
 * - PDF output
 * 
 * - other applications which render fonts on the screen
 *   (eg Swing applications)
 * 
 * @author jharrop
 *
 */
public abstract class Substituter {
	
	
	protected static Logger log = Logger.getLogger(Substituter.class);

	public Substituter() {
		super();
	}
	
	protected final static Map<String, FontMapping> fontMappings;
	public Map<String, FontMapping> getFontMappings() {
		return fontMappings;
	}	
	
	
	protected final static java.lang.CharSequence target;
    protected final static java.lang.CharSequence replacement;
    
    public final static String BOLD   = "Bold";
    public final static String ITALIC = "Italic";
    public final static String BOLD_ITALIC = "BoldItalic";
        
	
	static {
		fontMappings = Collections.synchronizedMap(new HashMap<String, FontMapping>());
		target = (new String(" ")).subSequence(0, 1);
		replacement = (new String("")).subSequence(0, 0);
	}
	
	/**
	 * Populate the fontMappings object. We make an entry for each
	 * of the documentFontNames.
	 * 
	 * @param documentFontNames - the fonts used in the document
	 * @param wmlFonts - the content model for the fonts part
	 * @throws Exception
	 */
	public abstract void populateFontMappings(Map documentFontNames, 
			org.docx4j.wml.Fonts wmlFonts ) throws Exception;
	
	
	
	// Remove spaces and make lower case        
	public final static String normalise(String realName) {		
        return realName.replace(target, replacement).toLowerCase();
	}
	
	
	// For Xalan
	public static String getSubstituteFontXsltExtension(Substituter s, String documentStyleId, String bolditalic, boolean fontFamilyStack) {
		
		return s.getSubstituteFontXsltExtension(documentStyleId, bolditalic, fontFamilyStack);
	}
	
	public String getSubstituteFontXsltExtension(String documentStyleId, 
			String bolditalic, boolean fontFamilyStack) {
				
		if (documentStyleId==null) {
			log.error("passed null documentStyleId");
			return "nullInputToExtension";
		}
		
		// Try with bold italic modifier		
		FontMapping fontMapping = (FontMapping)fontMappings.get(normalise(documentStyleId + bolditalic));

		if (fontMapping==null) {
			log.error("no mapping for:" + normalise(documentStyleId + bolditalic));
			
			// try without  bold italic modifier
			fontMapping = (FontMapping)fontMappings.get(normalise(documentStyleId));
			
			if (fontMapping==null) {
				log.error("still no good:" + normalise(documentStyleId));
				return "noMappingFor" + normalise(documentStyleId);
			}
		} else {
			
			documentStyleId = documentStyleId + bolditalic;
			
		}
		
		log.info(documentStyleId + " -> " + fontMapping.getPhysicalFont().getFamilyName() );
		
		if (fontFamilyStack) {
			
			// TODO - if this is an HTML document intended
			// for viewing in a web browser, we need to add a 
			// font-family cascade (since the true type font
			// specified for PDF purposes won't necessarily be
			// present on web browser's system).
			
			// The easiest way to do it might be to just
			// see whether the substitute font is serif or
			// not, and add cascade entries accordingly.
			
			// If we matched it via FontSubstitutions.xml,
			// maybe that file contains an HTML match as well?
			
			// Either way, this stuff should be worked out in
			// populateFontMappings, and added to the 
			// FontMapping objects.
			
			return fontMapping.getPhysicalFont().getFamilyName();
		} else {
			return fontMapping.getPhysicalFont().getFamilyName();
		}
		
	}
	
}
