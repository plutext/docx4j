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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ColorChangeEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorChangeEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clrFrom" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="clrTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *       &lt;/sequence>
 *       &lt;attribute name="useA" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorChangeEffect", propOrder = {
    "clrFrom",
    "clrTo"
})
public class CTColorChangeEffect {

    @XmlElement(required = true)
    protected CTColor clrFrom;
    @XmlElement(required = true)
    protected CTColor clrTo;
    @XmlAttribute
    protected Boolean useA;

    /**
     * Gets the value of the clrFrom property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClrFrom() {
        return clrFrom;
    }

    /**
     * Sets the value of the clrFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClrFrom(CTColor value) {
        this.clrFrom = value;
    }

    /**
     * Gets the value of the clrTo property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClrTo() {
        return clrTo;
    }

    /**
     * Sets the value of the clrTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClrTo(CTColor value) {
        this.clrTo = value;
    }

    /**
     * Gets the value of the useA property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseA() {
        if (useA == null) {
            return true;
        } else {
            return useA;
        }
    }

    /**
     * Sets the value of the useA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseA(Boolean value) {
        this.useA = value;
    }

}
