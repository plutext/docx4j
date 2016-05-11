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
package org.docx4j.model.properties.table.tc;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.TcPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class Shading extends AbstractTcProperty {
	
	public final static String CSS_NAME = "background-color"; 
	public final static String FO_NAME  = "background-color"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public Shading(CTShd textAlignment) {
		this.setObject(textAlignment);
	}
	
	public Shading(CSSValue value) {	
		
		CTShd shd = Context.getWmlObjectFactory().createCTShd();

		// PrimitiveType 25 -> RGBCOLOR
		short ignored = 1;

		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
		float fRed = cssPrimitiveValue.getRGBColorValue().getRed().getFloatValue(ignored); 
		float fGreen = cssPrimitiveValue.getRGBColorValue().getGreen().getFloatValue(ignored); 
		float fBlue = cssPrimitiveValue.getRGBColorValue().getBlue().getFloatValue(ignored); 
		
		shd.setFill(UnitsOfMeasurement.rgbTripleToHex(fRed, fGreen, fBlue)  );

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
	public void set(TcPr tcPr) {
		tcPr.setShd( (CTShd)this.getObject() );
	}
	
}
