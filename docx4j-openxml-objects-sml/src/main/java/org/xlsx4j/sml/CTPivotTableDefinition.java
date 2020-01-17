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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_pivotTableDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_pivotTableDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="location" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Location"/>
 *         &lt;element name="pivotFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotFields" minOccurs="0"/>
 *         &lt;element name="rowFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RowFields" minOccurs="0"/>
 *         &lt;element name="rowItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_rowItems" minOccurs="0"/>
 *         &lt;element name="colFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ColFields" minOccurs="0"/>
 *         &lt;element name="colItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_colItems" minOccurs="0"/>
 *         &lt;element name="pageFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageFields" minOccurs="0"/>
 *         &lt;element name="dataFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataFields" minOccurs="0"/>
 *         &lt;element name="formats" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Formats" minOccurs="0"/>
 *         &lt;element name="conditionalFormats" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ConditionalFormats" minOccurs="0"/>
 *         &lt;element name="chartFormats" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ChartFormats" minOccurs="0"/>
 *         &lt;element name="pivotHierarchies" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotHierarchies" minOccurs="0"/>
 *         &lt;element name="pivotTableStyleInfo" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotTableStyle" minOccurs="0"/>
 *         &lt;element name="filters" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotFilters" minOccurs="0"/>
 *         &lt;element name="rowHierarchiesUsage" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RowHierarchiesUsage" minOccurs="0"/>
 *         &lt;element name="colHierarchiesUsage" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ColHierarchiesUsage" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_AutoFormat"/>
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="cacheId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="dataOnRows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dataPosition" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="dataCaption" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="grandTotalCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="errorCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="showError" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="missingCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="showMissing" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="pageStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="pivotTableStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="vacatedStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="tag" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="updatedVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="minRefreshableVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="asteriskTotals" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showItems" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="editData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="disableFieldList" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showCalcMbrs" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="visualTotals" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showMultipleLabel" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showDataDropDown" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showDrill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="printDrill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showMemberPropertyTips" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showDataTips" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="enableWizard" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="enableDrill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="enableFieldProperties" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="preserveFormatting" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="useAutoFormatting" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pageWrap" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="pageOverThenDown" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="subtotalHiddenItems" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="rowGrandTotals" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="colGrandTotals" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="fieldPrintTitles" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="itemPrintTitles" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="mergeItem" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showDropZones" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="createdVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="indent" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="showEmptyRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showEmptyCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showHeaders" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="compact" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="outline" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="outlineData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="compactData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="published" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="gridDropZones" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="immersive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="multipleFieldFilters" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="chartFormat" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="rowHeaderCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="colHeaderCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="fieldListSortAscending" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="mdxSubqueries" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="customListSort" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_pivotTableDefinition", propOrder = {
    "location",
    "pivotFields",
    "rowFields",
    "rowItems",
    "colFields",
    "colItems",
    "pageFields",
    "dataFields",
    "formats",
    "conditionalFormats",
    "chartFormats",
    "pivotHierarchies",
    "pivotTableStyleInfo",
    "filters",
    "rowHierarchiesUsage",
    "colHierarchiesUsage",
    "extLst"
})
@XmlRootElement(name = "pivotTableDefinition")
public class CTPivotTableDefinition implements Child
{

    @XmlElement(required = true)
    protected CTLocation location;
    protected CTPivotFields pivotFields;
    protected CTRowFields rowFields;
    protected CTRowItems rowItems;
    protected CTColFields colFields;
    protected CTColItems colItems;
    protected CTPageFields pageFields;
    protected CTDataFields dataFields;
    protected CTFormats formats;
    protected CTConditionalFormats conditionalFormats;
    protected CTChartFormats chartFormats;
    protected CTPivotHierarchies pivotHierarchies;
    protected CTPivotTableStyle pivotTableStyleInfo;
    protected CTPivotFilters filters;
    protected CTRowHierarchiesUsage rowHierarchiesUsage;
    protected CTColHierarchiesUsage colHierarchiesUsage;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "cacheId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long cacheId;
    @XmlAttribute(name = "dataOnRows")
    protected Boolean dataOnRows;
    @XmlAttribute(name = "dataPosition")
    @XmlSchemaType(name = "unsignedInt")
    protected Long dataPosition;
    @XmlAttribute(name = "dataCaption", required = true)
    protected String dataCaption;
    @XmlAttribute(name = "grandTotalCaption")
    protected String grandTotalCaption;
    @XmlAttribute(name = "errorCaption")
    protected String errorCaption;
    @XmlAttribute(name = "showError")
    protected Boolean showError;
    @XmlAttribute(name = "missingCaption")
    protected String missingCaption;
    @XmlAttribute(name = "showMissing")
    protected Boolean showMissing;
    @XmlAttribute(name = "pageStyle")
    protected String pageStyle;
    @XmlAttribute(name = "pivotTableStyle")
    protected String pivotTableStyle;
    @XmlAttribute(name = "vacatedStyle")
    protected String vacatedStyle;
    @XmlAttribute(name = "tag")
    protected String tag;
    @XmlAttribute(name = "updatedVersion")
    @XmlSchemaType(name = "unsignedByte")
    protected Short updatedVersion;
    @XmlAttribute(name = "minRefreshableVersion")
    @XmlSchemaType(name = "unsignedByte")
    protected Short minRefreshableVersion;
    @XmlAttribute(name = "asteriskTotals")
    protected Boolean asteriskTotals;
    @XmlAttribute(name = "showItems")
    protected Boolean showItems;
    @XmlAttribute(name = "editData")
    protected Boolean editData;
    @XmlAttribute(name = "disableFieldList")
    protected Boolean disableFieldList;
    @XmlAttribute(name = "showCalcMbrs")
    protected Boolean showCalcMbrs;
    @XmlAttribute(name = "visualTotals")
    protected Boolean visualTotals;
    @XmlAttribute(name = "showMultipleLabel")
    protected Boolean showMultipleLabel;
    @XmlAttribute(name = "showDataDropDown")
    protected Boolean showDataDropDown;
    @XmlAttribute(name = "showDrill")
    protected Boolean showDrill;
    @XmlAttribute(name = "printDrill")
    protected Boolean printDrill;
    @XmlAttribute(name = "showMemberPropertyTips")
    protected Boolean showMemberPropertyTips;
    @XmlAttribute(name = "showDataTips")
    protected Boolean showDataTips;
    @XmlAttribute(name = "enableWizard")
    protected Boolean enableWizard;
    @XmlAttribute(name = "enableDrill")
    protected Boolean enableDrill;
    @XmlAttribute(name = "enableFieldProperties")
    protected Boolean enableFieldProperties;
    @XmlAttribute(name = "preserveFormatting")
    protected Boolean preserveFormatting;
    @XmlAttribute(name = "useAutoFormatting")
    protected Boolean useAutoFormatting;
    @XmlAttribute(name = "pageWrap")
    @XmlSchemaType(name = "unsignedInt")
    protected Long pageWrap;
    @XmlAttribute(name = "pageOverThenDown")
    protected Boolean pageOverThenDown;
    @XmlAttribute(name = "subtotalHiddenItems")
    protected Boolean subtotalHiddenItems;
    @XmlAttribute(name = "rowGrandTotals")
    protected Boolean rowGrandTotals;
    @XmlAttribute(name = "colGrandTotals")
    protected Boolean colGrandTotals;
    @XmlAttribute(name = "fieldPrintTitles")
    protected Boolean fieldPrintTitles;
    @XmlAttribute(name = "itemPrintTitles")
    protected Boolean itemPrintTitles;
    @XmlAttribute(name = "mergeItem")
    protected Boolean mergeItem;
    @XmlAttribute(name = "showDropZones")
    protected Boolean showDropZones;
    @XmlAttribute(name = "createdVersion")
    @XmlSchemaType(name = "unsignedByte")
    protected Short createdVersion;
    @XmlAttribute(name = "indent")
    @XmlSchemaType(name = "unsignedInt")
    protected Long indent;
    @XmlAttribute(name = "showEmptyRow")
    protected Boolean showEmptyRow;
    @XmlAttribute(name = "showEmptyCol")
    protected Boolean showEmptyCol;
    @XmlAttribute(name = "showHeaders")
    protected Boolean showHeaders;
    @XmlAttribute(name = "compact")
    protected Boolean compact;
    @XmlAttribute(name = "outline")
    protected Boolean outline;
    @XmlAttribute(name = "outlineData")
    protected Boolean outlineData;
    @XmlAttribute(name = "compactData")
    protected Boolean compactData;
    @XmlAttribute(name = "published")
    protected Boolean published;
    @XmlAttribute(name = "gridDropZones")
    protected Boolean gridDropZones;
    @XmlAttribute(name = "immersive")
    protected Boolean immersive;
    @XmlAttribute(name = "multipleFieldFilters")
    protected Boolean multipleFieldFilters;
    @XmlAttribute(name = "chartFormat")
    @XmlSchemaType(name = "unsignedInt")
    protected Long chartFormat;
    @XmlAttribute(name = "rowHeaderCaption")
    protected String rowHeaderCaption;
    @XmlAttribute(name = "colHeaderCaption")
    protected String colHeaderCaption;
    @XmlAttribute(name = "fieldListSortAscending")
    protected Boolean fieldListSortAscending;
    @XmlAttribute(name = "mdxSubqueries")
    protected Boolean mdxSubqueries;
    @XmlAttribute(name = "customListSort")
    protected Boolean customListSort;
    @XmlAttribute(name = "autoFormatId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long autoFormatId;
    @XmlAttribute(name = "applyNumberFormats")
    protected Boolean applyNumberFormats;
    @XmlAttribute(name = "applyBorderFormats")
    protected Boolean applyBorderFormats;
    @XmlAttribute(name = "applyFontFormats")
    protected Boolean applyFontFormats;
    @XmlAttribute(name = "applyPatternFormats")
    protected Boolean applyPatternFormats;
    @XmlAttribute(name = "applyAlignmentFormats")
    protected Boolean applyAlignmentFormats;
    @XmlAttribute(name = "applyWidthHeightFormats")
    protected Boolean applyWidthHeightFormats;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link CTLocation }
     *     
     */
    public CTLocation getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLocation }
     *     
     */
    public void setLocation(CTLocation value) {
        this.location = value;
    }

    /**
     * Gets the value of the pivotFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotFields }
     *     
     */
    public CTPivotFields getPivotFields() {
        return pivotFields;
    }

    /**
     * Sets the value of the pivotFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotFields }
     *     
     */
    public void setPivotFields(CTPivotFields value) {
        this.pivotFields = value;
    }

    /**
     * Gets the value of the rowFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTRowFields }
     *     
     */
    public CTRowFields getRowFields() {
        return rowFields;
    }

    /**
     * Sets the value of the rowFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRowFields }
     *     
     */
    public void setRowFields(CTRowFields value) {
        this.rowFields = value;
    }

    /**
     * Gets the value of the rowItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTRowItems }
     *     
     */
    public CTRowItems getRowItems() {
        return rowItems;
    }

    /**
     * Sets the value of the rowItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRowItems }
     *     
     */
    public void setRowItems(CTRowItems value) {
        this.rowItems = value;
    }

    /**
     * Gets the value of the colFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTColFields }
     *     
     */
    public CTColFields getColFields() {
        return colFields;
    }

    /**
     * Sets the value of the colFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColFields }
     *     
     */
    public void setColFields(CTColFields value) {
        this.colFields = value;
    }

    /**
     * Gets the value of the colItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTColItems }
     *     
     */
    public CTColItems getColItems() {
        return colItems;
    }

    /**
     * Sets the value of the colItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColItems }
     *     
     */
    public void setColItems(CTColItems value) {
        this.colItems = value;
    }

    /**
     * Gets the value of the pageFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTPageFields }
     *     
     */
    public CTPageFields getPageFields() {
        return pageFields;
    }

    /**
     * Sets the value of the pageFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPageFields }
     *     
     */
    public void setPageFields(CTPageFields value) {
        this.pageFields = value;
    }

    /**
     * Gets the value of the dataFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataFields }
     *     
     */
    public CTDataFields getDataFields() {
        return dataFields;
    }

    /**
     * Sets the value of the dataFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataFields }
     *     
     */
    public void setDataFields(CTDataFields value) {
        this.dataFields = value;
    }

    /**
     * Gets the value of the formats property.
     * 
     * @return
     *     possible object is
     *     {@link CTFormats }
     *     
     */
    public CTFormats getFormats() {
        return formats;
    }

    /**
     * Sets the value of the formats property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFormats }
     *     
     */
    public void setFormats(CTFormats value) {
        this.formats = value;
    }

    /**
     * Gets the value of the conditionalFormats property.
     * 
     * @return
     *     possible object is
     *     {@link CTConditionalFormats }
     *     
     */
    public CTConditionalFormats getConditionalFormats() {
        return conditionalFormats;
    }

    /**
     * Sets the value of the conditionalFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConditionalFormats }
     *     
     */
    public void setConditionalFormats(CTConditionalFormats value) {
        this.conditionalFormats = value;
    }

    /**
     * Gets the value of the chartFormats property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartFormats }
     *     
     */
    public CTChartFormats getChartFormats() {
        return chartFormats;
    }

    /**
     * Sets the value of the chartFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartFormats }
     *     
     */
    public void setChartFormats(CTChartFormats value) {
        this.chartFormats = value;
    }

    /**
     * Gets the value of the pivotHierarchies property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotHierarchies }
     *     
     */
    public CTPivotHierarchies getPivotHierarchies() {
        return pivotHierarchies;
    }

    /**
     * Sets the value of the pivotHierarchies property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotHierarchies }
     *     
     */
    public void setPivotHierarchies(CTPivotHierarchies value) {
        this.pivotHierarchies = value;
    }

    /**
     * Gets the value of the pivotTableStyleInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotTableStyle }
     *     
     */
    public CTPivotTableStyle getPivotTableStyleInfo() {
        return pivotTableStyleInfo;
    }

    /**
     * Sets the value of the pivotTableStyleInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotTableStyle }
     *     
     */
    public void setPivotTableStyleInfo(CTPivotTableStyle value) {
        this.pivotTableStyleInfo = value;
    }

    /**
     * Gets the value of the filters property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotFilters }
     *     
     */
    public CTPivotFilters getFilters() {
        return filters;
    }

    /**
     * Sets the value of the filters property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotFilters }
     *     
     */
    public void setFilters(CTPivotFilters value) {
        this.filters = value;
    }

    /**
     * Gets the value of the rowHierarchiesUsage property.
     * 
     * @return
     *     possible object is
     *     {@link CTRowHierarchiesUsage }
     *     
     */
    public CTRowHierarchiesUsage getRowHierarchiesUsage() {
        return rowHierarchiesUsage;
    }

    /**
     * Sets the value of the rowHierarchiesUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRowHierarchiesUsage }
     *     
     */
    public void setRowHierarchiesUsage(CTRowHierarchiesUsage value) {
        this.rowHierarchiesUsage = value;
    }

    /**
     * Gets the value of the colHierarchiesUsage property.
     * 
     * @return
     *     possible object is
     *     {@link CTColHierarchiesUsage }
     *     
     */
    public CTColHierarchiesUsage getColHierarchiesUsage() {
        return colHierarchiesUsage;
    }

    /**
     * Sets the value of the colHierarchiesUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColHierarchiesUsage }
     *     
     */
    public void setColHierarchiesUsage(CTColHierarchiesUsage value) {
        this.colHierarchiesUsage = value;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the cacheId property.
     * 
     */
    public long getCacheId() {
        return cacheId;
    }

    /**
     * Sets the value of the cacheId property.
     * 
     */
    public void setCacheId(long value) {
        this.cacheId = value;
    }

    /**
     * Gets the value of the dataOnRows property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDataOnRows() {
        if (dataOnRows == null) {
            return false;
        } else {
            return dataOnRows;
        }
    }

    /**
     * Sets the value of the dataOnRows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataOnRows(Boolean value) {
        this.dataOnRows = value;
    }

    /**
     * Gets the value of the dataPosition property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDataPosition() {
        return dataPosition;
    }

    /**
     * Sets the value of the dataPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDataPosition(Long value) {
        this.dataPosition = value;
    }

    /**
     * Gets the value of the dataCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataCaption() {
        return dataCaption;
    }

    /**
     * Sets the value of the dataCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataCaption(String value) {
        this.dataCaption = value;
    }

    /**
     * Gets the value of the grandTotalCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrandTotalCaption() {
        return grandTotalCaption;
    }

    /**
     * Sets the value of the grandTotalCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrandTotalCaption(String value) {
        this.grandTotalCaption = value;
    }

    /**
     * Gets the value of the errorCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorCaption() {
        return errorCaption;
    }

    /**
     * Sets the value of the errorCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorCaption(String value) {
        this.errorCaption = value;
    }

    /**
     * Gets the value of the showError property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowError() {
        if (showError == null) {
            return false;
        } else {
            return showError;
        }
    }

    /**
     * Sets the value of the showError property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowError(Boolean value) {
        this.showError = value;
    }

    /**
     * Gets the value of the missingCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMissingCaption() {
        return missingCaption;
    }

    /**
     * Sets the value of the missingCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMissingCaption(String value) {
        this.missingCaption = value;
    }

    /**
     * Gets the value of the showMissing property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowMissing() {
        if (showMissing == null) {
            return true;
        } else {
            return showMissing;
        }
    }

    /**
     * Sets the value of the showMissing property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowMissing(Boolean value) {
        this.showMissing = value;
    }

    /**
     * Gets the value of the pageStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageStyle() {
        return pageStyle;
    }

    /**
     * Sets the value of the pageStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageStyle(String value) {
        this.pageStyle = value;
    }

    /**
     * Gets the value of the pivotTableStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPivotTableStyle() {
        return pivotTableStyle;
    }

    /**
     * Sets the value of the pivotTableStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPivotTableStyle(String value) {
        this.pivotTableStyle = value;
    }

    /**
     * Gets the value of the vacatedStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVacatedStyle() {
        return vacatedStyle;
    }

    /**
     * Sets the value of the vacatedStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVacatedStyle(String value) {
        this.vacatedStyle = value;
    }

    /**
     * Gets the value of the tag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the value of the tag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTag(String value) {
        this.tag = value;
    }

    /**
     * Gets the value of the updatedVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getUpdatedVersion() {
        if (updatedVersion == null) {
            return ((short) 0);
        } else {
            return updatedVersion;
        }
    }

    /**
     * Sets the value of the updatedVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setUpdatedVersion(Short value) {
        this.updatedVersion = value;
    }

    /**
     * Gets the value of the minRefreshableVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getMinRefreshableVersion() {
        if (minRefreshableVersion == null) {
            return ((short) 0);
        } else {
            return minRefreshableVersion;
        }
    }

    /**
     * Sets the value of the minRefreshableVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMinRefreshableVersion(Short value) {
        this.minRefreshableVersion = value;
    }

    /**
     * Gets the value of the asteriskTotals property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAsteriskTotals() {
        if (asteriskTotals == null) {
            return false;
        } else {
            return asteriskTotals;
        }
    }

    /**
     * Sets the value of the asteriskTotals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAsteriskTotals(Boolean value) {
        this.asteriskTotals = value;
    }

    /**
     * Gets the value of the showItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowItems() {
        if (showItems == null) {
            return true;
        } else {
            return showItems;
        }
    }

    /**
     * Sets the value of the showItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowItems(Boolean value) {
        this.showItems = value;
    }

    /**
     * Gets the value of the editData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEditData() {
        if (editData == null) {
            return false;
        } else {
            return editData;
        }
    }

    /**
     * Sets the value of the editData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEditData(Boolean value) {
        this.editData = value;
    }

    /**
     * Gets the value of the disableFieldList property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDisableFieldList() {
        if (disableFieldList == null) {
            return false;
        } else {
            return disableFieldList;
        }
    }

    /**
     * Sets the value of the disableFieldList property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisableFieldList(Boolean value) {
        this.disableFieldList = value;
    }

    /**
     * Gets the value of the showCalcMbrs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowCalcMbrs() {
        if (showCalcMbrs == null) {
            return true;
        } else {
            return showCalcMbrs;
        }
    }

    /**
     * Sets the value of the showCalcMbrs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowCalcMbrs(Boolean value) {
        this.showCalcMbrs = value;
    }

    /**
     * Gets the value of the visualTotals property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVisualTotals() {
        if (visualTotals == null) {
            return true;
        } else {
            return visualTotals;
        }
    }

    /**
     * Sets the value of the visualTotals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisualTotals(Boolean value) {
        this.visualTotals = value;
    }

    /**
     * Gets the value of the showMultipleLabel property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowMultipleLabel() {
        if (showMultipleLabel == null) {
            return true;
        } else {
            return showMultipleLabel;
        }
    }

    /**
     * Sets the value of the showMultipleLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowMultipleLabel(Boolean value) {
        this.showMultipleLabel = value;
    }

    /**
     * Gets the value of the showDataDropDown property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowDataDropDown() {
        if (showDataDropDown == null) {
            return true;
        } else {
            return showDataDropDown;
        }
    }

    /**
     * Sets the value of the showDataDropDown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowDataDropDown(Boolean value) {
        this.showDataDropDown = value;
    }

    /**
     * Gets the value of the showDrill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowDrill() {
        if (showDrill == null) {
            return true;
        } else {
            return showDrill;
        }
    }

    /**
     * Sets the value of the showDrill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowDrill(Boolean value) {
        this.showDrill = value;
    }

    /**
     * Gets the value of the printDrill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPrintDrill() {
        if (printDrill == null) {
            return false;
        } else {
            return printDrill;
        }
    }

    /**
     * Sets the value of the printDrill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrintDrill(Boolean value) {
        this.printDrill = value;
    }

    /**
     * Gets the value of the showMemberPropertyTips property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowMemberPropertyTips() {
        if (showMemberPropertyTips == null) {
            return true;
        } else {
            return showMemberPropertyTips;
        }
    }

    /**
     * Sets the value of the showMemberPropertyTips property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowMemberPropertyTips(Boolean value) {
        this.showMemberPropertyTips = value;
    }

    /**
     * Gets the value of the showDataTips property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowDataTips() {
        if (showDataTips == null) {
            return true;
        } else {
            return showDataTips;
        }
    }

    /**
     * Sets the value of the showDataTips property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowDataTips(Boolean value) {
        this.showDataTips = value;
    }

    /**
     * Gets the value of the enableWizard property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnableWizard() {
        if (enableWizard == null) {
            return true;
        } else {
            return enableWizard;
        }
    }

    /**
     * Sets the value of the enableWizard property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableWizard(Boolean value) {
        this.enableWizard = value;
    }

    /**
     * Gets the value of the enableDrill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnableDrill() {
        if (enableDrill == null) {
            return true;
        } else {
            return enableDrill;
        }
    }

    /**
     * Sets the value of the enableDrill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableDrill(Boolean value) {
        this.enableDrill = value;
    }

    /**
     * Gets the value of the enableFieldProperties property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnableFieldProperties() {
        if (enableFieldProperties == null) {
            return true;
        } else {
            return enableFieldProperties;
        }
    }

    /**
     * Sets the value of the enableFieldProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableFieldProperties(Boolean value) {
        this.enableFieldProperties = value;
    }

    /**
     * Gets the value of the preserveFormatting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPreserveFormatting() {
        if (preserveFormatting == null) {
            return true;
        } else {
            return preserveFormatting;
        }
    }

    /**
     * Sets the value of the preserveFormatting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreserveFormatting(Boolean value) {
        this.preserveFormatting = value;
    }

    /**
     * Gets the value of the useAutoFormatting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseAutoFormatting() {
        if (useAutoFormatting == null) {
            return false;
        } else {
            return useAutoFormatting;
        }
    }

    /**
     * Sets the value of the useAutoFormatting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseAutoFormatting(Boolean value) {
        this.useAutoFormatting = value;
    }

    /**
     * Gets the value of the pageWrap property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPageWrap() {
        if (pageWrap == null) {
            return  0L;
        } else {
            return pageWrap;
        }
    }

    /**
     * Sets the value of the pageWrap property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPageWrap(Long value) {
        this.pageWrap = value;
    }

    /**
     * Gets the value of the pageOverThenDown property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPageOverThenDown() {
        if (pageOverThenDown == null) {
            return false;
        } else {
            return pageOverThenDown;
        }
    }

    /**
     * Sets the value of the pageOverThenDown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPageOverThenDown(Boolean value) {
        this.pageOverThenDown = value;
    }

    /**
     * Gets the value of the subtotalHiddenItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSubtotalHiddenItems() {
        if (subtotalHiddenItems == null) {
            return false;
        } else {
            return subtotalHiddenItems;
        }
    }

    /**
     * Sets the value of the subtotalHiddenItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSubtotalHiddenItems(Boolean value) {
        this.subtotalHiddenItems = value;
    }

    /**
     * Gets the value of the rowGrandTotals property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRowGrandTotals() {
        if (rowGrandTotals == null) {
            return true;
        } else {
            return rowGrandTotals;
        }
    }

    /**
     * Sets the value of the rowGrandTotals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRowGrandTotals(Boolean value) {
        this.rowGrandTotals = value;
    }

    /**
     * Gets the value of the colGrandTotals property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isColGrandTotals() {
        if (colGrandTotals == null) {
            return true;
        } else {
            return colGrandTotals;
        }
    }

    /**
     * Sets the value of the colGrandTotals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setColGrandTotals(Boolean value) {
        this.colGrandTotals = value;
    }

    /**
     * Gets the value of the fieldPrintTitles property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFieldPrintTitles() {
        if (fieldPrintTitles == null) {
            return false;
        } else {
            return fieldPrintTitles;
        }
    }

    /**
     * Sets the value of the fieldPrintTitles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFieldPrintTitles(Boolean value) {
        this.fieldPrintTitles = value;
    }

    /**
     * Gets the value of the itemPrintTitles property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isItemPrintTitles() {
        if (itemPrintTitles == null) {
            return false;
        } else {
            return itemPrintTitles;
        }
    }

    /**
     * Sets the value of the itemPrintTitles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setItemPrintTitles(Boolean value) {
        this.itemPrintTitles = value;
    }

    /**
     * Gets the value of the mergeItem property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMergeItem() {
        if (mergeItem == null) {
            return false;
        } else {
            return mergeItem;
        }
    }

    /**
     * Sets the value of the mergeItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMergeItem(Boolean value) {
        this.mergeItem = value;
    }

    /**
     * Gets the value of the showDropZones property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowDropZones() {
        if (showDropZones == null) {
            return true;
        } else {
            return showDropZones;
        }
    }

    /**
     * Sets the value of the showDropZones property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowDropZones(Boolean value) {
        this.showDropZones = value;
    }

    /**
     * Gets the value of the createdVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getCreatedVersion() {
        if (createdVersion == null) {
            return ((short) 0);
        } else {
            return createdVersion;
        }
    }

    /**
     * Sets the value of the createdVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setCreatedVersion(Short value) {
        this.createdVersion = value;
    }

    /**
     * Gets the value of the indent property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getIndent() {
        if (indent == null) {
            return  1L;
        } else {
            return indent;
        }
    }

    /**
     * Sets the value of the indent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIndent(Long value) {
        this.indent = value;
    }

    /**
     * Gets the value of the showEmptyRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowEmptyRow() {
        if (showEmptyRow == null) {
            return false;
        } else {
            return showEmptyRow;
        }
    }

    /**
     * Sets the value of the showEmptyRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowEmptyRow(Boolean value) {
        this.showEmptyRow = value;
    }

    /**
     * Gets the value of the showEmptyCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowEmptyCol() {
        if (showEmptyCol == null) {
            return false;
        } else {
            return showEmptyCol;
        }
    }

    /**
     * Sets the value of the showEmptyCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowEmptyCol(Boolean value) {
        this.showEmptyCol = value;
    }

    /**
     * Gets the value of the showHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowHeaders() {
        if (showHeaders == null) {
            return true;
        } else {
            return showHeaders;
        }
    }

    /**
     * Sets the value of the showHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowHeaders(Boolean value) {
        this.showHeaders = value;
    }

    /**
     * Gets the value of the compact property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCompact() {
        if (compact == null) {
            return true;
        } else {
            return compact;
        }
    }

    /**
     * Sets the value of the compact property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCompact(Boolean value) {
        this.compact = value;
    }

    /**
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutline() {
        if (outline == null) {
            return false;
        } else {
            return outline;
        }
    }

    /**
     * Sets the value of the outline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutline(Boolean value) {
        this.outline = value;
    }

    /**
     * Gets the value of the outlineData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutlineData() {
        if (outlineData == null) {
            return false;
        } else {
            return outlineData;
        }
    }

    /**
     * Sets the value of the outlineData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutlineData(Boolean value) {
        this.outlineData = value;
    }

    /**
     * Gets the value of the compactData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCompactData() {
        if (compactData == null) {
            return true;
        } else {
            return compactData;
        }
    }

    /**
     * Sets the value of the compactData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCompactData(Boolean value) {
        this.compactData = value;
    }

    /**
     * Gets the value of the published property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPublished() {
        if (published == null) {
            return false;
        } else {
            return published;
        }
    }

    /**
     * Sets the value of the published property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublished(Boolean value) {
        this.published = value;
    }

    /**
     * Gets the value of the gridDropZones property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGridDropZones() {
        if (gridDropZones == null) {
            return false;
        } else {
            return gridDropZones;
        }
    }

    /**
     * Sets the value of the gridDropZones property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGridDropZones(Boolean value) {
        this.gridDropZones = value;
    }

    /**
     * Gets the value of the immersive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isImmersive() {
        if (immersive == null) {
            return true;
        } else {
            return immersive;
        }
    }

    /**
     * Sets the value of the immersive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setImmersive(Boolean value) {
        this.immersive = value;
    }

    /**
     * Gets the value of the multipleFieldFilters property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMultipleFieldFilters() {
        if (multipleFieldFilters == null) {
            return true;
        } else {
            return multipleFieldFilters;
        }
    }

    /**
     * Sets the value of the multipleFieldFilters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMultipleFieldFilters(Boolean value) {
        this.multipleFieldFilters = value;
    }

    /**
     * Gets the value of the chartFormat property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getChartFormat() {
        if (chartFormat == null) {
            return  0L;
        } else {
            return chartFormat;
        }
    }

    /**
     * Sets the value of the chartFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setChartFormat(Long value) {
        this.chartFormat = value;
    }

    /**
     * Gets the value of the rowHeaderCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRowHeaderCaption() {
        return rowHeaderCaption;
    }

    /**
     * Sets the value of the rowHeaderCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRowHeaderCaption(String value) {
        this.rowHeaderCaption = value;
    }

    /**
     * Gets the value of the colHeaderCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColHeaderCaption() {
        return colHeaderCaption;
    }

    /**
     * Sets the value of the colHeaderCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColHeaderCaption(String value) {
        this.colHeaderCaption = value;
    }

    /**
     * Gets the value of the fieldListSortAscending property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFieldListSortAscending() {
        if (fieldListSortAscending == null) {
            return false;
        } else {
            return fieldListSortAscending;
        }
    }

    /**
     * Sets the value of the fieldListSortAscending property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFieldListSortAscending(Boolean value) {
        this.fieldListSortAscending = value;
    }

    /**
     * Gets the value of the mdxSubqueries property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMdxSubqueries() {
        if (mdxSubqueries == null) {
            return false;
        } else {
            return mdxSubqueries;
        }
    }

    /**
     * Sets the value of the mdxSubqueries property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMdxSubqueries(Boolean value) {
        this.mdxSubqueries = value;
    }

    /**
     * Gets the value of the customListSort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCustomListSort() {
        if (customListSort == null) {
            return true;
        } else {
            return customListSort;
        }
    }

    /**
     * Sets the value of the customListSort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomListSort(Boolean value) {
        this.customListSort = value;
    }

    /**
     * Gets the value of the autoFormatId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAutoFormatId() {
        return autoFormatId;
    }

    /**
     * Sets the value of the autoFormatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAutoFormatId(Long value) {
        this.autoFormatId = value;
    }

    /**
     * Gets the value of the applyNumberFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyNumberFormats() {
        return applyNumberFormats;
    }

    /**
     * Sets the value of the applyNumberFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyNumberFormats(Boolean value) {
        this.applyNumberFormats = value;
    }

    /**
     * Gets the value of the applyBorderFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyBorderFormats() {
        return applyBorderFormats;
    }

    /**
     * Sets the value of the applyBorderFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyBorderFormats(Boolean value) {
        this.applyBorderFormats = value;
    }

    /**
     * Gets the value of the applyFontFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyFontFormats() {
        return applyFontFormats;
    }

    /**
     * Sets the value of the applyFontFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyFontFormats(Boolean value) {
        this.applyFontFormats = value;
    }

    /**
     * Gets the value of the applyPatternFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyPatternFormats() {
        return applyPatternFormats;
    }

    /**
     * Sets the value of the applyPatternFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyPatternFormats(Boolean value) {
        this.applyPatternFormats = value;
    }

    /**
     * Gets the value of the applyAlignmentFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyAlignmentFormats() {
        return applyAlignmentFormats;
    }

    /**
     * Sets the value of the applyAlignmentFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyAlignmentFormats(Boolean value) {
        this.applyAlignmentFormats = value;
    }

    /**
     * Gets the value of the applyWidthHeightFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyWidthHeightFormats() {
        return applyWidthHeightFormats;
    }

    /**
     * Sets the value of the applyWidthHeightFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyWidthHeightFormats(Boolean value) {
        this.applyWidthHeightFormats = value;
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
