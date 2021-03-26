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
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.PPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Justification extends AbstractParagraphProperty {
	
	protected static Logger log = LoggerFactory.getLogger(Justification.class);	
	
	
	public final static String CSS_NAME = "text-align"; 
	public final static String FO_NAME  = "text-align"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public Justification(Jc jc) {
		this.setObject(jc);
	}
	
	public Justification(CSSValue value) {	
		
		debug(CSS_NAME, value);
				
		Jc jc = Context.getWmlObjectFactory().createJc();
		
		String lowerCase = value.getCssText().toLowerCase();
		
		if (lowerCase.equals("left")
				|| lowerCase.equals("start") ) {
			// TODO: this needs to be made RTL aware
			jc.setVal(JcEnumeration.LEFT);
		} else if (lowerCase.equals("center")) {
			jc.setVal(JcEnumeration.CENTER);
		} else if (lowerCase.equals("right")
				|| lowerCase.equals("end")) {
			jc.setVal(JcEnumeration.RIGHT);
		} else if (lowerCase.equals("justify")
				|| lowerCase.equals("justify-all")) {
			jc.setVal(JcEnumeration.BOTH);
		} else {
			log.warn("How to handle justification: " + value.getCssText());
		}		

		this.setObject( jc  );
	}

	@Override
	public String getCssProperty() {
		
		String val = ((Jc)this.getObject()).getVal().value();
		if (val.equals("left") || val.equals("center") || val.equals("right")) {			
			return composeCss(CSS_NAME, val);
		} else if (val.equals("both")) {
			return composeCss(CSS_NAME, "justify");
			// IE only: text-justify:inter-ideograph;" );
		} // ignore the other possibilities for now
		else {
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {
		
		if (((Jc)this.getObject()).getVal()==null) {
			log.error("justification missing value");
			return;
		}

		String val = ((Jc)this.getObject()).getVal().value();
		if (val.equals("left") || val.equals("center") || val.equals("right")) {			
			foElement.setAttribute(FO_NAME,  val);
		} else if (val.equals("both")) {
			foElement.setAttribute(FO_NAME,  "justify");
		} // ignore the other possibilities for now
		
	}

	@Override
	public void set(PPr pPr) {
		pPr.setJc( (Jc)this.getObject() );
	}
	
}
