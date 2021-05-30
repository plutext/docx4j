/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.xmlPackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Defines a part of a package using open package
 * 				conventions. A part with contentType containing xml
 * 				requires xmlData. All other contentType values use
 * 				binaryData.
 * 			
 * 
 * <p>Java class for CT_Part complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Part">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="xmlData" type="{http://schemas.microsoft.com/office/2006/xmlPackage}CT_XmlData"/>
 *         &lt;element name="binaryData" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/choice>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="contentType">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="text/xml"/>
 *             &lt;pattern value="application/xml"/>
 *             &lt;pattern value="application/.+\+xml"/>
 *             &lt;pattern value=".+"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="compression" default="deflateSuperFast">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="store"/>
 *             &lt;enumeration value="deflateSuperFast"/>
 *             &lt;enumeration value="deflateFast"/>
 *             &lt;enumeration value="deflateNormal"/>
 *             &lt;enumeration value="deflateMaximum"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="padding" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Part", propOrder = {
    "xmlData",
    "binaryData"
})
@XmlRootElement(name = "part")
public class Part {

    protected XmlData xmlData;
    protected byte[] binaryData;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage", required = true)
    protected String name;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage")
    protected String contentType;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage")
    protected String compression;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage")
    @XmlSchemaType(name = "unsignedInt")
    protected Long padding;

    /**
     * Gets the value of the xmlData property.
     * 
     * @return
     *     possible object is
     *     {@link XmlData }
     *     
     */
    public XmlData getXmlData() {
        return xmlData;
    }

    /**
     * Sets the value of the xmlData property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlData }
     *     
     */
    public void setXmlData(XmlData value) {
        this.xmlData = value;
    }

    /**
     * Gets the value of the binaryData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBinaryData() {
        return binaryData;
    }

    /**
     * Sets the value of the binaryData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBinaryData(byte[] value) {
        this.binaryData = ((byte[]) value);
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
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the compression property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompression() {
        if (compression == null) {
            return "deflateSuperFast";
        } else {
            return compression;
        }
    }

    /**
     * Sets the value of the compression property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompression(String value) {
        this.compression = value;
    }

    /**
     * Gets the value of the padding property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPadding() {
        if (padding == null) {
            return  0L;
        } else {
            return padding;
        }
    }

    /**
     * Sets the value of the padding property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPadding(Long value) {
        this.padding = value;
    }

}
