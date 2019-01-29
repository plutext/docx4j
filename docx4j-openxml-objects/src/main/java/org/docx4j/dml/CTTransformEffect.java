/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TransformEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TransformEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" />
 *       &lt;attribute name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" />
 *       &lt;attribute name="kx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" default="0" />
 *       &lt;attribute name="ky" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedAngle" default="0" />
 *       &lt;attribute name="tx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" default="0" />
 *       &lt;attribute name="ty" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TransformEffect")
public class CTTransformEffect {

    @XmlAttribute
    protected Integer sx;
    @XmlAttribute
    protected Integer sy;
    @XmlAttribute
    protected Integer kx;
    @XmlAttribute
    protected Integer ky;
    @XmlAttribute
    protected Long tx;
    @XmlAttribute
    protected Long ty;

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSx() {
        if (sx == null) {
            return  100000;
        } else {
            return sx;
        }
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSx(Integer value) {
        this.sx = value;
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSy() {
        if (sy == null) {
            return  100000;
        } else {
            return sy;
        }
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSy(Integer value) {
        this.sy = value;
    }

    /**
     * Gets the value of the kx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getKx() {
        if (kx == null) {
            return  0;
        } else {
            return kx;
        }
    }

    /**
     * Sets the value of the kx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKx(Integer value) {
        this.kx = value;
    }

    /**
     * Gets the value of the ky property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getKy() {
        if (ky == null) {
            return  0;
        } else {
            return ky;
        }
    }

    /**
     * Sets the value of the ky property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setKy(Integer value) {
        this.ky = value;
    }

    /**
     * Gets the value of the tx property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTx() {
        if (tx == null) {
            return  0L;
        } else {
            return tx;
        }
    }

    /**
     * Sets the value of the tx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTx(Long value) {
        this.tx = value;
    }

    /**
     * Gets the value of the ty property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTy() {
        if (ty == null) {
            return  0L;
        } else {
            return ty;
        }
    }

    /**
     * Sets the value of the ty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTy(Long value) {
        this.ty = value;
    }

}
