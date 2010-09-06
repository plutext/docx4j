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
 * <p>Java class for CT_PatternFill complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PatternFill">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fgColor" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="bgColor" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Color" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="patternType" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PatternType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PatternFill", propOrder = {
    "fgColor",
    "bgColor"
})
public class CTPatternFill {

    protected CTColor fgColor;
    protected CTColor bgColor;
    @XmlAttribute
    protected STPatternType patternType;

    /**
     * Gets the value of the fgColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getFgColor() {
        return fgColor;
    }

    /**
     * Sets the value of the fgColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setFgColor(CTColor value) {
        this.fgColor = value;
    }

    /**
     * Gets the value of the bgColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getBgColor() {
        return bgColor;
    }

    /**
     * Sets the value of the bgColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setBgColor(CTColor value) {
        this.bgColor = value;
    }

    /**
     * Gets the value of the patternType property.
     * 
     * @return
     *     possible object is
     *     {@link STPatternType }
     *     
     */
    public STPatternType getPatternType() {
        return patternType;
    }

    /**
     * Sets the value of the patternType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPatternType }
     *     
     */
    public void setPatternType(STPatternType value) {
        this.patternType = value;
    }

}
