
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PhoneticType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PhoneticType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="halfwidthKatakana"/>
 *     &lt;enumeration value="fullwidthKatakana"/>
 *     &lt;enumeration value="Hiragana"/>
 *     &lt;enumeration value="noConversion"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PhoneticType")
@XmlEnum
public enum STPhoneticType {

    @XmlEnumValue("halfwidthKatakana")
    HALFWIDTH_KATAKANA("halfwidthKatakana"),
    @XmlEnumValue("fullwidthKatakana")
    FULLWIDTH_KATAKANA("fullwidthKatakana"),
    @XmlEnumValue("Hiragana")
    HIRAGANA("Hiragana"),
    @XmlEnumValue("noConversion")
    NO_CONVERSION("noConversion");
    private final String value;

    STPhoneticType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPhoneticType fromValue(String v) {
        for (STPhoneticType c: STPhoneticType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
