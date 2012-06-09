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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;


/**
 * <p>Java class for CT_ErrBars complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ErrBars">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="errDir" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ErrDir" minOccurs="0"/>
 *         &lt;element name="errBarType" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ErrBarType"/>
 *         &lt;element name="errValType" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ErrValType"/>
 *         &lt;element name="noEndCap" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/>
 *         &lt;element name="plus" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumDataSource" minOccurs="0"/>
 *         &lt;element name="minus" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_NumDataSource" minOccurs="0"/>
 *         &lt;element name="val" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Double" minOccurs="0"/>
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/>
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
@XmlType(name = "CT_ErrBars", propOrder = {
    "errDir",
    "errBarType",
    "errValType",
    "noEndCap",
    "plus",
    "minus",
    "val",
    "spPr",
    "extLst"
})
public class CTErrBars {

    protected CTErrDir errDir;
    @XmlElement(required = true)
    protected CTErrBarType errBarType;
    @XmlElement(required = true)
    protected CTErrValType errValType;
    protected CTBoolean noEndCap;
    protected CTNumDataSource plus;
    protected CTNumDataSource minus;
    protected CTDouble val;
    protected CTShapeProperties spPr;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the errDir property.
     * 
     * @return
     *     possible object is
     *     {@link CTErrDir }
     *     
     */
    public CTErrDir getErrDir() {
        return errDir;
    }

    /**
     * Sets the value of the errDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTErrDir }
     *     
     */
    public void setErrDir(CTErrDir value) {
        this.errDir = value;
    }

    /**
     * Gets the value of the errBarType property.
     * 
     * @return
     *     possible object is
     *     {@link CTErrBarType }
     *     
     */
    public CTErrBarType getErrBarType() {
        return errBarType;
    }

    /**
     * Sets the value of the errBarType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTErrBarType }
     *     
     */
    public void setErrBarType(CTErrBarType value) {
        this.errBarType = value;
    }

    /**
     * Gets the value of the errValType property.
     * 
     * @return
     *     possible object is
     *     {@link CTErrValType }
     *     
     */
    public CTErrValType getErrValType() {
        return errValType;
    }

    /**
     * Sets the value of the errValType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTErrValType }
     *     
     */
    public void setErrValType(CTErrValType value) {
        this.errValType = value;
    }

    /**
     * Gets the value of the noEndCap property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getNoEndCap() {
        return noEndCap;
    }

    /**
     * Sets the value of the noEndCap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setNoEndCap(CTBoolean value) {
        this.noEndCap = value;
    }

    /**
     * Gets the value of the plus property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumDataSource }
     *     
     */
    public CTNumDataSource getPlus() {
        return plus;
    }

    /**
     * Sets the value of the plus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumDataSource }
     *     
     */
    public void setPlus(CTNumDataSource value) {
        this.plus = value;
    }

    /**
     * Gets the value of the minus property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumDataSource }
     *     
     */
    public CTNumDataSource getMinus() {
        return minus;
    }

    /**
     * Sets the value of the minus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumDataSource }
     *     
     */
    public void setMinus(CTNumDataSource value) {
        this.minus = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link CTDouble }
     *     
     */
    public CTDouble getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDouble }
     *     
     */
    public void setVal(CTDouble value) {
        this.val = value;
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
