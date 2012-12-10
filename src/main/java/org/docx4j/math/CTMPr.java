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
 * <p>Java class for CT_MPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="baseJc" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_YAlign" minOccurs="0"/>
 *         &lt;element name="plcHide" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="rSpRule" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_SpacingRule" minOccurs="0"/>
 *         &lt;element name="cGpRule" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_SpacingRule" minOccurs="0"/>
 *         &lt;element name="rSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_UnSignedInteger" minOccurs="0"/>
 *         &lt;element name="cSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_UnSignedInteger" minOccurs="0"/>
 *         &lt;element name="cGp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_UnSignedInteger" minOccurs="0"/>
 *         &lt;element name="mcs" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_MCS" minOccurs="0"/>
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
@XmlType(name = "CT_MPr", propOrder = {
    "baseJc",
    "plcHide",
    "rSpRule",
    "cGpRule",
    "rSp",
    "cSp",
    "cGp",
    "mcs",
    "ctrlPr"
})
public class CTMPr
    implements Child
{

    protected CTYAlign baseJc;
    protected CTOnOff plcHide;
    protected CTSpacingRule rSpRule;
    protected CTSpacingRule cGpRule;
    protected CTUnSignedInteger rSp;
    protected CTUnSignedInteger cSp;
    protected CTUnSignedInteger cGp;
    protected CTMCS mcs;
    protected CTCtrlPr ctrlPr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the baseJc property.
     * 
     * @return
     *     possible object is
     *     {@link CTYAlign }
     *     
     */
    public CTYAlign getBaseJc() {
        return baseJc;
    }

    /**
     * Sets the value of the baseJc property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTYAlign }
     *     
     */
    public void setBaseJc(CTYAlign value) {
        this.baseJc = value;
    }

    /**
     * Gets the value of the plcHide property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getPlcHide() {
        return plcHide;
    }

    /**
     * Sets the value of the plcHide property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setPlcHide(CTOnOff value) {
        this.plcHide = value;
    }

    /**
     * Gets the value of the rSpRule property.
     * 
     * @return
     *     possible object is
     *     {@link CTSpacingRule }
     *     
     */
    public CTSpacingRule getRSpRule() {
        return rSpRule;
    }

    /**
     * Sets the value of the rSpRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSpacingRule }
     *     
     */
    public void setRSpRule(CTSpacingRule value) {
        this.rSpRule = value;
    }

    /**
     * Gets the value of the cGpRule property.
     * 
     * @return
     *     possible object is
     *     {@link CTSpacingRule }
     *     
     */
    public CTSpacingRule getCGpRule() {
        return cGpRule;
    }

    /**
     * Sets the value of the cGpRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSpacingRule }
     *     
     */
    public void setCGpRule(CTSpacingRule value) {
        this.cGpRule = value;
    }

    /**
     * Gets the value of the rSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnSignedInteger }
     *     
     */
    public CTUnSignedInteger getRSp() {
        return rSp;
    }

    /**
     * Sets the value of the rSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnSignedInteger }
     *     
     */
    public void setRSp(CTUnSignedInteger value) {
        this.rSp = value;
    }

    /**
     * Gets the value of the cSp property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnSignedInteger }
     *     
     */
    public CTUnSignedInteger getCSp() {
        return cSp;
    }

    /**
     * Sets the value of the cSp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnSignedInteger }
     *     
     */
    public void setCSp(CTUnSignedInteger value) {
        this.cSp = value;
    }

    /**
     * Gets the value of the cGp property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnSignedInteger }
     *     
     */
    public CTUnSignedInteger getCGp() {
        return cGp;
    }

    /**
     * Sets the value of the cGp property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnSignedInteger }
     *     
     */
    public void setCGp(CTUnSignedInteger value) {
        this.cGp = value;
    }

    /**
     * Gets the value of the mcs property.
     * 
     * @return
     *     possible object is
     *     {@link CTMCS }
     *     
     */
    public CTMCS getMcs() {
        return mcs;
    }

    /**
     * Sets the value of the mcs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMCS }
     *     
     */
    public void setMcs(CTMCS value) {
        this.mcs = value;
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
