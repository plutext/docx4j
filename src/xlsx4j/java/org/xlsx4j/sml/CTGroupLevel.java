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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GroupLevel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupLevel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="groups" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Groups" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="uniqueName" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="caption" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="user" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="customRollUp" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GroupLevel", propOrder = {
    "groups",
    "extLst"
})
public class CTGroupLevel {

    protected CTGroups groups;
    protected CTExtensionList extLst;
    @XmlAttribute(required = true)
    protected String uniqueName;
    @XmlAttribute(required = true)
    protected String caption;
    @XmlAttribute
    protected Boolean user;
    @XmlAttribute
    protected Boolean customRollUp;

    /**
     * Gets the value of the groups property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroups }
     *     
     */
    public CTGroups getGroups() {
        return groups;
    }

    /**
     * Sets the value of the groups property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroups }
     *     
     */
    public void setGroups(CTGroups value) {
        this.groups = value;
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
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUser() {
        if (user == null) {
            return false;
        } else {
            return user;
        }
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUser(Boolean value) {
        this.user = value;
    }

    /**
     * Gets the value of the customRollUp property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCustomRollUp() {
        if (customRollUp == null) {
            return false;
        } else {
            return customRollUp;
        }
    }

    /**
     * Sets the value of the customRollUp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomRollUp(Boolean value) {
        this.customRollUp = value;
    }

}
