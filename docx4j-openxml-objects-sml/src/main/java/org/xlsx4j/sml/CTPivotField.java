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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PivotField complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotField">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="items" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Items" minOccurs="0"/>
 *         &lt;element name="autoSortScope" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_AutoSortScope" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="axis" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Axis" />
 *       &lt;attribute name="dataField" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="subtotalCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="showDropDowns" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="hiddenLevel" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="uniqueMemberProperty" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="compact" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="allDrilled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="numFmtId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_NumFmtId" />
 *       &lt;attribute name="outline" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="subtotalTop" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="multipleItemSelectionAllowed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dragToPage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragOff" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="showAll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="insertBlankRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="serverField" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="insertPageBreak" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="autoShow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="topAutoShow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="hideNewItems" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="measureFilter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="includeNewItemsInFilter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="itemPageCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="10" />
 *       &lt;attribute name="sortType" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FieldSortType" default="manual" />
 *       &lt;attribute name="dataSourceSort" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="nonAutoSortDefault" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="rankBy" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="defaultSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="sumSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="countASubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="avgSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="maxSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="minSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="productSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="countSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="stdDevSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="stdDevPSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="varSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="varPSubtotal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showPropCell" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showPropTip" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showPropAsCaption" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="defaultAttributeDrillState" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotField", propOrder = {
    "items",
    "autoSortScope",
    "extLst"
})
public class CTPivotField implements Child
{

    protected CTItems items;
    protected CTAutoSortScope autoSortScope;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "axis")
    protected STAxis axis;
    @XmlAttribute(name = "dataField")
    protected Boolean dataField;
    @XmlAttribute(name = "subtotalCaption")
    protected String subtotalCaption;
    @XmlAttribute(name = "showDropDowns")
    protected Boolean showDropDowns;
    @XmlAttribute(name = "hiddenLevel")
    protected Boolean hiddenLevel;
    @XmlAttribute(name = "uniqueMemberProperty")
    protected String uniqueMemberProperty;
    @XmlAttribute(name = "compact")
    protected Boolean compact;
    @XmlAttribute(name = "allDrilled")
    protected Boolean allDrilled;
    @XmlAttribute(name = "numFmtId")
    protected Long numFmtId;
    @XmlAttribute(name = "outline")
    protected Boolean outline;
    @XmlAttribute(name = "subtotalTop")
    protected Boolean subtotalTop;
    @XmlAttribute(name = "dragToRow")
    protected Boolean dragToRow;
    @XmlAttribute(name = "dragToCol")
    protected Boolean dragToCol;
    @XmlAttribute(name = "multipleItemSelectionAllowed")
    protected Boolean multipleItemSelectionAllowed;
    @XmlAttribute(name = "dragToPage")
    protected Boolean dragToPage;
    @XmlAttribute(name = "dragToData")
    protected Boolean dragToData;
    @XmlAttribute(name = "dragOff")
    protected Boolean dragOff;
    @XmlAttribute(name = "showAll")
    protected Boolean showAll;
    @XmlAttribute(name = "insertBlankRow")
    protected Boolean insertBlankRow;
    @XmlAttribute(name = "serverField")
    protected Boolean serverField;
    @XmlAttribute(name = "insertPageBreak")
    protected Boolean insertPageBreak;
    @XmlAttribute(name = "autoShow")
    protected Boolean autoShow;
    @XmlAttribute(name = "topAutoShow")
    protected Boolean topAutoShow;
    @XmlAttribute(name = "hideNewItems")
    protected Boolean hideNewItems;
    @XmlAttribute(name = "measureFilter")
    protected Boolean measureFilter;
    @XmlAttribute(name = "includeNewItemsInFilter")
    protected Boolean includeNewItemsInFilter;
    @XmlAttribute(name = "itemPageCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long itemPageCount;
    @XmlAttribute(name = "sortType")
    protected STFieldSortType sortType;
    @XmlAttribute(name = "dataSourceSort")
    protected Boolean dataSourceSort;
    @XmlAttribute(name = "nonAutoSortDefault")
    protected Boolean nonAutoSortDefault;
    @XmlAttribute(name = "rankBy")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rankBy;
    @XmlAttribute(name = "defaultSubtotal")
    protected Boolean defaultSubtotal;
    @XmlAttribute(name = "sumSubtotal")
    protected Boolean sumSubtotal;
    @XmlAttribute(name = "countASubtotal")
    protected Boolean countASubtotal;
    @XmlAttribute(name = "avgSubtotal")
    protected Boolean avgSubtotal;
    @XmlAttribute(name = "maxSubtotal")
    protected Boolean maxSubtotal;
    @XmlAttribute(name = "minSubtotal")
    protected Boolean minSubtotal;
    @XmlAttribute(name = "productSubtotal")
    protected Boolean productSubtotal;
    @XmlAttribute(name = "countSubtotal")
    protected Boolean countSubtotal;
    @XmlAttribute(name = "stdDevSubtotal")
    protected Boolean stdDevSubtotal;
    @XmlAttribute(name = "stdDevPSubtotal")
    protected Boolean stdDevPSubtotal;
    @XmlAttribute(name = "varSubtotal")
    protected Boolean varSubtotal;
    @XmlAttribute(name = "varPSubtotal")
    protected Boolean varPSubtotal;
    @XmlAttribute(name = "showPropCell")
    protected Boolean showPropCell;
    @XmlAttribute(name = "showPropTip")
    protected Boolean showPropTip;
    @XmlAttribute(name = "showPropAsCaption")
    protected Boolean showPropAsCaption;
    @XmlAttribute(name = "defaultAttributeDrillState")
    protected Boolean defaultAttributeDrillState;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link CTItems }
     *     
     */
    public CTItems getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTItems }
     *     
     */
    public void setItems(CTItems value) {
        this.items = value;
    }

    /**
     * Gets the value of the autoSortScope property.
     * 
     * @return
     *     possible object is
     *     {@link CTAutoSortScope }
     *     
     */
    public CTAutoSortScope getAutoSortScope() {
        return autoSortScope;
    }

    /**
     * Sets the value of the autoSortScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAutoSortScope }
     *     
     */
    public void setAutoSortScope(CTAutoSortScope value) {
        this.autoSortScope = value;
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
     * Gets the value of the axis property.
     * 
     * @return
     *     possible object is
     *     {@link STAxis }
     *     
     */
    public STAxis getAxis() {
        return axis;
    }

    /**
     * Sets the value of the axis property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAxis }
     *     
     */
    public void setAxis(STAxis value) {
        this.axis = value;
    }

    /**
     * Gets the value of the dataField property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDataField() {
        if (dataField == null) {
            return false;
        } else {
            return dataField;
        }
    }

    /**
     * Sets the value of the dataField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataField(Boolean value) {
        this.dataField = value;
    }

    /**
     * Gets the value of the subtotalCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubtotalCaption() {
        return subtotalCaption;
    }

    /**
     * Sets the value of the subtotalCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubtotalCaption(String value) {
        this.subtotalCaption = value;
    }

    /**
     * Gets the value of the showDropDowns property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowDropDowns() {
        if (showDropDowns == null) {
            return true;
        } else {
            return showDropDowns;
        }
    }

    /**
     * Sets the value of the showDropDowns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowDropDowns(Boolean value) {
        this.showDropDowns = value;
    }

    /**
     * Gets the value of the hiddenLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenLevel() {
        if (hiddenLevel == null) {
            return false;
        } else {
            return hiddenLevel;
        }
    }

    /**
     * Sets the value of the hiddenLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenLevel(Boolean value) {
        this.hiddenLevel = value;
    }

    /**
     * Gets the value of the uniqueMemberProperty property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueMemberProperty() {
        return uniqueMemberProperty;
    }

    /**
     * Sets the value of the uniqueMemberProperty property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueMemberProperty(String value) {
        this.uniqueMemberProperty = value;
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
     * Gets the value of the allDrilled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAllDrilled() {
        if (allDrilled == null) {
            return false;
        } else {
            return allDrilled;
        }
    }

    /**
     * Sets the value of the allDrilled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllDrilled(Boolean value) {
        this.allDrilled = value;
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
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutline() {
        if (outline == null) {
            return true;
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
     * Gets the value of the subtotalTop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSubtotalTop() {
        if (subtotalTop == null) {
            return true;
        } else {
            return subtotalTop;
        }
    }

    /**
     * Sets the value of the subtotalTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSubtotalTop(Boolean value) {
        this.subtotalTop = value;
    }

    /**
     * Gets the value of the dragToRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToRow() {
        if (dragToRow == null) {
            return true;
        } else {
            return dragToRow;
        }
    }

    /**
     * Sets the value of the dragToRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToRow(Boolean value) {
        this.dragToRow = value;
    }

    /**
     * Gets the value of the dragToCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToCol() {
        if (dragToCol == null) {
            return true;
        } else {
            return dragToCol;
        }
    }

    /**
     * Sets the value of the dragToCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToCol(Boolean value) {
        this.dragToCol = value;
    }

    /**
     * Gets the value of the multipleItemSelectionAllowed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMultipleItemSelectionAllowed() {
        if (multipleItemSelectionAllowed == null) {
            return false;
        } else {
            return multipleItemSelectionAllowed;
        }
    }

    /**
     * Sets the value of the multipleItemSelectionAllowed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMultipleItemSelectionAllowed(Boolean value) {
        this.multipleItemSelectionAllowed = value;
    }

    /**
     * Gets the value of the dragToPage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToPage() {
        if (dragToPage == null) {
            return true;
        } else {
            return dragToPage;
        }
    }

    /**
     * Sets the value of the dragToPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToPage(Boolean value) {
        this.dragToPage = value;
    }

    /**
     * Gets the value of the dragToData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToData() {
        if (dragToData == null) {
            return true;
        } else {
            return dragToData;
        }
    }

    /**
     * Sets the value of the dragToData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToData(Boolean value) {
        this.dragToData = value;
    }

    /**
     * Gets the value of the dragOff property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragOff() {
        if (dragOff == null) {
            return true;
        } else {
            return dragOff;
        }
    }

    /**
     * Sets the value of the dragOff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragOff(Boolean value) {
        this.dragOff = value;
    }

    /**
     * Gets the value of the showAll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowAll() {
        if (showAll == null) {
            return true;
        } else {
            return showAll;
        }
    }

    /**
     * Sets the value of the showAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowAll(Boolean value) {
        this.showAll = value;
    }

    /**
     * Gets the value of the insertBlankRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertBlankRow() {
        if (insertBlankRow == null) {
            return false;
        } else {
            return insertBlankRow;
        }
    }

    /**
     * Sets the value of the insertBlankRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertBlankRow(Boolean value) {
        this.insertBlankRow = value;
    }

    /**
     * Gets the value of the serverField property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isServerField() {
        if (serverField == null) {
            return false;
        } else {
            return serverField;
        }
    }

    /**
     * Sets the value of the serverField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setServerField(Boolean value) {
        this.serverField = value;
    }

    /**
     * Gets the value of the insertPageBreak property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertPageBreak() {
        if (insertPageBreak == null) {
            return false;
        } else {
            return insertPageBreak;
        }
    }

    /**
     * Sets the value of the insertPageBreak property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertPageBreak(Boolean value) {
        this.insertPageBreak = value;
    }

    /**
     * Gets the value of the autoShow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoShow() {
        if (autoShow == null) {
            return false;
        } else {
            return autoShow;
        }
    }

    /**
     * Sets the value of the autoShow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoShow(Boolean value) {
        this.autoShow = value;
    }

    /**
     * Gets the value of the topAutoShow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTopAutoShow() {
        if (topAutoShow == null) {
            return true;
        } else {
            return topAutoShow;
        }
    }

    /**
     * Sets the value of the topAutoShow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTopAutoShow(Boolean value) {
        this.topAutoShow = value;
    }

    /**
     * Gets the value of the hideNewItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHideNewItems() {
        if (hideNewItems == null) {
            return false;
        } else {
            return hideNewItems;
        }
    }

    /**
     * Sets the value of the hideNewItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHideNewItems(Boolean value) {
        this.hideNewItems = value;
    }

    /**
     * Gets the value of the measureFilter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMeasureFilter() {
        if (measureFilter == null) {
            return false;
        } else {
            return measureFilter;
        }
    }

    /**
     * Sets the value of the measureFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMeasureFilter(Boolean value) {
        this.measureFilter = value;
    }

    /**
     * Gets the value of the includeNewItemsInFilter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludeNewItemsInFilter() {
        if (includeNewItemsInFilter == null) {
            return false;
        } else {
            return includeNewItemsInFilter;
        }
    }

    /**
     * Sets the value of the includeNewItemsInFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeNewItemsInFilter(Boolean value) {
        this.includeNewItemsInFilter = value;
    }

    /**
     * Gets the value of the itemPageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getItemPageCount() {
        if (itemPageCount == null) {
            return  10L;
        } else {
            return itemPageCount;
        }
    }

    /**
     * Sets the value of the itemPageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setItemPageCount(Long value) {
        this.itemPageCount = value;
    }

    /**
     * Gets the value of the sortType property.
     * 
     * @return
     *     possible object is
     *     {@link STFieldSortType }
     *     
     */
    public STFieldSortType getSortType() {
        if (sortType == null) {
            return STFieldSortType.MANUAL;
        } else {
            return sortType;
        }
    }

    /**
     * Sets the value of the sortType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFieldSortType }
     *     
     */
    public void setSortType(STFieldSortType value) {
        this.sortType = value;
    }

    /**
     * Gets the value of the dataSourceSort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDataSourceSort() {
        return dataSourceSort;
    }

    /**
     * Sets the value of the dataSourceSort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataSourceSort(Boolean value) {
        this.dataSourceSort = value;
    }

    /**
     * Gets the value of the nonAutoSortDefault property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNonAutoSortDefault() {
        if (nonAutoSortDefault == null) {
            return false;
        } else {
            return nonAutoSortDefault;
        }
    }

    /**
     * Sets the value of the nonAutoSortDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNonAutoSortDefault(Boolean value) {
        this.nonAutoSortDefault = value;
    }

    /**
     * Gets the value of the rankBy property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRankBy() {
        return rankBy;
    }

    /**
     * Sets the value of the rankBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRankBy(Long value) {
        this.rankBy = value;
    }

    /**
     * Gets the value of the defaultSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefaultSubtotal() {
        if (defaultSubtotal == null) {
            return true;
        } else {
            return defaultSubtotal;
        }
    }

    /**
     * Sets the value of the defaultSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultSubtotal(Boolean value) {
        this.defaultSubtotal = value;
    }

    /**
     * Gets the value of the sumSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSumSubtotal() {
        if (sumSubtotal == null) {
            return false;
        } else {
            return sumSubtotal;
        }
    }

    /**
     * Sets the value of the sumSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSumSubtotal(Boolean value) {
        this.sumSubtotal = value;
    }

    /**
     * Gets the value of the countASubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCountASubtotal() {
        if (countASubtotal == null) {
            return false;
        } else {
            return countASubtotal;
        }
    }

    /**
     * Sets the value of the countASubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCountASubtotal(Boolean value) {
        this.countASubtotal = value;
    }

    /**
     * Gets the value of the avgSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAvgSubtotal() {
        if (avgSubtotal == null) {
            return false;
        } else {
            return avgSubtotal;
        }
    }

    /**
     * Sets the value of the avgSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAvgSubtotal(Boolean value) {
        this.avgSubtotal = value;
    }

    /**
     * Gets the value of the maxSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMaxSubtotal() {
        if (maxSubtotal == null) {
            return false;
        } else {
            return maxSubtotal;
        }
    }

    /**
     * Sets the value of the maxSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMaxSubtotal(Boolean value) {
        this.maxSubtotal = value;
    }

    /**
     * Gets the value of the minSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMinSubtotal() {
        if (minSubtotal == null) {
            return false;
        } else {
            return minSubtotal;
        }
    }

    /**
     * Sets the value of the minSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMinSubtotal(Boolean value) {
        this.minSubtotal = value;
    }

    /**
     * Gets the value of the productSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isProductSubtotal() {
        if (productSubtotal == null) {
            return false;
        } else {
            return productSubtotal;
        }
    }

    /**
     * Sets the value of the productSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProductSubtotal(Boolean value) {
        this.productSubtotal = value;
    }

    /**
     * Gets the value of the countSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCountSubtotal() {
        if (countSubtotal == null) {
            return false;
        } else {
            return countSubtotal;
        }
    }

    /**
     * Sets the value of the countSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCountSubtotal(Boolean value) {
        this.countSubtotal = value;
    }

    /**
     * Gets the value of the stdDevSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStdDevSubtotal() {
        if (stdDevSubtotal == null) {
            return false;
        } else {
            return stdDevSubtotal;
        }
    }

    /**
     * Sets the value of the stdDevSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStdDevSubtotal(Boolean value) {
        this.stdDevSubtotal = value;
    }

    /**
     * Gets the value of the stdDevPSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isStdDevPSubtotal() {
        if (stdDevPSubtotal == null) {
            return false;
        } else {
            return stdDevPSubtotal;
        }
    }

    /**
     * Sets the value of the stdDevPSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStdDevPSubtotal(Boolean value) {
        this.stdDevPSubtotal = value;
    }

    /**
     * Gets the value of the varSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVarSubtotal() {
        if (varSubtotal == null) {
            return false;
        } else {
            return varSubtotal;
        }
    }

    /**
     * Sets the value of the varSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVarSubtotal(Boolean value) {
        this.varSubtotal = value;
    }

    /**
     * Gets the value of the varPSubtotal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVarPSubtotal() {
        if (varPSubtotal == null) {
            return false;
        } else {
            return varPSubtotal;
        }
    }

    /**
     * Sets the value of the varPSubtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVarPSubtotal(Boolean value) {
        this.varPSubtotal = value;
    }

    /**
     * Gets the value of the showPropCell property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowPropCell() {
        if (showPropCell == null) {
            return false;
        } else {
            return showPropCell;
        }
    }

    /**
     * Sets the value of the showPropCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowPropCell(Boolean value) {
        this.showPropCell = value;
    }

    /**
     * Gets the value of the showPropTip property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowPropTip() {
        if (showPropTip == null) {
            return false;
        } else {
            return showPropTip;
        }
    }

    /**
     * Sets the value of the showPropTip property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowPropTip(Boolean value) {
        this.showPropTip = value;
    }

    /**
     * Gets the value of the showPropAsCaption property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowPropAsCaption() {
        if (showPropAsCaption == null) {
            return false;
        } else {
            return showPropAsCaption;
        }
    }

    /**
     * Sets the value of the showPropAsCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowPropAsCaption(Boolean value) {
        this.showPropAsCaption = value;
    }

    /**
     * Gets the value of the defaultAttributeDrillState property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefaultAttributeDrillState() {
        if (defaultAttributeDrillState == null) {
            return false;
        } else {
            return defaultAttributeDrillState;
        }
    }

    /**
     * Sets the value of the defaultAttributeDrillState property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultAttributeDrillState(Boolean value) {
        this.defaultAttributeDrillState = value;
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
