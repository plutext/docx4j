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
 * <p>Java class for CT_Tuple complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Tuple">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="fld" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="hier" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="item" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Tuple")
public class CTTuple {

    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long fld;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long hier;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long item;

    /**
     * Gets the value of the fld property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFld() {
        return fld;
    }

    /**
     * Sets the value of the fld property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFld(Long value) {
        this.fld = value;
    }

    /**
     * Gets the value of the hier property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getHier() {
        return hier;
    }

    /**
     * Sets the value of the hier property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHier(Long value) {
        this.hier = value;
    }

    /**
     * Gets the value of the item property.
     * 
     */
    public long getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     * 
     */
    public void setItem(long value) {
        this.item = value;
    }

}
