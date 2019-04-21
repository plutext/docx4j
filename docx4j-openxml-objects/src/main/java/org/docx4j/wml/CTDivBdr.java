/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DivBdr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DivBdr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="top" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *         &lt;element name="left" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *         &lt;element name="bottom" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *         &lt;element name="right" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Border" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DivBdr", propOrder = {
    "top",
    "left",
    "bottom",
    "right"
})
public class CTDivBdr implements Child
{

    protected CTBorder top;
    protected CTBorder left;
    protected CTBorder bottom;
    protected CTBorder right;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorder }
     *     
     */
    public CTBorder getTop() {
        return top;
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorder }
     *     
     */
    public void setTop(CTBorder value) {
        this.top = value;
    }

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorder }
     *     
     */
    public CTBorder getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorder }
     *     
     */
    public void setLeft(CTBorder value) {
        this.left = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorder }
     *     
     */
    public CTBorder getBottom() {
        return bottom;
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorder }
     *     
     */
    public void setBottom(CTBorder value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorder }
     *     
     */
    public CTBorder getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorder }
     *     
     */
    public void setRight(CTBorder value) {
        this.right = value;
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
