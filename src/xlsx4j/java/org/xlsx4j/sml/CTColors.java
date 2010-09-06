/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Colors complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Colors">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="indexedColors" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_IndexedColors" minOccurs="0"/>
 *         &lt;element name="mruColors" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MRUColors" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Colors", propOrder = {
    "indexedColors",
    "mruColors"
})
public class CTColors {

    protected CTIndexedColors indexedColors;
    protected CTMRUColors mruColors;

    /**
     * Gets the value of the indexedColors property.
     * 
     * @return
     *     possible object is
     *     {@link CTIndexedColors }
     *     
     */
    public CTIndexedColors getIndexedColors() {
        return indexedColors;
    }

    /**
     * Sets the value of the indexedColors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIndexedColors }
     *     
     */
    public void setIndexedColors(CTIndexedColors value) {
        this.indexedColors = value;
    }

    /**
     * Gets the value of the mruColors property.
     * 
     * @return
     *     possible object is
     *     {@link CTMRUColors }
     *     
     */
    public CTMRUColors getMruColors() {
        return mruColors;
    }

    /**
     * Sets the value of the mruColors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMRUColors }
     *     
     */
    public void setMruColors(CTMRUColors value) {
        this.mruColors = value;
    }

}
