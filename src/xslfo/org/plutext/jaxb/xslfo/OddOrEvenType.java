
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for odd_or_even_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="odd_or_even_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="odd"/>
 *     &lt;enumeration value="even"/>
 *     &lt;enumeration value="any"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "odd_or_even_Type")
@XmlEnum
public enum OddOrEvenType {

    @XmlEnumValue("odd")
    ODD("odd"),
    @XmlEnumValue("even")
    EVEN("even"),
    @XmlEnumValue("any")
    ANY("any"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    OddOrEvenType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OddOrEvenType fromValue(String v) {
        for (OddOrEvenType c: OddOrEvenType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
