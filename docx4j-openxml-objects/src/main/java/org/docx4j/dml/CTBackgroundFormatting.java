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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_BackgroundFormatting complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BackgroundFormatting">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillProperties" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_EffectProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BackgroundFormatting", propOrder = {
    "noFill",
    "solidFill",
    "gradFill",
    "blipFill",
    "pattFill",
    "grpFill",
    "effectLst",
    "effectDag"
})
public class CTBackgroundFormatting {

    protected CTNoFillProperties noFill;
    protected CTSolidColorFillProperties solidFill;
    protected CTGradientFillProperties gradFill;
    protected CTBlipFillProperties blipFill;
    protected CTPatternFillProperties pattFill;
    protected CTGroupFillProperties grpFill;
    protected CTEffectList effectLst;
    protected CTEffectContainer effectDag;

    /**
     * Gets the value of the noFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTNoFillProperties }
     *     
     */
    public CTNoFillProperties getNoFill() {
        return noFill;
    }

    /**
     * Sets the value of the noFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNoFillProperties }
     *     
     */
    public void setNoFill(CTNoFillProperties value) {
        this.noFill = value;
    }

    /**
     * Gets the value of the solidFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getSolidFill() {
        return solidFill;
    }

    /**
     * Sets the value of the solidFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setSolidFill(CTSolidColorFillProperties value) {
        this.solidFill = value;
    }

    /**
     * Gets the value of the gradFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public CTGradientFillProperties getGradFill() {
        return gradFill;
    }

    /**
     * Sets the value of the gradFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientFillProperties }
     *     
     */
    public void setGradFill(CTGradientFillProperties value) {
        this.gradFill = value;
    }

    /**
     * Gets the value of the blipFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public CTBlipFillProperties getBlipFill() {
        return blipFill;
    }

    /**
     * Sets the value of the blipFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public void setBlipFill(CTBlipFillProperties value) {
        this.blipFill = value;
    }

    /**
     * Gets the value of the pattFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public CTPatternFillProperties getPattFill() {
        return pattFill;
    }

    /**
     * Sets the value of the pattFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPatternFillProperties }
     *     
     */
    public void setPattFill(CTPatternFillProperties value) {
        this.pattFill = value;
    }

    /**
     * Gets the value of the grpFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public CTGroupFillProperties getGrpFill() {
        return grpFill;
    }

    /**
     * Sets the value of the grpFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupFillProperties }
     *     
     */
    public void setGrpFill(CTGroupFillProperties value) {
        this.grpFill = value;
    }

    /**
     * Gets the value of the effectLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectList }
     *     
     */
    public CTEffectList getEffectLst() {
        return effectLst;
    }

    /**
     * Sets the value of the effectLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectList }
     *     
     */
    public void setEffectLst(CTEffectList value) {
        this.effectLst = value;
    }

    /**
     * Gets the value of the effectDag property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectContainer }
     *     
     */
    public CTEffectContainer getEffectDag() {
        return effectDag;
    }

    /**
     * Sets the value of the effectDag property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectContainer }
     *     
     */
    public void setEffectDag(CTEffectContainer value) {
        this.effectDag = value;
    }

}
