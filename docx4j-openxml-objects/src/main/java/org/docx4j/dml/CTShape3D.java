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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shape3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="bevelT" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Bevel" minOccurs="0"/&gt;
 *         &lt;element name="bevelB" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Bevel" minOccurs="0"/&gt;
 *         &lt;element name="extrusionClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/&gt;
 *         &lt;element name="contourClr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="z" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" default="0" /&gt;
 *       &lt;attribute name="extrusionH" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="contourW" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" default="0" /&gt;
 *       &lt;attribute name="prstMaterial" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetMaterialType" default="warmMatte" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
public class CTShape3D implements Child
{

    protected CTBevel bevelT;
    protected CTBevel bevelB;
    protected CTColor extrusionClr;
    protected CTColor contourClr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "z")
    protected Long z;
    @XmlAttribute(name = "extrusionH")
    protected Long extrusionH;
    @XmlAttribute(name = "contourW")
    protected Long contourW;
    @XmlAttribute(name = "prstMaterial")
    protected STPresetMaterialType prstMaterial;
    @XmlTransient
    private Object parent;

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

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
