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
 * <p>Java class for CT_LimUpp complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LimUpp">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="limUppPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_LimUppPr" minOccurs="0"/>
 *         &lt;element name="e" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathArg"/>
 *         &lt;element name="lim" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathArg"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LimUpp", propOrder = {
    "limUppPr",
    "e",
    "lim"
})
public class CTLimUpp
    implements Child
{

    protected CTLimUppPr limUppPr;
    @XmlElement(required = true)
    protected CTOMathArg e;
    @XmlElement(required = true)
    protected CTOMathArg lim;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the limUppPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTLimUppPr }
     *     
     */
    public CTLimUppPr getLimUppPr() {
        return limUppPr;
    }

    /**
     * Sets the value of the limUppPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLimUppPr }
     *     
     */
    public void setLimUppPr(CTLimUppPr value) {
        this.limUppPr = value;
    }

    /**
     * Gets the value of the e property.
     * 
     * @return
     *     possible object is
     *     {@link CTOMathArg }
     *     
     */
    public CTOMathArg getE() {
        return e;
    }

    /**
     * Sets the value of the e property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOMathArg }
     *     
     */
    public void setE(CTOMathArg value) {
        this.e = value;
    }

    /**
     * Gets the value of the lim property.
     * 
     * @return
     *     possible object is
     *     {@link CTOMathArg }
     *     
     */
    public CTOMathArg getLim() {
        return lim;
    }

    /**
     * Sets the value of the lim property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOMathArg }
     *     
     */
    public void setLim(CTOMathArg value) {
        this.lim = value;
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
