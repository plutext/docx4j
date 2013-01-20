
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Pane.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Pane">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="bottomRight"/>
 *     &lt;enumeration value="topRight"/>
 *     &lt;enumeration value="bottomLeft"/>
 *     &lt;enumeration value="topLeft"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Pane")
@XmlEnum
public enum STPane {

    @XmlEnumValue("bottomRight")
    BOTTOM_RIGHT("bottomRight"),
    @XmlEnumValue("topRight")
    TOP_RIGHT("topRight"),
    @XmlEnumValue("bottomLeft")
    BOTTOM_LEFT("bottomLeft"),
    @XmlEnumValue("topLeft")
    TOP_LEFT("topLeft");
    private final String value;

    STPane(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPane fromValue(String v) {
        for (STPane c: STPane.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
