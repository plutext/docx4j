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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Macrosheet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Macrosheet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetPr" minOccurs="0"/>
 *         &lt;element name="dimension" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetDimension" minOccurs="0"/>
 *         &lt;element name="sheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetViews" minOccurs="0"/>
 *         &lt;element name="sheetFormatPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetFormatPr" minOccurs="0"/>
 *         &lt;element name="cols" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cols" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sheetData" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetData"/>
 *         &lt;element name="sheetProtection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetProtection" minOccurs="0"/>
 *         &lt;element name="autoFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_AutoFilter" minOccurs="0"/>
 *         &lt;element name="sortState" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SortState" minOccurs="0"/>
 *         &lt;element name="dataConsolidate" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataConsolidate" minOccurs="0"/>
 *         &lt;element name="customSheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomSheetViews" minOccurs="0"/>
 *         &lt;element name="phoneticPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PhoneticPr" minOccurs="0"/>
 *         &lt;element name="conditionalFormatting" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ConditionalFormatting" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="printOptions" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PrintOptions" minOccurs="0"/>
 *         &lt;element name="pageMargins" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageMargins" minOccurs="0"/>
 *         &lt;element name="pageSetup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageSetup" minOccurs="0"/>
 *         &lt;element name="headerFooter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_HeaderFooter" minOccurs="0"/>
 *         &lt;element name="rowBreaks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageBreak" minOccurs="0"/>
 *         &lt;element name="colBreaks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageBreak" minOccurs="0"/>
 *         &lt;element name="customProperties" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomProperties" minOccurs="0"/>
 *         &lt;element name="drawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Drawing" minOccurs="0"/>
 *         &lt;element name="legacyDrawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_LegacyDrawing" minOccurs="0"/>
 *         &lt;element name="legacyDrawingHF" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_LegacyDrawing" minOccurs="0"/>
 *         &lt;element name="drawingHF" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DrawingHF" minOccurs="0"/>
 *         &lt;element name="picture" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetBackgroundPicture" minOccurs="0"/>
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
@XmlType(name = "CT_Macrosheet", propOrder = {
    "sheetPr",
    "dimension",
    "sheetViews",
    "sheetFormatPr",
    "cols",
    "sheetData",
    "sheetProtection",
    "autoFilter",
    "sortState",
    "dataConsolidate",
    "customSheetViews",
    "phoneticPr",
    "conditionalFormatting",
    "printOptions",
    "pageMargins",
    "pageSetup",
    "headerFooter",
    "rowBreaks",
    "colBreaks",
    "customProperties",
    "drawing",
    "legacyDrawing",
    "legacyDrawingHF",
    "drawingHF",
    "picture",
    "oleObjects",
    "extLst"
})
public class Macrosheet implements Child
{

    protected CTSheetPr sheetPr;
    protected CTSheetDimension dimension;
    protected SheetViews sheetViews;
    protected CTSheetFormatPr sheetFormatPr;
    protected List<Cols> cols;
    @XmlElement(required = true)
    protected SheetData sheetData;
    protected CTSheetProtection sheetProtection;
    protected CTAutoFilter autoFilter;
    protected CTSortState sortState;
    protected CTDataConsolidate dataConsolidate;
    protected CTCustomSheetViews customSheetViews;
    protected CTPhoneticPr phoneticPr;
    protected List<CTConditionalFormatting> conditionalFormatting;
    protected CTPrintOptions printOptions;
    protected CTPageMargins pageMargins;
    protected CTPageSetup pageSetup;
    protected CTHeaderFooter headerFooter;
    protected CTPageBreak rowBreaks;
    protected CTPageBreak colBreaks;
    protected CTCustomProperties customProperties;
    protected CTDrawing drawing;
    protected CTLegacyDrawing legacyDrawing;
    protected CTLegacyDrawing legacyDrawingHF;
    protected CTDrawingHF drawingHF;
    protected CTSheetBackgroundPicture picture;
    protected CTOleObjects oleObjects;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the dimension property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetDimension }
     *     
     */
    public CTSheetDimension getDimension() {
        return dimension;
    }

    /**
     * Sets the value of the dimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetDimension }
     *     
     */
    public void setDimension(CTSheetDimension value) {
        this.dimension = value;
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
     * Gets the value of the cols property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cols property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCols().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Cols }
     * 
     * 
     */
    public List<Cols> getCols() {
        if (cols == null) {
            cols = new ArrayList<Cols>();
        }
        return this.cols;
    }

    /**
     * Gets the value of the sheetData property.
     * 
     * @return
     *     possible object is
     *     {@link SheetData }
     *     
     */
    public SheetData getSheetData() {
        return sheetData;
    }

    /**
     * Sets the value of the sheetData property.
     * 
     * @param value
     *     allowed object is
     *     {@link SheetData }
     *     
     */
    public void setSheetData(SheetData value) {
        this.sheetData = value;
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
     * Gets the value of the autoFilter property.
     * 
     * @return
     *     possible object is
     *     {@link CTAutoFilter }
     *     
     */
    public CTAutoFilter getAutoFilter() {
        return autoFilter;
    }

    /**
     * Sets the value of the autoFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAutoFilter }
     *     
     */
    public void setAutoFilter(CTAutoFilter value) {
        this.autoFilter = value;
    }

    /**
     * Gets the value of the sortState property.
     * 
     * @return
     *     possible object is
     *     {@link CTSortState }
     *     
     */
    public CTSortState getSortState() {
        return sortState;
    }

    /**
     * Sets the value of the sortState property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSortState }
     *     
     */
    public void setSortState(CTSortState value) {
        this.sortState = value;
    }

    /**
     * Gets the value of the dataConsolidate property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataConsolidate }
     *     
     */
    public CTDataConsolidate getDataConsolidate() {
        return dataConsolidate;
    }

    /**
     * Sets the value of the dataConsolidate property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataConsolidate }
     *     
     */
    public void setDataConsolidate(CTDataConsolidate value) {
        this.dataConsolidate = value;
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
     * Gets the value of the phoneticPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTPhoneticPr }
     *     
     */
    public CTPhoneticPr getPhoneticPr() {
        return phoneticPr;
    }

    /**
     * Sets the value of the phoneticPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPhoneticPr }
     *     
     */
    public void setPhoneticPr(CTPhoneticPr value) {
        this.phoneticPr = value;
    }

    /**
     * Gets the value of the conditionalFormatting property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conditionalFormatting property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConditionalFormatting().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTConditionalFormatting }
     * 
     * 
     */
    public List<CTConditionalFormatting> getConditionalFormatting() {
        if (conditionalFormatting == null) {
            conditionalFormatting = new ArrayList<CTConditionalFormatting>();
        }
        return this.conditionalFormatting;
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
     * Gets the value of the rowBreaks property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageBreak }
     *     
     */
    public CTPageBreak getRowBreaks() {
        return rowBreaks;
    }

    /**
     * Sets the value of the rowBreaks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageBreak }
     *     
     */
    public void setRowBreaks(CTPageBreak value) {
        this.rowBreaks = value;
    }

    /**
     * Gets the value of the colBreaks property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageBreak }
     *     
     */
    public CTPageBreak getColBreaks() {
        return colBreaks;
    }

    /**
     * Sets the value of the colBreaks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageBreak }
     *     
     */
    public void setColBreaks(CTPageBreak value) {
        this.colBreaks = value;
    }

    /**
     * Gets the value of the customProperties property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomProperties }
     *     
     */
    public CTCustomProperties getCustomProperties() {
        return customProperties;
    }

    /**
     * Sets the value of the customProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomProperties }
     *     
     */
    public void setCustomProperties(CTCustomProperties value) {
        this.customProperties = value;
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
