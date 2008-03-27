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
 * <p>Java class for CT_Backdrop complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Backdrop">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="anchor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point3D"/>
 *         &lt;element name="norm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Vector3D"/>
 *         &lt;element name="up" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Vector3D"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Backdrop", propOrder = {
    "anchor",
    "norm",
    "up",
    "extLst"
})
public class CTBackdrop {

    @XmlElement(required = true)
    protected CTPoint3D anchor;
    @XmlElement(required = true)
    protected CTVector3D norm;
    @XmlElement(required = true)
    protected CTVector3D up;
    protected CTOfficeArtExtensionList extLst;

    /**
     * Gets the value of the anchor property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint3D }
     *     
     */
    public CTPoint3D getAnchor() {
        return anchor;
    }

    /**
     * Sets the value of the anchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint3D }
     *     
     */
    public void setAnchor(CTPoint3D value) {
        this.anchor = value;
    }

    /**
     * Gets the value of the norm property.
     * 
     * @return
     *     possible object is
     *     {@link CTVector3D }
     *     
     */
    public CTVector3D getNorm() {
        return norm;
    }

    /**
     * Sets the value of the norm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVector3D }
     *     
     */
    public void setNorm(CTVector3D value) {
        this.norm = value;
    }

    /**
     * Gets the value of the up property.
     * 
     * @return
     *     possible object is
     *     {@link CTVector3D }
     *     
     */
    public CTVector3D getUp() {
        return up;
    }

    /**
     * Sets the value of the up property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVector3D }
     *     
     */
    public void setUp(CTVector3D value) {
        this.up = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

}
