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
 * <p>Java class for CT_XmlColumnPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_XmlColumnPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="mapId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="xpath" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="denormalized" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="xmlDataType" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_XmlDataType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_XmlColumnPr", propOrder = {
    "extLst"
})
public class CTXmlColumnPr {

    protected CTExtensionList extLst;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long mapId;
    @XmlAttribute(required = true)
    protected String xpath;
    @XmlAttribute
    protected Boolean denormalized;
    @XmlAttribute(required = true)
    protected STXmlDataType xmlDataType;

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
     * Gets the value of the mapId property.
     * 
     */
    public long getMapId() {
        return mapId;
    }

    /**
     * Sets the value of the mapId property.
     * 
     */
    public void setMapId(long value) {
        this.mapId = value;
    }

    /**
     * Gets the value of the xpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Sets the value of the xpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXpath(String value) {
        this.xpath = value;
    }

    /**
     * Gets the value of the denormalized property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDenormalized() {
        if (denormalized == null) {
            return false;
        } else {
            return denormalized;
        }
    }

    /**
     * Sets the value of the denormalized property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDenormalized(Boolean value) {
        this.denormalized = value;
    }

    /**
     * Gets the value of the xmlDataType property.
     * 
     * @return
     *     possible object is
     *     {@link STXmlDataType }
     *     
     */
    public STXmlDataType getXmlDataType() {
        return xmlDataType;
    }

    /**
     * Sets the value of the xmlDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STXmlDataType }
     *     
     */
    public void setXmlDataType(STXmlDataType value) {
        this.xmlDataType = value;
    }

}
