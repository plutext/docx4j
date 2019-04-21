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

package org.docx4j.docProps.variantTypes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CT_Vector complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Vector">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}variant"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i1"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i2"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i4"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}i8"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui1"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui2"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui4"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}ui8"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}r4"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}r8"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}lpstr"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}lpwstr"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}bstr"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}date"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}filetime"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}bool"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}cy"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}error"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}clsid"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes}cf"/>
 *       &lt;/choice>
 *       &lt;attribute name="baseType" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="variant"/>
 *             &lt;enumeration value="i1"/>
 *             &lt;enumeration value="i2"/>
 *             &lt;enumeration value="i4"/>
 *             &lt;enumeration value="i8"/>
 *             &lt;enumeration value="ui1"/>
 *             &lt;enumeration value="ui2"/>
 *             &lt;enumeration value="ui4"/>
 *             &lt;enumeration value="ui8"/>
 *             &lt;enumeration value="r4"/>
 *             &lt;enumeration value="r8"/>
 *             &lt;enumeration value="lpstr"/>
 *             &lt;enumeration value="lpwstr"/>
 *             &lt;enumeration value="bstr"/>
 *             &lt;enumeration value="date"/>
 *             &lt;enumeration value="filetime"/>
 *             &lt;enumeration value="bool"/>
 *             &lt;enumeration value="cy"/>
 *             &lt;enumeration value="error"/>
 *             &lt;enumeration value="clsid"/>
 *             &lt;enumeration value="cf"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="size" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Vector", propOrder = {
    "variantOrI1OrI2"
})
public class Vector {

    @XmlElementRefs({
        @XmlElementRef(name = "clsid", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "r4", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "i1", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "ui2", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "error", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "i8", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "date", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "i4", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "variant", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "ui8", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "ui1", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "bstr", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "lpwstr", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "ui4", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "bool", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "r8", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "cy", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "filetime", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "i2", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "lpstr", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class),
        @XmlElementRef(name = "cf", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> variantOrI1OrI2;
    @XmlAttribute(required = true)
    protected String baseType;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long size;

    /**
     * Gets the value of the variantOrI1OrI2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variantOrI1OrI2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariantOrI1OrI2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Float }{@code >}
     * {@link JAXBElement }{@code <}{@link Byte }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Long }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     * {@link JAXBElement }{@code <}{@link Variant }{@code >}
     * {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * {@link JAXBElement }{@code <}{@link Short }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Long }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Double }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     * {@link JAXBElement }{@code <}{@link Short }{@code >}
     * {@link JAXBElement }{@code <}{@link Cf }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getVariantOrI1OrI2() {
        if (variantOrI1OrI2 == null) {
            variantOrI1OrI2 = new ArrayList<JAXBElement<?>>();
        }
        return this.variantOrI1OrI2;
    }

    /**
     * Gets the value of the baseType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseType() {
        return baseType;
    }

    /**
     * Sets the value of the baseType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseType(String value) {
        this.baseType = value;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(long value) {
        this.size = value;
    }

}
