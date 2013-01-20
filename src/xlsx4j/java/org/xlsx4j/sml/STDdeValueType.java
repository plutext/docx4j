
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DdeValueType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DdeValueType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="nil"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="n"/>
 *     &lt;enumeration value="e"/>
 *     &lt;enumeration value="str"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_DdeValueType")
@XmlEnum
public enum STDdeValueType {

    @XmlEnumValue("nil")
    NIL("nil"),
    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("n")
    N("n"),
    @XmlEnumValue("e")
    E("e"),
    @XmlEnumValue("str")
    STR("str");
    private final String value;

    STDdeValueType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDdeValueType fromValue(String v) {
        for (STDdeValueType c: STDdeValueType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
