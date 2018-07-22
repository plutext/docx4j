/*
 *  Copyright 2018, Plutext Pty Ltd.
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
import org.docx4j.wml.TextDirection;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * @since 6.0.0
 */	
public class TextDir extends AbstractTcProperty {
	
	// see http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ST_TextDirection.html
	
	public final static String CSS_NAME = "transform"; // eg transform: rotate(90deg);
	public final static String FO_NAME  = "reference-orientation";  
	
	private final static String DEFAULT_LEFT_TO_RIGHT = "lrTb";
			
	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public TextDir() { 
		TextDirection textDirection = Context.getWmlObjectFactory().createTextDirection();
		textDirection.setVal(DEFAULT_LEFT_TO_RIGHT);
		this.setObject(textDirection);
	}
	
	public TextDir(org.docx4j.wml.TextDirection textDirection) {
		this.setObject(textDirection);
	}
	
	public TextDir(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		TextDirection textDirection = Context.getWmlObjectFactory().createTextDirection();
		
		if (value.getCssText().toLowerCase().equals("rotate(0deg)")) {
			textDirection.setVal(DEFAULT_LEFT_TO_RIGHT);
		} else if (value.getCssText().toLowerCase().equals("rotate(270deg)")) {
			textDirection.setVal("btLr");
		} else if (value.getCssText().toLowerCase().equals("rotate(90deg)")) {
			textDirection.setVal("tbRl");
		} else {
			log.warn("How to handle transform: " + value.getCssText()); 
			textDirection.setVal(DEFAULT_LEFT_TO_RIGHT);			
		}		

		this.setObject( textDirection  );
	}

	@Override
	public String getCssProperty() {
		
		String val = ((TextDirection)this.getObject()).getVal();
		if (("btLr").equals(val) ) {						
			return composeCss(CSS_NAME, "rotate(270deg)");
			
		} else if (("lrTb").equals(val) ) {
			return composeCss(CSS_NAME, "rotate(0deg)");
		} else if (("lrTbV").equals(val) ) {
			// TODO
			return composeCss(CSS_NAME, "rotate(0deg)");
		} else if (("tbLrV").equals(val) ) {
			// TODO
			return composeCss(CSS_NAME, "rotate(270deg)");
			
		} else if (("tbRl").equals(val) ) {
			return composeCss(CSS_NAME, "rotate(90deg)");
			
		} else if (("btLrl").equals(val) ) {
			return composeCss(CSS_NAME, "rotate(270deg)");
		} else {
			log.warn("How to handle TextDirection of " + val);
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {

		String val = ((TextDirection)this.getObject()).getVal();
		if (("btLr").equals(val) ) {						
			foElement.setAttribute(FO_NAME, "90");
		} else if (("lrTb").equals(val) ) {
			foElement.setAttribute(FO_NAME, "0");
		} else if (("lrTbV").equals(val) ) {
			// TODO
			foElement.setAttribute(FO_NAME, "0");
		} else if (("tbLrV").equals(val) ) {
			// TODO
			foElement.setAttribute(FO_NAME, "90");
		} else if (("tbRl").equals(val) ) {
			foElement.setAttribute(FO_NAME, "-90");
		} else if (("tbRl").equals(val) ) {
			foElement.setAttribute(FO_NAME, "-90");
		} else {
			log.warn("How to handle TextDirection of " + val);
		}
		
	}

	@Override
	public void set(TcPr tcPr) {
		tcPr.setTextDirection( (TextDirection)this.getObject() );
	}
	
}
