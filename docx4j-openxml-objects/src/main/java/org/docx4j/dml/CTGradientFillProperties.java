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
 * <p>Java class for CT_GradientFillProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GradientFillProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="gsLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GradientStopList" minOccurs="0"/&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_ShadeProperties" minOccurs="0"/&gt;
 *         &lt;element name="tileRect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_RelativeRect" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="flip" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_TileFlipMode" /&gt;
 *       &lt;attribute name="rotWithShape" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GradientFillProperties", propOrder = {
    "gsLst",
    "lin",
    "path",
    "tileRect"
})
public class CTGradientFillProperties implements Child
{

    protected CTGradientStopList gsLst;
    protected CTLinearShadeProperties lin;
    protected CTPathShadeProperties path;
    protected CTRelativeRect tileRect;
    @XmlAttribute(name = "flip")
    protected STTileFlipMode flip;
    @XmlAttribute(name = "rotWithShape")
    protected Boolean rotWithShape;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the gsLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTGradientStopList }
     *     
     */
    public CTGradientStopList getGsLst() {
        return gsLst;
    }

    /**
     * Sets the value of the gsLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGradientStopList }
     *     
     */
    public void setGsLst(CTGradientStopList value) {
        this.gsLst = value;
    }

    /**
     * Gets the value of the lin property.
     * 
     * @return
     *     possible object is
     *     {@link CTLinearShadeProperties }
     *     
     */
    public CTLinearShadeProperties getLin() {
        return lin;
    }

    /**
     * Sets the value of the lin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLinearShadeProperties }
     *     
     */
    public void setLin(CTLinearShadeProperties value) {
        this.lin = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link CTPathShadeProperties }
     *     
     */
    public CTPathShadeProperties getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPathShadeProperties }
     *     
     */
    public void setPath(CTPathShadeProperties value) {
        this.path = value;
    }

    /**
     * Gets the value of the tileRect property.
     * 
     * @return
     *     possible object is
     *     {@link CTRelativeRect }
     *     
     */
    public CTRelativeRect getTileRect() {
        return tileRect;
    }

    /**
     * Sets the value of the tileRect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRelativeRect }
     *     
     */
    public void setTileRect(CTRelativeRect value) {
        this.tileRect = value;
    }

    /**
     * Gets the value of the flip property.
     * 
     * @return
     *     possible object is
     *     {@link STTileFlipMode }
     *     
     */
    public STTileFlipMode getFlip() {
        return flip;
    }

    /**
     * Sets the value of the flip property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTileFlipMode }
     *     
     */
    public void setFlip(STTileFlipMode value) {
        this.flip = value;
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
