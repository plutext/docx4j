/*
 *  Copyright 2021, Plutext Pty Ltd.
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
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * @author jharrop
 * 
 * @since 8.3.0
 */
public class Spacing extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(Spacing.class);		
	
	public final static String CSS_NAME = "letter-spacing"; 
	public final static String FO_NAME  = "letter-spacing"; 
	
	public String getCssName() {
		return CSS_NAME;
	}
	
	/*
	 *       <w:spacing w:before="480" />  // twips
	 */	
	public Spacing(CTSignedTwipsMeasure ctSignedTwipsMeasure) {
		this.setObject(ctSignedTwipsMeasure);
	}
	
	public Spacing(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;	
		short ignored = 1;
		float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser
		if (fVal==0f) {
			this.setObject(null);
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
			this.setObject(null);
			return;
		} else {
			log.error("No support for unit " + type);
			this.setObject(null);
			return;
		}
		CTSignedTwipsMeasure spacing = Context.getWmlObjectFactory().createCTSignedTwipsMeasure();
		spacing.setVal(BigInteger.valueOf(twip));
		this.setObject( spacing);
		
	}

	@Override
	public String getCssProperty() {
		if (this.getObject()==null) return null; 
		return  composeCss(CSS_NAME, UnitsOfMeasurement.twipToBest(
				((CTSignedTwipsMeasure)this.getObject()).getVal().intValue()) );		
	}
	

	@Override
	public void setXslFO(Element foElement) {
		if (this.getObject()!=null) {
			foElement.setAttribute(FO_NAME, UnitsOfMeasurement.twipToBest(
					((CTSignedTwipsMeasure)this.getObject()).getVal().intValue()) );
		}
	}

	@Override
	public void set(RPr rPr) {
		if (this.getObject()!=null) {
			rPr.setSpacing((CTSignedTwipsMeasure)this.getObject() );
		}
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		// TODO Auto-generated method stub
		
	}


	
}
