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
 * <p>Java class for CT_Camera complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Camera">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SphereCoords" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prst" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetCameraType" />
 *       &lt;attribute name="fov" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FOVAngle" />
 *       &lt;attribute name="zoom" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositivePercentage" default="100000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Camera", propOrder = {
    "rot"
})
public class CTCamera {

    protected CTSphereCoords rot;
    @XmlAttribute(required = true)
    protected STPresetCameraType prst;
    @XmlAttribute
    protected Integer fov;
    @XmlAttribute
    protected Integer zoom;

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link CTSphereCoords }
     *     
     */
    public CTSphereCoords getRot() {
        return rot;
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSphereCoords }
     *     
     */
    public void setRot(CTSphereCoords value) {
        this.rot = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STPresetCameraType }
     *     
     */
    public STPresetCameraType getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPresetCameraType }
     *     
     */
    public void setPrst(STPresetCameraType value) {
        this.prst = value;
    }

    /**
     * Gets the value of the fov property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFov() {
        return fov;
    }

    /**
     * Sets the value of the fov property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFov(Integer value) {
        this.fov = value;
    }

    /**
     * Gets the value of the zoom property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getZoom() {
        if (zoom == null) {
            return  100000;
        } else {
            return zoom;
        }
    }

    /**
     * Sets the value of the zoom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setZoom(Integer value) {
        this.zoom = value;
    }

}
