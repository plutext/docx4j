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


package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;


/**
 * <p>Java class for CT_DPt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DPt">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="idx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt"/>
 *         &lt;element name="invertIfNegative" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="marker" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Marker" minOccurs="0"/>
 *         &lt;element name="bubble3D" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="explosion" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" minOccurs="0"/>
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/>
 *         &lt;element name="pictureOptions" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PictureOptions" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DPt", propOrder = {
    "idx",
    "invertIfNegative",
    "marker",
    "bubble3D",
    "explosion",
    "spPr",
    "pictureOptions",
    "extLst"
})
public class CTDPt {

    @XmlElement(required = true)
    protected CTUnsignedInt idx;
    protected CTBoolean invertIfNegative;
    protected CTMarker marker;
    protected CTBoolean bubble3D;
    protected CTUnsignedInt explosion;
    protected CTShapeProperties spPr;
    protected CTPictureOptions pictureOptions;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the idx property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getIdx() {
        return idx;
    }

    /**
     * Sets the value of the idx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setIdx(CTUnsignedInt value) {
        this.idx = value;
    }

    /**
     * Gets the value of the invertIfNegative property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getInvertIfNegative() {
        return invertIfNegative;
    }

    /**
     * Sets the value of the invertIfNegative property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setInvertIfNegative(CTBoolean value) {
        this.invertIfNegative = value;
    }

    /**
     * Gets the value of the marker property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getMarker() {
        return marker;
    }

    /**
     * Sets the value of the marker property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setMarker(CTMarker value) {
        this.marker = value;
    }

    /**
     * Gets the value of the bubble3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getBubble3D() {
        return bubble3D;
    }

    /**
     * Sets the value of the bubble3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setBubble3D(CTBoolean value) {
        this.bubble3D = value;
    }

    /**
     * Gets the value of the explosion property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getExplosion() {
        return explosion;
    }

    /**
     * Sets the value of the explosion property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setExplosion(CTUnsignedInt value) {
        this.explosion = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the pictureOptions property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureOptions }
     *     
     */
    public CTPictureOptions getPictureOptions() {
        return pictureOptions;
    }

    /**
     * Sets the value of the pictureOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureOptions }
     *     
     */
    public void setPictureOptions(CTPictureOptions value) {
        this.pictureOptions = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

}
