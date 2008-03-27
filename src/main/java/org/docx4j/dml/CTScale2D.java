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
 * <p>Java class for CT_Scale2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Scale2D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/>
 *         &lt;element name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Scale2D", propOrder = {
    "sx",
    "sy"
})
public class CTScale2D {

    @XmlElement(required = true)
    protected CTRatio sx;
    @XmlElement(required = true)
    protected CTRatio sy;

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSx() {
        return sx;
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSx(CTRatio value) {
        this.sx = value;
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSy() {
        return sy;
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSy(CTRatio value) {
        this.sy = value;
    }

}
