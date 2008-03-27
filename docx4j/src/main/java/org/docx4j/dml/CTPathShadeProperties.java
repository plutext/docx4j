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
 * <p>Java class for CT_PathShadeProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PathShadeProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fillToRect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_RelativeRect" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="path" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PathShadeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PathShadeProperties", propOrder = {
    "fillToRect"
})
public class CTPathShadeProperties {

    protected CTRelativeRect fillToRect;
    @XmlAttribute
    protected STPathShadeType path;

    /**
     * Gets the value of the fillToRect property.
     * 
     * @return
     *     possible object is
     *     {@link CTRelativeRect }
     *     
     */
    public CTRelativeRect getFillToRect() {
        return fillToRect;
    }

    /**
     * Sets the value of the fillToRect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRelativeRect }
     *     
     */
    public void setFillToRect(CTRelativeRect value) {
        this.fillToRect = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link STPathShadeType }
     *     
     */
    public STPathShadeType getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPathShadeType }
     *     
     */
    public void setPath(STPathShadeType value) {
        this.path = value;
    }

}
