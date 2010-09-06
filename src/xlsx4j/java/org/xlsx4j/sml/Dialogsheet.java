/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Dialogsheet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Dialogsheet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetPr" minOccurs="0"/>
 *         &lt;element name="sheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetViews" minOccurs="0"/>
 *         &lt;element name="sheetFormatPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetFormatPr" minOccurs="0"/>
 *         &lt;element name="sheetProtection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetProtection" minOccurs="0"/>
 *         &lt;element name="customSheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomSheetViews" minOccurs="0"/>
 *         &lt;element name="printOptions" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PrintOptions" minOccurs="0"/>
 *         &lt;element name="pageMargins" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageMargins" minOccurs="0"/>
 *         &lt;element name="pageSetup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageSetup" minOccurs="0"/>
 *         &lt;element name="headerFooter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_HeaderFooter" minOccurs="0"/>
 *         &lt;element name="drawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Drawing" minOccurs="0"/>
 *         &lt;element name="legacyDrawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_LegacyDrawing" minOccurs="0"/>
 *         &lt;element name="legacyDrawingHF" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_LegacyDrawing" minOccurs="0"/>
 *         &lt;element name="oleObjects" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_OleObjects" minOccurs="0"/>
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
@XmlType(name = "CT_Dialogsheet", propOrder = {
    "sheetPr",
    "sheetViews",
    "sheetFormatPr",
    "sheetProtection",
    "customSheetViews",
    "printOptions",
    "pageMargins",
    "pageSetup",
    "headerFooter",
    "drawing",
    "legacyDrawing",
    "legacyDrawingHF",
    "oleObjects",
    "extLst"
})
public class Dialogsheet {

    protected CTSheetPr sheetPr;
    protected SheetViews sheetViews;
    protected CTSheetFormatPr sheetFormatPr;
    protected CTSheetProtection sheetProtection;
    protected CTCustomSheetViews customSheetViews;
    protected CTPrintOptions printOptions;
    protected CTPageMargins pageMargins;
    protected CTPageSetup pageSetup;
    protected CTHeaderFooter headerFooter;
    protected CTDrawing drawing;
    protected CTLegacyDrawing legacyDrawing;
    protected CTLegacyDrawing legacyDrawingHF;
    protected CTOleObjects oleObjects;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the sheetPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetPr }
     *     
     */
    public CTSheetPr getSheetPr() {
        return sheetPr;
    }

    /**
     * Sets the value of the sheetPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetPr }
     *     
     */
    public void setSheetPr(CTSheetPr value) {
        this.sheetPr = value;
    }

    /**
     * Gets the value of the sheetViews property.
     * 
     * @return
     *     possible object is
     *     {@link SheetViews }
     *     
     */
    public SheetViews getSheetViews() {
        return sheetViews;
    }

    /**
     * Sets the value of the sheetViews property.
     * 
     * @param value
     *     allowed object is
     *     {@link SheetViews }
     *     
     */
    public void setSheetViews(SheetViews value) {
        this.sheetViews = value;
    }

    /**
     * Gets the value of the sheetFormatPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetFormatPr }
     *     
     */
    public CTSheetFormatPr getSheetFormatPr() {
        return sheetFormatPr;
    }

    /**
     * Sets the value of the sheetFormatPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetFormatPr }
     *     
     */
    public void setSheetFormatPr(CTSheetFormatPr value) {
        this.sheetFormatPr = value;
    }

    /**
     * Gets the value of the sheetProtection property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetProtection }
     *     
     */
    public CTSheetProtection getSheetProtection() {
        return sheetProtection;
    }

    /**
     * Sets the value of the sheetProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetProtection }
     *     
     */
    public void setSheetProtection(CTSheetProtection value) {
        this.sheetProtection = value;
    }

    /**
     * Gets the value of the customSheetViews property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomSheetViews }
     *     
     */
    public CTCustomSheetViews getCustomSheetViews() {
        return customSheetViews;
    }

    /**
     * Sets the value of the customSheetViews property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomSheetViews }
     *     
     */
    public void setCustomSheetViews(CTCustomSheetViews value) {
        this.customSheetViews = value;
    }

    /**
     * Gets the value of the printOptions property.
     * 
     * @return
     *     possible object is
     *     {@link CTPrintOptions }
     *     
     */
    public CTPrintOptions getPrintOptions() {
        return printOptions;
    }

    /**
     * Sets the value of the printOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPrintOptions }
     *     
     */
    public void setPrintOptions(CTPrintOptions value) {
        this.printOptions = value;
    }

    /**
     * Gets the value of the pageMargins property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageMargins }
     *     
     */
    public CTPageMargins getPageMargins() {
        return pageMargins;
    }

    /**
     * Sets the value of the pageMargins property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageMargins }
     *     
     */
    public void setPageMargins(CTPageMargins value) {
        this.pageMargins = value;
    }

    /**
     * Gets the value of the pageSetup property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageSetup }
     *     
     */
    public CTPageSetup getPageSetup() {
        return pageSetup;
    }

    /**
     * Sets the value of the pageSetup property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageSetup }
     *     
     */
    public void setPageSetup(CTPageSetup value) {
        this.pageSetup = value;
    }

    /**
     * Gets the value of the headerFooter property.
     * 
     * @return
     *     possible object is
     *     {@link CTHeaderFooter }
     *     
     */
    public CTHeaderFooter getHeaderFooter() {
        return headerFooter;
    }

    /**
     * Sets the value of the headerFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHeaderFooter }
     *     
     */
    public void setHeaderFooter(CTHeaderFooter value) {
        this.headerFooter = value;
    }

    /**
     * Gets the value of the drawing property.
     * 
     * @return
     *     possible object is
     *     {@link CTDrawing }
     *     
     */
    public CTDrawing getDrawing() {
        return drawing;
    }

    /**
     * Sets the value of the drawing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDrawing }
     *     
     */
    public void setDrawing(CTDrawing value) {
        this.drawing = value;
    }

    /**
     * Gets the value of the legacyDrawing property.
     * 
     * @return
     *     possible object is
     *     {@link CTLegacyDrawing }
     *     
     */
    public CTLegacyDrawing getLegacyDrawing() {
        return legacyDrawing;
    }

    /**
     * Sets the value of the legacyDrawing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLegacyDrawing }
     *     
     */
    public void setLegacyDrawing(CTLegacyDrawing value) {
        this.legacyDrawing = value;
    }

    /**
     * Gets the value of the legacyDrawingHF property.
     * 
     * @return
     *     possible object is
     *     {@link CTLegacyDrawing }
     *     
     */
    public CTLegacyDrawing getLegacyDrawingHF() {
        return legacyDrawingHF;
    }

    /**
     * Sets the value of the legacyDrawingHF property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLegacyDrawing }
     *     
     */
    public void setLegacyDrawingHF(CTLegacyDrawing value) {
        this.legacyDrawingHF = value;
    }

    /**
     * Gets the value of the oleObjects property.
     * 
     * @return
     *     possible object is
     *     {@link CTOleObjects }
     *     
     */
    public CTOleObjects getOleObjects() {
        return oleObjects;
    }

    /**
     * Sets the value of the oleObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOleObjects }
     *     
     */
    public void setOleObjects(CTOleObjects value) {
        this.oleObjects = value;
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
