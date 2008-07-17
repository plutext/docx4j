
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextVerticalType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextVerticalType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="horz"/>
 *     &lt;enumeration value="vert"/>
 *     &lt;enumeration value="vert270"/>
 *     &lt;enumeration value="wordArtVert"/>
 *     &lt;enumeration value="eaVert"/>
 *     &lt;enumeration value="mongolianVert"/>
 *     &lt;enumeration value="wordArtVertRtl"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextVerticalType")
@XmlEnum
public enum STTextVerticalType {


    /**
     * Vertical Text Type Enum ( Horizontal
     * 						)
     * 
     */
    @XmlEnumValue("horz")
    HORZ("horz"),

    /**
     * Vertical Text Type Enum ( Vertical
     * 						)
     * 
     */
    @XmlEnumValue("vert")
    VERT("vert"),

    /**
     * Vertical Text Type Enum ( Vertical 270
     * 						)
     * 
     */
    @XmlEnumValue("vert270")
    VERT_270("vert270"),

    /**
     * Vertical Text Type Enum ( WordArt Vertical
     * 						)
     * 
     */
    @XmlEnumValue("wordArtVert")
    WORD_ART_VERT("wordArtVert"),

    /**
     * Vertical Text Type Enum ( East Asian Vertical
     * 						)
     * 
     */
    @XmlEnumValue("eaVert")
    EA_VERT("eaVert"),

    /**
     * Vertical Text Type Enum ( Mongolian Vertical
     * 						)
     * 
     */
    @XmlEnumValue("mongolianVert")
    MONGOLIAN_VERT("mongolianVert"),

    /**
     * Vertical WordArt Right to
     * 						Left
     * 
     */
    @XmlEnumValue("wordArtVertRtl")
    WORD_ART_VERT_RTL("wordArtVertRtl");
    private final String value;

    STTextVerticalType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextVerticalType fromValue(String v) {
        for (STTextVerticalType c: STTextVerticalType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
