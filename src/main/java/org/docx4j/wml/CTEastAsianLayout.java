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
 * <p>Java class for CT_EastAsianLayout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EastAsianLayout">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="combine" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="combineBrackets" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_CombineBrackets" />
 *       &lt;attribute name="vert" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="vertCompress" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EastAsianLayout")
public class CTEastAsianLayout implements Child
{

    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger id;
    @XmlAttribute(name = "combine", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean combine;
    @XmlAttribute(name = "combineBrackets", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STCombineBrackets combineBrackets;
    @XmlAttribute(name = "vert", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean vert;
    @XmlAttribute(name = "vertCompress", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean vertCompress;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the combine property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCombine() {
        if (combine == null) {
            return true;
        } else {
            return combine;
        }
    }

    /**
     * Sets the value of the combine property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCombine(Boolean value) {
        this.combine = value;
    }

    /**
     * Gets the value of the combineBrackets property.
     * 
     * @return
     *     possible object is
     *     {@link STCombineBrackets }
     *     
     */
    public STCombineBrackets getCombineBrackets() {
        return combineBrackets;
    }

    /**
     * Sets the value of the combineBrackets property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCombineBrackets }
     *     
     */
    public void setCombineBrackets(STCombineBrackets value) {
        this.combineBrackets = value;
    }

    /**
     * Gets the value of the vert property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVert() {
        if (vert == null) {
            return true;
        } else {
            return vert;
        }
    }

    /**
     * Sets the value of the vert property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVert(Boolean value) {
        this.vert = value;
    }

    /**
     * Gets the value of the vertCompress property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVertCompress() {
        if (vertCompress == null) {
            return true;
        } else {
            return vertCompress;
        }
    }

    /**
     * Sets the value of the vertCompress property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVertCompress(Boolean value) {
        this.vertCompress = value;
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
