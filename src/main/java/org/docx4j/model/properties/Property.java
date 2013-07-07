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
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * A representation of a run or paragraph property,
 * designed for ease of conversion to or from
 * CSS or to XSL FO.
 * 
 * Currently this class assumes a one to one 
 * correspondence between properties.  This might
 * well have to change.
 * 
 * Examples of issues:
 * 
 * - w:ind maps to multiple css properties (left, right)
 * 
 * @author jason
 *
 */
public abstract class Property {
	
	protected static Logger log = LoggerFactory.getLogger(Property.class);	
	
	protected Object object;
	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}
	/**
	 * @param object the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}
	
	
	
	// convert to HTML (CSS) ------------------------------
	
	public abstract String getCssProperty();

	/**
	 * @since 2.7.2
	 */
	public abstract String getCssName();
	
	public final static String CSS_NULL = ""; 

	public final static String CSS_COLON = ":"; 
	public final static String CSS_SPACE = " "; 
	public final static String CSS_COMMA = ";"; 
	public final static String composeCss(String name, String value) {
		return name + CSS_COLON + CSS_SPACE + value + CSS_COMMA;
		
	}
	
	// convert to XSL FO ----------------------------------
	
	public abstract void setXslFO(Element foElement); 
	
	// convert from CSS  ----------------------------------
	
	public void debug(String propertyName, CSSValue value) {
		short valueType = value.getCssValueType();
		
		switch (valueType) {
		
			case CSSValue.CSS_CUSTOM:
				log.warn("valueType CUSTOM for " + propertyName + " - NOT IMPLEMENTED ");
				break;
			case CSSValue.CSS_INHERIT:
				log.warn("valueType INHERIT for " + propertyName + " - NOT IMPLEMENTED ");
				break;
			case CSSValue.CSS_PRIMITIVE_VALUE:
				log.debug("valueType PRIMITIVE for " + propertyName );
				CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
				log.debug("PrimitiveType: " + cssPrimitiveValue.getPrimitiveType() );
				break;
			case CSSValue.CSS_VALUE_LIST:
				log.debug("valueType LIST for " + propertyName );
				CSSValueList cssValueList = (CSSValueList)value;
				break;

			default:
				log.error("Unexpected valueType " + valueType + " for " + propertyName );
				return;
				
		}
		
	}

}
