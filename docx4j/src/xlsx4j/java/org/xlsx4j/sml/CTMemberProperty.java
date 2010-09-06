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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_MemberProperty complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MemberProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
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
public class CTMemberProperty {

    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean showCell;
    @XmlAttribute
    protected Boolean showTip;
    @XmlAttribute
    protected Boolean showAsCaption;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long nameLen;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long pPos;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long pLen;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long level;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long field;

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

}
