
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for leader_pattern_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="leader_pattern_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="space"/>
 *     &lt;enumeration value="rule"/>
 *     &lt;enumeration value="dots"/>
 *     &lt;enumeration value="use-content"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "leader_pattern_Type")
@XmlEnum
public enum LeaderPatternType {

    @XmlEnumValue("space")
    SPACE("space"),
    @XmlEnumValue("rule")
    RULE("rule"),
    @XmlEnumValue("dots")
    DOTS("dots"),
    @XmlEnumValue("use-content")
    USE_CONTENT("use-content"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    LeaderPatternType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LeaderPatternType fromValue(String v) {
        for (LeaderPatternType c: LeaderPatternType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
