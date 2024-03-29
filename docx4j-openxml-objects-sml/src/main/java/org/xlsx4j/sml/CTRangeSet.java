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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RangeSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RangeSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="i1" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="i2" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="i3" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="i4" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="ref" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Ref" />
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="sheet" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RangeSet")
public class CTRangeSet implements Child
{

    @XmlAttribute(name = "i1")
    @XmlSchemaType(name = "unsignedInt")
    protected Long i1;
    @XmlAttribute(name = "i2")
    @XmlSchemaType(name = "unsignedInt")
    protected Long i2;
    @XmlAttribute(name = "i3")
    @XmlSchemaType(name = "unsignedInt")
    protected Long i3;
    @XmlAttribute(name = "i4")
    @XmlSchemaType(name = "unsignedInt")
    protected Long i4;
    @XmlAttribute(name = "ref")
    protected String ref;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "sheet")
    protected String sheet;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the i1 property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getI1() {
        return i1;
    }

    /**
     * Sets the value of the i1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setI1(Long value) {
        this.i1 = value;
    }

    /**
     * Gets the value of the i2 property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getI2() {
        return i2;
    }

    /**
     * Sets the value of the i2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setI2(Long value) {
        this.i2 = value;
    }

    /**
     * Gets the value of the i3 property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getI3() {
        return i3;
    }

    /**
     * Sets the value of the i3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setI3(Long value) {
        this.i3 = value;
    }

    /**
     * Gets the value of the i4 property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getI4() {
        return i4;
    }

    /**
     * Sets the value of the i4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setI4(Long value) {
        this.i4 = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the sheet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSheet() {
        return sheet;
    }

    /**
     * Sets the value of the sheet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSheet(String value) {
        this.sheet = value;
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
