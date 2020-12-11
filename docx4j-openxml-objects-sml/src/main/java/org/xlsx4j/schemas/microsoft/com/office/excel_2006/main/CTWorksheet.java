
package org.xlsx4j.schemas.microsoft.com.office.excel_2006.main;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;
import org.xlsx4j.sml.CTAutoFilter;
import org.xlsx4j.sml.CTCellWatches;
import org.xlsx4j.sml.CTConditionalFormatting;
import org.xlsx4j.sml.CTControls;
import org.xlsx4j.sml.CTCustomProperties;
import org.xlsx4j.sml.CTCustomSheetViews;
import org.xlsx4j.sml.CTDataConsolidate;
import org.xlsx4j.sml.CTDataValidations;
import org.xlsx4j.sml.CTDrawing;
import org.xlsx4j.sml.CTExtensionList;
import org.xlsx4j.sml.CTHeaderFooter;
import org.xlsx4j.sml.CTHyperlinks;
import org.xlsx4j.sml.CTIgnoredErrors;
import org.xlsx4j.sml.CTMergeCells;
import org.xlsx4j.sml.CTOleObjects;
import org.xlsx4j.sml.CTPageBreak;
import org.xlsx4j.sml.CTPageMargins;
import org.xlsx4j.sml.CTPageSetup;
import org.xlsx4j.sml.CTPhoneticPr;
import org.xlsx4j.sml.CTPrintOptions;
import org.xlsx4j.sml.CTProtectedRanges;
import org.xlsx4j.sml.CTScenarios;
import org.xlsx4j.sml.CTSheetBackgroundPicture;
import org.xlsx4j.sml.CTSheetCalcPr;
import org.xlsx4j.sml.CTSheetDimension;
import org.xlsx4j.sml.CTSheetFormatPr;
import org.xlsx4j.sml.CTSheetPr;
import org.xlsx4j.sml.CTSheetProtection;
import org.xlsx4j.sml.CTSmartTags;
import org.xlsx4j.sml.CTSortState;
import org.xlsx4j.sml.CTTableParts;
import org.xlsx4j.sml.CTWebPublishItems;
import org.xlsx4j.sml.Cols;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.SheetViews;


/**
 * <p>Java class for CT_Worksheet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Worksheet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetPr" minOccurs="0"/>
 *         &lt;element name="dimension" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetDimension" minOccurs="0"/>
 *         &lt;element name="sheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetViews" minOccurs="0"/>
 *         &lt;element name="sheetFormatPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetFormatPr" minOccurs="0"/>
 *         &lt;element name="cols" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cols" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sheetData" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetData"/>
 *         &lt;element name="sheetCalcPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetCalcPr" minOccurs="0"/>
 *         &lt;element name="sheetProtection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetProtection" minOccurs="0"/>
 *         &lt;element name="protectedRanges" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ProtectedRanges" minOccurs="0"/>
 *         &lt;element name="scenarios" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Scenarios" minOccurs="0"/>
 *         &lt;element name="autoFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_AutoFilter" minOccurs="0"/>
 *         &lt;element name="sortState" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SortState" minOccurs="0"/>
 *         &lt;element name="dataConsolidate" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataConsolidate" minOccurs="0"/>
 *         &lt;element name="customSheetViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomSheetViews" minOccurs="0"/>
 *         &lt;element name="mergeCells" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MergeCells" minOccurs="0"/>
 *         &lt;element name="phoneticPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PhoneticPr" minOccurs="0"/>
 *         &lt;element name="conditionalFormatting" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ConditionalFormatting" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dataValidations" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataValidations" minOccurs="0"/>
 *         &lt;element name="hyperlinks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Hyperlinks" minOccurs="0"/>
 *         &lt;element name="printOptions" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PrintOptions" minOccurs="0"/>
 *         &lt;element name="pageMargins" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageMargins" minOccurs="0"/>
 *         &lt;element name="pageSetup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageSetup" minOccurs="0"/>
 *         &lt;element name="headerFooter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_HeaderFooter" minOccurs="0"/>
 *         &lt;element name="rowBreaks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageBreak" minOccurs="0"/>
 *         &lt;element name="colBreaks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageBreak" minOccurs="0"/>
 *         &lt;element name="customProperties" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomProperties" minOccurs="0"/>
 *         &lt;element name="cellWatches" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CellWatches" minOccurs="0"/>
 *         &lt;element name="ignoredErrors" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_IgnoredErrors" minOccurs="0"/>
 *         &lt;element name="smartTags" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SmartTags" minOccurs="0"/>
 *         &lt;element name="drawing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Drawing" minOccurs="0"/>
 *         &lt;element name="picture" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetBackgroundPicture" minOccurs="0"/>
 *         &lt;element name="oleObjects" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_OleObjects" minOccurs="0"/>
 *         &lt;element name="controls" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Controls" minOccurs="0"/>
 *         &lt;element name="webPublishItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WebPublishItems" minOccurs="0"/>
 *         &lt;element name="tableParts" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TableParts" minOccurs="0"/>
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
@XmlType(name = "CT_Worksheet", propOrder = {
    "sheetPr",
    "dimension",
    "sheetViews",
    "sheetFormatPr",
    "cols",
    "sheetData",
    "sheetCalcPr",
    "sheetProtection",
    "protectedRanges",
    "scenarios",
    "autoFilter",
    "sortState",
    "dataConsolidate",
    "customSheetViews",
    "mergeCells",
    "phoneticPr",
    "conditionalFormatting",
    "dataValidations",
    "hyperlinks",
    "printOptions",
    "pageMargins",
    "pageSetup",
    "headerFooter",
    "rowBreaks",
    "colBreaks",
    "customProperties",
    "cellWatches",
    "ignoredErrors",
    "smartTags",
    "drawing",
    "picture",
    "oleObjects",
    "controls",
    "webPublishItems",
    "tableParts",
    "extLst"
})
public class CTWorksheet implements Child
{

    protected CTSheetPr sheetPr;
    protected CTSheetDimension dimension;
    protected SheetViews sheetViews;
    protected CTSheetFormatPr sheetFormatPr;
    protected List<Cols> cols;
    @XmlElement(required = true)
    protected SheetData sheetData;
    protected CTSheetCalcPr sheetCalcPr;
    protected CTSheetProtection sheetProtection;
    protected CTProtectedRanges protectedRanges;
    protected CTScenarios scenarios;
    protected CTAutoFilter autoFilter;
    protected CTSortState sortState;
    protected CTDataConsolidate dataConsolidate;
    protected CTCustomSheetViews customSheetViews;
    protected CTMergeCells mergeCells;
    protected CTPhoneticPr phoneticPr;
    protected List<CTConditionalFormatting> conditionalFormatting;
    protected CTDataValidations dataValidations;
    protected CTHyperlinks hyperlinks;
    protected CTPrintOptions printOptions;
    protected CTPageMargins pageMargins;
    protected CTPageSetup pageSetup;
    protected CTHeaderFooter headerFooter;
    protected CTPageBreak rowBreaks;
    protected CTPageBreak colBreaks;
    protected CTCustomProperties customProperties;
    protected CTCellWatches cellWatches;
    protected CTIgnoredErrors ignoredErrors;
    protected CTSmartTags smartTags;
    protected CTDrawing drawing;
    protected CTSheetBackgroundPicture picture;
    protected CTOleObjects oleObjects;
    protected CTControls controls;
    protected CTWebPublishItems webPublishItems;
    protected CTTableParts tableParts;
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
     * Gets the value of the sheetCalcPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetCalcPr }
     *     
     */
    public CTSheetCalcPr getSheetCalcPr() {
        return sheetCalcPr;
    }

    /**
     * Sets the value of the sheetCalcPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetCalcPr }
     *     
     */
    public void setSheetCalcPr(CTSheetCalcPr value) {
        this.sheetCalcPr = value;
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
     * Gets the value of the protectedRanges property.
     * 
     * @return
     *     possible object is
     *     {@link CTProtectedRanges }
     *     
     */
    public CTProtectedRanges getProtectedRanges() {
        return protectedRanges;
    }

    /**
     * Sets the value of the protectedRanges property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTProtectedRanges }
     *     
     */
    public void setProtectedRanges(CTProtectedRanges value) {
        this.protectedRanges = value;
    }

    /**
     * Gets the value of the scenarios property.
     * 
     * @return
     *     possible object is
     *     {@link CTScenarios }
     *     
     */
    public CTScenarios getScenarios() {
        return scenarios;
    }

    /**
     * Sets the value of the scenarios property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScenarios }
     *     
     */
    public void setScenarios(CTScenarios value) {
        this.scenarios = value;
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
     * Gets the value of the mergeCells property.
     * 
     * @return
     *     possible object is
     *     {@link CTMergeCells }
     *     
     */
    public CTMergeCells getMergeCells() {
        return mergeCells;
    }

    /**
     * Sets the value of the mergeCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMergeCells }
     *     
     */
    public void setMergeCells(CTMergeCells value) {
        this.mergeCells = value;
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
     * Gets the value of the dataValidations property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataValidations }
     *     
     */
    public CTDataValidations getDataValidations() {
        return dataValidations;
    }

    /**
     * Sets the value of the dataValidations property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataValidations }
     *     
     */
    public void setDataValidations(CTDataValidations value) {
        this.dataValidations = value;
    }

    /**
     * Gets the value of the hyperlinks property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlinks }
     *     
     */
    public CTHyperlinks getHyperlinks() {
        return hyperlinks;
    }

    /**
     * Sets the value of the hyperlinks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlinks }
     *     
     */
    public void setHyperlinks(CTHyperlinks value) {
        this.hyperlinks = value;
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
     * Gets the value of the cellWatches property.
     * 
     * @return
     *     possible object is
     *     {@link CTCellWatches }
     *     
     */
    public CTCellWatches getCellWatches() {
        return cellWatches;
    }

    /**
     * Sets the value of the cellWatches property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCellWatches }
     *     
     */
    public void setCellWatches(CTCellWatches value) {
        this.cellWatches = value;
    }

    /**
     * Gets the value of the ignoredErrors property.
     * 
     * @return
     *     possible object is
     *     {@link CTIgnoredErrors }
     *     
     */
    public CTIgnoredErrors getIgnoredErrors() {
        return ignoredErrors;
    }

    /**
     * Sets the value of the ignoredErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIgnoredErrors }
     *     
     */
    public void setIgnoredErrors(CTIgnoredErrors value) {
        this.ignoredErrors = value;
    }

    /**
     * Gets the value of the smartTags property.
     * 
     * @return
     *     possible object is
     *     {@link CTSmartTags }
     *     
     */
    public CTSmartTags getSmartTags() {
        return smartTags;
    }

    /**
     * Sets the value of the smartTags property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSmartTags }
     *     
     */
    public void setSmartTags(CTSmartTags value) {
        this.smartTags = value;
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
     * Gets the value of the controls property.
     * 
     * @return
     *     possible object is
     *     {@link CTControls }
     *     
     */
    public CTControls getControls() {
        return controls;
    }

    /**
     * Sets the value of the controls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTControls }
     *     
     */
    public void setControls(CTControls value) {
        this.controls = value;
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
     * Gets the value of the tableParts property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableParts }
     *     
     */
    public CTTableParts getTableParts() {
        return tableParts;
    }

    /**
     * Sets the value of the tableParts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableParts }
     *     
     */
    public void setTableParts(CTTableParts value) {
        this.tableParts = value;
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
