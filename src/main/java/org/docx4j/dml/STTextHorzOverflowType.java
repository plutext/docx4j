
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextHorzOverflowType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextHorzOverflowType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="overflow"/>
 *     &lt;enumeration value="clip"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextHorzOverflowType")
@XmlEnum
public enum STTextHorzOverflowType {


    /**
     * Text Horizontal Overflow Enum ( Overflow
     * 						)
     * 
     */
    @XmlEnumValue("overflow")
    OVERFLOW("overflow"),

    /**
     * Text Horizontal Overflow Enum ( Clip
     * 						)
     * 
     */
    @XmlEnumValue("clip")
    CLIP("clip");
    private final String value;

    STTextHorzOverflowType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextHorzOverflowType fromValue(String v) {
        for (STTextHorzOverflowType c: STTextHorzOverflowType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
