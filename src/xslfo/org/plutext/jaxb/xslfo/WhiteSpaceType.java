
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for white_space_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="white_space_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="pre"/>
 *     &lt;enumeration value="nowrap"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "white_space_Type")
@XmlEnum
public enum WhiteSpaceType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("pre")
    PRE("pre"),
    @XmlEnumValue("nowrap")
    NOWRAP("nowrap"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    WhiteSpaceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WhiteSpaceType fromValue(String v) {
        for (WhiteSpaceType c: WhiteSpaceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
