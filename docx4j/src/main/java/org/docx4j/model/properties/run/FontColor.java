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

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.Color;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class FontColor extends AbstractRunProperty {

	public final static String CSS_NAME = "color"; 
	public final static String FO_NAME  = "color"; 
	
	public FontColor(Color color) {
		this.setObject(color);
	}

	public FontColor(CSSValue value) {
		
		// PrimitiveType 25 -> RGBCOLOR
		short ignored = 1;

		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
		float fRed = cssPrimitiveValue.getRGBColorValue().getRed().getFloatValue(ignored); 
		float fGreen = cssPrimitiveValue.getRGBColorValue().getGreen().getFloatValue(ignored); 
		float fBlue = cssPrimitiveValue.getRGBColorValue().getBlue().getFloatValue(ignored); 
		
		Color color = Context.getWmlObjectFactory().createColor();
		color.setVal( UnitsOfMeasurement.rgbTripleToHex(fRed, fGreen, fBlue)  );
		
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
			foElement.setAttribute(FO_NAME, "#" + ((Color)this.getObject()).getVal());
		} else {
			//
		}

	}

	@Override
	public void set(RPr rPr) {
		rPr.setColor((Color)this.getObject());
	}
	
}
