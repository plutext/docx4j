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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_WrapThrough complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WrapThrough">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="wrapPolygon" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_WrapPath"/>
 *       &lt;/sequence>
 *       &lt;attribute name="wrapText" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapText" />
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
@XmlType(name = "CT_WrapThrough", propOrder = {
    "wrapPolygon"
})
public class CTWrapThrough {

    @XmlElement(required = true)
    protected CTWrapPath wrapPolygon;
    @XmlAttribute(required = true)
    protected STWrapText wrapText;
    @XmlAttribute
    protected Long distL;
    @XmlAttribute
    protected Long distR;

    /**
     * Gets the value of the wrapPolygon property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapPath }
     *     
     */
    public CTWrapPath getWrapPolygon() {
        return wrapPolygon;
    }

    /**
     * Sets the value of the wrapPolygon property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapPath }
     *     
     */
    public void setWrapPolygon(CTWrapPath value) {
        this.wrapPolygon = value;
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
