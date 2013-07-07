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
package org.docx4j.model.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class AdHocProperty extends Property {
	
	protected static Logger log = LoggerFactory.getLogger(AdHocProperty.class);	
	
	private String cssName;
	private String cssValue;

	private String foName;
	private String foValue;
	
//	public AdHocProperty(String cssName, String cssValue) {
//		this.cssName  = cssName;
//		this.cssValue = cssValue;
//	}

	public AdHocProperty(String cssName, String cssValue, String foName, String foValue) {
		this.cssName  = cssName;
		this.cssValue = cssValue;
		this.foName = foName;
		this.foValue = foValue;
	}
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return cssName;
	}	
	
	public String getCssProperty() {
		return composeCss(cssName, cssValue);
	}
	
	public void setXslFO(Element foElement) {
		if (foName!=null && foValue!=null) {
			foElement.setAttribute(foName, foValue);
		}
	} 
	

}
