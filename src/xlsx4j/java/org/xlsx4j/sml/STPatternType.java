
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PatternType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PatternType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="mediumGray"/>
 *     &lt;enumeration value="darkGray"/>
 *     &lt;enumeration value="lightGray"/>
 *     &lt;enumeration value="darkHorizontal"/>
 *     &lt;enumeration value="darkVertical"/>
 *     &lt;enumeration value="darkDown"/>
 *     &lt;enumeration value="darkUp"/>
 *     &lt;enumeration value="darkGrid"/>
 *     &lt;enumeration value="darkTrellis"/>
 *     &lt;enumeration value="lightHorizontal"/>
 *     &lt;enumeration value="lightVertical"/>
 *     &lt;enumeration value="lightDown"/>
 *     &lt;enumeration value="lightUp"/>
 *     &lt;enumeration value="lightGrid"/>
 *     &lt;enumeration value="lightTrellis"/>
 *     &lt;enumeration value="gray125"/>
 *     &lt;enumeration value="gray0625"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PatternType")
@XmlEnum
public enum STPatternType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("solid")
    SOLID("solid"),
    @XmlEnumValue("mediumGray")
    MEDIUM_GRAY("mediumGray"),
    @XmlEnumValue("darkGray")
    DARK_GRAY("darkGray"),
    @XmlEnumValue("lightGray")
    LIGHT_GRAY("lightGray"),
    @XmlEnumValue("darkHorizontal")
    DARK_HORIZONTAL("darkHorizontal"),
    @XmlEnumValue("darkVertical")
    DARK_VERTICAL("darkVertical"),
    @XmlEnumValue("darkDown")
    DARK_DOWN("darkDown"),
    @XmlEnumValue("darkUp")
    DARK_UP("darkUp"),
    @XmlEnumValue("darkGrid")
    DARK_GRID("darkGrid"),
    @XmlEnumValue("darkTrellis")
    DARK_TRELLIS("darkTrellis"),
    @XmlEnumValue("lightHorizontal")
    LIGHT_HORIZONTAL("lightHorizontal"),
    @XmlEnumValue("lightVertical")
    LIGHT_VERTICAL("lightVertical"),
    @XmlEnumValue("lightDown")
    LIGHT_DOWN("lightDown"),
    @XmlEnumValue("lightUp")
    LIGHT_UP("lightUp"),
    @XmlEnumValue("lightGrid")
    LIGHT_GRID("lightGrid"),
    @XmlEnumValue("lightTrellis")
    LIGHT_TRELLIS("lightTrellis"),
    @XmlEnumValue("gray125")
    GRAY_125("gray125"),
    @XmlEnumValue("gray0625")
    GRAY_0625("gray0625");
    private final String value;

    STPatternType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPatternType fromValue(String v) {
        for (STPatternType c: STPatternType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
