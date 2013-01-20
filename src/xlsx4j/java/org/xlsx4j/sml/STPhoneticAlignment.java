
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PhoneticAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PhoneticAlignment">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="noControl"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="distributed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PhoneticAlignment")
@XmlEnum
public enum STPhoneticAlignment {

    @XmlEnumValue("noControl")
    NO_CONTROL("noControl"),
    @XmlEnumValue("left")
    LEFT("left"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("distributed")
    DISTRIBUTED("distributed");
    private final String value;

    STPhoneticAlignment(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPhoneticAlignment fromValue(String v) {
        for (STPhoneticAlignment c: STPhoneticAlignment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
