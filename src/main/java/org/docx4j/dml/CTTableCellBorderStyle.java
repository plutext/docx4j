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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TableCellBorderStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TableCellBorderStyle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="left" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="right" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="top" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="bottom" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="insideH" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="insideV" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="tl2br" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="tr2bl" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ThemeableLineStyle" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TableCellBorderStyle", propOrder = {
    "left",
    "right",
    "top",
    "bottom",
    "insideH",
    "insideV",
    "tl2Br",
    "tr2Bl",
    "extLst"
})
public class CTTableCellBorderStyle {

    protected CTThemeableLineStyle left;
    protected CTThemeableLineStyle right;
    protected CTThemeableLineStyle top;
    protected CTThemeableLineStyle bottom;
    protected CTThemeableLineStyle insideH;
    protected CTThemeableLineStyle insideV;
    @XmlElement(name = "tl2br")
    protected CTThemeableLineStyle tl2Br;
    @XmlElement(name = "tr2bl")
    protected CTThemeableLineStyle tr2Bl;
    protected CTOfficeArtExtensionList extLst;

    /**
     * Gets the value of the left property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getLeft() {
        return left;
    }

    /**
     * Sets the value of the left property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setLeft(CTThemeableLineStyle value) {
        this.left = value;
    }

    /**
     * Gets the value of the right property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getRight() {
        return right;
    }

    /**
     * Sets the value of the right property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setRight(CTThemeableLineStyle value) {
        this.right = value;
    }

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getTop() {
        return top;
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setTop(CTThemeableLineStyle value) {
        this.top = value;
    }

    /**
     * Gets the value of the bottom property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getBottom() {
        return bottom;
    }

    /**
     * Sets the value of the bottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setBottom(CTThemeableLineStyle value) {
        this.bottom = value;
    }

    /**
     * Gets the value of the insideH property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getInsideH() {
        return insideH;
    }

    /**
     * Sets the value of the insideH property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setInsideH(CTThemeableLineStyle value) {
        this.insideH = value;
    }

    /**
     * Gets the value of the insideV property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getInsideV() {
        return insideV;
    }

    /**
     * Sets the value of the insideV property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setInsideV(CTThemeableLineStyle value) {
        this.insideV = value;
    }

    /**
     * Gets the value of the tl2Br property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getTl2Br() {
        return tl2Br;
    }

    /**
     * Sets the value of the tl2Br property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setTl2Br(CTThemeableLineStyle value) {
        this.tl2Br = value;
    }

    /**
     * Gets the value of the tr2Bl property.
     * 
     * @return
     *     possible object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public CTThemeableLineStyle getTr2Bl() {
        return tr2Bl;
    }

    /**
     * Sets the value of the tr2Bl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTThemeableLineStyle }
     *     
     */
    public void setTr2Bl(CTThemeableLineStyle value) {
        this.tr2Bl = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

}
