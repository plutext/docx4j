
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_GradientType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_GradientType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="linear"/>
 *     &lt;enumeration value="path"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_GradientType")
@XmlEnum
public enum STGradientType {

    @XmlEnumValue("linear")
    LINEAR("linear"),
    @XmlEnumValue("path")
    PATH("path");
    private final String value;

    STGradientType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STGradientType fromValue(String v) {
        for (STGradientType c: STGradientType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
