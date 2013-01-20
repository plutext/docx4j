
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PaneState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PaneState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="split"/>
 *     &lt;enumeration value="frozen"/>
 *     &lt;enumeration value="frozenSplit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PaneState")
@XmlEnum
public enum STPaneState {

    @XmlEnumValue("split")
    SPLIT("split"),
    @XmlEnumValue("frozen")
    FROZEN("frozen"),
    @XmlEnumValue("frozenSplit")
    FROZEN_SPLIT("frozenSplit");
    private final String value;

    STPaneState(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPaneState fromValue(String v) {
        for (STPaneState c: STPaneState.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
