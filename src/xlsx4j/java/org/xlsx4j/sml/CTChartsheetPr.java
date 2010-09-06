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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ChartsheetPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ChartsheetPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tabColor" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Color" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="published" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="codeName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ChartsheetPr", propOrder = {
    "tabColor"
})
public class CTChartsheetPr {

    protected CTColor tabColor;
    @XmlAttribute
    protected Boolean published;
    @XmlAttribute
    protected String codeName;

    /**
     * Gets the value of the tabColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getTabColor() {
        return tabColor;
    }

    /**
     * Sets the value of the tabColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setTabColor(CTColor value) {
        this.tabColor = value;
    }

    /**
     * Gets the value of the published property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPublished() {
        if (published == null) {
            return true;
        } else {
            return published;
        }
    }

    /**
     * Sets the value of the published property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublished(Boolean value) {
        this.published = value;
    }

    /**
     * Gets the value of the codeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeName() {
        return codeName;
    }

    /**
     * Sets the value of the codeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeName(String value) {
        this.codeName = value;
    }

}
