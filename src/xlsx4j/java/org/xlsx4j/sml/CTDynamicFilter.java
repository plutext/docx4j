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
import javax.xml.datatype.XMLGregorianCalendar;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DynamicFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DynamicFilter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DynamicFilterType" />
 *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="valIso" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="maxVal" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="maxValIso" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DynamicFilter")
public class CTDynamicFilter implements Child
{

    @XmlAttribute(name = "type", required = true)
    protected STDynamicFilterType type;
    @XmlAttribute(name = "val")
    protected Double val;
    @XmlAttribute(name = "valIso")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar valIso;
    @XmlAttribute(name = "maxVal")
    protected Double maxVal;
    @XmlAttribute(name = "maxValIso")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar maxValIso;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STDynamicFilterType }
     *     
     */
    public STDynamicFilterType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDynamicFilterType }
     *     
     */
    public void setType(STDynamicFilterType value) {
        this.type = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setVal(Double value) {
        this.val = value;
    }

    /**
     * Gets the value of the valIso property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getValIso() {
        return valIso;
    }

    /**
     * Sets the value of the valIso property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setValIso(XMLGregorianCalendar value) {
        this.valIso = value;
    }

    /**
     * Gets the value of the maxVal property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxVal() {
        return maxVal;
    }

    /**
     * Sets the value of the maxVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxVal(Double value) {
        this.maxVal = value;
    }

    /**
     * Gets the value of the maxValIso property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMaxValIso() {
        return maxValIso;
    }

    /**
     * Sets the value of the maxValIso property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMaxValIso(XMLGregorianCalendar value) {
        this.maxValIso = value;
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
