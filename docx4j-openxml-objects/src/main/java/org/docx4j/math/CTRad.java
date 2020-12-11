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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Rad complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Rad">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="radPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_RadPr" minOccurs="0"/>
 *         &lt;element name="deg" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathArg"/>
 *         &lt;element name="e" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_OMathArg"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Rad", propOrder = {
    "radPr",
    "deg",
    "e"
})
public class CTRad
    implements Child
{

    protected CTRadPr radPr;
    @XmlElement(required = true)
    protected CTOMathArg deg;
    @XmlElement(required = true)
    protected CTOMathArg e;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the radPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTRadPr }
     *     
     */
    public CTRadPr getRadPr() {
        return radPr;
    }

    /**
     * Sets the value of the radPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRadPr }
     *     
     */
    public void setRadPr(CTRadPr value) {
        this.radPr = value;
    }

    /**
     * Gets the value of the deg property.
     * 
     * @return
     *     possible object is
     *     {@link CTOMathArg }
     *     
     */
    public CTOMathArg getDeg() {
        return deg;
    }

    /**
     * Sets the value of the deg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOMathArg }
     *     
     */
    public void setDeg(CTOMathArg value) {
        this.deg = value;
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
