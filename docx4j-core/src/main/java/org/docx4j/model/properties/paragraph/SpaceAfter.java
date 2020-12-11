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
package org.docx4j.model.properties.paragraph;

import java.math.BigInteger;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Spacing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class SpaceAfter extends AbstractParagraphProperty {
	
	protected static Logger log = LoggerFactory.getLogger(SpaceAfter.class);		
	
	public final static String CSS_NAME = "margin-bottom"; 
	public final static String FO_NAME  = "space-after"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	
	/*
	 *       <w:spacing w:before="480" />  // twips
	 */
	
	public SpaceAfter(BigInteger val) {
		this.setObject(val);
	}
	
	public SpaceAfter(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;	
		short ignored = 1;
		float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser
		if (fVal==0f) {
			this.setObject(BigInteger.ZERO);
			return;
		}

		int twip;
		
		short type = cssPrimitiveValue.getPrimitiveType();
		if (CSSPrimitiveValue.CSS_IN == type) {
			twip = UnitsOfMeasurement.inchToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_MM == type) {
			twip = UnitsOfMeasurement.mmToTwip(fVal);	
		} else if (CSSPrimitiveValue.CSS_PT == type) {
			twip = UnitsOfMeasurement.pointToTwip(fVal);	
		} else if (CSSPrimitiveValue.CSS_EMS == type) {
			// TODO: Don't hardcode 1em == 16px, but make it depend on the paragraph font.
			twip = UnitsOfMeasurement.pxToTwip(16.0f * fVal);
		} else if (CSSPrimitiveValue.CSS_EXS == type) {
			// TODO: Don't hardcode 1ex == 8px, but make it depend on the paragraph font.
			twip = UnitsOfMeasurement.pxToTwip(8.0f * fVal);
		} else if (CSSPrimitiveValue.CSS_PX == type) {
			twip = UnitsOfMeasurement.pxToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_NUMBER == type) {
			log.error("No support for unit: CSS_NUMBER ");
			twip = 0;			
		} else {
			log.error("No support for unit " + type);
			twip = 0;
		}
		this.setObject(BigInteger.valueOf(twip) );
		
	}

	@Override
	public String getCssProperty() {
		return  composeCss(CSS_NAME, UnitsOfMeasurement.twipToBest(
				((BigInteger)this.getObject()).intValue()) );		
	}
	

	@Override
	public void setXslFO(Element foElement) {

		foElement.setAttribute(FO_NAME, UnitsOfMeasurement.twipToBest(
				((BigInteger)this.getObject()).intValue()) );		
	}

	@Override
	public void set(PPr pPr) {
		if (pPr.getSpacing()==  null) {
			Spacing spacing = Context.getWmlObjectFactory().createPPrBaseSpacing();
			pPr.setSpacing(spacing);
		}
		pPr.getSpacing().setAfter((BigInteger)this.getObject() );
	}
	
}
