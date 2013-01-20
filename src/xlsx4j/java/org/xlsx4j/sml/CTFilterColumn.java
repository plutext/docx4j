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
 * <p>Java class for CT_FilterColumn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FilterColumn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="filters" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Filters" minOccurs="0"/>
 *         &lt;element name="top10" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Top10" minOccurs="0"/>
 *         &lt;element name="customFilters" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomFilters" minOccurs="0"/>
 *         &lt;element name="dynamicFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DynamicFilter" minOccurs="0"/>
 *         &lt;element name="colorFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ColorFilter" minOccurs="0"/>
 *         &lt;element name="iconFilter" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_IconFilter" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="colId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="hiddenButton" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showButton" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FilterColumn", propOrder = {
    "filters",
    "top10",
    "customFilters",
    "dynamicFilter",
    "colorFilter",
    "iconFilter",
    "extLst"
})
public class CTFilterColumn implements Child
{

    protected CTFilters filters;
    protected CTTop10 top10;
    protected CTCustomFilters customFilters;
    protected CTDynamicFilter dynamicFilter;
    protected CTColorFilter colorFilter;
    protected CTIconFilter iconFilter;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "colId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long colId;
    @XmlAttribute(name = "hiddenButton")
    protected Boolean hiddenButton;
    @XmlAttribute(name = "showButton")
    protected Boolean showButton;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the filters property.
     * 
     * @return
     *     possible object is
     *     {@link CTFilters }
     *     
     */
    public CTFilters getFilters() {
        return filters;
    }

    /**
     * Sets the value of the filters property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFilters }
     *     
     */
    public void setFilters(CTFilters value) {
        this.filters = value;
    }

    /**
     * Gets the value of the top10 property.
     * 
     * @return
     *     possible object is
     *     {@link CTTop10 }
     *     
     */
    public CTTop10 getTop10() {
        return top10;
    }

    /**
     * Sets the value of the top10 property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTop10 }
     *     
     */
    public void setTop10(CTTop10 value) {
        this.top10 = value;
    }

    /**
     * Gets the value of the customFilters property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomFilters }
     *     
     */
    public CTCustomFilters getCustomFilters() {
        return customFilters;
    }

    /**
     * Sets the value of the customFilters property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomFilters }
     *     
     */
    public void setCustomFilters(CTCustomFilters value) {
        this.customFilters = value;
    }

    /**
     * Gets the value of the dynamicFilter property.
     * 
     * @return
     *     possible object is
     *     {@link CTDynamicFilter }
     *     
     */
    public CTDynamicFilter getDynamicFilter() {
        return dynamicFilter;
    }

    /**
     * Sets the value of the dynamicFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDynamicFilter }
     *     
     */
    public void setDynamicFilter(CTDynamicFilter value) {
        this.dynamicFilter = value;
    }

    /**
     * Gets the value of the colorFilter property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorFilter }
     *     
     */
    public CTColorFilter getColorFilter() {
        return colorFilter;
    }

    /**
     * Sets the value of the colorFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorFilter }
     *     
     */
    public void setColorFilter(CTColorFilter value) {
        this.colorFilter = value;
    }

    /**
     * Gets the value of the iconFilter property.
     * 
     * @return
     *     possible object is
     *     {@link CTIconFilter }
     *     
     */
    public CTIconFilter getIconFilter() {
        return iconFilter;
    }

    /**
     * Sets the value of the iconFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIconFilter }
     *     
     */
    public void setIconFilter(CTIconFilter value) {
        this.iconFilter = value;
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
     * Gets the value of the colId property.
     * 
     */
    public long getColId() {
        return colId;
    }

    /**
     * Sets the value of the colId property.
     * 
     */
    public void setColId(long value) {
        this.colId = value;
    }

    /**
     * Gets the value of the hiddenButton property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHiddenButton() {
        if (hiddenButton == null) {
            return false;
        } else {
            return hiddenButton;
        }
    }

    /**
     * Sets the value of the hiddenButton property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHiddenButton(Boolean value) {
        this.hiddenButton = value;
    }

    /**
     * Gets the value of the showButton property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowButton() {
        if (showButton == null) {
            return true;
        } else {
            return showButton;
        }
    }

    /**
     * Sets the value of the showButton property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowButton(Boolean value) {
        this.showButton = value;
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
