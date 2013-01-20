
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SmartTagShow.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SmartTagShow">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="all"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="noIndicator"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SmartTagShow")
@XmlEnum
public enum STSmartTagShow {

    @XmlEnumValue("all")
    ALL("all"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("noIndicator")
    NO_INDICATOR("noIndicator");
    private final String value;

    STSmartTagShow(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSmartTagShow fromValue(String v) {
        for (STSmartTagShow c: STSmartTagShow.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
