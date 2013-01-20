
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PrintError.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PrintError">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="displayed"/>
 *     &lt;enumeration value="blank"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="NA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PrintError")
@XmlEnum
public enum STPrintError {

    @XmlEnumValue("displayed")
    DISPLAYED("displayed"),
    @XmlEnumValue("blank")
    BLANK("blank"),
    @XmlEnumValue("dash")
    DASH("dash"),
    NA("NA");
    private final String value;

    STPrintError(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPrintError fromValue(String v) {
        for (STPrintError c: STPrintError.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
