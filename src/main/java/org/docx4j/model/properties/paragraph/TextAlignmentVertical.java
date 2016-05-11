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

import org.docx4j.jaxb.Context;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class TextAlignmentVertical extends AbstractParagraphProperty {
	
	protected static Logger log = LoggerFactory.getLogger(TextAlignmentVertical.class);		
	
	public final static String CSS_NAME = "vertical-align"; 
	public final static String FO_NAME  = "vertical-align"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public TextAlignmentVertical(TextAlignment textAlignment) {
		this.setObject(textAlignment);
	}
	
	public TextAlignmentVertical(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		TextAlignment textAlignment = Context.getWmlObjectFactory().createPPrBaseTextAlignment();
		
		if (value.getCssText().toLowerCase().equals("top")) {
			textAlignment.setVal("top");
		} else if (value.getCssText().toLowerCase().equals("middle")) {
			textAlignment.setVal("center");
		} else if (value.getCssText().toLowerCase().equals("baseline")) {
			textAlignment.setVal("auto");
		} else {
			log.warn("How to handle vertical-align: " + value.getCssText());
		}		

		this.setObject( textAlignment  );
	}

	@Override
	public String getCssProperty() {
		
		String val = ((TextAlignment)this.getObject()).getVal();
		if (val.equals("top") || val.equals("bottom") || val.equals("baseline") ) {						
			return composeCss(CSS_NAME, val);
		} else if (val.equals("center")) {
			return composeCss(CSS_NAME, "middle");
		} else if (val.equals("auto")) {
			return composeCss(CSS_NAME, "baseline");
		} else {
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {

		String val = ((TextAlignment)this.getObject()).getVal();
		if (val.equals("top") || val.equals("bottom") || val.equals("baseline") ) {						
			foElement.setAttribute(FO_NAME, val);
		} else if (val.equals("center")) {
			foElement.setAttribute(FO_NAME, "middle");
		} else if (val.equals("auto")) {
			foElement.setAttribute(FO_NAME, "baseline");
		} 		
	}

	@Override
	public void set(PPr pPr) {
		pPr.setTextAlignment( (TextAlignment)this.getObject() );
	}
	
}
