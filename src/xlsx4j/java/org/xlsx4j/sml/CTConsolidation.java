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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Consolidation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Consolidation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pages" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Pages" minOccurs="0"/>
 *         &lt;element name="rangeSets" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RangeSets"/>
 *       &lt;/sequence>
 *       &lt;attribute name="autoPage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Consolidation", propOrder = {
    "pages",
    "rangeSets"
})
public class CTConsolidation {

    protected CTPages pages;
    @XmlElement(required = true)
    protected CTRangeSets rangeSets;
    @XmlAttribute
    protected Boolean autoPage;

    /**
     * Gets the value of the pages property.
     * 
     * @return
     *     possible object is
     *     {@link CTPages }
     *     
     */
    public CTPages getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPages }
     *     
     */
    public void setPages(CTPages value) {
        this.pages = value;
    }

    /**
     * Gets the value of the rangeSets property.
     * 
     * @return
     *     possible object is
     *     {@link CTRangeSets }
     *     
     */
    public CTRangeSets getRangeSets() {
        return rangeSets;
    }

    /**
     * Sets the value of the rangeSets property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRangeSets }
     *     
     */
    public void setRangeSets(CTRangeSets value) {
        this.rangeSets = value;
    }

    /**
     * Gets the value of the autoPage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoPage() {
        if (autoPage == null) {
            return true;
        } else {
            return autoPage;
        }
    }

    /**
     * Sets the value of the autoPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoPage(Boolean value) {
        this.autoPage = value;
    }

}
