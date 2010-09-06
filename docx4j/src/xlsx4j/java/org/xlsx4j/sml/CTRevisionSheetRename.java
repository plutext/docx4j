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
 * <p>Java class for CT_RevisionSheetRename complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionSheetRename">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_RevData"/>
 *       &lt;attribute name="sheetId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="oldName" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="newName" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionSheetRename", propOrder = {
    "extLst"
})
public class CTRevisionSheetRename {

    protected CTExtensionList extLst;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sheetId;
    @XmlAttribute(required = true)
    protected String oldName;
    @XmlAttribute(required = true)
    protected String newName;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long rId;
    @XmlAttribute
    protected Boolean ua;
    @XmlAttribute
    protected Boolean ra;

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
     * Gets the value of the sheetId property.
     * 
     */
    public long getSheetId() {
        return sheetId;
    }

    /**
     * Sets the value of the sheetId property.
     * 
     */
    public void setSheetId(long value) {
        this.sheetId = value;
    }

    /**
     * Gets the value of the oldName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Sets the value of the oldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldName(String value) {
        this.oldName = value;
    }

    /**
     * Gets the value of the newName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Sets the value of the newName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewName(String value) {
        this.newName = value;
    }

    /**
     * Gets the value of the rId property.
     * 
     */
    public long getRId() {
        return rId;
    }

    /**
     * Sets the value of the rId property.
     * 
     */
    public void setRId(long value) {
        this.rId = value;
    }

    /**
     * Gets the value of the ua property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUa() {
        if (ua == null) {
            return false;
        } else {
            return ua;
        }
    }

    /**
     * Sets the value of the ua property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUa(Boolean value) {
        this.ua = value;
    }

    /**
     * Gets the value of the ra property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRa() {
        if (ra == null) {
            return false;
        } else {
            return ra;
        }
    }

    /**
     * Sets the value of the ra property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRa(Boolean value) {
        this.ra = value;
    }

}
