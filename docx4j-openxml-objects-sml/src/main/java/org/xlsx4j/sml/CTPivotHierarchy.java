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
 * <p>Java class for CT_PivotHierarchy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotHierarchy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mps" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MemberProperties" minOccurs="0"/>
 *         &lt;element name="members" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Members" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="outline" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="multipleItemSelectionAllowed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="subtotalTop" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showInFieldList" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToPage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="dragToData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dragOff" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="includeNewItemsInFilter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="caption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotHierarchy", propOrder = {
    "mps",
    "members",
    "extLst"
})
public class CTPivotHierarchy implements Child
{

    protected CTMemberProperties mps;
    protected List<CTMembers> members;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "outline")
    protected Boolean outline;
    @XmlAttribute(name = "multipleItemSelectionAllowed")
    protected Boolean multipleItemSelectionAllowed;
    @XmlAttribute(name = "subtotalTop")
    protected Boolean subtotalTop;
    @XmlAttribute(name = "showInFieldList")
    protected Boolean showInFieldList;
    @XmlAttribute(name = "dragToRow")
    protected Boolean dragToRow;
    @XmlAttribute(name = "dragToCol")
    protected Boolean dragToCol;
    @XmlAttribute(name = "dragToPage")
    protected Boolean dragToPage;
    @XmlAttribute(name = "dragToData")
    protected Boolean dragToData;
    @XmlAttribute(name = "dragOff")
    protected Boolean dragOff;
    @XmlAttribute(name = "includeNewItemsInFilter")
    protected Boolean includeNewItemsInFilter;
    @XmlAttribute(name = "caption")
    protected String caption;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the mps property.
     * 
     * @return
     *     possible object is
     *     {@link CTMemberProperties }
     *     
     */
    public CTMemberProperties getMps() {
        return mps;
    }

    /**
     * Sets the value of the mps property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMemberProperties }
     *     
     */
    public void setMps(CTMemberProperties value) {
        this.mps = value;
    }

    /**
     * Gets the value of the members property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the members property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMembers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTMembers }
     * 
     * 
     */
    public List<CTMembers> getMembers() {
        if (members == null) {
            members = new ArrayList<CTMembers>();
        }
        return this.members;
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
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOutline() {
        if (outline == null) {
            return false;
        } else {
            return outline;
        }
    }

    /**
     * Sets the value of the outline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutline(Boolean value) {
        this.outline = value;
    }

    /**
     * Gets the value of the multipleItemSelectionAllowed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMultipleItemSelectionAllowed() {
        if (multipleItemSelectionAllowed == null) {
            return false;
        } else {
            return multipleItemSelectionAllowed;
        }
    }

    /**
     * Sets the value of the multipleItemSelectionAllowed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMultipleItemSelectionAllowed(Boolean value) {
        this.multipleItemSelectionAllowed = value;
    }

    /**
     * Gets the value of the subtotalTop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSubtotalTop() {
        if (subtotalTop == null) {
            return false;
        } else {
            return subtotalTop;
        }
    }

    /**
     * Sets the value of the subtotalTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSubtotalTop(Boolean value) {
        this.subtotalTop = value;
    }

    /**
     * Gets the value of the showInFieldList property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowInFieldList() {
        if (showInFieldList == null) {
            return true;
        } else {
            return showInFieldList;
        }
    }

    /**
     * Sets the value of the showInFieldList property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowInFieldList(Boolean value) {
        this.showInFieldList = value;
    }

    /**
     * Gets the value of the dragToRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToRow() {
        if (dragToRow == null) {
            return true;
        } else {
            return dragToRow;
        }
    }

    /**
     * Sets the value of the dragToRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToRow(Boolean value) {
        this.dragToRow = value;
    }

    /**
     * Gets the value of the dragToCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToCol() {
        if (dragToCol == null) {
            return true;
        } else {
            return dragToCol;
        }
    }

    /**
     * Sets the value of the dragToCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToCol(Boolean value) {
        this.dragToCol = value;
    }

    /**
     * Gets the value of the dragToPage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToPage() {
        if (dragToPage == null) {
            return true;
        } else {
            return dragToPage;
        }
    }

    /**
     * Sets the value of the dragToPage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToPage(Boolean value) {
        this.dragToPage = value;
    }

    /**
     * Gets the value of the dragToData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragToData() {
        if (dragToData == null) {
            return false;
        } else {
            return dragToData;
        }
    }

    /**
     * Sets the value of the dragToData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragToData(Boolean value) {
        this.dragToData = value;
    }

    /**
     * Gets the value of the dragOff property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDragOff() {
        if (dragOff == null) {
            return true;
        } else {
            return dragOff;
        }
    }

    /**
     * Sets the value of the dragOff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDragOff(Boolean value) {
        this.dragOff = value;
    }

    /**
     * Gets the value of the includeNewItemsInFilter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludeNewItemsInFilter() {
        if (includeNewItemsInFilter == null) {
            return false;
        } else {
            return includeNewItemsInFilter;
        }
    }

    /**
     * Sets the value of the includeNewItemsInFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeNewItemsInFilter(Boolean value) {
        this.includeNewItemsInFilter = value;
    }

    /**
     * Gets the value of the caption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the value of the caption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaption(String value) {
        this.caption = value;
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
