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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SharedItems complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SharedItems">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="m" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Missing"/>
 *         &lt;element name="n" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Number"/>
 *         &lt;element name="b" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Boolean"/>
 *         &lt;element name="e" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Error"/>
 *         &lt;element name="s" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_String"/>
 *         &lt;element name="d" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DateTime"/>
 *       &lt;/choice>
 *       &lt;attribute name="containsSemiMixedTypes" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="containsNonDate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="containsDate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="containsString" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="containsBlank" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="containsMixedTypes" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="containsNumber" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="containsInteger" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="minValue" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="maxValue" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="minDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="maxDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="longText" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SharedItems", propOrder = {
    "mOrNOrB"
})
public class CTSharedItems implements Child
{

    @XmlElements({
        @XmlElement(name = "m", type = CTMissing.class),
        @XmlElement(name = "n", type = CTNumber.class),
        @XmlElement(name = "b", type = CTBoolean.class),
        @XmlElement(name = "e", type = CTError.class),
        @XmlElement(name = "s", type = CTString.class),
        @XmlElement(name = "d", type = CTDateTime.class)
    })
    protected List<Object> mOrNOrB;
    @XmlAttribute(name = "containsSemiMixedTypes")
    protected Boolean containsSemiMixedTypes;
    @XmlAttribute(name = "containsNonDate")
    protected Boolean containsNonDate;
    @XmlAttribute(name = "containsDate")
    protected Boolean containsDate;
    @XmlAttribute(name = "containsString")
    protected Boolean containsString;
    @XmlAttribute(name = "containsBlank")
    protected Boolean containsBlank;
    @XmlAttribute(name = "containsMixedTypes")
    protected Boolean containsMixedTypes;
    @XmlAttribute(name = "containsNumber")
    protected Boolean containsNumber;
    @XmlAttribute(name = "containsInteger")
    protected Boolean containsInteger;
    @XmlAttribute(name = "minValue")
    protected Double minValue;
    @XmlAttribute(name = "maxValue")
    protected Double maxValue;
    @XmlAttribute(name = "minDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar minDate;
    @XmlAttribute(name = "maxDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar maxDate;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;
    @XmlAttribute(name = "longText")
    protected Boolean longText;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the mOrNOrB property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mOrNOrB property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMOrNOrB().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTMissing }
     * {@link CTNumber }
     * {@link CTBoolean }
     * {@link CTError }
     * {@link CTString }
     * {@link CTDateTime }
     * 
     * 
     */
    public List<Object> getMOrNOrB() {
        if (mOrNOrB == null) {
            mOrNOrB = new ArrayList<Object>();
        }
        return this.mOrNOrB;
    }

    /**
     * Gets the value of the containsSemiMixedTypes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsSemiMixedTypes() {
        if (containsSemiMixedTypes == null) {
            return true;
        } else {
            return containsSemiMixedTypes;
        }
    }

    /**
     * Sets the value of the containsSemiMixedTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsSemiMixedTypes(Boolean value) {
        this.containsSemiMixedTypes = value;
    }

    /**
     * Gets the value of the containsNonDate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsNonDate() {
        if (containsNonDate == null) {
            return true;
        } else {
            return containsNonDate;
        }
    }

    /**
     * Sets the value of the containsNonDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsNonDate(Boolean value) {
        this.containsNonDate = value;
    }

    /**
     * Gets the value of the containsDate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsDate() {
        if (containsDate == null) {
            return false;
        } else {
            return containsDate;
        }
    }

    /**
     * Sets the value of the containsDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsDate(Boolean value) {
        this.containsDate = value;
    }

    /**
     * Gets the value of the containsString property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsString() {
        if (containsString == null) {
            return true;
        } else {
            return containsString;
        }
    }

    /**
     * Sets the value of the containsString property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsString(Boolean value) {
        this.containsString = value;
    }

    /**
     * Gets the value of the containsBlank property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsBlank() {
        if (containsBlank == null) {
            return false;
        } else {
            return containsBlank;
        }
    }

    /**
     * Sets the value of the containsBlank property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsBlank(Boolean value) {
        this.containsBlank = value;
    }

    /**
     * Gets the value of the containsMixedTypes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsMixedTypes() {
        if (containsMixedTypes == null) {
            return false;
        } else {
            return containsMixedTypes;
        }
    }

    /**
     * Sets the value of the containsMixedTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsMixedTypes(Boolean value) {
        this.containsMixedTypes = value;
    }

    /**
     * Gets the value of the containsNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsNumber() {
        if (containsNumber == null) {
            return false;
        } else {
            return containsNumber;
        }
    }

    /**
     * Sets the value of the containsNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsNumber(Boolean value) {
        this.containsNumber = value;
    }

    /**
     * Gets the value of the containsInteger property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContainsInteger() {
        if (containsInteger == null) {
            return false;
        } else {
            return containsInteger;
        }
    }

    /**
     * Sets the value of the containsInteger property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContainsInteger(Boolean value) {
        this.containsInteger = value;
    }

    /**
     * Gets the value of the minValue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * Sets the value of the minValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinValue(Double value) {
        this.minValue = value;
    }

    /**
     * Gets the value of the maxValue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the value of the maxValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxValue(Double value) {
        this.maxValue = value;
    }

    /**
     * Gets the value of the minDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMinDate() {
        return minDate;
    }

    /**
     * Sets the value of the minDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMinDate(XMLGregorianCalendar value) {
        this.minDate = value;
    }

    /**
     * Gets the value of the maxDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMaxDate() {
        return maxDate;
    }

    /**
     * Sets the value of the maxDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMaxDate(XMLGregorianCalendar value) {
        this.maxDate = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCount(Long value) {
        this.count = value;
    }

    /**
     * Gets the value of the longText property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLongText() {
        if (longText == null) {
            return false;
        } else {
            return longText;
        }
    }

    /**
     * Sets the value of the longText property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLongText(Boolean value) {
        this.longText = value;
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
