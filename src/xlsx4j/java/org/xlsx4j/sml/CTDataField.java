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
 * <p>Java class for CT_DataField complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataField">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="fld" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="subtotal" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataConsolidateFunction" default="sum" />
 *       &lt;attribute name="showDataAs" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_ShowDataAs" default="normal" />
 *       &lt;attribute name="baseField" type="{http://www.w3.org/2001/XMLSchema}int" default="-1" />
 *       &lt;attribute name="baseItem" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1048832" />
 *       &lt;attribute name="numFmtId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_NumFmtId" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataField", propOrder = {
    "extLst"
})
public class CTDataField implements Child
{

    protected CTExtensionList extLst;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "fld", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long fld;
    @XmlAttribute(name = "subtotal")
    protected STDataConsolidateFunction subtotal;
    @XmlAttribute(name = "showDataAs")
    protected STShowDataAs showDataAs;
    @XmlAttribute(name = "baseField")
    protected Integer baseField;
    @XmlAttribute(name = "baseItem")
    @XmlSchemaType(name = "unsignedInt")
    protected Long baseItem;
    @XmlAttribute(name = "numFmtId")
    protected Long numFmtId;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the fld property.
     * 
     */
    public long getFld() {
        return fld;
    }

    /**
     * Sets the value of the fld property.
     * 
     */
    public void setFld(long value) {
        this.fld = value;
    }

    /**
     * Gets the value of the subtotal property.
     * 
     * @return
     *     possible object is
     *     {@link STDataConsolidateFunction }
     *     
     */
    public STDataConsolidateFunction getSubtotal() {
        if (subtotal == null) {
            return STDataConsolidateFunction.SUM;
        } else {
            return subtotal;
        }
    }

    /**
     * Sets the value of the subtotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataConsolidateFunction }
     *     
     */
    public void setSubtotal(STDataConsolidateFunction value) {
        this.subtotal = value;
    }

    /**
     * Gets the value of the showDataAs property.
     * 
     * @return
     *     possible object is
     *     {@link STShowDataAs }
     *     
     */
    public STShowDataAs getShowDataAs() {
        if (showDataAs == null) {
            return STShowDataAs.NORMAL;
        } else {
            return showDataAs;
        }
    }

    /**
     * Sets the value of the showDataAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link STShowDataAs }
     *     
     */
    public void setShowDataAs(STShowDataAs value) {
        this.showDataAs = value;
    }

    /**
     * Gets the value of the baseField property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getBaseField() {
        if (baseField == null) {
            return -1;
        } else {
            return baseField;
        }
    }

    /**
     * Sets the value of the baseField property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBaseField(Integer value) {
        this.baseField = value;
    }

    /**
     * Gets the value of the baseItem property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getBaseItem() {
        if (baseItem == null) {
            return  1048832L;
        } else {
            return baseItem;
        }
    }

    /**
     * Sets the value of the baseItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBaseItem(Long value) {
        this.baseItem = value;
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
