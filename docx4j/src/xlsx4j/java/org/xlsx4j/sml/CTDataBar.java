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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_DataBar complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataBar">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cfvo" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cfvo" maxOccurs="2" minOccurs="2"/>
 *         &lt;element name="color" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Color"/>
 *       &lt;/sequence>
 *       &lt;attribute name="minLength" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="10" />
 *       &lt;attribute name="maxLength" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="90" />
 *       &lt;attribute name="showValue" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataBar", propOrder = {
    "cfvo",
    "color"
})
public class CTDataBar {

    @XmlElement(required = true)
    protected List<CTCfvo> cfvo;
    @XmlElement(required = true)
    protected CTColor color;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long minLength;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long maxLength;
    @XmlAttribute
    protected Boolean showValue;

    /**
     * Gets the value of the cfvo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cfvo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCfvo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCfvo }
     * 
     * 
     */
    public List<CTCfvo> getCfvo() {
        if (cfvo == null) {
            cfvo = new ArrayList<CTCfvo>();
        }
        return this.cfvo;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setColor(CTColor value) {
        this.color = value;
    }

    /**
     * Gets the value of the minLength property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getMinLength() {
        if (minLength == null) {
            return  10L;
        } else {
            return minLength;
        }
    }

    /**
     * Sets the value of the minLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMinLength(Long value) {
        this.minLength = value;
    }

    /**
     * Gets the value of the maxLength property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getMaxLength() {
        if (maxLength == null) {
            return  90L;
        } else {
            return maxLength;
        }
    }

    /**
     * Sets the value of the maxLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMaxLength(Long value) {
        this.maxLength = value;
    }

    /**
     * Gets the value of the showValue property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowValue() {
        if (showValue == null) {
            return true;
        } else {
            return showValue;
        }
    }

    /**
     * Sets the value of the showValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowValue(Boolean value) {
        this.showValue = value;
    }

}
