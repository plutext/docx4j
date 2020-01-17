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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TLIterateData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLIterateData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="tmAbs" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLIterateIntervalTime"/&gt;
 *         &lt;element name="tmPct" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_TLIterateIntervalPercentage"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_IterateType" default="el" /&gt;
 *       &lt;attribute name="backwards" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLIterateData", propOrder = {
    "tmAbs",
    "tmPct"
})
public class CTTLIterateData implements Child
{

    protected CTTLIterateIntervalTime tmAbs;
    protected CTTLIterateIntervalPercentage tmPct;
    @XmlAttribute(name = "type")
    protected STIterateType type;
    @XmlAttribute(name = "backwards")
    protected Boolean backwards;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tmAbs property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLIterateIntervalTime }
     *     
     */
    public CTTLIterateIntervalTime getTmAbs() {
        return tmAbs;
    }

    /**
     * Sets the value of the tmAbs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLIterateIntervalTime }
     *     
     */
    public void setTmAbs(CTTLIterateIntervalTime value) {
        this.tmAbs = value;
    }

    /**
     * Gets the value of the tmPct property.
     * 
     * @return
     *     possible object is
     *     {@link CTTLIterateIntervalPercentage }
     *     
     */
    public CTTLIterateIntervalPercentage getTmPct() {
        return tmPct;
    }

    /**
     * Sets the value of the tmPct property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTLIterateIntervalPercentage }
     *     
     */
    public void setTmPct(CTTLIterateIntervalPercentage value) {
        this.tmPct = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STIterateType }
     *     
     */
    public STIterateType getType() {
        if (type == null) {
            return STIterateType.EL;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STIterateType }
     *     
     */
    public void setType(STIterateType value) {
        this.type = value;
    }

    /**
     * Gets the value of the backwards property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBackwards() {
        if (backwards == null) {
            return false;
        } else {
            return backwards;
        }
    }

    /**
     * Sets the value of the backwards property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBackwards(Boolean value) {
        this.backwards = value;
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
