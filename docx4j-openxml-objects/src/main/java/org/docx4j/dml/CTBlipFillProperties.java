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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_BlipFillProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BlipFillProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="blip" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Blip" minOccurs="0"/>
 *         &lt;element name="srcRect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_RelativeRect" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillModeProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dpi" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rotWithShape" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BlipFillProperties", propOrder = {
    "blip",
    "srcRect",
    "tile",
    "stretch"
})
public class CTBlipFillProperties {

    protected CTBlip blip;
    protected CTRelativeRect srcRect;
    protected CTTileInfoProperties tile;
    protected CTStretchInfoProperties stretch;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long dpi;
    @XmlAttribute
    protected Boolean rotWithShape;

    /**
     * Gets the value of the blip property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlip }
     *     
     */
    public CTBlip getBlip() {
        return blip;
    }

    /**
     * Sets the value of the blip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlip }
     *     
     */
    public void setBlip(CTBlip value) {
        this.blip = value;
    }

    /**
     * Gets the value of the srcRect property.
     * 
     * @return
     *     possible object is
     *     {@link CTRelativeRect }
     *     
     */
    public CTRelativeRect getSrcRect() {
        return srcRect;
    }

    /**
     * Sets the value of the srcRect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRelativeRect }
     *     
     */
    public void setSrcRect(CTRelativeRect value) {
        this.srcRect = value;
    }

    /**
     * Gets the value of the tile property.
     * 
     * @return
     *     possible object is
     *     {@link CTTileInfoProperties }
     *     
     */
    public CTTileInfoProperties getTile() {
        return tile;
    }

    /**
     * Sets the value of the tile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTileInfoProperties }
     *     
     */
    public void setTile(CTTileInfoProperties value) {
        this.tile = value;
    }

    /**
     * Gets the value of the stretch property.
     * 
     * @return
     *     possible object is
     *     {@link CTStretchInfoProperties }
     *     
     */
    public CTStretchInfoProperties getStretch() {
        return stretch;
    }

    /**
     * Sets the value of the stretch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStretchInfoProperties }
     *     
     */
    public void setStretch(CTStretchInfoProperties value) {
        this.stretch = value;
    }

    /**
     * Gets the value of the dpi property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDpi() {
        return dpi;
    }

    /**
     * Sets the value of the dpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDpi(Long value) {
        this.dpi = value;
    }

    /**
     * Gets the value of the rotWithShape property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRotWithShape() {
        return rotWithShape;
    }

    /**
     * Sets the value of the rotWithShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRotWithShape(Boolean value) {
        this.rotWithShape = value;
    }

}
