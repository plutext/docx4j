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
 * <p>Java class for ST_DynamicFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DynamicFilterType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="null"/>
 *     &lt;enumeration value="aboveAverage"/>
 *     &lt;enumeration value="belowAverage"/>
 *     &lt;enumeration value="tomorrow"/>
 *     &lt;enumeration value="today"/>
 *     &lt;enumeration value="yesterday"/>
 *     &lt;enumeration value="nextWeek"/>
 *     &lt;enumeration value="thisWeek"/>
 *     &lt;enumeration value="lastWeek"/>
 *     &lt;enumeration value="nextMonth"/>
 *     &lt;enumeration value="thisMonth"/>
 *     &lt;enumeration value="lastMonth"/>
 *     &lt;enumeration value="nextQuarter"/>
 *     &lt;enumeration value="thisQuarter"/>
 *     &lt;enumeration value="lastQuarter"/>
 *     &lt;enumeration value="nextYear"/>
 *     &lt;enumeration value="thisYear"/>
 *     &lt;enumeration value="lastYear"/>
 *     &lt;enumeration value="yearToDate"/>
 *     &lt;enumeration value="Q1"/>
 *     &lt;enumeration value="Q2"/>
 *     &lt;enumeration value="Q3"/>
 *     &lt;enumeration value="Q4"/>
 *     &lt;enumeration value="M1"/>
 *     &lt;enumeration value="M2"/>
 *     &lt;enumeration value="M3"/>
 *     &lt;enumeration value="M4"/>
 *     &lt;enumeration value="M5"/>
 *     &lt;enumeration value="M6"/>
 *     &lt;enumeration value="M7"/>
 *     &lt;enumeration value="M8"/>
 *     &lt;enumeration value="M9"/>
 *     &lt;enumeration value="M10"/>
 *     &lt;enumeration value="M11"/>
 *     &lt;enumeration value="M12"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DynamicFilterType")
@XmlEnum
public enum STDynamicFilterType {


    /**
     * Null
     * 
     */
    @XmlEnumValue("null")
    NULL("null"),

    /**
     * Above Average
     * 
     */
    @XmlEnumValue("aboveAverage")
    ABOVE_AVERAGE("aboveAverage"),

    /**
     * Below Average
     * 
     */
    @XmlEnumValue("belowAverage")
    BELOW_AVERAGE("belowAverage"),

    /**
     * Tomorrow
     * 
     */
    @XmlEnumValue("tomorrow")
    TOMORROW("tomorrow"),

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
     * Next Week
     * 
     */
    @XmlEnumValue("nextWeek")
    NEXT_WEEK("nextWeek"),

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
     * Next Month
     * 
     */
    @XmlEnumValue("nextMonth")
    NEXT_MONTH("nextMonth"),

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
     * Next Quarter
     * 
     */
    @XmlEnumValue("nextQuarter")
    NEXT_QUARTER("nextQuarter"),

    /**
     * This Quarter
     * 
     */
    @XmlEnumValue("thisQuarter")
    THIS_QUARTER("thisQuarter"),

    /**
     * Last Quarter
     * 
     */
    @XmlEnumValue("lastQuarter")
    LAST_QUARTER("lastQuarter"),

    /**
     * Next Year
     * 
     */
    @XmlEnumValue("nextYear")
    NEXT_YEAR("nextYear"),

    /**
     * This Year
     * 
     */
    @XmlEnumValue("thisYear")
    THIS_YEAR("thisYear"),

    /**
     * Last Year
     * 
     */
    @XmlEnumValue("lastYear")
    LAST_YEAR("lastYear"),

    /**
     * Year To Date
     * 
     */
    @XmlEnumValue("yearToDate")
    YEAR_TO_DATE("yearToDate"),

    /**
     *  1st Quarter
     * 
     */
    @XmlEnumValue("Q1")
    Q_1("Q1"),

    /**
     *  2nd Quarter
     * 
     */
    @XmlEnumValue("Q2")
    Q_2("Q2"),

    /**
     *  3rd Quarter
     * 
     */
    @XmlEnumValue("Q3")
    Q_3("Q3"),

    /**
     *  4th Quarter
     * 
     */
    @XmlEnumValue("Q4")
    Q_4("Q4"),

    /**
     *  1st Month
     * 
     */
    @XmlEnumValue("M1")
    M_1("M1"),

    /**
     *  2nd Month
     * 
     */
    @XmlEnumValue("M2")
    M_2("M2"),

    /**
     *  3rd Month
     * 
     */
    @XmlEnumValue("M3")
    M_3("M3"),

    /**
     *  4th Month
     * 
     */
    @XmlEnumValue("M4")
    M_4("M4"),

    /**
     *  5th Month
     * 
     */
    @XmlEnumValue("M5")
    M_5("M5"),

    /**
     *  6th Month
     * 
     */
    @XmlEnumValue("M6")
    M_6("M6"),

    /**
     *  7th Month
     * 
     */
    @XmlEnumValue("M7")
    M_7("M7"),

    /**
     *  8th Month
     * 
     */
    @XmlEnumValue("M8")
    M_8("M8"),

    /**
     *  9th Month
     * 
     */
    @XmlEnumValue("M9")
    M_9("M9"),

    /**
     *  10th Month
     * 
     */
    @XmlEnumValue("M10")
    M_10("M10"),

    /**
     *  11th Month
     * 
     */
    @XmlEnumValue("M11")
    M_11("M11"),

    /**
     *  12th Month
     * 
     */
    @XmlEnumValue("M12")
    M_12("M12");
    private final String value;

    STDynamicFilterType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDynamicFilterType fromValue(String v) {
        for (STDynamicFilterType c: STDynamicFilterType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
