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
 * <p>Java class for CT_HSLEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_HSLEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="hue" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedAngle" default="0" />
 *       &lt;attribute name="sat" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedPercentage" default="0" />
 *       &lt;attribute name="lum" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedPercentage" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_HSLEffect")
public class CTHSLEffect {

    @XmlAttribute
    protected Integer hue;
    @XmlAttribute
    protected Integer sat;
    @XmlAttribute
    protected Integer lum;

    /**
     * Gets the value of the hue property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getHue() {
        if (hue == null) {
            return  0;
        } else {
            return hue;
        }
    }

    /**
     * Sets the value of the hue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHue(Integer value) {
        this.hue = value;
    }

    /**
     * Gets the value of the sat property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSat() {
        if (sat == null) {
            return  0;
        } else {
            return sat;
        }
    }

    /**
     * Sets the value of the sat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSat(Integer value) {
        this.sat = value;
    }

    /**
     * Gets the value of the lum property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getLum() {
        if (lum == null) {
            return  0;
        } else {
            return lum;
        }
    }

    /**
     * Sets the value of the lum property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLum(Integer value) {
        this.lum = value;
    }

}
