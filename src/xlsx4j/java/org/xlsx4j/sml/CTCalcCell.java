/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_CalcCell complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CalcCell">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="r" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="i" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="s" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="l" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="t" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="a" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CalcCell")
public class CTCalcCell {

    @XmlAttribute(required = true)
    protected String r;
    @XmlAttribute
    protected Integer i;
    @XmlAttribute
    protected Boolean s;
    @XmlAttribute
    protected Boolean l;
    @XmlAttribute
    protected Boolean t;
    @XmlAttribute
    protected Boolean a;

    /**
     * Gets the value of the r property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR(String value) {
        this.r = value;
    }

    /**
     * Gets the value of the i property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getI() {
        if (i == null) {
            return  0;
        } else {
            return i;
        }
    }

    /**
     * Sets the value of the i property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setI(Integer value) {
        this.i = value;
    }

    /**
     * Gets the value of the s property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isS() {
        if (s == null) {
            return false;
        } else {
            return s;
        }
    }

    /**
     * Sets the value of the s property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setS(Boolean value) {
        this.s = value;
    }

    /**
     * Gets the value of the l property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isL() {
        if (l == null) {
            return false;
        } else {
            return l;
        }
    }

    /**
     * Sets the value of the l property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setL(Boolean value) {
        this.l = value;
    }

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isT() {
        if (t == null) {
            return false;
        } else {
            return t;
        }
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setT(Boolean value) {
        this.t = value;
    }

    /**
     * Gets the value of the a property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isA() {
        if (a == null) {
            return false;
        } else {
            return a;
        }
    }

    /**
     * Sets the value of the a property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setA(Boolean value) {
        this.a = value;
    }

}
