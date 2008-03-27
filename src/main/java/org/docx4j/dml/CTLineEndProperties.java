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
 * <p>Java class for CT_LineEndProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LineEndProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineEndType" />
 *       &lt;attribute name="w" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineEndWidth" />
 *       &lt;attribute name="len" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LineEndLength" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LineEndProperties")
public class CTLineEndProperties {

    @XmlAttribute
    protected STLineEndType type;
    @XmlAttribute
    protected STLineEndWidth w;
    @XmlAttribute
    protected STLineEndLength len;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STLineEndType }
     *     
     */
    public STLineEndType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineEndType }
     *     
     */
    public void setType(STLineEndType value) {
        this.type = value;
    }

    /**
     * Gets the value of the w property.
     * 
     * @return
     *     possible object is
     *     {@link STLineEndWidth }
     *     
     */
    public STLineEndWidth getW() {
        return w;
    }

    /**
     * Sets the value of the w property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineEndWidth }
     *     
     */
    public void setW(STLineEndWidth value) {
        this.w = value;
    }

    /**
     * Gets the value of the len property.
     * 
     * @return
     *     possible object is
     *     {@link STLineEndLength }
     *     
     */
    public STLineEndLength getLen() {
        return len;
    }

    /**
     * Sets the value of the len property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLineEndLength }
     *     
     */
    public void setLen(STLineEndLength value) {
        this.len = value;
    }

}
