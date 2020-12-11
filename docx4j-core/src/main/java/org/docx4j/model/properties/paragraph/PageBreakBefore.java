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
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.PPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class PageBreakBefore extends AbstractParagraphProperty {
	
	protected static Logger log = LoggerFactory.getLogger(PageBreakBefore.class);		
	
	public final static String CSS_NAME = "page-break-before"; 
	public final static String FO_NAME  = "break-before"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public PageBreakBefore(BooleanDefaultTrue val) {
		this.setObject(val);
	}
	
	public PageBreakBefore(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		if (value.getCssText().toLowerCase().equals("always")) {
			this.setObject( Context.getWmlObjectFactory().createBooleanDefaultTrue()  );
		} else {
			BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
			bdt.setVal(Boolean.FALSE);
		}
	}

	@Override
	public String getCssProperty() {
		
		if (((BooleanDefaultTrue)this.getObject()).isVal() ) {
			return composeCss(CSS_NAME, "always");
		} else {
			return composeCss(CSS_NAME, "auto");
		}
	}


	@Override
	public void setXslFO(Element foElement) {

		if (((BooleanDefaultTrue)this.getObject()).isVal() ) {
			foElement.setAttribute(FO_NAME, "page" );
		} else {
			foElement.setAttribute(FO_NAME, "auto" );
		}
		
	}

	@Override
	public void set(PPr pPr) {
		pPr.setPageBreakBefore( (BooleanDefaultTrue)this.getObject() );
	}
	
}
