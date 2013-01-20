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
 *       &lt;attribute name="customList" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
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
public class CTSortCondition implements Child
{

    @XmlAttribute(name = "descending")
    protected Boolean descending;
    @XmlAttribute(name = "sortBy")
    protected STSortBy sortBy;
    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlAttribute(name = "customList")
    protected String customList;
    @XmlAttribute(name = "dxfId")
    protected Long dxfId;
    @XmlAttribute(name = "iconSet")
    protected String iconSet;
    @XmlAttribute(name = "iconId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long iconId;
    @XmlTransient
    private Object parent;

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
