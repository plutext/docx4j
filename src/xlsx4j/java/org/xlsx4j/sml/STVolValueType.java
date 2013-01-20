
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_VolValueType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VolValueType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="n"/>
 *     &lt;enumeration value="e"/>
 *     &lt;enumeration value="s"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_VolValueType")
@XmlEnum
public enum STVolValueType {

    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("n")
    N("n"),
    @XmlEnumValue("e")
    E("e"),
    @XmlEnumValue("s")
    S("s");
    private final String value;

    STVolValueType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVolValueType fromValue(String v) {
        for (STVolValueType c: STVolValueType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
