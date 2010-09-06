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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_AutoFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AutoFilter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filterColumn" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FilterColumn" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sortState" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SortState" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ref" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AutoFilter", propOrder = {
    "filterColumn",
    "sortState",
    "extLst"
})
public class CTAutoFilter {

    protected List<CTFilterColumn> filterColumn;
    protected CTSortState sortState;
    protected CTExtensionList extLst;
    @XmlAttribute
    protected String ref;

    /**
     * Gets the value of the filterColumn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filterColumn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilterColumn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFilterColumn }
     * 
     * 
     */
    public List<CTFilterColumn> getFilterColumn() {
        if (filterColumn == null) {
            filterColumn = new ArrayList<CTFilterColumn>();
        }
        return this.filterColumn;
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

}
