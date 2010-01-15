/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PatternFillProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PatternFillProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fgClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="bgClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetPatternVal" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PatternFillProperties", propOrder = {
    "fgClr",
    "bgClr"
})
public class CTPatternFillProperties {

    protected CTColor fgClr;
    protected CTColor bgClr;
    @XmlAttribute
    protected STPresetPatternVal prst;

    /**
     * Gets the value of the fgClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getFgClr() {
        return fgClr;
    }

    /**
     * Sets the value of the fgClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setFgClr(CTColor value) {
        this.fgClr = value;
    }

    /**
     * Gets the value of the bgClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getBgClr() {
        return bgClr;
    }

    /**
     * Sets the value of the bgClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setBgClr(CTColor value) {
        this.bgClr = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STPresetPatternVal }
     *     
     */
    public STPresetPatternVal getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPresetPatternVal }
     *     
     */
    public void setPrst(STPresetPatternVal value) {
        this.prst = value;
    }

}
