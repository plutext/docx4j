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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CustomSheetView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CustomSheetView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pane" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Pane" minOccurs="0"/>
 *         &lt;element name="selection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Selection" minOccurs="0"/>
 *         &lt;element name="rowBreaks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageBreak" minOccurs="0"/>
 *         &lt;element name="colBreaks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageBreak" minOccurs="0"/>
 *         &lt;element name="pageMargins" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageMargins" minOccurs="0"/>
 *         &lt;element name="printOptions" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PrintOptions" minOccurs="0"/>
 *         &lt;element name="pageSetup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PageSetup" minOccurs="0"/>
 *         &lt;element name="headerFooter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_HeaderFooter" minOccurs="0"/>
 *         &lt;element name="autoFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_AutoFilter" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="scale" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="100" />
 *       &lt;attribute name="colorId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="64" />
 *       &lt;attribute name="showPageBreaks" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showFormulas" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showGridLines" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showRowCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="outlineSymbols" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="zeroValues" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="fitToPage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="printArea" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="filter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showAutoFilter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="hiddenRows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="hiddenColumns" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="state" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SheetState" default="visible" />
 *       &lt;attribute name="filterUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="view" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SheetViewType" default="normal" />
 *       &lt;attribute name="showRuler" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="topLeftCell" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CustomSheetView", propOrder = {
    "pane",
    "selection",
    "rowBreaks",
    "colBreaks",
    "pageMargins",
    "printOptions",
    "pageSetup",
    "headerFooter",
    "autoFilter",
    "extLst"
})
public class CTCustomSheetView implements Child
{

    protected CTPane pane;
    protected CTSelection selection;
    protected CTPageBreak rowBreaks;
    protected CTPageBreak colBreaks;
    protected CTPageMargins pageMargins;
    protected CTPrintOptions printOptions;
    protected CTPageSetup pageSetup;
    protected CTHeaderFooter headerFooter;
    protected CTAutoFilter autoFilter;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "guid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(name = "scale")
    @XmlSchemaType(name = "unsignedInt")
    protected Long scale;
    @XmlAttribute(name = "colorId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long colorId;
    @XmlAttribute(name = "showPageBreaks")
    protected Boolean showPageBreaks;
    @XmlAttribute(name = "showFormulas")
    protected Boolean showFormulas;
    @XmlAttribute(name = "showGridLines")
    protected Boolean showGridLines;
    @XmlAttribute(name = "showRowCol")
    protected Boolean showRowCol;
    @XmlAttribute(name = "outlineSymbols")
    protected Boolean outlineSymbols;
    @XmlAttribute(name = "zeroValues")
    protected Boolean zeroValues;
    @XmlAttribute(name = "fitToPage")
    protected Boolean fitToPage;
    @XmlAttribute(name = "printArea")
    protected Boolean printArea;
    @XmlAttribute(name = "filter")
    protected Boolean filter;
    @XmlAttribute(name = "showAutoFilter")
    protected Boolean showAutoFilter;
    @XmlAttribute(name = "hiddenRows")
    protected Boolean hiddenRows;
    @XmlAttribute(name = "hiddenColumns")
    protected Boolean hiddenColumns;
    @XmlAttribute(name = "state")
    protected STSheetState state;
    @XmlAttribute(name = "filterUnique")
    protected Boolean filterUnique;
    @XmlAttribute(name = "view")
    protected STSheetViewType view;
    @XmlAttribute(name = "showRuler")
    protected Boolean showRuler;
    @XmlAttribute(name = "topLeftCell")
    protected String topLeftCell;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pane property.
     * 
     * @return
     *     possible object is
     *     {@link CTPane }
     *     
     */
    public CTPane getPane() {
        return pane;
    }

    /**
     * Sets the value of the pane property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPane }
     *     
     */
    public void setPane(CTPane value) {
        this.pane = value;
    }

    /**
     * Gets the value of the selection property.
     * 
     * @return
     *     possible object is
     *     {@link CTSelection }
     *     
     */
    public CTSelection getSelection() {
        return selection;
    }

    /**
     * Sets the value of the selection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSelection }
     *     
     */
    public void setSelection(CTSelection value) {
        this.selection = value;
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
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getScale() {
        if (scale == null) {
            return  100L;
        } else {
            return scale;
        }
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setScale(Long value) {
        this.scale = value;
    }

    /**
     * Gets the value of the colorId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getColorId() {
        if (colorId == null) {
            return  64L;
        } else {
            return colorId;
        }
    }

    /**
     * Sets the value of the colorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setColorId(Long value) {
        this.colorId = value;
    }

    /**
     * Gets the value of the showPageBreaks property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowPageBreaks() {
        if (showPageBreaks == null) {
            return false;
        } else {
            return showPageBreaks;
        }
    }

    /**
     * Sets the value of the showPageBreaks property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowPageBreaks(Boolean value) {
        this.showPageBreaks = value;
    }

    /**
     * Gets the value of the showFormulas property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowFormulas() {
        if (showFormulas == null) {
            return false;
        } else {
            return showFormulas;
        }
    }

    /**
     * Sets the value of the showFormulas property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowFormulas(Boolean value) {
        this.showFormulas = value;
    }

    /**
     * Gets the value of the showGridLines property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowGridLines() {
        if (showGridLines == null) {
            return true;
        } else {
            return showGridLines;
        }
    }

    /**
     * Sets the value of the showGridLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowGridLines(Boolean value) {
        this.showGridLines = value;
    }

    /**
     * Gets the value of the showRowCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowRowCol() {
        if (showRowCol == null) {
            return true;
        } else {
            return showRowCol;
        }
    }

    /**
     * Sets the value of the showRowCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowRowCol(Boolean value) {
        this.showRowCol = value;
    }

    /**
     * Gets the value of the outlineSymbols property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutlineSymbols() {
        if (outlineSymbols == null) {
            return true;
        } else {
            return outlineSymbols;
        }
    }

    /**
     * Sets the value of the outlineSymbols property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutlineSymbols(Boolean value) {
        this.outlineSymbols = value;
    }

    /**
     * Gets the value of the zeroValues property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isZeroValues() {
        if (zeroValues == null) {
            return true;
        } else {
            return zeroValues;
        }
    }

    /**
     * Sets the value of the zeroValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setZeroValues(Boolean value) {
        this.zeroValues = value;
    }

    /**
     * Gets the value of the fitToPage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFitToPage() {
        if (fitToPage == null) {
            return false;
        } else {
            return fitToPage;
        }
    }

    /**
     * Sets the value of the fitToPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFitToPage(Boolean value) {
        this.fitToPage = value;
    }

    /**
     * Gets the value of the printArea property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPrintArea() {
        if (printArea == null) {
            return false;
        } else {
            return printArea;
        }
    }

    /**
     * Sets the value of the printArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrintArea(Boolean value) {
        this.printArea = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFilter() {
        if (filter == null) {
            return false;
        } else {
            return filter;
        }
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFilter(Boolean value) {
        this.filter = value;
    }

    /**
     * Gets the value of the showAutoFilter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowAutoFilter() {
        if (showAutoFilter == null) {
            return false;
        } else {
            return showAutoFilter;
        }
    }

    /**
     * Sets the value of the showAutoFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowAutoFilter(Boolean value) {
        this.showAutoFilter = value;
    }

    /**
     * Gets the value of the hiddenRows property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenRows() {
        if (hiddenRows == null) {
            return false;
        } else {
            return hiddenRows;
        }
    }

    /**
     * Sets the value of the hiddenRows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenRows(Boolean value) {
        this.hiddenRows = value;
    }

    /**
     * Gets the value of the hiddenColumns property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenColumns() {
        if (hiddenColumns == null) {
            return false;
        } else {
            return hiddenColumns;
        }
    }

    /**
     * Sets the value of the hiddenColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenColumns(Boolean value) {
        this.hiddenColumns = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link STSheetState }
     *     
     */
    public STSheetState getState() {
        if (state == null) {
            return STSheetState.VISIBLE;
        } else {
            return state;
        }
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSheetState }
     *     
     */
    public void setState(STSheetState value) {
        this.state = value;
    }

    /**
     * Gets the value of the filterUnique property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFilterUnique() {
        if (filterUnique == null) {
            return false;
        } else {
            return filterUnique;
        }
    }

    /**
     * Sets the value of the filterUnique property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFilterUnique(Boolean value) {
        this.filterUnique = value;
    }

    /**
     * Gets the value of the view property.
     * 
     * @return
     *     possible object is
     *     {@link STSheetViewType }
     *     
     */
    public STSheetViewType getView() {
        if (view == null) {
            return STSheetViewType.NORMAL;
        } else {
            return view;
        }
    }

    /**
     * Sets the value of the view property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSheetViewType }
     *     
     */
    public void setView(STSheetViewType value) {
        this.view = value;
    }

    /**
     * Gets the value of the showRuler property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowRuler() {
        if (showRuler == null) {
            return true;
        } else {
            return showRuler;
        }
    }

    /**
     * Sets the value of the showRuler property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowRuler(Boolean value) {
        this.showRuler = value;
    }

    /**
     * Gets the value of the topLeftCell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopLeftCell() {
        return topLeftCell;
    }

    /**
     * Sets the value of the topLeftCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopLeftCell(String value) {
        this.topLeftCell = value;
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
