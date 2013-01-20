
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_MdxKPIProperty.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MdxKPIProperty">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="v"/>
 *     &lt;enumeration value="g"/>
 *     &lt;enumeration value="s"/>
 *     &lt;enumeration value="t"/>
 *     &lt;enumeration value="w"/>
 *     &lt;enumeration value="m"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_MdxKPIProperty")
@XmlEnum
public enum STMdxKPIProperty {

    @XmlEnumValue("v")
    V("v"),
    @XmlEnumValue("g")
    G("g"),
    @XmlEnumValue("s")
    S("s"),
    @XmlEnumValue("t")
    T("t"),
    @XmlEnumValue("w")
    W("w"),
    @XmlEnumValue("m")
    M("m");
    private final String value;

    STMdxKPIProperty(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMdxKPIProperty fromValue(String v) {
        for (STMdxKPIProperty c: STMdxKPIProperty.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
