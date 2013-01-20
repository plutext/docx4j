
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_VolDepType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VolDepType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="realTimeData"/>
 *     &lt;enumeration value="olapFunctions"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_VolDepType")
@XmlEnum
public enum STVolDepType {

    @XmlEnumValue("realTimeData")
    REAL_TIME_DATA("realTimeData"),
    @XmlEnumValue("olapFunctions")
    OLAP_FUNCTIONS("olapFunctions");
    private final String value;

    STVolDepType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVolDepType fromValue(String v) {
        for (STVolDepType c: STVolDepType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
