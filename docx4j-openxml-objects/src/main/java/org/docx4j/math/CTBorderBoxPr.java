/*
 *  Copyright 2009-2012, Plutext Pty Ltd.
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
package org.docx4j.math;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BorderBoxPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BorderBoxPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="hideTop" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="hideBot" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="hideLeft" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="hideRight" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="strikeH" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="strikeV" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="strikeBLTR" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="strikeTLBR" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="ctrlPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_CtrlPr" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BorderBoxPr", propOrder = {
    "hideTop",
    "hideBot",
    "hideLeft",
    "hideRight",
    "strikeH",
    "strikeV",
    "strikeBLTR",
    "strikeTLBR",
    "ctrlPr"
})
public class CTBorderBoxPr
    implements Child
{

    protected CTOnOff hideTop;
    protected CTOnOff hideBot;
    protected CTOnOff hideLeft;
    protected CTOnOff hideRight;
    protected CTOnOff strikeH;
    protected CTOnOff strikeV;
    protected CTOnOff strikeBLTR;
    protected CTOnOff strikeTLBR;
    protected CTCtrlPr ctrlPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the hideTop property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getHideTop() {
        return hideTop;
    }

    /**
     * Sets the value of the hideTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setHideTop(CTOnOff value) {
        this.hideTop = value;
    }

    /**
     * Gets the value of the hideBot property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getHideBot() {
        return hideBot;
    }

    /**
     * Sets the value of the hideBot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setHideBot(CTOnOff value) {
        this.hideBot = value;
    }

    /**
     * Gets the value of the hideLeft property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getHideLeft() {
        return hideLeft;
    }

    /**
     * Sets the value of the hideLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setHideLeft(CTOnOff value) {
        this.hideLeft = value;
    }

    /**
     * Gets the value of the hideRight property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getHideRight() {
        return hideRight;
    }

    /**
     * Sets the value of the hideRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setHideRight(CTOnOff value) {
        this.hideRight = value;
    }

    /**
     * Gets the value of the strikeH property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getStrikeH() {
        return strikeH;
    }

    /**
     * Sets the value of the strikeH property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setStrikeH(CTOnOff value) {
        this.strikeH = value;
    }

    /**
     * Gets the value of the strikeV property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getStrikeV() {
        return strikeV;
    }

    /**
     * Sets the value of the strikeV property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setStrikeV(CTOnOff value) {
        this.strikeV = value;
    }

    /**
     * Gets the value of the strikeBLTR property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getStrikeBLTR() {
        return strikeBLTR;
    }

    /**
     * Sets the value of the strikeBLTR property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setStrikeBLTR(CTOnOff value) {
        this.strikeBLTR = value;
    }

    /**
     * Gets the value of the strikeTLBR property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getStrikeTLBR() {
        return strikeTLBR;
    }

    /**
     * Sets the value of the strikeTLBR property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setStrikeTLBR(CTOnOff value) {
        this.strikeTLBR = value;
    }

    /**
     * Gets the value of the ctrlPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCtrlPr }
     *     
     */
    public CTCtrlPr getCtrlPr() {
        return ctrlPr;
    }

    /**
     * Sets the value of the ctrlPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCtrlPr }
     *     
     */
    public void setCtrlPr(CTCtrlPr value) {
        this.ctrlPr = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
