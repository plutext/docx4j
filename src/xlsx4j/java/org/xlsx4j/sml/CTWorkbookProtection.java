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
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


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
 *       &lt;attribute name="workbookPasswordCharacterSet" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="revisionsPassword" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedShortHex" />
 *       &lt;attribute name="revisionsPasswordCharacterSet" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lockStructure" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="lockWindows" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="lockRevision" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="revisionsAlgorithmName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="revisionsHashValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="revisionsSaltValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="revisionsSpinCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="workbookAlgorithmName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="workbookHashValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="workbookSaltValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="workbookSpinCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WorkbookProtection")
public class CTWorkbookProtection implements Child
{

    @XmlAttribute(name = "workbookPassword")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] workbookPassword;
    @XmlAttribute(name = "workbookPasswordCharacterSet")
    protected String workbookPasswordCharacterSet;
    @XmlAttribute(name = "revisionsPassword")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] revisionsPassword;
    @XmlAttribute(name = "revisionsPasswordCharacterSet")
    protected String revisionsPasswordCharacterSet;
    @XmlAttribute(name = "lockStructure")
    protected Boolean lockStructure;
    @XmlAttribute(name = "lockWindows")
    protected Boolean lockWindows;
    @XmlAttribute(name = "lockRevision")
    protected Boolean lockRevision;
    @XmlAttribute(name = "revisionsAlgorithmName")
    protected String revisionsAlgorithmName;
    @XmlAttribute(name = "revisionsHashValue")
    protected byte[] revisionsHashValue;
    @XmlAttribute(name = "revisionsSaltValue")
    protected byte[] revisionsSaltValue;
    @XmlAttribute(name = "revisionsSpinCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long revisionsSpinCount;
    @XmlAttribute(name = "workbookAlgorithmName")
    protected String workbookAlgorithmName;
    @XmlAttribute(name = "workbookHashValue")
    protected byte[] workbookHashValue;
    @XmlAttribute(name = "workbookSaltValue")
    protected byte[] workbookSaltValue;
    @XmlAttribute(name = "workbookSpinCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long workbookSpinCount;
    @XmlTransient
    private Object parent;

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
        this.workbookPassword = value;
    }

    /**
     * Gets the value of the workbookPasswordCharacterSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkbookPasswordCharacterSet() {
        return workbookPasswordCharacterSet;
    }

    /**
     * Sets the value of the workbookPasswordCharacterSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkbookPasswordCharacterSet(String value) {
        this.workbookPasswordCharacterSet = value;
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
        this.revisionsPassword = value;
    }

    /**
     * Gets the value of the revisionsPasswordCharacterSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevisionsPasswordCharacterSet() {
        return revisionsPasswordCharacterSet;
    }

    /**
     * Sets the value of the revisionsPasswordCharacterSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisionsPasswordCharacterSet(String value) {
        this.revisionsPasswordCharacterSet = value;
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

    /**
     * Gets the value of the revisionsAlgorithmName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevisionsAlgorithmName() {
        return revisionsAlgorithmName;
    }

    /**
     * Sets the value of the revisionsAlgorithmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisionsAlgorithmName(String value) {
        this.revisionsAlgorithmName = value;
    }

    /**
     * Gets the value of the revisionsHashValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getRevisionsHashValue() {
        return revisionsHashValue;
    }

    /**
     * Sets the value of the revisionsHashValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setRevisionsHashValue(byte[] value) {
        this.revisionsHashValue = value;
    }

    /**
     * Gets the value of the revisionsSaltValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getRevisionsSaltValue() {
        return revisionsSaltValue;
    }

    /**
     * Sets the value of the revisionsSaltValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setRevisionsSaltValue(byte[] value) {
        this.revisionsSaltValue = value;
    }

    /**
     * Gets the value of the revisionsSpinCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRevisionsSpinCount() {
        return revisionsSpinCount;
    }

    /**
     * Sets the value of the revisionsSpinCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRevisionsSpinCount(Long value) {
        this.revisionsSpinCount = value;
    }

    /**
     * Gets the value of the workbookAlgorithmName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkbookAlgorithmName() {
        return workbookAlgorithmName;
    }

    /**
     * Sets the value of the workbookAlgorithmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkbookAlgorithmName(String value) {
        this.workbookAlgorithmName = value;
    }

    /**
     * Gets the value of the workbookHashValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getWorkbookHashValue() {
        return workbookHashValue;
    }

    /**
     * Sets the value of the workbookHashValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setWorkbookHashValue(byte[] value) {
        this.workbookHashValue = value;
    }

    /**
     * Gets the value of the workbookSaltValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getWorkbookSaltValue() {
        return workbookSaltValue;
    }

    /**
     * Sets the value of the workbookSaltValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setWorkbookSaltValue(byte[] value) {
        this.workbookSaltValue = value;
    }

    /**
     * Gets the value of the workbookSpinCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getWorkbookSpinCount() {
        return workbookSpinCount;
    }

    /**
     * Sets the value of the workbookSpinCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setWorkbookSpinCount(Long value) {
        this.workbookSpinCount = value;
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
