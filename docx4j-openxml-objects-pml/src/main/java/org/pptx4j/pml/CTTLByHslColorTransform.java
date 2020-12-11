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
 * <p>Java class for CT_TLByHslColorTransform complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TLByHslColorTransform"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="h" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" /&gt;
 *       &lt;attribute name="s" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedPercentage" /&gt;
 *       &lt;attribute name="l" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedPercentage" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TLByHslColorTransform")
public class CTTLByHslColorTransform implements Child
{

    @XmlAttribute(name = "h", required = true)
    protected int h;
    @XmlAttribute(name = "s", required = true)
    protected int s;
    @XmlAttribute(name = "l", required = true)
    protected int l;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the h property.
     * 
     */
    public int getH() {
        return h;
    }

    /**
     * Sets the value of the h property.
     * 
     */
    public void setH(int value) {
        this.h = value;
    }

    /**
     * Gets the value of the s property.
     * 
     */
    public int getS() {
        return s;
    }

    /**
     * Sets the value of the s property.
     * 
     */
    public void setS(int value) {
        this.s = value;
    }

    /**
     * Gets the value of the l property.
     * 
     */
    public int getL() {
        return l;
    }

    /**
     * Sets the value of the l property.
     * 
     */
    public void setL(int value) {
        this.l = value;
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
