
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CredMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CredMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="integrated"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="stored"/>
 *     &lt;enumeration value="prompt"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CredMethod")
@XmlEnum
public enum STCredMethod {

    @XmlEnumValue("integrated")
    INTEGRATED("integrated"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("stored")
    STORED("stored"),
    @XmlEnumValue("prompt")
    PROMPT("prompt");
    private final String value;

    STCredMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCredMethod fromValue(String v) {
        for (STCredMethod c: STCredMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
