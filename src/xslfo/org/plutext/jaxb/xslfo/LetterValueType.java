
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for letter_value_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="letter_value_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="alphabetic"/>
 *     &lt;enumeration value="traditional"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "letter_value_Type")
@XmlEnum
public enum LetterValueType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("alphabetic")
    ALPHABETIC("alphabetic"),
    @XmlEnumValue("traditional")
    TRADITIONAL("traditional");
    private final String value;

    LetterValueType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LetterValueType fromValue(String v) {
        for (LetterValueType c: LetterValueType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
