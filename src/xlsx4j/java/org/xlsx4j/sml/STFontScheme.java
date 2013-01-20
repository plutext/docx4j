
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FontScheme.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FontScheme">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="major"/>
 *     &lt;enumeration value="minor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FontScheme")
@XmlEnum
public enum STFontScheme {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("major")
    MAJOR("major"),
    @XmlEnumValue("minor")
    MINOR("minor");
    private final String value;

    STFontScheme(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFontScheme fromValue(String v) {
        for (STFontScheme c: STFontScheme.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
