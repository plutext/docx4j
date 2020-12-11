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

package org.docx4j.openpackaging.parts;

import java.util.HashMap;
import java.util.Map;

import org.docx4j.dml.FontCollection;
import org.docx4j.dml.FontCollection.Font;
import org.docx4j.dml.TextFont;
import org.docx4j.dml.Theme;
import org.docx4j.fonts.LanguageTagToScriptMapping;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.STTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class ThemePart extends JaxbXmlPartXPathAware<Theme> {
	
	private static Logger log = LoggerFactory.getLogger(ThemePart.class);		
	
	public ThemePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ThemePart() throws InvalidFormatException {
		super(new PartName("/word/theme/theme1.xml"));
		init();
	}
	
	
	public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_THEME));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.THEME);
		
//		setJAXBContext(Context.jc);						
		
		
	}

	private org.docx4j.dml.BaseStyles.FontScheme fontScheme = null;
    
    public org.docx4j.dml.BaseStyles.FontScheme getFontScheme() {
    	if (fontScheme == null) { // ie we haven't done this already
			org.docx4j.dml.Theme theme = (org.docx4j.dml.Theme) this
					.getJaxbElement();
			if (theme.getThemeElements() != null
					&& theme.getThemeElements().getFontScheme() != null) {
				fontScheme = theme.getThemeElements().getFontScheme();
			}
		}
    	return fontScheme;
	}
    
    private Map<String,String> scriptToTypefaceMajor = null;
    private Map<String,String> scriptToTypefaceMinor = null;
    
    private Map<String,String> getScriptToTypefaceMajor() throws Docx4JException {
    	
    	// init
    	if (scriptToTypefaceMajor==null) {
    		scriptToTypefaceMajor = new HashMap<String,String>();
    		
    		if (getMajorFontCollection()!=null) {
				for( Font  f : majorFontCollection.getFont()) {
    					scriptToTypefaceMajor.put(f.getScript(), f.getTypeface() );
    			}
    		}
    	}
    	
    	return scriptToTypefaceMajor;
    	
    }
    private Map<String,String> getScriptToTypefaceMinor() throws Docx4JException {
    	
    	// init
    	if (scriptToTypefaceMinor==null) {
    		scriptToTypefaceMinor = new HashMap<String,String>();
    		
    		if (getMinorFontCollection()!=null) {
				for( Font  f : minorFontCollection.getFont()) {
					scriptToTypefaceMinor.put(f.getScript(), f.getTypeface() );
				}
    		}
    	}
    	
    	return scriptToTypefaceMinor;
    	
    }

    private FontCollection majorFontCollection = null;
    private boolean majorFontCollectionInitialised = false;
    private FontCollection getMajorFontCollection() throws Docx4JException {
    	
    	if (majorFontCollectionInitialised) return majorFontCollection;
    	
		if (this.getContents().getThemeElements()!=null
				&& this.getContents().getThemeElements().getFontScheme()!=null) {
			
			majorFontCollection = this.getContents().getThemeElements().getFontScheme().getMajorFont();
		}
		majorFontCollectionInitialised = true;
		return majorFontCollection;
    }
    
    private FontCollection minorFontCollection = null;
    private boolean minorFontCollectionInitialised = false;
    private FontCollection getMinorFontCollection() throws Docx4JException {
    	
    	if (minorFontCollectionInitialised) return minorFontCollection;
    	
		if (this.getContents().getThemeElements()!=null
				&& this.getContents().getThemeElements().getFontScheme()!=null) {
			
			minorFontCollection = this.getContents().getThemeElements().getFontScheme().getMinorFont();
		}
		minorFontCollectionInitialised = true;
		return minorFontCollection;
    }

    
    /*
      <a:majorFont>
        <a:latin typeface="Cambria"/>
        <a:ea typeface=""/>
        <a:cs typeface=""/>
        <a:font script="Jpan" typeface="ＭＳ ゴシック"/>
		:
      </a:majorFont>
      <a:minorFont>
        <a:latin typeface="Calibri"/>
        <a:ea typeface=""/>
        <a:cs typeface=""/>    
     */
    
    private TextFont majorLatin = null;
	private TextFont majorEastAsian = null;
    private TextFont majorComplexScript = null;
    private TextFont minorLatin = null;
    private TextFont minorEastAsian = null;
    private TextFont minorComplexScript = null;
    
    public TextFont getMajorLatin() throws Docx4JException {
    	if (majorLatin==null
    			&& getMajorFontCollection()!=null) {
    		majorLatin = getMajorFontCollection().getLatin();
    	}
		return majorLatin;
	}

    private TextFont getMajorHighAnsi() throws Docx4JException {
    	return getMajorLatin();
    }    
    
    private TextFont getMajorEastAsian() throws Docx4JException {
    	if (majorEastAsian==null
    			&& getMajorFontCollection()!=null) {
    		majorEastAsian = getMajorFontCollection().getEa();
    	}
		return majorEastAsian;
	}

    private TextFont getMajorComplexScript() throws Docx4JException {
    	if (majorComplexScript==null
    			&& getMajorFontCollection()!=null) {
    		majorComplexScript = getMajorFontCollection().getCs();
    	}
		return majorComplexScript;
	}

    private TextFont getMinorLatin() throws Docx4JException {
    	if (minorLatin==null
    			&& getMinorFontCollection()!=null) {
    		minorLatin = getMinorFontCollection().getLatin();
    	}
		return minorLatin;
	}

    private TextFont getMinorHighAnsi() throws Docx4JException {
    	return getMinorLatin();
    }    
	
    private TextFont getMinorEastAsian() throws Docx4JException {
    	if (minorEastAsian==null
    			&& getMinorFontCollection()!=null) {
    		minorEastAsian = getMinorFontCollection().getEa();
    	}
		return minorEastAsian;
	}

    private TextFont getMinorComplexScript() throws Docx4JException {
    	if (minorComplexScript==null
    			&& getMinorFontCollection()!=null) {
    		minorComplexScript = getMinorFontCollection().getCs();
    	}
		return minorComplexScript;
	}

	private TextFont getTextFontFromTheme(STTheme type) throws Docx4JException {

		if (type.equals(STTheme.MAJOR_EAST_ASIA)) {
			return getMajorEastAsian();
		} else if (type.equals(STTheme.MINOR_EAST_ASIA)) {
			return getMinorEastAsian();
			
		} else if (type.equals(STTheme.MAJOR_ASCII)) {
			return getMajorLatin();
		} else if (type.equals(STTheme.MINOR_ASCII)) {
			return getMinorLatin();

		} else if (type.equals(STTheme.MAJOR_BIDI)) {
			return getMajorComplexScript();
		} else if (type.equals(STTheme.MINOR_BIDI)) {
			return getMinorComplexScript();

		} else if (type.equals(STTheme.MAJOR_H_ANSI)) {
			return getMajorHighAnsi();
		} else if (type.equals(STTheme.MINOR_H_ANSI)) {
			return getMinorHighAnsi();
		}	
		return getMinorLatin();
	}
    
	
	private boolean reportedEmptyMINOR_EAST_ASIA = false;
       
	public String getFontFromTheme(STTheme type) throws Docx4JException {

		TextFont textFont = getTextFontFromTheme(type);
		if (textFont==null) {
			
			log.warn("No font specified for " + type.toString() );
			return null;
			
		} else {
			String typeface = textFont.getTypeface();
    		if (typeface==null) {
    			log.warn("Missing typeface in font for " + type.toString() );
    			return null;
    		} else {
    			if (typeface.trim().length()==0) {
    				
    				if (type.equals(STTheme.MINOR_EAST_ASIA) ) {
    					
    					if (!reportedEmptyMINOR_EAST_ASIA) {
    	        			log.info("Empty typeface in font for " + type.toString() ); 
    	        			reportedEmptyMINOR_EAST_ASIA = true; // suppress extra warnings
    					}
    					
    				} else {
            			log.info("Empty typeface in font for " + type.toString() );     					
    				}
    				
        			// eg <a:ea typeface=""/>
        	        // or <a:cs typeface=""/>
        			return null;
    			}
    			log.debug("'" + typeface + "'");
    			return typeface;
    		}
		}
		
	}
	
	
    public String getFont(STTheme type, CTLanguage themeFontLang) throws Docx4JException {
    	

    	if (themeFontLang==null) {
    		// then the default fonts for each region as specified by the latin, ea, and cs elements should be used
        	log.debug("themeFontLang==null" );    		
    		return getFontFromTheme(type);
    		
    	} else {
    	
        	log.debug(themeFontLang.toString() );
        	log.debug(type.toString() );

        	String lang = this.getLang(themeFontLang, type);
    		if (lang==null) {
        		log.debug("lang==null");
        		return getFontFromTheme(type);
    		}
    		log.debug("--> " + lang);
    		
//    		Throwable t = new Throwable();
//    		t.printStackTrace();
    		
    		// need to convert
    		String script = LanguageTagToScriptMapping.getScriptForLanguageTag(lang);
    		log.debug("--> script: " + script);
    		
    		if (script==null) {
        		return getFontFromTheme(type);
    		} else {
    		
	    		// now, lookup @typeface in the map
	    		String typeface = null;
	    		if (isMajor(type)) {
	    			typeface = getScriptToTypefaceMajor().get(script);
	    		} else {
	    			typeface = getScriptToTypefaceMinor().get(script);	    			
	    		}
	    		
	    		if (typeface==null) {
	        		return getFontFromTheme(type);	    			
	    		} else {
	    			return typeface;
	    		}
    		}
    	}
    }
    
    private boolean isMajor(STTheme type) {
    	
    	if (type == STTheme.MAJOR_ASCII 
    			|| type == STTheme.MAJOR_BIDI  
    			|| type == STTheme.MAJOR_EAST_ASIA 
    			|| type == STTheme.MAJOR_H_ANSI) {
    		return true;
    	} else {
    		return false;
    	}
    	
    }
    
    /**
     * @param themeFontLang
     * @param range
     * @since 3.0.0
     */
    private String getLang(CTLanguage themeFontLang, STTheme range) {
    	
    	/* 
    	 * CTLanguage will be w:themeFontLang element from settings part,  
    	 * for example:
    	 * 
    	 *     <w:themeFontLang w:val="en-US" w:eastAsia="ko-KR"/>
    	 *     
    	 * Per spec, see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/themeFontLang.html    
    	 * These mappings are performed as follows:
    	 * 
			•	For majorAscii/majorHAnsi, locate the font element in the majorFont element
			 	in the theme part for the language specified by the val attribute
			 	
			•	For majorBidi, locate the font element in the majorFont element in the theme part 
				for the language specified by the bidi attribute
				
			•	For majorEastAsia, locate the font element in the majorFont element in the theme part
			 	for the language specified by the eastAsia attribute
			 	
			•	For minorAscii/minorHAnsi, locate the font element in the minorFont element 
				in the theme part for the language specified by the val attribute
				
			•	For minorBidi, locate the font element in the minorFont element in the theme part 
				for the language specified by the bidi attribute
				
			•	For minorEastAsia, locate the font element in the minorFont element in the theme part 
				for the language specified by the eastAsia attribute
				
			If this element is omitted, then the default fonts for each region as specified by the latin, ea, 
			and cs elements should be used.
			
    	 */
    	
		if (range==STTheme.MAJOR_ASCII 
				|| range==STTheme.MINOR_ASCII 
						|| range==STTheme.MAJOR_H_ANSI 
								|| range==STTheme.MINOR_H_ANSI ) {
			if (themeFontLang.getVal()==null) {
				// then the languages for the contents of this run using Latin characters 
				// shall be automatically determined based on their contents using any appropriate method.
				
				return null;
			} else {
				return themeFontLang.getVal();
			}
		} else if (range==STTheme.MAJOR_BIDI || range==STTheme.MINOR_BIDI) // complex script
				 {

			if (themeFontLang.getBidi()==null) {
				// then the languages for the contents of this run using complex script characters 
				// shall be automatically determined based on their contents using any appropriate method.
				
				return null;
			} else {
				return themeFontLang.getBidi();
			}
			
		} else //(range==UnicodeRange.EAST_ASIAN ) 
			{

			if (themeFontLang.getEastAsia()==null) {
				// then the languages for the contents of this run using East Asian characters  
				// shall be automatically determined based on their contents using any appropriate method.
				
				return null;
			} else {
				return themeFontLang.getEastAsia();
			}
			
		}    		
    	
    }
	
}
