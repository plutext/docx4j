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
 
package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_SerTx complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SerTx">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="strRef" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StrRef"/>
 *           &lt;element name="v" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}ST_Xstring"/>
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
@XmlType(name = "CT_SerTx", propOrder = {
    "strRef",
    "v"
})
public class CTSerTx {

    protected CTStrRef strRef;
    protected String v;

    /**
     * Gets the value of the strRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrRef }
     *     
     */
    public CTStrRef getStrRef() {
        return strRef;
    }

    /**
     * Sets the value of the strRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrRef }
     *     
     */
    public void setStrRef(CTStrRef value) {
        this.strRef = value;
    }

    /**
     * Gets the value of the v property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getV() {
        return v;
    }

    /**
     * Sets the value of the v property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setV(String value) {
        this.v = value;
    }

}
