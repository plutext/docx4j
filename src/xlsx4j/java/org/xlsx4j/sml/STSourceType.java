
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SourceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SourceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="worksheet"/>
 *     &lt;enumeration value="external"/>
 *     &lt;enumeration value="consolidation"/>
 *     &lt;enumeration value="scenario"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SourceType")
@XmlEnum
public enum STSourceType {

    @XmlEnumValue("worksheet")
    WORKSHEET("worksheet"),
    @XmlEnumValue("external")
    EXTERNAL("external"),
    @XmlEnumValue("consolidation")
    CONSOLIDATION("consolidation"),
    @XmlEnumValue("scenario")
    SCENARIO("scenario");
    private final String value;

    STSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSourceType fromValue(String v) {
        for (STSourceType c: STSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
