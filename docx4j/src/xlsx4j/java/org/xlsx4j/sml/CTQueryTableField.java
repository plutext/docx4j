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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_QueryTableField complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_QueryTableField">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="dataBound" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="rowNumbers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="fillFormulas" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="clipped" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="tableColumnId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_QueryTableField", propOrder = {
    "extLst"
})
public class CTQueryTableField {

    protected CTExtensionList extLst;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean dataBound;
    @XmlAttribute
    protected Boolean rowNumbers;
    @XmlAttribute
    protected Boolean fillFormulas;
    @XmlAttribute
    protected Boolean clipped;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long tableColumnId;

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
     * Gets the value of the dataBound property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDataBound() {
        if (dataBound == null) {
            return true;
        } else {
            return dataBound;
        }
    }

    /**
     * Sets the value of the dataBound property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataBound(Boolean value) {
        this.dataBound = value;
    }

    /**
     * Gets the value of the rowNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRowNumbers() {
        if (rowNumbers == null) {
            return false;
        } else {
            return rowNumbers;
        }
    }

    /**
     * Sets the value of the rowNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRowNumbers(Boolean value) {
        this.rowNumbers = value;
    }

    /**
     * Gets the value of the fillFormulas property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFillFormulas() {
        if (fillFormulas == null) {
            return false;
        } else {
            return fillFormulas;
        }
    }

    /**
     * Sets the value of the fillFormulas property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFillFormulas(Boolean value) {
        this.fillFormulas = value;
    }

    /**
     * Gets the value of the clipped property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isClipped() {
        if (clipped == null) {
            return false;
        } else {
            return clipped;
        }
    }

    /**
     * Sets the value of the clipped property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClipped(Boolean value) {
        this.clipped = value;
    }

    /**
     * Gets the value of the tableColumnId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTableColumnId() {
        if (tableColumnId == null) {
            return  0L;
        } else {
            return tableColumnId;
        }
    }

    /**
     * Sets the value of the tableColumnId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTableColumnId(Long value) {
        this.tableColumnId = value;
    }

}
