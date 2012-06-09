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
 
package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_UpDownBars complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_UpDownBars">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="gapWidth" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_GapAmount" minOccurs="0"/>
 *         &lt;element name="upBars" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UpDownBar" minOccurs="0"/>
 *         &lt;element name="downBars" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UpDownBar" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_UpDownBars", propOrder = {
    "gapWidth",
    "upBars",
    "downBars",
    "extLst"
})
public class CTUpDownBars {

    protected CTGapAmount gapWidth;
    protected CTUpDownBar upBars;
    protected CTUpDownBar downBars;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the gapWidth property.
     * 
     * @return
     *     possible object is
     *     {@link CTGapAmount }
     *     
     */
    public CTGapAmount getGapWidth() {
        return gapWidth;
    }

    /**
     * Sets the value of the gapWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGapAmount }
     *     
     */
    public void setGapWidth(CTGapAmount value) {
        this.gapWidth = value;
    }

    /**
     * Gets the value of the upBars property.
     * 
     * @return
     *     possible object is
     *     {@link CTUpDownBar }
     *     
     */
    public CTUpDownBar getUpBars() {
        return upBars;
    }

    /**
     * Sets the value of the upBars property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUpDownBar }
     *     
     */
    public void setUpBars(CTUpDownBar value) {
        this.upBars = value;
    }

    /**
     * Gets the value of the downBars property.
     * 
     * @return
     *     possible object is
     *     {@link CTUpDownBar }
     *     
     */
    public CTUpDownBar getDownBars() {
        return downBars;
    }

    /**
     * Sets the value of the downBars property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUpDownBar }
     *     
     */
    public void setDownBars(CTUpDownBar value) {
        this.downBars = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

}
