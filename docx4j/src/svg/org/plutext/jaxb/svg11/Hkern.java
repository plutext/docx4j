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


package org.plutext.jaxb.svg11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://www.w3.org/2000/svg}SVG.Core.attrib"/>
 *       &lt;attribute name="u1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="g1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="u2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="g2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="k" use="required" type="{http://www.w3.org/2000/svg}Number.datatype" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "hkern")
public class Hkern {

    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String u1;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String g1;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String u2;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String g2;
    @XmlAttribute(required = true)
    protected String k;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    protected String base;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lang;

    /**
     * Gets the value of the u1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getU1() {
        return u1;
    }

    /**
     * Sets the value of the u1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setU1(String value) {
        this.u1 = value;
    }

    /**
     * Gets the value of the g1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getG1() {
        return g1;
    }

    /**
     * Sets the value of the g1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setG1(String value) {
        this.g1 = value;
    }

    /**
     * Gets the value of the u2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getU2() {
        return u2;
    }

    /**
     * Sets the value of the u2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setU2(String value) {
        this.u2 = value;
    }

    /**
     * Gets the value of the g2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getG2() {
        return g2;
    }

    /**
     * Sets the value of the g2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setG2(String value) {
        this.g2 = value;
    }

    /**
     * Gets the value of the k property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getK() {
        return k;
    }

    /**
     * Sets the value of the k property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setK(String value) {
        this.k = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBase(String value) {
        this.base = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpace() {
        return space;
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(String value) {
        this.space = value;
    }

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

}
