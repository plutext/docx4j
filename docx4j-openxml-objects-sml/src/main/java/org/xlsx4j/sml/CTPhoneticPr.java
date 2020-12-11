/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PhoneticPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PhoneticPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="fontId" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FontId" />
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PhoneticType" default="fullwidthKatakana" />
 *       &lt;attribute name="alignment" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PhoneticAlignment" default="left" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PhoneticPr")
public class CTPhoneticPr implements Child
{

    @XmlAttribute(name = "fontId", required = true)
    protected long fontId;
    @XmlAttribute(name = "type")
    protected STPhoneticType type;
    @XmlAttribute(name = "alignment")
    protected STPhoneticAlignment alignment;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fontId property.
     * 
     */
    public long getFontId() {
        return fontId;
    }

    /**
     * Sets the value of the fontId property.
     * 
     */
    public void setFontId(long value) {
        this.fontId = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STPhoneticType }
     *     
     */
    public STPhoneticType getType() {
        if (type == null) {
            return STPhoneticType.FULLWIDTH_KATAKANA;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPhoneticType }
     *     
     */
    public void setType(STPhoneticType value) {
        this.type = value;
    }

    /**
     * Gets the value of the alignment property.
     * 
     * @return
     *     possible object is
     *     {@link STPhoneticAlignment }
     *     
     */
    public STPhoneticAlignment getAlignment() {
        if (alignment == null) {
            return STPhoneticAlignment.LEFT;
        } else {
            return alignment;
        }
    }

    /**
     * Sets the value of the alignment property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPhoneticAlignment }
     *     
     */
    public void setAlignment(STPhoneticAlignment value) {
        this.alignment = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
