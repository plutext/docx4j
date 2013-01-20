
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_MdxFunctionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MdxFunctionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="m"/>
 *     &lt;enumeration value="v"/>
 *     &lt;enumeration value="s"/>
 *     &lt;enumeration value="c"/>
 *     &lt;enumeration value="r"/>
 *     &lt;enumeration value="p"/>
 *     &lt;enumeration value="k"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MdxFunctionType")
@XmlEnum
public enum STMdxFunctionType {

    @XmlEnumValue("m")
    M("m"),
    @XmlEnumValue("v")
    V("v"),
    @XmlEnumValue("s")
    S("s"),
    @XmlEnumValue("c")
    C("c"),
    @XmlEnumValue("r")
    R("r"),
    @XmlEnumValue("p")
    P("p"),
    @XmlEnumValue("k")
    K("k");
    private final String value;

    STMdxFunctionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMdxFunctionType fromValue(String v) {
        for (STMdxFunctionType c: STMdxFunctionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
