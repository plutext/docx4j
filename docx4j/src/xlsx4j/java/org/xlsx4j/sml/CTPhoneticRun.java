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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PhoneticRun complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PhoneticRun">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="t" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sb" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="eb" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PhoneticRun", propOrder = {
    "t"
})
public class CTPhoneticRun {

    @XmlElement(required = true)
    protected String t;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sb;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long eb;

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setT(String value) {
        this.t = value;
    }

    /**
     * Gets the value of the sb property.
     * 
     */
    public long getSb() {
        return sb;
    }

    /**
     * Sets the value of the sb property.
     * 
     */
    public void setSb(long value) {
        this.sb = value;
    }

    /**
     * Gets the value of the eb property.
     * 
     */
    public long getEb() {
        return eb;
    }

    /**
     * Sets the value of the eb property.
     * 
     */
    public void setEb(long value) {
        this.eb = value;
    }

}
