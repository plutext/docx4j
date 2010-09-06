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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CT_RangePr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RangePr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="autoStart" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="autoEnd" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="groupBy" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_GroupBy" default="range" />
 *       &lt;attribute name="startNum" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="endNum" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="endDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="groupInterval" type="{http://www.w3.org/2001/XMLSchema}double" default="1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RangePr")
public class CTRangePr {

    @XmlAttribute
    protected Boolean autoStart;
    @XmlAttribute
    protected Boolean autoEnd;
    @XmlAttribute
    protected STGroupBy groupBy;
    @XmlAttribute
    protected Double startNum;
    @XmlAttribute
    protected Double endNum;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDate;
    @XmlAttribute
    protected Double groupInterval;

    /**
     * Gets the value of the autoStart property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoStart() {
        if (autoStart == null) {
            return true;
        } else {
            return autoStart;
        }
    }

    /**
     * Sets the value of the autoStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoStart(Boolean value) {
        this.autoStart = value;
    }

    /**
     * Gets the value of the autoEnd property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoEnd() {
        if (autoEnd == null) {
            return true;
        } else {
            return autoEnd;
        }
    }

    /**
     * Sets the value of the autoEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoEnd(Boolean value) {
        this.autoEnd = value;
    }

    /**
     * Gets the value of the groupBy property.
     * 
     * @return
     *     possible object is
     *     {@link STGroupBy }
     *     
     */
    public STGroupBy getGroupBy() {
        if (groupBy == null) {
            return STGroupBy.RANGE;
        } else {
            return groupBy;
        }
    }

    /**
     * Sets the value of the groupBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link STGroupBy }
     *     
     */
    public void setGroupBy(STGroupBy value) {
        this.groupBy = value;
    }

    /**
     * Gets the value of the startNum property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getStartNum() {
        return startNum;
    }

    /**
     * Sets the value of the startNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setStartNum(Double value) {
        this.startNum = value;
    }

    /**
     * Gets the value of the endNum property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEndNum() {
        return endNum;
    }

    /**
     * Sets the value of the endNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEndNum(Double value) {
        this.endNum = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDate(XMLGregorianCalendar value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the groupInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getGroupInterval() {
        if (groupInterval == null) {
            return  1.0D;
        } else {
            return groupInterval;
        }
    }

    /**
     * Sets the value of the groupInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setGroupInterval(Double value) {
        this.groupInterval = value;
    }

}
