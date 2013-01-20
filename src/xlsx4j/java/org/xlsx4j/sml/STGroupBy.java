
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_GroupBy.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_GroupBy">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="range"/>
 *     &lt;enumeration value="seconds"/>
 *     &lt;enumeration value="minutes"/>
 *     &lt;enumeration value="hours"/>
 *     &lt;enumeration value="days"/>
 *     &lt;enumeration value="months"/>
 *     &lt;enumeration value="quarters"/>
 *     &lt;enumeration value="years"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_GroupBy")
@XmlEnum
public enum STGroupBy {

    @XmlEnumValue("range")
    RANGE("range"),
    @XmlEnumValue("seconds")
    SECONDS("seconds"),
    @XmlEnumValue("minutes")
    MINUTES("minutes"),
    @XmlEnumValue("hours")
    HOURS("hours"),
    @XmlEnumValue("days")
    DAYS("days"),
    @XmlEnumValue("months")
    MONTHS("months"),
    @XmlEnumValue("quarters")
    QUARTERS("quarters"),
    @XmlEnumValue("years")
    YEARS("years");
    private final String value;

    STGroupBy(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STGroupBy fromValue(String v) {
        for (STGroupBy c: STGroupBy.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
