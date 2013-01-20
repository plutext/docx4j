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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Color complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Color">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="auto" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="indexed" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rgb" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedIntHex" />
 *       &lt;attribute name="theme" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="tint" type="{http://www.w3.org/2001/XMLSchema}double" default="0.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Color")
public class CTColor implements Child
{

    @XmlAttribute(name = "auto")
    protected Boolean auto;
    @XmlAttribute(name = "indexed")
    @XmlSchemaType(name = "unsignedInt")
    protected Long indexed;
    @XmlAttribute(name = "rgb")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] rgb;
    @XmlAttribute(name = "theme")
    @XmlSchemaType(name = "unsignedInt")
    protected Long theme;
    @XmlAttribute(name = "tint")
    protected Double tint;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the auto property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAuto() {
        return auto;
    }

    /**
     * Sets the value of the auto property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAuto(Boolean value) {
        this.auto = value;
    }

    /**
     * Gets the value of the indexed property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIndexed() {
        return indexed;
    }

    /**
     * Sets the value of the indexed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIndexed(Long value) {
        this.indexed = value;
    }

    /**
     * Gets the value of the rgb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getRgb() {
        return rgb;
    }

    /**
     * Sets the value of the rgb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRgb(byte[] value) {
        this.rgb = value;
    }

    /**
     * Gets the value of the theme property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTheme() {
        return theme;
    }

    /**
     * Sets the value of the theme property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTheme(Long value) {
        this.theme = value;
    }

    /**
     * Gets the value of the tint property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getTint() {
        if (tint == null) {
            return  0.0D;
        } else {
            return tint;
        }
    }

    /**
     * Sets the value of the tint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTint(Double value) {
        this.tint = value;
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
