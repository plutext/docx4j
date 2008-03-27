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
