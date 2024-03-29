/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GradientFill complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GradientFill">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stop" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_GradientStop" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_GradientType" default="linear" />
 *       &lt;attribute name="degree" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="left" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="right" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="top" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *       &lt;attribute name="bottom" type="{http://www.w3.org/2001/XMLSchema}double" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GradientFill", propOrder = {
    "stop"
})
public class CTGradientFill implements Child
{

    protected List<CTGradientStop> stop;
    @XmlAttribute(name = "type")
    protected STGradientType type;
    @XmlAttribute(name = "degree")
    protected Double degree;
    @XmlAttribute(name = "left")
    protected Double left;
    @XmlAttribute(name = "right")
    protected Double right;
    @XmlAttribute(name = "top")
    protected Double top;
    @XmlAttribute(name = "bottom")
    protected Double bottom;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the stop property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stop property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStop().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGradientStop }
     * 
     * 
     */
    public List<CTGradientStop> getStop() {
        if (stop == null) {
            stop = new ArrayList<CTGradientStop>();
        }
        return this.stop;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STGradientType }
     *     
     */
    public STGradientType getType() {
        if (type == null) {
            return STGradientType.LINEAR;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STGradientType }
     *     
     */
    public void setType(STGradientType value) {
        this.type = value;
    }

    /**
     * Gets the value of the degree property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getDegree() {
        if (degree == null) {
            return  0.0D;
        } else {
            return degree;
        }
    }

    /**
     * Sets the value of the degree property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setDegree(Double value) {
        this.degree = value;
    }

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getLeft() {
        if (left == null) {
            return  0.0D;
        } else {
            return left;
        }
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLeft(Double value) {
        this.left = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getRight() {
        if (right == null) {
            return  0.0D;
        } else {
            return right;
        }
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRight(Double value) {
        this.right = value;
    }

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getTop() {
        if (top == null) {
            return  0.0D;
        } else {
            return top;
        }
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTop(Double value) {
        this.top = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getBottom() {
        if (bottom == null) {
            return  0.0D;
        } else {
            return bottom;
        }
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setBottom(Double value) {
        this.bottom = value;
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
