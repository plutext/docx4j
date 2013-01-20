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
 * <p>Java class for CT_ChartsheetProtection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ChartsheetProtection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="password" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedShortHex" />
 *       &lt;attribute name="algorithmName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="hashValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="saltValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="spinCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="content" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="objects" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ChartsheetProtection")
public class CTChartsheetProtection implements Child
{

    @XmlAttribute(name = "password")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] password;
    @XmlAttribute(name = "algorithmName")
    protected String algorithmName;
    @XmlAttribute(name = "hashValue")
    protected byte[] hashValue;
    @XmlAttribute(name = "saltValue")
    protected byte[] saltValue;
    @XmlAttribute(name = "spinCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long spinCount;
    @XmlAttribute(name = "content")
    protected Boolean content;
    @XmlAttribute(name = "objects")
    protected Boolean objects;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(byte[] value) {
        this.password = value;
    }

    /**
     * Gets the value of the algorithmName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Sets the value of the algorithmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlgorithmName(String value) {
        this.algorithmName = value;
    }

    /**
     * Gets the value of the hashValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getHashValue() {
        return hashValue;
    }

    /**
     * Sets the value of the hashValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setHashValue(byte[] value) {
        this.hashValue = value;
    }

    /**
     * Gets the value of the saltValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSaltValue() {
        return saltValue;
    }

    /**
     * Sets the value of the saltValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSaltValue(byte[] value) {
        this.saltValue = value;
    }

    /**
     * Gets the value of the spinCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSpinCount() {
        return spinCount;
    }

    /**
     * Sets the value of the spinCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSpinCount(Long value) {
        this.spinCount = value;
    }

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContent() {
        if (content == null) {
            return false;
        } else {
            return content;
        }
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContent(Boolean value) {
        this.content = value;
    }

    /**
     * Gets the value of the objects property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isObjects() {
        if (objects == null) {
            return false;
        } else {
            return objects;
        }
    }

    /**
     * Sets the value of the objects property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setObjects(Boolean value) {
        this.objects = value;
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
