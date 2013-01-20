
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DateTimeGrouping.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DateTimeGrouping">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="year"/>
 *     &lt;enumeration value="month"/>
 *     &lt;enumeration value="day"/>
 *     &lt;enumeration value="hour"/>
 *     &lt;enumeration value="minute"/>
 *     &lt;enumeration value="second"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DateTimeGrouping")
@XmlEnum
public enum STDateTimeGrouping {

    @XmlEnumValue("year")
    YEAR("year"),
    @XmlEnumValue("month")
    MONTH("month"),
    @XmlEnumValue("day")
    DAY("day"),
    @XmlEnumValue("hour")
    HOUR("hour"),
    @XmlEnumValue("minute")
    MINUTE("minute"),
    @XmlEnumValue("second")
    SECOND("second");
    private final String value;

    STDateTimeGrouping(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDateTimeGrouping fromValue(String v) {
        for (STDateTimeGrouping c: STDateTimeGrouping.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
