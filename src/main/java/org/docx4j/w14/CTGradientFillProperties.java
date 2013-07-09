/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_GradientFillProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GradientFillProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="gsLst" type="{http://schemas.microsoft.com/office/word/2010/wordml}CT_GradientStopList" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.microsoft.com/office/word/2010/wordml}EG_ShadeProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GradientFillProperties", propOrder = {
    "gsLst",
    "lin",
    "path"
})
public class CTGradientFillProperties implements Child
{

    protected CTGradientStopList gsLst;
    protected CTLinearShadeProperties lin;
    protected CTPathShadeProperties path;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the gsLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientStopList }
     *     
     */
    public CTGradientStopList getGsLst() {
        return gsLst;
    }

    /**
     * Sets the value of the gsLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientStopList }
     *     
     */
    public void setGsLst(CTGradientStopList value) {
        this.gsLst = value;
    }

    /**
     * Gets the value of the lin property.
     * 
     * @return
     *     possible object is
     *     {@link CTLinearShadeProperties }
     *     
     */
    public CTLinearShadeProperties getLin() {
        return lin;
    }

    /**
     * Sets the value of the lin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLinearShadeProperties }
     *     
     */
    public void setLin(CTLinearShadeProperties value) {
        this.lin = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link CTPathShadeProperties }
     *     
     */
    public CTPathShadeProperties getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPathShadeProperties }
     *     
     */
    public void setPath(CTPathShadeProperties value) {
        this.path = value;
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
