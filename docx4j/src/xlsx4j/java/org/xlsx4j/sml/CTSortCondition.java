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
 * <p>Java class for CT_SortCondition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SortCondition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="descending" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sortBy" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SortBy" default="value" />
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="customList" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="dxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="iconSet" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_IconSetType" default="3Arrows" />
 *       &lt;attribute name="iconId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SortCondition")
public class CTSortCondition {

    @XmlAttribute
    protected Boolean descending;
    @XmlAttribute
    protected STSortBy sortBy;
    @XmlAttribute(required = true)
    protected String ref;
    @XmlAttribute
    protected String customList;
    @XmlAttribute
    protected Long dxfId;
    @XmlAttribute
    protected String iconSet;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long iconId;

    /**
     * Gets the value of the descending property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDescending() {
        if (descending == null) {
            return false;
        } else {
            return descending;
        }
    }

    /**
     * Sets the value of the descending property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDescending(Boolean value) {
        this.descending = value;
    }

    /**
     * Gets the value of the sortBy property.
     * 
     * @return
     *     possible object is
     *     {@link STSortBy }
     *     
     */
    public STSortBy getSortBy() {
        if (sortBy == null) {
            return STSortBy.VALUE;
        } else {
            return sortBy;
        }
    }

    /**
     * Sets the value of the sortBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSortBy }
     *     
     */
    public void setSortBy(STSortBy value) {
        this.sortBy = value;
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
     * Gets the value of the customList property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomList() {
        return customList;
    }

    /**
     * Sets the value of the customList property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomList(String value) {
        this.customList = value;
    }

    /**
     * Gets the value of the dxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDxfId() {
        return dxfId;
    }

    /**
     * Sets the value of the dxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDxfId(Long value) {
        this.dxfId = value;
    }

    /**
     * Gets the value of the iconSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIconSet() {
        if (iconSet == null) {
            return "3Arrows";
        } else {
            return iconSet;
        }
    }

    /**
     * Sets the value of the iconSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIconSet(String value) {
        this.iconSet = value;
    }

    /**
     * Gets the value of the iconId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIconId() {
        return iconId;
    }

    /**
     * Sets the value of the iconId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIconId(Long value) {
        this.iconId = value;
    }

}
