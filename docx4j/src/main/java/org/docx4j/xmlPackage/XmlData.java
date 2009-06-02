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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * 
 * 				Specifies the details of an xml part.
 * 			
 * 
 * <p>Java class for CT_XmlData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_XmlData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="originalXmlEncoding" type="{http://www.w3.org/2001/XMLSchema}string" default="UTF-8" />
 *       &lt;attribute name="originalXmlVersion" type="{http://www.w3.org/2001/XMLSchema}string" default="1.0" />
 *       &lt;attribute name="originalXmlStandalone" type="{http://www.w3.org/2001/XMLSchema}string" default="yes" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_XmlData", propOrder = {
    "any"
})
@XmlRootElement(name = "xmlData")
public class XmlData {

    @XmlAnyElement
    protected Element any;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage")
    protected String originalXmlEncoding;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage")
    protected String originalXmlVersion;
    @XmlAttribute(namespace = "http://schemas.microsoft.com/office/2006/xmlPackage")
    protected String originalXmlStandalone;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     
     */
    public Element getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     
     */
    public void setAny(Element value) {
        this.any = value;
    }

    /**
     * Gets the value of the originalXmlEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalXmlEncoding() {
        if (originalXmlEncoding == null) {
            return "UTF-8";
        } else {
            return originalXmlEncoding;
        }
    }

    /**
     * Sets the value of the originalXmlEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalXmlEncoding(String value) {
        this.originalXmlEncoding = value;
    }

    /**
     * Gets the value of the originalXmlVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalXmlVersion() {
        if (originalXmlVersion == null) {
            return "1.0";
        } else {
            return originalXmlVersion;
        }
    }

    /**
     * Sets the value of the originalXmlVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalXmlVersion(String value) {
        this.originalXmlVersion = value;
    }

    /**
     * Gets the value of the originalXmlStandalone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalXmlStandalone() {
        if (originalXmlStandalone == null) {
            return "yes";
        } else {
            return originalXmlStandalone;
        }
    }

    /**
     * Sets the value of the originalXmlStandalone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalXmlStandalone(String value) {
        this.originalXmlStandalone = value;
    }

}
