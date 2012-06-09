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
 
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_AlphaModulateEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AlphaModulateEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EffectContainer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AlphaModulateEffect", propOrder = {
    "cont"
})
public class CTAlphaModulateEffect {

    @XmlElement(required = true)
    protected CTEffectContainer cont;

    /**
     * Gets the value of the cont property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectContainer }
     *     
     */
    public CTEffectContainer getCont() {
        return cont;
    }

    /**
     * Sets the value of the cont property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectContainer }
     *     
     */
    public void setCont(CTEffectContainer value) {
        this.cont = value;
    }

}
