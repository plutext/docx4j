
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Visibility.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Visibility">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="visible"/>
 *     &lt;enumeration value="hidden"/>
 *     &lt;enumeration value="veryHidden"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Visibility")
@XmlEnum
public enum STVisibility {

    @XmlEnumValue("visible")
    VISIBLE("visible"),
    @XmlEnumValue("hidden")
    HIDDEN("hidden"),
    @XmlEnumValue("veryHidden")
    VERY_HIDDEN("veryHidden");
    private final String value;

    STVisibility(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVisibility fromValue(String v) {
        for (STVisibility c: STVisibility.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
