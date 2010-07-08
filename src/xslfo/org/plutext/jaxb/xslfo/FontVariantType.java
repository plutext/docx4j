
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for font_variant_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="font_variant_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="small-caps"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "font_variant_Type")
@XmlEnum
public enum FontVariantType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("small-caps")
    SMALL_CAPS("small-caps"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    FontVariantType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FontVariantType fromValue(String v) {
        for (FontVariantType c: FontVariantType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
