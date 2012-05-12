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
 * <p>Java class for CT_FieldGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FieldGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rangePr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RangePr" minOccurs="0"/>
 *         &lt;element name="discretePr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DiscretePr" minOccurs="0"/>
 *         &lt;element name="groupItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_GroupItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="par" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="base" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FieldGroup", propOrder = {
    "rangePr",
    "discretePr",
    "groupItems"
})
public class CTFieldGroup {

    protected CTRangePr rangePr;
    protected CTDiscretePr discretePr;
    protected CTGroupItems groupItems;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long par;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long base;

    /**
     * Gets the value of the rangePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTRangePr }
     *     
     */
    public CTRangePr getRangePr() {
        return rangePr;
    }

    /**
     * Sets the value of the rangePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRangePr }
     *     
     */
    public void setRangePr(CTRangePr value) {
        this.rangePr = value;
    }

    /**
     * Gets the value of the discretePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTDiscretePr }
     *     
     */
    public CTDiscretePr getDiscretePr() {
        return discretePr;
    }

    /**
     * Sets the value of the discretePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDiscretePr }
     *     
     */
    public void setDiscretePr(CTDiscretePr value) {
        this.discretePr = value;
    }

    /**
     * Gets the value of the groupItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupItems }
     *     
     */
    public CTGroupItems getGroupItems() {
        return groupItems;
    }

    /**
     * Sets the value of the groupItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupItems }
     *     
     */
    public void setGroupItems(CTGroupItems value) {
        this.groupItems = value;
    }

    /**
     * Gets the value of the par property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPar() {
        return par;
    }

    /**
     * Sets the value of the par property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPar(Long value) {
        this.par = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBase(Long value) {
        this.base = value;
    }

}
