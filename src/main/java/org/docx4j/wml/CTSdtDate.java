/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SdtDate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SdtDate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dateFormat" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="lid" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Lang" minOccurs="0"/>
 *         &lt;element name="storeMappedDataAs" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_SdtDateMappingType" minOccurs="0"/>
 *         &lt;element name="calendar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_CalendarType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="fullDate" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SdtDate", propOrder = {
    "dateFormat",
    "lid",
    "storeMappedDataAs",
    "calendar"
})
public class CTSdtDate implements Child
{

    protected CTSdtDate.DateFormat dateFormat;
    protected CTLang lid;
    protected CTSdtDateMappingType storeMappedDataAs;
    protected CTCalendarType calendar;
    @XmlAttribute(name = "fullDate", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected XMLGregorianCalendar fullDate;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dateFormat property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtDate.DateFormat }
     *     
     */
    public CTSdtDate.DateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the value of the dateFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtDate.DateFormat }
     *     
     */
    public void setDateFormat(CTSdtDate.DateFormat value) {
        this.dateFormat = value;
    }

    /**
     * Gets the value of the lid property.
     * 
     * @return
     *     possible object is
     *     {@link CTLang }
     *     
     */
    public CTLang getLid() {
        return lid;
    }

    /**
     * Sets the value of the lid property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLang }
     *     
     */
    public void setLid(CTLang value) {
        this.lid = value;
    }

    /**
     * Gets the value of the storeMappedDataAs property.
     * 
     * @return
     *     possible object is
     *     {@link CTSdtDateMappingType }
     *     
     */
    public CTSdtDateMappingType getStoreMappedDataAs() {
        return storeMappedDataAs;
    }

    /**
     * Sets the value of the storeMappedDataAs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSdtDateMappingType }
     *     
     */
    public void setStoreMappedDataAs(CTSdtDateMappingType value) {
        this.storeMappedDataAs = value;
    }

    /**
     * Gets the value of the calendar property.
     * 
     * @return
     *     possible object is
     *     {@link CTCalendarType }
     *     
     */
    public CTCalendarType getCalendar() {
        return calendar;
    }

    /**
     * Sets the value of the calendar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCalendarType }
     *     
     */
    public void setCalendar(CTCalendarType value) {
        this.calendar = value;
    }

    /**
     * Gets the value of the fullDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFullDate() {
        return fullDate;
    }

    /**
     * Sets the value of the fullDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFullDate(XMLGregorianCalendar value) {
        this.fullDate = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DateFormat implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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

}
