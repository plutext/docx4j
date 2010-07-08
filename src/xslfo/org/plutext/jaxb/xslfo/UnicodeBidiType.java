
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for unicode_bidi_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="unicode_bidi_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="embed"/>
 *     &lt;enumeration value="bidi-override"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "unicode_bidi_Type")
@XmlEnum
public enum UnicodeBidiType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("embed")
    EMBED("embed"),
    @XmlEnumValue("bidi-override")
    BIDI_OVERRIDE("bidi-override"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    UnicodeBidiType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UnicodeBidiType fromValue(String v) {
        for (UnicodeBidiType c: UnicodeBidiType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
