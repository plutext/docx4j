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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ColorSchemeAndMapping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorSchemeAndMapping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clrScheme" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorScheme"/>
 *         &lt;element name="clrMap" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMapping" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorSchemeAndMapping", propOrder = {
    "clrScheme",
    "clrMap"
})
public class CTColorSchemeAndMapping {

    @XmlElement(required = true)
    protected CTColorScheme clrScheme;
    protected CTColorMapping clrMap;

    /**
     * Gets the value of the clrScheme property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorScheme }
     *     
     */
    public CTColorScheme getClrScheme() {
        return clrScheme;
    }

    /**
     * Sets the value of the clrScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorScheme }
     *     
     */
    public void setClrScheme(CTColorScheme value) {
        this.clrScheme = value;
    }

    /**
     * Gets the value of the clrMap property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getClrMap() {
        return clrMap;
    }

    /**
     * Sets the value of the clrMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setClrMap(CTColorMapping value) {
        this.clrMap = value;
    }

}
