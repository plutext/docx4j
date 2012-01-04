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

import org.docx4j.jaxb.Context;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class FontSize extends AbstractRunProperty {

	public final static String CSS_NAME = "font-size"; 
	public final static String FO_NAME  = "font-size"; 

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
				
		HpsMeasure hpsMeasure = Context.getWmlObjectFactory().createHpsMeasure();
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
		// Assume size is in points; sanity test.
		if (cssPrimitiveValue.getPrimitiveType()==CSSPrimitiveValue.CSS_IDENT) {
			// eg 'medium'
			String adjective = cssPrimitiveValue.getStringValue();
			log.warn("TODO Handle FontSize units properly: " + adjective );			
			if (adjective.equals("medium")) {
				hpsMeasure.setVal( BigInteger.valueOf(22) ); //11 point for now
				this.setObject(hpsMeasure);				
			}
			
		} else  if (cssPrimitiveValue.getPrimitiveType()!=CSSPrimitiveValue.CSS_PT) {
			log.error("TODO FontSize Handle units: " + cssPrimitiveValue.getPrimitiveType() );
			debug(CSS_NAME, value);
		} else {
			short ignored = 1;
			float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser
			int iVal = Math.round(fVal*2);
			
			hpsMeasure.setVal( BigInteger.valueOf(iVal) );
			this.setObject(hpsMeasure);
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
	
}
