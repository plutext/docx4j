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

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("count")
    COUNT("count"),
    @XmlEnumValue("percent")
    PERCENT("percent"),
    @XmlEnumValue("sum")
    SUM("sum"),
    @XmlEnumValue("captionEqual")
    CAPTION_EQUAL("captionEqual"),
    @XmlEnumValue("captionNotEqual")
    CAPTION_NOT_EQUAL("captionNotEqual"),
    @XmlEnumValue("captionBeginsWith")
    CAPTION_BEGINS_WITH("captionBeginsWith"),
    @XmlEnumValue("captionNotBeginsWith")
    CAPTION_NOT_BEGINS_WITH("captionNotBeginsWith"),
    @XmlEnumValue("captionEndsWith")
    CAPTION_ENDS_WITH("captionEndsWith"),
    @XmlEnumValue("captionNotEndsWith")
    CAPTION_NOT_ENDS_WITH("captionNotEndsWith"),
    @XmlEnumValue("captionContains")
    CAPTION_CONTAINS("captionContains"),
    @XmlEnumValue("captionNotContains")
    CAPTION_NOT_CONTAINS("captionNotContains"),
    @XmlEnumValue("captionGreaterThan")
    CAPTION_GREATER_THAN("captionGreaterThan"),
    @XmlEnumValue("captionGreaterThanOrEqual")
    CAPTION_GREATER_THAN_OR_EQUAL("captionGreaterThanOrEqual"),
    @XmlEnumValue("captionLessThan")
    CAPTION_LESS_THAN("captionLessThan"),
    @XmlEnumValue("captionLessThanOrEqual")
    CAPTION_LESS_THAN_OR_EQUAL("captionLessThanOrEqual"),
    @XmlEnumValue("captionBetween")
    CAPTION_BETWEEN("captionBetween"),
    @XmlEnumValue("captionNotBetween")
    CAPTION_NOT_BETWEEN("captionNotBetween"),
    @XmlEnumValue("valueEqual")
    VALUE_EQUAL("valueEqual"),
    @XmlEnumValue("valueNotEqual")
    VALUE_NOT_EQUAL("valueNotEqual"),
    @XmlEnumValue("valueGreaterThan")
    VALUE_GREATER_THAN("valueGreaterThan"),
    @XmlEnumValue("valueGreaterThanOrEqual")
    VALUE_GREATER_THAN_OR_EQUAL("valueGreaterThanOrEqual"),
    @XmlEnumValue("valueLessThan")
    VALUE_LESS_THAN("valueLessThan"),
    @XmlEnumValue("valueLessThanOrEqual")
    VALUE_LESS_THAN_OR_EQUAL("valueLessThanOrEqual"),
    @XmlEnumValue("valueBetween")
    VALUE_BETWEEN("valueBetween"),
    @XmlEnumValue("valueNotBetween")
    VALUE_NOT_BETWEEN("valueNotBetween"),
    @XmlEnumValue("dateEqual")
    DATE_EQUAL("dateEqual"),
    @XmlEnumValue("dateNotEqual")
    DATE_NOT_EQUAL("dateNotEqual"),
    @XmlEnumValue("dateOlderThan")
    DATE_OLDER_THAN("dateOlderThan"),
    @XmlEnumValue("dateOlderThanOrEqual")
    DATE_OLDER_THAN_OR_EQUAL("dateOlderThanOrEqual"),
    @XmlEnumValue("dateNewerThan")
    DATE_NEWER_THAN("dateNewerThan"),
    @XmlEnumValue("dateNewerThanOrEqual")
    DATE_NEWER_THAN_OR_EQUAL("dateNewerThanOrEqual"),
    @XmlEnumValue("dateBetween")
    DATE_BETWEEN("dateBetween"),
    @XmlEnumValue("dateNotBetween")
    DATE_NOT_BETWEEN("dateNotBetween"),
    @XmlEnumValue("tomorrow")
    TOMORROW("tomorrow"),
    @XmlEnumValue("today")
    TODAY("today"),
    @XmlEnumValue("yesterday")
    YESTERDAY("yesterday"),
    @XmlEnumValue("nextWeek")
    NEXT_WEEK("nextWeek"),
    @XmlEnumValue("thisWeek")
    THIS_WEEK("thisWeek"),
    @XmlEnumValue("lastWeek")
    LAST_WEEK("lastWeek"),
    @XmlEnumValue("nextMonth")
    NEXT_MONTH("nextMonth"),
    @XmlEnumValue("thisMonth")
    THIS_MONTH("thisMonth"),
    @XmlEnumValue("lastMonth")
    LAST_MONTH("lastMonth"),
    @XmlEnumValue("nextQuarter")
    NEXT_QUARTER("nextQuarter"),
    @XmlEnumValue("thisQuarter")
    THIS_QUARTER("thisQuarter"),
    @XmlEnumValue("lastQuarter")
    LAST_QUARTER("lastQuarter"),
    @XmlEnumValue("nextYear")
    NEXT_YEAR("nextYear"),
    @XmlEnumValue("thisYear")
    THIS_YEAR("thisYear"),
    @XmlEnumValue("lastYear")
    LAST_YEAR("lastYear"),
    @XmlEnumValue("yearToDate")
    YEAR_TO_DATE("yearToDate"),
    @XmlEnumValue("Q1")
    Q_1("Q1"),
    @XmlEnumValue("Q2")
    Q_2("Q2"),
    @XmlEnumValue("Q3")
    Q_3("Q3"),
    @XmlEnumValue("Q4")
    Q_4("Q4"),
    @XmlEnumValue("M1")
    M_1("M1"),
    @XmlEnumValue("M2")
    M_2("M2"),
    @XmlEnumValue("M3")
    M_3("M3"),
    @XmlEnumValue("M4")
    M_4("M4"),
    @XmlEnumValue("M5")
    M_5("M5"),
    @XmlEnumValue("M6")
    M_6("M6"),
    @XmlEnumValue("M7")
    M_7("M7"),
    @XmlEnumValue("M8")
    M_8("M8"),
    @XmlEnumValue("M9")
    M_9("M9"),
    @XmlEnumValue("M10")
    M_10("M10"),
    @XmlEnumValue("M11")
    M_11("M11"),
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
