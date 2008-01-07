
package org.docx4j.jaxb.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PageOrientation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PageOrientation">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="portrait"/>
 *     &lt;enumeration value="landscape"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PageOrientation")
@XmlEnum
public enum STPageOrientation {


    /**
     * Portrait Mode
     * 
     */
    @XmlEnumValue("portrait")
    PORTRAIT("portrait"),

    /**
     * Landscape Mode
     * 
     */
    @XmlEnumValue("landscape")
    LANDSCAPE("landscape");
    private final String value;

    STPageOrientation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPageOrientation fromValue(String v) {
        for (STPageOrientation c: STPageOrientation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
