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
 * <p>Java class for CT_PivotArea complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotArea">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="references" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotAreaReferences" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="field" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PivotAreaType" default="normal" />
 *       &lt;attribute name="dataOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="labelOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="grandRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="grandCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="cacheIndex" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="outline" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="offset" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="collapsedLevelsAreSubtotals" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="axis" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Axis" />
 *       &lt;attribute name="fieldPosition" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotArea", propOrder = {
    "references",
    "extLst"
})
public class CTPivotArea implements Child
{

    protected CTPivotAreaReferences references;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "field")
    protected Integer field;
    @XmlAttribute(name = "type")
    protected STPivotAreaType type;
    @XmlAttribute(name = "dataOnly")
    protected Boolean dataOnly;
    @XmlAttribute(name = "labelOnly")
    protected Boolean labelOnly;
    @XmlAttribute(name = "grandRow")
    protected Boolean grandRow;
    @XmlAttribute(name = "grandCol")
    protected Boolean grandCol;
    @XmlAttribute(name = "cacheIndex")
    protected Boolean cacheIndex;
    @XmlAttribute(name = "outline")
    protected Boolean outline;
    @XmlAttribute(name = "offset")
    protected String offset;
    @XmlAttribute(name = "collapsedLevelsAreSubtotals")
    protected Boolean collapsedLevelsAreSubtotals;
    @XmlAttribute(name = "axis")
    protected STAxis axis;
    @XmlAttribute(name = "fieldPosition")
    @XmlSchemaType(name = "unsignedInt")
    protected Long fieldPosition;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the references property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotAreaReferences }
     *     
     */
    public CTPivotAreaReferences getReferences() {
        return references;
    }

    /**
     * Sets the value of the references property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotAreaReferences }
     *     
     */
    public void setReferences(CTPivotAreaReferences value) {
        this.references = value;
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
     * Gets the value of the field property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getField() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setField(Integer value) {
        this.field = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STPivotAreaType }
     *     
     */
    public STPivotAreaType getType() {
        if (type == null) {
            return STPivotAreaType.NORMAL;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPivotAreaType }
     *     
     */
    public void setType(STPivotAreaType value) {
        this.type = value;
    }

    /**
     * Gets the value of the dataOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDataOnly() {
        if (dataOnly == null) {
            return true;
        } else {
            return dataOnly;
        }
    }

    /**
     * Sets the value of the dataOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDataOnly(Boolean value) {
        this.dataOnly = value;
    }

    /**
     * Gets the value of the labelOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLabelOnly() {
        if (labelOnly == null) {
            return false;
        } else {
            return labelOnly;
        }
    }

    /**
     * Sets the value of the labelOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLabelOnly(Boolean value) {
        this.labelOnly = value;
    }

    /**
     * Gets the value of the grandRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGrandRow() {
        if (grandRow == null) {
            return false;
        } else {
            return grandRow;
        }
    }

    /**
     * Sets the value of the grandRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGrandRow(Boolean value) {
        this.grandRow = value;
    }

    /**
     * Gets the value of the grandCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGrandCol() {
        if (grandCol == null) {
            return false;
        } else {
            return grandCol;
        }
    }

    /**
     * Sets the value of the grandCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGrandCol(Boolean value) {
        this.grandCol = value;
    }

    /**
     * Gets the value of the cacheIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCacheIndex() {
        if (cacheIndex == null) {
            return false;
        } else {
            return cacheIndex;
        }
    }

    /**
     * Sets the value of the cacheIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCacheIndex(Boolean value) {
        this.cacheIndex = value;
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
            return true;
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
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOffset(String value) {
        this.offset = value;
    }

    /**
     * Gets the value of the collapsedLevelsAreSubtotals property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCollapsedLevelsAreSubtotals() {
        if (collapsedLevelsAreSubtotals == null) {
            return false;
        } else {
            return collapsedLevelsAreSubtotals;
        }
    }

    /**
     * Sets the value of the collapsedLevelsAreSubtotals property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCollapsedLevelsAreSubtotals(Boolean value) {
        this.collapsedLevelsAreSubtotals = value;
    }

    /**
     * Gets the value of the axis property.
     * 
     * @return
     *     possible object is
     *     {@link STAxis }
     *     
     */
    public STAxis getAxis() {
        return axis;
    }

    /**
     * Sets the value of the axis property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAxis }
     *     
     */
    public void setAxis(STAxis value) {
        this.axis = value;
    }

    /**
     * Gets the value of the fieldPosition property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFieldPosition() {
        return fieldPosition;
    }

    /**
     * Sets the value of the fieldPosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFieldPosition(Long value) {
        this.fieldPosition = value;
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
