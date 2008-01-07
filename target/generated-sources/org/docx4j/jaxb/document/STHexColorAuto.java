
package org.docx4j.jaxb.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_HexColorAuto.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_HexColorAuto">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="auto"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_HexColorAuto")
@XmlEnum
public enum STHexColorAuto {


    /**
     * Automatically Determined
     * 						Color
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto");
    private final String value;

    STHexColorAuto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STHexColorAuto fromValue(String v) {
        for (STHexColorAuto c: STHexColorAuto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
