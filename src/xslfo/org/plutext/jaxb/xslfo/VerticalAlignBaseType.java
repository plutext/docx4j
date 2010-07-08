
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for vertical_align_base_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="vertical_align_base_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baseline"/>
 *     &lt;enumeration value="middle"/>
 *     &lt;enumeration value="sub"/>
 *     &lt;enumeration value="super"/>
 *     &lt;enumeration value="text-top"/>
 *     &lt;enumeration value="text-bottom"/>
 *     &lt;enumeration value="top"/>
 *     &lt;enumeration value="bottom"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "vertical_align_base_Type")
@XmlEnum
public enum VerticalAlignBaseType {

    @XmlEnumValue("baseline")
    BASELINE("baseline"),
    @XmlEnumValue("middle")
    MIDDLE("middle"),
    @XmlEnumValue("sub")
    SUB("sub"),
    @XmlEnumValue("super")
    SUPER("super"),
    @XmlEnumValue("text-top")
    TEXT_TOP("text-top"),
    @XmlEnumValue("text-bottom")
    TEXT_BOTTOM("text-bottom"),
    @XmlEnumValue("top")
    TOP("top"),
    @XmlEnumValue("bottom")
    BOTTOM("bottom"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    VerticalAlignBaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VerticalAlignBaseType fromValue(String v) {
        for (VerticalAlignBaseType c: VerticalAlignBaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
