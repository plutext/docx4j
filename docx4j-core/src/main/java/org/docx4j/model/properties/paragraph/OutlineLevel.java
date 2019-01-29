/*
 *  Copyright 2014, Plutext Pty Ltd.
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
import org.docx4j.wml.PPrBase.OutlineLvl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * @author jharrop
 */
public class OutlineLevel extends AbstractParagraphProperty {
	
	
	protected static Logger log = LoggerFactory.getLogger(OutlineLevel.class);		
		
	
	
	public OutlineLevel(OutlineLvl val) {
		this.setObject(val);
	}

	
	@Override
	public String getCssName() {
		return null;
	}
	
	private OutlineLevel(CSSValue value) {	
		
		// Not implemented
	}
	


	@Override
	public String getCssProperty() {
		return null;
	}
	

	@Override
	public void setXslFO(Element foElement) {

		// Not implemented
		
	}
	

	
	@Override
	public void set(PPr pPr) {
		pPr.setOutlineLvl( (OutlineLvl)this.getObject() );
	}


	
}
