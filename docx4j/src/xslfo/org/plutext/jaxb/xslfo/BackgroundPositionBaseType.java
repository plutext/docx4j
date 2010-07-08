
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for background_position_base_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="background_position_base_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="top left"/>
 *     &lt;enumeration value="top center"/>
 *     &lt;enumeration value="top right"/>
 *     &lt;enumeration value="center left"/>
 *     &lt;enumeration value="center center"/>
 *     &lt;enumeration value="center right"/>
 *     &lt;enumeration value="bottom left"/>
 *     &lt;enumeration value="bottom center"/>
 *     &lt;enumeration value="bottom right"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "background_position_base_Type")
@XmlEnum
public enum BackgroundPositionBaseType {

    @XmlEnumValue("top left")
    TOP_LEFT("top left"),
    @XmlEnumValue("top center")
    TOP_CENTER("top center"),
    @XmlEnumValue("top right")
    TOP_RIGHT("top right"),
    @XmlEnumValue("center left")
    CENTER_LEFT("center left"),
    @XmlEnumValue("center center")
    CENTER_CENTER("center center"),
    @XmlEnumValue("center right")
    CENTER_RIGHT("center right"),
    @XmlEnumValue("bottom left")
    BOTTOM_LEFT("bottom left"),
    @XmlEnumValue("bottom center")
    BOTTOM_CENTER("bottom center"),
    @XmlEnumValue("bottom right")
    BOTTOM_RIGHT("bottom right");
    private final String value;

    BackgroundPositionBaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BackgroundPositionBaseType fromValue(String v) {
        for (BackgroundPositionBaseType c: BackgroundPositionBaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
