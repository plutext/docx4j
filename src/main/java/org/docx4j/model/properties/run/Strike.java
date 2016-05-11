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
import org.docx4j.dml.STTextStrikeType;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Strike extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(Strike.class);		

	public final static String CSS_NAME = "text-decoration"; 
	public final static String FO_NAME  = "text-decoration"; 

	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}

	public Strike(BooleanDefaultTrue val) {
		this.setObject(val);
	}
	
	public Strike(CSSValue value) {
        BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
        if (!(value.getCssText().toLowerCase().equals("line-through")
                || value.getCssText().toLowerCase().equals("[line-through]"))) {
			bdt.setVal(Boolean.FALSE);
		}
        this.setObject( bdt  );
	}

	@Override
	public String getCssProperty() {
		
		if (((BooleanDefaultTrue)this.getObject()).isVal() ) {
			return composeCss(CSS_NAME, "line-through");
		} else {
			return composeCss(CSS_NAME, "none");
		}
	}


	@Override
	public void setXslFO(Element foElement) {
		if (((BooleanDefaultTrue)this.getObject()).isVal() ) {
			foElement.setAttribute(FO_NAME, "line-through" );
		} else {
			foElement.setAttribute(FO_NAME, "none" );
		}

	}

	@Override
	public void set(RPr rPr) {
		rPr.setStrike( (BooleanDefaultTrue)this.getObject() );
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        if(((BooleanDefaultTrue)this.getObject()).isVal()) {
            rPr.setStrike(STTextStrikeType.SNG_STRIKE);
        } else {
            rPr.setStrike(STTextStrikeType.NO_STRIKE);
        }
    }
	
}
