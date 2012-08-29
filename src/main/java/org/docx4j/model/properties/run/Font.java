/*
 *  Copyright 2009, Plutext Pty Ltd.
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
package org.docx4j.model.properties.run;

import org.docx4j.XmlUtils;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STHint;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Font extends AbstractRunProperty {

	public final static String CSS_NAME = "font-family"; 
	public final static String FO_NAME  = "font-family"; 
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	private OpcPackage wmlPackage;
	
	public Font(OpcPackage wmlPackage, RFonts rFonts ) {
		this.setObject(rFonts);
		this.wmlPackage = wmlPackage;
	}

	public Font(CSSValue value) {
		
		debug(CSS_NAME, value);
		log.warn("TODO");
	}
	

	@Override
	public String getCssProperty() {
		
		String font = getPhysicalFont();
		
		if (font!=null) {					
			return composeCss(CSS_NAME, font );
		} else {
			log.warn("No mapping from " + font);
			return CSS_NULL;
		}
		
	}

	private String getPhysicalFont() {
		
		RFonts rFonts = (RFonts)this.getObject();
		
		String font=null;
		if (rFonts.getHint()!=null && rFonts.getHint().equals(STHint.EAST_ASIA) ) {
			
			font = rFonts.getEastAsia();		
			
			if (font==null && wmlPackage instanceof WordprocessingMLPackage) {
				font=((WordprocessingMLPackage)wmlPackage).getMainDocumentPart().getPropertyResolver().getDefaultFontEastAsia();
			}
// TODO	HAnsi, and CS		
//		} else if (rFonts.getHint().equals(STHint.CS) ) {
//				
//				font = rFonts.getCs();		
//				
//				if (font==null && wmlPackage instanceof WordprocessingMLPackage) {
//					font=((WordprocessingMLPackage)wmlPackage).getDefaultFont();

		} else {

			font = rFonts.getAscii();		
			
			if (font==null && wmlPackage instanceof WordprocessingMLPackage) {
				font=((WordprocessingMLPackage)wmlPackage).getDefaultFont();
			}
			
		}
		
		
		return getPhysicalFont(wmlPackage, font);
		
	}

	public static String getPhysicalFont(OpcPackage wmlPackage, String fontName) {

		if (!(wmlPackage instanceof WordprocessingMLPackage)) {
			log.error("Implement me for pptx4j");
			return null;
		}
		PhysicalFont pf = ((WordprocessingMLPackage)wmlPackage).getFontMapper().getFontMappings().get(fontName);
		if (pf!=null) {
			log.debug("Font '" + fontName + "' maps to " + pf.getName() );
			return pf.getName();
		} else {
			log.warn("Font '" + fontName + "' is not mapped to a physical font. " );			
			return null;
		}		
	}	
	
	@Override
	public void setXslFO(Element foElement) {

		String font = getPhysicalFont();
		
		if (font!=null) {					
			foElement.setAttribute(FO_NAME, font );
		} 
		
	}
		

	@Override
	public void set(RPr rPr) {
		rPr.setRFonts((RFonts)this.getObject());
	}
	
	
}
