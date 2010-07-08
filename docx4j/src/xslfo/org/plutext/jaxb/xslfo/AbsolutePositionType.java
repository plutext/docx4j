
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for absolute_position_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="absolute_position_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="absolute"/>
 *     &lt;enumeration value="fixed"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "absolute_position_Type")
@XmlEnum
public enum AbsolutePositionType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute"),
    @XmlEnumValue("fixed")
    FIXED("fixed"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    AbsolutePositionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AbsolutePositionType fromValue(String v) {
        for (AbsolutePositionType c: AbsolutePositionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
