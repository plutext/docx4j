
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PenAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PenAlignment">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ctr"/>
 *     &lt;enumeration value="in"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PenAlignment")
@XmlEnum
public enum STPenAlignment {


    /**
     * Center Alignment
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Inset Alignment
     * 
     */
    @XmlEnumValue("in")
    IN("in");
    private final String value;

    STPenAlignment(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPenAlignment fromValue(String v) {
        for (STPenAlignment c: STPenAlignment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
