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
import org.docx4j.wml.CTShd;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class RShading extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(RShading.class);		
	
	public final static String CSS_NAME = "background-color"; 
	public final static String FO_NAME  = "background-color"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public RShading(CTShd shading) {
		this.setObject(shading);
	}
	
	public RShading(CSSValue value) {	

	    CTShd shd = Context.getWmlObjectFactory().createCTShd();

	    // PrimitiveType 25 -> RGBCOLOR
	    short ignored = 1;

	    float fRed;
	    float fGreen;
	    float fBlue;

	    CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue) value;
	    try {
	        fRed = cssPrimitiveValue.getRGBColorValue().getRed()
	                .getFloatValue(ignored);
	        fGreen = cssPrimitiveValue.getRGBColorValue().getGreen()
	                .getFloatValue(ignored);
	        fBlue = cssPrimitiveValue.getRGBColorValue().getBlue()
	                .getFloatValue(ignored);
		    shd.setFill(UnitsOfMeasurement.rgbTripleToHex(fRed, fGreen, fBlue)  );
		    
	    } catch (UnsupportedOperationException e) {
	    	
		    try {
		    	Class<?> xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.FSColorToHexString");
		        Method rgbToHexMethod = xhtmlImporterClass.getMethod("rgbToHex", CSSPrimitiveValue.class);
		        shd.setFill((String)rgbToHexMethod.invoke(null, cssPrimitiveValue));
		    } catch (Exception e2) {
		        log.error("docx4j-XHTMLImport jar not found. Please add this to your classpath.");
				log.error(e2.getMessage(), e2);
				throw e; // same as before
			}  
        }

	    this.setObject( shd  );
	}

	@Override
	public String getCssProperty() {
		
		CTShd shd = ((CTShd)this.getObject());
		
		// TODO
		// STShd styleVal = shd.getVal();  

		if (shd.getColor()!=null &&
				!shd.getColor().equals("auto")) {
			log.warn("TODO support w:shd/@w:color=" + shd.getColor() );
		}
		
		// We just support fill color right now
		if (shd.getFill()!=null &&
				!shd.getFill().equals("auto")) {
			return composeCss(CSS_NAME, "#" + shd.getFill() );
		} else {
			return CSS_NULL;
		}
		
	}


	@Override
	public void setXslFO(Element foElement) {

		CTShd shd = ((CTShd)this.getObject());
		
		// TODO
		// STShd styleVal = shd.getVal();  

		if (shd.getColor()!=null &&
				!shd.getColor().equals("auto")) {
			log.warn("TODO support w:shd/@w:color=" + shd.getColor() );
		}
		
		// We just support fill color right now
		if (shd.getFill()!=null &&
				!shd.getFill().equals("auto")) {
			foElement.setAttribute(FO_NAME, "#" + shd.getFill() );
		} 
	}

	@Override
	public void set(RPr rPr) {
		rPr.setShd((CTShd)this.getObject() );		
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        // TODO
    }
	
}
