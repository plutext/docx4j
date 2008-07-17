
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BlipCompression.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BlipCompression">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="email"/>
 *     &lt;enumeration value="screen"/>
 *     &lt;enumeration value="print"/>
 *     &lt;enumeration value="hqprint"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BlipCompression")
@XmlEnum
public enum STBlipCompression {


    /**
     * Email Compression
     * 
     */
    @XmlEnumValue("email")
    EMAIL("email"),

    /**
     * Screen Viewing
     * 						Compression
     * 
     */
    @XmlEnumValue("screen")
    SCREEN("screen"),

    /**
     * Printing Compression
     * 
     */
    @XmlEnumValue("print")
    PRINT("print"),

    /**
     * High Quality Printing
     * 						Compression
     * 
     */
    @XmlEnumValue("hqprint")
    HQPRINT("hqprint"),

    /**
     * No Compression
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STBlipCompression(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBlipCompression fromValue(String v) {
        for (STBlipCompression c: STBlipCompression.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
