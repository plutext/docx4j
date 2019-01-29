/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TLTextTargetElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLTextTargetElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="charRg" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_IndexRange"/>
 *         &lt;element name="pRg" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_IndexRange"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLTextTargetElement", propOrder = {
    "charRg",
    "pRg"
})
public class CTTLTextTargetElement {

    protected CTIndexRange charRg;
    protected CTIndexRange pRg;

    /**
     * Gets the value of the charRg property.
     * 
     * @return
     *     possible object is
     *     {@link CTIndexRange }
     *     
     */
    public CTIndexRange getCharRg() {
        return charRg;
    }

    /**
     * Sets the value of the charRg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIndexRange }
     *     
     */
    public void setCharRg(CTIndexRange value) {
        this.charRg = value;
    }

    /**
     * Gets the value of the pRg property.
     * 
     * @return
     *     possible object is
     *     {@link CTIndexRange }
     *     
     */
    public CTIndexRange getPRg() {
        return pRg;
    }

    /**
     * Sets the value of the pRg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIndexRange }
     *     
     */
    public void setPRg(CTIndexRange value) {
        this.pRg = value;
    }

}
