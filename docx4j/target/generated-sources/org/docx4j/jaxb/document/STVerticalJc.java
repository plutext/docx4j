
package org.docx4j.jaxb.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_VerticalJc.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VerticalJc">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="top"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="both"/>
 *     &lt;enumeration value="bottom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_VerticalJc")
@XmlEnum
public enum STVerticalJc {


    /**
     * Align Top
     * 
     */
    @XmlEnumValue("top")
    TOP("top"),

    /**
     * Align Center
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Vertical Justification
     * 
     */
    @XmlEnumValue("both")
    BOTH("both"),

    /**
     * Align Bottom
     * 
     */
    @XmlEnumValue("bottom")
    BOTTOM("bottom");
    private final String value;

    STVerticalJc(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVerticalJc fromValue(String v) {
        for (STVerticalJc c: STVerticalJc.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
