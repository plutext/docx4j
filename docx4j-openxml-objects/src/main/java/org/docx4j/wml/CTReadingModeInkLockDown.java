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

import java.math.BigInteger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ReadingModeInkLockDown complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ReadingModeInkLockDown">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="actualPg" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="w" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PixelsMeasure" />
 *       &lt;attribute name="h" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_PixelsMeasure" />
 *       &lt;attribute name="fontSz" use="required" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ReadingModeInkLockDown")
public class CTReadingModeInkLockDown implements Child
{

    @XmlAttribute(name = "actualPg", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected boolean actualPg;
    @XmlAttribute(name = "w", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected BigInteger w;
    @XmlAttribute(name = "h", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected BigInteger h;
    @XmlAttribute(name = "fontSz", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    protected BigInteger fontSz;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the actualPg property.
     * 
     */
    public boolean isActualPg() {
        return actualPg;
    }

    /**
     * Sets the value of the actualPg property.
     * 
     */
    public void setActualPg(boolean value) {
        this.actualPg = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setW(BigInteger value) {
        this.w = value;
    }

    /**
     * Gets the value of the h property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getH() {
        return h;
    }

    /**
     * Sets the value of the h property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setH(BigInteger value) {
        this.h = value;
    }

    /**
     * Gets the value of the fontSz property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFontSz() {
        return fontSz;
    }

    /**
     * Sets the value of the fontSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFontSz(BigInteger value) {
        this.fontSz = value;
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
