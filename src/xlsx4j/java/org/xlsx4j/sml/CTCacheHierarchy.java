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
 * <p>Java class for CT_CacheHierarchy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CacheHierarchy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fieldsUsage" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FieldsUsage" minOccurs="0"/>
 *         &lt;element name="groupLevels" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_GroupLevels" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="uniqueName" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="caption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="measure" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="set" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="parentSet" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="iconSet" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="attribute" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="time" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="keyAttribute" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="defaultMemberUniqueName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="allUniqueName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="allCaption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="dimensionUniqueName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="displayFolder" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="measureGroup" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="measures" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="oneField" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="memberValueDatatype" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="unbalanced" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="unbalancedGroup" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CacheHierarchy", propOrder = {
    "fieldsUsage",
    "groupLevels",
    "extLst"
})
public class CTCacheHierarchy implements Child
{

    protected CTFieldsUsage fieldsUsage;
    protected CTGroupLevels groupLevels;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "uniqueName", required = true)
    protected String uniqueName;
    @XmlAttribute(name = "caption")
    protected String caption;
    @XmlAttribute(name = "measure")
    protected Boolean measure;
    @XmlAttribute(name = "set")
    protected Boolean set;
    @XmlAttribute(name = "parentSet")
    @XmlSchemaType(name = "unsignedInt")
    protected Long parentSet;
    @XmlAttribute(name = "iconSet")
    protected Integer iconSet;
    @XmlAttribute(name = "attribute")
    protected Boolean attribute;
    @XmlAttribute(name = "time")
    protected Boolean time;
    @XmlAttribute(name = "keyAttribute")
    protected Boolean keyAttribute;
    @XmlAttribute(name = "defaultMemberUniqueName")
    protected String defaultMemberUniqueName;
    @XmlAttribute(name = "allUniqueName")
    protected String allUniqueName;
    @XmlAttribute(name = "allCaption")
    protected String allCaption;
    @XmlAttribute(name = "dimensionUniqueName")
    protected String dimensionUniqueName;
    @XmlAttribute(name = "displayFolder")
    protected String displayFolder;
    @XmlAttribute(name = "measureGroup")
    protected String measureGroup;
    @XmlAttribute(name = "measures")
    protected Boolean measures;
    @XmlAttribute(name = "count", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long count;
    @XmlAttribute(name = "oneField")
    protected Boolean oneField;
    @XmlAttribute(name = "memberValueDatatype")
    @XmlSchemaType(name = "unsignedShort")
    protected Integer memberValueDatatype;
    @XmlAttribute(name = "unbalanced")
    protected Boolean unbalanced;
    @XmlAttribute(name = "unbalancedGroup")
    protected Boolean unbalancedGroup;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fieldsUsage property.
     * 
     * @return
     *     possible object is
     *     {@link CTFieldsUsage }
     *     
     */
    public CTFieldsUsage getFieldsUsage() {
        return fieldsUsage;
    }

    /**
     * Sets the value of the fieldsUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFieldsUsage }
     *     
     */
    public void setFieldsUsage(CTFieldsUsage value) {
        this.fieldsUsage = value;
    }

    /**
     * Gets the value of the groupLevels property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupLevels }
     *     
     */
    public CTGroupLevels getGroupLevels() {
        return groupLevels;
    }

    /**
     * Sets the value of the groupLevels property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupLevels }
     *     
     */
    public void setGroupLevels(CTGroupLevels value) {
        this.groupLevels = value;
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
     * Gets the value of the uniqueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * Sets the value of the uniqueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueName(String value) {
        this.uniqueName = value;
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
     * Gets the value of the measure property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMeasure() {
        if (measure == null) {
            return false;
        } else {
            return measure;
        }
    }

    /**
     * Sets the value of the measure property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMeasure(Boolean value) {
        this.measure = value;
    }

    /**
     * Gets the value of the set property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSet() {
        if (set == null) {
            return false;
        } else {
            return set;
        }
    }

    /**
     * Sets the value of the set property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSet(Boolean value) {
        this.set = value;
    }

    /**
     * Gets the value of the parentSet property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getParentSet() {
        return parentSet;
    }

    /**
     * Sets the value of the parentSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setParentSet(Long value) {
        this.parentSet = value;
    }

    /**
     * Gets the value of the iconSet property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getIconSet() {
        if (iconSet == null) {
            return  0;
        } else {
            return iconSet;
        }
    }

    /**
     * Sets the value of the iconSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIconSet(Integer value) {
        this.iconSet = value;
    }

    /**
     * Gets the value of the attribute property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAttribute() {
        if (attribute == null) {
            return false;
        } else {
            return attribute;
        }
    }

    /**
     * Sets the value of the attribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAttribute(Boolean value) {
        this.attribute = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTime() {
        if (time == null) {
            return false;
        } else {
            return time;
        }
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTime(Boolean value) {
        this.time = value;
    }

    /**
     * Gets the value of the keyAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isKeyAttribute() {
        if (keyAttribute == null) {
            return false;
        } else {
            return keyAttribute;
        }
    }

    /**
     * Sets the value of the keyAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKeyAttribute(Boolean value) {
        this.keyAttribute = value;
    }

    /**
     * Gets the value of the defaultMemberUniqueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultMemberUniqueName() {
        return defaultMemberUniqueName;
    }

    /**
     * Sets the value of the defaultMemberUniqueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultMemberUniqueName(String value) {
        this.defaultMemberUniqueName = value;
    }

    /**
     * Gets the value of the allUniqueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllUniqueName() {
        return allUniqueName;
    }

    /**
     * Sets the value of the allUniqueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllUniqueName(String value) {
        this.allUniqueName = value;
    }

    /**
     * Gets the value of the allCaption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllCaption() {
        return allCaption;
    }

    /**
     * Sets the value of the allCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllCaption(String value) {
        this.allCaption = value;
    }

    /**
     * Gets the value of the dimensionUniqueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDimensionUniqueName() {
        return dimensionUniqueName;
    }

    /**
     * Sets the value of the dimensionUniqueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDimensionUniqueName(String value) {
        this.dimensionUniqueName = value;
    }

    /**
     * Gets the value of the displayFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayFolder() {
        return displayFolder;
    }

    /**
     * Sets the value of the displayFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayFolder(String value) {
        this.displayFolder = value;
    }

    /**
     * Gets the value of the measureGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMeasureGroup() {
        return measureGroup;
    }

    /**
     * Sets the value of the measureGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMeasureGroup(String value) {
        this.measureGroup = value;
    }

    /**
     * Gets the value of the measures property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMeasures() {
        if (measures == null) {
            return false;
        } else {
            return measures;
        }
    }

    /**
     * Sets the value of the measures property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMeasures(Boolean value) {
        this.measures = value;
    }

    /**
     * Gets the value of the count property.
     * 
     */
    public long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     */
    public void setCount(long value) {
        this.count = value;
    }

    /**
     * Gets the value of the oneField property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOneField() {
        if (oneField == null) {
            return false;
        } else {
            return oneField;
        }
    }

    /**
     * Sets the value of the oneField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOneField(Boolean value) {
        this.oneField = value;
    }

    /**
     * Gets the value of the memberValueDatatype property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMemberValueDatatype() {
        return memberValueDatatype;
    }

    /**
     * Sets the value of the memberValueDatatype property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMemberValueDatatype(Integer value) {
        this.memberValueDatatype = value;
    }

    /**
     * Gets the value of the unbalanced property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUnbalanced() {
        return unbalanced;
    }

    /**
     * Sets the value of the unbalanced property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnbalanced(Boolean value) {
        this.unbalanced = value;
    }

    /**
     * Gets the value of the unbalancedGroup property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUnbalancedGroup() {
        return unbalancedGroup;
    }

    /**
     * Sets the value of the unbalancedGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnbalancedGroup(Boolean value) {
        this.unbalancedGroup = value;
    }

    /**
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
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
