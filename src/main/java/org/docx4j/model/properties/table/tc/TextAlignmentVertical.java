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

import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.TcPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class TextAlignmentVertical extends AbstractTcProperty {
	
	public final static String CSS_NAME = "vertical-align"; 
	public final static String FO_NAME  = "display-align";  
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	/**
	 * @since 3.0.0
	 */	
	public TextAlignmentVertical() { //Default-Alignment Top
	CTVerticalJc textAlignment = Context.getWmlObjectFactory().createCTVerticalJc();
		textAlignment.setVal(STVerticalJc.TOP);
		this.setObject(textAlignment);
	}
	
	public TextAlignmentVertical(CTVerticalJc textAlignment) {
		this.setObject(textAlignment);
	}
	
	public TextAlignmentVertical(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		CTVerticalJc textAlignment = Context.getWmlObjectFactory().createCTVerticalJc();
		
		if (value.getCssText().toLowerCase().equals("top")) {
			textAlignment.setVal(STVerticalJc.TOP);
		} else if (value.getCssText().toLowerCase().equals("middle")) {
			textAlignment.setVal(STVerticalJc.CENTER);
		} else if (value.getCssText().toLowerCase().equals("bottom")) {
			textAlignment.setVal(STVerticalJc.BOTTOM);
		} else {
			log.warn("How to handle vertical-align: " + value.getCssText()); // eg baseline
			// Default to top or center..
			textAlignment.setVal(STVerticalJc.CENTER);			
		}		

		this.setObject( textAlignment  );
	}

	@Override
	public String getCssProperty() {
		
		STVerticalJc val = ((CTVerticalJc)this.getObject()).getVal();
		if (val == STVerticalJc.TOP || val == STVerticalJc.BOTTOM ) {						
			return composeCss(CSS_NAME, val.value());
		} else if (val == STVerticalJc.CENTER) {
			return composeCss(CSS_NAME, "middle");
		} else {
			// No CSS equibalent for "both"
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {

		STVerticalJc val = ((CTVerticalJc)this.getObject()).getVal();
		if (val == STVerticalJc.TOP ) {						
			foElement.setAttribute(FO_NAME, "before");
		} else if (val == STVerticalJc.CENTER) {
			foElement.setAttribute(FO_NAME, "center");
		} else if (val == STVerticalJc.BOTTOM) {
			foElement.setAttribute(FO_NAME, "after");
		} else  {
			log.warn("How to handle vertical alignment of " + val.value());
		} 		
	}

	@Override
	public void set(TcPr tcPr) {
		tcPr.setVAlign( (CTVerticalJc)this.getObject() );
	}
	
}
