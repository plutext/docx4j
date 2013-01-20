
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RefMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RefMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="A1"/>
 *     &lt;enumeration value="R1C1"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RefMode")
@XmlEnum
public enum STRefMode {

    @XmlEnumValue("A1")
    A_1("A1"),
    @XmlEnumValue("R1C1")
    R_1_C_1("R1C1");
    private final String value;

    STRefMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRefMode fromValue(String v) {
        for (STRefMode c: STRefMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
