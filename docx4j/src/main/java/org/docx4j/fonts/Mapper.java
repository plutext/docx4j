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
 * Maps font names used in the document to 
 * fonts physically available
 * on the system.
 * 
 * So, a mapper per document.
 * If fonts are added to the document
 * (ie fonts introduced into use)
 * then the mapper should be updated
 * to include a mapping for the
 * new font.
 * 
 * There are 2 implementations:
 * 
 * - IndentityPlusMapper, which is best
 *   where most of the fonts used in the 
 *   document are physically present
 *   on the system
 *   
 * - BestMatchingMapper, useful on
 *   Linux and OSX systems on which
 *   Microsoft fonts have not been 
 *   installed.
 *   
 * Whichever one you use, you can 
 * add/remove mappings programmatically
 * to customise to your needs. 
 * 
 * @author jharrop
 *
 */
public abstract class Mapper {
	
	
	protected static Logger log = Logger.getLogger(Mapper.class);

	public Mapper() {
		super();
	}
	
	protected final static Map<String, PhysicalFont> fontMappings;
	public Map<String, PhysicalFont> getFontMappings() {
		return fontMappings;
	}	
		
//	protected final static java.lang.CharSequence target;
//	protected final static java.lang.CharSequence replacement;
    
    // iText style modifiers
    // see core.lowagie.text.pdf.BaseFont
    // iText prepends a "," but that's
    // problematic in xhtmlrenderer
    // (since font-family is a comma separated list.
    public final static String BOLD   = "Bold";
    public final static String ITALIC = "Italic";
    public final static String BOLD_ITALIC = "BoldItalic";
    
    public final static String SEPARATOR = "#";
	
	static {
		fontMappings = Collections.synchronizedMap(new HashMap<String, PhysicalFont>());
//		target = (new String(" ")).subSequence(0, 1);
//		replacement = (new String("")).subSequence(0, 0);
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
	
	
	
//	// Remove spaces and make lower case        
//	public final static String normalise(String realName) {		
//        return realName.replace(target, replacement).toLowerCase();
//	}
	
	
	// For Xalan
	public static String getSubstituteFontXsltExtension(Mapper s, String documentStyleId, String bolditalic, boolean fontFamilyStack) {
		
		return s.getSubstituteFontXsltExtension(documentStyleId, bolditalic, fontFamilyStack);
	}
	
	public String getSubstituteFontXsltExtension(String documentStyleId, 
			String bolditalic, boolean fontFamilyStack) {
		
		log.debug("Trying to insert HTML font-family value for " + documentStyleId);
				
		if (documentStyleId==null) {
			log.error("passed null documentStyleId");
			return "nullInputToExtension";
		}
		
		if (this instanceof IdentityPlusMapper) {	
			
			if (documentStyleId.equals("")) {
				return "EMPTY";
			}
			
			return documentStyleId;
		}
		
		// Try with bold italic modifier		
		PhysicalFont physicalFont = (PhysicalFont)fontMappings.get((documentStyleId + bolditalic));

		if (physicalFont==null) {
			log.error("no mapping for:" + (documentStyleId + SEPARATOR + bolditalic));
			
			// try without  bold italic modifier
			physicalFont = (PhysicalFont)fontMappings.get((documentStyleId));
			
			if (physicalFont==null) {
				log.error("still no good:" + (documentStyleId));
				return "noMappingFor" + (documentStyleId);
			}
		} else {
			
			documentStyleId = documentStyleId + bolditalic;
			
		}
		
		log.info(documentStyleId + " -> " + physicalFont.getName() );
		
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
			
//			return normalise(fontMapping.getPhysicalFont().getFamilyName());
			return physicalFont.getName();
		} else {
//			return normalise(fontMapping.getPhysicalFont().getFamilyName());
			return physicalFont.getName();
		}
		
		/*
		 * We want to return eg "Times New Roman" 
		 * or "Arial Unicode MS" here, ie _with spaces_, since that is 
		 * what xhtmlrender's org.xhtmlrenderer.pdf.ITextFontResolver sets up.
		 * 
		 * 
		 */
		
	}
	
}
