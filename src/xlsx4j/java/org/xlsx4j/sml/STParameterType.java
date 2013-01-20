
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ParameterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ParameterType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="prompt"/>
 *     &lt;enumeration value="value"/>
 *     &lt;enumeration value="cell"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ParameterType")
@XmlEnum
public enum STParameterType {

    @XmlEnumValue("prompt")
    PROMPT("prompt"),
    @XmlEnumValue("value")
    VALUE("value"),
    @XmlEnumValue("cell")
    CELL("cell");
    private final String value;

    STParameterType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STParameterType fromValue(String v) {
        for (STParameterType c: STParameterType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
