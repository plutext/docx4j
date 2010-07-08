
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for speak_header_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="speak_header_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="once"/>
 *     &lt;enumeration value="always"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "speak_header_Type")
@XmlEnum
public enum SpeakHeaderType {

    @XmlEnumValue("once")
    ONCE("once"),
    @XmlEnumValue("always")
    ALWAYS("always"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    SpeakHeaderType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SpeakHeaderType fromValue(String v) {
        for (SpeakHeaderType c: SpeakHeaderType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
