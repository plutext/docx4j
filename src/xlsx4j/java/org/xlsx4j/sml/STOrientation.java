
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Orientation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Orientation">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="default"/>
 *     &lt;enumeration value="portrait"/>
 *     &lt;enumeration value="landscape"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Orientation")
@XmlEnum
public enum STOrientation {

    @XmlEnumValue("default")
    DEFAULT("default"),
    @XmlEnumValue("portrait")
    PORTRAIT("portrait"),
    @XmlEnumValue("landscape")
    LANDSCAPE("landscape");
    private final String value;

    STOrientation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STOrientation fromValue(String v) {
        for (STOrientation c: STOrientation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
