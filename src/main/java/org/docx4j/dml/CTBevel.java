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
 * <p>Java class for CT_Bevel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Bevel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="76200" />
 *       &lt;attribute name="h" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="76200" />
 *       &lt;attribute name="prst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BevelPresetType" default="circle" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Bevel")
public class CTBevel {

    @XmlAttribute
    protected Long w;
    @XmlAttribute
    protected Long h;
    @XmlAttribute
    protected STBevelPresetType prst;

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getW() {
        if (w == null) {
            return  76200L;
        } else {
            return w;
        }
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setW(Long value) {
        this.w = value;
    }

    /**
     * Gets the value of the h property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getH() {
        if (h == null) {
            return  76200L;
        } else {
            return h;
        }
    }

    /**
     * Sets the value of the h property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setH(Long value) {
        this.h = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STBevelPresetType }
     *     
     */
    public STBevelPresetType getPrst() {
        if (prst == null) {
            return STBevelPresetType.CIRCLE;
        } else {
            return prst;
        }
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBevelPresetType }
     *     
     */
    public void setPrst(STBevelPresetType value) {
        this.prst = value;
    }

}
