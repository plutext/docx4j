/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_RelativeRect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RelativeRect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="l" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" />
 *       &lt;attribute name="t" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" />
 *       &lt;attribute name="r" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" />
 *       &lt;attribute name="b" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RelativeRect")
public class CTRelativeRect {

    @XmlAttribute
    protected Integer l;
    @XmlAttribute
    protected Integer t;
    @XmlAttribute
    protected Integer r;
    @XmlAttribute
    protected Integer b;

    /**
     * Gets the value of the l property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getL() {
        if (l == null) {
            return  0;
        } else {
            return l;
        }
    }

    /**
     * Sets the value of the l property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setL(Integer value) {
        this.l = value;
    }

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getT() {
        if (t == null) {
            return  0;
        } else {
            return t;
        }
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setT(Integer value) {
        this.t = value;
    }

    /**
     * Gets the value of the r property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getR() {
        if (r == null) {
            return  0;
        } else {
            return r;
        }
    }

    /**
     * Sets the value of the r property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setR(Integer value) {
        this.r = value;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getB() {
        if (b == null) {
            return  0;
        } else {
            return b;
        }
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setB(Integer value) {
        this.b = value;
    }

}
