
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextHAlign.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextHAlign">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="justify"/>
 *     &lt;enumeration value="distributed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextHAlign")
@XmlEnum
public enum STTextHAlign {

    @XmlEnumValue("left")
    LEFT("left"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("right")
    RIGHT("right"),
    @XmlEnumValue("justify")
    JUSTIFY("justify"),
    @XmlEnumValue("distributed")
    DISTRIBUTED("distributed");
    private final String value;

    STTextHAlign(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextHAlign fromValue(String v) {
        for (STTextHAlign c: STTextHAlign.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
