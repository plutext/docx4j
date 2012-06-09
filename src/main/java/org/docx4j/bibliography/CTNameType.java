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
 * <p>Java class for CT_NameType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NameType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NameList" type="{http://schemas.openxmlformats.org/officeDocument/2006/bibliography}CT_NameListType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NameType", propOrder = {
    "nameList"
})
public class CTNameType {

    @XmlElement(name = "NameList", required = true)
    protected CTNameListType nameList;

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

}
