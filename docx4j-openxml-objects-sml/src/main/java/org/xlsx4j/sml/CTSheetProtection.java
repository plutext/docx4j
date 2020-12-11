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
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SheetProtection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SheetProtection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="password" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedShortHex" />
 *       &lt;attribute name="algorithmName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="hashValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="saltValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="spinCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="sheet" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="objects" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="scenarios" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="formatCells" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="formatColumns" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="formatRows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="insertColumns" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="insertRows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="insertHyperlinks" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="deleteColumns" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="deleteRows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="selectLockedCells" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sort" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="autoFilter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="pivotTables" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="selectUnlockedCells" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SheetProtection")
public class CTSheetProtection implements Child
{

    @XmlAttribute(name = "password")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] password;
    @XmlAttribute(name = "algorithmName")
    protected String algorithmName;
    @XmlAttribute(name = "hashValue")
    protected byte[] hashValue;
    @XmlAttribute(name = "saltValue")
    protected byte[] saltValue;
    @XmlAttribute(name = "spinCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long spinCount;
    @XmlAttribute(name = "sheet")
    protected Boolean sheet;
    @XmlAttribute(name = "objects")
    protected Boolean objects;
    @XmlAttribute(name = "scenarios")
    protected Boolean scenarios;
    @XmlAttribute(name = "formatCells")
    protected Boolean formatCells;
    @XmlAttribute(name = "formatColumns")
    protected Boolean formatColumns;
    @XmlAttribute(name = "formatRows")
    protected Boolean formatRows;
    @XmlAttribute(name = "insertColumns")
    protected Boolean insertColumns;
    @XmlAttribute(name = "insertRows")
    protected Boolean insertRows;
    @XmlAttribute(name = "insertHyperlinks")
    protected Boolean insertHyperlinks;
    @XmlAttribute(name = "deleteColumns")
    protected Boolean deleteColumns;
    @XmlAttribute(name = "deleteRows")
    protected Boolean deleteRows;
    @XmlAttribute(name = "selectLockedCells")
    protected Boolean selectLockedCells;
    @XmlAttribute(name = "sort")
    protected Boolean sort;
    @XmlAttribute(name = "autoFilter")
    protected Boolean autoFilter;
    @XmlAttribute(name = "pivotTables")
    protected Boolean pivotTables;
    @XmlAttribute(name = "selectUnlockedCells")
    protected Boolean selectUnlockedCells;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(byte[] value) {
        this.password = value;
    }

    /**
     * Gets the value of the algorithmName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Sets the value of the algorithmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlgorithmName(String value) {
        this.algorithmName = value;
    }

    /**
     * Gets the value of the hashValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getHashValue() {
        return hashValue;
    }

    /**
     * Sets the value of the hashValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setHashValue(byte[] value) {
        this.hashValue = value;
    }

    /**
     * Gets the value of the saltValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSaltValue() {
        return saltValue;
    }

    /**
     * Sets the value of the saltValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSaltValue(byte[] value) {
        this.saltValue = value;
    }

    /**
     * Gets the value of the spinCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSpinCount() {
        return spinCount;
    }

    /**
     * Sets the value of the spinCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSpinCount(Long value) {
        this.spinCount = value;
    }

    /**
     * Gets the value of the sheet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSheet() {
        if (sheet == null) {
            return false;
        } else {
            return sheet;
        }
    }

    /**
     * Sets the value of the sheet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSheet(Boolean value) {
        this.sheet = value;
    }

    /**
     * Gets the value of the objects property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isObjects() {
        if (objects == null) {
            return false;
        } else {
            return objects;
        }
    }

    /**
     * Sets the value of the objects property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setObjects(Boolean value) {
        this.objects = value;
    }

    /**
     * Gets the value of the scenarios property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isScenarios() {
        if (scenarios == null) {
            return false;
        } else {
            return scenarios;
        }
    }

    /**
     * Sets the value of the scenarios property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setScenarios(Boolean value) {
        this.scenarios = value;
    }

    /**
     * Gets the value of the formatCells property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFormatCells() {
        if (formatCells == null) {
            return true;
        } else {
            return formatCells;
        }
    }

    /**
     * Sets the value of the formatCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFormatCells(Boolean value) {
        this.formatCells = value;
    }

    /**
     * Gets the value of the formatColumns property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFormatColumns() {
        if (formatColumns == null) {
            return true;
        } else {
            return formatColumns;
        }
    }

    /**
     * Sets the value of the formatColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFormatColumns(Boolean value) {
        this.formatColumns = value;
    }

    /**
     * Gets the value of the formatRows property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFormatRows() {
        if (formatRows == null) {
            return true;
        } else {
            return formatRows;
        }
    }

    /**
     * Sets the value of the formatRows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFormatRows(Boolean value) {
        this.formatRows = value;
    }

    /**
     * Gets the value of the insertColumns property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertColumns() {
        if (insertColumns == null) {
            return true;
        } else {
            return insertColumns;
        }
    }

    /**
     * Sets the value of the insertColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertColumns(Boolean value) {
        this.insertColumns = value;
    }

    /**
     * Gets the value of the insertRows property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertRows() {
        if (insertRows == null) {
            return true;
        } else {
            return insertRows;
        }
    }

    /**
     * Sets the value of the insertRows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertRows(Boolean value) {
        this.insertRows = value;
    }

    /**
     * Gets the value of the insertHyperlinks property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsertHyperlinks() {
        if (insertHyperlinks == null) {
            return true;
        } else {
            return insertHyperlinks;
        }
    }

    /**
     * Sets the value of the insertHyperlinks property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsertHyperlinks(Boolean value) {
        this.insertHyperlinks = value;
    }

    /**
     * Gets the value of the deleteColumns property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDeleteColumns() {
        if (deleteColumns == null) {
            return true;
        } else {
            return deleteColumns;
        }
    }

    /**
     * Sets the value of the deleteColumns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeleteColumns(Boolean value) {
        this.deleteColumns = value;
    }

    /**
     * Gets the value of the deleteRows property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDeleteRows() {
        if (deleteRows == null) {
            return true;
        } else {
            return deleteRows;
        }
    }

    /**
     * Sets the value of the deleteRows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeleteRows(Boolean value) {
        this.deleteRows = value;
    }

    /**
     * Gets the value of the selectLockedCells property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSelectLockedCells() {
        if (selectLockedCells == null) {
            return false;
        } else {
            return selectLockedCells;
        }
    }

    /**
     * Sets the value of the selectLockedCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSelectLockedCells(Boolean value) {
        this.selectLockedCells = value;
    }

    /**
     * Gets the value of the sort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSort() {
        if (sort == null) {
            return true;
        } else {
            return sort;
        }
    }

    /**
     * Sets the value of the sort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSort(Boolean value) {
        this.sort = value;
    }

    /**
     * Gets the value of the autoFilter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoFilter() {
        if (autoFilter == null) {
            return true;
        } else {
            return autoFilter;
        }
    }

    /**
     * Sets the value of the autoFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoFilter(Boolean value) {
        this.autoFilter = value;
    }

    /**
     * Gets the value of the pivotTables property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPivotTables() {
        if (pivotTables == null) {
            return true;
        } else {
            return pivotTables;
        }
    }

    /**
     * Sets the value of the pivotTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPivotTables(Boolean value) {
        this.pivotTables = value;
    }

    /**
     * Gets the value of the selectUnlockedCells property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSelectUnlockedCells() {
        if (selectUnlockedCells == null) {
            return false;
        } else {
            return selectUnlockedCells;
        }
    }

    /**
     * Sets the value of the selectUnlockedCells property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSelectUnlockedCells(Boolean value) {
        this.selectUnlockedCells = value;
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
