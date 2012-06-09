/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.model.properties.run;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.RPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Italics extends AbstractRunProperty {

	public final static String CSS_NAME = "font-style"; 
	public final static String FO_NAME  = "font-style"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public Italics(BooleanDefaultTrue val) {
		this.setObject(val);
	}
	
	public Italics(CSSValue value) {
		
		if (value.getCssText().toLowerCase().equals("italic")) {
			this.setObject( Context.getWmlObjectFactory().createBooleanDefaultTrue()  );
		} else {
			BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
			bdt.setVal(Boolean.FALSE);
		}
	}

	@Override
	public String getCssProperty() {
		
		if (((BooleanDefaultTrue)this.getObject()).isVal() ) {
			return composeCss(CSS_NAME, "italic");
		} else {
			return composeCss(CSS_NAME, "normal");
		}
	}


	@Override
	public void setXslFO(Element foElement) {
		if (((BooleanDefaultTrue)this.getObject()).isVal() ) {
			foElement.setAttribute(FO_NAME, "italic" );
		} else {
			foElement.setAttribute(FO_NAME, "normal" );
		}
	}

	@Override
	public void set(RPr rPr) {
		rPr.setI( (BooleanDefaultTrue)this.getObject() );
	}
	
}
