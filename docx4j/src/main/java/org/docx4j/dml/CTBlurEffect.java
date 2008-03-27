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
 * <p>Java class for CT_BlurEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BlurEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="rad" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" />
 *       &lt;attribute name="grow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BlurEffect")
public class CTBlurEffect {

    @XmlAttribute
    protected Long rad;
    @XmlAttribute
    protected Boolean grow;

    /**
     * Gets the value of the rad property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getRad() {
        if (rad == null) {
            return  0L;
        } else {
            return rad;
        }
    }

    /**
     * Sets the value of the rad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRad(Long value) {
        this.rad = value;
    }

    /**
     * Gets the value of the grow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGrow() {
        if (grow == null) {
            return true;
        } else {
            return grow;
        }
    }

    /**
     * Sets the value of the grow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGrow(Boolean value) {
        this.grow = value;
    }

}
