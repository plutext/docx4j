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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_RevisionComment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionComment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="sheetId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="cell" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Guid" />
 *       &lt;attribute name="action" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_RevisionAction" default="add" />
 *       &lt;attribute name="alwaysShow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="old" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="hiddenRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="hiddenColumn" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="author" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="oldLength" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="newLength" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionComment")
public class CTRevisionComment {

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sheetId;
    @XmlAttribute(required = true)
    protected String cell;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute
    protected STRevisionAction action;
    @XmlAttribute
    protected Boolean alwaysShow;
    @XmlAttribute
    protected Boolean old;
    @XmlAttribute
    protected Boolean hiddenRow;
    @XmlAttribute
    protected Boolean hiddenColumn;
    @XmlAttribute(required = true)
    protected String author;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long oldLength;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long newLength;

    /**
     * Gets the value of the sheetId property.
     * 
     */
    public long getSheetId() {
        return sheetId;
    }

    /**
     * Sets the value of the sheetId property.
     * 
     */
    public void setSheetId(long value) {
        this.sheetId = value;
    }

    /**
     * Gets the value of the cell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCell() {
        return cell;
    }

    /**
     * Sets the value of the cell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCell(String value) {
        this.cell = value;
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
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link STRevisionAction }
     *     
     */
    public STRevisionAction getAction() {
        if (action == null) {
            return STRevisionAction.ADD;
        } else {
            return action;
        }
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link STRevisionAction }
     *     
     */
    public void setAction(STRevisionAction value) {
        this.action = value;
    }

    /**
     * Gets the value of the alwaysShow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAlwaysShow() {
        if (alwaysShow == null) {
            return false;
        } else {
            return alwaysShow;
        }
    }

    /**
     * Sets the value of the alwaysShow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAlwaysShow(Boolean value) {
        this.alwaysShow = value;
    }

    /**
     * Gets the value of the old property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOld() {
        if (old == null) {
            return false;
        } else {
            return old;
        }
    }

    /**
     * Sets the value of the old property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOld(Boolean value) {
        this.old = value;
    }

    /**
     * Gets the value of the hiddenRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenRow() {
        if (hiddenRow == null) {
            return false;
        } else {
            return hiddenRow;
        }
    }

    /**
     * Sets the value of the hiddenRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenRow(Boolean value) {
        this.hiddenRow = value;
    }

    /**
     * Gets the value of the hiddenColumn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenColumn() {
        if (hiddenColumn == null) {
            return false;
        } else {
            return hiddenColumn;
        }
    }

    /**
     * Sets the value of the hiddenColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenColumn(Boolean value) {
        this.hiddenColumn = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the oldLength property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getOldLength() {
        if (oldLength == null) {
            return  0L;
        } else {
            return oldLength;
        }
    }

    /**
     * Sets the value of the oldLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOldLength(Long value) {
        this.oldLength = value;
    }

    /**
     * Gets the value of the newLength property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getNewLength() {
        if (newLength == null) {
            return  0L;
        } else {
            return newLength;
        }
    }

    /**
     * Sets the value of the newLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNewLength(Long value) {
        this.newLength = value;
    }

}
