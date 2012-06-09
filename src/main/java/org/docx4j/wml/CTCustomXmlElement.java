/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.wml;

import java.util.List;

import org.jvnet.jaxb2_commons.ppp.Child;

/**
 * @since 2.7
 */
public interface CTCustomXmlElement extends Child {

    /**
     * Gets the value of the customXmlPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomXmlPr }
     *     
     */
    public CTCustomXmlPr getCustomXmlPr();
    
    /**
     * Sets the value of the customXmlPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomXmlPr }
     *     
     */
    public void setCustomXmlPr(CTCustomXmlPr value);
    
    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri();

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value);

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElement();

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElement(String value);
    
    
    public List<Object> getContent();
}
