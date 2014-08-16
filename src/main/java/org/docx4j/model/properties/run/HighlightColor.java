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
import org.docx4j.jaxb.Context;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class HighlightColor extends AbstractRunProperty {

	protected static Logger log = LoggerFactory.getLogger(HighlightColor.class);	
	
	public final static String CSS_NAME = "background-color"; 
	public final static String FO_NAME  = "background-color"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	
	public HighlightColor(Highlight shading) {
		this.setObject(shading);
	}
	
	public HighlightColor(CSSValue value) {	
		
		Highlight shd = Context.getWmlObjectFactory().createHighlight();		
		shd.setVal(value.getCssText());
		this.setObject( shd  );
	}

	@Override
	public String getCssProperty() {
		
		Highlight shd = ((Highlight)this.getObject());
		
		if (shd.getVal()!=null &&
		!shd.getVal().equals("none")) {
			return composeCss(CSS_NAME, shd.getHexVal() );
		} else {
			return CSS_NULL;
		}
	}


	@Override
	public void setXslFO(Element foElement) {

		Highlight shd = ((Highlight)this.getObject());
		
		if (shd.getVal()!=null &&
				!shd.getVal().equals("none")) {
			foElement.setAttribute(FO_NAME, shd.getHexVal() );
		} 
		
	}

	@Override
	public void set(RPr rPr) {
		rPr.setHighlight((Highlight)this.getObject() );		
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        //TODO
    }
	
}
