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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TimePeriod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TimePeriod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="today"/>
 *     &lt;enumeration value="yesterday"/>
 *     &lt;enumeration value="tomorrow"/>
 *     &lt;enumeration value="last7Days"/>
 *     &lt;enumeration value="thisMonth"/>
 *     &lt;enumeration value="lastMonth"/>
 *     &lt;enumeration value="nextMonth"/>
 *     &lt;enumeration value="thisWeek"/>
 *     &lt;enumeration value="lastWeek"/>
 *     &lt;enumeration value="nextWeek"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TimePeriod")
@XmlEnum
public enum STTimePeriod {


    /**
     * Today
     * 
     */
    @XmlEnumValue("today")
    TODAY("today"),

    /**
     * Yesterday
     * 
     */
    @XmlEnumValue("yesterday")
    YESTERDAY("yesterday"),

    /**
     * Tomorrow
     * 
     */
    @XmlEnumValue("tomorrow")
    TOMORROW("tomorrow"),

    /**
     * Last 7 Days
     * 
     */
    @XmlEnumValue("last7Days")
    LAST_7_DAYS("last7Days"),

    /**
     * This Month
     * 
     */
    @XmlEnumValue("thisMonth")
    THIS_MONTH("thisMonth"),

    /**
     * Last Month
     * 
     */
    @XmlEnumValue("lastMonth")
    LAST_MONTH("lastMonth"),

    /**
     * Next Month
     * 
     */
    @XmlEnumValue("nextMonth")
    NEXT_MONTH("nextMonth"),

    /**
     * This Week
     * 
     */
    @XmlEnumValue("thisWeek")
    THIS_WEEK("thisWeek"),

    /**
     * Last Week
     * 
     */
    @XmlEnumValue("lastWeek")
    LAST_WEEK("lastWeek"),

    /**
     * Next Week
     * 
     */
    @XmlEnumValue("nextWeek")
    NEXT_WEEK("nextWeek");
    private final String value;

    STTimePeriod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTimePeriod fromValue(String v) {
        for (STTimePeriod c: STTimePeriod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
