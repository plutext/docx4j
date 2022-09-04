/*
 *  Copyright 2021, Plutext Pty Ltd.
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
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * Output language and country attributes to support 
 * hyphenation in PDF output.
 * 
 * @author jharrop
 *
 */
public class Lang extends AbstractRunProperty {
	
	protected static final Logger log = LoggerFactory.getLogger(Lang.class);
	
	public final static String CSS_NAME = null; 
	public final static String FO_NAME  = "language"; 
	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public Lang(CTLanguage val) {
		this.setObject(val);
	}
	
	public Lang(CSSValue value) {	
	}


	@Override
	public String getCssProperty() {
		return null;
	}


	@Override
	public void setXslFO(Element foElement) {
		
		if (this.getObject()==null) return;
		
		String val = ((CTLanguage)this.getObject()).getVal();
		
		if (val!=null) {

			String lang = val.substring(0, val.indexOf('-'));
			String country = val.substring(val.indexOf('-')+1);
			
			foElement.setAttribute(FO_NAME, lang );
			foElement.setAttribute("country", country );
		}
	}

	@Override
	public void set(RPr rPr) {
		rPr.setLang(((CTLanguage)this.getObject()));
	}

	@Override
	public void set(CTTextCharacterProperties rPr) {
		if (this.getObject()==null) return;
		rPr.setLang(((CTLanguage)this.getObject()).getVal());
		
	}

	
}
