
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PageOrder.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PageOrder">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="downThenOver"/>
 *     &lt;enumeration value="overThenDown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PageOrder")
@XmlEnum
public enum STPageOrder {

    @XmlEnumValue("downThenOver")
    DOWN_THEN_OVER("downThenOver"),
    @XmlEnumValue("overThenDown")
    OVER_THEN_DOWN("overThenDown");
    private final String value;

    STPageOrder(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPageOrder fromValue(String v) {
        for (STPageOrder c: STPageOrder.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
