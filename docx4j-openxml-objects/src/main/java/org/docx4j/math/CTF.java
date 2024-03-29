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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_F complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_F">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_FPr" minOccurs="0"/>
 *         &lt;element name="num" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathArg"/>
 *         &lt;element name="den" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathArg"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_F", propOrder = {
    "fPr",
    "num",
    "den"
})
public class CTF
    implements Child
{

    protected CTFPr fPr;
    @XmlElement(required = true)
    protected CTOMathArg num;
    @XmlElement(required = true)
    protected CTOMathArg den;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTFPr }
     *     
     */
    public CTFPr getFPr() {
        return fPr;
    }

    /**
     * Sets the value of the fPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFPr }
     *     
     */
    public void setFPr(CTFPr value) {
        this.fPr = value;
    }

    /**
     * Gets the value of the num property.
     * 
     * @return
     *     possible object is
     *     {@link CTOMathArg }
     *     
     */
    public CTOMathArg getNum() {
        return num;
    }

    /**
     * Sets the value of the num property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOMathArg }
     *     
     */
    public void setNum(CTOMathArg value) {
        this.num = value;
    }

    /**
     * Gets the value of the den property.
     * 
     * @return
     *     possible object is
     *     {@link CTOMathArg }
     *     
     */
    public CTOMathArg getDen() {
        return den;
    }

    /**
     * Sets the value of the den property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOMathArg }
     *     
     */
    public void setDen(CTOMathArg value) {
        this.den = value;
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
