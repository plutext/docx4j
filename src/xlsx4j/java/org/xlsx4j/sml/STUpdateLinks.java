
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_UpdateLinks.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_UpdateLinks">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="userSet"/>
 *     &lt;enumeration value="never"/>
 *     &lt;enumeration value="always"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_UpdateLinks")
@XmlEnum
public enum STUpdateLinks {

    @XmlEnumValue("userSet")
    USER_SET("userSet"),
    @XmlEnumValue("never")
    NEVER("never"),
    @XmlEnumValue("always")
    ALWAYS("always");
    private final String value;

    STUpdateLinks(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STUpdateLinks fromValue(String v) {
        for (STUpdateLinks c: STUpdateLinks.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
