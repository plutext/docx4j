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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CacheField complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CacheField">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sharedItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SharedItems" minOccurs="0"/>
 *         &lt;element name="fieldGroup" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FieldGroup" minOccurs="0"/>
 *         &lt;element name="mpMap" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_X" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="caption" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="propertyName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="serverField" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="uniqueList" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="numFmtId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_NumFmtId" />
 *       &lt;attribute name="formula" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="sqlType" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="hierarchy" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="level" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="databaseField" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="mappingCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="memberPropertyField" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CacheField", propOrder = {
    "sharedItems",
    "fieldGroup",
    "mpMap",
    "extLst"
})
public class CTCacheField implements Child
{

    protected CTSharedItems sharedItems;
    protected CTFieldGroup fieldGroup;
    protected List<CTX> mpMap;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "caption")
    protected String caption;
    @XmlAttribute(name = "propertyName")
    protected String propertyName;
    @XmlAttribute(name = "serverField")
    protected Boolean serverField;
    @XmlAttribute(name = "uniqueList")
    protected Boolean uniqueList;
    @XmlAttribute(name = "numFmtId")
    protected Long numFmtId;
    @XmlAttribute(name = "formula")
    protected String formula;
    @XmlAttribute(name = "sqlType")
    protected Integer sqlType;
    @XmlAttribute(name = "hierarchy")
    protected Integer hierarchy;
    @XmlAttribute(name = "level")
    @XmlSchemaType(name = "unsignedInt")
    protected Long level;
    @XmlAttribute(name = "databaseField")
    protected Boolean databaseField;
    @XmlAttribute(name = "mappingCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long mappingCount;
    @XmlAttribute(name = "memberPropertyField")
    protected Boolean memberPropertyField;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sharedItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTSharedItems }
     *     
     */
    public CTSharedItems getSharedItems() {
        return sharedItems;
    }

    /**
     * Sets the value of the sharedItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSharedItems }
     *     
     */
    public void setSharedItems(CTSharedItems value) {
        this.sharedItems = value;
    }

    /**
     * Gets the value of the fieldGroup property.
     * 
     * @return
     *     possible object is
     *     {@link CTFieldGroup }
     *     
     */
    public CTFieldGroup getFieldGroup() {
        return fieldGroup;
    }

    /**
     * Sets the value of the fieldGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFieldGroup }
     *     
     */
    public void setFieldGroup(CTFieldGroup value) {
        this.fieldGroup = value;
    }

    /**
     * Gets the value of the mpMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mpMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMpMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTX }
     * 
     * 
     */
    public List<CTX> getMpMap() {
        if (mpMap == null) {
            mpMap = new ArrayList<CTX>();
        }
        return this.mpMap;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Gets the value of the propertyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
    }

    /**
     * Gets the value of the serverField property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isServerField() {
        if (serverField == null) {
            return false;
        } else {
            return serverField;
        }
    }

    /**
     * Sets the value of the serverField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setServerField(Boolean value) {
        this.serverField = value;
    }

    /**
     * Gets the value of the uniqueList property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUniqueList() {
        if (uniqueList == null) {
            return true;
        } else {
            return uniqueList;
        }
    }

    /**
     * Sets the value of the uniqueList property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUniqueList(Boolean value) {
        this.uniqueList = value;
    }

    /**
     * Gets the value of the numFmtId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getNumFmtId() {
        return numFmtId;
    }

    /**
     * Sets the value of the numFmtId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNumFmtId(Long value) {
        this.numFmtId = value;
    }

    /**
     * Gets the value of the formula property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormula() {
        return formula;
    }

    /**
     * Sets the value of the formula property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormula(String value) {
        this.formula = value;
    }

    /**
     * Gets the value of the sqlType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSqlType() {
        if (sqlType == null) {
            return  0;
        } else {
            return sqlType;
        }
    }

    /**
     * Sets the value of the sqlType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSqlType(Integer value) {
        this.sqlType = value;
    }

    /**
     * Gets the value of the hierarchy property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getHierarchy() {
        if (hierarchy == null) {
            return  0;
        } else {
            return hierarchy;
        }
    }

    /**
     * Sets the value of the hierarchy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHierarchy(Integer value) {
        this.hierarchy = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getLevel() {
        if (level == null) {
            return  0L;
        } else {
            return level;
        }
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLevel(Long value) {
        this.level = value;
    }

    /**
     * Gets the value of the databaseField property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDatabaseField() {
        if (databaseField == null) {
            return true;
        } else {
            return databaseField;
        }
    }

    /**
     * Sets the value of the databaseField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDatabaseField(Boolean value) {
        this.databaseField = value;
    }

    /**
     * Gets the value of the mappingCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMappingCount() {
        return mappingCount;
    }

    /**
     * Sets the value of the mappingCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMappingCount(Long value) {
        this.mappingCount = value;
    }

    /**
     * Gets the value of the memberPropertyField property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMemberPropertyField() {
        if (memberPropertyField == null) {
            return false;
        } else {
            return memberPropertyField;
        }
    }

    /**
     * Sets the value of the memberPropertyField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMemberPropertyField(Boolean value) {
        this.memberPropertyField = value;
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
