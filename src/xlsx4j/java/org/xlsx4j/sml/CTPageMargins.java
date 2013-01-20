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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PageMargins complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PageMargins">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="left" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="right" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="top" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="bottom" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="header" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="footer" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PageMargins")
public class CTPageMargins implements Child
{

    @XmlAttribute(name = "left", required = true)
    protected double left;
    @XmlAttribute(name = "right", required = true)
    protected double right;
    @XmlAttribute(name = "top", required = true)
    protected double top;
    @XmlAttribute(name = "bottom", required = true)
    protected double bottom;
    @XmlAttribute(name = "header", required = true)
    protected double header;
    @XmlAttribute(name = "footer", required = true)
    protected double footer;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the left property.
     * 
     */
    public double getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     */
    public void setLeft(double value) {
        this.left = value;
    }

    /**
     * Gets the value of the right property.
     * 
     */
    public double getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     */
    public void setRight(double value) {
        this.right = value;
    }

    /**
     * Gets the value of the top property.
     * 
     */
    public double getTop() {
        return top;
    }

    /**
     * Sets the value of the top property.
     * 
     */
    public void setTop(double value) {
        this.top = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     */
    public double getBottom() {
        return bottom;
    }

    /**
     * Sets the value of the bottom property.
     * 
     */
    public void setBottom(double value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the header property.
     * 
     */
    public double getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     */
    public void setHeader(double value) {
        this.header = value;
    }

    /**
     * Gets the value of the footer property.
     * 
     */
    public double getFooter() {
        return footer;
    }

    /**
     * Sets the value of the footer property.
     * 
     */
    public void setFooter(double value) {
        this.footer = value;
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
