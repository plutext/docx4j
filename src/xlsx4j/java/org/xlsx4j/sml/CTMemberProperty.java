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
 * <p>Java class for CT_MemberProperty complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MemberProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="showCell" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showTip" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showAsCaption" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="nameLen" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="pPos" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="pLen" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="level" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="field" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MemberProperty")
public class CTMemberProperty implements Child
{

    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "showCell")
    protected Boolean showCell;
    @XmlAttribute(name = "showTip")
    protected Boolean showTip;
    @XmlAttribute(name = "showAsCaption")
    protected Boolean showAsCaption;
    @XmlAttribute(name = "nameLen")
    @XmlSchemaType(name = "unsignedInt")
    protected Long nameLen;
    @XmlAttribute(name = "pPos")
    @XmlSchemaType(name = "unsignedInt")
    protected Long pPos;
    @XmlAttribute(name = "pLen")
    @XmlSchemaType(name = "unsignedInt")
    protected Long pLen;
    @XmlAttribute(name = "level")
    @XmlSchemaType(name = "unsignedInt")
    protected Long level;
    @XmlAttribute(name = "field", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long field;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the showCell property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowCell() {
        if (showCell == null) {
            return false;
        } else {
            return showCell;
        }
    }

    /**
     * Sets the value of the showCell property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowCell(Boolean value) {
        this.showCell = value;
    }

    /**
     * Gets the value of the showTip property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowTip() {
        if (showTip == null) {
            return false;
        } else {
            return showTip;
        }
    }

    /**
     * Sets the value of the showTip property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowTip(Boolean value) {
        this.showTip = value;
    }

    /**
     * Gets the value of the showAsCaption property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowAsCaption() {
        if (showAsCaption == null) {
            return false;
        } else {
            return showAsCaption;
        }
    }

    /**
     * Sets the value of the showAsCaption property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowAsCaption(Boolean value) {
        this.showAsCaption = value;
    }

    /**
     * Gets the value of the nameLen property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getNameLen() {
        return nameLen;
    }

    /**
     * Sets the value of the nameLen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNameLen(Long value) {
        this.nameLen = value;
    }

    /**
     * Gets the value of the pPos property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPPos() {
        return pPos;
    }

    /**
     * Sets the value of the pPos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPPos(Long value) {
        this.pPos = value;
    }

    /**
     * Gets the value of the pLen property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPLen() {
        return pLen;
    }

    /**
     * Sets the value of the pLen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPLen(Long value) {
        this.pLen = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLevel() {
        return level;
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
     * Gets the value of the field property.
     * 
     */
    public long getField() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     */
    public void setField(long value) {
        this.field = value;
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
