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
 * <p>Java class for CT_Table complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Table">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="autoFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_AutoFilter" minOccurs="0"/>
 *         &lt;element name="sortState" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SortState" minOccurs="0"/>
 *         &lt;element name="tableColumns" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TableColumns"/>
 *         &lt;element name="tableStyleInfo" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TableStyleInfo" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="displayName" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="comment" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="tableType" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_TableType" default="worksheet" />
 *       &lt;attribute name="headerRowCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="insertRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="insertRowShift" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="totalsRowCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="totalsRowShown" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="published" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="headerRowDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="dataDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="totalsRowDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="headerRowBorderDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="tableBorderDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="totalsRowBorderDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="headerRowCellStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="dataCellStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="totalsRowCellStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="connectionId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Table", propOrder = {
    "autoFilter",
    "sortState",
    "tableColumns",
    "tableStyleInfo",
    "extLst"
})
@XmlRootElement(name = "table")
public class CTTable implements Child
{

    protected CTAutoFilter autoFilter;
    protected CTSortState sortState;
    @XmlElement(required = true)
    protected CTTableColumns tableColumns;
    protected CTTableStyleInfo tableStyleInfo;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "displayName", required = true)
    protected String displayName;
    @XmlAttribute(name = "comment")
    protected String comment;
    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlAttribute(name = "tableType")
    protected STTableType tableType;
    @XmlAttribute(name = "headerRowCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long headerRowCount;
    @XmlAttribute(name = "insertRow")
    protected Boolean insertRow;
    @XmlAttribute(name = "insertRowShift")
    protected Boolean insertRowShift;
    @XmlAttribute(name = "totalsRowCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long totalsRowCount;
    @XmlAttribute(name = "totalsRowShown")
    protected Boolean totalsRowShown;
    @XmlAttribute(name = "published")
    protected Boolean published;
    @XmlAttribute(name = "headerRowDxfId")
    protected Long headerRowDxfId;
    @XmlAttribute(name = "dataDxfId")
    protected Long dataDxfId;
    @XmlAttribute(name = "totalsRowDxfId")
    protected Long totalsRowDxfId;
    @XmlAttribute(name = "headerRowBorderDxfId")
    protected Long headerRowBorderDxfId;
    @XmlAttribute(name = "tableBorderDxfId")
    protected Long tableBorderDxfId;
    @XmlAttribute(name = "totalsRowBorderDxfId")
    protected Long totalsRowBorderDxfId;
    @XmlAttribute(name = "headerRowCellStyle")
    protected String headerRowCellStyle;
    @XmlAttribute(name = "dataCellStyle")
    protected String dataCellStyle;
    @XmlAttribute(name = "totalsRowCellStyle")
    protected String totalsRowCellStyle;
    @XmlAttribute(name = "connectionId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long connectionId;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the tableColumns property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableColumns }
     *     
     */
    public CTTableColumns getTableColumns() {
        return tableColumns;
    }

    /**
     * Sets the value of the tableColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableColumns }
     *     
     */
    public void setTableColumns(CTTableColumns value) {
        this.tableColumns = value;
    }

    /**
     * Gets the value of the tableStyleInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableStyleInfo }
     *     
     */
    public CTTableStyleInfo getTableStyleInfo() {
        return tableStyleInfo;
    }

    /**
     * Sets the value of the tableStyleInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableStyleInfo }
     *     
     */
    public void setTableStyleInfo(CTTableStyleInfo value) {
        this.tableStyleInfo = value;
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
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
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
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the tableType property.
     * 
     * @return
     *     possible object is
     *     {@link STTableType }
     *     
     */
    public STTableType getTableType() {
        if (tableType == null) {
            return STTableType.WORKSHEET;
        } else {
            return tableType;
        }
    }

    /**
     * Sets the value of the tableType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTableType }
     *     
     */
    public void setTableType(STTableType value) {
        this.tableType = value;
    }

    /**
     * Gets the value of the headerRowCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getHeaderRowCount() {
        if (headerRowCount == null) {
            return  1L;
        } else {
            return headerRowCount;
        }
    }

    /**
     * Sets the value of the headerRowCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHeaderRowCount(Long value) {
        this.headerRowCount = value;
    }

    /**
     * Gets the value of the insertRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertRow() {
        if (insertRow == null) {
            return false;
        } else {
            return insertRow;
        }
    }

    /**
     * Sets the value of the insertRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertRow(Boolean value) {
        this.insertRow = value;
    }

    /**
     * Gets the value of the insertRowShift property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertRowShift() {
        if (insertRowShift == null) {
            return false;
        } else {
            return insertRowShift;
        }
    }

    /**
     * Sets the value of the insertRowShift property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertRowShift(Boolean value) {
        this.insertRowShift = value;
    }

    /**
     * Gets the value of the totalsRowCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTotalsRowCount() {
        if (totalsRowCount == null) {
            return  0L;
        } else {
            return totalsRowCount;
        }
    }

    /**
     * Sets the value of the totalsRowCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalsRowCount(Long value) {
        this.totalsRowCount = value;
    }

    /**
     * Gets the value of the totalsRowShown property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTotalsRowShown() {
        if (totalsRowShown == null) {
            return true;
        } else {
            return totalsRowShown;
        }
    }

    /**
     * Sets the value of the totalsRowShown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTotalsRowShown(Boolean value) {
        this.totalsRowShown = value;
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
     * Gets the value of the headerRowDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getHeaderRowDxfId() {
        return headerRowDxfId;
    }

    /**
     * Sets the value of the headerRowDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHeaderRowDxfId(Long value) {
        this.headerRowDxfId = value;
    }

    /**
     * Gets the value of the dataDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDataDxfId() {
        return dataDxfId;
    }

    /**
     * Sets the value of the dataDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDataDxfId(Long value) {
        this.dataDxfId = value;
    }

    /**
     * Gets the value of the totalsRowDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTotalsRowDxfId() {
        return totalsRowDxfId;
    }

    /**
     * Sets the value of the totalsRowDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalsRowDxfId(Long value) {
        this.totalsRowDxfId = value;
    }

    /**
     * Gets the value of the headerRowBorderDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getHeaderRowBorderDxfId() {
        return headerRowBorderDxfId;
    }

    /**
     * Sets the value of the headerRowBorderDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHeaderRowBorderDxfId(Long value) {
        this.headerRowBorderDxfId = value;
    }

    /**
     * Gets the value of the tableBorderDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTableBorderDxfId() {
        return tableBorderDxfId;
    }

    /**
     * Sets the value of the tableBorderDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTableBorderDxfId(Long value) {
        this.tableBorderDxfId = value;
    }

    /**
     * Gets the value of the totalsRowBorderDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTotalsRowBorderDxfId() {
        return totalsRowBorderDxfId;
    }

    /**
     * Sets the value of the totalsRowBorderDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalsRowBorderDxfId(Long value) {
        this.totalsRowBorderDxfId = value;
    }

    /**
     * Gets the value of the headerRowCellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaderRowCellStyle() {
        return headerRowCellStyle;
    }

    /**
     * Sets the value of the headerRowCellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaderRowCellStyle(String value) {
        this.headerRowCellStyle = value;
    }

    /**
     * Gets the value of the dataCellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataCellStyle() {
        return dataCellStyle;
    }

    /**
     * Sets the value of the dataCellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataCellStyle(String value) {
        this.dataCellStyle = value;
    }

    /**
     * Gets the value of the totalsRowCellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotalsRowCellStyle() {
        return totalsRowCellStyle;
    }

    /**
     * Sets the value of the totalsRowCellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalsRowCellStyle(String value) {
        this.totalsRowCellStyle = value;
    }

    /**
     * Gets the value of the connectionId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the value of the connectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setConnectionId(Long value) {
        this.connectionId = value;
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
