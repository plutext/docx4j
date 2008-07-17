
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextStrikeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextStrikeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="noStrike"/>
 *     &lt;enumeration value="sngStrike"/>
 *     &lt;enumeration value="dblStrike"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextStrikeType")
@XmlEnum
public enum STTextStrikeType {


    /**
     * Text Strike Enum ( No Strike
     * 						)
     * 
     */
    @XmlEnumValue("noStrike")
    NO_STRIKE("noStrike"),

    /**
     * Text Strike Enum ( Single Strike
     * 						)
     * 
     */
    @XmlEnumValue("sngStrike")
    SNG_STRIKE("sngStrike"),

    /**
     * Text Strike Enum ( Double Strike
     * 						)
     * 
     */
    @XmlEnumValue("dblStrike")
    DBL_STRIKE("dblStrike");
    private final String value;

    STTextStrikeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextStrikeType fromValue(String v) {
        for (STTextStrikeType c: STTextStrikeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
