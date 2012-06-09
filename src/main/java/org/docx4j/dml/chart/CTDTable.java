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
 
package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;


/**
 * <p>Java class for CT_DTable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DTable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="showHorzBorder" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="showVertBorder" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="showOutline" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="showKeys" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/>
 *         &lt;element name="txPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/>
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
@XmlType(name = "CT_DTable", propOrder = {
    "showHorzBorder",
    "showVertBorder",
    "showOutline",
    "showKeys",
    "spPr",
    "txPr",
    "extLst"
})
public class CTDTable {

    protected CTBoolean showHorzBorder;
    protected CTBoolean showVertBorder;
    protected CTBoolean showOutline;
    protected CTBoolean showKeys;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the showHorzBorder property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowHorzBorder() {
        return showHorzBorder;
    }

    /**
     * Sets the value of the showHorzBorder property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowHorzBorder(CTBoolean value) {
        this.showHorzBorder = value;
    }

    /**
     * Gets the value of the showVertBorder property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowVertBorder() {
        return showVertBorder;
    }

    /**
     * Sets the value of the showVertBorder property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowVertBorder(CTBoolean value) {
        this.showVertBorder = value;
    }

    /**
     * Gets the value of the showOutline property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowOutline() {
        return showOutline;
    }

    /**
     * Sets the value of the showOutline property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowOutline(CTBoolean value) {
        this.showOutline = value;
    }

    /**
     * Gets the value of the showKeys property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getShowKeys() {
        return showKeys;
    }

    /**
     * Sets the value of the showKeys property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setShowKeys(CTBoolean value) {
        this.showKeys = value;
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
     * Gets the value of the txPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxPr() {
        return txPr;
    }

    /**
     * Sets the value of the txPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxPr(CTTextBody value) {
        this.txPr = value;
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
