
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

    @XmlEnumValue("today")
    TODAY("today"),
    @XmlEnumValue("yesterday")
    YESTERDAY("yesterday"),
    @XmlEnumValue("tomorrow")
    TOMORROW("tomorrow"),
    @XmlEnumValue("last7Days")
    LAST_7_DAYS("last7Days"),
    @XmlEnumValue("thisMonth")
    THIS_MONTH("thisMonth"),
    @XmlEnumValue("lastMonth")
    LAST_MONTH("lastMonth"),
    @XmlEnumValue("nextMonth")
    NEXT_MONTH("nextMonth"),
    @XmlEnumValue("thisWeek")
    THIS_WEEK("thisWeek"),
    @XmlEnumValue("lastWeek")
    LAST_WEEK("lastWeek"),
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
