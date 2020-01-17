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

import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class NumberingProperty extends AbstractParagraphProperty {
	
	protected static Logger log = LoggerFactory.getLogger(NumberingProperty.class);		
	
	public final static String CSS_NAME = "FIXME"; 
	public final static String FO_NAME  = "FIXME"; 
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public NumberingProperty(NumPr numPr) {
		this.setObject(numPr);
	}
	
	public NumberingProperty(CSSValue value) {	
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getCssProperty() {
		
		// Currently, numbering in HTML NG2 is handled via an extension function
		// directly from the XSLT. 
		return "";
		//throw new RuntimeException("Not implemented");
		
	}


	@Override
	public void setXslFO(Element foElement) {
		// Do nothing
	}

	@Override
	public void set(PPr pPr) {
		pPr.setNumPr( (NumPr)this.getObject() );
	}
	
}
