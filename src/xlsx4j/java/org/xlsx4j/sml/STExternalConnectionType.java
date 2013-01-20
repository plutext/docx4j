
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ExternalConnectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ExternalConnectionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="general"/>
 *     &lt;enumeration value="text"/>
 *     &lt;enumeration value="MDY"/>
 *     &lt;enumeration value="DMY"/>
 *     &lt;enumeration value="YMD"/>
 *     &lt;enumeration value="MYD"/>
 *     &lt;enumeration value="DYM"/>
 *     &lt;enumeration value="YDM"/>
 *     &lt;enumeration value="skip"/>
 *     &lt;enumeration value="EMD"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ExternalConnectionType")
@XmlEnum
public enum STExternalConnectionType {

    @XmlEnumValue("general")
    GENERAL("general"),
    @XmlEnumValue("text")
    TEXT("text"),
    MDY("MDY"),
    DMY("DMY"),
    YMD("YMD"),
    MYD("MYD"),
    DYM("DYM"),
    YDM("YDM"),
    @XmlEnumValue("skip")
    SKIP("skip"),
    EMD("EMD");
    private final String value;

    STExternalConnectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STExternalConnectionType fromValue(String v) {
        for (STExternalConnectionType c: STExternalConnectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
