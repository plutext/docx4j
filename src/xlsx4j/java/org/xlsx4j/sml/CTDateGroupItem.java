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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_DateGroupItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DateGroupItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="year" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="month" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="day" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="hour" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="minute" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="second" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="dateTimeGrouping" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DateTimeGrouping" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DateGroupItem")
public class CTDateGroupItem {

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedShort")
    protected int year;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer month;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer day;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer hour;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer minute;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer second;
    @XmlAttribute(required = true)
    protected STDateTimeGrouping dateTimeGrouping;

    /**
     * Gets the value of the year property.
     * 
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     * 
     */
    public void setYear(int value) {
        this.year = value;
    }

    /**
     * Gets the value of the month property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMonth(Integer value) {
        this.month = value;
    }

    /**
     * Gets the value of the day property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDay(Integer value) {
        this.day = value;
    }

    /**
     * Gets the value of the hour property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHour() {
        return hour;
    }

    /**
     * Sets the value of the hour property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHour(Integer value) {
        this.hour = value;
    }

    /**
     * Gets the value of the minute property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinute() {
        return minute;
    }

    /**
     * Sets the value of the minute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinute(Integer value) {
        this.minute = value;
    }

    /**
     * Gets the value of the second property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSecond() {
        return second;
    }

    /**
     * Sets the value of the second property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSecond(Integer value) {
        this.second = value;
    }

    /**
     * Gets the value of the dateTimeGrouping property.
     * 
     * @return
     *     possible object is
     *     {@link STDateTimeGrouping }
     *     
     */
    public STDateTimeGrouping getDateTimeGrouping() {
        return dateTimeGrouping;
    }

    /**
     * Sets the value of the dateTimeGrouping property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDateTimeGrouping }
     *     
     */
    public void setDateTimeGrouping(STDateTimeGrouping value) {
        this.dateTimeGrouping = value;
    }

}
