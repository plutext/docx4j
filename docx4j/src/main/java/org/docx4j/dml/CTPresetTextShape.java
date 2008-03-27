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
 * <p>Java class for CT_PresetTextShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PresetTextShape">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="avLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomGuideList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prst" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TextShapeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PresetTextShape", propOrder = {
    "avLst"
})
public class CTPresetTextShape {

    protected CTGeomGuideList avLst;
    @XmlAttribute(required = true)
    protected STTextShapeType prst;

    /**
     * Gets the value of the avLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeomGuideList }
     *     
     */
    public CTGeomGuideList getAvLst() {
        return avLst;
    }

    /**
     * Sets the value of the avLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeomGuideList }
     *     
     */
    public void setAvLst(CTGeomGuideList value) {
        this.avLst = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STTextShapeType }
     *     
     */
    public STTextShapeType getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTextShapeType }
     *     
     */
    public void setPrst(STTextShapeType value) {
        this.prst = value;
    }

}
