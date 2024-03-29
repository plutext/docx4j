/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextFont complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextFont"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="typeface"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="panose"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="pitchFamily" type="{http://www.w3.org/2001/XMLSchema}byte" default="0" /&gt;
 *       &lt;attribute name="charset" type="{http://www.w3.org/2001/XMLSchema}byte" default="1" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextFont")
public class TextFont implements Child
{

    @XmlAttribute(name = "typeface")
    protected String typeface;
    @XmlAttribute(name = "panose")
    protected String panose;
    @XmlAttribute(name = "pitchFamily")
    protected Byte pitchFamily;
    @XmlAttribute(name = "charset")
    protected Byte charset;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the typeface property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeface() {
        return typeface;
    }

    /**
     * Sets the value of the typeface property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeface(String value) {
        this.typeface = value;
    }

    /**
     * Gets the value of the panose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPanose() {
        return panose;
    }

    /**
     * Sets the value of the panose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPanose(String value) {
        this.panose = value;
    }

    /**
     * Gets the value of the pitchFamily property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public byte getPitchFamily() {
        if (pitchFamily == null) {
            return ((byte) 0);
        } else {
            return pitchFamily;
        }
    }

    /**
     * Sets the value of the pitchFamily property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setPitchFamily(Byte value) {
        this.pitchFamily = value;
    }

    /**
     * Gets the value of the charset property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public byte getCharset() {
        if (charset == null) {
            return ((byte) 1);
        } else {
            return charset;
        }
    }

    /**
     * Sets the value of the charset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setCharset(Byte value) {
        this.charset = value;
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
