
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for font_stretch_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="font_stretch_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="wider"/>
 *     &lt;enumeration value="narrower"/>
 *     &lt;enumeration value="ultra-condensed"/>
 *     &lt;enumeration value="extra-condensed"/>
 *     &lt;enumeration value="condensed"/>
 *     &lt;enumeration value="semi-condensed"/>
 *     &lt;enumeration value="semi-expanded"/>
 *     &lt;enumeration value="expanded"/>
 *     &lt;enumeration value="extra-expanded"/>
 *     &lt;enumeration value="ultra-expanded"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "font_stretch_Type")
@XmlEnum
public enum FontStretchType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("wider")
    WIDER("wider"),
    @XmlEnumValue("narrower")
    NARROWER("narrower"),
    @XmlEnumValue("ultra-condensed")
    ULTRA_CONDENSED("ultra-condensed"),
    @XmlEnumValue("extra-condensed")
    EXTRA_CONDENSED("extra-condensed"),
    @XmlEnumValue("condensed")
    CONDENSED("condensed"),
    @XmlEnumValue("semi-condensed")
    SEMI_CONDENSED("semi-condensed"),
    @XmlEnumValue("semi-expanded")
    SEMI_EXPANDED("semi-expanded"),
    @XmlEnumValue("expanded")
    EXPANDED("expanded"),
    @XmlEnumValue("extra-expanded")
    EXTRA_EXPANDED("extra-expanded"),
    @XmlEnumValue("ultra-expanded")
    ULTRA_EXPANDED("ultra-expanded"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    FontStretchType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FontStretchType fromValue(String v) {
        for (FontStretchType c: FontStretchType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
