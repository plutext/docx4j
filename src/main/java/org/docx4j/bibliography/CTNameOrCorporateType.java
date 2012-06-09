/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.bibliography;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_NameOrCorporateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NameOrCorporateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="NameList" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameListType"/>
 *           &lt;element name="Corporate" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_String"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NameOrCorporateType", propOrder = {
    "nameList",
    "corporate"
})
public class CTNameOrCorporateType {

    @XmlElement(name = "NameList")
    protected CTNameListType nameList;
    @XmlElement(name = "Corporate")
    protected String corporate;

    /**
     * Gets the value of the nameList property.
     * 
     * @return
     *     possible object is
     *     {@link CTNameListType }
     *     
     */
    public CTNameListType getNameList() {
        return nameList;
    }

    /**
     * Sets the value of the nameList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNameListType }
     *     
     */
    public void setNameList(CTNameListType value) {
        this.nameList = value;
    }

    /**
     * Gets the value of the corporate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorporate() {
        return corporate;
    }

    /**
     * Sets the value of the corporate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorporate(String value) {
        this.corporate = value;
    }

}
