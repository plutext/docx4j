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
 * <p>Java class for ST_PivotFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PivotFilterType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="count"/>
 *     &lt;enumeration value="percent"/>
 *     &lt;enumeration value="sum"/>
 *     &lt;enumeration value="captionEqual"/>
 *     &lt;enumeration value="captionNotEqual"/>
 *     &lt;enumeration value="captionBeginsWith"/>
 *     &lt;enumeration value="captionNotBeginsWith"/>
 *     &lt;enumeration value="captionEndsWith"/>
 *     &lt;enumeration value="captionNotEndsWith"/>
 *     &lt;enumeration value="captionContains"/>
 *     &lt;enumeration value="captionNotContains"/>
 *     &lt;enumeration value="captionGreaterThan"/>
 *     &lt;enumeration value="captionGreaterThanOrEqual"/>
 *     &lt;enumeration value="captionLessThan"/>
 *     &lt;enumeration value="captionLessThanOrEqual"/>
 *     &lt;enumeration value="captionBetween"/>
 *     &lt;enumeration value="captionNotBetween"/>
 *     &lt;enumeration value="valueEqual"/>
 *     &lt;enumeration value="valueNotEqual"/>
 *     &lt;enumeration value="valueGreaterThan"/>
 *     &lt;enumeration value="valueGreaterThanOrEqual"/>
 *     &lt;enumeration value="valueLessThan"/>
 *     &lt;enumeration value="valueLessThanOrEqual"/>
 *     &lt;enumeration value="valueBetween"/>
 *     &lt;enumeration value="valueNotBetween"/>
 *     &lt;enumeration value="dateEqual"/>
 *     &lt;enumeration value="dateNotEqual"/>
 *     &lt;enumeration value="dateOlderThan"/>
 *     &lt;enumeration value="dateOlderThanOrEqual"/>
 *     &lt;enumeration value="dateNewerThan"/>
 *     &lt;enumeration value="dateNewerThanOrEqual"/>
 *     &lt;enumeration value="dateBetween"/>
 *     &lt;enumeration value="dateNotBetween"/>
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
@XmlType(name = "ST_PivotFilterType")
@XmlEnum
public enum STPivotFilterType {


    /**
     * Unknown
     * 
     */
    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),

    /**
     * Count
     * 
     */
    @XmlEnumValue("count")
    COUNT("count"),

    /**
     * Percent
     * 
     */
    @XmlEnumValue("percent")
    PERCENT("percent"),

    /**
     * Sum
     * 
     */
    @XmlEnumValue("sum")
    SUM("sum"),

    /**
     * Caption Equals
     * 
     */
    @XmlEnumValue("captionEqual")
    CAPTION_EQUAL("captionEqual"),

    /**
     * Caption Not Equal
     * 
     */
    @XmlEnumValue("captionNotEqual")
    CAPTION_NOT_EQUAL("captionNotEqual"),

    /**
     * Caption Begins With
     * 
     */
    @XmlEnumValue("captionBeginsWith")
    CAPTION_BEGINS_WITH("captionBeginsWith"),

    /**
     * Caption Does Not Begin With
     * 
     */
    @XmlEnumValue("captionNotBeginsWith")
    CAPTION_NOT_BEGINS_WITH("captionNotBeginsWith"),

    /**
     * Caption Ends With
     * 
     */
    @XmlEnumValue("captionEndsWith")
    CAPTION_ENDS_WITH("captionEndsWith"),

    /**
     * Caption Does Not End With
     * 
     */
    @XmlEnumValue("captionNotEndsWith")
    CAPTION_NOT_ENDS_WITH("captionNotEndsWith"),

    /**
     * Caption Contains
     * 
     */
    @XmlEnumValue("captionContains")
    CAPTION_CONTAINS("captionContains"),

    /**
     * Caption Does Not Contain
     * 
     */
    @XmlEnumValue("captionNotContains")
    CAPTION_NOT_CONTAINS("captionNotContains"),

    /**
     * Caption Is Greater Than
     * 
     */
    @XmlEnumValue("captionGreaterThan")
    CAPTION_GREATER_THAN("captionGreaterThan"),

    /**
     * Caption Is Greater Than Or Equal To
     * 
     */
    @XmlEnumValue("captionGreaterThanOrEqual")
    CAPTION_GREATER_THAN_OR_EQUAL("captionGreaterThanOrEqual"),

    /**
     * Caption Is Less Than
     * 
     */
    @XmlEnumValue("captionLessThan")
    CAPTION_LESS_THAN("captionLessThan"),

    /**
     * Caption Is Less Than Or Equal To
     * 
     */
    @XmlEnumValue("captionLessThanOrEqual")
    CAPTION_LESS_THAN_OR_EQUAL("captionLessThanOrEqual"),

    /**
     * Caption Is Between
     * 
     */
    @XmlEnumValue("captionBetween")
    CAPTION_BETWEEN("captionBetween"),

    /**
     * Caption Is Not Between
     * 
     */
    @XmlEnumValue("captionNotBetween")
    CAPTION_NOT_BETWEEN("captionNotBetween"),

    /**
     * Value Equal
     * 
     */
    @XmlEnumValue("valueEqual")
    VALUE_EQUAL("valueEqual"),

    /**
     * Value Not Equal
     * 
     */
    @XmlEnumValue("valueNotEqual")
    VALUE_NOT_EQUAL("valueNotEqual"),

    /**
     * Value Greater Than
     * 
     */
    @XmlEnumValue("valueGreaterThan")
    VALUE_GREATER_THAN("valueGreaterThan"),

    /**
     * Value Greater Than Or Equal To
     * 
     */
    @XmlEnumValue("valueGreaterThanOrEqual")
    VALUE_GREATER_THAN_OR_EQUAL("valueGreaterThanOrEqual"),

    /**
     * Value Less Than
     * 
     */
    @XmlEnumValue("valueLessThan")
    VALUE_LESS_THAN("valueLessThan"),

    /**
     * Value Less Than Or Equal To
     * 
     */
    @XmlEnumValue("valueLessThanOrEqual")
    VALUE_LESS_THAN_OR_EQUAL("valueLessThanOrEqual"),

    /**
     * Value Between
     * 
     */
    @XmlEnumValue("valueBetween")
    VALUE_BETWEEN("valueBetween"),

    /**
     * Value Not Between
     * 
     */
    @XmlEnumValue("valueNotBetween")
    VALUE_NOT_BETWEEN("valueNotBetween"),

    /**
     * Date Equals
     * 
     */
    @XmlEnumValue("dateEqual")
    DATE_EQUAL("dateEqual"),

    /**
     * Date Does Not Equal
     * 
     */
    @XmlEnumValue("dateNotEqual")
    DATE_NOT_EQUAL("dateNotEqual"),

    /**
     * Date Older Than
     * 
     */
    @XmlEnumValue("dateOlderThan")
    DATE_OLDER_THAN("dateOlderThan"),

    /**
     * Date Older Than Or Equal
     * 
     */
    @XmlEnumValue("dateOlderThanOrEqual")
    DATE_OLDER_THAN_OR_EQUAL("dateOlderThanOrEqual"),

    /**
     * Date Newer Than
     * 
     */
    @XmlEnumValue("dateNewerThan")
    DATE_NEWER_THAN("dateNewerThan"),

    /**
     * Date Newer Than or Equal To
     * 
     */
    @XmlEnumValue("dateNewerThanOrEqual")
    DATE_NEWER_THAN_OR_EQUAL("dateNewerThanOrEqual"),

    /**
     * Date Between
     * 
     */
    @XmlEnumValue("dateBetween")
    DATE_BETWEEN("dateBetween"),

    /**
     * Date Not Between
     * 
     */
    @XmlEnumValue("dateNotBetween")
    DATE_NOT_BETWEEN("dateNotBetween"),

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
     * Year-To-Date
     * 
     */
    @XmlEnumValue("yearToDate")
    YEAR_TO_DATE("yearToDate"),

    /**
     * First Quarter
     * 
     */
    @XmlEnumValue("Q1")
    Q_1("Q1"),

    /**
     * Second Quarter
     * 
     */
    @XmlEnumValue("Q2")
    Q_2("Q2"),

    /**
     * Third Quarter
     * 
     */
    @XmlEnumValue("Q3")
    Q_3("Q3"),

    /**
     * Fourth Quarter
     * 
     */
    @XmlEnumValue("Q4")
    Q_4("Q4"),

    /**
     * January
     * 
     */
    @XmlEnumValue("M1")
    M_1("M1"),

    /**
     * Dates in February
     * 
     */
    @XmlEnumValue("M2")
    M_2("M2"),

    /**
     * Dates in March
     * 
     */
    @XmlEnumValue("M3")
    M_3("M3"),

    /**
     * Dates in April
     * 
     */
    @XmlEnumValue("M4")
    M_4("M4"),

    /**
     * Dates in May
     * 
     */
    @XmlEnumValue("M5")
    M_5("M5"),

    /**
     * Dates in June
     * 
     */
    @XmlEnumValue("M6")
    M_6("M6"),

    /**
     * Dates in July
     * 
     */
    @XmlEnumValue("M7")
    M_7("M7"),

    /**
     * Dates in August
     * 
     */
    @XmlEnumValue("M8")
    M_8("M8"),

    /**
     * Dates in September
     * 
     */
    @XmlEnumValue("M9")
    M_9("M9"),

    /**
     * Dates in October
     * 
     */
    @XmlEnumValue("M10")
    M_10("M10"),

    /**
     * Dates in November
     * 
     */
    @XmlEnumValue("M11")
    M_11("M11"),

    /**
     * Dates in December
     * 
     */
    @XmlEnumValue("M12")
    M_12("M12");
    private final String value;

    STPivotFilterType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPivotFilterType fromValue(String v) {
        for (STPivotFilterType c: STPivotFilterType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
