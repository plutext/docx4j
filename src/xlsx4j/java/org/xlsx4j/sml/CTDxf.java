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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Dxf complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Dxf">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="font" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Font" minOccurs="0"/>
 *         &lt;element name="numFmt" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_NumFmt" minOccurs="0"/>
 *         &lt;element name="fill" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Fill" minOccurs="0"/>
 *         &lt;element name="alignment" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CellAlignment" minOccurs="0"/>
 *         &lt;element name="border" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Border" minOccurs="0"/>
 *         &lt;element name="protection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CellProtection" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Dxf", propOrder = {
    "font",
    "numFmt",
    "fill",
    "alignment",
    "border",
    "protection",
    "extLst"
})
public class CTDxf implements Child
{

    protected CTFont font;
    protected CTNumFmt numFmt;
    protected CTFill fill;
    protected CTCellAlignment alignment;
    protected CTBorder border;
    protected CTCellProtection protection;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the font property.
     * 
     * @return
     *     possible object is
     *     {@link CTFont }
     *     
     */
    public CTFont getFont() {
        return font;
    }

    /**
     * Sets the value of the font property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFont }
     *     
     */
    public void setFont(CTFont value) {
        this.font = value;
    }

    /**
     * Gets the value of the numFmt property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumFmt }
     *     
     */
    public CTNumFmt getNumFmt() {
        return numFmt;
    }

    /**
     * Sets the value of the numFmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumFmt }
     *     
     */
    public void setNumFmt(CTNumFmt value) {
        this.numFmt = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link CTFill }
     *     
     */
    public CTFill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFill }
     *     
     */
    public void setFill(CTFill value) {
        this.fill = value;
    }

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
     * Gets the value of the border property.
     * 
     * @return
     *     possible object is
     *     {@link CTBorder }
     *     
     */
    public CTBorder getBorder() {
        return border;
    }

    /**
     * Sets the value of the border property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBorder }
     *     
     */
    public void setBorder(CTBorder value) {
        this.border = value;
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
