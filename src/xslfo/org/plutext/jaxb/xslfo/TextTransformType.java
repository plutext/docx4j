
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for text_transform_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="text_transform_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="capitalize"/>
 *     &lt;enumeration value="uppercase"/>
 *     &lt;enumeration value="lowercase"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "text_transform_Type")
@XmlEnum
public enum TextTransformType {

    @XmlEnumValue("capitalize")
    CAPITALIZE("capitalize"),
    @XmlEnumValue("uppercase")
    UPPERCASE("uppercase"),
    @XmlEnumValue("lowercase")
    LOWERCASE("lowercase"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    TextTransformType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TextTransformType fromValue(String v) {
        for (TextTransformType c: TextTransformType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
