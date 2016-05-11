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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FontSig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FontSig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="usb0" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="usb1" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="usb2" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="usb3" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="csb0" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *       &lt;attribute name="csb1" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_LongHexNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FontSig")
public class FontSig implements Child
{

    @XmlAttribute(name = "usb0", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String usb0;
    @XmlAttribute(name = "usb1", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String usb1;
    @XmlAttribute(name = "usb2", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String usb2;
    @XmlAttribute(name = "usb3", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String usb3;
    @XmlAttribute(name = "csb0", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String csb0;
    @XmlAttribute(name = "csb1", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected String csb1;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the usb0 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsb0() {
        return usb0;
    }

    /**
     * Sets the value of the usb0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsb0(String value) {
        this.usb0 = value;
    }

    /**
     * Gets the value of the usb1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsb1() {
        return usb1;
    }

    /**
     * Sets the value of the usb1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsb1(String value) {
        this.usb1 = value;
    }

    /**
     * Gets the value of the usb2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsb2() {
        return usb2;
    }

    /**
     * Sets the value of the usb2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsb2(String value) {
        this.usb2 = value;
    }

    /**
     * Gets the value of the usb3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsb3() {
        return usb3;
    }

    /**
     * Sets the value of the usb3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsb3(String value) {
        this.usb3 = value;
    }

    /**
     * Gets the value of the csb0 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCsb0() {
        return csb0;
    }

    /**
     * Sets the value of the csb0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCsb0(String value) {
        this.csb0 = value;
    }

    /**
     * Gets the value of the csb1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCsb1() {
        return csb1;
    }

    /**
     * Sets the value of the csb1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCsb1(String value) {
        this.csb1 = value;
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
