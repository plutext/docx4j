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

import java.math.BigInteger;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class FontSize extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(FontSize.class);		

	public final static String CSS_NAME = "font-size"; 
	public final static String FO_NAME  = "font-size"; 
	
	/* Flying Saucer has a hard-coded default for font-size
	 * in https://github.com/plutext/flyingsaucer/blob/master/src/java/org/docx4j/org/xhtmlrenderer/css/constants/CSSName.java
	 * at line 554, where it defaults to medium.
	 * 
	 * This could be overridden by the default CSS, but here we facilitate 
	 * setting on a per docx basis
	 */
	public static final ThreadLocal<BigInteger> mediumHalfPts = new ThreadLocal<BigInteger>(){
        @Override
        protected BigInteger initialValue()
        {
            return BigInteger.valueOf(22); //11 point for now
        }
    };
		

	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public FontSize(HpsMeasure val) {
		this.setObject(val);
	}

	public FontSize(CSSValue value) {
		
		
		int mediumHP = 22; 
		if (mediumHalfPts.get()!=null) {
			mediumHP = mediumHalfPts.get().intValue();
		}
				
		HpsMeasure hpsMeasure = Context.getWmlObjectFactory().createHpsMeasure();
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
		// Assume size is in points; sanity test.
		if (cssPrimitiveValue.getPrimitiveType()==CSSPrimitiveValue.CSS_IDENT) {
			
			String adjective = cssPrimitiveValue.getStringValue();
			if (adjective.equals("medium")) {
				hpsMeasure.setVal( BigInteger.valueOf( mediumHP) ); 
			} else if (adjective.equals("xx-small")) {
				hpsMeasure.setVal( BigInteger.valueOf( Math.round(0.6*mediumHP) )); 
			} else if (adjective.equals("x-small")) {
				hpsMeasure.setVal( BigInteger.valueOf( Math.round(0.75*mediumHP) )); 
			} else if (adjective.equals("small")) {
				hpsMeasure.setVal( BigInteger.valueOf( Math.round(0.89*mediumHP) )); 
			} else if (adjective.equals("large")) {
				hpsMeasure.setVal( BigInteger.valueOf( Math.round(1.2*mediumHP) )); 
			} else if (adjective.equals("x-large")) {
				hpsMeasure.setVal( BigInteger.valueOf( Math.round(1.5*mediumHP) )); 
			} else if (adjective.equals("xx-large")) {
				hpsMeasure.setVal( BigInteger.valueOf( Math.round(2*mediumHP) )); 
			} else {
				log.warn("TODO Handle FontSize units properly: " + adjective );							
				hpsMeasure.setVal( BigInteger.valueOf( mediumHP) ); 
			}
			this.setObject(hpsMeasure);				
			
		} else if(cssPrimitiveValue.getPrimitiveType() == CSSPrimitiveValue.CSS_PX){
			
		    float pxVal = cssPrimitiveValue.getFloatValue(CSSPrimitiveValue.CSS_PX);
		    int iVal = UnitsOfMeasurement.pxToTwip(pxVal)/10;
		    hpsMeasure.setVal(BigInteger.valueOf(iVal));
		    this.setObject(hpsMeasure);
		    
		} else if (cssPrimitiveValue.getPrimitiveType() == CSSPrimitiveValue.CSS_EMS) {
			
			// TODO: Use the size of the font on the parent element rather than assuming 1em == medium.
			float emVal = cssPrimitiveValue.getFloatValue(CSSPrimitiveValue.CSS_EMS);
			hpsMeasure.setVal( BigInteger.valueOf( Math.round(emVal * mediumHP) ));
		    this.setObject(hpsMeasure);
			
		} else if (cssPrimitiveValue.getPrimitiveType() ==CSSPrimitiveValue.CSS_EXS) {
			
			// TODO: Use the size of the font on the parent element rather than assuming 1ex == 0.5 * medium.
			float exVal = cssPrimitiveValue.getFloatValue(CSSPrimitiveValue.CSS_EXS);
			hpsMeasure.setVal( BigInteger.valueOf( Math.round(0.5 * exVal * mediumHP) ));
		    this.setObject(hpsMeasure);
			
		} else  if (cssPrimitiveValue.getPrimitiveType() ==CSSPrimitiveValue.CSS_PT) {
			
			// CSS_PT points
			short ignored = 1;
			float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser
			int iVal = Math.round(fVal*2);
			
			hpsMeasure.setVal( BigInteger.valueOf(iVal) );
			this.setObject(hpsMeasure);
			
		} else { 
			
			log.error("TODO FontSize Handle units: " + cssPrimitiveValue.getPrimitiveType() );
			debug(CSS_NAME, value);
		}
	}
	
	

	@Override
	public String getCssProperty() {
		
		if (((HpsMeasure)this.getObject())!=null ) {
			float pts = ((HpsMeasure)this.getObject()).getVal().floatValue()/2 ;
			return composeCss(CSS_NAME, pts + "pt" );
		} else {
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {
		
		if (((HpsMeasure)this.getObject())!=null ) {
			float pts = ((HpsMeasure)this.getObject()).getVal().floatValue()/2 ;
			foElement.setAttribute(FO_NAME, pts + "pt" );
		} else {
			//
		}

	}

	@Override
	public void set(RPr rPr) {
		rPr.setSz( (HpsMeasure)this.getObject() );
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        //TODO
    }
	
}
