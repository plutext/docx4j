/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
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
package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TLTimeAnimateValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLTimeAnimateValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="val" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLAnimVariant" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="tm" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_TLTimeAnimateValueTime" default="indefinite" />
 *       &lt;attribute name="fmla" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLTimeAnimateValue", propOrder = {
    "val"
})
public class CTTLTimeAnimateValue {

    protected CTTLAnimVariant val;
    @XmlAttribute(name = "tm")
    protected String tm;
    @XmlAttribute(name = "fmla")
    protected String fmla;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLAnimVariant }
     *     
     */
    public CTTLAnimVariant getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLAnimVariant }
     *     
     */
    public void setVal(CTTLAnimVariant value) {
        this.val = value;
    }

    /**
     * Gets the value of the tm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTm() {
        if (tm == null) {
            return "indefinite";
        } else {
            return tm;
        }
    }

    /**
     * Sets the value of the tm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTm(String value) {
        this.tm = value;
    }

    /**
     * Gets the value of the fmla property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFmla() {
        if (fmla == null) {
            return "";
        } else {
            return fmla;
        }
    }

    /**
     * Sets the value of the fmla property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFmla(String value) {
        this.fmla = value;
    }

}
