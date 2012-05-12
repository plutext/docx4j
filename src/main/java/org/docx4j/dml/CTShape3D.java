/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Shape3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape3D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bevelT" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Bevel" minOccurs="0"/>
 *         &lt;element name="bevelB" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Bevel" minOccurs="0"/>
 *         &lt;element name="extrusionClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="contourClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="z" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" default="0" />
 *       &lt;attribute name="extrusionH" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" />
 *       &lt;attribute name="contourW" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" />
 *       &lt;attribute name="prstMaterial" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetMaterialType" default="warmMatte" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shape3D", propOrder = {
    "bevelT",
    "bevelB",
    "extrusionClr",
    "contourClr",
    "extLst"
})
public class CTShape3D {

    protected CTBevel bevelT;
    protected CTBevel bevelB;
    protected CTColor extrusionClr;
    protected CTColor contourClr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute
    protected Long z;
    @XmlAttribute
    protected Long extrusionH;
    @XmlAttribute
    protected Long contourW;
    @XmlAttribute
    protected STPresetMaterialType prstMaterial;

    /**
     * Gets the value of the bevelT property.
     * 
     * @return
     *     possible object is
     *     {@link CTBevel }
     *     
     */
    public CTBevel getBevelT() {
        return bevelT;
    }

    /**
     * Sets the value of the bevelT property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBevel }
     *     
     */
    public void setBevelT(CTBevel value) {
        this.bevelT = value;
    }

    /**
     * Gets the value of the bevelB property.
     * 
     * @return
     *     possible object is
     *     {@link CTBevel }
     *     
     */
    public CTBevel getBevelB() {
        return bevelB;
    }

    /**
     * Sets the value of the bevelB property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBevel }
     *     
     */
    public void setBevelB(CTBevel value) {
        this.bevelB = value;
    }

    /**
     * Gets the value of the extrusionClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getExtrusionClr() {
        return extrusionClr;
    }

    /**
     * Sets the value of the extrusionClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setExtrusionClr(CTColor value) {
        this.extrusionClr = value;
    }

    /**
     * Gets the value of the contourClr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getContourClr() {
        return contourClr;
    }

    /**
     * Sets the value of the contourClr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setContourClr(CTColor value) {
        this.contourClr = value;
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

    /**
     * Gets the value of the z property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getZ() {
        if (z == null) {
            return  0L;
        } else {
            return z;
        }
    }

    /**
     * Sets the value of the z property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setZ(Long value) {
        this.z = value;
    }

    /**
     * Gets the value of the extrusionH property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getExtrusionH() {
        if (extrusionH == null) {
            return  0L;
        } else {
            return extrusionH;
        }
    }

    /**
     * Sets the value of the extrusionH property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExtrusionH(Long value) {
        this.extrusionH = value;
    }

    /**
     * Gets the value of the contourW property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getContourW() {
        if (contourW == null) {
            return  0L;
        } else {
            return contourW;
        }
    }

    /**
     * Sets the value of the contourW property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setContourW(Long value) {
        this.contourW = value;
    }

    /**
     * Gets the value of the prstMaterial property.
     * 
     * @return
     *     possible object is
     *     {@link STPresetMaterialType }
     *     
     */
    public STPresetMaterialType getPrstMaterial() {
        if (prstMaterial == null) {
            return STPresetMaterialType.WARM_MATTE;
        } else {
            return prstMaterial;
        }
    }

    /**
     * Sets the value of the prstMaterial property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPresetMaterialType }
     *     
     */
    public void setPrstMaterial(STPresetMaterialType value) {
        this.prstMaterial = value;
    }

}
