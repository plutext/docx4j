/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

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
package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Kinsoku complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Kinsoku">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="lang" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="invalStChars" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="invalEndChars" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Kinsoku")
public class CTKinsoku {

    @XmlAttribute(name = "lang")
    protected String lang;
    @XmlAttribute(name = "invalStChars", required = true)
    protected String invalStChars;
    @XmlAttribute(name = "invalEndChars", required = true)
    protected String invalEndChars;

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

    /**
     * Gets the value of the invalStChars property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvalStChars() {
        return invalStChars;
    }

    /**
     * Sets the value of the invalStChars property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvalStChars(String value) {
        this.invalStChars = value;
    }

    /**
     * Gets the value of the invalEndChars property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInvalEndChars() {
        return invalEndChars;
    }

    /**
     * Sets the value of the invalEndChars property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInvalEndChars(String value) {
        this.invalEndChars = value;
    }

}
