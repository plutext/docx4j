
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BevelPresetType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BevelPresetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="relaxedInset"/>
 *     &lt;enumeration value="circle"/>
 *     &lt;enumeration value="slope"/>
 *     &lt;enumeration value="cross"/>
 *     &lt;enumeration value="angle"/>
 *     &lt;enumeration value="softRound"/>
 *     &lt;enumeration value="convex"/>
 *     &lt;enumeration value="coolSlant"/>
 *     &lt;enumeration value="divot"/>
 *     &lt;enumeration value="riblet"/>
 *     &lt;enumeration value="hardEdge"/>
 *     &lt;enumeration value="artDeco"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BevelPresetType")
@XmlEnum
public enum STBevelPresetType {


    /**
     * Relaxed Inset
     * 
     */
    @XmlEnumValue("relaxedInset")
    RELAXED_INSET("relaxedInset"),

    /**
     * Circle
     * 
     */
    @XmlEnumValue("circle")
    CIRCLE("circle"),

    /**
     * Slope
     * 
     */
    @XmlEnumValue("slope")
    SLOPE("slope"),

    /**
     * Cross
     * 
     */
    @XmlEnumValue("cross")
    CROSS("cross"),

    /**
     * Angle
     * 
     */
    @XmlEnumValue("angle")
    ANGLE("angle"),

    /**
     * Soft Round
     * 
     */
    @XmlEnumValue("softRound")
    SOFT_ROUND("softRound"),

    /**
     * Convex
     * 
     */
    @XmlEnumValue("convex")
    CONVEX("convex"),

    /**
     * Cool Slant
     * 
     */
    @XmlEnumValue("coolSlant")
    COOL_SLANT("coolSlant"),

    /**
     * Divot
     * 
     */
    @XmlEnumValue("divot")
    DIVOT("divot"),

    /**
     * Riblet
     * 
     */
    @XmlEnumValue("riblet")
    RIBLET("riblet"),

    /**
     * Hard Edge
     * 
     */
    @XmlEnumValue("hardEdge")
    HARD_EDGE("hardEdge"),

    /**
     * Art Deco
     * 
     */
    @XmlEnumValue("artDeco")
    ART_DECO("artDeco");
    private final String value;

    STBevelPresetType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBevelPresetType fromValue(String v) {
        for (STBevelPresetType c: STBevelPresetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
