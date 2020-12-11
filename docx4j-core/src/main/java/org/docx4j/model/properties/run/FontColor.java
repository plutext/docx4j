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

import java.lang.reflect.Method;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.Color;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class FontColor extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(FontColor.class);		

	public final static String CSS_NAME = "color"; 
	public final static String FO_NAME  = "color"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public FontColor(Color color) {
		this.setObject(color);
	}

	public FontColor(CSSValue value) {

		// PrimitiveType 25 -> RGBCOLOR
		short ignored = 1;

		float fRed;
		float fGreen;
		float fBlue;

		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue) value;
		Color color = Context.getWmlObjectFactory().createColor();
		try {
			fRed = cssPrimitiveValue.getRGBColorValue().getRed()
					.getFloatValue(ignored);
			fGreen = cssPrimitiveValue.getRGBColorValue().getGreen()
					.getFloatValue(ignored);
			fBlue = cssPrimitiveValue.getRGBColorValue().getBlue()
					.getFloatValue(ignored);
			color.setVal(UnitsOfMeasurement.rgbTripleToHex(fRed, fGreen, fBlue));
			
		} catch (UnsupportedOperationException e) {			
        	
		    try {
		    	Class<?> xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.FSColorToHexString");
		        Method rgbToHexMethod = xhtmlImporterClass.getMethod("rgbToHex", CSSPrimitiveValue.class);
		        color.setVal((String)rgbToHexMethod.invoke(null, cssPrimitiveValue));
		    } catch (Exception e2) {
		        log.error("docx4j-XHTMLImport jar not found. Please add this to your classpath.");
				log.error(e2.getMessage(), e2);
				throw e; // same as before
			}  
		}

		this.setObject(color);
	}	
	
	@Override
	public String getCssProperty() {
		
		if (((Color)this.getObject()).getVal()!=null ) {
			return composeCss(CSS_NAME, "#" + ((Color)this.getObject()).getVal());
		} else {
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {
		
		if (((Color)this.getObject()).getVal()!=null ) {
			if (((Color)this.getObject()).getVal().equals("auto")) {
				// set it to black
				foElement.setAttribute(FO_NAME, "black");				
			} else {
				foElement.setAttribute(FO_NAME, "#" + ((Color)this.getObject()).getVal());
			}
		} else {
			//
		}

	}

	@Override
	public void set(RPr rPr) {
		rPr.setColor((Color)this.getObject());
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        //TODO
    }
	
}
