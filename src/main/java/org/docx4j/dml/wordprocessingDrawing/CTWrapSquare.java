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


package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_WrapSquare complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WrapSquare">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="effectExtent" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_EffectExtent" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="wrapText" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapText" />
 *       &lt;attribute name="distT" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distB" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distL" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distR" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WrapSquare", propOrder = {
    "effectExtent"
})
public class CTWrapSquare {

    protected CTEffectExtent effectExtent;
    @XmlAttribute(required = true)
    protected STWrapText wrapText;
    @XmlAttribute
    protected Long distT;
    @XmlAttribute
    protected Long distB;
    @XmlAttribute
    protected Long distL;
    @XmlAttribute
    protected Long distR;

    /**
     * Gets the value of the effectExtent property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectExtent }
     *     
     */
    public CTEffectExtent getEffectExtent() {
        return effectExtent;
    }

    /**
     * Sets the value of the effectExtent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectExtent }
     *     
     */
    public void setEffectExtent(CTEffectExtent value) {
        this.effectExtent = value;
    }

    /**
     * Gets the value of the wrapText property.
     * 
     * @return
     *     possible object is
     *     {@link STWrapText }
     *     
     */
    public STWrapText getWrapText() {
        return wrapText;
    }

    /**
     * Sets the value of the wrapText property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWrapText }
     *     
     */
    public void setWrapText(STWrapText value) {
        this.wrapText = value;
    }

    /**
     * Gets the value of the distT property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistT() {
        return distT;
    }

    /**
     * Sets the value of the distT property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistT(Long value) {
        this.distT = value;
    }

    /**
     * Gets the value of the distB property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistB() {
        return distB;
    }

    /**
     * Sets the value of the distB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistB(Long value) {
        this.distB = value;
    }

    /**
     * Gets the value of the distL property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistL() {
        return distL;
    }

    /**
     * Sets the value of the distL property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistL(Long value) {
        this.distL = value;
    }

    /**
     * Gets the value of the distR property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDistR() {
        return distR;
    }

    /**
     * Sets the value of the distR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDistR(Long value) {
        this.distR = value;
    }

}
