
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextWrappingType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextWrappingType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="square"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextWrappingType")
@XmlEnum
public enum STTextWrappingType {


    /**
     * Text Wrapping Type Enum ( None
     * 						)
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Text Wrapping Type Enum ( Square
     * 						)
     * 
     */
    @XmlEnumValue("square")
    SQUARE("square");
    private final String value;

    STTextWrappingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextWrappingType fromValue(String v) {
        for (STTextWrappingType c: STTextWrappingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
