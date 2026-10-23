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

import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.STTextUnderlineType;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.RPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Underline extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(Underline.class);		

	public final static String CSS_NAME = "text-decoration"; 
	public final static String FO_NAME  = "text-decoration"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public Underline(U u) {
		this.setObject(u);
	}

	public Underline(CSSValue value) {
		
		//debug(CSS_NAME, value);
		
		U u = Context.getWmlObjectFactory().createU();
		
		if (value.getCssText().toLowerCase().equals("underline")
				|| value.getCssText().toLowerCase().equals("[underline]")) {
			u.setVal(UnderlineEnumeration.SINGLE);
//		} else if (value.getCssText().toLowerCase().equals("underline")) {
//			u.setVal(UnderlineEnumeration.NONE);
		} else {
			log.error("How to handle " + CSS_NAME + " " + value.getCssText().toLowerCase());
		}

		this.setObject( u );
		
	}
	
	/** Whether this w:u draws an underline. A w:u without a w:val (bare, or stating only a colour) or with a value
	 *  outside ST_Underline (which JAXB reads as null) is no underline of its own: Word draws none, and over an
	 *  underlining style the style's underline shows (CR-060 section 1, rule 2). Until 17.3.5 it was drawn. */
	private boolean underlined() {
		UnderlineEnumeration val = ((U)this.getObject()).getVal();
		return val != null && val != UnderlineEnumeration.NONE;
	}

	@Override
	public String getCssProperty() {
		
		if (underlined()) {
			return composeCss(CSS_NAME, "underline");  
		} else {
			return CSS_NULL;  // text-decoration is also used for Strike (a la line-through)
		}
		// How to handle <w:u w:color="FF0000"> ie coloured underline?
		
	}


	@Override
	public void setXslFO(Element foElement) {

		if (underlined()) {
			foElement.setAttribute(FO_NAME, "underline");
		}
		
	}

	@Override
	public void set(RPr rPr) {
		rPr.setU((U)this.getObject());
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        if (underlined()) {
            rPr.setU(STTextUnderlineType.SNG);
        }
    }
	
}
