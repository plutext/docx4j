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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Chartsheet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Chartsheet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ChartsheetPr" minOccurs="0"/>
 *         &lt;element name="sheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ChartsheetViews"/>
 *         &lt;element name="sheetProtection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ChartsheetProtection" minOccurs="0"/>
 *         &lt;element name="customSheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomChartsheetViews" minOccurs="0"/>
 *         &lt;element name="pageMargins" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageMargins" minOccurs="0"/>
 *         &lt;element name="pageSetup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CsPageSetup" minOccurs="0"/>
 *         &lt;element name="headerFooter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_HeaderFooter" minOccurs="0"/>
 *         &lt;element name="drawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Drawing"/>
 *         &lt;element name="legacyDrawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_LegacyDrawing" minOccurs="0"/>
 *         &lt;element name="legacyDrawingHF" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_LegacyDrawing" minOccurs="0"/>
 *         &lt;element name="drawingHF" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DrawingHF" minOccurs="0"/>
 *         &lt;element name="picture" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetBackgroundPicture" minOccurs="0"/>
 *         &lt;element name="webPublishItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WebPublishItems" minOccurs="0"/>
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
@XmlType(name = "CT_Chartsheet", propOrder = {
    "sheetPr",
    "sheetViews",
    "sheetProtection",
    "customSheetViews",
    "pageMargins",
    "pageSetup",
    "headerFooter",
    "drawing",
    "legacyDrawing",
    "legacyDrawingHF",
    "drawingHF",
    "picture",
    "webPublishItems",
    "extLst"
})
@XmlRootElement(name = "chartsheet")
public class CTChartsheet implements Child
{

    protected CTChartsheetPr sheetPr;
    @XmlElement(required = true)
    protected CTChartsheetViews sheetViews;
    protected CTChartsheetProtection sheetProtection;
    protected CTCustomChartsheetViews customSheetViews;
    protected CTPageMargins pageMargins;
    protected CTCsPageSetup pageSetup;
    protected CTHeaderFooter headerFooter;
    @XmlElement(required = true)
    protected CTDrawing drawing;
    protected CTLegacyDrawing legacyDrawing;
    protected CTLegacyDrawing legacyDrawingHF;
    protected CTDrawingHF drawingHF;
    protected CTSheetBackgroundPicture picture;
    protected CTWebPublishItems webPublishItems;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sheetPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartsheetPr }
     *     
     */
    public CTChartsheetPr getSheetPr() {
        return sheetPr;
    }

    /**
     * Sets the value of the sheetPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartsheetPr }
     *     
     */
    public void setSheetPr(CTChartsheetPr value) {
        this.sheetPr = value;
    }

    /**
     * Gets the value of the sheetViews property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartsheetViews }
     *     
     */
    public CTChartsheetViews getSheetViews() {
        return sheetViews;
    }

    /**
     * Sets the value of the sheetViews property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartsheetViews }
     *     
     */
    public void setSheetViews(CTChartsheetViews value) {
        this.sheetViews = value;
    }

    /**
     * Gets the value of the sheetProtection property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartsheetProtection }
     *     
     */
    public CTChartsheetProtection getSheetProtection() {
        return sheetProtection;
    }

    /**
     * Sets the value of the sheetProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartsheetProtection }
     *     
     */
    public void setSheetProtection(CTChartsheetProtection value) {
        this.sheetProtection = value;
    }

    /**
     * Gets the value of the customSheetViews property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomChartsheetViews }
     *     
     */
    public CTCustomChartsheetViews getCustomSheetViews() {
        return customSheetViews;
    }

    /**
     * Sets the value of the customSheetViews property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomChartsheetViews }
     *     
     */
    public void setCustomSheetViews(CTCustomChartsheetViews value) {
        this.customSheetViews = value;
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
     *     {@link CTCsPageSetup }
     *     
     */
    public CTCsPageSetup getPageSetup() {
        return pageSetup;
    }

    /**
     * Sets the value of the pageSetup property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCsPageSetup }
     *     
     */
    public void setPageSetup(CTCsPageSetup value) {
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
     * Gets the value of the drawingHF property.
     * 
     * @return
     *     possible object is
     *     {@link CTDrawingHF }
     *     
     */
    public CTDrawingHF getDrawingHF() {
        return drawingHF;
    }

    /**
     * Sets the value of the drawingHF property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDrawingHF }
     *     
     */
    public void setDrawingHF(CTDrawingHF value) {
        this.drawingHF = value;
    }

    /**
     * Gets the value of the picture property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetBackgroundPicture }
     *     
     */
    public CTSheetBackgroundPicture getPicture() {
        return picture;
    }

    /**
     * Sets the value of the picture property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetBackgroundPicture }
     *     
     */
    public void setPicture(CTSheetBackgroundPicture value) {
        this.picture = value;
    }

    /**
     * Gets the value of the webPublishItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebPublishItems }
     *     
     */
    public CTWebPublishItems getWebPublishItems() {
        return webPublishItems;
    }

    /**
     * Sets the value of the webPublishItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebPublishItems }
     *     
     */
    public void setWebPublishItems(CTWebPublishItems value) {
        this.webPublishItems = value;
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
