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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SortState complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SortState">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sortCondition" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SortCondition" maxOccurs="64" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="columnSort" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="caseSensitive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="sortMethod" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_SortMethod" default="none" />
 *       &lt;attribute name="ref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SortState", propOrder = {
    "sortCondition",
    "extLst"
})
public class CTSortState implements Child
{

    protected List<CTSortCondition> sortCondition;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "columnSort")
    protected Boolean columnSort;
    @XmlAttribute(name = "caseSensitive")
    protected Boolean caseSensitive;
    @XmlAttribute(name = "sortMethod")
    protected STSortMethod sortMethod;
    @XmlAttribute(name = "ref", required = true)
    protected String ref;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sortCondition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sortCondition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSortCondition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSortCondition }
     * 
     * 
     */
    public List<CTSortCondition> getSortCondition() {
        if (sortCondition == null) {
            sortCondition = new ArrayList<CTSortCondition>();
        }
        return this.sortCondition;
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
     * Gets the value of the columnSort property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isColumnSort() {
        if (columnSort == null) {
            return false;
        } else {
            return columnSort;
        }
    }

    /**
     * Sets the value of the columnSort property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setColumnSort(Boolean value) {
        this.columnSort = value;
    }

    /**
     * Gets the value of the caseSensitive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCaseSensitive() {
        if (caseSensitive == null) {
            return false;
        } else {
            return caseSensitive;
        }
    }

    /**
     * Sets the value of the caseSensitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCaseSensitive(Boolean value) {
        this.caseSensitive = value;
    }

    /**
     * Gets the value of the sortMethod property.
     * 
     * @return
     *     possible object is
     *     {@link STSortMethod }
     *     
     */
    public STSortMethod getSortMethod() {
        if (sortMethod == null) {
            return STSortMethod.NONE;
        } else {
            return sortMethod;
        }
    }

    /**
     * Sets the value of the sortMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSortMethod }
     *     
     */
    public void setSortMethod(STSortMethod value) {
        this.sortMethod = value;
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
