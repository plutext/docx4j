/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml.spreadsheetdrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;


/**
 * <p>Java class for CT_Connector complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Connector">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nvCxnSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing}CT_ConnectorNonVisual"/>
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/>
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="macro" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fPublished" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Connector", propOrder = {
    "nvCxnSpPr",
    "spPr",
    "style"
})
public class CTConnector {

    @XmlElement(required = true)
    protected CTConnectorNonVisual nvCxnSpPr;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    protected CTShapeStyle style;
    @XmlAttribute
    protected String macro;
    @XmlAttribute
    protected Boolean fPublished;

    /**
     * Gets the value of the nvCxnSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnectorNonVisual }
     *     
     */
    public CTConnectorNonVisual getNvCxnSpPr() {
        return nvCxnSpPr;
    }

    /**
     * Sets the value of the nvCxnSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnectorNonVisual }
     *     
     */
    public void setNvCxnSpPr(CTConnectorNonVisual value) {
        this.nvCxnSpPr = value;
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
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeStyle }
     *     
     */
    public CTShapeStyle getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeStyle }
     *     
     */
    public void setStyle(CTShapeStyle value) {
        this.style = value;
    }

    /**
     * Gets the value of the macro property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacro() {
        return macro;
    }

    /**
     * Sets the value of the macro property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacro(String value) {
        this.macro = value;
    }

    /**
     * Gets the value of the fPublished property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFPublished() {
        if (fPublished == null) {
            return false;
        } else {
            return fPublished;
        }
    }

    /**
     * Sets the value of the fPublished property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFPublished(Boolean value) {
        this.fPublished = value;
    }

}
