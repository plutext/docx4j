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
 * <p>Java class for CT_ExternalDefinedName complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ExternalDefinedName">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="refersTo" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="sheetId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ExternalDefinedName")
public class CTExternalDefinedName {

    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String refersTo;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long sheetId;

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
     * Gets the value of the refersTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefersTo() {
        return refersTo;
    }

    /**
     * Sets the value of the refersTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefersTo(String value) {
        this.refersTo = value;
    }

    /**
     * Gets the value of the sheetId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSheetId() {
        return sheetId;
    }

    /**
     * Sets the value of the sheetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSheetId(Long value) {
        this.sheetId = value;
    }

}
