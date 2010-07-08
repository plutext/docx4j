
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for azimuth_base_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="azimuth_base_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="left-side"/>
 *     &lt;enumeration value="far-left"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center-left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="center-right"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="far-right"/>
 *     &lt;enumeration value="right-side"/>
 *     &lt;enumeration value="behind"/>
 *     &lt;enumeration value="leftwards"/>
 *     &lt;enumeration value="rightwards"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "azimuth_base_Type")
@XmlEnum
public enum AzimuthBaseType {

    @XmlEnumValue("left-side")
    LEFT_SIDE("left-side"),
    @XmlEnumValue("far-left")
    FAR_LEFT("far-left"),
    @XmlEnumValue("left")
    LEFT("left"),
    @XmlEnumValue("center-left")
    CENTER_LEFT("center-left"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("center-right")
    CENTER_RIGHT("center-right"),
    @XmlEnumValue("right")
    RIGHT("right"),
    @XmlEnumValue("far-right")
    FAR_RIGHT("far-right"),
    @XmlEnumValue("right-side")
    RIGHT_SIDE("right-side"),
    @XmlEnumValue("behind")
    BEHIND("behind"),
    @XmlEnumValue("leftwards")
    LEFTWARDS("leftwards"),
    @XmlEnumValue("rightwards")
    RIGHTWARDS("rightwards"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    AzimuthBaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AzimuthBaseType fromValue(String v) {
        for (AzimuthBaseType c: AzimuthBaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
