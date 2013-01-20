
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PivotAreaType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PivotAreaType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="data"/>
 *     &lt;enumeration value="all"/>
 *     &lt;enumeration value="origin"/>
 *     &lt;enumeration value="button"/>
 *     &lt;enumeration value="topEnd"/>
 *     &lt;enumeration value="topRight"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PivotAreaType")
@XmlEnum
public enum STPivotAreaType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("data")
    DATA("data"),
    @XmlEnumValue("all")
    ALL("all"),
    @XmlEnumValue("origin")
    ORIGIN("origin"),
    @XmlEnumValue("button")
    BUTTON("button"),
    @XmlEnumValue("topEnd")
    TOP_END("topEnd"),
    @XmlEnumValue("topRight")
    TOP_RIGHT("topRight");
    private final String value;

    STPivotAreaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPivotAreaType fromValue(String v) {
        for (STPivotAreaType c: STPivotAreaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
