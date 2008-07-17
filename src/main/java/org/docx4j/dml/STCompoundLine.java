
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CompoundLine.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CompoundLine">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="sng"/>
 *     &lt;enumeration value="dbl"/>
 *     &lt;enumeration value="thickThin"/>
 *     &lt;enumeration value="thinThick"/>
 *     &lt;enumeration value="tri"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CompoundLine")
@XmlEnum
public enum STCompoundLine {


    /**
     * Single Line
     * 
     */
    @XmlEnumValue("sng")
    SNG("sng"),

    /**
     * Double Lines
     * 
     */
    @XmlEnumValue("dbl")
    DBL("dbl"),

    /**
     * Thick Thin Double Lines
     * 
     */
    @XmlEnumValue("thickThin")
    THICK_THIN("thickThin"),

    /**
     * Thin Thick Double Lines
     * 
     */
    @XmlEnumValue("thinThick")
    THIN_THICK("thinThick"),

    /**
     * Thin Thick Thin Triple
     * 						Lines
     * 
     */
    @XmlEnumValue("tri")
    TRI("tri");
    private final String value;

    STCompoundLine(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCompoundLine fromValue(String v) {
        for (STCompoundLine c: STCompoundLine.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
