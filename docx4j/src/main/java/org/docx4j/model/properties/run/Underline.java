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

import org.docx4j.wml.RPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Underline extends AbstractRunProperty {

	public final static String CSS_NAME = "text-decoration"; 
	public final static String FO_NAME  = "text-decoration"; 
	
	public Underline(U u) {
		this.setObject(u);
	}

	public Underline(CSSValue value) {
		
		debug(CSS_NAME, value);
	}
	
	@Override
	public String getCssProperty() {
		
		if (((U)this.getObject()).getVal()==null ) {
			// This does happen			
			return composeCss(CSS_NAME, "underline");
		} else if (!((U)this.getObject()).getVal().equals( UnderlineEnumeration.NONE ) ) {
			return composeCss(CSS_NAME, "underline");
		} else {
			return CSS_NULL;
		}
		// How to handle <w:u w:color="FF0000"> ie coloured underline?
		
	}


	@Override
	public void setXslFO(Element foElement) {

		if (((U)this.getObject()).getVal()==null ) {
			// This does happen			
			foElement.setAttribute(FO_NAME, "underline");
		} else if (!((U)this.getObject()).getVal().equals( UnderlineEnumeration.NONE ) ) {
			foElement.setAttribute(FO_NAME, "underline");
		} else {
			//
		}
		
	}

	@Override
	public void set(RPr rPr) {
		rPr.setU((U)this.getObject());
	}
	
}
