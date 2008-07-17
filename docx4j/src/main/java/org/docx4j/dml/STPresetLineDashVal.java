
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PresetLineDashVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetLineDashVal">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="dot"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="lgDash"/>
 *     &lt;enumeration value="dashDot"/>
 *     &lt;enumeration value="lgDashDot"/>
 *     &lt;enumeration value="lgDashDotDot"/>
 *     &lt;enumeration value="sysDash"/>
 *     &lt;enumeration value="sysDot"/>
 *     &lt;enumeration value="sysDashDot"/>
 *     &lt;enumeration value="sysDashDotDot"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetLineDashVal")
@XmlEnum
public enum STPresetLineDashVal {


    /**
     * Solid
     * 
     */
    @XmlEnumValue("solid")
    SOLID("solid"),

    /**
     * Dot
     * 
     */
    @XmlEnumValue("dot")
    DOT("dot"),

    /**
     * Dash
     * 
     */
    @XmlEnumValue("dash")
    DASH("dash"),

    /**
     * Large Dash
     * 
     */
    @XmlEnumValue("lgDash")
    LG_DASH("lgDash"),

    /**
     * Dash Dot
     * 
     */
    @XmlEnumValue("dashDot")
    DASH_DOT("dashDot"),

    /**
     * Large Dash Dot
     * 
     */
    @XmlEnumValue("lgDashDot")
    LG_DASH_DOT("lgDashDot"),

    /**
     * Large Dash Dot Dot
     * 
     */
    @XmlEnumValue("lgDashDotDot")
    LG_DASH_DOT_DOT("lgDashDotDot"),

    /**
     * System Dash
     * 
     */
    @XmlEnumValue("sysDash")
    SYS_DASH("sysDash"),

    /**
     * System Dot
     * 
     */
    @XmlEnumValue("sysDot")
    SYS_DOT("sysDot"),

    /**
     * System Dash Dot
     * 
     */
    @XmlEnumValue("sysDashDot")
    SYS_DASH_DOT("sysDashDot"),

    /**
     * System Dash Dot Dot
     * 
     */
    @XmlEnumValue("sysDashDotDot")
    SYS_DASH_DOT_DOT("sysDashDotDot");
    private final String value;

    STPresetLineDashVal(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetLineDashVal fromValue(String v) {
        for (STPresetLineDashVal c: STPresetLineDashVal.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
