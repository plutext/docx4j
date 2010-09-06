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
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_WorkbookProtection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WorkbookProtection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="workbookPassword" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedShortHex" />
 *       &lt;attribute name="revisionsPassword" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedShortHex" />
 *       &lt;attribute name="lockStructure" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="lockWindows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="lockRevision" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WorkbookProtection")
public class CTWorkbookProtection {

    @XmlAttribute
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] workbookPassword;
    @XmlAttribute
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] revisionsPassword;
    @XmlAttribute
    protected Boolean lockStructure;
    @XmlAttribute
    protected Boolean lockWindows;
    @XmlAttribute
    protected Boolean lockRevision;

    /**
     * Gets the value of the workbookPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getWorkbookPassword() {
        return workbookPassword;
    }

    /**
     * Sets the value of the workbookPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkbookPassword(byte[] value) {
        this.workbookPassword = ((byte[]) value);
    }

    /**
     * Gets the value of the revisionsPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getRevisionsPassword() {
        return revisionsPassword;
    }

    /**
     * Sets the value of the revisionsPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisionsPassword(byte[] value) {
        this.revisionsPassword = ((byte[]) value);
    }

    /**
     * Gets the value of the lockStructure property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLockStructure() {
        if (lockStructure == null) {
            return false;
        } else {
            return lockStructure;
        }
    }

    /**
     * Sets the value of the lockStructure property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLockStructure(Boolean value) {
        this.lockStructure = value;
    }

    /**
     * Gets the value of the lockWindows property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLockWindows() {
        if (lockWindows == null) {
            return false;
        } else {
            return lockWindows;
        }
    }

    /**
     * Sets the value of the lockWindows property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLockWindows(Boolean value) {
        this.lockWindows = value;
    }

    /**
     * Gets the value of the lockRevision property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLockRevision() {
        if (lockRevision == null) {
            return false;
        } else {
            return lockRevision;
        }
    }

    /**
     * Sets the value of the lockRevision property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLockRevision(Boolean value) {
        this.lockRevision = value;
    }

}
