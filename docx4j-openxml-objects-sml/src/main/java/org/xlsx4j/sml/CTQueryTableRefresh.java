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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_QueryTableRefresh complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_QueryTableRefresh">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryTableFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_QueryTableFields"/>
 *         &lt;element name="queryTableDeletedFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_QueryTableDeletedFields" minOccurs="0"/>
 *         &lt;element name="sortState" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SortState" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="preserveSortFilterLayout" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="fieldIdWrapped" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="headersInLastRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="minimumVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="nextId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="unboundColumnsLeft" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="unboundColumnsRight" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_QueryTableRefresh", propOrder = {
    "queryTableFields",
    "queryTableDeletedFields",
    "sortState",
    "extLst"
})
public class CTQueryTableRefresh implements Child
{

    @XmlElement(required = true)
    protected CTQueryTableFields queryTableFields;
    protected CTQueryTableDeletedFields queryTableDeletedFields;
    protected CTSortState sortState;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "preserveSortFilterLayout")
    protected Boolean preserveSortFilterLayout;
    @XmlAttribute(name = "fieldIdWrapped")
    protected Boolean fieldIdWrapped;
    @XmlAttribute(name = "headersInLastRefresh")
    protected Boolean headersInLastRefresh;
    @XmlAttribute(name = "minimumVersion")
    @XmlSchemaType(name = "unsignedByte")
    protected Short minimumVersion;
    @XmlAttribute(name = "nextId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long nextId;
    @XmlAttribute(name = "unboundColumnsLeft")
    @XmlSchemaType(name = "unsignedInt")
    protected Long unboundColumnsLeft;
    @XmlAttribute(name = "unboundColumnsRight")
    @XmlSchemaType(name = "unsignedInt")
    protected Long unboundColumnsRight;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the queryTableFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTQueryTableFields }
     *     
     */
    public CTQueryTableFields getQueryTableFields() {
        return queryTableFields;
    }

    /**
     * Sets the value of the queryTableFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTQueryTableFields }
     *     
     */
    public void setQueryTableFields(CTQueryTableFields value) {
        this.queryTableFields = value;
    }

    /**
     * Gets the value of the queryTableDeletedFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTQueryTableDeletedFields }
     *     
     */
    public CTQueryTableDeletedFields getQueryTableDeletedFields() {
        return queryTableDeletedFields;
    }

    /**
     * Sets the value of the queryTableDeletedFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTQueryTableDeletedFields }
     *     
     */
    public void setQueryTableDeletedFields(CTQueryTableDeletedFields value) {
        this.queryTableDeletedFields = value;
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
     * Gets the value of the preserveSortFilterLayout property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPreserveSortFilterLayout() {
        if (preserveSortFilterLayout == null) {
            return true;
        } else {
            return preserveSortFilterLayout;
        }
    }

    /**
     * Sets the value of the preserveSortFilterLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreserveSortFilterLayout(Boolean value) {
        this.preserveSortFilterLayout = value;
    }

    /**
     * Gets the value of the fieldIdWrapped property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFieldIdWrapped() {
        if (fieldIdWrapped == null) {
            return false;
        } else {
            return fieldIdWrapped;
        }
    }

    /**
     * Sets the value of the fieldIdWrapped property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFieldIdWrapped(Boolean value) {
        this.fieldIdWrapped = value;
    }

    /**
     * Gets the value of the headersInLastRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHeadersInLastRefresh() {
        if (headersInLastRefresh == null) {
            return true;
        } else {
            return headersInLastRefresh;
        }
    }

    /**
     * Sets the value of the headersInLastRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeadersInLastRefresh(Boolean value) {
        this.headersInLastRefresh = value;
    }

    /**
     * Gets the value of the minimumVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getMinimumVersion() {
        if (minimumVersion == null) {
            return ((short) 0);
        } else {
            return minimumVersion;
        }
    }

    /**
     * Sets the value of the minimumVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMinimumVersion(Short value) {
        this.minimumVersion = value;
    }

    /**
     * Gets the value of the nextId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getNextId() {
        if (nextId == null) {
            return  1L;
        } else {
            return nextId;
        }
    }

    /**
     * Sets the value of the nextId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNextId(Long value) {
        this.nextId = value;
    }

    /**
     * Gets the value of the unboundColumnsLeft property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getUnboundColumnsLeft() {
        if (unboundColumnsLeft == null) {
            return  0L;
        } else {
            return unboundColumnsLeft;
        }
    }

    /**
     * Sets the value of the unboundColumnsLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUnboundColumnsLeft(Long value) {
        this.unboundColumnsLeft = value;
    }

    /**
     * Gets the value of the unboundColumnsRight property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getUnboundColumnsRight() {
        if (unboundColumnsRight == null) {
            return  0L;
        } else {
            return unboundColumnsRight;
        }
    }

    /**
     * Sets the value of the unboundColumnsRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUnboundColumnsRight(Long value) {
        this.unboundColumnsRight = value;
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
