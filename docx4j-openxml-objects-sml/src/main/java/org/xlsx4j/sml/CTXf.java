/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>A single xf (format) element describes all of the formatting for a cell.
 * 
 * <p>Java class for CT_Xf complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Xf">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="alignment" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CellAlignment" minOccurs="0"/>
 *         &lt;element name="protection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CellProtection" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="numFmtId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_NumFmtId" />
 *       &lt;attribute name="fontId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FontId" />
 *       &lt;attribute name="fillId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FillId" />
 *       &lt;attribute name="borderId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_BorderId" />
 *       &lt;attribute name="xfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellStyleXfId" />
 *       &lt;attribute name="quotePrefix" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pivotButton" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="applyNumberFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="applyFont" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="applyFill" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="applyBorder" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="applyAlignment" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="applyProtection" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Xf", propOrder = {
    "alignment",
    "protection",
    "extLst"
})
public class CTXf implements Child
{

    protected CTCellAlignment alignment;
    protected CTCellProtection protection;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "numFmtId")
    protected Long numFmtId;
    @XmlAttribute(name = "fontId")
    protected Long fontId;
    @XmlAttribute(name = "fillId")
    protected Long fillId;
    @XmlAttribute(name = "borderId")
    protected Long borderId;
    @XmlAttribute(name = "xfId")
    protected Long xfId;
    @XmlAttribute(name = "quotePrefix")
    protected Boolean quotePrefix;
    @XmlAttribute(name = "pivotButton")
    protected Boolean pivotButton;
    @XmlAttribute(name = "applyNumberFormat")
    protected Boolean applyNumberFormat;
    @XmlAttribute(name = "applyFont")
    protected Boolean applyFont;
    @XmlAttribute(name = "applyFill")
    protected Boolean applyFill;
    @XmlAttribute(name = "applyBorder")
    protected Boolean applyBorder;
    @XmlAttribute(name = "applyAlignment")
    protected Boolean applyAlignment;
    @XmlAttribute(name = "applyProtection")
    protected Boolean applyProtection;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the alignment property.
     * 
     * @return
     *     possible object is
     *     {@link CTCellAlignment }
     *     
     */
    public CTCellAlignment getAlignment() {
        return alignment;
    }

    /**
     * Sets the value of the alignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCellAlignment }
     *     
     */
    public void setAlignment(CTCellAlignment value) {
        this.alignment = value;
    }

    /**
     * Gets the value of the protection property.
     * 
     * @return
     *     possible object is
     *     {@link CTCellProtection }
     *     
     */
    public CTCellProtection getProtection() {
        return protection;
    }

    /**
     * Sets the value of the protection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCellProtection }
     *     
     */
    public void setProtection(CTCellProtection value) {
        this.protection = value;
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

    /**
     * Gets the value of the numFmtId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getNumFmtId() {
        return numFmtId;
    }

    /**
     * Sets the value of the numFmtId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNumFmtId(Long value) {
        this.numFmtId = value;
    }

    /**
     * Gets the value of the fontId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFontId() {
        return fontId;
    }

    /**
     * Sets the value of the fontId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFontId(Long value) {
        this.fontId = value;
    }

    /**
     * Gets the value of the fillId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFillId() {
        return fillId;
    }

    /**
     * Sets the value of the fillId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFillId(Long value) {
        this.fillId = value;
    }

    /**
     * Gets the value of the borderId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBorderId() {
        return borderId;
    }

    /**
     * Sets the value of the borderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBorderId(Long value) {
        this.borderId = value;
    }

    /**
     * Gets the value of the xfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getXfId() {
        return xfId;
    }

    /**
     * Sets the value of the xfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setXfId(Long value) {
        this.xfId = value;
    }

    /**
     * Gets the value of the quotePrefix property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isQuotePrefix() {
        if (quotePrefix == null) {
            return false;
        } else {
            return quotePrefix;
        }
    }

    /**
     * Sets the value of the quotePrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setQuotePrefix(Boolean value) {
        this.quotePrefix = value;
    }

    /**
     * Gets the value of the pivotButton property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPivotButton() {
        if (pivotButton == null) {
            return false;
        } else {
            return pivotButton;
        }
    }

    /**
     * Sets the value of the pivotButton property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPivotButton(Boolean value) {
        this.pivotButton = value;
    }

    /**
     * Gets the value of the applyNumberFormat property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyNumberFormat() {
        return applyNumberFormat;
    }

    /**
     * Sets the value of the applyNumberFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyNumberFormat(Boolean value) {
        this.applyNumberFormat = value;
    }

    /**
     * Gets the value of the applyFont property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyFont() {
        return applyFont;
    }

    /**
     * Sets the value of the applyFont property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyFont(Boolean value) {
        this.applyFont = value;
    }

    /**
     * Gets the value of the applyFill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyFill() {
        return applyFill;
    }

    /**
     * Sets the value of the applyFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyFill(Boolean value) {
        this.applyFill = value;
    }

    /**
     * Gets the value of the applyBorder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyBorder() {
        return applyBorder;
    }

    /**
     * Sets the value of the applyBorder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyBorder(Boolean value) {
        this.applyBorder = value;
    }

    /**
     * Gets the value of the applyAlignment property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyAlignment() {
        return applyAlignment;
    }

    /**
     * Sets the value of the applyAlignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyAlignment(Boolean value) {
        this.applyAlignment = value;
    }

    /**
     * Gets the value of the applyProtection property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyProtection() {
        return applyProtection;
    }

    /**
     * Sets the value of the applyProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyProtection(Boolean value) {
        this.applyProtection = value;
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
