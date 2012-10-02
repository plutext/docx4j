
package org.opendope.questions;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for appearanceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="appearanceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="full"/>
 *     &lt;enumeration value="compact"/>
 *     &lt;enumeration value="minimal"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "appearanceType")
@XmlEnum
public enum AppearanceType {

    @XmlEnumValue("full")
    FULL("full"),
    @XmlEnumValue("compact")
    COMPACT("compact"),
    @XmlEnumValue("minimal")
    MINIMAL("minimal");
    private final String value;

    AppearanceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AppearanceType fromValue(String v) {
        for (AppearanceType c: AppearanceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
