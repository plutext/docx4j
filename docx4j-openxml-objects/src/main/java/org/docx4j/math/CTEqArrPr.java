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
 * <p>Java class for CT_EqArrPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EqArrPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="baseJc" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_YAlign" minOccurs="0"/>
 *         &lt;element name="maxDist" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="objDist" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OnOff" minOccurs="0"/>
 *         &lt;element name="rSpRule" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_SpacingRule" minOccurs="0"/>
 *         &lt;element name="rSp" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_UnSignedInteger" minOccurs="0"/>
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
@XmlType(name = "CT_EqArrPr", propOrder = {
    "baseJc",
    "maxDist",
    "objDist",
    "rSpRule",
    "rSp",
    "ctrlPr"
})
public class CTEqArrPr
    implements Child
{

    protected CTYAlign baseJc;
    protected CTOnOff maxDist;
    protected CTOnOff objDist;
    protected CTSpacingRule rSpRule;
    protected CTUnSignedInteger rSp;
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
     * Gets the value of the maxDist property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getMaxDist() {
        return maxDist;
    }

    /**
     * Sets the value of the maxDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setMaxDist(CTOnOff value) {
        this.maxDist = value;
    }

    /**
     * Gets the value of the objDist property.
     * 
     * @return
     *     possible object is
     *     {@link CTOnOff }
     *     
     */
    public CTOnOff getObjDist() {
        return objDist;
    }

    /**
     * Sets the value of the objDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOnOff }
     *     
     */
    public void setObjDist(CTOnOff value) {
        this.objDist = value;
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
