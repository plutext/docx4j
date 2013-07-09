/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14; 

import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * <p>Java class for CT_Props3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Props3D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bevelT" type="{http://schemas.microsoft.com/office/word/2010/wordml}CT_Bevel" minOccurs="0"/>
 *         &lt;element name="bevelB" type="{http://schemas.microsoft.com/office/word/2010/wordml}CT_Bevel" minOccurs="0"/>
 *         &lt;element name="extrusionClr" type="{http://schemas.microsoft.com/office/word/2010/wordml}CT_Color" minOccurs="0"/>
 *         &lt;element name="contourClr" type="{http://schemas.microsoft.com/office/word/2010/wordml}CT_Color" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="extrusionH" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" />
 *       &lt;attribute name="contourW" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" />
 *       &lt;attribute name="prstMaterial" type="{http://schemas.microsoft.com/office/word/2010/wordml}ST_PresetMaterialType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Props3D", propOrder = {
    "bevelT",
    "bevelB",
    "extrusionClr",
    "contourClr"
})
public class CTProps3D implements Child
{

    protected CTBevel bevelT;
    protected CTBevel bevelB;
    protected CTColor extrusionClr;
    protected CTColor contourClr;
    @XmlAttribute(name = "extrusionH", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Long extrusionH;
    @XmlAttribute(name = "contourW", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected Long contourW;
    @XmlAttribute(name = "prstMaterial", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
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
     * Gets the value of the extrusionH property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExtrusionH() {
        return extrusionH;
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
    public Long getContourW() {
        return contourW;
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
        return prstMaterial;
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
